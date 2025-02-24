package com.cnki.www.cnki_java.repository;

import com.cnki.www.cnki_java.entity.SearchHistory;
import com.cnki.www.cnki_java.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SearchHistoryRepository extends JpaRepository<SearchHistory, Long> {
    List<SearchHistory> findByUserOrderBySearchTimeDesc(User user);
} 