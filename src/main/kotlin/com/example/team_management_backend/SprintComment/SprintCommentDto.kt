package com.example.team_management_backend.SprintComment

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.Instant
import java.util.*

data class CommentAuthorDto(
    val userId: UUID,
    val name: String,
    val email: String,
)

data class ReactionSummaryDto(
    val reaction: String,
    val count: Int,
    val userReacted: Boolean,
)

data class SprintCommentDto(
    val id: UUID,
    val sprintId: UUID,
    val author: CommentAuthorDto,
    val content: String,
    val parentId: UUID?,
    val reactions: List<ReactionSummaryDto>,
    val replies: List<SprintCommentDto>,
    val createdAt: Instant,
    val updatedAt: Instant,
    val edited: Boolean,
)

data class CreateCommentRequest(
    @field:NotBlank @field:Size(max = 2000) val content: String,
    val parentId: UUID? = null,
)

data class UpdateCommentRequest(
    @field:NotBlank @field:Size(max = 2000) val content: String,
)

data class ReactRequest(
    val reaction: String,
)