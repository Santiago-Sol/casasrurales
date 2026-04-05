package co.edu.uniquindio.casasrurales.entities;

import co.edu.uniquindio.casasrurales.enums.ModalidadAlquiler;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

import java.util.Date;

@Entity
@Table(name = "paquete_alquiler")
public class PaqueteAlquiler {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_paquete")
    private int idPaquete;

    @Temporal(TemporalType.DATE)
    @Column(name = "fecha_inicio", nullable = false)
    private Date fechaInicio;

    @Temporal(TemporalType.DATE)
    @Column(name = "fecha_fin", nullable = false)
    private Date fechaFin;

    @Enumerated(EnumType.STRING)
    @Column(name = "modalidad_alquiler", nullable = false, length = 30)
    private ModalidadAlquiler modalidad;

    @Column(name = "precio_casa_entera")
    private double precioCasaEntera;

    @Column(name = "precio_por_habitacion")
    private double precioHabitacion;

    @Column(name = "disponible")
    private boolean disponible;

    @Temporal(TemporalType.DATE)
    @Column(name = "fecha_creacion", nullable = false)
    private Date fechaCreacion;

    @Temporal(TemporalType.DATE)
    @Column(name = "fecha_modificacion", nullable = false)
    private Date fechaModificacion;

    @ManyToOne
    @JoinColumn(name = "codigo_casa", nullable = false)
    private CasaRural casaRural;

    protected PaqueteAlquiler() {
    }

    public PaqueteAlquiler(int idPaquete, Date fechaInicio, Date fechaFin, ModalidadAlquiler modalidad,
                           double precioCasaEntera, double precioHabitacion, boolean disponible) {
        this.idPaquete = idPaquete;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.modalidad = modalidad;
        this.precioCasaEntera = precioCasaEntera;
        this.precioHabitacion = precioHabitacion;
        this.disponible = disponible;
    }

    public int getIdPaquete() {
        return idPaquete;
    }

    public Date getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(Date fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public Date getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(Date fechaFin) {
        this.fechaFin = fechaFin;
    }

    public ModalidadAlquiler getModalidad() {
        return modalidad;
    }

    public double getPrecioCasaEntera() {
        return precioCasaEntera;
    }

    public double getPrecioHabitacion() {
        return precioHabitacion;
    }

    public boolean isDisponible() {
        return disponible;
    }

    public CasaRural getCasaRural() {
        return casaRural;
    }

    public void setCasaRural(CasaRural casaRural) {
        this.casaRural = casaRural;
    }

    public boolean incluyeFecha(Date fecha) {
        return disponible && !fecha.before(fechaInicio) && !fecha.after(fechaFin);
    }

    public boolean permiteCasaEntera() {
        return modalidad == ModalidadAlquiler.CASA_ENTERA || modalidad == ModalidadAlquiler.AMBAS;
    }

    public boolean permiteHabitaciones() {
        return modalidad == ModalidadAlquiler.POR_HABITACIONES || modalidad == ModalidadAlquiler.AMBAS;
    }

    public double calcularPrecio() {
        return switch (modalidad) {
            case CASA_ENTERA -> precioCasaEntera;
            case POR_HABITACIONES -> precioHabitacion;
            case AMBAS -> precioCasaEntera;
        };
    }

    public void modificar(Date fechaInicio, Date fechaFin, ModalidadAlquiler modalidad, double precioCasaEntera,
                          double precioHabitacion, boolean disponible) {
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.modalidad = modalidad;
        this.precioCasaEntera = precioCasaEntera;
        this.precioHabitacion = precioHabitacion;
        this.disponible = disponible;
    }

    @PrePersist
    public void registrarFechasCreacion() {
        Date ahora = new Date();
        fechaCreacion = ahora;
        fechaModificacion = ahora;
    }

    @PreUpdate
    public void actualizarFechaModificacion() {
        fechaModificacion = new Date();
    }
}
