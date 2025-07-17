package ru.bogatov.antiyoyo.server.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.HttpStatus;

@Data
@AllArgsConstructor
public class ErrorResponse {

    private HttpStatus status;
    private String message;
    private String error;

    public static ErrorResponse from(String message, String error, HttpStatus status) {
        return new ErrorResponse(status, message, error);
    }

    public static ErrorResponse from(String message, HttpStatus status) {
        return new ErrorResponse(status, message, "");
    }

    public static ErrorResponse from(String message) {
        return new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, message, "");
    }

    public static final ErrorResponse BASE_ERROR = ErrorResponse.from("Not qualified backend error", HttpStatus.INTERNAL_SERVER_ERROR);

}
