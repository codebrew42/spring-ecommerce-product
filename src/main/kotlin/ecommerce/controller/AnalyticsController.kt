package ecommerce.controller

import ecommerce.repository.CartRepository
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * REST API controller for administrative analytics and statistics
 * Implements Step 2-4 requirement: admin statistics based on cart data
 * All endpoints prefixed with "/admin/analytics" for logical grouping
 * Protected by AuthInterceptor to ensure only ADMIN role can access
 */
@RequestMapping("/admin/analytics")
@RestController
class AnalyticsController(
    private val cartRepository: CartRepository,
) {
    /**
     * Retrieves top 5 most added products to cart in last 30 days
     * Implements GET /admin/analytics/top-products endpoint
     * Returns JSON array with productName, addedCount, and mostRecentAdded
     * Ordered by total quantity added (DESC), then by most recent addition
     * Used by administrators to understand popular products and trends
     */
    @GetMapping("/top-products")
    fun getTopProducts(): List<Map<String, Any?>> {
        return cartRepository.findTop5MostAddedProductsLast30Days()
    }

    /**
     * Retrieves members who added items to cart in last 7 days
     * Implements GET /admin/analytics/active-users endpoint
     * Returns JSON array with memberId, memberName, and memberEmail
     * Each member appears only once regardless of cart activity frequency
     * Used by administrators to identify recently active users for engagement
     */
    @GetMapping("/active-users")
    fun getActiveUsers(): List<Map<String, Any?>> {
        return cartRepository.findMembersActiveInLast7Days()
    }
}
