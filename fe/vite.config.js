import tailwindcss from '@tailwindcss/vite'
import react from '@vitejs/plugin-react'
import path from 'node:path'
import { defineConfig } from 'vite'

export default defineConfig({
  plugins: [react(), tailwindcss()],
  resolve: {
    alias: { '@': path.resolve(import.meta.dirname, './src') },
  },
  build: {
    // L'unico file oltre 500 KB è la scena 3D (Three.js): è già caricata a parte, solo in home e dopo il
    // resto della pagina, quindi l'avviso scatta solo sopra 1 MB
    chunkSizeWarningLimit: 1000,
    rolldownOptions: {
      output: {
        // Librerie in file separati dal codice dell'app: a ogni nuovo deploy cambia solo il file dell'app
        // e il browser riusa dalla cache quelli delle librerie. Three.js resta nel file della scena 3D.
        codeSplitting: {
          groups: [
            { name: 'vendor-react', test: /node_modules[\\/](react|react-dom|scheduler|react-router)[\\/]/ },
            {
              name: 'vendor-ui',
              test: /node_modules[\\/](motion|motion-dom|motion-utils|framer-motion|radix-ui|@radix-ui|@floating-ui|lucide-react|sonner|next-themes|@tanstack)[\\/]/,
            },
          ],
        },
      },
    },
  },
  server: {
    // In sviluppo /api lo inoltra Vite al backend sulla 8080.
    // In produzione il proxy non esiste: serve VITE_API_URL.
    proxy: {
      '/api': { target: 'http://localhost:8080' },
    },
  },
})
