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
class InventoryControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void inventoryCrudTransactionsAndAlerts() throws Exception {
        String slug = "inv-" + System.currentTimeMillis();
        register(slug);
        Session session = login(slug);
        String restaurantId = fetchRestaurantId(session);

        MvcResult createResult = mockMvc.perform(post("/api/v1/inventory")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + session.accessToken())
                        .header("X-Tenant-ID", session.tenantId())
                        .content("""
                                {
                                  "restaurantId": "%s",
                                  "name": "Tomatoes",
                                  "sku": "PROD-001",
                                  "category": "Produce",
                                  "unit": "kg",
                                  "currentStock": 2.5,
                                  "minimumStock": 5.0,
                                  "maximumStock": 50.0,
                                  "unitCost": 3.50,
                                  "supplier": "Fresh Farms Co."
                                }
                                """.formatted(restaurantId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.isLowStock").value(true))
                .andReturn();

        String itemId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .get("data").get("id").asText();

        mockMvc.perform(get("/api/v1/inventory")
                        .param("restaurantId", restaurantId)
                        .param("lowStock", "true")
                        .header("Authorization", "Bearer " + session.accessToken())
                        .header("X-Tenant-ID", session.tenantId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1));

        mockMvc.perform(get("/api/v1/inventory/alerts")
                        .param("restaurantId", restaurantId)
                        .param("acknowledged", "false")
                        .header("Authorization", "Bearer " + session.accessToken())
                        .header("X-Tenant-ID", session.tenantId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1));

        mockMvc.perform(post("/api/v1/inventory/" + itemId + "/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + session.accessToken())
                        .header("X-Tenant-ID", session.tenantId())
                        .content("""
                                {
                                  "type": "PURCHASE",
                                  "quantity": 10.0,
                                  "unitCost": 3.50,
                                  "reference": "PO-2026-0142"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.quantity").value(10.0));

        mockMvc.perform(get("/api/v1/inventory/" + itemId)
                        .header("Authorization", "Bearer " + session.accessToken())
                        .header("X-Tenant-ID", session.tenantId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.currentStock").value(12.5))
                .andExpect(jsonPath("$.data.isLowStock").value(false));

        mockMvc.perform(get("/api/v1/dashboard/kpis")
                        .param("restaurantId", restaurantId)
                        .header("Authorization", "Bearer " + session.accessToken())
                        .header("X-Tenant-ID", session.tenantId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.lowStockItems.value").value(0));

        mockMvc.perform(get("/api/v1/audit-logs")
                        .param("entityType", "InventoryItem")
                        .header("Authorization", "Bearer " + session.accessToken())
                        .header("X-Tenant-ID", session.tenantId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2));

        mockMvc.perform(delete("/api/v1/inventory/" + itemId)
                        .header("Authorization", "Bearer " + session.accessToken())
                        .header("X-Tenant-ID", session.tenantId()))
                .andExpect(status().isNoContent());
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
                                  "tenantName": "Inventory Restaurant",
                                  "tenantSlug": "%s",
                                  "email": "owner@%s.com",
                                  "password": "SecurePass123!",
                                  "firstName": "Inv",
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
                loginJson.get("data").get("tenant").get("id").asText()
        );
    }

    private record Session(String accessToken, String tenantId) {}
}
