package co.edu.uniquindio.casasrurales.entities;

public abstract class Usuario {

    private int idUsuario;
    private String telefono;

    public Usuario(int idUsuario, String telefono) {
        this.idUsuario = idUsuario;
        this.telefono = telefono;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public void actualizarTelefono(String telefono) {
        this.telefono = telefono;
    }
}
