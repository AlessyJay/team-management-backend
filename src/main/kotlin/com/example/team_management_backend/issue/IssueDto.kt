package com.example.team_management_backend.issue

import com.example.team_management_backend.common.IssuePriority
import com.example.team_management_backend.common.IssueStatus
import com.example.team_management_backend.common.IssueType
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.Instant
import java.util.*

data class Issue(
    val id: UUID,
    val projectId: UUID,
    val sprintId: UUID?,
    val title: String,
    val description: String?,
    val type: IssueType,
    val status: IssueStatus,
    val priority: IssuePriority,
    val storyPoints: Int?,
    val assigneeId: UUID?,
    val assigneeName: String?,
    val createdBy: UUID,
    val createdAt: Instant,
    val updatedAt: Instant
)

data class CreateIssueRequest(
    @field:NotBlank @field:Size(max = 255) val title: String,
    val description: String? = null,
    val type: IssueType = IssueType.TASK,
    val priority: IssuePriority = IssuePriority.MEDIUM,
    val storyPoints: Int? = null,
    val sprintId: UUID? = null
)

data class UpdateIssueRequest(
    @field:Size(max = 255) val title: String? = null,
    val description: String? = null,
    val type: IssueType? = null,
    val status: IssueStatus? = null,
    val priority: IssuePriority? = null,
    val storyPoints: Int? = null
)

data class AssignIssueRequest(val assigneeId: UUID?)

data class MoveToSprintRequest(val sprintId: UUID?)