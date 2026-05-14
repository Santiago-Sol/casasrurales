package co.edu.uniquindio.casasrurales.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.edu.uniquindio.casasrurales.dto.CasaRuralFormDTO;
import co.edu.uniquindio.casasrurales.dto.PagoRegistroDTO;
import co.edu.uniquindio.casasrurales.entities.CasaRural;
import co.edu.uniquindio.casasrurales.entities.Pago;
import co.edu.uniquindio.casasrurales.entities.Propietario;
import co.edu.uniquindio.casasrurales.entities.Reserva;
import co.edu.uniquindio.casasrurales.enums.EstadoReserva;
import co.edu.uniquindio.casasrurales.repositories.CasaRuralRepository;
import co.edu.uniquindio.casasrurales.repositories.PropietarioRepository;
import co.edu.uniquindio.casasrurales.repositories.ReservaRepository;
import co.edu.uniquindio.casasrurales.repositories.PaqueteAlquilerRepository;
import co.edu.uniquindio.casasrurales.repositories.PagoRepository;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;

class PropietarioServiceTest {

    private PropietarioRepository propietarioRepository;
    private CasaRuralRepository casaRuralRepository;
    private ReservaRepository reservaRepository;
    private PaqueteAlquilerRepository paqueteAlquilerRepository;
    private PagoRepository pagoRepository;
    private PropietarioService propietarioService;

    @BeforeEach
    void setUp() {
        propietarioRepository = mock(PropietarioRepository.class);
        casaRuralRepository = mock(CasaRuralRepository.class);
        reservaRepository = mock(ReservaRepository.class);
        paqueteAlquilerRepository = mock(PaqueteAlquilerRepository.class);
        pagoRepository = mock(PagoRepository.class);
        propietarioService = new PropietarioService(propietarioRepository, casaRuralRepository, reservaRepository, paqueteAlquilerRepository, pagoRepository);
    }

