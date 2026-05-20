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
  const [cargando, setCargando] = useState(true)
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
      setMensaje('Error de conexión al cargar favoritos')
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
    } catch (error) {
      setMensaje('Error de conexión con favoritos')
      console.error(error)
    }
  }

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
          <h2>Aún no tienes casas favoritas</h2>
          <p>Usa el corazón en las tarjetas para armar tu lista de próximas escapadas.</p>
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
                  <span>Código {casa.codigoCasa}</span>
                </span>
              </div>
              <div className="property-body">
                <p className="location">{casa.poblacion}</p>
                <h3>{casa.nombrePropiedad}</h3>
                <p className="description">{casa.descripcionGeneral || 'Casa rural lista para una estadía tranquila.'}</p>
                <div className="spec-row">
                  <span>{casa.numDormitorios} hab.</span>
                  <span>{casa.numBanos} baños</span>
                  <span>{casa.numCocinas} cocina</span>
                </div>
                <button className="contact-button secondary-action" type="button" onClick={() => quitarFavorito(casa.codigoCasa)}>
                  Quitar de favoritos
                </button>
              </div>
            </article>
          ))}
        </div>
      )}
    </section>
  )
}
