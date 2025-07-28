package ecommerce.interceptor

import ecommerce.service.TokenService
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor

@Component
class AuthInterceptor(private val tokenService: TokenService) : HandlerInterceptor {
    companion object {
        const val USER_ID_ATTRIBUTE = "userId"
        const val USER_ROLE_ATTRIBUTE = "userRole"
        const val USER_EMAIL_ATTRIBUTE = "userEmail"
        const val USER_NAME_ATTRIBUTE = "userName"
        private const val BEARER_PREFIX = "Bearer "
        private const val ADMIN_PATH_PREFIX = "/admin/"
        private const val ADMIN_ROLE = "ADMIN"
    }

    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
    ): Boolean {
        return try {
            // 1. Extract and validate authorization header
            val token =
                extractToken(request) ?: return sendUnauthorizedResponse(
                    response, "Missing or invalid Authorization header",
                )

            // 2. Validate JWT token
            val claims =
                tokenService.validateToken(token) ?: return sendUnauthorizedResponse(
                    response, "Invalid or expired token",
                )

            // 3. Extract user info from token
            val userId =
                extractUserId(claims) ?: return sendUnauthorizedResponse(
                    response, "Invalid token payload",
                )

            // 4. Store user info in request attributes
            storeUserAttributes(request, claims, userId)

            // 5. Check admin access if needed
            if (requiresAdminAccess(request) && !hasAdminRole(claims)) {
                return sendForbiddenResponse(response, "Admin access required")
            }

            true // Continue to controller
        } catch (e: Exception) {
            sendUnauthorizedResponse(response, "Authentication error: ${e.message}")
        }
    }

    private fun extractToken(request: HttpServletRequest): String? {
        val authHeader = request.getHeader("Authorization")
        return if (authHeader?.startsWith(BEARER_PREFIX) == true) {
            authHeader.substring(BEARER_PREFIX.length)
        } else {
            null
        }
    }

    private fun extractUserId(claims: io.jsonwebtoken.Claims): Long? {
        return claims.subject?.toLongOrNull()
    }

    private fun storeUserAttributes(
        request: HttpServletRequest,
        claims: io.jsonwebtoken.Claims,
        userId: Long,
    ) {
        request.setAttribute(USER_ID_ATTRIBUTE, userId)
        request.setAttribute(USER_ROLE_ATTRIBUTE, claims["role"] as? String)
        request.setAttribute(USER_EMAIL_ATTRIBUTE, claims["email"] as? String)
        request.setAttribute(USER_NAME_ATTRIBUTE, claims["name"] as? String)
    }

    private fun requiresAdminAccess(request: HttpServletRequest): Boolean {
        return request.requestURI.startsWith(ADMIN_PATH_PREFIX)
    }

    private fun hasAdminRole(claims: io.jsonwebtoken.Claims): Boolean {
        return claims["role"] as? String == ADMIN_ROLE
    }

    private fun sendUnauthorizedResponse(
        response: HttpServletResponse,
        message: String,
    ): Boolean {
        response.status = HttpServletResponse.SC_UNAUTHORIZED
        response.contentType = "application/json"
        response.writer.write("""{"error": "Unauthorized", "message": "$message"}""")
        return false
    }

    private fun sendForbiddenResponse(
        response: HttpServletResponse,
        message: String,
    ): Boolean {
        response.status = HttpServletResponse.SC_FORBIDDEN
        response.contentType = "application/json"
        response.writer.write("""{"error": "Forbidden", "message": "$message"}""")
        return false
    }
}
