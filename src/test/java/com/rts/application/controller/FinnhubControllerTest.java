package com.rts.application.controller;

import com.rts.application.service.FinnhubService;
import com.rts.application.service.FinnhubService.InvalidSymbolException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.HttpServerErrorException;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class FinnhubControllerTest {

    @Mock
    private FinnhubService finnhubService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new FinnhubController(finnhubService)).build();
    }

    @Test
    void quoteReturnsFinnhubQuote() throws Exception {
        when(finnhubService.getQuote("AAPL"))
                .thenReturn(new FinnhubService.Quote(190.25, 192.10, 188.40, 189.75, 187.90, 1700000000L));

        mockMvc.perform(get("/api/v1/market/quote").param("symbol", "AAPL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.c").value(190.25))
                .andExpect(jsonPath("$.h").value(192.10))
                .andExpect(jsonPath("$.l").value(188.40))
                .andExpect(jsonPath("$.o").value(189.75))
                .andExpect(jsonPath("$.pc").value(187.90))
                .andExpect(jsonPath("$.t").value(1700000000));
    }

    @Test
    void invalidSymbolReturnsBadRequest() throws Exception {
        when(finnhubService.getQuote(" ")).thenThrow(new InvalidSymbolException());

        mockMvc.perform(get("/api/v1/market/quote").param("symbol", " "))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("A ticker symbol is required"));
    }

    @Test
    void finnhubFailureReturnsBadGateway() throws Exception {
        when(finnhubService.getQuote("AAPL"))
                .thenThrow(new HttpServerErrorException(HttpStatus.BAD_GATEWAY));

        mockMvc.perform(get("/api/v1/market/quote").param("symbol", "AAPL"))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.message").value("Market quote service is unavailable"));
    }
}
