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
        name = "valoracion_casa",
        uniqueConstraints = @UniqueConstraint(columnNames = {"id_cliente", "codigo_casa"})
)
public class ValoracionCasa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_valoracion")
    private int idValoracion;

    @ManyToOne
    @JoinColumn(name = "id_cliente", nullable = false)
    private Cliente cliente;

    @ManyToOne
    @JoinColumn(name = "codigo_casa", nullable = false)
    private CasaRural casaRural;

    @Column(nullable = false)
    private int estrellas;

    @Column(columnDefinition = "TEXT")
    private String comentario;

    @Column(name = "fecha_creacion", nullable = false)
    private Date fechaCreacion = new Date();

    @Column(name = "fecha_actualizacion", nullable = false)
    private Date fechaActualizacion = new Date();

    protected ValoracionCasa() {
    }

    public ValoracionCasa(Cliente cliente, CasaRural casaRural, int estrellas, String comentario) {
        this.cliente = cliente;
        this.casaRural = casaRural;
        actualizar(estrellas, comentario);
        this.fechaCreacion = new Date();
    }

    public void actualizar(int estrellas, String comentario) {
        if (estrellas < 1 || estrellas > 5) {
            throw new IllegalArgumentException("La valoracion debe estar entre 1 y 5 estrellas");
        }
        this.estrellas = estrellas;
        this.comentario = comentario == null ? "" : comentario.trim();
        this.fechaActualizacion = new Date();
    }

    public int getIdValoracion() {
        return idValoracion;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public CasaRural getCasaRural() {
        return casaRural;
    }

    public int getEstrellas() {
        return estrellas;
    }

    public String getComentario() {
        return comentario;
    }

    public Date getFechaCreacion() {
        return fechaCreacion;
    }

    public Date getFechaActualizacion() {
        return fechaActualizacion;
    }
}
