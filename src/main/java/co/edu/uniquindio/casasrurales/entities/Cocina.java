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
@Table(name = "cocina")
public class Cocina {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cocina")
    private int idCocina;

    @Column(name = "tiene_lavavajillas")
    private boolean tieneLavavajillas;

    @Column(name = "tiene_lavadora")
    private boolean tieneLavadora;

    @ManyToOne
    @JoinColumn(name = "codigo_casa", nullable = false)
    private CasaRural casaRural;

    protected Cocina() {
    }

    public Cocina(int idCocina, boolean tieneLavavajillas, boolean tieneLavadora) {
        this.idCocina = idCocina;
        this.tieneLavavajillas = tieneLavavajillas;
        this.tieneLavadora = tieneLavadora;
    }

    public int getIdCocina() {
        return idCocina;
    }

    public boolean isTieneLavavajillas() {
        return tieneLavavajillas;
    }

    public boolean isTieneLavadora() {
        return tieneLavadora;
    }

    public CasaRural getCasaRural() {
        return casaRural;
    }

    public void setCasaRural(CasaRural casaRural) {
        this.casaRural = casaRural;
    }

    public String mostrarDatos() {
        return "Cocina{id=%d, lavavajillas=%s, lavadora=%s}".formatted(idCocina, tieneLavavajillas, tieneLavadora);
    }
}
