package co.edu.uniquindio.casasrurales.services;

import java.util.List;

import org.springframework.stereotype.Service;

import co.edu.uniquindio.casasrurales.dto.CasaRuralListadoDTO;
import co.edu.uniquindio.casasrurales.dto.ValoracionCasaDTO;
import co.edu.uniquindio.casasrurales.dto.ValoracionCasaRequestDTO;
import co.edu.uniquindio.casasrurales.dto.ValoracionesCasaDTO;
import co.edu.uniquindio.casasrurales.entities.CasaRural;
import co.edu.uniquindio.casasrurales.entities.Cliente;
import co.edu.uniquindio.casasrurales.entities.FavoritoCasa;
import co.edu.uniquindio.casasrurales.entities.Foto;
import co.edu.uniquindio.casasrurales.entities.ValoracionCasa;
import co.edu.uniquindio.casasrurales.repositories.CasaRuralRepository;
import co.edu.uniquindio.casasrurales.repositories.ClienteRepository;
import co.edu.uniquindio.casasrurales.repositories.CuentaRepository;
import co.edu.uniquindio.casasrurales.repositories.FavoritoCasaRepository;
import co.edu.uniquindio.casasrurales.repositories.FotoRepository;
import co.edu.uniquindio.casasrurales.repositories.ValoracionCasaRepository;
import jakarta.transaction.Transactional;

@Service
@Transactional
public class ClienteInteraccionService {

    private final ClienteRepository clienteRepository;
    private final CasaRuralRepository casaRuralRepository;
    private final FavoritoCasaRepository favoritoCasaRepository;
    private final ValoracionCasaRepository valoracionCasaRepository;
    private final FotoRepository fotoRepository;
    private final CuentaRepository cuentaRepository;

    public ClienteInteraccionService(ClienteRepository clienteRepository,
                                     CasaRuralRepository casaRuralRepository,
                                     FavoritoCasaRepository favoritoCasaRepository,
                                     ValoracionCasaRepository valoracionCasaRepository,
                                     FotoRepository fotoRepository,
                                     CuentaRepository cuentaRepository) {
        this.clienteRepository = clienteRepository;
        this.casaRuralRepository = casaRuralRepository;
        this.favoritoCasaRepository = favoritoCasaRepository;
        this.valoracionCasaRepository = valoracionCasaRepository;
        this.fotoRepository = fotoRepository;
        this.cuentaRepository = cuentaRepository;
    }

    public List<CasaRuralListadoDTO> listarFavoritos(int idCliente) {
        return favoritoCasaRepository.findByCliente_IdUsuarioOrderByFechaCreacionDesc(idCliente).stream()
                .map(FavoritoCasa::getCasaRural)
                .filter(CasaRural::isActiva)
                .map(this::convertirACasaListadoDTO)
                .toList();
    }

    public boolean esFavorita(int idCliente, int codigoCasa) {
        return favoritoCasaRepository.existsByCliente_IdUsuarioAndCasaRural_CodigoCasa(idCliente, codigoCasa);
    }

    public void agregarFavorito(int idCliente, int codigoCasa) {
        if (esFavorita(idCliente, codigoCasa)) {
            return;
        }

        Cliente cliente = obtenerCliente(idCliente);
        CasaRural casa = obtenerCasaActiva(codigoCasa);
        favoritoCasaRepository.save(new FavoritoCasa(cliente, casa));
    }

    public void quitarFavorito(int idCliente, int codigoCasa) {
        favoritoCasaRepository.findByCliente_IdUsuarioAndCasaRural_CodigoCasa(idCliente, codigoCasa)
                .ifPresent(favoritoCasaRepository::delete);
    }

    public ValoracionesCasaDTO listarValoraciones(int codigoCasa) {
        List<ValoracionCasaDTO> valoraciones = valoracionCasaRepository
                .findByCasaRural_CodigoCasaOrderByFechaActualizacionDesc(codigoCasa).stream()
                .map(this::convertirAValoracionDTO)
                .toList();

        double promedio = valoraciones.stream()
                .mapToInt(ValoracionCasaDTO::getEstrellas)
                .average()
                .orElse(0);

        return new ValoracionesCasaDTO(Math.round(promedio * 10.0) / 10.0, valoraciones.size(), valoraciones);
    }

    public ValoracionesCasaDTO guardarValoracion(int idCliente, int codigoCasa, ValoracionCasaRequestDTO request) {
        Cliente cliente = obtenerCliente(idCliente);
        CasaRural casa = obtenerCasaActiva(codigoCasa);

        ValoracionCasa valoracion = valoracionCasaRepository
                .findByCliente_IdUsuarioAndCasaRural_CodigoCasa(idCliente, codigoCasa)
                .orElseGet(() -> new ValoracionCasa(cliente, casa, request.getEstrellas(), request.getComentario()));

        valoracion.actualizar(request.getEstrellas(), request.getComentario());
        valoracionCasaRepository.save(valoracion);
        return listarValoraciones(codigoCasa);
    }

    private Cliente obtenerCliente(int idCliente) {
        return clienteRepository.findById(idCliente)
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado"));
    }

    private CasaRural obtenerCasaActiva(int codigoCasa) {
        CasaRural casa = casaRuralRepository.findById(codigoCasa)
                .orElseThrow(() -> new IllegalArgumentException("Casa no encontrada"));
        if (!casa.isActiva()) {
            throw new IllegalArgumentException("La casa no esta disponible");
        }
        return casa;
    }

    private CasaRuralListadoDTO convertirACasaListadoDTO(CasaRural casa) {
        return new CasaRuralListadoDTO(
                casa.getCodigoCasa(),
                casa.getNombrePropiedad(),
                casa.getPoblacion(),
                casa.getHabitaciones().size(),
                casa.getBanos().size(),
                casa.getCocinas().size(),
                capacidadHuespedes(casa),
                casa.getDescripcionGeneral(),
                casa.getPropietario().getNombreCuenta(),
                obtenerUrlsFotos(casa.getCodigoCasa())
        );
    }

    private ValoracionCasaDTO convertirAValoracionDTO(ValoracionCasa valoracion) {
        int idCliente = valoracion.getCliente().getIdUsuario();
        String cliente = cuentaRepository.findByCliente_IdUsuario(idCliente)
                .map(cuenta -> cuenta.getEmail())
                .orElse("Cliente " + idCliente);

        return new ValoracionCasaDTO(
                valoracion.getIdValoracion(),
                valoracion.getCasaRural().getCodigoCasa(),
                idCliente,
                cliente,
                valoracion.getEstrellas(),
                valoracion.getComentario(),
                valoracion.getFechaActualizacion()
        );
    }

    private List<String> obtenerUrlsFotos(int codigoCasa) {
        return fotoRepository.findByCasaRuralCodigoCasa(codigoCasa).stream()
                .map(Foto::getRuta)
                .filter(ruta -> ruta != null && !ruta.isBlank() && !"SIN_RUTA".equalsIgnoreCase(ruta))
                .toList();
    }

    private int capacidadHuespedes(CasaRural casa) {
        int camas = casa.getHabitaciones().stream()
                .mapToInt(habitacion -> habitacion.getNumeroCamas())
                .sum();
        return Math.max(camas, casa.getHabitaciones().size());
    }
}
