package com.example.team_management_backend.Entities

import com.example.team_management_backend.common.SprintStatus
import jakarta.persistence.*
import java.time.LocalDate
import java.time.OffsetDateTime

@Entity
@Table(name = "sprints")
data class Sprints(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: String = "",

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    var project: Projects,

    @Column(nullable = false, length = 100)
    var name: String = "",

    @Column(columnDefinition = "TEXT")
    var goal: String? = null,

    @Column(length = 50)
    var category: String? = null,

    @Convert(converter = StringListConverter::class)
    @Column(name = "tags", columnDefinition = "TEXT")
    var tags: List<String> = emptyList(),

    @Column(name = "expected_duration", length = 50)
    var expectedDuration: String? = null,

    @Column(name = "start_date", nullable = false)
    var startDate: LocalDate = LocalDate.now(),

    @Column(name = "end_date")
    var endDate: LocalDate? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var status: SprintStatus = SprintStatus.PLANNING,

    @Column(name = "completed_at")
    var completedAt: OffsetDateTime? = null,

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