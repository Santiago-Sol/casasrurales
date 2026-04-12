package co.edu.uniquindio.casasrurales.controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import co.edu.uniquindio.casasrurales.dto.CasaRuralDetalleDTO;
import co.edu.uniquindio.casasrurales.dto.CasaRuralListadoDTO;
import co.edu.uniquindio.casasrurales.services.BusquedaCasasService;

/**
 * Pruebas unitarias del controlador REST de búsqueda de casas.
 * Valida los endpoints y las respuestas JSON sin necesidad de base de datos real.
 */
@DisplayName("BusquedaController - Pruebas Unitarias")
class BusquedaControllerTest {

    private BusquedaController busquedaController;
    private BusquedaCasasService busquedaCasasService;

    @BeforeEach
    void setUp() {
        busquedaCasasService = mock(BusquedaCasasService.class);
        busquedaController = new BusquedaController(busquedaCasasService);
    }

    @DisplayName("HU6-C01: GET /api/busqueda/por-poblacion retorna lista de casas")
    @Test
    void testBuscarPorPoblacion_OK() {
        // Arrange
        CasaRuralListadoDTO casa = new CasaRuralListadoDTO(1, "Salento", 3, 2, 1, "Casa hermosa", "Juan");
        when(busquedaCasasService.buscarCasasPorPoblacion("Salento"))
                .thenReturn(List.of(casa));

        // Act
        ResponseEntity<List<CasaRuralListadoDTO>> respuesta = busquedaController.buscarPorPoblacion("Salento");

        // Assert
        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertNotNull(respuesta.getBody());
        assertEquals(1, respuesta.getBody().size());
        verify(busquedaCasasService, times(1)).buscarCasasPorPoblacion("Salento");
    }

