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
    private val db: JdbcClient, private val passwordEncoder: PasswordEncoder, private val jwtProvider: JwtProvider
) {
    fun register(req: RegisterRequest): RegisterResponse {
        val exists =
            db.sql("select count(*) from users where email = :email").param("email", req.email).query(Int::class.java)
                .single() > 0

        if (exists) throw ConflictException("Email already exists!")

        val userId = UUID.randomUUID()

        db.sql("insert into users (id, name, email, password, created_at) values (:id, :name, :email, :password, :created_at)")
            .param("id", userId).param("name", req.name).param("email", req.email)
            .param("password", passwordEncoder.encode(req.password)).param("created_at", LocalDateTime.now()).update()

        return RegisterResponse("Account created successfully!", userId, req.email)
    }

    fun login(req: LoginRequest): Pair<LoginResponse, String> {
        val row = db.sql("select id, email, password from users where email = :email")
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
        print(userId)
        val email = row.second

        val accessToken = jwtProvider.generateAccessToken(userId, email)
        val refreshToken = jwtProvider.generateRefreshToken(userId)
        val tokenHash = jwtProvider.hashToken(refreshToken)
        val expiresAt = LocalDateTime.now().plusNanos(jwtProvider.getRefreshExpirationMs() * 1_000_000)

        db.sql(
            """
            insert into refresh_tokens (id, user_id, token_hash, expires_at, created_at)
            values (:id, :userId, :tokenHash, :expiresAt, :created_at)
            on conflict (user_id)
            do update set token_hash = :tokenHash, expires_at = :expiresAt, created_at = now()
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
            select token_hash, expires_at from refresh_tokens
            where user_id = :userId
        """
        )
            .param("userId", userId)
            .query { rs, _ ->
                Pair(rs.getString("token_hash"), rs.getObject("expires_at", OffsetDateTime::class.java))
            }
            .optional()
            .orElseThrow { UnauthorizedException("Refresh token not found") }

        if (!MessageDigest.isEqual(record.first.toByteArray(), tokenHash.toByteArray()))
            throw UnauthorizedException("Refresh token mismatch")

        if (record.second.isBefore(OffsetDateTime.now()))
            throw UnauthorizedException("Refresh token expired")

        val email = db.sql("select email from users where id = :id")
            .param("id", userId)
            .query(String::class.java).single()

        return RefreshResponse(jwtProvider.generateAccessToken(userId, email))
    }

    fun logout(userId: UUID) {
        db.sql("delete from refresh_tokens where user_id = :userId")
            .param("userId", userId)
            .update()
    }
}