CREATE TABLE view_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    paper_id BIGINT NOT NULL,
    view_time DATETIME NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (paper_id) REFERENCES papers(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 添加索引以提高查询性能
CREATE INDEX idx_view_history_user ON view_history(user_id);
CREATE INDEX idx_view_history_paper ON view_history(paper_id);
CREATE INDEX idx_view_history_time ON view_history(view_time); 