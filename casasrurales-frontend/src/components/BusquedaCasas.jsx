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
  const [termino, setTermino] = useState('')
  const [fechaEntrada, setFechaEntrada] = useState('')
  const [fechaSalida, setFechaSalida] = useState('')
  const [habitaciones, setHabitaciones] = useState('')
  const [resultados, setResultados] = useState([])
  const [detalle, setDetalle] = useState(null)
  const [cargando, setCargando] = useState(false)
  const [mensaje, setMensaje] = useState('')
  const [tipoMensaje, setTipoMensaje] = useState('info')
  const [casaReservando, setCasaReservando] = useState(null)

  const casasOrdenadas = useMemo(
    () => {
      const habitacionesMinimas = Number(habitaciones) || 0
      return [...resultados]
        .filter((casa) => !habitacionesMinimas || casa.numDormitorios >= habitacionesMinimas)
        .sort((a, b) => a.nombrePropiedad.localeCompare(b.nombrePropiedad))
    },
    [resultados, habitaciones]
  )

  useEffect(() => {
    cargarCasasDisponibles(true)
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

  const calcularNoches = () => {
    if (!fechaEntrada || !fechaSalida) return 0
    const entrada = new Date(`${fechaEntrada}T00:00:00`)
    const salida = new Date(`${fechaSalida}T00:00:00`)
    const noches = Math.round((salida - entrada) / (1000 * 60 * 60 * 24))
    return noches > 0 ? noches : 0
  }

  const descripcionFiltros = () => {
    const filtros = []
    if (termino.trim()) filtros.push(termino.trim())
    if (fechaEntrada && fechaSalida) filtros.push(`${fechaEntrada} a ${fechaSalida}`)
    if (habitaciones) filtros.push(`${habitaciones} habitacion${Number(habitaciones) === 1 ? '' : 'es'}`)
    return filtros.length ? filtros.join(' - ') : 'Todas las casas disponibles'
  }

  const cargarCasasDisponibles = async (silencioso = false) => {
    const poblacionOCodigo = termino.trim()
    const esCodigo = /^\d+$/.test(poblacionOCodigo)
    const noches = calcularNoches()

    if ((fechaEntrada || fechaSalida) && noches === 0) {
      mostrarMensaje('Selecciona una fecha de salida posterior a la entrada', 'info')
      return
    }

    if (esCodigo) {
      buscarPorCodigoComoResultado(poblacionOCodigo)
      return
    }

    setCargando(true)
    setMensaje('')
    limpiarVista()

    try {
      const params = new URLSearchParams()
      if (poblacionOCodigo) params.set('poblacion', poblacionOCodigo)
      if (fechaEntrada && noches > 0) {
        params.set('fechaEntrada', fechaEntrada)
        params.set('numeroNoches', String(noches))
      }
      const query = params.toString()
      const response = await fetch(`/api/busqueda${query ? `?${query}` : ''}`, {
        credentials: 'include'
      })

      if (response.status === 204) {
        mostrarMensaje('No encontramos casas con esos filtros', 'info')
        return
      }

      if (!response.ok) {
        if (response.status === 404 && poblacionOCodigo) {
          const fallbackResponse = await fetch(`/api/busqueda/por-poblacion?poblacion=${encodeURIComponent(poblacionOCodigo)}`, {
            credentials: 'include'
          })

          if (fallbackResponse.status === 204) {
            mostrarMensaje('No encontramos casas con esos filtros', 'info')
            return
          }

          if (fallbackResponse.ok) {
            const data = await fallbackResponse.json()
            const casasDisponibles = fechaEntrada && noches > 0
              ? (await Promise.all(data.map(async (casa) => ({
                  casa,
                  disponible: await consultarDisponibilidadCasa(casa)
                })))).filter(({ disponible }) => disponible).map(({ casa }) => casa)
              : data

            setResultados(casasDisponibles)
            if (!silencioso) {
              mostrarMensaje(`${casasDisponibles.length} casas rurales encontradas`, 'exito')
            }
            return
          }
        }

        mostrarMensaje('No fue posible cargar las casas disponibles', 'error')
        return
      }

      const data = await response.json()
      setResultados(data)
      if (!silencioso) {
        mostrarMensaje(`${data.length} casas rurales encontradas`, 'exito')
      }
    } catch (error) {
      mostrarMensaje('Error de conexion con el servidor', 'error')
      console.error(error)
    } finally {
      setCargando(false)
    }
  }

  const cargarTodasLasCasas = async () => {
    setCargando(true)
    setMensaje('')
    limpiarVista()

    try {
      const response = await fetch('/api/busqueda', { credentials: 'include' })

      if (response.status === 204) {
        mostrarMensaje('No hay casas disponibles por ahora', 'info')
        return
      }

      if (!response.ok) {
        if (response.status === 404) {
          mostrarMensaje('El catalogo general aun no esta disponible en el servidor publicado', 'info')
          return
        }

        mostrarMensaje('No fue posible cargar las casas disponibles', 'error')
        return
      }

      const data = await response.json()
      setResultados(data)
    } catch (error) {
      mostrarMensaje('Error de conexion con el servidor', 'error')
      console.error(error)
    } finally {
      setCargando(false)
    }
  }

  const buscarPorCodigo = async (codigo = termino, abrirDetalle = true) => {
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
      if (abrirDetalle) {
        setDetalle(data)
        mostrarMensaje('Detalle cargado', 'exito')
      }
      return data
    } catch (error) {
      mostrarMensaje('Error de conexion con el servidor', 'error')
      console.error(error)
    } finally {
      setCargando(false)
    }
  }

  const consultarDisponibilidadCasa = async (casa) => {
    const noches = calcularNoches()
    if (!fechaEntrada || noches === 0) return true

    const params = new URLSearchParams({
      fechaEntrada,
      numeroNoches: String(noches)
    })
    const response = await fetch(`/api/busqueda/${casa.codigoCasa}/disponibilidad?${params.toString()}`, {
      credentials: 'include'
    })

    if (!response.ok) return false

    const disponibilidad = await response.json()
    const dias = disponibilidad.dias || []
    return dias.every((dia) => dia.estadoCasaEntera === 'LIBRE')
  }

  const buscarPorCodigoComoResultado = async (codigo) => {
    limpiarVista()
    const casa = await buscarPorCodigo(codigo, false)
    if (!casa) return

    const cumpleHabitaciones = !habitaciones || casa.numDormitorios >= Number(habitaciones)
    const cumpleDisponibilidad = await consultarDisponibilidadCasa(casa)

    if (!cumpleHabitaciones || !cumpleDisponibilidad) {
      setResultados([])
      mostrarMensaje('La casa existe, pero no cumple los filtros seleccionados', 'info')
      return
    }

    setResultados([casa])
    setDetalle(null)
    mostrarMensaje('Casa encontrada', 'exito')
  }

  const handleBuscar = (event) => {
    event?.preventDefault()
    cargarCasasDisponibles()
  }

  const limpiarFiltros = () => {
    setTermino('')
    setFechaEntrada('')
    setFechaSalida('')
    setHabitaciones('')
    setMensaje('')
    cargarTodasLasCasas()
  }

  const imagenCasa = (casa, index = 0) => {
    if (casa.urlsFotos?.length) return casa.urlsFotos[0]
    return imagenesCasas[index % imagenesCasas.length]
  }

  return (
    <main className="catalog-page">
      <section className="catalog-hero">
        <div className="hero-content">
          <h1>Casas rurales disponibles</h1>
          <p className="hero-copy">Encuentra las casas rurales que van mas contigo</p>

          <form className="hero-search" onSubmit={handleBuscar}>
            <label className="search-field search-field-wide">
              <span>Destino o codigo</span>
              <input
                type="text"
                placeholder="Poblacion o codigo de casa"
                value={termino}
                onChange={(event) => setTermino(event.target.value)}
              />
              {termino && (
                <button className="clear-field" type="button" onClick={() => setTermino('')}>
                  x
                </button>
              )}
            </label>

            <label className="search-field">
              <span>Entrada</span>
              <input
                type="date"
                value={fechaEntrada}
                onChange={(event) => setFechaEntrada(event.target.value)}
              />
            </label>

            <label className="search-field">
              <span>Salida</span>
              <input
                type="date"
                min={fechaEntrada || undefined}
                value={fechaSalida}
                onChange={(event) => setFechaSalida(event.target.value)}
              />
            </label>

            <label className="search-field search-field-compact">
              <span>Hab.</span>
              <input
                type="number"
                min="1"
                placeholder="Todas"
                value={habitaciones}
                onChange={(event) => setHabitaciones(event.target.value)}
              />
            </label>

            <button className="hero-search-button" type="submit" disabled={cargando}>
              {cargando ? 'Buscando...' : 'Buscar'}
            </button>
          </form>

          <div className="hero-options">
            <button className="reset-search" type="button" onClick={limpiarFiltros}>
              Ver todas
            </button>
          </div>
        </div>
      </section>

      <section className="catalog-layout">
        <section className="results-panel">
          <div className="credit-strip">
            <strong>Tu proxima escapada rural empieza aqui</strong>
            <span>{descripcionFiltros()}</span>
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
                    <span>Codigo {casa.codigoCasa}</span>
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
