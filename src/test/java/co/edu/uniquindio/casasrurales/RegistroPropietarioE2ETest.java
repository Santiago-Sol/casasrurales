package co.edu.uniquindio.casasrurales;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import co.edu.uniquindio.casasrurales.dto.RegistroPropietarioForm;
import co.edu.uniquindio.casasrurales.entities.Cuenta;
import co.edu.uniquindio.casasrurales.enums.Rol;
import co.edu.uniquindio.casasrurales.repositories.CuentaRepository;
import co.edu.uniquindio.casasrurales.repositories.PropietarioRepository;
import co.edu.uniquindio.casasrurales.services.AutenticacionService;

/**
 * Pruebas de integración end-to-end del registro de propietarios.
 * Utilizan una base de datos H2 en memoria para testing.
 * Validan el flujo completo desde el formulario hasta la persistencia en BD.
 */
@DisplayName("RegistroPropietario - Pruebas E2E de Integración")
@SpringBootTest
@Transactional
class RegistroPropietarioE2ETest {

    @Autowired
    private AutenticacionService autenticacionService;

    @Autowired
    private CuentaRepository cuentaRepository;

    @Autowired
    private PropietarioRepository propietarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @DisplayName("HU1-E2E-01: Registro exitoso persiste datos en BD correctamente")
    @Test
    void testRegistroE2EExitosoPersistencia() {
        // Arrange
        RegistroPropietarioForm formulario = new RegistroPropietarioForm();
        formulario.setNombreCuenta("propietario1");
        formulario.setEmail("propietario1@example.com");
        formulario.setTelefono("3001234567");
        formulario.setNumeroCuentaBancaria("987654321");
        formulario.setPassword("Segura1234");

        // Act: Registrar propietario
        autenticacionService.registrarPropietario(formulario);

        // Assert: Verificar que la cuenta existe en BD
        var cuentaGuardada = cuentaRepository.findByEmail("propietario1@example.com");
        assertTrue(cuentaGuardada.isPresent(), "La cuenta debe existir en la BD");

        Cuenta cuenta = cuentaGuardada.get();
        assertNotNull(cuenta.getPropietario(), "La cuenta debe tener propietario asociado");
        assertEquals("propietario1", cuenta.getPropietario().getNombreCuenta());
        assertEquals("3001234567", cuenta.getPropietario().getTelefono());
        assertEquals("987654321", cuenta.getPropietario().getNumeroCuentaBancaria());
        assertEquals(Rol.PROPIETARIO, cuenta.getRol());
    }

    @DisplayName("HU1-E2E-02: La contraseña se almacena cifrada con bcrypt")
    @Test
    void testRegistroE2EContrasenaAlmacenadaCifrada() {
        String passwordPlaintext = "Segura1234";

        // Arrange
        RegistroPropietarioForm formulario = new RegistroPropietarioForm();
        formulario.setNombreCuenta("propietario2");
        formulario.setEmail("propietario2@example.com");
        formulario.setTelefono("3001234567");
        formulario.setNumeroCuentaBancaria("987654321");
        formulario.setPassword(passwordPlaintext);

        // Act: Registrar propietario
        autenticacionService.registrarPropietario(formulario);

        // Assert: Verificar que la contraseña está cifrada
        var cuentaGuardada = cuentaRepository.findByEmail("propietario2@example.com");
        assertTrue(cuentaGuardada.isPresent());

        Cuenta cuenta = cuentaGuardada.get();
        assertNotEquals(passwordPlaintext, cuenta.getPassword(), 
                "La contraseña no debe ser igual al texto plano");
        assertTrue(passwordEncoder.matches(passwordPlaintext, cuenta.getPassword()),
                "La contraseña debe coincidir cuando se verifica con bcrypt");
    }

    @DisplayName("HU1-E2E-03: No se pueden registrar dos propietarios con el mismo email")
    @Test
    void testRegistroE2EEmailUnicoEnBD() {
        String emailComun = "email@example.com";

        // Arrange & Act: Primer registro exitoso
        RegistroPropietarioForm formulario1 = new RegistroPropietarioForm();
        formulario1.setNombreCuenta("propietario3");
        formulario1.setEmail(emailComun);
        formulario1.setTelefono("3001234567");
        formulario1.setNumeroCuentaBancaria("987654321");
        formulario1.setPassword("Segura1234");
        autenticacionService.registrarPropietario(formulario1);

        // Arrange: Segundo registro con mismo email
        RegistroPropietarioForm formulario2 = new RegistroPropietarioForm();
        formulario2.setNombreCuenta("propietario4");
        formulario2.setEmail(emailComun);
        formulario2.setTelefono("3001234567");
        formulario2.setNumeroCuentaBancaria("987654321");
        formulario2.setPassword("Segura1234");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, 
            () -> autenticacionService.registrarPropietario(formulario2));

