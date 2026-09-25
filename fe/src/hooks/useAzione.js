import { useMutation, useQueryClient } from '@tanstack/react-query'
import { toast } from 'sonner'
import { ApiError } from '@/lib/api'
import { gestisciErrore } from '@/lib/errors'

/**
 * Mutation con i comportamenti standard dell'app: toast di successo, errori mostrati
 * (anche per campo se si passa il form) e cache invalidata per le query indicate.
 * @param {(variabili: any) => Promise<any>} fn chiamata API
 * @param {{ invalida?: string[], successo?: string | ((dati: any, variabili: any) => string),
 *           form?: import('react-hook-form').UseFormReturn, onSuccess?: (dati: any) => void }} opzioni
 */
export function useAzione(fn, { invalida = [], successo, form, onSuccess } = {}) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: fn,
    onSuccess: (dati, variabili) => {
      for (const chiave of invalida) queryClient.invalidateQueries({ queryKey: [chiave] })
      if (typeof successo === 'function') toast.success(successo(dati, variabili))
      else if (successo) toast.success(successo)
      onSuccess?.(dati, variabili)
    },
    onError: (err) => {
      // 409: qualcun altro ha appena cambiato i dati, si ricaricano per mostrare la versione aggiornata
      if (err instanceof ApiError && err.status === 409) {
        for (const chiave of invalida) queryClient.invalidateQueries({ queryKey: [chiave] })
      }
      gestisciErrore(err, form)
    },
  })
}
