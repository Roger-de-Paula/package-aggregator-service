package com.example.packageaggregator;

import com.example.packageaggregator.client.ExchangeRateClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CurrencyControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ExchangeRateClient exchangeRateClient;

    @Test
    void getCurrenciesReturnsAllWhenNoSearch() throws Exception {
        when(exchangeRateClient.getCurrencies()).thenReturn(
                Map.of("USD", "United States Dollar", "EUR", "Euro", "JPY", "Japanese Yen"));

        mockMvc.perform(get("/currencies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[?(@.code=='EUR')].name").value("Euro"));
    }

    @Test
    void getCurrenciesFiltersBySearch() throws Exception {
        when(exchangeRateClient.getCurrencies()).thenReturn(
                Map.of("USD", "United States Dollar", "GBP", "British Pound", "JPY", "Japanese Yen"));

        mockMvc.perform(get("/currencies").param("search", "ja"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].code").value("JPY"))
                .andExpect(jsonPath("$[0].name").value("Japanese Yen"));
    }
}
