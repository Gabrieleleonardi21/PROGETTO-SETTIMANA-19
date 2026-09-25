import { Navigate, Outlet, useLocation } from 'react-router'
import { Button } from '@/components/ui/button'
import { Skeleton } from '@/components/ui/skeleton'
import { useAuth } from '@/lib/auth-context'

/**
 * Protegge un gruppo di rotte. È solo UX: i permessi veri li impone il backend (401/403/404).
 * @param {{ admin?: boolean }} props admin=true: serve il ruolo ADMIN
 */
export function RequireAuth({ admin }) {
  const { utente, isLoading, isAdmin, erroreProfilo, riprova } = useAuth()
  const location = useLocation()

  if (isLoading) return <Skeleton className="mx-auto mt-10 h-64 max-w-6xl" />
  // Collegato ma il server non risponde: niente redirect al login, si propone di riprovare
  if (erroreProfilo) {
    return (
      <div role="alert" className="mx-auto mt-10 grid max-w-md justify-items-center gap-4 text-center">
        <p className="text-muted-foreground">Il server non risponde. Riprova tra qualche secondo.</p>
        <Button variant="outline" onClick={() => riprova()}>Riprova</Button>
      </div>
    )
  }
  // Dopo il login si torna alla pagina richiesta
  if (!utente) return <Navigate to="/accedi" replace state={{ da: location.pathname + location.search }} />
  if (admin && !isAdmin) return <Navigate to="/" replace />
  return <Outlet />
}
