package co.edu.uniquindio.casasrurales.dto;

/**
 * DTO para mostrar detalles de un baño.
 */
public class BanoDetalleDTO {

    private String observaciones;

    public BanoDetalleDTO(String observaciones) {
        this.observaciones = observaciones;
    }

    // Getter
    public String getObservaciones() {
        return observaciones;
    }
}
