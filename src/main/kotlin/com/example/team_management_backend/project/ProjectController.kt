package com.example.team_management_backend.project

import com.example.team_management_backend.Auth.AuthPrincipal
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/projects")
class ProjectController(private val service: ProjectService) {

    @GetMapping
    fun list(@AuthenticationPrincipal principal: AuthPrincipal) =
        service.listProjects(principal.id)

    @GetMapping("/recently-viewed")
    fun recentlyViewed(@AuthenticationPrincipal principal: AuthPrincipal) =
        service.getRecentlyViewed(principal.id)

    @GetMapping("/{id}")
    fun get(@PathVariable id: UUID, @AuthenticationPrincipal principal: AuthPrincipal) =
        service.getProject(id, principal.id)

    @PostMapping
    fun create(
        @Valid @RequestBody req: CreateProjectRequest,
        @AuthenticationPrincipal principal: AuthPrincipal
    ) = service.createProject(req, principal.id)

    @PostMapping("/{id}/view")
    fun recordView(
        @PathVariable id: UUID,
        @AuthenticationPrincipal principal: AuthPrincipal
    ): ResponseEntity<Void> {
        service.recordView(id, principal.id)
        return ResponseEntity.noContent().build()
    }

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