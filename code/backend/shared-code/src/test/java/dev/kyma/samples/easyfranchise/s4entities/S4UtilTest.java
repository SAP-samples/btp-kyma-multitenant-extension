package dev.kyma.samples.easyfranchise.s4entities;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

class S4UtilTest {

    @Test
    void testGetDateFromS4Date() {
        assertEquals(LocalDate.of(2020, 5, 12), S4Util.getDateFromS4Date("/Date(1589241600000)/"));
    }

}
