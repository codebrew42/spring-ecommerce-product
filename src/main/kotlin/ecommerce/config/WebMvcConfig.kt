package ecommerce.config

import ecommerce.interceptor.AuthInterceptor
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

/* configure which endpoints need authentication
        config specific paths require auth
 */
@Configuration
class WebMvcConfig(private val authInterceptor: AuthInterceptor) : WebMvcConfigurer {
    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(authInterceptor)
            // TODO: check
            .addPathPatterns("/api/cart/**") // Cart operations require authentication
            .addPathPatterns("/admin/**") // Admin endpoints require authentication
            .excludePathPatterns("/api/members/register") // exclue - public ; don't require auth
            .excludePathPatterns("/api/members/login")
            .excludePathPatterns("/api/products/**")
    }
}
