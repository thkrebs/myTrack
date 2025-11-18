# Sicherheit

- Autorisierung:
  - Methodenbasiert (PreAuthorize): Zugriff für Rolle GOD oder Ressourcenbesitzer.
- Authentifizierung:
  - Empfohlen: JWT Bearer Tokens (Authorization: Bearer <token>)
- CORS:
  - Für produktive Nutzung global konfigurieren (z. B. erlaubte Origins, Methoden, Header).
- Härtung:
  - Eingaben validieren (Jakarta Validation)
  - Fehlerdetails minimieren
  - HTTPS erzwingen
  - Sicherheitsrelevante Header setzen (z. B. Strict-Transport-Security, X-Content-Type-Options)
