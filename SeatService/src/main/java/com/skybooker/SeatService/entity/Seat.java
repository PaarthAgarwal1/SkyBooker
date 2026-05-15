package com.skybooker.SeatService.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@Table(name = "seats")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Seat {

    @Id
    @GeneratedValue
    private UUID seatId;

    private UUID flightId;

    private String seatNumber;

    @Enumerated(EnumType.STRING)
    private SeatClass seatClass;

    // ✅ FIX: avoid reserved keyword
    @Column(name = "row_num")
    private int rowNumber;

    private int columnNumber;

    private boolean isWindow;
    private boolean isAisle;
    private boolean hasExtraLegroom;

    // ✅ safer column name
    @Enumerated(EnumType.STRING)
    @Column(name = "seat_status")
    private SeatStatus status;

    private double priceMultiplier;

    private LocalDateTime holdExpiryTime;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Version
    private Integer version;

    @PrePersist
    void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        status = SeatStatus.AVAILABLE;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}