package com.hateskulls.hate.controller;

import org.springframework.hateoas.RepresentationModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
public class RootController {
    
    @GetMapping("/")
    public RepresentationModel<?> root() {
        RepresentationModel<?> rootResource = new RepresentationModel<>();
        
        // Add link to the main change-requests resource
        rootResource.add(linkTo(ChangeRequestController.class).withRel("change-requests"));
        
        return rootResource;
    }
}
