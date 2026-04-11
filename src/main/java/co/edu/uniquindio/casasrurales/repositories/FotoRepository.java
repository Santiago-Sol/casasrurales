package co.edu.uniquindio.casasrurales.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import co.edu.uniquindio.casasrurales.entities.Foto;

/**
 * Repositorio JPA para consultar las fotos asociadas a las casas rurales.
 */
public interface FotoRepository extends JpaRepository<Foto, Integer> {

    List<Foto> findByCasaRuralCodigoCasa(int codigoCasa);

    List<Foto> findByCodigoCasa(int codigoCasa);
}
