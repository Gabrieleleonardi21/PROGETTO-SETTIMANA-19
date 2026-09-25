import { Suspense } from 'react'
import { Link, NavLink, Outlet, useLocation, useNavigate } from 'react-router'
import { CarFront, LogOut } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Skeleton } from '@/components/ui/skeleton'
import { Toaster } from '@/components/ui/sonner'
import { ThemeToggle } from '@/components/ThemeToggle'
import { useAuth } from '@/lib/auth-context'
import { cn } from '@/lib/utils'

// Voci di menu con il livello minimo richiesto per vederle
const VOCI = [
  { to: '/', label: 'Catalogo', end: true },
  { to: '/preferiti', label: 'Preferiti', richiede: 'utente' },
  { to: '/avvisi', label: 'Avvisi di prezzo', richiede: 'utente' },
  { to: '/admin/auto', label: 'Gestione auto', richiede: 'admin' },
]

function voceVisibile(voce, auth) {
  if (voce.richiede === 'utente') return Boolean(auth.utente)
  if (voce.richiede === 'admin') return auth.isAdmin
  return true
}

function classeLink({ isActive }) {
  return cn(
    'rounded-md px-2.5 py-1.5 text-sm transition-colors hover:bg-accent',
    isActive && 'bg-accent font-medium text-foreground',
  )
}

export function AppLayout() {
  const auth = useAuth()
  const navigate = useNavigate()
  const { pathname } = useLocation()

  const esci = () => {
    auth.esci()
    navigate('/')
  }

  return (
    <div className="flex min-h-svh flex-col">
      <a
        href="#contenuto"
        className="sr-only focus:not-sr-only focus:fixed focus:top-2 focus:left-2 focus:z-50 focus:rounded-md focus:bg-primary focus:px-3 focus:py-2 focus:text-primary-foreground"
      >
        Vai al contenuto
      </a>

      <header className="border-b bg-card">
        <div className="mx-auto flex max-w-6xl flex-wrap items-center gap-x-6 gap-y-3 px-4 py-3">
          <NavLink to="/" className="flex items-center gap-2 text-xl font-semibold tracking-tight">
            <CarFront aria-hidden="true" className="size-6 text-primary" />
            Salone Auto
          </NavLink>

          <nav aria-label="Principale" className="flex flex-wrap items-center gap-1">
            {VOCI.filter((v) => voceVisibile(v, auth)).map((v) => (
              <NavLink key={v.to} to={v.to} end={v.end} className={classeLink}>
                {v.label}
              </NavLink>
            ))}
          </nav>

          <div className="ml-auto flex items-center gap-2">
            <ThemeToggle />
            {auth.utente && (
              <>
                <Button asChild variant="ghost" size="sm">
                  <Link to="/profilo">{auth.utente.nome}</Link>
                </Button>
                <Button variant="ghost" size="sm" onClick={esci}>
                  <LogOut aria-hidden="true" />
                  Esci
                </Button>
              </>
            )}
            {!auth.utente && !auth.isLoading && (
              <>
                <Button asChild variant="ghost" size="sm">
                  <Link to="/accedi">Accedi</Link>
                </Button>
                <Button asChild size="sm">
                  <Link to="/registrati">Registrati</Link>
                </Button>
              </>
            )}
          </div>
        </div>
      </header>

      <main id="contenuto" tabIndex={-1} className="mx-auto w-full max-w-6xl flex-1 px-4 py-8 outline-none">
        <Suspense fallback={<Skeleton className="h-64 w-full" />}>
          {/* Dissolvenza a ogni cambio pagina (la key sul percorso rimonta il contenitore).
              Animazione CSS e non JS: parte anche se il browser rallenta gli script, e con "riduci movimento" si spegne. */}
          <div key={pathname} className="animate-in fade-in slide-in-from-bottom-2 duration-300 motion-reduce:animate-none">
            <Outlet />
          </div>
        </Suspense>
      </main>

      {/* Privacy e Cookie Policy raggiungibili da ogni pagina */}
      <footer className="border-t bg-card">
        <div className="mx-auto flex max-w-6xl flex-wrap items-center gap-x-6 gap-y-2 px-4 py-4 text-sm text-muted-foreground">
          <span>
            © Salone Auto — progetto didattico. Auto, prezzi e km sono dati di esempio inventati.
          </span>
          <nav aria-label="Informative" className="ml-auto flex gap-4">
            <Link to="/privacy" className="hover:text-foreground hover:underline">Privacy Policy</Link>
            <Link to="/cookie" className="hover:text-foreground hover:underline">Cookie Policy</Link>
            <Link to="/crediti" className="hover:text-foreground hover:underline">Crediti e fonti</Link>
          </nav>
        </div>
      </footer>

      <Toaster richColors position="top-right" />
    </div>
  )
}
