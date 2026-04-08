package com.nexusops.booking.entity;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "resources")
public class Resource {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String type;

    private int capacity;

    private boolean requireApproval;

    public Resource() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    public boolean isRequireApproval() { return requireApproval; }
    public void setRequireApproval(boolean requireApproval) { this.requireApproval = requireApproval; }
}
