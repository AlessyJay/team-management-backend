package com.example.team_management_backend.Entities

import jakarta.persistence.*
import java.time.OffsetDateTime
import java.util.*

@Entity
@Table(name = "refresh_tokens")
data class RefreshToken(
    @Id
    @Column(nullable = false)
    val id: UUID = UUID.randomUUID(),

    @ManyToOne
    @JoinColumn(nullable = false, name = "user_id", unique = true)
    val user: User,

    @Column(nullable = false, unique = true, name = "token_hash", columnDefinition = "TEXT")
    val token: String,

    @Column(nullable = false, name = "expires_at")
    val expiresAt: OffsetDateTime,

    @Column(nullable = false, name = "created_at")
    val createdAt: OffsetDateTime = OffsetDateTime.now(),
)
