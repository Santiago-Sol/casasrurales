package co.edu.uniquindio.casasrurales.services;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
import co.edu.uniquindio.casasrurales.entities.CasaRural;
import co.edu.uniquindio.casasrurales.entities.PaqueteAlquiler;
import co.edu.uniquindio.casasrurales.entities.Propietario;
import co.edu.uniquindio.casasrurales.enums.ModalidadAlquiler;
import co.edu.uniquindio.casasrurales.repositories.BanoRepository;
import co.edu.uniquindio.casasrurales.repositories.CasaRuralRepository;
import co.edu.uniquindio.casasrurales.repositories.CocinaRepository;
import co.edu.uniquindio.casasrurales.repositories.FotoRepository;
import co.edu.uniquindio.casasrurales.repositories.HabitacionRepository;

@DisplayName("BusquedaCasasService - Pruebas Unitarias")
@ExtendWith(MockitoExtension.class)
class BusquedaCasasServiceTest {

    @Mock
    private CasaRuralRepository casaRuralRepository;

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

    @DisplayName("HU6-001: Buscar casas por poblacion retorna solo casas activas con paquetes disponibles")
    @Test
    void testBuscarCasasPorPoblacionConPaqueteActivo() {
        Propietario propietario = new Propietario("3001234567", "dueno", "secret123", "123456");

        CasaRural casaDisponible = new CasaRural(1, "Salento", "La Montanita", "Disponible", 1, 1, true);
        casaDisponible.setPropietario(propietario);
        casaDisponible.agregarPaqueteAlquiler(new PaqueteAlquiler(
                java.sql.Date.valueOf("2026-06-01"),
                java.sql.Date.valueOf("2026-06-05"),
                ModalidadAlquiler.CASA_ENTERA,
                450000,
                0,
                true
        ));

        CasaRural casaSinDisponibilidad = new CasaRural(2, "Salento", "Sin paquete", "No disponible", 1, 1, true);
        casaSinDisponibilidad.setPropietario(propietario);

        when(casaRuralRepository.findByPoblacionIgnoreCase(anyString()))
                .thenReturn(List.of(casaDisponible, casaSinDisponibilidad));

        List<CasaRuralListadoDTO> resultado = busquedaCasasService.buscarCasasPorPoblacion("Salento");

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals(1, resultado.get(0).getCodigoCasa());
        verify(casaRuralRepository, times(1)).findByPoblacionIgnoreCase(anyString());
    }

    @DisplayName("HU6-002: Buscar casas retorna resultado vacio si no hay coincidencias")
    @Test
    void testBuscarCasasPorPoblacionSinResultados() {
        when(casaRuralRepository.findByPoblacionIgnoreCase("Nonexistent"))
                .thenReturn(Collections.emptyList());

        List<CasaRuralListadoDTO> resultado = busquedaCasasService.buscarCasasPorPoblacion("Nonexistent");

        assertTrue(resultado.isEmpty());
    }

    @DisplayName("HU6-003: Obtener detalle lanza excepcion si la casa no existe")
    @Test
    void testObtenerDetalleCasaNoExiste() {
        when(casaRuralRepository.findById(999)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> busquedaCasasService.obtenerDetalleCasa(999)
        );

        assertTrue(exception.getMessage().contains("no existe"));
    }

    @DisplayName("HU6-004: Buscar por codigo retorna Optional vacio si no existe")
    @Test
    void testBuscarCasaPorCodigoNoExiste() {
        when(casaRuralRepository.findById(999)).thenReturn(Optional.empty());

        Optional<CasaRuralDetalleDTO> resultado = busquedaCasasService.buscarCasaPorCodigo(999);

        assertTrue(resultado.isEmpty());
    }

    @DisplayName("HU6-005: Busqueda con poblacion nula retorna lista vacia")
    @Test
    void testBusquedaPoblacionNula() {
        List<CasaRuralListadoDTO> resultado = busquedaCasasService.buscarCasasPorPoblacion(null);

        assertTrue(resultado.isEmpty());
        verify(casaRuralRepository, never()).findByPoblacionIgnoreCase(anyString());
    }

    @DisplayName("HU6-006: Busqueda con poblacion vacia retorna lista vacia")
    @Test
    void testBusquedaPoblacionVacia() {
        List<CasaRuralListadoDTO> resultado = busquedaCasasService.buscarCasasPorPoblacion("   ");

        assertTrue(resultado.isEmpty());
        verify(casaRuralRepository, never()).findByPoblacionIgnoreCase(anyString());
    }

    @DisplayName("HU6-007: Busqueda normaliza espacios en poblacion")
    @Test
    void testBusquedaNormalizaPoblacion() {
        when(casaRuralRepository.findByPoblacionIgnoreCase("Salento"))
                .thenReturn(Collections.emptyList());

        busquedaCasasService.buscarCasasPorPoblacion("  Salento  ");

        verify(casaRuralRepository, times(1)).findByPoblacionIgnoreCase("Salento");
    }
}
