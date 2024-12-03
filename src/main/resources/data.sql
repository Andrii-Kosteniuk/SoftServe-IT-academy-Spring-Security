-- SET IDENTITY_INSERT users ON

INSERT INTO users (id, first_name, last_name, email, password, role) VALUES (5, 'Nick', 'Green', 'nick@mail.com', '$2y$10$MylBJG.1uWKaJSKYePsxUObsqYjCNhaphWMUsRZSrXjgWVA65AaC6', 'USER');
INSERT INTO users (id, first_name, last_name, email, password, role) VALUES (6, 'Nora', 'White', 'nora@mail.com', '$2y$10$11T874WuR39e0kALIbwd.uXXrX4jDvtC/9NFQMcddQ03BRsh5RVRy', 'USER');
INSERT INTO users (id, first_name, last_name, email, password, role) VALUES (4, 'Mike', 'Brown', 'mike@mail.com', '$2a$12$Um1zTxPN7aNWzU/Jxz2eQ.y4vFMJX4jRRvxFdx.AxJhmB2HiE4j7K', 'ADMIN');

-- SET IDENTITY_INSERT users OFF
--
-- SET IDENTITY_INSERT states ON

INSERT INTO states (id, name) VALUES (5, 'New');
INSERT INTO states (id, name) VALUES (6, 'Doing');
INSERT INTO states (id, name) VALUES (7, 'Verify');
INSERT INTO states (id, name) VALUES (8, 'Done');

-- SET IDENTITY_INSERT states OFF
--
-- SET IDENTITY_INSERT todos ON

INSERT INTO todos (id, title, created_at, owner_id) VALUES (7, 'Mike''s To-Do #1', '2020-09-16 14:00:04.810221', 4);
INSERT INTO todos (id, title, created_at, owner_id) VALUES (8, 'Mike''s To-Do #2', '2020-09-16 14:00:11.480271', 4);
INSERT INTO todos (id, title, created_at, owner_id) VALUES (9, 'Mike''s To-Do #3', '2020-09-16 14:00:16.351238', 4);
INSERT INTO todos (id, title, created_at, owner_id) VALUES (10, 'Nick''s To-Do #1', '2020-09-16 14:14:54.532337', 5);
INSERT INTO todos (id, title, created_at, owner_id) VALUES (11, 'Nick''s To-Do #2', '2020-09-16 14:15:04.707176', 5);
INSERT INTO todos (id, title, created_at, owner_id) VALUES (12, 'Nora''s To-Do #1', '2020-09-16 14:15:32.464391', 6);
INSERT INTO todos (id, title, created_at, owner_id) VALUES (13, 'Nora''s To-Do #2', '2020-09-16 14:15:39.16246', 6);

-- SET IDENTITY_INSERT todos OFF
--
-- SET IDENTITY_INSERT tasks ON

INSERT INTO tasks (id, name, priority, todo_id, state_id) VALUES (6, 'Task #2', 'LOW', 7, 5);
INSERT INTO tasks (id, name, priority, todo_id, state_id) VALUES (5, 'Task #1', 'HIGH', 7, 8);
INSERT INTO tasks (id, name, priority, todo_id, state_id) VALUES (7, 'Task #3', 'MEDIUM', 7, 6);

-- SET IDENTITY_INSERT tasks OFF

-- SET IDENTITY_INSERT todo_collaborator ON

INSERT INTO todo_collaborator (todo_id, collaborator_id) VALUES (7, 5);
INSERT INTO todo_collaborator (todo_id, collaborator_id) VALUES (7, 6);
INSERT INTO todo_collaborator (todo_id, collaborator_id) VALUES (10, 6);
INSERT INTO todo_collaborator (todo_id, collaborator_id) VALUES (10, 4);
INSERT INTO todo_collaborator (todo_id, collaborator_id) VALUES (12, 5);
INSERT INTO todo_collaborator (todo_id, collaborator_id) VALUES (12, 4);

-- SET IDENTITY_INSERT todo_collaborator OFF