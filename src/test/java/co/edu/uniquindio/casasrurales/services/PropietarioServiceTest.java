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
import co.edu.uniquindio.casasrurales.dto.CocinaFormDTO;
import co.edu.uniquindio.casasrurales.dto.HabitacionFormDTO;
import co.edu.uniquindio.casasrurales.dto.PagoRegistroDTO;
import co.edu.uniquindio.casasrurales.dto.PaqueteAlquilerDTO;
import co.edu.uniquindio.casasrurales.entities.CasaRural;
import co.edu.uniquindio.casasrurales.entities.Pago;
import co.edu.uniquindio.casasrurales.entities.PaqueteAlquiler;
import co.edu.uniquindio.casasrurales.entities.Propietario;
import co.edu.uniquindio.casasrurales.entities.Reserva;
import co.edu.uniquindio.casasrurales.enums.EstadoReserva;
import co.edu.uniquindio.casasrurales.enums.ModalidadAlquiler;
import co.edu.uniquindio.casasrurales.enums.TipoCama;
import co.edu.uniquindio.casasrurales.enums.TipoReserva;
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
        form.setUrlsFotos(List.of("/uploads/casa-15.jpg"));
        form.setHabitaciones(habitacionesValidas());
        form.setCocinas(cocinasValidas());

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
        assertEquals("HAB-1", casa.getHabitaciones().get(0).getCodigoHabitacion());
        assertEquals(2, casa.getHabitaciones().get(1).getNumeroCamas());
        assertEquals(TipoCama.DOBLE, casa.getHabitaciones().get(1).getTipoCama());
        assertTrue(casa.getHabitaciones().get(1).isTieneBano());
        assertEquals(2, casa.getNumBanos());
        assertEquals(1, casa.getNumCocinas());
        assertEquals(1, casa.getCocinas().size());
        assertTrue(casa.getCocinas().getFirst().isTieneLavavajillas());
        assertFalse(casa.getCocinas().getFirst().isTieneLavadora());
        assertEquals(1, casa.getFotos().size());
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
        form.setUrlsFotos(List.of("/uploads/casa-15.jpg"));
        form.setHabitaciones(habitacionesValidas());
        form.setCocinas(List.of());

        when(propietarioRepository.findById(8)).thenReturn(Optional.of(propietario));
        when(casaRuralRepository.existsById(15)).thenReturn(false);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> propietarioService.crearCasa(form, 8));

        assertEquals("La casa debe tener minimo 3 habitaciones", ex.getMessage());
        verify(propietarioRepository, never()).save(any(Propietario.class));
    }

    @Test
    @DisplayName("crearCasa rechaza cuando no se registra al menos una foto")
    void crearCasaRechazaSinFotos() {
        Propietario propietario = new Propietario("3001234567", "dueno", "secret123", "123456");
        propietario.setIdUsuario(8);

        CasaRuralFormDTO form = new CasaRuralFormDTO();
        form.setCodigoCasa(15);
        form.setNombrePropiedad("La Montanita");
        form.setPoblacion("Salento");
        form.setDescripcion(null);
        form.setNumComedores(2);
        form.setNumPlazasGaraje(3);
        form.setNumHabitaciones(3);
        form.setNumBanos(2);
        form.setNumCocinas(1);
        form.setUrlsFotos(List.of("   "));
        form.setHabitaciones(habitacionesValidas());
        form.setCocinas(cocinasValidas());

        when(propietarioRepository.findById(8)).thenReturn(Optional.of(propietario));
        when(casaRuralRepository.existsById(15)).thenReturn(false);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> propietarioService.crearCasa(form, 8));

        assertEquals("Debe registrar al menos una foto de la casa", ex.getMessage());
        verify(propietarioRepository, never()).save(any(Propietario.class));
    }

    @Test
    @DisplayName("crearCasa rechaza habitaciones con codigo repetido en la misma casa")
    void crearCasaRechazaHabitacionesDuplicadas() {
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
        form.setUrlsFotos(List.of("/uploads/casa-15.jpg"));
        form.setHabitaciones(List.of(
                habitacion("HAB-1", 1, TipoCama.SENCILLA, false),
                habitacion("hab-1", 2, TipoCama.DOBLE, true),
                habitacion("HAB-3", 1, TipoCama.SENCILLA, false)
        ));
        form.setCocinas(cocinasValidas());

        when(propietarioRepository.findById(8)).thenReturn(Optional.of(propietario));
        when(casaRuralRepository.existsById(15)).thenReturn(false);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> propietarioService.crearCasa(form, 8));

        assertEquals("El codigo de habitacion no puede repetirse dentro de la misma casa", ex.getMessage());
        verify(propietarioRepository, never()).save(any(Propietario.class));
    }

    @Test
    @DisplayName("crearCasa rechaza cuando no se registran los datos de cada cocina")
    void crearCasaRechazaCocinasSinDetalle() {
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
        form.setUrlsFotos(List.of("/uploads/casa-15.jpg"));
        form.setHabitaciones(habitacionesValidas());
        form.setCocinas(List.of());

        when(propietarioRepository.findById(8)).thenReturn(Optional.of(propietario));
        when(casaRuralRepository.existsById(15)).thenReturn(false);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> propietarioService.crearCasa(form, 8));

        assertEquals("Debe registrar los datos de cada cocina", ex.getMessage());
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
    @DisplayName("darDeBajaCasa desactiva la casa cuando pertenece al propietario y no tiene reservas activas")
    void darDeBajaCasaExitosamente() {
        Propietario propietario = new Propietario("3001234567", "dueno", "secret123", "123456");
        propietario.setIdUsuario(8);
        CasaRural casa = new CasaRural(15, "Salento", "La Montanita", "Cabana familiar", 1, 1, true);
        casa.setPropietario(propietario);

        when(propietarioRepository.findById(8)).thenReturn(Optional.of(propietario));
        when(casaRuralRepository.findById(15)).thenReturn(Optional.of(casa));
        when(reservaRepository.findByCasaRuralCodigoCasa(15)).thenReturn(List.of());

        String respuesta = propietarioService.darDeBajaCasa(15, 8);

        assertEquals("Casa dada de baja exitosamente", respuesta);
        assertFalse(casa.isActiva());
        verify(casaRuralRepository).save(casa);
    }

    @Test
    @DisplayName("darDeBajaCasa rechaza casas con reservas confirmadas o pendientes")
    void darDeBajaCasaRechazaReservasActivas() {
        Propietario propietario = new Propietario("3001234567", "dueno", "secret123", "123456");
        propietario.setIdUsuario(8);
        CasaRural casa = new CasaRural(15, "Salento", "La Montanita", "Cabana familiar", 1, 1, true);
        casa.setPropietario(propietario);

        Reserva reservaConfirmada = mock(Reserva.class);
        when(reservaConfirmada.getEstado()).thenReturn(EstadoReserva.CONFIRMADA);
        Reserva reservaPendiente = mock(Reserva.class);
        when(reservaPendiente.getEstado()).thenReturn(EstadoReserva.PENDIENTE_PAGO);

        when(propietarioRepository.findById(8)).thenReturn(Optional.of(propietario));
        when(casaRuralRepository.findById(15)).thenReturn(Optional.of(casa));
        when(reservaRepository.findByCasaRuralCodigoCasa(15)).thenReturn(List.of(reservaConfirmada, reservaPendiente));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> propietarioService.darDeBajaCasa(15, 8));

        assertEquals("No puedes dar de baja la casa porque tiene 2 reserva(s) activa(s). Cancélalas primero.", ex.getMessage());
        assertTrue(casa.isActiva());
        verify(casaRuralRepository, never()).save(any(CasaRural.class));
    }

    @Test
    @DisplayName("darDeBajaCasa rechaza cuando el propietario no es el dueno")
    void darDeBajaCasaRechazaPropietarioIncorrecto() {
        Propietario propietarioSolicitante = new Propietario("3001234567", "solicitante", "secret123", "123456");
        propietarioSolicitante.setIdUsuario(8);
        Propietario propietarioCasa = new Propietario("3007654321", "dueno", "secret123", "654321");
        propietarioCasa.setIdUsuario(3);
        CasaRural casa = new CasaRural(15, "Salento", "La Montanita", "Cabana familiar", 1, 1, true);
        casa.setPropietario(propietarioCasa);

        when(propietarioRepository.findById(8)).thenReturn(Optional.of(propietarioSolicitante));
        when(casaRuralRepository.findById(15)).thenReturn(Optional.of(casa));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> propietarioService.darDeBajaCasa(15, 8));

        assertEquals("No tienes permiso para dar de baja esta casa", ex.getMessage());
        assertTrue(casa.isActiva());
        verify(casaRuralRepository, never()).save(any(CasaRural.class));
    }

    @Test
    @DisplayName("reactivarCasa activa nuevamente una casa del propietario")
    void reactivarCasaExitosamente() {
        Propietario propietario = new Propietario("3001234567", "dueno", "secret123", "123456");
        propietario.setIdUsuario(8);
        CasaRural casa = new CasaRural(15, "Salento", "La Montanita", "Cabana familiar", 1, 1, false);
        casa.setPropietario(propietario);

        when(propietarioRepository.findById(8)).thenReturn(Optional.of(propietario));
        when(casaRuralRepository.findById(15)).thenReturn(Optional.of(casa));

        String respuesta = propietarioService.reactivarCasa(15, 8);

        assertEquals("Casa reactivada exitosamente", respuesta);
        assertTrue(casa.isActiva());
        verify(casaRuralRepository).save(casa);
    }

    @Test
    @DisplayName("crearPaquete registra paquete valido sin solapar fechas")
    void crearPaqueteExitosamente() {
        Propietario propietario = new Propietario("3001234567", "dueno", "secret123", "123456");
        propietario.setIdUsuario(8);
        CasaRural casa = new CasaRural(15, "Salento", "La Montanita", "Cabana familiar", 1, 1, true);
        casa.setPropietario(propietario);

        PaqueteAlquilerDTO dto = new PaqueteAlquilerDTO(
                null,
                java.sql.Date.valueOf("2026-06-01"),
                java.sql.Date.valueOf("2026-06-05"),
                ModalidadAlquiler.AMBAS,
                450000,
                120000,
                true
        );

        when(casaRuralRepository.findById(15)).thenReturn(Optional.of(casa));

        PaqueteAlquilerDTO resultado = propietarioService.crearPaquete(15, 8, dto);

        assertEquals(ModalidadAlquiler.AMBAS, resultado.getModalidad());
        assertEquals(450000, resultado.getPrecioCasaEntera());
        assertEquals(120000, resultado.getPrecioHabitacion());
        verify(paqueteAlquilerRepository).save(any(PaqueteAlquiler.class));
    }

    @Test
    @DisplayName("crearPaquete rechaza fechas solapadas con paquetes existentes")
    void crearPaqueteRechazaSolapamiento() {
        Propietario propietario = new Propietario("3001234567", "dueno", "secret123", "123456");
        propietario.setIdUsuario(8);
        CasaRural casa = new CasaRural(15, "Salento", "La Montanita", "Cabana familiar", 1, 1, true);
        casa.setPropietario(propietario);
        casa.agregarPaqueteAlquiler(new PaqueteAlquiler(
                java.sql.Date.valueOf("2026-06-01"),
                java.sql.Date.valueOf("2026-06-05"),
                ModalidadAlquiler.CASA_ENTERA,
                450000,
                0,
                true
        ));

        PaqueteAlquilerDTO dto = new PaqueteAlquilerDTO(
                null,
                java.sql.Date.valueOf("2026-06-04"),
                java.sql.Date.valueOf("2026-06-10"),
                ModalidadAlquiler.POR_HABITACIONES,
                0,
                120000,
                true
        );

        when(casaRuralRepository.findById(15)).thenReturn(Optional.of(casa));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> propietarioService.crearPaquete(15, 8, dto));

        assertEquals("Las fechas se solapan con un paquete existente de esta casa", ex.getMessage());
        verify(paqueteAlquilerRepository, never()).save(any(PaqueteAlquiler.class));
    }

    @Test
    @DisplayName("crearPaquete valida precios requeridos por modalidad")
    void crearPaqueteRechazaPrecioInvalido() {
        Propietario propietario = new Propietario("3001234567", "dueno", "secret123", "123456");
        propietario.setIdUsuario(8);
        CasaRural casa = new CasaRural(15, "Salento", "La Montanita", "Cabana familiar", 1, 1, true);
        casa.setPropietario(propietario);

        PaqueteAlquilerDTO dto = new PaqueteAlquilerDTO(
                null,
                java.sql.Date.valueOf("2026-06-01"),
                java.sql.Date.valueOf("2026-06-05"),
                ModalidadAlquiler.CASA_ENTERA,
                0,
                0,
                true
        );

        when(casaRuralRepository.findById(15)).thenReturn(Optional.of(casa));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> propietarioService.crearPaquete(15, 8, dto));

        assertEquals("El precio de casa entera debe ser mayor a cero", ex.getMessage());
        verify(paqueteAlquilerRepository, never()).save(any(PaqueteAlquiler.class));
    }

    @Test
    @DisplayName("modificarPaquete actualiza modalidad y precios sin solapar")
    void modificarPaqueteExitosamente() {
        Propietario propietario = new Propietario("3001234567", "dueno", "secret123", "123456");
        propietario.setIdUsuario(8);
        CasaRural casa = new CasaRural(15, "Salento", "La Montanita", "Cabana familiar", 1, 1, true);
        casa.setPropietario(propietario);
        PaqueteAlquiler paquete = new PaqueteAlquiler(
                java.sql.Date.valueOf("2026-06-01"),
                java.sql.Date.valueOf("2026-06-05"),
                ModalidadAlquiler.CASA_ENTERA,
                450000,
                0,
                true
        );
        casa.agregarPaqueteAlquiler(paquete);

        PaqueteAlquilerDTO dto = new PaqueteAlquilerDTO(
                null,
                java.sql.Date.valueOf("2026-06-02"),
                java.sql.Date.valueOf("2026-06-06"),
                ModalidadAlquiler.AMBAS,
                500000,
                130000,
                true
        );

        when(casaRuralRepository.findById(15)).thenReturn(Optional.of(casa));
        when(paqueteAlquilerRepository.findById(0)).thenReturn(Optional.of(paquete));
        when(reservaRepository.findByCasaRuralCodigoCasa(15)).thenReturn(List.of());

        PaqueteAlquilerDTO resultado = propietarioService.modificarPaquete(15, 8, 0, dto);

        assertEquals(ModalidadAlquiler.AMBAS, resultado.getModalidad());
        assertEquals(500000, resultado.getPrecioCasaEntera());
        assertEquals(130000, resultado.getPrecioHabitacion());
        verify(paqueteAlquilerRepository).save(paquete);
    }

    @Test
    @DisplayName("modificarPaquete rechaza cambios que dejan reservas existentes sin cobertura")
    void modificarPaqueteRechazaReservaSinCobertura() {
        Propietario propietario = new Propietario("3001234567", "dueno", "secret123", "123456");
        propietario.setIdUsuario(8);
        CasaRural casa = new CasaRural(15, "Salento", "La Montanita", "Cabana familiar", 1, 1, true);
        casa.setPropietario(propietario);
        PaqueteAlquiler paquete = new PaqueteAlquiler(
                java.sql.Date.valueOf("2026-06-01"),
                java.sql.Date.valueOf("2026-06-10"),
                ModalidadAlquiler.CASA_ENTERA,
                450000,
                0,
                true
        );
        casa.agregarPaqueteAlquiler(paquete);

        Reserva reserva = mock(Reserva.class);
        when(reserva.getEstado()).thenReturn(EstadoReserva.CONFIRMADA);
        when(reserva.getFechaEntrada()).thenReturn(java.sql.Date.valueOf("2026-06-08"));
        when(reserva.getNumeroNoches()).thenReturn(2);
        when(reserva.getTipoReserva()).thenReturn(TipoReserva.CASA_ENTERA);

        PaqueteAlquilerDTO dto = new PaqueteAlquilerDTO(
                null,
                java.sql.Date.valueOf("2026-06-01"),
                java.sql.Date.valueOf("2026-06-05"),
                ModalidadAlquiler.CASA_ENTERA,
                450000,
                0,
                true
        );

        when(casaRuralRepository.findById(15)).thenReturn(Optional.of(casa));
        when(paqueteAlquilerRepository.findById(0)).thenReturn(Optional.of(paquete));
        when(reservaRepository.findByCasaRuralCodigoCasa(15)).thenReturn(List.of(reserva));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> propietarioService.modificarPaquete(15, 8, 0, dto));

        assertEquals("No se puede modificar el paquete porque dejaria reservas sin disponibilidad", ex.getMessage());
        verify(paqueteAlquilerRepository, never()).save(any(PaqueteAlquiler.class));
    }

    @Test
    @DisplayName("modificarPaquete rechaza modalidad incompatible con reservas existentes")
    void modificarPaqueteRechazaModalidadIncompatibleConReserva() {
        Propietario propietario = new Propietario("3001234567", "dueno", "secret123", "123456");
        propietario.setIdUsuario(8);
        CasaRural casa = new CasaRural(15, "Salento", "La Montanita", "Cabana familiar", 1, 1, true);
        casa.setPropietario(propietario);
        PaqueteAlquiler paquete = new PaqueteAlquiler(
                java.sql.Date.valueOf("2026-06-01"),
                java.sql.Date.valueOf("2026-06-10"),
                ModalidadAlquiler.AMBAS,
                450000,
                120000,
                true
        );
        casa.agregarPaqueteAlquiler(paquete);

        Reserva reserva = mock(Reserva.class);
        when(reserva.getEstado()).thenReturn(EstadoReserva.CONFIRMADA);
        when(reserva.getFechaEntrada()).thenReturn(java.sql.Date.valueOf("2026-06-03"));
        when(reserva.getNumeroNoches()).thenReturn(2);
        when(reserva.getTipoReserva()).thenReturn(TipoReserva.POR_HABITACIONES);

        PaqueteAlquilerDTO dto = new PaqueteAlquilerDTO(
                null,
                java.sql.Date.valueOf("2026-06-01"),
                java.sql.Date.valueOf("2026-06-10"),
                ModalidadAlquiler.CASA_ENTERA,
                450000,
                0,
                true
        );

        when(casaRuralRepository.findById(15)).thenReturn(Optional.of(casa));
        when(paqueteAlquilerRepository.findById(0)).thenReturn(Optional.of(paquete));
        when(reservaRepository.findByCasaRuralCodigoCasa(15)).thenReturn(List.of(reserva));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> propietarioService.modificarPaquete(15, 8, 0, dto));

        assertEquals("No se puede cambiar la modalidad porque contradice reservas existentes", ex.getMessage());
        verify(paqueteAlquilerRepository, never()).save(any(PaqueteAlquiler.class));
    }

    @Test
    @DisplayName("eliminarPaquete borra solo paquetes de la casa del propietario")
    void eliminarPaqueteExitosamente() {
        Propietario propietario = new Propietario("3001234567", "dueno", "secret123", "123456");
        propietario.setIdUsuario(8);
        CasaRural casa = new CasaRural(15, "Salento", "La Montanita", "Cabana familiar", 1, 1, true);
        casa.setPropietario(propietario);
        PaqueteAlquiler paquete = new PaqueteAlquiler(
                java.sql.Date.valueOf("2026-06-01"),
                java.sql.Date.valueOf("2026-06-05"),
                ModalidadAlquiler.CASA_ENTERA,
                450000,
                0,
                true
        );
        casa.agregarPaqueteAlquiler(paquete);

        when(casaRuralRepository.findById(15)).thenReturn(Optional.of(casa));
        when(paqueteAlquilerRepository.findById(0)).thenReturn(Optional.of(paquete));
        when(reservaRepository.findByCasaRuralCodigoCasa(15)).thenReturn(List.of());

        propietarioService.eliminarPaquete(15, 8, 0);

        verify(paqueteAlquilerRepository).delete(paquete);
    }

    @Test
    @DisplayName("eliminarPaquete rechaza paquetes con reservas existentes")
    void eliminarPaqueteRechazaReservasExistentes() {
        Propietario propietario = new Propietario("3001234567", "dueno", "secret123", "123456");
        propietario.setIdUsuario(8);
        CasaRural casa = new CasaRural(15, "Salento", "La Montanita", "Cabana familiar", 1, 1, true);
        casa.setPropietario(propietario);
        PaqueteAlquiler paquete = new PaqueteAlquiler(
                java.sql.Date.valueOf("2026-06-01"),
                java.sql.Date.valueOf("2026-06-05"),
                ModalidadAlquiler.CASA_ENTERA,
                450000,
                0,
                true
        );
        casa.agregarPaqueteAlquiler(paquete);

        Reserva reserva = mock(Reserva.class);
        when(reserva.getEstado()).thenReturn(EstadoReserva.PENDIENTE_PAGO);
        when(reserva.getFechaEntrada()).thenReturn(java.sql.Date.valueOf("2026-06-02"));
        when(reserva.getNumeroNoches()).thenReturn(2);

        when(casaRuralRepository.findById(15)).thenReturn(Optional.of(casa));
        when(paqueteAlquilerRepository.findById(0)).thenReturn(Optional.of(paquete));
        when(reservaRepository.findByCasaRuralCodigoCasa(15)).thenReturn(List.of(reserva));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> propietarioService.eliminarPaquete(15, 8, 0));

        assertEquals("No se puede eliminar un paquete con reservas existentes", ex.getMessage());
        verify(paqueteAlquilerRepository, never()).delete(any(PaqueteAlquiler.class));
    }

    @Test
    @DisplayName("dividirPaquete reemplaza el paquete original por paquetes no solapados")
    void dividirPaqueteExitosamente() {
        Propietario propietario = new Propietario("3001234567", "dueno", "secret123", "123456");
        propietario.setIdUsuario(8);
        CasaRural casa = new CasaRural(15, "Salento", "La Montanita", "Cabana familiar", 1, 1, true);
        casa.setPropietario(propietario);
        PaqueteAlquiler paquete = new PaqueteAlquiler(
                java.sql.Date.valueOf("2026-06-01"),
                java.sql.Date.valueOf("2026-06-10"),
                ModalidadAlquiler.AMBAS,
                900000,
                150000,
                true
        );
        casa.agregarPaqueteAlquiler(paquete);

        List<PaqueteAlquilerDTO> nuevosPaquetes = List.of(
                new PaqueteAlquilerDTO(null, java.sql.Date.valueOf("2026-06-01"),
                        java.sql.Date.valueOf("2026-06-05"), ModalidadAlquiler.CASA_ENTERA, 500000, 0, true),
                new PaqueteAlquilerDTO(null, java.sql.Date.valueOf("2026-06-06"),
                        java.sql.Date.valueOf("2026-06-10"), ModalidadAlquiler.POR_HABITACIONES, 0, 140000, true)
        );

        when(casaRuralRepository.findById(15)).thenReturn(Optional.of(casa));
        when(paqueteAlquilerRepository.findById(0)).thenReturn(Optional.of(paquete));
        when(reservaRepository.findByCasaRuralCodigoCasa(15)).thenReturn(List.of());
        when(paqueteAlquilerRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        List<PaqueteAlquilerDTO> resultado = propietarioService.dividirPaquete(15, 8, 0, nuevosPaquetes);

        assertEquals(2, resultado.size());
        assertEquals(java.sql.Date.valueOf("2026-06-01"), resultado.get(0).getFechaInicio());
        assertEquals(java.sql.Date.valueOf("2026-06-10"), resultado.get(1).getFechaFin());
        verify(paqueteAlquilerRepository).delete(paquete);
        verify(paqueteAlquilerRepository).saveAll(any());
    }

    @Test
    @DisplayName("dividirPaquete rechaza paquetes resultantes solapados")
    void dividirPaqueteRechazaSubpaquetesSolapados() {
        Propietario propietario = new Propietario("3001234567", "dueno", "secret123", "123456");
        propietario.setIdUsuario(8);
        CasaRural casa = new CasaRural(15, "Salento", "La Montanita", "Cabana familiar", 1, 1, true);
        casa.setPropietario(propietario);
        PaqueteAlquiler paquete = new PaqueteAlquiler(
                java.sql.Date.valueOf("2026-06-01"),
                java.sql.Date.valueOf("2026-06-10"),
                ModalidadAlquiler.AMBAS,
                900000,
                150000,
                true
        );
        casa.agregarPaqueteAlquiler(paquete);

        List<PaqueteAlquilerDTO> nuevosPaquetes = List.of(
                new PaqueteAlquilerDTO(null, java.sql.Date.valueOf("2026-06-01"),
                        java.sql.Date.valueOf("2026-06-06"), ModalidadAlquiler.CASA_ENTERA, 500000, 0, true),
                new PaqueteAlquilerDTO(null, java.sql.Date.valueOf("2026-06-06"),
                        java.sql.Date.valueOf("2026-06-10"), ModalidadAlquiler.POR_HABITACIONES, 0, 140000, true)
        );

        when(casaRuralRepository.findById(15)).thenReturn(Optional.of(casa));
        when(paqueteAlquilerRepository.findById(0)).thenReturn(Optional.of(paquete));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> propietarioService.dividirPaquete(15, 8, 0, nuevosPaquetes));

        assertEquals("Los paquetes resultantes no pueden solaparse entre si", ex.getMessage());
        verify(paqueteAlquilerRepository, never()).delete(any(PaqueteAlquiler.class));
        verify(paqueteAlquilerRepository, never()).saveAll(any());
    }

    @Test
    @DisplayName("dividirPaquete rechaza paquetes fuera del rango original")
    void dividirPaqueteRechazaRangoFueraDelOriginal() {
        Propietario propietario = new Propietario("3001234567", "dueno", "secret123", "123456");
        propietario.setIdUsuario(8);
        CasaRural casa = new CasaRural(15, "Salento", "La Montanita", "Cabana familiar", 1, 1, true);
        casa.setPropietario(propietario);
        PaqueteAlquiler paquete = new PaqueteAlquiler(
                java.sql.Date.valueOf("2026-06-01"),
                java.sql.Date.valueOf("2026-06-10"),
                ModalidadAlquiler.AMBAS,
                900000,
                150000,
                true
        );
        casa.agregarPaqueteAlquiler(paquete);

        List<PaqueteAlquilerDTO> nuevosPaquetes = List.of(
                new PaqueteAlquilerDTO(null, java.sql.Date.valueOf("2026-05-31"),
                        java.sql.Date.valueOf("2026-06-05"), ModalidadAlquiler.CASA_ENTERA, 500000, 0, true),
                new PaqueteAlquilerDTO(null, java.sql.Date.valueOf("2026-06-06"),
                        java.sql.Date.valueOf("2026-06-10"), ModalidadAlquiler.POR_HABITACIONES, 0, 140000, true)
        );

        when(casaRuralRepository.findById(15)).thenReturn(Optional.of(casa));
        when(paqueteAlquilerRepository.findById(0)).thenReturn(Optional.of(paquete));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> propietarioService.dividirPaquete(15, 8, 0, nuevosPaquetes));

        assertEquals("Los paquetes resultantes deben estar dentro del rango del paquete original", ex.getMessage());
        verify(paqueteAlquilerRepository, never()).delete(any(PaqueteAlquiler.class));
        verify(paqueteAlquilerRepository, never()).saveAll(any());
    }

    @Test
    @DisplayName("dividirPaquete rechaza division que contradice reservas existentes")
    void dividirPaqueteRechazaReservaSinCobertura() {
        Propietario propietario = new Propietario("3001234567", "dueno", "secret123", "123456");
        propietario.setIdUsuario(8);
        CasaRural casa = new CasaRural(15, "Salento", "La Montanita", "Cabana familiar", 1, 1, true);
        casa.setPropietario(propietario);
        PaqueteAlquiler paquete = new PaqueteAlquiler(
                java.sql.Date.valueOf("2026-06-01"),
                java.sql.Date.valueOf("2026-06-10"),
                ModalidadAlquiler.AMBAS,
                900000,
                150000,
                true
        );
        casa.agregarPaqueteAlquiler(paquete);

        Reserva reserva = mock(Reserva.class);
        when(reserva.getEstado()).thenReturn(EstadoReserva.CONFIRMADA);
        when(reserva.getFechaEntrada()).thenReturn(java.sql.Date.valueOf("2026-06-04"));
        when(reserva.getNumeroNoches()).thenReturn(4);
        when(reserva.getTipoReserva()).thenReturn(TipoReserva.CASA_ENTERA);

        List<PaqueteAlquilerDTO> nuevosPaquetes = List.of(
                new PaqueteAlquilerDTO(null, java.sql.Date.valueOf("2026-06-01"),
                        java.sql.Date.valueOf("2026-06-05"), ModalidadAlquiler.CASA_ENTERA, 500000, 0, true),
                new PaqueteAlquilerDTO(null, java.sql.Date.valueOf("2026-06-06"),
                        java.sql.Date.valueOf("2026-06-10"), ModalidadAlquiler.CASA_ENTERA, 500000, 0, true)
        );

        when(casaRuralRepository.findById(15)).thenReturn(Optional.of(casa));
        when(paqueteAlquilerRepository.findById(0)).thenReturn(Optional.of(paquete));
        when(reservaRepository.findByCasaRuralCodigoCasa(15)).thenReturn(List.of(reserva));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> propietarioService.dividirPaquete(15, 8, 0, nuevosPaquetes));

        assertEquals("No se puede dividir el paquete porque contradice reservas existentes", ex.getMessage());
        verify(paqueteAlquilerRepository, never()).delete(any(PaqueteAlquiler.class));
        verify(paqueteAlquilerRepository, never()).saveAll(any());
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

    private List<HabitacionFormDTO> habitacionesValidas() {
        return List.of(
                habitacion("HAB-1", 1, TipoCama.SENCILLA, false),
                habitacion("HAB-2", 2, TipoCama.DOBLE, true),
                habitacion("HAB-3", 1, TipoCama.SENCILLA, false)
        );
    }

    private HabitacionFormDTO habitacion(String codigo, int camas, TipoCama tipoCama, boolean tieneBano) {
        HabitacionFormDTO habitacion = new HabitacionFormDTO();
        habitacion.setCodigoHabitacion(codigo);
        habitacion.setNumeroCamas(camas);
        habitacion.setTipoCama(tipoCama);
        habitacion.setTieneBano(tieneBano);
        return habitacion;
    }

    private List<CocinaFormDTO> cocinasValidas() {
        CocinaFormDTO cocina = new CocinaFormDTO();
        cocina.setTieneLavavajillas(true);
        cocina.setTieneLavadora(false);
        return List.of(cocina);
    }
}
