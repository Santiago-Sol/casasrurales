package co.edu.uniquindio.casasrurales.dto;

import java.util.List;

/**
 * DTO para mostrar los detalles completos de una casa rural.
 * Incluye todas sus características, habitaciones, cocinas, baños y fotos.
 */
public class CasaRuralDetalleDTO {

    private int codigoCasa;
    private String poblacion;
    private String descripcionGeneral;
    private int numDormitorios;
    private int numBanos;
    private int numCocinas;
    private int numComedores;
    private int numPlazasGaraje;
    private String nombrePropietario;
    private String telefonoPropietario;
    
    private List<HabitacionDetalleDTO> habitaciones;
    private List<CocinaDetalleDTO> cocinas;
    private List<BanoDetalleDTO> banos;
    private List<String> urlsFotos;

    public CasaRuralDetalleDTO() {
    }

    public CasaRuralDetalleDTO(int codigoCasa, String poblacion, String descripcionGeneral,
                              int numDormitorios, int numBanos, int numCocinas,
                              int numComedores, int numPlazasGaraje,
                              String nombrePropietario, String telefonoPropietario) {
        this.codigoCasa = codigoCasa;
        this.poblacion = poblacion;
        this.descripcionGeneral = descripcionGeneral;
        this.numDormitorios = numDormitorios;
        this.numBanos = numBanos;
        this.numCocinas = numCocinas;
        this.numComedores = numComedores;
        this.numPlazasGaraje = numPlazasGaraje;
        this.nombrePropietario = nombrePropietario;
        this.telefonoPropietario = telefonoPropietario;
    }

    // Getters y Setters
    public int getCodigoCasa() {
        return codigoCasa;
    }

    public void setCodigoCasa(int codigoCasa) {
        this.codigoCasa = codigoCasa;
    }

    public String getPoblacion() {
        return poblacion;
    }

    public void setPoblacion(String poblacion) {
        this.poblacion = poblacion;
    }

    public String getDescripcionGeneral() {
        return descripcionGeneral;
    }

    public void setDescripcionGeneral(String descripcionGeneral) {
        this.descripcionGeneral = descripcionGeneral;
    }

    public int getNumDormitorios() {
        return numDormitorios;
    }

    public void setNumDormitorios(int numDormitorios) {
        this.numDormitorios = numDormitorios;
    }

    public int getNumBanos() {
        return numBanos;
    }

    public void setNumBanos(int numBanos) {
        this.numBanos = numBanos;
    }

    public int getNumCocinas() {
        return numCocinas;
    }

    public void setNumCocinas(int numCocinas) {
        this.numCocinas = numCocinas;
    }

    public int getNumComedores() {
        return numComedores;
    }

    public void setNumComedores(int numComedores) {
        this.numComedores = numComedores;
    }

    public int getNumPlazasGaraje() {
        return numPlazasGaraje;
    }

    public void setNumPlazasGaraje(int numPlazasGaraje) {
        this.numPlazasGaraje = numPlazasGaraje;
    }

    public String getNombrePropietario() {
        return nombrePropietario;
    }

    public void setNombrePropietario(String nombrePropietario) {
        this.nombrePropietario = nombrePropietario;
    }

    public String getTelefonoPropietario() {
        return telefonoPropietario;
    }

    public void setTelefonoPropietario(String telefonoPropietario) {
        this.telefonoPropietario = telefonoPropietario;
    }

    public List<HabitacionDetalleDTO> getHabitaciones() {
        return habitaciones;
    }

    public void setHabitaciones(List<HabitacionDetalleDTO> habitaciones) {
        this.habitaciones = habitaciones;
    }

    public List<CocinaDetalleDTO> getCocinas() {
        return cocinas;
    }

    public void setCocinas(List<CocinaDetalleDTO> cocinas) {
        this.cocinas = cocinas;
    }

    public List<BanoDetalleDTO> getBanos() {
        return banos;
    }

    public void setBanos(List<BanoDetalleDTO> banos) {
        this.banos = banos;
    }

    public List<String> getUrlsFotos() {
        return urlsFotos;
    }

    public void setUrlsFotos(List<String> urlsFotos) {
        this.urlsFotos = urlsFotos;
    }
}
