package com.example.team_management_backend.sprint

import com.example.team_management_backend.common.SprintStatus
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.Instant
import java.time.LocalDate
import java.util.*

data class SprintMemberDto(
    val userId: UUID,
    val name: String,
    val email: String,
    val role: String,
)

data class Sprint(
    val id: UUID,
    val projectId: UUID,
    val name: String,
    val goal: String?,
    val category: String?,
    val tags: List<String>,
    val expectedDuration: String?,
    val startDate: LocalDate,
    val endDate: LocalDate?,
    val status: SprintStatus,
    val leads: List<SprintMemberDto>,
    val members: List<SprintMemberDto>,
    val issueCount: Int,
    val doneCount: Int,
    val createdAt: Instant,
)

data class CreateSprintRequest(
    @field:NotBlank @field:Size(max = 100) val name: String,
    val goal: String? = null,
    val category: String? = null,
    val tags: List<String> = emptyList(),
    val expectedDuration: String? = null,
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val leadIds: List<UUID> = emptyList(),
    val memberIds: List<UUID> = emptyList(),
    val status: SprintStatus = SprintStatus.PLANNING,
)

data class UpdateSprintRequest(
    @field:Size(max = 100) val name: String? = null,
    val goal: String? = null,
    val category: String? = null,
    val tags: List<String>? = null,
    val expectedDuration: String? = null,
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
)

data class UpdateGoalRequest(
    @field:NotBlank val goal: String,
)