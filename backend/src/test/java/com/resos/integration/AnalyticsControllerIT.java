package com.resos.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AnalyticsControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void analyticsEndpointsReturnAggregatedData() throws Exception {
        String slug = "an-" + System.currentTimeMillis();
        register(slug);
        Session session = login(slug);
        String restaurantId = fetchRestaurantId(session);

        seedOrder(session, restaurantId);

        mockMvc.perform(get("/api/v1/analytics/revenue")
                        .param("restaurantId", restaurantId)
                        .param("startDate", LocalDate.now().minusDays(7).toString())
                        .param("endDate", LocalDate.now().toString())
                        .header("Authorization", "Bearer " + session.accessToken())
                        .header("X-Tenant-ID", session.tenantId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.labels").isArray());

        mockMvc.perform(get("/api/v1/analytics/inventory")
                        .param("restaurantId", restaurantId)
                        .header("Authorization", "Bearer " + session.accessToken())
                        .header("X-Tenant-ID", session.tenantId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalItems").exists());

        mockMvc.perform(get("/api/v1/analytics/employees")
                        .param("restaurantId", restaurantId)
                        .header("Authorization", "Bearer " + session.accessToken())
                        .header("X-Tenant-ID", session.tenantId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.activeEmployees").exists());

        mockMvc.perform(get("/api/v1/analytics/orders")
                        .param("restaurantId", restaurantId)
                        .header("Authorization", "Bearer " + session.accessToken())
                        .header("X-Tenant-ID", session.tenantId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalOrders").value(1));
    }

    private void seedOrder(Session session, String restaurantId) throws Exception {
        MvcResult categoryResult = mockMvc.perform(post("/api/v1/menu/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + session.accessToken())
                        .header("X-Tenant-ID", session.tenantId())
                        .content("""
                                {
                                  "restaurantId": "%s",
                                  "name": "Mains"
                                }
                                """.formatted(restaurantId)))
                .andExpect(status().isCreated())
                .andReturn();
        String categoryId = objectMapper.readTree(categoryResult.getResponse().getContentAsString())
                .get("data").get("id").asText();

        MvcResult itemResult = mockMvc.perform(post("/api/v1/menu/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + session.accessToken())
                        .header("X-Tenant-ID", session.tenantId())
                        .content("""
                                {
                                  "categoryId": "%s",
                                  "name": "Pasta",
                                  "price": 15.00
                                }
                                """.formatted(categoryId)))
                .andExpect(status().isCreated())
                .andReturn();
        String menuItemId = objectMapper.readTree(itemResult.getResponse().getContentAsString())
                .get("data").get("id").asText();

        MvcResult orderResult = mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + session.accessToken())
                        .header("X-Tenant-ID", session.tenantId())
                        .content("""
                                {
                                  "restaurantId": "%s",
                                  "customerName": "Guest",
                                  "items": [{ "menuItemId": "%s", "quantity": 1 }]
                                }
                                """.formatted(restaurantId, menuItemId)))
                .andExpect(status().isCreated())
                .andReturn();

        objectMapper.readTree(orderResult.getResponse().getContentAsString());
    }

    private String fetchRestaurantId(Session session) throws Exception {
        MvcResult restaurants = mockMvc.perform(get("/api/v1/restaurants")
                        .header("Authorization", "Bearer " + session.accessToken())
                        .header("X-Tenant-ID", session.tenantId()))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(restaurants.getResponse().getContentAsString())
                .get("data").get(0).get("id").asText();
    }

    private void register(String slug) throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "tenantName": "Analytics Restaurant",
                                  "tenantSlug": "%s",
                                  "email": "owner@%s.com",
                                  "password": "SecurePass123!",
                                  "firstName": "An",
                                  "lastName": "Owner"
                                }
                                """.formatted(slug, slug.replace("-", ""))))
                .andExpect(status().isCreated());
    }

    private Session login(String slug) throws Exception {
        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "owner@%s.com",
                                  "password": "SecurePass123!",
                                  "tenantSlug": "%s"
                                }
                                """.formatted(slug.replace("-", ""), slug)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode loginJson = objectMapper.readTree(loginResult.getResponse().getContentAsString());
        assertThat(loginJson.get("data").get("accessToken").asText()).isNotBlank();
        return new Session(
                loginJson.get("data").get("accessToken").asText(),
                loginJson.get("data").get("tenant").get("id").asText());
    }

    private record Session(String accessToken, String tenantId) {}
}
