package ecommerce.dto.member

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class RegisterRequest(
    @field:Email(message = "Invalid email format")
    @field:NotBlank(message = "Email must not be blank")
    val email: String,
    // still plain - will be hashed by server
    @field:Size(min = 5, message = "Password must be at least 5 characters")
    @field:NotBlank(message = "Password must not be blank")
    val password: String,
    @field:Size(min = 2, max = 30, message = "Name between 2 and 50 characters")
    @field:NotBlank(message = "Name must not be blank")
    val name: String,
)
