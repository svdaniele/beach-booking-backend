# 🏖️ Beach Booking API - Backend

Sistema Multi-Tenant per la gestione delle prenotazioni di ombrelloni per stabilimenti balneari.

## 📋 Prerequisiti

- **Java 17+** (JDK)
- **Maven 3.8+**
- **PostgreSQL 14+**
- **Git**

## 🚀 Quick Start

### 1. Clona il Repository

```bash
git clone <repository-url>
cd beach-booking-backend
```

### 2. Setup Database

Crea il database PostgreSQL:

```bash
# Accedi a PostgreSQL
psql -U postgres

# Crea il database
CREATE DATABASE beachbooking;
\q
```

Esegui lo script di inizializzazione:

```bash
psql -U postgres -d beachbooking -f src/main/resources/db/init-database.sql
```

### 3. Configurazione

Copia il file di configurazione:

```bash
cp src/main/resources/application.yml.example src/main/resources/application.yml
```

Modifica `application.yml` con le tue credenziali:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/beachbooking
    username: postgres
    password: your_password

jwt:
  secret: your-very-long-secret-key-min-256-bits
```

### 4. Build & Run

Con Maven:

```bash
# Build
./mvnw clean package -DskipTests

# Run
./mvnw spring-boot:run
```

Oppure con JAR:

```bash
# Build JAR
./mvnw clean package

