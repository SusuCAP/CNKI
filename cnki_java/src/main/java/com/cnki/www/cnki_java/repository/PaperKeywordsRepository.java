package com.cnki.www.cnki_java.repository;

import com.cnki.www.cnki_java.entity.PaperKeywords;
import com.cnki.www.cnki_java.entity.Paper;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PaperKeywordsRepository extends JpaRepository<PaperKeywords, Long> {
    Optional<PaperKeywords> findByPaper(Paper paper);
    boolean existsByPaper(Paper paper);
} 