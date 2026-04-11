package co.edu.uniquindio.casasrurales.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import co.edu.uniquindio.casasrurales.entities.Cocina;

/**
 * Repositorio JPA para operaciones de persistencia sobre cocinas.
 */
public interface CocinaRepository extends JpaRepository<Cocina, Integer> {

    List<Cocina> findByCodigoCasa(int codigoCasa);
}
