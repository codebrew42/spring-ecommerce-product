package ecommerce.repository

import ecommerce.dto.product.ProductPatchRequest
import ecommerce.exception.NotFoundException
import ecommerce.model.Product
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.support.GeneratedKeyHolder
import org.springframework.jdbc.support.KeyHolder
import org.springframework.stereotype.Repository
import java.sql.ResultSet

/**
 * Data access layer for Product entity using Spring JDBC
 * Implements Step 1-3 requirement: H2 database storage instead of in-memory collections
 * Uses JdbcTemplate for SQL operations and manual result set mapping
 * Handles database operations with proper exception handling and key generation
 */
@Repository
class ProductRepository(private val jdbc: JdbcTemplate) {
    /**
     * RowMapper for converting SQL ResultSet rows to Product objects
     * Maps database columns (id, name, price, imageUrl) to Product data class
     * Used across all query operations for consistent object construction
     * Implements Spring JDBC's functional interface for result set processing
     */
    private val productRowMapper =
        RowMapper<Product> { rs: ResultSet, _ ->
            Product(
                rs.getLong("id"),
                rs.getString("name"),
                rs.getDouble("price"),
                rs.getString("imageUrl"),
            )
        }

    /**
     * Retrieves all products from the database
     * Executes SELECT * query on products table
     * Uses productRowMapper to convert each row to Product object
     * Returns empty list if no products exist (never null)
     */
    fun findAll(): List<Product> {
        val sql = "SELECT * FROM products"
        return jdbc.query(sql, productRowMapper)
    }

    /**
     * Finds a single product by its ID
     * Uses parameterized query to prevent SQL injection
     * Handles Spring JDBC's EmptyResultDataAccessException for missing records
     * Throws custom NotFoundException with descriptive message for consistent error handling
     * Used by controller endpoints and other repository operations
     */
    fun findById(id: Long): Product {
        val sql = "SELECT * from products where ID = ?"
        return try {
            jdbc.queryForObject(sql, productRowMapper, id) ?: throw NotFoundException("Product with Id: $id. Not found.")
        } catch (_: org.springframework.dao.EmptyResultDataAccessException) {
            throw NotFoundException("Product with Id: $id. Not found.")
        }
    }

    /**
     * Inserts a new product into the database and returns it with generated ID
     * Uses GeneratedKeyHolder to capture auto-generated ID from H2 database
     * Executes INSERT statement with parameterized values for security
     * Returns copy of input product with the database-generated ID
     * ID generation handled by H2's AUTO_INCREMENT column definition
     */
    fun save(product: Product): Product {
        val sql = "insert into products (name, price, imageUrl) values (?, ?, ?)"
        val keyHolder: KeyHolder = GeneratedKeyHolder()
        jdbc.update({
            it.prepareStatement(sql, arrayOf("id")).apply {
                setString(1, product.name)
                setDouble(2, product.price)
                setString(3, product.imageUrl)
            }
        }, keyHolder)
        return product.copy(id = keyHolder.key!!.toLong())
    }

    /**
     * Completely replaces an existing product's data
     * Executes UPDATE statement setting all fields (name, price, imageUrl)
     * Checks rowsAffected to ensure product existed and was updated
     * Throws NotFoundException if no rows were affected (product not found)
     * Returns updated product with confirmed ID
     */
    fun update(
        id: Long,
        product: Product,
    ): Product {
        val sql = "UPDATE products SET name = ?, price = ?, imageUrl = ? WHERE id = ?"
        val rowsAffected = jdbc.update(sql, product.name, product.price, product.imageUrl, id)
        if (rowsAffected == 0) {
            throw NotFoundException("Product with Id: $id. Not found.")
        }
        return product.copy(id = id)
    }

    /**
     * Removes a product from the database by ID
     * Executes DELETE statement with parameterized ID value
     * Checks rowsAffected to verify product existed and was deleted
     * Throws NotFoundException if no rows were affected
     * Note: Controller returns 204 No Content regardless of existence
     */
    fun delete(id: Long) {
        val sql = "DELETE FROM products WHERE ID = ?"
        val rowsAffected = jdbc.update(sql, id)
        if (rowsAffected == 0) {
            throw NotFoundException("Product with Id: $id. Not found.")
        }
    }

    /**
     * Performs partial update of a product with only specified fields
     * First retrieves existing product to get current values
     * Uses null coalescing (?:) to keep existing values for unspecified fields
     * Constructs new Product with merged values and executes full UPDATE
     * More efficient than dynamic SQL generation for partial updates
     */
    fun patch(
        id: Long,
        patchProduct: ProductPatchRequest,
    ): Product {
        val existing = findById(id)
        val updatedName = patchProduct.name ?: existing.name
        val updatedPrice = patchProduct.price ?: existing.price
        val updatedImageUrl = patchProduct.imageUrl ?: existing.imageUrl

        val updatedProduct = Product(id, updatedName, updatedPrice, updatedImageUrl)
        val sql = "UPDATE products SET name = ?, price = ?, imageUrl = ? WHERE id = ?"
        jdbc.update(sql, updatedName, updatedPrice, updatedImageUrl, id)

        return updatedProduct
    }

    /**
     * Checks if a product with the given name already exists
     * Used by Step 2-1 validation to enforce unique product names
     * Executes COUNT query which is more efficient than SELECT for existence checks
     * Returns true if any products have the specified name, false otherwise
     * Supports custom @UniqueProductName validation annotation
     */
    fun existsByName(name: String): Boolean {
        val sql = "SELECT COUNT(*) FROM products WHERE name = ?"
        val count = jdbc.queryForObject(sql, Int::class.java, name) ?: 0
        return count > 0
    }
}
