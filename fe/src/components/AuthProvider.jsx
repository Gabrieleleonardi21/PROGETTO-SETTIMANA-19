import { useCallback, useEffect, useMemo, useState } from 'react'
import { useQuery, useQueryClient } from '@tanstack/react-query'
import { toast } from 'sonner'
import { ApiError, setUnauthorizedHandler } from '@/lib/api'
import { AuthContext } from '@/lib/auth-context'
import * as endpoints from '@/lib/endpoints'
import { CHIAVE_SESSIONE, cancellaSessione, leggiSessione, salvaSessione } from '@/lib/sessione'

// setTimeout accetta al massimo 2^31-1 ms (~24 giorni): oltre scatterebbe subito
const MAX_TIMEOUT_MS = 2 ** 31 - 1

// Chi e' l'utente lo dice sempre il backend (/api/me): nel browser resta solo il token
export function AuthProvider({ children }) {
  const queryClient = useQueryClient()
  const [collegato, setCollegato] = useState(() => Boolean(leggiSessione()))

  const profilo = useQuery({
    queryKey: ['me'],
    queryFn: ({ signal }) => endpoints.getProfilo({ signal }),
    enabled: collegato,
    staleTime: Infinity,
    // Un 401 non cambia riprovando; rete o backend in avvio (cold start di Render) si'
    retry: (tentativi, err) => !(err instanceof ApiError && err.status === 401) && tentativi < 3,
  })

  // Via token e dati personali dalla cache: si torna anonimi
  const chiudiSessione = useCallback(() => {
    cancellaSessione()
    setCollegato(false)
    queryClient.clear()
  }, [queryClient])

  // 401 da una chiamata autenticata: token scaduto, account eliminato o uscita da un'altra scheda
  useEffect(() => {
    // Si decide in base allo stato in memoria, non allo storage: un'altra scheda puo' averlo gia' svuotato
    setUnauthorizedHandler(() => {
      if (!collegato) return
      chiudiSessione()
      toast.info('La sessione è scaduta: accedi di nuovo.')
    })
  }, [collegato, chiudiSessione])

  // Login o uscita in un'altra scheda: ci si allinea subito, senza mostrare dati dell'utente sbagliato
  useEffect(() => {
    const onStorage = (e) => {
      if (e.key !== CHIAVE_SESSIONE && e.key !== null) return
      queryClient.clear()
      setCollegato(Boolean(leggiSessione()))
    }
    window.addEventListener('storage', onStorage)
    return () => window.removeEventListener('storage', onStorage)
  }, [queryClient])

  // Allo scadere del token la sessione si chiude da sola
  useEffect(() => {
    if (!collegato) return
    let timer
    const controlla = () => {
      const sessione = leggiSessione()
      if (!sessione) {
        chiudiSessione()
        toast.info('La sessione è scaduta: accedi di nuovo.')
        return
      }
      const mancano = new Date(sessione.scadenza).getTime() - Date.now()
      timer = setTimeout(controlla, Math.min(mancano, MAX_TIMEOUT_MS))
    }
    controlla()
    return () => clearTimeout(timer)
  }, [collegato, chiudiSessione])

  // Login e registrazione rispondono con lo stesso formato { token, scadenza, utente }
  const accedi = useCallback(
    (risposta) => {
      queryClient.clear()
      salvaSessione(risposta.token, risposta.scadenza)
      queryClient.setQueryData(['me'], risposta.utente)
      setCollegato(true)
    },
    [queryClient],
  )

  const value = useMemo(() => {
    let utente = null
    if (collegato && profilo.data) utente = profilo.data
    return {
      utente,
      isLoading: collegato && profilo.isPending,
      // Token presente ma /api/me non raggiungibile: non e' "anonimo", e' un errore da riprovare
      erroreProfilo: collegato && profilo.isError,
      riprova: profilo.refetch,
      isAdmin: utente?.ruolo === 'ADMIN',
      accedi,
      esci: chiudiSessione,
      chiudiSessione,
    }
  }, [collegato, profilo.data, profilo.isPending, profilo.isError, profilo.refetch, accedi, chiudiSessione])

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}