        // Assert: Solo una cuenta en BD
        var cuentas = cuentaRepository.findByEmail(emailComun);
        assertTrue(cuentas.isPresent());
    }

    @DisplayName("HU1-E2E-04: No se pueden registrar dos propietarios con el mismo nombreCuenta")
    @Test
    void testRegistroE2ENombreCuentaUnicoEnBD() {
        String nombreCuentaComun = "usuario_unico";

        // Arrange & Act: Primer registro exitoso
        RegistroPropietarioForm formulario1 = new RegistroPropietarioForm();
        formulario1.setNombreCuenta(nombreCuentaComun);
        formulario1.setEmail("email1@example.com");
        formulario1.setTelefono("3001234567");
        formulario1.setNumeroCuentaBancaria("987654321");
        formulario1.setPassword("Segura1234");
        autenticacionService.registrarPropietario(formulario1);

        // Arrange: Segundo registro con mismo nombreCuenta
        RegistroPropietarioForm formulario2 = new RegistroPropietarioForm();
        formulario2.setNombreCuenta(nombreCuentaComun);
        formulario2.setEmail("email2@example.com");
        formulario2.setTelefono("3001234567");
        formulario2.setNumeroCuentaBancaria("987654321");
        formulario2.setPassword("Segura1234");

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> autenticacionService.registrarPropietario(formulario2));

        // Assert: Solo un propietario con ese nombre de cuenta en BD
        var propietario = propietarioRepository.findByNombreCuenta(nombreCuentaComun);
        assertTrue(propietario.isPresent());
    }

    @DisplayName("HU1-E2E-05: Validación directa del servicio con contraseña corta")
    @Test
    void testRegistroE2EServicioContrasenaCortaRechaza() {
        // Arrange
        RegistroPropietarioForm formulario = new RegistroPropietarioForm();
        formulario.setNombreCuenta("propietario5");
        formulario.setEmail("propietario5@example.com");
        formulario.setTelefono("3001234567");
        formulario.setNumeroCuentaBancaria("987654321");
        formulario.setPassword("Short7"); // Menos de 8 caracteres

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> autenticacionService.registrarPropietario(formulario));
        assertEquals("La contraseña debe tener al menos 8 caracteres", exception.getMessage());

        // Verificar que NO se guardó en BD
        assertFalse(cuentaRepository.findByEmail("propietario5@example.com").isPresent());
    }

    @DisplayName("HU1-E2E-06: Validación del servicio rechaza email duplicado")
    @Test
    void testRegistroE2EServicioEmailDuplicado() {
        // Arrange
        RegistroPropietarioForm formulario1 = new RegistroPropietarioForm();
        formulario1.setNombreCuenta("propietario6");
        formulario1.setEmail("duplicado@example.com");
        formulario1.setTelefono("3001234567");
        formulario1.setNumeroCuentaBancaria("987654321");
        formulario1.setPassword("Segura1234");

        // Act: Primer registro exitoso
        autenticacionService.registrarPropietario(formulario1);

        // Arrange: Segundo intento con email duplicado
        RegistroPropietarioForm formulario2 = new RegistroPropietarioForm();
        formulario2.setNombreCuenta("propietario7");
        formulario2.setEmail("duplicado@example.com");
        formulario2.setTelefono("3001234567");
        formulario2.setNumeroCuentaBancaria("987654321");
        formulario2.setPassword("Segura1234");

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> autenticacionService.registrarPropietario(formulario2));
        assertEquals("Ya existe una cuenta registrada con ese correo", exception.getMessage());
    }

    @DisplayName("HU1-E2E-07: Validación del servicio rechaza nombreCuenta duplicado")
    @Test
    void testRegistroE2EServicioNombreCuentaDuplicado() {
        // Arrange
        RegistroPropietarioForm formulario1 = new RegistroPropietarioForm();
        formulario1.setNombreCuenta("usuario_repetido");
        formulario1.setEmail("email1@example.com");
        formulario1.setTelefono("3001234567");
        formulario1.setNumeroCuentaBancaria("987654321");
        formulario1.setPassword("Segura1234");

        // Act: Primer registro exitoso
        autenticacionService.registrarPropietario(formulario1);

        // Arrange: Segundo intento con nombreCuenta duplicado
        RegistroPropietarioForm formulario2 = new RegistroPropietarioForm();
        formulario2.setNombreCuenta("usuario_repetido");
        formulario2.setEmail("email2@example.com");
        formulario2.setTelefono("3001234567");
        formulario2.setNumeroCuentaBancaria("987654321");
        formulario2.setPassword("Segura1234");

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> autenticacionService.registrarPropietario(formulario2));
        assertEquals("El nombre de cuenta ya existe. Por favor, elige otro.", exception.getMessage());
    }

    @DisplayName("HU1-E2E-08: Múltiples registros exitosos sin conflictos")
    @Test
    void testRegistroE2EMultiplesPropiertariosExitosos() {
        // Act: Registrar varios propietarios
        for (int i = 1; i <= 5; i++) {
            RegistroPropietarioForm formulario = new RegistroPropietarioForm();
            formulario.setNombreCuenta("propietario_" + i);
            formulario.setEmail("propietario" + i + "@example.com");
            formulario.setTelefono("300123456" + i);
            formulario.setNumeroCuentaBancaria("" + (987654321 + i));
            formulario.setPassword("Segura1234");
            autenticacionService.registrarPropietario(formulario);
        }

        // Assert: Todos están en BD
        for (int i = 1; i <= 5; i++) {
            assertTrue(cuentaRepository.findByEmail("propietario" + i + "@example.com").isPresent(),
                    "Propietario " + i + " debe existir en BD");
        }
    }
}
