package com.example.team_management_backend.Auth

import com.example.team_management_backend.common.ConflictException
import com.example.team_management_backend.common.NotFoundException
import com.example.team_management_backend.common.UnauthorizedException
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.security.MessageDigest
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.util.*

@Service
class AuthService(
    private val db: JdbcClient,
    private val passwordEncoder: PasswordEncoder,
    private val jwtProvider: JwtProvider
) {

    fun register(req: RegisterRequest): RegisterResponse {
        val exists = db.sql("SELECT COUNT(*) FROM users WHERE email = :email")
            .param("email", req.email)
            .query(Int::class.java).single() > 0

        if (exists) throw ConflictException("Email already exists!")

        val userId = UUID.randomUUID()

        db.sql("INSERT INTO users (id, name, email, password) VALUES (:id, :name, :email, :password)")
            .param("id", userId)
            .param("name", req.name)
            .param("email", req.email)
            .param("password", passwordEncoder.encode(req.password))
            .update()

        return RegisterResponse("Account created successfully!", userId, req.email)
    }

    // Returns the LoginResponse (for the body) and the raw refresh token
    // (for the controller to set as an HttpOnly cookie, never in the body).
    fun login(req: LoginRequest): Pair<LoginResponse, String> {
        val row = db.sql("SELECT id, email, password FROM users WHERE email = :email")
            .param("email", req.email)
            .query { rs, _ ->
                Triple(
                    rs.getObject("id", UUID::class.java),
                    rs.getString("email"),
                    rs.getString("password")
                )
            }
            .optional()
            .orElseThrow { NotFoundException("Invalid credentials!") }

        if (!passwordEncoder.matches(req.password, row.third))
            throw NotFoundException("Invalid credentials!")

        val userId = row.first
        val email = row.second

        val accessToken = jwtProvider.generateAccessToken(userId, email)
        val refreshToken = jwtProvider.generateRefreshToken(userId)
        val tokenHash = jwtProvider.hashToken(refreshToken)
        val expiresAt = OffsetDateTime.now().plusNanos(jwtProvider.getRefreshExpirationMs() * 1_000_000)

        // One refresh token per user, upserts on every login.
        db.sql(
            """
            INSERT INTO refresh_tokens (id, user_id, token_hash, expires_at, created_at)
            VALUES (:id, :userId, :tokenHash, :expiresAt, :created_at)
            ON CONFLICT (user_id)
            DO UPDATE SET token_hash = :tokenHash, expires_at = :expiresAt, created_at = now()
        """
        )
            .param("id", UUID.randomUUID())
            .param("userId", userId)
            .param("tokenHash", tokenHash)
            .param("expiresAt", expiresAt)
            .param("created_at", LocalDateTime.now())
            .update()

        return Pair(LoginResponse(accessToken, userId, email), refreshToken)
    }

    fun refresh(rawRefreshToken: String): RefreshResponse {
        if (!jwtProvider.validateRefreshToken(rawRefreshToken))
            throw UnauthorizedException("Invalid refresh token")

        val userId = jwtProvider.getUserId(rawRefreshToken)
        val tokenHash = jwtProvider.hashToken(rawRefreshToken)

        val record = db.sql(
            """
            SELECT token_hash, expires_at FROM refresh_tokens WHERE user_id = :userId
        """
        )
            .param("userId", userId)
            .query { rs, _ ->
                Pair(rs.getString("token_hash"), rs.getObject("expires_at", OffsetDateTime::class.java))
            }
            .optional()
            .orElseThrow { UnauthorizedException("Refresh token not found") }

        // Constant-time comparison to prevent timing attacks
        if (!MessageDigest.isEqual(record.first.toByteArray(), tokenHash.toByteArray()))
            throw UnauthorizedException("Refresh token mismatch")

        if (record.second.isBefore(OffsetDateTime.now()))
            throw UnauthorizedException("Refresh token expired")

        val email = db.sql("SELECT email FROM users WHERE id = :id")
            .param("id", userId)
            .query(String::class.java).single()

        return RefreshResponse(jwtProvider.generateAccessToken(userId, email))
    }

    fun logout(userId: UUID) {
        db.sql("DELETE FROM refresh_tokens WHERE user_id = :userId")
            .param("userId", userId)
            .update()
    }
}