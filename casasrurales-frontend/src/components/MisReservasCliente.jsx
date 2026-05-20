import { useState, useEffect } from 'react';
import { formatFecha } from '../utils/formatFecha';
import { mostrarNotificacion } from '../utils/notificaciones';

export default function MisReservasCliente() {
  const [reservas, setReservas] = useState([]);
  const [cargando, setCargando] = useState(true);
  const [mensaje, setMensaje] = useState('');
  const [tipoMensaje, setTipoMensaje] = useState('error');
  const [reservaPago, setReservaPago] = useState(null);
  const [procesandoPago, setProcesandoPago] = useState(false);
  const [pagoForm, setPagoForm] = useState({
    titular: '',
    numeroTarjeta: '',
    vencimiento: '',
    cvv: ''
  });
  const [facturaActual, setFacturaActual] = useState(null);
  const [verFactura, setVerFactura] = useState(false);

  useEffect(() => {
    const fetchMisReservas = async () => {
      try {
        const response = await fetch('/api/reservas/mis-reservas', {
          credentials: 'include'
        });

        if (!response.ok) {
          throw new Error('No se pudieron cargar las reservas');
        }

        const data = await response.json();
        setReservas(data);
      } catch (error) {
        setMensaje(error.message);
        mostrarNotificacion(error.message, 'error');
      } finally {
        setCargando(false);
      }
    };

    fetchMisReservas();
  }, []);

  const formatearMoneda = (valor) => `$${Number(valor || 0).toLocaleString()}`;

  const saldoPendiente = (reserva) => Number(reserva.saldoPendiente ?? reserva.importeTotal ?? 0);

  const montoPasarela = (reserva) => {
    if (reserva.estado === 'PENDIENTE_PAGO') {
      return Number(reserva.importeAConsignar ?? reserva.importeAnticipo ?? 0);
    }
    return saldoPendiente(reserva);
  };

  const abrirFactura = (reserva) => {
    setFacturaActual(reserva);
    setVerFactura(true);
  };

  const cerrarFactura = () => {
    setFacturaActual(null);
    setVerFactura(false);
  };

  const descargarFactura = (reserva) => {
    const r = reserva || facturaActual;
    if (!r) return;
    const html = `
      <html>
      <head>
        <meta charset="utf-8" />
        <title>Factura Reserva ${r.numeroReserva}</title>
        <style>
          body { font-family: Arial, sans-serif; padding: 24px; color: #17351f }
          h1 { font-size: 20px }
          .section { margin-bottom: 12px }
          .right { float: right }
          table { width: 100%; border-collapse: collapse }
          td, th { padding: 8px; border-bottom: 1px solid #eee }
        </style>
      </head>
      <body>
        <h1>Factura - Reserva #${r.numeroReserva}</h1>
        <div class="section">
          <strong>Casa:</strong> ${r.nombreCasa || r.nombrePropiedad || ''} <span class="right"><strong>Estado:</strong> ${r.estado}</span>
        </div>
        <div class="section">
          <div><strong>Fecha Entrada:</strong> ${formatFecha(r.fechaEntrada)}</div>
          <div><strong>Noches:</strong> ${r.numeroNoches}</div>
          <div><strong>Poblacion:</strong> ${r.poblacionCasa || r.poblacion || ''}</div>
        </div>
        <div class="section">
          <table>
            <tr><td>Importe Total</td><td style="text-align:right">$${Number(r.importeTotal || 0).toLocaleString()}</td></tr>
            <tr><td>Anticipo</td><td style="text-align:right">$${Number(r.importeAnticipo || 0).toLocaleString()}</td></tr>
            <tr><td>Importe Pagado</td><td style="text-align:right">$${Number(r.importePagado || 0).toLocaleString()}</td></tr>
            <tr><td>Saldo Pendiente</td><td style="text-align:right">$${Number(r.saldoPendiente || 0).toLocaleString()}</td></tr>
          </table>
        </div>
        <div class="section">Gracias por su reserva.</div>
        <script>
          window.onload = function() { window.print(); }
        </script>
      </body>
      </html>
    `;

    const w = window.open('', '_blank');
    if (!w) {
      alert('Permite ventanas emergentes para descargar la factura.');
      return;
    }
    w.document.write(html);
    w.document.close();
  };

  const puedePagar = (reserva) =>
    reserva.estado === 'PENDIENTE_PAGO' || (reserva.estado === 'CONFIRMADA' && saldoPendiente(reserva) > 0);

  const textoBotonPago = (reserva) =>
    reserva.estado === 'PENDIENTE_PAGO' ? 'Pagar ahora' : 'Pagar saldo';

  const abrirPasarela = (reserva) => {
    setReservaPago(reserva);
    setMensaje('');
    setTipoMensaje('error');
    setPagoForm({
      titular: '',
      numeroTarjeta: '',
      vencimiento: '',
      cvv: ''
    });
  };

  const cerrarPasarela = () => {
    if (!procesandoPago) {
      setReservaPago(null);
    }
  };

  const actualizarPagoForm = (event) => {
    const { name, value } = event.target;
    setPagoForm((actual) => ({ ...actual, [name]: value }));
  };

  const pagarReserva = async (event) => {
    event.preventDefault();
    if (!reservaPago) return;

    setProcesandoPago(true);
    setMensaje('');
    try {
      const monto = montoPasarela(reservaPago);
      const response = await fetch(`/api/reservas/${reservaPago.numeroReserva}/pagar`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'include',
        body: JSON.stringify({
          ...pagoForm,
          monto
        })
      });
      const data = await response.json().catch(() => ({}));

      if (!response.ok) {
        const detalleCampos = data.campos
          ? Object.values(data.campos).filter(Boolean).join(' ')
          : '';
        throw new Error(detalleCampos || data.error || 'No fue posible procesar el pago');
      }

      setReservas((actuales) =>
        actuales.map((reserva) =>
          reserva.numeroReserva === data.reserva.numeroReserva ? data.reserva : reserva
        )
      );
      // abrir factura después de pago
      setFacturaActual(data.reserva);
      setVerFactura(true);
      setReservaPago(null);
      setMensaje(data.mensaje || 'Pago aprobado y reserva confirmada');
      setTipoMensaje('exito');
    } catch (error) {
      setMensaje(error.message);
      setTipoMensaje('error');
      mostrarNotificacion(error.message, 'error');
    } finally {
      setProcesandoPago(false);
    }
  };

  if (cargando) {
    return <div style={{ padding: '20px', textAlign: 'center' }}>Cargando reservas...</div>;
  }

  return (
    <div style={{ maxWidth: '1000px', margin: '0 auto', padding: '20px' }}>
      <h2>Mis Reservas</h2>

      {mensaje && <div className={`mensaje ${tipoMensaje}`} style={{ marginBottom: '20px' }}>{mensaje}</div>}

      {reservas.length === 0 && !mensaje ? (
        <div style={{ textAlign: 'center', padding: '40px', background: '#f5f5f5', borderRadius: '8px' }}>
          <p>Aun no has realizado ninguna reserva.</p>
        </div>
      ) : (
        <div style={{ display: 'grid', gap: '20px' }}>
          {reservas.map((reserva) => (
            <div key={reserva.numeroReserva} style={{
              background: 'white',
              border: '1px solid #ddd',
              borderRadius: '8px',
              padding: '20px',
              boxShadow: '0 2px 4px rgba(0,0,0,0.05)'
            }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', borderBottom: '1px solid #eee', paddingBottom: '10px', marginBottom: '10px' }}>
                <h3 style={{ margin: 0 }}>Reserva #{reserva.numeroReserva}</h3>
                <span className={`badge ${reserva.estado === 'PENDIENTE_PAGO' ? 'badge-yellow' : reserva.estado === 'CONFIRMADA' ? 'badge-green' : 'badge-red'}`} style={{
                  padding: '5px 10px',
                  borderRadius: '15px',
                  fontSize: '0.85em',
                  fontWeight: 'bold',
                  background: reserva.estado === 'PENDIENTE_PAGO' ? '#fff3cd' : reserva.estado === 'CONFIRMADA' ? '#d4edda' : '#f8d7da',
                  color: reserva.estado === 'PENDIENTE_PAGO' ? '#856404' : reserva.estado === 'CONFIRMADA' ? '#155724' : '#721c24'
                }}>
                  {reserva.estado.replace('_', ' ')}
                </span>
              </div>

              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '15px' }}>
                <div>
                  <p style={{ margin: '5px 0', fontSize: '0.9em', color: '#666' }}>Fecha de Entrada</p>
                  <p style={{ margin: 0, fontWeight: 'bold' }}>{formatFecha(reserva.fechaEntrada)}</p>
                </div>
                <div>
                  <p style={{ margin: '5px 0', fontSize: '0.9em', color: '#666' }}>Noches</p>
                  <p style={{ margin: 0, fontWeight: 'bold' }}>{reserva.numeroNoches}</p>
                </div>
                <div>
                  <p style={{ margin: '5px 0', fontSize: '0.9em', color: '#666' }}>Tipo</p>
                  <p style={{ margin: 0, fontWeight: 'bold' }}>{reserva.tipoReserva.replace('_', ' ')}</p>
                </div>
                <div>
                  <p style={{ margin: '5px 0', fontSize: '0.9em', color: '#666' }}>Poblacion</p>
                  <p style={{ margin: 0, fontWeight: 'bold' }}>{reserva.poblacionCasa || reserva.poblacion}</p>
                </div>
              </div>

              <div style={{ marginTop: '15px', padding: '10px', background: '#f8f9fa', borderRadius: '4px' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '5px' }}>
                  <span>Importe Total:</span>
                  <strong>{formatearMoneda(reserva.importeTotal)}</strong>
                </div>
                {reserva.estado === 'PENDIENTE_PAGO' && (
                  <>
                    <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '5px' }}>
                      <span>Anticipo Requerido (20%):</span>
                      <strong style={{ color: '#d32f2f' }}>{formatearMoneda(reserva.importeAnticipo)}</strong>
                    </div>
                    <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                      <span>Pagar antes de:</span>
                      <strong style={{ color: '#d32f2f' }}>{formatFecha(reserva.fechaLimitePago)}</strong>
                    </div>
                  </>
                )}
                {reserva.estado === 'CONFIRMADA' && saldoPendiente(reserva) > 0 && (
                  <>
                    <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '5px' }}>
                      <span>Pagado:</span>
                      <strong>{formatearMoneda(reserva.importePagado)}</strong>
                    </div>
                    <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                      <span>Saldo pendiente antes de salida:</span>
                      <strong style={{ color: '#d32f2f' }}>{formatearMoneda(saldoPendiente(reserva))}</strong>
                    </div>
                  </>
                )}
              </div>

              {puedePagar(reserva) && (
                <div style={{ marginTop: '15px', display: 'flex', justifyContent: 'flex-end' }}>
                  <button
                    type="button"
                    className="btn-primary-action"
                    onClick={() => abrirPasarela(reserva)}
                  >
                    {textoBotonPago(reserva)}
                  </button>
                </div>
              )}
              <div style={{ marginTop: '10px', display: 'flex', justifyContent: 'flex-end', gap: '8px' }}>
                <button type="button" className="btn-secondary" onClick={() => abrirFactura(reserva)}>
                  Ver factura
                </button>
                <button type="button" className="btn-primary-action" onClick={() => descargarFactura(reserva)}>
                  Descargar PDF
                </button>
              </div>
            </div>
          ))}
        </div>
      )}

      {reservaPago && (
        <div className="modal-overlay" onClick={cerrarPasarela}>
          <div className="modal-contenido" style={{ maxWidth: '540px' }} onClick={(event) => event.stopPropagation()}>
            <h3>Pasarela de pagos</h3>
            <form className="formulario-casa" onSubmit={pagarReserva}>
              <div style={{ padding: '12px', background: '#f8f9fa', borderRadius: '8px' }}>
                <p style={{ margin: '0 0 5px', color: '#666' }}>Reserva</p>
                <strong>#{reservaPago.numeroReserva}</strong>
              </div>
              <div style={{ padding: '12px', background: '#f8f9fa', borderRadius: '8px' }}>
                <p style={{ margin: '0 0 5px', color: '#666' }}>Valor a pagar ahora</p>
                <strong style={{ color: '#d32f2f', fontSize: '1.2em' }}>
                  {formatearMoneda(montoPasarela(reservaPago))}
                </strong>
              </div>
              <div style={{ padding: '12px', background: '#f8f9fa', borderRadius: '8px' }}>
                <p style={{ margin: '0 0 5px', color: '#666' }}>Cuenta del propietario</p>
                <strong>{reservaPago.cuentaCorrientePropietario || 'No disponible'}</strong>
              </div>
              <div style={{ padding: '12px', background: '#f8f9fa', borderRadius: '8px' }}>
                <p style={{ margin: '0 0 5px', color: '#666' }}>Concepto / referencia</p>
                <strong>{reservaPago.conceptoPago || reservaPago.numeroReserva}</strong>
              </div>
              <div style={{ padding: '12px', background: '#fff3cd', borderRadius: '8px', color: '#856404' }}>
                Pago seguro de prueba. Si pagas un anticipo, el saldo restante debe pagarse antes de la fecha de salida. El propietario recibe la notificacion en su vista de reservas.
              </div>

              <div className="campo-formulario">
                <label>Titular de la tarjeta</label>
                <input
                  name="titular"
                  value={pagoForm.titular}
                  onChange={actualizarPagoForm}
                  placeholder="Nombre como aparece en la tarjeta"
                  required
                />
              </div>
              <div className="campo-formulario">
                <label>Numero de tarjeta</label>
                <input
                  name="numeroTarjeta"
                  value={pagoForm.numeroTarjeta}
                  onChange={actualizarPagoForm}
                  inputMode="numeric"
                  placeholder="4111 1111 1111 1111"
                  required
                />
              </div>
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px' }}>
                <div className="campo-formulario">
                  <label>Vencimiento</label>
                  <input
                    name="vencimiento"
                    value={pagoForm.vencimiento}
                    onChange={actualizarPagoForm}
                    placeholder="MM/AA"
                    required
                  />
                </div>
                <div className="campo-formulario">
                  <label>CVV</label>
                  <input
                    name="cvv"
                    value={pagoForm.cvv}
                    onChange={actualizarPagoForm}
                    inputMode="numeric"
                    placeholder="123"
                    required
                  />
                </div>
              </div>

              <div className="modal-botones">
                <button type="button" className="btn-cancelar" onClick={cerrarPasarela} disabled={procesandoPago}>
                  Cerrar
                </button>
                <button type="submit" className="btn-success" disabled={procesandoPago}>
                  {procesandoPago ? 'Procesando...' : textoBotonPago(reservaPago)}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
      {verFactura && facturaActual && (
        <div className="modal-overlay" onClick={cerrarFactura}>
          <div className="modal-contenido" style={{ maxWidth: '720px' }} onClick={(e) => e.stopPropagation()}>
            <h3>Factura - Reserva #{facturaActual.numeroReserva}</h3>
            <div style={{ padding: 12 }}>
              <div style={{ marginBottom: 8 }}><strong>Casa:</strong> {facturaActual.nombreCasa || facturaActual.nombrePropiedad || ''} <span style={{ float: 'right' }}><strong>Estado:</strong> {facturaActual.estado}</span></div>
              <div style={{ marginBottom: 8 }}><strong>Fecha Entrada:</strong> {formatFecha(facturaActual.fechaEntrada)}</div>
              <div style={{ marginBottom: 8 }}><strong>Noches:</strong> {facturaActual.numeroNoches}</div>
              <div style={{ marginBottom: 8 }}>
                <table style={{ width: '100%', borderCollapse: 'collapse' }}>
                  <tbody>
                    <tr><td style={{ padding: 8 }}>Importe Total</td><td style={{ padding: 8, textAlign: 'right' }}>${Number(facturaActual.importeTotal || 0).toLocaleString()}</td></tr>
                    <tr><td style={{ padding: 8 }}>Anticipo</td><td style={{ padding: 8, textAlign: 'right' }}>${Number(facturaActual.importeAnticipo || 0).toLocaleString()}</td></tr>
                    <tr><td style={{ padding: 8 }}>Importe Pagado</td><td style={{ padding: 8, textAlign: 'right' }}>${Number(facturaActual.importePagado || 0).toLocaleString()}</td></tr>
                    <tr><td style={{ padding: 8 }}>Saldo Pendiente</td><td style={{ padding: 8, textAlign: 'right' }}>${Number(facturaActual.saldoPendiente || 0).toLocaleString()}</td></tr>
                  </tbody>
                </table>
              </div>
            </div>
            <div className="modal-botones">
              <button type="button" className="btn-cancelar" onClick={cerrarFactura}>Cerrar</button>
              <button type="button" className="btn-primary-action" onClick={() => descargarFactura(facturaActual)}>Descargar PDF</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
