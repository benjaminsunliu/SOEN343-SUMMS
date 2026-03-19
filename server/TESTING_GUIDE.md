# SUMMS Vehicle API - Manual Testing Guide

## Step 1: Start the MySQL Database

Open a PowerShell terminal and navigate to the server folder:

```powershell
cd C:\Users\grego\OneDrive\Documents\Concordia_Garbage\W2026\SOEN343\SOEN343-SUMMS\server
```

Start the MySQL container:

```powershell
docker-compose up
```

**What happens:**
- Docker pulls the MySQL 8.0 image (if not already cached)
- Creates a container with:
  - Database: `summs_db`
  - Root user: `root`
  - Password: `rootpassword`
  - Port: `3307` (mapped to container's 3306)
- Initializes the database volume (`mysql-data`)
- Waits for MySQL to be ready

**Expected output:**
```
summs-mysql-1  | 2026-03-19 10:30:45 0 [System] [MY-015015] [Server] MySQL Server - start.
summs-mysql-1  | ... ready for connections.
```

**Keep this terminal running.** The database must stay active for the app to work.

---

## Step 2: Build and Run the Spring Boot Server

Open a **new PowerShell terminal** and navigate to the server folder:

```powershell
cd C:\Users\grego\OneDrive\Documents\Concordia_Garbage\W2026\SOEN343\SOEN343-SUMMS\server
```

### Understanding the files:

**mvnw.cmd** — Maven Wrapper for Windows
- Automatically downloads and runs Maven
- No need to install Maven globally
- Ensures everyone uses the same version

**pom.xml** — Project configuration
- Defines dependencies (Spring Boot, JPA, MySQL, validation, etc.)
- Specifies Java version (17)
- Configures build plugins

**application.properties** — Runtime configuration
- Database connection URL: `jdbc:mysql://localhost:3307/summs_db`
- Credentials: `root / rootpassword`
- Hibernate auto-update DDL (creates tables automatically)

### Compile and run:

**Option A: One command (compile + run):**
```powershell
.\mvnw.cmd spring-boot:run
```

**Option B: Two commands (compile first, then run):**
```powershell
# Compile the project
.\mvnw.cmd clean compile

# Run the app
.\mvnw.cmd spring-boot:run
```

**Expected output:**
```
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::                2026-03-19
 
Tomcat started on port(s): 8080
Started SummsApplication in 5.123 seconds
```

**Keep this terminal running.** The app must stay active for testing.

---

## Step 3: Test the API Routes

Open a **third PowerShell terminal** and run the test scripts (see `test-vehicles.ps1`).

```powershell
cd C:\Users\grego\OneDrive\Documents\Concordia_Garbage\W2026\SOEN343\SOEN343-SUMMS\server

# Run all tests
.\test-vehicles.ps1
```

---

## Troubleshooting

### "Connection refused" or database errors
- Ensure `docker-compose up` is still running in the first terminal
- Check that port 3307 is not in use: `netstat -ano | findstr 3307`

### "Port 8080 already in use"
- Kill the process: `netstat -ano | findstr 8080` and `taskkill /PID <PID> /F`
- Or change the port in `application.properties`

### "BUILD FAILURE" during mvnw compile
- Check Java version: `java -version` (should be 17+)
- Clear Maven cache: `.\mvnw.cmd clean`

---

## Summary: Full Setup Workflow

**Terminal 1 (Database):**
```powershell
cd server
docker-compose up
# Keep running
```

**Terminal 2 (App Server):**
```powershell
cd server
.\mvnw.cmd spring-boot:run
# Keep running
```

**Terminal 3 (Testing):**
```powershell
cd server
.\test-vehicles.ps1
```

Once all three are running, the API is live at `http://localhost:8080` and ready for requests.
