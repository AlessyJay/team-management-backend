package com.example.team_management_backend.member

import com.example.team_management_backend.common.MemberRole
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.stereotype.Repository
import java.util.*

@Repository
class MemberRepository(val db: JdbcClient) {

    private fun rowMapper() = { rs: java.sql.ResultSet, _: Int ->
        ProjectMember(
            id = UUID.fromString(rs.getString("id")),
            projectId = UUID.fromString(rs.getString("project_id")),
            userId = UUID.fromString(rs.getString("user_id")),
            email = rs.getString("email"),
            name = rs.getString("name"),
            role = MemberRole.valueOf(rs.getString("role")),
            joinedAt = rs.getTimestamp("joined_at").toInstant()
        )
    }

    fun findAll(projectId: UUID): List<ProjectMember> =
        db.sql(
            """
            SELECT pm.*, u.email, u.name
            FROM project_members pm
            JOIN users u ON u.id = pm.user_id
            WHERE pm.project_id = :projectId
            ORDER BY pm.joined_at ASC
            """
        ).param("projectId", projectId).query(rowMapper()).list()

    fun add(projectId: UUID, userId: UUID, role: MemberRole): ProjectMember =
        db.sql(
            """
            INSERT INTO project_members (project_id, user_id, role)
            VALUES (:pid, :uid, :role)
            RETURNING *,
                (SELECT email FROM users WHERE id = :uid) AS email,
                (SELECT name  FROM users WHERE id = :uid) AS name
            """
        ).param("pid", projectId).param("uid", userId).param("role", role.name)
            .query(rowMapper()).single()

    fun remove(projectId: UUID, userId: UUID): Int =
        db.sql("DELETE FROM project_members WHERE project_id = :pid AND user_id = :uid")
            .param("pid", projectId).param("uid", userId).update()

    fun findUserByEmail(email: String): UUID? =
        db.sql("SELECT id FROM users WHERE email = :email")
            .param("email", email)
            .query(String::class.java).optional().orElse(null)
            ?.let { UUID.fromString(it) }
}