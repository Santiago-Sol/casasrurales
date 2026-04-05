package co.edu.uniquindio.casasrurales.entities;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Entity
@Table(name = "propietario")
@AttributeOverrides({
        @AttributeOverride(name = "idUsuario", column = @Column(name = "id_propietario")),
        @AttributeOverride(name = "telefono", column = @Column(name = "telefono", nullable = false, length = 30))
})
public class Propietario extends Usuario {

    @Column(name = "nombre_cuenta", nullable = false, length = 100)
    private String nombreCuenta;

    @Column(name = "contrasena", nullable = false, length = 100)
    private String contrasena;

    @Column(name = "numero_cuenta_bancaria", nullable = false, length = 50)
    private String numeroCuentaBancaria;

    @Transient
    private boolean sesionActiva;

    @OneToMany(mappedBy = "propietario", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CasaRural> casas = new ArrayList<>();

    @Transient
    private List<PaqueteAlquiler> paquetesAlquiler = new ArrayList<>();

    @Transient
    private List<Pago> pagos = new ArrayList<>();

    protected Propietario() {
    }

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
        casa.setPropietario(this);
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
