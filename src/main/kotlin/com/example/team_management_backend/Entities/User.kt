package com.example.team_management_backend.Entities

import jakarta.persistence.*
import java.time.OffsetDateTime

@Entity
@Table(name = "users")
data class User(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: String? = null,

    @Column(nullable = false, length = 100)
    var name: String,

    @Column(nullable = false, unique = true, length = 255)
    var email: String,

    @Column(nullable = false, columnDefinition = "TEXT")
    var password: String,

    @Column(name = "created_at", nullable = false)
    var joinedAt: OffsetDateTime = OffsetDateTime.now()
)
