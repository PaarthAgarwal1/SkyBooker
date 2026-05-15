package com.skybooker.BookingService.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "booking")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID bookingId;

    @Column(nullable = false, updatable = false)
    private UUID userId;

    @Column(nullable = false, updatable = false)
    private UUID flightId;

    @Column(unique = true, nullable = false, length = 6)
    private String pnrCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TripType tripType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status;

    // ✅ IMPORTANT: store seats booked
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "booking_seats",
            joinColumns = @JoinColumn(name = "booking_id")
    )
    @Column(name = "seat_id", nullable = false)
    private List<UUID> seatIds;

    // 💰 Financial fields (use precision-safe types)
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalFare;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal baseFare;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal taxes;

    // 🎒 Add-ons
    @Column(length = 20)
    private String mealPreference;

    private int luggageKg;

    // 📞 Contact info
    @Column(nullable = false)
    private String contactEmail;

    @Column(nullable = false)
    private String contactPhone;

    @Column(nullable = false, updatable = false)
    private LocalDateTime bookedAt;

    private UUID paymentId;

    @Column(unique = true, nullable = false)
    private String idempotencyKey;

    @Column(nullable = false)
    private LocalDateTime expiryTime;

    // 🔐 Optional but HIGHLY recommended (for concurrency)
    @Version
    private Integer version;

    // ⏱ Auto set booking time
    @PrePersist
    public void prePersist() {
        this.bookedAt = LocalDateTime.now();
    }
}