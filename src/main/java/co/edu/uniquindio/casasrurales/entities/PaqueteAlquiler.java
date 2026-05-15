package co.edu.uniquindio.casasrurales.entities;

import co.edu.uniquindio.casasrurales.enums.ModalidadAlquiler;
import co.edu.uniquindio.casasrurales.enums.TipoReserva;
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

import java.util.Calendar;
import java.util.Date;

/**
 * Entidad que define una oferta de alquiler para una casa rural en un rango de fechas.
 * Incluye modalidad, precio y reglas temporales de disponibilidad.
 */
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

    @Column(name = "precio_casa_entera", nullable = false)
    private double precioCasaEntera;

    @Column(name = "precio_por_habitacion", nullable = false)
    private double precioHabitacion;

    @Column(name = "disponible")
    private boolean disponible;

    @Column(name = "creado_por_propietario")
    private Integer creadoPorPropietario;

    @Column(name = "modificado_por_propietario")
    private Integer modificadoPorPropietario;

    @Column(name = "auditoria_cambios", length = 500)
    private String auditoriaCambios;

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

    public PaqueteAlquiler(Date fechaInicio, Date fechaFin, ModalidadAlquiler modalidad,
                           double precioCasaEntera, double precioHabitacion, boolean disponible) {
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

    public Integer getCreadoPorPropietario() {
        return creadoPorPropietario;
    }

    public Integer getModificadoPorPropietario() {
        return modificadoPorPropietario;
    }

    public String getAuditoriaCambios() {
        return auditoriaCambios;
    }

    public void setCasaRural(CasaRural casaRural) {
        this.casaRural = casaRural;
    }

    public boolean incluyeFecha(Date fecha) {
        Date fechaNormalizada = inicioDia(fecha);
        return disponible
                && !fechaNormalizada.before(inicioDia(fechaInicio))
                && !fechaNormalizada.after(inicioDia(fechaFin));
    }

    public boolean permiteCasaEntera() {
        return modalidad == ModalidadAlquiler.CASA_ENTERA || modalidad == ModalidadAlquiler.AMBAS;
    }

    public boolean permiteHabitaciones() {
        return modalidad == ModalidadAlquiler.POR_HABITACIONES || modalidad == ModalidadAlquiler.AMBAS;
    }

    public double obtenerPrecioPara(TipoReserva tipoReserva) {
        if (tipoReserva == null) {
            throw new IllegalArgumentException("El tipo de reserva es obligatorio");
        }
        return switch (tipoReserva) {
            case CASA_ENTERA -> {
                if (!permiteCasaEntera()) {
                    throw new IllegalStateException("El paquete no permite alquilar la casa completa");
                }
                yield precioCasaEntera;
            }
            case POR_HABITACIONES -> {
                if (!permiteHabitaciones()) {
                    throw new IllegalStateException("El paquete no permite alquilar por habitaciones");
                }
                yield precioHabitacion;
            }
        };
    }

    public double calcularPrecio() {
        return obtenerPrecioPara(modalidad == ModalidadAlquiler.POR_HABITACIONES
                ? TipoReserva.POR_HABITACIONES
                : TipoReserva.CASA_ENTERA);
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

    public void registrarCreacionPor(int idPropietario) {
        creadoPorPropietario = idPropietario;
        registrarModificacionPor(idPropietario, "Creacion del paquete");
    }

    public void registrarModificacionPor(int idPropietario, String detalle) {
        modificadoPorPropietario = idPropietario;
        auditoriaCambios = detalle == null || detalle.isBlank() ? "Cambio de paquete" : detalle;
    }

    @PrePersist
    public void registrarFechasCreacion() {
        validarIntegridad();
        validarPreciosObligatorios();
        Date ahora = new Date();
        fechaCreacion = ahora;
        fechaModificacion = ahora;
    }

    @PreUpdate
    public void actualizarFechaModificacion() {
        validarIntegridad();
        validarPreciosObligatorios();
        fechaModificacion = new Date();
    }

    public void validarPreciosObligatorios() {
        if (modalidad == null) {
            throw new IllegalArgumentException("La modalidad del paquete es obligatoria");
        }
        if (precioCasaEntera < 0 || precioHabitacion < 0) {
            throw new IllegalArgumentException("Los precios del paquete no pueden ser negativos");
        }
        if (permiteCasaEntera() && precioCasaEntera <= 0) {
            throw new IllegalArgumentException("El precio de casa entera debe ser mayor a cero");
        }
        if (permiteHabitaciones() && precioHabitacion <= 0) {
            throw new IllegalArgumentException("El precio por habitacion debe ser mayor a cero");
        }
    }

    public void validarIntegridad() {
        if (casaRural == null) {
            throw new IllegalStateException("El paquete debe estar asociado a una casa rural");
        }
    }

    private Date inicioDia(Date fecha) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(fecha);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }
}
