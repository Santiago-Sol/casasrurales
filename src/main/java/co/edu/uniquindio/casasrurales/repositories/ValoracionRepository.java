package co.edu.uniquindio.casasrurales.repositories;

import co.edu.uniquindio.casasrurales.entities.Valoracion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio JPA para la entidad Valoracion.
 */
public interface ValoracionRepository extends JpaRepository<Valoracion, Long> {

    List<Valoracion> findByCasaRuralCodigoCasaOrderByFechaCreacionDesc(int codigoCasa);

    Optional<Valoracion> findByClienteIdUsuarioAndCasaRuralCodigoCasa(int idCliente, int codigoCasa);
}
