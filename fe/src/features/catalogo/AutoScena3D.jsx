import { useRef } from 'react'
import { Canvas, useFrame } from '@react-three/fiber'
import { ContactShadows, Float, OrbitControls, RoundedBox } from '@react-three/drei'

// Auto low-poly costruita con forme semplici: niente modelli 3D da scaricare, niente licenze da gestire.
// Tutte le misure sono in "metri" della scena; l'auto è lunga circa 4.

const RUOTE = [
  [1.25, 0.38, 0.82],
  [1.25, 0.38, -0.82],
  [-1.25, 0.38, 0.82],
  [-1.25, 0.38, -0.82],
]

function Ruota({ posizione }) {
  return (
    <group position={posizione} rotation={[Math.PI / 2, 0, 0]}>
      <mesh>
        <cylinderGeometry args={[0.38, 0.38, 0.28, 32]} />
        <meshStandardMaterial color="#111318" roughness={0.9} />
      </mesh>
      {/* Cerchione: disco metallico leggermente sporgente */}
      <mesh position={[0, 0.145, 0]}>
        <cylinderGeometry args={[0.22, 0.22, 0.02, 24]} />
        <meshStandardMaterial color="#c9ced6" metalness={0.9} roughness={0.25} />
      </mesh>
      <mesh position={[0, -0.145, 0]}>
        <cylinderGeometry args={[0.22, 0.22, 0.02, 24]} />
        <meshStandardMaterial color="#c9ced6" metalness={0.9} roughness={0.25} />
      </mesh>
    </group>
  )
}

function Fari({ x, colore, intensita }) {
  return [0.55, -0.55].map((z) => (
    <mesh key={z} position={[x, 0.72, z]}>
      <boxGeometry args={[0.04, 0.12, 0.34]} />
      <meshStandardMaterial color={colore} emissive={colore} emissiveIntensity={intensita} />
    </mesh>
  ))
}

function Auto({ colore, gira }) {
  const gruppo = useRef(null)
  // Rotazione lenta sul posto; ferma se l'utente ha chiesto di ridurre il movimento
  useFrame((_, delta) => {
    if (gira && gruppo.current) gruppo.current.rotation.y += delta * 0.35
  })

  return (
    <group ref={gruppo} rotation={[0, -0.6, 0]}>
      {/* Scocca */}
      <RoundedBox args={[4, 0.62, 1.8]} radius={0.22} smoothness={4} position={[0, 0.68, 0]}>
        <meshStandardMaterial color={colore} metalness={0.55} roughness={0.28} />
      </RoundedBox>
      {/* Abitacolo arretrato come una berlina sportiva: fascia dei vetri scura e tetto in colore carrozzeria */}
      <RoundedBox args={[2.1, 0.42, 1.52]} radius={0.18} smoothness={4} position={[-0.25, 1.15, 0]}>
        <meshStandardMaterial color="#0d1117" metalness={0.8} roughness={0.08} />
      </RoundedBox>
      <RoundedBox args={[1.8, 0.12, 1.44]} radius={0.06} smoothness={4} position={[-0.3, 1.4, 0]}>
        <meshStandardMaterial color={colore} metalness={0.55} roughness={0.28} />
      </RoundedBox>
      <Fari x={2.01} colore="#fff4d6" intensita={2.2} />
      <Fari x={-2.01} colore="#ff2a2a" intensita={1.6} />
      {RUOTE.map((p) => (
        <Ruota key={p.join()} posizione={p} />
      ))}
    </group>
  )
}

/**
 * Scena dell'hero: auto che gira lentamente su una pedana, trascinabile col mouse.
 * @param {{ colore: string, scuro: boolean, gira: boolean }} props
 */
export default function AutoScena3D({ colore, scuro, gira }) {
  let luceAmbiente = 0.7
  if (scuro) luceAmbiente = 0.35

  return (
    // Niente mappa delle ombre del renderer (pesante da compilare e da disegnare): l'ombra sotto l'auto
    // la fa ContactShadows. Risoluzione massima 1.5x: sugli schermi retina basta e alleggerisce la GPU.
    <Canvas
      dpr={[1, 1.5]}
      camera={{ position: [5.5, 2.6, 5.5], fov: 35 }}
      // Canvas decorativo: lo screen reader lo salta, il testo dell'hero dice già tutto
      aria-hidden="true"
    >
      <ambientLight intensity={luceAmbiente} />
      <directionalLight position={[5, 8, 5]} intensity={2.2} />
      <directionalLight position={[-6, 3, -4]} intensity={0.8} color="#8fb4ff" />
      <Float speed={1.2} rotationIntensity={0} floatIntensity={0.35} floatingRange={[0, 0.12]}>
        <Auto colore={colore} gira={gira} />
      </Float>
      <ContactShadows position={[0, 0, 0]} opacity={0.55} scale={10} blur={2.4} far={3} />
      {/* Solo rotazione col trascinamento: niente zoom, così la rotella scorre la pagina */}
      <OrbitControls enableZoom={false} enablePan={false} minPolarAngle={0.9} maxPolarAngle={1.45} />
    </Canvas>
  )
}
