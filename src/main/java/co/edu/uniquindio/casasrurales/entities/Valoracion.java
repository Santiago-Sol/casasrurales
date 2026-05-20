package co.edu.uniquindio.casasrurales.entities;

import jakarta.persistence.*;
import java.util.Date;

/**
 * Entidad que representa la valoración y comentario que realiza un cliente sobre una casa rural.
 */
@Entity
@Table(name = "valoracion")
public class Valoracion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_valoracion")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cliente", nullable = false)
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "codigo_casa", nullable = false)
    private CasaRural casaRural;

    @Column(name = "calificacion", nullable = false)
    private int calificacion; // Escala de 1 a 5

    @Column(name = "comentario", columnDefinition = "TEXT")
    private String comentario;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "fecha_creacion", nullable = false)
    private Date fechaCreacion;

    protected Valoracion() {
    }

    public Valoracion(Cliente cliente, CasaRural casaRural, int calificacion, String comentario) {
        this.cliente = cliente;
        this.casaRural = casaRural;
        this.calificacion = calificacion;
        this.comentario = comentario;
        this.fechaCreacion = new Date();
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public CasaRural getCasaRural() {
        return casaRural;
    }

    public void setCasaRural(CasaRural casaRural) {
        this.casaRural = casaRural;
    }

    public int getCalificacion() {
        return calificacion;
    }

    public void setCalificacion(int calificacion) {
        this.calificacion = calificacion;
    }

    public String getComentario() {
        return comentario;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }

    public Date getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(Date fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }
}
