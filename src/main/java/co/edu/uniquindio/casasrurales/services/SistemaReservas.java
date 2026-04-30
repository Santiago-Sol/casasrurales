package co.edu.uniquindio.casasrurales.services;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;

import co.edu.uniquindio.casasrurales.entities.CasaRural;
import co.edu.uniquindio.casasrurales.entities.Cliente;
import co.edu.uniquindio.casasrurales.entities.Habitacion;
import co.edu.uniquindio.casasrurales.entities.Propietario;
import co.edu.uniquindio.casasrurales.entities.Reserva;
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
        return casa.consultarDisponibilidad(fechaEntrada, numeroNoches);
    }

    public Reserva realizarReserva(int codigoCasa, Date fechaEntrada, int numeroNoches, List<Habitacion> habitaciones) {
        CasaRural casa = Objects.requireNonNull(buscarCasaPorCodigo(codigoCasa), "La casa no existe");
        validarCasaReservable(casa);
        validarFechasReserva(fechaEntrada, numeroNoches);
        validarDisponibilidad(casa, fechaEntrada, numeroNoches);

        TipoReserva tipoReserva = (habitaciones == null || habitaciones.isEmpty())
                ? TipoReserva.CASA_ENTERA
                : TipoReserva.POR_HABITACIONES;

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
        validarDisponibilidad(casa, fechaEntrada, numeroNoches);

        TipoReserva tipoReserva = (habitaciones == null || habitaciones.isEmpty())
                ? TipoReserva.CASA_ENTERA
                : TipoReserva.POR_HABITACIONES;

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

    private void validarDisponibilidad(CasaRural casa, Date fechaEntrada, int numeroNoches) {
        String disponibilidad = casa.consultarDisponibilidad(fechaEntrada, numeroNoches);
        if ("RESERVADA".equals(disponibilidad)) {
            throw new IllegalStateException("La casa ya tiene una reserva para las fechas solicitadas");
        }

        if (!"LIBRE".equals(disponibilidad)) {
            throw new IllegalStateException("La casa no esta disponible");
        }
    }

}
