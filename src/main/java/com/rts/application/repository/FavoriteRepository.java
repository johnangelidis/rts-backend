package com.rts.application.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rts.application.entity.Favorite;

public interface FavoriteRepository extends JpaRepository<Favorite, Integer> {

    List<Favorite> findAllByUser_Id(Integer userId);

    Optional<Favorite> findByUser_IdAndTickerIgnoreCase(Integer userId, String ticker);
}
