package com.example.ui.driver

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.EventSeat
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.AccountStatus
import com.example.data.model.BookingEntity
import com.example.data.model.BookingStatus
import com.example.data.model.CampusEntity
import com.example.data.model.DriverProfileEntity
import com.example.data.model.DriverQueueEntryEntity
import com.example.data.model.DriverQueueStatus
import com.example.data.model.DriverVerificationStatus
import com.example.data.model.InstitutionEntity
import com.example.data.model.PickupLocationEntity
import com.example.data.model.RouteEntity
import com.example.data.model.ServiceZoneEntity
import com.example.data.model.TripEntity
import com.example.data.model.TripStatus
import com.example.data.model.UserRole
import com.example.data.model.VehicleCategory
import com.example.ui.QrScanFeedback
import com.example.ui.RydRoverViewModel
import com.example.ui.components.BoardingStatusChip
import com.example.ui.components.DriverQueueStatusChip
import com.example.ui.components.DriverRydWalletScreen
import com.example.ui.components.DriverVerificationBadge
import com.example.ui.components.PaymentStatusChip
import com.example.ui.components.PortalSupportAndNotificationsScreen
import com.example.ui.components.SeatAvailabilityIndicator
import com.example.ui.components.TransportNetworkSetupInProgressCard
import com.example.ui.components.TripStatusChip
import com.example.ui.components.UniversityLiveMapView
import com.example.ui.components.VehicleIconBadge
import com.example.ui.components.formatNaira
import com.example.ui.theme.RydBrandGreen
import com.example.ui.theme.RydBrandRed
import com.example.ui.theme.RydError
import com.example.ui.theme.RydGold
import com.example.ui.theme.RydGoldContainer
import com.example.ui.theme.RydGreenContainer
import com.example.ui.theme.RydGreenPrimary
import com.example.ui.theme.RydOnGoldContainer
import com.example.ui.theme.RydOnGreenContainer

