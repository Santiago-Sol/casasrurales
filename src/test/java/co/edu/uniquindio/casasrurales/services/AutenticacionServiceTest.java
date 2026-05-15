package co.edu.uniquindio.casasrurales.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.Optional;

import co.edu.uniquindio.casasrurales.dto.RegistroPropietarioForm;
import co.edu.uniquindio.casasrurales.entities.Cliente;
import co.edu.uniquindio.casasrurales.entities.Cuenta;
import co.edu.uniquindio.casasrurales.entities.Propietario;
import co.edu.uniquindio.casasrurales.enums.Rol;
import co.edu.uniquindio.casasrurales.repositories.CuentaRepository;
import co.edu.uniquindio.casasrurales.repositories.PropietarioRepository;

/**
 * Pruebas unitarias del servicio de autenticación.
 * Valida el comportamiento de registro de propietarios con las validaciones requeridas.
 */
@DisplayName("AutenticacionService - Pruebas Unitarias")
@ExtendWith(MockitoExtension.class)
class AutenticacionServiceTest {

    @Mock
    private CuentaRepository cuentaRepository;

    @Mock
    private PropietarioRepository propietarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AutenticacionService autenticacionService;

    private RegistroPropietarioForm formularioValido;

    @BeforeEach
    void setUp() {
        formularioValido = new RegistroPropietarioForm();
        formularioValido.setNombreCuenta("juan123");
        formularioValido.setEmail("juan@example.com");
        formularioValido.setTelefono("1234567890");
        formularioValido.setNumeroCuentaBancaria("123456789");
        formularioValido.setPassword("Password123");
    }

    @DisplayName("HU1-001: Registro exitoso cuando todos los datos son válidos")
    @Test
    void testRegistroPropietarioExitoso() {
        // Arrange
        when(cuentaRepository.existsByEmail(anyString())).thenReturn(false);
        when(propietarioRepository.existsByNombreCuenta(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");

        // Act
        autenticacionService.registrarPropietario(formularioValido);

        // Assert
        verify(cuentaRepository, times(1)).save(any(Cuenta.class));
        verify(passwordEncoder, times(1)).encode(formularioValido.getPassword());
    }

    @DisplayName("HU1-002: Error cuando el correo ya existe (validación de unicidad de email)")
    @Test
    void testRegistroFallaCorreoDuplicado() {
        // Arrange
        when(cuentaRepository.existsByEmail(formularioValido.getEmail())).thenReturn(true);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> autenticacionService.registrarPropietario(formularioValido)
        );

        assertEquals("Ya existe una cuenta registrada con ese correo", exception.getMessage());
        verify(cuentaRepository, never()).save(any(Cuenta.class));
    }

    @DisplayName("HU1-003: Error cuando el nombre de cuenta ya existe (validación de unicidad)")
    @Test
    void testRegistroFallaNombreCuentaDuplicado() {
        // Arrange
        when(cuentaRepository.existsByEmail(anyString())).thenReturn(false);
        when(propietarioRepository.existsByNombreCuenta(formularioValido.getNombreCuenta())).thenReturn(true);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> autenticacionService.registrarPropietario(formularioValido)
        );

        assertEquals("El nombre de cuenta ya existe. Por favor, elige otro.", exception.getMessage());
        verify(cuentaRepository, never()).save(any(Cuenta.class));
    }

    @DisplayName("HU1-004: Error cuando la contraseña tiene menos de 8 caracteres")
    @Test
    void testRegistroFallaContrasenaMenor8Caracteres() {
        // Arrange
        formularioValido.setPassword("Pass123"); // 7 caracteres
        when(cuentaRepository.existsByEmail(anyString())).thenReturn(false);
        when(propietarioRepository.existsByNombreCuenta(anyString())).thenReturn(false);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> autenticacionService.registrarPropietario(formularioValido)
        );

