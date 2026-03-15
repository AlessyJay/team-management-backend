package com.example.team_management_backend.Entities

import jakarta.persistence.*
import java.time.OffsetDateTime

@Entity
@Table(
    name = "organization_members",
    uniqueConstraints = [UniqueConstraint(columnNames = ["org_id", "user_id"])]
)
data class OrganizationMember(
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    var id: String = "",

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "org_id", nullable = false)
    var organization: Organization,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    var user: User,

    // OWNER | ADMIN | MEMBER
    @Column(nullable = false, length = 20)
    var role: String = "MEMBER",

    @Column(name = "joined_at", nullable = false)
    var joinedAt: OffsetDateTime = OffsetDateTime.now(),
) {
    @PrePersist
    fun prePersist() {
        joinedAt = OffsetDateTime.now()
    }
}