    @DisplayName("HU6-C02: GET /api/busqueda/por-poblacion sin resultados retorna NO_CONTENT")
    @Test
    void testBuscarPorPoblacion_SinResultados() {
        // Arrange
        when(busquedaCasasService.buscarCasasPorPoblacion("NoExiste"))
                .thenReturn(new ArrayList<>());

        // Act
        ResponseEntity<List<CasaRuralListadoDTO>> respuesta = busquedaController.buscarPorPoblacion("NoExiste");

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, respuesta.getStatusCode());
        verify(busquedaCasasService, times(1)).buscarCasasPorPoblacion("NoExiste");
    }

    @DisplayName("HU6-C03: GET /api/busqueda/por-poblacion población vacía retorna BAD_REQUEST")
    @Test
    void testBuscarPorPoblacion_VaciaBadRequest() {
        // Act
        ResponseEntity<List<CasaRuralListadoDTO>> respuesta = busquedaController.buscarPorPoblacion("");

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, respuesta.getStatusCode());
        verify(busquedaCasasService, never()).buscarCasasPorPoblacion(anyString());
    }

    @DisplayName("HU6-C04: GET /api/busqueda/por-poblacion población null retorna BAD_REQUEST")
    @Test
    void testBuscarPorPoblacion_NullBadRequest() {
        // Act
        ResponseEntity<List<CasaRuralListadoDTO>> respuesta = busquedaController.buscarPorPoblacion(null);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, respuesta.getStatusCode());
        verify(busquedaCasasService, never()).buscarCasasPorPoblacion(anyString());
    }

    @DisplayName("HU7-C01: GET /api/busqueda/{codigoCasa} retorna detalles - encontrado")
    @Test
    void testBuscarPorCodigo_Encontrado() {
        // Arrange
        CasaRuralDetalleDTO casa = new CasaRuralDetalleDTO(1, "Salento", "Casa hermosa",
                3, 2, 1, 1, 0, "Juan", "3008881111");
        when(busquedaCasasService.buscarCasaPorCodigo(1))
                .thenReturn(Optional.of(casa));

        // Act
        ResponseEntity<CasaRuralDetalleDTO> respuesta = busquedaController.buscarPorCodigo(1);

        // Assert
        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertNotNull(respuesta.getBody());
        assertEquals(1, respuesta.getBody().getCodigoCasa());
        verify(busquedaCasasService, times(1)).buscarCasaPorCodigo(1);
    }

    @DisplayName("HU7-C02: GET /api/busqueda/{codigoCasa} no encuentra casa retorna NOT_FOUND")
    @Test
    void testBuscarPorCodigo_NoEncontrado() {
        // Arrange
        when(busquedaCasasService.buscarCasaPorCodigo(999))
                .thenReturn(Optional.empty());

        // Act
        ResponseEntity<CasaRuralDetalleDTO> respuesta = busquedaController.buscarPorCodigo(999);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, respuesta.getStatusCode());
        verify(busquedaCasasService, times(1)).buscarCasaPorCodigo(999);
    }

    @DisplayName("HU7-C03: GET /api/busqueda/codigo/buscar busca por parámetro query - OK")
    @Test
    void testBuscarDetallePorCodigo_OK() {
        // Arrange
        CasaRuralDetalleDTO casa = new CasaRuralDetalleDTO(5, "Armenia", "Casa moderna",
                4, 3, 2, 2, 100000, "Maria", "3001234567");
        when(busquedaCasasService.buscarCasaPorCodigo(5))
                .thenReturn(Optional.of(casa));

        // Act
        ResponseEntity<CasaRuralDetalleDTO> respuesta = busquedaController.buscarDetallePorCodigo("5");

        // Assert
        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertNotNull(respuesta.getBody());
        assertEquals(5, respuesta.getBody().getCodigoCasa());
        verify(busquedaCasasService, times(1)).buscarCasaPorCodigo(5);
    }

    @DisplayName("HU7-C04: GET /api/busqueda/codigo/buscar código no válido retorna BAD_REQUEST")
    @Test
    void testBuscarDetallePorCodigo_NovalidoFormat() {
        // Act
        ResponseEntity<CasaRuralDetalleDTO> respuesta = busquedaController.buscarDetallePorCodigo("abc");

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, respuesta.getStatusCode());
        verify(busquedaCasasService, never()).buscarCasaPorCodigo(anyInt());
    }

    @DisplayName("HU7-C05: GET /api/busqueda/codigo/buscar código vacío retorna BAD_REQUEST")
    @Test
    void testBuscarDetallePorCodigo_Vacio() {
        // Act
        ResponseEntity<CasaRuralDetalleDTO> respuesta = busquedaController.buscarDetallePorCodigo("");

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, respuesta.getStatusCode());
        verify(busquedaCasasService, never()).buscarCasaPorCodigo(anyInt());
    }

    @DisplayName("HU7-C06: GET /api/busqueda/codigo/buscar código null retorna BAD_REQUEST")
    @Test
    void testBuscarDetallePorCodigo_Null() {
        // Act
        ResponseEntity<CasaRuralDetalleDTO> respuesta = busquedaController.buscarDetallePorCodigo(null);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, respuesta.getStatusCode());
        verify(busquedaCasasService, never()).buscarCasaPorCodigo(anyInt());
    }

    @DisplayName("HU6-C07: Búsqueda por población normaliza espacios")
    @Test
    void testBuscarPorPoblacion_NormalizaEspacios() {
        // Arrange
        CasaRuralListadoDTO casa = new CasaRuralListadoDTO(2, "Filandia", 2, 1, 0, "Casa rústica", "Pedro");
        when(busquedaCasasService.buscarCasasPorPoblacion("Filandia"))
                .thenReturn(List.of(casa));

        // Act
        ResponseEntity<List<CasaRuralListadoDTO>> respuesta = busquedaController.buscarPorPoblacion("  Filandia  ");

        // Assert
        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        verify(busquedaCasasService, times(1)).buscarCasasPorPoblacion("Filandia");
    }

    @DisplayName("HU7-C08: Búsqueda por código con espacios es normalizada")
    @Test
    void testBuscarDetallePorCodigo_NormalizaEspacios() {
        // Arrange
        CasaRuralDetalleDTO casa = new CasaRuralDetalleDTO(3, "Circasia", "Casa acogedora",
                2, 2, 1, 1, 50000, "Carlos", "3009876543");
        when(busquedaCasasService.buscarCasaPorCodigo(3))
                .thenReturn(Optional.of(casa));

        // Act
        ResponseEntity<CasaRuralDetalleDTO> respuesta = busquedaController.buscarDetallePorCodigo("  3  ");

        // Assert
        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        verify(busquedaCasasService, times(1)).buscarCasaPorCodigo(3);
    }
}
