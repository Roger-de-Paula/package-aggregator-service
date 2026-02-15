package com.example.packageaggregator;

import com.example.packageaggregator.client.ExchangeRateClient;
import com.example.packageaggregator.client.ProductClient;
import com.example.packageaggregator.client.dto.ExternalProductResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static java.util.Arrays.asList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ProductControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductClient productClient;

    @MockBean
    private ExchangeRateClient exchangeRateClient;

    @Test
    void getProductsReturnsCatalog() throws Exception {
        when(exchangeRateClient.getRateUsdTo(anyString())).thenReturn(BigDecimal.ONE);
        List<ExternalProductResponse> catalog = asList(
                ExternalProductResponse.builder().id("VqKb4tyj9V6i").name("Widget").usdPrice(new BigDecimal("9.99")).build(),
                ExternalProductResponse.builder().id("abc123").name("Gadget").usdPrice(new BigDecimal("19.50")).build()
        );
        when(productClient.getProducts()).thenReturn(catalog);

        mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value("VqKb4tyj9V6i"))
                .andExpect(jsonPath("$[0].name").value("Widget"))
                .andExpect(jsonPath("$[0].price").value(9.99))
                .andExpect(jsonPath("$[0].currency").value("USD"))
                .andExpect(jsonPath("$[1].id").value("abc123"))
                .andExpect(jsonPath("$[1].name").value("Gadget"))
                .andExpect(jsonPath("$[1].price").value(19.50))
                .andExpect(jsonPath("$[1].currency").value("USD"));
    }
}
