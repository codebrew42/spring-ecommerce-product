package ecommerce.controller

import ecommerce.dto.product.ProductPatchRequest
import ecommerce.dto.product.ProductRequest
import ecommerce.model.Product
import ecommerce.repository.ProductRepository
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.net.URI

/**
 * REST API controller for product management operations
 * Implements Step 1-1 requirement: HTTP API for CRUD operations with JSON format
 * Uses @RestController annotation for automatic JSON serialization/deserialization
 * All endpoints prefixed with "/api/products" following REST conventions
 */
@RequestMapping("/api/products")
@RestController
class ProductController(private val productRepository: ProductRepository) {
    /**
     * Retrieves all products from the database
     * Implements GET /api/products endpoint returning JSON array of products
     * Uses Spring JDBC through ProductRepository.findAll() for database access
     * Automatically serializes Product objects to JSON format via Jackson
     */
    @GetMapping()
    fun getProducts(): List<Product> = productRepository.findAll()

    /**
     * Retrieves a specific product by its ID
     * Implements GET /api/products/{id} endpoint for single product access
     * Uses @PathVariable to extract ID from URL path parameter
     * Returns 404 via GlobalExceptionAdvice if product not found
     */
    @GetMapping("/{id}")
    fun getProductById(
        @PathVariable id: Long,
    ): Product = productRepository.findById(id)

    /**
     * Creates a new product in the database
     * Implements POST /api/products endpoint for product creation
     * Uses @Valid annotation to trigger Step 2-1 validation rules:
     * - Product name: max 15 chars, specific special chars allowed, unique
     * - Product price: must be > 0
     * - Product imageUrl: must start with http:// or https://
     * Returns 201 Created with Location header pointing to new resource
     * Automatically generates ID via H2 database auto-increment
     */
    @PostMapping()
    fun createProduct(
        @Valid @RequestBody productRequest: ProductRequest,
    ): ResponseEntity<Product> {
        val product =
            Product(
                name = productRequest.name,
                price = productRequest.price,
                imageUrl = productRequest.imageUrl,
            )
        val saved = productRepository.save(product)
        return ResponseEntity.created(URI.create("/api/products/${saved.id}")).body(saved)
    }

    /**
     * Completely replaces an existing product with new data
     * Implements PUT /api/products/{id} endpoint for full product update
     * Requires all fields in ProductRequest (name, price, imageUrl)
     * Uses same validation rules as POST endpoint (@Valid annotation)
     * Creates new Product instance with provided ID and updates via repository
     * Returns 404 if product doesn't exist, handled by GlobalExceptionAdvice
     */
    @PutMapping("/{id}")
    fun updateProduct(
        @Valid @RequestBody productRequest: ProductRequest,
        @PathVariable id: Long,
    ): Product {
        val product =
            Product(
                id = id,
                name = productRequest.name,
                price = productRequest.price,
                imageUrl = productRequest.imageUrl,
            )
        return productRepository.update(id, product)
    }

    /**
     * Partially updates an existing product with only provided fields
     * Implements PATCH /api/products/{id} endpoint for selective updates
     * Uses ProductPatchRequest DTO with nullable fields (name?, price?, imageUrl?)
     * Only updates fields that are not null in the request
     * Validates provided fields using same rules as POST/PUT operations
     * Repository handles checking existing product and merging changes
     */
    @PatchMapping("/{id}")
    fun updateProductPartially(
        @Valid @RequestBody productPatchRequest: ProductPatchRequest,
        @PathVariable id: Long,
    ): Product {
        return productRepository.patch(id, productPatchRequest)
    }

    /**
     * Deletes a product from the database by ID
     * Implements DELETE /api/products/{id} endpoint for product removal
     * Returns 204 No Content on successful deletion (even if product didn't exist)
     * Uses ResponseEntity.noContent() to explicitly set HTTP status
     * Repository handles the actual deletion via JDBC DELETE statement
     */
    @DeleteMapping("/{id}")
    fun deleteProductById(
        @PathVariable id: Long,
    ): ResponseEntity<Unit> {
        productRepository.delete(id)
        return ResponseEntity.noContent().build()
    }
}
