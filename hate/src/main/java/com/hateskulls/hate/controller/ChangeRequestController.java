package com.hateskulls.hate.controller;

import com.hateskulls.hate.model.ChangeRequest;
import com.hateskulls.hate.repository.ChangeRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/change-requests")
public class ChangeRequestController {
    
    @Autowired
    private ChangeRequestRepository repository;
    
    @Autowired
    private PagedResourcesAssembler<ChangeRequest> pagedResourcesAssembler;
    
    // GET /change-requests
    @GetMapping
    public PagedModel<EntityModel<ChangeRequest>> getAllChangeRequests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) ChangeRequest.Status status) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<ChangeRequest> changeRequests;
        
        if (status != null) {
            changeRequests = repository.findByStatus(status, pageable);
        } else {
            changeRequests = repository.findAll(pageable);
        }
        
        PagedModel<EntityModel<ChangeRequest>> pagedModel = pagedResourcesAssembler.toModel(changeRequests, this::toEntityModel);
        
        // Add HATEOAS links to the collection
        pagedModel.add(linkTo(methodOn(ChangeRequestController.class)
            .getAllChangeRequests(page, size, sortBy, sortDir, status)).withSelfRel());
        
        // Add CREATE link - this is what was missing!
        pagedModel.add(linkTo(ChangeRequestController.class).withRel("create")
            .withType("application/json"));
        
        // Add search/filter links for available statuses
        pagedModel.add(linkTo(methodOn(ChangeRequestController.class)
            .getAllChangeRequests(page, size, sortBy, sortDir, ChangeRequest.Status.PENDING))
            .withRel("search-pending"));
        pagedModel.add(linkTo(methodOn(ChangeRequestController.class)
            .getAllChangeRequests(page, size, sortBy, sortDir, ChangeRequest.Status.IN_PROGRESS))
            .withRel("search-in-progress"));
        pagedModel.add(linkTo(methodOn(ChangeRequestController.class)
            .getAllChangeRequests(page, size, sortBy, sortDir, ChangeRequest.Status.COMPLETED))
            .withRel("search-completed"));
        
        return pagedModel;
    }
    
    // GET /change-requests/{id}
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<ChangeRequest>> getChangeRequest(@PathVariable Long id) {
        Optional<ChangeRequest> changeRequest = repository.findById(id);
        
        if (changeRequest.isPresent()) {
            return ResponseEntity.ok(toEntityModel(changeRequest.get()));
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    // POST /change-requests
    @PostMapping
    public EntityModel<ChangeRequest> createChangeRequest(@RequestBody ChangeRequest changeRequest) {
        ChangeRequest saved = repository.save(changeRequest);
        return toEntityModel(saved);
    }
    
    // PUT /change-requests/{id}
    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<ChangeRequest>> updateChangeRequest(
            @PathVariable Long id, @RequestBody ChangeRequest changeRequest) {
        
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        
        changeRequest.setId(id);
        ChangeRequest updated = repository.save(changeRequest);
        return ResponseEntity.ok(toEntityModel(updated));
    }
    
    // DELETE /change-requests/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteChangeRequest(@PathVariable Long id) {
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
    
    // Helper method to add HATEOAS links
    private EntityModel<ChangeRequest> toEntityModel(ChangeRequest changeRequest) {
        return EntityModel.of(changeRequest)
            .add(linkTo(methodOn(ChangeRequestController.class)
                .getChangeRequest(changeRequest.getId())).withSelfRel())
            .add(linkTo(methodOn(ChangeRequestController.class)
                .getAllChangeRequests(0, 10, "id", "desc", null)).withRel("all-change-requests"))
            .add(linkTo(methodOn(ChangeRequestController.class)
                .updateChangeRequest(changeRequest.getId(), null)).withRel("update"))
            .add(linkTo(methodOn(ChangeRequestController.class)
                .deleteChangeRequest(changeRequest.getId())).withRel("delete"));
    }
}
