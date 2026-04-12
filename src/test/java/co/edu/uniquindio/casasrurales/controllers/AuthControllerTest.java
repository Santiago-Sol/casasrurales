package co.edu.uniquindio.casasrurales.controllers;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import co.edu.uniquindio.casasrurales.dto.RegistroClienteForm;
import co.edu.uniquindio.casasrurales.dto.RegistroPropietarioForm;
import co.edu.uniquindio.casasrurales.services.AutenticacionService;

/**
 * Pruebas unitarias del controlador REST de autenticación.
 * Valida el comportamiento de los endpoints de registro con respuestas JSON.
 */
@DisplayName("AuthController - Pruebas Unitarias REST")
class AuthControllerTest {

    private AuthController authController;
    private AutenticacionService autenticacionService;

    @BeforeEach
    void setUp() {
        autenticacionService = mock(AutenticacionService.class);
        authController = new AuthController(autenticacionService);
    }

    @DisplayName("HU1-C01: POST /auth/registro/propietario exitoso retorna CREATED")
    @Test
    void testRegistroPropietario_Exitoso() {
        // Arrange
        RegistroPropietarioForm formulario = new RegistroPropietarioForm();
        formulario.setNombreCuenta("juan123");
        formulario.setEmail("juan@example.com");
        formulario.setTelefono("1234567890");
        formulario.setNumeroCuentaBancaria("123456789");
        formulario.setPassword("Password123");

        // Act
        ResponseEntity<Map<String, String>> respuesta = authController.registrarPropietario(formulario);

        // Assert
        assertEquals(HttpStatus.CREATED, respuesta.getStatusCode());
        assertNotNull(respuesta.getBody());
        assertEquals("Propietario registrado exitosamente", respuesta.getBody().get("mensaje"));
        verify(autenticacionService, times(1)).registrarPropietario(formulario);
    }

    @DisplayName("HU1-C02: POST /auth/registro/propietario email duplicado retorna BAD_REQUEST")
    @Test
    void testRegistroPropietario_EmailDuplicado() {
        // Arrange
        RegistroPropietarioForm formulario = new RegistroPropietarioForm();
        formulario.setNombreCuenta("juan123");
        formulario.setEmail("duplicado@example.com");
        formulario.setTelefono("1234567890");
        formulario.setNumeroCuentaBancaria("123456789");
        formulario.setPassword("Password123");

        doThrow(new IllegalArgumentException("Ya existe una cuenta registrada con ese correo"))
                .when(autenticacionService).registrarPropietario(formulario);

        // Act
        ResponseEntity<Map<String, String>> respuesta = authController.registrarPropietario(formulario);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, respuesta.getStatusCode());
        assertNotNull(respuesta.getBody());
        assertEquals("Ya existe una cuenta registrada con ese correo", respuesta.getBody().get("error"));
        verify(autenticacionService, times(1)).registrarPropietario(formulario);
    }

    @DisplayName("HU1-C03: POST /auth/registro/propietario nombreCuenta duplicado retorna BAD_REQUEST")
    @Test
    void testRegistroPropietario_NombreCuentaDuplicado() {
        // Arrange
        RegistroPropietarioForm formulario = new RegistroPropietarioForm();
        formulario.setNombreCuenta("existente");
        formulario.setEmail("nuevo@example.com");
        formulario.setTelefono("1234567890");
        formulario.setNumeroCuentaBancaria("123456789");
        formulario.setPassword("Password123");

        doThrow(new IllegalArgumentException("El nombre de cuenta ya existe. Por favor, elige otro."))
                .when(autenticacionService).registrarPropietario(formulario);

        // Act
        ResponseEntity<Map<String, String>> respuesta = authController.registrarPropietario(formulario);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, respuesta.getStatusCode());
        assertNotNull(respuesta.getBody());
        assertEquals("El nombre de cuenta ya existe. Por favor, elige otro.", respuesta.getBody().get("error"));
        verify(autenticacionService, times(1)).registrarPropietario(formulario);
    }

    @DisplayName("HU1-C04: POST /auth/registro/cliente exitoso retorna CREATED")
    @Test
    void testRegistroCliente_Exitoso() {
        // Arrange
        RegistroClienteForm formulario = new RegistroClienteForm();
        formulario.setEmail("carla@example.com");
        formulario.setTelefono("3008881234");
        formulario.setPassword("Password456");

        // Act
        ResponseEntity<Map<String, String>> respuesta = authController.registrarCliente(formulario);

        // Assert
        assertEquals(HttpStatus.CREATED, respuesta.getStatusCode());
        assertNotNull(respuesta.getBody());
        assertEquals("Cliente registrado exitosamente", respuesta.getBody().get("mensaje"));
        verify(autenticacionService, times(1)).registrarCliente(formulario);
    }

    @DisplayName("HU1-C05: POST /auth/registro/cliente email duplicado retorna BAD_REQUEST")
    @Test
    void testRegistroCliente_EmailDuplicado() {
        // Arrange
        RegistroClienteForm formulario = new RegistroClienteForm();
        formulario.setEmail("duplicado@example.com");
        formulario.setTelefono("3008881234");
        formulario.setPassword("Password456");

        doThrow(new IllegalArgumentException("Ya existe una cuenta registrada con ese correo"))
                .when(autenticacionService).registrarCliente(formulario);

        // Act
        ResponseEntity<Map<String, String>> respuesta = authController.registrarCliente(formulario);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, respuesta.getStatusCode());
        assertNotNull(respuesta.getBody());
        assertEquals("Ya existe una cuenta registrada con ese correo", respuesta.getBody().get("error"));
        verify(autenticacionService, times(1)).registrarCliente(formulario);
    }

    @DisplayName("HU1-C06: POST /auth/registro/cliente error genérico retorna BAD_REQUEST")
    @Test
    void testRegistroCliente_ErrorGenerico() {
        // Arrange
        RegistroClienteForm formulario = new RegistroClienteForm();
        formulario.setEmail("nuevo@example.com");
        formulario.setTelefono("3008881234");
        formulario.setPassword("Password456");

        doThrow(new IllegalArgumentException("Error en el registro"))
                .when(autenticacionService).registrarCliente(formulario);

        // Act
        ResponseEntity<Map<String, String>> respuesta = authController.registrarCliente(formulario);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, respuesta.getStatusCode());
        assertNotNull(respuesta.getBody());
        assertEquals("Error en el registro", respuesta.getBody().get("error"));
        verify(autenticacionService, times(1)).registrarCliente(formulario);
    }
}


