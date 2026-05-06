package co.edu.uniquindio.casasrurales.services;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import co.edu.uniquindio.casasrurales.dto.CasaRuralFormDTO;
import co.edu.uniquindio.casasrurales.dto.CasaRuralPropietarioDTO;
import co.edu.uniquindio.casasrurales.dto.RegistroCasaForm;
import co.edu.uniquindio.casasrurales.entities.Bano;
import co.edu.uniquindio.casasrurales.entities.CasaRural;
import co.edu.uniquindio.casasrurales.entities.Cocina;
import co.edu.uniquindio.casasrurales.entities.Habitacion;
import co.edu.uniquindio.casasrurales.entities.Propietario;
import co.edu.uniquindio.casasrurales.entities.Reserva;
import co.edu.uniquindio.casasrurales.enums.EstadoReserva;
import co.edu.uniquindio.casasrurales.enums.TipoCama;
import co.edu.uniquindio.casasrurales.repositories.CasaRuralRepository;
import co.edu.uniquindio.casasrurales.repositories.PropietarioRepository;
import co.edu.uniquindio.casasrurales.repositories.ReservaRepository;
import co.edu.uniquindio.casasrurales.repositories.PaqueteAlquilerRepository;
import co.edu.uniquindio.casasrurales.entities.PaqueteAlquiler;
import co.edu.uniquindio.casasrurales.dto.PaqueteAlquilerDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio que maneja operaciones relacionadas con propietarios.
 * Incluye gestión de casas, baja de propiedades, y validaciones de autorización.
 */
@Service
public class PropietarioService {

    private final PropietarioRepository propietarioRepository;
    private final CasaRuralRepository casaRuralRepository;
    private final ReservaRepository reservaRepository;
    private final PaqueteAlquilerRepository paqueteAlquilerRepository;

    public PropietarioService(PropietarioRepository propietarioRepository,
                              CasaRuralRepository casaRuralRepository,
                              ReservaRepository reservaRepository,
                              PaqueteAlquilerRepository paqueteAlquilerRepository) {
        this.propietarioRepository = propietarioRepository;
        this.casaRuralRepository = casaRuralRepository;
        this.reservaRepository = reservaRepository;
        this.paqueteAlquilerRepository = paqueteAlquilerRepository;
    }

    /**
     * Crea una nueva casa asociada al propietario.
     */
    @Transactional
    public CasaRuralPropietarioDTO crearCasa(RegistroCasaForm form, int idPropietario) {
        Optional<Propietario> propietarioOpt = propietarioRepository.findById(idPropietario);
        if (propietarioOpt.isEmpty()) {
            throw new IllegalArgumentException("Propietario no encontrado");
        }

        if (casaRuralRepository.existsById(form.getCodigoCasa())) {
            throw new IllegalArgumentException("Ya existe una casa con ese código");
        }

        CasaRural casa = new CasaRural(
                form.getCodigoCasa(),
                form.getPoblacion(),
                form.getNombrePropiedad(),
                form.getDescripcionGeneral(),
                form.getNumComedores(),
                form.getNumPlazasGaraje(),
                true
        );

        Propietario propietario = propietarioOpt.get();
        casa.setPropietario(propietario);

        casaRuralRepository.save(casa);

        return convertirACasaDTO(casa);
    }

    /**
     * Actualiza los campos editables de una casa si el propietario es el dueño.
     */
    @Transactional
    public CasaRuralPropietarioDTO actualizarCasa(int codigoCasa, RegistroCasaForm form, int idPropietario) {
        Optional<Propietario> propietarioOpt = propietarioRepository.findById(idPropietario);
        if (propietarioOpt.isEmpty()) {
            throw new IllegalArgumentException("Propietario no encontrado");
        }

        Optional<CasaRural> casaOpt = casaRuralRepository.findById(codigoCasa);
        if (casaOpt.isEmpty()) {
            throw new IllegalArgumentException("Casa no encontrada");
        }

        CasaRural casa = casaOpt.get();
        if (casa.getPropietario().getIdUsuario() != idPropietario) {
            throw new IllegalArgumentException("No tienes permiso para actualizar esta casa");
        }

        // Actualizar campos permitidos
        casa.setNombrePropiedad(form.getNombrePropiedad());
        casa.setPoblacion(form.getPoblacion());
        casa.setDescripcionGeneral(form.getDescripcionGeneral());
        casa.setNumComedores(form.getNumComedores());
        casa.setNumPlazasGaraje(form.getNumPlazasGaraje());

        casaRuralRepository.save(casa);

        return convertirACasaDTO(casa);
    }

