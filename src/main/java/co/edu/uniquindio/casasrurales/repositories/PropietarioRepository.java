package co.edu.uniquindio.casasrurales.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import co.edu.uniquindio.casasrurales.entities.Propietario;

/**
 * Repositorio JPA para la administracion de propietarios.
 */
public interface PropietarioRepository extends JpaRepository<Propietario, Integer> {

    Optional<Propietario> findByNombreCuenta(String nombreCuenta);

    boolean existsByNombreCuenta(String nombreCuenta);
}
