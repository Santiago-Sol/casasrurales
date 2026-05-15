import { useEffect, useState } from 'react';
import { formatFecha } from '../utils/formatFecha';

export default function GestionPagos({ onClose }) {
  const [reservas, setReservas] = useState([]);
  const [vencidas, setVencidas] = useState([]);
  const [cargando, setCargando] = useState(true);
  const [mensaje, setMensaje] = useState('');
  const [tipoMensaje, setTipoMensaje] = useState('info');
  const [montos, setMontos] = useState({});
  const [procesando, setProcesando] = useState(false);

  useEffect(() => {
    cargarDatos();
  }, []);

  const mostrarMensaje = (texto, tipo = 'info') => {
    setMensaje(texto);
    setTipoMensaje(tipo);
  };

  const cargarDatos = async () => {
    setCargando(true);
    try {
      const [reservasResponse, vencidasResponse] = await Promise.all([
        fetch('/api/propietario/reservas', { credentials: 'same-origin' }),
        fetch('/api/propietario/reservas/vencidas', { credentials: 'same-origin' })
      ]);

      if (reservasResponse.ok) {
        setReservas(await reservasResponse.json());
      }
      if (vencidasResponse.ok) {
        setVencidas(await vencidasResponse.json());
      }
    } catch (error) {
      mostrarMensaje('Error de conexion al cargar reservas', 'error');
    } finally {
      setCargando(false);
    }
  };

  const actualizarMonto = (numeroReserva, valor) => {
    setMontos((actual) => ({ ...actual, [numeroReserva]: valor }));
  };

  const registrarPago = async (numeroReserva, anticipo) => {
    const monto = Number(montos[numeroReserva] || anticipo || 0);
    if (monto <= 0) {
      mostrarMensaje('Ingresa un monto valido para registrar el pago', 'error');
      return;
    }

    setProcesando(true);
    try {
      const response = await fetch(`/api/propietario/reservas/${numeroReserva}/pago`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'same-origin',
        body: JSON.stringify({ monto })
      });
      const data = await response.json().catch(() => ({}));

      if (response.ok) {
        mostrarMensaje(data.mensaje || 'Pago registrado exitosamente', 'exito');
        await cargarDatos();
        return;
      }
      mostrarMensaje(data.error || 'No fue posible registrar el pago', 'error');
    } catch (error) {
      mostrarMensaje('Error de conexion al registrar pago', 'error');
    } finally {
      setProcesando(false);
    }
  };

  const gestionarVencida = async (numeroReserva, accion) => {
    if (accion === 'anular' && !window.confirm(`Seguro que deseas anular la reserva ${numeroReserva}?`)) {
      return;
    }

    setProcesando(true);
    try {
      const response = await fetch(`/api/propietario/reservas/${numeroReserva}/${accion}`, {
        method: 'POST',
        credentials: 'same-origin'
      });
      const data = await response.json().catch(() => ({}));

      if (response.ok) {
        mostrarMensaje(data.mensaje || 'Reserva gestionada', 'exito');
        await cargarDatos();
        return;
      }
      mostrarMensaje(data.error || 'No fue posible gestionar la reserva', 'error');
    } catch (error) {
      mostrarMensaje('Error de conexion al gestionar reserva', 'error');
    } finally {
      setProcesando(false);
    }
  };

  const reservasPendientes = reservas.filter((reserva) => reserva.estado === 'PENDIENTE_PAGO');

  return (
    <div className="modal-overlay">
      <div className="modal-contenido" style={{ maxWidth: '980px' }}>
        <h3>Pagos y reservas vencidas</h3>
        {mensaje && <div className={`mensaje ${tipoMensaje}`}>{mensaje}</div>}
        {!cargando && vencidas.length > 0 && (
          <div className="mensaje warning">
            Tienes {vencidas.length} reserva(s) vencida(s) pendiente(s) de gestion.
          </div>
        )}

        {cargando ? (
          <p>Cargando reservas...</p>
        ) : (
          <>
            <section style={{ marginBottom: '24px' }}>
              <h4>Registrar pagos recibidos</h4>
              {reservasPendientes.length === 0 ? (
                <p>No hay reservas pendientes de pago.</p>
              ) : (
                <div className="tabla-wrapper">
                  <table className="tabla-casas">
                    <thead>
                      <tr>
                        <th>Reserva</th>
                        <th>Casa</th>
                        <th>Entrada</th>
                        <th>Anticipo</th>
                        <th>Limite</th>
                        <th>Monto</th>
                        <th>Accion</th>
                      </tr>
                    </thead>
                    <tbody>
                      {reservasPendientes.map((reserva) => (
                        <tr key={reserva.numeroReserva}>
                          <td>{reserva.numeroReserva}</td>
                          <td>{reserva.nombreCasa}</td>
                          <td>{formatFecha(reserva.fechaEntrada)}</td>
                          <td>${reserva.importeAnticipo.toLocaleString()}</td>
                          <td>{formatFecha(reserva.fechaLimitePago)}</td>
                          <td>
                            <input
                              type="number"
                              min="1"
                              value={montos[reserva.numeroReserva] ?? reserva.importeAnticipo}
                              onChange={(event) => actualizarMonto(reserva.numeroReserva, event.target.value)}
                              style={{ width: '120px' }}
                            />
                          </td>
                          <td>
                            <button
                              className="btn-success"
                              disabled={procesando}
                              onClick={() => registrarPago(reserva.numeroReserva, reserva.importeAnticipo)}
                            >
                              Registrar pago
                            </button>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              )}
            </section>

            <section>
              <h4>Reservas vencidas</h4>
              {vencidas.length === 0 ? (
                <p>No hay reservas vencidas sin pago.</p>
              ) : (
                <div className="tabla-wrapper">
                  <table className="tabla-casas">
                    <thead>
                      <tr>
                        <th>Reserva</th>
                        <th>Casa</th>
                        <th>Entrada</th>
                        <th>Limite pago</th>
                        <th>Estado</th>
                        <th>Acciones</th>
                      </tr>
                    </thead>
                    <tbody>
                      {vencidas.map((reserva) => (
                        <tr key={reserva.numeroReserva}>
                          <td>{reserva.numeroReserva}</td>
                          <td>{reserva.nombreCasa}</td>
                          <td>{formatFecha(reserva.fechaEntrada)}</td>
                          <td>{formatFecha(reserva.fechaLimitePago)}</td>
                          <td>{reserva.estado}</td>
                          <td className="acciones">
                            <button
                              className="btn-danger"
                              disabled={procesando}
                              onClick={() => gestionarVencida(reserva.numeroReserva, 'anular')}
                            >
                              Anular
                            </button>
                            <button
                              className="btn-warning"
                              disabled={procesando}
                              onClick={() => gestionarVencida(reserva.numeroReserva, 'mantener')}
                            >
                              Mantener
                            </button>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              )}
            </section>
          </>
        )}

        <div className="modal-botones">
          <button className="btn-cancelar" onClick={onClose}>Cerrar</button>
        </div>
      </div>
    </div>
  );
}
