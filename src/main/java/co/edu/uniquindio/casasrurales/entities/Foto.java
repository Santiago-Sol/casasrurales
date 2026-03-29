package co.edu.uniquindio.casasrurales.entities;

public class Foto {

    private int idFoto;
    private String ruta;
    private String descripcion;

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

    public String visualizar() {
        return ruta;
    }
}
