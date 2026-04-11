package co.edu.uniquindio.casasrurales.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import co.edu.uniquindio.casasrurales.entities.Habitacion;

/**
 * Repositorio JPA encargado de las habitaciones registradas en el sistema.
 */
public interface HabitacionRepository extends JpaRepository<Habitacion, Integer> {

    List<Habitacion> findByCasaRuralCodigoCasa(int codigoCasa);

    Optional<Habitacion> findByCodigoHabitacion(String codigoHabitacion);
}
