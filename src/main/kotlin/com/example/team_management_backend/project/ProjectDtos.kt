package com.example.team_management_backend.project

import com.example.team_management_backend.common.ProjectStatus
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.Instant
import java.time.LocalDate
import java.util.*

data class ActiveSprintDto(
    val id: UUID,
    val name: String,
    val endDate: LocalDate?
)

data class ProjectDto(
    val id: UUID,
    val name: String,
    val description: String?,
    val ownerId: UUID,
    val status: ProjectStatus,
    val createdAt: Instant,
    val joinedAt: Instant,
    val memberCount: Int,
    val activeSprint: ActiveSprintDto?,
    val hasUrgentIssues: Boolean,
)

data class CreateProjectRequest(
    @field:NotBlank @field:Size(max = 150) val name: String,
    val description: String? = null
)

data class UpdateProjectRequest(
    @field:Size(max = 150) val name: String? = null,
    val description: String? = null,
    val status: ProjectStatus? = null
)