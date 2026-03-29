package co.edu.uniquindio.casasrurales.entities;

import co.edu.uniquindio.casasrurales.enums.EstadoPago;
import co.edu.uniquindio.casasrurales.enums.EstadoReserva;
import co.edu.uniquindio.casasrurales.enums.TipoReserva;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class Reserva {

    private int numeroReserva;
    private Date fechaReserva;
    private Date fechaEntrada;
    private int numeroNoches;
    private TipoReserva tipoReserva;
    private double importeTotal;
    private double importeAnticipo;
    private Date fechaLimitePago;
    private EstadoReserva estado;
    private Cliente cliente;
    private CasaRural casaRural;
    private final List<Habitacion> habitaciones = new ArrayList<>();
    private final List<Pago> pagos = new ArrayList<>();

    public Reserva(int numeroReserva, Date fechaReserva, Date fechaEntrada, int numeroNoches, TipoReserva tipoReserva,
                   double importeTotal, EstadoReserva estado, Cliente cliente, CasaRural casaRural,
                   List<Habitacion> habitaciones) {
        this.numeroReserva = numeroReserva;
        this.fechaReserva = fechaReserva;
        this.fechaEntrada = fechaEntrada;
        this.numeroNoches = numeroNoches;
        this.tipoReserva = tipoReserva;
        this.importeTotal = importeTotal;
        this.importeAnticipo = calcularAnticipo();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(fechaReserva);
        calendar.add(Calendar.DAY_OF_MONTH, 3);
        this.fechaLimitePago = calendar.getTime();
        this.estado = estado;
        this.cliente = cliente;
        this.casaRural = casaRural;
        if (habitaciones != null) {
            this.habitaciones.addAll(habitaciones);
        }
    }

    public int getNumeroReserva() {
        return numeroReserva;
    }

    public Date getFechaReserva() {
        return fechaReserva;
    }

    public Date getFechaEntrada() {
        return fechaEntrada;
    }

    public int getNumeroNoches() {
        return numeroNoches;
    }

    public TipoReserva getTipoReserva() {
        return tipoReserva;
    }

    public double getImporteTotal() {
        return importeTotal;
    }

    public double getImporteAnticipo() {
        return importeAnticipo;
    }

    public Date getFechaLimitePago() {
        return fechaLimitePago;
    }

    public EstadoReserva getEstado() {
        return estado;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public CasaRural getCasaRural() {
        return casaRural;
    }

    public List<Habitacion> getHabitaciones() {
        return List.copyOf(habitaciones);
    }

    public List<Pago> getPagos() {
        return List.copyOf(pagos);
    }

    public double calcularAnticipo() {
        return importeTotal * 0.20;
    }

    public void agregarPago(Pago pago) {
        pagos.add(pago);
    }

    public void confirmar() {
        double totalPagado = pagos.stream()
                .filter(pago -> pago.getEstado() == EstadoPago.VERIFICADO)
                .map(Pago::getMonto)
                .reduce(0.0, Double::sum);

        if (totalPagado >= importeAnticipo) {
            estado = EstadoReserva.CONFIRMADA;
        }
    }

    public void cancelar() {
        estado = EstadoReserva.ANULADA;
    }

    public boolean estaVencida() {
        return estado == EstadoReserva.PENDIENTE_PAGO && new Date().after(fechaLimitePago);
    }

    public String mostrarResumen() {
        return "Reserva{numero=%d, estado=%s, fechaLimitePago=%s}".formatted(numeroReserva, estado, fechaLimitePago);
    }
}
