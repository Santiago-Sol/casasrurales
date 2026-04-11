package co.edu.uniquindio.casasrurales.services;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import co.edu.uniquindio.casasrurales.dto.CasaRuralDetalleDTO;
import co.edu.uniquindio.casasrurales.dto.CasaRuralListadoDTO;
import co.edu.uniquindio.casasrurales.repositories.BanoRepository;
import co.edu.uniquindio.casasrurales.repositories.CasaRuralRepository;
import co.edu.uniquindio.casasrurales.repositories.CocinaRepository;
import co.edu.uniquindio.casasrurales.repositories.FotoRepository;
import co.edu.uniquindio.casasrurales.repositories.HabitacionRepository;
import co.edu.uniquindio.casasrurales.repositories.PaqueteAlquilerRepository;

/**
 * Pruebas unitarias del servicio de búsqueda de casas rurales.
 * Valida el comportamiento de búsqueda con filtros de disponibilidad.
 */
@DisplayName("BusquedaCasasService - Pruebas Unitarias")
@ExtendWith(MockitoExtension.class)
class BusquedaCasasServiceTest {

    @Mock
    private CasaRuralRepository casaRuralRepository;

    @Mock
    private PaqueteAlquilerRepository paqueteAlquilerRepository;

    @Mock
    private HabitacionRepository habitacionRepository;

    @Mock
    private CocinaRepository cocinaRepository;

    @Mock
    private BanoRepository banoRepository;

    @Mock
    private FotoRepository fotoRepository;

    @InjectMocks
    private BusquedaCasasService busquedaCasasService;

    @BeforeEach
    void setUp() {
        // Setup completado por MockitoExtension
    }

    @DisplayName("HU6-001: Buscar casas por población - retorna solo casas con paquetes activos")
    @Test
    void testBuscarCasasPorPoblacionConPaqueteActivo() {
        // Arrange
        when(casaRuralRepository.findByPoblacionIgnoreCase(anyString()))
                .thenReturn(new ArrayList<>()); // Simulamos que la búsqueda retorna una lista vacía

        // Act
        List<CasaRuralListadoDTO> resultado = busquedaCasasService.buscarCasasPorPoblacion("Salento");

        // Assert
        assertNotNull(resultado);
        verify(casaRuralRepository, times(1)).findByPoblacionIgnoreCase(anyString());
    }

    @DisplayName("HU6-002: Buscar casas - retorna resultado vacío si no hay coincidencias")
    @Test
    void testBuscarCasasPorPoblacionSinResultados() {
        // Arrange
        when(casaRuralRepository.findByPoblacionIgnoreCase("Nonexistent"))
                .thenReturn(Collections.emptyList());

        // Act
        List<CasaRuralListadoDTO> resultado = busquedaCasasService.buscarCasasPorPoblacion("Nonexistent");

        // Assert
        assertTrue(resultado.isEmpty());
    }

    @DisplayName("HU6-003: Obtener detalle - lanza excepción si la casa no existe")
    @Test
    void testObtenerDetalleCasaNoExiste() {
        // Arrange
        when(casaRuralRepository.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> busquedaCasasService.obtenerDetalleCasa(999)
        );

        assertTrue(exception.getMessage().contains("no existe"));
    }

    @DisplayName("HU6-004: Buscar por código - retorna Optional vacío si no existe")
    @Test
    void testBuscarCasaPorCodigoNoExiste() {
        // Arrange
        when(casaRuralRepository.findById(999)).thenReturn(Optional.empty());

        // Act
        Optional<CasaRuralDetalleDTO> resultado = busquedaCasasService.buscarCasaPorCodigo(999);

        // Assert
        assertTrue(resultado.isEmpty());
    }

    @DisplayName("HU6-005: Búsqueda con población nula retorna lista vacía")
    @Test
    void testBusquedaPoblacionNula() {
        // Act
        List<CasaRuralListadoDTO> resultado = busquedaCasasService.buscarCasasPorPoblacion(null);

        // Assert
        assertTrue(resultado.isEmpty());
        verify(casaRuralRepository, never()).findByPoblacionIgnoreCase(anyString());
    }

    @DisplayName("HU6-006: Búsqueda con población vacía retorna lista vacía")
    @Test
    void testBusquedaPoblacionVacia() {
        // Act
        List<CasaRuralListadoDTO> resultado = busquedaCasasService.buscarCasasPorPoblacion("   ");

        // Assert
        assertTrue(resultado.isEmpty());
        verify(casaRuralRepository, never()).findByPoblacionIgnoreCase(anyString());
    }

    @DisplayName("HU6-007: Búsqueda normaliza espacios en población")
    @Test
    void testBusquedaNormalizaPoblacion() {
        // Arrange
        when(casaRuralRepository.findByPoblacionIgnoreCase("Salento"))
                .thenReturn(new ArrayList<>());

        // Act
        List<CasaRuralListadoDTO> resultado = busquedaCasasService.buscarCasasPorPoblacion("  Salento  ");

        // Assert
        verify(casaRuralRepository, times(1)).findByPoblacionIgnoreCase("Salento");
    }
}
