package com.rts.application.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Locale;

@Service
public class FinnhubService {
    private final RestClient restClient;
    private final String apiKey;

    public FinnhubService(RestClient.Builder restClientBuilder,
                          @Value("${FINNHUB_API_KEY:}") String apiKey) {
        this.restClient = restClientBuilder.baseUrl("https://finnhub.io/api/v1").build();
        this.apiKey = apiKey;
    }

    public Quote getQuote(String symbol) {
        String normalizedSymbol = symbol == null ? "" : symbol.trim().toUpperCase(Locale.ROOT);
        if (normalizedSymbol.isBlank()) {
            throw new InvalidSymbolException();
        }
        if (apiKey.isBlank()) {
            throw new FinnhubConfigurationException();
        }

        Quote quote = restClient.get()
                .uri(uriBuilder -> uriBuilder.path("/quote")
                        .queryParam("symbol", normalizedSymbol)
                        .queryParam("token", apiKey)
                        .build())
                .retrieve()
                .body(Quote.class);
        if (quote == null) {
            throw new FinnhubUnavailableException();
        }
        return quote;
    }

    public record Quote(double c, double h, double l, double o, double pc, long t) {
    }

    public static class InvalidSymbolException extends RuntimeException {
        public InvalidSymbolException() {
            super("A ticker symbol is required");
        }
    }

    public static class FinnhubConfigurationException extends RuntimeException {
        public FinnhubConfigurationException() {
            super("Finnhub is not configured");
        }
    }

    public static class FinnhubUnavailableException extends RuntimeException {
        public FinnhubUnavailableException() {
            super("Finnhub did not return a quote");
        }
    }
}
