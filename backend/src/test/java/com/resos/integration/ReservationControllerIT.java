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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ReservationControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void tableReservationCrudAndAvailability() throws Exception {
        String slug = "res-" + System.currentTimeMillis();
        register(slug);
        Session session = login(slug);
        String restaurantId = fetchRestaurantId(session);

        MvcResult tableResult = mockMvc.perform(post("/api/v1/tables")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + session.accessToken())
                        .header("X-Tenant-ID", session.tenantId())
                        .content("""
                                {
                                  "restaurantId": "%s",
                                  "tableNumber": "T12",
                                  "capacity": 4,
                                  "location": "Main Floor"
                                }
                                """.formatted(restaurantId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.tableNumber").value("T12"))
                .andReturn();

        String tableId = objectMapper.readTree(tableResult.getResponse().getContentAsString())
                .get("data").get("id").asText();

        String reservationDate = LocalDate.now().plusDays(2).toString();

        mockMvc.perform(get("/api/v1/reservations/availability")
                        .param("restaurantId", restaurantId)
                        .param("date", reservationDate)
                        .param("partySize", "4")
                        .param("startTime", "19:00")
                        .header("Authorization", "Bearer " + session.accessToken())
                        .header("X-Tenant-ID", session.tenantId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.available").value(true))
                .andExpect(jsonPath("$.data.suggestedTables.length()").value(1));

        MvcResult reservationResult = mockMvc.perform(post("/api/v1/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + session.accessToken())
                        .header("X-Tenant-ID", session.tenantId())
                        .content("""
                                {
                                  "restaurantId": "%s",
                                  "tableId": "%s",
                                  "guestName": "John Doe",
                                  "guestPhone": "+1234567890",
                                  "guestEmail": "john@example.com",
                                  "partySize": 4,
                                  "reservationDate": "%s",
                                  "startTime": "19:00",
                                  "specialRequests": "Window seat"
                                }
                                """.formatted(restaurantId, tableId, reservationDate)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.status").value("CONFIRMED"))
                .andReturn();

        String reservationId = objectMapper.readTree(reservationResult.getResponse().getContentAsString())
                .get("data").get("id").asText();

        mockMvc.perform(get("/api/v1/reservations")
                        .param("restaurantId", restaurantId)
                        .param("date", reservationDate)
                        .header("Authorization", "Bearer " + session.accessToken())
                        .header("X-Tenant-ID", session.tenantId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1));

        mockMvc.perform(patch("/api/v1/reservations/" + reservationId + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + session.accessToken())
                        .header("X-Tenant-ID", session.tenantId())
                        .content("""
                                { "status": "SEATED" }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("SEATED"));

        mockMvc.perform(get("/api/v1/dashboard/kpis")
                        .param("restaurantId", restaurantId)
                        .header("Authorization", "Bearer " + session.accessToken())
                        .header("X-Tenant-ID", session.tenantId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.reservations.value").exists());

        mockMvc.perform(delete("/api/v1/reservations/" + reservationId)
                        .header("Authorization", "Bearer " + session.accessToken())
                        .header("X-Tenant-ID", session.tenantId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(delete("/api/v1/tables/" + tableId)
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
                                  "tenantName": "Reservation Restaurant",
                                  "tenantSlug": "%s",
                                  "email": "owner@%s.com",
                                  "password": "SecurePass123!",
                                  "firstName": "Res",
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
