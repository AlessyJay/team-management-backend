package com.example.team_management_backend.Entities

import jakarta.persistence.*
import java.time.OffsetDateTime

@Entity
@Table(
    name = "sprint_comments",
    indexes = [Index(name = "idx_sprint_comments_sprint", columnList = "sprint_id")]
)
data class SprintComment(
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    var id: String = "",

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sprint_id", nullable = false)
    var sprint: Sprints,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    var user: User,

    @Column(name = "parent_id")
    var parentId: String? = null,

    @Column(nullable = false, columnDefinition = "TEXT")
    var content: String = "",

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