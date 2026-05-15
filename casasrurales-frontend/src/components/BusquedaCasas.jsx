import { useEffect, useMemo, useState } from 'react'
import '../styles/busqueda.css'
import ReservaCasa from './ReservaCasa'
import { notificarSiEsError } from '../utils/notificaciones'

const imagenesCasas = [
  'https://images.unsplash.com/photo-1564013799919-ab600027ffc6?auto=format&fit=crop&w=900&q=80',
  'https://images.unsplash.com/photo-1518780664697-55e3ad937233?auto=format&fit=crop&w=900&q=80',
  'https://images.unsplash.com/photo-1505693416388-ac5ce068fe85?auto=format&fit=crop&w=900&q=80',
  'https://images.unsplash.com/photo-1600585154340-be6161a56a0c?auto=format&fit=crop&w=900&q=80'
]

export default function BusquedaCasas({ usuarioAutenticado, onRequireLogin, onAuthExpired }) {
  const [tipoBusqueda, setTipoBusqueda] = useState('poblacion')
  const [termino, setTermino] = useState('Armenia')
  const [resultados, setResultados] = useState([])
  const [detalle, setDetalle] = useState(null)
  const [cargando, setCargando] = useState(false)
  const [mensaje, setMensaje] = useState('')
  const [tipoMensaje, setTipoMensaje] = useState('info')
  const [soloCasas, setSoloCasas] = useState(true)
  const [casaReservando, setCasaReservando] = useState(null)

  const casasOrdenadas = useMemo(
    () => [...resultados].sort((a, b) => a.nombrePropiedad.localeCompare(b.nombrePropiedad)),
    [resultados]
  )

  useEffect(() => {
    buscarPorPoblacion('Armenia')
  }, [])

  const mostrarMensaje = (texto, tipo = 'info') => {
    setMensaje(texto)
    setTipoMensaje(tipo)
    notificarSiEsError(texto, tipo)
  }

  const limpiarVista = () => {
    setResultados([])
    setDetalle(null)
  }

  const buscarPorPoblacion = async (valor = termino) => {
    const poblacion = valor.trim()
    if (!poblacion) {
      mostrarMensaje('Ingresa una poblacion para buscar casas', 'info')
      return
    }

    setCargando(true)
    setMensaje('')
    limpiarVista()

    try {
      const response = await fetch(
        `/api/busqueda/por-poblacion?poblacion=${encodeURIComponent(poblacion)}`,
        { credentials: 'include' }
      )

      if (response.status === 204) {
        mostrarMensaje('No encontramos casas en esa poblacion', 'info')
        return
      }

      if (!response.ok) {
        mostrarMensaje('No fue posible cargar las casas disponibles', 'error')
        return
      }

      const data = await response.json()
      setResultados(data)
      mostrarMensaje(`${data.length} casas rurales encontradas en ${poblacion}`, 'exito')
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
      mostrarMensaje('Ingresa el codigo de la casa', 'info')
      return
    }

    setCargando(true)
    setMensaje('')
    setDetalle(null)

    try {
      const response = await fetch(`/api/busqueda/${codigoNormalizado}`, {
        credentials: 'include'
      })

      if (response.status === 404) {
        mostrarMensaje('No encontramos una casa con ese codigo', 'info')
        return
      }

      if (!response.ok) {
        mostrarMensaje('No fue posible consultar el detalle', 'error')
        return
      }

      const data = await response.json()
      setDetalle(data)
      mostrarMensaje('Detalle cargado', 'exito')
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

  const cambiarTipoBusqueda = (value) => {
    setTipoBusqueda(value)
    setTermino(value === 'poblacion' ? 'Armenia' : '')
    limpiarVista()
    setMensaje('')
  }

  const imagenCasa = (casa, index = 0) => {
    if (casa.urlsFotos?.length) return casa.urlsFotos[0]
    return imagenesCasas[index % imagenesCasas.length]
  }

  return (
    <main className="catalog-page">
      <section className="catalog-hero">
        <div>
          <p className="breadcrumb">Inicio / Casas rurales / Quindio</p>
          <h1>Casas rurales en Armenia</h1>
          <p className="hero-copy">
            Encuentra estadias tranquilas, verdes y listas para reservar cerca del paisaje cafetero.
          </p>
        </div>

      </section>

      <section className="catalog-layout">
        <aside className="filters-panel">
          <div className="filter-block">
            <h2>Ubicacion y tipo</h2>
            <input
              type={tipoBusqueda === 'codigo' ? 'number' : 'text'}
              placeholder={tipoBusqueda === 'poblacion' ? 'Armenia' : 'Codigo de casa'}
              value={termino}
              onChange={(event) => setTermino(event.target.value)}
              onKeyDown={(event) => event.key === 'Enter' && handleBuscar()}
            />
          </div>

          <div className="filter-block">
            <h3>Buscar por</h3>
            <div className="segmented-control">
              <button
                className={tipoBusqueda === 'poblacion' ? 'selected' : ''}
                onClick={() => cambiarTipoBusqueda('poblacion')}
              >
                Poblacion
              </button>
              <button
                className={tipoBusqueda === 'codigo' ? 'selected' : ''}
                onClick={() => cambiarTipoBusqueda('codigo')}
              >
                Codigo
              </button>
            </div>
          </div>

          <div className="filter-block">
            <h3>Oferta</h3>
            <div className="offer-grid">
              <button>Arrendar</button>
              <button className="active">Reservar</button>
            </div>
          </div>

          <div className="filter-block">
            <h3>Tipo de inmueble</h3>
            <label className="check-row">
              <span>Casa rural</span>
              <input
                type="checkbox"
                checked={soloCasas}
                onChange={(event) => setSoloCasas(event.target.checked)}
              />
            </label>
          </div>

          <button className="search-button" onClick={handleBuscar} disabled={cargando}>
            {cargando ? 'Buscando...' : 'Buscar casas'}
          </button>
        </aside>

        <section className="results-panel">
          <div className="credit-strip">
            <strong>Escapadas verdes en el Quindio</strong>
            <span>Casas con espacios familiares, cocina y zonas para descansar.</span>
          </div>

          {mensaje && <div className={`mensaje ${tipoMensaje}`}>{mensaje}</div>}

          <div className="results-toolbar">
            <h2>{casasOrdenadas.length || 0} casas disponibles</h2>
            <select aria-label="Ordenar resultados">
              <option>Ordenar por recomendadas</option>
              <option>Nombre A-Z</option>
              <option>Mas habitaciones</option>
            </select>
          </div>

          <div className="property-grid">
            {casasOrdenadas.map((casa, index) => (
              <article className="property-card" key={casa.codigoCasa}>
                <button className="image-button" onClick={() => buscarPorCodigo(casa.codigoCasa)}>
                  <img src={imagenCasa(casa, index)} alt={casa.nombrePropiedad} />
                  <span className="badge">Disponible</span>
                  <span className="badge badge-yellow">Rural</span>
                </button>
                <div className="property-body">
                  <p className="location">{casa.poblacion}</p>
                  <h3>{casa.nombrePropiedad}</h3>
                  <p className="description">{casa.descripcionGeneral || 'Casa rural lista para una estadia tranquila.'}</p>
                  <div className="spec-row">
                    <span>{casa.numDormitorios} hab.</span>
                    <span>{casa.numBanos} banos</span>
                    <span>{casa.numCocinas} cocina</span>
                  </div>
                  <button className="contact-button" onClick={() => buscarPorCodigo(casa.codigoCasa)}>
                    Ver detalle
                  </button>
                </div>
              </article>
            ))}
          </div>
        </section>
      </section>

      {detalle && (
        <div className="detail-overlay" onClick={() => setDetalle(null)}>
          <section className="detail-modal" onClick={(event) => event.stopPropagation()}>
            <button className="close-detail" onClick={() => setDetalle(null)}>x</button>
            <img src={imagenCasa(detalle)} alt={detalle.nombrePropiedad} />
            <div className="detail-content">
              <p className="location">{detalle.poblacion}</p>
              <h2>{detalle.nombrePropiedad}</h2>
              <p>{detalle.descripcionGeneral || 'Casa rural con espacios completos para descansar.'}</p>
              <div className="detail-specs">
                <span>{detalle.numDormitorios} habitaciones</span>
                <span>{detalle.numBanos} banos</span>
                <span>{detalle.numCocinas} cocinas</span>
                <span>{detalle.numPlazasGaraje} garajes</span>
              </div>
              <p className="owner">Telefono: {detalle.telefonoPropietario}</p>
              <button 
                className="btn-primary-action" 
                style={{ marginTop: '20px', width: '100%' }}
                onClick={() => {
                  const esClienteValido = usuarioAutenticado && 
                    (usuarioAutenticado.tipoUsuario === 'cliente' || 
                     String(usuarioAutenticado.tipoUsuario).toLowerCase() === 'cliente' ||
                     !usuarioAutenticado.tipoUsuario); // Permitir si por alguna razon falta pero esta autenticado
                     
                  if (!esClienteValido) {
                    alert("Por favor inicia sesión como cliente para poder reservar.");
                    onRequireLogin();
                  } else {
                    setCasaReservando(detalle);
                  }
                }}
              >
                Reservar esta casa
              </button>
            </div>
          </section>
        </div>
      )}

      {casaReservando && (
        <ReservaCasa 
          casa={casaReservando} 
          onClose={() => setCasaReservando(null)} 
          onAuthExpired={onAuthExpired}
        />
      )}
    </main>
  )
}
