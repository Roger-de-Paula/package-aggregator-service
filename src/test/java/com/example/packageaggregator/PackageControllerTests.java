package com.example.packageaggregator;

import com.example.packageaggregator.client.ExchangeRateClient;
import com.example.packageaggregator.client.ProductClient;
import com.example.packageaggregator.client.dto.ExternalProductResponse;
import com.example.packageaggregator.repository.PackageJpaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PackageControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PackageJpaRepository packageRepository;

    @MockBean
    private ProductClient productClient;

    @MockBean
    private ExchangeRateClient exchangeRateClient;

    @Test
    void createPackage() throws Exception {
        when(productClient.getProductById(1L)).thenReturn(ExternalProductResponse.builder()
                .id(1L)
                .name("Product 1")
                .usdPrice(new BigDecimal("10.00"))
                .build());
        when(productClient.getProductById(2L)).thenReturn(ExternalProductResponse.builder()
                .id(2L)
                .name("Product 2")
                .usdPrice(new BigDecimal("20.00"))
                .build());

        String body = "{\"name\":\"Starter Pack\",\"description\":\"Basic bundle\",\"productIds\":[1,2]}";
        mockMvc.perform(post("/packages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Starter Pack"))
                .andExpect(jsonPath("$.description").value("Basic bundle"))
                .andExpect(jsonPath("$.totalPrice").value(30.0))
                .andExpect(jsonPath("$.currency").value("USD"))
                .andExpect(jsonPath("$.products.length()").value(2));
    }

    @Test
    void getPackagesPaginated() throws Exception {
        mockMvc.perform(get("/packages").param("page", "0").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10));
    }

    @Test
    void getPackagesWithCurrency() throws Exception {
        when(exchangeRateClient.getRateUsdTo(anyString())).thenReturn(new BigDecimal("0.92"));
        mockMvc.perform(get("/packages").param("currency", "EUR"))
                .andExpect(status().isOk());
    }
}
