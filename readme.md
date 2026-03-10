# Team Management Backend

A REST API for a project management tool built with Spring Boot and Kotlin. It covers the full lifecycle of a software
project from creating a team and planning sprints to tracking issues on a board and monitoring member capacity.

## Tech Stack

- **Runtime:** Java 21
- **Language:** Kotlin 2.2
- **Framework:** Spring Boot 4.0.3
- **Security:** Spring Security 7 with JWT (jjwt 0.12.6)
- **Database:** PostgreSQL (NeonDB) via Spring Data JPA and Hibernate 7
- **Migrations:** Flyway 11
- **Build Tool:** Gradle

## Prerequisites

- JDK 21 or later
- A PostgreSQL database (the project is configured for NeonDB but works with any PostgreSQL instance)

## Getting Started

Clone the repository and navigate into it:

```bash
git clone <repository-url>
cd team_management_backend
```

Copy the example properties and fill in your own values:

```bash
cp src/main/resources/application.properties.example src/main/resources/application.properties
```

Run the application:

```bash
./gradlew bootRun
```

The server starts on port `8080` by default.

## Configuration

All configuration lives in `src/main/resources/application.properties`. The values you need to set before running are:

```properties
# Your PostgreSQL connection string
spring.datasource.url=jdbc:postgresql://<host>/<db>?sslmode=require
spring.datasource.username=<username>
spring.datasource.password=<password>
# A long, random string used to sign JWTs keep this secret
app.jwt.secret=<your-secret>
# Token lifetimes
app.jwt.access-expiration-ms=120000       # 2 minutes
app.jwt.refresh-expiration-ms=15552000000 # 6 months
```

If you are using NeonDB with connection pooling, note that Flyway requires a direct (non-pooler) connection to run
migrations due to advisory lock requirements. Add a separate Flyway datasource pointing to the direct host if you see
Flyway silently skipping on startup.

## Authentication

The API uses a dual-token approach. Access tokens are short-lived (2 minutes) and are returned in the response body.
Refresh tokens last 6 months they are stored as a SHA-256 hash in the database and sent to the client as an HttpOnly,
SameSite=Strict cookie scoped to `/api/auth`.

On every request to a protected endpoint, include the access token as a Bearer token:

```
Authorization: Bearer <access_token>
```

When the access token expires, call `POST /api/auth/refresh`. The browser or HTTP client will automatically include the
cookie, and you will receive a new access token in return.

## API Reference

All endpoints are prefixed with `/api`. Endpoints marked as **public** do not require authentication. All others require
a valid `Authorization: Bearer` header.

---

### Auth

| Method | Endpoint             | Auth            | Description                                            |
|--------|----------------------|-----------------|--------------------------------------------------------|
| POST   | `/api/auth/register` | Public          | Create a new account                                   |
| POST   | `/api/auth/login`    | Public          | Log in and receive an access token                     |
| POST   | `/api/auth/refresh`  | Public (cookie) | Exchange a refresh token cookie for a new access token |
| POST   | `/api/auth/logout`   | Optional        | Invalidate the refresh token and clear the cookie      |

**Register**

```json
POST /api/auth/register
{
  "name": "Tony",
  "email": "tony@example.com",
  "password": "password123"
}
```

**Login**

```json
POST /api/auth/login
{
  "email": "tony@example.com",
  "password": "password123"
}
```

Response:

```json
{
  "accessToken": "<jwt>",
  "userId": "<uuid>",
  "email": "tony@example.com"
}
```

---

### Projects

| Method | Endpoint             | Description                                                |
|--------|----------------------|------------------------------------------------------------|
| GET    | `/api/projects`      | List all projects the authenticated user belongs to        |
| GET    | `/api/projects/{id}` | Get a single project                                       |
| POST   | `/api/projects`      | Create a project (caller becomes MANAGER automatically)    |
| PATCH  | `/api/projects/{id}` | Update project name, description, or status (MANAGER only) |
| DELETE | `/api/projects/{id}` | Delete a project (owner only)                              |

**Create project**

```json
POST /api/projects
{
  "name": "Project Alpha",
  "description": "Optional description"
}
```

**Update project**

```json
PATCH /api/projects/{id}
{
  "name": "Renamed Project",
  "status": "ARCHIVED"
}
```

Project statuses: `ACTIVE`, `ARCHIVED`

---

### Members

| Method | Endpoint                                     | Description                            |
|--------|----------------------------------------------|----------------------------------------|
| GET    | `/api/projects/{projectId}/members`          | List all members of a project          |
| POST   | `/api/projects/{projectId}/members`          | Add a member by user ID (MANAGER only) |
| DELETE | `/api/projects/{projectId}/members/{userId}` | Remove a member (MANAGER only)         |

**Add member**

```json
POST /api/projects/{projectId}/members
{
  "userId": "<uuid>",
  "role": "MEMBER"
}
```

Member roles: `MANAGER`, `MEMBER`

---

### Sprints

