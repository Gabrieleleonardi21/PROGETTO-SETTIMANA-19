import { useEffect } from 'react'
import { Controller, useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { Checkbox } from '@/components/ui/checkbox'
import { Label } from '@/components/ui/label'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import { Textarea } from '@/components/ui/textarea'
import { FormDialog } from '@/components/FormDialog'
import { FormField } from '@/components/FormField'
import { useAzione } from '@/hooks/useAzione'
import { creaAuto, DOPO_MODIFICA_AUTO, modificaAuto } from '@/lib/endpoints'
import { ALIMENTAZIONI } from '@/lib/format'
import { numero } from '@/lib/validazione'

// Stesse regole di AutoRequest lato BE
const schema = z.object({
  marca: z.string().trim().min(1, 'Obbligatoria.').max(60),
  modello: z.string().trim().min(1, 'Obbligatorio.').max(80),
  anno: numero('Anno non valido.', (n) => n.int('Deve essere un numero intero.').min(1900, 'Anno non valido.').max(2100, 'Anno non valido.')),
  km: numero('Inserisci i km.', (n) => n.int('Deve essere un numero intero.').min(0, 'Non può essere negativo.')),
  alimentazione: z.string().min(1, 'Scegli l\'alimentazione.'),
  descrizione: z.string().max(2000, 'Massimo 2000 caratteri.'),
  prezzo: numero('Inserisci il prezzo.', (n) => n.positive('Deve essere positivo.')),
  prezzoAcquisto: numero('Inserisci il prezzo d\'acquisto.', (n) => n.positive('Deve essere positivo.')),
  pubblicata: z.boolean(),
  // Un indirizzo per riga; i siti ammessi li controlla il backend (400 con l'elenco se non va bene)
  fotoTesto: z.string().max(4000),
  creditiFoto: z.string().max(200, 'Massimo 200 caratteri.'),
  fonteFoto: z.string().max(500),
})

const VUOTA = {
  marca: '', modello: '', anno: '', km: '', alimentazione: '', descrizione: '', prezzo: '', prezzoAcquisto: '', pubblicata: false,
  fotoTesto: '', creditiFoto: '', fonteFoto: '',
}

// Valori del form a partire da un'auto esistente (modifica) o vuoti (nuova)
function valoriIniziali(auto) {
  if (!auto) return VUOTA
  return {
    ...auto,
    descrizione: auto.descrizione ?? '',
    fotoTesto: (auto.immagini ?? []).join('\n'),
    creditiFoto: auto.creditiFoto ?? '',
    fonteFoto: auto.fonteFoto ?? '',
  }
}

// Dal form al formato di AutoRequest: le righe della textarea diventano la lista delle foto
function perIlServer({ fotoTesto, ...dati }) {
  const immagini = fotoTesto.split('\n').map((riga) => riga.trim()).filter(Boolean)
  return { ...dati, immagini }
}

/**
 * Crea (auto assente) o modifica un'auto. Se in modifica il prezzo scende, il BE fa partire gli avvisi.
 * @param {{ open: boolean, onOpenChange: (o: boolean) => void, auto?: object }} props
 */
export function AutoDialog({ open, onOpenChange, auto }) {
  const form = useForm({ resolver: zodResolver(schema), defaultValues: valoriIniziali(auto) })
  const { errors } = form.formState

  // Riapertura su un'altra auto: il form riparte dai suoi valori
  useEffect(() => {
    if (open) form.reset(valoriIniziali(auto))
  }, [open, auto, form])

  let fn = creaAuto
  let titolo = 'Nuova auto'
  if (auto) {
    // La versione vista all'apertura: se un altro admin salva prima, il BE risponde 409
    fn = (dati) => modificaAuto({ id: auto.id, versione: auto.versione, ...dati })
    titolo = `Modifica ${auto.marca} ${auto.modello}`
  }
  const salva = useAzione(fn, { invalida: DOPO_MODIFICA_AUTO, successo: 'Auto salvata', form, onSuccess: () => onOpenChange(false) })

  return (
    <FormDialog
      open={open}
      onOpenChange={onOpenChange}
      titolo={titolo}
      larga
      conferma="Salva"
      inCorso={salva.isPending}
      onSubmit={form.handleSubmit((d) => salva.mutate(perIlServer(d)))}
    >
      <div className="grid gap-4 sm:grid-cols-2">
        <FormField id="marca" label="Marca" error={errors.marca} {...form.register('marca')} />
        <FormField id="modello" label="Modello" error={errors.modello} {...form.register('modello')} />
        <FormField id="anno" label="Anno" type="number" error={errors.anno} {...form.register('anno')} />
        <FormField id="km" label="Km" type="number" min="0" error={errors.km} {...form.register('km')} />
        <FormField id="prezzo" label="Prezzo di vendita (€)" type="number" min="1" error={errors.prezzo} {...form.register('prezzo')} />
        <FormField id="prezzoAcquisto" label="Prezzo d'acquisto (€)" type="number" min="1" error={errors.prezzoAcquisto} {...form.register('prezzoAcquisto')} />
        <FormField id="alimentazione" label="Alimentazione" error={errors.alimentazione}>
          {(aria) => (
            <Controller
              control={form.control}
              name="alimentazione"
              render={({ field }) => (
                <Select value={field.value} onValueChange={field.onChange}>
                  <SelectTrigger {...aria} className="w-full">
                    <SelectValue placeholder="Scegli" />
                  </SelectTrigger>
                  <SelectContent>
                    {ALIMENTAZIONI.map((a) => (
                      <SelectItem key={a.value} value={a.value}>
                        {a.label}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              )}
            />
          )}
        </FormField>
        <Controller
          control={form.control}
          name="pubblicata"
          render={({ field }) => (
            <div className="flex items-center gap-2 self-end pb-2">
              <Checkbox id="pubblicata" checked={field.value} onCheckedChange={(v) => field.onChange(v === true)} />
              <Label htmlFor="pubblicata">Pubblicata (altrimenti bozza)</Label>
            </div>
          )}
        />
      </div>
      <FormField id="descrizione" label="Descrizione" error={errors.descrizione}>
        {(aria) => <Textarea {...aria} rows={4} maxLength={2000} {...form.register('descrizione')} />}
      </FormField>
      <FormField
        id="fotoTesto"
        label="Foto (un indirizzo per riga, la prima è la copertina)"
        hint="Solo https da upload.wikimedia.org o cdn.dummyjson.com. Massimo 8."
        error={errors.fotoTesto ?? errors.immagini}
      >
        {(aria) => <Textarea {...aria} rows={3} className="font-mono text-xs" {...form.register('fotoTesto')} />}
      </FormField>
      <div className="grid gap-4 sm:grid-cols-2">
        <FormField id="creditiFoto" label="Crediti foto" error={errors.creditiFoto} {...form.register('creditiFoto')} />
        <FormField id="fonteFoto" label="Pagina di origine (link)" error={errors.fonteFoto} {...form.register('fonteFoto')} />
      </div>
    </FormDialog>
  )
}
