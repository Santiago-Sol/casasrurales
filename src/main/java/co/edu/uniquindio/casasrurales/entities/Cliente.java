package co.edu.uniquindio.casasrurales.entities;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

/**
 * Entidad que representa a un usuario con rol de cliente.
 * Mantiene la relacion con las reservas realizadas dentro de la plataforma.
 */
@Entity
@Table(name = "cliente")
@AttributeOverrides({
        @AttributeOverride(name = "idUsuario", column = @Column(name = "id_cliente")),
        @AttributeOverride(name = "telefono", column = @Column(name = "telefono_contacto", nullable = false, length = 30))
})
public class Cliente extends Usuario {

    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Reserva> reservas = new ArrayList<>();

    protected Cliente() {
    }

    public Cliente(String telefono) {
        super(telefono);
    }

    public List<Reserva> getReservas() {
        return List.copyOf(reservas);
    }

    public void solicitarReserva() {
    }

    public void agregarReserva(Reserva reserva) {
        reserva.setCliente(this);
        reservas.add(reserva);
    }
}
