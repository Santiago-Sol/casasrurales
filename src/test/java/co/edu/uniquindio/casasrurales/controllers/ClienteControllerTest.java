package co.edu.uniquindio.casasrurales.controllers;

import co.edu.uniquindio.casasrurales.dto.CasaRuralListadoDTO;
import co.edu.uniquindio.casasrurales.entities.CasaRural;
import co.edu.uniquindio.casasrurales.entities.Cliente;
import co.edu.uniquindio.casasrurales.entities.Valoracion;
import co.edu.uniquindio.casasrurales.repositories.CasaRuralRepository;
import co.edu.uniquindio.casasrurales.repositories.ClienteRepository;
import co.edu.uniquindio.casasrurales.repositories.ValoracionRepository;
import co.edu.uniquindio.casasrurales.services.BusquedaCasasService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("ClienteController - Pruebas Unitarias")
class ClienteControllerTest {

    private ClienteController clienteController;
    private ClienteRepository clienteRepository;
    private CasaRuralRepository casaRuralRepository;
    private ValoracionRepository valoracionRepository;
    private BusquedaCasasService busquedaCasasService;

    @BeforeEach
    void setUp() {
        clienteRepository = mock(ClienteRepository.class);
        casaRuralRepository = mock(CasaRuralRepository.class);
        valoracionRepository = mock(ValoracionRepository.class);
        busquedaCasasService = mock(BusquedaCasasService.class);
        clienteController = new ClienteController(
                clienteRepository, casaRuralRepository, valoracionRepository, busquedaCasasService);
    }

    @Test
    @DisplayName("Obtener favoritos retorna UNAUTHORIZED si no está autenticado")
    void testObtenerFavoritos_SinAutenticacion() {
        ResponseEntity<?> response = clienteController.obtenerFavoritos(null);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    @DisplayName("Obtener favoritos retorna FORBIDDEN si el usuario no es un cliente")
    void testObtenerFavoritos_UsuarioNoCliente() {
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getName()).thenReturn("999");
        when(clienteRepository.findById(999)).thenReturn(Optional.empty());

        ResponseEntity<?> response = clienteController.obtenerFavoritos(auth);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    @DisplayName("Obtener favoritos retorna la lista de favoritos de forma exitosa")
    void testObtenerFavoritos_Exitoso() {
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getName()).thenReturn("1");

        Cliente cliente = new Cliente("3001112222");
        cliente.setIdUsuario(1);
        CasaRural casa = new CasaRural(100, "Salento", "Casa Linda", "Desc", 1, 1, true);
        cliente.agregarFavorito(casa);

        when(clienteRepository.findById(1)).thenReturn(Optional.of(cliente));
        CasaRuralListadoDTO dto = new CasaRuralListadoDTO(100, "Casa Linda", "Salento", 3, 2, 1, "Desc", "Pedro");
        when(busquedaCasasService.convertirACasaListadoDTO(casa)).thenReturn(dto);

        ResponseEntity<?> response = clienteController.obtenerFavoritos(auth);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        List<?> body = (List<?>) response.getBody();
        assertNotNull(body);
        assertEquals(1, body.size());
    }

    @Test
    @DisplayName("Toggle favorito agrega una casa si no está en favoritos")
    void testToggleFavorito_Agregar() {
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getName()).thenReturn("1");

        Cliente cliente = new Cliente("3001112222");
        cliente.setIdUsuario(1);
        CasaRural casa = new CasaRural(100, "Salento", "Casa Linda", "Desc", 1, 1, true);

        when(clienteRepository.findById(1)).thenReturn(Optional.of(cliente));
        when(casaRuralRepository.findById(100)).thenReturn(Optional.of(casa));

        ResponseEntity<?> response = clienteController.toggleFavorito(100, auth);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertNotNull(body);
        assertEquals(true, body.get("esFavorito"));
        assertTrue(cliente.getFavoritos().contains(casa));
        verify(clienteRepository, times(1)).save(cliente);
    }

    @Test
    @DisplayName("Toggle favorito elimina una casa si ya está en favoritos")
    void testToggleFavorito_Eliminar() {
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getName()).thenReturn("1");

        Cliente cliente = new Cliente("3001112222");
        cliente.setIdUsuario(1);
        CasaRural casa = new CasaRural(100, "Salento", "Casa Linda", "Desc", 1, 1, true);
        cliente.agregarFavorito(casa);

        when(clienteRepository.findById(1)).thenReturn(Optional.of(cliente));
        when(casaRuralRepository.findById(100)).thenReturn(Optional.of(casa));

        ResponseEntity<?> response = clienteController.toggleFavorito(100, auth);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertNotNull(body);
        assertEquals(false, body.get("esFavorito"));
        assertFalse(cliente.getFavoritos().contains(casa));
        verify(clienteRepository, times(1)).save(cliente);
    }

    @Test
    @DisplayName("Registrar valoración crea una nueva valoración si no existe previa")
    void testRegistrarValoracion_Nueva() {
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getName()).thenReturn("1");

        Cliente cliente = new Cliente("3001112222");
        cliente.setIdUsuario(1);
        CasaRural casa = new CasaRural(100, "Salento", "Casa Linda", "Desc", 1, 1, true);

        when(clienteRepository.findById(1)).thenReturn(Optional.of(cliente));
        when(casaRuralRepository.findById(100)).thenReturn(Optional.of(casa));
        when(valoracionRepository.findByClienteIdUsuarioAndCasaRuralCodigoCasa(1, 100))
                .thenReturn(Optional.empty());

        ClienteController.ValoracionRequest req = new ClienteController.ValoracionRequest();
        req.setCalificacion(5);
        req.setComentario("Excelente estadía!");

        ResponseEntity<?> response = clienteController.registrarValoracion(100, req, auth);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertNotNull(body);
        assertEquals(5, body.get("calificacion"));
        verify(valoracionRepository, times(1)).save(any(Valoracion.class));
    }
}
