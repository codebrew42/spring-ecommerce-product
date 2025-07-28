package ecommerce.model

data class Member(
    // auto-incremented primary key by database
    val id: Long = 0L,
    val email: String,
    // hashed (never plain)
    val password: String,
    val name: String,
    // or "ADMIN"
    val role: String = "USER",
)
