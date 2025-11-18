# Setup & Build

Voraussetzungen
- Java 23
- Gradle (Wrapper vorhanden)
- Docker (optional)

Lokaler Start
- ./gradlew clean build
- ./gradlew bootRun
- Standard-Port: 8080 (falls nicht konfiguriert)

Docker (Beispiel)
- docker build -t mytrack:latest .
- docker run -p 8080:8080 mytrack:latest

Konfiguration
- Anwendungseigenschaften über application.properties/yaml oder Umgebungsvariablen
- DB-Konfiguration gemäß JPA/Hibernate-Setup

Health Check
- Optional via actuator falls aktiviert (z. B. /actuator/health)
