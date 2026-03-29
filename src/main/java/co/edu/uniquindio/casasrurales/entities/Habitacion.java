package co.edu.uniquindio.casasrurales.entities;

import co.edu.uniquindio.casasrurales.enums.EstadoReserva;
import co.edu.uniquindio.casasrurales.enums.TipoCama;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class Habitacion {

    private String codigoHabitacion;
    private int numeroCamas;
    private TipoCama tipoCama;
    private boolean tieneBano;
    private final List<Reserva> reservas = new ArrayList<>();

    public Habitacion(String codigoHabitacion, int numeroCamas, TipoCama tipoCama, boolean tieneBano) {
        this.codigoHabitacion = codigoHabitacion;
        this.numeroCamas = numeroCamas;
        this.tipoCama = tipoCama;
        this.tieneBano = tieneBano;
    }

    public String getCodigoHabitacion() {
        return codigoHabitacion;
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
