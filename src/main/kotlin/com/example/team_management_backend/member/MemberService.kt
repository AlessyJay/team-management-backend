package com.example.team_management_backend.member

import com.example.team_management_backend.common.BadRequestException
import com.example.team_management_backend.common.ConflictException
import com.example.team_management_backend.common.NotFoundException
import com.example.team_management_backend.project.ProjectService
import org.springframework.stereotype.Service
import java.util.*

@Service
class MemberService(
    private val repo: MemberRepository,
    private val projectService: ProjectService,
) {
    fun listMembers(projectId: UUID, userId: UUID): List<ProjectMember> {
        projectService.requireMember(projectId, userId)
        return repo.findAll(projectId)
    }

    fun addMember(projectId: UUID, req: AddMemberRequest, requesterId: UUID): ProjectMember {
        projectService.requireManager(projectId, requesterId)
        val targetUserId = repo.findUserByEmail(req.email)
            ?: throw NotFoundException("No user found with email ${req.email}")
        return try {
            repo.add(projectId, targetUserId, req.role)
        } catch (ex: Exception) {
            throw ConflictException("User is already a member of this project")
        }
    }

    fun removeMember(projectId: UUID, targetUserId: UUID, requesterId: UUID) {
        projectService.requireManager(projectId, requesterId)
        if (targetUserId == requesterId) throw BadRequestException("Cannot remove yourself")
        val removed = repo.remove(projectId, targetUserId)
        if (removed == 0) throw NotFoundException("Member not found")
    }
}