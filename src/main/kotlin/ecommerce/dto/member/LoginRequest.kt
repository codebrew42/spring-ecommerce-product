package ecommerce.dto.member

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

data class LoginRequest(
    @field:Email(message = "Email format is invalid")
    @field:NotBlank(message = "Email must not be blank")
    val email: String,
    // still plain - server will hash
    @field:NotBlank(message = "Email must not be blank")
    val password: String,
)
