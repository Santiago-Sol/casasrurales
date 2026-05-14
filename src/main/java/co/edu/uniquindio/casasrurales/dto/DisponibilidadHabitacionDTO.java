package co.edu.uniquindio.casasrurales.dto;

import co.edu.uniquindio.casasrurales.enums.EstadoDisponibilidad;

/**
 * Estado de una habitacion puntual dentro de un dia consultado.
 */
public class DisponibilidadHabitacionDTO {

    private int idHabitacion;
    private String codigoHabitacion;
    private EstadoDisponibilidad estado;

    public DisponibilidadHabitacionDTO() {
    }

    public DisponibilidadHabitacionDTO(int idHabitacion, String codigoHabitacion, EstadoDisponibilidad estado) {
        this.idHabitacion = idHabitacion;
        this.codigoHabitacion = codigoHabitacion;
        this.estado = estado;
    }

    public int getIdHabitacion() {
        return idHabitacion;
    }

    public String getCodigoHabitacion() {
        return codigoHabitacion;
    }

    public EstadoDisponibilidad getEstado() {
        return estado;
    }
}
