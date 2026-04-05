package co.edu.uniquindio.casasrurales.repositories;

import co.edu.uniquindio.casasrurales.entities.Foto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FotoRepository extends JpaRepository<Foto, Integer> {

    List<Foto> findByCasaRuralCodigoCasa(int codigoCasa);
}
