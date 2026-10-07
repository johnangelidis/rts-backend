package com.rts.application.controller;

import com.rts.application.entity.Favorite;
import com.rts.application.model.ErrorResponse;
import com.rts.application.service.FavoriteService;
import com.rts.application.service.FavoriteService.CreateFavoriteResult;
import com.rts.application.service.FavoriteService.FavoriteNotFoundException;
import com.rts.application.service.FavoriteService.UserNotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/favorites")
public class FavoriteController {
    private final FavoriteService favoriteService;

    public FavoriteController(FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    @PostMapping
    public ResponseEntity<FavoriteResponse> create(@Valid @RequestBody CreateFavoriteRequest request) {
        CreateFavoriteResult result = favoriteService.create(request.userId(), request.ticker(), request.openingPrice());
        HttpStatus status = result.created() ? HttpStatus.CREATED : HttpStatus.OK;
        return ResponseEntity.status(status).body(toResponse(result.favorite()));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<FavoriteResponse>> findByUser(@PathVariable Integer userId) {
        List<FavoriteResponse> favorites = favoriteService.findByUserId(userId).stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(favorites);
    }

    @DeleteMapping("/{favoriteId}")
    public ResponseEntity<Void> delete(@PathVariable Integer favoriteId) {
        favoriteService.delete(favoriteId);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler({UserNotFoundException.class, FavoriteNotFoundException.class})
    public ResponseEntity<ErrorResponse> handleNotFound(RuntimeException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(exception.getMessage()));
    }

    private FavoriteResponse toResponse(Favorite favorite) {
        return new FavoriteResponse(favorite.getId(), favorite.getUser().getId(), favorite.getTicker(),
                favorite.getOpeningPrice());
    }

    public record CreateFavoriteRequest(@NotNull Integer userId, @NotBlank String ticker,
                                        @NotNull BigDecimal openingPrice) {
    }

    public record FavoriteResponse(Integer id, Integer userId, String ticker, BigDecimal openingPrice) {
    }
}
