/** Impaginazione comune di Privacy e Cookie Policy. */
export function Informativa({ titolo, aggiornata, children }) {
  return (
    <article className="mx-auto grid max-w-3xl gap-4 rounded-xl border bg-card p-8 leading-relaxed [&_h2]:mt-4 [&_h2]:text-xl [&_h2]:font-semibold [&_li]:ml-5 [&_li]:list-disc [&_td]:border [&_td]:p-2 [&_th]:border [&_th]:p-2 [&_th]:text-left">
      <h1 className="text-3xl font-semibold">{titolo}</h1>
      <p className="text-sm text-muted-foreground">Ultimo aggiornamento: {aggiornata}</p>
      {children}
    </article>
  )
}
