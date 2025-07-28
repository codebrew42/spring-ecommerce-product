package ecommerce.service

import ecommerce.model.Member
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.Date
import javax.crypto.SecretKey

@Service
class TokenService(
    @Value("\${jwt.secret}") private val secretKey: String,
    @Value("\${jwt.expiration}") private val expiration: Long,
) {
    private val key: SecretKey = Keys.hmacShaKeyFor(secretKey.toByteArray())

    // generate JWT token for authenticated user
    fun generateToken(member: Member): String {
        val now = Date()
        val expiryDate = Date(now.time + expiration)

        // start building JWT
        return Jwts.builder()
            // "sub": "123" set user ID as subject
            .subject(member.id.toString())
            // "email": ...
            .claim("email", member.email)
            .claim("role", member.role)
            .claim("name", member.name)
            // "iat":
            .issuedAt(now)
            // "exp":
            .expiration(expiryDate)
            // creates digital signiture using HMAC-SHA256 alg.
            .signWith(key)
            // convert to string
            .compact()
    }

    // validate JWT token and extract claims, ret null if invalid/expired
    fun validateToken(token: String): Claims? {
        return try {
            // create parser
            Jwts.parser()
                // set secret key
                .verifyWith(key)
                // build parser
                .build()
                // parse token: check valid signature/expiration/format
                .parseSignedClaims(token)
                // get claims object with all the data
                .payload
        } catch (e: Exception) {
            // Token invalid
            null
        }
    }
    /* if valid, client get

    Claims object containing:
    {
        "sub": "123,
        "email": ""
        "role":
        "name":
        "iat":
        "exp":
    }
     */

    // extract user ID from valid token
    fun extractMemberId(token: String): Long? {
        // validate token
        val claims = validateToken(token)
        // extract and convert subject to Long
        return claims?.subject?.toLongOrNull()
    }
}
