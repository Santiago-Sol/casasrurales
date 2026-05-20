package co.edu.uniquindio.casasrurales.controllers;

import co.edu.uniquindio.casasrurales.dto.CasaRuralListadoDTO;
import co.edu.uniquindio.casasrurales.entities.CasaRural;
import co.edu.uniquindio.casasrurales.entities.Cliente;
import co.edu.uniquindio.casasrurales.entities.Valoracion;
import co.edu.uniquindio.casasrurales.repositories.CasaRuralRepository;
import co.edu.uniquindio.casasrurales.repositories.ClienteRepository;
import co.edu.uniquindio.casasrurales.repositories.ValoracionRepository;
import co.edu.uniquindio.casasrurales.services.BusquedaCasasService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Controlador API REST para operaciones específicas de Clientes autenticados.
 * Maneja favoritos y la publicación de valoraciones/comentarios.
 */
@RestController
@RequestMapping("/api/cliente")
public class ClienteController {

    private final ClienteRepository clienteRepository;
    private final CasaRuralRepository casaRuralRepository;
    private final ValoracionRepository valoracionRepository;
    private final BusquedaCasasService busquedaCasasService;

    public ClienteController(ClienteRepository clienteRepository,
                             CasaRuralRepository casaRuralRepository,
                             ValoracionRepository valoracionRepository,
                             BusquedaCasasService busquedaCasasService) {
        this.clienteRepository = clienteRepository;
        this.casaRuralRepository = casaRuralRepository;
        this.valoracionRepository = valoracionRepository;
        this.busquedaCasasService = busquedaCasasService;
    }

    /**
     * Obtiene el listado de casas favoritas del cliente autenticado.
     */
    @GetMapping("/favoritos")
    public ResponseEntity<?> obtenerFavoritos(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Debes iniciar sesión para ver tus favoritos"));
        }

        Optional<Cliente> clienteOpt = clienteRepository.findById(Integer.parseInt(authentication.getName()));
        if (clienteOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Solo los clientes pueden tener favoritos"));
        }

        Cliente cliente = clienteOpt.get();
        List<CasaRuralListadoDTO> favoritosDTO = cliente.getFavoritos().stream()
                .map(busquedaCasasService::convertirACasaListadoDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(favoritosDTO);
    }

    /**
     * Agrega o elimina una casa rural de la lista de favoritos del cliente.
     */
    @PostMapping("/favoritos/{codigoCasa}")
    public ResponseEntity<?> toggleFavorito(@PathVariable int codigoCasa, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Debes iniciar sesión para modificar favoritos"));
        }

        Optional<Cliente> clienteOpt = clienteRepository.findById(Integer.parseInt(authentication.getName()));
        if (clienteOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Solo los clientes pueden agregar favoritos"));
        }

        Cliente cliente = clienteOpt.get();
        Optional<CasaRural> casaOpt = casaRuralRepository.findById(codigoCasa);
        if (casaOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "La casa rural no existe"));
        }

        CasaRural casa = casaOpt.get();
        boolean esFavorito;

        if (cliente.getFavoritos().contains(casa)) {
            cliente.removerFavorito(casa);
            esFavorito = false;
        } else {
            cliente.agregarFavorito(casa);
            esFavorito = true;
        }

        clienteRepository.save(cliente);

        return ResponseEntity.ok(Map.of(
                "esFavorito", esFavorito,
                "mensaje", esFavorito ? "Casa agregada a favoritos" : "Casa eliminada de favoritos"
        ));
    }

    /**
     * Publica una valoración y comentario para una casa rural.
     */
    @PostMapping("/valoracion/{codigoCasa}")
    public ResponseEntity<?> registrarValoracion(@PathVariable int codigoCasa,
                                                 @Valid @RequestBody ValoracionRequest request,
                                                 Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Debes iniciar sesión para valorar"));
        }

        Optional<Cliente> clienteOpt = clienteRepository.findById(Integer.parseInt(authentication.getName()));
        if (clienteOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Solo los clientes pueden dejar valoraciones"));
        }

        Cliente cliente = clienteOpt.get();
        Optional<CasaRural> casaOpt = casaRuralRepository.findById(codigoCasa);
        if (casaOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "La casa rural no existe"));
        }

        CasaRural casa = casaOpt.get();

        // Verificar si el cliente ya valoró esta casa para actualizarla o prevenir duplicados
        Optional<Valoracion> valoracionExistente = valoracionRepository
                .findByClienteIdUsuarioAndCasaRuralCodigoCasa(cliente.getIdUsuario(), casa.getCodigoCasa());

        Valoracion valoracion;
        if (valoracionExistente.isPresent()) {
            valoracion = valoracionExistente.get();
            valoracion.setCalificacion(request.getCalificacion());
            valoracion.setComentario(request.getComentario());
        } else {
            valoracion = new Valoracion(cliente, casa, request.getCalificacion(), request.getComentario());
        }

        valoracionRepository.save(valoracion);

        return ResponseEntity.ok(Map.of(
                "mensaje", "Valoración registrada con éxito",
                "calificacion", valoracion.getCalificacion()
        ));
    }

    /**
     * DTO de petición para registrar valoraciones.
     */
    public static class ValoracionRequest {

        @NotNull(message = "La calificación es obligatoria")
        @Min(value = 1, message = "La calificación mínima es 1 estrella")
        @Max(value = 5, message = "La calificación máxima es 5 estrellas")
        private Integer calificacion;

        private String comentario;

        public Integer getCalificacion() {
            return calificacion;
        }

        public void setCalificacion(Integer calificacion) {
            this.calificacion = calificacion;
        }

        public String getComentario() {
            return comentario;
        }

        public void setComentario(String comentario) {
            this.comentario = comentario;
        }
    }
}
