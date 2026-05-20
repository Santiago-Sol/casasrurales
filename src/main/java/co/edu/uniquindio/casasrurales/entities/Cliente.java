package co.edu.uniquindio.casasrurales.entities;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
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

    @ManyToMany
    @JoinTable(
            name = "cliente_favoritos",
            joinColumns = @JoinColumn(name = "id_cliente"),
            inverseJoinColumns = @JoinColumn(name = "codigo_casa")
    )
    private List<CasaRural> favoritos = new ArrayList<>();

    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Valoracion> valoraciones = new ArrayList<>();

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

    public List<CasaRural> getFavoritos() {
        return favoritos;
    }

    public void setFavoritos(List<CasaRural> favoritos) {
        this.favoritos = favoritos;
    }

    public void agregarFavorito(CasaRural casa) {
        if (!favoritos.contains(casa)) {
            favoritos.add(casa);
        }
    }

    public void removerFavorito(CasaRural casa) {
        favoritos.remove(casa);
    }

    public List<Valoracion> getValoraciones() {
        return List.copyOf(valoraciones);
    }

    public void agregarValoracion(Valoracion valoracion) {
        valoracion.setCliente(this);
        valoraciones.add(valoracion);
    }
}
