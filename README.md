# PWM GraphQL Gateway (separat deploybar)

Dieses Projekt stellt einen eigenständigen GraphQL‑Server bereit, der die REST‑API deines bestehenden `pwm-backend` konsumiert.

## Endpunkte
- GraphQL: `/graphql`
- GraphiQL: `/graphiql`
- Health: `/actuator/health`

## Umgebung
- `BACKEND_BASE_URL` (z. B. `https://pwm-backend.onrender.com`)

## Deploy (Render)
1. Repo anlegen und dieses Projekt pushen.
2. In Render: **New → Web Service → Docker** (oder **Blueprint** mit `render.yaml`).
3. Env Var `BACKEND_BASE_URL` setzen.
4. Deploy starten.

## Schema (Kurz)
- `viewer: Viewer!` (liefert nur eine ID‑Platzhalter, falls du die JWT nicht parsen willst)
- `vaultItems`, `vaultItem(id)`
- `createVaultItem`, `updateVaultItem`, `deleteVaultItem`

> Hinweis: Der Gateway übergibt den `Authorization`‑Header an dein Backend. Stelle sicher, dass dein Backend CORS/Headers akzeptiert.
