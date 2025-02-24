package com.cnki.www.cnki_java.repository;

import com.cnki.www.cnki_java.entity.ViewHistory;
import com.cnki.www.cnki_java.entity.User;
import com.cnki.www.cnki_java.entity.Paper;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ViewHistoryRepository extends JpaRepository<ViewHistory, Long> {
    List<ViewHistory> findByUserOrderByViewTimeDesc(User user);
    boolean existsByUserAndPaper(User user, Paper paper);
} 