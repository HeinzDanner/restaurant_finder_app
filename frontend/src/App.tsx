import { useEffect, useState } from 'react'
import './App.css'

type ConnectionState =
  | { kind: 'loading' }
  | { kind: 'connected' }
  | { kind: 'error'; message: string }

function App() {
  const [connection, setConnection] = useState<ConnectionState>({ kind: 'loading' })
  const [checkId, setCheckId] = useState(0)

  useEffect(() => {
    const controller = new AbortController()

    async function checkConnection() {
      try {
        const response = await fetch('/api/health', {
          signal: AbortSignal.any([controller.signal, AbortSignal.timeout(10000)]),
          cache: 'no-store',
        })
        if (response.status === 503) {
          throw new Error('Das Backend meldet: MongoDB ist nicht erreichbar.')
        }
        if (!response.ok) {
          throw new Error(`Das Backend antwortet mit HTTP ${response.status}.`)
        }

        const data: unknown = await response.json()
        if (
          typeof data !== 'object' ||
          data === null ||
          !('status' in data) ||
          data.status !== 'ok' ||
          !('database' in data) ||
          data.database !== 'connected'
        ) {
          throw new Error('Das Backend hat einen unerwarteten Status geliefert.')
        }
        if (!controller.signal.aborted) {
          setConnection({ kind: 'connected' })
        }
      } catch (error) {
        if (!controller.signal.aborted) {
          setConnection({
            kind: 'error',
            message:
              error instanceof Error
                ? error.message
                : 'Die Verbindung zum Backend konnte nicht geprueft werden.',
          })
        }
      }
    }

    void checkConnection()
    return () => controller.abort()
  }, [checkId])

  function retryConnection() {
    setConnection({ kind: 'loading' })
    setCheckId((value) => value + 1)
  }

  return (
    <main className="app">
      <header className="intro">
        <p className="eyebrow">Restaurant Finder</p>
        <h1>Gutes Essen beginnt hier.</h1>
        <p>Die Basis steht. Als Naechstes finden hier eure Restaurants Platz.</p>
      </header>

      <section className="status-card" aria-labelledby="status-heading">
        <h2 id="status-heading">Verbindungsstatus</h2>
        <div role="status" aria-live="polite">
          {connection.kind === 'loading' && <p>Backend und MongoDB werden geprueft ...</p>}
          {connection.kind === 'connected' && (
            <p className="status-success">Backend erreichbar. MongoDB verbunden.</p>
          )}
          {connection.kind === 'error' && (
            <div className="status-error">
              <p>{connection.message}</p>
              <p>Pruefe, ob das Backend auf Port 8080 laeuft und MongoDB erreichbar ist.</p>
            </div>
          )}
        </div>
        <button
          type="button"
          onClick={retryConnection}
          disabled={connection.kind === 'loading'}
        >
          Erneut pruefen
        </button>
      </section>
    </main>
  )
}

export default App
