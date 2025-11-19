# IMEI API

Ressource-Basis: /api/v1/imeis

1) IMEI anlegen
- POST /api/v1/imeis
- Body: ImeiDTO (application/json)
- 201 Created: Imei (persistierte Entity-Repräsentation)
- Fehler: 400/422, 401/403

2) IMEIs des aktuellen Nutzers
- GET /api/v1/imeis
- 200 OK: [ImeiDTO]
- Fehler: 401/403

3) IMEI per ID abrufen
- GET /api/v1/imeis/{id}
- Security: hasRole('GOD') oder isOwner(id)
- 200 OK: ImeiDTO
- 404 Not Found
- 401/403

4) IMEI vollständig aktualisieren
- PUT /api/v1/imeis/{id}
- Security: hasRole('GOD') oder isOwner(id)
- Body: ImeiDTO
- 200 OK: ImeiDTO
- Fehler: 404/400/401/403

5) IMEI teilweise aktualisieren
- PATCH /api/v1/imeis/{id}
- Security: hasRole('GOD') oder isOwner(id)
- Body: Map<String, Object> (zu ändernde Felder)
- 200 OK: ImeiDTO
- Fehler: 404/400/401/403

6) IMEI löschen
- DELETE /api/v1/imeis/{id}
- Security: hasRole('GOD') oder isOwner(id)
- 204 No Content
- Fehler: 404/401/403
