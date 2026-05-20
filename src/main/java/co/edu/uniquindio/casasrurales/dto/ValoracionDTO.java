package co.edu.uniquindio.casasrurales.dto;

import java.util.Date;

/**
 * DTO para transferir información de las valoraciones y comentarios.
 */
public class ValoracionDTO {

    private Long id;
    private String clienteEmail;
    private int calificacion;
    private String comentario;
    private Date fechaCreacion;

    public ValoracionDTO() {
    }

    public ValoracionDTO(Long id, String clienteEmail, int calificacion, String comentario, Date fechaCreacion) {
        this.id = id;
        this.clienteEmail = clienteEmail;
        this.calificacion = calificacion;
        this.comentario = comentario;
        this.fechaCreacion = fechaCreacion;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getClienteEmail() {
        return clienteEmail;
    }

    public void setClienteEmail(String clienteEmail) {
        this.clienteEmail = clienteEmail;
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
