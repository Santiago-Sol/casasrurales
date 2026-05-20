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
import co.edu.uniquindio.casasrurales.entities.Bano;
import co.edu.uniquindio.casasrurales.entities.CasaRural;
import co.edu.uniquindio.casasrurales.entities.Cocina;
import co.edu.uniquindio.casasrurales.entities.Habitacion;
import co.edu.uniquindio.casasrurales.entities.PaqueteAlquiler;
import co.edu.uniquindio.casasrurales.entities.Propietario;
import co.edu.uniquindio.casasrurales.enums.ModalidadAlquiler;
import co.edu.uniquindio.casasrurales.enums.TipoCama;
import co.edu.uniquindio.casasrurales.repositories.BanoRepository;
import co.edu.uniquindio.casasrurales.repositories.CasaRuralRepository;
import co.edu.uniquindio.casasrurales.repositories.CocinaRepository;
import co.edu.uniquindio.casasrurales.repositories.FotoRepository;
import co.edu.uniquindio.casasrurales.repositories.HabitacionRepository;
import co.edu.uniquindio.casasrurales.repositories.ValoracionRepository;
import co.edu.uniquindio.casasrurales.repositories.CuentaRepository;

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

    @Mock
    private SistemaReservas sistemaReservas;

    @Mock
    private ValoracionRepository valoracionRepository;

    @Mock
    private CuentaRepository cuentaRepository;

    @InjectMocks
    private BusquedaCasasService busquedaCasasService;

    @DisplayName("HU6-001/RN111: Buscar casas por poblacion retorna solo activas con paquetes disponibles")
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

        CasaRural casaDadaDeBaja = new CasaRural(3, "Salento", "Dada de baja", "No reservable", 1, 1, false);
        casaDadaDeBaja.setPropietario(propietario);
        casaDadaDeBaja.agregarPaqueteAlquiler(new PaqueteAlquiler(
                java.sql.Date.valueOf("2026-06-01"),
                java.sql.Date.valueOf("2026-06-05"),
                ModalidadAlquiler.CASA_ENTERA,
                450000,
                0,
                true
        ));

        when(casaRuralRepository.findByPoblacionIgnoreCase(anyString()))
                .thenReturn(List.of(casaDisponible, casaSinDisponibilidad, casaDadaDeBaja));

        List<CasaRuralListadoDTO> resultado = busquedaCasasService.buscarCasasPorPoblacion("Salento");

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals(1, resultado.get(0).getCodigoCasa());
        verify(casaRuralRepository, times(1)).findByPoblacionIgnoreCase(anyString());
    }

    @DisplayName("Inicio: listar disponibles retorna casas activas con paquetes")
    @Test
    void buscarCasasDisponiblesSinFiltros() {
        Propietario propietario = new Propietario("3001234567", "dueno", "secret123", "123456");

        CasaRural casaDisponible = new CasaRural(1, "Armenia", "Casa Verde", "Disponible", 1, 1, true);
        casaDisponible.setPropietario(propietario);
        casaDisponible.agregarHabitacion(new Habitacion("HAB-1", 2, TipoCama.DOBLE, true));
        casaDisponible.agregarPaqueteAlquiler(new PaqueteAlquiler(
                java.sql.Date.valueOf("2026-06-01"),
                java.sql.Date.valueOf("2026-06-05"),
                ModalidadAlquiler.AMBAS,
                450000,
                90000,
                true
        ));

        CasaRural casaSinPaquete = new CasaRural(2, "Salento", "Casa Gris", "Sin paquete", 1, 1, true);
        casaSinPaquete.setPropietario(propietario);

        when(casaRuralRepository.findByActivaTrue()).thenReturn(List.of(casaDisponible, casaSinPaquete));

        List<CasaRuralListadoDTO> resultado = busquedaCasasService.buscarCasasDisponibles(null, null, null, null);

        assertEquals(1, resultado.size());
        assertEquals(1, resultado.get(0).getCodigoCasa());
        assertEquals(2, resultado.get(0).getCapacidadHuespedes());
        verify(casaRuralRepository, times(1)).findByActivaTrue();
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

    @DisplayName("RN105-RN107: Detalle de casa incluye caracteristicas, habitaciones, banos y cocinas")
    @Test
    void obtenerDetalleCasaIncluyeCaracteristicasCompletas() {
        Propietario propietario = new Propietario("3001234567", "dueno", "secret123", "123456");
        CasaRural casa = new CasaRural(7, "Filandia", "Casa Laurel", "Vista al valle", 2, 1, true);
        casa.setPropietario(propietario);
        casa.agregarHabitacion(new Habitacion("HAB-101", 2, TipoCama.DOBLE, true));
        casa.agregarBano(new Bano("Bano social"));
        casa.agregarCocina(new Cocina(true, false));

        Habitacion habitacion = new Habitacion("HAB-101", 2, TipoCama.DOBLE, true);
        Bano bano = new Bano("Bano social");
        Cocina cocina = new Cocina(true, false);

        when(casaRuralRepository.findById(7)).thenReturn(Optional.of(casa));
        when(habitacionRepository.findByCasaRuralCodigoCasa(7)).thenReturn(List.of(habitacion));
        when(banoRepository.findByCasaRuralCodigoCasa(7)).thenReturn(List.of(bano));
        when(cocinaRepository.findByCasaRuralCodigoCasa(7)).thenReturn(List.of(cocina));
        when(fotoRepository.findByCasaRuralCodigoCasa(7)).thenReturn(List.of());
        when(valoracionRepository.findByCasaRuralCodigoCasaOrderByFechaCreacionDesc(7)).thenReturn(List.of());

        CasaRuralDetalleDTO detalle = busquedaCasasService.obtenerDetalleCasa(7);

        assertEquals(7, detalle.getCodigoCasa());
        assertEquals("Vista al valle", detalle.getDescripcionGeneral());
        assertEquals(2, detalle.getNumComedores());
        assertEquals(1, detalle.getNumPlazasGaraje());
        assertEquals("HAB-101", detalle.getHabitaciones().get(0).getCodigoHabitacion());
        assertEquals(2, detalle.getHabitaciones().get(0).getNumeroCamas());
        assertTrue(detalle.getHabitaciones().get(0).isTieneBano());
        assertEquals("Bano social", detalle.getBanos().get(0).getObservaciones());
        assertTrue(detalle.getCocinas().get(0).isTieneLavavajillas());
    }
}
