package co.edu.uniquindio.casasrurales.dto;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Resultado completo de una consulta de disponibilidad para una casa rural.
 */
public class DisponibilidadCasaDTO {

    private int codigoCasa;
    private Date fechaEntrada;
    private Date fechaSalida;
    private int numeroNoches;
    private List<DisponibilidadDiaDTO> dias;

    public DisponibilidadCasaDTO() {
    }

    public DisponibilidadCasaDTO(int codigoCasa, Date fechaEntrada, int numeroNoches,
                                 List<DisponibilidadDiaDTO> dias) {
        this.codigoCasa = codigoCasa;
        this.fechaEntrada = fechaEntrada;
        this.numeroNoches = numeroNoches;
        this.fechaSalida = calcularFechaSalida(fechaEntrada, numeroNoches);
        this.dias = dias;
    }

    public int getCodigoCasa() {
        return codigoCasa;
    }

    public Date getFechaEntrada() {
        return fechaEntrada;
    }

    public Date getFechaSalida() {
        return fechaSalida;
    }

    public int getNumeroNoches() {
        return numeroNoches;
    }

    public List<DisponibilidadDiaDTO> getDias() {
        return dias;
    }

    private Date calcularFechaSalida(Date fechaEntrada, int numeroNoches) {
        if (fechaEntrada == null || numeroNoches < 1) {
            return null;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(fechaEntrada);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.add(Calendar.DAY_OF_MONTH, numeroNoches);
        return calendar.getTime();
    }
}
