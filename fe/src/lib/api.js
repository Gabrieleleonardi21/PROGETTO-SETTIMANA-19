// Client HTTP verso il backend Spring.
// In sviluppo BASE e' vuota e il proxy di Vite inoltra /api alla 8080.
// In produzione arriva da VITE_API_URL, iniettata durante la build: FE e BE stanno su due domini.
import { leggiToken } from '@/lib/sessione'

const BASE = (import.meta.env.VITE_API_URL ?? '').replace(/\/$/, '')

// Messaggi per gli status che il backend restituisce senza body
const MESSAGGI_STATUS = {
  401: 'Sessione scaduta o credenziali non valide.',
  403: 'Non hai i permessi per questa operazione.',
  404: 'Risorsa non trovata.',
}

/** Errore HTTP con status, messaggio leggibile e, per i 400, gli errori per campo. */
export class ApiError extends Error {
  constructor(status, message, fieldErrors = {}) {
    super(message)
    this.status = status
    this.fieldErrors = fieldErrors
  }
}

// Callback di "sessione scaduta", impostata da AuthProvider
let onUnauthorized = () => {}

export function setUnauthorizedHandler(handler) {
  onUnauthorized = handler
}

/** Costruisce la query string saltando i valori vuoti. */
export function toQuery(params = {}) {
  const qs = new URLSearchParams()
  for (const [key, value] of Object.entries(params)) {
    if (value === undefined || value === null || value === '') continue
    qs.append(key, String(value))
  }
  const str = qs.toString()
  if (!str) return ''
  return `?${str}`
}

async function leggiErrore(res) {
  const body = await res.json().catch(() => null)
  const fallback = MESSAGGI_STATUS[res.status] ?? `Errore ${res.status}`
  if (!body) return new ApiError(res.status, fallback)
  // Validazione: { status, errors: { campo: messaggio } }
  if (body.errors) return new ApiError(res.status, 'Controlla i campi evidenziati.', body.errors)
  return new ApiError(res.status, body.message ?? fallback)
}

/**
 * Esegue una richiesta JSON. Restituisce il body (o null se vuoto), lancia ApiError sugli status non 2xx.
 * @param {string} path es. "/api/auto?page=0"
 * @param {{ method?: string, body?: unknown, signal?: AbortSignal, sessione?: boolean }} [options]
 *   sessione=false: un 401 non significa "sessione scaduta" (es. login con password sbagliata)
 */
export async function api(path, { method = 'GET', body, signal, sessione = true } = {}) {
  const init = { method, signal, headers: {} }
  // Il token parte solo verso il nostro backend, nell'header Authorization
  const token = leggiToken()
  if (token) init.headers.Authorization = `Bearer ${token}`
  if (body !== undefined) {
    init.headers['Content-Type'] = 'application/json'
    init.body = JSON.stringify(body)
  }
  const res = await fetch(`${BASE}${path}`, init)

  if (!res.ok) {
    const errore = await leggiErrore(res)
    // 401 su una chiamata autenticata: sessione scaduta, ci pensa AuthProvider con il suo avviso
    if (res.status === 401 && sessione) {
      errore.sessione = true
      onUnauthorized()
    }
    throw errore
  }

  const text = await res.text()
  if (!text) return null
  return JSON.parse(text)
}
