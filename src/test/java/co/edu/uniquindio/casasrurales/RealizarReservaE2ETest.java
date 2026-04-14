package co.edu.uniquindio.casasrurales;

import co.edu.uniquindio.casasrurales.entities.Bano;
import co.edu.uniquindio.casasrurales.entities.CasaRural;
import co.edu.uniquindio.casasrurales.entities.Cliente;
import co.edu.uniquindio.casasrurales.entities.Cocina;
import co.edu.uniquindio.casasrurales.entities.Foto;
import co.edu.uniquindio.casasrurales.entities.Habitacion;
import co.edu.uniquindio.casasrurales.entities.Propietario;
import co.edu.uniquindio.casasrurales.entities.Reserva;
import co.edu.uniquindio.casasrurales.enums.EstadoReserva;
import co.edu.uniquindio.casasrurales.enums.TipoReserva;
import co.edu.uniquindio.casasrurales.repositories.CasaRuralRepository;
import co.edu.uniquindio.casasrurales.repositories.ClienteRepository;
import co.edu.uniquindio.casasrurales.repositories.PropietarioRepository;
import co.edu.uniquindio.casasrurales.repositories.ReservaRepository;
import co.edu.uniquindio.casasrurales.services.SistemaReservas;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas de integracion end-to-end del flujo completo de realizacion de reserva.
 * Utilizan una base de datos H2 en memoria para testing.
 * Validan el flujo completo desde la solicitud hasta la persistencia en BD.
 */
@DisplayName("RealizarReserva - Pruebas E2E HU9")
@SpringBootTest
@Transactional
class RealizarReservaE2ETest {

    @Autowired
    private SistemaReservas sistemaReservas;

    @Autowired
    private CasaRuralRepository casaRuralRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private PropietarioRepository propietarioRepository;

    @Autowired
    private ReservaRepository reservaRepository;

    private CasaRural casaRural;
    private Cliente cliente;

    @BeforeEach
    void prepararDatos() {
        // Propietario
        Propietario propietario = new Propietario("3009999999");
        propietarioRepository.save(propietario);

        // Casa valida (minimo: 1 foto, 3 habitaciones, 1 cocina, 2 banos)
        casaRural = new CasaRural(1, "Armenia", "Casa de prueba E2E", 1, 2, true);
        casaRural.setPropietario(propietario);
        casaRural.agregarFoto(new Foto());
        casaRural.agregarHabitacion(new Habitacion());
        casaRural.agregarHabitacion(new Habitacion());
        casaRural.agregarHabitacion(new Habitacion());
        casaRural.agregarCocina(new Cocina());
        casaRural.agregarBano(new Bano());
        casaRural.agregarBano(new Bano());
        casaRuralRepository.save(casaRural);

        // Cliente
        cliente = new Cliente("3001234567");
        clienteRepository.save(cliente);
    }

    private Date fechaFutura(int dias) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, dias);
        return cal.getTime();
    }

    // ─── FLUJO EXITOSO ────────────────────────────────────────────────────────

    @Test
    @DisplayName("HU9-E2E-01: Flujo completo - reserva se persiste con numero unico generado")
    void testFlujoCompleto_ReservaExitosa() {
        Reserva reserva = sistemaReservas.realizarReserva(
                1, cliente, fechaFutura(10), 5, List.of(), 500000
        );

        assertNotNull(reserva);
        assertTrue(reserva.getNumeroReserva() > 0, "El numero de reserva debe ser unico y positivo");
        assertEquals(EstadoReserva.PENDIENTE_PAGO, reserva.getEstado());
        assertEquals(TipoReserva.CASA_ENTERA, reserva.getTipoReserva());
        assertNotNull(reserva.getFechaReserva(), "La fecha de reserva debe asignarse automaticamente");
        assertNotNull(reserva.getFechaLimitePago(), "La fecha limite de pago debe calcularse automaticamente");
    }

    @Test
    @DisplayName("HU9-E2E-02: La reserva queda persistida en la base de datos")
    void testFlujoCompleto_ReservaPersistidaEnBD() {
        Reserva reserva = sistemaReservas.realizarReserva(
                1, cliente, fechaFutura(10), 3, List.of(), 300000
        );

        Reserva reservaEnBD = reservaRepository.findById(reserva.getNumeroReserva()).orElse(null);
        assertNotNull(reservaEnBD, "La reserva debe existir en la base de datos");
        assertEquals(reserva.getNumeroReserva(), reservaEnBD.getNumeroReserva());
    }

    @Test
    @DisplayName("HU9-E2E-03: El anticipo es el 20% del importe total")
    void testFlujoCompleto_AnticipoCalculadoCorrectamente() {
        Reserva reserva = sistemaReservas.realizarReserva(
                1, cliente, fechaFutura(10), 3, List.of(), 600000
        );

        assertEquals(120000.0, reserva.getImporteAnticipo(), 0.01);
    }

    @Test
    @DisplayName("HU9-E2E-04: La fecha limite de pago es 3 dias despues de la fecha de reserva")
    void testFlujoCompleto_FechaLimitePago() {
        Reserva reserva = sistemaReservas.realizarReserva(
                1, cliente, fechaFutura(10), 3, List.of(), 300000
        );

        Calendar esperado = Calendar.getInstance();
        esperado.setTime(reserva.getFechaReserva());
        esperado.add(Calendar.DAY_OF_MONTH, 3);

        Calendar actual = Calendar.getInstance();
        actual.setTime(reserva.getFechaLimitePago());

        assertEquals(esperado.get(Calendar.DAY_OF_MONTH), actual.get(Calendar.DAY_OF_MONTH));
    }

    @Test
    @DisplayName("HU9-E2E-05: Dos reservas en fechas distintas generan numeros unicos")
    void testFlujoCompleto_NumerosReservaUnicos() {
        Reserva reserva1 = sistemaReservas.realizarReserva(
                1, cliente, fechaFutura(10), 2, List.of(), 200000
        );
        Reserva reserva2 = sistemaReservas.realizarReserva(
                1, cliente, fechaFutura(30), 2, List.of(), 200000
        );

        assertNotEquals(reserva1.getNumeroReserva(), reserva2.getNumeroReserva(),
                "Dos reservas no pueden tener el mismo numero");
    }

    // ─── FLUJOS DE ERROR ──────────────────────────────────────────────────────

    @Test
    @DisplayName("HU9-E2E-06: Reserva en fechas solapadas lanza IllegalStateException")
    void testFlujoCompleto_FechasSolapadas() {
        sistemaReservas.realizarReserva(
                1, cliente, fechaFutura(10), 5, List.of(), 300000
        );

        assertThrows(IllegalStateException.class, () ->
                sistemaReservas.realizarReserva(
                        1, cliente, fechaFutura(12), 3, List.of(), 300000
                )
        );
    }

    @Test
    @DisplayName("HU9-E2E-07: Importe cero lanza IllegalArgumentException sin persistir")
    void testFlujoCompleto_ImporteCero_NoGuarda() {
        long reservasAntes = reservaRepository.count();

        assertThrows(IllegalArgumentException.class, () ->
                sistemaReservas.realizarReserva(1, cliente, fechaFutura(10), 3, List.of(), 0)
        );

        assertEquals(reservasAntes, reservaRepository.count(), "No debe haber guardado ninguna reserva");
    }
}
