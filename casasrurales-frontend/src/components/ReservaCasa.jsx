import { useState, useEffect } from 'react';
import { formatFechaCorta, formatFecha } from '../utils/formatFecha';

export default function ReservaCasa({ casa, onClose }) {
  const [paquetes, setPaquetes] = useState([]);
  const [cargando, setCargando] = useState(true);
  const [mensaje, setMensaje] = useState('');
  const [tipoMensaje, setTipoMensaje] = useState('info');
  const [resumen, setResumen] = useState(null);

  const [formulario, setFormulario] = useState({
    fechaEntrada: '',
    numeroNoches: 1,
    tipo: 'CASA_ENTERA',
    importeTotal: 0
  });

  const [guardando, setGuardando] = useState(false);

  useEffect(() => {
    const fetchPaquetes = async () => {
      try {
        const response = await fetch(`/api/busqueda/${casa.codigoCasa}/paquetes`);
        if (response.ok) {
          const data = await response.json();
          setPaquetes(data);
        }
      } catch (error) {
        console.error("Error al cargar paquetes", error);
      } finally {
        setCargando(false);
      }
    };
    fetchPaquetes();
  }, [casa]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormulario({ ...formulario, [name]: value });
  };

  const [paqueteSeleccionado, setPaqueteSeleccionado] = useState('');

  const seleccionarPaquete = (idPaquete) => {
    setPaqueteSeleccionado(idPaquete);
    if (!idPaquete) return;
    
    const pq = paquetes.find(p => p.idPaquete.toString() === idPaquete);
    if (pq) {
      // Calculate nights based on dates
      const start = new Date(pq.fechaInicio);
      const end = new Date(pq.fechaFin);
      const diffTime = Math.abs(end - start);
      const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
      
      setFormulario({
        ...formulario,
        fechaEntrada: pq.fechaInicio,
        numeroNoches: diffDays > 0 ? diffDays : 1,
        tipo: pq.modalidad === 'AMBAS' ? 'CASA_ENTERA' : pq.modalidad
      });
    }
  };

  const calcularPrecio = () => {
    if (paqueteSeleccionado) {
      const pq = paquetes.find(p => p.idPaquete.toString() === paqueteSeleccionado);
      if (pq) {
        return formulario.tipo === 'CASA_ENTERA' ? pq.precioCasaEntera : pq.precioHabitacion;
      }
    }
    return formulario.numeroNoches * (formulario.tipo === 'CASA_ENTERA' ? 150000 : 50000);
  };

  const confirmarReserva = async (e) => {
    e.preventDefault();
    setGuardando(true);
    setMensaje('');

    const payload = {
      codigoCasa: casa.codigoCasa,
      fechaEntrada: formulario.fechaEntrada,
      numeroNoches: Number(formulario.numeroNoches),
      importeTotal: calcularPrecio(),
      idsHabitaciones: [] // Vacio para casa entera o sin especificar
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
        setMensaje('Reserva realizada exitosamente. ¡Revisa los datos para el pago!');
        setTipoMensaje('exito');
      } else {
        setMensaje(data.error || 'No fue posible realizar la reserva.');
        setTipoMensaje('error');
      }
    } catch (error) {
      setMensaje('Error de conexión con el servidor');
      setTipoMensaje('error');
    } finally {
      setGuardando(false);
    }
  };

  if (resumen) {
    return (
      <div className="modal-overlay">
        <div className="modal-contenido" style={{ maxWidth: '500px' }}>
          <h3>Reserva Confirmada</h3>
          <div className="mensaje exito">{mensaje}</div>
          <div className="info-reserva" style={{ margin: '20px 0', padding: '15px', background: '#f5f5f5', borderRadius: '8px' }}>
            <p><strong>Número de Reserva:</strong> {resumen.numeroReserva}</p>
            <p><strong>Fecha Entrada:</strong> {formatFecha(resumen.fechaEntrada)}</p>
            <p><strong>Noches:</strong> {resumen.numeroNoches}</p>
            <p><strong>Importe Total:</strong> ${resumen.importeTotal}</p>
            <p><strong>Anticipo Requerido (20%):</strong> ${resumen.importeAnticipo}</p>
            <p><strong>Fecha Límite Pago:</strong> {formatFecha(resumen.fechaLimitePago)}</p>
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
      <div className="modal-contenido" style={{ maxWidth: '500px' }}>
        <h3>Reservar {casa.nombrePropiedad}</h3>
        {mensaje && <div className={`mensaje ${tipoMensaje}`}>{mensaje}</div>}
        
        <form className="formulario-casa" onSubmit={confirmarReserva}>
          {!cargando && paquetes.length > 0 && (
            <div className="campo-formulario">
              <label>Seleccionar Paquete (Opcional)</label>
              <select value={paqueteSeleccionado} onChange={(e) => seleccionarPaquete(e.target.value)}>
                <option value="">Reserva Personalizada</option>
                {paquetes.map(p => (
                  <option key={p.idPaquete} value={p.idPaquete}>
                    {formatFechaCorta(p.fechaInicio)} a {formatFechaCorta(p.fechaFin)} - {p.modalidad === 'CASA_ENTERA' ? `Casa: $${p.precioCasaEntera}` : `Hab: $${p.precioHabitacion}`}
                  </option>
                ))}
              </select>
            </div>
          )}

          <div className="campo-formulario">
            <label>Fecha de Entrada</label>
            <input type="date" name="fechaEntrada" value={formulario.fechaEntrada} onChange={handleChange} required />
          </div>
          <div className="campo-formulario">
            <label>Número de Noches</label>
            <input type="number" min="1" name="numeroNoches" value={formulario.numeroNoches} onChange={handleChange} required />
          </div>
          <div className="campo-formulario">
            <label>Modalidad Deseada</label>
            <select name="tipo" value={formulario.tipo} onChange={handleChange}>
              <option value="CASA_ENTERA">Casa Entera</option>
              <option value="POR_HABITACIONES">Por Habitaciones (Sujeto a disp.)</option>
            </select>
          </div>

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
            Se requerirá un pago de anticipo del 20% dentro de los 3 días siguientes a la confirmación para asegurar la reserva.
          </p>
        </form>
      </div>
    </div>
  );
}
