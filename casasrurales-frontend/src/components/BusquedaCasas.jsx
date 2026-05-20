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

const TAMANO_PAGINA = 6

const obtenerFechaLocalISO = (fecha = new Date()) => {
  const anio = fecha.getFullYear()
  const mes = String(fecha.getMonth() + 1).padStart(2, '0')
  const dia = String(fecha.getDate()).padStart(2, '0')
  return `${anio}-${mes}-${dia}`
}

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
  const [paginaActual, setPaginaActual] = useState(0)
  const [totalPaginas, setTotalPaginas] = useState(1)
  const [totalResultados, setTotalResultados] = useState(0)
  const hoyISO = useMemo(() => obtenerFechaLocalISO(), [])

  const casasOrdenadas = useMemo(
    () => {
      return [...resultados]
        .sort((a, b) => a.nombrePropiedad.localeCompare(b.nombrePropiedad))
    },
    [resultados]
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
    setTotalResultados(0)
    setTotalPaginas(1)
  }

  const filtrarHabitacionesEnCliente = (casas) => {
    const habitacionesMinimas = Number(habitaciones) || 0
    if (!habitacionesMinimas) return casas
    return casas.filter((casa) => casa.numDormitorios >= habitacionesMinimas)
  }

  const aplicarResultadoBusqueda = (data, paginaSolicitada = 0) => {
    if (Array.isArray(data)) {
      const casas = filtrarHabitacionesEnCliente(data)
      setResultados(casas)
      setPaginaActual(0)
      setTotalResultados(casas.length)
      setTotalPaginas(1)
      return casas
    }

    const casas = Array.isArray(data?.contenido) ? data.contenido : []
    setResultados(casas)
    setPaginaActual(Number.isInteger(data?.pagina) ? data.pagina : paginaSolicitada)
    setTotalResultados(Number.isFinite(data?.totalElementos) ? data.totalElementos : casas.length)
    setTotalPaginas(Math.max(1, Number.isFinite(data?.totalPaginas) ? data.totalPaginas : 1))
    return casas
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
    if (habitaciones) filtros.push(`${habitaciones} habitación${Number(habitaciones) === 1 ? '' : 'es'}`)
    return filtros.length ? filtros.join(' - ') : 'Todas las casas disponibles'
  }

  const cargarCasasDisponibles = async (silencioso = false, pagina = 0) => {
    const poblacionOCodigo = termino.trim()
    const esCodigo = /^\d+$/.test(poblacionOCodigo)
    const noches = calcularNoches()

    if (fechaEntrada && fechaEntrada < hoyISO) {
      mostrarMensaje('La fecha de entrada no puede ser anterior a hoy', 'info')
      return
    }

    if (fechaSalida && !fechaEntrada) {
      mostrarMensaje('Selecciona una fecha de entrada', 'info')
      return
    }

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
      params.set('pagina', String(pagina))
      params.set('tamano', String(TAMANO_PAGINA))
      if (poblacionOCodigo) params.set('poblacion', poblacionOCodigo)
      if (habitaciones) params.set('habitaciones', habitaciones)
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
            const casasDisponiblesBase = fechaEntrada && noches > 0
              ? (await Promise.all(data.map(async (casa) => ({
                  casa,
                  disponible: await consultarDisponibilidadCasa(casa)
                })))).filter(({ disponible }) => disponible).map(({ casa }) => casa)
              : data
            const casasDisponibles = filtrarHabitacionesEnCliente(casasDisponiblesBase)

            setResultados(casasDisponibles)
            setPaginaActual(0)
            setTotalResultados(casasDisponibles.length)
            setTotalPaginas(1)
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
      const casas = aplicarResultadoBusqueda(data, pagina)
      if (!silencioso) {
        const total = Array.isArray(data) ? casas.length : (Number.isFinite(data?.totalElementos) ? data.totalElementos : casas.length)
        mostrarMensaje(`${total} casas rurales encontradas`, 'exito')
      }
    } catch (error) {
      mostrarMensaje('Error de conexion con el servidor', 'error')
      console.error(error)
    } finally {
      setCargando(false)
    }
  }

  const cargarTodasLasCasas = async (pagina = 0) => {
    setCargando(true)
    setMensaje('')
    limpiarVista()

    try {
      const params = new URLSearchParams({
        pagina: String(pagina),
        tamano: String(TAMANO_PAGINA)
      })
      const response = await fetch(`/api/busqueda?${params.toString()}`, { credentials: 'include' })

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
      aplicarResultadoBusqueda(data, pagina)
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
      mostrarMensaje('Ingresa el código de la casa', 'info')
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
        mostrarMensaje('No encontramos una casa con ese código', 'info')
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
      setTotalResultados(0)
      setTotalPaginas(1)
      setPaginaActual(0)
      mostrarMensaje('La casa existe, pero no cumple los filtros seleccionados', 'info')
      return
    }

    setResultados([casa])
    setTotalResultados(1)
    setTotalPaginas(1)
    setPaginaActual(0)
    setDetalle(null)
    mostrarMensaje('Casa encontrada', 'exito')
  }

  const handleBuscar = (event) => {
    event?.preventDefault()
    cargarCasasDisponibles(false, 0)
  }

  const manejarCambioFechaEntrada = (event) => {
    const nuevaFecha = event.target.value
    setFechaEntrada(nuevaFecha)
    if (fechaSalida && nuevaFecha && fechaSalida <= nuevaFecha) {
      setFechaSalida('')
    }
  }

  const manejarCambioFechaSalida = (event) => {
    setFechaSalida(event.target.value)
  }

  const limpiarFiltros = () => {
    setTermino('')
    setFechaEntrada('')
    setFechaSalida('')
    setHabitaciones('')
    setMensaje('')
    cargarTodasLasCasas(0)
  }

  const irAPagina = (pagina) => {
    const paginaSegura = Math.max(0, Math.min(pagina, totalPaginas - 1))
    if (paginaSegura === paginaActual || cargando) return
    if (termino.trim() || fechaEntrada || fechaSalida || habitaciones) {
      cargarCasasDisponibles(true, paginaSegura)
    } else {
      cargarTodasLasCasas(paginaSegura)
    }
  }

  const imagenCasa = (casa, index = 0) => {
    if (casa.urlsFotos?.length) return casa.urlsFotos[0]
    return imagenesCasas[index % imagenesCasas.length]
  }

  const formatearEnum = (valor) => {
    if (!valor) return 'Sin especificar'
    return String(valor)
      .toLowerCase()
      .replaceAll('_', ' ')
      .replace(/\b\w/g, (letra) => letra.toUpperCase())
  }

  const habitacionesDetalle = Array.isArray(detalle?.habitaciones) ? detalle.habitaciones : []
  const cocinasDetalle = Array.isArray(detalle?.cocinas) ? detalle.cocinas : []
  const banosDetalle = Array.isArray(detalle?.banos) ? detalle.banos : []

  return (
    <main className="catalog-page">
      <section className="catalog-hero">
        <div className="hero-content">
          <h1>Casas rurales disponibles</h1>
          <p className="hero-copy">Encuentra las casas rurales que van mas contigo</p>

          <form className="hero-search" onSubmit={handleBuscar}>
            <label className="search-field search-field-wide">
              <span>Destino o código</span>
              <input
                type="text"
                placeholder="Población o código de casa"
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
                min={hoyISO}
                value={fechaEntrada}
                onChange={manejarCambioFechaEntrada}
              />
            </label>

            <label className="search-field">
              <span>Salida</span>
              <input
                type="date"
                min={fechaEntrada || hoyISO}
                value={fechaSalida}
                onChange={manejarCambioFechaSalida}
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
            <strong>Tu próxima escapada rural empieza aquí</strong>
            <span>{descripcionFiltros()}</span>
          </div>

          {mensaje && <div className={`mensaje ${tipoMensaje}`}>{mensaje}</div>}

          <div className="results-toolbar">
            <h2>{totalResultados || 0} casas disponibles</h2>
            <select aria-label="Ordenar resultados">
              <option>Ordenar por recomendadas</option>
              <option>Nombre A-Z</option>
              <option>Más habitaciones</option>
            </select>
          </div>

          <div className="property-grid">
            {casasOrdenadas.map((casa, index) => (
              <article className="property-card" key={casa.codigoCasa}>
                <button className="image-button" onClick={() => buscarPorCodigo(casa.codigoCasa)}>
                  <img src={imagenCasa(casa, index)} alt={casa.nombrePropiedad} />
                  <span className="badge">Disponible</span>
                  <span className="badge badge-yellow">Rural</span>
                  <span className="preview-meta">
                    <span>{casa.poblacion}</span>
                    <span>Código {casa.codigoCasa}</span>
                  </span>
                </button>
                <div className="property-body">
                  <p className="location">{casa.poblacion}</p>
                  <h3>{casa.nombrePropiedad}</h3>
                  <p className="description">{casa.descripcionGeneral || 'Casa rural lista para una estadia tranquila.'}</p>
                  <div className="spec-row">
                    <span>{casa.numDormitorios} hab.</span>
                    <span>{casa.numBanos} baños</span>
                    <span>{casa.numCocinas} cocina</span>
                  </div>
                  <button className="contact-button" onClick={() => buscarPorCodigo(casa.codigoCasa)}>
                    Ver detalle
                  </button>
                </div>
              </article>
            ))}
          </div>

          {totalPaginas > 1 && (
            <div className="pagination-bar">
              <button type="button" onClick={() => irAPagina(paginaActual - 1)} disabled={paginaActual === 0 || cargando}>
                Anterior
              </button>
              <span>
                Página {paginaActual + 1} de {totalPaginas} · {totalResultados} casas
              </span>
              <button type="button" onClick={() => irAPagina(paginaActual + 1)} disabled={paginaActual >= totalPaginas - 1 || cargando}>
                Siguiente
              </button>
            </div>
          )}
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
                <span>{detalle.numBanos} baños</span>
                <span>{detalle.numCocinas} cocinas</span>
                <span>{detalle.numPlazasGaraje} garajes</span>
              </div>

              <div className="detail-sections">
                <section className="detail-section">
                  <div className="detail-section-title">
                    <h3>Habitaciones</h3>
                    <span>{habitacionesDetalle.length}</span>
                  </div>
                  {habitacionesDetalle.length > 0 ? (
                    <div className="detail-list">
                      {habitacionesDetalle.map((habitacion, index) => (
                        <article className="detail-item" key={habitacion.idHabitacion || habitacion.codigoHabitacion || index}>
                          <div>
                            <strong>Habitación {habitacion.codigoHabitacion || index + 1}</strong>
                            <p>
                              {habitacion.numeroCamas} {habitacion.numeroCamas === 1 ? 'cama' : 'camas'} - {formatearEnum(habitacion.tipoCama)}
                            </p>
                          </div>
                          <span className={`feature-pill ${habitacion.tieneBano ? 'feature-pill-ok' : ''}`}>
                            {habitacion.tieneBano ? 'Con baño' : 'Sin baño'}
                          </span>
                        </article>
                      ))}
                    </div>
                  ) : (
                    <p className="detail-empty">No hay habitaciones detalladas para esta casa.</p>
                  )}
                </section>

                <section className="detail-section">
                  <div className="detail-section-title">
                    <h3>Cocinas</h3>
                    <span>{cocinasDetalle.length}</span>
                  </div>
                  {cocinasDetalle.length > 0 ? (
                    <div className="detail-list">
                      {cocinasDetalle.map((cocina, index) => (
                        <article className="detail-item detail-item-stacked" key={`cocina-${index}`}>
                          <strong>Cocina {index + 1}</strong>
                          <div className="feature-row">
                            <span className={`feature-pill ${cocina.tieneLavavajillas ? 'feature-pill-ok' : ''}`}>
                              {cocina.tieneLavavajillas ? 'Lavavajillas' : 'Sin lavavajillas'}
                            </span>
                            <span className={`feature-pill ${cocina.tieneLavadora ? 'feature-pill-ok' : ''}`}>
                              {cocina.tieneLavadora ? 'Lavadora' : 'Sin lavadora'}
                            </span>
                          </div>
                        </article>
                      ))}
                    </div>
                  ) : (
                    <p className="detail-empty">No hay cocinas detalladas para esta casa.</p>
                  )}
                </section>

                <section className="detail-section detail-section-wide">
                  <div className="detail-section-title">
                    <h3>Baños</h3>
                    <span>{banosDetalle.length}</span>
                  </div>
                  {banosDetalle.length > 0 ? (
                    <div className="bath-list">
                      {banosDetalle.map((bano, index) => (
                        <article className="bath-item" key={`bano-${index}`}>
                          <strong>Baño {index + 1}</strong>
                          <p>{bano.observaciones || 'Sin observaciones adicionales.'}</p>
                        </article>
                      ))}
                    </div>
                  ) : (
                    <p className="detail-empty">No hay baños detallados para esta casa.</p>
                  )}
                </section>
              </div>
              <p className="owner">Teléfono: {detalle.telefonoPropietario}</p>
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
