import { useState } from 'react'
import '../styles/formulario.css'

export default function Login({ onLoginSuccess, onRegistroClick, onVolver }) {
  const [tipoUsuario, setTipoUsuario] = useState('cliente')
  const [usuario, setUsuario] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [cargando, setCargando] = useState(false)

  const handleSubmit = async (event) => {
    event.preventDefault()
    setError('')
    setCargando(true)

    try {
      const esCliente = tipoUsuario === 'cliente'
      const response = await fetch(esCliente ? '/auth/login/cliente' : '/auth/login/propietario', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'same-origin',
        body: JSON.stringify(
          esCliente
            ? { email: usuario, contrasena: password }
            : { nombreCuenta: usuario, contrasena: password }
        )
      })

      if (!response.ok) {
        const errorData = await response.json().catch(() => ({}))
        throw new Error(errorData.error || 'Error en las credenciales')
      }

      const datos = await response.json()
      const nombreUsuario = esCliente ? datos.email : datos.nombreCuenta
      const usuarioSesion = {
        idUsuario: datos.idUsuario,
        nombreUsuario,
        tipoUsuario
      }

      localStorage.setItem('usuarioAutenticado', JSON.stringify(usuarioSesion))
      onLoginSuccess(usuarioSesion)
    } catch (err) {
      setError(err.message || 'Error en el inicio de sesion')
    } finally {
      setCargando(false)
    }
  }

  return (
    <div className="login-container">
      <div className="login-box">
        {onVolver && (
          <button type="button" className="back-to-catalog" onClick={onVolver}>
            Volver a casas
          </button>
        )}
        <h1>Casas Rurales</h1>
        <h2>Iniciar sesion</h2>

        {error && <div className="error-message">{error}</div>}

        <form onSubmit={handleSubmit} autoComplete="off">
          <input type="text" hidden autoComplete="off" value={usuario} onChange={() => {}} />
          <input type="password" hidden autoComplete="off" onChange={() => {}} />

          <div className="form-group">
            <label>Tipo de usuario</label>
            <select
              value={tipoUsuario}
              onChange={(event) => {
                setTipoUsuario(event.target.value)
                setError('')
              }}
            >
              <option value="cliente">Cliente</option>
              <option value="propietario">Propietario</option>
            </select>
          </div>

          <div className="form-group">
            <label>{tipoUsuario === 'cliente' ? 'Email' : 'Nombre de cuenta'}</label>
            <input
              type="text"
              placeholder={tipoUsuario === 'cliente' ? 'cliente@correo.com' : 'usuario propietario'}
              value={usuario}
              onChange={(event) => setUsuario(event.target.value)}
              autoComplete="off"
              required
            />
          </div>

          <div className="form-group">
            <label>Contrasena</label>
            <input
              type="password"
              placeholder="Ingrese su contrasena"
              value={password}
              onChange={(event) => setPassword(event.target.value)}
              autoComplete="off"
              required
            />
          </div>

          <button type="submit" className="btn-primary" disabled={cargando}>
            {cargando ? 'Iniciando sesion...' : 'Iniciar sesion'}
          </button>
        </form>

        <div className="login-links">
          <p>No tienes cuenta?</p>
          <div className="registro-buttons">
            <button
              type="button"
              className="btn-link cliente"
              onClick={() => onRegistroClick('registro-cliente')}
            >
              Registrarse como cliente
            </button>
            <button
              type="button"
              className="btn-link propietario"
              onClick={() => onRegistroClick('registro-propietario')}
            >
              Registrarse como propietario
            </button>
          </div>
        </div>
      </div>
    </div>
  )
}
