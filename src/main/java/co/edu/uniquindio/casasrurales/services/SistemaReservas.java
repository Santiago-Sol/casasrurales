package co.edu.uniquindio.casasrurales.services;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;

import co.edu.uniquindio.casasrurales.dto.DisponibilidadCasaDTO;
import co.edu.uniquindio.casasrurales.dto.DisponibilidadDiaDTO;
import co.edu.uniquindio.casasrurales.dto.DisponibilidadHabitacionDTO;
import co.edu.uniquindio.casasrurales.entities.CasaRural;
import co.edu.uniquindio.casasrurales.entities.Cliente;
import co.edu.uniquindio.casasrurales.entities.Habitacion;
import co.edu.uniquindio.casasrurales.entities.PaqueteAlquiler;
import co.edu.uniquindio.casasrurales.entities.Propietario;
import co.edu.uniquindio.casasrurales.entities.Reserva;
import co.edu.uniquindio.casasrurales.enums.EstadoDisponibilidad;
import co.edu.uniquindio.casasrurales.enums.EstadoReserva;
import co.edu.uniquindio.casasrurales.enums.TipoReserva;
import co.edu.uniquindio.casasrurales.repositories.CasaRuralRepository;
import co.edu.uniquindio.casasrurales.repositories.PropietarioRepository;
import co.edu.uniquindio.casasrurales.repositories.ReservaRepository;
import jakarta.transaction.Transactional;

/**
 * Servicio principal del dominio de reservas.
 * Coordina consultas de casas, disponibilidad y creacion de reservas.
 */
@Service
@Transactional
public class SistemaReservas {

    private final PropietarioRepository propietarioRepository;
    private final CasaRuralRepository casaRuralRepository;
    private final ReservaRepository reservaRepository;

    public SistemaReservas(PropietarioRepository propietarioRepository,
                           CasaRuralRepository casaRuralRepository,
                           ReservaRepository reservaRepository) {
        this.propietarioRepository = propietarioRepository;
        this.casaRuralRepository = casaRuralRepository;
        this.reservaRepository = reservaRepository;
    }

    public List<Propietario> getPropietarios() {
        return propietarioRepository.findAll();
    }

    public List<CasaRural> getCasas() {
        return casaRuralRepository.findAll();
    }

    public List<Reserva> getReservas() {
        return reservaRepository.findAll();
    }

    public void agregarPropietario(Propietario propietario) {
        propietarioRepository.save(propietario);
    }

    public List<CasaRural> buscarCasasPorPoblacion(String poblacion) {
        return casaRuralRepository.findByPoblacionIgnoreCase(poblacion);
    }

    public CasaRural buscarCasaPorCodigo(int codigoCasa) {
        return casaRuralRepository.findById(codigoCasa).orElse(null);
    }

    public String consultarDisponibilidad(int codigoCasa, Date fechaEntrada, int numeroNoches) {
        CasaRural casa = buscarCasaPorCodigo(codigoCasa);
        if (casa == null) {
            return "CASA_NO_ENCONTRADA";
        }
        DisponibilidadCasaDTO disponibilidad = consultarDisponibilidadDetallada(codigoCasa, fechaEntrada, numeroNoches);
        if (disponibilidad.getDias().stream()
                .allMatch(dia -> dia.getEstadoCasaEntera() == EstadoDisponibilidad.LIBRE)) {
            return EstadoDisponibilidad.LIBRE.name();
        }
        if (disponibilidad.getDias().stream()
                .anyMatch(dia -> dia.getEstadoCasaEntera() == EstadoDisponibilidad.RESERVADA)) {
            return EstadoDisponibilidad.RESERVADA.name();
        }
        return EstadoDisponibilidad.NO_DISPONIBLE.name();
    }

    public DisponibilidadCasaDTO consultarDisponibilidadDetallada(int codigoCasa, Date fechaEntrada, int numeroNoches) {
        CasaRural casa = Objects.requireNonNull(buscarCasaPorCodigo(codigoCasa), "La casa no existe");
        validarFechasReserva(fechaEntrada, numeroNoches);

        List<Reserva> reservas = reservaRepository.findByCasaRuralCodigoCasa(codigoCasa).stream()
                .filter(reserva -> reserva.getEstado() != EstadoReserva.ANULADA)
                .toList();

        List<DisponibilidadDiaDTO> dias = java.util.stream.IntStream.range(0, numeroNoches)
                .mapToObj(offset -> construirDisponibilidadDia(casa, reservas, sumarDias(fechaEntrada, offset)))
                .toList();

        return new DisponibilidadCasaDTO(codigoCasa, fechaEntrada, numeroNoches, dias);
    }

