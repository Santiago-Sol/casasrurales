package co.edu.uniquindio.casasrurales.controllers;

import co.edu.uniquindio.casasrurales.dto.ReservaRequestDTO;
import co.edu.uniquindio.casasrurales.dto.ReservaResumenDTO;
import co.edu.uniquindio.casasrurales.entities.CasaRural;
import co.edu.uniquindio.casasrurales.entities.Cliente;
import co.edu.uniquindio.casasrurales.entities.Reserva;
import co.edu.uniquindio.casasrurales.enums.EstadoReserva;
import co.edu.uniquindio.casasrurales.enums.TipoReserva;
import co.edu.uniquindio.casasrurales.repositories.ClienteRepository;
import co.edu.uniquindio.casasrurales.repositories.HabitacionRepository;
import co.edu.uniquindio.casasrurales.services.SistemaReservas;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias del controlador REST de reservas para la HU9.
 * Valida respuestas HTTP, manejo de errores y casos limite del endpoint POST /api/reservas.
 */
@DisplayName("ReservaController - Pruebas Unitarias HU9")
class ReservaControllerTest {

    private ReservaController reservaController;
    private SistemaReservas sistemaReservas;
    private ClienteRepository clienteRepository;
    private HabitacionRepository habitacionRepository;
    private Authentication authentication;

    private Cliente clienteMock;
    private Reserva reservaMock;

    @BeforeEach
    void setUp() {
        sistemaReservas = mock(SistemaReservas.class);
        clienteRepository = mock(ClienteRepository.class);
        habitacionRepository = mock(HabitacionRepository.class);

        reservaController = new ReservaController(sistemaReservas, clienteRepository, habitacionRepository);

        authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("3001234567");

        clienteMock = mock(Cliente.class);
        when(clienteMock.getIdUsuario()).thenReturn(1);
        when(clienteRepository.findByTelefono("3001234567")).thenReturn(Optional.of(clienteMock));

        CasaRural casaMock = mock(CasaRural.class);
        when(casaMock.getPoblacion()).thenReturn("Armenia");
        when(casaMock.getCodigoCasa()).thenReturn(1);

        reservaMock = mock(Reserva.class);
        when(reservaMock.getNumeroReserva()).thenReturn(42);
        when(reservaMock.getFechaReserva()).thenReturn(new Date());
        when(reservaMock.getFechaEntrada()).thenReturn(fechaFutura(5));
        when(reservaMock.getNumeroNoches()).thenReturn(3);
        when(reservaMock.getTipoReserva()).thenReturn(TipoReserva.CASA_ENTERA);
        when(reservaMock.getImporteTotal()).thenReturn(300000.0);
        when(reservaMock.getImporteAnticipo()).thenReturn(60000.0);
        when(reservaMock.getFechaLimitePago()).thenReturn(fechaFutura(3));
        when(reservaMock.getEstado()).thenReturn(EstadoReserva.PENDIENTE_PAGO);
        when(reservaMock.getCasaRural()).thenReturn(casaMock);
    }

    private Date fechaFutura(int dias) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, dias);
        return cal.getTime();
    }

    private ReservaRequestDTO requestValido() {
        ReservaRequestDTO dto = new ReservaRequestDTO();
        dto.setCodigoCasa(1);
        dto.setFechaEntrada(fechaFutura(5));
        dto.setNumeroNoches(3);
        dto.setImporteTotal(300000.0);
        dto.setIdsHabitaciones(List.of());
        return dto;
    }

    // ─── POST /api/reservas ───────────────────────────────────────────────────

    @Test
    @DisplayName("HU9-REST-C01: POST exitoso retorna CREATED con el numero de reserva")
    void testRealizarReserva_Exitosa() {
        when(sistemaReservas.realizarReserva(anyInt(), any(), any(), anyInt(), anyList(), anyDouble()))
                .thenReturn(reservaMock);

        ResponseEntity<?> respuesta = reservaController.realizarReserva(requestValido(), authentication);

        assertEquals(HttpStatus.CREATED, respuesta.getStatusCode());
        ReservaResumenDTO body = (ReservaResumenDTO) respuesta.getBody();
        assertNotNull(body);
        assertEquals(42, body.getNumeroReserva());
        assertEquals(EstadoReserva.PENDIENTE_PAGO, body.getEstado());
    }

    @Test
    @DisplayName("HU9-REST-C02: Sin autenticacion retorna UNAUTHORIZED")
    void testRealizarReserva_SinAutenticacion() {
        ResponseEntity<?> respuesta = reservaController.realizarReserva(requestValido(), null);

        assertEquals(HttpStatus.UNAUTHORIZED, respuesta.getStatusCode());
    }

    @Test
    @DisplayName("HU9-REST-C03: Usuario no es cliente retorna FORBIDDEN")
    void testRealizarReserva_UsuarioNoEsCliente() {
        when(clienteRepository.findByTelefono("3001234567")).thenReturn(Optional.empty());

        ResponseEntity<?> respuesta = reservaController.realizarReserva(requestValido(), authentication);

        assertEquals(HttpStatus.FORBIDDEN, respuesta.getStatusCode());
        Map<?, ?> body = (Map<?, ?>) respuesta.getBody();
        assertNotNull(body);
        assertEquals("Solo los clientes pueden realizar reservas", body.get("error"));
    }

    // ─── GET /api/reservas/mis-reservas ──────────────────────────────────────

    @Test
    @DisplayName("HU9-REST-C04: GET mis-reservas retorna lista vacia si no hay reservas")
    void testGetMisReservas_ListaVacia() {
        when(sistemaReservas.getReservasPorCliente(1)).thenReturn(List.of());

        ResponseEntity<?> respuesta = reservaController.getMisReservas(authentication);

        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        List<?> body = (List<?>) respuesta.getBody();
        assertNotNull(body);
        assertTrue(body.isEmpty());
    }

    @Test
    @DisplayName("HU9-REST-C05: GET mis-reservas sin autenticacion retorna UNAUTHORIZED")
    void testGetMisReservas_SinAuth() {
        ResponseEntity<?> respuesta = reservaController.getMisReservas(null);
        assertEquals(HttpStatus.UNAUTHORIZED, respuesta.getStatusCode());
    }

    // ─── GET /api/reservas/{numero} ───────────────────────────────────────────

    @Test
    @DisplayName("HU9-REST-C06: GET reserva por numero existente retorna OK con datos")
    void testGetReservaPorNumero_Existe() {
        when(sistemaReservas.buscarReservaPorNumero(42)).thenReturn(reservaMock);

        ResponseEntity<?> respuesta = reservaController.getReservaPorNumero(42);

        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        ReservaResumenDTO body = (ReservaResumenDTO) respuesta.getBody();
        assertNotNull(body);
        assertEquals(42, body.getNumeroReserva());
    }

    @Test
    @DisplayName("HU9-REST-C07: GET reserva inexistente retorna NOT_FOUND")
    void testGetReservaPorNumero_NoExiste() {
        when(sistemaReservas.buscarReservaPorNumero(999)).thenReturn(null);

        ResponseEntity<?> respuesta = reservaController.getReservaPorNumero(999);

        assertEquals(HttpStatus.NOT_FOUND, respuesta.getStatusCode());
        Map<?, ?> body = (Map<?, ?>) respuesta.getBody();
        assertNotNull(body);
        assertEquals("Reserva no encontrada", body.get("error"));
    }
}