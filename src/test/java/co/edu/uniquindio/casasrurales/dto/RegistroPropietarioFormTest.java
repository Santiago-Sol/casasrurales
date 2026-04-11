package co.edu.uniquindio.casasrurales.dto;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

/**
 * Pruebas de validación del DTO RegistroPropietarioForm.
 * Valida que las restricciones de validación funcionen correctamente.
 */
@DisplayName("RegistroPropietarioForm - Pruebas de Validación")
@SpringBootTest
class RegistroPropietarioFormTest {

    @Autowired
    private Validator validator;

    private RegistroPropietarioForm formulario;

    @BeforeEach
    void setUp() {
        formulario = new RegistroPropietarioForm();
        formulario.setNombreCuenta("juan123");
        formulario.setEmail("juan@example.com");
        formulario.setTelefono("1234567890");
        formulario.setNumeroCuentaBancaria("123456789");
        formulario.setPassword("Password123");
    }

    @DisplayName("HU1-V01: Formulario válido sin errores de validación")
    @Test
    void testFormularioValido() {
        Set<ConstraintViolation<RegistroPropietarioForm>> violations = validator.validate(formulario);
        assertTrue(violations.isEmpty(), "El formulario válido no debe tener errores");
    }

    @DisplayName("HU1-V02: Rechaza nombreCuenta vacío")
    @Test
    void testNombreCuentaVacio() {
        formulario.setNombreCuenta("");
        Set<ConstraintViolation<RegistroPropietarioForm>> violations = validator.validate(formulario);
        assertFalse(violations.isEmpty(), "Debe rechazar nombreCuenta vacío");
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("nombre de cuenta")));
    }

    @DisplayName("HU1-V03: Rechaza nombreCuenta null")
    @Test
    void testNombreCuentaNull() {
        formulario.setNombreCuenta(null);
        Set<ConstraintViolation<RegistroPropietarioForm>> violations = validator.validate(formulario);
        assertFalse(violations.isEmpty(), "Debe rechazar nombreCuenta null");
    }

    @DisplayName("HU1-V04: Rechaza email vacío")
    @Test
    void testEmailVacio() {
        formulario.setEmail("");
        Set<ConstraintViolation<RegistroPropietarioForm>> violations = validator.validate(formulario);
        assertFalse(violations.isEmpty(), "Debe rechazar email vacío");
    }

    @DisplayName("HU1-V05: Rechaza email inválido")
    @Test
    void testEmailInvalido() {
        formulario.setEmail("emailSinArroba");
        Set<ConstraintViolation<RegistroPropietarioForm>> violations = validator.validate(formulario);
        assertFalse(violations.isEmpty(), "Debe rechazar email inválido");
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("correo")));
    }

    @DisplayName("HU1-V06: Rechaza teléfono vacío")
    @Test
    void testTelefonoVacio() {
        formulario.setTelefono("");
        Set<ConstraintViolation<RegistroPropietarioForm>> violations = validator.validate(formulario);
        assertFalse(violations.isEmpty(), "Debe rechazar teléfono vacío");
    }

    @DisplayName("HU1-V07: Rechaza teléfono con menos de 7 caracteres")
    @Test
    void testTelefonoMenor7Caracteres() {
        formulario.setTelefono("12345");
        Set<ConstraintViolation<RegistroPropietarioForm>> violations = validator.validate(formulario);
        assertFalse(violations.isEmpty(), "Debe rechazar teléfono menor a 7 caracteres");
    }

    @DisplayName("HU1-V08: Rechaza teléfono con más de 20 caracteres")
    @Test
    void testTelefonoMayor20Caracteres() {
        formulario.setTelefono("123456789012345678901");
        Set<ConstraintViolation<RegistroPropietarioForm>> violations = validator.validate(formulario);
        assertFalse(violations.isEmpty(), "Debe rechazar teléfono mayor a 20 caracteres");
    }

    @DisplayName("HU1-V09: Rechaza numeroCuentaBancaria vacío")
    @Test
    void testNumeroCuentaBancariaVacio() {
        formulario.setNumeroCuentaBancaria("");
        Set<ConstraintViolation<RegistroPropietarioForm>> violations = validator.validate(formulario);
        assertFalse(violations.isEmpty(), "Debe rechazar numeroCuentaBancaria vacío");
    }

    @DisplayName("HU1-V10: Rechaza contraseña vacía")
    @Test
    void testContraseñaVacia() {
        formulario.setPassword("");
        Set<ConstraintViolation<RegistroPropietarioForm>> violations = validator.validate(formulario);
        assertFalse(violations.isEmpty(), "Debe rechazar contraseña vacía");
    }

    @DisplayName("HU1-V11: Rechaza contraseña con menos de 8 caracteres")
    @Test
    void testContrasenaMenor8Caracteres() {
        formulario.setPassword("Pass123"); // 7 caracteres
        Set<ConstraintViolation<RegistroPropietarioForm>> violations = validator.validate(formulario);
        assertFalse(violations.isEmpty(), "Debe rechazar contraseña menor a 8 caracteres");
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("8 caracteres")));
    }

    @DisplayName("HU1-V12: Acepta contraseña con exactamente 8 caracteres")
    @Test
    void testContrasena8CaracteresExactos() {
        formulario.setPassword("Pass1234");
        Set<ConstraintViolation<RegistroPropietarioForm>> violations = validator.validate(formulario);
        assertTrue(violations.isEmpty(), "Debe aceptar contraseña con exactamente 8 caracteres");
    }

    @DisplayName("HU1-V13: Acepta contraseña larga")
    @Test
    void testContraseñaLarga() {
        formulario.setPassword("UnaContraseñaMuyLargaYSegura12345");
        Set<ConstraintViolation<RegistroPropietarioForm>> violations = validator.validate(formulario);
        assertTrue(violations.isEmpty(), "Debe aceptar contraseñas largas");
    }

    @DisplayName("HU1-V14: Teléfono válido con caracteres especiales")
    @Test
    void testTelefonoConCaracteresEspeciales() {
        formulario.setTelefono("+57 300-1234567");
        Set<ConstraintViolation<RegistroPropietarioForm>> violations = validator.validate(formulario);
        assertTrue(violations.isEmpty(), "Debe aceptar teléfono con caracteres especiales como +, -, espacio");
    }

    @DisplayName("HU1-V15: Email válido con diferentes dominios")
    @Test
    void testEmailDiferentesDominios() {
        formulario.setEmail("usuario@dominio.co");
        Set<ConstraintViolation<RegistroPropietarioForm>> violations = validator.validate(formulario);
        assertTrue(violations.isEmpty(), "Debe aceptar emails con diferentes dominios");
    }
}
