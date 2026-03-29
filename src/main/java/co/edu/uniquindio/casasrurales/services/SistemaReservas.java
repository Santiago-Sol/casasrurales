package co.edu.uniquindio.casasrurales.services;

import co.edu.uniquindio.casasrurales.entities.CasaRural;
import co.edu.uniquindio.casasrurales.entities.Habitacion;
import co.edu.uniquindio.casasrurales.entities.Propietario;
import co.edu.uniquindio.casasrurales.entities.Reserva;
import co.edu.uniquindio.casasrurales.enums.EstadoReserva;
import co.edu.uniquindio.casasrurales.enums.TipoReserva;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class SistemaReservas {

    private final List<Propietario> propietarios = new ArrayList<>();
    private final List<CasaRural> casas = new ArrayList<>();
    private final List<Reserva> reservas = new ArrayList<>();

    public List<Propietario> getPropietarios() {
        return List.copyOf(propietarios);
    }

    public List<CasaRural> getCasas() {
        return List.copyOf(casas);
    }

    public List<Reserva> getReservas() {
        return List.copyOf(reservas);
    }

    public void agregarPropietario(Propietario propietario) {
        propietarios.add(propietario);
        casas.addAll(propietario.getCasas());
    }

    public List<CasaRural> buscarCasasPorPoblacion(String poblacion) {
        return casas.stream()
                .filter(casa -> casa.getPoblacion().equalsIgnoreCase(poblacion))
                .toList();
    }

    public CasaRural buscarCasaPorCodigo(int codigoCasa) {
        return casas.stream()
                .filter(casa -> casa.getCodigoCasa() == codigoCasa)
                .findFirst()
                .orElse(null);
    }

    public String consultarDisponibilidad(int codigoCasa, Date fechaEntrada, int numeroNoches) {
        CasaRural casa = buscarCasaPorCodigo(codigoCasa);
        if (casa == null) {
            return "CASA_NO_ENCONTRADA";
        }
        return casa.consultarDisponibilidad(fechaEntrada, numeroNoches);
    }

    public Reserva realizarReserva(int codigoCasa, Date fechaEntrada, int numeroNoches, List<Habitacion> habitaciones) {
        CasaRural casa = Objects.requireNonNull(buscarCasaPorCodigo(codigoCasa), "La casa no existe");
        String disponibilidad = casa.consultarDisponibilidad(fechaEntrada, numeroNoches);
        if (!"LIBRE".equals(disponibilidad)) {
            throw new IllegalStateException("La casa no esta disponible");
        }

        TipoReserva tipoReserva = (habitaciones == null || habitaciones.isEmpty())
                ? TipoReserva.CASA_ENTERA
                : TipoReserva.POR_HABITACIONES;

        Reserva reserva = new Reserva(
                generarNumeroReserva(),
                new Date(),
                fechaEntrada,
                numeroNoches,
                tipoReserva,
                0,
                EstadoReserva.PENDIENTE_PAGO,
                null,
                casa,
                habitaciones
        );

        reservas.add(reserva);
        casa.agregarReserva(reserva);
        if (habitaciones != null) {
            habitaciones.forEach(habitacion -> habitacion.agregarReserva(reserva));
        }
        return reserva;
    }

    public int generarNumeroReserva() {
        return reservas.size() + 1;
    }

    public String mostrarResultadoConsulta() {
        return "Casas registradas: %d, reservas activas: %d".formatted(casas.size(), reservas.size());
    }
}
