import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { Link, Navigate, useLocation } from 'react-router'
import { Button } from '@/components/ui/button'
import { FormField } from '@/components/FormField'
import { useAuth } from '@/lib/auth-context'
import { registrati } from '@/lib/endpoints'
import { gestisciErrore } from '@/lib/errors'
import { AuthShell } from './AuthShell'

// Stesse regole di RegistrazioneRequest lato BE (il controllo vero lo rifà il server)
const schema = z.object({
  nome: z.string().trim().min(1, 'Inserisci il nome.').max(60, 'Massimo 60 caratteri.'),
  email: z.email('Inserisci un indirizzo email valido.').max(120),
  password: z.string().min(8, 'Almeno 8 caratteri.').max(72, 'Massimo 72 caratteri.'),
})

export function RegisterPage() {
  const { utente, accedi } = useAuth()
  const location = useLocation()
  const form = useForm({ resolver: zodResolver(schema), defaultValues: { nome: '', email: '', password: '' } })
  const { errors, isSubmitting } = form.formState

  if (utente) return <Navigate to={location.state?.da ?? '/'} replace />

  const onSubmit = async (dati) => {
    try {
      accedi(await registrati(dati))
    } catch (err) {
      gestisciErrore(err, form)
    }
  }

  return (
    <AuthShell titolo="Crea un account" sottotitolo="Salva le auto che ti piacciono e ricevi una mail quando il prezzo scende.">
      <form noValidate onSubmit={form.handleSubmit(onSubmit)} className="grid gap-5">
        <FormField id="nome" label="Nome" autoComplete="given-name" error={errors.nome} {...form.register('nome')} />
        <FormField id="email" label="Email" type="email" autoComplete="email" error={errors.email} {...form.register('email')} />
        <FormField
          id="password"
          label="Password"
          type="password"
          autoComplete="new-password"
          hint="Da 8 a 72 caratteri."
          error={errors.password}
          {...form.register('password')}
        />
        <p className="text-xs text-muted-foreground">
          Registrandoti confermi di aver letto la{' '}
          <Link to="/privacy" className="underline underline-offset-4">Privacy Policy</Link>.
        </p>
        <Button type="submit" size="lg" disabled={isSubmitting}>
          {isSubmitting && 'Registrazione in corso…'}
          {!isSubmitting && 'Registrati'}
        </Button>
        <p className="text-sm text-muted-foreground">
          Hai già un account?{' '}
          <Link to="/accedi" state={location.state} className="text-primary underline underline-offset-4">
            Accedi
          </Link>
        </p>
      </form>
    </AuthShell>
  )
}
