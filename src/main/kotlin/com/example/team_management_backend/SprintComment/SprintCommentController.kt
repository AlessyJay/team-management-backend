package com.example.team_management_backend.sprint

import com.example.team_management_backend.Auth.AuthPrincipal
import com.example.team_management_backend.SprintComment.CreateCommentRequest
import com.example.team_management_backend.SprintComment.ReactRequest
import com.example.team_management_backend.SprintComment.UpdateCommentRequest
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/projects/{projectId}/sprints/{sprintId}/comments")
class SprintCommentController(private val service: SprintCommentService) {

    @GetMapping
    fun list(
        @PathVariable projectId: UUID,
        @PathVariable sprintId: UUID,
        @AuthenticationPrincipal p: AuthPrincipal,
    ) = service.listComments(projectId, sprintId, p.id)

    @PostMapping
    fun create(
        @PathVariable projectId: UUID,
        @PathVariable sprintId: UUID,
        @Valid @RequestBody req: CreateCommentRequest,
        @AuthenticationPrincipal p: AuthPrincipal,
    ) = service.createComment(projectId, sprintId, req, p.id)

    @PatchMapping("/{commentId}")
    fun update(
        @PathVariable projectId: UUID,
        @PathVariable sprintId: UUID,
        @PathVariable commentId: UUID,
        @Valid @RequestBody req: UpdateCommentRequest,
        @AuthenticationPrincipal p: AuthPrincipal,
    ) = service.updateComment(projectId, sprintId, commentId, req, p.id)

    @DeleteMapping("/{commentId}")
    fun delete(
        @PathVariable projectId: UUID,
        @PathVariable sprintId: UUID,
        @PathVariable commentId: UUID,
        @AuthenticationPrincipal p: AuthPrincipal,
    ): ResponseEntity<Void> {
        service.deleteComment(projectId, sprintId, commentId, p.id)
        return ResponseEntity.noContent().build()
    }

    @PostMapping("/{commentId}/react")
    fun react(
        @PathVariable projectId: UUID,
        @PathVariable sprintId: UUID,
        @PathVariable commentId: UUID,
        @Valid @RequestBody req: ReactRequest,
        @AuthenticationPrincipal p: AuthPrincipal,
    ) = service.toggleReaction(projectId, sprintId, commentId, req, p.id)
}