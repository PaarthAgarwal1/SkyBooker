package com.skybooker.PassengerService.util;

import java.util.Random;

public class TicketGenerator {
    private static final String PREFIX="TKT-";
    private static final String CHARACTERS="ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    public static String generateTicket(){
        StringBuilder sb=new StringBuilder(PREFIX);
        Random random=new Random();
        for(int i=0;i<6;i++){
            sb.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }
        return sb.toString();
    }
}
