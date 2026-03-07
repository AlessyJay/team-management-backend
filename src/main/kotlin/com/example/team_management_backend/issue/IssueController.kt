package com.example.team_management_backend.issue

import com.example.team_management_backend.Auth.AuthPrincipal
import jakarta.validation.Valid
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/projects/{projectId}/issues")
class IssueController(private val service: IssueService) {
    @GetMapping
    fun list(
        @PathVariable projectId: UUID,
        @RequestParam sprintId: UUID?,
        @AuthenticationPrincipal principal: AuthPrincipal
    ) = service.listIssues(projectId, sprintId, principal.id)

    @PostMapping
    fun create(
        @PathVariable projectId: UUID,
        @Valid @RequestBody req: CreateIssueRequest,
        @AuthenticationPrincipal principal: AuthPrincipal
    ) = service.createIssue(projectId, req, principal.id)

    @PatchMapping("/{issueId}")
    fun update(
        @PathVariable projectId: UUID,
        @PathVariable issueId: UUID,
        @Valid @RequestBody req: UpdateIssueRequest,
        @AuthenticationPrincipal principal: AuthPrincipal
    ) = service.updateIssue(projectId, issueId, req, principal.id)

    @PatchMapping("/{issueId}/assign")
    fun assign(
        @PathVariable projectId: UUID,
        @PathVariable issueId: UUID,
        @RequestBody req: AssignIssueRequest,
        @AuthenticationPrincipal principal: AuthPrincipal
    ) = service.assignIssue(projectId, issueId, req, principal.id)

    @PatchMapping("/{issueId}/move")
    fun move(
        @PathVariable projectId: UUID,
        @PathVariable issueId: UUID,
        @RequestBody req: MoveToSprintRequest,
        @AuthenticationPrincipal principal: AuthPrincipal
    ) = service.moveToSprint(projectId, issueId, req, principal.id)

    @DeleteMapping("/{issueId}")
    fun delete(
        @PathVariable projectId: UUID,
        @PathVariable issueId: UUID,
        @AuthenticationPrincipal principal: AuthPrincipal
    ) = service.deleteIssue(projectId, issueId, principal.id)
}