package com.example.team_management_backend.Entities

import jakarta.persistence.*

@Entity
@Table(
    name = "sprint_capacity",
    uniqueConstraints = [UniqueConstraint(columnNames = ["sprint_id", "user_id"])]
)
data class SprintCapacity(
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    var id: String,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sprint_id", nullable = false)
    var sprint: Sprints,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    var user: User,

    @Column(name = "capacity_points", nullable = false)
    var capacityPoints: Short = 0
)