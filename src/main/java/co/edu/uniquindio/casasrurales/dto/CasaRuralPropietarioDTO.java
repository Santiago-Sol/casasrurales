package co.edu.uniquindio.casasrurales.dto;

/**
 * DTO para detalles de una casa con información del propietario.
 * Usado en dashboards de propietarios.
 */
public class CasaRuralPropietarioDTO {
    
    private int codigoCasa;
    private String nombrePropiedad;
    private String poblacion;
    private String descripcion;
    private int habitaciones;
    private int banos;
    private int salas;
    private int cocinas;
    private int plazasGaraje;
    private long precioAproximado;
    private boolean activa;
    private int totalReservas;
    private int reservasActivas;

    public CasaRuralPropietarioDTO(int codigoCasa, String nombrePropiedad, String poblacion, 
                                   String descripcion, int habitaciones, int banos, int salas, 
                                   int cocinas, int plazasGaraje, long precioAproximado, 
                                   boolean activa, int totalReservas, int reservasActivas) {
        this.codigoCasa = codigoCasa;
        this.nombrePropiedad = nombrePropiedad;
        this.poblacion = poblacion;
        this.descripcion = descripcion;
        this.habitaciones = habitaciones;
        this.banos = banos;
        this.salas = salas;
        this.cocinas = cocinas;
        this.plazasGaraje = plazasGaraje;
        this.precioAproximado = precioAproximado;
        this.activa = activa;
        this.totalReservas = totalReservas;
        this.reservasActivas = reservasActivas;
    }

    // Getters
    public int getCodigoCasa() { return codigoCasa; }
    public String getNombrePropiedad() { return nombrePropiedad; }
    public String getPoblacion() { return poblacion; }
    public String getDescripcion() { return descripcion; }
    public int getHabitaciones() { return habitaciones; }
    public int getBanos() { return banos; }
    public int getSalas() { return salas; }
    public int getCocinas() { return cocinas; }
    public int getPlazasGaraje() { return plazasGaraje; }
    public long getPrecioAproximado() { return precioAproximado; }
    public boolean isActiva() { return activa; }
    public int getTotalReservas() { return totalReservas; }
    public int getReservasActivas() { return reservasActivas; }
}
