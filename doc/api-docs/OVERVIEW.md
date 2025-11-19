# Übersicht

Controller-Endpoints:
- AuthenticationController: Authentifizierung und Token-Refresh
- ImeiController: IMEI CRUD
- JourneyController: Journeys, Tracks, Parkspots, Cache
- PositionController: Positionen je IMEI und Position CRUD
- ApiTokenController: API-Token des Nutzers verwalten

Allgemeine Sicherheitsprinzipien:
- Methodensicherheit via @PreAuthorize (Rollen, Ownership)
- Zusätzliche Prüfung aktiver IMEI über Interceptor (Request wird abgelehnt, wenn IMEI inaktiv/unbekannt)
