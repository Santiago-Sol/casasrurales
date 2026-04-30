package co.edu.uniquindio.casasrurales.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO para registrar y editar casas rurales desde el dashboard del propietario.
 */
public class CasaRuralFormDTO {

    @NotNull(message = "El codigo de la casa es obligatorio")
    @Min(value = 1, message = "El codigo de la casa debe ser mayor a cero")
    private Integer codigoCasa;

    @NotBlank(message = "El nombre de la propiedad es obligatorio")
    @Size(max = 150, message = "El nombre de la propiedad no puede superar 150 caracteres")
    private String nombrePropiedad;

    @NotBlank(message = "La poblacion es obligatoria")
    @Size(max = 120, message = "La poblacion no puede superar 120 caracteres")
    private String poblacion;

    @Size(max = 4000, message = "La descripcion no puede superar 4000 caracteres")
    private String descripcion;

    @NotNull(message = "El numero de comedores es obligatorio")
    @Min(value = 0, message = "El numero de comedores no puede ser negativo")
    private Integer numComedores;

    @NotNull(message = "El numero de plazas de garaje es obligatorio")
    @Min(value = 0, message = "El numero de plazas de garaje no puede ser negativo")
    private Integer numPlazasGaraje;

    @Min(value = 3, message = "La casa debe tener minimo 3 habitaciones")
    private Integer numHabitaciones;

    @Min(value = 1, message = "La casa debe tener minimo 1 bano")
    private Integer numBanos;

    @Min(value = 1, message = "La casa debe tener minimo 1 cocina")
    private Integer numCocinas;

    public Integer getCodigoCasa() {
        return codigoCasa;
    }

    public void setCodigoCasa(Integer codigoCasa) {
        this.codigoCasa = codigoCasa;
    }

    public String getNombrePropiedad() {
        return nombrePropiedad;
    }

    public void setNombrePropiedad(String nombrePropiedad) {
        this.nombrePropiedad = nombrePropiedad;
    }

    public String getPoblacion() {
        return poblacion;
    }

    public void setPoblacion(String poblacion) {
        this.poblacion = poblacion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Integer getNumComedores() {
        return numComedores;
    }

    public void setNumComedores(Integer numComedores) {
        this.numComedores = numComedores;
    }

    public Integer getNumPlazasGaraje() {
        return numPlazasGaraje;
    }

    public void setNumPlazasGaraje(Integer numPlazasGaraje) {
        this.numPlazasGaraje = numPlazasGaraje;
    }

    public Integer getNumHabitaciones() {
        return numHabitaciones;
    }

    public void setNumHabitaciones(Integer numHabitaciones) {
        this.numHabitaciones = numHabitaciones;
    }

    public Integer getNumBanos() {
        return numBanos;
    }

    public void setNumBanos(Integer numBanos) {
        this.numBanos = numBanos;
    }

    public Integer getNumCocinas() {
        return numCocinas;
    }

    public void setNumCocinas(Integer numCocinas) {
        this.numCocinas = numCocinas;
    }
}
