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

CREATE TABLE driving_school_db.payments (
                          id BIGINT AUTO_INCREMENT PRIMARY KEY,
                          registration_id BIGINT NOT NULL,
                          amount_paid DECIMAL(10, 2) NOT NULL,
                          payment_date DATETIME DEFAULT CURRENT_TIMESTAMP,
                          payment_mode VARCHAR(50) NOT NULL, -- e.g., 'Cash', 'UPI', 'Card'
                          transaction_id VARCHAR(100) UNIQUE, -- Useful for tracking UTR/Ref numbers
                          remarks VARCHAR(255),

    -- Foreign Key Constraint
                          CONSTRAINT fk_registration
                              FOREIGN KEY (registration_id)
                                  REFERENCES registrations_data(id)
                                  ON DELETE CASCADE
);