package co.edu.uniquindio.casasrurales.entities;

public class Cocina {

    private int idCocina;
    private boolean tieneLavavajillas;
    private boolean tieneLavadora;

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

    public String mostrarDatos() {
        return "Cocina{id=%d, lavavajillas=%s, lavadora=%s}".formatted(idCocina, tieneLavavajillas, tieneLavadora);
    }
}
