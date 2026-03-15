package com.example.team_management_backend.org

import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.*

@Repository
class OrgRepository(val db: JdbcClient) {
    private fun orgRowMapper(viewerUserId: UUID) = { rs: java.sql.ResultSet, _: Int ->
        OrgDto(
            id = UUID.fromString(rs.getString("id")),
            slug = rs.getString("slug"),
            name = rs.getString("name"),
            description = rs.getString("description"),
            logoUrl = rs.getString("logo_url"),
            ownerId = UUID.fromString(rs.getString("owner_id")),
            memberCount = rs.getInt("member_count"),
            myRole = rs.getString("my_role") ?: "MEMBER",
            createdAt = rs.getTimestamp("created_at").toInstant(),
        )
    }

    private fun memberRowMapper() = { rs: java.sql.ResultSet, _: Int ->
        OrgMemberDto(
            id = UUID.fromString(rs.getString("id")),
            userId = UUID.fromString(rs.getString("user_id")),
            name = rs.getString("name"),
            email = rs.getString("email"),
            role = rs.getString("role"),
            joinedAt = rs.getTimestamp("joined_at").toInstant(),
        )
    }

    fun findAllForUser(userId: UUID): List<OrgDto> = db.sql(
        """
        SELECT o.*,
               (SELECT COUNT(*) FROM organization_members om2 WHERE om2.org_id = o.id) AS member_count,
               om.role AS my_role
        FROM organizations o
        JOIN organization_members om ON om.org_id = o.id AND om.user_id = :uid
        ORDER BY o.name ASC
        """
    ).param("uid", userId.toString()).query(orgRowMapper(userId)).list()

    fun findById(id: UUID, viewerUserId: UUID): OrgDto? = db.sql(
        """
        SELECT o.*,
               (SELECT COUNT(*) FROM organization_members om2 WHERE om2.org_id = o.id) AS member_count,
               om.role AS my_role
        FROM organizations o
        LEFT JOIN organization_members om ON om.org_id = o.id AND om.user_id = :uid
        WHERE o.id = :id
        """
    ).param("uid", viewerUserId.toString()).param("id", id.toString())
        .query(orgRowMapper(viewerUserId)).optional().orElse(null)

    fun findBySlug(slug: String, viewerUserId: UUID): OrgDto? = db.sql(
        """
        SELECT o.*,
               (SELECT COUNT(*) FROM organization_members om2 WHERE om2.org_id = o.id) AS member_count,
               om.role AS my_role
        FROM organizations o
        LEFT JOIN organization_members om ON om.org_id = o.id AND om.user_id = :uid
        WHERE o.slug = :slug
        """
    ).param("uid", viewerUserId.toString()).param("slug", slug)
        .query(orgRowMapper(viewerUserId)).optional().orElse(null)

    fun slugExists(slug: String): Boolean =
        db.sql("SELECT COUNT(*) FROM organizations WHERE slug = :slug")
            .param("slug", slug).query(Int::class.java).single() > 0

    fun create(req: CreateOrgRequest, ownerId: UUID): OrgDto {
        val id = UUID.randomUUID()
        val slug = resolveSlug(req.slug, req.name)

        db.sql(
            """
            INSERT INTO organizations (id, slug, name, description, owner_id, created_at, updated_at)
            VALUES (:id, :slug, :name, :description, :ownerId, :createdAt, :updatedAt)
            """
        )
            .param("id", id.toString())
            .param("slug", slug)
            .param("name", req.name)
            .param("description", req.description)
            .param("ownerId", ownerId.toString())
            .param("createdAt", LocalDateTime.now())
            .param("updatedAt", LocalDateTime.now())
            .update()

        db.sql(
            """
            INSERT INTO organization_members (id, org_id, user_id, role, joined_at)
            VALUES (:id, :orgId, :userId, 'OWNER', :joinedAt)
            """
        )
            .param("id", UUID.randomUUID().toString())
            .param("orgId", id.toString())
            .param("userId", ownerId.toString())
            .param("joinedAt", LocalDateTime.now())
            .update()

        return findById(id, ownerId)!!
    }

