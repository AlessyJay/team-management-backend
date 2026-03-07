package com.example.team_management_backend.member

import com.example.team_management_backend.common.MemberRole
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import java.time.Instant
import java.util.*

data class ProjectMember(
    val id: UUID,
    val projectId: UUID,
    val userId: UUID,
    val email: String,
    val name: String,
    val role: MemberRole,
    val joinedAt: Instant,
)

data class AddMemberRequest(
    @field:Email @field:NotBlank val email: String,
    val role: MemberRole = MemberRole.MEMBER,
)
