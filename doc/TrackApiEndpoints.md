# myTrack REST API

Basis-URL
- http(s)://<host>:<port>/api/v1

Ressource: IMEIs

1) IMEI anlegen
- POST /api/v1/imeis
- Request (application/json):
  {
    "field1": "string",
    "field2": "string"
  }
- Responses:
  - 201 Created:
    {
      "id": 123,
      "field1": "string",
      "field2": "string"
    }
  - 400/422: Validierungsfehler
  - 401/403: Unauthenticated/Unauthorized
- Beispiel:
  curl -X POST "<base>/imeis" -H "Content-Type: application/json" -d '{"field1":"v","field2":"v"}'

2) Alle IMEIs des aktuellen Nutzers
- GET /api/v1/imeis
- Responses:
  - 200 OK: [ { "id": 1, "field1": "...", "field2": "..." }, ... ]
  - 401/403
- Beispiel:
  curl -X GET "<base>/imeis"

3) IMEI nach ID
- GET /api/v1/imeis/{id}
- Path: id (Long)
- Responses:
  - 200 OK: { "id": 123, "field1": "...", "field2": "..." }
  - 404 Not Found
  - 401/403
- Beispiel:
  curl -X GET "<base>/imeis/123"

4) IMEI voll aktualisieren
- PUT /api/v1/imeis/{id}
- Path: id (Long)
- Request (application/json):
  {
    "field1": "new",
    "field2": "new"
  }
- Responses:
  - 200 OK: aktualisiertes DTO
  - 404/400/401/403
- Beispiel:
  curl -X PUT "<base>/imeis/123" -H "Content-Type: application/json" -d '{"field1":"n","field2":"n"}'

5) IMEI teilweise aktualisieren
- PATCH /api/v1/imeis/{id}
- Path: id (Long)
- Request (application/json):
  {
    "field1": "updatedValue"
  }
- Responses:
  - 200 OK: aktualisiertes DTO
  - 404/400/401/403
- Beispiel:
  curl -X PATCH "<base>/imeis/123" -H "Content-Type: application/json" -d '{"field1":"u"}'

6) IMEI löschen
- DELETE /api/v1/imeis/{id}
- Path: id (Long)
- Responses:
  - 204 No Content
  - 404/401/403
- Beispiel:
  curl -X DELETE "<base>/imeis/123"

Fehlerformate (Beispiele)
- 404:
  { "error": "Imei not found with id: {id}" }
- 400/422:
  { "error": "Validation failed", "details": { "field1": "must not be blank" } }

Sicherheit
- Autorisierung:
  - hasRole('GOD') oder Ownership-Prüfung (isOwner)
- Authentifizierung:
  - abhängig von deiner Security-Konfiguration (z. B. JWT/Bearer)
- CORS:
  - Siehe SECURITY.md
