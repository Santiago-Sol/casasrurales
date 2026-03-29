package co.edu.uniquindio.casasrurales.entities;

import co.edu.uniquindio.casasrurales.enums.EstadoPago;

import java.util.Date;

public class Pago {

    private int idPago;
    private Date fechaPago;
    private double monto;
    private EstadoPago estado;

    public Pago(int idPago, Date fechaPago, double monto, EstadoPago estado) {
        this.idPago = idPago;
        this.fechaPago = fechaPago;
        this.monto = monto;
        this.estado = estado;
    }

    public int getIdPago() {
        return idPago;
    }

    public Date getFechaPago() {
        return fechaPago;
    }

    public double getMonto() {
        return monto;
    }

    public EstadoPago getEstado() {
        return estado;
    }

    public boolean validarPago() {
        return monto > 0;
    }

    public void registrar() {
        estado = validarPago() ? EstadoPago.VERIFICADO : EstadoPago.RECHAZADO;
    }
}
