package co.edu.uniquindio.casasrurales.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO para crear o actualizar una casa rural desde el formulario del propietario.
 */
public class RegistroCasaForm {

    @NotNull
    private Integer codigoCasa;

    @NotBlank
    private String nombrePropiedad;

    @NotBlank
    private String poblacion;

    private String descripcionGeneral;

    @Min(0)
    private int numComedores;

    @Min(0)
    private int numPlazasGaraje;

    public RegistroCasaForm() {}

    public Integer getCodigoCasa() { return codigoCasa; }
    public void setCodigoCasa(Integer codigoCasa) { this.codigoCasa = codigoCasa; }

    public String getNombrePropiedad() { return nombrePropiedad; }
    public void setNombrePropiedad(String nombrePropiedad) { this.nombrePropiedad = nombrePropiedad; }

    public String getPoblacion() { return poblacion; }
    public void setPoblacion(String poblacion) { this.poblacion = poblacion; }

    public String getDescripcionGeneral() { return descripcionGeneral; }
    public void setDescripcionGeneral(String descripcionGeneral) { this.descripcionGeneral = descripcionGeneral; }

    public int getNumComedores() { return numComedores; }
    public void setNumComedores(int numComedores) { this.numComedores = numComedores; }

    public int getNumPlazasGaraje() { return numPlazasGaraje; }
    public void setNumPlazasGaraje(int numPlazasGaraje) { this.numPlazasGaraje = numPlazasGaraje; }
}
