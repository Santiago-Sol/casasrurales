import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
  server: {
    port: 3000,
    proxy: {
      '/api': {
        target: 'https://casasrurales-production.up.railway.app',
        changeOrigin: true
      },
      '/auth': {
        target: 'https://casasrurales-production.up.railway.app',
        changeOrigin: true
      },
      '/uploads': {
        target: 'https://casasrurales-production.up.railway.app',
        changeOrigin: true
      }
    }
  }
})
