package co.edu.uniquindio.casasrurales.controllers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import co.edu.uniquindio.casasrurales.dto.CasaRuralFormDTO;
import co.edu.uniquindio.casasrurales.dto.CasaRuralPropietarioDTO;
import co.edu.uniquindio.casasrurales.dto.PagoRegistroDTO;
import co.edu.uniquindio.casasrurales.dto.RegistroCasaForm;
import co.edu.uniquindio.casasrurales.dto.ReservaPropietarioDTO;
import co.edu.uniquindio.casasrurales.services.PropietarioService;
import jakarta.validation.Valid;

/**
 * API REST para operaciones de propietarios.
 * Permite gestionar casas, dar de baja propiedades, etc.
 * Solo propietarios autenticados pueden acceder.
 */
@RestController
@RequestMapping("/api/propietario")
public class PropietarioController {

    private final PropietarioService propietarioService;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    public PropietarioController(PropietarioService propietarioService) {
        this.propietarioService = propietarioService;
    }

    @PostMapping("/fotos")
    public ResponseEntity<?> subirFotos(
            @RequestParam("fotos") List<MultipartFile> fotos,
            Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Debe estar autenticado"));
        }

        try {
            if (fotos == null || fotos.isEmpty() || fotos.stream().allMatch(MultipartFile::isEmpty)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Debes seleccionar al menos una foto"));
            }

            Path carpetaFotos = Paths.get(uploadDir, "fotos").toAbsolutePath().normalize();
            Files.createDirectories(carpetaFotos);

            List<String> urls = fotos.stream()
                    .filter(foto -> !foto.isEmpty())
                    .map(foto -> guardarFoto(foto, carpetaFotos))
                    .collect(Collectors.toList());

            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("urls", urls));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
        } catch (IOException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "No fue posible guardar las fotos"));
        }
    }

    private String guardarFoto(MultipartFile foto, Path carpetaFotos) {
        String nombreOriginal = StringUtils.cleanPath(foto.getOriginalFilename() != null ? foto.getOriginalFilename() : "");
        String extension = obtenerExtension(nombreOriginal);
        validarFoto(foto, extension);

        String nombreArchivo = UUID.randomUUID() + extension;
        Path destino = carpetaFotos.resolve(nombreArchivo).normalize();

        try {
            foto.transferTo(destino);
        } catch (IOException ex) {
            throw new IllegalArgumentException("No fue posible guardar la foto " + nombreOriginal);
        }

        return "/uploads/fotos/" + nombreArchivo;
    }

    private String obtenerExtension(String nombreArchivo) {
        int posicionPunto = nombreArchivo.lastIndexOf('.');
        if (posicionPunto < 0) {
            return "";
        }
        return nombreArchivo.substring(posicionPunto).toLowerCase();
    }

    private void validarFoto(MultipartFile foto, String extension) {
        String contentType = foto.getContentType();
        boolean tipoPermitido = "image/jpeg".equals(contentType)
                || "image/png".equals(contentType)
                || "image/webp".equals(contentType);
        boolean extensionPermitida = extension.equals(".jpg")
                || extension.equals(".jpeg")
                || extension.equals(".png")
                || extension.equals(".webp");

        if (!tipoPermitido || !extensionPermitida) {
            throw new IllegalArgumentException("Las fotos deben estar en formato JPG, PNG o WEBP");
        }
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
     * Obtiene el detalle de una casa puntual del propietario autenticado.
     */
    @GetMapping("/mis-casas/{codigoCasa}")
    public ResponseEntity<?> obtenerCasa(
            @PathVariable int codigoCasa,
            Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .build();
        }

        try {
            int idPropietario = Integer.parseInt(authentication.getName());
            CasaRuralPropietarioDTO casa = propietarioService.obtenerCasaPropietario(codigoCasa, idPropietario);
            return ResponseEntity.ok(casa);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", ex.getMessage()));
        }
    }

    /**
     * Registra una nueva casa del propietario autenticado.
     */
    @PostMapping("/mis-casas")
    public ResponseEntity<Map<String, String>> crearCasa(
            @Valid @RequestBody CasaRuralFormDTO form,
            Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Debe estar autenticado"));
        }

        try {
            int idPropietario = Integer.parseInt(authentication.getName());
            String mensaje = propietarioService.crearCasa(form, idPropietario);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("mensaje", mensaje));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", ex.getMessage()));
        }
    }

    /**
     * Edita una casa del propietario autenticado.
     */
    @PutMapping("/mis-casas/{codigoCasa}")
    public ResponseEntity<Map<String, String>> editarCasa(
            @PathVariable int codigoCasa,
            @Valid @RequestBody CasaRuralFormDTO form,
            Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Debe estar autenticado"));
        }

        try {
            int idPropietario = Integer.parseInt(authentication.getName());
            String mensaje = propietarioService.editarCasa(codigoCasa, form, idPropietario);
            return ResponseEntity.ok(Map.of("mensaje", mensaje));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
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

    /**
     * Crea una nueva casa para el propietario autenticado.
     */
    @PostMapping("/casas")
    public ResponseEntity<?> crearCasa(@Valid @RequestBody RegistroCasaForm form, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Debe estar autenticado"));
        }

        try {
            int idPropietario = Integer.parseInt(authentication.getName());
            var dto = propietarioService.crearCasa(form, idPropietario);
            return ResponseEntity.status(HttpStatus.CREATED).body(dto);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
        }
    }

    /**
     * Actualiza información básica de una casa del propietario.
     */
    @PutMapping("/{codigoCasa}")
    public ResponseEntity<?> actualizarCasa(@PathVariable int codigoCasa,
                                           @Valid @RequestBody RegistroCasaForm form,
                                           Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Debe estar autenticado"));
        }

        try {
            int idPropietario = Integer.parseInt(authentication.getName());
            var dto = propietarioService.actualizarCasa(codigoCasa, form, idPropietario);
            return ResponseEntity.ok(dto);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
        }
    }

    /**
     * HU-05: Obtener paquetes de alquiler de una casa.
     */
    @GetMapping("/mis-casas/{codigoCasa}/paquetes")
    public ResponseEntity<?> obtenerPaquetesCasa(
            @PathVariable int codigoCasa,
            Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Debe estar autenticado"));
        }
        try {
            int idPropietario = Integer.parseInt(authentication.getName());
            List<co.edu.uniquindio.casasrurales.dto.PaqueteAlquilerDTO> paquetes = propietarioService.obtenerPaquetesCasa(codigoCasa, idPropietario);
            return ResponseEntity.ok(paquetes);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
        }
    }

    /**
     * HU-05: Crear paquete de alquiler.
     */
    @PostMapping("/mis-casas/{codigoCasa}/paquetes")
    public ResponseEntity<?> crearPaquete(
            @PathVariable int codigoCasa,
            @Valid @RequestBody co.edu.uniquindio.casasrurales.dto.PaqueteAlquilerDTO dto,
            Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Debe estar autenticado"));
        }
        try {
            int idPropietario = Integer.parseInt(authentication.getName());
            var paquete = propietarioService.crearPaquete(codigoCasa, idPropietario, dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(paquete);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
        }
    }

    /**
     * HU-05: Modificar paquete de alquiler.
     */
    @PutMapping("/mis-casas/{codigoCasa}/paquetes/{idPaquete}")
    public ResponseEntity<?> modificarPaquete(
            @PathVariable int codigoCasa,
            @PathVariable int idPaquete,
            @Valid @RequestBody co.edu.uniquindio.casasrurales.dto.PaqueteAlquilerDTO dto,
            Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Debe estar autenticado"));
        }
        try {
            int idPropietario = Integer.parseInt(authentication.getName());
            var paquete = propietarioService.modificarPaquete(codigoCasa, idPropietario, idPaquete, dto);
            return ResponseEntity.ok(paquete);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", ex.getMessage()));
        }
    }

    /**
     * HU-05: Eliminar paquete de alquiler.
     */
    @DeleteMapping("/mis-casas/{codigoCasa}/paquetes/{idPaquete}")
    public ResponseEntity<?> eliminarPaquete(
            @PathVariable int codigoCasa,
            @PathVariable int idPaquete,
            Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Debe estar autenticado"));
        }
        try {
            int idPropietario = Integer.parseInt(authentication.getName());
            propietarioService.eliminarPaquete(codigoCasa, idPropietario, idPaquete);
            return ResponseEntity.ok(Map.of("mensaje", "Paquete eliminado exitosamente"));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", ex.getMessage()));
        }
    }

    /**
     * RN43: Divide un paquete de alquiler en varios paquetes mas pequenos.
     */
    @PostMapping("/mis-casas/{codigoCasa}/paquetes/{idPaquete}/dividir")
    public ResponseEntity<?> dividirPaquete(
            @PathVariable int codigoCasa,
            @PathVariable int idPaquete,
            @Valid @RequestBody List<co.edu.uniquindio.casasrurales.dto.PaqueteAlquilerDTO> nuevosPaquetes,
            Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Debe estar autenticado"));
        }
        try {
            int idPropietario = Integer.parseInt(authentication.getName());
            var paquetes = propietarioService.dividirPaquete(codigoCasa, idPropietario, idPaquete, nuevosPaquetes);
            return ResponseEntity.status(HttpStatus.CREATED).body(paquetes);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", ex.getMessage()));
        }
    }

    @GetMapping("/reservas")
    public ResponseEntity<?> obtenerReservas(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Debe estar autenticado"));
        }
        try {
            int idPropietario = Integer.parseInt(authentication.getName());
            List<ReservaPropietarioDTO> reservas = propietarioService.obtenerReservasPropietario(idPropietario);
            return ResponseEntity.ok(reservas);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
        }
    }

    @GetMapping("/reservas/vencidas")
    public ResponseEntity<?> obtenerReservasVencidas(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Debe estar autenticado"));
        }
        try {
            int idPropietario = Integer.parseInt(authentication.getName());
            List<ReservaPropietarioDTO> reservas = propietarioService.obtenerReservasVencidas(idPropietario);
            return ResponseEntity.ok(reservas);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
        }
    }

    @PostMapping("/reservas/{numeroReserva}/pago")
    public ResponseEntity<?> registrarPago(
            @PathVariable int numeroReserva,
            @Valid @RequestBody PagoRegistroDTO dto,
            Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Debe estar autenticado"));
        }
        try {
            int idPropietario = Integer.parseInt(authentication.getName());
            ReservaPropietarioDTO reserva = propietarioService.registrarPagoReserva(numeroReserva, idPropietario, dto);
            List<ReservaPropietarioDTO> vencidas = propietarioService.obtenerReservasVencidas(idPropietario);
            return ResponseEntity.ok(Map.of(
                    "mensaje", "Pago registrado exitosamente",
                    "reserva", reserva,
                    "reservasVencidas", vencidas
            ));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", ex.getMessage()));
        }
    }

    @PostMapping("/reservas/{numeroReserva}/anular")
    public ResponseEntity<?> anularReservaVencida(
            @PathVariable int numeroReserva,
            Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Debe estar autenticado"));
        }
        try {
            int idPropietario = Integer.parseInt(authentication.getName());
            ReservaPropietarioDTO reserva = propietarioService.anularReservaVencida(numeroReserva, idPropietario);
            return ResponseEntity.ok(Map.of("mensaje", "Reserva anulada exitosamente", "reserva", reserva));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", ex.getMessage()));
        }
    }

    @PostMapping("/reservas/{numeroReserva}/mantener")
    public ResponseEntity<?> mantenerReservaVencida(
            @PathVariable int numeroReserva,
            Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Debe estar autenticado"));
        }
        try {
            int idPropietario = Integer.parseInt(authentication.getName());
            ReservaPropietarioDTO reserva = propietarioService.mantenerReservaVencida(numeroReserva, idPropietario);
            return ResponseEntity.ok(Map.of("mensaje", "Reserva mantenida", "reserva", reserva));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", ex.getMessage()));
        }
    }
}
