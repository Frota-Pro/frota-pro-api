package br.com.frotasPro.api.controller.handlers;

import br.com.frotasPro.api.excption.BusinessException;
import br.com.frotasPro.api.excption.CustomException;
import br.com.frotasPro.api.excption.ObjectNotFound;
import br.com.frotasPro.api.excption.ValidationError;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.Instant;

@ControllerAdvice
public class ControllerExceptionHandler {

    @ExceptionHandler(ObjectNotFound.class)
    public ResponseEntity<CustomException> objectNotFound(ObjectNotFound e, HttpServletRequest request) {
        HttpStatus status = HttpStatus.NOT_FOUND;
        CustomException err = new CustomException(Instant.now(), status.value(), e.getMessage(), request.getRequestURI());
        return ResponseEntity.status(status).body(err);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<CustomException> handleBusinessException(BusinessException e, HttpServletRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        CustomException err = new CustomException(Instant.now(), status.value(), e.getMessage(), request.getRequestURI());
        return ResponseEntity.status(status).body(err);
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationError> validationError(MethodArgumentNotValidException e, HttpServletRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        ValidationError err = new ValidationError(
                Instant.now(),
                status.value(),
                "Erro de validação",
                request.getRequestURI()
        );
        e.getBindingResult().getFieldErrors()
                .forEach(fieldError -> err.addError(fieldError.getField(), fieldError.getDefaultMessage()));
        return ResponseEntity.status(status).body(err);
    }

}
