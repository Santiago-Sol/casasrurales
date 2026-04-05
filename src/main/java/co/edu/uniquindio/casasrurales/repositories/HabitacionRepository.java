package co.edu.uniquindio.casasrurales.repositories;

import co.edu.uniquindio.casasrurales.entities.Habitacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface HabitacionRepository extends JpaRepository<Habitacion, Integer> {

    List<Habitacion> findByCasaRuralCodigoCasa(int codigoCasa);

    Optional<Habitacion> findByCodigoHabitacion(String codigoHabitacion);
}
