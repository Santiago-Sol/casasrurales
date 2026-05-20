import { useEffect, useState } from 'react'
import '../styles/busqueda.css'
import { notificarSiEsError } from '../utils/notificaciones'

const imagenesCasas = [
  'https://images.unsplash.com/photo-1564013799919-ab600027ffc6?auto=format&fit=crop&w=900&q=80',
  'https://images.unsplash.com/photo-1518780664697-55e3ad937233?auto=format&fit=crop&w=900&q=80',
  'https://images.unsplash.com/photo-1505693416388-ac5ce068fe85?auto=format&fit=crop&w=900&q=80',
  'https://images.unsplash.com/photo-1600585154340-be6161a56a0c?auto=format&fit=crop&w=900&q=80'
]

export default function FavoritosCliente({ usuarioAutenticado, onRequireLogin }) {
  const [favoritos, setFavoritos] = useState([])
  const [cargando, setCargando] = useState(true)
  const [detalle, setDetalle] = useState(null)

  useEffect(() => {
    cargarFavoritos()
  }, [])

  const cargarFavoritos = async () => {
    setCargando(true)
    try {
      const response = await fetch('/api/cliente/favoritos', {
        credentials: 'include'
      })
      if (response.ok) {
        const data = await response.json()
        setFavoritos(data)
      } else {
        notificarSiEsError('No se pudieron cargar tus favoritos', 'error')
      }
    } catch (error) {
      console.error(error)
      notificarSiEsError('Error de conexión al cargar favoritos', 'error')
    } finally {
      setCargando(false)
    }
  }

  const removerFavorito = async (codigoCasa, event) => {
    event.stopPropagation()
    try {
      const response = await fetch(`/api/cliente/favoritos/${codigoCasa}`, {
        method: 'POST',
        credentials: 'include'
      })
      if (response.ok) {
        setFavoritos(prev => prev.filter(c => c.codigoCasa !== codigoCasa))
        notificarSiEsError('Casa eliminada de favoritos', 'exito')
      } else {
        notificarSiEsError('No se pudo quitar de favoritos', 'error')
      }
    } catch (error) {
      console.error(error)
      notificarSiEsError('Error de conexión', 'error')
    }
  }

  const verDetalle = async (codigoCasa) => {
    try {
      const response = await fetch(`/api/busqueda/${codigoCasa}`, {
        credentials: 'include'
      })
      if (response.ok) {
        const data = await response.json()
        setDetalle(data)
      } else {
        notificarSiEsError('No se pudo cargar el detalle de la casa', 'error')
      }
    } catch (error) {
      console.error(error)
    }
  }

  const imagenCasa = (casa, index = 0) => {
    if (casa.urlsFotos?.length) return normalizarUrlFoto(casa.urlsFotos[0])
    return imagenesCasas[index % imagenesCasas.length]
  }

  const normalizarUrlFoto = (url) => {
    if (!url) return ''
    if (/^https?:\/\//i.test(url)) return url
    if (url.startsWith('/')) return url
    return `/${url}`
  }

  const usarImagenAlterna = (event, index = 0) => {
    if (event.currentTarget.dataset.fallback === 'true') return
    event.currentTarget.dataset.fallback = 'true'
    event.currentTarget.src = imagenesCasas[index % imagenesCasas.length]
  }

  return (
    <main className="catalog-page">
      <section className="catalog-hero" style={{ minHeight: '160px', padding: '30px 20px' }}>
        <div className="hero-content">
          <h1>Mis Favoritos</h1>
          <p className="hero-copy">Tus casas rurales favoritas guardadas para planear tu escapada</p>
        </div>
      </section>

      <section className="catalog-layout">
        <section className="results-panel">
          {cargando ? (
            <div className="mensaje info">Cargando favoritos...</div>
          ) : favoritos.length === 0 ? (
            <div className="mensaje info">No tienes casas agregadas a tus favoritos. ¡Explora el catálogo y pulsa el corazón en las casas que te gusten!</div>
          ) : (
            <div className="property-grid">
              {favoritos.map((casa, index) => (
                <article className="property-card" key={casa.codigoCasa} style={{ position: 'relative' }}>
                  {/* Heart Icon to remove */}
                  <button 
                    className="fav-heart-btn active"
                    onClick={(e) => removerFavorito(casa.codigoCasa, e)}
                    aria-label="Quitar de favoritos"
                    title="Quitar de favoritos"
                  >
                    ❤️
                  </button>

                  <button className="image-button" onClick={() => verDetalle(casa.codigoCasa)}>
                    <img
                      src={imagenCasa(casa, index)}
                      alt={casa.nombrePropiedad}
                      onError={(event) => usarImagenAlterna(event, index)}
                    />
                    <span className="badge">Favorito</span>
                    <span className="preview-meta">
                      <span>{casa.poblacion}</span>
                      <span>Código {casa.codigoCasa}</span>
                    </span>
                  </button>
                  <div className="property-body">
                    <p className="location">{casa.poblacion}</p>
                    <h3>{casa.nombrePropiedad}</h3>
                    <p className="description">{casa.descripcionGeneral || 'Casa rural favorita.'}</p>
                    <div className="spec-row">
                      <span>{casa.numDormitorios} hab.</span>
                      <span>{casa.numBanos} baños</span>
                      <span>{casa.numCocinas} cocina</span>
                    </div>
                    <button className="contact-button" onClick={() => verDetalle(casa.codigoCasa)}>
                      Ver detalle
                    </button>
                  </div>
                </article>
              ))}
            </div>
          )}
        </section>
      </section>

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
              <h2>{detalle.nombrePropiedad}</h2>
              <p>{detalle.descripcionGeneral || 'Casa rural con espacios completos para descansar.'}</p>
              <div className="detail-specs">
                <span>{detalle.numDormitorios} habitaciones</span>
                <span>{detalle.numBanos} baños</span>
                <span>{detalle.numCocinas} cocinas</span>
                <span>{detalle.numPlazasGaraje} garajes</span>
              </div>
              <p className="owner">Teléfono: {detalle.telefonoPropietario}</p>
            </div>
          </section>
        </div>
      )}
    </main>
  )
}
