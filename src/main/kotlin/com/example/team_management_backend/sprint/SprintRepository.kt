package com.example.team_management_backend.sprint

import com.example.team_management_backend.common.SprintStatus
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.stereotype.Repository
import java.sql.Date
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

@Repository
class SprintRepository(val db: JdbcClient) {
    private val durationDays = mapOf(
        "1 week" to 7L, "2 weeks" to 14L, "1 month" to 30L,
        "6 weeks" to 42L, "2 months" to 60L, "3 months" to 90L,
    )

    private fun sprintRowMapper() = { rs: java.sql.ResultSet, _: Int ->
        val tagsStr = rs.getString("tags")
        Sprint(
            id = UUID.fromString(rs.getString("id")),
            projectId = UUID.fromString(rs.getString("project_id")),
            name = rs.getString("name"),
            goal = rs.getString("goal"),
            category = rs.getString("category"),
            tags = tagsStr?.split(",")?.filter { it.isNotBlank() } ?: emptyList(),
            expectedDuration = rs.getString("expected_duration"),
            startDate = rs.getDate("start_date").toLocalDate(),
            endDate = rs.getDate("end_date")?.toLocalDate(),
            status = SprintStatus.valueOf(rs.getString("status")),
            leads = emptyList(),
            members = emptyList(),
            issueCount = rs.getInt("issue_count"),
            doneCount = rs.getInt("done_count"),
            createdAt = rs.getTimestamp("created_at").toInstant(),
        )
    }

    private fun memberRowMapper() = { rs: java.sql.ResultSet, _: Int ->
        Pair(
            UUID.fromString(rs.getString("sprint_id")),
            SprintMemberDto(
                userId = UUID.fromString(rs.getString("user_id")),
                name = rs.getString("name"),
                email = rs.getString("email"),
                role = rs.getString("role"),
            )
        )
    }

    private val baseSelect = """
        SELECT
            s.*,
            (SELECT COUNT(*) FROM issues i WHERE i.sprint_id = s.id)                        AS issue_count,
            (SELECT COUNT(*) FROM issues i WHERE i.sprint_id = s.id AND i.status = 'DONE')  AS done_count
        FROM sprints s
    """.trimIndent()

    private fun enrich(sprints: List<Sprint>): List<Sprint> {
        if (sprints.isEmpty()) return emptyList()
        val ids = sprints.map { it.id.toString() }
        val placeholders = ids.indices.joinToString(",") { ":id$it" }
        var q = db.sql(
            """
            SELECT sm.sprint_id, sm.user_id, sm.role, u.name, u.email
            FROM sprint_members sm
            JOIN users u ON u.id = sm.user_id
            WHERE sm.sprint_id IN ($placeholders)
            """
        )
        ids.forEachIndexed { i, id -> q = q.param("id$i", id) }
        val rows = q.query(memberRowMapper()).list()
        val bySprintId = rows.groupBy({ it.first }, { it.second })
        return sprints.map { s ->
            val all = bySprintId[s.id] ?: emptyList()
            s.copy(leads = all.filter { it.role == "LEAD" }, members = all.filter { it.role == "MEMBER" })
        }
    }

    private fun resolveDates(req: CreateSprintRequest): Pair<LocalDate, LocalDate?> {
        if (req.expectedDuration != null && req.startDate == null) {
            val days = durationDays[req.expectedDuration] ?: error("Unknown duration: ${req.expectedDuration}")
            val start = LocalDate.now()
            return start to start.plusDays(days)
        }
        return (req.startDate ?: LocalDate.now()) to req.endDate
    }

    fun findAll(projectId: UUID): List<Sprint> {
        val sprints = db.sql("$baseSelect WHERE s.project_id = :pid ORDER BY s.created_at DESC")
            .param("pid", projectId.toString()).query(sprintRowMapper()).list()
        return enrich(sprints)
    }

    fun findById(id: UUID): Sprint? {
        val sprint = db.sql("$baseSelect WHERE s.id = :id")
            .param("id", id.toString()).query(sprintRowMapper()).optional().orElse(null) ?: return null
        return enrich(listOf(sprint)).first()
    }

