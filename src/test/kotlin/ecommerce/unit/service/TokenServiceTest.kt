package ecommerce.unit.service

import ecommerce.model.Member
import ecommerce.service.TokenService
import org.assertj.core.api.Assertions.assertThat
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TokenServiceTest {
    private val tokenService =
        TokenService(
            secretKey = "dGVzdC1zZWNyZXQtdGhhdC1pcy1hdC1sZWFzdC0zMi1ieXRlcy1sb25n",
            expiration = 3600000,
        )
    private val member = Member(1, "g@gmail.com", "123", "jieun")

    @Test
    fun `generateToken should return valid JWT token`() {
        val token = tokenService.generateToken(member)

        println("token: " + token)
        assertNotNull(token)
        assertTrue(token.isNotEmpty())
        assertEquals(2, token.count { it == '.' })
    }

    @Test
    fun `generated token should be decodable and contain correct claims`() {
        val token = tokenService.generateToken(member)
        val claims = tokenService.validateToken(token)

        assertNotNull(claims)
        assertThat(claims.subject).isEqualTo(member.id.toString())
        assertThat(claims["email"]).isEqualTo(member.email)
        assertThat(claims["role"]).isEqualTo(member.role)
        assertThat(claims["name"]).isEqualTo(member.name)
    }

    @Test
    fun `extractMemberId should return correct member ID`() {
        val token = tokenService.generateToken(member)
        val extractedId = tokenService.extractMemberId(token)

        assertThat(extractedId).isEqualTo(member.id)
    }
}
