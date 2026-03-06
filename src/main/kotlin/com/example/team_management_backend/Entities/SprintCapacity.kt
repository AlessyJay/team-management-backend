package com.example.team_management_backend.Entities

import jakarta.persistence.*

@Entity
@Table(
    name = "sprint_capacity",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["sprint_id", "user_id"])
    ]
)
data class SprintCapacity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: String,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sprintId", nullable = false)
    var sprint: Sprints,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "userId", nullable = false)
    var user: User,

    @Column(name = "capacityPoints", nullable = false)
    var capacityPoints: Short = 0
)
