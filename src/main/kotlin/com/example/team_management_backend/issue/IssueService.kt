package com.example.team_management_backend.issue

import com.example.team_management_backend.common.NotFoundException
import com.example.team_management_backend.project.ProjectService
import org.springframework.stereotype.Service
import java.util.*

@Service
class IssueService(
    private val repo: IssueRepository,
    private val projectService: ProjectService
) {
    fun listIssues(projectId: UUID, sprintId: UUID?, userId: UUID): List<Issue> {
        projectService.requireMember(projectId, userId)
        return repo.findAll(projectId, sprintId)
    }

    fun createIssue(projectId: UUID, req: CreateIssueRequest, userId: UUID): Issue {
        projectService.requireMember(projectId, userId)
        return repo.create(projectId, req, userId)
    }

    fun updateIssue(projectId: UUID, issueId: UUID, req: UpdateIssueRequest, userId: UUID): Issue {
        projectService.requireMember(projectId, userId)
        repo.findById(issueId) ?: throw NotFoundException("Issue not found")
        return repo.update(issueId, req)
    }

    fun assignIssue(projectId: UUID, issueId: UUID, req: AssignIssueRequest, userId: UUID): Issue {
        projectService.requireMember(projectId, userId)
        repo.findById(issueId) ?: throw NotFoundException("Issue not found")
        return repo.assign(issueId, req.assigneeId)
    }

    fun moveToSprint(projectId: UUID, issueId: UUID, req: MoveToSprintRequest, userId: UUID): Issue {
        projectService.requireMember(projectId, userId)
        repo.findById(issueId) ?: throw NotFoundException("Issue not found")
        return repo.moveToSprint(issueId, req.sprintId)
    }

    fun deleteIssue(projectId: UUID, issueId: UUID, userId: UUID) {
        projectService.requireMember(projectId, userId)
        repo.findById(issueId) ?: throw NotFoundException("Issue not found")
        repo.delete(issueId)
    }
}