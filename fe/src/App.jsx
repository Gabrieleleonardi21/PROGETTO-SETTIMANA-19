import { lazy } from 'react'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { MotionConfig } from 'motion/react'
import { ThemeProvider } from 'next-themes'
import { createBrowserRouter, RouterProvider } from 'react-router'
import { AppLayout } from '@/components/AppLayout'
import { AuthProvider } from '@/components/AuthProvider'
import { ErrorPage, NotFoundPage } from '@/components/ErrorPage'
import { RequireAuth } from '@/components/RequireAuth'
import { ApiError } from '@/lib/api'
// Il catalogo è la home: arriva subito, insieme al resto del bundle iniziale
import { CatalogoPage } from '@/features/catalogo/CatalogoPage'

/**
 * Lazy loading delle pagine: ognuna diventa un file JS separato, scaricato solo quando ci si entra.
 * lazy() vuole un export default, le pagine hanno export con nome: questo helper fa da ponte.
 * Il caricamento lo mostra il <Suspense> di AppLayout (skeleton al posto della pagina).
 */
function pagina(carica, nome) {
  return lazy(() => carica().then((modulo) => ({ default: modulo[nome] })))
}

const DettaglioAutoPage = pagina(() => import('@/features/catalogo/DettaglioAutoPage'), 'DettaglioAutoPage')
const LoginPage = pagina(() => import('@/features/auth/LoginPage'), 'LoginPage')
const RegisterPage = pagina(() => import('@/features/auth/RegisterPage'), 'RegisterPage')
const PreferitiPage = pagina(() => import('@/features/preferiti/PreferitiPage'), 'PreferitiPage')
const AvvisiPage = pagina(() => import('@/features/avvisi/AvvisiPage'), 'AvvisiPage')
const DisattivaAvvisoPage = pagina(() => import('@/features/avvisi/DisattivaAvvisoPage'), 'DisattivaAvvisoPage')
const ProfiloPage = pagina(() => import('@/features/profilo/ProfiloPage'), 'ProfiloPage')
const PrivacyPage = pagina(() => import('@/features/legale/PrivacyPage'), 'PrivacyPage')
const CookiePage = pagina(() => import('@/features/legale/CookiePage'), 'CookiePage')
const CreditiPage = pagina(() => import('@/features/legale/CreditiPage'), 'CreditiPage')
// La gestione auto la scarica solo l'amministratore (ha già l'export default)
const AdminAutoPage = lazy(() => import('@/features/admin/AdminAutoPage'))

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 30_000,
      // Si riprova solo per errori di rete o 5xx: un 4xx non cambia riprovando
      retry: (tentativi, err) => {
        if (!(err instanceof ApiError)) return tentativi < 2
        return err.status >= 500 && tentativi < 2
      },
    },
  },
})

const router = createBrowserRouter([
  {
    element: <AppLayout />,
    errorElement: <ErrorPage />,
    children: [
      { index: true, element: <CatalogoPage /> },
      { path: 'auto/:id', element: <DettaglioAutoPage /> },
      { path: 'accedi', element: <LoginPage /> },
      { path: 'registrati', element: <RegisterPage /> },
      { path: 'privacy', element: <PrivacyPage /> },
      { path: 'cookie', element: <CookiePage /> },
      { path: 'crediti', element: <CreditiPage /> },
      { path: 'avvisi/disattiva', element: <DisattivaAvvisoPage /> },
      {
        element: <RequireAuth />,
        children: [
          { path: 'preferiti', element: <PreferitiPage /> },
          { path: 'avvisi', element: <AvvisiPage /> },
          { path: 'profilo', element: <ProfiloPage /> },
        ],
      },
      {
        path: 'admin',
        element: <RequireAuth admin />,
        children: [{ path: 'auto', element: <AdminAutoPage /> }],
      },
      { path: '*', element: <NotFoundPage /> },
    ],
  },
])

function App() {
  return (
    // Tema: classe "dark" su <html>, scelta salvata in "salone.tema", di default quello del sistema.
    // La chiave è la stessa letta da public/tema-iniziale.js prima del primo disegno: è quel file a evitare
    // lo sfarfallio, perché lo script inline di next-themes in un'app solo client non verrebbe mai eseguito.
    // type application/json lo rende un dato inerte, così React non segnala un <script> nel render.
    <ThemeProvider
      attribute="class"
      defaultTheme="system"
      enableSystem
      storageKey="salone.tema"
      disableTransitionOnChange
      scriptProps={{ type: 'application/json' }}
    >
      {/* reducedMotion="user": chi ha "riduci movimento" nel sistema non vede le animazioni */}
      <MotionConfig reducedMotion="user">
        <QueryClientProvider client={queryClient}>
          <AuthProvider>
            <RouterProvider router={router} />
          </AuthProvider>
        </QueryClientProvider>
      </MotionConfig>
    </ThemeProvider>
  )
}

export default App
