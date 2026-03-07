package com.example.team_management_backend.capacity

import com.example.team_management_backend.Auth.AuthPrincipal
import jakarta.validation.Valid
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/projects/{projectId}/sprints/{sprintId}/capacity")
class CapacityController(private val service: CapacityService) {
    @GetMapping
    fun getReport(
        @PathVariable projectId: UUID,
        @PathVariable sprintId: UUID,
        @AuthenticationPrincipal principal: AuthPrincipal
    ) = service.getCapacityReport(projectId, sprintId, principal.id)

    @PutMapping("/{userId}")
    fun setCapacity(
        @PathVariable projectId: UUID,
        @PathVariable sprintId: UUID,
        @PathVariable userId: UUID,
        @Valid @RequestBody req: SetCapacityRequest,
        @AuthenticationPrincipal principal: AuthPrincipal
    ) = service.setCapacity(projectId, sprintId, userId, req, principal.id)
}