package com.nexusops.booking.controller;

import com.nexusops.booking.entity.Resource;
import com.nexusops.booking.repository.ResourceRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/resources")
public class ResourceController {
    private final ResourceRepository resourceRepository;

    public ResourceController(ResourceRepository resourceRepository) {
        this.resourceRepository = resourceRepository;
    }

    @GetMapping
    public List<Resource> getAll() {
        return resourceRepository.findAll();
    }

    @PreAuthorize("hasAnyAuthority('SCOPE_FACILITY_MANAGER', 'SCOPE_SUPER_ADMIN')")
    @PostMapping
    public Resource create(@RequestBody Resource resource) {
        return resourceRepository.save(resource);
    }
}
