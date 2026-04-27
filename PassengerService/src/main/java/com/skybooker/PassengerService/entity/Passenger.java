package com.skybooker.PassengerService.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "passengers")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Passenger {
    @Id
    @GeneratedValue
    private UUID passengerId;

    private UUID bookingId;

    private String title;
    private String firstName;
    private String lastName;

    private LocalDate dateOfBirth;
    private String gender;

    private String passportNumber;
    private String nationality;
    private LocalDate passportExpiry;

    private UUID seatId;
    private String seatNumber;

    @Column(unique = true)
    private String ticketNumber;

    @Enumerated(EnumType.STRING)
    private PassengerType passengerType;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    public void onCreate(){
        createdAt=LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate(){
        updatedAt=LocalDateTime.now();
    }
}
