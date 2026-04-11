package co.edu.uniquindio.casasrurales.dto;

/**
 * DTO para listar casas rurales con paquetes activos en búsquedas.
 * Contiene información resumida de la casa para mostrar en lista.
 */
public class CasaRuralListadoDTO {

    private int codigoCasa;
    private String poblacion;
    private int numDormitorios;
    private int numBanos;
    private int numCocinas;
    private String descripcionGeneral;
    private String nombrePropietario;

    public CasaRuralListadoDTO(int codigoCasa, String poblacion, int numDormitorios,
                              int numBanos, int numCocinas, String descripcionGeneral,
                              String nombrePropietario) {
        this.codigoCasa = codigoCasa;
        this.poblacion = poblacion;
        this.numDormitorios = numDormitorios;
        this.numBanos = numBanos;
        this.numCocinas = numCocinas;
        this.descripcionGeneral = descripcionGeneral;
        this.nombrePropietario = nombrePropietario;
    }

    // Getters
    public int getCodigoCasa() {
        return codigoCasa;
    }

    public String getPoblacion() {
        return poblacion;
    }

    public int getNumDormitorios() {
        return numDormitorios;
    }

    public int getNumBanos() {
        return numBanos;
    }

    public int getNumCocinas() {
        return numCocinas;
    }

    public String getDescripcionGeneral() {
        return descripcionGeneral;
    }

    public String getNombrePropietario() {
        return nombrePropietario;
    }
}
