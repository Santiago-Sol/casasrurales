package co.edu.uniquindio.casasrurales.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.Date;

/**
 * Datos requeridos para registrar un pago recibido por el propietario.
 */
public class PagoRegistroDTO {

    @NotNull(message = "El monto del pago es obligatorio")
    @Min(value = 1, message = "El monto debe ser mayor a cero")
    private Double monto;

    private Date fechaPago;

    public Double getMonto() {
        return monto;
    }

    public void setMonto(Double monto) {
        this.monto = monto;
    }

    public Date getFechaPago() {
        return fechaPago;
    }

    public void setFechaPago(Date fechaPago) {
        this.fechaPago = fechaPago;
    }
}
