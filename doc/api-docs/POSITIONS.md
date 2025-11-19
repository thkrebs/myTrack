# Positions API

Ressourcen:
- /api/v1/imeis/{imei}/positions
- /api/v1/positions

1) Letzte Position(en) zu IMEI
- GET /api/v1/imeis/{imei}/positions/last
- Security: hasRole('GOD') oder isOwner(imei)
- 200 OK: Iterable<PositionDTO>
- Fehler: 401/403

2) Alle Positionen zu IMEI (optional gefiltert)
- GET /api/v1/imeis/{imei}/positions
- Query (optional):
  - from: Datum/Zeit
  - to: Datum/Zeit
- Security: hasRole('GOD') oder isOwner(imei)
- 200 OK: Iterable<PositionDTO>
- Fehler: 401/403

3) Position anlegen
- POST /api/v1/positions
- Security: hasRole('GOD')
- Body: Position (Entity)
- 201 Created: PositionDTO
- Fehler: 400/401/403

4) Position per ID abrufen
- GET /api/v1/positions/{id}
- Security: hasRole('GOD')
- 200 OK: PositionDTO
- Fehler: 404/401/403

5) Position ersetzen
- PUT /api/v1/positions/{id}
- Security: hasRole('GOD')
- Body: Position (Entity)
- 200 OK: PositionDTO
- Fehler: 404/400/401/403

6) Position löschen
- DELETE /api/v1/positions/{id}
- Security: hasRole('GOD')
- 204 No Content
- Fehler: 404/401/403
