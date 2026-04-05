package co.edu.uniquindio.casasrurales.repositories;

import co.edu.uniquindio.casasrurales.entities.Bano;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio JPA para persistir y consultar banos.
 */
public interface BanoRepository extends JpaRepository<Bano, Integer> {
}
