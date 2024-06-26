package com.eskgus.nammunity.util;

import com.eskgus.nammunity.domain.enums.ExceptionMessages;
import org.junit.jupiter.api.function.Executable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ServiceExceptionTestUtil {
    public static void assertIllegalArgumentException(Executable executable, ExceptionMessages exceptionMessage) {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, executable);

        assertEquals(exceptionMessage.getMessage(), exception.getMessage());
    }
}
