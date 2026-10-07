package com.rts.application.controller;

import com.rts.application.entity.Favorite;
import com.rts.application.entity.User;
import com.rts.application.service.FavoriteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class FavoriteControllerTest {

    @Mock
    private FavoriteService favoriteService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new FavoriteController(favoriteService)).build();
    }

    @Test
    void createReturnsCreatedForNewFavorite() throws Exception {
        Favorite favorite = favorite(5, 11, "AAPL", "150.25");
        when(favoriteService.create(11, "AAPL", new BigDecimal("150.25")))
                .thenReturn(new FavoriteService.CreateFavoriteResult(favorite, true));

        mockMvc.perform(post("/api/v1/favorites")
                        .contentType("application/json")
                        .content("{\"userId\":11,\"ticker\":\"AAPL\",\"openingPrice\":150.25}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.userId").value(11))
                .andExpect(jsonPath("$.ticker").value("AAPL"))
                .andExpect(jsonPath("$.openingPrice").value(150.25));
    }

    @Test
    void createReturnsOkForIdempotentDuplicate() throws Exception {
        Favorite favorite = favorite(5, 11, "AAPL", "150.25");
        when(favoriteService.create(11, "aapl", new BigDecimal("150.25")))
                .thenReturn(new FavoriteService.CreateFavoriteResult(favorite, false));

        mockMvc.perform(post("/api/v1/favorites")
                        .contentType("application/json")
                        .content("{\"userId\":11,\"ticker\":\"aapl\",\"openingPrice\":150.25}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5));
    }

    @Test
    void listReturnsUserFavorites() throws Exception {
        when(favoriteService.findByUserId(11))
                .thenReturn(List.of(favorite(5, 11, "AAPL", "150.25"), favorite(6, 11, "MSFT", "420.50")));

        mockMvc.perform(get("/api/v1/favorites/user/11"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].ticker").value("AAPL"))
                .andExpect(jsonPath("$[0].openingPrice").value(150.25))
                .andExpect(jsonPath("$[1].ticker").value("MSFT"))
                .andExpect(jsonPath("$[1].openingPrice").value(420.50));
    }

    @Test
    void deleteReturnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/v1/favorites/5"))
                .andExpect(status().isNoContent());
    }

    private Favorite favorite(Integer id, Integer userId, String ticker, String openingPrice) {
        User user = new User("alice", "hashed-password", LocalDate.of(2026, 1, 1));
        user.setId(userId);
        Favorite favorite = new Favorite(user, ticker, new BigDecimal(openingPrice));
        favorite.setId(id);
        return favorite;
    }
}
