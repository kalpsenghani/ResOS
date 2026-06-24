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
class EmployeeControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void employeeCrudAndScheduling() throws Exception {
        String slug = "emp-" + System.currentTimeMillis();
        register(slug);
        Session session = login(slug);
        String restaurantId = fetchRestaurantId(session);

        MvcResult createResult = mockMvc.perform(post("/api/v1/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + session.accessToken())
                        .header("X-Tenant-ID", session.tenantId())
                        .content("""
                                {
                                  "restaurantId": "%s",
                                  "firstName": "Mike",
                                  "lastName": "Johnson",
                                  "email": "mike@%s.com",
                                  "phone": "+1234567890",
                                  "position": "Server",
                                  "hourlyRate": 15.50,
                                  "hireDate": "2026-03-01"
                                }
                                """.formatted(restaurantId, slug.replace("-", ""))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.status").value("ACTIVE"))
                .andReturn();

        String employeeId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .get("data").get("id").asText();

        mockMvc.perform(get("/api/v1/employees")
                        .param("restaurantId", restaurantId)
                        .param("status", "ACTIVE")
                        .header("Authorization", "Bearer " + session.accessToken())
                        .header("X-Tenant-ID", session.tenantId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1));

        String shiftDate = LocalDate.now().plusDays(1).toString();
        MvcResult scheduleResult = mockMvc.perform(post("/api/v1/employees/" + employeeId + "/schedules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + session.accessToken())
                        .header("X-Tenant-ID", session.tenantId())
                        .content("""
                                {
                                  "restaurantId": "%s",
                                  "shiftDate": "%s",
                                  "startTime": "09:00",
                                  "endTime": "17:00",
                                  "notes": "Opening shift"
                                }
                                """.formatted(restaurantId, shiftDate)))
                .andExpect(status().isCreated())
                .andReturn();

        String scheduleId = objectMapper.readTree(scheduleResult.getResponse().getContentAsString())
                .get("data").get("id").asText();

        mockMvc.perform(get("/api/v1/employees/" + employeeId + "/schedules")
                        .header("Authorization", "Bearer " + session.accessToken())
                        .header("X-Tenant-ID", session.tenantId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1));

        mockMvc.perform(get("/api/v1/dashboard/kpis")
                        .param("restaurantId", restaurantId)
                        .header("Authorization", "Bearer " + session.accessToken())
                        .header("X-Tenant-ID", session.tenantId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.activeEmployees.value").value(1));

        mockMvc.perform(delete("/api/v1/employees/schedules/" + scheduleId)
                        .header("Authorization", "Bearer " + session.accessToken())
                        .header("X-Tenant-ID", session.tenantId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(delete("/api/v1/employees/" + employeeId)
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
                                  "tenantName": "Employee Restaurant",
                                  "tenantSlug": "%s",
                                  "email": "owner@%s.com",
                                  "password": "SecurePass123!",
                                  "firstName": "Emp",
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
