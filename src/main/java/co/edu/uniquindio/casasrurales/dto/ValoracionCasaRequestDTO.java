package co.edu.uniquindio.casasrurales.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public class ValoracionCasaRequestDTO {

    @Min(value = 1, message = "La valoracion minima es 1 estrella")
    @Max(value = 5, message = "La valoracion maxima es 5 estrellas")
    private int estrellas;

    @Size(max = 600, message = "El comentario no puede superar 600 caracteres")
    private String comentario;

    public int getEstrellas() {
        return estrellas;
    }

    public void setEstrellas(int estrellas) {
        this.estrellas = estrellas;
    }

    public String getComentario() {
        return comentario;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }
}