@Composable
fun DriverScreen(
    viewModel: RydRoverViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Step 1: Collect Institution Readiness & Authenticated Driver Context
    val isInstitutionContextReady by viewModel.isInstitutionContextReady.collectAsStateWithLifecycle()
    val driver by viewModel.currentUser.collectAsStateWithLifecycle()
    val driverProfile by viewModel.currentDriverProfile.collectAsStateWithLifecycle()
    val currentInstitution by viewModel.currentInstitution.collectAsStateWithLifecycle()
    val currentCampus by viewModel.currentCampus.collectAsStateWithLifecycle()
    val institutionCampuses by viewModel.institutionCampuses.collectAsStateWithLifecycle()
    val allInstitutions by viewModel.allInstitutions.collectAsStateWithLifecycle()
    val allUsers by viewModel.allUsers.collectAsStateWithLifecycle()

    // Step 2: Collect Strictly Institution-Isolated Transport Data & Driver RydWallet / Support Data
    val driverTrips by viewModel.currentDriverTrips.collectAsStateWithLifecycle()
    val driverBookings by viewModel.driverAssignedBookings.collectAsStateWithLifecycle()
    val pickupLocations by viewModel.institutionPickupLocations.collectAsStateWithLifecycle()
    val serviceZones by viewModel.institutionServiceZones.collectAsStateWithLifecycle()
    val routes by viewModel.institutionRoutes.collectAsStateWithLifecycle()
    val queueEntries by viewModel.institutionQueueEntries.collectAsStateWithLifecycle()
    val currentQueueEntry by viewModel.currentDriverQueueEntry.collectAsStateWithLifecycle()
    val qrScanFeedback by viewModel.qrVerificationResult.collectAsStateWithLifecycle()
    val hasGpsPermission by viewModel.hasGpsPermission.collectAsStateWithLifecycle()
    val deviceGpsCoordinates by viewModel.deviceGpsCoordinates.collectAsStateWithLifecycle()
    val driverWallet by viewModel.currentDriverWallet.collectAsStateWithLifecycle()
    val walletTransactions by viewModel.currentUserWalletTransactions.collectAsStateWithLifecycle()
    val driverNotifications by viewModel.currentUserNotifications.collectAsStateWithLifecycle()
    val driverSupportTickets by viewModel.currentUserSupportTickets.collectAsStateWithLifecycle()
    val platformSettings by viewModel.platformSettings.collectAsStateWithLifecycle()

    // Check GPS permission & sync live device coordinates on launch / driver change
    val gpsPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        viewModel.updateGpsPermissionStatus(granted)
        if (granted) {
            viewModel.syncDeviceGpsLocation(context)
        }
    }

    LaunchedEffect(driver?.id) {
        val fineGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarseGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        if (fineGranted || coarseGranted) {
            viewModel.syncDeviceGpsLocation(context)
        } else {
            viewModel.updateGpsPermissionStatus(false)
        }
    }

    // Step 3: LOADING STATE — Never render default or UNIPORT data while driver's institution context is loading
    val activeDriver = driver
    val activeProfile = driverProfile
    val activeInstitution = currentInstitution
    if (!isInstitutionContextReady || activeDriver == null || activeProfile == null || activeInstitution == null) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color(0xFFF6F8F6))
                .padding(24.dp)
                .testTag("driver_institution_loading_state"),
            contentAlignment = Alignment.Center
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(
                        color = RydGreenPrimary,
                        strokeWidth = 3.dp,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Loading Driver Institution Network...",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color(0xFF0F172A)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Establishing authenticated driver profile, campus zones, routes, queue & passenger assignments.",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
        return
    }

    var selectedDriverTab by remember { mutableIntStateOf(0) }
    var showCreateTripModal by remember { mutableStateOf(false) }
    var showQrVerifyModal by remember { mutableStateOf(false) }
    var showZoneSwitchModal by remember { mutableStateOf(false) }
    var qrInputText by remember { mutableStateOf("") }

    val unreadNotificationsCount = driverNotifications.count { !it.isRead }
    val isTransportNetworkEmpty = pickupLocations.isEmpty() && serviceZones.isEmpty() && routes.isEmpty()

    val isOnline = activeProfile.isOnline
    val currentQueueStatus = currentQueueEntry?.queueStatus
        ?: activeProfile.queueStatus

    // Resolve current zone & route strictly from the driver's institution data (Zero UNIPORT fallback)
    val currentZoneName = currentQueueEntry?.serviceZoneName?.takeIf { it.isNotBlank() }
        ?: activeProfile.currentServiceZoneName.takeIf { it.isNotBlank() }
        ?: serviceZones.firstOrNull()?.name
        ?: currentCampus?.name
        ?: "${activeInstitution.shortCode} Service Zone"

    val defaultInstRoute = routes.firstOrNull()?.let { "${it.originName} → ${it.destinationName}" }
        ?: serviceZones.firstOrNull()?.let { z ->
            val dest = z.supportedRoutes.split(",").firstOrNull()?.trim().orEmpty()
            val orig = z.name.removeSuffix(" Zone").trim()
            if (dest.isNotBlank()) "$orig → $dest" else orig
        }
        ?: "${activeInstitution.shortCode} Campus Route"

    val currentRouteDirection = currentQueueEntry?.routeDirection?.takeIf { it.isNotBlank() }
        ?: activeProfile.currentRouteDirection.takeIf { it.isNotBlank() }
        ?: defaultInstRoute

    // Compute driver's rank within their institution's service zone & route direction queue
    val zoneRoutePeerQueue = queueEntries
        .filter {
            it.institutionId == activeInstitution.id &&
                it.serviceZoneName.equals(currentZoneName, ignoreCase = true) &&
                it.routeDirection.equals(currentRouteDirection, ignoreCase = true)
        }
        .sortedWith(
            compareBy<DriverQueueEntryEntity> { it.temporarilySkippedReason.isNotBlank() }
                .thenBy { it.queueEntryTimestamp }
        )

    val myQueueRank = (zoneRoutePeerQueue.indexOfFirst { it.driverId == activeDriver.id } + 1).coerceAtLeast(1)
    val totalInZoneRouteQueue = zoneRoutePeerQueue.size.coerceAtLeast(1)

    val activeTrip = driverTrips.firstOrNull {
        it.institutionId == activeInstitution.id &&
            it.status != TripStatus.COMPLETED &&
            it.status != TripStatus.CANCELLED
    } ?: driverTrips.firstOrNull { it.institutionId == activeInstitution.id }

    // Enforce booking.institutionId === driver.institutionId (and campusId where applicable)
    val activeTripBookings = if (activeTrip != null) {
        driverBookings.filter {
            it.tripId == activeTrip.id &&
                it.institutionId == activeInstitution.id &&
                it.status != BookingStatus.CANCELLED &&
                it.status != BookingStatus.REJECTED
        }.sortedBy { it.passengerIndex }
    } else emptyList()

    val boardedSeatsCount = activeTripBookings
        .filter { it.boardingStatus == "Boarded" || it.isQrVerified }
        .sumOf { it.seatsCount }

    val isPendingApproval = activeDriver.verificationStatus == DriverVerificationStatus.PENDING ||
        activeDriver.accountStatus == AccountStatus.DRIVER_APPROVAL_PENDING ||
        activeProfile.verificationStatus == DriverVerificationStatus.PENDING

    // New booking requests waiting for driver's ACCEPT or REJECT response
    val newBookingRequests = remember(driverBookings, activeInstitution.id) {
        driverBookings.filter { b ->
            b.institutionId == activeInstitution.id &&
                (b.status == BookingStatus.PENDING_APPROVAL ||
                    b.status == BookingStatus.PENDING ||
                    (b.status == BookingStatus.CONFIRMED &&
                        !b.isQrVerified &&
                        b.boardingStatus == "Waiting" &&
                        !b.driverNotes.contains("[ACCEPTED]")))
        }.sortedByDescending { it.bookingTime }
    }

    // Accepted / active passenger bookings currently being handled by the driver
    val acceptedAndBoardedBookings = remember(activeTripBookings) {
        activeTripBookings.filter { b ->
            b.status == BookingStatus.CONFIRMED &&
                (b.isQrVerified ||
                    b.boardingStatus == "Boarded" ||
                    b.boardingStatus == "Accepted" ||
                    b.boardingStatus == "Arrived at Pickup" ||
                    b.driverNotes.contains("[ACCEPTED]"))
        }
    }

    val nextActionInstruction = when {
        isPendingApproval -> "PENDING APPROVAL: Your driver profile is awaiting Institution Admin verification before going online or accepting trips."
        !isOnline -> "You are OFFLINE. Toggle ONLINE above to join the $currentZoneName queue and receive ride requests."
        newBookingRequests.isNotEmpty() -> "ACTION REQUIRED: You have ${newBookingRequests.size} new ride request(s) waiting below! Tap ACCEPT or REJECT."
        activeTrip?.status == TripStatus.IN_PROGRESS -> "TRIP IN PROGRESS: Drive safely to ${activeTrip.destination}, then tap 'Complete Trip' to settle your earnings."
        activeTripBookings.any { !it.isQrVerified && it.boardingStatus != "Boarded" } -> "NEXT STEP: Drive to passenger pickup, tap 'Arrived at Pickup', and verify passenger's QR / 4-digit code before starting the trip."
        activeTripBookings.isNotEmpty() && activeTrip != null -> "ALL PASSENGERS BOARDED: Tap 'Start Trip' below to begin the ride with live GPS tracking."
        else -> "ONLINE & QUEUED (#$myQueueRank of $totalInZoneRouteQueue in $currentZoneName): Waiting for passenger bookings on $currentRouteDirection."
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when (selectedDriverTab) {
                0 -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 14.dp),
                        contentPadding = PaddingValues(vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // 1. Unified Driver Header Banner (Same visual language & structure as Passenger Home)
                        item {
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = RydGreenContainer.copy(alpha = 0.55f)),
                                border = BorderStroke(1.dp, RydBrandGreen.copy(alpha = 0.35f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("driver_institution_header_card")
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Surface(
                                                    shape = RoundedCornerShape(4.dp),
                                                    color = RydBrandGreen
                                                ) {
                                                    Text(
                                                        text = activeInstitution.institutionType.label.uppercase(),
                                                        fontSize = 8.5.sp,
                                                        fontWeight = FontWeight.Black,
                                                        color = Color.White,
                                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.5.dp)
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "${activeInstitution.shortCode} • ${currentCampus?.name ?: "Main Campus"}",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = RydOnGreenContainer
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(3.dp))
                                            Text(
                                                text = activeDriver.fullName,
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = Color(0xFF0F172A)
                                            )
                                            Text(
                                                text = "${activeProfile.vehicleMakeModel} • ${activeProfile.registrationNumber.ifBlank { activeProfile.licensePlate }}",
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color(0xFF1E293B)
                                            )
                                            Text(
                                                text = "${activeInstitution.name} (${currentCampus?.name ?: "Main Campus"})",
                                                fontSize = 10.5.sp,
                                                color = Color(0xFF475569),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }

                                        // Quick Driver RydWallet Balance Pill (Matches Passenger Home Header Pill)
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = RydGreenPrimary,
                                            modifier = Modifier
                                                .clickable { selectedDriverTab = 3 }
                                                .testTag("driver_header_rydwallet_pill")
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.AccountBalanceWallet,
                                                    contentDescription = null,
                                                    tint = Color.White,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Spacer(modifier = Modifier.width(5.dp))
                                                Column(horizontalAlignment = Alignment.End) {
                                                    Text(
                                                        text = "Driver Wallet",
                                                        fontSize = 8.5.sp,
                                                        color = Color.White.copy(alpha = 0.85f),
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    Text(
                                                        text = formatNaira(driverWallet?.availableBalanceNaira ?: 0),
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Black,
                                                        color = Color.White
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    // Multi-Campus Selector within the Driver's Assigned Institution only
                                    if (institutionCampuses.size > 1) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            items(institutionCampuses) { camp ->
                                                val isSelectedCamp = camp.id == currentCampus?.id
                                                Surface(
                                                    shape = RoundedCornerShape(14.dp),
                                                    color = if (isSelectedCamp) RydBrandGreen else Color.White,
                                                    border = BorderStroke(1.dp, if (isSelectedCamp) RydBrandGreen else Color(0xFFCBD5E1)),
                                                    modifier = Modifier.clickable {
                                                        viewModel.switchDriverActiveInstitution(activeInstitution.id, camp.id)
                                                    }
                                                ) {
                                                    Text(
                                                        text = camp.name,
                                                        color = if (isSelectedCamp) Color.White else Color(0xFF334155),
                                                        fontSize = 10.5.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    // Prominent PENDING APPROVAL Banner if Driver is Pending Verification
                                    if (isPendingApproval) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = Color(0xFFFEF3C7),
                                            border = BorderStroke(1.dp, Color(0xFFF59E0B)),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .testTag("driver_pending_approval_banner")
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(10.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Warning,
                                                    contentDescription = null,
                                                    tint = Color(0xFFB45309),
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Column {
                                                    Text(
                                                        text = "PENDING APPROVAL",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Black,
                                                        color = Color(0xFF92400E)
                                                    )
                                                    Text(
                                                        text = "Your commercial driver profile is awaiting ${activeInstitution.shortCode} Institution Admin verification. Operational queue & trip acceptance are restricted until approved.",
                                                        fontSize = 10.5.sp,
                                                        color = Color(0xFF92400E)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // 2. Main Driver Operations, Online Status, Queue Position & Quick Actions Card (Matches Passenger Home Main Card)
                        item {
                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    // Top Row: Online/Offline Control + Verification & Queue Status
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Surface(
                                                    shape = CircleShape,
                                                    color = if (isOnline && !isPendingApproval) RydBrandGreen else Color(0xFF94A3B8),
                                                    modifier = Modifier.size(10.dp)
                                                ) {}
                                                Text(
                                                    text = when {
                                                        isPendingApproval -> "PENDING APPROVAL"
                                                        isOnline -> "ONLINE • READY FOR BOOKINGS"
                                                        else -> "OFFLINE • NOT IN QUEUE"
                                                    },
                                                    fontSize = 13.5.sp,
                                                    fontWeight = FontWeight.Black,
                                                    color = if (isOnline && !isPendingApproval) RydGreenPrimary else Color(0xFF475569)
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                DriverVerificationBadge(status = activeDriver.verificationStatus)
                                                DriverQueueStatusChip(status = currentQueueStatus)
                                            }
                                        }

                                        Column(horizontalAlignment = Alignment.End) {
                                            Switch(
                                                checked = isOnline && !isPendingApproval,
                                                onCheckedChange = { wantOnline ->
                                                    viewModel.toggleDriverOnlineStatus(wantOnline)
                                                },
                                                enabled = !isPendingApproval,
                                                colors = SwitchDefaults.colors(
                                                    checkedThumbColor = Color.White,
                                                    checkedTrackColor = RydBrandGreen,
                                                    uncheckedThumbColor = Color.White,
                                                    uncheckedTrackColor = Color(0xFFCBD5E1)
                                                ),
                                                modifier = Modifier.testTag("driver_online_switch")
                                            )
                                            Text(
                                                text = if (isOnline && !isPendingApproval) "Tap to go Offline" else "Tap to go Online",
                                                fontSize = 9.5.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color(0xFF64748B)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Clear Next Step Operational Guidance Banner ("What do I need to do next?")
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (newBookingRequests.isNotEmpty()) RydGoldContainer else Color(0xFFF0FDF4),
                                        border = BorderStroke(
                                            1.dp,
                                            if (newBookingRequests.isNotEmpty()) Color(0xFFF59E0B) else RydBrandGreen.copy(alpha = 0.35f)
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = if (newBookingRequests.isNotEmpty()) Icons.Default.NotificationsActive else Icons.Default.Navigation,
                                                contentDescription = null,
                                                tint = if (newBookingRequests.isNotEmpty()) RydOnGoldContainer else RydGreenPrimary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = nextActionInstruction,
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (newBookingRequests.isNotEmpty()) RydOnGoldContainer else Color(0xFF0F172A)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Dynamic Service Zone & Route Queue Position Box
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = Color(0xFFF8FAFC),
                                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Surface(
                                                        shape = RoundedCornerShape(6.dp),
                                                        color = if (isOnline) RydGreenPrimary else Color(0xFF64748B)
                                                    ) {
                                                        Text(
                                                            text = if (isOnline) "QUEUE #$myQueueRank OF $totalInZoneRouteQueue" else "OFFLINE",
                                                            color = Color.White,
                                                            fontSize = 10.5.sp,
                                                            fontWeight = FontWeight.Black,
                                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                        )
                                                    }
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Column {
                                                        Text(
                                                            text = currentZoneName,
                                                            color = Color(0xFF0F172A),
                                                            fontSize = 12.5.sp,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                        Text(
                                                            text = "Route: $currentRouteDirection",
                                                            color = RydGreenPrimary,
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.SemiBold
                                                        )
                                                    }
                                                }

                                                OutlinedButton(
                                                    onClick = { showZoneSwitchModal = true },
                                                    shape = RoundedCornerShape(8.dp),
                                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                                    border = BorderStroke(1.dp, RydBrandGreen),
                                                    modifier = Modifier.testTag("change_zone_route_button")
                                                ) {
                                                    Text(
                                                        text = "Change Zone",
                                                        color = RydGreenPrimary,
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(6.dp))

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(
                                                    text = "Joined Queue: ${currentQueueEntry?.entryTimeFormatted ?: activeTrip?.entryTimeFormatted ?: "Just Now"}",
                                                    color = Color(0xFF64748B),
                                                    fontSize = 10.5.sp
                                                )
                                                Text(
                                                    text = "Completed Today: ${activeProfile.completedTripsToday} trips",
                                                    color = Color(0xFF0F172A),
                                                    fontSize = 10.5.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Driver Quick Earnings & Seats At-a-Glance Strip
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = RydGreenContainer.copy(alpha = 0.5f),
                                            modifier = Modifier
                                                .weight(1f)
                                                .clickable { selectedDriverTab = 3 }
                                        ) {
                                            Column(modifier = Modifier.padding(8.dp)) {
                                                Text("Available Balance", fontSize = 9.5.sp, color = Color(0xFF475569))
                                                Text(
                                                    text = formatNaira(driverWallet?.availableBalanceNaira ?: 0),
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Black,
                                                    color = RydGreenPrimary
                                                )
                                            }
                                        }
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = Color(0xFFFEF3C7).copy(alpha = 0.7f),
                                            modifier = Modifier
                                                .weight(1f)
                                                .clickable { selectedDriverTab = 3 }
                                        ) {
                                            Column(modifier = Modifier.padding(8.dp)) {
                                                Text("Pending Escrow", fontSize = 9.5.sp, color = Color(0xFF92400E))
                                                Text(
                                                    text = formatNaira(driverWallet?.pendingEarningsNaira ?: 0),
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Black,
                                                    color = Color(0xFF92400E)
                                                )
                                            }
                                        }
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = Color(0xFFF1F5F9),
                                            modifier = Modifier
                                                .weight(1f)
                                                .clickable { selectedDriverTab = 2 }
                                        ) {
                                            Column(modifier = Modifier.padding(8.dp)) {
                                                Text("Seats Filled", fontSize = 9.5.sp, color = Color(0xFF475569))
                                                Text(
                                                    text = "${activeTrip?.bookedSeatsCount ?: 0}/${activeTrip?.totalCapacity ?: activeProfile.vehicleCategory.defaultSeats} Seats",
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Black,
                                                    color = Color(0xFF0F172A)
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Primary Action Buttons: Verify QR / Code & New Route Trip
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = {
                                                qrInputText = ""
                                                viewModel.clearQrVerificationFeedback()
                                                showQrVerifyModal = true
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = RydGreenPrimary,
                                                contentColor = Color.White
                                            ),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("scan_passenger_qr_button")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.QrCodeScanner,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "Verify QR / Code",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp
                                            )
                                        }

                                        OutlinedButton(
                                            onClick = { showCreateTripModal = true },
                                            enabled = !isPendingApproval,
                                            shape = RoundedCornerShape(10.dp),
                                            border = BorderStroke(1.dp, RydGreenPrimary),
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("driver_publish_trip_button")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.AddCircle,
                                                contentDescription = null,
                                                tint = RydGreenPrimary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "New Route Trip",
                                                color = RydGreenPrimary,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        if (isTransportNetworkEmpty) {
                            item {
                                TransportNetworkSetupInProgressCard(
                                    institutionName = activeInstitution.name,
                                    campusName = currentCampus?.name
                                )
                            }
                        }

                        // 3. PROMINENT "NEW BOOKINGS" SECTION (Requirement 5, 6, 7)
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("driver_new_bookings_header"),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = if (newBookingRequests.isEmpty()) "NEW BOOKINGS" else "NEW BOOKINGS (${newBookingRequests.size})",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFF0F172A),
                                        letterSpacing = 0.5.sp
                                    )
                                    if (newBookingRequests.isNotEmpty()) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = RydBrandRed
                                        ) {
                                            Text(
                                                text = "AWAITING RESPONSE",
                                                fontSize = 9.5.sp,
                                                fontWeight = FontWeight.Black,
                                                color = Color.White,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                                TextButton(onClick = { selectedDriverTab = 1 }) {
                                    Text(
                                        text = "View All Bookings",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = RydGreenPrimary
                                    )
                                }
                            }
                        }

                        if (newBookingRequests.isEmpty()) {
                            item {
                                Card(
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("no_new_bookings_card")
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            shape = CircleShape,
                                            color = RydGreenContainer,
                                            modifier = Modifier.size(38.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = Icons.Default.EventSeat,
                                                    contentDescription = null,
                                                    tint = RydGreenPrimary,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = "No new ride requests.",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = Color(0xFF0F172A)
                                            )
                                            Text(
                                                text = if (isOnline) {
                                                    "You are ONLINE (#$myQueueRank in $currentZoneName). New ${activeInstitution.shortCode} passenger bookings will appear here automatically."
                                                } else {
                                                    "Go ONLINE to enter the $currentZoneName queue and receive ${activeInstitution.shortCode} ride requests."
                                                },
                                                fontSize = 11.5.sp,
                                                color = Color(0xFF64748B)
                                            )
                                        }
                                    }
                                }
                            }
                        } else {
                            items(newBookingRequests, key = { "new_${it.id}" }) { booking ->
                                NewBookingRequestCard(
                                    booking = booking,
                                    isDriverOnline = isOnline && !isPendingApproval,
                                    onAcceptBooking = { viewModel.acceptDriverBooking(booking.id) },
                                    onRejectBooking = { viewModel.rejectDriverBooking(booking.id) }
                                )
                            }
                        }

                        // 4. QR Verification Feedback Banner (if recently verified)
                        item {
                            androidx.compose.animation.AnimatedVisibility(visible = qrScanFeedback != null) {
                                qrScanFeedback?.let { feedback ->
                                    QrVerificationFeedbackBanner(
                                        feedback = feedback,
                                        onDismiss = { viewModel.clearQrVerificationFeedback() }
                                    )
                                }
                            }
                        }

                        // 5. Active Ride & Passenger Boarding Workflow Card
                        if (activeTrip != null) {
                            item {
                                ActiveDriverTripControlCard(
                                    institution = activeInstitution,
                                    trip = activeTrip,
                                    boardedCount = boardedSeatsCount,
                                    bookingsCount = activeTripBookings.size,
                                    onStartTrip = { viewModel.startDriverTrip(activeTrip.id) },
                                    onCompleteTrip = { viewModel.completeDriverTrip(activeTrip.id) },
                                    onOpenQrScanner = {
                                        qrInputText = ""
                                        viewModel.clearQrVerificationFeedback()
                                        showQrVerifyModal = true
                                    }
                                )
                            }

                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "ACTIVE RIDE PASSENGERS (${activeTripBookings.size})",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Black,
                                            color = Color(0xFF0F172A),
                                            letterSpacing = 0.5.sp
                                        )
                                        Text(
                                            text = "${activeInstitution.shortCode} • Verify QR or 4-digit code upon boarding",
                                            fontSize = 11.5.sp,
                                            color = Color(0xFF64748B)
                                        )
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = RydGreenContainer
                                    ) {
                                        Text(
                                            text = "$boardedSeatsCount/${activeTrip.bookedSeatsCount} Boarded",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = RydOnGreenContainer,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }

                            if (activeTripBookings.isEmpty()) {
                                item {
                                    Card(
                                        shape = RoundedCornerShape(14.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color.White),
                                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(20.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Person,
                                                contentDescription = null,
                                                tint = Color(0xFF94A3B8),
                                                modifier = Modifier.size(32.dp)
                                            )
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = "Waiting for ${activeInstitution.shortCode} Passenger Assignments",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = Color(0xFF334155)
                                            )
                                            Text(
                                                text = "You are #$myQueueRank in the $currentZoneName queue for $currentRouteDirection.",
                                                fontSize = 12.sp,
                                                color = Color(0xFF64748B),
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }
                                }
                            } else {
                                items(activeTripBookings, key = { it.id }) { booking ->
                                    AssignedPassengerManifestCard(
                                        booking = booking,
                                        onAcceptQuick = { viewModel.acceptDriverBooking(booking.id) },
                                        onMarkArrivedPickup = { viewModel.markArrivedAtPassengerPickup(booking.id) },
                                        onVerifyQrQuick = {
                                            viewModel.verifyPassengerBookingCodeOrQr(booking.bookingReference)
                                        },
                                        onUpdateBoardingStatus = { newStatus ->
                                            viewModel.updateBoardingStatus(booking.id, newStatus)
                                        }
                                    )
                                }
                            }
                        }

                        // 6. Institution-Centered Driver Map & Live GPS Positioning Card
                        item {
                            DriverInstitutionMapCard(
                                institution = activeInstitution,
                                campus = currentCampus,
                                driverProfile = activeProfile,
                                serviceZones = serviceZones,
                                pickupLocations = pickupLocations,
                                routes = routes,
                                assignedBookings = activeTripBookings,
                                hasGpsPermission = hasGpsPermission,
                                deviceGpsCoordinates = deviceGpsCoordinates,
                                onRequestGpsPermission = {
                                    gpsPermissionLauncher.launch(
                                        arrayOf(
                                            Manifest.permission.ACCESS_FINE_LOCATION,
                                            Manifest.permission.ACCESS_COARSE_LOCATION
                                        )
                                    )
                                },
                                onRefreshGps = { viewModel.syncDeviceGpsLocation(context) }
                            )
                        }

                        // 7. Live Peer Queue Order in Current Institution Service Zone
                        item {
                            ServiceZoneQueueTransparencyCard(
                                institutionShortCode = activeInstitution.shortCode,
                                zoneName = currentZoneName,
                                routeDirection = currentRouteDirection,
                                peerEntries = queueEntries
                                    .filter {
                                        it.institutionId == activeInstitution.id &&
                                            it.serviceZoneName.equals(currentZoneName, ignoreCase = true)
                                    }
                                    .sortedWith(
                                        compareBy<DriverQueueEntryEntity> { it.temporarilySkippedReason.isNotBlank() }
                                            .thenBy { it.queueEntryTimestamp }
                                    ),
                                currentDriverId = activeDriver.id
                            )
                        }

                        // 8. Institution Routes & Pickup Points Directory Card
                        item {
                            InstitutionRoutesAndPickupsCard(
                                institution = activeInstitution,
                                campus = currentCampus,
                                routes = routes,
                                pickupLocations = pickupLocations,
                                serviceZones = serviceZones
                            )
                        }
                    }
                }
                1 -> {
                    // TAB 1: Dedicated Driver Bookings & Trip History Screen
                    DriverBookingsAndTripsScreen(
                        institution = activeInstitution,
                        driverBookings = driverBookings.filter { it.institutionId == activeInstitution.id },
                        newBookingRequests = newBookingRequests,
                        driverTrips = driverTrips.filter { it.institutionId == activeInstitution.id },
                        isDriverOnline = isOnline && !isPendingApproval,
                        onAcceptBooking = { viewModel.acceptDriverBooking(it) },
                        onRejectBooking = { viewModel.rejectDriverBooking(it) },
                        onMarkArrivedPickup = { viewModel.markArrivedAtPassengerPickup(it) },
                        onVerifyQrQuick = { viewModel.verifyPassengerBookingCodeOrQr(it) },
                        onOpenActiveRideTab = { selectedDriverTab = 2 }
                    )
                }
                2 -> {
                    // TAB 2: Dedicated Driver Active Ride, Navigation & Boarding Screen (Mirrors Passenger "My Ride" Tab)
                    DriverActiveRideScreen(
                        institution = activeInstitution,
                        campus = currentCampus,
                        driverProfile = activeProfile,
                        activeTrip = activeTrip,
                        activeTripBookings = activeTripBookings,
                        newBookingRequests = newBookingRequests,
                        boardedSeatsCount = boardedSeatsCount,
                        serviceZones = serviceZones,
                        pickupLocations = pickupLocations,
                        routes = routes,
                        hasGpsPermission = hasGpsPermission,
                        deviceGpsCoordinates = deviceGpsCoordinates,
                        qrScanFeedback = qrScanFeedback,
                        onDismissQrFeedback = { viewModel.clearQrVerificationFeedback() },
                        onRequestGpsPermission = {
                            gpsPermissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        },
                        onRefreshGps = { viewModel.syncDeviceGpsLocation(context) },
                        onAcceptBooking = { viewModel.acceptDriverBooking(it) },
                        onRejectBooking = { viewModel.rejectDriverBooking(it) },
                        onMarkArrivedPickup = { viewModel.markArrivedAtPassengerPickup(it) },
                        onVerifyQrQuick = { viewModel.verifyPassengerBookingCodeOrQr(it) },
                        onUpdateBoardingStatus = { bookingId, status ->
                            viewModel.updateBoardingStatus(bookingId, status)
                        },
                        onOpenQrScannerModal = {
                            qrInputText = ""
                            viewModel.clearQrVerificationFeedback()
                            showQrVerifyModal = true
                        },
                        onStartTrip = { tripId -> viewModel.startDriverTrip(tripId) },
                        onCompleteTrip = { tripId -> viewModel.completeDriverTrip(tripId) },
                        onPublishNewTrip = { showCreateTripModal = true }
                    )
                }
                3 -> {
                    // TAB 3: Dedicated Driver RydWallet, Earnings & Bank Payouts
                    DriverRydWalletScreen(
                        driverUser = activeDriver,
                        currentInstitution = activeInstitution,
                        driverWallet = driverWallet,
                        transactions = walletTransactions,
                        platformSettings = platformSettings,
                        onWithdrawFunds = { amount, bankName, accountNumber, accountName ->
                            viewModel.withdrawDriverEarnings(amount, bankName, accountNumber, accountName)
                        },
                        onUpdateBankInfo = { bankName, accountNumber, accountName ->
                            viewModel.updateDriverBankDetails(bankName, accountNumber, accountName)
                        }
                    )
                }
                4 -> {
                    // TAB 4: Driver Notifications, Help & Support, Complaints & Technical Problems
                    PortalSupportAndNotificationsScreen(
                        currentUser = activeDriver,
                        currentInstitution = activeInstitution,
                        currentCampus = currentCampus,
                        notifications = driverNotifications,
                        myTickets = driverSupportTickets,
                        bookings = driverBookings,
                        trips = driverTrips,
                        emergencyContact = platformSettings?.emergencyContact ?: "+234 800 RYDROVER",
                        onMarkNotificationRead = { viewModel.markNotificationRead(it) },
                        onMarkAllNotificationsRead = { viewModel.markAllNotificationsRead() },
                        onSubmitComplaint = { category, subject, desc, tripId, bookingId, reportedDriverId, plate ->
                            viewModel.submitSupportComplaint(
                                category = category,
                                subject = subject,
                                description = desc,
                                tripId = tripId,
                                bookingId = bookingId,
                                reportedDriverId = reportedDriverId,
                                reportedVehiclePlate = plate
                            )
                        },
                        onSubmitTechnicalProblem = { issueCat, desc, tripId, bookingId, screenshotRef ->
                            viewModel.submitTechnicalProblemReport(
                                issueCategory = issueCat,
                                description = desc,
                                tripId = tripId,
                                bookingId = bookingId,
                                screenshotReference = screenshotRef
                            )
                        }
                    )
                }
            }
        }

        // Bottom Navigation Bar for Driver Portal (Mirrors Passenger Portal 5-Tab Structure, Strictly Driver-Only)
        NavigationBar(
            containerColor = Color.White,
            tonalElevation = 8.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            NavigationBarItem(
                selected = selectedDriverTab == 0,
                onClick = { selectedDriverTab = 0 },
                icon = { Icon(Icons.Default.DirectionsCar, contentDescription = "Home & Queue") },
                label = { Text("Home", fontSize = 10.sp, fontWeight = if (selectedDriverTab == 0) FontWeight.Bold else FontWeight.Normal) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = RydBrandGreen,
                    selectedTextColor = RydBrandGreen,
                    indicatorColor = RydGreenContainer
                ),
                modifier = Modifier.testTag("nav_driver_operations")
            )
            NavigationBarItem(
                selected = selectedDriverTab == 1,
                onClick = { selectedDriverTab = 1 },
                icon = {
                    Box {
                        Icon(Icons.Default.EventSeat, contentDescription = "Bookings")
                        if (newBookingRequests.isNotEmpty()) {
                            Surface(
                                shape = CircleShape,
                                color = RydBrandRed,
                                modifier = Modifier
                                    .size(9.dp)
                                    .align(Alignment.TopEnd)
                            ) {}
                        }
                    }
                },
                label = {
                    Text(
                        text = if (newBookingRequests.isNotEmpty()) "Bookings (${newBookingRequests.size})" else "Bookings",
                        fontSize = 10.sp,
                        fontWeight = if (selectedDriverTab == 1) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = RydBrandGreen,
                    selectedTextColor = RydBrandGreen,
                    indicatorColor = RydGreenContainer
                ),
                modifier = Modifier.testTag("nav_driver_bookings")
            )
            NavigationBarItem(
                selected = selectedDriverTab == 2,
                onClick = { selectedDriverTab = 2 },
                icon = {
                    Box {
                        Icon(Icons.Default.Navigation, contentDescription = "Active Ride")
                        if (activeTripBookings.isNotEmpty()) {
                            Surface(
                                shape = CircleShape,
                                color = RydBrandGreen,
                                modifier = Modifier
                                    .size(8.dp)
                                    .align(Alignment.TopEnd)
                            ) {}
                        }
                    }
                },
                label = { Text("Active Ride", fontSize = 10.sp, fontWeight = if (selectedDriverTab == 2) FontWeight.Bold else FontWeight.Normal) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = RydBrandGreen,
                    selectedTextColor = RydBrandGreen,
                    indicatorColor = RydGreenContainer
                ),
                modifier = Modifier.testTag("nav_driver_active_ride")
            )
            NavigationBarItem(
                selected = selectedDriverTab == 3,
                onClick = { selectedDriverTab = 3 },
                icon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = "Driver RydWallet") },
                label = { Text("RydWallet", fontSize = 10.sp, fontWeight = if (selectedDriverTab == 3) FontWeight.Bold else FontWeight.Normal) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = RydBrandGreen,
                    selectedTextColor = RydBrandGreen,
                    indicatorColor = RydGreenContainer
                ),
                modifier = Modifier.testTag("nav_driver_wallet")
            )
            NavigationBarItem(
                selected = selectedDriverTab == 4,
                onClick = { selectedDriverTab = 4 },
                icon = {
                    Box {
                        Icon(Icons.Default.SupportAgent, contentDescription = "Support & Alerts")
                        if (unreadNotificationsCount > 0) {
                            Surface(
                                shape = CircleShape,
                                color = RydBrandRed,
                                modifier = Modifier
                                    .size(8.dp)
                                    .align(Alignment.TopEnd)
                            ) {}
                        }
                    }
                },
                label = { Text("Support", fontSize = 10.sp, fontWeight = if (selectedDriverTab == 4) FontWeight.Bold else FontWeight.Normal) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = RydBrandGreen,
                    selectedTextColor = RydBrandGreen,
                    indicatorColor = RydGreenContainer
                ),
                modifier = Modifier.testTag("nav_driver_support")
            )
        }
    }

    // Modal 1: QR Code / Booking Verification Dialog (Strictly Institution-Scoped)
    if (showQrVerifyModal) {
        val sampleBooking = activeTripBookings.firstOrNull { !it.isQrVerified } ?: activeTripBookings.firstOrNull()
        AlertDialog(
            onDismissRequest = { showQrVerifyModal = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.QrCodeScanner,
                        contentDescription = null,
                        tint = RydGreenPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Verify ${activeInstitution.shortCode} Passenger QR / Code",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Scan passenger's RydRover QR code or enter their Booking ID / 4-digit Verification Code (${activeInstitution.shortCode} network only).",
                        fontSize = 12.sp,
                        color = Color(0xFF475569)
                    )

                    // Simulated Camera Viewfinder Box
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF0F172A),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(125.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Box(
                                modifier = Modifier
                                    .size(92.dp)
                                    .border(2.dp, RydBrandGreen, RoundedCornerShape(10.dp))
                            )
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.QrCodeScanner,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(30.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${activeInstitution.shortCode} QR Scanner Active",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                if (sampleBooking != null) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = RydBrandGreen,
                                        modifier = Modifier.clickable {
                                            qrInputText = sampleBooking.qrCodePayload
                                            viewModel.verifyPassengerBookingCodeOrQr(sampleBooking.qrCodePayload)
                                            showQrVerifyModal = false
                                        }
                                    ) {
                                        Text(
                                            text = "Tap to Simulate Scan (${sampleBooking.bookingReference})",
                                            color = Color.White,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = qrInputText,
                        onValueChange = { qrInputText = it },
                        label = { Text("Booking ID or 4-Digit Code") },
                        placeholder = {
                            Text(
                                if (sampleBooking != null) {
                                    "${sampleBooking.bookingReference} or ${sampleBooking.verificationCode}"
                                } else {
                                    "Enter Booking ID or 4-Digit Code"
                                }
                            )
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("qr_code_input_field")
                    )

                    // Quick select waiting passengers on this driver's institution trip
                    if (activeTripBookings.isNotEmpty()) {
                        Text(
                            text = "Quick Verify Waiting ${activeInstitution.shortCode} Passengers:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF475569)
                        )
                        activeTripBookings.forEach { bk ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (bk.isQrVerified) RydGreenContainer else Color(0xFFF1F5F9),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        qrInputText = bk.bookingReference
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${bk.passengerName} (${bk.bookingReference})",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0F172A)
                                    )
                                    Text(
                                        text = if (bk.isQrVerified) "Verified" else "Code: ${bk.verificationCode}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (bk.isQrVerified) RydBrandGreen else Color(0xFF0369A1)
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.verifyPassengerBookingCodeOrQr(qrInputText)
                        showQrVerifyModal = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RydGreenPrimary),
                    modifier = Modifier.testTag("confirm_qr_verify_button")
                ) {
                    Text("Verify & Board Passenger", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showQrVerifyModal = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Modal 2: Switch Service Zone & Route Direction (Strictly Filtered to Driver's Institution)
    if (showZoneSwitchModal) {
        ZoneAndRouteSelectionModal(
            institution = activeInstitution,
            serviceZones = serviceZones,
            routes = routes,
            pickupLocations = pickupLocations,
            currentZoneName = currentZoneName,
            currentRouteDirection = currentRouteDirection,
            onDismiss = { showZoneSwitchModal = false },
            onSelectZoneAndRoute = { zoneId, zoneName, routeDir ->
                viewModel.switchDriverServiceZoneAndRoute(zoneId, zoneName, routeDir)
                showZoneSwitchModal = false
            }
        )
    }

    // Modal 3: Create New Trip (Strictly Scoped to Driver's Institution & Campus)
    if (showCreateTripModal) {
        PublishTripDialog(
            institution = activeInstitution,
            driverProfile = activeProfile,
            pickupLocations = pickupLocations,
            serviceZones = serviceZones,
            routes = routes,
            onDismiss = { showCreateTripModal = false },
            onPublish = { cat, model, plate, origin, dest, corridor, time, date, fare, cap, pickups, notes ->
                viewModel.createDriverTrip(
                    vehicleCategory = cat,
                    vehicleModel = model,
                    licensePlate = plate,
                    origin = origin,
                    destination = dest,
                    corridor = corridor,
                    departureTime = time,
                    departureDate = date,
                    totalFareNaira = fare,
                    totalCapacity = cap,
                    pickupPoints = pickups,
                    notes = notes
                )
                showCreateTripModal = false
            }
        )
    }
}

@Composable
private fun DriverInstitutionMapCard(
    institution: InstitutionEntity,
    campus: CampusEntity?,
    driverProfile: DriverProfileEntity,
    serviceZones: List<ServiceZoneEntity>,
    pickupLocations: List<PickupLocationEntity>,
    routes: List<RouteEntity>,
    assignedBookings: List<BookingEntity>,
    hasGpsPermission: Boolean,
    deviceGpsCoordinates: Pair<Double, Double>?,
    onRequestGpsPermission: () -> Unit,
    onRefreshGps: () -> Unit
) {
    // Center map strictly on driver's assigned campus / service zone (Never UNIPORT unless driver belongs to UNIPORT)
    val activeZone = serviceZones.find { it.id == driverProfile.currentServiceZoneId }
        ?: serviceZones.firstOrNull()
    val mapCenterLat = activeZone?.centerLat?.takeIf { it != 0.0 }
        ?: campus?.centerLat?.takeIf { it != 0.0 }
        ?: institution.centerLat
    val mapCenterLng = activeZone?.centerLng?.takeIf { it != 0.0 }
        ?: campus?.centerLng?.takeIf { it != 0.0 }
        ?: institution.centerLng

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("driver_institution_map_card")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(
                        imageVector = Icons.Default.Map,
                        contentDescription = null,
                        tint = RydGreenPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = "${institution.shortCode} CAMPUS & SERVICE ZONE MAP",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = RydGreenPrimary,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "${campus?.name ?: institution.name} • Zone: ${activeZone?.name ?: driverProfile.currentServiceZoneName}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (hasGpsPermission) RydGreenContainer else Color(0xFFFEF3C7)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (hasGpsPermission) Icons.Default.GpsFixed else Icons.Default.LocationOff,
                            contentDescription = null,
                            tint = if (hasGpsPermission) RydBrandGreen else Color(0xFFB45309),
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (hasGpsPermission) "Live GPS Active" else "Location unavailable",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (hasGpsPermission) RydOnGreenContainer else Color(0xFF92400E)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Requirement 7: Clear "Location unavailable" state if GPS permission is unavailable
            if (!hasGpsPermission) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFFEF3C7),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("driver_location_unavailable_banner")
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOff,
                                contentDescription = "Location unavailable",
                                tint = Color(0xFFB45309),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Location unavailable (GPS permission not granted)",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF92400E)
                                )
                                Text(
                                    text = "Map centered on your assigned ${institution.shortCode} campus zone (${String.format("%.4f", mapCenterLat)}, ${String.format("%.4f", mapCenterLng)}).",
                                    fontSize = 10.5.sp,
                                    color = Color(0xFF92400E)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = onRequestGpsPermission,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("enable_driver_gps_button")
                        ) {
                            Text(
                                text = "Enable GPS",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            } else {
                val liveLat = deviceGpsCoordinates?.first ?: driverProfile.currentLat.takeIf { it != 0.0 } ?: mapCenterLat
                val liveLng = deviceGpsCoordinates?.second ?: driverProfile.currentLng.takeIf { it != 0.0 } ?: mapCenterLng
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFF0FDF4),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Live Driver GPS: ${String.format("%.4f", liveLat)}, ${String.format("%.4f", liveLng)} • ${institution.shortCode} Network",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = RydOnGreenContainer
                        )
                        Text(
                            text = "Sync GPS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = RydGreenPrimary,
                            modifier = Modifier.clickable { onRefreshGps() }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Institution-Specific Campus & Service Zone Map Canvas
            val zoneLabels = serviceZones.map { it.name.removeSuffix(" Zone") }
                .ifEmpty { pickupLocations.map { it.name } }
                .ifEmpty { listOf(campus?.name ?: institution.shortCode) }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(155.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFECFDF5))
                    .border(1.dp, Color(0xFFA7F3D0), RoundedCornerShape(12.dp))
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height

                    // Geofence boundary ring for active campus
                    drawCircle(
                        color = RydBrandGreen.copy(alpha = 0.08f),
                        radius = h * 0.44f,
                        center = Offset(w * 0.5f, h * 0.52f)
                    )
                    drawCircle(
                        color = RydBrandGreen.copy(alpha = 0.35f),
                        radius = h * 0.44f,
                        center = Offset(w * 0.5f, h * 0.52f),
                        style = Stroke(
                            width = 2f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f)
                        )
                    )

                    // Primary campus transit corridor road
                    val roadY = h * 0.54f
                    drawLine(
                        color = Color.White,
                        start = Offset(w * 0.08f, roadY),
                        end = Offset(w * 0.92f, roadY),
                        strokeWidth = 18f,
                        cap = StrokeCap.Round
                    )
                    drawLine(
                        color = RydGreenPrimary,
                        start = Offset(w * 0.08f, roadY),
                        end = Offset(w * 0.92f, roadY),
                        strokeWidth = 5f,
                        cap = StrokeCap.Round,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 10f), 0f)
                    )

                    // Plot institution service zones / pickup points along the campus corridor
                    val count = zoneLabels.size.coerceIn(1, 5)
                    for (idx in 0 until count) {
                        val frac = if (count == 1) 0.5f else 0.12f + (idx.toFloat() / (count - 1)) * 0.76f
                        val nodeOffset = Offset(w * frac, roadY)
                        drawCircle(
                            color = if (idx == 0) RydGreenPrimary else Color(0xFF0369A1),
                            radius = 9f,
                            center = nodeOffset
                        )
                        drawCircle(
                            color = Color.White,
                            radius = 4f,
                            center = nodeOffset
                        )
                    }

                    // Plot driver marker at active service zone
                    val driverOffset = Offset(w * 0.28f, roadY - 4f)
                    drawCircle(color = Color.White, radius = 13f, center = driverOffset)
                    drawCircle(color = RydBrandGreen, radius = 10f, center = driverOffset)
                }

                // Top-left Institution & Campus Map Header Badge
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color.White.copy(alpha = 0.92f),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                ) {
                    Text(
                        text = "${institution.shortCode} • ${campus?.name ?: "Main Campus"} (${String.format("%.4f", mapCenterLat)}, ${String.format("%.4f", mapCenterLng)})",
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A),
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                    )
                }

                // Bottom Stop / Zone Labels strictly from this institution
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .background(Color.White.copy(alpha = 0.88f))
                        .padding(horizontal = 8.dp, vertical = 5.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    zoneLabels.take(4).forEach { label ->
                        Text(
                            text = label,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            if (assignedBookings.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Assigned ${institution.shortCode} Passenger Pickups on Map: " +
                        assignedBookings.joinToString(" • ") { "${it.passengerName} @ ${it.pickupLocation}" },
                    fontSize = 11.sp,
                    color = Color(0xFF0369A1),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun ActiveDriverTripControlCard(
    institution: InstitutionEntity,
    trip: TripEntity,
    boardedCount: Int,
    bookingsCount: Int,
    onStartTrip: () -> Unit,
    onCompleteTrip: () -> Unit,
    onOpenQrScanner: () -> Unit
) {
    val farePerSeat = (trip.totalFareNaira / trip.totalCapacity.coerceAtLeast(1)).coerceAtLeast(100)

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    VehicleIconBadge(category = trip.vehicleCategory)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "${trip.origin} → ${trip.destination}",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = "${institution.shortCode} • ${trip.serviceZoneName} • ${trip.vehicleRegistration}",
                            fontSize = 11.5.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }
                TripStatusChip(status = trip.status)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Real-Time Seat Capacity & Boarding Counter
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFF8FAFC),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    SeatAvailabilityIndicator(
                        availableSeats = trip.availableSeats,
                        totalCapacity = trip.totalCapacity
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Standard Fare: ${formatNaira(farePerSeat)} / seat",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = RydGreenPrimary
                        )
                        Text(
                            text = "QR Verified & Boarded: $boardedCount of ${trip.bookedSeatsCount}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0369A1)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Live Campus GPS Map View for Active Trip
            UniversityLiveMapView(
                trip = trip,
                passengerPickup = trip.origin
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Action Controls: Verify Boarding QR, Start Trip, Complete Trip -> Re-queue at back
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (trip.status != TripStatus.IN_PROGRESS && trip.status != TripStatus.COMPLETED) {
                    OutlinedButton(
                        onClick = onOpenQrScanner,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Scan QR", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    Button(
                        onClick = onStartTrip,
                        colors = ButtonDefaults.buttonColors(containerColor = RydGreenPrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("start_trip_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Start Trip", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                } else if (trip.status == TripStatus.IN_PROGRESS) {
                    Button(
                        onClick = onCompleteTrip,
                        colors = ButtonDefaults.buttonColors(containerColor = RydBrandGreen),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("complete_trip_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Complete Trip & Return to Back of ${institution.shortCode} Queue",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.5.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NewBookingRequestCard(
    booking: BookingEntity,
    isDriverOnline: Boolean,
    onAcceptBooking: () -> Unit,
    onRejectBooking: () -> Unit
) {
    val initials = booking.passengerName
        .split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .joinToString("") { it.take(1).uppercase() }
        .ifBlank { "P" }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.5.dp, RydBrandGreen),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("new_booking_request_card_${booking.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Top Row: NEW RIDE REQUEST Badge + Booking ID + Requested Time
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = RydGreenPrimary
                    ) {
                        Text(
                            text = "NEW RIDE REQUEST",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFF1F5F9)
                    ) {
                        Text(
                            text = booking.bookingReference,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFF0F172A),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.5.dp)
                        )
                    }
                }
                Text(
                    text = "Requested Just Now",
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF64748B)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Passenger Avatar + Name + Phone + Payment Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = RydGreenContainer,
                        border = BorderStroke(1.dp, RydBrandGreen),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = initials,
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp,
                                color = RydGreenPrimary
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = booking.passengerName,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.5.sp,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = booking.passengerPhone,
                            fontSize = 11.5.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = formatNaira(booking.totalFareNaira),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black,
                        color = RydGreenPrimary
                    )
                    PaymentStatusChip(status = booking.paymentStatus)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Structured Details Box (Pickup, Destination, Seats, Fare, Payment, Distance/ETA)
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFFF8FAFC),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.MyLocation,
                            contentDescription = null,
                            tint = Color(0xFF0369A1),
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Pickup: ",
                            fontSize = 11.5.sp,
                            color = Color(0xFF64748B),
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = booking.pickupLocation,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Place,
                            contentDescription = null,
                            tint = RydBrandGreen,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Destination: ",
                            fontSize = 11.5.sp,
                            color = Color(0xFF64748B),
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = booking.destinationLocation,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                    }
                    HorizontalDivider(color = Color(0xFFE2E8F0), modifier = Modifier.padding(vertical = 2.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Seats: ${booking.seatsCount} • Fare: ${formatNaira(booking.totalFareNaira)}",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = "Payment: RydWallet — ${booking.paymentStatus.label}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = RydGreenPrimary
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Distance: ${booking.distanceMeters}m (~${booking.estimatedArrivalMinutes} mins ETA)",
                            fontSize = 11.sp,
                            color = Color(0xFF0369A1),
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Verify Code: ${booking.verificationCode}",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF334155)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Prominent ACCEPT and REJECT Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onAcceptBooking,
                    enabled = isDriverOnline,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RydGreenPrimary,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1.2f)
                        .height(46.dp)
                        .testTag("accept_booking_button_${booking.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(17.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "ACCEPT",
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp
                    )
                }

                OutlinedButton(
                    onClick = onRejectBooking,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.2.dp, RydError),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = RydError),
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                        .testTag("reject_booking_button_${booking.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = null,
                        modifier = Modifier.size(17.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "REJECT",
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun AssignedPassengerManifestCard(
    booking: BookingEntity,
    onAcceptQuick: () -> Unit = {},
    onMarkArrivedPickup: () -> Unit = {},
    onVerifyQrQuick: () -> Unit,
    onUpdateBoardingStatus: (String) -> Unit
) {
    val isWaitingAcceptance = !booking.isQrVerified &&
        booking.boardingStatus == "Waiting" &&
        !booking.driverNotes.contains("[ACCEPTED]")

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(
            1.dp,
            if (booking.isQrVerified) RydBrandGreen.copy(alpha = 0.5f) else Color(0xFFE2E8F0)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = RydGreenContainer,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "#${booking.passengerIndex}",
                                fontWeight = FontWeight.Black,
                                fontSize = 12.sp,
                                color = RydGreenPrimary
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = booking.passengerName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.5.sp,
                                color = Color(0xFF0F172A)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFFF1F5F9)
                            ) {
                                Text(
                                    text = booking.bookingReference,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = Color(0xFF334155),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${booking.passengerPhone} • ${booking.seatsCount} seat(s) • ${formatNaira(booking.totalFareNaira)}",
                                fontSize = 11.5.sp,
                                color = Color(0xFF64748B)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            PaymentStatusChip(status = booking.paymentStatus)
                        }
                    }
                }

                BoardingStatusChip(status = booking.boardingStatus)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Pickup, Destination, Distance & ETA Box
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFFF8FAFC),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.MyLocation,
                            contentDescription = null,
                            tint = Color(0xFF0369A1),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Pickup: ${booking.pickupLocation}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF0F172A)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Place,
                            contentDescription = null,
                            tint = RydBrandGreen,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Destination: ${booking.destinationLocation}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF0F172A)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Distance: ${booking.distanceMeters}m (~${booking.estimatedArrivalMinutes} mins ETA)",
                            fontSize = 11.sp,
                            color = Color(0xFF0369A1),
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Code: ${booking.verificationCode}",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = RydGreenPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Boarding Verification & Step-by-Step Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!booking.isQrVerified && booking.boardingStatus != "Boarded") {
                    if (isWaitingAcceptance) {
                        OutlinedButton(
                            onClick = onAcceptQuick,
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                            border = BorderStroke(1.dp, RydBrandGreen),
                            modifier = Modifier.weight(0.95f)
                        ) {
                            Text("Accept", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = RydGreenPrimary)
                        }
                    } else if (booking.boardingStatus != "Arrived at Pickup") {
                        OutlinedButton(
                            onClick = onMarkArrivedPickup,
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                            border = BorderStroke(1.dp, Color(0xFF0369A1)),
                            modifier = Modifier.weight(1.05f)
                        ) {
                            Text("Arrived Pickup", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0369A1))
                        }
                    }

                    Button(
                        onClick = onVerifyQrQuick,
                        colors = ButtonDefaults.buttonColors(containerColor = RydGreenPrimary),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier.weight(1.35f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Verify QR (${booking.verificationCode})", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = RydGreenContainer,
                        modifier = Modifier.weight(1.3f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = null,
                                tint = RydBrandGreen,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "QR Verified & Boarded",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = RydOnGreenContainer
                            )
                        }
                    }
                }

                // Status cycle buttons
                listOf("Waiting", "Boarded", "Completed").forEach { statusOption ->
                    val isCurrent = booking.boardingStatus == statusOption
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isCurrent) Color(0xFF0F172A) else Color(0xFFF1F5F9),
                        modifier = Modifier.clickable { onUpdateBoardingStatus(statusOption) }
                    ) {
                        Text(
                            text = statusOption,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isCurrent) Color.White else Color(0xFF475569),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DriverBookingsAndTripsScreen(
    institution: InstitutionEntity,
    driverBookings: List<BookingEntity>,
    newBookingRequests: List<BookingEntity>,
    driverTrips: List<TripEntity>,
    isDriverOnline: Boolean,
    onAcceptBooking: (String) -> Unit,
    onRejectBooking: (String) -> Unit,
    onMarkArrivedPickup: (String) -> Unit,
    onVerifyQrQuick: (String) -> Unit,
    onOpenActiveRideTab: () -> Unit
) {
    var selectedFilter by remember { mutableIntStateOf(0) }
    val filters = listOf(
        "New Requests (${newBookingRequests.size})",
        "Active / Boarding",
        "Completed",
        "Rejected / Cancelled"
    )

    val filteredBookings = remember(selectedFilter, driverBookings, newBookingRequests) {
        when (selectedFilter) {
            0 -> newBookingRequests
            1 -> driverBookings.filter {
                it.status == BookingStatus.CONFIRMED &&
                    (it.isQrVerified || it.boardingStatus == "Boarded" || it.boardingStatus == "Accepted" || it.boardingStatus == "Arrived at Pickup" || it.driverNotes.contains("[ACCEPTED]"))
            }
            2 -> driverBookings.filter { it.status == BookingStatus.COMPLETED || it.boardingStatus == "Completed" }
            3 -> driverBookings.filter { it.status == BookingStatus.REJECTED || it.status == BookingStatus.CANCELLED }
            else -> driverBookings
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        contentPadding = PaddingValues(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = RydGreenContainer.copy(alpha = 0.55f)),
                border = BorderStroke(1.dp, RydBrandGreen.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${institution.shortCode} DRIVER BOOKINGS & MANIFEST",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = RydGreenPrimary,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "Manage New Ride Requests, Boarding & Trip History",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF0F172A)
                        )
                    }
                    Button(
                        onClick = onOpenActiveRideTab,
                        colors = ButtonDefaults.buttonColors(containerColor = RydGreenPrimary),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Active Ride", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(filters.size) { idx ->
                    val isSel = selectedFilter == idx
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSel) RydGreenPrimary else Color.White,
                        border = BorderStroke(1.dp, if (isSel) RydGreenPrimary else Color(0xFFCBD5E1)),
                        modifier = Modifier.clickable { selectedFilter = idx }
                    ) {
                        Text(
                            text = filters[idx],
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSel) Color.White else Color(0xFF334155),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                        )
                    }
                }
            }
        }

        if (filteredBookings.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.EventSeat,
                            contentDescription = null,
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(34.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (selectedFilter == 0) "No new ride requests." else "No bookings in this category.",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = "All ${institution.shortCode} passenger bookings assigned to your shuttle appear here.",
                            fontSize = 11.5.sp,
                            color = Color(0xFF64748B),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(filteredBookings, key = { "tab_bk_${it.id}" }) { booking ->
                if (selectedFilter == 0) {
                    NewBookingRequestCard(
                        booking = booking,
                        isDriverOnline = isDriverOnline,
                        onAcceptBooking = { onAcceptBooking(booking.id) },
                        onRejectBooking = { onRejectBooking(booking.id) }
                    )
                } else {
                    AssignedPassengerManifestCard(
                        booking = booking,
                        onAcceptQuick = { onAcceptBooking(booking.id) },
                        onMarkArrivedPickup = { onMarkArrivedPickup(booking.id) },
                        onVerifyQrQuick = { onVerifyQrQuick(booking.bookingReference) },
                        onUpdateBoardingStatus = {}
                    )
                }
            }
        }

        // Trip History Section
        item {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "${institution.shortCode} DRIVER TRIP HISTORY (${driverTrips.size})",
                fontSize = 13.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF0F172A),
                letterSpacing = 0.5.sp
            )
        }

        items(driverTrips, key = { "hist_trip_${it.id}" }) { trip ->
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${trip.origin} → ${trip.destination}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = "${trip.tripNumber} • ${trip.vehicleRegistration} • ${trip.bookedSeatsCount}/${trip.totalCapacity} seats",
                            fontSize = 11.5.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                    TripStatusChip(status = trip.status)
                }
            }
        }
    }
}

