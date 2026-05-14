package co.edu.uniquindio.casasrurales.dto;

/**
 * DTO para mostrar detalles de una habitación.
 */
public class HabitacionDetalleDTO {

    private String codigoHabitacion;
    private int idHabitacion;
    private int numeroCamas;
    private String tipoCama;
    private boolean tieneBano;

    public HabitacionDetalleDTO(String codigoHabitacion, int numeroCamas, String tipoCama, boolean tieneBano) {
        this(0, codigoHabitacion, numeroCamas, tipoCama, tieneBano);
    }

    public HabitacionDetalleDTO(int idHabitacion, String codigoHabitacion, int numeroCamas, String tipoCama, boolean tieneBano) {
        this.idHabitacion = idHabitacion;
        this.codigoHabitacion = codigoHabitacion;
        this.numeroCamas = numeroCamas;
        this.tipoCama = tipoCama;
        this.tieneBano = tieneBano;
    }

    // Getters
    public String getCodigoHabitacion() {
        return codigoHabitacion;
    }

    public int getIdHabitacion() {
        return idHabitacion;
    }

    public int getNumeroCamas() {
        return numeroCamas;
    }

    public String getTipoCama() {
        return tipoCama;
    }

    public boolean isTieneBano() {
        return tieneBano;
    }
}
