package ecommerce.repository

import ecommerce.model.Cart
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Repository
import java.sql.Timestamp
import java.time.LocalDateTime

/**
 * Data access layer for Cart entity using Spring JDBC
 * Implements Step 2-3 requirement: cart data persistence and analytics
 * Handles cart item storage, retrieval, updates, and statistical queries
 * Works with cart table linking members to products with quantities and timestamps
 */
@Repository
class CartRepository(private val jdbc: JdbcTemplate) {
    /**
     * RowMapper for converting SQL ResultSet rows to Cart objects
     * Maps database columns (id, member_id, product_id, quantity, added_at) to Cart data class
     * Handles timestamp conversion from SQL Timestamp to LocalDateTime
     * Used across all cart query operations for consistent object construction
     */
    private val cartRowMapper =
        RowMapper<Cart> { rs, _ ->
            Cart(
                id = rs.getLong("id"),
                memberId = rs.getLong("member_id"),
                productId = rs.getLong("product_id"),
                quantity = rs.getInt("quantity"),
                addedAt = rs.getTimestamp("added_at")?.toLocalDateTime(),
            )
        }

    /**
     * Inserts a new cart item into the database
     * Creates record linking member to product with quantity and timestamp
     * Uses current timestamp if addedAt is null in input Cart
     * Converts LocalDateTime to SQL Timestamp for database storage
     * Returns updated Cart object with confirmed addedAt timestamp
     */
    fun save(cart: Cart): Cart {
        val sql =
            """
            INSERT INTO cart (member_id, product_id, quantity, added_at)
            VALUES (?, ?, ?, ?)
            """.trimIndent()

        val addedAt = cart.addedAt ?: LocalDateTime.now()

        jdbc.update(
            sql,
            cart.memberId,
            cart.productId,
            cart.quantity,
            Timestamp.valueOf(addedAt),
        )
        return cart.copy(addedAt = addedAt)
    }

    /**
     * Retrieves all cart items for a specific user
     * Uses member_id to filter cart entries for authenticated user
     * Returns list of Cart objects ordered by database insertion order
     * Returns empty list if user has no items in cart
     */
    fun findByUserId(userId: Long): List<Cart> {
        val sql = "SELECT * FROM cart WHERE member_id = ?"
        return jdbc.query(sql, cartRowMapper, userId)
    }

    /**
     * Finds a specific cart item by user and product combination
     * Used to check if product already exists in user's cart before adding
     * Returns Cart object if combination exists, null otherwise
     * Prevents duplicate entries and enables quantity updates
     */
    fun findByUserIdAndProductId(
        userId: Long,
        productId: Long,
    ): Cart? {
        val sql = "SELECT * FROM cart WHERE member_id = ? AND product_id = ?"
        val results = jdbc.query(sql, cartRowMapper, userId, productId)
        return results.firstOrNull()
    }

    /**
     * Updates an existing cart item's quantity and timestamp
     * Uses member_id and product_id as composite key for updates
     * Updates both quantity and added_at timestamp for analytics
     * Uses current timestamp if addedAt is null in input Cart
     * Returns updated Cart object with confirmed addedAt timestamp
     */
    fun update(cart: Cart): Cart {
        val sql =
            """
            UPDATE cart 
            SET quantity = ?, added_at = ?
            WHERE member_id = ? AND product_id = ?
            """.trimIndent()

        val addedAt = cart.addedAt ?: LocalDateTime.now()

        jdbc.update(
            sql,
            cart.quantity,
            Timestamp.valueOf(addedAt),
            cart.memberId,
            cart.productId,
        )
        return cart.copy(addedAt = addedAt)
    }

    /**
     * Removes a specific product from user's cart
     * Deletes cart entry matching both member_id and product_id
     * Silently succeeds if no matching entry exists (idempotent)
     * Used by CartController.removeFromCart() endpoint
     */
    fun deleteByUserIdAndProductId(
        userId: Long,
        productId: Long,
    ) {
        val sql = "DELETE FROM cart WHERE member_id = ? AND product_id = ?"
        jdbc.update(sql, userId, productId)
    }

    /**
     * Removes all items from a user's shopping cart
     * Deletes all cart entries for the specified member_id
     * Used for "Clear Cart" functionality
     * Silently succeeds if user has no cart items
     */
    fun deleteByUserId(userId: Long) {
        val sql = "DELETE FROM cart WHERE member_id = ?"
        jdbc.update(sql, userId)
    }

    /**
     * Retrieves all cart items from all users
     * Used for administrative purposes and analytics
     * Returns complete list of all cart entries in database
     * May be performance-intensive on large datasets
     */
    fun findAll(): List<Cart> {
        val sql = "SELECT * FROM cart"
        return jdbc.query(sql, cartRowMapper)
    }

    /**
     * Retrieves top 5 most added products to cart in the last 30 days
     * Implements Step 2-4 requirement: admin statistics for cart analytics
     * Uses SUM(quantity) to count total additions, not just entries
     * Joins cart and products tables to get product names
     * Orders by total count DESC, then by most recent addition for ties
     * Returns Map with productName, addedCount, and mostRecentAdded fields
     */
    fun findTop5MostAddedProductsLast30Days(): List<Map<String, Any?>> {
        val sql =
            """
            SELECT 
                p.name as productName,
                SUM(c.quantity) as addedCount,
                MAX(c.added_at) as mostRecentAdded
            FROM cart c
            JOIN products p ON c.product_id = p.id
            WHERE c.added_at >= DATEADD('DAY', -30, CURRENT_TIMESTAMP)
            GROUP BY c.product_id, p.name
            ORDER BY SUM(c.quantity) DESC, MAX(c.added_at) DESC
            LIMIT 5
            """.trimIndent()

        return try {
            jdbc.query(sql) { rs, _ ->
                mapOf(
                    "productName" to rs.getString("productName"),
                    "addedCount" to rs.getInt("addedCount"),
                    "mostRecentAdded" to rs.getTimestamp("mostRecentAdded")?.toLocalDateTime(),
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Retrieves members who added items to cart in the last 7 days
     * Implements Step 2-4 requirement: admin statistics for active user analytics
     * Uses EXISTS subquery to check for cart activity in past 7 days
     * Returns DISTINCT members to avoid duplicates for multiple cart additions
     * Returns Map with memberId, memberName, and memberEmail fields
     * Returns empty list on SQL exceptions for graceful error handling
     */
    fun findMembersActiveInLast7Days(): List<Map<String, Any?>> {
        val sql =
            """
            SELECT DISTINCT 
                m.id as memberId,
                m.name as memberName,
                m.email as memberEmail
            FROM members m
            WHERE EXISTS (
                SELECT 1 FROM cart c 
                WHERE c.member_id = m.id 
                AND c.added_at >= DATEADD('DAY', -7, CURRENT_TIMESTAMP)
            )
            """.trimIndent()

        return try {
            jdbc.query(sql) { rs, _ ->
                mapOf(
                    "memberId" to rs.getLong("memberId"),
                    "memberName" to rs.getString("memberName"),
                    "memberEmail" to rs.getString("memberEmail"),
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
