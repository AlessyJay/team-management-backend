package com.example.team_management_backend.Entities

import com.example.team_management_backend.common.IssuePriority
import com.example.team_management_backend.common.IssueStatus
import com.example.team_management_backend.common.IssueType
import jakarta.persistence.*
import java.time.LocalDate
import java.time.OffsetDateTime

@Entity
@Table(
    name = "issues", indexes = [
        Index(name = "idx_issues_project", columnList = "project_id"),
        Index(name = "idx_issues_sprint", columnList = "sprint_id")
    ]
)
data class Issues(
    @Id @GeneratedValue(strategy = GenerationType.UUID) var id: String,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    var project: Projects,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sprint_id")
    var sprint: Sprints? = null,

    @Column(nullable = false, length = 255)
    var title: String,

    @Column(columnDefinition = "TEXT")
    var description: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignee_id")
    var assignee: User? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var type: IssueType = IssueType.TASK,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var status: IssueStatus = IssueStatus.BACKLOG,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var priority: IssuePriority = IssuePriority.MEDIUM,

    @Column(name = "story_points")
    var storyPoints: Short? = null,

    @Column(name = "due_date")
    var dueDate: LocalDate? = null,

    @Column(name = "position", nullable = false)
    var position: Int = 0,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by", nullable = false)
    var createdBy: User,

    @Column(name = "created_at", nullable = false)
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