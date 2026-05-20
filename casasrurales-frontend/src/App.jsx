import { useEffect, useState } from 'react'
import './App.css'
import Login from './components/Login'
import RegistroPropietario from './components/RegistroPropietario'
import RegistroCliente from './components/RegistroCliente'
import BusquedaCasas from './components/BusquedaCasas'
import DashboardPropietario from './components/DashboardPropietario'
import MisReservasCliente from './components/MisReservasCliente'

function App() {
  const [seccionActiva, setSeccionActiva] = useState('busqueda')
  const [usuarioAutenticado, setUsuarioAutenticado] = useState(null)
  const [notificacion, setNotificacion] = useState(null)

  useEffect(() => {
    const validarSesion = async () => {
      const usuarioGuardado = localStorage.getItem('usuarioAutenticado')
      if (!usuarioGuardado) return

      try {
        const usuario = JSON.parse(usuarioGuardado)
        const response = await fetch('/auth/me', { credentials: 'include' })

        if (!response.ok) {
          localStorage.removeItem('usuarioAutenticado')
          setUsuarioAutenticado(null)
          setSeccionActiva('busqueda')
          return
        }

        setUsuarioAutenticado(usuario)
        setSeccionActiva(usuario.tipoUsuario === 'propietario' ? 'dashboard-propietario' : 'busqueda')
      } catch (error) {
        localStorage.removeItem('usuarioAutenticado')
        setUsuarioAutenticado(null)
        setSeccionActiva('busqueda')
      }
    }

    validarSesion()
  }, [])

  useEffect(() => {
    const mostrarNotificacion = (event) => {
      const detalle = event.detail || {}
      if (!detalle.mensaje) return

      setNotificacion({
        mensaje: detalle.mensaje,
        tipo: detalle.tipo || 'error'
      })
    }

    window.addEventListener('app-notificacion', mostrarNotificacion)
    return () => window.removeEventListener('app-notificacion', mostrarNotificacion)
  }, [])

  useEffect(() => {
    if (!notificacion) return undefined
    const timeout = setTimeout(() => setNotificacion(null), 6500)
    return () => clearTimeout(timeout)
  }, [notificacion])

  const handleLoginSuccess = (datosUsuario) => {
    setUsuarioAutenticado(datosUsuario)
    setSeccionActiva(datosUsuario.tipoUsuario === 'propietario' ? 'dashboard-propietario' : 'busqueda')
  }

  const handleRegistroExitoso = () => {
    setSeccionActiva('login')
  }

  const handleLogout = async () => {
    try {
      await fetch('/auth/logout', {
        method: 'POST',
        credentials: 'same-origin'
      })
    } catch (error) {
      console.error('No se pudo cerrar la sesion en el servidor', error)
    } finally {
      localStorage.removeItem('usuarioAutenticado')
      setUsuarioAutenticado(null)
      setSeccionActiva('busqueda')
    }
  }

  const esPropietario = usuarioAutenticado?.tipoUsuario === 'propietario'
  const esCliente = usuarioAutenticado && String(usuarioAutenticado.tipoUsuario).toLowerCase() === 'cliente'

  const renderContenido = () => {
    if (seccionActiva === 'login') {
      return (
        <Login
          onLoginSuccess={handleLoginSuccess}
          onRegistroClick={setSeccionActiva}
          onVolver={() => setSeccionActiva('busqueda')}
        />
      )
    }

    if (seccionActiva === 'registro-propietario') {
      return (
        <main className="main-content main-content-form registro-main">
          <RegistroPropietario
            onRegistroExitoso={handleRegistroExitoso}
            onVolver={() => setSeccionActiva('login')}
          />
        </main>
      )
    }

    if (seccionActiva === 'registro-cliente') {
      return (
        <main className="main-content main-content-form registro-main">
          <RegistroCliente
            onRegistroExitoso={handleRegistroExitoso}
            onVolver={() => setSeccionActiva('login')}
          />
        </main>
      )
    }

    if (seccionActiva === 'dashboard-propietario' && esPropietario) {
      return (
        <main className="main-content">
          <DashboardPropietario />
        </main>
      )
    }

    if (seccionActiva === 'mis-reservas' && esCliente) {
      return (
        <main className="main-content">
          <MisReservasCliente />
        </main>
      )
    }

    return <BusquedaCasas 
             usuarioAutenticado={usuarioAutenticado} 
             onRequireLogin={() => setSeccionActiva('login')} 
             onAuthExpired={() => {
               localStorage.removeItem('usuarioAutenticado')
               setUsuarioAutenticado(null)
               setSeccionActiva('login')
             }}
           />
  }

  return (
    <div className="app">
      {seccionActiva !== 'login' && (
        <nav className="navbar">
          <div className="navbar-container">
            <button className="brand" onClick={() => setSeccionActiva('busqueda')}>
              <span className="brand-mark">CR</span>
              <span>
                <strong>Casas Rurales</strong>
                <small>Armenia y Quindio</small>
              </span>
            </button>

            <div className="nav-actions">
              <button
                className={seccionActiva === 'busqueda' ? 'nav-button active' : 'nav-button'}
                onClick={() => setSeccionActiva('busqueda')}
              >
                Casas
              </button>
              {esPropietario && (
                <button
                  className={seccionActiva === 'dashboard-propietario' ? 'nav-button active' : 'nav-button'}
                  onClick={() => setSeccionActiva('dashboard-propietario')}
                >
                  Mi dashboard
                </button>
              )}
              {esCliente && (
                <button
                  className={seccionActiva === 'mis-reservas' ? 'nav-button active' : 'nav-button'}
                  onClick={() => setSeccionActiva('mis-reservas')}
                >
                  Mis Reservas
                </button>
              )}
              {usuarioAutenticado ? (
                <button
                  onClick={handleLogout}
                  className="session-button"
                  title={`Cerrar sesion (${usuarioAutenticado?.nombreUsuario})`}
                >
                  Salir
                </button>
              ) : (
                <button className="session-button" onClick={() => setSeccionActiva('login')}>
                  Ingresar
                </button>
              )}
            </div>
          </div>
        </nav>
      )}

      {renderContenido()}

      {notificacion && (
        <div className="global-notification-layer" role="alert" aria-live="assertive">
          <div className={`global-notification ${notificacion.tipo}`}>
            <div>
              <strong>{notificacion.tipo === 'exito' ? 'Operacion realizada' : 'Atencion'}</strong>
              <p>{notificacion.mensaje}</p>
            </div>
            <button type="button" onClick={() => setNotificacion(null)} aria-label="Cerrar notificacion">
              x
            </button>
          </div>
        </div>
      )}
    </div>
  )
}

export default App
