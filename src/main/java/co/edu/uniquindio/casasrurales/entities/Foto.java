package co.edu.uniquindio.casasrurales.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "foto")
public class Foto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_foto")
    private int idFoto;

    @Column(name = "ruta_foto", nullable = false, length = 255)
    private String ruta;

    @Column(length = 255)
    private String descripcion;

    @ManyToOne
    @JoinColumn(name = "codigo_casa", nullable = false)
    private CasaRural casaRural;

    protected Foto() {
    }

    public Foto(int idFoto, String ruta, String descripcion) {
        this.idFoto = idFoto;
        this.ruta = ruta;
        this.descripcion = descripcion;
    }

    public int getIdFoto() {
        return idFoto;
    }

    public String getRuta() {
        return ruta;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public CasaRural getCasaRural() {
        return casaRural;
    }

    public void setCasaRural(CasaRural casaRural) {
        this.casaRural = casaRural;
    }

    public String visualizar() {
        return ruta;
    }
}
