import { useState } from 'react'
import '../styles/formulario.css'

export default function RegistroCliente({ onRegistroExitoso, onVolver }) {
  const [formulario, setFormulario] = useState({
    email: '',
    telefono: '',
    password: ''
  })
  const [mensaje, setMensaje] = useState('')
  const [cargando, setCargando] = useState(false)

  const handleChange = (e) => {
    const { name, value } = e.target
    setFormulario(prev => ({
      ...prev,
      [name]: value
    }))
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    setCargando(true)
    setMensaje('')

    try {
      const response = await fetch('/auth/registro/cliente', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(formulario)
      })

      const data = await response.json()

      if (response.ok) {
        setMensaje('✅ Registro exitoso. Redirigiendo...')
        setFormulario({
          email: '',
          telefono: '',
          password: ''
        })
        // Redirigir inmediatamente sin delay
        onRegistroExitoso()
      } else {
        setMensaje(`❌ ${data.error || 'Error en el registro'}`)
      }
    } catch (error) {
      setMensaje(' Error de conexión con el servidor')
      console.error(error)
    } finally {
      setCargando(false)
    }
  }

  return (
    <div className="formulario-container">
      <div className="formulario-container-card">
        <h2>Registrarse como Cliente</h2>
        <form onSubmit={handleSubmit} className="formulario" autoComplete="on">
          {/* Campos ocultos para prevenir diálogo de autenticación del navegador */}
          <input type="text" style={{display: 'none'}} autoComplete="username" value={formulario.email} onChange={() => {}} />
          <input type="password" style={{display: 'none'}} autoComplete="current-password" onChange={() => {}} />
          
        <div className="form-group">
          <label htmlFor="email">Email:</label>
          <input
            type="email"
            id="email"
            name="email"
            value={formulario.email}
            onChange={handleChange}
            required
          />
        </div>

        <div className="form-group">
          <label htmlFor="telefono">Teléfono:</label>
          <input
            type="tel"
            id="telefono"
            name="telefono"
            value={formulario.telefono}
            onChange={handleChange}
            required
          />
        </div>

        <div className="form-group">
          <label htmlFor="password">Contraseña:</label>
          <input
            type="password"
            id="password"
            name="password"
            value={formulario.password}
            onChange={handleChange}
            autoComplete="off"
            required
          />
        </div>

        <button type="submit" disabled={cargando}>
          {cargando ? 'Registrando...' : 'Registrar'}
        </button>

        <button type="button" className="btn-volver" onClick={onVolver}>
          ← Volver al Login
        </button>
      </form>

      {mensaje && <div className={`mensaje ${mensaje.includes('') ? 'exito' : 'error'}`}>
        {mensaje}
      </div>}
      </div>
    </div>
  )
}