    /**
     * Obtiene todas las casas de un propietario específico.
     * 
     * @param idPropietario ID del propietario
     * @return Lista de casas con información de reservas
     */
    public List<CasaRuralPropietarioDTO> obtenerCasasPropietario(int idPropietario) {
        Optional<Propietario> propietarioOpt = propietarioRepository.findById(idPropietario);
        
        if (propietarioOpt.isEmpty()) {
            throw new IllegalArgumentException("Propietario no encontrado");
        }

        Propietario propietario = propietarioOpt.get();
        
        return propietario.getCasas().stream()
                .map(casa -> convertirACasaDTO(casa))
                .collect(Collectors.toList());
    }

    /**
     * Obtiene una casa puntual del propietario para precargar el formulario de edicion.
     */
    public CasaRuralPropietarioDTO obtenerCasaPropietario(int codigoCasa, int idPropietario) {
        CasaRural casa = obtenerCasaDelPropietario(codigoCasa, idPropietario);
        return convertirACasaDTO(casa);
    }

    /**
     * Registra una nueva casa para el propietario autenticado.
     */
    @Transactional
    public String crearCasa(CasaRuralFormDTO form, int idPropietario) {
        Optional<Propietario> propietarioOpt = propietarioRepository.findById(idPropietario);
        if (propietarioOpt.isEmpty()) {
            throw new IllegalArgumentException("Propietario no encontrado");
        }

        if (casaRuralRepository.existsById(form.getCodigoCasa())) {
            throw new IllegalArgumentException("Ya existe una casa con ese codigo");
        }

        validarMinimosCreacion(form);

        Propietario propietario = propietarioOpt.get();
        CasaRural casa = new CasaRural(
                form.getCodigoCasa(),
                form.getPoblacion().trim(),
                form.getNombrePropiedad().trim(),
                form.getDescripcion() != null ? form.getDescripcion().trim() : null,
                form.getNumComedores(),
                form.getNumPlazasGaraje(),
                true
        );

        agregarEspaciosMinimos(casa, form);
        propietario.darAltaCasa(casa);
        propietarioRepository.save(propietario);

        return "Casa registrada exitosamente";
    }

    /**
     * Edita los datos generales de una casa del propietario autenticado.
     */
    @Transactional
    public String editarCasa(int codigoCasa, CasaRuralFormDTO form, int idPropietario) {
        CasaRural casa = obtenerCasaDelPropietario(codigoCasa, idPropietario);

        if (form.getCodigoCasa() != null && form.getCodigoCasa() != codigoCasa) {
            throw new IllegalArgumentException("El codigo de la casa no se puede modificar");
        }

        casa.setNombrePropiedad(form.getNombrePropiedad().trim());
        casa.setPoblacion(form.getPoblacion().trim());
        casa.setDescripcionGeneral(form.getDescripcion() != null ? form.getDescripcion().trim() : null);
        casa.setNumComedores(form.getNumComedores());
        casa.setNumPlazasGaraje(form.getNumPlazasGaraje());

        casaRuralRepository.save(casa);
        return "Casa actualizada exitosamente";
    }

    /**
     * Da de baja una casa rural.
     * Valida que:
     * - El propietario sea el dueño de la casa
     * - No haya reservas activas
     * - La casa exista
     * 
     * @param codigoCasa Código de la casa a dar de baja
     * @param idPropietario ID del propietario que solicita la baja
     * @return Mensaje de confirmación
     */
    @Transactional
    public String darDeBajaCasa(int codigoCasa, int idPropietario) {
        // Validar que el propietario existe
        Optional<Propietario> propietarioOpt = propietarioRepository.findById(idPropietario);
        if (propietarioOpt.isEmpty()) {
            throw new IllegalArgumentException("Propietario no encontrado");
        }

        // Validar que la casa existe
        Optional<CasaRural> casaOpt = casaRuralRepository.findById(codigoCasa);
        if (casaOpt.isEmpty()) {
            throw new IllegalArgumentException("Casa no encontrada");
        }

        CasaRural casa = casaOpt.get();
        Propietario propietario = propietarioOpt.get();

        // Validar que el propietario es el dueño
        if (casa.getPropietario().getIdUsuario() != idPropietario) {
            throw new IllegalArgumentException("No tienes permiso para dar de baja esta casa");
        }

        // Validar que no hay reservas activas (confirmadas)
        List<Reserva> reservasActivas = reservaRepository.findByCasaRuralCodigoCasa(codigoCasa).stream()
                .filter(r -> r.getEstado() == EstadoReserva.CONFIRMADA)
                .collect(Collectors.toList());

        if (!reservasActivas.isEmpty()) {
            throw new IllegalStateException(
                "No puedes dar de baja la casa porque tiene " + reservasActivas.size() + 
                " reserva(s) activa(s). Cancélalas primero."
            );
        }

        // Dar de baja la casa
        casa.setActiva(false);
        casaRuralRepository.save(casa);

        return "Casa dada de baja exitosamente";
    }

