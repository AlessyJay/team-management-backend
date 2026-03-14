package com.example.team_management_backend.common

enum class ProjectStatus { ACTIVE, INACTIVE, ARCHIVED }

enum class MemberRole { MANAGER, MEMBER }

enum class SprintStatus { PLANNING, ACTIVE, REVIEWING, REVIEWED, COMPLETED, CLOSED }

enum class IssueType { STORY, TASK, BUG }

enum class IssueStatus { BACKLOG, TODO, IN_PROGRESS, IN_REVIEW, DONE }

enum class IssuePriority { LOW, MEDIUM, HIGH, CRITICAL }

class NotFoundException(message: String) : RuntimeException(message)
class ForbiddenException(message: String) : RuntimeException(message)
class ConflictException(message: String) : RuntimeException(message)
class BadRequestException(message: String) : RuntimeException(message)