package com.hateskulls.hate.repository;

import com.hateskulls.hate.model.ChangeRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChangeRequestRepository extends JpaRepository<ChangeRequest, Long> {
    
    // Find by status
    Page<ChangeRequest> findByStatus(ChangeRequest.Status status, Pageable pageable);
    
    // Find by requested by
    Page<ChangeRequest> findByRequestedBy(String requestedBy, Pageable pageable);
    
    // Find by title containing (case insensitive search)
    @Query("SELECT cr FROM ChangeRequest cr WHERE LOWER(cr.title) LIKE LOWER(CONCAT('%', :title, '%'))")
    Page<ChangeRequest> findByTitleContainingIgnoreCase(@Param("title") String title, Pageable pageable);
    
    // Count by status
    long countByStatus(ChangeRequest.Status status);
}
