package co.edu.uniquindio.casasrurales.controllers;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Calendar;

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
import co.edu.uniquindio.casasrurales.dto.PagoPasarelaDTO;
import co.edu.uniquindio.casasrurales.entities.Cliente;
import co.edu.uniquindio.casasrurales.entities.Habitacion;
import co.edu.uniquindio.casasrurales.entities.Pago;
import co.edu.uniquindio.casasrurales.entities.Propietario;
import co.edu.uniquindio.casasrurales.entities.Reserva;
import co.edu.uniquindio.casasrurales.enums.EstadoPago;
import co.edu.uniquindio.casasrurales.enums.EstadoReserva;
import co.edu.uniquindio.casasrurales.repositories.ClienteRepository;
import co.edu.uniquindio.casasrurales.repositories.HabitacionRepository;
import co.edu.uniquindio.casasrurales.repositories.PagoRepository;
import co.edu.uniquindio.casasrurales.repositories.ReservaRepository;
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
    private final ReservaRepository reservaRepository;
    private final PagoRepository pagoRepository;

    public ReservaController(SistemaReservas sistemaReservas,
                             ClienteRepository clienteRepository,
                             HabitacionRepository habitacionRepository,
                             ReservaRepository reservaRepository,
                             PagoRepository pagoRepository) {
        this.sistemaReservas = sistemaReservas;
        this.clienteRepository = clienteRepository;
        this.habitacionRepository = habitacionRepository;
        this.reservaRepository = reservaRepository;
        this.pagoRepository = pagoRepository;
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

        Optional<Cliente> clienteOpt = clienteRepository.findById(Integer.parseInt(authentication.getName()));
        if (clienteOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Solo los clientes pueden realizar reservas"));
        }

        if (requestDTO.getTelefonoContacto() == null || requestDTO.getTelefonoContacto().isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "El telefono de contacto es obligatorio"));
        }
        if (!requestDTO.getTelefonoContacto().matches("^[0-9+\\- ]{7,20}$")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Ingresa un telefono valido"));
        }

        Cliente cliente = clienteOpt.get();
        cliente.actualizarTelefono(requestDTO.getTelefonoContacto().trim());

        List<Habitacion> habitaciones = resolverHabitaciones(requestDTO);
        if (habitaciones == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Todas las habitaciones seleccionadas deben existir"));
        }

        boolean algunaNoPertenece = habitaciones.stream()
                .anyMatch(habitacion -> habitacion.getCasaRural() == null
                        || habitacion.getCasaRural().getCodigoCasa() != requestDTO.getCodigoCasa());
        if (algunaNoPertenece) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Todas las habitaciones seleccionadas deben pertenecer a la casa reservada"));
        }

        try {
            Reserva reserva = sistemaReservas.realizarReservaCalculandoImporte(
                    requestDTO.getCodigoCasa(),
                    cliente,
                    requestDTO.getFechaEntrada(),
                    requestDTO.getNumeroNoches(),
                    habitaciones
            );

            return ResponseEntity.status(HttpStatus.CREATED).body(crearResumen(reserva));
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                    "error", ex.getMessage(),
                    "disponibilidad", sistemaReservas.consultarDisponibilidadDetallada(
                            requestDTO.getCodigoCasa(),
                            requestDTO.getFechaEntrada(),
                            requestDTO.getNumeroNoches())
            ));
        } catch (IllegalArgumentException | NullPointerException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
        }
    }

    private List<Habitacion> resolverHabitaciones(ReservaRequestDTO requestDTO) {
        if (requestDTO.getCodigosHabitaciones() != null && !requestDTO.getCodigosHabitaciones().isEmpty()) {
            List<String> codigos = requestDTO.getCodigosHabitaciones().stream()
                    .filter(codigo -> codigo != null && !codigo.isBlank())
                    .map(codigo -> codigo.trim())
                    .distinct()
                    .toList();
            if (codigos.size() != requestDTO.getCodigosHabitaciones().size()) {
                return null;
            }
            List<Habitacion> habitaciones = codigos.stream()
                    .map(codigo -> habitacionRepository
                            .findByCasaRuralCodigoCasaAndCodigoHabitacion(requestDTO.getCodigoCasa(), codigo)
                            .orElse(null))
                    .toList();
            return habitaciones.stream().anyMatch(habitacion -> habitacion == null) ? null : habitaciones;
        }

        if (requestDTO.getIdsHabitaciones() != null && !requestDTO.getIdsHabitaciones().isEmpty()) {
            long idsDistintos = requestDTO.getIdsHabitaciones().stream().distinct().count();
            if (idsDistintos != requestDTO.getIdsHabitaciones().size()) {
                return null;
            }
            List<Habitacion> habitaciones = habitacionRepository.findAllById(requestDTO.getIdsHabitaciones());
            return habitaciones.size() != requestDTO.getIdsHabitaciones().size() ? null : habitaciones;
        }

        return List.of();
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

        Optional<Cliente> clienteOpt = clienteRepository.findById(Integer.parseInt(authentication.getName()));
        if (clienteOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Solo los clientes pueden consultar reservas"));
        }

        List<ReservaResumenDTO> reservas = sistemaReservas
                .getReservasPorCliente(clienteOpt.get().getIdUsuario())
                .stream()
                .map(this::crearResumen)
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

        return ResponseEntity.ok(crearResumen(reserva));
    }

    @PostMapping("/{numeroReserva}/pagar")
    public ResponseEntity<?> pagarReserva(@PathVariable int numeroReserva,
                                          @Valid @RequestBody PagoPasarelaDTO pagoDTO,
                                          Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Debes iniciar sesion para pagar una reserva"));
        }

        Optional<Cliente> clienteOpt = clienteRepository.findById(Integer.parseInt(authentication.getName()));
        if (clienteOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Solo los clientes pueden pagar reservas"));
        }

        Reserva reserva = sistemaReservas.buscarReservaPorNumero(numeroReserva);
        if (reserva == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Reserva no encontrada"));
        }
        if (reserva.getCliente() == null || reserva.getCliente().getIdUsuario() != clienteOpt.get().getIdUsuario()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "No tienes permiso para pagar esta reserva"));
        }
        double saldoPendiente = calcularSaldoPendiente(reserva);
        if (reserva.getEstado() != EstadoReserva.PENDIENTE_PAGO
                && !(reserva.getEstado() == EstadoReserva.CONFIRMADA && saldoPendiente > 0)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "La reserva no tiene pagos pendientes"));
        }
        double montoRequerido = reserva.getEstado() == EstadoReserva.PENDIENTE_PAGO
                ? calcularImporteAConsignar(reserva)
                : saldoPendiente;
        if (pagoDTO.getMonto() == null || pagoDTO.getMonto() < montoRequerido) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "El pago debe cubrir el valor requerido para esta fecha de entrada"));
        }

        Pago pago = new Pago(new java.util.Date(), pagoDTO.getMonto(), EstadoPago.PENDIENTE);
        pago.registrar();
        reserva.agregarPago(pago);
        reserva.confirmar();

        pagoRepository.save(pago);
        reservaRepository.save(reserva);

        double saldoDespuesPago = calcularSaldoPendiente(reserva);
        String mensajePago = saldoDespuesPago <= 0
                ? "Pago aprobado. La reserva quedo pagada en su totalidad y el propietario fue notificado."
                : "Pago aprobado. La reserva fue confirmada y el saldo restante debe pagarse antes de la fecha de salida.";

        return ResponseEntity.ok(Map.of(
                "mensaje", mensajePago,
                "reserva", crearResumen(reserva),
                "notificacionPropietario", "El cliente pago el anticipo de la reserva " + reserva.getNumeroReserva()
        ));
    }

    private ReservaResumenDTO crearResumen(Reserva reserva) {
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
                reserva.getCasaRural().getCodigoCasa(),
                obtenerCuentaPropietario(reserva)
        );
        resumen.setImportePagado(calcularImportePagado(reserva));
        return resumen;
    }

    private double calcularImporteAConsignar(Reserva reserva) {
        Calendar limiteAnticipo = Calendar.getInstance();
        normalizarInicioDia(limiteAnticipo);
        limiteAnticipo.add(Calendar.DAY_OF_MONTH, 3);

        Calendar entrada = Calendar.getInstance();
        entrada.setTime(reserva.getFechaEntrada());
        normalizarInicioDia(entrada);

        return entrada.after(limiteAnticipo) ? reserva.getImporteAnticipo() : reserva.getImporteTotal();
    }

    private void normalizarInicioDia(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
    }

    private double calcularImportePagado(Reserva reserva) {
        if (reserva.getPagos() == null) {
            return 0;
        }
        return reserva.getPagos().stream()
                .filter(pago -> pago.getEstado() == EstadoPago.VERIFICADO)
                .map(Pago::getMonto)
                .reduce(0.0, Double::sum);
    }

    private double calcularSaldoPendiente(Reserva reserva) {
        return Math.max(0, reserva.getImporteTotal() - calcularImportePagado(reserva));
    }

    private String obtenerCuentaPropietario(Reserva reserva) {
        Propietario propietario = reserva.getCasaRural().getPropietario();
        return propietario != null ? propietario.getNumeroCuentaBancaria() : null;
    }

    /**
     * Cancela una reserva si pertenece al cliente autenticado.
     */
    @PostMapping("/{numeroReserva}/cancelar")
    public ResponseEntity<?> cancelarReserva(@PathVariable int numeroReserva, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Debes iniciar sesion"));
        }

        Optional<Cliente> clienteOpt = clienteRepository.findById(Integer.parseInt(authentication.getName()));
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
