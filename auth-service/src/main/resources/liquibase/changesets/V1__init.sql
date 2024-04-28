CREATE TABLE IF NOT EXISTS person
(
    person_id bigint PRIMARY KEY GENERATED BY DEFAULT AS IDENTITY,

    username  varchar(255) NOT NULL UNIQUE CHECK ( length(username) > 3 ),
    email     varchar(255) NOT NULL UNIQUE CHECK ( length(email) > 3 ),
    password  varchar(255) NOT NULL CHECK ( length(password) > 3 ),
    role      varchar(255) NOT NULL
);

-- person-details (one-to-one)
-- (points, missed_tasks, completed_tasks, created_at, updated_at)  must be create together with USER
-- (name, age) can update via PATCH or PUT after create USER
CREATE TABLE IF NOT EXISTS details
(
    details_id      bigint PRIMARY KEY GENERATED BY DEFAULT AS IDENTITY,
    person_id       bigint UNIQUE REFERENCES person (person_id) ON DELETE CASCADE,

    first_name      varchar(255) CHECK ( length(first_name) > 3 ),
    second_name     varchar(255) CHECK ( length(second_name) > 3 ),
    age             int CHECK ( age > 1 AND age < 125),

    points          bigint    NOT NULL DEFAULT 0,
    missed_tasks    bigint    NOT NULL DEFAULT 0,
    completed_tasks bigint    NOT NULL DEFAULT 0,

    created_at      timestamp NOT NULL,
    updated_at      timestamp NOT NULL
);