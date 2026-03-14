package com.example.team_management_backend.sprint

import com.example.team_management_backend.Auth.AuthPrincipal
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/projects/{projectId}/sprints")
class SprintController(val service: SprintService) {

    @GetMapping
    fun list(@PathVariable projectId: UUID, @AuthenticationPrincipal p: AuthPrincipal) =
        service.listSprints(projectId, p.id)

    @GetMapping("/{sprintId}")
    fun get(@PathVariable projectId: UUID, @PathVariable sprintId: UUID, @AuthenticationPrincipal p: AuthPrincipal) =
        service.getSprint(projectId, sprintId, p.id)

    @PostMapping
    fun create(
        @PathVariable projectId: UUID,
        @Valid @RequestBody req: CreateSprintRequest,
        @AuthenticationPrincipal p: AuthPrincipal
    ) =
        service.createSprint(projectId, req, p.id)

    @PatchMapping("/{sprintId}")
    fun update(
        @PathVariable projectId: UUID,
        @PathVariable sprintId: UUID,
        @Valid @RequestBody req: UpdateSprintRequest,
        @AuthenticationPrincipal p: AuthPrincipal
    ) =
        service.updateSprint(projectId, sprintId, req, p.id)

    @PatchMapping("/{sprintId}/goal")
    fun updateGoal(
        @PathVariable projectId: UUID,
        @PathVariable sprintId: UUID,
        @Valid @RequestBody req: UpdateGoalRequest,
        @AuthenticationPrincipal p: AuthPrincipal
    ) =
        service.updateGoal(projectId, sprintId, req, p.id)

    @DeleteMapping("/{sprintId}")
    fun delete(
        @PathVariable projectId: UUID,
        @PathVariable sprintId: UUID,
        @AuthenticationPrincipal p: AuthPrincipal
    ): ResponseEntity<Void> {
        service.deleteSprint(projectId, sprintId, p.id)
        return ResponseEntity.noContent().build()
    }

    @PostMapping("/{sprintId}/start")
    fun start(@PathVariable projectId: UUID, @PathVariable sprintId: UUID, @AuthenticationPrincipal p: AuthPrincipal) =
        service.startSprint(projectId, sprintId, p.id)

    @PostMapping("/{sprintId}/complete")
    fun complete(
        @PathVariable projectId: UUID,
        @PathVariable sprintId: UUID,
        @AuthenticationPrincipal p: AuthPrincipal
    ) =
        service.completeSprint(projectId, sprintId, p.id)
}