import { Component } from 'react'

/**
 * Se un figlio va in errore mostra `riserva` invece di far cadere tutta la pagina.
 * Serve per la scena 3D: senza WebGL (browser vecchi, GPU disattivata) si vede un'immagine statica.
 */
export class LimiteErrori extends Component {
  state = { errore: false }

  static getDerivedStateFromError() {
    return { errore: true }
  }

  render() {
    if (this.state.errore) return this.props.riserva
    return this.props.children
  }
}
