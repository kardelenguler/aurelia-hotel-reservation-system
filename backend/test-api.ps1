# =====================================================
#  AURELIA — OTOMATİK API TESTİ
#  Çalıştırma: powershell -ExecutionPolicy Bypass -File .\test-api.ps1
#  (Backend çalışıyor olmalı)
# =====================================================

$base = "http://localhost:8080"
$script:pass = 0
$script:fail = 0

function Call($method, $path, $token, $body) {
    $headers = @{}
    if ($token) { $headers["Authorization"] = "Bearer $token" }

    $params = @{
        Method          = $method
        Uri             = "$base$path"
        Headers         = $headers
        ContentType     = "application/json; charset=utf-8"
        UseBasicParsing = $true
    }
    if ($body) {
        $params["Body"] = [System.Text.Encoding]::UTF8.GetBytes(($body | ConvertTo-Json -Depth 5))
    }

    try {
        $response = Invoke-WebRequest @params
        $data = $null
        if ($response.Content) { $data = $response.Content | ConvertFrom-Json }
        return @{ Status = [int]$response.StatusCode; Body = $data }
    }
    catch {
        if ($_.Exception.Response) {
            return @{ Status = [int]$_.Exception.Response.StatusCode; Body = $null }
        }
        Write-Host "[HATA] Backend'e ulasilamadi. Calisiyor mu? ($base)" -ForegroundColor Red
        exit 1
    }
}

function Check($name, $condition, $detail) {
    if ($condition) {
        Write-Host "[OK]   $name" -ForegroundColor Green
        $script:pass++
    } else {
        Write-Host "[HATA] $name  -->  $detail" -ForegroundColor Red
        $script:fail++
    }
}

function Day($offset) { return (Get-Date).AddDays($offset).ToString("yyyy-MM-dd") }

# ---------- A) GİRİŞ ----------
Write-Host "`n--- A) GIRIS ---" -ForegroundColor Cyan
$admin = Call "POST" "/api/auth/login" $null @{ email = "kardelen@aurelia.com"; password = "Aurelia2026!" }
Check "A1 Yonetici girisi" ($admin.Status -eq 200 -and $admin.Body.role -eq "admin") "status=$($admin.Status)"
$adminToken = $admin.Body.token

$cust = Call "POST" "/api/auth/login" $null @{ email = "elif@example.com"; password = "Musteri2026!" }
Check "A2 Musteri girisi" ($cust.Status -eq 200 -and $cust.Body.role -eq "customer") "status=$($cust.Status)"
$custToken = $cust.Body.token

$recep = Call "POST" "/api/auth/login" $null @{ email = "resepsiyon@aurelia.com"; password = "Resepsiyon2026!" }
Check "A3 Resepsiyon girisi" ($recep.Status -eq 200 -and $recep.Body.role -eq "receptionist") "status=$($recep.Status), rol=$($recep.Body.role)"
$recepToken = $recep.Body.token

# ---------- P) HERKESE AÇIK ----------
Write-Host "`n--- P) HERKESE ACIK ---" -ForegroundColor Cyan
$types = Call "GET" "/api/room-types" $null $null
Check "P1 Oda tipleri (4 adet)" ($types.Status -eq 200 -and @($types.Body).Count -eq 4) "status=$($types.Status)"

$avail = Call "GET" "/api/rooms/available?checkIn=$(Day 30)&checkOut=$(Day 32)&guests=4" $null $null
$nonFamily = @($avail.Body | Where-Object { $_.roomType.name -ne "Family Suite" }).Count
Check "P2 4 kisi -> sadece Family Suite" ($avail.Status -eq 200 -and @($avail.Body).Count -gt 0 -and $nonFamily -eq 0) "sonuc=$(@($avail.Body).Count), baska tip=$nonFamily"

$p3 = Call "GET" "/api/rooms" $null $null
Check "P3 Tum odalar, token yok -> 401" ($p3.Status -eq 401) "status=$($p3.Status)"

$p4 = Call "GET" "/api/rooms" $custToken $null
Check "P4 Tum odalar, musteri -> 403" ($p4.Status -eq 403) "status=$($p4.Status)"

# ---------- C) TAM İADE (girişe 60 gün) ----------
Write-Host "`n--- C) TAM IADE ---" -ForegroundColor Cyan
$free = Call "GET" "/api/rooms/available?checkIn=$(Day 60)&checkOut=$(Day 62)&guests=2" $null $null
$roomC = (@($free.Body) | Where-Object { $_.roomType.name -eq "Standard Room" } | Select-Object -First 1).id

$c1 = Call "POST" "/api/reservations" $custToken @{ roomId = $roomC; checkInDate = (Day 60); checkOutDate = (Day 62); guestCount = 2 }
Check "C1 Rezervasyon -> 201" ($c1.Status -eq 201) "status=$($c1.Status)"
$resC = $c1.Body.id

$c2 = Call "POST" "/api/reservations/$resC/payment" $custToken @{ method = "card" }
Check "C2 Odeme -> 201" ($c2.Status -eq 201) "status=$($c2.Status)"

$c3 = Call "GET" "/api/reservations/$resC/cancellation-preview" $custToken $null
Check "C3 Onizleme -> %100, 6000" ($c3.Body.refundPercent -eq 100 -and $c3.Body.refundAmount -eq 6000) "yuzde=$($c3.Body.refundPercent), iade=$($c3.Body.refundAmount)"

$c4 = Call "PATCH" "/api/reservations/$resC/cancel" $custToken $null
Check "C4 Iptal -> cancelled" ($c4.Status -eq 200 -and $c4.Body.status -eq "cancelled") "status=$($c4.Status)"

