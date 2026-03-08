package com.example.team_management_backend.Auth

import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth")
class AuthController(private val service: AuthService) {

    @PostMapping("/register")
    fun register(@Valid @RequestBody req: RegisterRequest): ResponseEntity<RegisterResponse> =
        ResponseEntity.ok(service.register(req))

    @PostMapping("/login")
    fun login(
        @Valid @RequestBody req: LoginRequest,
        res: HttpServletResponse
    ): ResponseEntity<LoginResponse> {
        val (loginResponse, rawRefreshToken) = service.login(req)
        setRefreshTokenCookie(res, rawRefreshToken)
        return ResponseEntity.ok(loginResponse)
    }

    @PostMapping("/refresh")
    fun refresh(
        request: HttpServletRequest,
        res: HttpServletResponse
    ): ResponseEntity<RefreshResponse> {
        val rawRefreshToken = extractRefreshTokenCookie(request)
            ?: return ResponseEntity.status(401).build()

        val response = service.refresh(rawRefreshToken)
        return ResponseEntity.ok(response)
    }

    @PostMapping("/logout")
    fun logout(
        @AuthenticationPrincipal principal: AuthPrincipal?,
        res: HttpServletResponse
    ): ResponseEntity<Map<String, String>> {
        principal?.let { service.logout(it.id) }
        clearRefreshTokenCookie(res)
        return ResponseEntity.ok(mapOf("message" to "Logged out successfully"))
    }

    private fun setRefreshTokenCookie(res: HttpServletResponse, token: String) {
        val cookie = Cookie("refresh_token", token).apply {
            isHttpOnly = true
            secure = false
            path = "/api/auth"
            maxAge = 60 * 60 * 24 * 180
        }
        res.addCookie(cookie)

        val cookieHeader = res.getHeaders("Set-Cookie")
            .lastOrNull()
            ?.let { "$it; SameSite=Lax" }
        if (cookieHeader != null) {
            res.setHeader("Set-Cookie", cookieHeader)
        }
    }

    private fun clearRefreshTokenCookie(res: HttpServletResponse) {
        val cookie = Cookie("refresh_token", "").apply {
            isHttpOnly = true
            secure = false
            path = "/api/auth"
            maxAge = 0
        }
        res.addCookie(cookie)
    }

    private fun extractRefreshTokenCookie(req: HttpServletRequest): String? =
        req.cookies?.firstOrNull { it.name == "refresh_token" }?.value
}