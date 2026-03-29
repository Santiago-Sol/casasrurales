package co.edu.uniquindio.casasrurales.entities;

import java.util.ArrayList;
import java.util.List;

public class Cliente extends Usuario {

    private final List<Reserva> reservas = new ArrayList<>();

    public Cliente(int idUsuario, String telefono) {
        super(idUsuario, telefono);
    }

    public List<Reserva> getReservas() {
        return List.copyOf(reservas);
    }

    public void solicitarReserva() {
    }

    public void agregarReserva(Reserva reserva) {
        reservas.add(reserva);
    }
}
