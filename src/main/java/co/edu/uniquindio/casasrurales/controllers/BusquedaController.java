package co.edu.uniquindio.casasrurales.controllers;

import java.util.List;
import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import co.edu.uniquindio.casasrurales.dto.CasaRuralDetalleDTO;
import co.edu.uniquindio.casasrurales.dto.CasaRuralListadoDTO;
import co.edu.uniquindio.casasrurales.services.BusquedaCasasService;

/**
 * Controlador para la búsqueda de casas rurales.
 * Solo clientes autenticados pueden acceder.
 * Implementa búsqueda por población y por código.
 */
@Controller
@RequestMapping("/busqueda")
public class BusquedaController {

    private final BusquedaCasasService busquedaCasasService;

    public BusquedaController(BusquedaCasasService busquedaCasasService) {
        this.busquedaCasasService = busquedaCasasService;
    }

    /**
     * Muestra el formulario de búsqueda de casas rurales.
     * Solo accesible para usuarios autenticados.
     */
    @GetMapping("")
    public String verFormularioBusqueda(Authentication authentication) {
        // La autenticación es requerida por SecurityConfig
        return "busqueda/formulario-busqueda";
    }

    /**
     * Busca casas rurales por población.
     * Devuelve una lista de casas con paquetes activos.
     * Si no hay resultados, muestra mensaje informativo.
     * 
     * @param poblacion nombre de la población a buscar
     * @param model para pasar datos a la vista
     * @return vista con resultados o formulario con mensaje
     */
    @GetMapping("/resultados")
    public String buscarPorPoblacion(@RequestParam(required = false) String poblacion, Model model) {
        if (poblacion == null || poblacion.trim().isEmpty()) {
            model.addAttribute("mensaje", "Por favor, ingresa una población para buscar");
            return "busqueda/formulario-busqueda";
        }

        List<CasaRuralListadoDTO> casas = busquedaCasasService.buscarCasasPorPoblacion(poblacion);

        if (casas.isEmpty()) {
            model.addAttribute("mensaje", "No se encontraron casas disponibles en " + poblacion + 
                    ". Por favor, intenta con otra población.");
            model.addAttribute("poblacionBuscada", poblacion);
            return "busqueda/formulario-busqueda";
        }

        model.addAttribute("casas", casas);
        model.addAttribute("poblacionBuscada", poblacion);
        model.addAttribute("cantidadResultados", casas.size());
        
        return "busqueda/resultados-busqueda";
    }

    /**
     * Busca una casa por su código único de forma directa.
     * Si existe y tiene paquetes activos, redirige a la vista de detalle.
     * 
     * @param codigoCasa el código único de la casa
     * @param model para pasar datos a la vista
     * @return vista de detalle o formulario con error
     */
    @GetMapping("/codigo/{codigoCasa}")
    public String buscarPorCodigo(@PathVariable int codigoCasa, Model model) {
        Optional<CasaRuralDetalleDTO> casaOpt = busquedaCasasService.buscarCasaPorCodigo(codigoCasa);

        if (casaOpt.isEmpty()) {
            model.addAttribute("mensaje", "No se encontró la casa con código " + codigoCasa + 
                    " o no está disponible para consultar.");
            return "busqueda/formulario-busqueda";
        }

        model.addAttribute("casa", casaOpt.get());
        return "busqueda/detalle-casa";
    }

    /**
     * Muestra todos los detalles de una casa rural específica.
     * Incluye: fotos, habitaciones, cocinas, baños y datos del propietario.
     * 
     * @param codigoCasa el código único de la casa
     * @param model para pasar datos a la vista
     * @return vista con detalles completos
     */
    @GetMapping("/detalle/{codigoCasa}")
    public String verDetalleCasa(@PathVariable int codigoCasa, Model model) {
        try {
            CasaRuralDetalleDTO casa = busquedaCasasService.obtenerDetalleCasa(codigoCasa);
            model.addAttribute("casa", casa);
            return "busqueda/detalle-casa";
        } catch (IllegalArgumentException e) {
            model.addAttribute("mensaje", e.getMessage());
            return "busqueda/formulario-busqueda";
        }
    }
}
