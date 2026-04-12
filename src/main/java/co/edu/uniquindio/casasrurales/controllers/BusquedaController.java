package co.edu.uniquindio.casasrurales.controllers;

import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import co.edu.uniquindio.casasrurales.dto.CasaRuralDetalleDTO;
import co.edu.uniquindio.casasrurales.dto.CasaRuralListadoDTO;
import co.edu.uniquindio.casasrurales.services.BusquedaCasasService;

/**
 * API REST para búsqueda de casas rurales.
 * Solo clientes autenticados pueden acceder.
 * Implementa búsqueda por población y por código.
 */
@RestController
@RequestMapping("/api/busqueda")
public class BusquedaController {

    private final BusquedaCasasService busquedaCasasService;

    public BusquedaController(BusquedaCasasService busquedaCasasService) {
        this.busquedaCasasService = busquedaCasasService;
    }

    /**
     * Busca casas rurales por población.
     * Devuelve una lista de casas con paquetes activos.
     * 
     * @param poblacion nombre de la población a buscar
     * @return lista de casas disponibles en esa población
     */
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
}
