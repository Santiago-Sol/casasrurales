package co.edu.uniquindio.casasrurales.services;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import co.edu.uniquindio.casasrurales.dto.BanoDetalleDTO;
import co.edu.uniquindio.casasrurales.dto.CasaRuralDetalleDTO;
import co.edu.uniquindio.casasrurales.dto.CasaRuralListadoDTO;
import co.edu.uniquindio.casasrurales.dto.CocinaDetalleDTO;
import co.edu.uniquindio.casasrurales.dto.DisponibilidadCasaDTO;
import co.edu.uniquindio.casasrurales.dto.DisponibilidadDiaDTO;
import co.edu.uniquindio.casasrurales.dto.ResultadoBusquedaCasasDTO;
import co.edu.uniquindio.casasrurales.dto.HabitacionDetalleDTO;
import co.edu.uniquindio.casasrurales.entities.Bano;
import co.edu.uniquindio.casasrurales.entities.CasaRural;
import co.edu.uniquindio.casasrurales.entities.Cocina;
import co.edu.uniquindio.casasrurales.entities.Foto;
import co.edu.uniquindio.casasrurales.entities.Habitacion;
import co.edu.uniquindio.casasrurales.enums.EstadoDisponibilidad;
import co.edu.uniquindio.casasrurales.repositories.BanoRepository;
import co.edu.uniquindio.casasrurales.repositories.CasaRuralRepository;
import co.edu.uniquindio.casasrurales.repositories.CocinaRepository;
import co.edu.uniquindio.casasrurales.repositories.FotoRepository;
import co.edu.uniquindio.casasrurales.repositories.HabitacionRepository;
import jakarta.transaction.Transactional;

/**
 * Servicio de búsqueda de casas rurales.
 * Implementa la lógica para búsqueda por población y obtención de detalles.
 * Solo devuelve casas con al menos un paquete de alquiler activo.
 */
@Service
@Transactional
public class BusquedaCasasService {

    private final CasaRuralRepository casaRuralRepository;
    private final HabitacionRepository habitacionRepository;
    private final CocinaRepository cocinaRepository;
    private final BanoRepository banoRepository;
    private final FotoRepository fotoRepository;
    private final SistemaReservas sistemaReservas;

    public BusquedaCasasService(CasaRuralRepository casaRuralRepository,
                               HabitacionRepository habitacionRepository,
                               CocinaRepository cocinaRepository,
                               BanoRepository banoRepository,
                               FotoRepository fotoRepository,
                               SistemaReservas sistemaReservas) {
        this.casaRuralRepository = casaRuralRepository;
        this.habitacionRepository = habitacionRepository;
        this.cocinaRepository = cocinaRepository;
        this.banoRepository = banoRepository;
        this.fotoRepository = fotoRepository;
        this.sistemaReservas = sistemaReservas;
    }

    /**
     * Busca casas rurales activas por población.
     * 
     * @param poblacion la población donde buscar
     * @return lista de DTOs con casas disponibles; lista vacía si no hay resultados
     */
    public List<CasaRuralListadoDTO> buscarCasasPorPoblacion(String poblacion) {
        if (poblacion == null || poblacion.trim().isEmpty()) {
            return new ArrayList<>();
        }

        List<CasaRural> casas = casaRuralRepository.findByPoblacionIgnoreCase(poblacion.trim());
        
        return casas.stream()
                .filter(CasaRural::isActiva)
                .filter(casa -> casa.getPaquetesAlquiler().stream().anyMatch(paquete -> paquete.isDisponible()))
                .map(this::convertirACasaListadoDTO)
                .collect(Collectors.toList());
    }

    /**
     * Lista casas activas con paquetes disponibles, aplicando filtros opcionales.
     */
    public List<CasaRuralListadoDTO> buscarCasasDisponibles(String poblacion, Date fechaEntrada,
                                                            Integer numeroNoches, Integer huespedes) {
        return buscarCasasDisponibles(poblacion, fechaEntrada, numeroNoches, huespedes, null);
    }

    public List<CasaRuralListadoDTO> buscarCasasDisponibles(String poblacion, Date fechaEntrada,
                                                            Integer numeroNoches, Integer huespedes,
                                                            Integer habitaciones) {
        List<CasaRural> casas = casaRuralRepository.findByActivaTrue();
        String poblacionNormalizada = poblacion == null ? "" : poblacion.trim();
        int huespedesSolicitados = huespedes == null ? 0 : Math.max(0, huespedes);
        int habitacionesMinimas = habitaciones == null ? 0 : Math.max(0, habitaciones);
        int nochesSolicitadas = numeroNoches == null ? 0 : numeroNoches;
        boolean filtrarPorFechas = fechaEntrada != null && nochesSolicitadas > 0;

        return casas.stream()
                .filter(casa -> casa.getPaquetesAlquiler().stream().anyMatch(paquete -> paquete.isDisponible()))
                .filter(casa -> poblacionNormalizada.isEmpty()
                        || casa.getPoblacion().equalsIgnoreCase(poblacionNormalizada))
                .filter(casa -> habitacionesMinimas == 0 || casa.getHabitaciones().size() >= habitacionesMinimas)
                .filter(casa -> huespedesSolicitados == 0 || capacidadHuespedes(casa) >= huespedesSolicitados)
                .filter(casa -> !filtrarPorFechas
                        || estaDisponible(casa, fechaEntrada, nochesSolicitadas, huespedesSolicitados))
                .map(this::convertirACasaListadoDTO)
                .collect(Collectors.toList());
    }

