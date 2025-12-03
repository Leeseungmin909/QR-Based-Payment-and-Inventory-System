package min.example.QRp.exception;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class ExceptionHandler {

    /**
     * DTO 유효성 검사 실패시 예외를 처리합니다.
     * @param ex 유효성 검사 실패 내용을 담고있는 예외 객체
     * @return 예외 객체가 가지고 있는 예외 메세지
     */
    @org.springframework.web.bind.annotation.ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return ResponseEntity.badRequest().body(errors);
    }

    /**
     * 서비스 계층 로직 위반시 예외를 처리합니다.
     * @param ex 로직 위배시 위배 내용을 담고있는 예외 객체
     * @return 예외 객체가 가지고 있는 예외 메세지
     */
    @org.springframework.web.bind.annotation.ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> handleIllegalStateException(IllegalStateException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getMessage());
        return ResponseEntity.badRequest().body(error);
    }

    /**
     * DB에 객체를 요청했는데 찾을 수 없을떄의 예외를 처리합니다
     * @param ex 찾을 수 없는 객체에 대한 내용을 담고있는 예외 객체
     * @return 404 에러 메세지
     */
    @org.springframework.web.bind.annotation.ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleEntityNotFoundException(EntityNotFoundException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    /**
     * DB 무결성 제약조건 위배시 발생되는 예외 처리를 합니다
     * @param ex DB 무결성 제약조건 위배에 대한 내용을 담고있는 예외 객체
     * @return 409 에러 메세지
     */
    @org.springframework.web.bind.annotation.ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        System.err.println("데이터 무결성 예외 발생: " + ex.getMessage());
        Map<String, String> error = new HashMap<>();
        error.put("error", "데이터 무결성 오류: 이 상품을 참조하는 구매 내역이 있어 삭제할 수 없습니다.");
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    /**
     * 위에서 찾지 못한 그 외 모든 예외를 처리를 합니다. 보통 서버 내부 오류이다.
     * @param ex 발생한 알 수 없는 예외 객체
     * @return 500 에러 메세지
     */
    @org.springframework.web.bind.annotation.ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGlobalException(Exception ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "서버 내부 오류가 발생했습니다: " + ex.getMessage());
        ex.printStackTrace(); // 서버 로그에 실제 오류 스택을 출력
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}