package co.edu.uniquindio.casasrurales.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import co.edu.uniquindio.casasrurales.entities.Bano;

/**
 * Repositorio JPA para persistir y consultar banos.
 */
public interface BanoRepository extends JpaRepository<Bano, Integer> {

    List<Bano> findByCodigoCasa(int codigoCasa);
}
