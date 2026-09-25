// Formattazione in italiano dei valori restituiti dal backend
const euro = new Intl.NumberFormat('it-IT', { style: 'currency', currency: 'EUR', maximumFractionDigits: 0 })
const numero = new Intl.NumberFormat('it-IT')

export function formatEuro(valore) {
  if (valore === null || valore === undefined) return '—'
  return euro.format(valore)
}

export function formatKm(valore) {
  return `${numero.format(valore)} km`
}

// Valori dell'enum Alimentazione del backend con l'etichetta da mostrare
export const ALIMENTAZIONI = [
  { value: 'BENZINA', label: 'Benzina' },
  { value: 'DIESEL', label: 'Diesel' },
  { value: 'GPL', label: 'GPL' },
  { value: 'METANO', label: 'Metano' },
  { value: 'IBRIDA', label: 'Ibrida' },
  { value: 'ELETTRICA', label: 'Elettrica' },
]

export function etichettaAlimentazione(valore) {
  return ALIMENTAZIONI.find((a) => a.value === valore)?.label ?? valore
}
