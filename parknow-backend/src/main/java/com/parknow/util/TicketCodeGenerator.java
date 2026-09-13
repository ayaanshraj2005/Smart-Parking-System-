package com.parknow.util;

import java.util.UUID;

public class TicketCodeGenerator {

    public static String generateTicketCode() {
        return "PN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
