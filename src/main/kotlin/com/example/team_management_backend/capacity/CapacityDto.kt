package com.example.team_management_backend.capacity

import jakarta.validation.constraints.Min
import java.util.*

data class MemberLoad(
    val userId: UUID,
    val name: String,
    val email: String,
    val capacityPoints: Int,
    val assignedPoints: Int,
    val utilization: Double,       // 0.0–1.0+ (over 1.0 = over capacity)
    val isOverCapacity: Boolean
)

data class SprintCapacityReport(
    val sprintId: UUID,
    val totalCapacity: Int,
    val totalAssigned: Int,
    val members: List<MemberLoad>
)

data class SetCapacityRequest(
    @field:Min(0) val capacityPoints: Int
)