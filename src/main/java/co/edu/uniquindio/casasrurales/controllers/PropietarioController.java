package co.edu.uniquindio.casasrurales.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.edu.uniquindio.casasrurales.dto.CasaRuralPropietarioDTO;
import co.edu.uniquindio.casasrurales.services.PropietarioService;

/**
 * API REST para operaciones de propietarios.
 * Permite gestionar casas, dar de baja propiedades, etc.
 * Solo propietarios autenticados pueden acceder.
 */
@RestController
@RequestMapping("/api/propietario")
public class PropietarioController {

    private final PropietarioService propietarioService;

    public PropietarioController(PropietarioService propietarioService) {
        this.propietarioService = propietarioService;
    }

    /**
     * Obtiene todas las casas del propietario autenticado.
     * 
     * @param authentication información del usuario autenticado
     * @return lista de casas del propietario
     */
    @GetMapping("/mis-casas")
    public ResponseEntity<?> obtenerMisCasas(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .build();
        }

        try {
            int idPropietario = Integer.parseInt(authentication.getName());
            List<CasaRuralPropietarioDTO> casas = propietarioService.obtenerCasasPropietario(idPropietario);
            return ResponseEntity.ok(casas);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", ex.getMessage()));
        }
    }

    /**
     * Da de baja una casa rural específica.
     * Requiere autenticación y que el propietario sea el dueño.
     * No puede haber reservas activas.
     * 
     * @param codigoCasa código de la casa a dar de baja
     * @param authentication información del propietario autenticado
     * @return respuesta con confirmación o error
     */
    @DeleteMapping("/{codigoCasa}")
    public ResponseEntity<Map<String, String>> darDeBajaCasa(
            @PathVariable int codigoCasa,
            Authentication authentication) {
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Debe estar autenticado"));
        }

        try {
            int idPropietario = Integer.parseInt(authentication.getName());
            String mensaje = propietarioService.darDeBajaCasa(codigoCasa, idPropietario);
            
            return ResponseEntity.ok(Map.of("mensaje", mensaje));
        } catch (IllegalArgumentException ex) {
            // Errores de validación (propietario no encontrado, casa no encontrada, no es propietario)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", ex.getMessage()));
        } catch (IllegalStateException ex) {
            // Error: hay reservas activas
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", ex.getMessage()));
        }
    }

    /**
     * Reactiva una casa que fue dada de baja.
     * 
     * @param codigoCasa código de la casa a reactivar
     * @param authentication información del propietario autenticado
     * @return respuesta con confirmación o error
     */
    @PostMapping("/{codigoCasa}/reactivar")
    public ResponseEntity<Map<String, String>> reactivarCasa(
            @PathVariable int codigoCasa,
            Authentication authentication) {
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Debe estar autenticado"));
        }

        try {
            int idPropietario = Integer.parseInt(authentication.getName());
            String mensaje = propietarioService.reactivarCasa(codigoCasa, idPropietario);
            
            return ResponseEntity.ok(Map.of("mensaje", mensaje));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", ex.getMessage()));
        }
    }
}
