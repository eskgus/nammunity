package com.eskgus.nammunity.util;

import java.time.LocalDateTime;
import java.time.Period;
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

    public static String convertPeriodToString(Period period) {
        if (period.get(ChronoUnit.YEARS) >= 100) {
            return "영구";
        } else if (period.get(ChronoUnit.YEARS) > 0) {
            return period.get(ChronoUnit.YEARS) + "년";
        } else if (period.get(ChronoUnit.MONTHS) > 0) {
            return period.get(ChronoUnit.MONTHS) + "개월";
        } else if (period.get(ChronoUnit.DAYS) / 7 > 0) {
            return period.get(ChronoUnit.DAYS) / 7 + "주";
        }
        return "0일";
    }
}