# Run JAR
java -jar target/beach-booking-0.0.1-SNAPSHOT.jar
```

L'applicazione sarà disponibile su: **http://localhost:8080**

## 🧪 Credenziali di Test

### Lido Marechiaro (Tenant 1)
- **Admin:** `admin@lidomarechiaro.it` / `Admin123!`
- **Staff:** `staff@lidomarechiaro.it` / `Admin123!`
- **Cliente:** `cliente@test.it` / `Admin123!`

### Bagni Napoli (Tenant 2)
- **Admin:** `admin@bagninapoli.it` / `Admin123!`

## 📡 API Endpoints

### Autenticazione

#### POST `/api/auth/login`
Login utente

**Request:**
```json
{
  "email": "admin@lidomarechiaro.it",
  "password": "Admin123!"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "user": {
    "id": "uuid",
    "email": "admin@lidomarechiaro.it",
    "nome": "Mario",
    "cognome": "Rossi",
    "ruolo": "TENANT_ADMIN",
    "tenantId": "uuid",
    "tenantSlug": "lido-marechiaro"
  }
}
```

#### POST `/api/auth/register/tenant`
Registra nuovo stabilimento

**Request:**
```json
{
  "nomeStabilimento": "Lido Azzurro",
  "email": "info@lidoazzurro.it",
  "password": "SecurePass123!",
  "nomeAdmin": "Giovanni",
  "cognomeAdmin": "Verdi",
  "telefono": "3331234567",
  "indirizzo": "Via Mare, 1",
  "citta": "Napoli",
  "provincia": "NA",
  "cap": "80100"
}
```

#### POST `/api/auth/register/customer`
Registra nuovo cliente (richiede header X-Tenant-ID o subdomain)

**Headers:**
```
X-Tenant-ID: 11111111-1111-1111-1111-111111111111
```

**Request:**
```json
{
  "email": "nuovo@cliente.it",
  "password": "Password123!",
  "nome": "Luca",
  "cognome": "Neri",
  "telefono": "3339876543"
}
```

#### GET `/api/auth/me`
Ottieni utente corrente

**Headers:**
```
Authorization: Bearer <token>
```

### Ombrelloni

#### GET `/api/ombrelloni`
Lista ombrelloni del tenant

**Headers:**
```
Authorization: Bearer <token>
```

#### POST `/api/ombrelloni`
Crea ombrellone

**Headers:**
```
Authorization: Bearer <token>
```

**Request:**
```json
{
  "numero": 51,
  "fila": "D",
  "tipo": "PREMIUM",
  "descrizione": "Ombrellone con vista mare",
  "posizioneX": 100,
  "posizioneY": 350
}
```

#### GET `/api/ombrelloni/{id}`
Dettagli ombrellone

#### PUT `/api/ombrelloni/{id}`
Aggiorna ombrellone

#### DELETE `/api/ombrelloni/{id}`
Elimina ombrellone

### Prenotazioni

#### GET `/api/prenotazioni`
Lista prenotazioni del tenant

**Query Parameters:**
- `stato`: PENDING, CONFIRMED, PAID, CANCELLED, COMPLETED
- `dataInizio`: YYYY-MM-DD
- `dataFine`: YYYY-MM-DD

#### POST `/api/prenotazioni`
Crea prenotazione

**Request:**
```json
{
  "userId": "uuid",
  "ombrelloneId": "uuid",
  "dataInizio": "2025-07-01",
  "dataFine": "2025-07-07",
  "tipoPrenotazione": "SETTIMANALE",
  "note": "Preferenza prima fila"
}
```

#### GET `/api/prenotazioni/{id}`
Dettagli prenotazione

#### PUT `/api/prenotazioni/{id}/confirm`
Conferma prenotazione

#### PUT `/api/prenotazioni/{id}/pay`
Marca come pagata

#### PUT `/api/prenotazioni/{id}/cancel`
Cancella prenotazione

#### GET `/api/prenotazioni/disponibili`
Ombrelloni disponibili in un periodo

**Query Parameters:**
- `dataInizio`: YYYY-MM-DD (required)
- `dataFine`: YYYY-MM-DD (required)

## 🔐 Autenticazione Multi-Tenant

L'API supporta diverse strategie per identificare il tenant:

### 1. Da JWT Token (Raccomandato)
Il token contiene il `tenantId` come claim:
```
Authorization: Bearer <token>
```

### 2. Da Header Custom
```
X-Tenant-ID: 11111111-1111-1111-1111-111111111111
```

### 3. Da Subdomain
```
Host: lido-marechiaro.beachbooking.com
```
Il sistema estrae automaticamente "lido-marechiaro" come slug tenant.

### 4. Da Query Parameter
```
GET /api/ombrelloni?tenantSlug=lido-marechiaro
```

## 📊 Database Schema

### Principali Tabelle

- **tenants**: Stabilimenti balneari
- **users**: Utenti (admin, staff, clienti)
- **ombrelloni**: Ombrelloni disponibili
- **prenotazioni**: Prenotazioni effettuate
- **pagamenti**: Pagamenti associati alle prenotazioni

### Relazioni

```
tenants (1) ─── (N) users
tenants (1) ─── (N) ombrelloni
tenants (1) ─── (N) prenotazioni
prenotazioni (1) ─── (1) pagamenti
```

## 🛡️ Sicurezza

### Ruoli Utente

- **SUPER_ADMIN**: Amministratore della piattaforma
- **TENANT_ADMIN**: Proprietario dello stabilimento
- **STAFF**: Personale dello stabilimento
- **CUSTOMER**: Cliente

### Protezione Endpoints

Gli endpoint sono protetti in base al ruolo:

- `/api/admin/**`: Solo SUPER_ADMIN
- `/api/tenant-admin/**`: TENANT_ADMIN e SUPER_ADMIN
- `/api/staff/**`: STAFF, TENANT_ADMIN, SUPER_ADMIN
- Altri endpoint: Tutti gli utenti autenticati

## 🧪 Testing

### Test con cURL

Login:
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@lidomarechiaro.it",
    "password": "Admin123!"
  }'
```

Lista ombrelloni:
```bash
curl -X GET http://localhost:8080/api/ombrelloni \
  -H "Authorization: Bearer <your-token>"
```

### Test con Postman

Importa la collection Postman disponibile in `/postman/BeachBooking.postman_collection.json`

## 📦 Deployment

### Railway

```bash
# Installa Railway CLI
npm i -g @railway/cli

# Login
railway login

# Deploy
railway up
```

### Render

1. Connetti il repository GitHub
2. Imposta le variabili d'ambiente
3. Deploy automatico

### Docker (Opzionale)

```dockerfile
# Dockerfile
FROM eclipse-temurin:17-jdk-alpine
VOLUME /tmp
COPY target/*.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
```

Build e run:
```bash
docker build -t beach-booking-api .
docker run -p 8080:8080 beach-booking-api
```

## 🔧 Configurazione Avanzata

### Variabili d'Ambiente

```bash
# Database
DATABASE_URL=jdbc:postgresql://localhost:5432/beachbooking
DATABASE_USERNAME=postgres
DATABASE_PASSWORD=yourpassword

# JWT
JWT_SECRET=your-256-bit-secret-key-here
JWT_EXPIRATION=86400000

# Email (opzionale)
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password

# Application
APP_BASE_URL=http://localhost:8080
FRONTEND_URL=http://localhost:3000
```

### Profili Spring

- **dev**: Sviluppo locale (crea/drop tabelle)
- **prod**: Produzione (solo validate schema)

Attiva profilo:
```bash
java -jar app.jar --spring.profiles.active=prod
```

## 📝 TODO

- [ ] Implementare EmailService per notifiche
- [ ] Aggiungere Swagger/OpenAPI documentation
- [ ] Implementare cache con Redis
- [ ] Aggiungere rate limiting
- [ ] Test unitari e integration tests
- [ ] CI/CD pipeline
- [ ] Monitoring con Actuator

## 🤝 Contribuire

1. Fork il progetto
2. Crea un branch (`git checkout -b feature/AmazingFeature`)
3. Commit i cambiamenti (`git commit -m 'Add some AmazingFeature'`)
4. Push al branch (`git push origin feature/AmazingFeature`)
5. Apri una Pull Request

## 📄 Licenza

Questo progetto è sotto licenza MIT.

## 📞 Supporto

Per supporto, contatta: support@beachbooking.com

---

**Made with ☕ and 🏖️ in Italy**