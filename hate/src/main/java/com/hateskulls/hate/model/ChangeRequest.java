package com.hateskulls.hate.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "change_requests")
public class ChangeRequest {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    @NotBlank(message = "Title is required")
    @Size(min = 3, max = 100, message = "Title must be between 3 and 100 characters")
    private String title;
    
    @Column(length = 1000)
    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;
    
    @Enumerated(EnumType.STRING)
    private Status status = Status.PENDING;
    
    @Column(name = "requested_by", nullable = false)
    @NotBlank(message = "Requested by is required")
    @Size(min = 2, max = 50, message = "Requested by must be between 2 and 50 characters")
    private String requestedBy;
    
    @Column(name = "created_at", nullable = false)
    @JsonIgnore // Don't include in HAL-FORMS templates
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "updated_at")
    @JsonIgnore // Don't include in HAL-FORMS templates
    private LocalDateTime updatedAt;
    
    // Default constructor
    public ChangeRequest() {}
    
    // Constructor
    public ChangeRequest(String title, String description, String requestedBy) {
        this.title = title;
        this.description = description;
        this.requestedBy = requestedBy;
    }
    
    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    
    public String getRequestedBy() { return requestedBy; }
    public void setRequestedBy(String requestedBy) { this.requestedBy = requestedBy; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    public enum Status {
        PENDING, APPROVED, REJECTED, IN_PROGRESS, COMPLETED
    }
}
