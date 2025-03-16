-- Insert sample people
INSERT INTO person (ist_id, name, type, email) VALUES
-- Students
('ist123456', 'Alice Johnson', 'STUDENT', 'alice.johnson@example.com'),
('ist234567', 'Bob Smith', 'STUDENT', 'bob.smith@example.com'),

-- Teachers
('ist345678', 'Dr. Emily Davis', 'TEACHER', 'emily.davis@example.com'),
('ist456789', 'Prof. Michael Brown', 'TEACHER', 'michael.brown@example.com'),

-- Staff
('ist567890', 'Sarah Wilson', 'STAFF', 'sarah.wilson@example.com'),
('ist678901', 'David Martinez', 'STAFF', 'david.martinez@example.com'),

-- SC (Student Committee)
('ist789012', 'Laura Garcia', 'SC', 'laura.garcia@example.com'),
('ist890123', 'James Rodriguez', 'SC', 'james.rodriguez@example.com'),

-- Coordinators
('ist901234', 'Dr. Olivia Taylor', 'COORDINATOR', 'olivia.taylor@example.com'),
('ist012345', 'Prof. William Anderson', 'COORDINATOR', 'william.anderson@example.com');

-- Set the sequence to the next value after the inserted records
SELECT setval('person_id_seq', (SELECT MAX(id) FROM person));