package ecommerce.service

import ecommerce.dto.member.RegisterRequest
import ecommerce.model.Member
import ecommerce.repository.MemberRepository
import org.springframework.stereotype.Service

/**
 * Service layer for member management operations
 * Implements Step 2-2 requirement: user registration and authentication
 * Orchestrates password hashing, JWT token generation, and database operations
 * Handles business logic between controllers and repositories
 */
@Service
class MemberService(
    private val memberRepository: MemberRepository,
    private val passwordService: PasswordService,
    private val tokenService: TokenService,
) {
    /**
     * Registers a new member and returns JWT token
     * Checks for email uniqueness to prevent duplicate accounts
     * Uses PasswordService to hash password with salt for security
     * Creates Member with auto-generated ID and saves to database
     * Returns JWT token immediately after registration for seamless login
     */
    fun register(request: RegisterRequest): String {
        if (memberRepository.existsByEmail(request.email)) {
            throw IllegalArgumentException("Email already exists")
        }
        val hashedPassword = passwordService.hashPassword(request.password)
        val member =
            Member(
                0L,
                request.email,
                hashedPassword,
                request.name,
                request.role,
            )
        val savedMember = memberRepository.save(member)
        return tokenService.generateToken(savedMember)
    }

    /**
     * Authenticates existing member and returns JWT token
     * Finds member by email address from database
     * Uses PasswordService to verify provided password against stored hash
     * Returns consistent error message for security (timing attack prevention)
     * Generates new JWT token on successful authentication for API access
     */
    fun authenticate(
        email: String,
        password: String,
    ): String {
        val member =
            memberRepository.findByEmail(email)
                ?: throw IllegalArgumentException("Invalid email or password")

        if (!passwordService.verifyPassword(password, member.password)) {
            throw IllegalArgumentException("Invalid email or password")
        }

        return tokenService.generateToken(member)
    }
}