@Composable
private fun DriverActiveRideScreen(
    institution: InstitutionEntity,
    campus: CampusEntity?,
    driverProfile: DriverProfileEntity,
    activeTrip: TripEntity?,
    activeTripBookings: List<BookingEntity>,
    newBookingRequests: List<BookingEntity>,
    boardedSeatsCount: Int,
    serviceZones: List<ServiceZoneEntity>,
    pickupLocations: List<PickupLocationEntity>,
    routes: List<RouteEntity>,
    hasGpsPermission: Boolean,
    deviceGpsCoordinates: Pair<Double, Double>?,
    qrScanFeedback: QrScanFeedback?,
    onDismissQrFeedback: () -> Unit,
    onRequestGpsPermission: () -> Unit,
    onRefreshGps: () -> Unit,
    onAcceptBooking: (String) -> Unit,
    onRejectBooking: (String) -> Unit,
    onMarkArrivedPickup: (String) -> Unit,
    onVerifyQrQuick: (String) -> Unit,
    onUpdateBoardingStatus: (String, String) -> Unit,
    onOpenQrScannerModal: () -> Unit,
    onStartTrip: (String) -> Unit,
    onCompleteTrip: (String) -> Unit,
    onPublishNewTrip: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        contentPadding = PaddingValues(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = RydGreenContainer.copy(alpha = 0.55f)),
                border = BorderStroke(1.dp, RydBrandGreen.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${institution.shortCode} ACTIVE RIDE & BOARDING",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = RydGreenPrimary,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = activeTrip?.let { "${it.origin} → ${it.destination}" } ?: "No Active Trip Published",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = "Pickup navigation • QR/Code verification • Start & Complete Trip",
                            fontSize = 11.sp,
                            color = Color(0xFF475569)
                        )
                    }
                    Button(
                        onClick = onOpenQrScannerModal,
                        colors = ButtonDefaults.buttonColors(containerColor = RydGreenPrimary),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.QrCodeScanner, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Verify QR", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            androidx.compose.animation.AnimatedVisibility(visible = qrScanFeedback != null) {
                qrScanFeedback?.let { feedback ->
                    QrVerificationFeedbackBanner(
                        feedback = feedback,
                        onDismiss = onDismissQrFeedback
                    )
                }
            }
        }

        if (newBookingRequests.isNotEmpty()) {
            item {
                Text(
                    text = "PENDING RIDE REQUESTS (${newBookingRequests.size})",
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Black,
                    color = RydBrandRed
                )
            }
            items(newBookingRequests, key = { "act_new_${it.id}" }) { req ->
                NewBookingRequestCard(
                    booking = req,
                    isDriverOnline = driverProfile.isOnline,
                    onAcceptBooking = { onAcceptBooking(req.id) },
                    onRejectBooking = { onRejectBooking(req.id) }
                )
            }
        }

        if (activeTrip == null) {
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.DirectionsCar,
                            contentDescription = null,
                            tint = RydGreenPrimary,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No Active Trip Currently Assigned",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color(0xFF0F172A)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Publish a new campus route trip to start boarding passengers.",
                            fontSize = 12.sp,
                            color = Color(0xFF64748B),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = onPublishNewTrip,
                            colors = ButtonDefaults.buttonColors(containerColor = RydGreenPrimary)
                        ) {
                            Text("Publish New Route Trip", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            item {
                ActiveDriverTripControlCard(
                    institution = institution,
                    trip = activeTrip,
                    boardedCount = boardedSeatsCount,
                    bookingsCount = activeTripBookings.size,
                    onStartTrip = { onStartTrip(activeTrip.id) },
                    onCompleteTrip = { onCompleteTrip(activeTrip.id) },
                    onOpenQrScanner = onOpenQrScannerModal
                )
            }

            item {
                DriverInstitutionMapCard(
                    institution = institution,
                    campus = campus,
                    driverProfile = driverProfile,
                    serviceZones = serviceZones,
                    pickupLocations = pickupLocations,
                    routes = routes,
                    assignedBookings = activeTripBookings,
                    hasGpsPermission = hasGpsPermission,
                    deviceGpsCoordinates = deviceGpsCoordinates,
                    onRequestGpsPermission = onRequestGpsPermission,
                    onRefreshGps = onRefreshGps
                )
            }

            item {
                Text(
                    text = "PASSENGERS ON THIS RIDE (${activeTripBookings.size})",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF0F172A)
                )
            }

            items(activeTripBookings, key = { "act_pass_${it.id}" }) { booking ->
                AssignedPassengerManifestCard(
                    booking = booking,
                    onAcceptQuick = { onAcceptBooking(booking.id) },
                    onMarkArrivedPickup = { onMarkArrivedPickup(booking.id) },
                    onVerifyQrQuick = { onVerifyQrQuick(booking.bookingReference) },
                    onUpdateBoardingStatus = { newStatus ->
                        onUpdateBoardingStatus(booking.id, newStatus)
                    }
                )
            }
        }
    }
}

