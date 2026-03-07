package com.example.team_management_backend.Entities

import com.example.team_management_backend.common.MemberRole
import jakarta.persistence.*
import java.time.OffsetDateTime

@Entity
@Table(name = "project_members", uniqueConstraints = [UniqueConstraint(columnNames = ["projectId", "userId"])])
data class ProjectMember(
    @Id @GeneratedValue(strategy = GenerationType.UUID) var id: String,
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(
        name = "projectId",
        nullable = false
    ) var project: Projects,
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(nullable = false, name = "userId") var user: User,
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) var role: MemberRole = MemberRole.MEMBER,
    @Column(name = "joinedAt", nullable = false) var joinedAt: OffsetDateTime = OffsetDateTime.now(),
    @Column(name = "updatedAt", nullable = false) var updatedAt: OffsetDateTime = OffsetDateTime.now(),
)
