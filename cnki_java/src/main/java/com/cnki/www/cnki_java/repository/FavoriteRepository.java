package com.cnki.www.cnki_java.repository;

import com.cnki.www.cnki_java.entity.Favorite;
import com.cnki.www.cnki_java.entity.User;
import com.cnki.www.cnki_java.entity.Paper;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
    List<Favorite> findByUserOrderByFavoriteTimeDesc(User user);
    Optional<Favorite> findByUserAndPaper(User user, Paper paper);
    boolean existsByUserAndPaper(User user, Paper paper);
} 