    public Reserva realizarReserva(int codigoCasa, Date fechaEntrada, int numeroNoches, List<Habitacion> habitaciones) {
        CasaRural casa = Objects.requireNonNull(buscarCasaPorCodigo(codigoCasa), "La casa no existe");
        validarCasaReservable(casa);
        validarFechasReserva(fechaEntrada, numeroNoches);
        TipoReserva tipoReserva = (habitaciones == null || habitaciones.isEmpty())
                ? TipoReserva.CASA_ENTERA
                : TipoReserva.POR_HABITACIONES;
        validarDisponibilidad(casa, fechaEntrada, numeroNoches, habitaciones, tipoReserva);

        Reserva reserva = new Reserva(
                fechaEntrada,
                numeroNoches,
                tipoReserva,
                0,
                EstadoReserva.PENDIENTE_PAGO,
                null,
                casa,
                habitaciones
        );

        casa.agregarReserva(reserva);
        if (habitaciones != null) {
            habitaciones.forEach(habitacion -> habitacion.agregarReserva(reserva));
        }

        return reservaRepository.save(reserva);
    }

    public Reserva realizarReserva(int codigoCasa, Cliente cliente, Date fechaEntrada, int numeroNoches,
                                   List<Habitacion> habitaciones, double importeTotal) {
        CasaRural casa = Objects.requireNonNull(buscarCasaPorCodigo(codigoCasa), "La casa no existe");
        validarCasaReservable(casa);
        validarFechasReserva(fechaEntrada, numeroNoches);
        validarImporte(importeTotal);
        TipoReserva tipoReserva = (habitaciones == null || habitaciones.isEmpty())
                ? TipoReserva.CASA_ENTERA
                : TipoReserva.POR_HABITACIONES;
        validarDisponibilidad(casa, fechaEntrada, numeroNoches, habitaciones, tipoReserva);

        Reserva reserva = new Reserva(
                fechaEntrada,
                numeroNoches,
                tipoReserva,
                importeTotal,
                EstadoReserva.PENDIENTE_PAGO,
                cliente,
                casa,
                habitaciones
        );

        casa.agregarReserva(reserva);
        if (cliente != null) {
            cliente.agregarReserva(reserva);
        }
        if (habitaciones != null) {
            habitaciones.forEach(habitacion -> habitacion.agregarReserva(reserva));
        }

        return reservaRepository.save(reserva);
    }

    public String mostrarResultadoConsulta() {
        return "Casas registradas: %d, reservas activas: %d"
                .formatted(casaRuralRepository.count(), reservaRepository.count());
    }
    public List<Reserva> getReservasPorCliente(int idCliente) {
        return reservaRepository.findByClienteIdUsuario(idCliente);
    }
    public Reserva buscarReservaPorNumero(int numeroReserva) {
        return reservaRepository.findById(numeroReserva).orElse(null);
    }

    /**
     * Cancela una reserva si pertenece al cliente y su estado permite cancelación.
     * @param numeroReserva id de la reserva
     * @param idCliente id del cliente que solicita la cancelación
     * @return la reserva actualizada
     */
    public Reserva cancelarReserva(int numeroReserva, int idCliente) {
        Reserva reserva = buscarReservaPorNumero(numeroReserva);
        if (reserva == null) {
            throw new IllegalArgumentException("Reserva no encontrada");
        }

        if (reserva.getCliente() == null || reserva.getCliente().getIdUsuario() != idCliente) {
            throw new IllegalArgumentException("No tienes permiso para cancelar esta reserva");
        }

        if (reserva.getEstado() == co.edu.uniquindio.casasrurales.enums.EstadoReserva.ANULADA) {
            throw new IllegalStateException("La reserva ya está anulada");
        }

        reserva.cancelar();
        return reservaRepository.save(reserva);
    }

    private void validarCasaReservable(CasaRural casa) {
        if (!casa.isActiva()) {
            throw new IllegalStateException("La casa no esta activa y no puede ser reservada");
        }

        if (!casa.esValida()) {
            throw new IllegalStateException("La casa no cumple los requisitos minimos para ser reservada");
        }
    }

    private void validarFechasReserva(Date fechaEntrada, int numeroNoches) {
        if (fechaEntrada == null) {
            throw new IllegalArgumentException("La fecha de entrada es obligatoria");
        }

        Calendar hoy = Calendar.getInstance();
        hoy.set(Calendar.HOUR_OF_DAY, 0);
        hoy.set(Calendar.MINUTE, 0);
        hoy.set(Calendar.SECOND, 0);
        hoy.set(Calendar.MILLISECOND, 0);

        if (fechaEntrada.before(hoy.getTime())) {
            throw new IllegalArgumentException("La fecha de entrada no puede ser en el pasado");
        }

        if (numeroNoches < 1) {
            throw new IllegalArgumentException("El numero de noches debe ser al menos 1");
        }
    }

    private void validarImporte(double importeTotal) {
        if (importeTotal <= 0) {
            throw new IllegalArgumentException("El importe total debe ser mayor a cero");
        }
    }

