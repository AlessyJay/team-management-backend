package com.example.team_management_backend.sprint

import com.example.team_management_backend.common.SprintStatus
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.Instant
import java.time.LocalDate
import java.util.*

data class Sprint(
    val id: UUID,
    val projectId: UUID,
    val name: String,
    val goal: String?,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val status: SprintStatus,
    val createdAt: Instant
)

data class CreateSprintRequest(
    @field:NotBlank @field:Size(max = 100) val name: String,
    val goal: String? = null,
    val startDate: LocalDate,
    val endDate: LocalDate
)

data class UpdateSprintRequest(
    @field:Size(max = 100) val name: String? = null,
    val goal: String? = null,
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null
)