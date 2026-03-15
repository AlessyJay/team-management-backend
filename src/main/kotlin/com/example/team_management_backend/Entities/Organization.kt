package com.example.team_management_backend.Entities

import jakarta.persistence.*
import java.time.OffsetDateTime

@Entity
@Table(name = "organizations")
data class Organization(
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, unique = true)
    var id: String = "",

    @Column(nullable = false, unique = true, length = 100)
    var slug: String = "",

    @Column(nullable = false, length = 100)
    var name: String = "",

    @Column(columnDefinition = "TEXT")
    var description: String? = null,

    @Column(name = "logo_url")
    var logoUrl: String? = null,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    var owner: User,

    @Column(name = "created_at", nullable = false)
    var createdAt: OffsetDateTime = OffsetDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: OffsetDateTime = OffsetDateTime.now(),
) {
    @PrePersist
    fun prePersist() {
        val n = OffsetDateTime.now(); createdAt = n; updatedAt = n
    }

    @PreUpdate
    fun preUpdate() {
        updatedAt = OffsetDateTime.now()
    }
}