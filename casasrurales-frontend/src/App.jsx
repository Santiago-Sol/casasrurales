import { useEffect, useState } from 'react'
import './App.css'
import Login from './components/Login'
import RegistroPropietario from './components/RegistroPropietario'
import RegistroCliente from './components/RegistroCliente'
import BusquedaCasas from './components/BusquedaCasas'
import DashboardPropietario from './components/DashboardPropietario'

function App() {
  const [seccionActiva, setSeccionActiva] = useState('busqueda')
  const [usuarioAutenticado, setUsuarioAutenticado] = useState(null)

  useEffect(() => {
    const usuarioGuardado = localStorage.getItem('usuarioAutenticado')
    if (usuarioGuardado) {
      const usuario = JSON.parse(usuarioGuardado)
      setUsuarioAutenticado(usuario)
      setSeccionActiva(usuario.tipoUsuario === 'propietario' ? 'dashboard-propietario' : 'busqueda')
    }
  }, [])

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
        <main className="main-content main-content-form">
          <RegistroPropietario
            onRegistroExitoso={handleRegistroExitoso}
            onVolver={() => setSeccionActiva('login')}
          />
        </main>
      )
    }

    if (seccionActiva === 'registro-cliente') {
      return (
        <main className="main-content main-content-form">
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

    return <BusquedaCasas />
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
    </div>
  )
}

export default App
