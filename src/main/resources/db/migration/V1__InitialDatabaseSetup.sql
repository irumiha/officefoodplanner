CREATE TABLE USERS
(
    id         UUID PRIMARY KEY,
    username   TEXT      NOT NULL,
    first_name TEXT      NOT NULL,
    last_name  TEXT      NOT NULL,
    email      TEXT      NOT NULL,
    hash       TEXT      NOT NULL,
    phone      TEXT      NOT NULL,
    created_on TIMESTAMP NOT NULL,
    updated_on TIMESTAMP NOT NULL
);
