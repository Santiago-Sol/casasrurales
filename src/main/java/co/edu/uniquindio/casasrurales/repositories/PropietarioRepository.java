package co.edu.uniquindio.casasrurales.repositories;

import co.edu.uniquindio.casasrurales.entities.Propietario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PropietarioRepository extends JpaRepository<Propietario, Integer> {

    Optional<Propietario> findByNombreCuenta(String nombreCuenta);
}
