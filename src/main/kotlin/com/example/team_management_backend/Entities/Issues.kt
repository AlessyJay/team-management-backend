package com.example.team_management_backend.Entities

import jakarta.persistence.*
import java.time.OffsetDateTime

enum class IssueStatus {
    BACKLOG,
    TODO,
    IN_PROGRESS,
    IN_REVIEW,
    DONE
}

enum class IssueType {
    STORY,
    TASK,
    BUG
}

enum class IssuePriority {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}

@Entity
@Table(
    name = "issues", indexes = [
        Index(name = "idxIssuesProject", columnList = "projectId"),
        Index(name = "idxIssuesSprint", columnList = "sprintId")
    ]
)
data class Issues(
    @Id @GeneratedValue(strategy = GenerationType.UUID) var id: String,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "projectId", nullable = false)
    var project: Projects,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sprintId")
    var sprint: Sprints? = null,

    @Column(nullable = false, length = 255)
    var title: String,

    @Column(columnDefinition = "TEXT")
    var description: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var type: IssueType = IssueType.TASK,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var status: IssueStatus = IssueStatus.BACKLOG,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var priority: IssuePriority = IssuePriority.MEDIUM,

    @Column(name = "storyPoints")
    var storyPoints: Short? = null,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "createdBy", nullable = false)
    var createdBy: User,

    @Column(name = "createdAt", nullable = false)
    var createdAt: OffsetDateTime = OffsetDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: OffsetDateTime = OffsetDateTime.now()
) {
    @PrePersist
    fun prePersist() {
        val now = OffsetDateTime.now()
        createdAt = now
        updatedAt = now
    }

    @PreUpdate
    fun preUpdate() {
        updatedAt = OffsetDateTime.now()
    }
}
