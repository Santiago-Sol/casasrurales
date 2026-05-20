package co.edu.uniquindio.casasrurales.controllers;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import co.edu.uniquindio.casasrurales.dto.ValoracionCasaRequestDTO;
import co.edu.uniquindio.casasrurales.services.ClienteInteraccionService;
import jakarta.validation.Valid;

@RestController
public class ClienteInteraccionController {

    private final ClienteInteraccionService clienteInteraccionService;

    public ClienteInteraccionController(ClienteInteraccionService clienteInteraccionService) {
        this.clienteInteraccionService = clienteInteraccionService;
    }

    @GetMapping("/api/busqueda/{codigoCasa}/valoraciones")
    public ResponseEntity<?> listarValoraciones(@PathVariable int codigoCasa) {
        return ResponseEntity.ok(clienteInteraccionService.listarValoraciones(codigoCasa));
    }

    @GetMapping("/api/clientes/favoritos")
    public ResponseEntity<?> listarFavoritos(Authentication authentication) {
        return ResponseEntity.ok(clienteInteraccionService.listarFavoritos(idCliente(authentication)));
    }

    @GetMapping("/api/clientes/favoritos/{codigoCasa}")
    public ResponseEntity<?> consultarFavorito(@PathVariable int codigoCasa, Authentication authentication) {
        boolean favorita = clienteInteraccionService.esFavorita(idCliente(authentication), codigoCasa);
        return ResponseEntity.ok(Map.of("favorita", favorita));
    }

    @PostMapping("/api/clientes/favoritos/{codigoCasa}")
    public ResponseEntity<?> agregarFavorito(@PathVariable int codigoCasa, Authentication authentication) {
        clienteInteraccionService.agregarFavorito(idCliente(authentication), codigoCasa);
        return ResponseEntity.ok(Map.of("favorita", true));
    }

    @DeleteMapping("/api/clientes/favoritos/{codigoCasa}")
    public ResponseEntity<?> quitarFavorito(@PathVariable int codigoCasa, Authentication authentication) {
        clienteInteraccionService.quitarFavorito(idCliente(authentication), codigoCasa);
        return ResponseEntity.ok(Map.of("favorita", false));
    }

    @PostMapping("/api/clientes/valoraciones/{codigoCasa}")
    public ResponseEntity<?> guardarValoracion(
            @PathVariable int codigoCasa,
            @Valid @RequestBody ValoracionCasaRequestDTO request,
            Authentication authentication) {
        return ResponseEntity.ok(
                clienteInteraccionService.guardarValoracion(idCliente(authentication), codigoCasa, request));
    }

    private int idCliente(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new IllegalArgumentException("Cliente no autenticado");
        }
        return Integer.parseInt(authentication.getName());
    }
}
