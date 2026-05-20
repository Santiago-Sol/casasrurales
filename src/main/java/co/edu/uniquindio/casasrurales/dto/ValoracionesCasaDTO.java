package co.edu.uniquindio.casasrurales.dto;

import java.util.List;

public class ValoracionesCasaDTO {

    private double promedio;
    private int total;
    private List<ValoracionCasaDTO> valoraciones;

    public ValoracionesCasaDTO(double promedio, int total, List<ValoracionCasaDTO> valoraciones) {
        this.promedio = promedio;
        this.total = total;
        this.valoraciones = valoraciones;
    }

    public double getPromedio() {
        return promedio;
    }

    public int getTotal() {
        return total;
    }

    public List<ValoracionCasaDTO> getValoraciones() {
        return valoraciones;
    }
}
