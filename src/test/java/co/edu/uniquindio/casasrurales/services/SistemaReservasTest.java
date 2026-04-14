package co.edu.uniquindio.casasrurales.services;

import co.edu.uniquindio.casasrurales.entities.CasaRural;
import co.edu.uniquindio.casasrurales.entities.Cliente;
import co.edu.uniquindio.casasrurales.entities.Reserva;
import co.edu.uniquindio.casasrurales.enums.EstadoReserva;
import co.edu.uniquindio.casasrurales.repositories.CasaRuralRepository;
import co.edu.uniquindio.casasrurales.repositories.PropietarioRepository;
import co.edu.uniquindio.casasrurales.repositories.ReservaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias del servicio SistemaReservas para la HU9 - Realizacion de Reserva.
 * Valida la logica de validacion, disponibilidad y creacion de reservas.
 */
@DisplayName("SistemaReservas - Pruebas Unitarias HU9")
class SistemaReservasTest {

    private SistemaReservas sistemaReservas;
    private CasaRuralRepository casaRuralRepository;
    private ReservaRepository reservaRepository;
    private PropietarioRepository propietarioRepository;

    private CasaRural casaValida;
    private Cliente clienteValido;

    @BeforeEach
    void setUp() {
        casaRuralRepository = mock(CasaRuralRepository.class);
        reservaRepository = mock(ReservaRepository.class);
        propietarioRepository = mock(PropietarioRepository.class);

        sistemaReservas = new SistemaReservas(propietarioRepository, casaRuralRepository, reservaRepository);

        casaValida = mock(CasaRural.class);
        when(casaValida.getCodigoCasa()).thenReturn(1);
        when(casaValida.getPoblacion()).thenReturn("Armenia");
        when(casaValida.isActiva()).thenReturn(true);
        when(casaValida.esValida()).thenReturn(true);
        when(casaValida.consultarDisponibilidad(any(Date.class), anyInt())).thenReturn("LIBRE");

        clienteValido = mock(Cliente.class);

        when(casaRuralRepository.findById(1)).thenReturn(Optional.of(casaValida));
        when(reservaRepository.save(any(Reserva.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private Date fechaFutura(int diasDesdeHoy) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, diasDesdeHoy);
        return cal.getTime();
    }

    // ─── TESTS DE RESERVA EXITOSA ───────────────────────────────────────────

    @Test
    @DisplayName("HU9-C01: Reserva exitosa retorna entidad con estado PENDIENTE_PAGO")
    void testRealizarReserva_Exitosa() {
        Reserva reserva = sistemaReservas.realizarReserva(
                1, clienteValido, fechaFutura(5), 3, List.of(), 300000
        );

        assertNotNull(reserva);
        assertEquals(EstadoReserva.PENDIENTE_PAGO, reserva.getEstado());
        verify(reservaRepository, times(1)).save(any(Reserva.class));
    }

    @Test
    @DisplayName("HU9-C02: El anticipo calculado es el 20% del importe total")
    void testRealizarReserva_AnticipoCalculado() {
        Reserva reserva = sistemaReservas.realizarReserva(
                1, clienteValido, fechaFutura(5), 3, List.of(), 500000
        );

        assertEquals(100000.0, reserva.calcularAnticipo(), 0.01);
    }

    // ─── TESTS DE VALIDACION DE CASA ────────────────────────────────────────

    @Test
    @DisplayName("HU9-C03: Casa inexistente lanza NullPointerException")
    void testRealizarReserva_CasaNoExiste() {
        when(casaRuralRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(NullPointerException.class, () ->
                sistemaReservas.realizarReserva(99, clienteValido, fechaFutura(5), 3, List.of(), 200000)
        );
    }

    @Test
    @DisplayName("HU9-C04: Casa inactiva lanza IllegalStateException")
    void testRealizarReserva_CasaInactiva() {
        when(casaValida.isActiva()).thenReturn(false);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                sistemaReservas.realizarReserva(1, clienteValido, fechaFutura(5), 3, List.of(), 200000)
        );

        assertEquals("La casa no esta activa y no puede ser reservada", ex.getMessage());
    }

    @Test
    @DisplayName("HU9-C05: Casa invalida (sin requisitos minimos) lanza IllegalStateException")
    void testRealizarReserva_CasaNoValida() {
        when(casaValida.esValida()).thenReturn(false);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                sistemaReservas.realizarReserva(1, clienteValido, fechaFutura(5), 3, List.of(), 200000)
        );

        assertEquals("La casa no cumple los requisitos minimos para ser reservada", ex.getMessage());
    }

    // ─── TESTS DE VALIDACION DE FECHAS ──────────────────────────────────────

    @Test
    @DisplayName("HU9-C06: Fecha de entrada en el pasado lanza IllegalArgumentException")
    void testRealizarReserva_FechaPasado() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -1);
        Date ayer = cal.getTime();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                sistemaReservas.realizarReserva(1, clienteValido, ayer, 3, List.of(), 200000)
        );

        assertEquals("La fecha de entrada no puede ser en el pasado", ex.getMessage());
    }

    @Test
    @DisplayName("HU9-C07: Numero de noches menor a 1 lanza IllegalArgumentException")
    void testRealizarReserva_NochesInvalidas() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                sistemaReservas.realizarReserva(1, clienteValido, fechaFutura(5), 0, List.of(), 200000)
        );

        assertEquals("El numero de noches debe ser al menos 1", ex.getMessage());
    }

    // ─── TESTS DE VALIDACION DE IMPORTE ─────────────────────────────────────

    @Test
    @DisplayName("HU9-C08: Importe cero lanza IllegalArgumentException")
    void testRealizarReserva_ImporteCero() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                sistemaReservas.realizarReserva(1, clienteValido, fechaFutura(5), 3, List.of(), 0)
        );

        assertEquals("El importe total debe ser mayor a cero", ex.getMessage());
    }

    @Test
    @DisplayName("HU9-C09: Importe negativo lanza IllegalArgumentException")
    void testRealizarReserva_ImporteNegativo() {
        assertThrows(IllegalArgumentException.class, () ->
                sistemaReservas.realizarReserva(1, clienteValido, fechaFutura(5), 3, List.of(), -100)
        );
    }

    // ─── TESTS DE DISPONIBILIDAD ─────────────────────────────────────────────

    @Test
    @DisplayName("HU9-C10: Casa ya reservada lanza IllegalStateException con mensaje claro")
    void testRealizarReserva_CasaReservada() {
        when(casaValida.consultarDisponibilidad(any(Date.class), anyInt())).thenReturn("RESERVADA");

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                sistemaReservas.realizarReserva(1, clienteValido, fechaFutura(5), 3, List.of(), 200000)
        );

        assertEquals("La casa ya tiene una reserva para las fechas solicitadas", ex.getMessage());
    }

    @Test
    @DisplayName("HU9-C11: Casa no disponible lanza IllegalStateException")
    void testRealizarReserva_CasaNoDisponible() {
        when(casaValida.consultarDisponibilidad(any(Date.class), anyInt())).thenReturn("NO_DISPONIBLE");

        assertThrows(IllegalStateException.class, () ->
                sistemaReservas.realizarReserva(1, clienteValido, fechaFutura(5), 3, List.of(), 200000)
        );
    }

    // ─── TESTS DE CONSULTAS ───────────────────────────────────────────────────

    @Test
    @DisplayName("HU9-C12: buscarReservaPorNumero retorna null si no existe")
    void testBuscarReservaPorNumero_NoExiste() {
        when(reservaRepository.findById(999)).thenReturn(Optional.empty());
        assertNull(sistemaReservas.buscarReservaPorNumero(999));
    }

    @Test
    @DisplayName("HU9-C13: getReservasPorCliente delega correctamente al repositorio")
    void testGetReservasPorCliente() {
        when(reservaRepository.findByClienteIdUsuario(5)).thenReturn(List.of());
        List<Reserva> resultado = sistemaReservas.getReservasPorCliente(5);
        assertNotNull(resultado);
        verify(reservaRepository, times(1)).findByClienteIdUsuario(5);
    }
}
