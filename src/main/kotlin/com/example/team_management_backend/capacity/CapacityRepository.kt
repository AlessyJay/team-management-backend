package com.example.team_management_backend.capacity

import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.stereotype.Repository
import java.util.*

@Repository
class CapacityRepository(val db: JdbcClient) {
    fun getSprintCapacityReport(sprintId: UUID): List<MemberLoad> =
        db.sql(
            """
            SELECT
                u.id                                              AS user_id,
                u.name,
                u.email,
                COALESCE(sc.capacity_points, 0)                  AS capacity_points,
                COALESCE(SUM(i.story_points), 0)::INT            AS assigned_points
            FROM project_members pm
            JOIN users u ON u.id = pm.user_id
            JOIN sprints s ON s.id = :sprintId AND s.project_id = pm.project_id
            LEFT JOIN sprint_capacity sc ON sc.sprint_id = :sprintId AND sc.user_id = u.id
            LEFT JOIN issues i
                ON i.sprint_id = :sprintId
                AND i.assignee_id = u.id
            GROUP BY u.id, u.name, u.email, sc.capacity_points
            ORDER BY u.name
        """
        )
            .param("sprintId", sprintId)
            .query { rs, _ ->
                val capacity = rs.getInt("capacity_points")
                val assigned = rs.getInt("assigned_points")
                MemberLoad(
                    userId = rs.getObject("user_id", UUID::class.java),
                    name = rs.getString("name"),
                    email = rs.getString("email"),
                    capacityPoints = capacity,
                    assignedPoints = assigned,
                    utilization = if (capacity > 0) assigned.toDouble() / capacity else 0.0,
                    isOverCapacity = assigned > capacity && capacity > 0
                )
            }.list()

    fun upsertCapacity(sprintId: UUID, userId: UUID, points: Int) =
        db.sql(
            """
            INSERT INTO sprint_capacity (sprint_id, user_id, capacity_points)
            VALUES (:sprintId, :userId, :points)
            ON CONFLICT (sprint_id, user_id)
            DO UPDATE SET capacity_points = :points
        """
        )
            .param("sprintId", sprintId)
            .param("userId", userId)
            .param("points", points)
            .update()
}