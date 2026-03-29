package co.edu.uniquindio.casasrurales.entities;

import co.edu.uniquindio.casasrurales.enums.EstadoDisponibilidad;
import co.edu.uniquindio.casasrurales.enums.EstadoReserva;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class CasaRural {

    private int codigoCasa;
    private String poblacion;
    private String descripcionGeneral;
    private int numComedores;
    private int numPlazasGaraje;
    private boolean activa;
    private final List<Foto> fotos = new ArrayList<>();
    private final List<Habitacion> habitaciones = new ArrayList<>();
    private final List<Cocina> cocinas = new ArrayList<>();
    private final List<Bano> banos = new ArrayList<>();
    private final List<PaqueteAlquiler> paquetesAlquiler = new ArrayList<>();
    private final List<Reserva> reservas = new ArrayList<>();

    public CasaRural(int codigoCasa, String poblacion, String descripcionGeneral, int numComedores,
                     int numPlazasGaraje, boolean activa) {
        this.codigoCasa = codigoCasa;
        this.poblacion = poblacion;
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

    public String getDescripcionGeneral() {
        return descripcionGeneral;
    }

    public int getNumComedores() {
        return numComedores;
    }

    public int getNumPlazasGaraje() {
        return numPlazasGaraje;
    }

    public boolean isActiva() {
        return activa;
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
        fotos.add(foto);
    }

    public void agregarHabitacion(Habitacion habitacion) {
        habitaciones.add(habitacion);
    }

    public void agregarCocina(Cocina cocina) {
        cocinas.add(cocina);
    }

    public void agregarBano(Bano bano) {
        banos.add(bano);
    }

    public void agregarPaqueteAlquiler(PaqueteAlquiler paqueteAlquiler) {
        paquetesAlquiler.add(paqueteAlquiler);
    }

    public void agregarReserva(Reserva reserva) {
        reservas.add(reserva);
    }

    public boolean esValida() {
        return !fotos.isEmpty() && habitaciones.size() >= 3 && cocinas.size() >= 1 && banos.size() >= 2;
    }
}
