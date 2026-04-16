package co.edu.uniquindio.casasrurales.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import co.edu.uniquindio.casasrurales.dto.RegistroClienteForm;
import co.edu.uniquindio.casasrurales.dto.RegistroPropietarioForm;
import co.edu.uniquindio.casasrurales.services.AutenticacionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
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

    /**
     * Autentica un propietario con nombre de cuenta y contraseña.
     * 
     * @param loginData mapa con nombreCuenta y contrasena
     * @return información del propietario autenticado
     */
    @PostMapping("/auth/login/propietario")
    public ResponseEntity<Map<String, Object>> loginPropietario(
            @RequestBody Map<String, String> loginData,
            HttpServletRequest request) {
        try {
            String nombreCuenta = loginData.get("nombreCuenta");
            String contrasena = loginData.get("contrasena");

            if (nombreCuenta == null || contrasena == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Nombre de cuenta y contraseña son requeridos"));
            }

            int idPropietario = autenticacionService.autenticarPropietario(nombreCuenta, contrasena);
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    String.valueOf(idPropietario),
                    null,
                    List.of(new SimpleGrantedAuthority("ROLE_PROPIETARIO")));
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);

            HttpSession session = request.getSession(true);
            session.setAttribute("SPRING_SECURITY_CONTEXT", context);

            return ResponseEntity.ok(Map.of(
                    "idUsuario", idPropietario,
                    "nombreCuenta", nombreCuenta,
                    "rol", "PROPIETARIO"
            ));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", ex.getMessage()));
        }
    }

    @PostMapping("/auth/logout")
    public ResponseEntity<Map<String, String>> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok()
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .body(Map.of("mensaje", "Sesion cerrada exitosamente"));
    }

    /**
     * Autentica un cliente con email y contraseña.
     * 
     * @param loginData mapa con email y contrasena
     * @return información del cliente autenticado
     */
    @PostMapping("/auth/login/cliente")
    public ResponseEntity<Map<String, Object>> loginCliente(
            @RequestBody Map<String, String> loginData,
            HttpServletRequest request) {
        try {
            String email = loginData.get("email");
            String contrasena = loginData.get("contrasena");

            if (email == null || contrasena == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Usuario y contraseña son requeridos"));
            }

            int idCliente = autenticacionService.autenticarCliente(email, contrasena);
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    String.valueOf(idCliente),
                    null,
                    List.of(new SimpleGrantedAuthority("ROLE_CLIENTE")));
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);

            HttpSession session = request.getSession(true);
            session.setAttribute("SPRING_SECURITY_CONTEXT", context);

            // Crear autenticación y sesión similar al login de propietario
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                String.valueOf(idCliente),
                null,
                List.of(new SimpleGrantedAuthority("ROLE_CLIENTE")));
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);

            // Obtener/crear sesión y almacenar el contexto de seguridad
            HttpSession session = ((jakarta.servlet.http.HttpServletRequest) ((org.springframework.web.context.request.ServletRequestAttributes) org.springframework.web.context.request.RequestContextHolder.currentRequestAttributes()).getRequest()).getSession(true);
            session.setAttribute("SPRING_SECURITY_CONTEXT", context);

            return ResponseEntity.ok(Map.of(
                "idUsuario", idCliente,
                "email", email,
                "rol", "CLIENTE"
            ));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", ex.getMessage()));
        }
    }
}
