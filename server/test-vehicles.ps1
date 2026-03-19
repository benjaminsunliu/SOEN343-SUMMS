# Vehicle API Manual Testing Script - Happy Path
# This script tests all endpoints with valid data in a logical sequence

$baseUrl = "http://localhost:8080/api/vehicles"
$headers = @{"Content-Type" = "application/json"}

Write-Host "===== SUMMS Vehicle API Test Suite =====" -ForegroundColor Cyan

# Test 1: Create a Bicycle
Write-Host "`n[TEST 1] Creating a Bicycle..." -ForegroundColor Yellow
$bicycleBody = @{
    location = @{
        latitude = 45.5017
        longitude = -73.5673
    }
    providerId = 1
    costPerMinute = 0.25
} | ConvertTo-Json

try {
    $bicycleResponse = Invoke-WebRequest -Uri "$baseUrl/bicycles" `
        -Method POST `
        -Headers $headers `
        -Body $bicycleBody

    $bicycle = $bicycleResponse.Content | ConvertFrom-Json
    $bicycleId = $bicycle.id
    Write-Host "✓ Bicycle created successfully" -ForegroundColor Green
    Write-Host "  ID: $bicycleId, Type: $($bicycle.type), Status: $($bicycle.status)" -ForegroundColor Green
} catch {
    Write-Host "✗ Failed to create bicycle" -ForegroundColor Red
    Write-Host "  Error: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 2: Create a Scooter
Write-Host "`n[TEST 2] Creating a Scooter..." -ForegroundColor Yellow
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
    $scooterResponse = Invoke-WebRequest -Uri "$baseUrl/scooters" `
        -Method POST `
        -Headers $headers `
        -Body $scooterBody

    $scooter = $scooterResponse.Content | ConvertFrom-Json
    $scooterId = $scooter.id
    Write-Host "✓ Scooter created successfully" -ForegroundColor Green
    Write-Host "  ID: $scooterId, Type: $($scooter.type), MaxRange: $($scooter.maxRange) km" -ForegroundColor Green
} catch {
    Write-Host "✗ Failed to create scooter" -ForegroundColor Red
    Write-Host "  Error: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 3: Create a Car
Write-Host "`n[TEST 3] Creating a Car..." -ForegroundColor Yellow
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
    $carResponse = Invoke-WebRequest -Uri "$baseUrl/cars" `
        -Method POST `
        -Headers $headers `
        -Body $carBody

    $car = $carResponse.Content | ConvertFrom-Json
    $carId = $car.id
    Write-Host "✓ Car created successfully" -ForegroundColor Green
    Write-Host "  ID: $carId, Type: $($car.type), License: $($car.licensePlate), Seats: $($car.seatingCapacity)" -ForegroundColor Green
} catch {
    Write-Host "✗ Failed to create car" -ForegroundColor Red
    Write-Host "  Error: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 4: Get All Vehicles
Write-Host "`n[TEST 4] Fetching All Vehicles..." -ForegroundColor Yellow
try {
    $allResponse = Invoke-WebRequest -Uri $baseUrl -Method GET -Headers $headers
    $allVehicles = $allResponse.Content | ConvertFrom-Json
    $count = $allVehicles.Count
    Write-Host "✓ Fetched $count vehicle(s)" -ForegroundColor Green
    foreach ($v in $allVehicles) {
        Write-Host "  - Vehicle ID: $($v.id), Type: $($v.type), Status: $($v.status)" -ForegroundColor Green
    }
} catch {
    Write-Host "✗ Failed to fetch all vehicles" -ForegroundColor Red
    Write-Host "  Error: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 5: Get Vehicle by ID
if ($bicycleId) {
    Write-Host "`n[TEST 5] Fetching Vehicle by ID ($bicycleId)..." -ForegroundColor Yellow
    try {
        $byIdResponse = Invoke-WebRequest -Uri "$baseUrl/$bicycleId" -Method GET -Headers $headers
        $vehicle = $byIdResponse.Content | ConvertFrom-Json
        Write-Host "✓ Fetched vehicle by ID" -ForegroundColor Green
        Write-Host "  ID: $($vehicle.id), Type: $($vehicle.type), Location: Lat $($vehicle.location.latitude), Lon $($vehicle.location.longitude)" -ForegroundColor Green
    } catch {
        Write-Host "✗ Failed to fetch vehicle by ID" -ForegroundColor Red
        Write-Host "  Error: $($_.Exception.Message)" -ForegroundColor Red
    }
}

# Test 6: Filter by Status
Write-Host "`n[TEST 6] Filtering Vehicles by Status (AVAILABLE)..." -ForegroundColor Yellow
try {
    $statusResponse = Invoke-WebRequest -Uri "$baseUrl/status/AVAILABLE" -Method GET -Headers $headers
    $statusVehicles = $statusResponse.Content | ConvertFrom-Json
    $statusCount = $statusVehicles.Count
    Write-Host "✓ Found $statusCount vehicle(s) with status AVAILABLE" -ForegroundColor Green
    foreach ($v in $statusVehicles) {
        Write-Host "  - ID: $($v.id), Type: $($v.type), Status: $($v.status)" -ForegroundColor Green
    }
} catch {
    Write-Host "✗ Failed to filter by status" -ForegroundColor Red
    Write-Host "  Error: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 7: Filter by Provider ID
Write-Host "`n[TEST 7] Filtering Vehicles by Provider ID (1)..." -ForegroundColor Yellow
try {
    $providerResponse = Invoke-WebRequest -Uri "$baseUrl/provider/1" -Method GET -Headers $headers
    $providerVehicles = $providerResponse.Content | ConvertFrom-Json
    $providerCount = $providerVehicles.Count
    Write-Host "✓ Found $providerCount vehicle(s) for provider 1" -ForegroundColor Green
    foreach ($v in $providerVehicles) {
        Write-Host "  - ID: $($v.id), Type: $($v.type), Provider: $($v.providerId)" -ForegroundColor Green
    }
} catch {
    Write-Host "✗ Failed to filter by provider" -ForegroundColor Red
    Write-Host "  Error: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 8: Update Vehicle Status
if ($scooterId) {
    Write-Host "`n[TEST 8] Updating Vehicle Status (Scooter $scooterId to IN_USE)..." -ForegroundColor Yellow
    try {
        $statusUpdateResponse = Invoke-WebRequest -Uri "$baseUrl/$scooterId/status?status=IN_USE" `
            -Method PATCH `
            -Headers $headers

        $updatedVehicle = $statusUpdateResponse.Content | ConvertFrom-Json
        Write-Host "✓ Status updated successfully" -ForegroundColor Green
        Write-Host "  ID: $($updatedVehicle.id), New Status: $($updatedVehicle.status)" -ForegroundColor Green
    } catch {
        Write-Host "✗ Failed to update status" -ForegroundColor Red
        Write-Host "  Error: $($_.Exception.Message)" -ForegroundColor Red
    }
}

# Test 9: Update Vehicle Location
if ($carId) {
    Write-Host "`n[TEST 9] Updating Vehicle Location (Car $carId)..." -ForegroundColor Yellow
    $newLocationBody = @{
        latitude = 45.5100
        longitude = -73.5800
    } | ConvertTo-Json

    try {
        $locationUpdateResponse = Invoke-WebRequest -Uri "$baseUrl/$carId/location" `
            -Method PATCH `
            -Headers $headers `
            -Body $newLocationBody

        $updatedVehicle = $locationUpdateResponse.Content | ConvertFrom-Json
        Write-Host "✓ Location updated successfully" -ForegroundColor Green
        Write-Host "  ID: $($updatedVehicle.id), New Location: Lat $($updatedVehicle.location.latitude), Lon $($updatedVehicle.location.longitude)" -ForegroundColor Green
    } catch {
        Write-Host "✗ Failed to update location" -ForegroundColor Red
        Write-Host "  Error: $($_.Exception.Message)" -ForegroundColor Red
    }
}

# Test 10: Delete Vehicle
if ($bicycleId) {
    Write-Host "`n[TEST 10] Deleting Vehicle (Bicycle $bicycleId)..." -ForegroundColor Yellow
    try {
        Invoke-WebRequest -Uri "$baseUrl/$bicycleId" -Method DELETE -Headers $headers | Out-Null
        Write-Host "✓ Vehicle deleted successfully" -ForegroundColor Green
        Write-Host "  ID: $bicycleId has been removed" -ForegroundColor Green
    } catch {
        Write-Host "✗ Failed to delete vehicle" -ForegroundColor Red
        Write-Host "  Error: $($_.Exception.Message)" -ForegroundColor Red
    }
}

# Test 11: Verify deletion (should not find the deleted bicycle)
if ($bicycleId) {
    Write-Host "`n[TEST 11] Verifying Deletion (should return 404)..." -ForegroundColor Yellow
    try {
        Invoke-WebRequest -Uri "$baseUrl/$bicycleId" -Method GET -Headers $headers
        Write-Host "✗ Vehicle still exists (should have been deleted)" -ForegroundColor Red
    } catch {
        if ($_.Exception.Response.StatusCode -eq 404) {
            Write-Host "✓ Vehicle correctly not found (404)" -ForegroundColor Green
            Write-Host "  Deletion verified" -ForegroundColor Green
        } else {
            Write-Host "✗ Unexpected error" -ForegroundColor Red
            Write-Host "  Error: $($_.Exception.Message)" -ForegroundColor Red
        }
    }
}

Write-Host "`n===== Test Suite Complete =====" -ForegroundColor Cyan
