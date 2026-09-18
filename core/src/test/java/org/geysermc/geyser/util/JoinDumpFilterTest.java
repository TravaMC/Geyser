package org.geysermc.geyser.util;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JoinDumpFilterTest {

    @Test
    void emptyIsOff() {
        assertFalse(JoinDumpFilter.matches(List.of(), 582, "1.19.83"));
        assertFalse(JoinDumpFilter.matches(null, 582, "1.19.83"));
    }

    @Test
    void starDumpsEveryProtocol() {
        assertTrue(JoinDumpFilter.matches(List.of("*"), 582, "1.19.83"));
        assertTrue(JoinDumpFilter.matches(List.of("all"), 2193, "26.51"));
    }

    @Test
    void protocolNumberAndVersionString() {
        assertTrue(JoinDumpFilter.matches(List.of("582"), 582, null));
        assertTrue(JoinDumpFilter.matches(List.of("1.19.83"), 582, null));
        assertTrue(JoinDumpFilter.matches(List.of("1.19.80", "748"), 582, "1.19.83"));
        assertFalse(JoinDumpFilter.matches(List.of("748"), 582, "1.19.83"));
        assertFalse(JoinDumpFilter.matches(List.of("1.21.40"), 582, "1.19.83"));
    }
}
