package com.rts.application.service;

import com.rts.application.entity.Favorite;
import com.rts.application.entity.User;
import com.rts.application.repository.FavoriteRepository;
import com.rts.application.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

@Service
public class FavoriteService {
    private final FavoriteRepository favoriteRepository;
    private final UserRepository userRepository;

    public FavoriteService(FavoriteRepository favoriteRepository, UserRepository userRepository) {
        this.favoriteRepository = favoriteRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public CreateFavoriteResult create(Integer userId, String ticker, BigDecimal openingPrice) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
        String normalizedTicker = ticker.trim().toUpperCase(Locale.ROOT);

        return favoriteRepository.findByUser_IdAndTickerIgnoreCase(userId, normalizedTicker)
                .map(favorite -> new CreateFavoriteResult(favorite, false))
                .orElseGet(() -> new CreateFavoriteResult(
                        favoriteRepository.save(new Favorite(user, normalizedTicker, openingPrice)), true));
    }

    @Transactional(readOnly = true)
    public List<Favorite> findByUserId(Integer userId) {
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException(userId);
        }
        return favoriteRepository.findAllByUser_Id(userId);
    }

    @Transactional
    public void delete(Integer favoriteId) {
        if (!favoriteRepository.existsById(favoriteId)) {
            throw new FavoriteNotFoundException(favoriteId);
        }
        favoriteRepository.deleteById(favoriteId);
    }

    public record CreateFavoriteResult(Favorite favorite, boolean created) {
    }

    public static class UserNotFoundException extends RuntimeException {
        public UserNotFoundException(Integer userId) {
            super("User not found: " + userId);
        }
    }

    public static class FavoriteNotFoundException extends RuntimeException {
        public FavoriteNotFoundException(Integer favoriteId) {
            super("Favorite not found: " + favoriteId);
        }
    }
}
