package com.skybooker.AirlineAirportService.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "airlines",uniqueConstraints = @UniqueConstraint(columnNames = "iata_code"))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Airline {
    @Id
    @GeneratedValue
    private UUID airlineId;

    @Column(nullable = false)
    private String airlineName;

    @Column(nullable = false,unique = true)
    private String iataCode;
    private String icaoCode;

    private String country;
    private String logoUrl;

    private String contactEmail;

    private String contactPhone;

    @Column(nullable = false)
    private boolean isActive = true;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
