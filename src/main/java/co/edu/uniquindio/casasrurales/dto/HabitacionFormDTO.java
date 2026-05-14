package co.edu.uniquindio.casasrurales.dto;

import co.edu.uniquindio.casasrurales.enums.TipoCama;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO para registrar las habitaciones de una casa rural.
 */
public class HabitacionFormDTO {

    @NotBlank(message = "El codigo de la habitacion es obligatorio")
    @Size(max = 50, message = "El codigo de la habitacion no puede superar 50 caracteres")
    private String codigoHabitacion;

    @Min(value = 1, message = "Cada habitacion debe tener al menos una cama")
    private Integer numeroCamas;

    @NotNull(message = "El tipo de cama es obligatorio")
    private TipoCama tipoCama;

    @NotNull(message = "Debe indicar si la habitacion tiene bano")
    private Boolean tieneBano;

    public String getCodigoHabitacion() {
        return codigoHabitacion;
    }

    public void setCodigoHabitacion(String codigoHabitacion) {
        this.codigoHabitacion = codigoHabitacion;
    }

    public Integer getNumeroCamas() {
        return numeroCamas;
    }

    public void setNumeroCamas(Integer numeroCamas) {
        this.numeroCamas = numeroCamas;
    }

    public TipoCama getTipoCama() {
        return tipoCama;
    }

    public void setTipoCama(TipoCama tipoCama) {
        this.tipoCama = tipoCama;
    }

    public Boolean getTieneBano() {
        return tieneBano;
    }

    public void setTieneBano(Boolean tieneBano) {
        this.tieneBano = tieneBano;
    }
}
