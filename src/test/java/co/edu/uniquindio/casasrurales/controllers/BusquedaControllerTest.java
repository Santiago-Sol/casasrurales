package co.edu.uniquindio.casasrurales.controllers;

import co.edu.uniquindio.casasrurales.dto.CasaRuralDetalleDTO;
import co.edu.uniquindio.casasrurales.dto.CasaRuralListadoDTO;
import co.edu.uniquindio.casasrurales.services.BusquedaCasasService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.ui.Model;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias del controlador de búsqueda de casas.
 * Valida los endpoints y las vistas sin necesidad de base de datos real.
 */
@DisplayName("BusquedaController - Pruebas Unitarias")
class BusquedaControllerTest {

    private BusquedaController busquedaController;
    private BusquedaCasasService busquedaCasasService;
    private Model model;

    @BeforeEach
    void setUp() {
        busquedaCasasService = mock(BusquedaCasasService.class);
        busquedaController = new BusquedaController(busquedaCasasService);
        model = mock(Model.class);
    }

    @DisplayName("HU6-C01: GET /busqueda retorna formulario de búsqueda")
    @Test
    void testVerFormularioBusqueda() {
        // Act
        String vista = busquedaController.verFormularioBusqueda(null);

        // Assert
        assertEquals("busqueda/formulario-busqueda", vista);
    }

    @DisplayName("HU6-C02: GET /busqueda/resultados busca casas por población")
    @Test
    void testBuscarPorPoblacion() {
        // Arrange
        CasaRuralListadoDTO casa = new CasaRuralListadoDTO(1, "Salento", 3, 2, 1, "Casa hermosa", "Juan");
        when(busquedaCasasService.buscarCasasPorPoblacion("Salento"))
                .thenReturn(List.of(casa));

        // Act
        String vista = busquedaController.buscarPorPoblacion("Salento", model);

        // Assert
        assertEquals("busqueda/resultados-busqueda", vista);
        verify(busquedaCasasService, times(1)).buscarCasasPorPoblacion("Salento");
        verify(model, times(1)).addAttribute(eq("casas"), any(List.class));
    }

    @DisplayName("HU6-C03: GET /busqueda/resultados sin población muestra formulario con mensaje")
    @Test
    void testBuscarSinPoblacion() {
        // Act
        String vista = busquedaController.buscarPorPoblacion("", model);

        // Assert
        assertEquals("busqueda/formulario-busqueda", vista);
        verify(busquedaCasasService, never()).buscarCasasPorPoblacion(anyString());
        verify(model, times(1)).addAttribute(eq("mensaje"), anyString());
    }

    @DisplayName("HU6-C04: GET /busqueda/resultados sin resultados muestra formulario")
    @Test
    void testBuscarSinResultados() {
        // Arrange
        when(busquedaCasasService.buscarCasasPorPoblacion("NoExiste"))
                .thenReturn(new ArrayList<>());

        // Act
        String vista = busquedaController.buscarPorPoblacion("NoExiste", model);

        // Assert
        assertEquals("busqueda/formulario-busqueda", vista);
        verify(model, times(1)).addAttribute(eq("mensaje"), anyString());
    }

    @DisplayName("HU6-C05: GET /busqueda/codigo/{codigoCasa} busca por código - encontrado")
    @Test
    void testBuscarPorCodigoExiste() {
        // Arrange
        CasaRuralDetalleDTO casa = new CasaRuralDetalleDTO(1, "Salento", "Casa hermosa",
                3, 2, 1, 1, 0, "Juan", "3008881111");
        when(busquedaCasasService.buscarCasaPorCodigo(1))
                .thenReturn(Optional.of(casa));

        // Act
        String vista = busquedaController.buscarPorCodigo(1, model);

        // Assert
        assertEquals("busqueda/detalle-casa", vista);
        verify(busquedaCasasService, times(1)).buscarCasaPorCodigo(1);
    }

    @DisplayName("HU6-C06: GET /busqueda/codigo/{codigoCasa} no encontrado")
    @Test
    void testBuscarPorCodigoNoExiste() {
        // Arrange
        when(busquedaCasasService.buscarCasaPorCodigo(999))
                .thenReturn(Optional.empty());

        // Act
        String vista = busquedaController.buscarPorCodigo(999, model);

        // Assert
        assertEquals("busqueda/formulario-busqueda", vista);
        verify(model, times(1)).addAttribute(eq("mensaje"), anyString());
    }

    @DisplayName("HU6-C07: GET /busqueda/detalle/{codigoCasa} retorna detalles")
    @Test
    void testVerDetalleCasa() {
        // Arrange
        CasaRuralDetalleDTO casa = new CasaRuralDetalleDTO(1, "Salento", "Casa hermosa",
                3, 2, 1, 1, 0, "Juan", "3008881111");
        when(busquedaCasasService.obtenerDetalleCasa(1))
                .thenReturn(casa);

        // Act
        String vista = busquedaController.verDetalleCasa(1, model);

        // Assert
        assertEquals("busqueda/detalle-casa", vista);
        verify(busquedaCasasService, times(1)).obtenerDetalleCasa(1);
        verify(model, times(1)).addAttribute(eq("casa"), eq(casa));
    }

    @DisplayName("HU6-C08: GET /busqueda/detalle/{codigoCasa} maneja excepciones")
    @Test
    void testVerDetalleCasaError() {
        // Arrange
        when(busquedaCasasService.obtenerDetalleCasa(999))
                .thenThrow(new IllegalArgumentException("Casa no disponible"));

        // Act
        String vista = busquedaController.verDetalleCasa(999, model);

        // Assert
        assertEquals("busqueda/formulario-busqueda", vista);
        verify(model, times(1)).addAttribute(eq("mensaje"), anyString());
    }

    @DisplayName("HU6-C09: Búsqueda con espacios en población es normalizada")
    @Test
    void testBusquedaNormalizaPoblacion() {
        // Arrange
        when(busquedaCasasService.buscarCasasPorPoblacion("Salento"))
                .thenReturn(new ArrayList<>());

        // Act
        String vista = busquedaController.buscarPorPoblacion("  Salento  ", model);

        // Assert
        assertEquals("busqueda/formulario-busqueda", vista);
        verify(busquedaCasasService, times(1)).buscarCasasPorPoblacion("Salento");
    }
}
