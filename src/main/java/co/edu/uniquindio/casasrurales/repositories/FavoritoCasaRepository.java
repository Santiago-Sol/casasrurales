package co.edu.uniquindio.casasrurales.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import co.edu.uniquindio.casasrurales.entities.FavoritoCasa;

public interface FavoritoCasaRepository extends JpaRepository<FavoritoCasa, Integer> {

    List<FavoritoCasa> findByCliente_IdUsuarioOrderByFechaCreacionDesc(int idCliente);

    Optional<FavoritoCasa> findByCliente_IdUsuarioAndCasaRural_CodigoCasa(int idCliente, int codigoCasa);

    boolean existsByCliente_IdUsuarioAndCasaRural_CodigoCasa(int idCliente, int codigoCasa);
}
