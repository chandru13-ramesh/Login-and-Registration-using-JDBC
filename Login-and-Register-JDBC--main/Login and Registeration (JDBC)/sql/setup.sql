CREATE DATABASE IF NOT EXISTS secure_user_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE secure_user_db;

CREATE TABLE IF NOT EXISTS users (
    id       INT          PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50)  UNIQUE NOT NULL,
    email    VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL
);


