package co.edu.uniquindio.casasrurales.controllers;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import co.edu.uniquindio.casasrurales.dto.RegistroClienteForm;
import co.edu.uniquindio.casasrurales.dto.RegistroPropietarioForm;
import co.edu.uniquindio.casasrurales.services.AutenticacionService;
import jakarta.validation.Valid;

/**
 * API REST encargada del flujo de autenticación y registro.
 * Proporciona endpoints para login, registro de propietarios y clientes.
 */
@RestController
public class AuthController {

    private final AutenticacionService autenticacionService;

    public AuthController(AutenticacionService autenticacionService) {
        this.autenticacionService = autenticacionService;
    }

    /**
     * Registra un nuevo propietario en el sistema.
     * 
     * @param registroPropietarioForm datos del propietario
     * @return respuesta con mensaje de éxito o error
     */
    @PostMapping("/auth/registro/propietario")
    public ResponseEntity<Map<String, String>> registrarPropietario(
            @Valid @RequestBody RegistroPropietarioForm registroPropietarioForm) {
        try {
            autenticacionService.registrarPropietario(registroPropietarioForm);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("mensaje", "Propietario registrado exitosamente"));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", ex.getMessage()));
        }
    }

    /**
     * Registra un nuevo cliente en el sistema.
     * 
     * @param registroClienteForm datos del cliente
     * @return respuesta con mensaje de éxito o error
     */
    @PostMapping("/auth/registro/cliente")
    public ResponseEntity<Map<String, String>> registrarCliente(
            @Valid @RequestBody RegistroClienteForm registroClienteForm) {
        try {
            autenticacionService.registrarCliente(registroClienteForm);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("mensaje", "Cliente registrado exitosamente"));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", ex.getMessage()));
        }
    }

    /**
     * Obtiene información del usuario autenticado.
     * 
     * @param authentication información del usuario actual
     * @return información del usuario autenticado
     */
    @GetMapping("/auth/me")
    public ResponseEntity<Map<String, Object>> getUsuarioActual(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "No autenticado"));
        }

        boolean esPropietario = authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_PROPIETARIO"));

        Map<String, Object> respuesta = Map.of(
                "usuario", authentication.getName(),
                "rol", esPropietario ? "PROPIETARIO" : "CLIENTE",
                "autenticado", true
        );
        
        return ResponseEntity.ok(respuesta);
    }
}
