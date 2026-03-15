package com.example.team_management_backend.org

import com.example.team_management_backend.Auth.AuthPrincipal
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/orgs")
class OrgController(private val service: OrgService) {

    @GetMapping
    fun list(@AuthenticationPrincipal p: AuthPrincipal) =
        service.listOrgs(p.id)

    @GetMapping("/{orgId}")
    fun get(@PathVariable orgId: UUID, @AuthenticationPrincipal p: AuthPrincipal) =
        service.getOrg(orgId, p.id)

    @GetMapping("/slug/{slug}")
    fun getBySlug(@PathVariable slug: String, @AuthenticationPrincipal p: AuthPrincipal) =
        service.getOrgBySlug(slug, p.id)

    @PostMapping
    fun create(@Valid @RequestBody req: CreateOrgRequest, @AuthenticationPrincipal p: AuthPrincipal) =
        service.createOrg(req, p.id)

    @PatchMapping("/{orgId}")
    fun update(
        @PathVariable orgId: UUID,
        @Valid @RequestBody req: UpdateOrgRequest,
        @AuthenticationPrincipal p: AuthPrincipal,
    ) = service.updateOrg(orgId, req, p.id)

    @DeleteMapping("/{orgId}")
    fun delete(@PathVariable orgId: UUID, @AuthenticationPrincipal p: AuthPrincipal): ResponseEntity<Void> {
        service.deleteOrg(orgId, p.id)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/{orgId}/members")
    fun listMembers(@PathVariable orgId: UUID, @AuthenticationPrincipal p: AuthPrincipal) =
        service.listMembers(orgId, p.id)

    @PostMapping("/{orgId}/members")
    fun inviteMember(
        @PathVariable orgId: UUID,
        @Valid @RequestBody req: InviteMemberRequest,
        @AuthenticationPrincipal p: AuthPrincipal,
    ) = service.inviteMember(orgId, req, p.id)

    @PatchMapping("/{orgId}/members/{userId}")
    fun updateRole(
        @PathVariable orgId: UUID,
        @PathVariable userId: UUID,
        @Valid @RequestBody req: UpdateMemberRoleRequest,
        @AuthenticationPrincipal p: AuthPrincipal,
    ) = service.updateMemberRole(orgId, userId, req, p.id)

    @DeleteMapping("/{orgId}/members/{userId}")
    fun removeMember(
        @PathVariable orgId: UUID,
        @PathVariable userId: UUID,
        @AuthenticationPrincipal p: AuthPrincipal,
    ): ResponseEntity<Void> {
        service.removeMember(orgId, userId, p.id)
        return ResponseEntity.noContent().build()
    }
}