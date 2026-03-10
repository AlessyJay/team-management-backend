-- ============================================================
-- V1: Core schema
-- ============================================================

CREATE TABLE IF NOT EXISTS users
(
    id
    UUID
    PRIMARY
    KEY
    DEFAULT
    gen_random_uuid
(
),
    name VARCHAR
(
    100
) NOT NULL,
    email VARCHAR
(
    255
) NOT NULL UNIQUE,
    password TEXT NOT NULL,
    avatar_url TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now
(
)
    );

CREATE TABLE IF NOT EXISTS projects
(
    id
    UUID
    PRIMARY
    KEY
    DEFAULT
    gen_random_uuid
(
),
    name TEXT NOT NULL,
    description TEXT,
    owner_id UUID NOT NULL REFERENCES users
(
    id
),
    status VARCHAR
(
    20
) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL DEFAULT now
(
),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now
(
)
    );

CREATE TABLE IF NOT EXISTS project_members
(
    id
    UUID
    PRIMARY
    KEY
    DEFAULT
    gen_random_uuid
(
),
    project_id UUID NOT NULL REFERENCES projects
(
    id
) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users
(
    id
)
  ON DELETE CASCADE,
    role VARCHAR
(
    20
) NOT NULL DEFAULT 'MEMBER',
    joined_at TIMESTAMPTZ NOT NULL DEFAULT now
(
),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now
(
),
    UNIQUE
(
    project_id,
    user_id
)
    );

CREATE TABLE IF NOT EXISTS refresh_tokens
(
    id
    UUID
    PRIMARY
    KEY
    DEFAULT
    gen_random_uuid
(
),
    user_id UUID NOT NULL REFERENCES users
(
    id
) ON DELETE CASCADE UNIQUE,
    token_hash TEXT NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now
(
)
    );

CREATE TABLE IF NOT EXISTS sprints
(
    id
    UUID
    PRIMARY
    KEY
    DEFAULT
    gen_random_uuid
(
),
    project_id UUID NOT NULL REFERENCES projects
(
    id
) ON DELETE CASCADE,
    name VARCHAR
(
    100
) NOT NULL,
    goal TEXT,
    category VARCHAR
(
    50
),
    tags TEXT[] NOT NULL DEFAULT '{}',
    expected_duration VARCHAR
(
    50
),
    start_date DATE NOT NULL,
    end_date DATE,
    status VARCHAR
(
    20
) NOT NULL DEFAULT 'PLANNING',
    completed_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now
(
),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now
(
),
    CONSTRAINT sprints_dates_check CHECK
(
    end_date
    IS
    NULL
    OR
    end_date >
    start_date
)
    );

CREATE TABLE IF NOT EXISTS sprint_members
(
    id
    UUID
    PRIMARY
    KEY
    DEFAULT
    gen_random_uuid
(
),
    sprint_id UUID NOT NULL REFERENCES sprints
(
    id
) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users
(
    id
)
  ON DELETE CASCADE,
    role VARCHAR
(
    10
) NOT NULL DEFAULT 'MEMBER',
    joined_at TIMESTAMPTZ NOT NULL DEFAULT now
(
),
    UNIQUE
(
    sprint_id,
    user_id
),
    CONSTRAINT sprint_member_role_check CHECK
(
    role
    IN
(
    'LEAD',
    'MEMBER'
))
    );

CREATE TABLE IF NOT EXISTS issues
(
    id
    UUID
    PRIMARY
    KEY
    DEFAULT
    gen_random_uuid
(
),
    project_id UUID NOT NULL REFERENCES projects
(
    id
) ON DELETE CASCADE,
    sprint_id UUID REFERENCES sprints
(
    id
)
  ON DELETE SET NULL,
    title VARCHAR
(
    255
) NOT NULL,
    description TEXT,
    type VARCHAR
(
    20
) NOT NULL DEFAULT 'TASK',
    status VARCHAR
(
    20
) NOT NULL DEFAULT 'BACKLOG',
    priority VARCHAR
(
    20
) NOT NULL DEFAULT 'MEDIUM',
    story_points SMALLINT,
    due_date DATE,
    position INTEGER NOT NULL DEFAULT 0,
    assignee_id UUID REFERENCES users
(
    id
)
  ON DELETE SET NULL,
    created_by UUID NOT NULL REFERENCES users
(
    id
),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now
(
),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now
(
)
    );

CREATE INDEX IF NOT EXISTS idx_issues_project ON issues (project_id);
CREATE INDEX IF NOT EXISTS idx_issues_sprint ON issues (sprint_id);

CREATE TABLE IF NOT EXISTS sprint_capacity
(
    id
    UUID
    PRIMARY
    KEY
    DEFAULT
    gen_random_uuid
(
),
    sprint_id UUID NOT NULL REFERENCES sprints
(
    id
) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users
(
    id
)
  ON DELETE CASCADE,
    capacity_points SMALLINT NOT NULL DEFAULT 0,
    UNIQUE
(
    sprint_id,
    user_id
)
    );

CREATE TABLE IF NOT EXISTS project_views
(
    id
    UUID
    PRIMARY
    KEY
    DEFAULT
    gen_random_uuid
(
),
    user_id UUID NOT NULL REFERENCES users
(
    id
) ON DELETE CASCADE,
    project_id UUID NOT NULL REFERENCES projects
(
    id
)
  ON DELETE CASCADE,
    viewed_at TIMESTAMPTZ NOT NULL DEFAULT now
(
),
    UNIQUE
(
    user_id,
    project_id
)
    );

CREATE INDEX IF NOT EXISTS idx_project_views_user ON project_views (user_id, viewed_at DESC);
CREATE INDEX IF NOT EXISTS idx_sprint_members_sprint ON sprint_members (sprint_id);

CREATE TABLE IF NOT EXISTS issue_comments
(
    id
    UUID
    PRIMARY
    KEY
    DEFAULT
    gen_random_uuid
(
),
    issue_id UUID NOT NULL REFERENCES issues
(
    id
) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users
(
    id
)
  ON DELETE CASCADE,
    content TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now
(
),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now
(
)
    );

CREATE INDEX IF NOT EXISTS idx_comments_issue ON issue_comments (issue_id);

CREATE TABLE IF NOT EXISTS labels
(
    id
    UUID
    PRIMARY
    KEY
    DEFAULT
    gen_random_uuid
(
),
    project_id UUID NOT NULL REFERENCES projects
(
    id
) ON DELETE CASCADE,
    name VARCHAR
(
    50
) NOT NULL,
    color VARCHAR
(
    7
) NOT NULL DEFAULT '#6366f1',
    UNIQUE
(
    project_id,
    name
)
    );

CREATE TABLE IF NOT EXISTS issue_labels
(
    issue_id
    UUID
    NOT
    NULL
    REFERENCES
    issues
(
    id
) ON DELETE CASCADE,
    label_id UUID NOT NULL REFERENCES labels
(
    id
)
  ON DELETE CASCADE,
    PRIMARY KEY
(
    issue_id,
    label_id
)
    );