package co.edu.uniquindio.casasrurales.repositories;

import co.edu.uniquindio.casasrurales.entities.Pago;
import co.edu.uniquindio.casasrurales.enums.EstadoPago;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PagoRepository extends JpaRepository<Pago, Integer> {

    List<Pago> findByReservaNumeroReserva(int numeroReserva);

    List<Pago> findByEstado(EstadoPago estado);
}
