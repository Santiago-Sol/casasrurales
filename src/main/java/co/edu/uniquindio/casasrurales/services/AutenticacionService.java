package co.edu.uniquindio.casasrurales.services;

import co.edu.uniquindio.casasrurales.dto.RegistroClienteForm;
import co.edu.uniquindio.casasrurales.dto.RegistroPropietarioForm;
import co.edu.uniquindio.casasrurales.entities.Cliente;
import co.edu.uniquindio.casasrurales.entities.Cuenta;
import co.edu.uniquindio.casasrurales.entities.Propietario;
import co.edu.uniquindio.casasrurales.enums.Rol;
import co.edu.uniquindio.casasrurales.repositories.CuentaRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class AutenticacionService {

    private final CuentaRepository cuentaRepository;
    private final PasswordEncoder passwordEncoder;

    public AutenticacionService(CuentaRepository cuentaRepository, PasswordEncoder passwordEncoder) {
        this.cuentaRepository = cuentaRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void registrarPropietario(RegistroPropietarioForm form) {
        validarCorreoUnico(form.getEmail());

        String passwordEncriptado = passwordEncoder.encode(form.getPassword());
        Propietario propietario = new Propietario(
                form.getTelefono(),
                form.getNombreCuenta(),
                passwordEncriptado,
                form.getNumeroCuentaBancaria()
        );

        Cuenta cuenta = new Cuenta(form.getEmail(), passwordEncriptado, Rol.PROPIETARIO);
        cuenta.setPropietario(propietario);
        cuentaRepository.save(cuenta);
    }

    public void registrarCliente(RegistroClienteForm form) {
        validarCorreoUnico(form.getEmail());

        Cliente cliente = new Cliente(form.getTelefono());
        Cuenta cuenta = new Cuenta(
                form.getEmail(),
                passwordEncoder.encode(form.getPassword()),
                Rol.CLIENTE
        );
        cuenta.setCliente(cliente);
        cuentaRepository.save(cuenta);
    }

    private void validarCorreoUnico(String email) {
        if (cuentaRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Ya existe una cuenta registrada con ese correo");
        }
    }
}
