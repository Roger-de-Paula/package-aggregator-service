package com.example.packageaggregator;

import com.example.packageaggregator.client.ExchangeRateClient;
import com.example.packageaggregator.client.ProductClient;
import com.example.packageaggregator.client.dto.ExternalProductResponse;
import com.example.packageaggregator.repository.PackageJpaRepository;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
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
        when(productClient.getProductsByIds(anyList())).thenReturn(Map.of(
                "id-1", ExternalProductResponse.builder()
                        .id("id-1")
                        .name("Product 1")
                        .usdPrice(new BigDecimal("10.00"))
                        .build(),
                "id-2", ExternalProductResponse.builder()
                        .id("id-2")
                        .name("Product 2")
                        .usdPrice(new BigDecimal("20.00"))
                        .build()
        ));

        String body = "{\"name\":\"Starter Pack\",\"description\":\"Basic bundle\",\"productIds\":[\"id-1\",\"id-2\"]}";
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
    void createPackage_blankName_returns400() throws Exception {
        mockMvc.perform(post("/packages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\",\"description\":\"\",\"productIds\":[\"id-1\"]}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createPackage_emptyProductIds_returns400() throws Exception {
        mockMvc.perform(post("/packages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"A Pack\",\"description\":\"\",\"productIds\":[]}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createPackage_partialProductResponse_returns400() throws Exception {
        when(productClient.getProductsByIds(anyList())).thenReturn(Map.of(
                "id-1", ExternalProductResponse.builder().id("id-1").name("Product 1").usdPrice(new BigDecimal("10.00")).build()
        ));
        mockMvc.perform(post("/packages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Pack\",\"description\":\"\",\"productIds\":[\"id-1\",\"id-missing\"]}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createPackage_invalidProduct_returns400() throws Exception {
        when(productClient.getProductsByIds(anyList())).thenReturn(Map.of(
                "id-1", ExternalProductResponse.builder().id("id-1").name("Product 1").usdPrice(new BigDecimal("10.00")).build(),
                "id-missing", ExternalProductResponse.builder().id("id-missing").name("Unknown").usdPrice(null).build()
        ));
        String body = "{\"name\":\"Bad Pack\",\"description\":\"\",\"productIds\":[\"id-1\",\"id-missing\"]}";
        mockMvc.perform(post("/packages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
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

    @Test
    void deletePackage_secondDeleteReturns204() throws Exception {
        when(productClient.getProductsByIds(anyList())).thenReturn(Map.of(
                "id-1", ExternalProductResponse.builder().id("id-1").name("P1").usdPrice(new BigDecimal("5.00")).build()
        ));
        String createBody = "{\"name\":\"To Delete\",\"description\":\"\",\"productIds\":[\"id-1\"]}";
        String responseBody = mockMvc.perform(post("/packages").contentType(MediaType.APPLICATION_JSON).content(createBody))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String id = JsonPath.read(responseBody, "$.id");
        mockMvc.perform(delete("/packages/" + id)).andExpect(status().isNoContent());
        mockMvc.perform(delete("/packages/" + id)).andExpect(status().isNoContent());
    }
}
