import { useEffect, useState } from 'react'
import '../styles/dashboard.css'

const formularioInicial = {
  codigoCasa: '',
  nombrePropiedad: '',
  poblacion: '',
  descripcion: '',
  numComedores: 0,
  numPlazasGaraje: 0,
  numHabitaciones: 3,
  numBanos: 1,
  numCocinas: 1
}

export default function DashboardPropietario() {
  const [casas, setCasas] = useState([])
  const [cargando, setCargando] = useState(true)
  const [mensaje, setMensaje] = useState('')
  const [tipoMensaje, setTipoMensaje] = useState('exito')
  const [casaAEliminar, setCasaAEliminar] = useState(null)
  const [confirmacion, setConfirmacion] = useState(false)
  const [modalFormularioAbierto, setModalFormularioAbierto] = useState(false)
  const [modoFormulario, setModoFormulario] = useState('crear')
  const [casaEditando, setCasaEditando] = useState(null)
  const [formulario, setFormulario] = useState(formularioInicial)
  const [guardando, setGuardando] = useState(false)

  useEffect(() => {
    obtenerMisCasas()
  }, [])

  const mostrarMensaje = (texto, tipo = 'exito') => {
    setMensaje(texto)
    setTipoMensaje(tipo)
  }

  const obtenerMisCasas = async () => {
    try {
      const response = await fetch('/api/propietario/mis-casas', {
        credentials: 'same-origin'
      })

      if (response.status === 401) {
        mostrarMensaje('Debes estar autenticado como propietario', 'error')
        return
      }

      if (response.ok) {
        const data = await response.json()
        setCasas(data)
        if (data.length === 0) {
          mostrarMensaje('No tienes casas registradas aun', 'advertencia')
        }
        return
      }

      if (response.status === 404) {
        setCasas([])
        mostrarMensaje('No tienes casas registradas aun', 'advertencia')
        return
      }

      mostrarMensaje('Error al cargar tus casas', 'error')
    } catch (error) {
      mostrarMensaje('Error de conexion con el servidor', 'error')
      console.error(error)
    } finally {
      setCargando(false)
    }
  }

  const abrirModalCrear = () => {
    setModoFormulario('crear')
    setCasaEditando(null)
    setFormulario(formularioInicial)
    setModalFormularioAbierto(true)
  }

  const abrirModalEditar = (casa) => {
    setModoFormulario('editar')
    setCasaEditando(casa)
    setFormulario({
      codigoCasa: casa.codigoCasa ?? '',
      nombrePropiedad: casa.nombrePropiedad ?? '',
      poblacion: casa.poblacion ?? '',
      descripcion: casa.descripcion ?? '',
      numComedores: casa.comedores ?? 0,
      numPlazasGaraje: casa.plazasGaraje ?? 0,
      numHabitaciones: casa.habitaciones ?? 3,
      numBanos: casa.banos ?? 1,
      numCocinas: casa.cocinas ?? 1
    })
    setModalFormularioAbierto(true)
  }

  const cerrarModalFormulario = () => {
    if (guardando) return
    setModalFormularioAbierto(false)
    setCasaEditando(null)
    setFormulario(formularioInicial)
  }

  const resetearFormulario = () => {
    setModalFormularioAbierto(false)
    setCasaEditando(null)
    setFormulario(formularioInicial)
  }

  const actualizarFormulario = ({ target }) => {
    const { name, value } = target
    setFormulario((actual) => ({
      ...actual,
      [name]: value
    }))
  }

  const guardarCasa = async (event) => {
    event.preventDefault()
    setGuardando(true)

    const payload = {
      codigoCasa: Number(formulario.codigoCasa),
      nombrePropiedad: formulario.nombrePropiedad.trim(),
      poblacion: formulario.poblacion.trim(),
      descripcion: formulario.descripcion.trim(),
      numComedores: Number(formulario.numComedores),
      numPlazasGaraje: Number(formulario.numPlazasGaraje),
      numHabitaciones: Number(formulario.numHabitaciones),
      numBanos: Number(formulario.numBanos),
      numCocinas: Number(formulario.numCocinas)
    }

    const esEdicion = modoFormulario === 'editar' && casaEditando
    const url = esEdicion
      ? `/api/propietario/mis-casas/${casaEditando.codigoCasa}`
      : '/api/propietario/mis-casas'
    const method = esEdicion ? 'PUT' : 'POST'

    try {
      const response = await fetch(url, {
        method,
        headers: {
          'Content-Type': 'application/json'
        },
        credentials: 'same-origin',
        body: JSON.stringify(payload)
      })

      const data = await response.json().catch(() => ({}))

      if (response.ok) {
        mostrarMensaje(
          esEdicion ? 'Casa actualizada exitosamente' : 'Casa registrada exitosamente',
          'exito'
        )
        resetearFormulario()
        await obtenerMisCasas()
        return
      }

      mostrarMensaje(data.error || 'No fue posible guardar la casa', 'error')
    } catch (error) {
      mostrarMensaje('Error de conexion al guardar la casa', 'error')
      console.error(error)
    } finally {
      setGuardando(false)
    }
  }

  const mostrarConfirmacion = (casa) => {
    setCasaAEliminar(casa)
    setConfirmacion(true)
  }

  const cancelarBaja = () => {
    setCasaAEliminar(null)
    setConfirmacion(false)
  }

  const confirmarBaja = async () => {
    if (!casaAEliminar) return

    try {
      const response = await fetch(`/api/propietario/${casaAEliminar.codigoCasa}`, {
        method: 'DELETE',
        credentials: 'same-origin'
      })

      const data = await response.json().catch(() => ({}))

      if (response.ok) {
        mostrarMensaje('Casa dada de baja exitosamente', 'exito')
        await obtenerMisCasas()
      } else if (response.status === 409) {
        mostrarMensaje(data.error || 'La casa no se puede dar de baja', 'advertencia')
      } else {
        mostrarMensaje(data.error || 'Error al dar de baja la casa', 'error')
      }
    } catch (error) {
      mostrarMensaje('Error de conexion', 'error')
      console.error(error)
    } finally {
      setCasaAEliminar(null)
      setConfirmacion(false)
    }
  }

  const reactivarCasa = async (codigoCasa) => {
    try {
      const response = await fetch(`/api/propietario/${codigoCasa}/reactivar`, {
        method: 'POST',
        credentials: 'same-origin'
      })

      const data = await response.json().catch(() => ({}))

      if (response.ok) {
        mostrarMensaje('Casa reactivada exitosamente', 'exito')
        await obtenerMisCasas()
      } else {
        mostrarMensaje(data.error || 'Error al reactivar', 'error')
      }
    } catch (error) {
      mostrarMensaje('Error de conexion', 'error')
      console.error(error)
    }
  }

  if (cargando) {
    return <div className="dashboard-container"><p>Cargando...</p></div>
  }

  return (
    <div className="dashboard-container">
      <div className="dashboard-header">
        <button className="btn-primary-action" onClick={abrirModalCrear}>
          + Registrar Casa
        </button>
        <h2>Mi Dashboard de Propietario</h2>
      </div>

      {mensaje && <div className={`mensaje ${tipoMensaje}`}>{mensaje}</div>}

      {casas.length === 0 ? (
        <div className="sin-casas">
          <p>No tienes casas registradas aun</p>
          <button className="btn-primary-action" onClick={abrirModalCrear}>
            Registrar mi primera casa
          </button>
        </div>
      ) : (
        <div className="casas-propietario">
          <h3>Mis Casas ({casas.length})</h3>
          <table className="tabla-casas">
            <thead>
              <tr>
                <th>Codigo</th>
                <th>Nombre</th>
                <th>Poblacion</th>
                <th>Habitaciones</th>
                <th>Reservas Activas</th>
                <th>Estado</th>
                <th>Acciones</th>
              </tr>
            </thead>
            <tbody>
              {casas.map((casa) => (
                <tr key={casa.codigoCasa} className={!casa.activa ? 'inactiva' : ''}>
                  <td>{casa.codigoCasa}</td>
                  <td>{casa.nombrePropiedad}</td>
                  <td>{casa.poblacion}</td>
                  <td>{casa.habitaciones}</td>
                  <td>{casa.reservasActivas}</td>
                  <td>
                    <span className={`estado-badge ${casa.activa ? 'activa' : 'inactiva'}`}>
                      {casa.activa ? 'Activa' : 'Dada de Baja'}
                    </span>
                  </td>
                  <td className="acciones">
                    <button
                      className="btn-warning"
                      onClick={() => abrirModalEditar(casa)}
                    >
                      Editar
                    </button>
                    {casa.activa ? (
                      <button
                        className="btn-danger"
                        onClick={() => mostrarConfirmacion(casa)}
                        disabled={casa.reservasActivas > 0}
                        title={casa.reservasActivas > 0 ? 'No puedes dar de baja con reservas activas' : ''}
                      >
                        Dar de Baja
                      </button>
                    ) : (
                      <button
                        className="btn-success"
                        onClick={() => reactivarCasa(casa.codigoCasa)}
                      >
                        Reactivar
                      </button>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {modalFormularioAbierto && (
        <div className="modal-overlay">
          <div className="modal-contenido modal-formulario">
            <h3>{modoFormulario === 'crear' ? 'Registrar Casa' : 'Editar Casa'}</h3>
            <form className="formulario-casa" onSubmit={guardarCasa}>
              <div className="campo-formulario">
                <label htmlFor="codigoCasa">Codigo de la casa</label>
                <input
                  id="codigoCasa"
                  name="codigoCasa"
                  type="number"
                  min="1"
                  value={formulario.codigoCasa}
                  onChange={actualizarFormulario}
                  required
                  disabled={modoFormulario === 'editar'}
                />
              </div>

              <div className="campo-formulario">
                <label htmlFor="nombrePropiedad">Nombre</label>
                <input
                  id="nombrePropiedad"
                  name="nombrePropiedad"
                  type="text"
                  value={formulario.nombrePropiedad}
                  onChange={actualizarFormulario}
                  required
                />
              </div>

              <div className="campo-formulario">
                <label htmlFor="poblacion">Poblacion</label>
                <input
                  id="poblacion"
                  name="poblacion"
                  type="text"
                  value={formulario.poblacion}
                  onChange={actualizarFormulario}
                  required
                />
              </div>

              <div className="formulario-grid">
                <div className="campo-formulario">
                  <label htmlFor="numHabitaciones">Habitaciones</label>
                  <input
                    id="numHabitaciones"
                    name="numHabitaciones"
                    type="number"
                    min="3"
                    value={formulario.numHabitaciones}
                    onChange={actualizarFormulario}
                    required
                    disabled={modoFormulario === 'editar'}
                  />
                </div>

                <div className="campo-formulario">
                  <label htmlFor="numBanos">Banos</label>
                  <input
                    id="numBanos"
                    name="numBanos"
                    type="number"
                    min="1"
                    value={formulario.numBanos}
                    onChange={actualizarFormulario}
                    required
                    disabled={modoFormulario === 'editar'}
                  />
                </div>

                <div className="campo-formulario">
                  <label htmlFor="numCocinas">Cocinas</label>
                  <input
                    id="numCocinas"
                    name="numCocinas"
                    type="number"
                    min="1"
                    value={formulario.numCocinas}
                    onChange={actualizarFormulario}
                    required
                    disabled={modoFormulario === 'editar'}
                  />
                </div>

                <div className="campo-formulario">
                  <label htmlFor="numComedores">Comedores</label>
                  <input
                    id="numComedores"
                    name="numComedores"
                    type="number"
                    min="0"
                    value={formulario.numComedores}
                    onChange={actualizarFormulario}
                    required
                  />
                </div>

                <div className="campo-formulario">
                  <label htmlFor="numPlazasGaraje">Plazas de garaje</label>
                  <input
                    id="numPlazasGaraje"
                    name="numPlazasGaraje"
                    type="number"
                    min="0"
                    value={formulario.numPlazasGaraje}
                    onChange={actualizarFormulario}
                    required
                  />
                </div>
              </div>

              <div className="campo-formulario">
                <label htmlFor="descripcion">Descripcion</label>
                <textarea
                  id="descripcion"
                  name="descripcion"
                  rows="4"
                  value={formulario.descripcion}
                  onChange={actualizarFormulario}
                  required
                />
              </div>

              <div className="modal-botones">
                <button type="button" className="btn-cancelar" onClick={cerrarModalFormulario}>
                  Cancelar
                </button>
                <button type="submit" className="btn-primary-action" disabled={guardando}>
                  {guardando
                    ? 'Guardando...'
                    : modoFormulario === 'crear'
                      ? 'Registrar Casa'
                      : 'Guardar Cambios'}
                </button>
              </div>
              {modoFormulario === 'editar' && (
                <p className="advertencia">
                  La cantidad de habitaciones, banos y cocinas se define al crear la casa.
                </p>
              )}
            </form>
          </div>
        </div>
      )}

      {confirmacion && casaAEliminar && (
        <div className="modal-overlay">
          <div className="modal-contenido">
            <h3>Confirmar Baja de Casa</h3>
            <div className="modal-cuerpo">
              <p>Estas seguro de que quieres dar de baja esta casa?</p>
              <div className="info-casa">
                <p><strong>Codigo:</strong> {casaAEliminar.codigoCasa}</p>
                <p><strong>Nombre:</strong> {casaAEliminar.nombrePropiedad}</p>
                <p><strong>Poblacion:</strong> {casaAEliminar.poblacion}</p>
                <p><strong>Reservas Activas:</strong> {casaAEliminar.reservasActivas}</p>
              </div>
              <p className="advertencia">
                Una vez dada de baja, la casa no aparecera en busquedas ni dispondra de nuevas
                reservas. Puedes reactivarla en cualquier momento.
              </p>
            </div>
            <div className="modal-botones">
              <button className="btn-cancelar" onClick={cancelarBaja}>
                Cancelar
              </button>
              <button className="btn-confirmar" onClick={confirmarBaja}>
                Confirmar Baja
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
