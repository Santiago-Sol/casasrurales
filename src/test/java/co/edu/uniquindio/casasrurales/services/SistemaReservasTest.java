package co.edu.uniquindio.casasrurales.services;

import co.edu.uniquindio.casasrurales.entities.CasaRural;
import co.edu.uniquindio.casasrurales.entities.Cliente;
import co.edu.uniquindio.casasrurales.entities.Habitacion;
import co.edu.uniquindio.casasrurales.entities.PaqueteAlquiler;
import co.edu.uniquindio.casasrurales.entities.Reserva;
import co.edu.uniquindio.casasrurales.enums.EstadoDisponibilidad;
import co.edu.uniquindio.casasrurales.enums.EstadoReserva;
import co.edu.uniquindio.casasrurales.enums.ModalidadAlquiler;
import co.edu.uniquindio.casasrurales.enums.TipoReserva;
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
        when(casaValida.getHabitaciones()).thenReturn(List.of());
        when(casaValida.getPaquetesAlquiler()).thenReturn(List.of(new PaqueteAlquiler(
                fechaFutura(1),
                fechaFutura(30),
                ModalidadAlquiler.AMBAS,
                500000,
                120000,
                true
        )));

        clienteValido = mock(Cliente.class);

        when(casaRuralRepository.findById(1)).thenReturn(Optional.of(casaValida));
        when(reservaRepository.findByCasaRuralCodigoCasa(1)).thenReturn(List.of());
        when(reservaRepository.save(any(Reserva.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private Date fechaFutura(int diasDesdeHoy) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, diasDesdeHoy);
        return cal.getTime();
    }

    private Date inicioDiaMas(Date fecha, int dias) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(fecha);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        cal.add(Calendar.DAY_OF_MONTH, dias);
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

    // ─── TESTS DE PRECIOS ───────────────────────────────────────────────────

    // ─── TESTS DE VALIDACION DE CASA ────────────────────────────────────────

    @Test
    @DisplayName("RN126/RN128: El importe de casa completa sale del paquete y la modalidad")
    void realizarReservaCalculaImporteCasaCompletaDesdePaquete() {
        Reserva reserva = sistemaReservas.realizarReservaCalculandoImporte(
                1, clienteValido, fechaFutura(5), 3, List.of()
        );

        assertEquals(500000.0, reserva.getImporteTotal(), 0.01);
        assertEquals(100000.0, reserva.getImporteAnticipo(), 0.01);
    }

    @Test
    @DisplayName("RN127/RN128: El importe por habitaciones usa su precio independiente")
    void realizarReservaCalculaImporteHabitacionesConPrecioIndependiente() {
        Habitacion habitacionUno = mock(Habitacion.class);
        Habitacion habitacionDos = mock(Habitacion.class);
        when(habitacionUno.getIdHabitacion()).thenReturn(1);
        when(habitacionUno.getCodigoHabitacion()).thenReturn("HAB-1");
        when(habitacionDos.getIdHabitacion()).thenReturn(2);
        when(habitacionDos.getCodigoHabitacion()).thenReturn("HAB-2");
        when(casaValida.getHabitaciones()).thenReturn(List.of(habitacionUno, habitacionDos));

        Reserva reserva = sistemaReservas.realizarReservaCalculandoImporte(
                1, clienteValido, fechaFutura(5), 3, List.of(habitacionUno, habitacionDos)
        );

        assertEquals(720000.0, reserva.getImporteTotal(), 0.01);
        assertEquals(144000.0, reserva.getImporteAnticipo(), 0.01);
    }

    @Test
    @DisplayName("RN125: Rechaza paquete sin precio definido para la modalidad")
    void realizarReservaRechazaPaqueteSinPrecioDeModalidad() {
        Habitacion habitacion = mock(Habitacion.class);
        when(habitacion.getIdHabitacion()).thenReturn(1);
        when(habitacion.getCodigoHabitacion()).thenReturn("HAB-1");
        when(casaValida.getHabitaciones()).thenReturn(List.of(habitacion));
        when(casaValida.getPaquetesAlquiler()).thenReturn(List.of(new PaqueteAlquiler(
                fechaFutura(1),
                fechaFutura(30),
                ModalidadAlquiler.POR_HABITACIONES,
                500000,
                0,
                true
        )));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                sistemaReservas.realizarReservaCalculandoImporte(
                        1, clienteValido, fechaFutura(5), 2, List.of(habitacion))
        );

        assertEquals("El precio por habitacion debe ser mayor a cero", ex.getMessage());
        verify(reservaRepository, never()).save(any(Reserva.class));
    }

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

    @Test
    @DisplayName("RN119: Numero de noches negativo lanza IllegalArgumentException")
    void realizarReservaRechazaNochesNegativas() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                sistemaReservas.realizarReserva(1, clienteValido, fechaFutura(5), -2, List.of(), 200000)
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
    @DisplayName("HU9-C10: Casa ya reservada lanza IllegalStateException")
    void testRealizarReserva_CasaReservada() {
        Reserva reservaExistente = mock(Reserva.class);
        when(reservaExistente.getEstado()).thenReturn(EstadoReserva.CONFIRMADA);
        when(reservaExistente.getFechaEntrada()).thenReturn(fechaFutura(5));
        when(reservaExistente.getNumeroNoches()).thenReturn(3);
        when(reservaExistente.getTipoReserva()).thenReturn(TipoReserva.CASA_ENTERA);
        when(reservaRepository.findByCasaRuralCodigoCasa(1)).thenReturn(List.of(reservaExistente));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                sistemaReservas.realizarReserva(1, clienteValido, fechaFutura(5), 3, List.of(), 200000)
        );

        assertEquals("La casa no esta disponible", ex.getMessage());
    }

    @Test
    @DisplayName("HU9-C11: Casa sin paquete explicito no esta disponible")
    void testRealizarReserva_CasaSinPaqueteExplicito() {
        when(casaValida.getPaquetesAlquiler()).thenReturn(List.of());

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                sistemaReservas.realizarReserva(1, clienteValido, fechaFutura(5), 3, List.of(), 200000)
        );

        assertEquals("La casa no esta disponible", ex.getMessage());
    }

    @Test
    @DisplayName("HU9-C14: Consulta detallada devuelve un dia por cada noche consultada")
    void consultarDisponibilidadDetalladaDevuelveDiasDelPeriodo() {
        var disponibilidad = sistemaReservas.consultarDisponibilidadDetallada(1, fechaFutura(5), 3);

        assertEquals(3, disponibilidad.getDias().size());
    }

    @Test
    @DisplayName("RN120-RN121: Calcula fecha de salida y periodo continuo")
    void consultarDisponibilidadCalculaSalidaYDiasConsecutivos() {
        Date fechaEntrada = fechaFutura(5);

        var disponibilidad = sistemaReservas.consultarDisponibilidadDetallada(1, fechaEntrada, 3);

        assertEquals(inicioDiaMas(fechaEntrada, 3), disponibilidad.getFechaSalida());
        assertEquals(inicioDiaMas(fechaEntrada, 0), disponibilidad.getDias().get(0).getFecha());
        assertEquals(inicioDiaMas(fechaEntrada, 1), disponibilidad.getDias().get(1).getFecha());
        assertEquals(inicioDiaMas(fechaEntrada, 2), disponibilidad.getDias().get(2).getFecha());
    }

    @Test
    @DisplayName("RN120: Reserva expone fecha de salida sumando las noches")
    void reservaCalculaFechaSalida() {
        Date fechaEntrada = fechaFutura(8);

        Reserva reserva = sistemaReservas.realizarReserva(
                1, clienteValido, fechaEntrada, 4, List.of(), 300000
        );

        assertEquals(inicioDiaMas(fechaEntrada, 4), inicioDiaMas(reserva.getFechaSalida(), 0));
    }

    @Test
    @DisplayName("HU9-C15: Sin paquete explicito todos los dias son no disponibles")
    void consultarDisponibilidadSinPaqueteEsNoDisponible() {
        when(casaValida.getPaquetesAlquiler()).thenReturn(List.of());

        var disponibilidad = sistemaReservas.consultarDisponibilidadDetallada(1, fechaFutura(5), 2);

        assertTrue(disponibilidad.getDias().stream()
                .allMatch(dia -> dia.getEstadoCasaEntera().name().equals("NO_DISPONIBLE")));
    }

    @Test
    @DisplayName("RN111: Casa dada de baja se consulta como no disponible")
    void consultarDisponibilidadCasaInactivaEsNoDisponible() {
        Habitacion habitacion = mock(Habitacion.class);
        when(habitacion.getIdHabitacion()).thenReturn(1);
        when(habitacion.getCodigoHabitacion()).thenReturn("HAB-1");
        when(casaValida.isActiva()).thenReturn(false);
        when(casaValida.getHabitaciones()).thenReturn(List.of(habitacion));

        var disponibilidad = sistemaReservas.consultarDisponibilidadDetallada(1, fechaFutura(5), 2);

        assertTrue(disponibilidad.getDias().stream()
                .allMatch(dia -> dia.getEstadoCasaEntera() == EstadoDisponibilidad.NO_DISPONIBLE));
        assertTrue(disponibilidad.getDias().stream()
                .flatMap(dia -> dia.getHabitaciones().stream())
                .allMatch(habitacionDia -> habitacionDia.getEstado() == EstadoDisponibilidad.NO_DISPONIBLE));
    }

    @Test
    @DisplayName("RN112-RN117: Estados validos de reserva y disponibilidad")
    void estadosValidosDeReservaYDisponibilidad() {
        assertEquals(List.of(
                EstadoReserva.PENDIENTE_PAGO,
                EstadoReserva.CONFIRMADA,
                EstadoReserva.VENCIDA,
                EstadoReserva.ANULADA
        ), List.of(EstadoReserva.values()));
        assertEquals(List.of(
                EstadoDisponibilidad.LIBRE,
                EstadoDisponibilidad.RESERVADA,
                EstadoDisponibilidad.NO_DISPONIBLE
        ), List.of(EstadoDisponibilidad.values()));
    }

    // ─── TESTS DE CONSULTAS ───────────────────────────────────────────────────

    @Test
    @DisplayName("RN57: Reserva de casa completa bloquea todas las habitaciones del dia")
    void consultarDisponibilidadCasaEnteraBloqueaHabitaciones() {
        Habitacion habitacionUno = mock(Habitacion.class);
        Habitacion habitacionDos = mock(Habitacion.class);
        when(habitacionUno.getIdHabitacion()).thenReturn(1);
        when(habitacionUno.getCodigoHabitacion()).thenReturn("HAB-1");
        when(habitacionDos.getIdHabitacion()).thenReturn(2);
        when(habitacionDos.getCodigoHabitacion()).thenReturn("HAB-2");
        when(casaValida.getHabitaciones()).thenReturn(List.of(habitacionUno, habitacionDos));

        Reserva reservaCasaEntera = mock(Reserva.class);
        when(reservaCasaEntera.getEstado()).thenReturn(EstadoReserva.CONFIRMADA);
        when(reservaCasaEntera.getFechaEntrada()).thenReturn(fechaFutura(5));
        when(reservaCasaEntera.getNumeroNoches()).thenReturn(1);
        when(reservaCasaEntera.getTipoReserva()).thenReturn(TipoReserva.CASA_ENTERA);
        when(reservaRepository.findByCasaRuralCodigoCasa(1)).thenReturn(List.of(reservaCasaEntera));

        var disponibilidad = sistemaReservas.consultarDisponibilidadDetallada(1, fechaFutura(5), 1);

        assertTrue(disponibilidad.getDias().get(0).getHabitaciones().stream()
                .allMatch(habitacion -> habitacion.getEstado() == EstadoDisponibilidad.RESERVADA));
    }

    @Test
    @DisplayName("RN58: Habitacion reservada bloquea la reserva de casa completa")
    void realizarReservaCasaEnteraFallaSiHabitacionEstaReservada() {
        Habitacion habitacionUno = mock(Habitacion.class);
        when(habitacionUno.getIdHabitacion()).thenReturn(1);
        when(habitacionUno.getCodigoHabitacion()).thenReturn("HAB-1");

        Reserva reservaHabitacion = mock(Reserva.class);
        when(reservaHabitacion.getEstado()).thenReturn(EstadoReserva.CONFIRMADA);
        when(reservaHabitacion.getFechaEntrada()).thenReturn(fechaFutura(5));
        when(reservaHabitacion.getNumeroNoches()).thenReturn(1);
        when(reservaHabitacion.getTipoReserva()).thenReturn(TipoReserva.POR_HABITACIONES);
        when(reservaHabitacion.getHabitaciones()).thenReturn(List.of(habitacionUno));
        when(reservaRepository.findByCasaRuralCodigoCasa(1)).thenReturn(List.of(reservaHabitacion));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                sistemaReservas.realizarReserva(1, clienteValido, fechaFutura(5), 1, List.of(), 200000)
        );

        assertEquals("La casa no esta disponible", ex.getMessage());
    }

    @Test
    @DisplayName("RN59-RN60: Reserva falla si algun dia del periodo no esta disponible")
    void realizarReservaFallaSiPeriodoTieneDiasNoDisponibles() {
        when(casaValida.getPaquetesAlquiler()).thenReturn(List.of(new PaqueteAlquiler(
                fechaFutura(5),
                fechaFutura(5),
                ModalidadAlquiler.CASA_ENTERA,
                200000,
                0,
                true
        )));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                sistemaReservas.realizarReserva(1, clienteValido, fechaFutura(5), 2, List.of(), 200000)
        );

        assertEquals("La casa no esta disponible", ex.getMessage());
    }

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
