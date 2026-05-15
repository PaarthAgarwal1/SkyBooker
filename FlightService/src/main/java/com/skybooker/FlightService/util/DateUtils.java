package com.skybooker.FlightService.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

public final class DateUtils {
    private DateUtils(){

    }

    public static LocalDateTime getStartOfDay(LocalDate date){
        if(date==null){
            throw new IllegalArgumentException("Date cannot be null");
        }
        return date.atStartOfDay();
    }

    public static LocalDateTime getEndOfDay(LocalDate date){
        if(date == null){
            throw new IllegalArgumentException("Date cannot be null");
        }
        return date.atTime(23,59,59,999999999);
    }

    public static int calculateDurationMinutes(LocalDateTime departure,LocalDateTime arrival){
        if(departure==null || arrival==null){
            throw new IllegalArgumentException("DateTime cannot be null");
        }
        return (int) java.time.Duration.between(departure,arrival).toMinutes();
    }

    public static void validateFlightTimes(LocalDateTime departure,LocalDateTime arrival){
        if(departure==null || arrival==null){
            throw new IllegalArgumentException("DateTime cannot be null");
        }
        if(!arrival.isAfter(departure)){
            throw new IllegalArgumentException("Arrival time must be after departure time");
        }
    }
}
