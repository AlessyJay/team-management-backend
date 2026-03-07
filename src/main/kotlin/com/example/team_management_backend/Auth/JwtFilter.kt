package com.example.team_management_backend.Auth

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtFilter(private val jwtProvider: JwtProvider) : OncePerRequestFilter() {
    override fun doFilterInternal(
        req: HttpServletRequest,
        res: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val token = extractToken(req)

        if (token != null && jwtProvider.validateToken(token)) {
            val userId = jwtProvider.getUserId(token)
            val email = jwtProvider.getEmail(token)

            val auth = UsernamePasswordAuthenticationToken(
                AuthPrincipal(userId, email),
                null,
                listOf(SimpleGrantedAuthority("ROLE_USER"))
            ).also { it.details = WebAuthenticationDetailsSource().buildDetails(req) }

            SecurityContextHolder.getContext().authentication = auth
        }

        filterChain.doFilter(req, res)
    }

    private fun extractToken(req: HttpServletRequest): String? = req.getHeader("Authorization")
        ?.takeIf { it.startsWith("Bearer ") }
        ?.substring(7)
}