package com.example.team_management_backend.Entities

import jakarta.persistence.*
import java.time.OffsetDateTime

@Entity
@Table(
    name = "comment_reactions",
    uniqueConstraints = [UniqueConstraint(columnNames = ["comment_id", "user_id", "reaction"])]
)
data class CommentReaction(
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    var id: String = "",

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "comment_id", nullable = false)
    var comment: SprintComment,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    var user: User,

    @Column(nullable = false, length = 20)
    var reaction: String = "",

    @Column(name = "created_at", nullable = false)
    var createdAt: OffsetDateTime = OffsetDateTime.now(),
) {
    @PrePersist
    fun prePersist() {
        createdAt = OffsetDateTime.now()
    }
}