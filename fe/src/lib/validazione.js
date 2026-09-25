import { z } from 'zod'

// Un input number vuoto arriva come '': senza questo passaggio z.coerce lo trasformerebbe in 0
function vuotoInUndefined(valore) {
  if (valore === '') return undefined
  return valore
}

/**
 * Numero obbligatorio da un campo del form.
 * Le regole (int, min, positive...) si passano come funzione: z.preprocess restituisce una pipe,
 * su cui non si possono concatenare, quindi vanno applicate al numero prima di avvolgerlo.
 * @param {string} messaggio errore se il campo è vuoto o non è un numero
 * @param {(n: import('zod').ZodNumber) => import('zod').ZodType} [regole] es. (n) => n.int().min(0)
 */
export function numero(messaggio, regole = (n) => n) {
  return z.preprocess(vuotoInUndefined, regole(z.coerce.number({ error: messaggio })))
}
