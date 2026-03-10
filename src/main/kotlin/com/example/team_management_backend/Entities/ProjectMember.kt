package com.example.team_management_backend.Entities

import com.example.team_management_backend.common.MemberRole
import jakarta.persistence.*
import java.time.OffsetDateTime

@Entity
@Table(
    name = "project_members",
    uniqueConstraints = [UniqueConstraint(columnNames = ["project_id", "user_id"])]
)
data class ProjectMember(
    @Id @GeneratedValue(strategy = GenerationType.UUID) var id: String,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    var project: Projects,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    var user: User,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var role: MemberRole = MemberRole.MEMBER,

    @Column(name = "joined_at", nullable = false)
    var joinedAt: OffsetDateTime = OffsetDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: OffsetDateTime = OffsetDateTime.now(),
)