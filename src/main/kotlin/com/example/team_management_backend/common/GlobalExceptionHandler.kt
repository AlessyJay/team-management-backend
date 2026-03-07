package com.example.team_management_backend.common

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.time.Instant

data class ApiError(
    val status: Int,
    val error: String,
    val message: String,
    val timestamp: Instant = Instant.now()
)

class UnauthorizedException(message: String) : RuntimeException(message)

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException::class)
    fun handleNotFound(ex: NotFoundException): ResponseEntity<ApiError> =
        ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ApiError(404, "Not Found", ex.message ?: "Resource not found"))

    @ExceptionHandler(ForbiddenException::class)
    fun handleForbidden(ex: ForbiddenException): ResponseEntity<ApiError> =
        ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(ApiError(403, "Forbidden", ex.message ?: "Access denied"))

    @ExceptionHandler(UnauthorizedException::class)
    fun handleUnauthorized(ex: UnauthorizedException): ResponseEntity<ApiError> =
        ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(ApiError(401, "Unauthorized", ex.message ?: "Authentication required"))

    @ExceptionHandler(ConflictException::class)
    fun handleConflict(ex: ConflictException): ResponseEntity<ApiError> =
        ResponseEntity.status(HttpStatus.CONFLICT)
            .body(ApiError(409, "Conflict", ex.message ?: "Resource conflict"))

    @ExceptionHandler(BadRequestException::class)
    fun handleBadRequest(ex: BadRequestException): ResponseEntity<ApiError> =
        ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ApiError(400, "Bad Request", ex.message ?: "Invalid request"))

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(ex: MethodArgumentNotValidException): ResponseEntity<ApiError> {
        val message = ex.bindingResult.fieldErrors
            .joinToString(", ") { "${it.field}: ${it.defaultMessage}" }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ApiError(400, "Validation Failed", message))
    }

    @ExceptionHandler(Exception::class)
    fun handleGeneric(ex: Exception): ResponseEntity<ApiError> =
        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiError(500, "Internal Server Error", "An unexpected error occurred"))
}