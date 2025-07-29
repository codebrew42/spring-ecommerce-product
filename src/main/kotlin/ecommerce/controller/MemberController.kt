package ecommerce.controller

import ecommerce.dto.member.LoginRequest
import ecommerce.dto.member.RegisterRequest
import ecommerce.dto.member.TokenResponse
import ecommerce.service.MemberService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * REST API controller for member authentication operations
 * Implements Step 2-2 requirement: registration and login endpoints
 * Handles user account creation, authentication, and JWT token issuance
 * All endpoints return JSON responses with token for API access
 */
@RequestMapping("/api/members")
@RestController
class MemberController(private val memberService: MemberService) {
    /**
     * Registers a new member and returns JWT token
     * Implements POST /api/members/register endpoint
     * Uses @Valid to trigger validation on RegisterRequest fields
     * Returns 201 Created status with TokenResponse containing JWT
     * Allows immediate API access after successful registration
     */
    @PostMapping("/register")
    fun register(
        @Valid @RequestBody registerRequest: RegisterRequest,
    ): ResponseEntity<TokenResponse> {
        val token = memberService.register(registerRequest)
        val tokenResponse = TokenResponse(token = token)
        return ResponseEntity.status(HttpStatus.CREATED).body(tokenResponse)
    }

    /**
     * Authenticates existing member and returns JWT token
     * Implements POST /api/members/login endpoint
     * Validates credentials and returns 200 OK with TokenResponse
     * Returns 403 Forbidden for invalid credentials via GlobalExceptionAdvice
     * Token can be used in Authorization header for protected endpoints
     */
    @PostMapping("/login")
    fun login(
        @Valid @RequestBody loginRequest: LoginRequest,
    ): ResponseEntity<TokenResponse> {
        val token = memberService.authenticate(loginRequest.email, loginRequest.password)
        val tokenResponse = TokenResponse(token = token)
        return ResponseEntity.ok(tokenResponse)
    }
}
