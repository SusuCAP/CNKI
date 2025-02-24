CREATE TABLE search_history (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    keyword VARCHAR(255) NOT NULL,
    search_time DATETIME NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id)
); 