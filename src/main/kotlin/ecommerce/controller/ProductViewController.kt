package ecommerce.controller

import ecommerce.repository.ProductRepository
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping

/**
 * Web controller for serving HTML pages with Thymeleaf templates
 * Implements Step 1-2 requirement: admin interface for product management
 * Uses @Controller (not @RestController) to return view names instead of JSON
 * Serves server-side rendered HTML pages for traditional web interface
 */
@Controller
@RequestMapping
class ProductViewController(private val productRepository: ProductRepository) {
    /**
     * Displays the main product management page with all products
     * Maps to root path "/" and renders Thymeleaf template "product-list.html"
     * Loads all products from database and passes to template via Model attribute
     * Template includes JavaScript for AJAX operations using the REST API endpoints
     * Supports both traditional form submission and asynchronous operations
     */
    @GetMapping
    fun showProducts(model: Model): String {
        model.addAttribute("products", productRepository.findAll())
        return "product-list"
    }
}