    private void validarDisponibilidad(CasaRural casa, Date fechaEntrada, int numeroNoches,
                                       List<Habitacion> habitaciones, TipoReserva tipoReserva) {
        DisponibilidadCasaDTO disponibilidad = consultarDisponibilidadDetallada(
                casa.getCodigoCasa(), fechaEntrada, numeroNoches);

        boolean disponible = switch (tipoReserva) {
            case CASA_ENTERA -> disponibilidad.getDias().stream()
                    .allMatch(dia -> dia.getEstadoCasaEntera() == EstadoDisponibilidad.LIBRE);
            case POR_HABITACIONES -> disponibilidad.getDias().stream()
                    .allMatch(dia -> habitacionesDisponibles(dia, habitaciones));
        };

        if (!disponible) {
            throw new IllegalStateException("La casa no esta disponible");
        }
    }

    private DisponibilidadDiaDTO construirDisponibilidadDia(CasaRural casa, List<Reserva> reservas, Date fecha) {
        PaqueteAlquiler paquete = casa.getPaquetesAlquiler().stream()
                .filter(p -> p.incluyeFecha(fecha))
                .findFirst()
                .orElse(null);

        boolean casaReservada = reservas.stream()
                .anyMatch(reserva -> reservaCubreFecha(reserva, fecha));

        EstadoDisponibilidad estadoCasa = estadoCasaEntera(paquete, casaReservada);

        List<DisponibilidadHabitacionDTO> habitaciones = casa.getHabitaciones().stream()
                .map(habitacion -> new DisponibilidadHabitacionDTO(
                        habitacion.getIdHabitacion(),
                        habitacion.getCodigoHabitacion(),
                        estadoHabitacion(paquete, casaReservada, reservas, habitacion, fecha)))
                .toList();

        return new DisponibilidadDiaDTO(fecha, estadoCasa, paquete != null ? paquete.getModalidad() : null, habitaciones);
    }

    private EstadoDisponibilidad estadoCasaEntera(PaqueteAlquiler paquete, boolean casaReservada) {
        if (paquete == null || !paquete.permiteCasaEntera()) {
            return EstadoDisponibilidad.NO_DISPONIBLE;
        }
        return casaReservada ? EstadoDisponibilidad.RESERVADA : EstadoDisponibilidad.LIBRE;
    }

    private EstadoDisponibilidad estadoHabitacion(PaqueteAlquiler paquete, boolean casaReservada,
                                                  List<Reserva> reservas, Habitacion habitacion, Date fecha) {
        if (paquete == null || !paquete.permiteHabitaciones()) {
            return EstadoDisponibilidad.NO_DISPONIBLE;
        }
        if (casaReservadaPorCompleto(reservas, fecha)) {
            return EstadoDisponibilidad.RESERVADA;
        }
        boolean reservada = reservas.stream()
                .filter(reserva -> reserva.getTipoReserva() == TipoReserva.POR_HABITACIONES)
                .filter(reserva -> reservaCubreFecha(reserva, fecha))
                .flatMap(reserva -> reserva.getHabitaciones().stream())
                .anyMatch(reservadaHabitacion -> reservadaHabitacion.getIdHabitacion() == habitacion.getIdHabitacion());
        return reservada ? EstadoDisponibilidad.RESERVADA : EstadoDisponibilidad.LIBRE;
    }

    private boolean habitacionesDisponibles(DisponibilidadDiaDTO dia, List<Habitacion> habitaciones) {
        if (habitaciones == null || habitaciones.isEmpty()) {
            return false;
        }
        return habitaciones.stream().allMatch(habitacion -> dia.getHabitaciones().stream()
                .anyMatch(estado -> estado.getIdHabitacion() == habitacion.getIdHabitacion()
                        && estado.getEstado() == EstadoDisponibilidad.LIBRE));
    }

    private boolean casaReservadaPorCompleto(List<Reserva> reservas, Date fecha) {
        return reservas.stream()
                .anyMatch(reserva -> reserva.getTipoReserva() == TipoReserva.CASA_ENTERA && reservaCubreFecha(reserva, fecha));
    }

    private boolean reservaCubreFecha(Reserva reserva, Date fecha) {
        Date fechaNormalizada = inicioDia(fecha);
        Date fechaEntradaReserva = inicioDia(reserva.getFechaEntrada());
        Calendar fin = Calendar.getInstance();
        fin.setTime(fechaEntradaReserva);
        fin.add(Calendar.DAY_OF_MONTH, reserva.getNumeroNoches() - 1);
        return !fechaNormalizada.before(fechaEntradaReserva) && !fechaNormalizada.after(fin.getTime());
    }

    private Date sumarDias(Date fecha, int dias) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(fecha);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.add(Calendar.DAY_OF_MONTH, dias);
        return calendar.getTime();
    }

    private Date inicioDia(Date fecha) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(fecha);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

}
