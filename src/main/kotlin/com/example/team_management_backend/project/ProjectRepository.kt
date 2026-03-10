package com.example.team_management_backend.project

import com.example.team_management_backend.common.MemberRole
import com.example.team_management_backend.common.ProjectStatus
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.stereotype.Repository
import java.time.Instant
import java.time.LocalDateTime
import java.util.*

@Repository
class ProjectRepository(val db: JdbcClient) {
    private fun rowMapper() = { rs: java.sql.ResultSet, _: Int ->
        val sprintIdStr = rs.getString("sprint_id")
        ProjectDto(
            id = UUID.fromString(rs.getString("id")),
            name = rs.getString("name"),
            description = rs.getString("description"),
            ownerId = UUID.fromString(rs.getString("owner_id")),
            status = ProjectStatus.valueOf(rs.getString("status")),
            createdAt = rs.getTimestamp("created_at").toInstant(),
            joinedAt = rs.getTimestamp("joined_at").toInstant(),
            memberCount = rs.getInt("member_count"),
            activeSprint = sprintIdStr?.let {
                ActiveSprintDto(
                    id = UUID.fromString(it),
                    name = rs.getString("sprint_name"),
                    endDate = rs.getDate("sprint_end_date")?.toLocalDate()
                )
            },
            hasUrgentIssues = false  // computed separately in getProject() only
        )
    }

    private fun simpleRowMapper() = { rs: java.sql.ResultSet, _: Int ->
        ProjectDto(
            id = UUID.fromString(rs.getString("id")),
            name = rs.getString("name"),
            description = rs.getString("description"),
            ownerId = UUID.fromString(rs.getString("owner_id")),
            status = ProjectStatus.valueOf(rs.getString("status")),
            createdAt = rs.getTimestamp("created_at").toInstant(),
            joinedAt = Instant.EPOCH,
            memberCount = 0,
            activeSprint = null,
            hasUrgentIssues = false
        )
    }

    fun findAllForUser(userId: UUID): List<ProjectDto> = db.sql(
        """
        SELECT
            p.*,
            pm.joined_at,
            (SELECT COUNT(*) FROM project_members pm2
             WHERE pm2.project_id = p.id) AS member_count,
            s.id       AS sprint_id,
            s.name     AS sprint_name,
            s.end_date AS sprint_end_date
        FROM projects p
        JOIN  project_members pm ON pm.project_id = p.id
        LEFT JOIN sprints s     ON s.project_id   = p.id AND s.status = 'ACTIVE'
        WHERE pm.user_id = :userId
        ORDER BY p.name ASC
        """
    ).param("userId", userId.toString()).query(rowMapper()).list()

    fun findRecentlyViewed(userId: UUID): List<ProjectDto> = db.sql(
        """
        SELECT
            p.*,
            pm.joined_at,
            (SELECT COUNT(*) FROM project_members pm2
             WHERE pm2.project_id = p.id) AS member_count,
            s.id       AS sprint_id,
            s.name     AS sprint_name,
            s.end_date AS sprint_end_date
        FROM projects p
        JOIN  project_members pm ON pm.project_id = p.id AND pm.user_id = :userId
        LEFT JOIN sprints s     ON s.project_id   = p.id AND s.status = 'ACTIVE'
        JOIN  project_views pv  ON pv.project_id  = p.id AND pv.user_id = :userId
        ORDER BY pv.viewed_at DESC
        LIMIT 8
        """
    ).param("userId", userId.toString()).query(rowMapper()).list()

    fun findById(id: UUID): ProjectDto? =
        db.sql("SELECT * FROM projects WHERE id = :id")
            .param("id", id.toString())
            .query(simpleRowMapper()).optional().orElse(null)

    fun create(name: String, description: String?, ownerId: UUID): ProjectDto = db.sql(
        """
        INSERT INTO projects (id, name, description, owner_id, status, created_at, updated_at)
        VALUES (:id, :name, :description, :ownerId, 'ACTIVE', :created_at, :updated_at)
        RETURNING *
        """
    )
        .param("id", UUID.randomUUID().toString())
        .param("name", name)
        .param("description", description)
        .param("ownerId", ownerId.toString())
        .param("created_at", LocalDateTime.now())
        .param("updated_at", LocalDateTime.now())
        .query(simpleRowMapper()).single()

    fun update(id: UUID, name: String?, description: String?, status: ProjectStatus?): ProjectDto = db.sql(
        """
        UPDATE projects SET
            name        = COALESCE(:name, name),
            description = COALESCE(:description, description),
            status      = COALESCE(:status::VARCHAR, status),
            updated_at  = now()
        WHERE id = :id
        RETURNING *
        """
    )
        .param("name", name)
        .param("description", description)
        .param("status", status?.name)
        .param("id", id.toString())
        .query(simpleRowMapper()).single()

    fun delete(id: UUID) =
        db.sql("DELETE FROM projects WHERE id = :id")
            .param("id", id.toString()).update()

    fun isMember(projectId: UUID, userId: UUID): Boolean =
        db.sql("SELECT COUNT(*) FROM project_members WHERE project_id = :pid AND user_id = :uid")
            .param("pid", projectId.toString())
            .param("uid", userId.toString())
            .query(Int::class.java).single() > 0

    fun getRole(projectId: UUID, userId: UUID): MemberRole? =
        db.sql("SELECT role FROM project_members WHERE project_id = :pid AND user_id = :uid")
            .param("pid", projectId.toString())
            .param("uid", userId.toString())
            .query(String::class.java).optional().orElse(null)
            ?.let { MemberRole.valueOf(it) }

    fun hasUrgentIssues(projectId: UUID): Boolean =
        db.sql(
            """
            SELECT COUNT(*) FROM issues
            WHERE project_id = :pid
              AND priority IN ('HIGH', 'CRITICAL')
              AND status != 'DONE'
            """
        ).param("pid", projectId.toString()).query(Int::class.java).single() > 0

    fun upsertView(projectId: UUID, userId: UUID) = db.sql(
        """
        INSERT INTO project_views (user_id, project_id, viewed_at)
        VALUES (:uid, :pid, now())
        ON CONFLICT (user_id, project_id) DO UPDATE SET viewed_at = now()
        """
    ).param("uid", userId.toString()).param("pid", projectId.toString()).update()
}