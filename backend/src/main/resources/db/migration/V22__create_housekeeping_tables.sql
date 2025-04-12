-- Create custom ENUM type for staff skill level if it doesn't exist
DO
$$
    BEGIN
        IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'staff_skill_level_enum') THEN
            CREATE TYPE public."staff_skill_level_enum" AS ENUM (
                'BEGINNER',
                'INTERMEDIATE',
                'EXPERT'
                );
        END IF;
    END
$$;

-- Create Property_Preferences Table if it doesn't exist
CREATE TABLE IF NOT EXISTS property_preferences
(
    property_id    SERIAL PRIMARY KEY,
    timezone       VARCHAR(50) NOT NULL, -- e.g., 'America/New_York'
    check_out_time TIME        NOT NULL, -- Timezone-agnostic, interpreted via property timezone
    check_in_time  TIME        NOT NULL, -- Timezone-agnostic, interpreted via property timezone
    created_at     TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Create Shifts Table if it doesn't exist
CREATE TABLE IF NOT EXISTS shifts
(
    shift_id    SERIAL PRIMARY KEY,
    shift_name  VARCHAR(100) NOT NULL,
    start_time  TIME         NOT NULL, -- Timezone-agnostic, interpreted via property timezone
    end_time    TIME         NOT NULL, -- Timezone-agnostic, interpreted via property timezone
    property_id INT          NOT NULL,
    created_at  TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_shifts_property_id FOREIGN KEY (property_id)
        REFERENCES property_preferences (property_id) ON DELETE CASCADE
);

-- Create Staff Table if it doesn't exist
CREATE TABLE IF NOT EXISTS staff
(
    staff_id           SERIAL PRIMARY KEY,
    staff_name         VARCHAR(100)                    NOT NULL,
    phone              VARCHAR(20),
    preferred_shift_id INT,
    property_id        INT                             NOT NULL,
    skill_level        public."staff_skill_level_enum" NOT NULL,
    created_at         TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at         TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_staff_preferred_shift_id FOREIGN KEY (preferred_shift_id)
        REFERENCES shifts (shift_id) ON DELETE SET NULL,
    CONSTRAINT fk_staff_property_id FOREIGN KEY (property_id)
        REFERENCES property_preferences (property_id) ON DELETE CASCADE
);

-- Create Clean_Task_Types Table if it doesn't exist
CREATE TABLE IF NOT EXISTS clean_task_types
(
    task_type_id  SERIAL PRIMARY KEY,
    type_name     VARCHAR(100) NOT NULL,
    required_time INTERVAL     NOT NULL,
    created_at    TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Create Clean_Tasks Table if it doesn't exist
CREATE TABLE IF NOT EXISTS clean_tasks
(
    task_id         SERIAL PRIMARY KEY,
    property_id     INT          NOT NULL,
    staff_id        INT          NOT NULL,
    start_time      TIME         NOT NULL, -- Timezone-agnostic, interpreted via property timezone
    task_type_id    INT          NOT NULL,
    date            DATE         NOT NULL,
    external_room_id VARCHAR(50) NOT NULL, -- Store GraphQL room ID as a string
    created_at      TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_clean_tasks_property_id FOREIGN KEY (property_id)
        REFERENCES property_preferences (property_id) ON DELETE CASCADE,
    CONSTRAINT fk_clean_tasks_staff_id FOREIGN KEY (staff_id)
        REFERENCES staff (staff_id) ON DELETE CASCADE,
    CONSTRAINT fk_clean_tasks_task_type_id FOREIGN KEY (task_type_id)
        REFERENCES clean_task_types (task_type_id) ON DELETE RESTRICT
);

-- Create Absent Table if it doesn't exist
CREATE TABLE IF NOT EXISTS absent
(
    staff_id   INT  NOT NULL,
    date       DATE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (staff_id, date),
    CONSTRAINT fk_absent_staff_id FOREIGN KEY (staff_id)
        REFERENCES staff (staff_id) ON DELETE CASCADE
);

-- Create Housekeeping Users Table for authentication and role-based access
CREATE TABLE IF NOT EXISTS housekeeping_users
(
    user_id    SERIAL PRIMARY KEY,
    username   VARCHAR(100) NOT NULL UNIQUE,
    password   VARCHAR(255) NOT NULL,
    email      VARCHAR(255),
    role       VARCHAR(50)  NOT NULL, -- ADMIN, STAFF
    staff_id   INT,
    is_active  BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_housekeeping_users_staff_id FOREIGN KEY (staff_id)
        REFERENCES staff (staff_id) ON DELETE SET NULL
);

-- Insert default task types
INSERT INTO clean_task_types (type_name, required_time)
VALUES ('CHECKOUT_CLEANING', '02:00:00'),
       ('DAILY_CLEANING', '00:30:00')
ON CONFLICT DO NOTHING; 