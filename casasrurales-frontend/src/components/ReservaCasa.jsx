import { useState, useEffect } from 'react';
import { formatFechaCorta, formatFecha } from '../utils/formatFecha';
import { mostrarNotificacion } from '../utils/notificaciones';

export default function ReservaCasa({ casa, onClose, onAuthExpired }) {
  const [paquetes, setPaquetes] = useState([]);
  const [cargando, setCargando] = useState(true);
  const [mensaje, setMensaje] = useState('');
  const [tipoMensaje, setTipoMensaje] = useState('info');
  const [resumen, setResumen] = useState(null);
  const [disponibilidad, setDisponibilidad] = useState(null);
  const [consultando, setConsultando] = useState(false);
  const [paqueteSeleccionado, setPaqueteSeleccionado] = useState('');
  const [habitacionesSeleccionadas, setHabitacionesSeleccionadas] = useState([]);

  const [formulario, setFormulario] = useState({
    fechaEntrada: '',
    numeroNoches: 1,
    tipo: 'CASA_ENTERA',
    telefonoContacto: ''
  });

  const [guardando, setGuardando] = useState(false);

  const toInputDate = (fecha) => String(fecha || '').slice(0, 10);
  const mostrarMensaje = (texto, tipo = 'error') => {
    setMensaje(texto);
    setTipoMensaje(tipo);
    if (tipo === 'error') {
      mostrarNotificacion(texto, tipo);
    }
  };

  useEffect(() => {
    const fetchPaquetes = async () => {
      try {
        const response = await fetch(`/api/busqueda/${casa.codigoCasa}/paquetes`);
        if (response.ok) {
          const data = await response.json();
          setPaquetes(data);
        }
      } catch (error) {
        console.error('Error al cargar paquetes', error);
      } finally {
        setCargando(false);
      }
    };
    fetchPaquetes();
  }, [casa]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormulario((actual) => ({ ...actual, [name]: value }));
    setDisponibilidad(null);
  };

  const seleccionarPaquete = (idPaquete) => {
    setPaqueteSeleccionado(idPaquete);
    setDisponibilidad(null);
    if (!idPaquete) return;

    const paquete = paquetes.find((p) => p.idPaquete.toString() === idPaquete);
    if (paquete) {
      const start = new Date(paquete.fechaInicio);
      const end = new Date(paquete.fechaFin);
      const diffTime = Math.abs(end - start);
      const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24)) + 1;

      setFormulario((actual) => ({
        ...actual,
        fechaEntrada: toInputDate(paquete.fechaInicio),
        numeroNoches: diffDays > 0 ? diffDays : 1,
        tipo: paquete.modalidad === 'POR_HABITACIONES' ? 'POR_HABITACIONES' : 'CASA_ENTERA'
      }));
    }
  };

  const habitaciones = casa.habitaciones || [];

  const toggleHabitacion = (idHabitacion) => {
    setHabitacionesSeleccionadas((actual) =>
      actual.includes(idHabitacion)
        ? actual.filter((id) => id !== idHabitacion)
        : [...actual, idHabitacion]
    );
  };

  const calcularPrecio = () => {
    const noches = Number(formulario.numeroNoches) || 1;
    const paquete = paquetes.find((p) => p.idPaquete.toString() === paqueteSeleccionado);
    if (paquete) {
      if (formulario.tipo === 'CASA_ENTERA') {
        return paquete.precioCasaEntera;
      }
      return paquete.precioHabitacion * Math.max(habitacionesSeleccionadas.length, 1) * noches;
    }
    return noches * (formulario.tipo === 'CASA_ENTERA' ? 150000 : 50000 * Math.max(habitacionesSeleccionadas.length, 1));
  };

  const consultarDisponibilidad = async () => {
    if (!formulario.fechaEntrada || Number(formulario.numeroNoches) < 1) {
      mostrarMensaje('Ingresa fecha de entrada y numero de noches para consultar disponibilidad.', 'info');
      return null;
    }

    setConsultando(true);
    setMensaje('');
    try {
      const params = new URLSearchParams({
        fechaEntrada: formulario.fechaEntrada,
        numeroNoches: String(formulario.numeroNoches)
      });
      const response = await fetch(`/api/busqueda/${casa.codigoCasa}/disponibilidad?${params.toString()}`);
      const data = await response.json().catch(() => ({}));
      if (response.ok) {
        setDisponibilidad(data);
        return data;
      }
      mostrarMensaje(data.error || 'No fue posible consultar disponibilidad.', 'error');
      return null;
    } catch (error) {
      mostrarMensaje('Error de conexion con el servidor', 'error');
      return null;
    } finally {
      setConsultando(false);
    }
  };

  const validarDisponibilidadSeleccionada = (disponibilidadActual) => {
    const dias = disponibilidadActual?.dias || [];
    if (dias.length === 0) {
      return 'Consulta la disponibilidad antes de confirmar la reserva.';
    }

    if (formulario.tipo === 'CASA_ENTERA') {
      const disponible = dias.every((dia) => dia.estadoCasaEntera === 'LIBRE');
      return disponible ? '' : 'La casa no esta disponible para las fechas seleccionadas.';
    }

    const habitacionesDisponibles = dias.every((dia) =>
      habitacionesSeleccionadas.every((idHabitacion) =>
        (dia.habitaciones || []).some((habitacion) =>
          habitacion.idHabitacion === idHabitacion && habitacion.estado === 'LIBRE'
        )
      )
    );

    return habitacionesDisponibles ? '' : 'Una o mas habitaciones no estan disponibles para las fechas seleccionadas.';
  };

  const confirmarReserva = async (e) => {
    e.preventDefault();
    setGuardando(true);
    setMensaje('');

    if (formulario.tipo === 'POR_HABITACIONES' && habitacionesSeleccionadas.length === 0) {
      mostrarMensaje('Selecciona al menos una habitacion para reservar por habitaciones.', 'error');
      setGuardando(false);
      return;
    }

    const disponibilidadActual = disponibilidad || await consultarDisponibilidad();
    if (!disponibilidadActual) {
      setGuardando(false);
      return;
    }

    const errorDisponibilidad = validarDisponibilidadSeleccionada(disponibilidadActual);
    if (errorDisponibilidad) {
      mostrarMensaje(errorDisponibilidad, 'error');
      setGuardando(false);
      return;
    }

    const payload = {
      codigoCasa: casa.codigoCasa,
      fechaEntrada: formulario.fechaEntrada,
      numeroNoches: Number(formulario.numeroNoches),
      telefonoContacto: formulario.telefonoContacto,
      idsHabitaciones: formulario.tipo === 'POR_HABITACIONES' ? habitacionesSeleccionadas : []
    };

    try {
      const response = await fetch('/api/reservas', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'include',
        body: JSON.stringify(payload)
      });
      const data = await response.clone().json().catch(async () => {
        const texto = await response.text().catch(() => '');
        return texto ? { error: texto } : {};
      });

      if (response.ok) {
        setResumen(data);
        mostrarMensaje('Reserva realizada exitosamente. Revisa los datos para el pago.', 'exito');
      } else {
        if (response.status === 401 || response.status === 403) {
          mostrarMensaje('Tu sesion expiro o no estas autenticado como cliente. Inicia sesion nuevamente para confirmar la reserva.', 'error');
          onAuthExpired?.();
          return;
        }

        if (data.disponibilidad) {
          setDisponibilidad(data.disponibilidad);
        }
        const detalleCampos = data.campos
          ? Object.values(data.campos).filter(Boolean).join(' ')
          : '';
        mostrarMensaje(detalleCampos || data.error || 'No fue posible realizar la reserva.', 'error');
      }
    } catch (error) {
      mostrarMensaje('Error de conexion con el servidor', 'error');
    } finally {
      setGuardando(false);
    }
  };

  const renderDisponibilidad = () => {
    if (!disponibilidad) return null;
    return (
      <div className="info-reserva disponibilidad-reserva">
        <strong>Disponibilidad</strong>
        <div className="disponibilidad-lista">
          {disponibilidad.dias.map((dia) => (
            <div key={dia.fecha} className="disponibilidad-dia">
              <p>
              {formatFecha(dia.fecha)} - Casa entera: <strong>{dia.estadoCasaEntera}</strong>
              </p>
              {dia.habitaciones.length > 0 && (
                <p className="disponibilidad-habitaciones">
                  Habitaciones: {dia.habitaciones.map((h) => `${h.codigoHabitacion}: ${h.estado}`).join(' | ')}
                </p>
              )}
            </div>
          ))}
        </div>
      </div>
    );
  };

  if (resumen) {
    return (
      <div className="modal-overlay">
        <div className="modal-contenido reserva-modal reserva-modal-confirmada">
          <h3>Reserva Confirmada</h3>
          <div className="reserva-modal-scroll">
            <div className="mensaje exito">{mensaje}</div>
            <div className="info-reserva reserva-confirmacion">
            <p><strong>Numero de Reserva:</strong> {resumen.numeroReserva}</p>
            <p><strong>Fecha Entrada:</strong> {formatFecha(resumen.fechaEntrada)}</p>
            <p><strong>Fecha Salida:</strong> {formatFecha(resumen.fechaSalida)}</p>
            <p><strong>Noches:</strong> {resumen.numeroNoches}</p>
            <p><strong>Importe Total:</strong> ${resumen.importeTotal.toLocaleString()}</p>
            <p><strong>Importe a Consignar:</strong> ${(resumen.importeAConsignar ?? resumen.importeAnticipo).toLocaleString()}</p>
            <p><strong>Anticipo Requerido (20%):</strong> ${resumen.importeAnticipo.toLocaleString()}</p>
            <p><strong>Cuenta del propietario:</strong> {resumen.cuentaCorrientePropietario || 'No disponible'}</p>
            <p><strong>Fecha Limite Pago:</strong> {formatFecha(resumen.fechaLimitePago)}</p>
            <p><strong>Estado:</strong> <span className="badge badge-yellow">{resumen.estado}</span></p>
            </div>
          </div>
          <div className="modal-botones">
            <button className="btn-primary-action" onClick={onClose}>Aceptar y Cerrar</button>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="modal-overlay">
      <div className="modal-contenido reserva-modal">
        <div className="reserva-modal-header">
          <div>
            <span>Reserva</span>
            <h3>{casa.nombrePropiedad}</h3>
          </div>
          <button type="button" className="reserva-cerrar" onClick={onClose} aria-label="Cerrar reserva">x</button>
        </div>

        <form className="formulario-casa reserva-formulario" onSubmit={confirmarReserva}>
          <div className="reserva-modal-scroll">
            {mensaje && <div className={`mensaje ${tipoMensaje}`}>{mensaje}</div>}
            {!cargando && paquetes.length > 0 && (
            <div className="campo-formulario">
              <label>Seleccionar Paquete</label>
              <select value={paqueteSeleccionado} onChange={(e) => seleccionarPaquete(e.target.value)}>
                <option value="">Reserva personalizada</option>
                {paquetes.map((p) => (
                  <option key={p.idPaquete} value={p.idPaquete}>
                    {formatFechaCorta(p.fechaInicio)} a {formatFechaCorta(p.fechaFin)} - {p.modalidad}
                  </option>
                ))}
              </select>
            </div>
            )}

            <div className="reserva-grid">
              <div className="campo-formulario">
                <label>Telefono de contacto</label>
                <input type="tel" name="telefonoContacto" value={formulario.telefonoContacto} onChange={handleChange} required />
              </div>
              <div className="campo-formulario">
                <label>Fecha de Entrada</label>
                <input type="date" name="fechaEntrada" value={formulario.fechaEntrada} onChange={handleChange} required />
              </div>
              <div className="campo-formulario">
                <label>Numero de Noches</label>
                <input type="number" min="1" name="numeroNoches" value={formulario.numeroNoches} onChange={handleChange} required />
              </div>
              <div className="campo-formulario">
                <label>Modalidad Deseada</label>
                <select name="tipo" value={formulario.tipo} onChange={handleChange}>
                  <option value="CASA_ENTERA">Casa Entera</option>
                  <option value="POR_HABITACIONES">Por Habitaciones</option>
                </select>
              </div>
            </div>

            {formulario.tipo === 'POR_HABITACIONES' && habitaciones.length > 0 && (
              <div className="campo-formulario habitaciones-reserva">
                <label>Habitaciones</label>
                <div className="habitaciones-reserva-lista">
                  {habitaciones.map((habitacion) => (
                    <label key={habitacion.idHabitacion || habitacion.codigoHabitacion} className="habitacion-reserva-opcion">
                      <span>
                        <strong>{habitacion.codigoHabitacion}</strong>
                        {habitacion.numeroCamas} cama(s) {habitacion.tipoCama}
                      </span>
                      <input
                        type="checkbox"
                        checked={habitacionesSeleccionadas.includes(habitacion.idHabitacion)}
                        onChange={() => toggleHabitacion(habitacion.idHabitacion)}
                      />
                    </label>
                  ))}
                </div>
              </div>
            )}

            <button type="button" className="btn-primary reserva-consultar" onClick={consultarDisponibilidad} disabled={consultando}>
              {consultando ? 'Consultando...' : 'Consultar Disponibilidad'}
            </button>

            {renderDisponibilidad()}

            <p className="advertencia reserva-advertencia">
              Se requerira un pago de anticipo del 20% dentro de los 3 dias siguientes a la confirmacion para asegurar la reserva.
            </p>
          </div>

          <div className="reserva-footer">
            <strong>Importe Estimado: ${calcularPrecio().toLocaleString()}</strong>
            <div className="modal-botones">
            <button type="button" className="btn-cancelar" onClick={onClose}>Cancelar</button>
            <button type="submit" className="btn-primary-action" disabled={guardando}>
              {guardando ? 'Procesando...' : 'Confirmar Reserva'}
            </button>
            </div>
          </div>
        </form>
      </div>
    </div>
  );
}
