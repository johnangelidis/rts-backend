package com.rts.application.service;

import com.rts.application.service.FinnhubService.FinnhubConfigurationException;
import com.rts.application.service.FinnhubService.InvalidSymbolException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class FinnhubServiceTest {

    private RestClient.Builder restClientBuilder;
    private MockRestServiceServer server;

    @BeforeEach
    void setUp() {
        restClientBuilder = RestClient.builder();
        server = MockRestServiceServer.bindTo(restClientBuilder).build();
    }

    @Test
    void getQuoteNormalizesSymbolAndMapsFinnhubResponse() {
        server.expect(requestTo("https://finnhub.io/api/v1/quote?symbol=AAPL&token=test-key"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(
                        "{\"c\":190.25,\"h\":192.1,\"l\":188.4,\"o\":189.75,\"pc\":187.9,\"t\":1700000000}",
                        MediaType.APPLICATION_JSON));
        FinnhubService service = new FinnhubService(restClientBuilder, "test-key");

        FinnhubService.Quote quote = service.getQuote("  aapl ");

        assertThat(quote.c()).isEqualTo(190.25);
        assertThat(quote.h()).isEqualTo(192.1);
        assertThat(quote.l()).isEqualTo(188.4);
        assertThat(quote.o()).isEqualTo(189.75);
        assertThat(quote.pc()).isEqualTo(187.9);
        assertThat(quote.t()).isEqualTo(1700000000L);
        server.verify();
    }

    @Test
    void getQuoteRejectsBlankSymbol() {
        FinnhubService service = new FinnhubService(restClientBuilder, "test-key");

        assertThatThrownBy(() -> service.getQuote("  "))
                .isInstanceOf(InvalidSymbolException.class)
                .hasMessage("A ticker symbol is required");
    }

    @Test
    void getQuoteRejectsMissingApiKey() {
        FinnhubService service = new FinnhubService(restClientBuilder, "");

        assertThatThrownBy(() -> service.getQuote("AAPL"))
                .isInstanceOf(FinnhubConfigurationException.class)
                .hasMessage("Finnhub is not configured");
    }
}
