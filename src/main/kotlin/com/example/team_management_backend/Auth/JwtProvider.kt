package com.example.team_management_backend.Auth

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.security.MessageDigest
import java.util.*
import javax.crypto.SecretKey

@Component
class JwtProvider(
    @Value("\${app.jwt.secret}") private val secret: String,
    @Value("\${app.jwt.access-expiration-ms}") private val accessExpirationMs: Long,
    @Value("\${app.jwt.refresh-expiration-ms}") private val refreshExpirationMs: Long
) {
    private val key: SecretKey by lazy {
        Keys.hmacShaKeyFor(secret.toByteArray())
    }

    fun generateAccessToken(userId: UUID, email: String): String = Jwts.builder()
        .subject(userId.toString())
        .claim("email", email)
        .claim("type", "access")
        .issuedAt(Date())
        .expiration(Date(System.currentTimeMillis() + accessExpirationMs))
        .signWith(key)
        .compact()

    fun generateRefreshToken(userId: UUID): String = Jwts.builder()
        .subject(userId.toString())
        .claim("type", "refresh")
        .issuedAt(Date())
        .expiration(Date(System.currentTimeMillis() + refreshExpirationMs))
        .signWith(key)
        .compact()

    fun getRefreshExpirationMs(): Long = refreshExpirationMs

    fun hashToken(token: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        return digest.digest(token.toByteArray()).joinToString("") { "%02x".format(it) }
    }

    fun validateToken(token: String): Boolean = runCatching {
        val claims = getClaims(token)
        claims["type"] == "access"
    }.getOrDefault(false)

    fun validateRefreshToken(token: String): Boolean = runCatching {
        val claims = getClaims(token)
        claims["type"] == "refresh"
    }.getOrDefault(false)

    fun getUserId(token: String): UUID = UUID.fromString(getClaims(token).subject)

    fun getEmail(token: String): String = getClaims(token)["email"] as String

    private fun getClaims(token: String): Claims =
        Jwts.parser().verifyWith(key).build().parseSignedClaims(token).payload
}