# Reservations API

All reservation endpoints are under `/api` and require JWT authentication.

## Authentication

Include this header in every request:

```http
Authorization: Bearer <your-jwt-token>
```

---

## 1. Create Reservation

- **Method:** `POST`
- **Path:** `/api/vehicles/{vehicleId}/reservations`
- **Description:** Creates a reservation for a specific vehicle for the authenticated user.

### Example Request

```http
POST /api/vehicles/12/reservations HTTP/1.1
Authorization: Bearer <your-jwt-token>
Content-Type: application/json
```

```json
{
  "startLocation": {
    "latitude": 45.5017,
    "longitude": -73.5673
  },
  "endLocation": {
    "latitude": 45.5088,
    "longitude": -73.5617
  },
  "city": "Montreal",
  "startDate": "2026-03-25T09:00:00",
  "endDate": "2026-03-25T10:30:00"
}
```

### Example Response (`201 Created`)

```http
HTTP/1.1 201 Created
Location: /api/reservations/101
Content-Type: application/json
```

```json
{
  "reservationId": 101,
  "userId": 5,
  "vehicleId": 12,
  "city": "Montreal",
  "status": "CONFIRMED",
  "startLocation": {
    "latitude": 45.5017,
    "longitude": -73.5673
  },
  "endLocation": {
    "latitude": 45.5088,
    "longitude": -73.5617
  }
}
```

---

## 2. Get Current User Reservations

- **Method:** `GET`
- **Path:** `/api/reservations`
- **Description:** Returns all reservations for the authenticated user.

### Example Request

```http
GET /api/reservations HTTP/1.1
Authorization: Bearer <your-jwt-token>
```

### Example Response (`200 OK`)

```json
[
  {
    "reservationId": 101,
    "userId": 5,
    "vehicleId": 12,
    "city": "Montreal",
    "status": "CONFIRMED",
    "startLocation": {
      "latitude": 45.5017,
      "longitude": -73.5673
    },
    "endLocation": {
      "latitude": 45.5088,
      "longitude": -73.5617
    }
  },
  {
    "reservationId": 102,
    "userId": 5,
    "vehicleId": 19,
    "city": "Montreal",
    "status": "CANCELLED",
    "startLocation": {
      "latitude": 45.5001,
      "longitude": -73.5705
    },
    "endLocation": {
      "latitude": 45.5033,
      "longitude": -73.5552
    }
  }
]
```

---

## 3. Get Reservation By ID

- **Method:** `GET`
- **Path:** `/api/reservations/{reservationId}`
- **Description:** Returns one reservation if it belongs to the authenticated user.

### Example Request

```http
GET /api/reservations/101 HTTP/1.1
Authorization: Bearer <your-jwt-token>
```

### Example Response (`200 OK`)

```json
{
  "reservationId": 101,
  "userId": 5,
  "vehicleId": 12,
  "city": "Montreal",
  "status": "CONFIRMED",
  "startLocation": {
    "latitude": 45.5017,
    "longitude": -73.5673
  },
  "endLocation": {
    "latitude": 45.5088,
    "longitude": -73.5617
  }
}
```

---

## 4. Cancel Reservation

- **Method:** `DELETE`
- **Path:** `/api/reservations/{reservationId}`
- **Description:** Cancels a reservation if it belongs to the authenticated user.

### Example Request

```http
DELETE /api/reservations/101 HTTP/1.1
Authorization: Bearer <your-jwt-token>
```

### Example Response (`204 No Content`)

```http
HTTP/1.1 204 No Content
```

---

## Common Error Cases

### Example Validation Error (`400 Bad Request`)

```json
{
  "message": "Start location is required"
}
```

### Example Unauthorized (`401 Unauthorized`)

```json
{
  "message": "Authentication required"
}
```

### Example Forbidden (`403 Forbidden`)

```json
{
  "message": "You do not have access to this reservation"
}
```

### Example Not Found (`404 Not Found`)

```json
{
  "message": "Reservation not found"
}
```

### Example Conflict (`409 Conflict`)

```json
{
  "message": "Vehicle already reserved in this period"
}
```
