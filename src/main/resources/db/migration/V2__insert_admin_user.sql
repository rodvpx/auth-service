INSERT INTO users (id, name, email, password, role, created_at)
VALUES (
  gen_random_uuid(),
  'Admin',
  'admin@email.com',
  '$2a$10$Dh45Zq1fFJGLckY.b.4wrOMxrc6utvoBa1MQvnRbojS8ivzQXv39i',
  'ADMIN',
  CURRENT_TIMESTAMP
);
