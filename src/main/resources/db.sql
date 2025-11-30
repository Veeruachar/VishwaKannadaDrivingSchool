CREATE TABLE driving_school_db.registrations_data (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    first_name VARCHAR(255),
    address VARCHAR(255),
    phone VARCHAR(255),
    course_type VARCHAR(255),
    dlnumber VARCHAR(255),
    admission_date DATE NOT NULL
);