    public ResultadoBusquedaCasasDTO buscarCasasDisponiblesPaginadas(String poblacion, Date fechaEntrada,
                                                                     Integer numeroNoches, Integer huespedes,
                                                                     Integer habitaciones, int pagina, int tamano) {
        int paginaSegura = Math.max(0, pagina);
        int tamanoSeguro = Math.max(1, Math.min(tamano, 24));
        List<CasaRuralListadoDTO> casas = buscarCasasDisponibles(
                poblacion, fechaEntrada, numeroNoches, huespedes, habitaciones);

        int totalElementos = casas.size();
        int totalPaginas = totalElementos == 0 ? 0 : (int) Math.ceil((double) totalElementos / tamanoSeguro);
        int desde = Math.min(paginaSegura * tamanoSeguro, totalElementos);
        int hasta = Math.min(desde + tamanoSeguro, totalElementos);

        return new ResultadoBusquedaCasasDTO(
                casas.subList(desde, hasta),
                paginaSegura,
                tamanoSeguro,
                totalElementos,
                totalPaginas
        );
    }

    /**
     * Obtiene los detalles completos de una casa rural por su código.
     * 
     * @param codigoCasa el código único de la casa
     * @return DTO con todos los detalles de la casa
     * @throws IllegalArgumentException si la casa no existe o no esta activa
     */
    public CasaRuralDetalleDTO obtenerDetalleCasa(int codigoCasa) {
        Optional<CasaRural> casaOpt = casaRuralRepository.findById(codigoCasa);
        
        if (casaOpt.isEmpty()) {
            throw new IllegalArgumentException("La casa con código " + codigoCasa + " no existe");
        }

        CasaRural casa = casaOpt.get();
        
        if (!casa.isActiva()) {
            throw new IllegalArgumentException("La casa no está disponible para consultar");
        }

        return convertirACasaDetalleDTO(casa);
    }

