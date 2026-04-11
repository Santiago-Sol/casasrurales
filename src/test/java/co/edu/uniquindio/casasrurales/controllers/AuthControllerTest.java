package co.edu.uniquindio.casasrurales.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import co.edu.uniquindio.casasrurales.dto.RegistroPropietarioForm;
import co.edu.uniquindio.casasrurales.services.AutenticacionService;

/**
 * Pruebas unitarias del controlador de autenticación.
 * Valida el comportamiento de los endpoints de registro sin necesidad de MockMvc.
 */
@DisplayName("AuthController - Pruebas Unitarias")
class AuthControllerTest {

    private AuthController authController;
    private AutenticacionService autenticacionService;
    private Model model;
    private BindingResult bindingResult;

    @BeforeEach
    void setUp() {
        autenticacionService = mock(AutenticacionService.class);
        authController = new AuthController(autenticacionService);
        model = mock(Model.class);
        bindingResult = new MapBindingResult(java.util.Map.of(), "registroPropietarioForm");
    }

    @DisplayName("HU1-C01: GET /registro/propietario retorna la vista de formulario")
    @Test
    void testObtenerFormularioRegistroPropietario() {
        // Act
        String vista = authController.verRegistroPropietario(model);

        // Assert
        assertEquals("auth/registro-propietario", vista);
        verify(model, times(1)).addAttribute(eq("registroPropietarioForm"), any());
    }

    @DisplayName("HU1-C02: POST /registro/propietario exitoso redirige a login")
    @Test
    void testRegistroPropietarioExitoso() {
        // Arrange
        RegistroPropietarioForm formulario = new RegistroPropietarioForm();
        formulario.setNombreCuenta("juan123");
        formulario.setEmail("juan@example.com");
        formulario.setTelefono("1234567890");
        formulario.setNumeroCuentaBancaria("123456789");
        formulario.setPassword("Password123");

        // Act
        String resultado = authController.registrarPropietario(formulario, bindingResult, model);

        // Assert
        assertEquals("redirect:/login?registroExitoso", resultado);
        verify(autenticacionService, times(1)).registrarPropietario(formulario);
    }

    @DisplayName("HU1-C03: POST /registro/propietario con error devuelve formulario")
    @Test
    void testRegistroFallaConErrorDeValidacion() {
        // Arrange
        RegistroPropietarioForm formulario = new RegistroPropietarioForm();
        formulario.setNombreCuenta("juan123");
        MapBindingResult bindingResultConErrores = new MapBindingResult(
                java.util.Map.of(), "registroPropietarioForm");
        bindingResultConErrores.reject("campo.requerido", "El campo es requerido");

        // Act
        String resultado = authController.registrarPropietario(formulario, bindingResultConErrores, model);

        // Assert
        assertEquals("auth/registro-propietario", resultado);
        verify(autenticacionService, never()).registrarPropietario(any());
    }

    @DisplayName("HU1-C04: POST /registro/propietario muestra error si email existe")
    @Test
    void testRegistroFallaEmailDuplicado() {
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
        String resultado = authController.registrarPropietario(formulario, bindingResult, model);

        // Assert
        assertEquals("auth/registro-propietario", resultado);
        verify(model, times(1)).addAttribute("errorRegistro", "Ya existe una cuenta registrada con ese correo");
    }

    @DisplayName("HU1-C05: POST /registro/propietario muestra error si nombreCuenta existe")
    @Test
    void testRegistroFallaNombreCuentaDuplicado() {
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
        String resultado = authController.registrarPropietario(formulario, bindingResult, model);

        // Assert
        assertEquals("auth/registro-propietario", resultado);
        verify(model, times(1)).addAttribute("errorRegistro", "El nombre de cuenta ya existe. Por favor, elige otro.");
    }

    @DisplayName("HU1-C06: POST /registro/propietario maneja excepciones genéricas")
    @Test
    void testRegistroManejaSexcepciones() {
        // Arrange
        RegistroPropietarioForm formulario = new RegistroPropietarioForm();
        formulario.setNombreCuenta("juan123");
        formulario.setEmail("juan@example.com");
        formulario.setTelefono("1234567890");
        formulario.setNumeroCuentaBancaria("123456789");
        formulario.setPassword("Password123");

        doThrow(new RuntimeException("Error inesperado"))
                .when(autenticacionService).registrarPropietario(formulario);

        // Act & Assert - Espera que lance la excepción o la maneje
        assertThrows(RuntimeException.class, () -> 
            authController.registrarPropietario(formulario, bindingResult, model)
        );
    }
}

