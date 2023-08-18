package com.eskgus.nammunity.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class DateTimeUtil {
    public static String formatDateTime(LocalDateTime dateTime) {
        return dateTime.format(DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm"));
    }

    public static String formatModifiedDate(LocalDateTime createdDate, LocalDateTime modifiedDate) {
        if (!createdDate.truncatedTo(ChronoUnit.MINUTES).isEqual(modifiedDate.truncatedTo(ChronoUnit.MINUTES))) {
            return formatDateTime(modifiedDate);
        }
        return null;
    }
}
