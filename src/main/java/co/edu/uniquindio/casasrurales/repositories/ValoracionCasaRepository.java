package co.edu.uniquindio.casasrurales.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import co.edu.uniquindio.casasrurales.entities.ValoracionCasa;

public interface ValoracionCasaRepository extends JpaRepository<ValoracionCasa, Integer> {

    List<ValoracionCasa> findByCasaRural_CodigoCasaOrderByFechaActualizacionDesc(int codigoCasa);

    Optional<ValoracionCasa> findByCliente_IdUsuarioAndCasaRural_CodigoCasa(int idCliente, int codigoCasa);
}
