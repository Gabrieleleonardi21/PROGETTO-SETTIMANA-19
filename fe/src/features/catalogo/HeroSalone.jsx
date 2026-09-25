import { lazy, Suspense } from 'react'
import { motion, useReducedMotion } from 'motion/react'
import { useTheme } from 'next-themes'
import { CarFront, ShieldCheck, BellRing } from 'lucide-react'
import { LimiteErrori } from '@/components/LimiteErrori'

// Three.js pesa: la scena si scarica solo quando serve, in un file a parte
const AutoScena3D = lazy(() => import('./AutoScena3D'))

// Rosso del marchio in esadecimale: Three.js non legge i colori oklch dei token CSS
const ROSSO = { chiaro: '#d42a2a', scuro: '#e5484d' }

const PUNTI = [
  { icona: CarFront, testo: 'Usato selezionato e controllato' },
  { icona: BellRing, testo: 'Una mail quando il prezzo scende sotto la tua soglia' },
  { icona: ShieldCheck, testo: 'Nessun cookie di profilazione' },
]

// Comparsa in sequenza di titolo, testo e punti
const contenitore = { nascosto: {}, visibile: { transition: { staggerChildren: 0.12 } } }
const elemento = { nascosto: { opacity: 0, y: 16 }, visibile: { opacity: 1, y: 0, transition: { duration: 0.5 } } }

function Riserva() {
  return (
    <div className="flex h-full items-center justify-center">
      <CarFront aria-hidden="true" className="size-32 text-primary/70" />
    </div>
  )
}

/** Intestazione del catalogo: testo animato a sinistra, auto 3D a destra. */
export function HeroSalone({ totale }) {
  const { resolvedTheme } = useTheme()
  const riduci = useReducedMotion()
  const scuro = resolvedTheme === 'dark'
  let colore = ROSSO.chiaro
  if (scuro) colore = ROSSO.scuro

  return (
    <section className="relative grid items-center gap-6 overflow-hidden rounded-2xl border bg-card p-6 sm:p-10 lg:grid-cols-2">
      {/* Alone colorato di sfondo, solo decorativo */}
      <div
        aria-hidden="true"
        className="pointer-events-none absolute -top-24 -right-24 size-96 rounded-full bg-primary/15 blur-3xl"
      />
      <motion.div variants={contenitore} initial="nascosto" animate="visibile" className="relative grid gap-4">
        <motion.p variants={elemento} className="text-sm font-medium tracking-wide text-primary uppercase">
          Salone Auto
        </motion.p>
        <motion.h1 variants={elemento} className="text-4xl font-semibold sm:text-5xl">
          Trova l'auto giusta, al prezzo giusto.
        </motion.h1>
        <motion.p variants={elemento} className="max-w-md text-muted-foreground">
          {totale} auto disponibili. Salva le preferite e fissa una soglia: ti scriviamo noi quando il prezzo scende.
        </motion.p>
        <motion.ul variants={elemento} className="grid gap-2 text-sm">
          {PUNTI.map(({ icona: Icona, testo }) => (
            <li key={testo} className="flex items-center gap-2">
              <Icona aria-hidden="true" className="size-4 text-primary" />
              {testo}
            </li>
          ))}
        </motion.ul>
      </motion.div>

      <div className="relative h-64 sm:h-80">
        <LimiteErrori riserva={<Riserva />}>
          <Suspense fallback={<Riserva />}>
            <AutoScena3D colore={colore} scuro={scuro} gira={!riduci} />
          </Suspense>
        </LimiteErrori>
      </div>
    </section>
  )
}
