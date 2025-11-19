# API-Dokumentation (myTrack)

Dieses Paket enthält die vollständige, zippbare Dokumentation der REST-Endpunkte im Paket controller.
- Geeignet als Prompt-Anhang zur Client-API-Generierung
- Enthalten:
  - OVERVIEW.md (Kurzüberblick)
  - AUTHENTICATION.md (Auth-Endpoints)
  - IMEI.md
  - JOURNEYS.md
  - POSITIONS.md
  - API-TOKENS.md
  - ERRORS.md (Fehlerformat, Statuscodes)
  - openapi.yaml (vereinfachte Spezifikation)

Basis-URL
- http(s)://<host>:<port>/api/v1

Authentifizierung
- Typisch per JWT Bearer: Authorization: Bearer <token>
- Optional per X-Api-Token für ausgewählte Endpunkte (Rolle ROLE_API)
