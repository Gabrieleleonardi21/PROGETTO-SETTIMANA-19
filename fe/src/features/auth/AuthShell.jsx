/**
 * Impaginazione comune di login e registrazione: una card centrata.
 * @param {{ titolo: string, sottotitolo: string, children: import('react').ReactNode }} props
 */
export function AuthShell({ titolo, sottotitolo, children }) {
  return (
    <div className="mx-auto max-w-md rounded-xl border bg-card p-8 shadow-sm sm:p-10">
      <h1 className="text-3xl font-semibold">{titolo}</h1>
      <p className="mt-2 mb-8 text-sm text-muted-foreground">{sottotitolo}</p>
      {children}
    </div>
  )
}
