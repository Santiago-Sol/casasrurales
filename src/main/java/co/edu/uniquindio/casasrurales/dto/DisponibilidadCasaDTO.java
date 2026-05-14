package co.edu.uniquindio.casasrurales.dto;

import java.util.Date;
import java.util.List;

/**
 * Resultado completo de una consulta de disponibilidad para una casa rural.
 */
public class DisponibilidadCasaDTO {

    private int codigoCasa;
    private Date fechaEntrada;
    private int numeroNoches;
    private List<DisponibilidadDiaDTO> dias;

    public DisponibilidadCasaDTO() {
    }

    public DisponibilidadCasaDTO(int codigoCasa, Date fechaEntrada, int numeroNoches,
                                 List<DisponibilidadDiaDTO> dias) {
        this.codigoCasa = codigoCasa;
        this.fechaEntrada = fechaEntrada;
        this.numeroNoches = numeroNoches;
        this.dias = dias;
    }

    public int getCodigoCasa() {
        return codigoCasa;
    }

    public Date getFechaEntrada() {
        return fechaEntrada;
    }

    public int getNumeroNoches() {
        return numeroNoches;
    }

    public List<DisponibilidadDiaDTO> getDias() {
        return dias;
    }
}
