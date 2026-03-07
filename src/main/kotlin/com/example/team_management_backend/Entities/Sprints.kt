package com.example.team_management_backend.Entities

import com.example.team_management_backend.common.SprintStatus
import jakarta.persistence.*
import java.time.LocalDate
import java.time.OffsetDateTime

@Entity
@Table(name = "sprints")
data class Sprints(
    @Id @GeneratedValue(strategy = GenerationType.UUID) var id: String,
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(
        name = "projectId",
        nullable = false
    ) var project: Projects,
    @Column(nullable = false, length = 100) var name: String,
    @Column(columnDefinition = "TEXT") var goal: String? = null,
    @Column(nullable = false, name = "startDate") var startDate: LocalDate,
    @Column(nullable = false, name = "endDate") var endDate: LocalDate? = null,
    @Enumerated(EnumType.STRING) @Column(
        nullable = false,
        length = 20
    ) var status: SprintStatus = SprintStatus.PLANNING,
    @Column(name = "createdAt", nullable = false) var createdAt: OffsetDateTime = OffsetDateTime.now(),
    @Column(name = "updatedAt", nullable = false) var updatedAt: OffsetDateTime = OffsetDateTime.now(),
)
