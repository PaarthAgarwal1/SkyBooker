package com.skybooker.BookingService.util;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class PnrGenerator {

    private static final String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int PNR_LENGTH = 6;

    private final SecureRandom random = new SecureRandom();

    public String generate() {
        StringBuilder pnr = new StringBuilder(PNR_LENGTH);

        for (int i = 0; i < PNR_LENGTH; i++) {
            int index = random.nextInt(CHARS.length());
            pnr.append(CHARS.charAt(index));
        }

        return pnr.toString();
    }
}