$c5 = Call "GET" "/api/reservations/$resC/payment" $custToken $null
Check "C5 Odeme -> refunded, 6000" ($c5.Body.status -eq "refunded" -and $c5.Body.refundAmount -eq 6000) "durum=$($c5.Body.status), iade=$($c5.Body.refundAmount)"

# ---------- H) YARIM İADE (girişe 3 gün) ----------
Write-Host "`n--- H) YARIM IADE ---" -ForegroundColor Cyan
$free = Call "GET" "/api/rooms/available?checkIn=$(Day 3)&checkOut=$(Day 5)&guests=2" $null $null
$roomH = (@($free.Body) | Where-Object { $_.roomType.name -eq "Standard Room" } | Select-Object -First 1).id

$h1 = Call "POST" "/api/reservations" $custToken @{ roomId = $roomH; checkInDate = (Day 3); checkOutDate = (Day 5); guestCount = 2 }
Check "H1 Rezervasyon -> 201" ($h1.Status -eq 201) "status=$($h1.Status)"
$resH = $h1.Body.id

$h2 = Call "POST" "/api/reservations/$resH/payment" $custToken @{ method = "card" }
Check "H2 Odeme -> 201" ($h2.Status -eq 201) "status=$($h2.Status)"

$h3 = Call "GET" "/api/reservations/$resH/cancellation-preview" $custToken $null
Check "H3 Onizleme -> %50, 3000" ($h3.Body.refundPercent -eq 50 -and $h3.Body.refundAmount -eq 3000) "yuzde=$($h3.Body.refundPercent), iade=$($h3.Body.refundAmount)"

$h4 = Call "PATCH" "/api/reservations/$resH/cancel" $custToken $null
Check "H4 Iptal -> cancelled" ($h4.Status -eq 200 -and $h4.Body.status -eq "cancelled") "status=$($h4.Status)"

$h5 = Call "GET" "/api/reservations/$resH/payment" $custToken $null
Check "H5 Odeme -> refunded, 3000" ($h5.Body.status -eq "refunded" -and $h5.Body.refundAmount -eq 3000) "durum=$($h5.Body.status), iade=$($h5.Body.refundAmount)"

# ---------- D) YÖNETİCİ ----------
Write-Host "`n--- D) YONETICI ---" -ForegroundColor Cyan
$d1 = Call "GET" "/api/admin/dashboard" $adminToken $null
Check "D1 Dashboard -> 300 oda, gelir gorunur" ($d1.Status -eq 200 -and $d1.Body.activeRooms -eq 300 -and $null -ne $d1.Body.revenueThisMonth) "oda=$($d1.Body.activeRooms), gelir=$($d1.Body.revenueThisMonth)"
Write-Host "       Bu ayin net geliri: $($d1.Body.revenueThisMonth) TL" -ForegroundColor Gray

$d2 = Call "GET" "/api/reservations/3/cancellation-preview" $adminToken $null
Check "D2 Odenmemis rez. -> iptal edilebilir, iade 0" ($d2.Body.cancellable -eq $true -and $d2.Body.refundAmount -eq 0) "iptal=$($d2.Body.cancellable), iade=$($d2.Body.refundAmount)"

$d3 = Call "GET" "/api/reservations/2/cancellation-preview" $adminToken $null
Check "D3 Oteldeki misafir -> iptal edilemez" ($d3.Body.cancellable -eq $false) "iptal=$($d3.Body.cancellable)"

$d4 = Call "GET" "/api/reservations/1/cancellation-preview" $custToken $null
Check "D4 Baskasinin rezervasyonu (musteri) -> 403" ($d4.Status -eq 403) "status=$($d4.Status)"

# ---------- R) RESEPSİYON ----------
Write-Host "`n--- R) RESEPSIYON ---" -ForegroundColor Cyan
$r1 = Call "GET" "/api/admin/dashboard" $recepToken $null
Check "R1 Dashboard -> acik, gelir GIZLI" ($r1.Status -eq 200 -and $null -eq $r1.Body.revenueThisMonth) "status=$($r1.Status), gelir=$($r1.Body.revenueThisMonth)"

$r2 = Call "GET" "/api/reservations" $recepToken $null
Check "R2 Tum rezervasyonlar -> 200" ($r2.Status -eq 200) "status=$($r2.Status)"

$r3 = Call "GET" "/api/reservations/1" $recepToken $null
Check "R3 Misafirin rezervasyonunu gorme -> 200" ($r3.Status -eq 200) "status=$($r3.Status)"

$r4 = Call "GET" "/api/rooms" $recepToken $null
Check "R4 Oda listesi -> 200" ($r4.Status -eq 200) "status=$($r4.Status)"

$r5 = Call "POST" "/api/rooms" $recepToken @{ roomNumber = "9999"; roomTypeId = 1; floor = 1 }
Check "R5 Oda ekleme -> 403 (sadece yonetici)" ($r5.Status -eq 403) "status=$($r5.Status)"

$r6 = Call "PATCH" "/api/rooms/1/deactivate" $recepToken $null
Check "R6 Oda pasife alma -> 403 (sadece yonetici)" ($r6.Status -eq 403) "status=$($r6.Status)"

# ---------- SONUÇ ----------
Write-Host "`n=====================================" -ForegroundColor Cyan
$color = if ($script:fail -eq 0) { "Green" } else { "Yellow" }
Write-Host " SONUC: $($script:pass) basarili, $($script:fail) hatali" -ForegroundColor $color
Write-Host "=====================================`n" -ForegroundColor Cyan