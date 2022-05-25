package dev.kyma.samples.easyfranchise.s4entities;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

public class S4Util {

    /**
     * Converts the S4 json Dateformat to LocalDate. Returns null if s4Date is invalid string. 
     * date example: "/Date(1589241600000)/".
     * 
     * @param s4Date String in format 
     * @return the concerted LocalDate
     */
    public static LocalDate getDateFromS4Date(String s4Date) {
        if (s4Date == null || s4Date.length() != 21 || !s4Date.startsWith("/Date(") || !s4Date.endsWith(")/")) {
            return null;
        }
        String longDateString = s4Date.substring(6, 19);
        return Instant.ofEpochMilli(Long.parseLong(longDateString)).atZone(ZoneOffset.UTC).toLocalDate();
    }
}
