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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TenantIsolationIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void missingTenantHeaderReturns400() throws Exception {
        TenantSession session = registerAndLogin("alpha-" + System.currentTimeMillis());

        mockMvc.perform(get("/api/v1/auth/me")
                        .header("Authorization", "Bearer " + session.accessToken()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
    }

    @Test
    void mismatchedTenantHeaderReturns403() throws Exception {
        TenantSession session = registerAndLogin("beta-" + System.currentTimeMillis());
        String wrongTenantId = "00000000-0000-0000-0000-000000000001";

        mockMvc.perform(get("/api/v1/auth/me")
                        .header("Authorization", "Bearer " + session.accessToken())
                        .header("X-Tenant-ID", wrongTenantId))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("TENANT_MISMATCH"));
    }

    @Test
    void tenantCannotAccessOtherTenantUser() throws Exception {
        TenantSession tenantA = registerAndLogin("tenant-a-" + System.currentTimeMillis());
        TenantSession tenantB = registerAndLogin("tenant-b-" + System.currentTimeMillis());

        mockMvc.perform(get("/api/v1/users/" + tenantB.userId())
                        .header("Authorization", "Bearer " + tenantA.accessToken())
                        .header("X-Tenant-ID", tenantA.tenantId()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("NOT_FOUND"));
    }

    @Test
    void tenantCanAccessOwnProfileWithMatchingHeader() throws Exception {
        TenantSession session = registerAndLogin("gamma-" + System.currentTimeMillis());

        mockMvc.perform(get("/api/v1/auth/me")
                        .header("Authorization", "Bearer " + session.accessToken())
                        .header("X-Tenant-ID", session.tenantId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").isNotEmpty());
    }

    @Test
    void getCurrentTenantReturnsTenantDetails() throws Exception {
        TenantSession session = registerAndLogin("delta-" + System.currentTimeMillis());

        mockMvc.perform(get("/api/v1/tenants/current")
                        .header("Authorization", "Bearer " + session.accessToken())
                        .header("X-Tenant-ID", session.tenantId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.slug").isNotEmpty())
                .andExpect(jsonPath("$.data.subscription.plan").value("Starter"));
    }

    private TenantSession registerAndLogin(String slug) throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "tenantName": "%s Restaurant",
                                  "tenantSlug": "%s",
                                  "email": "owner@%s.com",
                                  "password": "SecurePass123!",
                                  "firstName": "Owner",
                                  "lastName": "Test"
                                }
                                """.formatted(slug, slug, slug.replace("-", ""))))
                .andExpect(status().isCreated());

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
        String accessToken = loginJson.get("data").get("accessToken").asText();
        String tenantId = loginJson.get("data").get("tenant").get("id").asText();
        String userId = loginJson.get("data").get("user").get("id").asText();

        assertThat(accessToken).isNotBlank();
        return new TenantSession(accessToken, tenantId, userId);
    }

    private record TenantSession(String accessToken, String tenantId, String userId) {}
}
