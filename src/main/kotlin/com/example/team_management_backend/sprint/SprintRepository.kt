package com.example.team_management_backend.sprint

import com.example.team_management_backend.common.SprintStatus
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.stereotype.Repository
import java.sql.Date
import java.util.*

@Repository
class SprintRepository(val db: JdbcClient) {
    private fun rowMapper() = { rs: java.sql.ResultSet, _: Int ->
        Sprint(
            id = rs.getObject("id", UUID::class.java),
            projectId = rs.getObject("project_id", UUID::class.java),
            name = rs.getString("name"),
            goal = rs.getString("goal"),
            startDate = rs.getDate("start_date").toLocalDate(),
            endDate = rs.getDate("end_date").toLocalDate(),
            status = SprintStatus.valueOf(rs.getString("status")),
            createdAt = rs.getTimestamp("created_at").toInstant()
        )
    }

    fun findAll(projectId: UUID): List<Sprint> =
        db.sql("SELECT * FROM sprints WHERE project_id = :pid ORDER BY start_date DESC")
            .param("pid", projectId).query(rowMapper()).list()

    fun findById(id: UUID): Sprint? =
        db.sql("SELECT * FROM sprints WHERE id = :id")
            .param("id", id).query(rowMapper()).optional().orElse(null)

    fun create(projectId: UUID, req: CreateSprintRequest): Sprint =
        db.sql(
            """
            INSERT INTO sprints (project_id, name, goal, start_date, end_date)
            VALUES (:pid, :name, :goal, :startDate, :endDate)
            RETURNING *
        """
        )
            .param("pid", projectId)
            .param("name", req.name)
            .param("goal", req.goal)
            .param("startDate", Date.valueOf(req.startDate))
            .param("endDate", Date.valueOf(req.endDate))
            .query(rowMapper()).single()

    fun update(id: UUID, req: UpdateSprintRequest): Sprint =
        db.sql(
            """
            UPDATE sprints SET
                name       = COALESCE(:name, name),
                goal       = COALESCE(:goal, goal),
                start_date = COALESCE(:startDate, start_date),
                end_date   = COALESCE(:endDate, end_date)
            WHERE id = :id
            RETURNING *
        """
        )
            .param("name", req.name)
            .param("goal", req.goal)
            .param("startDate", req.startDate?.let { Date.valueOf(it) })
            .param("endDate", req.endDate?.let { Date.valueOf(it) })
            .param("id", id)
            .query(rowMapper()).single()

    fun updateStatus(id: UUID, status: SprintStatus): Sprint =
        db.sql("UPDATE sprints SET status = :status WHERE id = :id RETURNING *")
            .param("status", status.name).param("id", id)
            .query(rowMapper()).single()

    fun hasActiveSprint(projectId: UUID): Boolean =
        db.sql("SELECT COUNT(*) FROM sprints WHERE project_id = :pid AND status = 'ACTIVE'")
            .param("pid", projectId).query(Int::class.java).single() > 0
}