package com.nexusops.booking.controller;

import com.nexusops.booking.entity.Resource;
import com.nexusops.booking.repository.ResourceRepository;
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

    @PostMapping
    public Resource create(@RequestBody Resource resource) {
        return resourceRepository.save(resource);
    }
}
