# API Tokens

Ressource-Basis: /api/v1/api-tokens
- Authentifizierung: eingeloggter Benutzer
- Rolle: keine spezielle Rolle erforderlich, aber nur eigene Tokens verwaltbar

1) Token anlegen
- POST /api/v1/api-tokens
- Body: ApiTokenDTO (nur description relevant)
- 200 OK: ApiTokenDTO (inkl. token)
- Fehler: 401/403

2) Token widerrufen
- DELETE /api/v1/api-tokens/{tokenId}
- 204 No Content
- Fehler: 401/403/404

3) Eigene Tokens abrufen
- GET /api/v1/api-tokens
- 200 OK: [ApiTokenDTO]
- Fehler: 401/403