| Method | Endpoint                                                | Description                          |
|--------|---------------------------------------------------------|--------------------------------------|
| GET    | `/api/projects/{projectId}/sprints`                     | List all sprints in a project        |
| POST   | `/api/projects/{projectId}/sprints`                     | Create a sprint (MANAGER only)       |
| PATCH  | `/api/projects/{projectId}/sprints/{sprintId}`          | Update sprint details (MANAGER only) |
| POST   | `/api/projects/{projectId}/sprints/{sprintId}/start`    | Start a sprint (MANAGER only)        |
| POST   | `/api/projects/{projectId}/sprints/{sprintId}/complete` | Complete a sprint (MANAGER only)     |

**Create sprint**

```json
POST /api/projects/{projectId}/sprints
{
  "name": "Sprint 1",
  "goal": "Ship the auth flow",
  "startDate": "2026-03-10",
  "endDate": "2026-03-24"
}
```

Sprint statuses: `PLANNING`, `ACTIVE`, `COMPLETED`

---

### Issues

| Method | Endpoint                                            | Description                                                        |
|--------|-----------------------------------------------------|--------------------------------------------------------------------|
| GET    | `/api/projects/{projectId}/issues`                  | List issues. Filter by sprint with `?sprintId=`. Omit for backlog. |
| POST   | `/api/projects/{projectId}/issues`                  | Create an issue                                                    |
| PATCH  | `/api/projects/{projectId}/issues/{issueId}`        | Update title, description, type, status, priority, or story points |
| PATCH  | `/api/projects/{projectId}/issues/{issueId}/assign` | Assign or unassign an issue to a team member                       |
| PATCH  | `/api/projects/{projectId}/issues/{issueId}/move`   | Move an issue to a sprint, or back to backlog                      |
| DELETE | `/api/projects/{projectId}/issues/{issueId}`        | Delete an issue                                                    |

**Create issue**

```json
POST /api/projects/{projectId}/issues
{
  "title": "Fix login redirect loop",
  "description": "Users are being redirected in a loop after login on Safari",
  "type": "BUG",
  "priority": "HIGH",
  "storyPoints": 3,
  "sprintId": "<uuid>"
}
```

**Assign issue**

```json
PATCH /api/projects/{projectId}/issues/{issueId}/assign
{
  "assigneeId": "<uuid>"
}
```

Pass `null` for `assigneeId` to unassign.

**Move to sprint**

```json
PATCH /api/projects/{projectId}/issues/{issueId}/move
{
  "sprintId": "<uuid>"
}
```

Pass `null` for `sprintId` to move back to the backlog.

Issue types: `STORY`, `TASK`, `BUG`

Issue statuses: `BACKLOG`, `TODO`, `IN_PROGRESS`, `IN_REVIEW`, `DONE`

Issue priorities: `LOW`, `MEDIUM`, `HIGH`, `CRITICAL`

---

### Capacity

| Method | Endpoint                                                         | Description                                         |
|--------|------------------------------------------------------------------|-----------------------------------------------------|
| GET    | `/api/projects/{projectId}/sprints/{sprintId}/capacity`          | Get the capacity report for all members in a sprint |
| PUT    | `/api/projects/{projectId}/sprints/{sprintId}/capacity/{userId}` | Set story point capacity for a member in a sprint   |

**Set capacity**

```json
PUT /api/projects/{projectId}/sprints/{sprintId}/capacity/{userId}
{
  "capacity": 40
}
```

The capacity report response includes each member's set capacity alongside the total story points assigned to them in
that sprint, so you can see utilization at a glance.

---

## Error Responses

All errors follow the same shape:

```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Project not found",
  "timestamp": "2026-03-07T16:09:47.689Z"
}
```

| Status | Meaning                                                                  |
|--------|--------------------------------------------------------------------------|
| 400    | Validation failed check the `message` field for which fields are invalid |
| 401    | Missing or invalid access token, or expired refresh token                |
| 403    | Authenticated but not permitted usually means you need MANAGER role      |
| 404    | Resource does not exist, or credentials are wrong on login               |
| 409    | Conflict email already registered, or duplicate resource                 |
| 500    | Unexpected server error                                                  |

## Project Structure

```
src/main/kotlin/com/example/team_management_backend/
├── Auth/               JWT filter, token provider, auth controller and service
├── Entities/           JPA entity classes used for Hibernate schema validation
├── capacity/           Sprint capacity planning
├── common/             Enums, shared exceptions, global exception handler
├── config/             Spring Security configuration
├── issue/              Issue CRUD, assignment, and sprint movement
├── member/             Project member management
├── project/            Project CRUD
└── sprint/             Sprint lifecycle management

src/main/resources/
├── application.properties
└── db/migration/       Flyway SQL migration files
```

The data access layer uses `JdbcClient` with raw SQL throughout. The JPA entity classes exist solely to let Hibernate
validate that the schema matches expectations on startup no JPQL or Spring Data repositories are used for queries.

## Database Schema

Six tables: `users`, `projects`, `project_members`, `sprints`, `issues`, `sprint_capacity`, and `refresh_tokens`. All
primary keys are UUIDs generated by the application. Foreign keys on child tables use `ON DELETE CASCADE` so deleting a
project removes all its associated data cleanly.