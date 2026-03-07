package com.example.team_management_backend.member

import com.example.team_management_backend.Auth.AuthPrincipal
import jakarta.validation.Valid
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/projects/{projectId}/members")
class MemberController(private val service: MemberService) {
    @GetMapping
    fun list(
        @PathVariable projectId: UUID,
        @AuthenticationPrincipal principal: AuthPrincipal
    ) = service.listMembers(projectId, principal.id)

    @PostMapping
    fun add(
        @PathVariable projectId: UUID,
        @Valid @RequestBody req: AddMemberRequest,
        @AuthenticationPrincipal principal: AuthPrincipal
    ) = service.addMember(projectId, req, principal.id)

    @DeleteMapping("/{userId}")
    fun remove(
        @PathVariable projectId: UUID,
        @PathVariable userId: UUID,
        @AuthenticationPrincipal principal: AuthPrincipal
    ) = service.removeMember(projectId, userId, principal.id)
}