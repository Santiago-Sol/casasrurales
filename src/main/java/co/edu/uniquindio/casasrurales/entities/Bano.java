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
@Table(name = "bano")
public class Bano {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_bano")
    private int idBano;

    @Column(length = 255)
    private String observaciones;

    @ManyToOne
    @JoinColumn(name = "codigo_casa", nullable = false)
    private CasaRural casaRural;

    protected Bano() {
    }

    public Bano(int idBano) {
        this.idBano = idBano;
    }

    public int getIdBano() {
        return idBano;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public CasaRural getCasaRural() {
        return casaRural;
    }

    public void setCasaRural(CasaRural casaRural) {
        this.casaRural = casaRural;
    }

    public String mostrarDatos() {
        return "Bano{id=%d}".formatted(idBano);
    }
}
