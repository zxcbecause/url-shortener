package io.github.zxcbecause.shortener.config;

import io.github.zxcbecause.shortener.link.InvalidUrlException;
import io.github.zxcbecause.shortener.link.LinkExceptions;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(LinkExceptions.NotFound.class)
    public ProblemDetail notFound(LinkExceptions.NotFound ex) {
        return problem(HttpStatus.NOT_FOUND, "Link not found", ex.getMessage());
    }

    @ExceptionHandler(LinkExceptions.Expired.class)
    public ProblemDetail expired(LinkExceptions.Expired ex) {
        return problem(HttpStatus.GONE, "Link expired", ex.getMessage());
    }

    @ExceptionHandler(LinkExceptions.AliasTaken.class)
    public ProblemDetail aliasTaken(LinkExceptions.AliasTaken ex) {
        return problem(HttpStatus.CONFLICT, "Alias taken", ex.getMessage());
    }

    @ExceptionHandler(InvalidUrlException.class)
    public ProblemDetail invalidUrl(InvalidUrlException ex) {
        return problem(HttpStatus.BAD_REQUEST, "Invalid URL", ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail validation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(e -> errors.putIfAbsent(e.getField(), e.getDefaultMessage()));
        ProblemDetail problem = problem(HttpStatus.BAD_REQUEST, "Validation error", "Validation failed");
        problem.setProperty("errors", errors);
        return problem;
    }

    private static ProblemDetail problem(HttpStatus status, String title, String detail) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(title);
        return problem;
    }
}
