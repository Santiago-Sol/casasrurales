package co.edu.uniquindio.casasrurales.controllers;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.edu.uniquindio.casasrurales.dto.ReservaRequestDTO;
import co.edu.uniquindio.casasrurales.dto.ReservaResumenDTO;
import co.edu.uniquindio.casasrurales.entities.Cliente;
import co.edu.uniquindio.casasrurales.entities.Habitacion;
import co.edu.uniquindio.casasrurales.entities.Reserva;
import co.edu.uniquindio.casasrurales.repositories.ClienteRepository;
import co.edu.uniquindio.casasrurales.repositories.HabitacionRepository;
import co.edu.uniquindio.casasrurales.services.SistemaReservas;
import jakarta.validation.Valid;

/**
 * API REST para la realizacion de reservas.
 * Solo clientes autenticados pueden crear reservas.
 * Implementa creacion y consulta de reservas propias.
 */
@RestController
@RequestMapping("/api/reservas")
public class ReservaController {

    private final SistemaReservas sistemaReservas;
    private final ClienteRepository clienteRepository;
    private final HabitacionRepository habitacionRepository;

    public ReservaController(SistemaReservas sistemaReservas,
                             ClienteRepository clienteRepository,
                             HabitacionRepository habitacionRepository) {
        this.sistemaReservas = sistemaReservas;
        this.clienteRepository = clienteRepository;
        this.habitacionRepository = habitacionRepository;
    }

    /**
     * Crea una nueva reserva para el cliente autenticado.
     * Genera automaticamente el numero de reserva unico.
     *
     * @param requestDTO  datos de la reserva enviados en el cuerpo
     * @param authentication usuario autenticado por Spring Security
     * @return resumen de la reserva creada con su numero unico
     */
    @PostMapping
    public ResponseEntity<?> realizarReserva(@Valid @RequestBody ReservaRequestDTO requestDTO,
                                             Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Debes iniciar sesion para realizar una reserva"));
        }

        Optional<Cliente> clienteOpt = clienteRepository.findByTelefono(authentication.getName());
        if (clienteOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Solo los clientes pueden realizar reservas"));
        }

        List<Habitacion> habitaciones = List.of();
        if (requestDTO.getIdsHabitaciones() != null && !requestDTO.getIdsHabitaciones().isEmpty()) {
            habitaciones = habitacionRepository.findAllById(requestDTO.getIdsHabitaciones());
        }

        Reserva reserva = sistemaReservas.realizarReserva(
                requestDTO.getCodigoCasa(),
                clienteOpt.get(),
                requestDTO.getFechaEntrada(),
                requestDTO.getNumeroNoches(),
                habitaciones,
                requestDTO.getImporteTotal()
        );

        ReservaResumenDTO resumen = new ReservaResumenDTO(
                reserva.getNumeroReserva(),
                reserva.getFechaReserva(),
                reserva.getFechaEntrada(),
                reserva.getNumeroNoches(),
                reserva.getTipoReserva(),
                reserva.getImporteTotal(),
                reserva.getImporteAnticipo(),
                reserva.getFechaLimitePago(),
                reserva.getEstado(),
                reserva.getCasaRural().getPoblacion(),
                reserva.getCasaRural().getCodigoCasa()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(resumen);
    }

    /**
     * Consulta todas las reservas del cliente autenticado.
     *
     * @param authentication usuario autenticado
     * @return lista de reservas del cliente
     */
    @GetMapping("/mis-reservas")
    public ResponseEntity<?> getMisReservas(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Debes iniciar sesion"));
        }

        Optional<Cliente> clienteOpt = clienteRepository.findByTelefono(authentication.getName());
        if (clienteOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Solo los clientes pueden consultar reservas"));
        }

        List<ReservaResumenDTO> reservas = sistemaReservas
                .getReservasPorCliente(clienteOpt.get().getIdUsuario())
                .stream()
                .map(r -> new ReservaResumenDTO(
                        r.getNumeroReserva(),
                        r.getFechaReserva(),
                        r.getFechaEntrada(),
                        r.getNumeroNoches(),
                        r.getTipoReserva(),
                        r.getImporteTotal(),
                        r.getImporteAnticipo(),
                        r.getFechaLimitePago(),
                        r.getEstado(),
                        r.getCasaRural().getPoblacion(),
                        r.getCasaRural().getCodigoCasa()
                ))
                .toList();

        return ResponseEntity.ok(reservas);
    }

    /**
     * Consulta el detalle de una reserva por su numero unico.
     *
     * @param numeroReserva identificador unico de la reserva
     * @return resumen de la reserva o 404 si no existe
     */
    @GetMapping("/{numeroReserva}")
    public ResponseEntity<?> getReservaPorNumero(@PathVariable int numeroReserva) {
        Reserva reserva = sistemaReservas.buscarReservaPorNumero(numeroReserva);
        if (reserva == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Reserva no encontrada"));
        }

        ReservaResumenDTO resumen = new ReservaResumenDTO(
                reserva.getNumeroReserva(),
                reserva.getFechaReserva(),
                reserva.getFechaEntrada(),
                reserva.getNumeroNoches(),
                reserva.getTipoReserva(),
                reserva.getImporteTotal(),
                reserva.getImporteAnticipo(),
                reserva.getFechaLimitePago(),
                reserva.getEstado(),
                reserva.getCasaRural().getPoblacion(),
                reserva.getCasaRural().getCodigoCasa()
        );

        return ResponseEntity.ok(resumen);
    }

    /**
     * Cancela una reserva si pertenece al cliente autenticado.
     */
    @PostMapping("/{numeroReserva}/cancelar")
    public ResponseEntity<?> cancelarReserva(@PathVariable int numeroReserva, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Debes iniciar sesion"));
        }

        Optional<Cliente> clienteOpt = clienteRepository.findByTelefono(authentication.getName());
        if (clienteOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Solo los clientes pueden cancelar reservas"));
        }

        try {
            Reserva reserva = sistemaReservas.cancelarReserva(numeroReserva, clienteOpt.get().getIdUsuario());
            return ResponseEntity.ok(Map.of("mensaje", "Reserva anulada", "numeroReserva", String.valueOf(reserva.getNumeroReserva())));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
        }
    }
}
