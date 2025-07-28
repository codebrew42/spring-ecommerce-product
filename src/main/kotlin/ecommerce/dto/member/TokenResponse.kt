package ecommerce.dto.member

data class TokenResponse(
    // actual JWT tk
    val token: String,
    // fixed
    val type: String = "Bearer",
    // 24 hours
    val expiresIn: Long = 86400,
)
