CREATE TABLE tasks
(
    id        UUID         NOT NULL,
    user_id   UUID         NOT NULL,
    task_name VARCHAR(255) NOT NULL,
    finished  BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP   NOT NULL,
    updated_at TIMESTAMP   NOT NULL,
    CONSTRAINT pk_tasks      PRIMARY KEY (id),
    CONSTRAINT fk_tasks_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE INDEX idx_tasks_user_id ON tasks (user_id);
