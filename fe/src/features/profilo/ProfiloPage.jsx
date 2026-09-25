import { useState } from 'react'
import { useQueryClient } from '@tanstack/react-query'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { useNavigate } from 'react-router'
import { toast } from 'sonner'
import { Button } from '@/components/ui/button'
import { ConfirmDialog } from '@/components/ConfirmDialog'
import { FormField } from '@/components/FormField'
import { useAzione } from '@/hooks/useAzione'
import { useAuth } from '@/lib/auth-context'
import { aggiornaProfilo, eliminaAccount } from '@/lib/endpoints'

const schema = z.object({ nome: z.string().trim().min(1, 'Inserisci il nome.').max(60, 'Massimo 60 caratteri.') })

export function ProfiloPage() {
  const { utente, chiudiSessione } = useAuth()
  const queryClient = useQueryClient()
  const navigate = useNavigate()
  const [conferma, setConferma] = useState(false)

  const form = useForm({ resolver: zodResolver(schema), defaultValues: { nome: utente.nome } })
  const salva = useAzione(aggiornaProfilo, {
    form,
    successo: 'Profilo aggiornato',
    onSuccess: (aggiornato) => queryClient.setQueryData(['me'], aggiornato),
  })
  // Il BE cancella avvisi, preferiti e utente: da qui non parte più nessuna mail
  const elimina = useAzione(eliminaAccount, {
    onSuccess: () => {
      chiudiSessione()
      toast.success('Account eliminato insieme ad avvisi e preferiti')
      navigate('/')
    },
  })

  return (
    <div className="mx-auto grid max-w-xl gap-8">
      <h1 className="text-3xl font-semibold">Il mio profilo</h1>

      <form noValidate onSubmit={form.handleSubmit((d) => salva.mutate(d))} className="grid gap-4 rounded-xl border bg-card p-6">
        <FormField id="email" label="Email" value={utente.email} disabled readOnly />
        <FormField id="nome" label="Nome" error={form.formState.errors.nome} {...form.register('nome')} />
        <Button type="submit" className="justify-self-start" disabled={salva.isPending}>
          Salva
        </Button>
      </form>

      <section className="grid gap-3 rounded-xl border border-destructive/30 bg-card p-6">
        <h2 className="text-lg font-semibold">Elimina il mio account</h2>
        <p className="text-sm text-muted-foreground">
          Cancelliamo il tuo account, i preferiti e gli avvisi di prezzo. Non riceverai più nessuna mail. L'operazione non si
          può annullare.
        </p>
        <Button variant="destructive" className="justify-self-start" onClick={() => setConferma(true)}>
          Elimina account
        </Button>
      </section>

      <ConfirmDialog
        open={conferma}
        onOpenChange={setConferma}
        titolo="Eliminare l'account?"
        descrizione="Account, preferiti e avvisi verranno cancellati definitivamente."
        conferma="Elimina definitivamente"
        distruttiva
        inCorso={elimina.isPending}
        onConfirm={() => elimina.mutate()}
      />
    </div>
  )
}
