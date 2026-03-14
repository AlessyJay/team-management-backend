package com.example.team_management_backend.sprint

import com.example.team_management_backend.SprintComment.CreateCommentRequest
import com.example.team_management_backend.SprintComment.ReactRequest
import com.example.team_management_backend.SprintComment.SprintCommentDto
import com.example.team_management_backend.SprintComment.UpdateCommentRequest
import com.example.team_management_backend.common.BadRequestException
import com.example.team_management_backend.common.ForbiddenException
import com.example.team_management_backend.common.NotFoundException
import com.example.team_management_backend.project.ProjectService
import org.springframework.stereotype.Service
import java.util.*

private val VALID_REACTIONS = setOf("LIKE", "LOVE", "WOW", "CHEERS", "APPLAUSE")

@Service
class SprintCommentService(
    private val repo: SprintCommentRepository,
    private val sprintRepo: SprintRepository,
    private val projectService: ProjectService,
) {
    fun listComments(projectId: UUID, sprintId: UUID, userId: UUID): List<SprintCommentDto> {
        projectService.requireMember(projectId, userId)
        return repo.findAllForSprint(sprintId, userId)
    }

    fun createComment(projectId: UUID, sprintId: UUID, req: CreateCommentRequest, userId: UUID): SprintCommentDto {
        projectService.requireMember(projectId, userId)
        val isMember = sprintRepo.isSprintMember(sprintId, userId)
        val isManager = runCatching { projectService.requireManager(projectId, userId); true }.getOrDefault(false)
        if (!isMember && !isManager) throw ForbiddenException("Only sprint members can comment")
        return repo.create(sprintId, userId, req)
    }

    fun updateComment(
        projectId: UUID,
        sprintId: UUID,
        commentId: UUID,
        req: UpdateCommentRequest,
        userId: UUID
    ): SprintCommentDto {
        projectService.requireMember(projectId, userId)
        val ownerId = repo.getOwnerId(commentId) ?: throw NotFoundException("Comment not found")
        if (ownerId != userId) throw ForbiddenException("You can only edit your own comments")
        return repo.update(commentId, req.content) ?: throw NotFoundException("Comment not found")
    }

    fun deleteComment(projectId: UUID, sprintId: UUID, commentId: UUID, userId: UUID) {
        projectService.requireMember(projectId, userId)
        val ownerId = repo.getOwnerId(commentId) ?: throw NotFoundException("Comment not found")
        val isManager = runCatching { projectService.requireManager(projectId, userId); true }.getOrDefault(false)
        if (ownerId != userId && !isManager) throw ForbiddenException("You can only delete your own comments")
        repo.delete(commentId)
    }

    fun toggleReaction(
        projectId: UUID,
        sprintId: UUID,
        commentId: UUID,
        req: ReactRequest,
        userId: UUID
    ): Map<String, Any> {
        projectService.requireMember(projectId, userId)
        if (req.reaction !in VALID_REACTIONS) throw BadRequestException("Invalid reaction type")
        val isMember = sprintRepo.isSprintMember(sprintId, userId)
        val isManager = runCatching { projectService.requireManager(projectId, userId); true }.getOrDefault(false)
        if (!isMember && !isManager) throw ForbiddenException("Only sprint members can react")
        val added = repo.toggleReaction(commentId, userId, req.reaction)
        return mapOf("added" to added, "reaction" to req.reaction)
    }
}