package co.edu.uniquindio.casasrurales.entities;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import co.edu.uniquindio.casasrurales.enums.EstadoDisponibilidad;
import co.edu.uniquindio.casasrurales.enums.EstadoReserva;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

/**
 * Entidad principal del dominio que representa una casa rural publicada en el sistema.
 * Agrupa su informacion general, propietario, espacios, paquetes y reservas asociadas.
 */
@Entity
@Table(name = "casa_rural")
public class CasaRural {

    @Id
    @Column(name = "codigo_casa")
    private int codigoCasa;

    @Column(nullable = false, length = 120)
    private String poblacion;

    @Column(nullable = false, length = 150)
    private String nombrePropiedad;

    @Column(name = "descripcion_general", columnDefinition = "TEXT")
    private String descripcionGeneral;

    @Column(name = "num_dormitorios")
    private int numDormitorios;

    @Column(name = "num_banos")
    private int numBanos;

    @Column(name = "num_cocinas")
    private int numCocinas;

    @Column(name = "num_comedores")
    private int numComedores;

    @Column(name = "num_plazas_garaje")
    private int numPlazasGaraje;

    @Column(name = "estado_activa")
    private boolean activa;

    @ManyToOne
    @JoinColumn(name = "id_propietario", nullable = false)
    private Propietario propietario;

    @OneToMany(mappedBy = "casaRural", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Foto> fotos = new ArrayList<>();

    @OneToMany(mappedBy = "casaRural", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Habitacion> habitaciones = new ArrayList<>();

    @OneToMany(mappedBy = "casaRural", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Cocina> cocinas = new ArrayList<>();

    @OneToMany(mappedBy = "casaRural", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Bano> banos = new ArrayList<>();

    @OneToMany(mappedBy = "casaRural", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PaqueteAlquiler> paquetesAlquiler = new ArrayList<>();

    @OneToMany(mappedBy = "casaRural", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Reserva> reservas = new ArrayList<>();

    protected CasaRural() {
    }

    public CasaRural(int codigoCasa, String poblacion, String nombrePropiedad, String descripcionGeneral, int numComedores,
                     int numPlazasGaraje, boolean activa) {
        this.codigoCasa = codigoCasa;
        this.poblacion = poblacion;
        this.nombrePropiedad = nombrePropiedad;
        this.descripcionGeneral = descripcionGeneral;
        this.numComedores = numComedores;
        this.numPlazasGaraje = numPlazasGaraje;
        this.activa = activa;
    }

    public int getCodigoCasa() {
        return codigoCasa;
    }

    public String getPoblacion() {
        return poblacion;
    }

    public void setPoblacion(String poblacion) {
        this.poblacion = poblacion;
    }

    public String getDescripcionGeneral() {
        return descripcionGeneral;
    }

    public void setDescripcionGeneral(String descripcionGeneral) {
        this.descripcionGeneral = descripcionGeneral;
    }

    public int getNumComedores() {
        return numComedores;
    }

    public void setNumComedores(int numComedores) {
        this.numComedores = numComedores;
    }

    public int getNumPlazasGaraje() {
        return numPlazasGaraje;
    }

    public void setNumPlazasGaraje(int numPlazasGaraje) {
        this.numPlazasGaraje = numPlazasGaraje;
    }

    public boolean isActiva() {
        return activa;
    }

    public void setActiva(boolean activa) {
        this.activa = activa;
    }

    public int getNumDormitorios() {
        return numDormitorios;
    }

    public void setNumDormitorios(int numDormitorios) {
        this.numDormitorios = numDormitorios;
    }

    public int getNumBanos() {
        return numBanos;
    }

    public void setNumBanos(int numBanos) {
        this.numBanos = numBanos;
    }

    public int getNumCocinas() {
        return numCocinas;
    }

    public void setNumCocinas(int numCocinas) {
        this.numCocinas = numCocinas;
    }

    public String getNombrePropiedad() {
        return nombrePropiedad;
    }

    public void setNombrePropiedad(String nombrePropiedad) {
        this.nombrePropiedad = nombrePropiedad;
    }

    public Propietario getPropietario() {
        return propietario;
    }

    public void setPropietario(Propietario propietario) {
        this.propietario = propietario;
    }

    public List<Foto> getFotos() {
        return List.copyOf(fotos);
    }

    public List<Habitacion> getHabitaciones() {
        return List.copyOf(habitaciones);
    }

    public List<Cocina> getCocinas() {
        return List.copyOf(cocinas);
    }

    public List<Bano> getBanos() {
        return List.copyOf(banos);
    }

    public List<PaqueteAlquiler> getPaquetesAlquiler() {
        return List.copyOf(paquetesAlquiler);
    }

    public List<Reserva> getReservas() {
        return List.copyOf(reservas);
    }

    public String mostrarInformacion() {
        return "CasaRural{codigo=%d, poblacion='%s', activa=%s}".formatted(codigoCasa, poblacion, activa);
    }

    public String consultarDisponibilidad(Date fechaEntrada, int numeroNoches) {
        if (!activa || !esValida()) {
            return EstadoDisponibilidad.NO_DISPONIBLE.name();
        }

        boolean reservada = reservas.stream()
                .filter(reserva -> reserva.getEstado() != EstadoReserva.ANULADA)
                .anyMatch(reserva -> seCruzaReserva(fechaEntrada, numeroNoches, reserva));

        return reservada ? EstadoDisponibilidad.RESERVADA.name() : EstadoDisponibilidad.LIBRE.name();
    }

    private boolean seCruzaReserva(Date fechaEntrada, int numeroNoches, Reserva reserva) {
        Calendar solicitudFin = Calendar.getInstance();
        solicitudFin.setTime(fechaEntrada);
        solicitudFin.add(Calendar.DAY_OF_MONTH, numeroNoches - 1);

        Calendar reservaFin = Calendar.getInstance();
        reservaFin.setTime(reserva.getFechaEntrada());
        reservaFin.add(Calendar.DAY_OF_MONTH, reserva.getNumeroNoches() - 1);

        Date fechaFinSolicitud = solicitudFin.getTime();
        Date fechaFinReserva = reservaFin.getTime();

        return !fechaFinSolicitud.before(reserva.getFechaEntrada()) && !fechaFinReserva.before(fechaEntrada);
    }

    public void agregarFoto(Foto foto) {
        foto.setCasaRural(this);
        fotos.add(foto);
    }

    public void agregarHabitacion(Habitacion habitacion) {
        habitacion.setCasaRural(this);
        habitaciones.add(habitacion);
        numDormitorios = habitaciones.size();
    }

    public void agregarCocina(Cocina cocina) {
        cocina.setCasaRural(this);
        cocinas.add(cocina);
        numCocinas = cocinas.size();
    }

    public void agregarBano(Bano bano) {
        bano.setCasaRural(this);
        banos.add(bano);
        numBanos = banos.size();
    }

    public void agregarPaqueteAlquiler(PaqueteAlquiler paqueteAlquiler) {
        paqueteAlquiler.setCasaRural(this);
        paquetesAlquiler.add(paqueteAlquiler);
    }

    public void agregarReserva(Reserva reserva) {
        reserva.setCasaRural(this);
        reservas.add(reserva);
    }

    public boolean esValida() {
        return !fotos.isEmpty() && habitaciones.size() >= 3 && cocinas.size() >= 1 && banos.size() >= 1;
    }
}
