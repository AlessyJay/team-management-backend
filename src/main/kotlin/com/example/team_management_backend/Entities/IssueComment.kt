package com.example.team_management_backend.Entities

import jakarta.persistence.*
import java.time.OffsetDateTime

@Entity
@Table(
    name = "issue_comments",
    indexes = [Index(name = "idx_comments_issue", columnList = "issue_id")]
)
data class IssueComment(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: String = "",

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "issue_id", nullable = false)
    var issue: Issues,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    var user: User,

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