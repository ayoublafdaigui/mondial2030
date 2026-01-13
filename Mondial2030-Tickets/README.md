<br>
<img src="Moi.jpg" style="height:200px;margin-right:600px"/>
# Mondial 2030 - Ticket Management System

## 🏟️ Overview

Intelligent ticket management system for FIFA World Cup 2030 (Morocco, Spain, Portugal).

## 🛠️ Tech Stack

- **Java 17+** - Core language
- **JavaFX 21** - Desktop UI framework
- **Hibernate 6** - ORM for database operations
- **PostgreSQL 16** - Primary database
- **Maven** - Build tool
- **Docker** - Containerization

## 🚀 Quick Start

### Prerequisites

- Java 17 or higher
- Maven 3.8+
- Docker & Docker Compose (for containerized deployment)
- PostgreSQL (for local development without Docker)

### Local Development

1. **Start PostgreSQL** (if not using Docker):
   ```bash
   # Create database
   createdb mondial2030
   ```

2. **Configure Database**:
   Edit `src/main/resources/hibernate.cfg.xml` with your database credentials.

3. **Run the Application**:
   ```bash
   mvn clean javafx:run
   ```

### Docker Deployment

1. **Start Services**:
   ```bash
   # Start database only
   docker-compose up -d db
   
   # Start all services
   docker-compose up --build
   ```

2. **With pgAdmin** (optional database UI):
   ```bash
   docker-compose --profile admin up
   ```
   Access pgAdmin at: http://localhost:5050

## 🔐 Default Credentials

### Application Login
- **Admin**: `admin` / `admin` (note: password is actually "admin", not "admin123")

### pgAdmin
- **Email**: admin@mondial2030.com
- **Password**: admin123

## 📁 Project Structure

```
Mondial2030-Tickets/
├── src/main/java/com/mondial2030/
│   ├── app/                    # Application entry points
│   │   ├── MainApp.java        # Main JavaFX application
│   │   └── LegacyDashboard.java# Fallback UI
│   ├── controller/             # JavaFX Controllers
│   │   ├── LoginController.java
│   │   ├── AdminDashboardController.java
│   │   ├── HomeController.java
│   │   └── UserViewController.java
│   ├── dao/                    # Data Access Objects
│   │   ├── UserDAO.java
│   │   ├── TicketDAO.java
│   │   ├── MatchDAO.java
│   │   ├── StadiumDAO.java
│   │   └── TicketTransferDAO.java
│   ├── model/                  # Entity Classes
│   │   ├── User.java
│   │   ├── Ticket.java
│   │   ├── Match.java
│   │   ├── Stadium.java
│   │   └── TicketTransfer.java
│   ├── service/                # Business Logic
│   │   ├── AuthenticationService.java
│   │   ├── FraudDetectionService.java
│   │   ├── BlockchainService.java
│   │   ├── MatchService.java
│   │   └── StadiumService.java
│   └── util/                   # Utilities
│       ├── HibernateUtil.java  # Enhanced with Docker support
│       ├── DatabaseSeeder.java
│       ├── DatabaseHealthCheck.java
│       ├── ValidationUtil.java
│       ├── LanguageManager.java
│       └── QRCodeGenerator.java
├── src/main/resources/
│   ├── fxml/                   # FXML Layouts
│   ├── css/                    # Stylesheets
│   ├── i18n/                   # Internationalization
│   ├── hibernate.cfg.xml       # Local Hibernate config
│   └── hibernate-docker.cfg.xml# Docker Hibernate config
├── docker/
│   └── init-db/                # Database init scripts
│       └── 01-init.sql
├── Dockerfile
├── docker-compose.yml
├── .env.example                # Environment template
├── pom.xml
└── README.md
```

## 🗄️ Database Schema

### Tables

**users**
- `id` (BIGSERIAL PRIMARY KEY)
- `username` (VARCHAR, UNIQUE)
- `password` (VARCHAR, hashed)
- `name` (VARCHAR)
- `email` (VARCHAR, UNIQUE)
- `role` (VARCHAR: ADMIN/USER)
- `created_at` (TIMESTAMP)
- `last_login` (TIMESTAMP)

**stadiums**
- `id` (BIGSERIAL PRIMARY KEY)
- `name` (VARCHAR)
- `city` (VARCHAR)
- `country` (VARCHAR)
- `capacity` (INTEGER)
- `image_url` (VARCHAR)
- `description` (TEXT)
- `year_built` (INTEGER)
- `is_main_venue` (BOOLEAN)

**matches**
- `id` (BIGSERIAL PRIMARY KEY)
- `home_team` (VARCHAR)
- `away_team` (VARCHAR)
- `match_date` (TIMESTAMP)
- `stadium` (VARCHAR)
- `city` (VARCHAR)
- `phase` (VARCHAR: GROUP_STAGE/ROUND_16/QUARTER_FINAL/SEMI_FINAL/FINAL)
- `base_price` (DECIMAL)
- `total_seats` (INTEGER)
- `available_seats` (INTEGER)

**tickets**
- `id` (BIGSERIAL PRIMARY KEY)
- `seat_number` (VARCHAR)
- `seat_zone` (VARCHAR)
- `price` (DECIMAL)
- `category` (VARCHAR: STANDARD/PREMIUM/VIP)
- `status` (VARCHAR: ACTIVE/USED/CANCELLED)
- `blockchain_hash` (VARCHAR, UNIQUE)
- `fraud_score` (DECIMAL)
- `transfer_count` (INTEGER)
- `purchase_date` (TIMESTAMP)
- `last_transfer_date` (TIMESTAMP)
- `owner_id` (BIGINT, FK → users)
- `match_id` (BIGINT, FK → matches)

