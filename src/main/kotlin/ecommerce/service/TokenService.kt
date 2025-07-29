package ecommerce.service

import ecommerce.model.Member
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.Date
import javax.crypto.SecretKey

/**
 * Service for JWT token operations
 * Implements Step 2-2 requirement: JWT token generation and validation
 * Uses JJWT library for secure token creation and parsing
 * Configured via application.properties for secret key and expiration time
 */
@Service
class TokenService(
    @Value("\${jwt.secret}") private val secretKey: String,
    @Value("\${jwt.expiration}") private val expiration: Long,
) {
    private val key: SecretKey = Keys.hmacShaKeyFor(secretKey.toByteArray())

    /**
     * Generates JWT token containing member information
     * Uses member ID as subject for token identification
     * Includes custom claims for email, role, and name for authorization
     * Sets issued at and expiration dates for token lifecycle management
     * Signs with HMAC-SHA256 using configured secret key
     */
    fun generateToken(member: Member): String {
        val now = Date()
        val expiryDate = Date(now.time + expiration)

        return Jwts.builder()
            .subject(member.id.toString())
            .claim("email", member.email)
            .claim("role", member.role)
            .claim("name", member.name)
            .issuedAt(now)
            .expiration(expiryDate)
            .signWith(key)
            .compact()
    }

    /**
     * Validates JWT token and extracts claims
     * Parses token using same secret key used for signing
     * Returns Claims object with subject and custom claims if valid
     * Catches all exceptions (expired, invalid signature, malformed) and returns null
     * Used by authentication interceptors and argument resolvers
     */
    fun validateToken(token: String): Claims? {
        return try {
            Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .payload
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Extracts member ID from JWT token
     * Uses validateToken() to ensure token is valid before extraction
     * Converts subject claim (stored as String) to Long for database lookups
     * Returns null if token is invalid or subject cannot be parsed as Long
     * Commonly used by authentication mechanisms to identify the current user
     */
    fun extractMemberId(token: String): Long? {
        val claims = validateToken(token)
        return claims?.subject?.toLongOrNull()
    }
}
