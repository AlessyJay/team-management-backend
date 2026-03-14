package com.example.team_management_backend.sprint

import com.example.team_management_backend.SprintComment.CommentAuthorDto
import com.example.team_management_backend.SprintComment.CreateCommentRequest
import com.example.team_management_backend.SprintComment.ReactionSummaryDto
import com.example.team_management_backend.SprintComment.SprintCommentDto
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.stereotype.Repository
import java.time.Instant
import java.time.LocalDateTime
import java.util.*

@Repository
class SprintCommentRepository(val db: JdbcClient) {
    private data class RawComment(
        val id: UUID,
        val sprintId: UUID,
        val authorId: UUID,
        val authorName: String,
        val authorEmail: String,
        val content: String,
        val parentId: UUID?,
        val createdAt: Instant,
        val updatedAt: Instant,
    )

    private fun rawMapper() = { rs: java.sql.ResultSet, _: Int ->
        val parentStr = rs.getString("parent_id")
        RawComment(
            id = UUID.fromString(rs.getString("id")),
            sprintId = UUID.fromString(rs.getString("sprint_id")),
            authorId = UUID.fromString(rs.getString("user_id")),
            authorName = rs.getString("author_name"),
            authorEmail = rs.getString("author_email"),
            content = rs.getString("content"),
            parentId = if (parentStr != null) UUID.fromString(parentStr) else null,
            createdAt = rs.getTimestamp("created_at").toInstant(),
            updatedAt = rs.getTimestamp("updated_at").toInstant(),
        )
    }

    fun findAllForSprint(sprintId: UUID, viewerUserId: UUID): List<SprintCommentDto> {
        val raws = db.sql(
            """
            SELECT c.id, c.sprint_id, c.user_id, c.parent_id, c.content,
                   c.created_at, c.updated_at,
                   u.name AS author_name, u.email AS author_email
            FROM sprint_comments c
            JOIN users u ON u.id = c.user_id
            WHERE c.sprint_id = :sid
            ORDER BY c.created_at ASC
            """
        ).param("sid", sprintId.toString()).query(rawMapper()).list()

        if (raws.isEmpty()) return emptyList()

        val ids = raws.map { it.id.toString() }
        val placeholders = ids.indices.joinToString(",") { ":cid$it" }
        var q = db.sql(
            """
            SELECT r.comment_id,
                   r.reaction,
                   COUNT(*)                                               AS cnt,
                   SUM(CASE WHEN r.user_id = :viewer THEN 1 ELSE 0 END) AS viewer_count
            FROM comment_reactions r
            WHERE r.comment_id IN ($placeholders)
            GROUP BY r.comment_id, r.reaction
            """
        ).param("viewer", viewerUserId.toString())
        ids.forEachIndexed { i, id -> q = q.param("cid$i", id) }

        data class RxRow(val commentId: UUID, val reaction: String, val count: Int, val userReacted: Boolean)

        val rxRows = q.query { rs, _ ->
            RxRow(
                commentId = UUID.fromString(rs.getString("comment_id")),
                reaction = rs.getString("reaction"),
                count = rs.getInt("cnt"),
                userReacted = rs.getInt("viewer_count") > 0,
            )
        }.list()

        val rxByComment = rxRows.groupBy({ it.commentId }) {
            ReactionSummaryDto(it.reaction, it.count, it.userReacted)
        }

        fun RawComment.toDto(replies: List<SprintCommentDto> = emptyList()) = SprintCommentDto(
            id = id,
            sprintId = sprintId,
            author = CommentAuthorDto(authorId, authorName, authorEmail),
            content = content,
            parentId = parentId,
            reactions = rxByComment[id] ?: emptyList(),
            replies = replies,
            createdAt = createdAt,
            updatedAt = updatedAt,
            edited = updatedAt.isAfter(createdAt.plusSeconds(2)),
        )

        val topLevel = raws.filter { it.parentId == null }
        val repliesByParent = raws.filter { it.parentId != null }
            .groupBy { it.parentId!! }
            .mapValues { (_, rs) -> rs.map { it.toDto() } }

        return topLevel.map { it.toDto(replies = repliesByParent[it.id] ?: emptyList()) }
    }

