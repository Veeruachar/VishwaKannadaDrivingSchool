CREATE TABLE driving_school_db.registrations_data (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    first_name VARCHAR(255),
    address VARCHAR(255),
    phone VARCHAR(255),
    course_type VARCHAR(255),
    dlnumber VARCHAR(255),
    admission_date DATE NOT NULL,
    profile_image LONGBLOB
);



CREATE TABLE driving_school_db.users (
                                         id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                         username VARCHAR(255) NOT NULL UNIQUE,
                                         password VARCHAR(255) NOT NULL
);


INSERT INTO driving_school_db.users (username, password) VALUES ('admin', 'Imadmin');