package com.example.team_management_backend.Entities

import jakarta.persistence.*
import java.time.OffsetDateTime

enum class STATUS {
    ACTIVE,
    ARCHIVED
}

@Entity
@Table(name = "projects")
data class Projects(
    @Id @Column(nullable = false, unique = true) @GeneratedValue(strategy = GenerationType.UUID) var id: String,

    @Column(nullable = false) var name: String,

    @Column(nullable = true) var description: String,

    @OneToOne
    @JoinColumn(name = "owner_id", nullable = false, unique = true)
    var owner: User,

    @Enumerated(EnumType.STRING) @Column(nullable = false) var status: STATUS = STATUS.ACTIVE,

    @Column(nullable = false) var createdAt: OffsetDateTime = OffsetDateTime.now(),

    @Column(nullable = false) var updatedAt: OffsetDateTime = OffsetDateTime.now()
)
