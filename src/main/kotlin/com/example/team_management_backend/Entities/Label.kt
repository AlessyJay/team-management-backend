package com.example.team_management_backend.Entities

import jakarta.persistence.*

@Entity
@Table(
    name = "labels",
    uniqueConstraints = [UniqueConstraint(columnNames = ["project_id", "name"])]
)
data class Label(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: String = "",

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    var project: Projects,

    @Column(nullable = false, length = 50)
    var name: String = "",

    // Hex color code e.g. "#6366f1"
    @Column(nullable = false, length = 7)
    var color: String = "#6366f1",
)