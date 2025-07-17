package ru.bogatov.antiyoyo.server.exception;


import lombok.Data;
import lombok.Getter;


@Getter
public class ApplicationException extends RuntimeException {

    public final ErrorResponse errorResponse;

    public ApplicationException(String message, ErrorResponse errorResponse) {
        super(message);
        this.errorResponse = errorResponse;
    }

    public ApplicationException(ErrorResponse errorResponse) {
        super(errorResponse.getMessage());
        this.errorResponse = errorResponse;
    }

    public ApplicationException() {
        super("Not qualified backend error");
        this.errorResponse = ErrorResponse.BASE_ERROR;
    }

    public ApplicationException(Throwable throwable) {
        super(throwable.getMessage());
        this.errorResponse = ErrorResponse.BASE_ERROR;
    }

}