    fun create(projectId: UUID, req: CreateSprintRequest): Sprint {
        val (startDate, endDate) = resolveDates(req)
        val tagsStr = req.tags.filter { it.isNotBlank() }.joinToString(",").ifBlank { null }

        val sprint = db.sql(
            """
            INSERT INTO sprints
                (id, project_id, name, goal, category, tags, status, expected_duration, start_date, end_date, created_at, updated_at)
            VALUES
                (:id, :pid, :name, :goal, :category, :tags, :status, :expectedDuration, :startDate, :endDate, :created_at, :updated_at)
            RETURNING *,
                0 AS issue_count,
                0 AS done_count
            """
        )
            .param("id", UUID.randomUUID())
            .param("pid", projectId.toString())
            .param("name", req.name)
            .param("goal", req.goal)
            .param("category", req.category)
            .param("tags", tagsStr)
            .param("status", req.status.name)
            .param("expectedDuration", req.expectedDuration)
            .param("startDate", Date.valueOf(startDate))
            .param("endDate", endDate?.let { Date.valueOf(it) })
            .param("created_at", LocalDateTime.now())
            .param("updated_at", LocalDateTime.now())
            .query(sprintRowMapper()).single()

        val allToAdd =
            req.leadIds.map { it to "LEAD" } +
                    req.memberIds.map { it to "MEMBER" }

        allToAdd.forEach { (userId, role) ->
            db.sql(
                """
                INSERT INTO sprint_members (id, sprint_id, user_id, role, joined_at)
                VALUES (:id, :sid, :uid, :role, :joined_at)
                ON CONFLICT (sprint_id, user_id) DO UPDATE SET role = :role
                """
            )
                .param("id", UUID.randomUUID())
                .param("sid", sprint.id.toString())
                .param("uid", userId.toString())
                .param("role", role)
                .param("joined_at", LocalDateTime.now())
                .update()
        }

        return enrich(listOf(sprint)).first()
    }

    fun update(id: UUID, req: UpdateSprintRequest): Sprint {
        val tagsStr = req.tags?.filter { it.isNotBlank() }?.joinToString(",")
        db.sql(
            """
            UPDATE sprints SET
                name              = COALESCE(:name, name),
                goal              = COALESCE(:goal, goal),
                category          = COALESCE(:category, category),
                tags              = COALESCE(:tags, tags),
                expected_duration = COALESCE(:expectedDuration, expected_duration),
                start_date        = COALESCE(:startDate, start_date),
                end_date          = COALESCE(:endDate, end_date),
                updated_at        = now()
            WHERE id = :id
            """
        )
            .param("name", req.name).param("goal", req.goal).param("category", req.category)
            .param("tags", tagsStr).param("expectedDuration", req.expectedDuration)
            .param("startDate", req.startDate?.let { Date.valueOf(it) })
            .param("endDate", req.endDate?.let { Date.valueOf(it) })
            .param("id", id.toString()).update()
        return findById(id)!!
    }

    fun updateGoal(id: UUID, goal: String): Sprint {
        db.sql("UPDATE sprints SET goal = :goal, updated_at = now() WHERE id = :id")
            .param("goal", goal).param("id", id.toString()).update()
        return findById(id)!!
    }

    fun updateStatus(id: UUID, status: SprintStatus): Sprint {
        val extra =
            if (status == SprintStatus.COMPLETED || status == SprintStatus.CLOSED) ", completed_at = now()" else ""
        db.sql("UPDATE sprints SET status = :status$extra WHERE id = :id")
            .param("status", status.name).param("id", id.toString()).update()
        return findById(id)!!
    }

    fun delete(id: UUID) = db.sql("DELETE FROM sprints WHERE id = :id").param("id", id.toString()).update()

    fun hasActiveSprint(projectId: UUID): Boolean =
        db.sql("SELECT COUNT(*) FROM sprints WHERE project_id = :pid AND status = 'ACTIVE'")
            .param("pid", projectId.toString()).query(Int::class.java).single() > 0

    fun isSprintMember(sprintId: UUID, userId: UUID): Boolean =
        db.sql("SELECT COUNT(*) FROM sprint_members WHERE sprint_id = :sid AND user_id = :uid")
            .param("sid", sprintId.toString()).param("uid", userId.toString())
            .query(Int::class.java).single() > 0

    fun isSprintLead(sprintId: UUID, userId: UUID): Boolean =
        db.sql("SELECT COUNT(*) FROM sprint_members WHERE sprint_id = :sid AND user_id = :uid AND role = 'LEAD'")
            .param("sid", sprintId.toString()).param("uid", userId.toString())
            .query(Int::class.java).single() > 0
}