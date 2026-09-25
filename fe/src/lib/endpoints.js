// Tutte le chiamate al backend in un posto solo
import { api, toQuery } from '@/lib/api'

// ---------- Auth e profilo ----------
export const login = (dati) => api('/api/auth/login', { method: 'POST', body: dati, sessione: false })
export const registrati = (dati) => api('/api/auth/registrati', { method: 'POST', body: dati, sessione: false })
export const getProfilo = (opts) => api('/api/me', opts)
export const aggiornaProfilo = (dati) => api('/api/me', { method: 'PUT', body: dati })
export const eliminaAccount = () => api('/api/me', { method: 'DELETE' })

// ---------- Catalogo pubblico ----------
export const getCatalogo = (params, opts) => api(`/api/auto${toQuery(params)}`, opts)
export const getAuto = (id, opts) => api(`/api/auto/${encodeURIComponent(id)}`, opts)

// ---------- Preferiti ----------
export const getPreferiti = (opts) => api('/api/preferiti', opts)
export const aggiungiPreferito = (autoId) => api('/api/preferiti', { method: 'POST', body: { autoId } })
export const rimuoviPreferito = (id) => api(`/api/preferiti/${encodeURIComponent(id)}`, { method: 'DELETE' })

// ---------- Avvisi di prezzo ----------
export const getAvvisi = (opts) => api('/api/avvisi', opts)
export const creaAvviso = (dati) => api('/api/avvisi', { method: 'POST', body: dati })
export const eliminaAvviso = (id) => api(`/api/avvisi/${encodeURIComponent(id)}`, { method: 'DELETE' })
export const disattivaAvviso = (token) =>
  api('/api/avvisi/disattiva', { method: 'POST', body: { token }, sessione: false })

// ---------- Amministratore ----------
// Tutte le query che mostrano un prezzo o lo stato di un'auto: da invalidare dopo ogni modifica
export const DOPO_MODIFICA_AUTO = ['admin-auto', 'catalogo', 'auto', 'avvisi', 'preferiti']

export const getAutoAdmin = (params, opts) => api(`/api/admin/auto${toQuery(params)}`, opts)
export const creaAuto = (dati) => api('/api/admin/auto', { method: 'POST', body: dati })
export const modificaAuto = ({ id, ...dati }) =>
  api(`/api/admin/auto/${encodeURIComponent(id)}`, { method: 'PUT', body: dati })
export const cambiaPrezzo = ({ id, prezzo, versione }) =>
  api(`/api/admin/auto/${encodeURIComponent(id)}/prezzo`, { method: 'PATCH', body: { prezzo, versione } })
