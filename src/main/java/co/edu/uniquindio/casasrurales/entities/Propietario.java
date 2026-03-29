package co.edu.uniquindio.casasrurales.entities;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Propietario extends Usuario {

    private String nombreCuenta;
    private String contrasena;
    private String numeroCuentaBancaria;
    private boolean sesionActiva;
    private final List<CasaRural> casas = new ArrayList<>();
    private final List<PaqueteAlquiler> paquetesAlquiler = new ArrayList<>();
    private final List<Pago> pagos = new ArrayList<>();

    public Propietario(int idUsuario, String telefono, String nombreCuenta, String contrasena, String numeroCuentaBancaria) {
        super(idUsuario, telefono);
        this.nombreCuenta = nombreCuenta;
        this.contrasena = contrasena;
        this.numeroCuentaBancaria = numeroCuentaBancaria;
    }

    public String getNombreCuenta() {
        return nombreCuenta;
    }

    public void setNombreCuenta(String nombreCuenta) {
        this.nombreCuenta = nombreCuenta;
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }

    public String getNumeroCuentaBancaria() {
        return numeroCuentaBancaria;
    }

    public void setNumeroCuentaBancaria(String numeroCuentaBancaria) {
        this.numeroCuentaBancaria = numeroCuentaBancaria;
    }

    public List<CasaRural> getCasas() {
        return List.copyOf(casas);
    }

    public boolean isSesionActiva() {
        return sesionActiva;
    }

    public void registrarse() {
        sesionActiva = false;
    }

    public boolean iniciarSesion() {
        sesionActiva = nombreCuenta != null && contrasena != null;
        return sesionActiva;
    }

    public void cerrarSesion() {
        sesionActiva = false;
    }

    public void darAltaCasa(CasaRural casa) {
        casas.add(casa);
    }

    public void darBajaCasa(int codigoCasa) {
        casas.removeIf(casa -> casa.getCodigoCasa() == codigoCasa);
    }

    public void modificarCasa(CasaRural casa) {
        darBajaCasa(casa.getCodigoCasa());
        darAltaCasa(casa);
    }

    public void crearPaqueteAlquiler(PaqueteAlquiler paquete) {
        paquetesAlquiler.add(paquete);
    }

    public void modificarPaqueteAlquiler(PaqueteAlquiler paquete) {
        Optional<PaqueteAlquiler> actual = paquetesAlquiler.stream()
                .filter(item -> item.getIdPaquete() == paquete.getIdPaquete())
                .findFirst();

        actual.ifPresent(paquetesAlquiler::remove);
        paquetesAlquiler.add(paquete);
    }

    public void registrarPago(Pago pago) {
        pagos.add(pago);
    }

    public void anularReserva(Reserva reserva) {
        reserva.cancelar();
    }
}
