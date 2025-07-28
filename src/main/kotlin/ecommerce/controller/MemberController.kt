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

@RequestMapping("/api/members")
@RestController
class MemberController(private val memberService: MemberService) {
    @PostMapping("/register")
    fun register(
        @Valid @RequestBody registerRequest: RegisterRequest,
    ): ResponseEntity<TokenResponse> {
        val token = memberService.register(registerRequest)
        val tokenResponse = TokenResponse(token = token)
        return ResponseEntity.status(HttpStatus.CREATED).body(tokenResponse)
    }

    @PostMapping("/login")
    fun login(
        @Valid @RequestBody loginRequest: LoginRequest,
    ): ResponseEntity<TokenResponse> {
        val token = memberService.authenticate(loginRequest.email, loginRequest.password)
        val tokenResponse = TokenResponse(token = token)
        return ResponseEntity.ok(tokenResponse)
    }
}
