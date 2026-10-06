package util;

import java.util.UUID;

public class PNRGenerator {

    private PNRGenerator() {
    }

    public static String generatePNR() {
        return UUID.randomUUID().toString().replace("-", "")
                .substring(0, 10).toUpperCase();
    }
}