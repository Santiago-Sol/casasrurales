package co.edu.uniquindio.casasrurales.repositories;

import co.edu.uniquindio.casasrurales.entities.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, Integer> {

    Optional<Cliente> findByTelefono(String telefono);
}
