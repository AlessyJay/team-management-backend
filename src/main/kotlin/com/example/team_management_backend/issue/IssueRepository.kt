package com.example.team_management_backend.issue

import com.example.team_management_backend.common.IssuePriority
import com.example.team_management_backend.common.IssueStatus
import com.example.team_management_backend.common.IssueType
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.stereotype.Repository
import java.util.*

@Repository
class IssueRepository(val db: JdbcClient) {
    private fun rowMapper() = { rs: java.sql.ResultSet, _: Int ->
        Issue(
            id = rs.getObject("id", UUID::class.java),
            projectId = rs.getObject("project_id", UUID::class.java),
            sprintId = rs.getObject("sprint_id", UUID::class.java),
            title = rs.getString("title"),
            description = rs.getString("description"),
            type = IssueType.valueOf(rs.getString("type")),
            status = IssueStatus.valueOf(rs.getString("status")),
            priority = IssuePriority.valueOf(rs.getString("priority")),
            storyPoints = rs.getObject("story_points") as? Int,
            assigneeId = rs.getObject("assignee_id", UUID::class.java),
            assigneeName = rs.getString("assignee_name"),
            createdBy = rs.getObject("created_by", UUID::class.java),
            createdAt = rs.getTimestamp("created_at").toInstant(),
            updatedAt = rs.getTimestamp("updated_at").toInstant()
        )
    }

    fun findAll(projectId: UUID, sprintId: UUID?): List<Issue> {
        val sprintCondition = if (sprintId != null) "AND i.sprint_id = :sprintId" else "AND i.sprint_id IS NULL"
        return db.sql(
            """
            SELECT i.*, u.name AS assignee_name
            FROM issues i
            LEFT JOIN users u ON u.id = i.assignee_id
            WHERE i.project_id = :projectId $sprintCondition
            ORDER BY i.priority DESC, i.created_at DESC
        """
        )
            .param("projectId", projectId)
            .also { query -> sprintId?.let { query.param("sprintId", it) } }
            .query(rowMapper()).list()
    }

    fun findById(id: UUID): Issue? =
        db.sql(
            """
            SELECT i.*, u.name AS assignee_name
            FROM issues i
            LEFT JOIN users u ON u.id = i.assignee_id
            WHERE i.id = :id
        """
        )
            .param("id", id).query(rowMapper()).optional().orElse(null)

    fun create(projectId: UUID, req: CreateIssueRequest, createdBy: UUID): Issue =
        db.sql(
            """
            WITH inserted AS (
                INSERT INTO issues (project_id, sprint_id, title, description, type, priority, story_points, created_by)
                VALUES (:pid, :sprintId, :title, :description, :type, :priority, :storyPoints, :createdBy)
                RETURNING *
            )
            SELECT i.*, u.name AS assignee_name
            FROM inserted i
            LEFT JOIN users u ON u.id = i.assignee_id
        """
        )
            .param("pid", projectId)
            .param("sprintId", req.sprintId)
            .param("title", req.title)
            .param("description", req.description)
            .param("type", req.type.name)
            .param("priority", req.priority.name)
            .param("storyPoints", req.storyPoints)
            .param("createdBy", createdBy)
            .query(rowMapper()).single()

    fun update(id: UUID, req: UpdateIssueRequest): Issue =
        db.sql(
            """
            WITH updated AS (
                UPDATE issues SET
                    title        = COALESCE(:title, title),
                    description  = COALESCE(:description, description),
                    type         = COALESCE(:type::varchar, type),
                    status       = COALESCE(:status::varchar, status),
                    priority     = COALESCE(:priority::varchar, priority),
                    story_points = COALESCE(:storyPoints, story_points),
                    updated_at   = now()
                WHERE id = :id
                RETURNING *
            )
            SELECT u.*, usr.name AS assignee_name
            FROM updated u
            LEFT JOIN users usr ON usr.id = u.assignee_id
        """
        )
            .param("title", req.title)
            .param("description", req.description)
            .param("type", req.type?.name)
            .param("status", req.status?.name)
            .param("priority", req.priority?.name)
            .param("storyPoints", req.storyPoints)
            .param("id", id)
            .query(rowMapper()).single()

    fun assign(id: UUID, assigneeId: UUID?): Issue =
        db.sql(
            """
            WITH updated AS (
                UPDATE issues SET assignee_id = :assigneeId, updated_at = now()
                WHERE id = :id RETURNING *
            )
            SELECT u.*, usr.name AS assignee_name
            FROM updated u LEFT JOIN users usr ON usr.id = u.assignee_id
        """
        )
            .param("assigneeId", assigneeId).param("id", id)
            .query(rowMapper()).single()

    fun moveToSprint(id: UUID, sprintId: UUID?): Issue =
        db.sql(
            """
            WITH updated AS (
                UPDATE issues SET sprint_id = :sprintId, updated_at = now()
                WHERE id = :id RETURNING *
            )
            SELECT u.*, usr.name AS assignee_name
            FROM updated u LEFT JOIN users usr ON usr.id = u.assignee_id
        """
        )
            .param("sprintId", sprintId).param("id", id)
            .query(rowMapper()).single()

    fun delete(id: UUID) =
        db.sql("DELETE FROM issues WHERE id = :id").param("id", id).update()
}