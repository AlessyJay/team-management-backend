package com.example.team_management_backend.sprint

import com.example.team_management_backend.Auth.AuthPrincipal
import jakarta.validation.Valid
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/projects/{projectId}/sprints")
class SprintController(val service: SprintService) {
    @GetMapping
    fun list(
        @PathVariable projectId: UUID,
        @AuthenticationPrincipal principal: AuthPrincipal
    ) = service.listSprints(projectId, principal.id)

    @PostMapping
    fun create(
        @PathVariable projectId: UUID,
        @Valid @RequestBody req: CreateSprintRequest,
        @AuthenticationPrincipal principal: AuthPrincipal
    ) = service.createSprint(projectId, req, principal.id)

    @PatchMapping("/{sprintId}")
    fun update(
        @PathVariable projectId: UUID,
        @PathVariable sprintId: UUID,
        @Valid @RequestBody req: UpdateSprintRequest,
        @AuthenticationPrincipal principal: AuthPrincipal
    ) = service.updateSprint(projectId, sprintId, req, principal.id)

    @PostMapping("/{sprintId}/start")
    fun start(
        @PathVariable projectId: UUID,
        @PathVariable sprintId: UUID,
        @AuthenticationPrincipal principal: AuthPrincipal
    ) = service.startSprint(projectId, sprintId, principal.id)

    @PostMapping("/{sprintId}/complete")
    fun complete(
        @PathVariable projectId: UUID,
        @PathVariable sprintId: UUID,
        @AuthenticationPrincipal principal: AuthPrincipal
    ) = service.completeSprint(projectId, sprintId, principal.id)
}