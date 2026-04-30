import { useState } from 'react'
import '../styles/formulario.css'

export default function Login({ onLoginSuccess, onRegistroClick }) {
  const [tipoUsuario, setTipoUsuario] = useState('cliente')
  const [usuario, setUsuario] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [cargando, setCargando] = useState(false)

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    setCargando(true)

    try {
      let response
      let datos

      if (tipoUsuario === 'cliente') {
        // Login para clientes - usa email (etiquetado como usuario)
        response = await fetch('/auth/login/cliente', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          credentials: 'same-origin',
          body: JSON.stringify({ 
            email: usuario,
            contrasena: password
          })
        })

        if (!response.ok) {
          const errorData = await response.json()
          throw new Error(errorData.error || 'Error en las credenciales')
        }

        datos = await response.json()
        datos.nombreUsuario = datos.email
      } else {
        // Login para propietarios
        response = await fetch('/auth/login/propietario', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          credentials: 'same-origin',
          body: JSON.stringify({ 
            nombreCuenta: usuario,
            contrasena: password
          })
        })

        if (!response.ok) {
          const errorData = await response.json()
          throw new Error(errorData.error || 'Error en las credenciales')
        }

        datos = await response.json()
        datos.nombreUsuario = datos.nombreCuenta
      }

      // Guardar en localStorage
      localStorage.setItem('usuarioAutenticado', JSON.stringify({
        idUsuario: datos.idUsuario,
        nombreUsuario: datos.nombreUsuario,
        tipoUsuario
      }))

      onLoginSuccess({ 
        idUsuario: datos.idUsuario, 
        nombreUsuario: datos.nombreUsuario, 
        tipoUsuario 
      })
    } catch (err) {
      setError(err.message || 'Error en el inicio de sesión')
    } finally {
      setCargando(false)
    }
  }

  return (
    <div className="login-container">
      <div className="login-box">
        <h1>Casas Rurales</h1>
        <h2>Iniciar Sesión</h2>

        {error && (
          <div className="error-message">
            ⚠️ {error}
          </div>
        )}

        <form onSubmit={handleSubmit} autoComplete="off">
          {/* Campos ocultos para prevenir diálogo de autenticación del navegador */}
          <input type="text" style={{display: 'none'}} autoComplete="off" value={usuario} onChange={() => {}} />
          <input type="password" style={{display: 'none'}} autoComplete="off" onChange={() => {}} />
          
          <div className="form-group">
            <label>Tipo de Usuario</label>
            <select 
              value={tipoUsuario} 
              onChange={(e) => {
                setTipoUsuario(e.target.value)
                setError('')
              }}
            >
              <option value="cliente">Cliente</option>
              <option value="propietario">Propietario</option>
            </select>
          </div>

          <div className="form-group">
            <label>
              {tipoUsuario === 'cliente' ? 'Usuario' : 'Usuario'}
            </label>
            <input
              type="text"
              placeholder={tipoUsuario === 'cliente' ? 'Ingrese su usuario' : 'Ingrese su usuario'}
              value={usuario}
              onChange={(e) => setUsuario(e.target.value)}
              autoComplete="off"
              required
            />
          </div>

          <div className="form-group">
            <label>Contraseña</label>
            <input
              type="password"
              placeholder="Ingrese su contraseña"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              autoComplete="off"
              required
            />
          </div>

          <button 
            type="submit" 
            className="btn-primary"
            disabled={cargando}
          >
            {cargando ? 'Iniciando sesión...' : 'Iniciar Sesión'}
          </button>
        </form>

        <div className="login-links">
          <p>¿No tienes cuenta?</p>
          <div className="registro-buttons">
            <button 
              type="button"
              className="btn-link cliente"
              onClick={() => onRegistroClick('registro-cliente')}
            >
              Registrarse como Cliente
            </button>
            <button 
              type="button"
              className="btn-link propietario"
              onClick={() => onRegistroClick('registro-propietario')}
            >
              Registrarse como Propietario
            </button>
          </div>
        </div>
      </div>
    </div>
  )
}