    /**
     * Reactiva una casa que fue dada de baja.
     * Solo el propietario dueño puede reactivar.
     * 
     * @param codigoCasa Código de la casa
     * @param idPropietario ID del propietario
     * @return Mensaje de confirmación
     */
    @Transactional
    public String reactivarCasa(int codigoCasa, int idPropietario) {
        Optional<Propietario> propietarioOpt = propietarioRepository.findById(idPropietario);
        if (propietarioOpt.isEmpty()) {
            throw new IllegalArgumentException("Propietario no encontrado");
        }

        Optional<CasaRural> casaOpt = casaRuralRepository.findById(codigoCasa);
        if (casaOpt.isEmpty()) {
            throw new IllegalArgumentException("Casa no encontrada");
        }

        CasaRural casa = casaOpt.get();

        if (casa.getPropietario().getIdUsuario() != idPropietario) {
            throw new IllegalArgumentException("No tienes permiso para reactivar esta casa");
        }

        casa.setActiva(true);
        casaRuralRepository.save(casa);

        return "Casa reactivada exitosamente";
    }

    /**
     * Convierte una CasaRural a DTO con información de reservas.
     */
    private CasaRuralPropietarioDTO convertirACasaDTO(CasaRural casa) {
        List<Reserva> todasLasReservas = reservaRepository.findByCasaRuralCodigoCasa(casa.getCodigoCasa());
        long reservasActivas = todasLasReservas.stream()
                .filter(r -> r.getEstado() == EstadoReserva.CONFIRMADA || r.getEstado() == EstadoReserva.PENDIENTE_PAGO)
                .count();

        return new CasaRuralPropietarioDTO(
            casa.getCodigoCasa(),
            casa.getNombrePropiedad(),
            casa.getPoblacion(),
            casa.getDescripcionGeneral(),
            casa.getNumDormitorios(),
            casa.getNumBanos(),
            casa.getNumComedores(),
            0, // salas - no está en la entidad actual
            casa.getNumCocinas(),
            casa.getNumPlazasGaraje(),
            0, // precio aproximado - revisar entidad
            casa.isActiva(),
            todasLasReservas.size(),
            (int) reservasActivas
        );
    }

    private CasaRural obtenerCasaDelPropietario(int codigoCasa, int idPropietario) {
        Optional<CasaRural> casaOpt = casaRuralRepository.findById(codigoCasa);
        if (casaOpt.isEmpty()) {
            throw new IllegalArgumentException("Casa no encontrada");
        }

        CasaRural casa = casaOpt.get();
        if (casa.getPropietario() == null || casa.getPropietario().getIdUsuario() != idPropietario) {
            throw new IllegalArgumentException("No tienes permiso para editar esta casa");
        }

        return casa;
    }

    private void validarMinimosCreacion(CasaRuralFormDTO form) {
        if (form.getNumHabitaciones() == null || form.getNumHabitaciones() < 3) {
            throw new IllegalArgumentException("La casa debe tener minimo 3 habitaciones");
        }

        if (form.getNumBanos() == null || form.getNumBanos() < 1) {
            throw new IllegalArgumentException("La casa debe tener minimo 1 bano");
        }

        if (form.getNumCocinas() == null || form.getNumCocinas() < 1) {
            throw new IllegalArgumentException("La casa debe tener minimo 1 cocina");
        }
    }

    private void agregarEspaciosMinimos(CasaRural casa, CasaRuralFormDTO form) {
        for (int i = 1; i <= form.getNumHabitaciones(); i++) {
            casa.agregarHabitacion(new Habitacion("HAB-" + i, 1, TipoCama.SENCILLA, false));
        }

        for (int i = 0; i < form.getNumBanos(); i++) {
            casa.agregarBano(new Bano());
        }

        for (int i = 0; i < form.getNumCocinas(); i++) {
            casa.agregarCocina(new Cocina());
        }
    }
    /**
     * HU-05: Crear un paquete de alquiler
     */
    @Transactional
    public PaqueteAlquilerDTO crearPaquete(int codigoCasa, int idPropietario, PaqueteAlquilerDTO dto) {
        CasaRural casa = obtenerCasaDelPropietario(codigoCasa, idPropietario);

        // Validar fechas
        if (dto.getFechaInicio().after(dto.getFechaFin())) {
            throw new IllegalArgumentException("La fecha de inicio no puede ser posterior a la fecha de fin");
        }

        // Validar solapamiento
        boolean solapamiento = casa.getPaquetesAlquiler().stream().anyMatch(p -> 
            (dto.getFechaInicio().before(p.getFechaFin()) || dto.getFechaInicio().equals(p.getFechaFin())) &&
            (dto.getFechaFin().after(p.getFechaInicio()) || dto.getFechaFin().equals(p.getFechaInicio()))
        );

        if (solapamiento) {
            throw new IllegalArgumentException("Las fechas se solapan con un paquete existente de esta casa");
        }

        PaqueteAlquiler paquete = new PaqueteAlquiler(
                dto.getFechaInicio(),
                dto.getFechaFin(),
                dto.getModalidad(),
                dto.getPrecioCasaEntera(),
                dto.getPrecioHabitacion(),
                dto.isDisponible()
        );
        paquete.setCasaRural(casa);
        
        paqueteAlquilerRepository.save(paquete);
        
        return new PaqueteAlquilerDTO(
                paquete.getIdPaquete(),
                paquete.getFechaInicio(),
                paquete.getFechaFin(),
                paquete.getModalidad(),
                paquete.getPrecioCasaEntera(),
                paquete.getPrecioHabitacion(),
                paquete.isDisponible()
        );
    }

