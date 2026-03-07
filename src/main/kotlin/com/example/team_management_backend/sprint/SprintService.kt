package com.example.team_management_backend.sprint

import com.example.team_management_backend.common.BadRequestException
import com.example.team_management_backend.common.ConflictException
import com.example.team_management_backend.common.NotFoundException
import com.example.team_management_backend.common.SprintStatus
import com.example.team_management_backend.project.ProjectService
import org.springframework.stereotype.Service
import java.util.*

@Service
class SprintService(
    private val repo: SprintRepository,
    private val projectService: ProjectService
) {
    fun listSprints(projectId: UUID, userId: UUID): List<Sprint> {
        projectService.requireMember(projectId, userId)
        return repo.findAll(projectId)
    }

    fun createSprint(projectId: UUID, req: CreateSprintRequest, userId: UUID): Sprint {
        projectService.requireManager(projectId, userId)
        if (req.endDate <= req.startDate) throw BadRequestException("End date must be after start date")
        return repo.create(projectId, req)
    }

    fun updateSprint(projectId: UUID, sprintId: UUID, req: UpdateSprintRequest, userId: UUID): Sprint {
        projectService.requireManager(projectId, userId)
        repo.findById(sprintId) ?: throw NotFoundException("Sprint not found")
        return repo.update(sprintId, req)
    }

    fun startSprint(projectId: UUID, sprintId: UUID, userId: UUID): Sprint {
        projectService.requireManager(projectId, userId)
        val sprint = repo.findById(sprintId) ?: throw NotFoundException("Sprint not found")
        if (sprint.status != SprintStatus.PLANNING) throw BadRequestException("Only PLANNING sprints can be started")
        if (repo.hasActiveSprint(projectId)) throw ConflictException("A sprint is already active in this project")
        return repo.updateStatus(sprintId, SprintStatus.ACTIVE)
    }

    fun completeSprint(projectId: UUID, sprintId: UUID, userId: UUID): Sprint {
        projectService.requireManager(projectId, userId)
        val sprint = repo.findById(sprintId) ?: throw NotFoundException("Sprint not found")
        if (sprint.status != SprintStatus.ACTIVE) throw BadRequestException("Only ACTIVE sprints can be completed")
        return repo.updateStatus(sprintId, SprintStatus.COMPLETED)
    }
}