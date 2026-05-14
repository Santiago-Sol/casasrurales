import { useEffect, useState } from 'react'

const formularioInicial = {
  fechaInicio: '',
  fechaFin: '',
  modalidad: 'CASA_ENTERA',
  precioCasaEntera: 0,
  precioHabitacion: 0,
  disponible: true
}

export default function GestionPaquetes({ casa, onClose }) {
  const [paquetes, setPaquetes] = useState([])
  const [cargando, setCargando] = useState(true)
  const [mensaje, setMensaje] = useState('')
  const [modo, setModo] = useState('lista')
  const [paqueteEditando, setPaqueteEditando] = useState(null)
  const [formulario, setFormulario] = useState(formularioInicial)
  const [guardando, setGuardando] = useState(false)

  useEffect(() => {
    cargarPaquetes()
  }, [casa])

  const cargarPaquetes = async () => {
    try {
      setCargando(true)
      const response = await fetch(`/api/propietario/mis-casas/${casa.codigoCasa}/paquetes`, {
        credentials: 'same-origin'
      })

      if (response.ok) {
        const data = await response.json()
        setPaquetes(data)
      } else {
        setMensaje('Error al cargar paquetes')
      }
    } catch (error) {
      setMensaje('Error de conexion')
    } finally {
      setCargando(false)
    }
  }

  const abrirFormulario = (paquete = null) => {
    if (paquete) {
      setPaqueteEditando(paquete)
      setFormulario({
        fechaInicio: paquete.fechaInicio,
        fechaFin: paquete.fechaFin,
        modalidad: paquete.modalidad,
        precioCasaEntera: paquete.precioCasaEntera || 0,
        precioHabitacion: paquete.precioHabitacion || 0,
        disponible: paquete.disponible
      })
    } else {
      setPaqueteEditando(null)
      setFormulario(formularioInicial)
    }
    setMensaje('')
    setModo('formulario')
  }

  const actualizarFormulario = (event) => {
    const { name, value, type, checked } = event.target
    setFormulario((actual) => ({
      ...actual,
      [name]: type === 'checkbox' ? checked : value
    }))
  }

  const guardarPaquete = async (event) => {
    event.preventDefault()
    setGuardando(true)
    setMensaje('')

    const payload = {
      ...formulario,
      precioCasaEntera: Number(formulario.precioCasaEntera),
      precioHabitacion: Number(formulario.precioHabitacion)
    }

    const url = paqueteEditando
      ? `/api/propietario/mis-casas/${casa.codigoCasa}/paquetes/${paqueteEditando.idPaquete}`
      : `/api/propietario/mis-casas/${casa.codigoCasa}/paquetes`
    const method = paqueteEditando ? 'PUT' : 'POST'

    try {
      const response = await fetch(url, {
        method,
        headers: { 'Content-Type': 'application/json' },
        credentials: 'same-origin',
        body: JSON.stringify(payload)
      })
      const data = await response.json().catch(() => ({}))

      if (response.ok) {
        setModo('lista')
        cargarPaquetes()
      } else {
        setMensaje(data.error || 'Error al guardar el paquete')
      }
    } catch (error) {
      setMensaje('Error de conexion')
    } finally {
      setGuardando(false)
    }
  }

  const eliminarPaquete = async (idPaquete) => {
    if (!window.confirm('Seguro que deseas eliminar este paquete?')) return

    try {
      const response = await fetch(`/api/propietario/mis-casas/${casa.codigoCasa}/paquetes/${idPaquete}`, {
        method: 'DELETE',
        credentials: 'same-origin'
      })

      if (response.ok) {
        cargarPaquetes()
      } else {
        const data = await response.json().catch(() => ({}))
        setMensaje(data.error || 'Error al eliminar')
      }
    } catch (error) {
      setMensaje('Error de conexion')
    }
  }

  const requiereCasaEntera = formulario.modalidad === 'CASA_ENTERA' || formulario.modalidad === 'AMBAS'
  const requiereHabitacion = formulario.modalidad === 'POR_HABITACIONES' || formulario.modalidad === 'AMBAS'

  return (
    <div className="modal-overlay">
      <div className="modal-contenido" style={{ maxWidth: '800px' }}>
        <h3>Gestion de Paquetes - {casa.nombrePropiedad}</h3>
        {mensaje && <div className="mensaje error">{mensaje}</div>}

        {modo === 'lista' ? (
          <div>
            <button className="btn-primary-action" onClick={() => abrirFormulario()}>
              + Nuevo Paquete
            </button>

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
                  {paquetes.map((paquete) => (
                    <tr key={paquete.idPaquete}>
                      <td>{paquete.fechaInicio}</td>
                      <td>{paquete.fechaFin}</td>
                      <td>{paquete.modalidad}</td>
                      <td>${paquete.precioCasaEntera}</td>
                      <td>${paquete.precioHabitacion}</td>
                      <td>{paquete.disponible ? 'Disponible' : 'Oculto'}</td>
                      <td className="acciones">
                        <button className="btn-warning" onClick={() => abrirFormulario(paquete)}>
                          Editar
                        </button>
                        <button className="btn-danger" onClick={() => eliminarPaquete(paquete.idPaquete)}>
                          Eliminar
                        </button>
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
                <input type="date" name="fechaInicio" value={formulario.fechaInicio} onChange={actualizarFormulario} required />
              </div>

              <div className="campo-formulario">
                <label>Fecha Fin</label>
                <input type="date" name="fechaFin" value={formulario.fechaFin} onChange={actualizarFormulario} required />
              </div>

              <div className="campo-formulario">
                <label>Modalidad</label>
                <select name="modalidad" value={formulario.modalidad} onChange={actualizarFormulario}>
                  <option value="CASA_ENTERA">Casa Entera</option>
                  <option value="POR_HABITACIONES">Por Habitaciones</option>
                  <option value="AMBAS">Ambas</option>
                </select>
              </div>

              {requiereCasaEntera && (
                <div className="campo-formulario">
                  <label>Precio Casa Entera</label>
                  <input type="number" min="0.01" step="0.01" name="precioCasaEntera" value={formulario.precioCasaEntera} onChange={actualizarFormulario} required />
                </div>
              )}

              {requiereHabitacion && (
                <div className="campo-formulario">
                  <label>Precio por Habitacion</label>
                  <input type="number" min="0.01" step="0.01" name="precioHabitacion" value={formulario.precioHabitacion} onChange={actualizarFormulario} required />
                </div>
              )}

              <div className="campo-formulario" style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                <label>Disponible</label>
                <input type="checkbox" name="disponible" checked={formulario.disponible} onChange={actualizarFormulario} />
              </div>
            </div>

            <div className="modal-botones">
              <button type="button" className="btn-cancelar" onClick={() => { setModo('lista'); setMensaje('') }}>
                Cancelar
              </button>
              <button type="submit" className="btn-primary-action" disabled={guardando}>
                {guardando ? 'Guardando...' : 'Guardar Paquete'}
              </button>
            </div>
          </form>
        )}
      </div>
    </div>
  )
}
