package co.edu.uniquindio.casasrurales.services;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import co.edu.uniquindio.casasrurales.dto.RegistroClienteForm;
import co.edu.uniquindio.casasrurales.dto.RegistroPropietarioForm;
import co.edu.uniquindio.casasrurales.entities.Cliente;
import co.edu.uniquindio.casasrurales.entities.Cuenta;
import co.edu.uniquindio.casasrurales.entities.Propietario;
import co.edu.uniquindio.casasrurales.enums.Rol;
import co.edu.uniquindio.casasrurales.repositories.CuentaRepository;
import co.edu.uniquindio.casasrurales.repositories.PropietarioRepository;
import jakarta.transaction.Transactional;

/**
 * Servicio de aplicacion encargado del registro de usuarios y creacion de cuentas.
 * Centraliza la validacion del correo, nombre de cuenta y el cifrado de contrasenas.
 */
@Service
@Transactional
public class AutenticacionService {

    private final CuentaRepository cuentaRepository;
    private final PropietarioRepository propietarioRepository;
    private final PasswordEncoder passwordEncoder;

    public AutenticacionService(CuentaRepository cuentaRepository,
                                PropietarioRepository propietarioRepository,
                                PasswordEncoder passwordEncoder) {
        this.cuentaRepository = cuentaRepository;
        this.propietarioRepository = propietarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Registra un nuevo propietario en el sistema.
     * Valida que el correo y nombre de cuenta sean únicos, y que la contraseña tenga al menos 8 caracteres.
     * 
     * @param form Formulario con los datos del propietario a registrar
     * @throws IllegalArgumentException si el correo o nombre de cuenta ya existen, o la contraseña no es válida
     */
    public void registrarPropietario(RegistroPropietarioForm form) {
        // Validar que el correo sea único
        validarCorreoUnico(form.getEmail());

        // Validar que el nombre de cuenta sea único
        validarNombreCuentaUnico(form.getNombreCuenta());

        // Validar que la contraseña cumpla los requisitos mínimos
        validarRequsitosContrasena(form.getPassword());

        // Cifrar la contraseña usando bcrypt
        String passwordEncriptado = passwordEncoder.encode(form.getPassword());
        
        // Crear la entidad Propietario
        Propietario propietario = new Propietario(
                form.getTelefono(),
                form.getNombreCuenta(),
                passwordEncriptado,
                form.getNumeroCuentaBancaria()
        );

        // Crear la cuenta de acceso
        Cuenta cuenta = new Cuenta(form.getEmail(), passwordEncriptado, Rol.PROPIETARIO);
        cuenta.setPropietario(propietario);
        
        // Guardar la transacción
        cuentaRepository.save(cuenta);
    }

    /**
     * Registra un nuevo cliente en el sistema.
     * Valida que el correo sea único y que la contraseña tenga al menos 8 caracteres.
     * 
     * @param form Formulario con los datos del cliente a registrar
     * @throws IllegalArgumentException si el correo ya existe o la contraseña no es válida
     */
    public void registrarCliente(RegistroClienteForm form) {
        validarCorreoUnico(form.getEmail());
        validarRequsitosContrasena(form.getPassword());

        Cliente cliente = new Cliente(form.getTelefono());
        Cuenta cuenta = new Cuenta(
                form.getEmail(),
                passwordEncoder.encode(form.getPassword()),
                Rol.CLIENTE
        );
        cuenta.setCliente(cliente);
        cuentaRepository.save(cuenta);
    }

    /**
     * Valida que el correo no exista en el sistema.
     * 
     * @param email Correo a validar
     * @throws IllegalArgumentException si el correo ya existe
     */
    private void validarCorreoUnico(String email) {
        if (cuentaRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Ya existe una cuenta registrada con ese correo");
        }
    }

    /**
     * Valida que el nombre de cuenta sea único en el sistema.
     * 
     * @param nombreCuenta Nombre de cuenta a validar
     * @throws IllegalArgumentException si el nombre de cuenta ya existe
     */
    private void validarNombreCuentaUnico(String nombreCuenta) {
        if (propietarioRepository.existsByNombreCuenta(nombreCuenta)) {
            throw new IllegalArgumentException("El nombre de cuenta ya existe. Por favor, elige otro.");
        }
    }

    /**
     * Valida que la contraseña cumpla con los requisitos mínimos de seguridad.
     * - Mínimo 8 caracteres
     * 
     * @param password Contraseña a validar
     * @throws IllegalArgumentException si la contraseña no cumple los requisitos
     */
    private void validarRequsitosContrasena(String password) {
        if (password == null || password.length() < 8) {
            throw new IllegalArgumentException("La contraseña debe tener al menos 8 caracteres");
        }
    }

    /**
     * Autentica un propietario validando nombre de cuenta y contraseña.
     * 
     * @param nombreCuenta Nombre de cuenta del propietario
     * @param contrasena Contraseña sin cifrar
     * @return ID del propietario autenticado
     * @throws IllegalArgumentException si las credenciales son inválidas
     */
    public int autenticarPropietario(String nombreCuenta, String contrasena) {
        Propietario propietario = propietarioRepository.findByNombreCuenta(nombreCuenta)
                .orElseThrow(() -> new IllegalArgumentException("Nombre de cuenta o contraseña incorrectos"));
        
        if (!passwordEncoder.matches(contrasena, propietario.getContrasena())) {
            throw new IllegalArgumentException("Nombre de cuenta o contraseña incorrectos");
        }

        return propietario.getIdUsuario();
    }

    /**
     * Autentica un cliente validando email y contraseña.
     * 
     * @param email Email del cliente
     * @param contrasena Contraseña sin cifrar
     * @return ID del cliente autenticado
     * @throws IllegalArgumentException si las credenciales son inválidas
     */
    public int autenticarCliente(String email, String contrasena) {
        Cuenta cuenta = cuentaRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Email o contraseña incorrectos"));
        
        if (!passwordEncoder.matches(contrasena, cuenta.getPassword())) {
            throw new IllegalArgumentException("Email o contraseña incorrectos");
        }

        if (cuenta.getCliente() == null) {
            throw new IllegalArgumentException("Esta cuenta no es de cliente");
        }

        return cuenta.getCliente().getIdUsuario();
    }
}
