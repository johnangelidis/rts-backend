package com.rts.application.controller;

import com.rts.application.model.ErrorResponse;
import com.rts.application.model.QuoteResponse;
import com.rts.application.service.FinnhubService;
import com.rts.application.service.FinnhubService.FinnhubConfigurationException;
import com.rts.application.service.FinnhubService.InvalidSymbolException;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClientException;

@Validated
@RestController
@RequestMapping("/api/v1/market")
public class FinnhubController {
    private final FinnhubService finnhubService;

    public FinnhubController(FinnhubService finnhubService) {
        this.finnhubService = finnhubService;
    }

    @GetMapping("/quote")
    public ResponseEntity<QuoteResponse> quote(@RequestParam @NotBlank String symbol) {
        FinnhubService.Quote quote = finnhubService.getQuote(symbol);
        return ResponseEntity.ok(new QuoteResponse(quote.c(), quote.h(), quote.l(), quote.o(), quote.pc(), quote.t()));
    }

    @ExceptionHandler(InvalidSymbolException.class)
    public ResponseEntity<ErrorResponse> handleInvalidSymbol(RuntimeException exception) {
        return ResponseEntity.badRequest().body(new ErrorResponse(exception.getMessage()));
    }

    @ExceptionHandler({FinnhubConfigurationException.class, RestClientException.class})
    public ResponseEntity<ErrorResponse> handleFinnhubFailure(RuntimeException exception) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(new ErrorResponse("Market quote service is unavailable"));
    }

}
