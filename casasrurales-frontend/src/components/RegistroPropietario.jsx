import { useState } from 'react'
import '../styles/formulario.css'

export default function RegistroPropietario({ onRegistroExitoso, onVolver }) {
  const [formulario, setFormulario] = useState({
    nombreCuenta: '',
    email: '',
    telefono: '',
    numeroCuentaBancaria: '',
    password: ''
  })
  const [mensaje, setMensaje] = useState('')
  const [cargando, setCargando] = useState(false)

  const handleChange = (e) => {
    const { name, value } = e.target
    setFormulario((prev) => ({
      ...prev,
      [name]: value
    }))
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    setCargando(true)
    setMensaje('')

    try {
      const response = await fetch('/auth/registro/propietario', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(formulario)
      })

      const data = await response.json()

      if (response.ok) {
        setMensaje('Registro exitoso. Redirigiendo...')
        setFormulario({
          nombreCuenta: '',
          email: '',
          telefono: '',
          numeroCuentaBancaria: '',
          password: ''
        })
        onRegistroExitoso()
      } else {
        setMensaje(data.error || 'Error en el registro')
      }
    } catch (error) {
      setMensaje('Error de conexion con el servidor')
      console.error(error)
    } finally {
      setCargando(false)
    }
  }

  return (
    <div className="formulario-container registro-page">
      <div className="formulario-container-card registro-card registro-propietario-card">
        <h2>Registrarse como Propietario</h2>

        <form onSubmit={handleSubmit} className="formulario registro-form registro-propietario-form" autoComplete="on">
          <input type="text" style={{ display: 'none' }} autoComplete="username" value={formulario.email} onChange={() => {}} />
          <input type="password" style={{ display: 'none' }} autoComplete="current-password" onChange={() => {}} />

          <div className="form-group">
            <label htmlFor="nombreCuenta">Nombre de usuario</label>
            <input
              type="text"
              id="nombreCuenta"
              name="nombreCuenta"
              value={formulario.nombreCuenta}
              onChange={handleChange}
              required
            />
          </div>

          <div className="form-group">
            <label htmlFor="email">Email</label>
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
            <label htmlFor="telefono">Telefono</label>
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
            <label htmlFor="numeroCuentaBancaria">Numero de cuenta bancaria</label>
            <input
              type="text"
              id="numeroCuentaBancaria"
              name="numeroCuentaBancaria"
              value={formulario.numeroCuentaBancaria}
              onChange={handleChange}
              required
            />
          </div>

          <div className="form-group">
            <label htmlFor="password">Contrasena</label>
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
            Volver al Login
          </button>
        </form>

        {mensaje && (
          <div className={`mensaje ${mensaje.includes('Registro exitoso') ? 'exito' : 'error'}`}>
            {mensaje}
          </div>
        )}
      </div>
    </div>
  )
}
