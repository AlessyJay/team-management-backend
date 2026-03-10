package com.example.team_management_backend.Entities

import jakarta.persistence.*
import java.time.OffsetDateTime

enum class SprintMemberRole { LEAD, MEMBER }

@Entity
@Table(
    name = "sprint_members",
    uniqueConstraints = [UniqueConstraint(columnNames = ["sprint_id", "user_id"])]
)
data class SprintMember(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: String = "",

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sprint_id", nullable = false)
    var sprint: Sprints,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    var user: User,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    var role: SprintMemberRole = SprintMemberRole.MEMBER,

    @Column(name = "joined_at", nullable = false)
    var joinedAt: OffsetDateTime = OffsetDateTime.now(),
) {
    @PrePersist
    fun prePersist() {
        joinedAt = OffsetDateTime.now()
    }
}