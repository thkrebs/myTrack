# Journeys API

Ressource-Basis: /api/v1/journeys

1) Track der Journey als GeoJSON (mit Parkspots, optional conceal)
- GET /api/v1/journeys/{journey}/track
- Query-Parameter (optional):
  - from: Datum/Zeit
  - to: Datum/Zeit
  - conceal: true|false (Default abhängig vom Status der Journey)
- Security: hasRole('GOD') oder hasAuthority('ROLE_API') oder isOwner(journey)
- 200 OK: application/json (GeoJSON FeatureCollection)
- Fehler: 401/403/404

2) Journey anlegen
- POST /api/v1/journeys
- Body: CreateJourneyDTO (application/json)
- 201 Created: JourneyDTO
- Fehler: 400/401/403

3) Journey per ID abrufen
- GET /api/v1/journeys/{id}
- Security: hasRole('GOD') oder isOwner(id)
- 200 OK: JourneyDTO
- 404/401/403

4) Journey aktualisieren (vollständig)
- PUT /api/v1/journeys/{id}
- Security: hasRole('GOD') oder isOwner(id)
- Body: CreateJourneyDTO
- 200 OK: JourneyDTO
- 404/400/401/403

5) Journey starten
- PUT /api/v1/journeys/{id}/start
- Security: hasRole('GOD') oder isOwner(id)
- 200 OK: JourneyDTO
- Fehler: 400/401/403/404, 422 wenn kein aktiver Tracker

6) Journey beenden
- PUT /api/v1/journeys/{id}/end
- Security: hasRole('GOD') oder isOwner(id)
- 200 OK: JourneyDTO
- Fehler: 400/401/403/404

7) Journey löschen
- DELETE /api/v1/journeys/{id}
- Security: hasRole('GOD') oder isOwner(id)
- 204 No Content
- Fehler: 404/401/403

8) Overnight-Parking anlegen
- POST /api/v1/journeys/{journeyId}/overnight-parking
- Params:
  - name: string (required)
  - description: string (optional)
  - createWPPost: boolean (optional)
  - date: yyyy-MM-dd (optional)
- Security: hasRole('GOD') oder isOwner(journeyId)
- 201 Created: ParkSpotDTO
- Fehler: 400/401/403/404/409

9) Nahegelegene Parkspots
- GET /api/v1/journeys/{journeyId}/nearbyParkspots
- Query:
  - distance: Long (optional, Default 50)
- Security: hasRole('GOD') oder isOwner(journeyId)
- 200 OK: [ParkSpotDTO]
- Fehler: 401/403/404

10) Overnight-Parking aktualisieren
- PUT /journeys/{journeyId}/overnight-parking
- Security: hasRole('GOD') oder isOwner(journeyId)
- Body: OvernightParkingDTO
- 200 OK: OvernightParkingDTO
- Fehler: 400/401/403/404

11) Journey-Cache leeren (alle Journeys)
- POST /api/v1/journeys/clear-cache
- Security: hasRole('GOD') oder hasRole('ADMIN')
- 200 OK: { status, message }
