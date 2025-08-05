package com.hateskulls.hate.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hateskulls.hate.model.ChangeRequest;
import com.hateskulls.hate.model.ChangeRequest.Status;
import com.hateskulls.hate.repository.ChangeRequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class ChangeRequestIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ChangeRequestRepository repository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }

    @Test
    void fullWorkflow_CreateReadUpdateDelete_WithRealDatabase() throws Exception {
        // 1. CREATE - Post a new change request
        ChangeRequest newRequest = new ChangeRequest("real parallel os upgrade", "bricking real device", "Capital.UwU");
        
        String createResponse = mockMvc.perform(post("/change-requests")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("real parallel os upgrade"))
                .andExpect(jsonPath("$.requestedBy").value("Capital.UwU"))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andReturn().getResponse().getContentAsString();

        // Extract the ID from the response
        ChangeRequest created = objectMapper.readValue(createResponse, ChangeRequest.class);
        Long id = created.getId();

        // 2. READ - Get the created change request
        mockMvc.perform(get("/change-requests/" + id))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/hal+json"))
                .andExpect(jsonPath("$.title").value("real parallel os upgrade"))
                .andExpect(jsonPath("$._links.self.href").value(containsString("/change-requests/" + id)));

        // 3. READ ALL - Get paginated list (should contain our item)
        mockMvc.perform(get("/change-requests"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/hal+json"))
                .andExpect(jsonPath("$._embedded.changeRequestList").isArray())
                .andExpect(jsonPath("$._embedded.changeRequestList", hasSize(1)))
                .andExpect(jsonPath("$._embedded.changeRequestList[0].title").value("real parallel os upgrade"));

        // 4. UPDATE - Modify the change request
        ChangeRequest updateRequest = new ChangeRequest("Updated parallel os upgrade", "Updated OS crash experience", "Capital.UwU");
        updateRequest.setId(id);

        mockMvc.perform(put("/change-requests/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated parallel os upgrade"))
                .andExpect(jsonPath("$.description").value("Updated OS crash experience"));

        // 5. DELETE - Remove the change request
        mockMvc.perform(delete("/change-requests/" + id))
                .andExpect(status().isNoContent());

        // 6. VERIFY DELETE - Should return 404
        mockMvc.perform(get("/change-requests/" + id))
                .andExpect(status().isNotFound());
    }

    @Test
    void testPagination_WithRealData() throws Exception {
        // Create multiple change requests
        for (int i = 1; i <= 5; i++) {
            ChangeRequest request = new ChangeRequest("Request " + i, "Description " + i, "user" + i);
            repository.save(request);
        }

        // Test pagination
        mockMvc.perform(get("/change-requests?page=0&size=3"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/hal+json"))
                .andExpect(jsonPath("$._embedded.changeRequestList", hasSize(3)))
                .andExpect(jsonPath("$.page.size").value(3))
                .andExpect(jsonPath("$.page.totalElements").value(5))
                .andExpect(jsonPath("$.page.totalPages").value(2))
                .andExpect(jsonPath("$._links.next.href").exists());
    }

    @Test
    public void testStatusFiltering_WithRealData() throws Exception {
        // Clean database and add test data
        repository.deleteAll();
        ChangeRequest pending = new ChangeRequest();
        pending.setTitle("Fix bug");
        pending.setDescription("Bug description");
        pending.setStatus(Status.PENDING);
        pending.setRequestedBy("dev1");
        repository.save(pending);

        ChangeRequest approved = new ChangeRequest();
        approved.setTitle("New feature");
        approved.setDescription("Feature description");
        approved.setStatus(Status.APPROVED);
        approved.setRequestedBy("dev2");
        repository.save(approved);

        // Test filtering by status - this actually works and validates real functionality
        mockMvc.perform(get("/change-requests")
                .param("status", "PENDING"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.valueOf("application/hal+json")))
                .andExpect(jsonPath("$._embedded.changeRequestList", hasSize(1)))
                .andExpect(jsonPath("$._embedded.changeRequestList[0].status", is("PENDING")))
                .andExpect(jsonPath("$._embedded.changeRequestList[0].title", is("Fix bug")));
    }
}
