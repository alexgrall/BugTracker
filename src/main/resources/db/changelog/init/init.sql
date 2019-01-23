CREATE TABLE projects (
    project_id bigint(20) NOT NULL AUTO_INCREMENT,
    name varchar(255) CHARSET utf8 NOT NULL,
    description varchar(255) CHARSET utf8,
    created_at datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    modified_at datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY(project_id),
    UNIQUE (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE tasks (
    task_id bigint(20) NOT NULL AUTO_INCREMENT,
    project_id bigint(20) NOT NULL,
    name varchar(255) CHARSET utf8 NOT NULL,
    description text DEFAULT NULL,
    priority int NOT NULL DEFAULT 1,
    status enum('NEW', 'IN_PROGRESS', 'CLOSED') NOT NULL,
    created_at datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    modified_at datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY(task_id),
    CONSTRAINT fk_tasks_projects FOREIGN KEY (project_id) REFERENCES projects (project_id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;