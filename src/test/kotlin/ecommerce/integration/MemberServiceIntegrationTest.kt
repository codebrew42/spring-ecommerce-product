package ecommerce.integration

import ecommerce.dto.member.RegisterRequest
import ecommerce.repository.MemberRepository
import ecommerce.service.MemberService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import kotlin.test.assertNotNull

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Transactional
class MemberServiceIntegrationTest {
    @Autowired private lateinit var memberService: MemberService

    @Autowired private lateinit var memberRepository: MemberRepository

    @Test
    fun `should return generated token - valid register request`() {
        val request = RegisterRequest("test@email.com", "password123", "Test User")
        val token = memberService.register(request)

        assertNotNull(token)
        assertThat(token).isNotEmpty()
        assertThat(token.count { it == '.' }).isEqualTo(2)
    }

    @Test
    fun `should hash password and authenticate correctly`() {
        // register
        val registerToken = memberService.register(RegisterRequest("jj@gmail.com", "validPw", "jj"))
        // verify member was saved with hashed pw
        val member = memberRepository.findByEmail("jj@gmail.com")

        assertThat(registerToken).isNotBlank()
        assertThat(member!!.password).isNotEqualTo("validPw")
        assertThat(member.password).contains(":")

        // login with same plain pw should work
        val loginToken = memberService.authenticate("jj@gmail.com", "validPw")
        assertThat(loginToken).isNotBlank()
        assertThat(loginToken.count { it == '.' }).isEqualTo(2)
    }
}
