package co.edu.uniquindio.casasrurales.entities;

import co.edu.uniquindio.casasrurales.enums.ModalidadAlquiler;

import java.util.Date;

public class PaqueteAlquiler {

    private int idPaquete;
    private Date fechaInicio;
    private Date fechaFin;
    private ModalidadAlquiler modalidad;
    private double precioCasaEntera;
    private double precioHabitacion;
    private boolean disponible;

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
}
