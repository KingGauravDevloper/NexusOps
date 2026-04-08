Write-Host "Killing old java processes to free ports..."
Get-Process | Where-Object {$_.ProcessName -eq "java"} | Stop-Process -Force -ErrorAction SilentlyContinue

Write-Host "Rebuilding all microservices with Maven..."
mvn clean package -DskipTests

Write-Host "Starting microservices in separate windows..."
Start-Process -FilePath java -ArgumentList "-jar","api-gateway/target/api-gateway-1.0.0-SNAPSHOT.jar"
Start-Process -FilePath java -ArgumentList "-jar","auth-service/target/auth-service-1.0.0-SNAPSHOT.jar"
Start-Process -FilePath java -ArgumentList "-jar","booking-service/target/booking-service-1.0.0-SNAPSHOT.jar"
Start-Process -FilePath java -ArgumentList "-jar","billing-service/target/billing-service-1.0.0-SNAPSHOT.jar"
Start-Process -FilePath "cmd.exe" -ArgumentList "/c","set GOOGLE_CLOUD_PROJECT_ID=nexusops && set GOOGLE_CLOUD_LOCATION=us-central1 && java -jar ai-concierge/target/ai-concierge-1.0.0-SNAPSHOT.jar"

Write-Host "Sleeping 35 seconds to allow Spring Boot containers to fully boot up..."
Start-Sleep -Seconds 35

Write-Host ""
Write-Host "--- TEST 1: Registering User ---"
try {
    $reg = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/register" -Method Post -ContentType "application/json" -Body '{"email": "agent@nexusops.com", "password": "password123", "role": "EMPLOYEE"}'
    Write-Host "Registration Response: $reg"
} catch {
    Write-Host "Error Registering: $($_.Exception.Message)"
}

Write-Host "--- TEST 2: Logging In ---"
$token = ""
try {
    $loginResp = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" -Method Post -ContentType "application/json" -Body '{"email": "agent@nexusops.com", "password": "password123"}'
    $token = $loginResp.token
    Write-Host "Login successful! JWT Generated."
} catch {
    Write-Host "Error Logging In: $($_.Exception.Message)"
}

Write-Host "--- TEST 3: Create Infrastructure Resource (Room) ---"
$resId = ""
try {
    $headers = @{}
    $headers.Add("Authorization", "Bearer $token")
    # Note: Requires SCOPE_SUPER_ADMIN or SCOPE_FACILITY_MANAGER, but we passed SCOPE_EMPLOYEE. Let's see if gateway correctly blocks it!
    $roomResp = Invoke-RestMethod -Uri "http://localhost:8080/api/resources" -Method Post -Headers $headers -ContentType "application/json" -Body '{"name": "Conference Room A", "capacity": 20}'
    $resId = $roomResp.id
    Write-Host "Room Created dynamically: $resId"
} catch {
    Write-Host "Expected RBAC 403 Forbidden because Employee tried to create a facility resource: $($_.Exception.Message)"
}
