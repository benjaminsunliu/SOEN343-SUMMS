# Vehicle API Testing Suite
$baseUrl = "http://localhost:8080/api/vehicles"
$headers = @{"Content-Type" = "application/json"}

Write-Host "Starting Vehicle API Tests..." -ForegroundColor Cyan
Write-Host ""

# Test 1: Create a Bicycle
Write-Host "TEST 1: Create a Bicycle" -ForegroundColor Yellow
$bicycleBody = @{
    location = @{
        latitude = 45.5017
        longitude = -73.5673
    }
    providerId = 1
    costPerMinute = 0.25
} | ConvertTo-Json

try {
    $response = Invoke-WebRequest -Uri "$baseUrl/bicycles" `
        -Method POST `
        -Headers $headers `
        -Body $bicycleBody `
        -UseBasicParsing

    $bicycle = $response.Content | ConvertFrom-Json
    $bicycleId = $bicycle.id
    Write-Host "  SUCCESS: Bicycle created with ID $bicycleId" -ForegroundColor Green
} catch {
    Write-Host "  FAILED: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""

# Test 2: Create a Scooter
Write-Host "TEST 2: Create a Scooter" -ForegroundColor Yellow
$scooterBody = @{
    location = @{
        latitude = 45.5050
        longitude = -73.5750
    }
    providerId = 1
    costPerMinute = 0.35
    maxRange = 45.5
} | ConvertTo-Json

try {
    $response = Invoke-WebRequest -Uri "$baseUrl/scooters" `
        -Method POST `
        -Headers $headers `
        -Body $scooterBody `
        -UseBasicParsing

    $scooter = $response.Content | ConvertFrom-Json
    $scooterId = $scooter.id
    Write-Host "  SUCCESS: Scooter created with ID $scooterId, MaxRange: $($scooter.maxRange) km" -ForegroundColor Green
} catch {
    Write-Host "  FAILED: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""

# Test 3: Create a Car
Write-Host "TEST 3: Create a Car" -ForegroundColor Yellow
$carBody = @{
    location = @{
        latitude = 45.4900
        longitude = -73.5600
    }
    providerId = 1
    costPerMinute = 0.75
    licensePlate = "QC-ABC-123"
    seatingCapacity = 5
} | ConvertTo-Json

try {
    $response = Invoke-WebRequest -Uri "$baseUrl/cars" `
        -Method POST `
        -Headers $headers `
        -Body $carBody `
        -UseBasicParsing

    $car = $response.Content | ConvertFrom-Json
    $carId = $car.id
    Write-Host "  SUCCESS: Car created with ID $carId, License: $($car.licensePlate), Seats: $($car.seatingCapacity)" -ForegroundColor Green
} catch {
    Write-Host "  FAILED: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""

# Test 4: Get All Vehicles
Write-Host "TEST 4: Get All Vehicles" -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri $baseUrl -Method GET -Headers $headers -UseBasicParsing
    $allVehicles = $response.Content | ConvertFrom-Json
    $count = $allVehicles.Count
    Write-Host "  SUCCESS: Retrieved $count vehicle(s)" -ForegroundColor Green
    foreach ($v in $allVehicles) {
        Write-Host "    - ID: $($v.id), Type: $($v.type), Status: $($v.status)" -ForegroundColor Green
    }
} catch {
    Write-Host "  FAILED: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""

# Test 5: Get Vehicle by ID
Write-Host "TEST 5: Get Vehicle by ID" -ForegroundColor Yellow
if ($bicycleId) {
    try {
        $response = Invoke-WebRequest -Uri "$baseUrl/$bicycleId" -Method GET -Headers $headers -UseBasicParsing
        $vehicle = $response.Content | ConvertFrom-Json
        Write-Host "  SUCCESS: Retrieved vehicle ID $($vehicle.id)" -ForegroundColor Green
        Write-Host "    Type: $($vehicle.type), Status: $($vehicle.status)" -ForegroundColor Green
        Write-Host "    Location: Lat $($vehicle.location.latitude), Lon $($vehicle.location.longitude)" -ForegroundColor Green
    } catch {
        Write-Host "  FAILED: $($_.Exception.Message)" -ForegroundColor Red
    }
} else {
    Write-Host "  SKIPPED: No bicycle ID available from Test 1" -ForegroundColor Yellow
}

Write-Host ""

# Test 6: Filter by Status
Write-Host "TEST 6: Filter by Status (AVAILABLE)" -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "$baseUrl/status/AVAILABLE" -Method GET -Headers $headers -UseBasicParsing
    $statusVehicles = $response.Content | ConvertFrom-Json
    $statusCount = $statusVehicles.Count
    Write-Host "  SUCCESS: Found $statusCount vehicle(s) with status AVAILABLE" -ForegroundColor Green
    foreach ($v in $statusVehicles) {
        Write-Host "    - ID: $($v.id), Type: $($v.type)" -ForegroundColor Green
    }
} catch {
    Write-Host "  FAILED: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""
Write-Host "Test Summary: First 5 tests complete" -ForegroundColor Cyan
Write-Host ""

# Test 7: Filter by Provider ID
Write-Host "TEST 7: Filter by Provider ID (1)" -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "$baseUrl/provider/1" -Method GET -Headers $headers -UseBasicParsing
    $providerVehicles = $response.Content | ConvertFrom-Json
    $providerCount = $providerVehicles.Count
    Write-Host "  SUCCESS: Found $providerCount vehicle(s) for provider 1" -ForegroundColor Green
    foreach ($v in $providerVehicles) {
        Write-Host "    - ID: $($v.id), Type: $($v.type)" -ForegroundColor Green
    }
} catch {
    Write-Host "  FAILED: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""

# Test 8: Update Vehicle Status
Write-Host "TEST 8: Update Vehicle Status" -ForegroundColor Yellow
if ($scooterId) {
    try {
        $response = Invoke-WebRequest -Uri "$baseUrl/$scooterId/status?status=IN_USE" `
            -Method PATCH `
            -Headers $headers `
            -UseBasicParsing

        $updatedVehicle = $response.Content | ConvertFrom-Json
        Write-Host "  SUCCESS: Status updated for vehicle ID $scooterId" -ForegroundColor Green
        Write-Host "    New Status: $($updatedVehicle.status)" -ForegroundColor Green
    } catch {
        Write-Host "  FAILED: $($_.Exception.Message)" -ForegroundColor Red
    }
} else {
    Write-Host "  SKIPPED: No scooter ID available from Test 2" -ForegroundColor Yellow
}

Write-Host ""

# Test 9: Update Vehicle Location
Write-Host "TEST 9: Update Vehicle Location" -ForegroundColor Yellow
if ($carId) {
    $newLocationBody = @{
        latitude = 45.5100
        longitude = -73.5800
    } | ConvertTo-Json

    try {
        $response = Invoke-WebRequest -Uri "$baseUrl/$carId/location" `
            -Method PATCH `
            -Headers $headers `
            -Body $newLocationBody `
            -UseBasicParsing

        $updatedVehicle = $response.Content | ConvertFrom-Json
        Write-Host "  SUCCESS: Location updated for vehicle ID $carId" -ForegroundColor Green
        Write-Host "    New Location: Lat $($updatedVehicle.location.latitude), Lon $($updatedVehicle.location.longitude)" -ForegroundColor Green
    } catch {
        Write-Host "  FAILED: $($_.Exception.Message)" -ForegroundColor Red
    }
} else {
    Write-Host "  SKIPPED: No car ID available from Test 3" -ForegroundColor Yellow
}

Write-Host ""

# Test 10: Delete Vehicle
Write-Host "TEST 10: Delete Vehicle" -ForegroundColor Yellow
if ($bicycleId) {
    try {
        Invoke-WebRequest -Uri "$baseUrl/$bicycleId" -Method DELETE -Headers $headers -UseBasicParsing | Out-Null
        Write-Host "  SUCCESS: Vehicle ID $bicycleId deleted" -ForegroundColor Green
    } catch {
        Write-Host "  FAILED: $($_.Exception.Message)" -ForegroundColor Red
    }
} else {
    Write-Host "  SKIPPED: No bicycle ID available from Test 1" -ForegroundColor Yellow
}

Write-Host ""

# Test 11: Verify Deletion (404)
Write-Host "TEST 11: Verify Deletion (404)" -ForegroundColor Yellow
if ($bicycleId) {
    try {
        Invoke-WebRequest -Uri "$baseUrl/$bicycleId" -Method GET -Headers $headers -UseBasicParsing
        Write-Host "  FAILED: Vehicle should have been deleted but still exists" -ForegroundColor Red
    } catch {
        if ($_.Exception.Response.StatusCode -eq 404) {
            Write-Host "  SUCCESS: Vehicle correctly returns 404 (not found)" -ForegroundColor Green
        } else {
            Write-Host "  FAILED: Unexpected error - $($_.Exception.Message)" -ForegroundColor Red
        }
    }
} else {
    Write-Host "  SKIPPED: No bicycle ID available from Test 1" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "All Tests Complete" -ForegroundColor Cyan
