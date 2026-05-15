package co.edu.uniquindio.casasrurales.dto;

import co.edu.uniquindio.casasrurales.enums.EstadoReserva;
import co.edu.uniquindio.casasrurales.enums.TipoReserva;

import java.util.Date;

/**
 * DTO de respuesta que devuelve el resumen de una reserva recien creada.
 * Contiene el numero de reserva unico, estado, fechas e importes.
 */
public class ReservaResumenDTO {

    private int numeroReserva;
    private Date fechaReserva;
    private Date fechaEntrada;
    private int numeroNoches;
    private TipoReserva tipoReserva;
    private double importeTotal;
    private double importeAnticipo;
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
        this.tipoReserva = tipoReserva;
        this.importeTotal = importeTotal;
        this.importeAnticipo = importeAnticipo;
        this.fechaLimitePago = fechaLimitePago;
        this.estado = estado;
        this.poblacionCasa = poblacionCasa;
        this.codigoCasa = codigoCasa;
        this.cuentaCorrientePropietario = cuentaCorrientePropietario;
        this.conceptoPago = String.valueOf(numeroReserva);
        this.advertenciaPago = "Debe consignar el anticipo dentro de los 3 dias siguientes o la reserva podra quedar vencida para decision del propietario.";
    }

    public int getNumeroReserva() { return numeroReserva; }
    public Date getFechaReserva() { return fechaReserva; }
    public Date getFechaEntrada() { return fechaEntrada; }
    public int getNumeroNoches() { return numeroNoches; }
    public TipoReserva getTipoReserva() { return tipoReserva; }
    public double getImporteTotal() { return importeTotal; }
    public double getImporteAnticipo() { return importeAnticipo; }
    public Date getFechaLimitePago() { return fechaLimitePago; }
    public EstadoReserva getEstado() { return estado; }
    public String getPoblacionCasa() { return poblacionCasa; }
    public int getCodigoCasa() { return codigoCasa; }
    public String getCuentaCorrientePropietario() { return cuentaCorrientePropietario; }
    public String getConceptoPago() { return conceptoPago; }
    public String getAdvertenciaPago() { return advertenciaPago; }
}
