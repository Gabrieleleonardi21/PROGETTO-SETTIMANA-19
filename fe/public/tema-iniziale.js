// Legge la scelta salvata da next-themes (chiave "salone.tema") e mette subito la classe "dark" su <html>.
// Senza scelta salvata segue il tema del sistema operativo.
;(function () {
  try {
    var scelta = localStorage.getItem('salone.tema') || 'system'
    var scuro = scelta === 'dark' || (scelta === 'system' && window.matchMedia('(prefers-color-scheme: dark)').matches)
    if (scuro) document.documentElement.classList.add('dark')
  } catch {
    // localStorage non disponibile: resta il tema chiaro
  }
})()
