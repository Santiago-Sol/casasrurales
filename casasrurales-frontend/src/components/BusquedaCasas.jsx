import { useState } from 'react'
import '../styles/busqueda.css'

export default function BusquedaCasas() {
  const [tipoBusqueda, setTipoBusqueda] = useState('poblacion')
  const [termino, setTermino] = useState('')
  const [resultados, setResultados] = useState([])
  const [detalle, setDetalle] = useState(null)
  const [cargando, setCargando] = useState(false)
  const [mensaje, setMensaje] = useState('')
  const [tipoMensaje, setTipoMensaje] = useState('info')

  const mostrarMensaje = (texto, tipo = 'info') => {
    setMensaje(texto)
    setTipoMensaje(tipo)
  }

  const limpiarVista = () => {
    setResultados([])
    setDetalle(null)
  }

  const buscarPorPoblacion = async () => {
    if (!termino.trim()) {
      mostrarMensaje('Por favor ingresa un nombre de poblacion', 'info')
      return
    }

    setCargando(true)
    setMensaje('')
    setTipoMensaje('info')
    limpiarVista()

    try {
      const response = await fetch(
        `/api/busqueda/por-poblacion?poblacion=${encodeURIComponent(termino.trim())}`,
        { credentials: 'include' }
      )

      if (response.status === 401 || response.status === 403) {
        mostrarMensaje('Debes iniciar sesion como cliente para buscar casas', 'error')
        return
      }

      if (response.status === 204) {
        mostrarMensaje('No se encontraron casas en esa poblacion', 'info')
        return
      }

      if (!response.ok) {
        mostrarMensaje('Error en la busqueda', 'error')
        return
      }

      const data = await response.json()
      setResultados(data)
      mostrarMensaje(`Se encontraron ${data.length} casas`, 'exito')
    } catch (error) {
      mostrarMensaje('Error de conexion con el servidor', 'error')
      console.error(error)
    } finally {
      setCargando(false)
    }
  }

  const buscarPorCodigo = async (codigo = termino) => {
    const codigoNormalizado = String(codigo).trim()

    if (!codigoNormalizado) {
      mostrarMensaje('Por favor ingresa un codigo', 'info')
      return
    }

    setCargando(true)
    setMensaje('')
    setTipoMensaje('info')
    setDetalle(null)

    try {
      const response = await fetch(`/api/busqueda/${codigoNormalizado}`, {
        credentials: 'include'
      })

      if (response.status === 401 || response.status === 403) {
        mostrarMensaje('Debes iniciar sesion como cliente para consultar detalles', 'error')
        return
      }

      if (response.status === 404) {
        mostrarMensaje('No se encontro casa con ese codigo', 'info')
        return
      }

      if (!response.ok) {
        mostrarMensaje('Error en la busqueda', 'error')
        return
      }

      const data = await response.json()
      setDetalle(data)
      mostrarMensaje('Casa encontrada', 'exito')
    } catch (error) {
      mostrarMensaje('Error de conexion con el servidor', 'error')
      console.error(error)
    } finally {
      setCargando(false)
    }
  }

  const handleBuscar = () => {
    if (tipoBusqueda === 'poblacion') {
      buscarPorPoblacion()
      return
    }

    buscarPorCodigo()
  }

  const handleKeyDown = (event) => {
    if (event.key === 'Enter') {
      handleBuscar()
    }
  }

  const cambiarTipoBusqueda = (value) => {
    setTipoBusqueda(value)
    setTermino('')
    limpiarVista()
    setMensaje('')
    setTipoMensaje('info')
  }

  return (
    <div className="busqueda-container">
      <h2>Buscar Casas Rurales</h2>

      <div className="busqueda-options">
        <label>
          <input
            type="radio"
            value="poblacion"
            checked={tipoBusqueda === 'poblacion'}
            onChange={(event) => cambiarTipoBusqueda(event.target.value)}
          />
          Buscar por Poblacion
        </label>
        <label>
          <input
            type="radio"
            value="codigo"
            checked={tipoBusqueda === 'codigo'}
            onChange={(event) => cambiarTipoBusqueda(event.target.value)}
          />
          Buscar por Codigo
        </label>
      </div>

      <div className="busqueda-input">
        <input
          type={tipoBusqueda === 'codigo' ? 'number' : 'text'}
          placeholder={tipoBusqueda === 'poblacion' ? 'Ej: Salento' : 'Ej: 101'}
          value={termino}
          onChange={(event) => setTermino(event.target.value)}
          onKeyDown={handleKeyDown}
        />
        <button onClick={handleBuscar} disabled={cargando}>
          {cargando ? 'Buscando...' : 'Buscar'}
        </button>
      </div>

      {mensaje && (
        <div className={`mensaje ${tipoMensaje}`}>
          {mensaje}
        </div>
      )}

      {resultados.length > 0 && (
        <div className="resultados">
          <h3>Resultados ({resultados.length})</h3>
          <div className="casas-grid">
            {resultados.map((casa) => (
              <div
                key={casa.codigoCasa}
                className="casa-card"
                onClick={() => buscarPorCodigo(casa.codigoCasa)}
              >
                <h4>{casa.nombrePropiedad}</h4>
                <p><strong>Codigo:</strong> {casa.codigoCasa}</p>
                <p><strong>Poblacion:</strong> {casa.poblacion}</p>
                <p><strong>Propietario:</strong> {casa.nombrePropietario}</p>
                <p><strong>Habitaciones:</strong> {casa.numDormitorios}</p>
                <p><strong>Banos:</strong> {casa.numBanos}</p>
                <p><strong>Cocinas:</strong> {casa.numCocinas}</p>
                <p><strong>Descripcion:</strong> {casa.descripcionGeneral}</p>
              </div>
            ))}
          </div>
        </div>
      )}

      {detalle && (
        <div className="detalle">
          <h3>Detalles de la Casa</h3>
          <div className="detalle-content">
            <div className="detalle-info">
              <h4>{detalle.nombrePropiedad}</h4>
              <p><strong>Codigo:</strong> {detalle.codigoCasa}</p>
              <p><strong>Poblacion:</strong> {detalle.poblacion}</p>
              <p><strong>Descripcion:</strong> {detalle.descripcionGeneral}</p>
              <p><strong>Propietario:</strong> {detalle.nombrePropietario}</p>
              <p><strong>Telefono:</strong> {detalle.telefonoPropietario}</p>
            </div>
            <div className="detalle-specs">
              <h4>Especificaciones</h4>
              <p><strong>Habitaciones:</strong> {detalle.numDormitorios}</p>
              <p><strong>Banos:</strong> {detalle.numBanos}</p>
              <p><strong>Cocinas:</strong> {detalle.numCocinas}</p>
              <p><strong>Comedores:</strong> {detalle.numComedores}</p>
              <p><strong>Plazas de garaje:</strong> {detalle.numPlazasGaraje}</p>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
