export function mostrarNotificacion(mensaje, tipo = 'error') {
  window.dispatchEvent(new CustomEvent('app-notificacion', {
    detail: {
      mensaje,
      tipo
    }
  }));
}

export function notificarSiEsError(mensaje, tipo = 'error') {
  if (tipo === 'error' || tipo === 'advertencia' || tipo === 'warning') {
    mostrarNotificacion(mensaje, tipo);
  }
}
