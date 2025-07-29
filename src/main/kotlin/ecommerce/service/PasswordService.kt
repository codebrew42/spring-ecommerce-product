package ecommerce.service

import org.springframework.stereotype.Service
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64

/**
 * Service for secure password hashing and verification
 * Implements Step 2-2 requirement: secure password storage
 * Uses SHA-256 with random salt to prevent rainbow table attacks
 * Stores salt and hash together in colon-separated format
 */
@Service
class PasswordService {
    private val secureRandom = SecureRandom()

    /**
     * Hashes password with random salt for secure storage
     * Generates 16-byte random salt using SecureRandom
     * Combines password with Base64-encoded salt before hashing
     * Uses SHA-256 algorithm for cryptographic hashing
     * Returns "salt:hash" format for database storage
     */
    fun hashPassword(password: String): String {
        val salt = ByteArray(16)
        secureRandom.nextBytes(salt)
        val saltedPassword = password + Base64.getEncoder().encodeToString(salt)
        val hashedBytes = MessageDigest.getInstance("SHA-256").digest(saltedPassword.toByteArray())
        return Base64.getEncoder().encodeToString(salt) + ":" + Base64.getEncoder().encodeToString(hashedBytes)
    }

    /**
     * Verifies password against stored hash
     * Parses stored hash to extract salt and expected hash
     * Recreates salted password using same process as hashPassword()
     * Compares computed hash with stored hash using constant-time comparison
     * Returns false for malformed stored hash or mismatched passwords
     */
    fun verifyPassword(
        password: String,
        storedHash: String,
    ): Boolean {
        val parts = storedHash.split(":")
        if (parts.size != 2) return false

        val salt = parts[0]
        val expectedHash = parts[1]
        val saltedPassword = password + salt
        val hashedBytes = MessageDigest.getInstance("SHA-256").digest(saltedPassword.toByteArray())
        val actualHash = Base64.getEncoder().encodeToString(hashedBytes)

        return actualHash == expectedHash
    }
}
