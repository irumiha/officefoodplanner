CREATE TABLE SESSIONS
(
    ID         UUID PRIMARY KEY,
    user_id    UUID      NOT NULL,
    created_on TIMESTAMP NOT NULL,
    expires_on TIMESTAMP NOT NULL,
    updated_on TIMESTAMP NOT NULL
);
