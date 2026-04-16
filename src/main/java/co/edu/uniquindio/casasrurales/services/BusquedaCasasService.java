package co.edu.uniquindio.casasrurales.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import co.edu.uniquindio.casasrurales.dto.BanoDetalleDTO;
import co.edu.uniquindio.casasrurales.dto.CasaRuralDetalleDTO;
import co.edu.uniquindio.casasrurales.dto.CasaRuralListadoDTO;
import co.edu.uniquindio.casasrurales.dto.CocinaDetalleDTO;
import co.edu.uniquindio.casasrurales.dto.HabitacionDetalleDTO;
import co.edu.uniquindio.casasrurales.entities.Bano;
import co.edu.uniquindio.casasrurales.entities.CasaRural;
import co.edu.uniquindio.casasrurales.entities.Cocina;
import co.edu.uniquindio.casasrurales.entities.Foto;
import co.edu.uniquindio.casasrurales.entities.Habitacion;
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

    public BusquedaCasasService(CasaRuralRepository casaRuralRepository,
                               HabitacionRepository habitacionRepository,
                               CocinaRepository cocinaRepository,
                               BanoRepository banoRepository,
                               FotoRepository fotoRepository) {
        this.casaRuralRepository = casaRuralRepository;
        this.habitacionRepository = habitacionRepository;
        this.cocinaRepository = cocinaRepository;
        this.banoRepository = banoRepository;
        this.fotoRepository = fotoRepository;
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
                .map(this::convertirACasaListadoDTO)
                .collect(Collectors.toList());
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

    // === Métodos privados de utilidad ===

    /**
     * Convierte una entidad CasaRural a CasaRuralListadoDTO.
     */
    private CasaRuralListadoDTO convertirACasaListadoDTO(CasaRural casa) {
        int numDormitorios = (int) casa.getHabitaciones().size();
        int numBanos = (int) casa.getBanos().size();
        int numCocinas = (int) casa.getCocinas().size();
        
        return new CasaRuralListadoDTO(
                casa.getCodigoCasa(),
                casa.getNombrePropiedad(),
                casa.getPoblacion(),
                numDormitorios,
                numBanos,
                numCocinas,
                casa.getDescripcionGeneral(),
                casa.getPropietario().getNombreCuenta()
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
        List<Foto> fotos = fotoRepository.findByCasaRuralCodigoCasa(casa.getCodigoCasa());
        List<String> urlsFotos = fotos.stream()
                .map(Foto::getRuta)
                .collect(Collectors.toList());
        detalle.setUrlsFotos(urlsFotos);

        return detalle;
    }
}
