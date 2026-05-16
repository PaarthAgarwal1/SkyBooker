package com.skybooker.PassengerService.util;

import java.security.SecureRandom;

public class TicketGenerator {

    private static final String PREFIX = "TKT-";
    private static final String CHARACTERS =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    private static final SecureRandom RANDOM =
            new SecureRandom();

    public static String generateTicket() {

        StringBuilder sb = new StringBuilder(PREFIX);

        for (int i = 0; i < 6; i++) {

            sb.append(
                    CHARACTERS.charAt(
                            RANDOM.nextInt(CHARACTERS.length())
                    )
            );
        }

        return sb.toString();
    }
}
