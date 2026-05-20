import { useEffect, useState } from 'react'
import '../styles/busqueda.css'

const imagenesCasas = [
  'https://images.unsplash.com/photo-1564013799919-ab600027ffc6?auto=format&fit=crop&w=900&q=80',
  'https://images.unsplash.com/photo-1518780664697-55e3ad937233?auto=format&fit=crop&w=900&q=80',
  'https://images.unsplash.com/photo-1505693416388-ac5ce068fe85?auto=format&fit=crop&w=900&q=80',
  'https://images.unsplash.com/photo-1600585154340-be6161a56a0c?auto=format&fit=crop&w=900&q=80'
]

export default function FavoritosCliente({ onVerCasas }) {
  const [favoritos, setFavoritos] = useState([])
  const [detalle, setDetalle] = useState(null)
  const [cargando, setCargando] = useState(true)
  const [cargandoDetalle, setCargandoDetalle] = useState(false)
  const [mensaje, setMensaje] = useState('')

  useEffect(() => {
    cargarFavoritos()
  }, [])

  const normalizarUrlFoto = (url) => {
    if (!url) return ''
    if (/^https?:\/\//i.test(url)) return url
    if (url.startsWith('/')) return url
    return `/${url}`
  }

  const imagenCasa = (casa, index = 0) => {
    if (casa.urlsFotos?.length) return normalizarUrlFoto(casa.urlsFotos[0])
    return imagenesCasas[index % imagenesCasas.length]
  }

  const usarImagenAlterna = (event, index = 0) => {
    if (event.currentTarget.dataset.fallback === 'true') return
    event.currentTarget.dataset.fallback = 'true'
    event.currentTarget.src = imagenesCasas[index % imagenesCasas.length]
  }

  const cargarFavoritos = async () => {
    setCargando(true)
    setMensaje('')
    try {
      const response = await fetch('/api/clientes/favoritos', { credentials: 'include' })
      if (!response.ok) {
        setMensaje('No fue posible cargar tus favoritos')
        return
      }

      const data = await response.json()
      setFavoritos(Array.isArray(data) ? data : [])
    } catch (error) {
      setMensaje('Error de conexion al cargar favoritos')
      console.error(error)
    } finally {
      setCargando(false)
    }
  }

  const quitarFavorito = async (codigoCasa) => {
    try {
      const response = await fetch(`/api/clientes/favoritos/${codigoCasa}`, {
        method: 'DELETE',
        credentials: 'include'
      })

      if (!response.ok) {
        setMensaje('No fue posible quitar la casa de favoritos')
        return
      }

      setFavoritos((actuales) => actuales.filter((casa) => casa.codigoCasa !== codigoCasa))
      if (detalle?.codigoCasa === codigoCasa) {
        setDetalle(null)
      }
    } catch (error) {
      setMensaje('Error de conexion con favoritos')
      console.error(error)
    }
  }

  const verDetalle = async (codigoCasa) => {
    setCargandoDetalle(true)
    setMensaje('')
    try {
      const response = await fetch(`/api/busqueda/${codigoCasa}`, {
        credentials: 'include'
      })

      if (response.status === 404) {
        setMensaje('No encontramos el detalle de esta casa')
        return
      }

      if (!response.ok) {
        setMensaje('No fue posible cargar el detalle de la casa')
        return
      }

      const data = await response.json()
      setDetalle(data)
    } catch (error) {
      setMensaje('Error de conexion al cargar el detalle')
      console.error(error)
    } finally {
      setCargandoDetalle(false)
    }
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
    <section className="favorites-page">
      <div className="favorites-header">
        <div>
          <p>Casas guardadas</p>
          <h1>Mis favoritos</h1>
        </div>
        <button type="button" onClick={onVerCasas}>Ver casas</button>
      </div>

      {mensaje && <div className="mensaje error">{mensaje}</div>}
      {cargando && <div className="mensaje info">Cargando favoritos...</div>}

      {!cargando && favoritos.length === 0 ? (
        <div className="favorites-empty">
          <h2>Aun no tienes casas favoritas</h2>
          <p>Usa el corazon en las tarjetas para armar tu lista de proximas escapadas.</p>
          <button type="button" onClick={onVerCasas}>Explorar casas</button>
        </div>
      ) : (
        <div className="property-grid favorites-grid">
          {favoritos.map((casa, index) => (
            <article className="property-card" key={casa.codigoCasa}>
              <div className="image-button">
                <img
                  src={imagenCasa(casa, index)}
                  alt={casa.nombrePropiedad}
                  onError={(event) => usarImagenAlterna(event, index)}
                />
                <span className="badge">Favorita</span>
                <span className="preview-meta">
                  <span>{casa.poblacion}</span>
                  <span>Codigo {casa.codigoCasa}</span>
                </span>
              </div>
              <div className="property-body">
                <p className="location">{casa.poblacion}</p>
                <h3>{casa.nombrePropiedad}</h3>
                <p className="description">{casa.descripcionGeneral || 'Casa rural lista para una estadia tranquila.'}</p>
                <div className="spec-row">
                  <span>{casa.numDormitorios} hab.</span>
                  <span>{casa.numBanos} banos</span>
                  <span>{casa.numCocinas} cocina</span>
                </div>
                <div className="favorite-card-actions">
                  <button
                    className="contact-button"
                    type="button"
                    onClick={() => verDetalle(casa.codigoCasa)}
                    disabled={cargandoDetalle}
                  >
                    {cargandoDetalle ? 'Cargando...' : 'Ver detalle'}
                  </button>
                  <button className="contact-button secondary-action" type="button" onClick={() => quitarFavorito(casa.codigoCasa)}>
                    Quitar de favoritos
                  </button>
                </div>
              </div>
            </article>
          ))}
        </div>
      )}

      {detalle && (
        <div className="detail-overlay" onClick={() => setDetalle(null)}>
          <section className="detail-modal" onClick={(event) => event.stopPropagation()}>
            <button className="close-detail" onClick={() => setDetalle(null)}>x</button>
            <img
              src={imagenCasa(detalle)}
              alt={detalle.nombrePropiedad}
              onError={(event) => usarImagenAlterna(event)}
            />
            <div className="detail-content">
              <p className="location">{detalle.poblacion}</p>
              <div className="detail-title-row">
                <h2>{detalle.nombrePropiedad}</h2>
                <span className="favorite-detail-button active">Favorita</span>
              </div>
              <p>{detalle.descripcionGeneral || 'Casa rural con espacios completos para descansar.'}</p>
              <div className="detail-specs">
                <span>{detalle.numDormitorios} habitaciones</span>
                <span>{detalle.numBanos} banos</span>
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
                            <strong>Habitacion {habitacion.codigoHabitacion || index + 1}</strong>
                            <p>
                              {habitacion.numeroCamas} {habitacion.numeroCamas === 1 ? 'cama' : 'camas'} - {formatearEnum(habitacion.tipoCama)}
                            </p>
                          </div>
                          <span className={`feature-pill ${habitacion.tieneBano ? 'feature-pill-ok' : ''}`}>
                            {habitacion.tieneBano ? 'Con bano' : 'Sin bano'}
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
                    <h3>Banos</h3>
                    <span>{banosDetalle.length}</span>
                  </div>
                  {banosDetalle.length > 0 ? (
                    <div className="bath-list">
                      {banosDetalle.map((bano, index) => (
                        <article className="bath-item" key={`bano-${index}`}>
                          <strong>Bano {index + 1}</strong>
                          <p>{bano.observaciones || 'Sin observaciones adicionales.'}</p>
                        </article>
                      ))}
                    </div>
                  ) : (
                    <p className="detail-empty">No hay banos detallados para esta casa.</p>
                  )}
                </section>
              </div>

              <p className="owner">Propietario: {detalle.nombrePropietario || 'No disponible'}</p>
              <p className="owner">Telefono: {detalle.telefonoPropietario || 'No disponible'}</p>
            </div>
          </section>
        </div>
      )}
    </section>
  )
}