@Composable
private fun InstitutionRoutesAndPickupsCard(
    institution: InstitutionEntity,
    campus: CampusEntity?,
    routes: List<RouteEntity>,
    pickupLocations: List<PickupLocationEntity>,
    serviceZones: List<ServiceZoneEntity>
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("driver_institution_routes_card")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Route,
                        contentDescription = null,
                        tint = RydGreenPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = "${institution.shortCode} Approved Routes & Pickup Points",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = "${campus?.name ?: institution.name} • ${routes.size} routes • ${pickupLocations.size} pickup stands",
                            fontSize = 11.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Routes list for this institution
            routes.forEach { route ->
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFF8FAFC),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${route.originName} → ${route.destinationName}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.5.sp,
                                color = Color(0xFF0F172A)
                            )
                            Text(
                                text = "Stops: ${route.pickupPoints}",
                                fontSize = 11.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = RydGreenContainer
                        ) {
                            Text(
                                text = "${formatNaira(route.standardFareNaira)}/seat",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = RydOnGreenContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            if (pickupLocations.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${institution.shortCode} Designated Pickup Points:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF475569)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    pickupLocations.forEach { loc ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFE0F2FE)
                        ) {
                            Text(
                                text = loc.name,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0369A1),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QrVerificationFeedbackBanner(
    feedback: QrScanFeedback,
    onDismiss: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (feedback.isSuccess) RydGreenContainer else Color(0xFFFEE2E2),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (feedback.isSuccess) RydBrandGreen else RydError
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (feedback.isSuccess) Icons.Default.Verified else Icons.Default.Warning,
                    contentDescription = null,
                    tint = if (feedback.isSuccess) RydBrandGreen else RydError,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = feedback.title,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 13.sp,
                        color = if (feedback.isSuccess) RydOnGreenContainer else RydError
                    )
                    Text(
                        text = feedback.message,
                        fontSize = 11.5.sp,
                        color = if (feedback.isSuccess) RydOnGreenContainer else RydError
                    )
                }
            }
            IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Dismiss",
                    tint = if (feedback.isSuccess) RydOnGreenContainer else RydError,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun ServiceZoneQueueTransparencyCard(
    institutionShortCode: String,
    zoneName: String,
    routeDirection: String,
    peerEntries: List<DriverQueueEntryEntity>,
    currentDriverId: String
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("driver_institution_queue_card")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.FormatListNumbered,
                        contentDescription = null,
                        tint = RydGreenPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = "Live $institutionShortCode Queue Order • $zoneName",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = "FIFO by zone entry timestamp + route compatibility ($institutionShortCode network only)",
                            fontSize = 11.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (peerEntries.isEmpty()) {
                Text(
                    text = "No queued vehicles currently in $zoneName ($institutionShortCode).",
                    fontSize = 12.sp,
                    color = Color(0xFF64748B)
                )
            } else {
                peerEntries.forEachIndexed { index, entry ->
                    val isMe = entry.driverId == currentDriverId
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isMe) RydGreenContainer.copy(alpha = 0.45f) else Color(0xFFF8FAFC),
                        border = if (isMe) androidx.compose.foundation.BorderStroke(1.dp, RydBrandGreen) else null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Surface(
                                    shape = CircleShape,
                                    color = if (index == 0) RydGreenPrimary else Color(0xFF334155),
                                    modifier = Modifier.size(26.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = "#${index + 1}",
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "${entry.driverName} (${entry.vehicleRegistration})" + if (isMe) " • YOU" else "",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.5.sp,
                                        color = Color(0xFF0F172A)
                                    )
                                    Text(
                                        text = "${entry.routeDirection} • Joined ${entry.entryTimeFormatted} • ${entry.availableSeats}/${entry.totalCapacity} seats",
                                        fontSize = 11.sp,
                                        color = Color(0xFF64748B)
                                    )
                                }
                            }
                            DriverQueueStatusChip(status = entry.queueStatus)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ZoneAndRouteSelectionModal(
    institution: InstitutionEntity,
    serviceZones: List<ServiceZoneEntity>,
    routes: List<RouteEntity>,
    pickupLocations: List<PickupLocationEntity>,
    currentZoneName: String,
    currentRouteDirection: String,
    onDismiss: () -> Unit,
    onSelectZoneAndRoute: (String, String, String) -> Unit
) {
    var selectedZone by remember {
        mutableStateOf(
            serviceZones.find { it.name.equals(currentZoneName, ignoreCase = true) }
                ?: serviceZones.firstOrNull()
        )
    }

    val supportedDestinations = remember(selectedZone, pickupLocations, routes) {
        val zoneDests = selectedZone?.supportedRoutes
            ?.split(",")
            ?.map { it.trim() }
            ?.filter { it.isNotBlank() }
            .orEmpty()
        if (zoneDests.isNotEmpty()) {
            zoneDests
        } else {
            val routeDests = routes.map { it.destinationName }.distinct()
            routeDests.ifEmpty { pickupLocations.map { it.name } }
        }
    }

    var selectedDestination by remember(selectedZone) {
        val currentDest = currentRouteDirection.substringAfter("→", "").trim()
        mutableStateOf(
            if (currentDest in supportedDestinations) currentDest
            else supportedDestinations.firstOrNull() ?: ""
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Select ${institution.shortCode} Service Zone & Route",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Drivers enter a FIFO queue based on their ${institution.shortCode} service zone and route direction.",
                    fontSize = 12.sp,
                    color = Color(0xFF64748B)
                )

                Text(
                    text = "1. Select ${institution.shortCode} Service Zone:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.5.sp
                )

                serviceZones.forEach { zone ->
                    val isSelected = selectedZone?.id == zone.id
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) RydGreenContainer else Color(0xFFF8FAFC),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) RydGreenPrimary else Color(0xFFE2E8F0)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedZone = zone
                                val firstDest = zone.supportedRoutes.split(",").firstOrNull()?.trim() ?: ""
                                if (firstDest.isNotBlank()) {
                                    selectedDestination = firstDest
                                }
                            }
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = zone.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color(0xFF0F172A)
                            )
                            Text(
                                text = "Supported destinations: ${zone.supportedRoutes}",
                                fontSize = 11.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "2. Select Route Destination:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.5.sp
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    supportedDestinations.forEach { dest ->
                        FilterChip(
                            selected = selectedDestination.equals(dest, ignoreCase = true),
                            onClick = { selectedDestination = dest },
                            label = { Text(dest, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = RydGreenPrimary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val zone = selectedZone
                    if (zone != null) {
                        val cleanOrigin = zone.name.removeSuffix(" Zone").trim()
                        val fullDirection = if (selectedDestination.isNotBlank()) {
                            "$cleanOrigin → $selectedDestination"
                        } else {
                            cleanOrigin
                        }
                        onSelectZoneAndRoute(zone.id, zone.name, fullDirection)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = RydGreenPrimary)
            ) {
                Text("Join Zone Queue", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PublishTripDialog(
    institution: InstitutionEntity,
    driverProfile: DriverProfileEntity?,
    pickupLocations: List<PickupLocationEntity>,
    serviceZones: List<ServiceZoneEntity>,
    routes: List<RouteEntity>,
    onDismiss: () -> Unit,
    onPublish: (
        VehicleCategory,
        String,
        String,
        String,
        String,
        String,
        String,
        String,
        Int,
        Int,
        String,
        String
    ) -> Unit
) {
    // Strictly use only pickup locations / service zones belonging to the driver's institution
    val locationNames = remember(pickupLocations, serviceZones, routes) {
        val fromPickups = pickupLocations.map { it.name }
        if (fromPickups.isNotEmpty()) {
            fromPickups
        } else {
            val fromZones = serviceZones.map { it.name.removeSuffix(" Zone").trim() }
            fromZones.ifEmpty {
                routes.flatMap { listOf(it.originName, it.destinationName) }.distinct()
            }
        }
    }

    val defaultSeatFare = routes.firstOrNull()?.standardFareNaira ?: 150
    val defaultSeats = driverProfile?.vehicleCategory?.defaultSeats ?: 6

    var selectedCategory by remember { mutableStateOf(driverProfile?.vehicleCategory ?: VehicleCategory.SUV_SIENNA) }
    var vehicleModel by remember { mutableStateOf(driverProfile?.vehicleMakeModel ?: "${institution.shortCode} Campus Shuttle") }
    var licensePlate by remember { mutableStateOf(driverProfile?.registrationNumber?.ifBlank { driverProfile.licensePlate } ?: "${institution.shortCode}-SH-01") }
    var origin by remember { mutableStateOf(locationNames.firstOrNull() ?: "") }
    var destination by remember { mutableStateOf(locationNames.getOrNull(1) ?: locationNames.firstOrNull() ?: "") }
    var departureTime by remember { mutableStateOf("3 mins away") }
    var departureDate by remember { mutableStateOf("Today") }
    var totalFare by remember { mutableStateOf((defaultSeatFare * defaultSeats).toString()) }
    var totalCapacity by remember { mutableStateOf(defaultSeats.toString()) }
    var pickupPoints by remember {
        mutableStateOf(
            routes.firstOrNull()?.pickupPoints
                ?: locationNames.take(4).joinToString(", ")
        )
    }
    var notes by remember { mutableStateOf("") }

    var originExpanded by remember { mutableStateOf(false) }
    var destExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Publish ${institution.shortCode} Campus Route Trip",
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ExposedDropdownMenuBox(
                    expanded = originExpanded,
                    onExpandedChange = { originExpanded = !originExpanded }
                ) {
                    OutlinedTextField(
                        value = origin,
                        onValueChange = { origin = it },
                        label = { Text("Service Zone / Origin (${institution.shortCode})") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = originExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = originExpanded,
                        onDismissRequest = { originExpanded = false }
                    ) {
                        locationNames.forEach { loc ->
                            DropdownMenuItem(
                                text = { Text(loc) },
                                onClick = {
                                    origin = loc
                                    originExpanded = false
                                }
                            )
                        }
                    }
                }

                ExposedDropdownMenuBox(
                    expanded = destExpanded,
                    onExpandedChange = { destExpanded = !destExpanded }
                ) {
                    OutlinedTextField(
                        value = destination,
                        onValueChange = { destination = it },
                        label = { Text("Route Destination (${institution.shortCode})") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = destExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = destExpanded,
                        onDismissRequest = { destExpanded = false }
                    ) {
                        locationNames.forEach { loc ->
                            DropdownMenuItem(
                                text = { Text(loc) },
                                onClick = {
                                    destination = loc
                                    destExpanded = false
                                }
                            )
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = totalCapacity,
                        onValueChange = { totalCapacity = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Total Seats") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = totalFare,
                        onValueChange = { totalFare = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Total Trip Fare (₦)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = pickupPoints,
                    onValueChange = { pickupPoints = it },
                    label = { Text("Supported Pickup Stops along Route") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = licensePlate,
                    onValueChange = { licensePlate = it },
                    label = { Text("Shuttle Reg / Plate Number") },
                    placeholder = { Text("e.g., ${institution.shortCode}-SH-01") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val fareInt = totalFare.toIntOrNull() ?: (defaultSeatFare * defaultSeats)
                    val capInt = totalCapacity.toIntOrNull() ?: defaultSeats
                    onPublish(
                        selectedCategory,
                        vehicleModel,
                        licensePlate,
                        origin,
                        destination,
                        "$origin → $destination",
                        departureTime,
                        departureDate,
                        fareInt,
                        capInt,
                        pickupPoints,
                        notes
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = RydGreenPrimary)
            ) {
                Text("Publish to ${institution.shortCode} Queue", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
