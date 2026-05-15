package co.edu.uniquindio.casasrurales.dto;

import co.edu.uniquindio.casasrurales.enums.EstadoReserva;
import co.edu.uniquindio.casasrurales.enums.TipoReserva;

import java.util.Calendar;
import java.util.Date;

/**
 * DTO de respuesta que devuelve el resumen de una reserva recien creada.
 * Contiene el numero de reserva unico, estado, fechas e importes.
 */
public class ReservaResumenDTO {

    private int numeroReserva;
    private Date fechaReserva;
    private Date fechaEntrada;
    private Date fechaSalida;
    private int numeroNoches;
    private TipoReserva tipoReserva;
    private double importeTotal;
    private double importeAnticipo;
    private double importeAConsignar;
    private double importePagado;
    private double saldoPendiente;
    private Date fechaLimitePago;
    private EstadoReserva estado;
    private String poblacionCasa;
    private int codigoCasa;
    private String cuentaCorrientePropietario;
    private String conceptoPago;
    private String advertenciaPago;

    public ReservaResumenDTO() {
    }

    public ReservaResumenDTO(int numeroReserva, Date fechaReserva, Date fechaEntrada,
                             int numeroNoches, TipoReserva tipoReserva, double importeTotal,
                             double importeAnticipo, Date fechaLimitePago, EstadoReserva estado,
                             String poblacionCasa, int codigoCasa) {
        this(numeroReserva, fechaReserva, fechaEntrada, numeroNoches, tipoReserva, importeTotal,
                importeAnticipo, fechaLimitePago, estado, poblacionCasa, codigoCasa, null);
    }

    public ReservaResumenDTO(int numeroReserva, Date fechaReserva, Date fechaEntrada,
                             int numeroNoches, TipoReserva tipoReserva, double importeTotal,
                             double importeAnticipo, Date fechaLimitePago, EstadoReserva estado,
                             String poblacionCasa, int codigoCasa, String cuentaCorrientePropietario) {
        this.numeroReserva = numeroReserva;
        this.fechaReserva = fechaReserva;
        this.fechaEntrada = fechaEntrada;
        this.numeroNoches = numeroNoches;
        this.fechaSalida = calcularFechaSalida(fechaEntrada, numeroNoches);
        this.tipoReserva = tipoReserva;
        this.importeTotal = importeTotal;
        this.importeAnticipo = importeAnticipo;
        this.importeAConsignar = calcularImporteAConsignar(fechaEntrada, importeTotal, importeAnticipo);
        this.saldoPendiente = importeTotal;
        this.fechaLimitePago = fechaLimitePago;
        this.estado = estado;
        this.poblacionCasa = poblacionCasa;
        this.codigoCasa = codigoCasa;
        this.cuentaCorrientePropietario = cuentaCorrientePropietario;
        this.conceptoPago = String.valueOf(numeroReserva);
        this.advertenciaPago = this.importeAConsignar >= importeTotal
                ? "Debe pagar el valor total porque la fecha de entrada es dentro de los proximos 3 dias."
                : "Debe consignar el anticipo dentro de los 3 dias siguientes. El saldo restante debe pagarse antes de la fecha de salida.";
    }

    public int getNumeroReserva() { return numeroReserva; }
    public Date getFechaReserva() { return fechaReserva; }
    public Date getFechaEntrada() { return fechaEntrada; }
    public Date getFechaSalida() { return fechaSalida; }
    public int getNumeroNoches() { return numeroNoches; }
    public TipoReserva getTipoReserva() { return tipoReserva; }
    public double getImporteTotal() { return importeTotal; }
    public double getImporteAnticipo() { return importeAnticipo; }
    public double getImporteAConsignar() { return importeAConsignar; }
    public double getImportePagado() { return importePagado; }
    public double getSaldoPendiente() { return saldoPendiente; }
    public Date getFechaLimitePago() { return fechaLimitePago; }
    public EstadoReserva getEstado() { return estado; }
    public String getPoblacionCasa() { return poblacionCasa; }
    public int getCodigoCasa() { return codigoCasa; }
    public String getCuentaCorrientePropietario() { return cuentaCorrientePropietario; }
    public String getConceptoPago() { return conceptoPago; }
    public String getAdvertenciaPago() { return advertenciaPago; }

    public void setImportePagado(double importePagado) {
        this.importePagado = importePagado;
        this.saldoPendiente = Math.max(0, importeTotal - importePagado);
    }

    private double calcularImporteAConsignar(Date fechaEntrada, double importeTotal, double importeAnticipo) {
        if (fechaEntrada == null) {
            return importeAnticipo;
        }

        Calendar limiteAnticipo = Calendar.getInstance();
        normalizarInicioDia(limiteAnticipo);
        limiteAnticipo.add(Calendar.DAY_OF_MONTH, 3);

        Calendar entrada = Calendar.getInstance();
        entrada.setTime(fechaEntrada);
        normalizarInicioDia(entrada);

        return entrada.after(limiteAnticipo) ? importeAnticipo : importeTotal;
    }

    private Date calcularFechaSalida(Date fechaEntrada, int numeroNoches) {
        if (fechaEntrada == null || numeroNoches < 1) {
            return null;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(fechaEntrada);
        normalizarInicioDia(calendar);
        calendar.add(Calendar.DAY_OF_MONTH, numeroNoches);
        return calendar.getTime();
    }

    private void normalizarInicioDia(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
    }
}
