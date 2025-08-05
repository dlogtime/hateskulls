package com.hateskulls.hate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hateskulls.hate.model.ChangeRequest;
import com.hateskulls.hate.repository.ChangeRequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ChangeRequestController.class)
class ChangeRequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ChangeRequestRepository repository;

    @Autowired
    private ObjectMapper objectMapper;

    private ChangeRequest sampleChangeRequest;

    @BeforeEach
    void setUp() {
        sampleChangeRequest = new ChangeRequest("parallel os upgrade", "Upgrade all 47 servers simultaneously without causing chaos", "Capital.UwU");
        sampleChangeRequest.setId(1L);
        sampleChangeRequest.setCreatedAt(LocalDateTime.now());
        sampleChangeRequest.setStatus(ChangeRequest.Status.PENDING);
    }

    @Test
    void getAllChangeRequests_ReturnsPagedResults() throws Exception {
        // Given
        Page<ChangeRequest> page = new PageImpl<>(Arrays.asList(sampleChangeRequest));
        
        when(repository.findAll(any(PageRequest.class))).thenReturn(page);

        // When & Then
        mockMvc.perform(get("/change-requests")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(repository).findAll(any(PageRequest.class));
    }

    @Test
    void getChangeRequest_WhenExists_ReturnsChangeRequest() throws Exception {
        // Given
        when(repository.findById(1L)).thenReturn(Optional.of(sampleChangeRequest));

        // When & Then
        mockMvc.perform(get("/change-requests/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("parallel os upgrade"))
                .andExpect(jsonPath("$.requestedBy").value("Capital.UwU"))
                .andExpect(jsonPath("$._links.self.href").exists());

        verify(repository).findById(1L);
    }

    @Test
    void getChangeRequest_WhenNotExists_Returns404() throws Exception {
        // Given
        when(repository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/change-requests/999")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(repository).findById(999L);
    }

    @Test
    void createChangeRequest_ValidRequest_ReturnsCreated() throws Exception {
        // Given
        ChangeRequest newRequest = new ChangeRequest("New feature", "Add dark mode", "jane.doe");
        ChangeRequest savedRequest = new ChangeRequest("New feature", "Add dark mode", "jane.doe");
        savedRequest.setId(2L);

        when(repository.save(any(ChangeRequest.class))).thenReturn(savedRequest);

        // When & Then
        mockMvc.perform(post("/change-requests")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("New feature"))
                .andExpect(jsonPath("$._links.self.href").exists());

        verify(repository).save(any(ChangeRequest.class));
    }

    @Test
    void updateChangeRequest_WhenExists_ReturnsUpdated() throws Exception {
        // Given
        ChangeRequest updatedRequest = new ChangeRequest("Updated title", "Updated description", "john.doe");
        updatedRequest.setId(1L);

        when(repository.existsById(1L)).thenReturn(true);
        when(repository.save(any(ChangeRequest.class))).thenReturn(updatedRequest);

        // When & Then
        mockMvc.perform(put("/change-requests/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated title"));

        verify(repository).existsById(1L);
        verify(repository).save(any(ChangeRequest.class));
    }

    @Test
    void updateChangeRequest_WhenNotExists_Returns404() throws Exception {
        // Given
        ChangeRequest updatedRequest = new ChangeRequest("Updated title", "Updated description", "john.doe");
        when(repository.existsById(999L)).thenReturn(false);

        // When & Then
        mockMvc.perform(put("/change-requests/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedRequest)))
                .andExpect(status().isNotFound());

        verify(repository).existsById(999L);
        verify(repository, never()).save(any(ChangeRequest.class));
    }

    @Test
    void deleteChangeRequest_WhenExists_ReturnsNoContent() throws Exception {
        // Given
        when(repository.existsById(1L)).thenReturn(true);

        // When & Then
        mockMvc.perform(delete("/change-requests/1"))
                .andExpect(status().isNoContent());

        verify(repository).existsById(1L);
        verify(repository).deleteById(1L);
    }

    @Test
    void deleteChangeRequest_WhenNotExists_Returns404() throws Exception {
        // Given
        when(repository.existsById(999L)).thenReturn(false);

        // When & Then
        mockMvc.perform(delete("/change-requests/999"))
                .andExpect(status().isNotFound());

        verify(repository).existsById(999L);
        verify(repository, never()).deleteById(999L);
    }
}
