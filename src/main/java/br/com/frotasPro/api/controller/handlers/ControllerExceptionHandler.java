package br.com.frotasPro.api.controller.handlers;

import br.com.frotasPro.api.excption.BusinessException;
import br.com.frotasPro.api.excption.CustomException;
import br.com.frotasPro.api.excption.ObjectNotFound;
import br.com.frotasPro.api.excption.ValidationError;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
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

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<CustomException> illegalArgument(IllegalArgumentException e, HttpServletRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        CustomException err = new CustomException(Instant.now(), status.value(), e.getMessage(), request.getRequestURI());
        return ResponseEntity.status(status).body(err);
    }

    // validação de @RequestParam / @PathVariable quando usar @Validated + constraints
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<CustomException> constraintViolation(ConstraintViolationException e, HttpServletRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        String msg = e.getConstraintViolations().stream()
                .findFirst()
                .map(v -> v.getMessage())
                .orElse("Parâmetros inválidos");
        CustomException err = new CustomException(Instant.now(), status.value(), msg, request.getRequestURI());
        return ResponseEntity.status(status).body(err);
    }

    // ex: data inválida em query param (inicio=abc) ou enum inválido
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<CustomException> typeMismatch(MethodArgumentTypeMismatchException e, HttpServletRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        String msg = "Parâmetro inválido: " + e.getName();
        CustomException err = new CustomException(Instant.now(), status.value(), msg, request.getRequestURI());
        return ResponseEntity.status(status).body(err);
    }

    // body inválido (datas/enums no JSON)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<CustomException> notReadable(HttpMessageNotReadableException e, HttpServletRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        String msg = "Corpo da requisição inválido (verifique datas e enums).";
        CustomException err = new CustomException(Instant.now(), status.value(), msg, request.getRequestURI());
        return ResponseEntity.status(status).body(err);
    }

    // erro de constraint no banco (unique, not-null etc.)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<CustomException> dataIntegrity(DataIntegrityViolationException e, HttpServletRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        String msg = "Violação de integridade: verifique campos obrigatórios e registros duplicados.";
        CustomException err = new CustomException(Instant.now(), status.value(), msg, request.getRequestURI());
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
