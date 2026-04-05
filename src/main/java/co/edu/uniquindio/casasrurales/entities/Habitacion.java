package co.edu.uniquindio.casasrurales.entities;

import co.edu.uniquindio.casasrurales.enums.EstadoReserva;
import co.edu.uniquindio.casasrurales.enums.TipoCama;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "habitacion")
public class Habitacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_habitacion")
    private int idHabitacion;

    @Column(name = "codigo_habitacion", nullable = false, length = 50)
    private String codigoHabitacion;

    @Column(name = "numero_camas")
    private int numeroCamas;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_cama", nullable = false, length = 30)
    private TipoCama tipoCama;

    @Column(name = "tiene_bano")
    private boolean tieneBano;

    @ManyToOne
    @JoinColumn(name = "codigo_casa", nullable = false)
    private CasaRural casaRural;

    @ManyToMany(mappedBy = "habitaciones")
    private List<Reserva> reservas = new ArrayList<>();

    protected Habitacion() {
    }

    public Habitacion(String codigoHabitacion, int numeroCamas, TipoCama tipoCama, boolean tieneBano) {
        this.codigoHabitacion = codigoHabitacion;
        this.numeroCamas = numeroCamas;
        this.tipoCama = tipoCama;
        this.tieneBano = tieneBano;
    }

    public String getCodigoHabitacion() {
        return codigoHabitacion;
    }

    public int getIdHabitacion() {
        return idHabitacion;
    }

    public int getNumeroCamas() {
        return numeroCamas;
    }

    public TipoCama getTipoCama() {
        return tipoCama;
    }

    public boolean isTieneBano() {
        return tieneBano;
    }

    public List<Reserva> getReservas() {
        return List.copyOf(reservas);
    }

    public CasaRural getCasaRural() {
        return casaRural;
    }

    public void setCasaRural(CasaRural casaRural) {
        this.casaRural = casaRural;
    }

    public boolean estaDisponible(Date fecha) {
        return reservas.stream()
                .filter(reserva -> reserva.getEstado() != EstadoReserva.ANULADA)
                .noneMatch(reserva -> estaEnRango(fecha, reserva));
    }

    private boolean estaEnRango(Date fecha, Reserva reserva) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(reserva.getFechaEntrada());
        calendar.add(Calendar.DAY_OF_MONTH, reserva.getNumeroNoches() - 1);
        Date fechaSalida = calendar.getTime();
        return !fecha.before(reserva.getFechaEntrada()) && !fecha.after(fechaSalida);
    }

    public void agregarReserva(Reserva reserva) {
        reservas.add(reserva);
    }

    public String mostrarDatos() {
        return "Habitacion{codigo='%s', camas=%d, tipoCama=%s, tieneBano=%s}"
                .formatted(codigoHabitacion, numeroCamas, tipoCama, tieneBano);
    }
}
