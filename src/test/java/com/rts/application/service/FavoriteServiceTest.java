package com.rts.application.service;

import java.math.BigDecimal;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.rts.application.entity.Favorite;
import com.rts.application.entity.User;
import com.rts.application.repository.FavoriteRepository;
import com.rts.application.repository.UserRepository;
import com.rts.application.service.FavoriteService.FavoriteNotFoundException;
import com.rts.application.service.FavoriteService.UserNotFoundException;

@ExtendWith(MockitoExtension.class)
class FavoriteServiceTest {

    @Mock
    private FavoriteRepository favoriteRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private FavoriteService favoriteService;

    @Test
    void createNormalizesTickerAndCreatesNewFavorite() {
        User user = user(7);
        Favorite saved = new Favorite(user, "AAPL", new BigDecimal("150.25"));
        when(userRepository.findById(7)).thenReturn(Optional.of(user));
        when(favoriteRepository.findByUser_IdAndTickerIgnoreCase(7, "AAPL")).thenReturn(Optional.empty());
        when(favoriteRepository.save(any(Favorite.class))).thenReturn(saved);

        FavoriteService.CreateFavoriteResult result = favoriteService.create(7, "  aapl ", new BigDecimal("150.25"));

        assertThat(result.created()).isTrue();
        assertThat(result.favorite()).isSameAs(saved);
        verify(favoriteRepository).save(any(Favorite.class));
    }

    @Test
    void createReturnsExistingFavoriteForDuplicateTicker() {
        User user = user(7);
        Favorite existing = new Favorite(user, "AAPL", new BigDecimal("150.25"));
        when(userRepository.findById(7)).thenReturn(Optional.of(user));
        when(favoriteRepository.findByUser_IdAndTickerIgnoreCase(7, "AAPL"))
                .thenReturn(Optional.of(existing));

        FavoriteService.CreateFavoriteResult result = favoriteService.create(7, "aapl", new BigDecimal("150.25"));

        assertThat(result.created()).isFalse();
        assertThat(result.favorite()).isSameAs(existing);
        verify(favoriteRepository, never()).save(any(Favorite.class));
    }

    @Test
    void createRejectsUnknownUser() {
        when(userRepository.findById(7)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> favoriteService.create(7, "AAPL", new BigDecimal("150.25")))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User not found: 7");
    }

    @Test
    void findByUserIdReturnsFavorites() {
        User user = user(7);
        List<Favorite> favorites = List.of(new Favorite(user, "AAPL", new BigDecimal("150.25")));
        when(userRepository.existsById(7)).thenReturn(true);
        when(favoriteRepository.findAllByUser_Id(7)).thenReturn(favorites);

        assertThat(favoriteService.findByUserId(7)).containsExactlyElementsOf(favorites);
    }

    @Test
    void findByUserIdRejectsUnknownUser() {
        when(userRepository.existsById(7)).thenReturn(false);

        assertThatThrownBy(() -> favoriteService.findByUserId(7))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void deleteRemovesExistingFavorite() {
        when(favoriteRepository.existsById(3)).thenReturn(true);

        favoriteService.delete(3);

        verify(favoriteRepository).deleteById(3);
    }

    @Test
    void deleteRejectsMissingFavorite() {
        when(favoriteRepository.existsById(3)).thenReturn(false);

        assertThatThrownBy(() -> favoriteService.delete(3))
                .isInstanceOf(FavoriteNotFoundException.class)
                .hasMessage("Favorite not found: 3");
        verify(favoriteRepository, never()).deleteById(3);
    }

    private User user(Integer id) {
        User user = new User("alice", "hashed-password", LocalDate.now());
        user.setId(id);
        return user;
    }
}
