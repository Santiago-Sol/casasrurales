package co.edu.uniquindio.casasrurales.repositories;

import co.edu.uniquindio.casasrurales.entities.Cocina;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio JPA para operaciones de persistencia sobre cocinas.
 */
public interface CocinaRepository extends JpaRepository<Cocina, Integer> {
}
