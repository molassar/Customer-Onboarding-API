package com.example.onboarding.api.exceptionhandling;

import com.example.onboarding.domain.exception.AccountNotFoundException;
import com.example.onboarding.domain.exception.IbanAlreadyExistsException;
import com.example.onboarding.domain.exception.InvalidCredentialsException;
import com.example.onboarding.domain.exception.UnauthenticatedException;
import com.example.onboarding.domain.exception.UnderageCustomerException;
import com.example.onboarding.domain.exception.UnsupportedCountryException;
import com.example.onboarding.domain.exception.UsernameAlreadyExistsException;
import com.example.onboarding.infrastructure.exception.DatabaseRateLimitExceededException;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

  @ExceptionHandler(UnauthenticatedException.class)
  ResponseEntity<ProblemDetail> handleUnauthenticated(UnauthenticatedException ex) {
    var problem = ProblemDetail.forStatus(HttpStatus.UNAUTHORIZED);
    problem.setTitle("Authentication required");
    problem.setDetail(ex.getMessage());

    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(problem);
  }

  @ExceptionHandler(AccountNotFoundException.class)
  ResponseEntity<ProblemDetail> handleAccountNotFound(AccountNotFoundException ex) {
    var problem = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
    problem.setTitle("Account not found");
    problem.setDetail(ex.getMessage());

    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problem);
  }

  @ExceptionHandler(InvalidCredentialsException.class)
  ResponseEntity<ProblemDetail> handleInvalidCredentials(InvalidCredentialsException ex) {
    var problem = ProblemDetail.forStatus(HttpStatus.UNAUTHORIZED);
    problem.setTitle("Authentication failed");
    problem.setDetail(ex.getMessage());

    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(problem);
  }

  @ExceptionHandler(DatabaseRateLimitExceededException.class)
  ResponseEntity<ProblemDetail> handleDatabaseRateLimitExceeded(
      DatabaseRateLimitExceededException ex) {
    var problem = ProblemDetail.forStatus(HttpStatus.TOO_MANY_REQUESTS);
    problem.setTitle("Too many requests");
    problem.setDetail(ex.getMessage());

    return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(problem);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  ResponseEntity<ProblemDetail> handleValidation(MethodArgumentNotValidException ex) {
    var problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);

    problem.setTitle("Validation failed");
    problem.setDetail("One or more request fields are invalid");

    var fieldErrors =
        ex.getBindingResult().getFieldErrors().stream()
            .collect(
                Collectors.toMap(
                    FieldError::getField, ApiExceptionHandler::message, (first, second) -> first));

    problem.setProperty("errors", fieldErrors);

    return ResponseEntity.badRequest().body(problem);
  }

  @ExceptionHandler(IllegalArgumentException.class)
  ResponseEntity<ProblemDetail> handleIllegalArgument(IllegalArgumentException ex) {
    var problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
    problem.setTitle("Invalid request");
    problem.setDetail(ex.getMessage());

    return ResponseEntity.badRequest().body(problem);
  }

  @ExceptionHandler(UsernameAlreadyExistsException.class)
  ResponseEntity<ProblemDetail> handleUsernameAlreadyExists(UsernameAlreadyExistsException ex) {
    var problem = ProblemDetail.forStatus(HttpStatus.CONFLICT);
    problem.setTitle("Username already exists");
    problem.setDetail(ex.getMessage());

    return ResponseEntity.status(HttpStatus.CONFLICT).body(problem);
  }

  @ExceptionHandler(IbanAlreadyExistsException.class)
  ResponseEntity<ProblemDetail> handleIbanAlreadyExists(IbanAlreadyExistsException ex) {
    var problem = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
    problem.setTitle("IBAN generation failed");
    problem.setDetail("Could not generate a unique IBAN");

    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problem);
  }

  @ExceptionHandler(UnsupportedCountryException.class)
  ResponseEntity<ProblemDetail> handleUnsupportedCountry(UnsupportedCountryException ex) {
    var problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
    problem.setTitle("Unsupported country");
    problem.setDetail(ex.getMessage());

    return ResponseEntity.badRequest().body(problem);
  }

  @ExceptionHandler(UnderageCustomerException.class)
  ResponseEntity<ProblemDetail> handleUnderageCustomer(UnderageCustomerException ex) {
    var problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
    problem.setTitle("Customer is underage");
    problem.setDetail(ex.getMessage());

    return ResponseEntity.badRequest().body(problem);
  }

  private static String message(FieldError error) {
    return Objects.requireNonNullElse(error.getDefaultMessage(), "Invalid value");
  }
}
