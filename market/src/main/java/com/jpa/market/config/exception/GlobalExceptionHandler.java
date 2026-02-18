    package com.jpa.market.config.exception;


import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

//@ControllerAdvice : 모든 컨트롤러의 예외를 가로채 처리하는 클래스임을 명시
//@ControllerAdvice + @ResponseBody = @RestControllerAdvice
@RestControllerAdvice   //JSON 형태 응답을 자동으로 처리할 때 사용
public class GlobalExceptionHandler {

    //@ExceptionHandler : 특정 예외 클래스를 처리하는 메서드 지정
    //MethodArgumentNotValidException : @Valid 유효성 검사 실패 예외
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationException(MethodArgumentNotValidException e) {

        Map<String, String> errors = new HashMap<>();

        for (FieldError error : e.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }

        return ResponseEntity
                .badRequest()
                .body(errors);
    }

    @ExceptionHandler({IllegalStateException.class, IllegalArgumentException.class})
    public ResponseEntity<?> handleIllegalStateException(RuntimeException e) {
        // IllegalStateException과 IllegalArgumentException 모두
        // RuntimeException의 자식들이라 매개변수를 RuntimeException으로 받을 수 있습니다.
        return ResponseEntity
                .badRequest()
                .body(Map.of("message", e.getMessage()));
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<?> handleEntityNotFoundException(EntityNotFoundException e) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                //.body(Map.of("message", "해당 정보를 찾을 수 없습니다."));
                .body(Map.of("message", e.getMessage()));
    }

    @ExceptionHandler(OutOfStockException.class)
    public ResponseEntity<?> handleOutOfStockException(OutOfStockException e) {
        return ResponseEntity
                .badRequest()   //요청 값 오류
                .body(Map.of("message", e.getMessage()));
    }

    // 인증은 되었으나 권한이 없을 때 발생하는 예외
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<?> handleAccessDeniedException(AccessDeniedException e) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN) // 403 에러
                .body(Map.of("message", e.getMessage()));
    }

    // GlobalExceptionHandler.java에 추가
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleAllException(Exception e) {
        e.printStackTrace();
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "상품 등록 중 에러가 발생하였습니다."));
    }
}

















