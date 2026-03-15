package com.example.team_management_backend.org

import com.example.team_management_backend.common.BadRequestException
import com.example.team_management_backend.common.ConflictException
import com.example.team_management_backend.common.ForbiddenException
import com.example.team_management_backend.common.NotFoundException
import org.springframework.stereotype.Service
import java.util.*

private val VALID_ROLES = setOf("ADMIN", "MEMBER")

@Service
class OrgService(private val repo: OrgRepository) {
    fun listOrgs(userId: UUID) = repo.findAllForUser(userId)

    fun getOrg(orgId: UUID, userId: UUID): OrgDto =
        repo.findById(orgId, userId) ?: throw NotFoundException("Organization not found")

    fun getOrgBySlug(slug: String, userId: UUID): OrgDto =
        repo.findBySlug(slug, userId) ?: throw NotFoundException("Organization not found")

    fun createOrg(req: CreateOrgRequest, userId: UUID): OrgDto =
        repo.create(req, userId)

    fun updateOrg(orgId: UUID, req: UpdateOrgRequest, userId: UUID): OrgDto {
        requireAdminOrOwner(orgId, userId)
        return repo.update(orgId, req) ?: throw NotFoundException("Organization not found")
    }

    fun deleteOrg(orgId: UUID, userId: UUID) {
        requireOwner(orgId, userId)
        repo.delete(orgId)
    }

    fun listMembers(orgId: UUID, userId: UUID): List<OrgMemberDto> {
        requireMember(orgId, userId)
        return repo.findMembers(orgId)
    }

    fun inviteMember(orgId: UUID, req: InviteMemberRequest, userId: UUID): OrgMemberDto {
        requireAdminOrOwner(orgId, userId)
        if (req.role !in VALID_ROLES) throw BadRequestException("Role must be ADMIN or MEMBER")

        val targetId = repo.findUserByEmail(req.email)
            ?: throw NotFoundException("No user found with email ${req.email}")

        if (repo.isMember(orgId, targetId)) throw ConflictException("User is already a member")

        repo.addMember(orgId, targetId, req.role)
        return repo.findMembers(orgId).first { it.userId == targetId }
    }

    fun updateMemberRole(orgId: UUID, targetUserId: UUID, req: UpdateMemberRoleRequest, userId: UUID): OrgMemberDto {
        requireOwner(orgId, userId)
        if (req.role !in VALID_ROLES) throw BadRequestException("Role must be ADMIN or MEMBER")
        if (targetUserId == userId) throw BadRequestException("Cannot change your own role")

        val targetRole = repo.getRole(orgId, targetUserId)
            ?: throw NotFoundException("Member not found")
        if (targetRole == "OWNER") throw ForbiddenException("Cannot change the owner's role")

        repo.updateMemberRole(orgId, targetUserId, req.role)
        return repo.findMembers(orgId).first { it.userId == targetUserId }
    }

    fun removeMember(orgId: UUID, targetUserId: UUID, userId: UUID) {
        if (targetUserId == userId) {
            val role = repo.getRole(orgId, userId) ?: throw NotFoundException("Not a member")
            if (role == "OWNER") throw BadRequestException("Owner cannot leave the organization. Transfer ownership first.")
        } else {
            requireAdminOrOwner(orgId, userId)
            val targetRole = repo.getRole(orgId, targetUserId)
                ?: throw NotFoundException("Member not found")
            if (targetRole == "OWNER") throw ForbiddenException("Cannot remove the owner")
        }
        repo.removeMember(orgId, targetUserId)
    }

    fun requireMember(orgId: UUID, userId: UUID) {
        if (!repo.isMember(orgId, userId)) throw ForbiddenException("Not a member of this organization")
    }

    private fun requireAdminOrOwner(orgId: UUID, userId: UUID) {
        val role = repo.getRole(orgId, userId) ?: throw ForbiddenException("Not a member of this organization")
        if (role !in setOf("OWNER", "ADMIN")) throw ForbiddenException("Admin or owner role required")
    }

    private fun requireOwner(orgId: UUID, userId: UUID) {
        val role = repo.getRole(orgId, userId) ?: throw ForbiddenException("Not a member of this organization")
        if (role != "OWNER") throw ForbiddenException("Only the owner can perform this action")
    }
}