    fun create(sprintId: UUID, userId: UUID, req: CreateCommentRequest): SprintCommentDto {
        val id = UUID.randomUUID()
        db.sql(
            """
            INSERT INTO sprint_comments (id, sprint_id, user_id, parent_id, content, created_at, updated_at)
            VALUES (:id, :sid, :uid, :parentId, :content, :created_at, :updated_at)
            """
        )
            .param("id", id.toString())
            .param("sid", sprintId.toString())
            .param("uid", userId.toString())
            .param("parentId", req.parentId?.toString())
            .param("content", req.content)
            .param("created_at", LocalDateTime.now())
            .param("updated_at", LocalDateTime.now())
            .update()

        return findById(id, userId)!!
    }

    fun update(commentId: UUID, content: String): SprintCommentDto? {
        db.sql("UPDATE sprint_comments SET content = :content, updated_at = now() WHERE id = :id")
            .param("content", content).param("id", commentId.toString()).update()
        return findById(commentId, UUID.randomUUID()) // reactions don't matter for own edit
    }

    fun delete(commentId: UUID) =
        db.sql("DELETE FROM sprint_comments WHERE id = :id OR parent_id = :id")
            .param("id", commentId.toString()).update()

    fun findById(id: UUID, viewerUserId: UUID): SprintCommentDto? {
        val raw = db.sql(
            """
            SELECT c.id, c.sprint_id, c.user_id, c.parent_id, c.content,
                   c.created_at, c.updated_at,
                   u.name AS author_name, u.email AS author_email
            FROM sprint_comments c JOIN users u ON u.id = c.user_id
            WHERE c.id = :id
            """
        ).param("id", id.toString()).query(rawMapper()).optional().orElse(null) ?: return null

        val rxRows = db.sql(
            """
            SELECT reaction, COUNT(*) AS cnt,
                   SUM(CASE WHEN user_id = :viewer THEN 1 ELSE 0 END) AS viewer_count
            FROM comment_reactions WHERE comment_id = :cid
            GROUP BY reaction
            """
        ).param("viewer", viewerUserId.toString()).param("cid", id.toString())
            .query { rs, _ ->
                ReactionSummaryDto(rs.getString("reaction"), rs.getInt("cnt"), rs.getInt("viewer_count") > 0)
            }.list()

        return SprintCommentDto(
            id = raw.id, sprintId = raw.sprintId,
            author = CommentAuthorDto(raw.authorId, raw.authorName, raw.authorEmail),
            content = raw.content, parentId = raw.parentId,
            reactions = rxRows, replies = emptyList(),
            createdAt = raw.createdAt, updatedAt = raw.updatedAt,
            edited = raw.updatedAt.isAfter(raw.createdAt.plusSeconds(2)),
        )
    }

    fun getOwnerId(commentId: UUID): UUID? =
        db.sql("SELECT user_id FROM sprint_comments WHERE id = :id")
            .param("id", commentId.toString())
            .query(String::class.java).optional().orElse(null)?.let { UUID.fromString(it) }

    fun toggleReaction(commentId: UUID, userId: UUID, reaction: String): Boolean {
        val exists = db.sql(
            "SELECT COUNT(*) FROM comment_reactions WHERE comment_id = :cid AND user_id = :uid AND reaction = :r"
        ).param("cid", commentId.toString()).param("uid", userId.toString()).param("r", reaction)
            .query(Int::class.java).single() > 0

        if (exists) {
            db.sql("DELETE FROM comment_reactions WHERE comment_id = :cid AND user_id = :uid AND reaction = :r")
                .param("cid", commentId.toString()).param("uid", userId.toString()).param("r", reaction).update()
            return false
        } else {
            db.sql(
                "INSERT INTO comment_reactions (id, comment_id, user_id, reaction, created_at) VALUES (:id, :cid, :uid, :r, :created_at)"
            )
                .param("id", UUID.randomUUID().toString())
                .param("cid", commentId.toString())
                .param("uid", userId.toString())
                .param("r", reaction)
                .param("created_at", LocalDateTime.now()).update()
            return true
        }
    }
}