        assertEquals("La contraseña debe tener al menos 8 caracteres", exception.getMessage());
        verify(cuentaRepository, never()).save(any(Cuenta.class));
    }

    @DisplayName("HU1-005: Error cuando la contraseña es null")
    @Test
    void testRegistroFallaContrasenaNull() {
        // Arrange
        formularioValido.setPassword(null);
        when(cuentaRepository.existsByEmail(anyString())).thenReturn(false);
        when(propietarioRepository.existsByNombreCuenta(anyString())).thenReturn(false);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> autenticacionService.registrarPropietario(formularioValido)
        );

        assertEquals("La contraseña debe tener al menos 8 caracteres", exception.getMessage());
        verify(cuentaRepository, never()).save(any(Cuenta.class));
    }

    @DisplayName("HU1-006: La contraseña se cifra correctamente usando bcrypt")
    @Test
    void testContrasenaCifradaConBcrypt() {
        // Arrange
        when(cuentaRepository.existsByEmail(anyString())).thenReturn(false);
        when(propietarioRepository.existsByNombreCuenta(anyString())).thenReturn(false);
        String passwordCifrada = "$2a$10$hashedPasswordExample";
        when(passwordEncoder.encode(formularioValido.getPassword())).thenReturn(passwordCifrada);

        // Act
        autenticacionService.registrarPropietario(formularioValido);

        // Assert
        verify(passwordEncoder, times(1)).encode(formularioValido.getPassword());
        verify(cuentaRepository, times(1)).save(argThat(cuenta ->
                cuenta.getPassword().equals(passwordCifrada) &&
                cuenta.getRol() == Rol.PROPIETARIO
        ));
    }

    @DisplayName("HU1-007: El nombre de cuenta se asigna correctamente al propietario")
    @Test
    void testNombreCuentaAsignadoAlPropietario() {
        // Arrange
        when(cuentaRepository.existsByEmail(anyString())).thenReturn(false);
        when(propietarioRepository.existsByNombreCuenta(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");

        // Act
        autenticacionService.registrarPropietario(formularioValido);

        // Assert
        verify(cuentaRepository, times(1)).save(argThat(cuenta ->
                cuenta.getPropietario() != null &&
                cuenta.getPropietario().getNombreCuenta().equals(formularioValido.getNombreCuenta())
        ));
    }

    @DisplayName("HU1-008: Orden correcto de validaciones (email primero)")
    @Test
    void testOrdenValidacionesEmailPrimero() {
        // Arrange
        when(cuentaRepository.existsByEmail(anyString())).thenReturn(true);
        // No se necesita stub de propietarioRepository porque falla antes

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> autenticacionService.registrarPropietario(formularioValido)
        );

        // Debe fallar por email, no por nombre de cuenta
        assertEquals("Ya existe una cuenta registrada con ese correo", exception.getMessage());
    }

    @DisplayName("HU1-009: Contraseña con exactamente 8 caracteres es válida")
    @Test
    void testContrasena8CaracteresExactosValida() {
        // Arrange
        formularioValido.setPassword("Pass1234"); // Exactamente 8 caracteres
        when(cuentaRepository.existsByEmail(anyString())).thenReturn(false);
        when(propietarioRepository.existsByNombreCuenta(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");

        // Act
        autenticacionService.registrarPropietario(formularioValido);

        // Assert
        verify(cuentaRepository, times(1)).save(any(Cuenta.class));
    }

    @DisplayName("HU1-010: No se guarda si no cumple validaciones (transacción atómica)")
    @Test
    void testTransaccionAtomicaNoGuardaSiValidaciFalla() {
        // Arrange
        formularioValido.setPassword("Short"); // Menos de 8 caracteres
        when(cuentaRepository.existsByEmail(anyString())).thenReturn(false);
        when(propietarioRepository.existsByNombreCuenta(anyString())).thenReturn(false);

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> autenticacionService.registrarPropietario(formularioValido)
        );

        verify(cuentaRepository, never()).save(any(Cuenta.class));
    }

    @DisplayName("RN131: Registro rechaza propietario sin nombre de cuenta")
    @Test
    void registrarPropietarioRechazaNombreCuentaVacio() {
        formularioValido.setNombreCuenta(" ");
        when(cuentaRepository.existsByEmail(anyString())).thenReturn(false);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> autenticacionService.registrarPropietario(formularioValido));

        assertEquals("El nombre de cuenta es obligatorio", exception.getMessage());
        verify(propietarioRepository, never()).existsByNombreCuenta(anyString());
        verify(cuentaRepository, never()).save(any(Cuenta.class));
    }

    @DisplayName("RN131: Registro rechaza propietario con contraseña en blanco")
    @Test
    void registrarPropietarioRechazaContrasenaBlanco() {
        formularioValido.setPassword("        ");
        when(cuentaRepository.existsByEmail(anyString())).thenReturn(false);
        when(propietarioRepository.existsByNombreCuenta(anyString())).thenReturn(false);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> autenticacionService.registrarPropietario(formularioValido));

        assertEquals("La contraseña debe tener al menos 8 caracteres", exception.getMessage());
        verify(cuentaRepository, never()).save(any(Cuenta.class));
    }

    @DisplayName("RN140: Registro rechaza propietario sin cuenta bancaria")
    @Test
    void registrarPropietarioRechazaCuentaBancariaVacia() {
        formularioValido.setNumeroCuentaBancaria(" ");
        when(cuentaRepository.existsByEmail(anyString())).thenReturn(false);
        when(propietarioRepository.existsByNombreCuenta(anyString())).thenReturn(false);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> autenticacionService.registrarPropietario(formularioValido));

        assertEquals("La cuenta bancaria del propietario es obligatoria", exception.getMessage());
        verify(cuentaRepository, never()).save(any(Cuenta.class));
    }

    @DisplayName("RN132: Autentica propietario con nombre de cuenta y contraseña correctos")
    @Test
    void autenticarPropietarioValidaCredenciales() {
        Propietario propietario = new Propietario("3001234567", "juan123", "hashedPassword", "123456789");
        propietario.setIdUsuario(8);
        when(propietarioRepository.findByNombreCuenta("juan123")).thenReturn(Optional.of(propietario));
        when(passwordEncoder.matches("Password123", "hashedPassword")).thenReturn(true);

        int id = autenticacionService.autenticarPropietario(" juan123 ", "Password123");

        assertEquals(8, id);
        verify(propietarioRepository).findByNombreCuenta("juan123");
    }

    @DisplayName("RN132: Autenticacion de propietario rechaza contraseña incorrecta")
    @Test
    void autenticarPropietarioRechazaContrasenaIncorrecta() {
        Propietario propietario = new Propietario("3001234567", "juan123", "hashedPassword", "123456789");
        when(propietarioRepository.findByNombreCuenta("juan123")).thenReturn(Optional.of(propietario));
        when(passwordEncoder.matches("mala", "hashedPassword")).thenReturn(false);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> autenticacionService.autenticarPropietario("juan123", "mala"));

        assertEquals("Nombre de cuenta o contraseña incorrectos", exception.getMessage());
    }

    @DisplayName("RN132: Autenticacion de propietario rechaza credenciales vacias")
    @Test
    void autenticarPropietarioRechazaCredencialesVacias() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> autenticacionService.autenticarPropietario(" ", "Password123"));

        assertEquals("Nombre de cuenta o contraseña incorrectos", exception.getMessage());
        verify(propietarioRepository, never()).findByNombreCuenta(anyString());
    }

    @DisplayName("RN134: Autenticacion de cliente rechaza cuentas de propietario")
    @Test
    void autenticarClienteRechazaCuentaDePropietario() {
        Cuenta cuenta = new Cuenta("dueno@example.com", "hashedPassword", Rol.PROPIETARIO);
        when(cuentaRepository.findByEmail("dueno@example.com")).thenReturn(Optional.of(cuenta));
        when(passwordEncoder.matches("Password123", "hashedPassword")).thenReturn(true);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> autenticacionService.autenticarCliente("dueno@example.com", "Password123"));

        assertEquals("Esta cuenta no es de cliente", exception.getMessage());
    }

    @DisplayName("RN134: Autentica cliente solo cuando la cuenta pertenece a cliente")
    @Test
    void autenticarClienteValidaCuentaCliente() {
        Cliente cliente = new Cliente("3008881234");
        cliente.setIdUsuario(12);
        Cuenta cuenta = new Cuenta("cliente@example.com", "hashedPassword", Rol.CLIENTE);
        cuenta.setCliente(cliente);
        when(cuentaRepository.findByEmail("cliente@example.com")).thenReturn(Optional.of(cuenta));
        when(passwordEncoder.matches("Password123", "hashedPassword")).thenReturn(true);

        int id = autenticacionService.autenticarCliente(" cliente@example.com ", "Password123");

        assertEquals(12, id);
        verify(cuentaRepository).findByEmail("cliente@example.com");
    }
}
