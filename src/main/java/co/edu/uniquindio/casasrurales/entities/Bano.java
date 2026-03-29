package co.edu.uniquindio.casasrurales.entities;

public class Bano {

    private int idBano;

    public Bano(int idBano) {
        this.idBano = idBano;
    }

    public int getIdBano() {
        return idBano;
    }

    public String mostrarDatos() {
        return "Bano{id=%d}".formatted(idBano);
    }
}
