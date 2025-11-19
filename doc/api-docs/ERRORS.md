# Fehlerformat und Statuscodes

Allgemein
- 400 Bad Request: Ungültige Anfrage/Validierungsfehler
- 401 Unauthorized: Nicht authentifiziert
- 403 Forbidden: Keine Berechtigung
- 404 Not Found: Ressource existiert nicht
- 409 Conflict: Ressource bereits vorhanden
- 422 Unprocessable Entity: Fachliche Einschränkung verletzt
- 500 Internal Server Error: Unerwarteter Fehler

Beispiel-Struktur
- { "message": "Fehlermeldung" }

Spezifische Meldungen (Beispiele)
- 404: "Imei not found with id: {id}" oder "Journey not found with id: {id}"
- 422: "Cannot start journey. No tracker is active"
