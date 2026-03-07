package com.example.team_management_backend.project

import com.example.team_management_backend.common.MemberRole
import com.example.team_management_backend.common.ProjectStatus
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.*

@Repository
class ProjectRepository(val db: JdbcClient) {
    private fun rowMapper() = { rs: java.sql.ResultSet, _: Int ->
        ProjectDto(
            id = rs.getObject("id", UUID::class.java),
            name = rs.getString("name"),
            description = rs.getString("description"),
            ownerId = rs.getObject("owner_id", UUID::class.java),
            status = ProjectStatus.valueOf(rs.getString("status")),
            createdAt = rs.getTimestamp("created_at").toInstant()
        )
    }

    fun findAllForUser(userId: UUID): List<ProjectDto> = db.sql(
        """
            select p.* from projects p
            join project_members pm on pm.project_id = p.id
            where pm.user_id = :userId
            order by p.created_at desc
            """
    ).param("userId", userId).query(rowMapper()).list()

    fun findById(id: UUID): ProjectDto? =
        db.sql("select * from projects where id = :id").param("id", id).query(rowMapper()).optional().orElse(null)

    fun create(name: String, description: String?, ownerId: UUID): ProjectDto = db.sql(
        """
            INSERT INTO projects (id, name, description, owner_id, created_at)   -- was ownerId
            VALUES (:id, :name, :description, :ownerId, :created_at)
            RETURNING *
            """
    ).param("id", UUID.randomUUID()).param("name", name).param("description", description).param("ownerId", ownerId)
        .param("created_at", LocalDateTime.now()).query(rowMapper()).single()

    fun update(id: UUID, name: String?, description: String?, status: ProjectStatus?): ProjectDto = db.sql(
        """
                update projects set
                name = coalesce(:name, name),
                description = coalesce(:description, description),
                status = coalesce(:status::varchar, status)
                where id = :id
                returning *
                """
    ).param("name", name).param("description", description).param("status", status?.name).param("id", id)
        .query(rowMapper()).single()

    fun delete(id: UUID) = db.sql(
        "delete from projects where id = :id"
    ).param("id", id).update()

    fun isMember(projectId: UUID, userId: UUID): Boolean =
        db.sql("select count(*) from project_members where project_id = :pid and user_id = :uid")
            .param("pid", projectId).param("uid", userId).query(Int::class.java).single() > 0

    fun getRole(projectId: UUID, userId: UUID): MemberRole? =
        db.sql("select role from project_members where project_id = :pid and user_id = :uid").param("pid", projectId)
            .param("uid", userId).query(String::class.java).optional().orElse(null)?.let { MemberRole.valueOf(it) }
}