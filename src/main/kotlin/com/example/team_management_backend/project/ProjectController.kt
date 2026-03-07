package com.example.team_management_backend.project

import com.example.team_management_backend.Auth.AuthPrincipal
import jakarta.validation.Valid
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/projects")
class ProjectController(private val service: ProjectService) {
    @GetMapping
    fun list(@AuthenticationPrincipal principal: AuthPrincipal) =
        service.listProjects(principal.id)

    @GetMapping("/{id}")
    fun get(@PathVariable id: UUID, @AuthenticationPrincipal principal: AuthPrincipal) =
        service.getProject(id, principal.id)

    @PostMapping
    fun create(
        @Valid @RequestBody req: CreateProjectRequest,
        @AuthenticationPrincipal principal: AuthPrincipal
    ) = service.createProject(req, principal.id)

    @PatchMapping("/{id}")
    fun update(
        @PathVariable id: UUID,
        @Valid @RequestBody req: UpdateProjectRequest,
        @AuthenticationPrincipal principal: AuthPrincipal
    ) = service.updateProject(id, req, principal.id)

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: UUID, @AuthenticationPrincipal principal: AuthPrincipal) {
        service.deleteProject(id, principal.id)
    }
}