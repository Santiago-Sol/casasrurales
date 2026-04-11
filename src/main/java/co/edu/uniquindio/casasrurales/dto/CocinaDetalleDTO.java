package co.edu.uniquindio.casasrurales.dto;

/**
 * DTO para mostrar detalles de una cocina.
 */
public class CocinaDetalleDTO {

    private boolean tieneLavavajillas;
    private boolean tieneLavadora;

    public CocinaDetalleDTO(boolean tieneLavavajillas, boolean tieneLavadora) {
        this.tieneLavavajillas = tieneLavavajillas;
        this.tieneLavadora = tieneLavadora;
    }

    // Getters
    public boolean isTieneLavavajillas() {
        return tieneLavavajillas;
    }

    public boolean isTieneLavadora() {
        return tieneLavadora;
    }
}
