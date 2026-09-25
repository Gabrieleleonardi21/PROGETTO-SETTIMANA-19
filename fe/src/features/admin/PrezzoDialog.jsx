import { useEffect } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { FormDialog } from '@/components/FormDialog'
import { FormField } from '@/components/FormField'
import { useAzione } from '@/hooks/useAzione'
import { cambiaPrezzo, DOPO_MODIFICA_AUTO } from '@/lib/endpoints'
import { formatEuro } from '@/lib/format'
import { numero } from '@/lib/validazione'

const schema = z.object({ prezzo: numero('Inserisci il prezzo.', (n) => n.positive('Deve essere positivo.')) })

/** Cambio rapido del prezzo. Le mail agli utenti partono dal BE dopo il salvataggio, senza farci aspettare. */
export function PrezzoDialog({ open, onOpenChange, auto }) {
  const form = useForm({ resolver: zodResolver(schema), defaultValues: { prezzo: '' } })

  useEffect(() => {
    if (open && auto) form.reset({ prezzo: auto.prezzo })
  }, [open, auto, form])

  const salva = useAzione(cambiaPrezzo, {
    invalida: DOPO_MODIFICA_AUTO,
    successo: 'Prezzo aggiornato',
    form,
    onSuccess: () => onOpenChange(false),
  })

  if (!auto) return null
  return (
    <FormDialog
      open={open}
      onOpenChange={onOpenChange}
      titolo={`Prezzo di ${auto.marca} ${auto.modello}`}
      descrizione={`Prezzo attuale ${formatEuro(auto.prezzo)}. Se scende sotto la soglia di un utente, gli arriva una mail.`}
      conferma="Aggiorna prezzo"
      inCorso={salva.isPending}
      onSubmit={form.handleSubmit((d) => salva.mutate({ id: auto.id, versione: auto.versione, prezzo: d.prezzo }))}
    >
      <FormField id="nuovo-prezzo" label="Nuovo prezzo (€)" type="number" min="1" error={form.formState.errors.prezzo} {...form.register('prezzo')} />
    </FormDialog>
  )
}
