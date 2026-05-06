package co.edu.uniquindio.casasrurales.dto;

import co.edu.uniquindio.casasrurales.enums.ModalidadAlquiler;
import jakarta.validation.constraints.NotNull;
import java.util.Date;

public class PaqueteAlquilerDTO {

    private Integer idPaquete;

    @NotNull(message = "La fecha de inicio es obligatoria")
    private Date fechaInicio;

    @NotNull(message = "La fecha de fin es obligatoria")
    private Date fechaFin;

    @NotNull(message = "La modalidad es obligatoria")
    private ModalidadAlquiler modalidad;

    private double precioCasaEntera;
    private double precioHabitacion;

    private boolean disponible = true;

    public PaqueteAlquilerDTO() {
    }

    public PaqueteAlquilerDTO(Integer idPaquete, Date fechaInicio, Date fechaFin, ModalidadAlquiler modalidad, double precioCasaEntera, double precioHabitacion, boolean disponible) {
        this.idPaquete = idPaquete;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.modalidad = modalidad;
        this.precioCasaEntera = precioCasaEntera;
        this.precioHabitacion = precioHabitacion;
        this.disponible = disponible;
    }

    public Integer getIdPaquete() {
        return idPaquete;
    }

    public void setIdPaquete(Integer idPaquete) {
        this.idPaquete = idPaquete;
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

    public void setModalidad(ModalidadAlquiler modalidad) {
        this.modalidad = modalidad;
    }

    public double getPrecioCasaEntera() {
        return precioCasaEntera;
    }

    public void setPrecioCasaEntera(double precioCasaEntera) {
        this.precioCasaEntera = precioCasaEntera;
    }

    public double getPrecioHabitacion() {
        return precioHabitacion;
    }

    public void setPrecioHabitacion(double precioHabitacion) {
        this.precioHabitacion = precioHabitacion;
    }

    public boolean isDisponible() {
        return disponible;
    }

    public void setDisponible(boolean disponible) {
        this.disponible = disponible;
    }
}
