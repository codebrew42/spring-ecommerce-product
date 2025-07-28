package ecommerce.service

import ecommerce.dto.member.RegisterRequest
import ecommerce.model.Member
import ecommerce.repository.MemberRepository
import org.springframework.stereotype.Service

@Service
class MemberService(
    private val memberRepository: MemberRepository,
    private val passwordService: PasswordService,
    private val tokenService: TokenService,
) {
    fun register(request: RegisterRequest): String {
        // check if same email exists : existsByEmail
        if (memberRepository.existsByEmail(request.email)) {
            throw IllegalArgumentException("Email already exists")
        }
        // hash password
        val hashedPassword = passwordService.hashPassword(request.password)
        // create Member obj -> save to db
        val member =
            Member(
                0L,
                request.email,
                hashedPassword,
                request.name,
                "USER",
            )
        val savedMember = memberRepository.save(member)
        // generate token
        return tokenService.generateToken(savedMember)
    }

    fun authenticate(
        email: String,
        password: String,
    ): String {
        // Find member by email
        val member =
            memberRepository.findByEmail(email)
                ?: throw IllegalArgumentException("Invalid email or password")

        // Verify password
        if (!passwordService.verifyPassword(password, member.password)) {
            throw IllegalArgumentException("Invalid email or password")
        }

        // Generate and return token
        return tokenService.generateToken(member)
    }
}
