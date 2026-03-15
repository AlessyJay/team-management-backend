package com.example.team_management_backend.org

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import java.time.Instant
import java.util.*

data class OrgMemberDto(
    val id: UUID,
    val userId: UUID,
    val name: String,
    val email: String,
    val role: String,
    val joinedAt: Instant,
)

data class OrgDto(
    val id: UUID,
    val slug: String,
    val name: String,
    val description: String?,
    val logoUrl: String?,
    val ownerId: UUID,
    val memberCount: Int,
    val myRole: String,
    val createdAt: Instant,
)

data class CreateOrgRequest(
    @field:NotBlank @field:Size(max = 100)
    val name: String,
    @field:Size(max = 100)
    @field:Pattern(regexp = "^[a-z0-9-]*$", message = "Slug may only contain lowercase letters, digits, and hyphens")
    val slug: String? = null,
    val description: String? = null,
)

data class UpdateOrgRequest(
    @field:Size(max = 100) val name: String? = null,
    val description: String? = null,
    val logoUrl: String? = null,
)

data class InviteMemberRequest(
    @field:NotBlank val email: String,
    val role: String = "MEMBER",
)

data class UpdateMemberRoleRequest(
    @field:NotBlank val role: String,
)