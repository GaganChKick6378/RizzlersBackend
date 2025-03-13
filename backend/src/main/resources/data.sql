-- Clean up existing data (use IF EXISTS for H2 compatibility)
DELETE FROM students WHERE 1=1;

-- Reset the sequence in a database-agnostic way
-- H2 doesn't use the same sequence syntax as PostgreSQL
-- So we'll skip this for H2 compatibility
-- ALTER SEQUENCE students_id_seq RESTART WITH 1;

-- Sample student data for Computer Science department
INSERT INTO students (first_name, last_name, email, enrollment_number, year_of_study, cgpa, department, date_of_birth, mobile_number, address, created_at, updated_at, is_active)
VALUES 
    ('John', 'Smith', 'john.smith@example.com', 'CS20210001', 3, 8.75, 'Computer Science', '2000-05-15', '9876543210', '123 College St, Apt 45, New York, NY 10001', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, TRUE),
    ('Emily', 'Johnson', 'emily.johnson@example.com', 'CS20210002', 3, 9.20, 'Computer Science', '2001-07-22', '9876543211', '456 University Ave, Boston, MA 02215', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, TRUE),
    ('Michael', 'Williams', 'michael.williams@example.com', 'CS20210003', 3, 7.90, 'Computer Science', '2000-11-30', '9876543212', '789 Campus Rd, Chicago, IL 60637', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, TRUE),
    ('Sarah', 'Brown', 'sarah.brown@example.com', 'CS20220001', 2, 8.50, 'Computer Science', '2002-03-17', '9876543213', '321 Dorm St, San Francisco, CA 94107', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, TRUE),
    ('David', 'Jones', 'david.jones@example.com', 'CS20220002', 2, 7.75, 'Computer Science', '2001-09-05', '9876543214', '654 Student Housing, Seattle, WA 98105', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, TRUE);

-- Sample student data for Electrical Engineering department
INSERT INTO students (first_name, last_name, email, enrollment_number, year_of_study, cgpa, department, date_of_birth, mobile_number, address, created_at, updated_at, is_active)
VALUES 
    ('Jessica', 'Miller', 'jessica.miller@example.com', 'EE20210001', 3, 8.30, 'Electrical Engineering', '2000-04-12', '9876543215', '987 Engineering Blvd, Austin, TX 78712', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, TRUE),
    ('James', 'Davis', 'james.davis@example.com', 'EE20210002', 3, 9.10, 'Electrical Engineering', '2000-08-29', '9876543216', '654 Tech Lane, San Jose, CA 95110', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, TRUE),
    ('Jennifer', 'Wilson', 'jennifer.wilson@example.com', 'EE20220001', 2, 8.85, 'Electrical Engineering', '2001-12-03', '9876543217', '321 Circuit St, Philadelphia, PA 19104', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, TRUE);

-- Sample student data for Mechanical Engineering department
INSERT INTO students (first_name, last_name, email, enrollment_number, year_of_study, cgpa, department, date_of_birth, mobile_number, address, created_at, updated_at, is_active)
VALUES 
    ('Robert', 'Taylor', 'robert.taylor@example.com', 'ME20210001', 3, 7.95, 'Mechanical Engineering', '2000-02-18', '9876543218', '456 Mechanics Dr, Detroit, MI 48201', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, TRUE),
    ('Emma', 'Anderson', 'emma.anderson@example.com', 'ME20220001', 2, 8.40, 'Mechanical Engineering', '2002-01-25', '9876543219', '789 Design Ave, Pittsburgh, PA 15213', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, TRUE);

-- Sample student data for Business Administration department
INSERT INTO students (first_name, last_name, email, enrollment_number, year_of_study, cgpa, department, date_of_birth, mobile_number, address, created_at, updated_at, is_active)
VALUES 
    ('Daniel', 'Thomas', 'daniel.thomas@example.com', 'BA20210001', 3, 8.20, 'Business Administration', '2000-06-10', '9876543220', '123 Commerce St, Atlanta, GA 30303', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, TRUE),
    ('Olivia', 'Martin', 'olivia.martin@example.com', 'BA20210002', 3, 9.30, 'Business Administration', '2000-10-07', '9876543221', '456 Finance Rd, Charlotte, NC 28202', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, TRUE);

-- Sample freshman students (1st year)
INSERT INTO students (first_name, last_name, email, enrollment_number, year_of_study, cgpa, department, date_of_birth, mobile_number, address, created_at, updated_at, is_active)
VALUES 
    ('Noah', 'Jackson', 'noah.jackson@example.com', 'CS20230001', 1, 8.90, 'Computer Science', '2003-04-20', '9876543222', '789 Freshman Dorm, Berkeley, CA 94720', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, TRUE),
    ('Sophia', 'White', 'sophia.white@example.com', 'EE20230001', 1, 9.15, 'Electrical Engineering', '2003-08-11', '9876543223', '321 New Student Blvd, Cambridge, MA 02139', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, TRUE),
    ('Ethan', 'Harris', 'ethan.harris@example.com', 'ME20230001', 1, 8.25, 'Mechanical Engineering', '2003-11-29', '9876543224', '654 First Year Lane, Ithaca, NY 14850', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, TRUE); 