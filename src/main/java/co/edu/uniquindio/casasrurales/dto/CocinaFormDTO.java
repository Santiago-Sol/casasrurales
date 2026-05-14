package co.edu.uniquindio.casasrurales.dto;

import jakarta.validation.constraints.NotNull;

/**
 * DTO para registrar el equipamiento de una cocina.
 */
public class CocinaFormDTO {

    @NotNull(message = "Debe indicar si la cocina tiene lavavajillas")
    private Boolean tieneLavavajillas;

    @NotNull(message = "Debe indicar si la cocina tiene lavadora")
    private Boolean tieneLavadora;

    public Boolean getTieneLavavajillas() {
        return tieneLavavajillas;
    }

    public void setTieneLavavajillas(Boolean tieneLavavajillas) {
        this.tieneLavavajillas = tieneLavavajillas;
    }

    public Boolean getTieneLavadora() {
        return tieneLavadora;
    }

    public void setTieneLavadora(Boolean tieneLavadora) {
        this.tieneLavadora = tieneLavadora;
    }
}
