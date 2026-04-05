package co.edu.uniquindio.casasrurales.services;

import co.edu.uniquindio.casasrurales.entities.Cuenta;
import co.edu.uniquindio.casasrurales.repositories.CuentaRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Adaptador entre la entidad {@code Cuenta} y Spring Security.
 * Carga usuarios por correo y construye sus permisos segun el rol almacenado.
 */
@Service
public class CuentaUserDetailsService implements UserDetailsService {

    private final CuentaRepository cuentaRepository;

    public CuentaUserDetailsService(CuentaRepository cuentaRepository) {
        this.cuentaRepository = cuentaRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Cuenta cuenta = cuentaRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("No existe una cuenta con ese correo"));

        return User.withUsername(cuenta.getEmail())
                .password(cuenta.getPassword())
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_" + cuenta.getRol().name())))
                .build();
    }
}