    /**
     * HU-05: Modificar un paquete de alquiler
     */
    @Transactional
    public PaqueteAlquilerDTO modificarPaquete(int codigoCasa, int idPropietario, int idPaquete, PaqueteAlquilerDTO dto) {
        CasaRural casa = obtenerCasaDelPropietario(codigoCasa, idPropietario);
        
        Optional<PaqueteAlquiler> paqueteOpt = paqueteAlquilerRepository.findById(idPaquete);
        if (paqueteOpt.isEmpty() || paqueteOpt.get().getCasaRural().getCodigoCasa() != codigoCasa) {
            throw new IllegalArgumentException("Paquete no encontrado para esta casa");
        }
        
        PaqueteAlquiler paquete = paqueteOpt.get();

        // Validar fechas
        if (dto.getFechaInicio().after(dto.getFechaFin())) {
            throw new IllegalArgumentException("La fecha de inicio no puede ser posterior a la fecha de fin");
        }

        // Validar solapamiento excluyendo el paquete actual
        boolean solapamiento = casa.getPaquetesAlquiler().stream()
            .filter(p -> p.getIdPaquete() != idPaquete)
            .anyMatch(p -> 
                (dto.getFechaInicio().before(p.getFechaFin()) || dto.getFechaInicio().equals(p.getFechaFin())) &&
                (dto.getFechaFin().after(p.getFechaInicio()) || dto.getFechaFin().equals(p.getFechaInicio()))
            );

        if (solapamiento) {
            throw new IllegalArgumentException("Las fechas se solapan con otro paquete existente de esta casa");
        }

        paquete.modificar(
                dto.getFechaInicio(),
                dto.getFechaFin(),
                dto.getModalidad(),
                dto.getPrecioCasaEntera(),
                dto.getPrecioHabitacion(),
                dto.isDisponible()
        );

        paqueteAlquilerRepository.save(paquete);

        return new PaqueteAlquilerDTO(
                paquete.getIdPaquete(),
                paquete.getFechaInicio(),
                paquete.getFechaFin(),
                paquete.getModalidad(),
                paquete.getPrecioCasaEntera(),
                paquete.getPrecioHabitacion(),
                paquete.isDisponible()
        );
    }

    /**
     * HU-05: Eliminar un paquete de alquiler
     */
    @Transactional
    public void eliminarPaquete(int codigoCasa, int idPropietario, int idPaquete) {
        CasaRural casa = obtenerCasaDelPropietario(codigoCasa, idPropietario);
        
        Optional<PaqueteAlquiler> paqueteOpt = paqueteAlquilerRepository.findById(idPaquete);
        if (paqueteOpt.isEmpty() || paqueteOpt.get().getCasaRural().getCodigoCasa() != codigoCasa) {
            throw new IllegalArgumentException("Paquete no encontrado para esta casa");
        }
        
        paqueteAlquilerRepository.delete(paqueteOpt.get());
    }

    /**
     * Obtener paquetes de una casa
     */
    public List<PaqueteAlquilerDTO> obtenerPaquetesCasa(int codigoCasa, int idPropietario) {
        CasaRural casa = obtenerCasaDelPropietario(codigoCasa, idPropietario);
        
        return casa.getPaquetesAlquiler().stream()
                .map(paquete -> new PaqueteAlquilerDTO(
                        paquete.getIdPaquete(),
                        paquete.getFechaInicio(),
                        paquete.getFechaFin(),
                        paquete.getModalidad(),
                        paquete.getPrecioCasaEntera(),
                        paquete.getPrecioHabitacion(),
                        paquete.isDisponible()
                ))
                .collect(Collectors.toList());
    }
}

