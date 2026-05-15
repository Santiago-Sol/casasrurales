package co.edu.uniquindio.casasrurales.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * Datos enviados por el cliente desde la pasarela de pagos simulada.
 */
public class PagoPasarelaDTO {

    @NotNull(message = "El monto del pago es obligatorio")
    @Min(value = 1, message = "El monto debe ser mayor a cero")
    private Double monto;

    @NotBlank(message = "El nombre del titular es obligatorio")
    private String titular;

    @NotBlank(message = "El numero de tarjeta es obligatorio")
    @Pattern(regexp = "^[0-9 ]{13,19}$", message = "Ingresa un numero de tarjeta valido")
    private String numeroTarjeta;

    @NotBlank(message = "La fecha de vencimiento es obligatoria")
    @Pattern(regexp = "^(0[1-9]|1[0-2])/[0-9]{2}$", message = "Usa el formato MM/AA")
    private String vencimiento;

    @NotBlank(message = "El codigo de seguridad es obligatorio")
    @Pattern(regexp = "^[0-9]{3,4}$", message = "Ingresa un codigo de seguridad valido")
    private String cvv;

    public Double getMonto() {
        return monto;
    }

    public void setMonto(Double monto) {
        this.monto = monto;
    }

    public String getTitular() {
        return titular;
    }

    public void setTitular(String titular) {
        this.titular = titular;
    }

    public String getNumeroTarjeta() {
        return numeroTarjeta;
    }

    public void setNumeroTarjeta(String numeroTarjeta) {
        this.numeroTarjeta = numeroTarjeta;
    }

    public String getVencimiento() {
        return vencimiento;
    }

    public void setVencimiento(String vencimiento) {
        this.vencimiento = vencimiento;
    }

    public String getCvv() {
        return cvv;
    }

    public void setCvv(String cvv) {
        this.cvv = cvv;
    }
}