**ticket_transfers**
- `id` (BIGSERIAL PRIMARY KEY)
- `ticket_id` (BIGINT, FK → tickets)
- `from_user_id` (BIGINT, FK → users)
- `to_user_id` (BIGINT, FK → users)
- `transfer_date` (TIMESTAMP)
- `transfer_price` (DECIMAL)
- `blockchain_tx_hash` (VARCHAR)

## 🎯 Features

### Authentication & Authorization
- Role-based access (ADMIN / USER)
- Secure password hashing (SHA-256)
- Session management

### Admin Dashboard
- Real-time ticket monitoring
- Ticket generation with blockchain hash
- Transfer management
- Fraud detection analysis

### User View
- Personal ticket inventory
- Ticket transfer capability
- Ticket details view

### Fraud Detection (AI Simulation)
- Transfer count analysis
- Rapid transfer detection
- Price anomaly detection
- Suspicious pattern recognition

### Blockchain Security (Simulation)
- Unique hash generation for each ticket
- Transfer transaction hashing
- Hash verification

## 🐳 X11 Forwarding (GUI in Docker)

### Linux
```bash
xhost +local:docker
DISPLAY=$DISPLAY docker-compose up
```

### Windows (VcXsrv)
1. Install VcXsrv
2. Run XLaunch → "Disable access control"
3. `docker-compose up`

### macOS (XQuartz)
1. Install XQuartz
2. Enable network clients in Preferences
3. `xhost +localhost && docker-compose up`

## 📋 API/DAO Methods

### UserDAO
- `saveUser(User)` - Create new user
- `authenticate(username, password)` - Login
- `findByUsername(username)` - Find user
- `getAllUsers()` - List all users

### TicketDAO
- `saveTicket(Ticket)` - Create ticket
- `transferTicket(id, newOwner, price)` - Transfer ownership
- `getHighRiskTickets()` - Get fraud alerts
- `findByBlockchainHash(hash)` - Verify ticket

### MatchDAO
- `saveMatch(Match)` - Create match
- `getUpcomingMatches()` - Future matches
- `getMatchesByPhase(phase)` - Filter by phase

### StadiumDAO (NEW)
- `saveStadium(Stadium)` - Create stadium
- `getAllStadiums()` - List all stadiums
- `getStadiumsByCountry(country)` - Filter by country
- `getMainVenues()` - Get main venues

## ⚙️ Environment Variables

Create a `.env` file based on `.env.example`:

```bash
cp .env.example .env
```

Key variables:
- `DB_HOST` - Database host (`db` for Docker, `localhost` for local)
- `DB_PORT` - Database port (default: 5432)
- `DB_NAME` - Database name
- `DB_USER` - Database username
- `DB_PASSWORD` - Database password
- `HIBERNATE_HBM2DDL_AUTO` - Schema management (update/create/validate)

## 🔧 Troubleshooting

### Database Connection Issues

**Problem**: Application fails to connect to database

**Solutions**:
1. **Check PostgreSQL is running**:
   ```bash
   # For Docker
   docker-compose ps
   docker-compose logs db
   
   # For local PostgreSQL
   pg_isready -h localhost -p 5432
   ```

2. **Verify credentials**: Ensure `.env` file has correct database credentials

3. **Check Docker network**: 
   ```bash
   docker network ls
   docker network inspect mondial2030-network
   ```

### Hibernate SessionFactory Errors

**Problem**: `Failed to create SessionFactory`

**Solutions**:
1. **Check Hibernate configuration**: Verify `hibernate.cfg.xml` or `hibernate-docker.cfg.xml`
2. **Entity mapping**: Ensure all entities are properly annotated and mapped
3. **Database schema**: The application will auto-create tables with `hbm2ddl.auto=update`

### Docker GUI Not Showing

**Problem**: JavaFX application doesn't display in Docker

**Solutions**:

**On Windows**:
1. Install and run VcXsrv
2. Configure XLaunch with "Disable access control"
3. Set `DISPLAY=host.docker.internal:0` in `.env`

**On Linux**:
```bash
xhost +local:docker
export DISPLAY=:0
docker-compose up
```

**On macOS**:
```bash
# Install XQuartz first
xhost +localhost
docker-compose up
```

### Build Errors

**Problem**: Maven build fails

**Solutions**:
1. **Clean build**:
   ```bash
   mvn clean install
   ```

2. **Update dependencies**:
   ```bash
   mvn -U clean install
   ```

3. **Check Java version**:
   ```bash
   java -version  # Should be 17 or higher
   mvn -version   # Should use Java 17+
   ```

### Performance Issues

**Problem**: Application runs slowly

**Solutions**:
1. **Increase Java heap**:
   ```bash
   export JAVA_OPTS="-Xmx1024m -Xms512m"
   ```

2. **Check connection pool**: Verify HikariCP configuration in `hibernate-docker.cfg.xml`

3. **Enable SQL logging**: Set `hibernate.show_sql=false` in production

## 🛡️ Security Considerations

- Passwords are hashed before storage
- Role-based access control
- Blockchain simulation for ticket authenticity
- Fraud scoring for suspicious activity

## 📄 License

© 2030 FIFA World Cup - Morocco, Spain, Portugal

---

**Built with ❤️ for Mondial 2030**
