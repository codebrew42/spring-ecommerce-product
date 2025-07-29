package ecommerce.controller

import ecommerce.dto.cart.AddToCartRequest
import ecommerce.dto.cart.UpdateQuantityRequest
import ecommerce.model.Cart
import ecommerce.repository.CartRepository
import ecommerce.repository.ProductRepository
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.net.URI
import java.time.LocalDateTime

/**
 * REST API controller for shopping cart operations
 * Implements Step 2-3 requirement: user cart functionality with JWT authentication
 * Requires Authorization header with Bearer token for all endpoints
 * Uses HttpServletRequest to extract userId from interceptor-set attribute
 */
@RequestMapping("/api/cart")
@RestController
class CartController(
    private val cartRepository: CartRepository,
    private val productRepository: ProductRepository,
) {
    /**
     * Retrieves all items in the authenticated user's shopping cart
     * Implements GET /api/cart/items endpoint
     * Extracts userId from request attribute set by authentication interceptor
     * Returns list of Cart objects with product IDs, quantities, and timestamps
     * Returns empty list if user has no cart items
     */
    @GetMapping("/items")
    fun getCartItems(request: HttpServletRequest): List<Cart> {
        val userId = request.getAttribute("userId") as Long
        return cartRepository.findByUserId(userId)
    }

    /**
     * Adds a product to the authenticated user's shopping cart
     * Implements POST /api/cart/items endpoint for Step 2-3 requirement
     * Validates product exists before adding to prevent orphaned cart items
     * Handles duplicate products by incrementing existing quantity
     * Creates new cart entry if product not already in user's cart
     * Returns 201 Created with Location header and Cart object
     */
    @PostMapping("/items")
    fun addToCart(
        @Valid @RequestBody addToCartRequest: AddToCartRequest,
        request: HttpServletRequest,
    ): ResponseEntity<Cart> {
        val userId = request.getAttribute("userId") as Long

        productRepository.findById(addToCartRequest.productId)

        val existingCart = cartRepository.findByUserIdAndProductId(userId, addToCartRequest.productId)

        val cart =
            if (existingCart != null) {
                val updatedCart = existingCart.copy(quantity = existingCart.quantity + addToCartRequest.quantity)
                cartRepository.update(updatedCart)
            } else {
                val newCart =
                    Cart(
                        memberId = userId,
                        productId = addToCartRequest.productId,
                        quantity = addToCartRequest.quantity,
                        addedAt = LocalDateTime.now(),
                    )
                cartRepository.save(newCart)
            }

        return ResponseEntity.created(URI.create("/api/cart/items")).body(cart)
    }

    /**
     * Updates the quantity of a specific product in user's cart
     * Implements PUT /api/cart/items/{productId} endpoint
     * Validates product exists and is in user's cart before updating
     * Replaces existing quantity with new value (not increment)
     * Throws IllegalArgumentException if product not in user's cart
     * Returns updated Cart object with new quantity
     */
    @PutMapping("/items/{productId}")
    fun updateQuantity(
        @PathVariable productId: Long,
        @Valid @RequestBody updateRequest: UpdateQuantityRequest,
        request: HttpServletRequest,
    ): Cart {
        val userId = request.getAttribute("userId") as Long

        productRepository.findById(productId)

        val existingCart =
            cartRepository.findByUserIdAndProductId(userId, productId)
                ?: throw IllegalArgumentException("Item not found in cart")

        val updatedCart = existingCart.copy(quantity = updateRequest.quantity)
        return cartRepository.update(updatedCart)
    }

    /**
     * Removes a specific product from user's shopping cart
     * Implements DELETE /api/cart/items/{productId} endpoint for Step 2-3 requirement
     * Deletes cart entry matching both userId and productId
     * Returns 204 No Content regardless of whether item existed
     * Silently succeeds if product was not in cart (idempotent operation)
     */
    @DeleteMapping("/items/{productId}")
    fun removeFromCart(
        @PathVariable productId: Long,
        request: HttpServletRequest,
    ): ResponseEntity<Unit> {
        val userId = request.getAttribute("userId") as Long
        cartRepository.deleteByUserIdAndProductId(userId, productId)
        return ResponseEntity.noContent().build()
    }

    /**
     * Removes all items from user's shopping cart
     * Implements DELETE /api/cart/items endpoint (bulk deletion)
     * Deletes all cart entries for the authenticated user
     * Returns 204 No Content regardless of cart contents
     * Useful for implementing "Clear Cart" functionality
     */
    @DeleteMapping("/items")
    fun clearCart(request: HttpServletRequest): ResponseEntity<Unit> {
        val userId = request.getAttribute("userId") as Long
        cartRepository.deleteByUserId(userId)
        return ResponseEntity.noContent().build()
    }
}
