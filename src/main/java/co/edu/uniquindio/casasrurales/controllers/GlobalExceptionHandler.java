package co.edu.uniquindio.casasrurales.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * Manejador global de excepciones para la API REST.
 * Captura errores de validacion, logica de negocio y errores inesperados,
 * devolviendo respuestas JSON claras al cliente.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Captura errores de validacion de campos (@Valid).
     * Devuelve un mapa con el nombre del campo y el mensaje de error.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> erroresCampos = new HashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            erroresCampos.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        Map<String, Object> respuesta = Map.of(
                "error", "Error de validacion",
                "campos", erroresCampos
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(respuesta);
    }

    /**
     * Captura errores de logica de negocio (casa no disponible, datos invalidos, etc.).
     * Estos son lanzados explicitamente desde SistemaReservas.
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> handleIllegalState(IllegalStateException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", ex.getMessage()));
    }

    /**
     * Captura argumentos invalidos lanzados desde los servicios.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", ex.getMessage()));
    }

    /**
     * Captura NullPointerException, como cuando la casa no existe.
     */
    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<Map<String, String>> handleNullPointer(NullPointerException ex) {
        String mensaje = ex.getMessage() != null ? ex.getMessage() : "Recurso no encontrado";
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", mensaje));
    }

    /**
     * Captura cualquier otro error inesperado del servidor.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGenericException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error interno del servidor. Intentalo de nuevo mas tarde."));
    }
}