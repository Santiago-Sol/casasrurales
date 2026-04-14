package co.edu.uniquindio.casasrurales.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.Date;
import java.util.List;

/**
 * DTO que transporta los datos del formulario de reserva enviado por el cliente.
 * Concentra las validaciones basicas del lado del servidor.
 */
public class ReservaRequestDTO {

    @NotNull(message = "El codigo de la casa es obligatorio")
    private Integer codigoCasa;

    @NotNull(message = "La fecha de entrada es obligatoria")
    @Future(message = "La fecha de entrada debe ser futura")
    private Date fechaEntrada;

    @Min(value = 1, message = "El numero de noches debe ser al menos 1")
    private int numeroNoches;

    @NotNull(message = "El importe total es obligatorio")
    @Min(value = 0, message = "El importe total no puede ser negativo")
    private Double importeTotal;

    private List<Integer> idsHabitaciones;

    public Integer getCodigoCasa() {
        return codigoCasa;
    }

    public void setCodigoCasa(Integer codigoCasa) {
        this.codigoCasa = codigoCasa;
    }

    public Date getFechaEntrada() {
        return fechaEntrada;
    }

    public void setFechaEntrada(Date fechaEntrada) {
        this.fechaEntrada = fechaEntrada;
    }

    public int getNumeroNoches() {
        return numeroNoches;
    }

    public void setNumeroNoches(int numeroNoches) {
        this.numeroNoches = numeroNoches;
    }

    public Double getImporteTotal() {
        return importeTotal;
    }

    public void setImporteTotal(Double importeTotal) {
        this.importeTotal = importeTotal;
    }

    public List<Integer> getIdsHabitaciones() {
        return idsHabitaciones;
    }

    public void setIdsHabitaciones(List<Integer> idsHabitaciones) {
        this.idsHabitaciones = idsHabitaciones;
    }
}
