package ecommerce.advice

import ecommerce.exception.ErrorResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class GlobalExceptionAdvice {
    @ExceptionHandler(IllegalArgumentException::class)
    fun handleAuthenticationError(e: IllegalArgumentException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(ErrorResponse(error = "Unauthorized", message = e.message ?: "Authentication failed"))
    }
}
