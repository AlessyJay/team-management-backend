package com.example.team_management_backend.Entities

import jakarta.persistence.*
import java.time.OffsetDateTime
import java.util.*

@Entity
@Table(
    name = "project_views",
    uniqueConstraints = [UniqueConstraint(columnNames = ["user_id", "project_id"])]
)
data class ProjectView(
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    var user: User,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    var project: Projects,

    @Column(name = "viewed_at", nullable = false)
    var viewedAt: OffsetDateTime = OffsetDateTime.now()
)