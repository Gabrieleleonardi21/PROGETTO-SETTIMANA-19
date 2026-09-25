// Token JWT nel localStorage: FE e BE sono su domini diversi, quindi niente cookie di sessione.
// Si salvano solo token e scadenza (dichiarati nella Cookie Policy); nome ed email si chiedono a /api/me.
export const CHIAVE_SESSIONE = 'salone.sessione'

export function leggiSessione() {
  try {
    const grezzo = localStorage.getItem(CHIAVE_SESSIONE)
    if (!grezzo) return null
    const sessione = JSON.parse(grezzo)
    // Token scaduto: inutile tenerlo
    if (!sessione.token || new Date(sessione.scadenza).getTime() <= Date.now()) {
      localStorage.removeItem(CHIAVE_SESSIONE)
      return null
    }
    return sessione
  } catch {
    return null
  }
}

export function leggiToken() {
  return leggiSessione()?.token ?? null
}

export function salvaSessione(token, scadenza) {
  try {
    localStorage.setItem(CHIAVE_SESSIONE, JSON.stringify({ token, scadenza }))
  } catch {
    // storage non disponibile: la sessione vale finche' la pagina resta aperta
  }
}

export function cancellaSessione() {
  try {
    localStorage.removeItem(CHIAVE_SESSIONE)
  } catch {
    // niente da fare
  }
}
