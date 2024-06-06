CREATE TABLE IF NOT EXISTS verify
(
    verify_id  bigint PRIMARY KEY GENERATED BY DEFAULT AS IDENTITY,
    username   varchar NOT NULL,
    email      varchar NOT NULL,
    code       int,
    created_at timestamp
)