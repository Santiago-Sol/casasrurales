package co.edu.uniquindio.casasrurales.entities;

import co.edu.uniquindio.casasrurales.enums.EstadoPago;
import co.edu.uniquindio.casasrurales.enums.EstadoReserva;
import co.edu.uniquindio.casasrurales.enums.TipoReserva;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Entidad que representa una reserva creada por un cliente sobre una casa rural.
 * Centraliza fechas, estado, tipo de reserva, habitaciones involucradas y pagos.
 */
@Entity
@Table(name = "reserva")
public class Reserva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "numero_reserva")
    private int numeroReserva;

    @Temporal(TemporalType.DATE)
    @Column(name = "fecha_reserva", nullable = false)
    private Date fechaReserva;

    @Temporal(TemporalType.DATE)
    @Column(name = "fecha_entrada", nullable = false)
    private Date fechaEntrada;

    @Column(name = "numero_noches")
    private int numeroNoches;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_reserva", nullable = false, length = 30)
    private TipoReserva tipoReserva;

    @Column(name = "importe_total")
    private double importeTotal;

    @Column(name = "importe_anticipo")
    private double importeAnticipo;

    @Temporal(TemporalType.DATE)
    @Column(name = "fecha_limite_pago")
    private Date fechaLimitePago;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EstadoReserva estado;

    @ManyToOne
    @JoinColumn(name = "id_cliente")
    private Cliente cliente;

    @ManyToOne
    @JoinColumn(name = "codigo_casa", nullable = false)
    private CasaRural casaRural;

    @ManyToMany
    @JoinTable(
            name = "detalle_reserva_habitacion",
            joinColumns = @JoinColumn(name = "numero_reserva"),
            inverseJoinColumns = @JoinColumn(name = "id_habitacion")
    )
    private List<Habitacion> habitaciones = new ArrayList<>();

    @OneToMany(mappedBy = "reserva", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Pago> pagos = new ArrayList<>();

    protected Reserva() {
    }

    public Reserva(Date fechaEntrada, int numeroNoches, TipoReserva tipoReserva,
                   double importeTotal, EstadoReserva estado, Cliente cliente, CasaRural casaRural,
                   List<Habitacion> habitaciones) {
        this.fechaEntrada = fechaEntrada;
        this.numeroNoches = numeroNoches;
        this.tipoReserva = tipoReserva;
        this.importeTotal = importeTotal;
        this.importeAnticipo = calcularAnticipo();
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

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public CasaRural getCasaRural() {
        return casaRural;
    }

    public void setCasaRural(CasaRural casaRural) {
        this.casaRural = casaRural;
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
        pago.setReserva(this);
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

    @PrePersist
    public void prepararPersistencia() {
        if (fechaReserva == null) {
            fechaReserva = new Date();
        }
        importeAnticipo = calcularAnticipo();
        if (fechaLimitePago == null && fechaReserva != null) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(fechaReserva);
            calendar.add(Calendar.DAY_OF_MONTH, 3);
            fechaLimitePago = calendar.getTime();
        }
    }
}
