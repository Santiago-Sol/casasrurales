package co.edu.uniquindio.casasrurales.controllers;

import java.util.List;
import java.util.Optional;
import java.util.Date;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import co.edu.uniquindio.casasrurales.dto.CasaRuralDetalleDTO;
import co.edu.uniquindio.casasrurales.dto.CasaRuralListadoDTO;
import co.edu.uniquindio.casasrurales.dto.DisponibilidadCasaDTO;
import co.edu.uniquindio.casasrurales.dto.ResultadoBusquedaCasasDTO;
import co.edu.uniquindio.casasrurales.services.BusquedaCasasService;
import co.edu.uniquindio.casasrurales.services.SistemaReservas;

/**
 * API REST para búsqueda de casas rurales.
 * Solo clientes autenticados pueden acceder.
 * Implementa búsqueda por población y por código.
 */
@RestController
@RequestMapping("/api/busqueda")
public class BusquedaController {

    private final BusquedaCasasService busquedaCasasService;
    private final SistemaReservas sistemaReservas;

    public BusquedaController(BusquedaCasasService busquedaCasasService, SistemaReservas sistemaReservas) {
        this.busquedaCasasService = busquedaCasasService;
        this.sistemaReservas = sistemaReservas;
    }

    /**
     * Busca casas rurales por población.
     * Devuelve una lista de casas con paquetes activos.
     * 
     * @param poblacion nombre de la población a buscar
     * @return lista de casas disponibles en esa población
     */
    /**
     * Lista todas las casas disponibles y permite filtrar por poblacion, fechas y huespedes.
     */
    @GetMapping
    public ResponseEntity<?> listarDisponibles(
            @RequestParam(required = false) String poblacion,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date fechaEntrada,
            @RequestParam(required = false) Integer numeroNoches,
            @RequestParam(required = false) Integer huespedes,
            @RequestParam(required = false) Integer habitaciones,
            @RequestParam(required = false) Integer pagina,
            @RequestParam(required = false) Integer tamano) {

        if (pagina != null || tamano != null) {
            ResultadoBusquedaCasasDTO resultado = busquedaCasasService.buscarCasasDisponiblesPaginadas(
                    poblacion, fechaEntrada, numeroNoches, huespedes, habitaciones,
                    pagina == null ? 0 : pagina,
                    tamano == null ? 6 : tamano);

            return ResponseEntity.ok(resultado);
        }

        List<CasaRuralListadoDTO> casas = busquedaCasasService.buscarCasasDisponibles(
                poblacion, fechaEntrada, numeroNoches, huespedes);

        if (casas.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }

        return ResponseEntity.ok(casas);
    }

    public ResponseEntity<List<CasaRuralListadoDTO>> listarDisponibles(
            String poblacion,
            Date fechaEntrada,
            Integer numeroNoches,
            Integer huespedes) {
        List<CasaRuralListadoDTO> casas = busquedaCasasService.buscarCasasDisponibles(
                poblacion, fechaEntrada, numeroNoches, huespedes);

        if (casas.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }

        return ResponseEntity.ok(casas);
    }

    @GetMapping("/por-poblacion")
    public ResponseEntity<List<CasaRuralListadoDTO>> buscarPorPoblacion(
            @RequestParam String poblacion) {
        
        if (poblacion == null || poblacion.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        String poblacionNormalizada = poblacion.trim();
        List<CasaRuralListadoDTO> casas = busquedaCasasService.buscarCasasPorPoblacion(poblacionNormalizada);

        if (casas.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }

        return ResponseEntity.ok(casas);
    }

    /**
     * Busca una casa por su código único (PathVariable).
     * Si existe y tiene paquetes activos, devuelve sus detalles completos.
     * 
     * @param codigoCasa el código único de la casa
     * @return detalles completos de la casa
     */
    @GetMapping("/{codigoCasa}")
    public ResponseEntity<CasaRuralDetalleDTO> buscarPorCodigo(
            @PathVariable int codigoCasa) {
        
        Optional<CasaRuralDetalleDTO> casaOpt = busquedaCasasService.buscarCasaPorCodigo(codigoCasa);

        if (casaOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(casaOpt.get());
    }

    /**
     * Busca una casa por código usando parámetro query.
     * Versión complementaria del search por código usando QueryParam.
     * 
     * @param codigo el código de la casa (números)
     * @return detalles completos de la casa
     */
    @GetMapping("/codigo/buscar")
    public ResponseEntity<CasaRuralDetalleDTO> buscarDetallePorCodigo(
            @RequestParam(required = false) String codigo) {
        
        if (codigo == null || codigo.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        try {
            int codigoCasa = Integer.parseInt(codigo.trim());
            Optional<CasaRuralDetalleDTO> casaOpt = busquedaCasasService.buscarCasaPorCodigo(codigoCasa);

            if (casaOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok(casaOpt.get());
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Busca los paquetes disponibles para una casa.
     * 
     * @param codigoCasa el código de la casa
     * @return lista de paquetes disponibles
     */
    @GetMapping("/{codigoCasa}/paquetes")
    public ResponseEntity<?> obtenerPaquetesCasa(@PathVariable int codigoCasa) {
        try {
            return ResponseEntity.ok(busquedaCasasService.obtenerPaquetesCasa(codigoCasa));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{codigoCasa}/disponibilidad")
    public ResponseEntity<?> consultarDisponibilidad(
            @PathVariable int codigoCasa,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date fechaEntrada,
            @RequestParam int numeroNoches) {
        try {
            DisponibilidadCasaDTO disponibilidad = sistemaReservas.consultarDisponibilidadDetallada(
                    codigoCasa, fechaEntrada, numeroNoches);
            return ResponseEntity.ok(disponibilidad);
        } catch (NullPointerException | IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", ex.getMessage()));
        }
    }
}
