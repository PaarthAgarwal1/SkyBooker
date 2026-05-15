package com.skybooker.BookingService.scheduler;

import com.skybooker.BookingService.entity.Booking;
import com.skybooker.BookingService.entity.BookingStatus;
import com.skybooker.BookingService.feign.SeatClient;
import com.skybooker.BookingService.repository.BookingRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class BookingExpiryScheduler {

    private final BookingRepository bookingRepository;
    private final SeatClient seatClient;

    @Scheduled(fixedRate = 60000) // every 1 minute
    @Transactional
    public void expirePendingBookings() {

        List<Booking> expiredBookings =
                bookingRepository.findByStatusAndExpiryTimeBefore(
                        BookingStatus.PAYMENT_PENDING,
                        LocalDateTime.now()
                );

        if (expiredBookings.isEmpty()) {
            return;
        }

        log.info("Found {} expired bookings", expiredBookings.size());

        for (Booking booking : expiredBookings) {

            try {

                // release all seats
                booking.getSeatIds().forEach(seatClient::releaseSeat);

                // cancel booking
                booking.setStatus(BookingStatus.CANCELLED);

                bookingRepository.save(booking);

                log.info("Booking expired: {}", booking.getBookingId());

            } catch (Exception e) {

                log.error(
                        "Failed to expire booking {}",
                        booking.getBookingId(),
                        e
                );
            }
        }
    }
}