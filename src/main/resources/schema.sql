CREATE TABLE IF NOT EXISTS pt_user (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    first_name VARCHAR(255) NOT NULL,
    last_name  VARCHAR(255) DEFAULT NULL,
    password VARCHAR(255) NOT NULL,
    reset_password_token VARCHAR(255) DEFAULT NULL,
    role TINYINT NOT NULL DEFAULT 0 COMMENT '0 = user, 1 = admin',
    active TINYINT NOT NULL DEFAULT 0 COMMENT '0 = active, 1 = inactive',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS pt_work_report (
    work_report_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    work_date DATE NOT NULL,
    total_work_time INT DEFAULT 0 COMMENT 'Total work duration in minutes for the day',
    total_break_time INT DEFAULT 0 COMMENT 'Total break duration in minutes for the day',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES pt_user(user_id),
    UNIQUE (user_id, work_date)
);

CREATE TABLE IF NOT EXISTS pt_work_session (
    work_session_id INT AUTO_INCREMENT PRIMARY KEY,
    work_report_id INT NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NULL,
    work_time INT DEFAULT 0 COMMENT 'Work duration in minutes for this session',
    break_time INT DEFAULT 0 COMMENT 'Total break time in minutes for this session',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_report FOREIGN KEY (work_report_id) REFERENCES pt_work_report(work_report_id)
);

CREATE TABLE IF NOT EXISTS pt_work_break (
    work_break_id INT AUTO_INCREMENT PRIMARY KEY,
    work_session_id INT NOT NULL,
    break_start TIME NOT NULL,
    break_end TIME DEFAULT 0 ,
    break_time INT DEFAULT 0 COMMENT 'Break duration in minutes',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_session FOREIGN KEY (work_session_id) REFERENCES pt_work_session(work_session_id)
);