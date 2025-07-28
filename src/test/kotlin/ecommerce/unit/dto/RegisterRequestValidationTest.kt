package ecommerce.unit.dto

import ecommerce.dto.member.RegisterRequest
import jakarta.validation.Validation
import jakarta.validation.Validator
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class RegisterRequestValidationTest {
    private lateinit var validator: Validator

    @BeforeEach
    fun setUp() {
        validator = Validation.buildDefaultValidatorFactory().validator
    }

    @Test
    fun `should pass validation for valid request`() {
        val request = RegisterRequest("test@email.com", "password123", "Test User")
        val violations = validator.validate(request)

        assertThat(violations).isEmpty()
    }

    @Test
    fun `should fail validation for empty email`() {
        val request = RegisterRequest("", "password123", "Test User")
        val violations = validator.validate(request)

        assertThat(violations).hasSize(1)
        assertThat(violations.first().message).isEqualTo("Email must not be blank")
    }

    @Test
    fun `should fail validation for invalid email format`() {
        val request = RegisterRequest("invalid-email", "password123", "Test User")
        val violations = validator.validate(request)

        assertThat(violations).hasSize(1)
        assertThat(violations.first().message).isEqualTo("Invalid email format")
    }

    @Test
    fun `should fail validation for empty password`() {
        val request = RegisterRequest("test@email.com", "", "Test User")
        val violations = validator.validate(request)

        assertThat(violations).hasSize(2) // NotBlank + Size
        assertThat(violations.map { it.message }).containsExactlyInAnyOrder(
            "Password must not be blank",
            "Password must be at least 5 characters",
        )
    }

    @Test
    fun `should fail validation for short password`() {
        val request = RegisterRequest("test@email.com", "123", "Test User")
        val violations = validator.validate(request)

        assertThat(violations).hasSize(1)
        assertThat(violations.first().message).isEqualTo("Password must be at least 5 characters")
    }

    @Test
    fun `should fail validation for empty name`() {
        val request = RegisterRequest("test@email.com", "password123", "")
        val violations = validator.validate(request)

        assertThat(violations).hasSize(2) // NotBlank + Size
        assertThat(violations.map { it.message }).containsExactlyInAnyOrder(
            "Name must not be blank",
            "Name between 2 and 50 characters",
        )
    }

    @Test
    fun `should fail validation for short name`() {
        val request = RegisterRequest("test@email.com", "password123", "A")
        val violations = validator.validate(request)

        assertThat(violations).hasSize(1)
        assertThat(violations.first().message).isEqualTo("Name between 2 and 50 characters")
    }

    @Test
    fun `should fail validation for long name`() {
        val longName = "A".repeat(31)
        val request = RegisterRequest("test@email.com", "password123", longName)
        val violations = validator.validate(request)

        assertThat(violations).hasSize(1)
        assertThat(violations.first().message).isEqualTo("Name between 2 and 50 characters")
    }
}