    fun update(id: UUID, req: UpdateOrgRequest): OrgDto? {
        db.sql(
            """
            UPDATE organizations SET
                name        = COALESCE(:name, name),
                description = COALESCE(:description, description),
                logo_url    = COALESCE(:logoUrl, logo_url),
                updated_at  = now()
            WHERE id = :id
            """
        )
            .param("name", req.name)
            .param("description", req.description)
            .param("logoUrl", req.logoUrl)
            .param("id", id.toString())
            .update()
        return db.sql(
            """
            SELECT o.*,
                   (SELECT COUNT(*) FROM organization_members om2 WHERE om2.org_id = o.id) AS member_count,
                   (SELECT role FROM organization_members WHERE org_id = o.id LIMIT 1) AS my_role
            FROM organizations o WHERE o.id = :id
            """
        ).param("id", id.toString())
            .query { rs, _ ->
                OrgDto(
                    id = UUID.fromString(rs.getString("id")),
                    slug = rs.getString("slug"),
                    name = rs.getString("name"),
                    description = rs.getString("description"),
                    logoUrl = rs.getString("logo_url"),
                    ownerId = UUID.fromString(rs.getString("owner_id")),
                    memberCount = rs.getInt("member_count"),
                    myRole = rs.getString("my_role") ?: "MEMBER",
                    createdAt = rs.getTimestamp("created_at").toInstant(),
                )
            }.optional().orElse(null)
    }

    fun delete(id: UUID) =
        db.sql("DELETE FROM organizations WHERE id = :id")
            .param("id", id.toString()).update()

    fun findMembers(orgId: UUID): List<OrgMemberDto> = db.sql(
        """
        SELECT om.id, om.user_id, om.role, om.joined_at, u.name, u.email
        FROM organization_members om
        JOIN users u ON u.id = om.user_id
        WHERE om.org_id = :orgId
        ORDER BY
            CASE om.role WHEN 'OWNER' THEN 0 WHEN 'ADMIN' THEN 1 ELSE 2 END,
            om.joined_at ASC
        """
    ).param("orgId", orgId.toString()).query(memberRowMapper()).list()

    fun findUserByEmail(email: String): UUID? =
        db.sql("SELECT id FROM users WHERE email = :email")
            .param("email", email)
            .query(String::class.java).optional().orElse(null)
            ?.let { UUID.fromString(it) }

    fun isMember(orgId: UUID, userId: UUID): Boolean =
        db.sql("SELECT COUNT(*) FROM organization_members WHERE org_id = :oid AND user_id = :uid")
            .param("oid", orgId.toString()).param("uid", userId.toString())
            .query(Int::class.java).single() > 0

    fun getRole(orgId: UUID, userId: UUID): String? =
        db.sql("SELECT role FROM organization_members WHERE org_id = :oid AND user_id = :uid")
            .param("oid", orgId.toString()).param("uid", userId.toString())
            .query(String::class.java).optional().orElse(null)

    fun addMember(orgId: UUID, userId: UUID, role: String) {
        db.sql(
            """
            INSERT INTO organization_members (id, org_id, user_id, role, joined_at)
            VALUES (:id, :orgId, :userId, :role, :joinedAt)
            ON CONFLICT (org_id, user_id) DO UPDATE SET role = :role
            """
        )
            .param("id", UUID.randomUUID().toString())
            .param("orgId", orgId.toString())
            .param("userId", userId.toString())
            .param("role", role)
            .param("joinedAt", LocalDateTime.now())
            .update()
    }

    fun updateMemberRole(orgId: UUID, userId: UUID, role: String) =
        db.sql("UPDATE organization_members SET role = :role WHERE org_id = :oid AND user_id = :uid")
            .param("role", role)
            .param("oid", orgId.toString())
            .param("uid", userId.toString())
            .update()

    fun removeMember(orgId: UUID, userId: UUID) =
        db.sql("DELETE FROM organization_members WHERE org_id = :oid AND user_id = :uid")
            .param("oid", orgId.toString()).param("uid", userId.toString()).update()

    private fun resolveSlug(providedSlug: String?, name: String): String {
        val base = (providedSlug ?: name)
            .lowercase()
            .replace(Regex("[^a-z0-9]+"), "-")
            .trim('-')
            .take(80)

        if (!slugExists(base)) return base

        var i = 2
        while (slugExists("$base-$i")) i++
        return "$base-$i"
    }
}