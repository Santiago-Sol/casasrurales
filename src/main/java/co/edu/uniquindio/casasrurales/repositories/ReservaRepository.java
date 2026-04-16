package co.edu.uniquindio.casasrurales.repositories;

import co.edu.uniquindio.casasrurales.entities.Reserva;
import co.edu.uniquindio.casasrurales.enums.EstadoReserva;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;
import java.util.List;

/**
 * Repositorio JPA para consultas de reservas por casa, cliente, estado o fechas.
 */
public interface ReservaRepository extends JpaRepository<Reserva, Integer> {

    List<Reserva> findByCasaRuralCodigoCasa(int codigoCasa);

    List<Reserva> findByClienteIdUsuario(int idCliente);

    List<Reserva> findByEstado(EstadoReserva estado);

    List<Reserva> findByFechaEntradaBetween(Date fechaInicio, Date fechaFin);
    List<Reserva> findByCliente_IdUsuario(int idUsuario);
}
