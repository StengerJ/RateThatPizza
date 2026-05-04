package com.pghpizza.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pghpizza.api.auth.PasswordResetTokenRepository;
import com.pghpizza.api.user.UserRepository;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ApiFlowSecurityTests {
    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordResetTokenRepository tokenRepository;

    @Test
    void publicRatingsEndpointAllowsAnonymousUsers() throws Exception {
        mockMvc.perform(get("/api/ratings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void contributorApplicationsAllowProductionCorsOrigin() throws Exception {
        mockMvc.perform(options("/api/applications")
                .header("Origin", "https://pghpizza.org")
                .header("Access-Control-Request-Method", "POST")
                .header("Access-Control-Request-Headers", "content-type"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "https://pghpizza.org"));
    }

    @Test
    void pendingContributorCannotPublishUntilAdminApproves() throws Exception {
        submitApplication("pending@example.com", "Pending Person", "PendingPassword123!");
        String pendingToken = login("pending@example.com", "PendingPassword123!");

        mockMvc.perform(post("/api/ratings")
                .header("Authorization", "Bearer " + pendingToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of(
                        "restaurantName", "Mineo's",
                        "sauce", "Balanced",
                        "toppings", "Pepperoni",
                        "crust", "Crisp",
                        "overallRating", 8,
                        "comments", "Pending users should not be able to publish"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminsApproveContributorsAndContributorsCanOnlyDeleteTheirOwnRatings() throws Exception {
        submitApplication("first@example.com", "First Contributor", "FirstPassword123!");
        submitApplication("second@example.com", "Second Contributor", "SecondPassword123!");

        String adminToken = login("admin@pgh-pizza.local", "ChangeMe123!");
        approveFirstPendingApplication(adminToken);
        approveFirstPendingApplication(adminToken);

        String firstToken = login("first@example.com", "FirstPassword123!");
        String secondToken = login("second@example.com", "SecondPassword123!");

        String ratingResponse = mockMvc.perform(post("/api/ratings")
                .header("Authorization", "Bearer " + firstToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of(
                        "restaurantName", "Driftwood Oven",
                        "sauce", "Bright",
                        "toppings", "Mushroom",
                        "crust", "Wood fired",
                        "overallRating", 9,
                        "comments", "Great slice"))))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String ratingId = objectMapper.readTree(ratingResponse).get("id").asText();

        mockMvc.perform(delete("/api/ratings/{id}", ratingId)
                .header("Authorization", "Bearer " + secondToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(delete("/api/ratings/{id}", ratingId)
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());
    }

    @Test
    void passwordsAndResetTokensAreStoredOnlyAsHashes() throws Exception {
        String rawPassword = "HashMePlease123!";
        submitApplication("hashes@example.com", "Hash Tester", rawPassword);

        var user = userRepository.findByEmail("hashes@example.com").orElseThrow();
        assertThat(user.getPasswordHash()).isNotEqualTo(rawPassword);
        assertThat(user.getPasswordHash()).startsWith("$2");

        mockMvc.perform(post("/api/auth/password-reset/request")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("email", "hashes@example.com"))))
                .andExpect(status().isAccepted());

        var token = tokenRepository.findAll().getFirst();
        assertThat(token.getTokenHash()).hasSize(64);
        assertThat(token.getTokenHash()).doesNotContain("hashes@example.com");
    }

    @Test
    void sqlInjectionLookingInputIsStoredAsTextAndDoesNotChangeQueryBehavior() throws Exception {
        String adminToken = login("admin@pgh-pizza.local", "ChangeMe123!");
        String maliciousText = "'); DROP TABLE users; --";

        mockMvc.perform(post("/api/ratings")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of(
                        "restaurantName", maliciousText,
                        "sauce", "Still sauce",
                        "toppings", "Plain text",
                        "crust", "Safe",
                        "overallRating", 7,
                        "comments", maliciousText))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.restaurantName").value(maliciousText))
                .andExpect(jsonPath("$.comments").value(maliciousText));

        assertThat(userRepository.findByEmail("admin@pgh-pizza.local")).isPresent();
        mockMvc.perform(get("/api/ratings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].restaurantName").value(maliciousText));
    }

    private void submitApplication(String email, String displayName, String password) throws Exception {
        mockMvc.perform(post("/api/applications")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of(
                        "email", email,
                        "displayName", displayName,
                        "password", password,
                        "applicationReason", "I want to help review Pittsburgh pizza spots."))))
                .andExpect(status().isOk());
    }

    private String login(String email, String password) throws Exception {
        String response = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("email", email, "password", password))))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(response).get("token").asText();
    }

    private void approveFirstPendingApplication(String adminToken) throws Exception {
        String response = mockMvc.perform(get("/api/admin/applications")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode applications = objectMapper.readTree(response);
        String pendingId = null;
        for (JsonNode application : applications) {
            if ("PENDING".equals(application.get("status").asText())) {
                pendingId = application.get("id").asText();
                break;
            }
        }

        assertThat(pendingId).isNotNull();
        mockMvc.perform(post("/api/admin/applications/{id}/approve", pendingId)
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }

    private String json(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }
}
