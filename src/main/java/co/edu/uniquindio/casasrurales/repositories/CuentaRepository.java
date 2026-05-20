package co.edu.uniquindio.casasrurales.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import co.edu.uniquindio.casasrurales.entities.Cuenta;

/**
 * Repositorio JPA para cuentas de acceso y autenticacion.
 */
public interface CuentaRepository extends JpaRepository<Cuenta, Long> {

    Optional<Cuenta> findByEmail(String email);

    Optional<Cuenta> findByCliente_IdUsuario(int idCliente);

    boolean existsByEmail(String email);
}
