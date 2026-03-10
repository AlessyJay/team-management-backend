package com.example.team_management_backend.project

import com.example.team_management_backend.common.ForbiddenException
import com.example.team_management_backend.common.MemberRole
import com.example.team_management_backend.common.NotFoundException
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.*

@Service
class ProjectService(val repo: ProjectRepository) {

    fun listProjects(userId: UUID) = repo.findAllForUser(userId)

    fun getRecentlyViewed(userId: UUID) = repo.findRecentlyViewed(userId)

    fun getProject(id: UUID, userId: UUID): ProjectDto {
        val project = repo.findById(id) ?: throw NotFoundException("Project not found")
        if (!repo.isMember(id, userId)) throw ForbiddenException("Not a member of this project")
        return project
    }

    fun createProject(req: CreateProjectRequest, ownerId: UUID): ProjectDto {
        val project = repo.create(req.name, req.description, ownerId)
        repo.db.sql(
            """
            INSERT INTO project_members (id, project_id, user_id, role, joined_at, updated_at)
            VALUES (:id, :pid, :uid, 'MANAGER', :joined_at, :updated_at)
            """
        )
            .param("id", UUID.randomUUID())
            .param("pid", project.id.toString())
            .param("uid", ownerId.toString())
            .param("joined_at", LocalDateTime.now())
            .param("updated_at", LocalDateTime.now())
            .update()
        return project
    }

    fun recordView(projectId: UUID, userId: UUID) {
        if (!repo.isMember(projectId, userId)) return
        repo.upsertView(projectId, userId)
    }

    fun updateProject(id: UUID, req: UpdateProjectRequest, userId: UUID): ProjectDto {
        requireManager(id, userId)
        return repo.update(id, req.name, req.description, req.status)
    }

    fun deleteProject(id: UUID, userId: UUID) {
        val project = repo.findById(id) ?: throw NotFoundException("Project not found")
        if (project.ownerId != userId) throw ForbiddenException("Only the owner can delete this project")
        repo.delete(id)
    }

    fun requireManager(projectId: UUID, userId: UUID) {
        val role = repo.getRole(projectId, userId)
            ?: throw ForbiddenException("Not a member of this project")
        if (role != MemberRole.MANAGER) throw ForbiddenException("Manager role required")
    }

    fun requireMember(projectId: UUID, userId: UUID) {
        if (!repo.isMember(projectId, userId)) throw ForbiddenException("Not a member of this project")
    }
}