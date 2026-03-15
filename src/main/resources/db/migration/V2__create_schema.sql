-- Create main schema
CREATE SCHEMA IF NOT EXISTS culturaalsur;
CREATE TABLE app_user
(
    id       BIGSERIAL PRIMARY KEY,
    username VARCHAR(50)  NOT NULL 1NIQUE,
    email    VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role     VARCHAR(20)  NOT NULL DEFAULT 'ROLE_USER'
);

CREATE TABLE post
(
    id         BIGSERIAL PRIMARY KEY,
    title      VARCHAR(200) NOT NULL,
    body       TEXT,
    categories VARCHAR(50),
    tag        VARCHAR(50),
    created_at TIMESTAMP    NOT NULL DEFAULT NOW(),
    author_id  BIGINT REFERENCES app_user (id)
);

CREATE TABLE post_media
(
    id         BIGSERIAL PRIMARY KEY,
    post_id    BIGINT       NOT NULL REFERENCES post (id) ON DELETE CASCADE,
    url        VARCHAR(500) NOT NULL,
    media_type VARCHAR(20) DEFAULT 'IMAGE',
    position   INT         DEFAULT 0
);

CREATE TABLE comment
(
    id         BIGSERIAL PRIMARY KEY,
    content    TEXT      NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    post_id    BIGINT    NOT NULL REFERENCES post (id) ON DELETE CASCADE,
    author_id  BIGINT REFERENCES app_user (id)
);