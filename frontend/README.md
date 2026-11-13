# Restaurant Finder Frontend

React, TypeScript und Vite. Voraussetzung: Node.js 24 LTS mit npm.

## Lokal starten

Im Projektordner:

```powershell
cd frontend
npm ci
npm run dev
```

Die Seite ist unter http://localhost:5173 erreichbar. Das Spring-Boot-Backend
muss separat auf Port 8080 laufen. Vite leitet `/api` an
`http://localhost:8080` weiter; eine CORS-Freigabe ist lokal nicht erforderlich.

Die Startseite fragt `/api/health` ab und zeigt den Backend-/MongoDB-Status.
Bei Fehlern oder einem Timeout nach zehn Sekunden erscheint eine Fehlermeldung.
Der Status kann mit dem Button erneut abgefragt werden.

Die MongoDB-Verbindung bleibt ausschliesslich im Backend. Keine Datenbank-URI
oder Zugangsdaten in Frontend-Dateien oder `VITE_`-Umgebungsvariablen ablegen:
Diese waeren im Browser sichtbar.

## Qualitaetspruefung und Build

```powershell
npm run lint
npm run build
```

Der Build liegt in `dist`. Der Entwicklungsproxy ist nicht Teil des Builds:
Im Deployment muss der Webserver `/api` an das Backend weiterleiten.

## Zusammenarbeit

`package-lock.json` mit dem Projekt teilen; alle Teammitglieder installieren mit
`npm ci` dieselben Abhaengigkeiten. `node_modules` und `dist` bleiben lokal
(siehe `.gitignore`). UI und Statusabfrage stehen in `src/App.tsx`,
das Layout in `src/App.css` und globale Stile in `src/index.css`.
