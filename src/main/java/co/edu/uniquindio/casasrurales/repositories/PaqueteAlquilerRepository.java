package co.edu.uniquindio.casasrurales.repositories;

import co.edu.uniquindio.casasrurales.entities.PaqueteAlquiler;
import co.edu.uniquindio.casasrurales.enums.ModalidadAlquiler;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;
import java.util.List;

public interface PaqueteAlquilerRepository extends JpaRepository<PaqueteAlquiler, Integer> {

    List<PaqueteAlquiler> findByCasaRuralCodigoCasa(int codigoCasa);

    List<PaqueteAlquiler> findByModalidad(ModalidadAlquiler modalidad);

    List<PaqueteAlquiler> findByFechaInicioLessThanEqualAndFechaFinGreaterThanEqual(Date fechaInicio, Date fechaFin);
}
