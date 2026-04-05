package co.edu.uniquindio.casasrurales.repositories;

import co.edu.uniquindio.casasrurales.entities.CasaRural;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CasaRuralRepository extends JpaRepository<CasaRural, Integer> {

    List<CasaRural> findByPoblacionIgnoreCase(String poblacion);

    List<CasaRural> findByActivaTrue();

    List<CasaRural> findByPropietarioIdUsuario(int idPropietario);
}
