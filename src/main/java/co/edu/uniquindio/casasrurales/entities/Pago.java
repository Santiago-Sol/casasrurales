package co.edu.uniquindio.casasrurales.entities;

import co.edu.uniquindio.casasrurales.enums.EstadoPago;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

import java.util.Date;

@Entity
@Table(name = "pago")
public class Pago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pago")
    private int idPago;

    @Temporal(TemporalType.DATE)
    @Column(name = "fecha_pago", nullable = false)
    private Date fechaPago;

    @Column(nullable = false)
    private double monto;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 30)
    private EstadoPago estado;

    @ManyToOne
    @JoinColumn(name = "numero_reserva", nullable = false)
    private Reserva reserva;

    protected Pago() {
    }

    public Pago(Date fechaPago, double monto, EstadoPago estado) {
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

    public Reserva getReserva() {
        return reserva;
    }

    public void setReserva(Reserva reserva) {
        this.reserva = reserva;
    }

    public boolean validarPago() {
        return monto > 0;
    }

    public void registrar() {
        estado = validarPago() ? EstadoPago.VERIFICADO : EstadoPago.RECHAZADO;
    }
}
