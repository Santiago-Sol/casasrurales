# Casas Rurales - Frontend React

Frontend React básico para la aplicación de búsqueda y gestión de casas rurales.

## Características

- ✅ Registro de propietarios
- ✅ Registro de clientes
- ✅ Búsqueda de casas por población
- ✅ Búsqueda de casas por código
- ✅ Visualización detallada de casas
- ✅ Diseño responsivo
- ✅ Integración con API REST (Spring Boot)

## Estructura del Proyecto

```
casasrurales-frontend/
├── src/
│   ├── components/          # Componentes React
│   │   ├── RegistroPropietario.jsx
│   │   ├── RegistroCliente.jsx
│   │   └── BusquedaCasas.jsx
│   ├── styles/              # Hojas de estilos
│   │   ├── formulario.css
│   │   └── busqueda.css
│   ├── App.jsx              # Componente principal
│   ├── App.css              # Estilos de App
│   ├── index.css            # Estilos globales
│   └── main.jsx             # Entry point
├── index.html               # HTML base
├── vite.config.js           # Configuración de Vite
└── package.json             # Dependencias
```

## Requisitos Previos

- Node.js 14+ instalado
- npm o yarn
- Backend Spring Boot ejecutándose en `http://localhost:8080`

## Instalación

1. Navega a la carpeta del proyecto:
```bash
cd casasrurales-frontend
```

2. Instala las dependencias:
```bash
npm install
```

## Desarrollo

Para ejecutar la aplicación en modo desarrollo:

```bash
npm run dev
```

La aplicación se abrirá en `http://localhost:3000`

### Configuración de Proxy

El archivo `vite.config.js` incluye un proxy que redirige las peticiones `/api` y `/auth` al backend en `http://localhost:8080`.

Si necesitas cambiar la URL del backend, edita `vite.config.js`:

```javascript
proxy: {
  '/api': {
    target: 'http://tu-url-backend:puerto',
    changeOrigin: true
  },
  '/auth': {
    target: 'http://tu-url-backend:puerto',
    changeOrigin: true
  }
}
```

## Build para Producción

Para crear una versión optimizada para producción:

```bash
npm run build
```

Los archivos compilados se guardarán en la carpeta `dist/`.

## Estructura de Componentes

### RegistroPropietario
Formulario para que nuevos propietarios se registren con:
- Nombre de usuario
- Email
- Teléfono
- Número de cuenta bancaria
- Contraseña

### RegistroCliente
Formulario simplificado para que clientes se registren con:
- Email
- Teléfono
- Contraseña

### BusquedaCasas
Búsqueda con dos opciones:
1. **Por Población**: Lista todas las casas en una población
2. **Por Código**: Muestra detalle completo de una casa específica

## Endpoints API Consumidos

### Autenticación (AuthController)
- `POST /auth/registro/propietario` - Registrar propietario
- `POST /auth/registro/cliente` - Registrar cliente
- `GET /auth/me` - Obtener usuario actual

### Búsqueda (BusquedaController)
- `GET /api/busqueda/por-poblacion?poblacion=XXX` - Listar casas por población
- `GET /api/busqueda/{codigoCasa}` - Obtener detalle de casa

## Próximas Mejoras

- [ ] Autenticación con tokens (JWT)
- [ ] Persistencia de sesión de usuario
- [ ] Páginas de usuario (cliente/propietario)
- [ ] Gestión de reservas
- [ ] Galerías de fotos
- [ ] Comentarios y calificaciones
- [ ] Mapas interactivos
- [ ] Filtros avanzados de búsqueda
- [ ] Dashboard administrativo

## Licencia

ISC
