import { useState, useEffect } from 'react';

export default function GestionPaquetes({ casa, onClose }) {
  const [paquetes, setPaquetes] = useState([]);
  const [cargando, setCargando] = useState(true);
  const [mensaje, setMensaje] = useState('');
  const [modo, setModo] = useState('lista'); // 'lista' o 'formulario'
  const [paqueteEditando, setPaqueteEditando] = useState(null);
  const [formulario, setFormulario] = useState({
    fechaInicio: '',
    fechaFin: '',
    modalidad: 'CASA_ENTERA',
    precioCasaEntera: 0,
    precioHabitacion: 0,
    disponible: true
  });
  const [guardando, setGuardando] = useState(false);

  useEffect(() => {
    cargarPaquetes();
  }, [casa]);

  const cargarPaquetes = async () => {
    try {
      setCargando(true);
      const response = await fetch(`/api/propietario/mis-casas/${casa.codigoCasa}/paquetes`, {
        credentials: 'same-origin'
      });
      if (response.ok) {
        const data = await response.json();
        setPaquetes(data);
      } else {
        setMensaje('Error al cargar paquetes');
      }
    } catch (error) {
      setMensaje('Error de conexión');
    } finally {
      setCargando(false);
    }
  };

  const abrirFormulario = (paquete = null) => {
    if (paquete) {
      setPaqueteEditando(paquete);
      setFormulario({
        fechaInicio: paquete.fechaInicio,
        fechaFin: paquete.fechaFin,
        modalidad: paquete.modalidad,
        precioCasaEntera: paquete.precioCasaEntera || 0,
        precioHabitacion: paquete.precioHabitacion || 0,
        disponible: paquete.disponible
      });
    } else {
      setPaqueteEditando(null);
      setFormulario({
        fechaInicio: '',
        fechaFin: '',
        modalidad: 'CASA_ENTERA',
        precioCasaEntera: 0,
        precioHabitacion: 0,
        disponible: true
      });
    }
    setMensaje('');
    setModo('formulario');
  };

  const handleChange = (e) => {
    const { name, value, type, checked } = e.target;
    setFormulario({
      ...formulario,
      [name]: type === 'checkbox' ? checked : value
    });
  };

  const guardarPaquete = async (e) => {
    e.preventDefault();
    setGuardando(true);
    setMensaje('');

    const payload = {
      ...formulario,
      precioCasaEntera: Number(formulario.precioCasaEntera),
      precioHabitacion: Number(formulario.precioHabitacion)
    };

    const url = paqueteEditando 
      ? `/api/propietario/mis-casas/${casa.codigoCasa}/paquetes/${paqueteEditando.idPaquete}`
      : `/api/propietario/mis-casas/${casa.codigoCasa}/paquetes`;
    
    const method = paqueteEditando ? 'PUT' : 'POST';

    try {
      const response = await fetch(url, {
        method,
        headers: { 'Content-Type': 'application/json' },
        credentials: 'same-origin',
        body: JSON.stringify(payload)
      });
      const data = await response.json().catch(() => ({}));
      
      if (response.ok) {
        setModo('lista');
        cargarPaquetes();
      } else {
        setMensaje(data.error || 'Error al guardar el paquete');
      }
    } catch (error) {
      setMensaje('Error de conexión');
    } finally {
      setGuardando(false);
    }
  };

  const eliminarPaquete = async (idPaquete) => {
    if (!window.confirm('¿Seguro que deseas eliminar este paquete?')) return;
    try {
      const response = await fetch(`/api/propietario/mis-casas/${casa.codigoCasa}/paquetes/${idPaquete}`, {
        method: 'DELETE',
        credentials: 'same-origin'
      });
      if (response.ok) {
        cargarPaquetes();
      } else {
        const data = await response.json().catch(() => ({}));
        setMensaje(data.error || 'Error al eliminar');
      }
    } catch (error) {
      setMensaje('Error de conexión');
    }
  };

  return (
    <div className="modal-overlay">
      <div className="modal-contenido" style={{ maxWidth: '800px' }}>
        <h3>Gestión de Paquetes - {casa.nombrePropiedad}</h3>
        {mensaje && <div className="mensaje error">{mensaje}</div>}
        
        {modo === 'lista' ? (
          <div>
            <button className="btn-primary-action" onClick={() => abrirFormulario()}>+ Nuevo Paquete</button>
            {cargando ? (
              <p>Cargando paquetes...</p>
            ) : paquetes.length === 0 ? (
              <p>No hay paquetes configurados.</p>
            ) : (
              <table className="tabla-casas" style={{ marginTop: '1rem' }}>
                <thead>
                  <tr>
                    <th>Inicio</th>
                    <th>Fin</th>
                    <th>Modalidad</th>
                    <th>Precio C. Entera</th>
                    <th>Precio Hab.</th>
                    <th>Estado</th>
                    <th>Acciones</th>
                  </tr>
                </thead>
                <tbody>
                  {paquetes.map(p => (
                    <tr key={p.idPaquete}>
                      <td>{p.fechaInicio}</td>
                      <td>{p.fechaFin}</td>
                      <td>{p.modalidad}</td>
                      <td>${p.precioCasaEntera}</td>
                      <td>${p.precioHabitacion}</td>
                      <td>{p.disponible ? 'Disponible' : 'Oculto'}</td>
                      <td className="acciones">
                        <button className="btn-warning" onClick={() => abrirFormulario(p)}>Editar</button>
                        <button className="btn-danger" onClick={() => eliminarPaquete(p.idPaquete)}>Eliminar</button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
            <div className="modal-botones" style={{ marginTop: '20px' }}>
              <button className="btn-cancelar" onClick={onClose}>Cerrar</button>
            </div>
          </div>
        ) : (
          <form className="formulario-casa" onSubmit={guardarPaquete}>
            <div className="formulario-grid">
              <div className="campo-formulario">
                <label>Fecha Inicio</label>
                <input type="date" name="fechaInicio" value={formulario.fechaInicio} onChange={handleChange} required />
              </div>
              <div className="campo-formulario">
                <label>Fecha Fin</label>
                <input type="date" name="fechaFin" value={formulario.fechaFin} onChange={handleChange} required />
              </div>
              <div className="campo-formulario">
                <label>Modalidad</label>
                <select name="modalidad" value={formulario.modalidad} onChange={handleChange}>
                  <option value="CASA_ENTERA">Casa Entera</option>
                  <option value="POR_HABITACIONES">Por Habitaciones</option>
                  <option value="AMBAS">Ambas</option>
                </select>
              </div>
              {(formulario.modalidad === 'CASA_ENTERA' || formulario.modalidad === 'AMBAS') && (
                <div className="campo-formulario">
                  <label>Precio Casa Entera</label>
                  <input type="number" min="0" step="0.01" name="precioCasaEntera" value={formulario.precioCasaEntera} onChange={handleChange} required />
                </div>
              )}
              {(formulario.modalidad === 'POR_HABITACIONES' || formulario.modalidad === 'AMBAS') && (
                <div className="campo-formulario">
                  <label>Precio por Habitación</label>
                  <input type="number" min="0" step="0.01" name="precioHabitacion" value={formulario.precioHabitacion} onChange={handleChange} required />
                </div>
              )}
              <div className="campo-formulario" style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                <label>Disponible</label>
                <input type="checkbox" name="disponible" checked={formulario.disponible} onChange={handleChange} />
              </div>
            </div>
            <div className="modal-botones">
              <button type="button" className="btn-cancelar" onClick={() => { setModo('lista'); setMensaje(''); }}>Cancelar</button>
              <button type="submit" className="btn-primary-action" disabled={guardando}>
                {guardando ? 'Guardando...' : 'Guardar Paquete'}
              </button>
            </div>
          </form>
        )}
      </div>
    </div>
  );
}