    /**
     * Busca casas por código (búsqueda directa por identificador).
     * 
     * @param codigoCasa el código de la casa
     * @return DTO si existe y está activa, Optional vacío en caso contrario
     */
    public Optional<CasaRuralDetalleDTO> buscarCasaPorCodigo(int codigoCasa) {
        try {
            CasaRuralDetalleDTO detalle = obtenerDetalleCasa(codigoCasa);
            return Optional.of(detalle);
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    /**
     * Obtiene los paquetes de alquiler disponibles para una casa.
     */
    public List<co.edu.uniquindio.casasrurales.dto.PaqueteAlquilerDTO> obtenerPaquetesCasa(int codigoCasa) {
        Optional<CasaRural> casaOpt = casaRuralRepository.findById(codigoCasa);
        if (casaOpt.isEmpty() || !casaOpt.get().isActiva()) {
            throw new IllegalArgumentException("Casa no encontrada o inactiva");
        }
        return casaOpt.get().getPaquetesAlquiler().stream()
                .filter(p -> p.isDisponible())
                .map(paquete -> new co.edu.uniquindio.casasrurales.dto.PaqueteAlquilerDTO(
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

    // === Métodos privados de utilidad ===

    /**
     * Convierte una entidad CasaRural a CasaRuralListadoDTO.
     */
    private CasaRuralListadoDTO convertirACasaListadoDTO(CasaRural casa) {
        int numDormitorios = (int) casa.getHabitaciones().size();
        int numBanos = (int) casa.getBanos().size();
        int numCocinas = (int) casa.getCocinas().size();
        int capacidadHuespedes = capacidadHuespedes(casa);
        
        return new CasaRuralListadoDTO(
                casa.getCodigoCasa(),
                casa.getNombrePropiedad(),
                casa.getPoblacion(),
                numDormitorios,
                numBanos,
                numCocinas,
                capacidadHuespedes,
                casa.getDescripcionGeneral(),
                casa.getPropietario().getNombreCuenta(),
                obtenerUrlsFotos(casa.getCodigoCasa())
        );
    }

    /**
     * Convierte una entidad CasaRural a CasaRuralDetalleDTO con todos sus detalles.
     */
    private CasaRuralDetalleDTO convertirACasaDetalleDTO(CasaRural casa) {
        int numDormitorios = (int) casa.getHabitaciones().size();
        int numBanos = (int) casa.getBanos().size();
        int numCocinas = (int) casa.getCocinas().size();
        
        CasaRuralDetalleDTO detalle = new CasaRuralDetalleDTO(
                casa.getCodigoCasa(),
                casa.getNombrePropiedad(),
                casa.getPoblacion(),
                casa.getDescripcionGeneral(),
                numDormitorios,
                numBanos,
                numCocinas,
                casa.getNumComedores(),
                casa.getNumPlazasGaraje(),
                casa.getPropietario().getNombreCuenta(),
                casa.getPropietario().getTelefono()
        );

        // Obtener habitaciones
        List<Habitacion> habitaciones = habitacionRepository.findByCasaRuralCodigoCasa(casa.getCodigoCasa());
        List<HabitacionDetalleDTO> habitacionesDTO = habitaciones.stream()
                .map(h -> new HabitacionDetalleDTO(
                        h.getIdHabitacion(),
                        h.getCodigoHabitacion(),
                        h.getNumeroCamas(),
                        h.getTipoCama().toString(),
                        h.isTieneBano()
                ))
                .collect(Collectors.toList());
        detalle.setHabitaciones(habitacionesDTO);

        // Obtener cocinas
        List<Cocina> cocinas = cocinaRepository.findByCasaRuralCodigoCasa(casa.getCodigoCasa());
        List<CocinaDetalleDTO> cocinasDTO = cocinas.stream()
                .map(c -> new CocinaDetalleDTO(c.isTieneLavavajillas(), c.isTieneLavadora()))
                .collect(Collectors.toList());
        detalle.setCocinas(cocinasDTO);

        // Obtener baños
        List<Bano> banos = banoRepository.findByCasaRuralCodigoCasa(casa.getCodigoCasa());
        List<BanoDetalleDTO> banosDTO = banos.stream()
                .map(b -> new BanoDetalleDTO(b.getObservaciones()))
                .collect(Collectors.toList());
        detalle.setBanos(banosDTO);

        // Obtener fotos
        detalle.setUrlsFotos(obtenerUrlsFotos(casa.getCodigoCasa()));

        return detalle;
    }

    private List<String> obtenerUrlsFotos(int codigoCasa) {
        return fotoRepository.findByCasaRuralCodigoCasa(codigoCasa).stream()
                .map(Foto::getRuta)
                .filter(ruta -> ruta != null && !ruta.isBlank() && !"SIN_RUTA".equalsIgnoreCase(ruta))
                .collect(Collectors.toList());
    }

    private int capacidadHuespedes(CasaRural casa) {
        int camas = casa.getHabitaciones().stream()
                .mapToInt(Habitacion::getNumeroCamas)
                .sum();
        return Math.max(camas, casa.getHabitaciones().size());
    }

    private boolean estaDisponible(CasaRural casa, Date fechaEntrada, int numeroNoches, int huespedes) {
        try {
            DisponibilidadCasaDTO disponibilidad = sistemaReservas.consultarDisponibilidadDetallada(
                    casa.getCodigoCasa(), fechaEntrada, numeroNoches);
            if (disponibilidad.getDias().stream()
                    .allMatch(dia -> dia.getEstadoCasaEntera() == EstadoDisponibilidad.LIBRE)) {
                return true;
            }
            if (huespedes <= 0) {
                return false;
            }
            return capacidadHabitacionesLibresTodoElPeriodo(casa, disponibilidad) >= huespedes;
        } catch (IllegalArgumentException | NullPointerException ex) {
            return false;
        }
    }

    private int capacidadHabitacionesLibresTodoElPeriodo(CasaRural casa, DisponibilidadCasaDTO disponibilidad) {
        if (disponibilidad.getDias() == null || disponibilidad.getDias().isEmpty()) {
            return 0;
        }

        Set<Integer> idsLibres = disponibilidad.getDias().get(0).getHabitaciones().stream()
                .filter(habitacion -> habitacion.getEstado() == EstadoDisponibilidad.LIBRE)
                .map(habitacion -> habitacion.getIdHabitacion())
                .collect(Collectors.toSet());

        for (DisponibilidadDiaDTO dia : disponibilidad.getDias().subList(1, disponibilidad.getDias().size())) {
            Set<Integer> libresDelDia = dia.getHabitaciones().stream()
                    .filter(habitacion -> habitacion.getEstado() == EstadoDisponibilidad.LIBRE)
                    .map(habitacion -> habitacion.getIdHabitacion())
                    .collect(Collectors.toSet());
            idsLibres.retainAll(libresDelDia);
        }

        return casa.getHabitaciones().stream()
                .filter(habitacion -> idsLibres.contains(habitacion.getIdHabitacion()))
                .mapToInt(Habitacion::getNumeroCamas)
                .sum();
    }
}
