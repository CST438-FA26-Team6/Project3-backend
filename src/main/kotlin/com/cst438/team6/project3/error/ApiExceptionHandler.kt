package com.cst438.team6.project3.error

import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.server.ResponseStatusException
import java.net.URI

@RestControllerAdvice
class ApiExceptionHandler {
    @ExceptionHandler(ResponseStatusException::class)
    fun handleResponseStatusException(
        exception: ResponseStatusException,
        request: HttpServletRequest
    ): ResponseEntity<ProblemDetail> {
        val problem = ProblemDetail.forStatusAndDetail(
            exception.statusCode,
            exception.reason ?: "The request could not be processed."
        )

        problem.title = HttpStatus
            .resolve(exception.statusCode.value())
            ?.reasonPhrase
            ?: "Request Failed"
        problem.instance = URI.create(request.requestURI)

        return ResponseEntity
            .status(exception.statusCode)
            .headers(exception.headers)
            .body(problem)
    }
}
