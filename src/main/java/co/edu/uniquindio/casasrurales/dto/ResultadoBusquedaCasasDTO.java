package co.edu.uniquindio.casasrurales.dto;

import java.util.List;

public class ResultadoBusquedaCasasDTO {

    private List<CasaRuralListadoDTO> contenido;
    private int pagina;
    private int tamano;
    private long totalElementos;
    private int totalPaginas;
    private boolean primera;
    private boolean ultima;

    public ResultadoBusquedaCasasDTO(List<CasaRuralListadoDTO> contenido, int pagina, int tamano,
                                     long totalElementos, int totalPaginas) {
        this.contenido = contenido;
        this.pagina = pagina;
        this.tamano = tamano;
        this.totalElementos = totalElementos;
        this.totalPaginas = totalPaginas;
        this.primera = pagina <= 0;
        this.ultima = totalPaginas == 0 || pagina >= totalPaginas - 1;
    }

    public List<CasaRuralListadoDTO> getContenido() {
        return contenido;
    }

    public int getPagina() {
        return pagina;
    }

    public int getTamano() {
        return tamano;
    }

    public long getTotalElementos() {
        return totalElementos;
    }

    public int getTotalPaginas() {
        return totalPaginas;
    }

    public boolean isPrimera() {
        return primera;
    }

    public boolean isUltima() {
        return ultima;
    }
}
