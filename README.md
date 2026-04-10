# Smart Urban Mobility Management System

Smart Urban Mobility Management System (SUMMS) is a full-stack project with a React frontend and Spring Boot backend.

## Team members
| Name | Student ID| GitHub Username | Role |
|-----------------|-----------------|-----------------|-----------------|
| Benjamin Liu    | 40280899  | benjaminsunliu| Team Leader / Full-Stack |
| Vincent de Serres | 40272920 | vinnythepoo2 | UI/UX - Frontend |
| Madison Luu | 40282381 | maddie-luu | Database / Documentation |
| Emily Ng | 40285171 | enyc24 | Full-Stack |
| Gregory Sacciadis | 40207512 | SacciadisG | Backend / QA |
| Aditi Abhaysingh Ingle | 40266449 | aditiingle | CI/CD and Backend |

## Features
### Base features
- Authentication and role-based access (user, provider, city/admin views)
- Vehicle discovery and reservation workflow
- Active trip and trip summary flow
- Parking support and public transit integration
- Provider and city analytics dashboards

### Extra features
- Context-aware vehicle search with weather-aware recommendations
- Sustainability insights with trip-level CO2 savings tracking

## System Architecture

### Architectural choice
SUMMS uses a layered, modular monolith architecture:
- Client layer: React frontend for UI, routing, and user interaction.
- API layer: Spring Boot controllers exposing REST endpoints.
- Business layer: application services containing reservation, trip, payment, and analytics logic.
- Data layer: Spring Data JPA repositories with MySQL persistence.

## Design Patterns

For the SUMMS implementation, we used these patterns to improve flexibility, maintainability, and scalability:

- **Factory Pattern**: `UserFactory` and `VehicleFactory` centralize object creation (`createCitizen`, `createProvider`, `createCar`, `createScooter`, `createBicycle`) so initialization rules stay consistent.
- **Strategy Pattern**: role-based registration (`UserRegistrationStrategy` implementations) and payment method processing (`CreditCardPaymentStrategy`, `PaypalPaymentStrategy`, `WalletPaymentStrategy`) avoid large conditional blocks.
- **Facade Pattern**: `AdminAnalyticsService` provides one entry point for analytics by coordinating rental and gateway analytics services.
- **Adapter Pattern**: parking and transit integrations are abstracted behind interfaces (`IParkingService`, `TransitService`) with interchangeable adapters such as `MockParkingAdapter`, `DatabaseParkingAdapter`, `GoogleTransitAdapter`, and `RealTransitAdapter`.
- **Decorator Pattern**: payment adjustments are composed dynamically with decorators (`ServiceFeeDecorator`, `TaxDecorator`, `InsuranceFeeDecorator`, `DiscountDecorator`).
- **Template Method Pattern**: `ReservationCreationTemplate` defines a common reservation workflow, specialized by `VehicleReservationService` and `ParkingReservationService`.
- **Data Mapper Pattern**: mapper classes convert between entities, domain models, and API DTOs (for example reservation, vehicle, user, and analytics mappers).

## Quick Start
### 1. Start database
```bash
cd server
docker compose up -d
```

### 2. Start backend
```bash
cd server
./mvnw spring-boot:run
```

### 3. Start frontend
```bash
cd client
npm install
npm run dev
```

Frontend: http://localhost:5173  
Backend: http://localhost:8080
