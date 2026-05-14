import { useState, useEffect } from 'react';
import { formatFechaCorta, formatFecha } from '../utils/formatFecha';

export default function ReservaCasa({ casa, onClose }) {
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
      setMensaje('Ingresa fecha de entrada y numero de noches para consultar disponibilidad.');
      setTipoMensaje('info');
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
      setMensaje(data.error || 'No fue posible consultar disponibilidad.');
      setTipoMensaje('error');
      return null;
    } catch (error) {
      setMensaje('Error de conexion con el servidor');
      setTipoMensaje('error');
      return null;
    } finally {
      setConsultando(false);
    }
  };

  const confirmarReserva = async (e) => {
    e.preventDefault();
    setGuardando(true);
    setMensaje('');

    if (formulario.tipo === 'POR_HABITACIONES' && habitacionesSeleccionadas.length === 0) {
      setMensaje('Selecciona al menos una habitacion para reservar por habitaciones.');
      setTipoMensaje('error');
      setGuardando(false);
      return;
    }

    const disponibilidadActual = disponibilidad || await consultarDisponibilidad();
    if (!disponibilidadActual) {
      setGuardando(false);
      return;
    }

    const payload = {
      codigoCasa: casa.codigoCasa,
      fechaEntrada: formulario.fechaEntrada,
      numeroNoches: Number(formulario.numeroNoches),
      importeTotal: calcularPrecio(),
      telefonoContacto: formulario.telefonoContacto,
      idsHabitaciones: formulario.tipo === 'POR_HABITACIONES' ? habitacionesSeleccionadas : []
    };

    try {
      const response = await fetch('/api/reservas', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'same-origin',
        body: JSON.stringify(payload)
      });
      const data = await response.json().catch(() => ({}));

      if (response.ok) {
        setResumen(data);
        setMensaje('Reserva realizada exitosamente. Revisa los datos para el pago.');
        setTipoMensaje('exito');
      } else {
        if (data.disponibilidad) {
          setDisponibilidad(data.disponibilidad);
        }
        setMensaje(data.error || 'No fue posible realizar la reserva.');
        setTipoMensaje('error');
      }
    } catch (error) {
      setMensaje('Error de conexion con el servidor');
      setTipoMensaje('error');
    } finally {
      setGuardando(false);
    }
  };

  const renderDisponibilidad = () => {
    if (!disponibilidad) return null;
    return (
      <div className="info-reserva" style={{ margin: '16px 0', padding: '12px', background: '#f5f5f5', borderRadius: '8px' }}>
        <strong>Disponibilidad</strong>
        {disponibilidad.dias.map((dia) => (
          <div key={dia.fecha} style={{ marginTop: '10px' }}>
            <p style={{ margin: '0 0 4px' }}>
              {formatFecha(dia.fecha)} - Casa entera: <strong>{dia.estadoCasaEntera}</strong>
            </p>
            {dia.habitaciones.length > 0 && (
              <p style={{ margin: 0, fontSize: '0.9em' }}>
                Habitaciones: {dia.habitaciones.map((h) => `${h.codigoHabitacion}: ${h.estado}`).join(' | ')}
              </p>
            )}
          </div>
        ))}
      </div>
    );
  };

  if (resumen) {
    return (
      <div className="modal-overlay">
        <div className="modal-contenido" style={{ maxWidth: '560px' }}>
          <h3>Reserva Confirmada</h3>
          <div className="mensaje exito">{mensaje}</div>
          <div className="info-reserva" style={{ margin: '20px 0', padding: '15px', background: '#f5f5f5', borderRadius: '8px' }}>
            <p><strong>Numero de Reserva:</strong> {resumen.numeroReserva}</p>
            <p><strong>Fecha Entrada:</strong> {formatFecha(resumen.fechaEntrada)}</p>
            <p><strong>Noches:</strong> {resumen.numeroNoches}</p>
            <p><strong>Importe Total:</strong> ${resumen.importeTotal.toLocaleString()}</p>
            <p><strong>Anticipo Requerido (20%):</strong> ${resumen.importeAnticipo.toLocaleString()}</p>
            <p><strong>Cuenta del propietario:</strong> {resumen.cuentaCorrientePropietario || 'No disponible'}</p>
            <p><strong>Fecha Limite Pago:</strong> {formatFecha(resumen.fechaLimitePago)}</p>
            <p><strong>Estado:</strong> <span className="badge badge-yellow">{resumen.estado}</span></p>
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
      <div className="modal-contenido" style={{ maxWidth: '620px' }}>
        <h3>Reservar {casa.nombrePropiedad}</h3>
        {mensaje && <div className={`mensaje ${tipoMensaje}`}>{mensaje}</div>}

        <form className="formulario-casa" onSubmit={confirmarReserva}>
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

          {formulario.tipo === 'POR_HABITACIONES' && habitaciones.length > 0 && (
            <div className="campo-formulario">
              <label>Habitaciones</label>
              {habitaciones.map((habitacion) => (
                <label key={habitacion.idHabitacion || habitacion.codigoHabitacion} style={{ display: 'block', marginTop: '6px' }}>
                  <input
                    type="checkbox"
                    checked={habitacionesSeleccionadas.includes(habitacion.idHabitacion)}
                    onChange={() => toggleHabitacion(habitacion.idHabitacion)}
                  />
                  {' '}{habitacion.codigoHabitacion} - {habitacion.numeroCamas} cama(s) {habitacion.tipoCama}
                </label>
              ))}
            </div>
          )}

          <button type="button" className="btn-primary" onClick={consultarDisponibilidad} disabled={consultando}>
            {consultando ? 'Consultando...' : 'Consultar Disponibilidad'}
          </button>

          {renderDisponibilidad()}

          <div style={{ margin: '20px 0', fontSize: '1.2em', fontWeight: 'bold', textAlign: 'right' }}>
            Importe Estimado: ${calcularPrecio().toLocaleString()}
          </div>

          <div className="modal-botones">
            <button type="button" className="btn-cancelar" onClick={onClose}>Cancelar</button>
            <button type="submit" className="btn-primary-action" disabled={guardando}>
              {guardando ? 'Procesando...' : 'Confirmar Reserva'}
            </button>
          </div>
          <p className="advertencia" style={{ marginTop: '10px', fontSize: '0.85em' }}>
            Se requerira un pago de anticipo del 20% dentro de los 3 dias siguientes a la confirmacion para asegurar la reserva.
          </p>
        </form>
      </div>
    </div>
  );
}
