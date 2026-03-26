package com.thehorselegend.summs.shared.time;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

public final class SummsTime {

    private static final ZoneId ZONE_ID = ZoneOffset.ofHours(-4);

    private SummsTime() {
    }

    public static ZoneId zoneId() {
        return ZONE_ID;
    }

    public static LocalDateTime now() {
        return LocalDateTime.now(ZONE_ID);
    }
}
