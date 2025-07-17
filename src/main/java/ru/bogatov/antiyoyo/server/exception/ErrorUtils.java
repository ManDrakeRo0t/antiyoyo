package ru.bogatov.antiyoyo.server.exception;

import lombok.experimental.UtilityClass;
import org.springframework.http.HttpStatus;

@UtilityClass
public class ErrorUtils {

    public static void failWithBadRequest(String message) {
        throw new ApplicationException(message, ErrorResponse.from(message, HttpStatus.BAD_REQUEST));
    }

    public static void failWithInternalError(String message) {
        throw new ApplicationException(message, ErrorResponse.from(message, HttpStatus.INTERNAL_SERVER_ERROR));
    }

}
