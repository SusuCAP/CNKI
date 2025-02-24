package com.cnki.www.cnki_java.repository;

import com.cnki.www.cnki_java.entity.Paper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface PaperRepository extends JpaRepository<Paper, Long> {
    @Query("SELECT p FROM Paper p WHERE p.title LIKE %:keyword%")
    List<Paper> searchByTitle(@Param("keyword") String keyword);
    
    @Query("SELECT p FROM Paper p WHERE p.title LIKE %:keyword%")
    List<Paper> findByTitleContaining(@Param("keyword") String keyword);

    @Query("SELECT p FROM Paper p WHERE p.title = :title")
    List<Paper> findByTitle(@Param("title") String title);

    Optional<Paper> findByPaperId(String paperId);

    // 查找指定年份的所有论文
    @Query("SELECT p FROM Paper p WHERE p.year = :year ORDER BY p.year DESC")
    List<Paper> findByYear(@Param("year") Integer year);

    // 查找最大年份
    @Query("SELECT MAX(p.year) FROM Paper p")
    Integer findMaxYear();
} 