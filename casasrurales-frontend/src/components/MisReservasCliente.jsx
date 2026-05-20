import { useState, useEffect } from 'react';
import { formatFecha } from '../utils/formatFecha';
import { mostrarNotificacion } from '../utils/notificaciones';
import '../styles/dashboard.css';

export default function MisReservasCliente() {
  const [reservas, setReservas] = useState([]);
  const [cargando, setCargando] = useState(true);
  const [mensaje, setMensaje] = useState('');
  const [tipoMensaje, setTipoMensaje] = useState('error');
  const [reservaPago, setReservaPago] = useState(null);
  const [procesandoPago, setProcesandoPago] = useState(false);
  const [facturaActual, setFacturaActual] = useState(null);
  const [verFactura, setVerFactura] = useState(false);
  const [pagoForm, setPagoForm] = useState({
    titular: '',
    numeroTarjeta: '',
    vencimiento: '',
    cvv: ''
  });

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

  const puedePagar = (reserva) =>
    reserva.estado === 'PENDIENTE_PAGO' || (reserva.estado === 'CONFIRMADA' && saldoPendiente(reserva) > 0);

  const textoBotonPago = (reserva) =>
    reserva.estado === 'PENDIENTE_PAGO' ? 'Pagar ahora' : 'Pagar saldo';

  const construirFactura = (reserva) => {
    const importePagado = Number(reserva.importePagado || 0);
    const saldo = saldoPendiente(reserva);
    const total = Number(reserva.importeTotal || 0);
    const fechaEmision = new Date();

    return {
      numeroFactura: `FAC-${reserva.numeroReserva}`,
      fechaEmision,
      numeroReserva: reserva.numeroReserva,
      codigoCasa: reserva.codigoCasa,
      poblacionCasa: reserva.poblacionCasa || reserva.poblacion,
      fechaReserva: reserva.fechaReserva,
      fechaEntrada: reserva.fechaEntrada,
      fechaSalida: reserva.fechaSalida,
      numeroNoches: reserva.numeroNoches,
      tipoReserva: reserva.tipoReserva,
      estado: reserva.estado,
      importeTotal: total,
      importeAnticipo: Number(reserva.importeAnticipo || 0),
      importePagado,
      saldoPendiente: saldo,
      valorFacturado: total - saldo,
      cuentaCorrientePropietario: reserva.cuentaCorrientePropietario,
      conceptoPago: reserva.conceptoPago || reserva.numeroReserva
    };
  };

  const abrirFactura = (reserva) => {
    setFacturaActual(construirFactura(reserva));
    setVerFactura(true);
  };

  const cerrarFactura = () => {
    setVerFactura(false);
    setFacturaActual(null);
  };

  const normalizarTextoPdf = (valor) =>
    String(valor ?? '')
      .normalize('NFD')
      .replace(/[\u0300-\u036f]/g, '')
      .replace(/[^\x20-\x7E]/g, '');

  const escaparTextoPdf = (valor) =>
    normalizarTextoPdf(valor).replace(/\\/g, '\\\\').replace(/\(/g, '\\(').replace(/\)/g, '\\)');

  const crearPdfFactura = (factura) => {
    const lineas = [
      'Casas Rurales - Factura',
      `Factura: ${factura.numeroFactura}`,
      `Emitida: ${formatFecha(factura.fechaEmision)}`,
      '',
      `Reserva: #${factura.numeroReserva}`,
      `Codigo casa: ${factura.codigoCasa}`,
      `Poblacion: ${factura.poblacionCasa || 'No disponible'}`,
      `Estado: ${String(factura.estado || '').replaceAll('_', ' ')}`,
      `Tipo: ${String(factura.tipoReserva || '').replaceAll('_', ' ')}`,
      '',
      `Fecha reserva: ${formatFecha(factura.fechaReserva)}`,
      `Entrada: ${formatFecha(factura.fechaEntrada)}`,
      `Salida: ${formatFecha(factura.fechaSalida)}`,
      `Noches: ${factura.numeroNoches}`,
      '',
      `Importe total: ${formatearMoneda(factura.importeTotal)}`,
      `Anticipo: ${formatearMoneda(factura.importeAnticipo)}`,
      `Pagado: ${formatearMoneda(factura.importePagado)}`,
      `Saldo pendiente: ${formatearMoneda(factura.saldoPendiente)}`,
      '',
      `Cuenta propietario: ${factura.cuentaCorrientePropietario || 'No disponible'}`,
      `Concepto: ${factura.conceptoPago}`,
      '',
      'Documento generado automaticamente por Casas Rurales.'
    ];

    const contenido = [
      'BT',
      '/F1 18 Tf',
      '50 780 Td',
      `(${escaparTextoPdf(lineas[0])}) Tj`,
      '/F1 11 Tf',
      ...lineas.slice(1).map((linea) => `0 -20 Td (${escaparTextoPdf(linea)}) Tj`),
      'ET'
    ].join('\n');

    const objetos = [
      '1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n',
      '2 0 obj\n<< /Type /Pages /Kids [3 0 R] /Count 1 >>\nendobj\n',
      '3 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 612 792] /Resources << /Font << /F1 5 0 R >> >> /Contents 4 0 R >>\nendobj\n',
      `4 0 obj\n<< /Length ${contenido.length} >>\nstream\n${contenido}\nendstream\nendobj\n`,
      '5 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>\nendobj\n'
    ];

    let pdf = '%PDF-1.4\n';
    const offsets = [0];
    objetos.forEach((objeto) => {
      offsets.push(pdf.length);
      pdf += objeto;
    });

    const inicioXref = pdf.length;
    pdf += `xref\n0 ${objetos.length + 1}\n`;
    pdf += '0000000000 65535 f \n';
    offsets.slice(1).forEach((offset) => {
      pdf += `${String(offset).padStart(10, '0')} 00000 n \n`;
    });
    pdf += `trailer\n<< /Size ${objetos.length + 1} /Root 1 0 R >>\nstartxref\n${inicioXref}\n%%EOF`;

    return new Blob([pdf], { type: 'application/pdf' });
  };

  const descargarFactura = (reservaOFactura = facturaActual) => {
    if (!reservaOFactura) return;

    const factura = reservaOFactura.numeroFactura ? reservaOFactura : construirFactura(reservaOFactura);
    const blob = crearPdfFactura(factura);
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = `${factura.numeroFactura}.pdf`;
    document.body.appendChild(link);
    link.click();
    link.remove();
    URL.revokeObjectURL(url);
  };

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
      setReservaPago(null);
      setMensaje(data.mensaje || 'Pago aprobado y reserva confirmada');
      setTipoMensaje('exito');
      abrirFactura(data.reserva);
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
              <div style={{ display: 'flex', justifyContent: 'space-between', borderBottom: '1px solid #eee', paddingBottom: '10px', marginBottom: '10px', gap: '12px' }}>
                <h3 style={{ margin: 0 }}>Reserva #{reserva.numeroReserva}</h3>
                <span className={`badge ${reserva.estado === 'PENDIENTE_PAGO' ? 'badge-yellow' : reserva.estado === 'CONFIRMADA' ? 'badge-green' : 'badge-red'}`} style={{
                  padding: '5px 10px',
                  borderRadius: '15px',
                  fontSize: '0.85em',
                  fontWeight: 'bold',
                  background: reserva.estado === 'PENDIENTE_PAGO' ? '#fff3cd' : reserva.estado === 'CONFIRMADA' ? '#d4edda' : '#f8d7da',
                  color: reserva.estado === 'PENDIENTE_PAGO' ? '#856404' : reserva.estado === 'CONFIRMADA' ? '#155724' : '#721c24',
                  position: 'static'
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
                <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '5px', gap: '12px' }}>
                  <span>Importe Total:</span>
                  <strong>{formatearMoneda(reserva.importeTotal)}</strong>
                </div>
                {reserva.estado === 'PENDIENTE_PAGO' && (
                  <>
                    <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '5px', gap: '12px' }}>
                      <span>Anticipo Requerido (20%):</span>
                      <strong style={{ color: '#d32f2f' }}>{formatearMoneda(reserva.importeAnticipo)}</strong>
                    </div>
                    <div style={{ display: 'flex', justifyContent: 'space-between', gap: '12px' }}>
                      <span>Pagar antes de:</span>
                      <strong style={{ color: '#d32f2f' }}>{formatFecha(reserva.fechaLimitePago)}</strong>
                    </div>
                  </>
                )}
                {reserva.estado === 'CONFIRMADA' && saldoPendiente(reserva) > 0 && (
                  <>
                    <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '5px', gap: '12px' }}>
                      <span>Pagado:</span>
                      <strong>{formatearMoneda(reserva.importePagado)}</strong>
                    </div>
                    <div style={{ display: 'flex', justifyContent: 'space-between', gap: '12px' }}>
                      <span>Saldo pendiente antes de salida:</span>
                      <strong style={{ color: '#d32f2f' }}>{formatearMoneda(saldoPendiente(reserva))}</strong>
                    </div>
                  </>
                )}
              </div>

              <div style={{ marginTop: '15px', display: 'flex', justifyContent: 'flex-end', gap: '10px', flexWrap: 'wrap' }}>
                <button
                  type="button"
                  className="btn-warning"
                  onClick={() => abrirFactura(reserva)}
                >
                  Ver factura
                </button>
                <button
                  type="button"
                  className="btn-success"
                  onClick={() => descargarFactura(reserva)}
                >
                  Descargar PDF
                </button>
                {puedePagar(reserva) && (
                  <button
                    type="button"
                    className="btn-primary-action"
                    onClick={() => abrirPasarela(reserva)}
                  >
                    {textoBotonPago(reserva)}
                  </button>
                )}
              </div>
            </div>
          ))}
        </div>
      )}

      {verFactura && facturaActual && (
        <div className="modal-overlay" onClick={cerrarFactura}>
          <div className="modal-contenido factura-modal" style={{ maxWidth: '640px' }} onClick={(event) => event.stopPropagation()}>
            <div style={{ display: 'flex', justifyContent: 'space-between', gap: '12px', borderBottom: '1px solid #e5e7eb', paddingBottom: '12px', marginBottom: '16px' }}>
              <div>
                <p style={{ margin: '0 0 4px', color: '#666', fontWeight: 700 }}>Factura</p>
                <h3 style={{ margin: 0 }}>{facturaActual.numeroFactura}</h3>
              </div>
              <div style={{ textAlign: 'right' }}>
                <p style={{ margin: '0 0 4px', color: '#666', fontWeight: 700 }}>Emitida</p>
                <strong>{formatFecha(facturaActual.fechaEmision)}</strong>
              </div>
            </div>

            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(180px, 1fr))', gap: '12px' }}>
              <div style={{ padding: '12px', background: '#f8f9fa', borderRadius: '8px' }}>
                <p style={{ margin: '0 0 5px', color: '#666' }}>Reserva</p>
                <strong>#{facturaActual.numeroReserva}</strong>
              </div>
              <div style={{ padding: '12px', background: '#f8f9fa', borderRadius: '8px' }}>
                <p style={{ margin: '0 0 5px', color: '#666' }}>Casa</p>
                <strong>{facturaActual.codigoCasa} - {facturaActual.poblacionCasa || 'No disponible'}</strong>
              </div>
              <div style={{ padding: '12px', background: '#f8f9fa', borderRadius: '8px' }}>
                <p style={{ margin: '0 0 5px', color: '#666' }}>Entrada</p>
                <strong>{formatFecha(facturaActual.fechaEntrada)}</strong>
              </div>
              <div style={{ padding: '12px', background: '#f8f9fa', borderRadius: '8px' }}>
                <p style={{ margin: '0 0 5px', color: '#666' }}>Salida</p>
                <strong>{formatFecha(facturaActual.fechaSalida)}</strong>
              </div>
            </div>

            <div style={{ marginTop: '16px', border: '1px solid #e5e7eb', borderRadius: '8px', overflow: 'hidden' }}>
              {[
                ['Importe total', facturaActual.importeTotal],
                ['Anticipo', facturaActual.importeAnticipo],
                ['Pagado', facturaActual.importePagado],
                ['Saldo pendiente', facturaActual.saldoPendiente]
              ].map(([label, value]) => (
                <div key={label} style={{ display: 'flex', justifyContent: 'space-between', gap: '12px', padding: '12px 14px', borderBottom: label === 'Saldo pendiente' ? '0' : '1px solid #e5e7eb' }}>
                  <span>{label}</span>
                  <strong>{formatearMoneda(value)}</strong>
                </div>
              ))}
            </div>

            <div style={{ marginTop: '16px', padding: '12px', background: '#fff3cd', borderRadius: '8px', color: '#856404' }}>
              Concepto: {facturaActual.conceptoPago}. Cuenta propietario: {facturaActual.cuentaCorrientePropietario || 'No disponible'}.
            </div>

            <div className="modal-botones" style={{ marginTop: '18px' }}>
              <button type="button" className="btn-cancelar" onClick={cerrarFactura}>
                Cerrar
              </button>
              <button type="button" className="btn-success" onClick={() => descargarFactura(facturaActual)}>
                Descargar PDF
              </button>
            </div>
          </div>
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
    </div>
  );
}
