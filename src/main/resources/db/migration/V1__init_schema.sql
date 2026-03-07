CREATE TABLE users
(
    id         UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    name       VARCHAR(100) NOT NULL,
    email      VARCHAR(255) NOT NULL UNIQUE,
    password   TEXT         NOT NULL,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE projects
(
    id          UUID PRIMARY KEY     DEFAULT gen_random_uuid(),
    name        TEXT        NOT NULL,
    description TEXT,
    owner_id    UUID        NOT NULL REFERENCES users (id),
    status      VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE project_members
(
    id         UUID PRIMARY KEY     DEFAULT gen_random_uuid(),
    project_id UUID        NOT NULL REFERENCES projects (id) ON DELETE CASCADE,
    user_id    UUID        NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    role       VARCHAR(20) NOT NULL DEFAULT 'MEMBER',
    joined_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (project_id, user_id)
);

CREATE TABLE sprints
(
    id         UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    project_id UUID         NOT NULL REFERENCES projects (id) ON DELETE CASCADE,
    name       VARCHAR(100) NOT NULL,
    goal       TEXT,
    start_date DATE         NOT NULL,
    end_date   DATE,
    status     VARCHAR(20)  NOT NULL DEFAULT 'PLANNING',
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT sprints_dates_check CHECK (end_date IS NULL OR end_date > start_date)
);

CREATE TABLE issues
(
    id           UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    project_id   UUID         NOT NULL REFERENCES projects (id) ON DELETE CASCADE,
    sprint_id    UUID         REFERENCES sprints (id) ON DELETE SET NULL,
    title        VARCHAR(255) NOT NULL,
    description  TEXT,
    type         VARCHAR(20)  NOT NULL DEFAULT 'TASK',
    status       VARCHAR(20)  NOT NULL DEFAULT 'BACKLOG',
    priority     VARCHAR(20)  NOT NULL DEFAULT 'MEDIUM',
    story_points SMALLINT,
    assignee_id  UUID         REFERENCES users (id) ON DELETE SET NULL,
    created_by   UUID         NOT NULL REFERENCES users (id),
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at   TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_issues_project ON issues (project_id);
CREATE INDEX idx_issues_sprint ON issues (sprint_id);

CREATE TABLE sprint_capacity
(
    id              UUID PRIMARY KEY  DEFAULT gen_random_uuid(),
    sprint_id       UUID     NOT NULL REFERENCES sprints (id) ON DELETE CASCADE,
    user_id         UUID     NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    capacity_points SMALLINT NOT NULL DEFAULT 0,
    UNIQUE (sprint_id, user_id)
);