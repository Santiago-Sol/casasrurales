package co.edu.uniquindio.casasrurales.dto;

import co.edu.uniquindio.casasrurales.enums.EstadoReserva;
import co.edu.uniquindio.casasrurales.enums.TipoReserva;

import java.util.Calendar;
import java.util.Date;

/**
 * Resumen de reserva visible para el propietario.
 */
public class ReservaPropietarioDTO {

    private int numeroReserva;
    private int codigoCasa;
    private String nombreCasa;
    private String poblacion;
    private Date fechaReserva;
    private Date fechaEntrada;
    private Date fechaSalida;
    private int numeroNoches;
    private Date fechaLimitePago;
    private double importeTotal;
    private double importeAnticipo;
    private EstadoReserva estado;
    private TipoReserva tipoReserva;
    private boolean vencida;

    public ReservaPropietarioDTO() {
    }

    public ReservaPropietarioDTO(int numeroReserva, int codigoCasa, String nombreCasa, String poblacion,
                                 Date fechaReserva, Date fechaEntrada, int numeroNoches, Date fechaLimitePago,
                                 double importeTotal, double importeAnticipo, EstadoReserva estado,
                                 TipoReserva tipoReserva, boolean vencida) {
        this.numeroReserva = numeroReserva;
        this.codigoCasa = codigoCasa;
        this.nombreCasa = nombreCasa;
        this.poblacion = poblacion;
        this.fechaReserva = fechaReserva;
        this.fechaEntrada = fechaEntrada;
        this.numeroNoches = numeroNoches;
        this.fechaSalida = calcularFechaSalida(fechaEntrada, numeroNoches);
        this.fechaLimitePago = fechaLimitePago;
        this.importeTotal = importeTotal;
        this.importeAnticipo = importeAnticipo;
        this.estado = estado;
        this.tipoReserva = tipoReserva;
        this.vencida = vencida;
    }

    public int getNumeroReserva() { return numeroReserva; }
    public int getCodigoCasa() { return codigoCasa; }
    public String getNombreCasa() { return nombreCasa; }
    public String getPoblacion() { return poblacion; }
    public Date getFechaReserva() { return fechaReserva; }
    public Date getFechaEntrada() { return fechaEntrada; }
    public Date getFechaSalida() { return fechaSalida; }
    public int getNumeroNoches() { return numeroNoches; }
    public Date getFechaLimitePago() { return fechaLimitePago; }
    public double getImporteTotal() { return importeTotal; }
    public double getImporteAnticipo() { return importeAnticipo; }
    public EstadoReserva getEstado() { return estado; }
    public TipoReserva getTipoReserva() { return tipoReserva; }
    public boolean isVencida() { return vencida; }

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
