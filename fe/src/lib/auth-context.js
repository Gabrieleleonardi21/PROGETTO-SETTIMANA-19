import { createContext, useContext } from 'react'

export const AuthContext = createContext(null)

/** Stato di autenticazione: { utente, isLoading, isAdmin, accedi, esci, chiudiSessione }. */
export function useAuth() {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth va usato dentro <AuthProvider>')
  return ctx
}
