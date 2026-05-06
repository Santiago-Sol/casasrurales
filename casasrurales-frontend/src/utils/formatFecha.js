/**
 * Formatea una fecha ISO (e.g. "2026-05-17T05:00:00.000Z") a formato legible en español.
 * Ejemplo: "17 de mayo de 2026"
 */
export function formatFecha(fechaISO) {
  if (!fechaISO) return '';
  const fecha = new Date(fechaISO);
  return fecha.toLocaleDateString('es-CO', {
    day: 'numeric',
    month: 'long',
    year: 'numeric',
    timeZone: 'UTC'
  });
}

/**
 * Formato corto: "17 may 2026"
 */
export function formatFechaCorta(fechaISO) {
  if (!fechaISO) return '';
  const fecha = new Date(fechaISO);
  return fecha.toLocaleDateString('es-CO', {
    day: 'numeric',
    month: 'short',
    year: 'numeric',
    timeZone: 'UTC'
  });
}
