CREATE TABLE users
(
    id          UUID PRIMARY KEY,
    username    TEXT      NOT NULL,
    first_name  TEXT      NOT NULL,
    last_name   TEXT      NOT NULL,
    email       TEXT      NOT NULL,
    hash        TEXT      NOT NULL,
    phone       TEXT      NOT NULL,
    initialized BOOLEAN   NOT NULL DEFAULT FALSE,
    created_on  TIMESTAMP NOT NULL,
    updated_on  TIMESTAMP NOT NULL
);

CREATE TABLE sessions
(
    id         UUID PRIMARY KEY,
    user_id    UUID      NOT NULL,
    created_on TIMESTAMP NOT NULL,
    expires_on TIMESTAMP NOT NULL,
    updated_on TIMESTAMP NOT NULL
);

CREATE TABLE groups
(
    id   UUID PRIMARY KEY,
    name TEXT NOT NULL
);

CREATE TABLE user_groups
(
    user_id  UUID NOT NULL,
    group_id UUID NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users (id),
    FOREIGN KEY (group_id) REFERENCES groups (id)
);

CREATE TABLE permissions
(
    id          UUID PRIMARY KEY,
    code        TEXT,
    description TEXT
);

CREATE TABLE user_permissions
(
    user_id       UUID NOT NULL,
    permission_id UUID NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users (id),
    FOREIGN KEY (permission_id) REFERENCES permissions (id)
);

CREATE TABLE group_permissions
(
    group_id      UUID NOT NULL,
    permission_id UUID NOT NULL,
    FOREIGN KEY (group_id) REFERENCES groups (id),
    FOREIGN KEY (permission_id) REFERENCES permissions (id)
);