    @Test
    @DisplayName("crearCasa registra una casa activa para el propietario")
    void crearCasaExitosamente() {
        Propietario propietario = new Propietario("3001234567", "dueno", "secret123", "123456");
        propietario.setIdUsuario(8);

        CasaRuralFormDTO form = new CasaRuralFormDTO();
        form.setCodigoCasa(15);
        form.setNombrePropiedad("La Montanita");
        form.setPoblacion("Salento");
        form.setDescripcion("Cabana familiar");
        form.setNumComedores(2);
        form.setNumPlazasGaraje(3);
        form.setNumHabitaciones(3);
        form.setNumBanos(2);
        form.setNumCocinas(1);

        when(propietarioRepository.findById(8)).thenReturn(Optional.of(propietario));
        when(casaRuralRepository.existsById(15)).thenReturn(false);
        when(propietarioRepository.save(any(Propietario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        String respuesta = propietarioService.crearCasa(form, 8);

        assertEquals("Casa registrada exitosamente con codigo 15", respuesta);
        assertEquals(1, propietario.getCasas().size());
        CasaRural casa = propietario.getCasas().getFirst();
        assertEquals("La Montanita", casa.getNombrePropiedad());
        assertEquals("Salento", casa.getPoblacion());
        assertTrue(casa.isActiva());
        assertEquals(3, casa.getNumDormitorios());
        assertEquals(2, casa.getNumBanos());
        assertEquals(1, casa.getNumCocinas());
        verify(propietarioRepository, times(1)).save(any(Propietario.class));
    }

    @Test
    @DisplayName("crearCasa rechaza cuando no cumple los minimos de habitaciones, banos o cocinas")
    void crearCasaRechazaMinimosInvalidos() {
        Propietario propietario = new Propietario("3001234567", "dueno", "secret123", "123456");
        propietario.setIdUsuario(8);

        CasaRuralFormDTO form = new CasaRuralFormDTO();
        form.setCodigoCasa(15);
        form.setNombrePropiedad("La Montanita");
        form.setPoblacion("Salento");
        form.setDescripcion("Cabana familiar");
        form.setNumComedores(2);
        form.setNumPlazasGaraje(3);
        form.setNumHabitaciones(2);
        form.setNumBanos(0);
        form.setNumCocinas(0);

        when(propietarioRepository.findById(8)).thenReturn(Optional.of(propietario));
        when(casaRuralRepository.existsById(15)).thenReturn(false);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> propietarioService.crearCasa(form, 8));

        assertEquals("La casa debe tener minimo 3 habitaciones", ex.getMessage());
        verify(propietarioRepository, never()).save(any(Propietario.class));
    }

    @Test
    @DisplayName("editarCasa actualiza la informacion basica de la casa")
    void editarCasaExitosamente() {
        Propietario propietario = new Propietario("3001234567", "dueno", "secret123", "123456");
        propietario.setIdUsuario(8);

        CasaRural casa = new CasaRural(15, "Salento", "La Montanita", "Cabana familiar", 1, 1, true);
        casa.setPropietario(propietario);
        propietario.darAltaCasa(casa);

        CasaRuralFormDTO form = new CasaRuralFormDTO();
        form.setCodigoCasa(15);
        form.setNombrePropiedad("La Montanita Renovada");
        form.setPoblacion("Filandia");
        form.setDescripcion("Descripcion nueva");
        form.setNumComedores(4);
        form.setNumPlazasGaraje(2);

        when(casaRuralRepository.findById(15)).thenReturn(Optional.of(casa));
        when(casaRuralRepository.save(any(CasaRural.class))).thenAnswer(invocation -> invocation.getArgument(0));

        String respuesta = propietarioService.editarCasa(15, form, 8);

        assertEquals("Casa actualizada exitosamente", respuesta);
        assertEquals("La Montanita Renovada", casa.getNombrePropiedad());
        assertEquals("Filandia", casa.getPoblacion());
        assertEquals("Descripcion nueva", casa.getDescripcionGeneral());
        assertEquals(4, casa.getNumComedores());
        assertEquals(2, casa.getNumPlazasGaraje());
    }

    @Test
    @DisplayName("editarCasa rechaza cuando el propietario no es el dueno")
    void editarCasaRechazaPropietarioIncorrecto() {
        Propietario propietarioCasa = new Propietario("3001234567", "dueno", "secret123", "123456");
        propietarioCasa.setIdUsuario(3);

        CasaRural casa = new CasaRural(15, "Salento", "La Montanita", "Cabana familiar", 1, 1, true);
        casa.setPropietario(propietarioCasa);

        CasaRuralFormDTO form = new CasaRuralFormDTO();
        form.setCodigoCasa(15);
        form.setNombrePropiedad("Cambio");
        form.setPoblacion("Filandia");
        form.setDescripcion("Descripcion nueva");
        form.setNumComedores(4);
        form.setNumPlazasGaraje(2);

        when(casaRuralRepository.findById(15)).thenReturn(Optional.of(casa));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> propietarioService.editarCasa(15, form, 8));

        assertEquals("No tienes permiso para editar esta casa", ex.getMessage());
        verify(casaRuralRepository, never()).save(any(CasaRural.class));
    }

    @Test
    @DisplayName("obtenerCasasPropietario devuelve la lista del propietario")
    void obtenerCasasPropietarioExitosamente() {
        Propietario propietario = new Propietario("3001234567", "dueno", "secret123", "123456");
        propietario.setIdUsuario(8);
        CasaRural casa = new CasaRural(15, "Salento", "La Montanita", "Cabana familiar", 1, 1, true);
        casa.setPropietario(propietario);
        propietario.darAltaCasa(casa);

        when(propietarioRepository.findById(8)).thenReturn(Optional.of(propietario));
        when(reservaRepository.findByCasaRuralCodigoCasa(15)).thenReturn(List.of());

        List<?> resultado = propietarioService.obtenerCasasPropietario(8);

        assertFalse(resultado.isEmpty());
    }

    @Test
    @DisplayName("registrarPagoReserva registra pago y confirma reserva del propietario")
    void registrarPagoReservaExitosamente() {
        Propietario propietario = new Propietario("3001234567", "dueno", "secret123", "123456");
        propietario.setIdUsuario(8);
        CasaRural casa = new CasaRural(15, "Salento", "La Montanita", "Cabana familiar", 1, 1, true);
        casa.setPropietario(propietario);

        Reserva reserva = mock(Reserva.class);
        when(reserva.getNumeroReserva()).thenReturn(99);
        when(reserva.getCasaRural()).thenReturn(casa);
        when(reserva.getEstado()).thenReturn(EstadoReserva.PENDIENTE_PAGO);

        PagoRegistroDTO dto = new PagoRegistroDTO();
        dto.setMonto(120000.0);

        when(reservaRepository.findById(99)).thenReturn(Optional.of(reserva));

        propietarioService.registrarPagoReserva(99, 8, dto);

        verify(reserva).agregarPago(any(Pago.class));
        verify(reserva).confirmar();
        verify(pagoRepository).save(any(Pago.class));
        verify(reservaRepository).save(reserva);
    }

    @Test
    @DisplayName("obtenerReservasVencidas devuelve solo reservas vencidas del propietario")
    void obtenerReservasVencidasExitosamente() {
        Propietario propietario = new Propietario("3001234567", "dueno", "secret123", "123456");
        propietario.setIdUsuario(8);
        CasaRural casa = new CasaRural(15, "Salento", "La Montanita", "Cabana familiar", 1, 1, true);
        casa.setPropietario(propietario);

        Reserva vencida = mock(Reserva.class);
        when(vencida.getCasaRural()).thenReturn(casa);
        when(vencida.estaVencida()).thenReturn(true);

        Reserva vigente = mock(Reserva.class);
        when(vigente.getCasaRural()).thenReturn(casa);
        when(vigente.estaVencida()).thenReturn(false);

        when(propietarioRepository.findById(8)).thenReturn(Optional.of(propietario));
        when(reservaRepository.findAll()).thenReturn(List.of(vencida, vigente));

        List<?> resultado = propietarioService.obtenerReservasVencidas(8);

        assertEquals(1, resultado.size());
    }

    @Test
    @DisplayName("anularReservaVencida cancela reserva vencida del propietario")
    void anularReservaVencidaExitosamente() {
        Propietario propietario = new Propietario("3001234567", "dueno", "secret123", "123456");
        propietario.setIdUsuario(8);
        CasaRural casa = new CasaRural(15, "Salento", "La Montanita", "Cabana familiar", 1, 1, true);
        casa.setPropietario(propietario);

        Reserva reserva = mock(Reserva.class);
        when(reserva.getCasaRural()).thenReturn(casa);
        when(reserva.estaVencida()).thenReturn(true);
        when(reservaRepository.findById(99)).thenReturn(Optional.of(reserva));

        propietarioService.anularReservaVencida(99, 8);

        verify(reserva).cancelar();
        verify(reservaRepository).save(reserva);
    }
}
