# SUMMS - Quick Start Guide

Welcome to **SUMMS (Smart Urban Mobility Management System)**! This guide will walk you through running the entire application locally.

---

## 📋 Prerequisites

Before you begin, ensure you have the following installed:

- **Docker & Docker Compose** — [Download](https://www.docker.com/products/docker-desktop)
- **Java 17+** — [Download](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)
- **Node.js 18+** — [Download](https://nodejs.org/)
- **Maven** — Usually bundled with Java, or [Download](https://maven.apache.org/download.cgi)

**Verify installations:**
```powershell
java -version          # Should show Java 17+
node -v                # Should show v18+
docker -v              # Should show Docker version
docker-compose -v      # Should show Docker Compose version
```

---

## 🚀 Quick Start (3 Steps)

### **Step 1: Start the Database**

Navigate to the server directory and bring up MySQL using Docker Compose:

```powershell
cd server
docker-compose up -d
```

**What this does:**
- Pulls MySQL 8.0 Docker image
- Creates a container named `summs-mysql` (or similar)
- Exposes database on `localhost:3307`
- Initializes database: `summs_db`
- Creates volume for data persistence

**Verify the database is running:**
```powershell
docker ps
```

You should see a MySQL container running on port 3307.

---

### **Step 2: Start the Backend Server**

In a **new terminal**, from the `server` directory:

```powershell
cd server
.\mvnw.cmd spring-boot:run
```

**What this does:**
- Compiles the Spring Boot application
- Connects to MySQL database at `localhost:3307`
- Starts server on `http://localhost:8080`
- Initializes Hibernate tables (auto-create)

**Wait for this log message:**
```
Started SummsApplication in X.XXX seconds
```

✅ **Backend is ready!** API accessible at `http://localhost:8080`

---

### **Step 3: Start the Frontend**

In a **new terminal**, from the `client` directory:

```powershell
cd client
npm install          # First time only
npm run dev
```

**What this does:**
- Installs React Router dependencies
- Starts Vite dev server on `http://localhost:5173`
- Hot-reloads on file changes

**Wait for this log message:**
```
  VITE v7.x.x  ready in xxx ms
```

✅ **Frontend is ready!** Access at `http://localhost:5173`

---

## 🌐 Accessing the Application

**Open your browser and navigate to:**
```
http://localhost:5173
```

You'll see the SUMMS login page.

---

## 👤 Default Test Users

Use these credentials to test the system:

### **Regular User**
- **Email:** `user@example.com`
- **Password:** `password123`
- **Role:** USER
- **Features:** Can search vehicles, make reservations, access dashboards

### **Provider/Admin**
- **Email:** `provider@example.com`
- **Password:** `password123`
- **Role:** ADMIN
- **Features:** All user features + Admin analytics dashboard

*(These users must be created via registration page or database seed)*

---

## 📊 Testing the Analytics System

Once logged in as **ADMIN**, you can test the new analytics features:

### **1. Generate API Traffic**
- Search for vehicles: `Dashboard → Find a Vehicle`
- Make a reservation: `Dashboard → Book / Reserve`
- Check public transit: `Services → Public Transit`

These actions trigger the **gateway analytics interceptor**.

### **2. View Rental Analytics**
```
Sidebar → Insights → Analytics
```
Shows:
- Total active rentals count
- Breakdown by vehicle type (Car, Bike, etc.)

### **3. View System Analytics (Admin Only)**
```
Sidebar → Admin → System Analytics
```
Shows a two-panel dashboard:
- **Left:** Rental analytics (active rentals, vehicle breakdown)
- **Right:** Gateway analytics (API request counts by time window)

---

## 🛑 Stopping the Application

### **Stop Backend Server**
Press `Ctrl+C` in the Maven terminal

### **Stop Frontend Dev Server**
Press `Ctrl+C` in the Node terminal

### **Stop Database**
```powershell
cd server
docker-compose down
```

**To preserve database data:**
```powershell
docker-compose down  # Data persists in named volume
```

**To remove all data:**
```powershell
docker-compose down -v  # -v removes volumes
```

---

## 📦 Running Containers in Background

If you want to run everything in one terminal, use `docker-compose` detached mode:

```powershell
cd server
docker-compose up -d        # Starts DB in background

cd ../..
cd server
.\mvnw.cmd spring-boot:run  # Terminal blocks on backend

# In another terminal:
cd client
npm run dev                  # Frontend dev server
```

Or create shell scripts (see `run-all.sh` and `run-all.ps1` if provided).

---

## 🐛 Troubleshooting

### **"Port 3307 already in use"**
```powershell
# Kill existing container
docker-compose down

# Or change port in docker-compose.yml:
# ports:
#   - "3308:3306"
```

### **"Cannot connect to database"**
- Verify Docker is running: `docker ps`
- Check logs: `docker logs <container_name>`
- Ensure port 3307 is accessible

### **"npm ERR! No matching version"**
```powershell
cd client
npm install --legacy-peer-deps
```

### **"Java not found"**
- Install Java 17+ and add to PATH
- Restart terminal for PATH changes to take effect

### **"Port 8080 already in use"**
```powershell
# Kill process using port 8080
netstat -ano | findstr :8080
taskkill /PID <PID> /F
```

---

## 📁 Project Structure

```
SOEN343-SUMMS/
├── client/                 # React Router frontend
│   ├── app/
│   │   ├── routes/        # Page components
│   │   ├── utils/         # API, auth helpers
│   │   └── root.tsx       # Navigation layout
│   ├── package.json
│   └── vite.config.ts
├── server/                 # Spring Boot backend
│   ├── src/main/java/     # Application code
│   ├── src/test/java/     # Unit tests
│   ├── pom.xml
│   ├── docker-compose.yml # MySQL configuration
│   └── mvnw.cmd          # Maven wrapper
└── README.md
```

---

## 🧪 Running Tests

### **Unit Tests**
```powershell
cd server
.\mvnw.cmd test
```

### **Specific Test Suite**
```powershell
.\mvnw.cmd test -Dtest="RentalAnalyticsServiceTest,GatewayAnalyticsServiceTest"
```

---

## 📚 Additional Resources

- **Backend API Docs:** [API Documentation](server/docs/reservations-api.md)
- **Analytics Implementation:** See `server/src/main/java/com/thehorselegend/summs/application/service/analytics/`
- **Frontend Routes:** See `client/app/routes.ts`

---

## 💡 Tips

1. **Keep terminals separate** — Use one terminal per service (DB, Backend, Frontend)
2. **Check logs** — Errors usually appear in terminal output
3. **Hard refresh browser** — `Ctrl+Shift+R` if styles/JS seem cached
4. **Check dev tools** — `F12` for browser console if requests fail
5. **Database persists** — Docker volume survives `docker-compose down`

---

## ✅ Success Checklist

- [ ] MySQL running (`docker ps` shows container)
- [ ] Backend server started (logs show "Started SummsApplication")
- [ ] Frontend dev server started (Vite shows "ready in X ms")
- [ ] Can access `http://localhost:5173` in browser
- [ ] Can log in with test credentials
- [ ] Can navigate dashboard and see vehicles
- [ ] Admin user can access analytics dashboard
- [ ] Unit tests pass (`.\mvnw.cmd test`)

You're all set! 🎉

