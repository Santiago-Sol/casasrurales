import { useState, useEffect } from 'react';
import { formatFecha } from '../utils/formatFecha';

export default function MisReservasCliente() {
  const [reservas, setReservas] = useState([]);
  const [cargando, setCargando] = useState(true);
  const [mensaje, setMensaje] = useState('');

  useEffect(() => {
    const fetchMisReservas = async () => {
      try {
        const response = await fetch('/api/reservas/mis-reservas', {
          credentials: 'same-origin'
        });

        if (!response.ok) {
          throw new Error('No se pudieron cargar las reservas');
        }

        const data = await response.json();
        setReservas(data);
      } catch (error) {
        setMensaje(error.message);
      } finally {
        setCargando(false);
      }
    };

    fetchMisReservas();
  }, []);

  if (cargando) {
    return <div style={{ padding: '20px', textAlign: 'center' }}>Cargando reservas...</div>;
  }

  return (
    <div style={{ maxWidth: '1000px', margin: '0 auto', padding: '20px' }}>
      <h2>Mis Reservas</h2>
      
      {mensaje && <div className="mensaje error" style={{ marginBottom: '20px' }}>{mensaje}</div>}

      {reservas.length === 0 && !mensaje ? (
        <div style={{ textAlign: 'center', padding: '40px', background: '#f5f5f5', borderRadius: '8px' }}>
          <p>Aún no has realizado ninguna reserva.</p>
        </div>
      ) : (
        <div style={{ display: 'grid', gap: '20px' }}>
          {reservas.map(reserva => (
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
                  <p style={{ margin: '5px 0', fontSize: '0.9em', color: '#666' }}>Población</p>
                  <p style={{ margin: 0, fontWeight: 'bold' }}>{reserva.poblacion}</p>
                </div>
              </div>

              <div style={{ marginTop: '15px', padding: '10px', background: '#f8f9fa', borderRadius: '4px' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '5px' }}>
                  <span>Importe Total:</span>
                  <strong>${reserva.importeTotal.toLocaleString()}</strong>
                </div>
                {reserva.estado === 'PENDIENTE_PAGO' && (
                  <>
                    <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '5px' }}>
                      <span>Anticipo Requerido (20%):</span>
                      <strong style={{ color: '#d32f2f' }}>${reserva.importeAnticipo.toLocaleString()}</strong>
                    </div>
                    <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                      <span>Pagar antes de:</span>
                      <strong style={{ color: '#d32f2f' }}>{formatFecha(reserva.fechaLimitePago)}</strong>
                    </div>
                  </>
                )}
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
