# Authentifizierung

1) Access-Token erstellen
- POST /api/v1/authenticate
- Request (application/json):
  {
    "username": "string",
    "password": "string"
  }
- 200 OK:
  {
    "jwt": "string",
    "refreshToken": "string"
  }
- Fehler:
  - 400 Bad Request | 401 Unauthorized

2) Access-Token via Refresh-Token erneuern
- POST /api/v1/refresh-token
- Request:
  - Content-Type: text/plain oder application/json
  - Body: "<refreshToken>"
- 200 OK:
  {
    "jwt": "string",
    "refreshToken": "string"
  }
- Fehler:
  - 401 Invalid refresh token
  - 400 Could not refresh token
