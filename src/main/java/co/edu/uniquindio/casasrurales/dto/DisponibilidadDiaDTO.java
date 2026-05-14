package co.edu.uniquindio.casasrurales.dto;

import co.edu.uniquindio.casasrurales.enums.EstadoDisponibilidad;
import co.edu.uniquindio.casasrurales.enums.ModalidadAlquiler;

import java.util.Date;
import java.util.List;

/**
 * Estado de disponibilidad para una fecha especifica de la consulta.
 */
public class DisponibilidadDiaDTO {

    private Date fecha;
    private EstadoDisponibilidad estadoCasaEntera;
    private ModalidadAlquiler modalidadPaquete;
    private List<DisponibilidadHabitacionDTO> habitaciones;

    public DisponibilidadDiaDTO() {
    }

    public DisponibilidadDiaDTO(Date fecha, EstadoDisponibilidad estadoCasaEntera,
                                ModalidadAlquiler modalidadPaquete,
                                List<DisponibilidadHabitacionDTO> habitaciones) {
        this.fecha = fecha;
        this.estadoCasaEntera = estadoCasaEntera;
        this.modalidadPaquete = modalidadPaquete;
        this.habitaciones = habitaciones;
    }

    public Date getFecha() {
        return fecha;
    }

    public EstadoDisponibilidad getEstadoCasaEntera() {
        return estadoCasaEntera;
    }

    public ModalidadAlquiler getModalidadPaquete() {
        return modalidadPaquete;
    }

    public List<DisponibilidadHabitacionDTO> getHabitaciones() {
        return habitaciones;
    }
}
