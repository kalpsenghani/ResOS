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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MenuOrderControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void menuAndOrderLifecycle() throws Exception {
        String slug = "menu-" + System.currentTimeMillis();
        register(slug);
        Session session = login(slug);
        String restaurantId = fetchRestaurantId(session);

        MvcResult categoryResult = mockMvc.perform(post("/api/v1/menu/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + session.accessToken())
                        .header("X-Tenant-ID", session.tenantId())
                        .content("""
                                {
                                  "restaurantId": "%s",
                                  "name": "Mains",
                                  "description": "Main courses",
                                  "sortOrder": 1
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
                                  "name": "Classic Burger",
                                  "description": "Angus beef patty",
                                  "price": 12.99,
                                  "cost": 4.50,
                                  "preparationTime": 12,
                                  "modifiers": [
                                    { "name": "Extra Cheese", "priceAdjustment": 1.50 }
                                  ]
                                }
                                """.formatted(categoryId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.available").value(true))
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
                                  "customerName": "Table 3",
                                  "orderType": "DINE_IN",
                                  "items": [
                                    {
                                      "menuItemId": "%s",
                                      "quantity": 2,
                                      "specialInstructions": "No onions"
                                    }
                                  ],
                                  "notes": "Rush order"
                                }
                                """.formatted(restaurantId, menuItemId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.status").value("PENDING"))
                .andReturn();

        String orderId = objectMapper.readTree(orderResult.getResponse().getContentAsString())
                .get("data").get("id").asText();
        String itemId = objectMapper.readTree(orderResult.getResponse().getContentAsString())
                .get("data").get("items").get(0).get("id").asText();

        mockMvc.perform(patch("/api/v1/orders/" + orderId + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + session.accessToken())
                        .header("X-Tenant-ID", session.tenantId())
                        .content("""
                                { "status": "CONFIRMED", "notes": "Accepted" }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CONFIRMED"));

        mockMvc.perform(patch("/api/v1/orders/" + orderId + "/items/" + itemId + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + session.accessToken())
                        .header("X-Tenant-ID", session.tenantId())
                        .content("""
                                { "status": "PREPARING" }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PREPARING"));

        mockMvc.perform(get("/api/v1/dashboard/kpis")
                        .param("restaurantId", restaurantId)
                        .header("Authorization", "Bearer " + session.accessToken())
                        .header("X-Tenant-ID", session.tenantId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.orders.value").value(1));

        mockMvc.perform(get("/api/v1/dashboard/recent-orders")
                        .param("restaurantId", restaurantId)
                        .header("Authorization", "Bearer " + session.accessToken())
                        .header("X-Tenant-ID", session.tenantId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1));
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
                                  "tenantName": "Menu Restaurant",
                                  "tenantSlug": "%s",
                                  "email": "owner@%s.com",
                                  "password": "SecurePass123!",
                                  "firstName": "Menu",
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
