package co.edu.uniquindio.casasrurales.services;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;
import java.util.Date;

import co.edu.uniquindio.casasrurales.dto.CasaRuralFormDTO;
import co.edu.uniquindio.casasrurales.dto.CasaRuralPropietarioDTO;
import co.edu.uniquindio.casasrurales.dto.CocinaFormDTO;
import co.edu.uniquindio.casasrurales.dto.HabitacionFormDTO;
import co.edu.uniquindio.casasrurales.dto.PagoRegistroDTO;
import co.edu.uniquindio.casasrurales.dto.RegistroCasaForm;
import co.edu.uniquindio.casasrurales.dto.ReservaPropietarioDTO;
import co.edu.uniquindio.casasrurales.entities.Bano;
import co.edu.uniquindio.casasrurales.entities.CasaRural;
import co.edu.uniquindio.casasrurales.entities.Cocina;
import co.edu.uniquindio.casasrurales.entities.Foto;
import co.edu.uniquindio.casasrurales.entities.Habitacion;
import co.edu.uniquindio.casasrurales.entities.Pago;
import co.edu.uniquindio.casasrurales.entities.Propietario;
import co.edu.uniquindio.casasrurales.entities.Reserva;
import co.edu.uniquindio.casasrurales.enums.EstadoPago;
import co.edu.uniquindio.casasrurales.enums.EstadoReserva;
import co.edu.uniquindio.casasrurales.enums.ModalidadAlquiler;
import co.edu.uniquindio.casasrurales.repositories.CasaRuralRepository;
import co.edu.uniquindio.casasrurales.repositories.PropietarioRepository;
import co.edu.uniquindio.casasrurales.repositories.ReservaRepository;
import co.edu.uniquindio.casasrurales.repositories.PaqueteAlquilerRepository;
import co.edu.uniquindio.casasrurales.repositories.PagoRepository;
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
    private final PagoRepository pagoRepository;

    public PropietarioService(PropietarioRepository propietarioRepository,
                              CasaRuralRepository casaRuralRepository,
                              ReservaRepository reservaRepository,
                              PaqueteAlquilerRepository paqueteAlquilerRepository,
                              PagoRepository pagoRepository) {
        this.propietarioRepository = propietarioRepository;
        this.casaRuralRepository = casaRuralRepository;
        this.reservaRepository = reservaRepository;
        this.paqueteAlquilerRepository = paqueteAlquilerRepository;
        this.pagoRepository = pagoRepository;
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
        casa.registrarCreacionPor(propietario);

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
        casa.registrarModificacionPor(idPropietario);

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

        int codigoCasa = resolverCodigoCasa(form.getCodigoCasa());

        if (casaRuralRepository.existsById(codigoCasa)) {
            throw new IllegalArgumentException("Ya existe una casa con ese codigo");
        }

        validarMinimosCreacion(form);
        validarFotosCreacion(form);
        validarHabitacionesCreacion(form);
        validarCocinasCreacion(form);

        Propietario propietario = propietarioOpt.get();
        CasaRural casa = new CasaRural(
                codigoCasa,
                form.getPoblacion().trim(),
                form.getNombrePropiedad().trim(),
                form.getDescripcion() != null ? form.getDescripcion().trim() : null,
                form.getNumComedores(),
                form.getNumPlazasGaraje(),
                true
        );

        agregarEspaciosMinimos(casa, form);
        agregarFotos(casa, form);
        casa.registrarCreacionPor(propietario);
        propietario.darAltaCasa(casa);
        propietarioRepository.save(propietario);

        return "Casa registrada exitosamente con codigo " + codigoCasa;
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
        casa.registrarModificacionPor(idPropietario);

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

        // Validar que no hay reservas activas o pendientes que bloqueen disponibilidad.
        List<Reserva> reservasActivas = reservaRepository.findByCasaRuralCodigoCasa(codigoCasa).stream()
                .filter(r -> r.getEstado() == EstadoReserva.CONFIRMADA || r.getEstado() == EstadoReserva.PENDIENTE_PAGO)
                .collect(Collectors.toList());

        if (!reservasActivas.isEmpty()) {
            throw new IllegalStateException(
                "No puedes dar de baja la casa porque tiene " + reservasActivas.size() + 
                " reserva(s) activa(s). Cancélalas primero."
            );
        }

        // Dar de baja la casa
        casa.setActiva(false);
        casa.registrarModificacionPor(idPropietario);
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
        casa.registrarModificacionPor(idPropietario);
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

        if (form.getNumBanos() == null || form.getNumBanos() < 2) {
            throw new IllegalArgumentException("La casa debe tener minimo 2 banos");
        }

        if (form.getNumCocinas() == null || form.getNumCocinas() < 1) {
            throw new IllegalArgumentException("La casa debe tener minimo 1 cocina");
        }
    }

    private void validarFotosCreacion(CasaRuralFormDTO form) {
        if (form.getUrlsFotos() == null || form.getUrlsFotos().stream().noneMatch(url -> url != null && !url.isBlank())) {
            throw new IllegalArgumentException("Debe registrar al menos una foto de la casa");
        }

        boolean formatoInvalido = form.getUrlsFotos().stream()
                .filter(url -> url != null && !url.isBlank())
                .anyMatch(url -> !tieneFormatoImagenPermitido(url.trim()));
        if (formatoInvalido) {
            throw new IllegalArgumentException("Las fotos deben estar en formato JPG, PNG o WEBP");
        }
    }

    private boolean tieneFormatoImagenPermitido(String rutaFoto) {
        String rutaNormalizada = rutaFoto.toLowerCase();
        return rutaNormalizada.endsWith(".jpg")
                || rutaNormalizada.endsWith(".jpeg")
                || rutaNormalizada.endsWith(".png")
                || rutaNormalizada.endsWith(".webp");
    }

    private void validarHabitacionesCreacion(CasaRuralFormDTO form) {
        if (form.getHabitaciones() == null || form.getHabitaciones().size() != form.getNumHabitaciones()) {
            throw new IllegalArgumentException("Debe registrar los datos de cada habitacion");
        }

        Set<String> codigos = new HashSet<>();
        for (HabitacionFormDTO habitacion : form.getHabitaciones()) {
            if (habitacion == null) {
                throw new IllegalArgumentException("Debe registrar los datos de cada habitacion");
            }

            if (habitacion.getCodigoHabitacion() == null || habitacion.getCodigoHabitacion().isBlank()) {
                throw new IllegalArgumentException("El codigo de cada habitacion es obligatorio");
            }

            String codigoNormalizado = habitacion.getCodigoHabitacion().trim().toUpperCase();
            if (!codigos.add(codigoNormalizado)) {
                throw new IllegalArgumentException("El codigo de habitacion no puede repetirse dentro de la misma casa");
            }

            if (habitacion.getNumeroCamas() == null || habitacion.getNumeroCamas() < 1) {
                throw new IllegalArgumentException("Cada habitacion debe tener al menos una cama");
            }

            if (habitacion.getTipoCama() == null) {
                throw new IllegalArgumentException("El tipo de cama de cada habitacion es obligatorio");
            }

            if (habitacion.getTieneBano() == null) {
                throw new IllegalArgumentException("Debe indicar si cada habitacion tiene bano");
            }
        }
    }

    private void validarCocinasCreacion(CasaRuralFormDTO form) {
        if (form.getCocinas() == null || form.getCocinas().size() != form.getNumCocinas()) {
            throw new IllegalArgumentException("Debe registrar los datos de cada cocina");
        }

        for (CocinaFormDTO cocina : form.getCocinas()) {
            if (cocina == null) {
                throw new IllegalArgumentException("Debe registrar los datos de cada cocina");
            }

            if (cocina.getTieneLavavajillas() == null) {
                throw new IllegalArgumentException("Debe indicar si cada cocina tiene lavavajillas");
            }

            if (cocina.getTieneLavadora() == null) {
                throw new IllegalArgumentException("Debe indicar si cada cocina tiene lavadora");
            }
        }
    }

    private void agregarEspaciosMinimos(CasaRural casa, CasaRuralFormDTO form) {
        form.getHabitaciones().stream()
                .map(habitacion -> new Habitacion(
                        habitacion.getCodigoHabitacion().trim(),
                        habitacion.getNumeroCamas(),
                        habitacion.getTipoCama(),
                        habitacion.getTieneBano()
                ))
                .forEach(casa::agregarHabitacion);

        for (int i = 0; i < form.getNumBanos(); i++) {
            casa.agregarBano(new Bano());
        }

        form.getCocinas().stream()
                .map(cocina -> new Cocina(cocina.getTieneLavavajillas(), cocina.getTieneLavadora()))
                .forEach(casa::agregarCocina);
    }

    private void agregarFotos(CasaRural casa, CasaRuralFormDTO form) {
        form.getUrlsFotos().stream()
                .filter(url -> url != null && !url.isBlank())
                .map(url -> new Foto(url.trim(), null))
                .forEach(casa::agregarFoto);
    }
    /**
     * HU-05: Crear un paquete de alquiler
     */
    @Transactional
    public PaqueteAlquilerDTO crearPaquete(int codigoCasa, int idPropietario, PaqueteAlquilerDTO dto) {
        CasaRural casa = obtenerCasaDelPropietario(codigoCasa, idPropietario);
        validarDatosPaquete(dto);

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
        paquete.registrarCreacionPor(idPropietario);
        
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

    private int resolverCodigoCasa(Integer codigoSolicitado) {
        if (codigoSolicitado != null && codigoSolicitado > 0) {
            return codigoSolicitado;
        }
        return casaRuralRepository.obtenerMayorCodigoCasa() + 1;
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
        validarDatosPaquete(dto);
        validarModificacionNoContradiceReservas(casa, paquete, dto);

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
        paquete.registrarModificacionPor(idPropietario, "Modificacion de paquete de alquiler");

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

        validarEliminacionNoContradiceReservas(casa, paqueteOpt.get());
        
        paqueteAlquilerRepository.delete(paqueteOpt.get());
    }

    /**
     * RN43: Divide un paquete existente en paquetes mas pequenos sin generar doble disponibilidad.
     */
    @Transactional
    public List<PaqueteAlquilerDTO> dividirPaquete(int codigoCasa, int idPropietario, int idPaquete,
                                                   List<PaqueteAlquilerDTO> nuevosPaquetesDto) {
        CasaRural casa = obtenerCasaDelPropietario(codigoCasa, idPropietario);

        Optional<PaqueteAlquiler> paqueteOpt = paqueteAlquilerRepository.findById(idPaquete);
        if (paqueteOpt.isEmpty() || paqueteOpt.get().getCasaRural().getCodigoCasa() != codigoCasa) {
            throw new IllegalArgumentException("Paquete no encontrado para esta casa");
        }

        PaqueteAlquiler paqueteOriginal = paqueteOpt.get();
        validarDivisionPaquete(casa, paqueteOriginal, nuevosPaquetesDto);

        List<PaqueteAlquiler> nuevosPaquetes = nuevosPaquetesDto.stream()
                .map(dto -> {
                    PaqueteAlquiler paquete = new PaqueteAlquiler(
                            dto.getFechaInicio(),
                            dto.getFechaFin(),
                            dto.getModalidad(),
                            dto.getPrecioCasaEntera(),
                            dto.getPrecioHabitacion(),
                            dto.isDisponible()
                    );
                    paquete.setCasaRural(casa);
                    paquete.registrarCreacionPor(idPropietario);
                    return paquete;
                })
                .collect(Collectors.toList());

        paqueteAlquilerRepository.delete(paqueteOriginal);
        List<PaqueteAlquiler> guardados = paqueteAlquilerRepository.saveAll(nuevosPaquetes);

        return guardados.stream()
                .map(this::convertirAPaqueteDTO)
                .collect(Collectors.toList());
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

    private PaqueteAlquilerDTO convertirAPaqueteDTO(PaqueteAlquiler paquete) {
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

    private void validarDatosPaquete(PaqueteAlquilerDTO dto) {
        if (dto.getFechaInicio() == null || dto.getFechaFin() == null) {
            throw new IllegalArgumentException("Las fechas del paquete son obligatorias");
        }

        if (dto.getFechaInicio().after(dto.getFechaFin())) {
            throw new IllegalArgumentException("La fecha de inicio no puede ser posterior a la fecha de fin");
        }

        if (dto.getModalidad() == null) {
            throw new IllegalArgumentException("La modalidad del paquete es obligatoria");
        }

        if (dto.getModalidad() == ModalidadAlquiler.CASA_ENTERA || dto.getModalidad() == ModalidadAlquiler.AMBAS) {
            if (dto.getPrecioCasaEntera() <= 0) {
                throw new IllegalArgumentException("El precio de casa entera debe ser mayor a cero");
            }
        }

        if (dto.getModalidad() == ModalidadAlquiler.POR_HABITACIONES || dto.getModalidad() == ModalidadAlquiler.AMBAS) {
            if (dto.getPrecioHabitacion() <= 0) {
                throw new IllegalArgumentException("El precio por habitacion debe ser mayor a cero");
            }
        }
    }

    private void validarDivisionPaquete(CasaRural casa, PaqueteAlquiler paqueteOriginal,
                                        List<PaqueteAlquilerDTO> nuevosPaquetes) {
        if (nuevosPaquetes == null || nuevosPaquetes.size() < 2) {
            throw new IllegalArgumentException("La division debe contener al menos dos paquetes");
        }

        for (PaqueteAlquilerDTO nuevoPaquete : nuevosPaquetes) {
            validarDatosPaquete(nuevoPaquete);
            if (nuevoPaquete.getFechaInicio().before(paqueteOriginal.getFechaInicio())
                    || nuevoPaquete.getFechaFin().after(paqueteOriginal.getFechaFin())) {
                throw new IllegalArgumentException("Los paquetes resultantes deben estar dentro del rango del paquete original");
            }
        }

        validarPaquetesResultantesNoSeSolapan(nuevosPaquetes);
        validarDivisionNoSolapaOtrosPaquetes(casa, paqueteOriginal, nuevosPaquetes);
        validarDivisionNoContradiceReservas(casa, paqueteOriginal, nuevosPaquetes);
    }

    private void validarPaquetesResultantesNoSeSolapan(List<PaqueteAlquilerDTO> nuevosPaquetes) {
        for (int i = 0; i < nuevosPaquetes.size(); i++) {
            for (int j = i + 1; j < nuevosPaquetes.size(); j++) {
                if (fechasSeCruzan(
                        nuevosPaquetes.get(i).getFechaInicio(),
                        nuevosPaquetes.get(i).getFechaFin(),
                        nuevosPaquetes.get(j).getFechaInicio(),
                        nuevosPaquetes.get(j).getFechaFin())) {
                    throw new IllegalArgumentException("Los paquetes resultantes no pueden solaparse entre si");
                }
            }
        }
    }

    private void validarDivisionNoSolapaOtrosPaquetes(CasaRural casa, PaqueteAlquiler paqueteOriginal,
                                                       List<PaqueteAlquilerDTO> nuevosPaquetes) {
        boolean solapaOtroPaquete = casa.getPaquetesAlquiler().stream()
                .filter(paquete -> paquete != paqueteOriginal)
                .anyMatch(paquete -> nuevosPaquetes.stream().anyMatch(nuevo ->
                        fechasSeCruzan(nuevo.getFechaInicio(), nuevo.getFechaFin(),
                                paquete.getFechaInicio(), paquete.getFechaFin())));

        if (solapaOtroPaquete) {
            throw new IllegalArgumentException("Los paquetes resultantes se solapan con otro paquete existente de esta casa");
        }
    }

    private void validarDivisionNoContradiceReservas(CasaRural casa, PaqueteAlquiler paqueteOriginal,
                                                     List<PaqueteAlquilerDTO> nuevosPaquetes) {
        List<Reserva> reservasAfectadas = reservasVigentesCubiertasPorPaquete(casa, paqueteOriginal);
        for (Reserva reserva : reservasAfectadas) {
            boolean reservaSoportada = nuevosPaquetes.stream()
                    .anyMatch(paquete -> paquete.isDisponible()
                            && paqueteCubreReserva(paquete.getFechaInicio(), paquete.getFechaFin(), reserva)
                            && modalidadSoportaReserva(paquete.getModalidad(), reserva));

            if (!reservaSoportada) {
                throw new IllegalStateException("No se puede dividir el paquete porque contradice reservas existentes");
            }
        }
    }

    private void validarModificacionNoContradiceReservas(CasaRural casa, PaqueteAlquiler paqueteActual,
                                                          PaqueteAlquilerDTO dto) {
        List<Reserva> reservasAfectadas = reservasVigentesCubiertasPorPaquete(casa, paqueteActual);
        for (Reserva reserva : reservasAfectadas) {
            if (!dto.isDisponible()) {
                throw new IllegalStateException("No se puede ocultar un paquete con reservas existentes");
            }

            if (!paqueteCubreReserva(dto.getFechaInicio(), dto.getFechaFin(), reserva)) {
                throw new IllegalStateException("No se puede modificar el paquete porque dejaria reservas sin disponibilidad");
            }

            if (!modalidadSoportaReserva(dto.getModalidad(), reserva)) {
                throw new IllegalStateException("No se puede cambiar la modalidad porque contradice reservas existentes");
            }
        }
    }

    private void validarEliminacionNoContradiceReservas(CasaRural casa, PaqueteAlquiler paquete) {
        if (!reservasVigentesCubiertasPorPaquete(casa, paquete).isEmpty()) {
            throw new IllegalStateException("No se puede eliminar un paquete con reservas existentes");
        }
    }

    private List<Reserva> reservasVigentesCubiertasPorPaquete(CasaRural casa, PaqueteAlquiler paquete) {
        return reservaRepository.findByCasaRuralCodigoCasa(casa.getCodigoCasa()).stream()
                .filter(reserva -> reserva.getEstado() != EstadoReserva.ANULADA)
                .filter(reserva -> fechasSeCruzan(
                        paquete.getFechaInicio(),
                        paquete.getFechaFin(),
                        reserva.getFechaEntrada(),
                        calcularFechaFinReserva(reserva)))
                .collect(Collectors.toList());
    }

    private boolean paqueteCubreReserva(Date fechaInicioPaquete, Date fechaFinPaquete, Reserva reserva) {
        Date fechaFinReserva = calcularFechaFinReserva(reserva);
        return !reserva.getFechaEntrada().before(fechaInicioPaquete) && !fechaFinReserva.after(fechaFinPaquete);
    }

    private boolean modalidadSoportaReserva(ModalidadAlquiler modalidad, Reserva reserva) {
        return switch (reserva.getTipoReserva()) {
            case CASA_ENTERA -> modalidad == ModalidadAlquiler.CASA_ENTERA || modalidad == ModalidadAlquiler.AMBAS;
            case POR_HABITACIONES -> modalidad == ModalidadAlquiler.POR_HABITACIONES || modalidad == ModalidadAlquiler.AMBAS;
        };
    }

    private boolean fechasSeCruzan(Date inicioA, Date finA, Date inicioB, Date finB) {
        return !finA.before(inicioB) && !finB.before(inicioA);
    }

    private Date calcularFechaFinReserva(Reserva reserva) {
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        calendar.setTime(reserva.getFechaEntrada());
        calendar.add(java.util.Calendar.DAY_OF_MONTH, reserva.getNumeroNoches() - 1);
        return calendar.getTime();
    }

    public List<ReservaPropietarioDTO> obtenerReservasPropietario(int idPropietario) {
        validarPropietarioExiste(idPropietario);
        return reservaRepository.findAll().stream()
                .filter(reserva -> perteneceAlPropietario(reserva, idPropietario))
                .map(this::convertirAReservaPropietarioDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public List<ReservaPropietarioDTO> obtenerReservasVencidas(int idPropietario) {
        validarPropietarioExiste(idPropietario);
        return reservaRepository.findAll().stream()
                .filter(reserva -> perteneceAlPropietario(reserva, idPropietario))
                .peek(this::marcarVencidaSiCorresponde)
                .filter(Reserva::estaVencida)
                .map(this::convertirAReservaPropietarioDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public ReservaPropietarioDTO registrarPagoReserva(int numeroReserva, int idPropietario, PagoRegistroDTO dto) {
        Reserva reserva = obtenerReservaDelPropietario(numeroReserva, idPropietario);
        if (reserva.getEstado() == EstadoReserva.ANULADA) {
            throw new IllegalStateException("No se puede registrar pago a una reserva anulada");
        }

        double monto = dto.getMonto() != null ? dto.getMonto() : 0;
        if (monto <= 0) {
            throw new IllegalArgumentException("El monto debe ser mayor a cero");
        }

        Pago pago = new Pago(dto.getFechaPago() != null ? dto.getFechaPago() : new Date(), monto, EstadoPago.PENDIENTE);
        pago.registrar();
        reserva.agregarPago(pago);
        reserva.confirmar();
        marcarVencidaSiCorresponde(reserva);

        pagoRepository.save(pago);
        reservaRepository.save(reserva);
        return convertirAReservaPropietarioDTO(reserva);
    }

    @Transactional
    public ReservaPropietarioDTO anularReservaVencida(int numeroReserva, int idPropietario) {
        Reserva reserva = obtenerReservaDelPropietario(numeroReserva, idPropietario);
        if (!reserva.estaVencida()) {
            throw new IllegalStateException("La reserva no esta vencida");
        }

        reserva.cancelar();
        reservaRepository.save(reserva);
        return convertirAReservaPropietarioDTO(reserva);
    }

    @Transactional
    public ReservaPropietarioDTO mantenerReservaVencida(int numeroReserva, int idPropietario) {
        Reserva reserva = obtenerReservaDelPropietario(numeroReserva, idPropietario);
        if (!reserva.estaVencida()) {
            throw new IllegalStateException("La reserva no esta vencida");
        }
        return convertirAReservaPropietarioDTO(reserva);
    }

    private void marcarVencidaSiCorresponde(Reserva reserva) {
        if (reserva.marcarVencidaSiCorresponde()) {
            reservaRepository.save(reserva);
        }
    }

    private void validarPropietarioExiste(int idPropietario) {
        if (propietarioRepository.findById(idPropietario).isEmpty()) {
            throw new IllegalArgumentException("Propietario no encontrado");
        }
    }

    private Reserva obtenerReservaDelPropietario(int numeroReserva, int idPropietario) {
        Reserva reserva = reservaRepository.findById(numeroReserva)
                .orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada"));
        if (!perteneceAlPropietario(reserva, idPropietario)) {
            throw new IllegalArgumentException("No tienes permiso para gestionar esta reserva");
        }
        return reserva;
    }

    private boolean perteneceAlPropietario(Reserva reserva, int idPropietario) {
        return reserva.getCasaRural() != null
                && reserva.getCasaRural().getPropietario() != null
                && reserva.getCasaRural().getPropietario().getIdUsuario() == idPropietario;
    }

    private ReservaPropietarioDTO convertirAReservaPropietarioDTO(Reserva reserva) {
        CasaRural casa = reserva.getCasaRural();
        return new ReservaPropietarioDTO(
                reserva.getNumeroReserva(),
                casa != null ? casa.getCodigoCasa() : 0,
                casa != null ? casa.getNombrePropiedad() : "",
                casa != null ? casa.getPoblacion() : "",
                reserva.getFechaReserva(),
                reserva.getFechaEntrada(),
                reserva.getNumeroNoches(),
                reserva.getFechaLimitePago(),
                reserva.getImporteTotal(),
                reserva.getImporteAnticipo(),
                reserva.getEstado(),
                reserva.getTipoReserva(),
                reserva.estaVencida()
        );
    }
}

