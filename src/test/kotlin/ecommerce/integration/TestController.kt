package ecommerce.integration

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class TestController {
    @GetMapping("/admin")
    fun adminEndpoint() = "Admin access granted"

    @GetMapping("/api/cart/items")
    fun cartEndpoint() = "Cart access granted"
}
