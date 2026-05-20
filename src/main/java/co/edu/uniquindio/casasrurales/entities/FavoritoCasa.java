package co.edu.uniquindio.casasrurales.entities;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
        name = "favorito_casa",
        uniqueConstraints = @UniqueConstraint(columnNames = {"id_cliente", "codigo_casa"})
)
public class FavoritoCasa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_favorito")
    private int idFavorito;

    @ManyToOne
    @JoinColumn(name = "id_cliente", nullable = false)
    private Cliente cliente;

    @ManyToOne
    @JoinColumn(name = "codigo_casa", nullable = false)
    private CasaRural casaRural;

    @Column(name = "fecha_creacion", nullable = false)
    private Date fechaCreacion = new Date();

    protected FavoritoCasa() {
    }

    public FavoritoCasa(Cliente cliente, CasaRural casaRural) {
        this.cliente = cliente;
        this.casaRural = casaRural;
        this.fechaCreacion = new Date();
    }

    public int getIdFavorito() {
        return idFavorito;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public CasaRural getCasaRural() {
        return casaRural;
    }

    public Date getFechaCreacion() {
        return fechaCreacion;
    }
}
