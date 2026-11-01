package com.example.ui.passenger

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.EventSeat
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.model.BookingEntity
import com.example.data.model.BookingStatus
import com.example.data.model.CampusEntity
import com.example.data.model.InstitutionEntity
import com.example.data.model.NotificationEntity
import com.example.data.model.PassengerWalletEntity
import com.example.data.model.PickupLocationEntity
import com.example.data.model.PlatformSettingsEntity
import com.example.data.model.ReportEntity
import com.example.data.model.RouteEntity
import com.example.data.model.ServiceZoneEntity
import com.example.data.model.TripEntity
import com.example.data.model.TripStatus
import com.example.data.model.UserEntity
import com.example.data.model.WalletTransactionEntity
import com.example.ui.components.AddMoneyToRydWalletDialog
import com.example.ui.components.BoardingStatusChip
import com.example.ui.components.BookingQrCodeCard
import com.example.ui.components.BookingStatusChip
import com.example.ui.components.PassengerRydWalletScreen
import com.example.ui.components.PaymentStatusChip
import com.example.ui.components.PortalSupportAndNotificationsScreen
import com.example.ui.components.SeatAvailabilityIndicator
import com.example.ui.components.TransportNetworkSetupInProgressCard
import com.example.ui.components.TripStatusChip
import com.example.ui.components.UniversityLiveMapView
import com.example.ui.components.VehicleIconBadge
import com.example.ui.components.formatNaira
import com.example.ui.theme.DarkRydSurfaceCard
import com.example.ui.theme.RydBrandGreen
import com.example.ui.theme.RydBrandRed
import com.example.ui.theme.RydError
import com.example.ui.theme.RydGoldContainer
import com.example.ui.theme.RydGreenContainer
import com.example.ui.theme.RydGreenPrimary
import com.example.ui.theme.RydOnGoldContainer
import com.example.ui.theme.RydOnGreenContainer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PassengerDashboard(
    currentUser: UserEntity?,
    currentInstitution: InstitutionEntity? = null,
    currentCampus: CampusEntity? = null,
    allInstitutions: List<InstitutionEntity> = emptyList(),
    allCampuses: List<CampusEntity> = emptyList(),
    trips: List<TripEntity>,
    bookings: List<BookingEntity>,
    pickupLocations: List<PickupLocationEntity>,
    serviceZones: List<ServiceZoneEntity> = emptyList(),
    routes: List<RouteEntity> = emptyList(),
    activeBooking: BookingEntity?,
    activeTrip: TripEntity?,
    passengerWallet: PassengerWalletEntity? = null,
    walletTransactions: List<WalletTransactionEntity> = emptyList(),
    notifications: List<NotificationEntity> = emptyList(),
    supportTickets: List<ReportEntity> = emptyList(),
    platformSettings: PlatformSettingsEntity? = null,
    passengerPickup: String,
    passengerDestination: String,
    onPickupChanged: (String) -> Unit,
    onDestinationChanged: (String) -> Unit,
    onFindRides: () -> Unit,
    onConfirmRide: (tripId: String, pickup: String, dest: String) -> Unit,
    onCancelBooking: (String) -> Unit,
    onTopUpWallet: (amountNaira: Int, methodLabel: String) -> Unit = { _, _ -> },
    onRateRide: (bookingId: String, ratingStars: Int, comment: String) -> Unit = { _, _, _ -> },
    onMarkNotificationRead: (String) -> Unit = {},
    onMarkAllNotificationsRead: () -> Unit = {},
    onSubmitComplaint: (
        category: String,
        subject: String,
        description: String,
        tripId: String,
        bookingId: String,
        reportedDriverId: String,
        reportedVehiclePlate: String
    ) -> Unit = { _, _, _, _, _, _, _ -> },
    onSubmitTechnicalProblem: (
        issueCategory: String,
        description: String,
        tripId: String,
        bookingId: String,
        screenshotReference: String
    ) -> Unit = { _, _, _, _, _ -> },
    onSelectInstitutionAndCampus: (institutionId: String, campusId: String) -> Unit = { _, _ -> },
    onRegisterPassenger: (fullName: String, phone: String, email: String, institutionId: String, campusId: String) -> Unit = { _, _, _, _, _ -> },
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var selectedTripForConfirmation by remember { mutableStateOf<TripEntity?>(null) }
    var showAddMoneyFromBookingModal by remember { mutableStateOf(false) }
    var showMessageDriverDialog by remember { mutableStateOf<String?>(null) }
    var showCallDriverDialog by remember { mutableStateOf<String?>(null) }

    val unreadNotificationsCount = notifications.count { !it.isRead }
    val walletBalance = passengerWallet?.balanceNaira ?: 0

    val context = LocalContext.current
    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        val granted = perms[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                perms[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        hasLocationPermission = granted
        if (granted) {
            onPickupChanged("Current Location (GPS: ${currentCampus?.name ?: pickupLocations.firstOrNull()?.name ?: "Campus"})")
        }
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
            when (selectedTab) {
                0 -> {
                    // TAB 0: Find a Ride (Home & Search Flow)
                    PassengerSearchAndRidesScreen(
                        currentUser = currentUser,
                        currentInstitution = currentInstitution,
                        currentCampus = currentCampus,
                        institutionCampuses = allCampuses.filter { it.institutionId == (currentInstitution?.id ?: currentUser?.institutionId ?: "") },
                        walletBalanceNaira = walletBalance,
                        pickup = passengerPickup,
                        destination = passengerDestination,
                        locations = pickupLocations,
                        serviceZones = serviceZones,
                        routes = routes,
                        trips = trips,
                        hasLocationPermission = hasLocationPermission,
                        onRequestLocation = {
                            locationPermissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        },
                        onPickupChanged = onPickupChanged,
                        onDestinationChanged = onDestinationChanged,
                        onFindRides = onFindRides,
                        onBookSeatClicked = { trip ->
                            selectedTripForConfirmation = trip
                        },
                        onOpenWalletTab = { selectedTab = 2 },
                        onSelectCampus = { campId ->
                            val instId = currentInstitution?.id ?: currentUser?.institutionId ?: ""
                            if (instId.isNotBlank()) {
                                onSelectInstitutionAndCampus(instId, campId)
                            }
                        }
                    )
                }
                1 -> {
                    // TAB 1: Dedicated My Ride Screen (Live Tracking, Payment Status & Boarding)
                    PassengerMyRideScreen(
                        booking = activeBooking,
                        trip = activeTrip,
                        onCallDriver = { showCallDriverDialog = it },
                        onMessageDriver = { showMessageDriverDialog = it },
                        onCancelRide = onCancelBooking,
                        onFindRideClick = { selectedTab = 0 }
                    )
                }
                2 -> {
                    // TAB 2: Dedicated Passenger RydWallet Screen
                    PassengerRydWalletScreen(
                        currentUser = currentUser,
                        currentInstitution = currentInstitution,
                        wallet = passengerWallet,
                        transactions = walletTransactions,
                        activeBooking = activeBooking,
                        bookings = bookings,
                        platformSettings = platformSettings,
                        onTopUpWallet = onTopUpWallet,
                        onGoToFindRide = { selectedTab = 0 }
                    )
                }
                3 -> {
                    // TAB 3: My Bookings, Refunds & Ratings History
                    PassengerBookingsHistoryScreen(
                        bookings = bookings,
                        trips = trips,
                        onSelectBookingForTracking = { b ->
                            selectedTab = 1
                        },
                        onCancelBooking = onCancelBooking,
                        onRateRide = onRateRide
                    )
                }
                4 -> {
                    // TAB 4: Notifications, Help & Support, Complaints & Technical Problems
                    PortalSupportAndNotificationsScreen(
                        currentUser = currentUser,
                        currentInstitution = currentInstitution,
                        currentCampus = currentCampus,
                        notifications = notifications,
                        myTickets = supportTickets,
                        bookings = bookings,
                        trips = trips,
                        emergencyContact = platformSettings?.emergencyContact ?: "+234 800 RYDROVER",
                        onMarkNotificationRead = onMarkNotificationRead,
                        onMarkAllNotificationsRead = onMarkAllNotificationsRead,
                        onSubmitComplaint = onSubmitComplaint,
                        onSubmitTechnicalProblem = onSubmitTechnicalProblem
                    )
                }
            }
        }

        // Bottom Navigation Bar for Passenger Portal (Strictly Passenger-Only)
        NavigationBar(
            containerColor = Color.White,
            tonalElevation = 8.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            NavigationBarItem(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                icon = { Icon(Icons.Default.Search, contentDescription = "Find Ride") },
                label = { Text("Find Ride", fontSize = 10.sp, fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = RydBrandGreen,
                    selectedTextColor = RydBrandGreen,
                    indicatorColor = RydGreenContainer
                ),
                modifier = Modifier.testTag("nav_find_ride")
            )
            NavigationBarItem(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                icon = {
                    if (activeBooking != null) {
                        Surface(
                            shape = CircleShape,
                            color = RydBrandGreen,
                            modifier = Modifier.size(8.dp)
                        ) {}
                    }
                    Icon(Icons.Default.DirectionsCar, contentDescription = "My Ride")
                },
                label = { Text("My Ride", fontSize = 10.sp, fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = RydBrandGreen,
                    selectedTextColor = RydBrandGreen,
                    indicatorColor = RydGreenContainer
                ),
                modifier = Modifier.testTag("nav_my_ride")
            )
            NavigationBarItem(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                icon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = "RydWallet") },
                label = { Text("RydWallet", fontSize = 10.sp, fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = RydBrandGreen,
                    selectedTextColor = RydBrandGreen,
                    indicatorColor = RydGreenContainer
                ),
                modifier = Modifier.testTag("nav_rydwallet")
            )
            NavigationBarItem(
                selected = selectedTab == 3,
                onClick = { selectedTab = 3 },
                icon = { Icon(Icons.Default.History, contentDescription = "Bookings") },
                label = { Text("Bookings", fontSize = 10.sp, fontWeight = if (selectedTab == 3) FontWeight.Bold else FontWeight.Normal) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = RydBrandGreen,
                    selectedTextColor = RydBrandGreen,
                    indicatorColor = RydGreenContainer
                ),
                modifier = Modifier.testTag("nav_my_bookings")
            )
            NavigationBarItem(
                selected = selectedTab == 4,
                onClick = { selectedTab = 4 },
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
                label = { Text("Support", fontSize = 10.sp, fontWeight = if (selectedTab == 4) FontWeight.Bold else FontWeight.Normal) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = RydBrandGreen,
                    selectedTextColor = RydBrandGreen,
                    indicatorColor = RydGreenContainer
                ),
                modifier = Modifier.testTag("nav_passenger_support")
            )
        }
    }

    // Ride Confirmation & RydWallet Payment Modal ("Book & Pay ₦...")
    selectedTripForConfirmation?.let { trip ->
        RideConfirmationDialog(
            trip = trip,
            pickup = passengerPickup.ifBlank { trip.origin },
            destination = passengerDestination.ifBlank { trip.destination },
            walletBalanceNaira = walletBalance,
            onDismiss = { selectedTripForConfirmation = null },
            onAddMoneyClicked = {
                showAddMoneyFromBookingModal = true
            },
            onConfirmRide = {
                onConfirmRide(
                    trip.id,
                    passengerPickup.ifBlank { trip.origin },
                    passengerDestination.ifBlank { trip.destination }
                )
                selectedTripForConfirmation = null
                selectedTab = 1 // Immediately transition to dedicated "My Ride" screen
            }
        )
    }

    if (showAddMoneyFromBookingModal) {
        AddMoneyToRydWalletDialog(
            currentBalance = walletBalance,
            paymentEnvironmentLabel = platformSettings?.paymentEnvironmentMode?.label ?: "Demo Wallet Mode",
            onDismiss = { showAddMoneyFromBookingModal = false },
            onConfirmTopUp = { amount, method ->
                onTopUpWallet(amount, method)
                showAddMoneyFromBookingModal = false
            }
        )
    }

    // Call Driver Simulation Modal
    showCallDriverDialog?.let { driverPhone ->
        AlertDialog(
            onDismissRequest = { showCallDriverDialog = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Call, contentDescription = null, tint = RydBrandGreen)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Call University Shuttle Driver", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column {
                    Text("Connecting directly to driver telephone line:")
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(driverPhone, fontSize = 17.sp, fontWeight = FontWeight.Black, color = Color(0xFF0F172A))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Standard voice network charges apply.", fontSize = 11.5.sp, color = Color(0xFF64748B))
                }
            },
            confirmButton = {
                Button(
                    onClick = { showCallDriverDialog = null },
                    colors = ButtonDefaults.buttonColors(containerColor = RydBrandGreen)
                ) {
                    Text("Close")
                }
            }
        )
    }

    // Message Driver Simulation Modal
    showMessageDriverDialog?.let { driverName ->
        var messageText by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showMessageDriverDialog = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Message, contentDescription = null, tint = RydBrandGreen)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Message Driver", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column {
                    Text("Send a quick arrival or waiting note to $driverName:", fontSize = 12.sp, color = Color(0xFF64748B))
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = messageText,
                        onValueChange = { messageText = it },
                        placeholder = { Text("e.g., I am standing by the gate canopy...") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showMessageDriverDialog = null },
                    colors = ButtonDefaults.buttonColors(containerColor = RydBrandGreen)
                ) {
                    Text("Send Note")
                }
            },
            dismissButton = {
                TextButton(onClick = { showMessageDriverDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun PassengerSchoolRegistrationDialog(
    currentUser: UserEntity?,
    currentInstitution: InstitutionEntity?,
    currentCampus: CampusEntity?,
    allInstitutions: List<InstitutionEntity>,
    allCampuses: List<CampusEntity>,
    onDismiss: () -> Unit,
    onSelectInstitution: (institutionId: String, campusId: String) -> Unit,
    onRegisterNewPassenger: (fullName: String, phone: String, email: String, institutionId: String, campusId: String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedInstId by remember { mutableStateOf(currentInstitution?.id ?: allInstitutions.firstOrNull()?.id ?: "") }
    val matchingCampuses = remember(selectedInstId, allCampuses) {
        allCampuses.filter { it.institutionId == selectedInstId }
    }
    var selectedCampusId by remember(selectedInstId) {
        mutableStateOf(
            if (selectedInstId == currentInstitution?.id && currentCampus != null) currentCampus.id
            else matchingCampuses.firstOrNull()?.id ?: ""
        )
    }
    var isNewRegistrationMode by remember { mutableStateOf(false) }
    var fullName by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }

    val filteredInstitutions = remember(searchQuery, allInstitutions) {
        if (searchQuery.isBlank()) allInstitutions
        else allInstitutions.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
                it.shortCode.contains(searchQuery, ignoreCase = true) ||
                it.city.contains(searchQuery, ignoreCase = true) ||
                it.state.contains(searchQuery, ignoreCase = true) ||
                it.institutionType.label.contains(searchQuery, ignoreCase = true)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "What school do you attend?",
                    fontWeight = FontWeight.Black,
                    fontSize = 17.sp,
                    color = Color(0xFF0F172A)
                )
                Text(
                    text = "Select your institution to load its isolated transport network, zones, routes & fares.",
                    fontSize = 11.5.sp,
                    color = Color(0xFF64748B)
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Mode Switch: Switch Current Passenger School vs Register New Passenger
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (!isNewRegistrationMode) RydGreenPrimary else Color(0xFFF1F5F9),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { isNewRegistrationMode = false }
                    ) {
                        Text(
                            text = "Select My School",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (!isNewRegistrationMode) Color.White else Color(0xFF334155),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 7.dp)
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isNewRegistrationMode) RydGreenPrimary else Color(0xFFF1F5F9),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { isNewRegistrationMode = true }
                    ) {
                        Text(
                            text = "New Registration",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isNewRegistrationMode) Color.White else Color(0xFF334155),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 7.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (isNewRegistrationMode) {
                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        label = { Text("Student / Passenger Full Name") },
                        placeholder = { Text("e.g., Amaka Okon") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedTextField(
                            value = phoneNumber,
                            onValueChange = { phoneNumber = it },
                            label = { Text("Phone") },
                            placeholder = { Text("+234 803...") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("School Email") },
                            placeholder = { Text("student@edu.ng") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Search Institution Field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search university, polytechnic, college...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_institution_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "AVAILABLE INSTITUTIONS (${filteredInstitutions.size})",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF475569)
                )
                Spacer(modifier = Modifier.height(4.dp))

                filteredInstitutions.forEach { inst ->
                    val isSelected = inst.id == selectedInstId
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) RydGreenContainer.copy(alpha = 0.6f) else Color(0xFFF8FAFC),
                        border = BorderStroke(
                            if (isSelected) 1.5.dp else 1.dp,
                            if (isSelected) RydBrandGreen else Color(0xFFE2E8F0)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp)
                            .clickable { selectedInstId = inst.id }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = inst.name,
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F172A)
                                )
                                Text(
                                    text = "${inst.institutionType.label} • ${inst.city}, ${inst.state}",
                                    fontSize = 10.5.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isSelected) RydBrandGreen else Color(0xFFE2E8F0)
                            ) {
                                Text(
                                    text = inst.shortCode,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (isSelected) Color.White else Color(0xFF334155),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }
                }

                if (matchingCampuses.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "SELECT CAMPUS (${matchingCampuses.size})",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF475569)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(matchingCampuses) { camp ->
                            val isCampSelected = camp.id == selectedCampusId
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isCampSelected) RydBrandGreen else Color(0xFFF1F5F9),
                                modifier = Modifier.clickable { selectedCampusId = camp.id }
                            ) {
                                Text(
                                    text = camp.name,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isCampSelected) Color.White else Color(0xFF334155),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
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
                    if (isNewRegistrationMode) {
                        onRegisterNewPassenger(
                            fullName.ifBlank { "Student Passenger" },
                            phoneNumber.ifBlank { "+234 803 555 0199" },
                            email.ifBlank { "student@institution.edu.ng" },
                            selectedInstId,
                            selectedCampusId
                        )
                    } else {
                        onSelectInstitution(selectedInstId, selectedCampusId)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = RydBrandGreen)
            ) {
                Text(if (isNewRegistrationMode) "Complete Registration" else "Load Institution Network")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// -------------------------------------------------------------
// TAB 0: PASSENGER SEARCH & AVAILABLE RIDES
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PassengerSearchAndRidesScreen(
    currentUser: UserEntity? = null,
    currentInstitution: InstitutionEntity? = null,
    currentCampus: CampusEntity? = null,
    institutionCampuses: List<CampusEntity> = emptyList(),
    walletBalanceNaira: Int = 0,
    pickup: String,
    destination: String,
    locations: List<PickupLocationEntity>,
    serviceZones: List<ServiceZoneEntity> = emptyList(),
    routes: List<RouteEntity> = emptyList(),
    trips: List<TripEntity>,
    hasLocationPermission: Boolean,
    onRequestLocation: () -> Unit,
    onPickupChanged: (String) -> Unit,
    onDestinationChanged: (String) -> Unit,
    onFindRides: () -> Unit,
    onBookSeatClicked: (TripEntity) -> Unit,
    onOpenWalletTab: () -> Unit = {},
    onSelectCampus: (String) -> Unit = {}
) {
    var destinationExpanded by remember { mutableStateOf(false) }

    val isTransportNetworkEmpty = locations.isEmpty() && serviceZones.isEmpty() && routes.isEmpty()

    val universityDestinations = remember(locations, routes, trips) {
        val stopNames = locations.map { it.name }.distinct()
        if (stopNames.isNotEmpty()) stopNames
        else {
            val routeNames = routes.flatMap { listOf(it.originName, it.destinationName) }.distinct()
            if (routeNames.isNotEmpty()) routeNames else trips.map { it.destination }.distinct()
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(10.dp))

            // Authenticated Institution, Campus & RydWallet Quick Balance Banner
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = RydGreenContainer.copy(alpha = 0.55f)),
                border = BorderStroke(1.dp, RydBrandGreen.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth()
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
                                        text = currentInstitution?.institutionType?.label?.uppercase() ?: "INSTITUTION",
                                        fontSize = 8.5.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.5.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = currentInstitution?.state ?: "",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = RydOnGreenContainer
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = currentInstitution?.name ?: "Loading Institution...",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF0F172A),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "Passenger: ${currentUser?.fullName ?: "Student"} • ${currentInstitution?.shortCode ?: "Institution"} Network",
                                fontSize = 10.5.sp,
                                color = Color(0xFF334155)
                            )
                        }

                        // Quick RydWallet Balance Pill
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = RydGreenPrimary,
                            modifier = Modifier
                                .clickable { onOpenWalletTab() }
                                .testTag("header_rydwallet_pill")
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
                                        text = "RydWallet",
                                        fontSize = 8.5.sp,
                                        color = Color.White.copy(alpha = 0.85f),
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = formatNaira(walletBalanceNaira),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }

                    // Multi-Campus Selector Chips (Strictly within the passenger's own institution)
                    if (institutionCampuses.size > 1) {
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(institutionCampuses) { camp ->
                                val isSelected = camp.id == currentCampus?.id
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = if (isSelected) RydBrandGreen else Color.White,
                                    border = BorderStroke(1.dp, if (isSelected) RydBrandGreen else Color(0xFFCBD5E1)),
                                    modifier = Modifier.clickable { onSelectCampus(camp.id) }
                                ) {
                                    Text(
                                        text = camp.name,
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.White else Color(0xFF334155),
                                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (isTransportNetworkEmpty) {
                Spacer(modifier = Modifier.height(10.dp))
                TransportNetworkSetupInProgressCard(
                    institutionName = currentInstitution?.name ?: "This institution",
                    campusName = currentCampus?.name
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Prominent "Where are you going?" Main Card
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Where are you going?",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF0F172A)
                    )
                    Text(
                        text = "${currentInstitution?.shortCode ?: "Institution"} (${currentCampus?.name ?: "Main Campus"}) • Intra & Inter-campus transport",
                        fontSize = 11.5.sp,
                        color = Color(0xFF64748B)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Pickup Field (Current Location / GPS)
                    Text(
                        text = "PICKUP POINT",
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF475569),
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFF1F5F9),
                        border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.MyLocation,
                                contentDescription = "Current GPS Location",
                                tint = RydBrandGreen,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = pickup.ifBlank { "Current location (GPS Active)" },
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF0F172A),
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = { onRequestLocation() },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Place,
                                    contentDescription = "Detect Location",
                                    tint = RydBrandGreen,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Destination Field with University Locations Dropdown
                    Text(
                        text = "DESTINATION",
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF475569),
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    OutlinedTextField(
                        value = destination,
                        onValueChange = {
                            onDestinationChanged(it)
                            destinationExpanded = it.isNotBlank()
                        },
                        placeholder = { Text("Search campus destination or faculty...") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = Color(0xFF64748B)
                            )
                        },
                        trailingIcon = {
                            if (destination.isNotBlank()) {
                                IconButton(onClick = {
                                    onDestinationChanged("")
                                    destinationExpanded = false
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Clear",
                                        tint = Color(0xFF64748B),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            } else {
                                IconButton(onClick = { destinationExpanded = !destinationExpanded }) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = "Show Destinations",
                                        tint = RydBrandGreen
                                    )
                                }
                            }
                        },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("destination_input")
                    )

                    // Inline Filtered University Destinations (Zero Window popups, zero BLASTBufferQueue errors)
                    val filteredDestinations = remember(destination, universityDestinations) {
                        if (destination.isBlank()) {
                            universityDestinations.take(5)
                        } else {
                            universityDestinations.filter { it.contains(destination, ignoreCase = true) }
                        }
                    }

                    if (destinationExpanded || destination.isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFF8FAFC),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
                                Text(
                                    text = "SELECT CAMPUS DESTINATION",
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF64748B),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                                )
                                filteredDestinations.forEach { destOption ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(6.dp))
                                            .clickable {
                                                onDestinationChanged(destOption)
                                                destinationExpanded = false
                                            }
                                            .padding(horizontal = 8.dp, vertical = 7.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Place,
                                            contentDescription = null,
                                            tint = RydBrandGreen,
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = destOption,
                                            fontSize = 12.5.sp,
                                            fontWeight = if (destOption == destination) FontWeight.Bold else FontWeight.Medium,
                                            color = if (destOption == destination) RydBrandGreen else Color(0xFF0F172A)
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        // Quick 1-tap destination chips when field is empty
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(universityDestinations.take(4)) { quickStop ->
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = Color(0xFFF1F5F9),
                                    border = BorderStroke(0.8.dp, Color(0xFFE2E8F0)),
                                    modifier = Modifier.clickable {
                                        onDestinationChanged(quickStop)
                                    }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Place,
                                            contentDescription = null,
                                            tint = RydBrandGreen,
                                            modifier = Modifier.size(11.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = quickStop.replace("University ", ""),
                                            fontSize = 11.sp,
                                            color = Color(0xFF334155),
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Primary Button: "Find a Ride"
                    Button(
                        onClick = { onFindRides() },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = RydGreenPrimary,
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("find_ride_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Find a Ride",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Section Header: Available Rides (Queue + GPS Dispatched)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "ELIGIBLE QUEUED DRIVERS (FIFO + GPS)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF475569),
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "Longest-waiting driver in service zone within GPS ETA threshold",
                        fontSize = 10.sp,
                        color = Color(0xFF64748B)
                    )
                }
                Text(
                    text = "${trips.size} in queue",
                    fontSize = 11.5.sp,
                    color = RydBrandGreen,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
        }

        if (trips.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
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
                            tint = Color(0xFFCBD5E1),
                            modifier = Modifier.size(44.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No Shuttles Currently Matching",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Try clearing search destination or check again shortly as drivers go online.",
                            fontSize = 12.sp,
                            color = Color(0xFF64748B),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(trips) { trip ->
                AvailableRideCard(
                    trip = trip,
                    onBookSeat = { onBookSeatClicked(trip) }
                )
                Spacer(modifier = Modifier.height(10.dp))
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// Simple, Clean Available Ride Card
@Composable
fun AvailableRideCard(
    trip: TripEntity,
    onBookSeat: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header: Vehicle type, Registration identifier, Fare highlight
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    VehicleIconBadge(trip.vehicleCategory)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = trip.vehicleModel,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0xFFF1F5F9)
                            ) {
                                Text(
                                    text = trip.vehicleRegistration.ifBlank { trip.licensePlate },
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF475569),
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Trip #${trip.tripNumber}",
                                fontSize = 10.5.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                    }
                }

                // Fare Display: Prominent, RydRover Green
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${formatNaira(trip.calculateCurrentFarePerPassenger())} / seat",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        color = RydGreenPrimary
                    )
                    Text(
                        text = "Campus fixed fare",
                        fontSize = 9.5.sp,
                        color = Color(0xFF64748B)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Divider(color = Color(0xFFF1F5F9))
            Spacer(modifier = Modifier.height(10.dp))

            // Queue Priority & Service Zone Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (trip.queuePriorityRank == 1) RydGreenContainer else Color(0xFFF1F5F9)
                ) {
                    Text(
                        text = "Queue #${trip.queuePriorityRank} • Joined ${trip.entryTimeFormatted}",
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (trip.queuePriorityRank == 1) RydGreenPrimary else Color(0xFF334155),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
                Text(
                    text = "${trip.serviceZoneName} • ${trip.distanceToPassengerMeters}m (~${trip.estimatedArrivalMinutes} min ETA)",
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF0369A1)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Route & Driver info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "ROUTE",
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF64748B)
                    )
                    Text(
                        text = "${trip.origin} → ${trip.destination}",
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF0F172A),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "DEPARTURE",
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF64748B)
                    )
                    Text(
                        text = trip.departureTime,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Driver name & rating
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Driver: ${trip.driverName}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF334155)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = Color(0xFFEAB308),
                    modifier = Modifier.size(13.dp)
                )
                Text(
                    text = "${trip.driverRating}",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Available Seats & Occupancy indicator
            SeatAvailabilityIndicator(
                availableSeats = trip.availableSeats,
                totalCapacity = trip.totalCapacity
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Primary Button: "Book Seat"
            Button(
                onClick = onBookSeat,
                enabled = trip.availableSeats > 0,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = RydGreenPrimary,
                    contentColor = Color.White,
                    disabledContainerColor = Color(0xFFE2E8F0),
                    disabledContentColor = Color(0xFF94A3B8)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag("book_seat_btn_${trip.id}")
            ) {
                Icon(Icons.Default.EventSeat, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (trip.availableSeats > 0) "Book Seat" else "Vehicle Fully Booked",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// -------------------------------------------------------------
// CONFIRMATION & RYDWALLET PAYMENT SCREEN / DIALOG (REQ 4, 5, 6)
// -------------------------------------------------------------
@Composable
fun RideConfirmationDialog(
    trip: TripEntity,
    pickup: String,
    destination: String,
    walletBalanceNaira: Int = 0,
    onDismiss: () -> Unit,
    onAddMoneyClicked: () -> Unit = {},
    onConfirmRide: () -> Unit
) {
    val fareNaira = trip.calculateCurrentFarePerPassenger()
    val hasSufficientFunds = walletBalanceNaira >= fareNaira

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Book & Pay with RydWallet",
                fontWeight = FontWeight.Black,
                fontSize = 18.sp,
                color = Color(0xFF0F172A)
            )
        },
        text = {
            Column {
                Text(
                    text = "Review your ride and RydWallet payment authorization:",
                    fontSize = 12.sp,
                    color = Color(0xFF64748B)
                )
                Spacer(modifier = Modifier.height(10.dp))

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFF8FAFC),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        ConfirmDetailRow("Pickup", pickup)
                        ConfirmDetailRow("Destination", destination)
                        ConfirmDetailRow("Vehicle", "${trip.vehicleModel} (${trip.vehicleRegistration})")
                        ConfirmDetailRow("Driver", "${trip.driverName} (★ ${trip.driverRating})")
                        ConfirmDetailRow("Available Seats", "${trip.availableSeats} of ${trip.totalCapacity}")
                        ConfirmDetailRow("Estimated Departure", trip.departureTime)
                        Divider(modifier = Modifier.padding(vertical = 6.dp), color = Color(0xFFE2E8F0))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Ride Fare", fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                            Text(
                                formatNaira(fareNaira),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = RydGreenPrimary
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("RydWallet Balance", fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF475569))
                            Text(
                                formatNaira(walletBalanceNaira),
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (hasSufficientFunds) RydGreenPrimary else RydError
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (hasSufficientFunds) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFF0FDF4),
                        border = BorderStroke(1.dp, RydBrandGreen.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "✓ ${formatNaira(fareNaira)} will be reserved in RydWallet against your booking and only released to the driver when your trip completes.",
                            fontSize = 11.sp,
                            color = RydGreenPrimary,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFFEE2E2),
                        border = BorderStroke(1.dp, RydError.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "Insufficient RydWallet balance.",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Black,
                                color = RydError
                            )
                            Text(
                                text = "You need ${formatNaira(fareNaira - walletBalanceNaira)} more to book this ride.",
                                fontSize = 11.sp,
                                color = Color(0xFF7F1D1D)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (hasSufficientFunds) {
                Button(
                    onClick = onConfirmRide,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = RydGreenPrimary),
                    modifier = Modifier.testTag("confirm_ride_button")
                ) {
                    Text("Book & Pay ${formatNaira(fareNaira)}", fontWeight = FontWeight.Bold)
                }
            } else {
                Button(
                    onClick = onAddMoneyClicked,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = RydGreenPrimary),
                    modifier = Modifier.testTag("insufficient_wallet_add_money_button")
                ) {
                    Text("Add Money", fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color(0xFF64748B))
            }
        }
    )
}

@Composable
private fun ConfirmDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.5.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 11.5.sp, color = Color(0xFF64748B))
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF0F172A),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// -------------------------------------------------------------
// TAB 1: DEDICATED "MY RIDE" SCREEN
// -------------------------------------------------------------
@Composable
fun PassengerMyRideScreen(
    booking: BookingEntity?,
    trip: TripEntity?,
    onCallDriver: (String) -> Unit,
    onMessageDriver: (String) -> Unit,
    onCancelRide: (String) -> Unit,
    onFindRideClick: () -> Unit
) {
    if (booking == null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                shape = CircleShape,
                color = Color(0xFFF1F5F9),
                modifier = Modifier.size(72.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.DirectionsCar,
                        contentDescription = null,
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "No Active Ride Booked",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Choose your destination on the Find Ride tab to book a seat on a campus shuttle.",
                fontSize = 12.5.sp,
                color = Color(0xFF64748B),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onFindRideClick,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RydGreenPrimary)
            ) {
                Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Find a Ride", fontWeight = FontWeight.Bold)
            }
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(12.dp))

            // Dedicated MY RIDE Card
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Header: Trip number & Booking status
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "MY CAMPUS RIDE",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Black,
                                color = RydBrandGreen,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "Trip #${trip?.tripNumber ?: "RYD-TRIP"}",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF0F172A)
                            )
                        }

                        BoardingStatusChip(status = booking.boardingStatus)
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Divider(color = Color(0xFFF1F5F9))
                    Spacer(modifier = Modifier.height(12.dp))

                    // Vehicle & Driver Details
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("ASSIGNED VEHICLE", fontSize = 9.5.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Bold)
                            Text(
                                text = trip?.vehicleModel ?: "Campus Shuttle",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                            Text(
                                text = "Plate: ${trip?.vehicleRegistration ?: "SHUTTLE"}",
                                fontSize = 11.5.sp,
                                color = Color(0xFF475569)
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text("DRIVER", fontSize = 9.5.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Bold)
                            Text(
                                text = trip?.driverName ?: "Assigned Driver",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFEAB308), modifier = Modifier.size(12.dp))
                                Text(
                                    text = " ${trip?.driverRating ?: 4.95}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F172A)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Pickup point & Destination
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFF8FAFC),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(shape = CircleShape, color = Color(0xFF0284C7), modifier = Modifier.size(8.dp)) {}
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text("PICKUP POINT", fontSize = 9.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Bold)
                                    Text(booking.pickupLocation, fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF0F172A))
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(shape = CircleShape, color = RydBrandGreen, modifier = Modifier.size(8.dp)) {}
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text("DESTINATION", fontSize = 9.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Bold)
                                    Text(booking.destinationLocation, fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF0F172A))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Occupancy & Fare info
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("OCCUPANCY", fontSize = 9.5.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Bold)
                            Text(
                                text = "${trip?.bookedSeatsCount ?: 2} of ${trip?.totalCapacity ?: 6} seats occupied",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text("FARE & PAYMENT", fontSize = 9.5.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Bold)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = formatNaira(booking.totalFareNaira),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Black,
                                    color = RydGreenPrimary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                PaymentStatusChip(status = booking.paymentStatus)
                            }
                            Text(
                                text = "Ref: ${booking.paymentReference.ifBlank { "RYDWALLET" }}",
                                fontSize = 10.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // BOOKING VERIFICATION & QR CODE PASS (RR-48291 / 7394)
            BookingQrCodeCard(booking = booking)

            Spacer(modifier = Modifier.height(12.dp))

            // LIVE MAP VISUALIZER
            UniversityLiveMapView(
                trip = trip,
                passengerPickup = booking.pickupLocation
            )

            Spacer(modifier = Modifier.height(10.dp))

            // PASSENGER PRIVACY GUARANTEE NOTICE
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Privacy",
                        tint = Color(0xFF475569),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Passenger Privacy Protected: Other passengers' personal names, phones, and live GPS are hidden. Only occupancy and boarding status are shown.",
                        fontSize = 11.sp,
                        color = Color(0xFF475569),
                        lineHeight = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ACTION BUTTONS: Call Driver, Message Driver, Cancel Ride
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { onCallDriver(trip?.driverPhone ?: "+234 802 334 1122") },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF0F172A)),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("call_driver_btn")
                ) {
                    Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(15.dp), tint = RydBrandGreen)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Call", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = { onMessageDriver(trip?.driverName ?: "Driver") },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF0F172A)),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("message_driver_btn")
                ) {
                    Icon(Icons.Default.Message, contentDescription = null, modifier = Modifier.size(15.dp), tint = RydBrandGreen)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Message", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { onCancelRide(booking.id) },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFEE2E2), contentColor = RydError),
                    modifier = Modifier
                        .weight(1.1f)
                        .height(44.dp)
                        .testTag("cancel_ride_btn")
                ) {
                    Icon(Icons.Default.Cancel, contentDescription = null, modifier = Modifier.size(15.dp), tint = RydError)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Cancel", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// -------------------------------------------------------------
// TAB 3: MY BOOKINGS, PAYMENT STATUS, REFUNDS & RATINGS SCREEN
// -------------------------------------------------------------
@Composable
fun PassengerBookingsHistoryScreen(
    bookings: List<BookingEntity>,
    trips: List<TripEntity>,
    onSelectBookingForTracking: (BookingEntity) -> Unit,
    onCancelBooking: (String) -> Unit,
    onRateRide: (bookingId: String, ratingStars: Int, comment: String) -> Unit = { _, _, _ -> }
) {
    var bookingToRate by remember { mutableStateOf<BookingEntity?>(null) }
    var ratingStars by remember { mutableIntStateOf(5) }
    var ratingComment by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "MY CAMPUS RIDE PASSES, PAYMENTS & REFUNDS",
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF475569),
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (bookings.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
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
                        Text("No ride history yet.", color = Color(0xFF64748B), fontSize = 13.sp)
                    }
                }
            }
        } else {
            items(bookings) { booking ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectBookingForTracking(booking) }
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Booking ID: ${booking.bookingReference} (Code: ${booking.verificationCode})",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                            BoardingStatusChip(status = booking.boardingStatus)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "${booking.pickupLocation} → ${booking.destinationLocation}",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF334155)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Fare: ${formatNaira(booking.totalFareNaira)}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = RydGreenPrimary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                PaymentStatusChip(status = booking.paymentStatus)
                            }
                            if (booking.refundStatus != "NONE") {
                                Text(
                                    text = "Refund: ${booking.refundStatus}",
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0369A1)
                                )
                            } else {
                                Text(
                                    text = "Tap to track live",
                                    fontSize = 11.sp,
                                    color = Color(0xFF0284C7),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (booking.passengerRating != null) {
                                Text(
                                    text = "Your Rating: ${"★".repeat(booking.passengerRating)} (${booking.passengerRating}/5)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFD97706)
                                )
                            } else if (booking.status == BookingStatus.COMPLETED || booking.boardingStatus == "Boarded") {
                                OutlinedButton(
                                    onClick = {
                                        ratingStars = 5
                                        ratingComment = ""
                                        bookingToRate = booking
                                    },
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFEAB308), modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Rate Ride", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            } else {
                                Spacer(modifier = Modifier.width(4.dp))
                            }

                            if (booking.status == BookingStatus.CONFIRMED || booking.status == BookingStatus.PENDING) {
                                TextButton(onClick = { onCancelBooking(booking.id) }) {
                                    Text("Cancel & Refund", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = RydError)
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    bookingToRate?.let { b ->
        AlertDialog(
            onDismissRequest = { bookingToRate = null },
            title = { Text("Rate Campus Ride (${b.bookingReference})", fontWeight = FontWeight.Black) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Select star rating for your shuttle trip:", fontSize = 12.sp, color = Color(0xFF64748B))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        (1..5).forEach { star ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (star <= ratingStars) RydGoldContainer else Color(0xFFF1F5F9),
                                modifier = Modifier.clickable { ratingStars = star }
                            ) {
                                Text(
                                    text = "★ $star",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (star <= ratingStars) RydOnGoldContainer else Color(0xFF475569),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                    OutlinedTextField(
                        value = ratingComment,
                        onValueChange = { ratingComment = it },
                        label = { Text("Feedback / Comment (Optional)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onRateRide(b.id, ratingStars, ratingComment)
                        bookingToRate = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RydGreenPrimary)
                ) {
                    Text("Submit Rating", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { bookingToRate = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
