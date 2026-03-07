package com.example.team_management_backend.capacity

import com.example.team_management_backend.project.ProjectService
import org.springframework.stereotype.Service
import java.util.*

@Service
class CapacityService(
    private val repo: CapacityRepository,
    private val projectService: ProjectService
) {
    fun getCapacityReport(projectId: UUID, sprintId: UUID, userId: UUID): SprintCapacityReport {
        projectService.requireMember(projectId, userId)
        val members = repo.getSprintCapacityReport(sprintId)
        return SprintCapacityReport(
            sprintId = sprintId,
            totalCapacity = members.sumOf { it.capacityPoints },
            totalAssigned = members.sumOf { it.assignedPoints },
            members = members
        )
    }

    fun setCapacity(projectId: UUID, sprintId: UUID, targetUserId: UUID, req: SetCapacityRequest, requesterId: UUID) {
        projectService.requireManager(projectId, requesterId)
        repo.upsertCapacity(sprintId, targetUserId, req.capacityPoints)
    }
}