package co.edu.uniquindio.casasrurales.dto;

import java.util.Date;

public class ValoracionCasaDTO {

    private int idValoracion;
    private int codigoCasa;
    private int idCliente;
    private String cliente;
    private int estrellas;
    private String comentario;
    private Date fechaActualizacion;

    public ValoracionCasaDTO(int idValoracion, int codigoCasa, int idCliente, String cliente,
                             int estrellas, String comentario, Date fechaActualizacion) {
        this.idValoracion = idValoracion;
        this.codigoCasa = codigoCasa;
        this.idCliente = idCliente;
        this.cliente = cliente;
        this.estrellas = estrellas;
        this.comentario = comentario;
        this.fechaActualizacion = fechaActualizacion;
    }

    public int getIdValoracion() {
        return idValoracion;
    }

    public int getCodigoCasa() {
        return codigoCasa;
    }

    public int getIdCliente() {
        return idCliente;
    }

    public String getCliente() {
        return cliente;
    }

    public int getEstrellas() {
        return estrellas;
    }

    public String getComentario() {
        return comentario;
    }

    public Date getFechaActualizacion() {
        return fechaActualizacion;
    }
}
