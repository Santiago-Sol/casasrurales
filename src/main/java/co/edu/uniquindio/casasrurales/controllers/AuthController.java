package co.edu.uniquindio.casasrurales.controllers;

import co.edu.uniquindio.casasrurales.dto.RegistroClienteForm;
import co.edu.uniquindio.casasrurales.dto.RegistroPropietarioForm;
import co.edu.uniquindio.casasrurales.services.AutenticacionService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping
public class AuthController {

    private final AutenticacionService autenticacionService;

    public AuthController(AutenticacionService autenticacionService) {
        this.autenticacionService = autenticacionService;
    }

    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }

    @GetMapping("/registro/propietario")
    public String verRegistroPropietario(Model model) {
        model.addAttribute("registroPropietarioForm", new RegistroPropietarioForm());
        return "auth/registro-propietario";
    }

    @PostMapping("/registro/propietario")
    public String registrarPropietario(@Valid @ModelAttribute RegistroPropietarioForm registroPropietarioForm,
                                       BindingResult bindingResult,
                                       Model model) {
        if (bindingResult.hasErrors()) {
            return "auth/registro-propietario";
        }

        try {
            autenticacionService.registrarPropietario(registroPropietarioForm);
        } catch (IllegalArgumentException ex) {
            model.addAttribute("errorRegistro", ex.getMessage());
            return "auth/registro-propietario";
        }

        return "redirect:/login?registroExitoso";
    }

    @GetMapping("/registro/cliente")
    public String verRegistroCliente(Model model) {
        model.addAttribute("registroClienteForm", new RegistroClienteForm());
        return "auth/registro-cliente";
    }

    @PostMapping("/registro/cliente")
    public String registrarCliente(@Valid @ModelAttribute RegistroClienteForm registroClienteForm,
                                   BindingResult bindingResult,
                                   Model model) {
        if (bindingResult.hasErrors()) {
            return "auth/registro-cliente";
        }

        try {
            autenticacionService.registrarCliente(registroClienteForm);
        } catch (IllegalArgumentException ex) {
            model.addAttribute("errorRegistro", ex.getMessage());
            return "auth/registro-cliente";
        }

        return "redirect:/login?registroExitoso";
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication) {
        boolean esPropietario = authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_PROPIETARIO"));

        return esPropietario ? "redirect:/dashboard/propietario" : "redirect:/dashboard/cliente";
    }

    @GetMapping("/dashboard/propietario")
    public String dashboardPropietario() {
        return "dashboard/propietario";
    }

    @GetMapping("/dashboard/cliente")
    public String dashboardCliente() {
        return "dashboard/cliente";
    }
}
