package com.example.ui.admin

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Report
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BookingEntity
import com.example.data.model.CampusEntity
import com.example.data.model.DriverProfileEntity
import com.example.data.model.DriverQueueEntryEntity
import com.example.data.model.DriverVerificationStatus
import com.example.data.model.InstitutionEntity
import com.example.data.model.PickupLocationEntity
import com.example.data.model.PlatformSettingsEntity
import com.example.data.model.ReportEntity
import com.example.data.model.RouteEntity
import com.example.data.model.ServiceZoneEntity
import com.example.data.model.TransportParkEntity
import com.example.data.model.TripEntity
import com.example.data.model.UniversityEntity
import com.example.data.model.UserEntity
import com.example.data.model.UserRole
import com.example.data.model.VehicleEntity
import com.example.ui.AdminAnalytics
import com.example.ui.components.BoardingStatusChip
import com.example.ui.components.DriverQueueStatusChip
import com.example.ui.components.DriverVerificationBadge
import com.example.ui.components.PaymentStatusChip
import com.example.ui.components.TripStatusChip
import com.example.ui.components.formatNaira
import com.example.ui.theme.RydBrandGreen
import com.example.ui.theme.RydBrandRed
import com.example.ui.theme.RydError
import com.example.ui.theme.RydGreenContainer
import com.example.ui.theme.RydGreenPrimary
import com.example.ui.theme.RydOnGreenContainer

@Composable
fun AdminDashboard(
    currentInstitution: InstitutionEntity? = null,
    currentCampus: CampusEntity? = null,
    analytics: AdminAnalytics,
    allUsers: List<UserEntity>,
    driverProfiles: List<DriverProfileEntity>,
    pickupLocations: List<PickupLocationEntity>,
    allTrips: List<TripEntity>,
    allBookings: List<BookingEntity>,
    allVehicles: List<VehicleEntity>,
    allParks: List<TransportParkEntity>,
    allServiceZones: List<ServiceZoneEntity>,
    allQueueEntries: List<DriverQueueEntryEntity>,
    allCampuses: List<CampusEntity>,
    allUniversities: List<UniversityEntity>,
    allRoutes: List<RouteEntity>,
    reports: List<ReportEntity>,
    settings: PlatformSettingsEntity,
    onVerifyDriver: (driverId: String, approve: Boolean) -> Unit,
    onSuspendDriver: (driverId: String) -> Unit,
    onToggleTemporarySkipDriver: (driverId: String, skipReason: String) -> Unit,
    onAddServiceZone: (uniId: String, campusId: String, name: String, lat: Double, lng: Double, radius: Int, maxEta: Int, routes: String) -> Unit,
    onDeleteServiceZone: (zoneId: String) -> Unit,
    onRegisterVehicle: (driverId: String, uniId: String, campusId: String, type: String, reg: String, model: String, plate: String, cap: Int) -> Unit,
    onAddUniversity: (name: String, shortCode: String, city: String) -> Unit,
    onAddCampus: (uniId: String, name: String, desc: String) -> Unit,
    onAddPark: (uniId: String, campusId: String, name: String, manager: String) -> Unit,
    onAddRoute: (uniId: String, campusId: String, origin: String, dest: String, corr: String, fare: Int, stops: String, mins: Int) -> Unit,
    onAddPickupLocation: (uniId: String, campusId: String, name: String, corr: String, land: String) -> Unit,
    onDeletePickupLocation: (id: String) -> Unit,
    onResolveReport: (String) -> Unit,
    onUpdateComplaintStatus: (reportId: String, status: String, adminNotes: String) -> Unit = { id, status, _ ->
        if (status == "RESOLVED") onResolveReport(id)
    },
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    val tabs = listOf(
        "Overview & Queue",
        "Service Zones (${allServiceZones.size})",
        "Drivers, Students & Fleet",
        "Parks & Routes",
        "Active Trips (${allTrips.size})",
        "Bookings (${allBookings.size})",
        "Complaints & Reports (${reports.count { it.status != "RESOLVED" }})"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
    ) {
        // Tab Navigation
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.White,
            contentColor = RydBrandGreen,
            edgePadding = 12.dp
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = title,
                            fontSize = 12.sp,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium,
                            color = if (selectedTab == index) RydGreenPrimary else Color(0xFF64748B)
                        )
                    }
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp)
        ) {
            when (selectedTab) {
                0 -> AdminOverviewTab(
                    currentInstitution = currentInstitution,
                    currentCampus = currentCampus,
                    analytics = analytics,
                    settings = settings,
                    queueEntries = allQueueEntries,
                    onToggleTemporarySkipDriver = onToggleTemporarySkipDriver
                )
                1 -> AdminServiceZonesTab(
                    currentInstitution = currentInstitution,
                    currentCampus = currentCampus,
                    serviceZones = allServiceZones,
                    onAddServiceZone = onAddServiceZone,
                    onDeleteServiceZone = onDeleteServiceZone
                )
                2 -> AdminDriversAndVehiclesTab(
                    currentInstitution = currentInstitution,
                    currentCampus = currentCampus,
                    allUsers = allUsers,
                    profiles = driverProfiles,
                    vehicles = allVehicles,
                    onVerifyDriver = onVerifyDriver,
                    onSuspendDriver = onSuspendDriver,
                    onRegisterVehicle = onRegisterVehicle
                )
                3 -> AdminParksAndRoutesTab(
                    currentInstitution = currentInstitution,
                    currentCampus = currentCampus,
                    universities = allUniversities,
                    campuses = allCampuses,
                    parks = allParks,
                    routes = allRoutes,
                    pickupLocations = pickupLocations,
                    onAddUniversity = onAddUniversity,
                    onAddCampus = onAddCampus,
                    onAddPark = onAddPark,
                    onAddRoute = onAddRoute,
                    onAddPickup = onAddPickupLocation,
                    onDeletePickup = onDeletePickupLocation
                )
                4 -> AdminActiveTripsTab(allTrips = allTrips)
                5 -> AdminBookingsTab(allBookings = allBookings, allTrips = allTrips)
                6 -> AdminReportsTab(
                    reports = reports,
                    onResolveReport = onResolveReport,
                    onUpdateComplaintStatus = onUpdateComplaintStatus
                )
            }
        }
    }
}

// -------------------------------------------------------------
// TAB 0: OVERVIEW, STATS & LIVE ROUTE-AWARE DRIVER QUEUE
// -------------------------------------------------------------
@Composable
fun AdminOverviewTab(
    currentInstitution: InstitutionEntity? = null,
    currentCampus: CampusEntity? = null,
    analytics: AdminAnalytics,
    settings: PlatformSettingsEntity,
    queueEntries: List<DriverQueueEntryEntity>,
    onToggleTemporarySkipDriver: (driverId: String, skipReason: String) -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            Spacer(modifier = Modifier.height(14.dp))

            // Main Institution Operations Header Card
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "AUTHORIZED INSTITUTION OPERATIONS (${currentInstitution?.institutionType?.label?.uppercase() ?: "UNIVERSITY"})",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = RydBrandGreen,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = currentInstitution?.name ?: settings.activeUniversity,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF0F172A)
                            )
                            Text(
                                text = "${currentInstitution?.city ?: "Port Harcourt"}, ${currentInstitution?.state ?: "Rivers State"} • Active Campus: ${currentCampus?.name ?: "Main Campus"}",
                                fontSize = 11.sp,
                                color = Color(0xFF475569),
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Surface(shape = RoundedCornerShape(8.dp), color = RydGreenContainer) {
                            Text(
                                text = currentInstitution?.shortCode ?: "INST",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = RydBrandGreen,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Isolated institution operations console managing geofenced transport zones, FIFO route queues, approved vehicles, and passenger bookings.", fontSize = 11.5.sp, color = Color(0xFF64748B))
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            Text("TODAY'S METRICS & FLEET STATUS", fontSize = 11.5.sp, fontWeight = FontWeight.Black, color = Color(0xFF475569), letterSpacing = 0.5.sp)
            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AdminMetricTile("Queued Drivers", "${analytics.queuedDriversCount}", RydBrandGreen, Icons.Default.DirectionsCar, Modifier.weight(1f))
                AdminMetricTile("Service Zones", "${analytics.totalServiceZonesCount}", Color(0xFF0369A1), Icons.Default.Place, Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AdminMetricTile("Active Shuttles", "${analytics.activeTripsCount}", Color(0xFF0369A1), Icons.Default.DirectionsBus, Modifier.weight(1f))
                AdminMetricTile("Completed Trips", "${analytics.completedTripsCount}", RydBrandGreen, Icons.Default.CheckCircle, Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AdminMetricTile("Total Revenue (GMV)", formatNaira(analytics.totalGmvNaira), RydBrandGreen, Icons.Default.MonetizationOn, Modifier.weight(1f))
                AdminMetricTile("Open Complaints", "${analytics.openReportsCount}", if (analytics.openReportsCount > 0) RydError else Color(0xFF64748B), Icons.Default.Report, Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(18.dp))
            Text("LIVE ROUTE-AWARE DRIVER QUEUES (FIFO + GPS ETA)", fontSize = 11.5.sp, fontWeight = FontWeight.Black, color = Color(0xFF475569), letterSpacing = 0.5.sp)
            Spacer(modifier = Modifier.height(8.dp))
        }

        val groupedByRoute = queueEntries.groupBy { "${it.serviceZoneName} • ${it.routeDirection}" }
        groupedByRoute.forEach { (routeHeader, driversInRoute) ->
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = routeHeader,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF0369A1)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        val sorted = driversInRoute.sortedWith(
                            compareBy<DriverQueueEntryEntity> { it.temporarilySkippedReason.isNotBlank() }
                                .thenBy { it.queueEntryTimestamp }
                        )
                        sorted.forEachIndexed { idx, entry ->
                            val isSkipped = entry.temporarilySkippedReason.isNotBlank()
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSkipped) Color(0xFFFEF3C7) else Color(0xFFF8FAFC),
                                border = BorderStroke(1.dp, if (isSkipped) Color(0xFFF59E0B) else Color(0xFFE2E8F0)),
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
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "#${idx + 1} ${entry.driverName}",
                                                fontSize = 12.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF0F172A)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            DriverQueueStatusChip(status = entry.queueStatus)
                                        }
                                        Text(
                                            text = "Joined: ${entry.entryTimeFormatted} • Reg: ${entry.vehicleRegistration} (${entry.availableSeats}/${entry.totalCapacity} seats)",
                                            fontSize = 11.sp,
                                            color = Color(0xFF475569)
                                        )
                                        Text(
                                            text = "GPS: ${entry.currentLat}, ${entry.currentLng} • ETA: ~${entry.estimatedArrivalMinutes} mins (${entry.distanceToPassengerMeters}m)",
                                            fontSize = 10.5.sp,
                                            color = Color(0xFF64748B)
                                        )
                                        if (isSkipped) {
                                            Text(
                                                text = "Temporarily Skipped: ${entry.temporarilySkippedReason}",
                                                fontSize = 10.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFFB45309)
                                            )
                                        }
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            if (isSkipped) {
                                                onToggleTemporarySkipDriver(entry.driverId, "")
                                            } else {
                                                onToggleTemporarySkipDriver(entry.driverId, "Exceeds Zone ETA Threshold")
                                            }
                                        },
                                        shape = RoundedCornerShape(6.dp),
                                        modifier = Modifier.height(32.dp)
                                    ) {
                                        Text(
                                            text = if (isSkipped) "Restore" else "Skip (ETA)",
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
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
}

// -------------------------------------------------------------
// TAB 1: CONFIGURABLE GEOGRAPHIC SERVICE ZONES (GEOFENCES)
// -------------------------------------------------------------
@Composable
fun AdminServiceZonesTab(
    currentInstitution: InstitutionEntity? = null,
    currentCampus: CampusEntity? = null,
    serviceZones: List<ServiceZoneEntity>,
    onAddServiceZone: (uniId: String, campusId: String, name: String, lat: Double, lng: Double, radius: Int, maxEta: Int, routes: String) -> Unit,
    onDeleteServiceZone: (zoneId: String) -> Unit
) {
    var showAddZoneDialog by remember { mutableStateOf(false) }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            Spacer(modifier = Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("GEOGRAPHIC SERVICE ZONES (GEOFENCES)", fontSize = 12.sp, fontWeight = FontWeight.Black, color = Color(0xFF475569))
                    Text("Drivers entering a zone while ONLINE & AVAILABLE join its route-aware digital queue", fontSize = 10.5.sp, color = Color(0xFF64748B))
                }
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = RydGreenContainer,
                    modifier = Modifier
                        .clickable { showAddZoneDialog = true }
                        .testTag("add_service_zone_btn")
                ) {
                    Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = RydBrandGreen, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("New Zone", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = RydBrandGreen)
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        items(serviceZones) { zone ->
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Place, contentDescription = null, tint = RydBrandGreen, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(zone.name, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF0F172A))
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "GPS Center: ${zone.centerLat}, ${zone.centerLng} • Geofence Radius: ${zone.radiusMeters}m",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF0369A1)
                        )
                        Text(
                            text = "Max Dispatch ETA Threshold: ${zone.maxEtaMinutesThreshold} mins",
                            fontSize = 11.sp,
                            color = Color(0xFF475569)
                        )
                        Text(
                            text = "Supported Routes: ${zone.supportedRoutes}",
                            fontSize = 11.sp,
                            color = Color(0xFF64748B)
                        )
                    }

                    IconButton(onClick = { onDeleteServiceZone(zone.id) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Zone", tint = RydError, modifier = Modifier.size(18.dp))
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }

    if (showAddZoneDialog) {
        var name by remember { mutableStateOf("") }
        var latStr by remember { mutableStateOf((currentCampus?.centerLat ?: currentInstitution?.centerLat ?: 0.0).toString()) }
        var lngStr by remember { mutableStateOf((currentCampus?.centerLng ?: currentInstitution?.centerLng ?: 0.0).toString()) }
        var radiusStr by remember { mutableStateOf("450") }
        var maxEtaStr by remember { mutableStateOf("8") }
        var routes by remember { mutableStateOf(serviceZones.firstOrNull()?.supportedRoutes ?: "") }

        AlertDialog(
            onDismissRequest = { showAddZoneDialog = false },
            title = { Text("Create Geographic Service Zone", fontWeight = FontWeight.Bold, fontSize = 16.5.sp) },
            text = {
                Column {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Zone Name (e.g. Senate Building Zone)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedTextField(
                            value = latStr,
                            onValueChange = { latStr = it },
                            label = { Text("Center Lat") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = lngStr,
                            onValueChange = { lngStr = it },
                            label = { Text("Center Lng") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedTextField(
                            value = radiusStr,
                            onValueChange = { radiusStr = it },
                            label = { Text("Radius (m)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = maxEtaStr,
                            onValueChange = { maxEtaStr = it },
                            label = { Text("Max ETA (mins)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = routes,
                        onValueChange = { routes = it },
                        label = { Text("Supported Destinations / Routes") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (name.isNotBlank()) {
                            onAddServiceZone(
                                currentInstitution?.id ?: "",
                                currentCampus?.id ?: "",
                                name,
                                latStr.toDoubleOrNull() ?: (currentCampus?.centerLat ?: currentInstitution?.centerLat ?: 0.0),
                                lngStr.toDoubleOrNull() ?: (currentCampus?.centerLng ?: currentInstitution?.centerLng ?: 0.0),
                                radiusStr.toIntOrNull() ?: 450,
                                maxEtaStr.toIntOrNull() ?: 8,
                                routes
                            )
                            showAddZoneDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RydGreenPrimary)
                ) {
                    Text("Save Service Zone", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddZoneDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}


@Composable
fun AdminMetricTile(
    label: String,
    value: String,
    accentColor: Color,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(label, fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                Icon(imageVector = icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(16.dp))
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, fontSize = 17.sp, fontWeight = FontWeight.Black, color = accentColor)
        }
    }
}

// -------------------------------------------------------------
// TAB 1: DRIVERS & VEHICLES (Approve, Reject, Suspend, Register)
// -------------------------------------------------------------
@Composable
fun AdminDriversAndVehiclesTab(
    currentInstitution: InstitutionEntity? = null,
    currentCampus: CampusEntity? = null,
    allUsers: List<UserEntity>,
    profiles: List<DriverProfileEntity>,
    vehicles: List<VehicleEntity>,
    onVerifyDriver: (String, Boolean) -> Unit,
    onSuspendDriver: (String) -> Unit,
    onRegisterVehicle: (String, String, String, String, String, String, String, Int) -> Unit
) {
    var showRegisterVehicleDialog by remember { mutableStateOf(false) }

    val drivers = allUsers.filter { it.role == UserRole.DRIVER }
    val passengers = allUsers.filter { it.role == UserRole.PASSENGER }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            Spacer(modifier = Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("REGISTERED DRIVERS (${drivers.size})", fontSize = 12.sp, fontWeight = FontWeight.Black, color = Color(0xFF475569))
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = RydGreenContainer,
                    modifier = Modifier.clickable { showRegisterVehicleDialog = true }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = RydBrandGreen, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("Register Vehicle", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = RydBrandGreen)
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        items(drivers) { driver ->
            val profile = profiles.find { it.driverId == driver.id }
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(driver.fullName, fontSize = 14.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                            Text("${driver.phoneNumber} • ${driver.email}", fontSize = 11.sp, color = Color(0xFF64748B))
                        }
                        DriverVerificationBadge(status = driver.verificationStatus)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Vehicle: ${profile?.vehicleMakeModel ?: "Campus Shuttle"} (${profile?.registrationNumber ?: "SHUTTLE"})",
                        fontSize = 12.sp,
                        color = Color(0xFF334155),
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    // Action buttons: Approve / Reject / Suspend
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (driver.verificationStatus != DriverVerificationStatus.VERIFIED) {
                            Button(
                                onClick = { onVerifyDriver(driver.id, true) },
                                shape = RoundedCornerShape(6.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = RydGreenPrimary),
                                modifier = Modifier.weight(1f).height(36.dp)
                            ) {
                                Text("Approve", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        if (driver.verificationStatus == DriverVerificationStatus.VERIFIED) {
                            OutlinedButton(
                                onClick = { onSuspendDriver(driver.id) },
                                shape = RoundedCornerShape(6.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = RydError),
                                modifier = Modifier.weight(1f).height(36.dp)
                            ) {
                                Icon(Icons.Default.Block, contentDescription = null, modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Suspend", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            OutlinedButton(
                                onClick = { onVerifyDriver(driver.id, false) },
                                shape = RoundedCornerShape(6.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = RydError),
                                modifier = Modifier.weight(1f).height(36.dp)
                            ) {
                                Text("Reject", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text("REGISTERED CAMPUS VEHICLES (${vehicles.size})", fontSize = 12.sp, fontWeight = FontWeight.Black, color = Color(0xFF475569))
            Spacer(modifier = Modifier.height(8.dp))
        }

        items(vehicles) { vehicle ->
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(vehicle.registrationNumber, fontSize = 13.sp, fontWeight = FontWeight.Black, color = Color(0xFF0F172A))
                        Text("${vehicle.makeModel} • Capacity: ${vehicle.capacity}", fontSize = 11.5.sp, color = Color(0xFF475569))
                        Text("Plate: ${vehicle.licensePlate}", fontSize = 10.5.sp, color = Color(0xFF64748B))
                    }
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = if (vehicle.isAvailable) RydGreenContainer else Color(0xFFFEE2E2)
                    ) {
                        Text(
                            text = if (vehicle.isAvailable) "Active" else "Grounded",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (vehicle.isAvailable) RydBrandGreen else RydError,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }

    if (showRegisterVehicleDialog) {
        val shortCode = currentInstitution?.shortCode?.split("/")?.firstOrNull()?.trim() ?: "INST"
        var regNum by remember { mutableStateOf("$shortCode-SH-06") }
        var model by remember { mutableStateOf("Toyota Sienna Shuttle") }
        var plate by remember { mutableStateOf("RIV-904-$shortCode") }
        var capStr by remember { mutableStateOf("6") }

        AlertDialog(
            onDismissRequest = { showRegisterVehicleDialog = false },
            title = { Text("Register $shortCode Institution Vehicle", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    OutlinedTextField(value = regNum, onValueChange = { regNum = it }, label = { Text("Registration Identifier (e.g. $shortCode-SH-06)") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(value = model, onValueChange = { model = it }, label = { Text("Make & Model") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(value = plate, onValueChange = { plate = it }, label = { Text("License Plate") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(value = capStr, onValueChange = { capStr = it }, label = { Text("Passenger Capacity") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val cap = capStr.toIntOrNull() ?: 6
                        val instId = currentInstitution?.id ?: ""
                        val campId = currentCampus?.id ?: ""
                        val targetDriverId = drivers.firstOrNull()?.id ?: ""
                        onRegisterVehicle(targetDriverId, instId, campId, "Campus Shuttle", regNum, model, plate, cap)
                        showRegisterVehicleDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RydGreenPrimary)
                ) {
                    Text("Register")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRegisterVehicleDialog = false }) { Text("Cancel") }
            }
        )
    }
}

// -------------------------------------------------------------
// TAB 2: PARKS & ROUTES (Universities, Campuses, Parks, Routes, Stops)
// -------------------------------------------------------------
@Composable
fun AdminParksAndRoutesTab(
    currentInstitution: InstitutionEntity? = null,
    currentCampus: CampusEntity? = null,
    universities: List<UniversityEntity>,
    campuses: List<CampusEntity>,
    parks: List<TransportParkEntity>,
    routes: List<RouteEntity>,
    pickupLocations: List<PickupLocationEntity>,
    onAddUniversity: (String, String, String) -> Unit,
    onAddCampus: (String, String, String) -> Unit,
    onAddPark: (String, String, String, String) -> Unit,
    onAddRoute: (String, String, String, String, String, Int, String, Int) -> Unit,
    onAddPickup: (String, String, String, String, String) -> Unit,
    onDeletePickup: (String) -> Unit
) {
    var showAddPickupDialog by remember { mutableStateOf(false) }
    var showAddRouteDialog by remember { mutableStateOf(false) }
    var showAddCampusDialog by remember { mutableStateOf(false) }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            Spacer(modifier = Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("CAMPUS TRANSPORT PARKS (${parks.size})", fontSize = 12.sp, fontWeight = FontWeight.Black, color = Color(0xFF475569))
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        items(parks) { park ->
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(park.name, fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                    Text("Manager: ${park.parkManager} • Bays: ${park.totalBays}", fontSize = 11.5.sp, color = Color(0xFF64748B))
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
        }

        item {
            Spacer(modifier = Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("INSTITUTION CAMPUSES (${campuses.size})", fontSize = 12.sp, fontWeight = FontWeight.Black, color = Color(0xFF475569))
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = RydGreenContainer,
                    modifier = Modifier.clickable { showAddCampusDialog = true }
                ) {
                    Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = RydBrandGreen, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("Add Campus", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = RydBrandGreen)
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        items(campuses) { camp ->
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(camp.name, fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                    Text(camp.description, fontSize = 11.sp, color = Color(0xFF64748B))
                    Text("GPS Boundary: ${camp.centerLat}, ${camp.centerLng} (${camp.boundaryRadiusMeters}m radius)", fontSize = 10.sp, color = Color(0xFF0369A1))
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("INSTITUTION ROUTES & FARES (${routes.size})", fontSize = 12.sp, fontWeight = FontWeight.Black, color = Color(0xFF475569))
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = RydGreenContainer,
                    modifier = Modifier.clickable { showAddRouteDialog = true }
                ) {
                    Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = RydBrandGreen, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("Add Route", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = RydBrandGreen)
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        items(routes) { route ->
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(route.corridorName, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                        Text("${formatNaira(route.standardFareNaira)} / seat", fontSize = 12.sp, fontWeight = FontWeight.Black, color = RydGreenPrimary)
                    }
                    Text("${route.originName} → ${route.destinationName}", fontSize = 11.5.sp, color = Color(0xFF334155))
                    Text("Stops: ${route.pickupPoints}", fontSize = 10.5.sp, color = Color(0xFF64748B), maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("DESIGNATED PICKUP STOPS (${pickupLocations.size})", fontSize = 12.sp, fontWeight = FontWeight.Black, color = Color(0xFF475569))
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = RydGreenContainer,
                    modifier = Modifier.clickable { showAddPickupDialog = true }
                ) {
                    Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = RydBrandGreen, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("Add Stop", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = RydBrandGreen)
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        items(pickupLocations) { loc ->
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(loc.name, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                        Text(loc.corridor, fontSize = 11.sp, color = Color(0xFF475569))
                        Text("Landmark: ${loc.landmarkDescription}", fontSize = 10.sp, color = Color(0xFF64748B))
                    }
                    IconButton(onClick = { onDeletePickup(loc.id) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = RydError, modifier = Modifier.size(18.dp))
                    }
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }

    if (showAddPickupDialog) {
        var name by remember { mutableStateOf("") }
        var corridor by remember { mutableStateOf("Campus Intra-Loop") }
        var landmark by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddPickupDialog = false },
            title = { Text("Add Designated Pickup Stop", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Stop Name (e.g. Science Complex)") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(value = corridor, onValueChange = { corridor = it }, label = { Text("Corridor / Axis") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(value = landmark, onValueChange = { landmark = it }, label = { Text("Landmark & Shelter Description") }, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (name.isNotBlank()) {
                            val instId = currentInstitution?.id ?: ""
                            val campId = currentCampus?.id ?: ""
                            onAddPickup(instId, campId, name, corridor, landmark)
                            showAddPickupDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RydGreenPrimary)
                ) {
                    Text("Add Stop")
                }
            },
            dismissButton = { TextButton(onClick = { showAddPickupDialog = false }) { Text("Cancel") } }
        )
    }

    if (showAddRouteDialog) {
        var origin by remember { mutableStateOf("") }
        var dest by remember { mutableStateOf("") }
        var fareStr by remember { mutableStateOf("200") }
        var stops by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddRouteDialog = false },
            title = { Text("Create Institution Route", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    OutlinedTextField(value = origin, onValueChange = { origin = it }, label = { Text("Origin Stop / Zone") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(value = dest, onValueChange = { dest = it }, label = { Text("Destination Stop / Zone") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(value = fareStr, onValueChange = { fareStr = it }, label = { Text("Standard Fare (₦)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(value = stops, onValueChange = { stops = it }, label = { Text("Pickup Points (comma-separated)") }, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (origin.isNotBlank() && dest.isNotBlank()) {
                            val instId = currentInstitution?.id ?: ""
                            val campId = currentCampus?.id ?: ""
                            val corridor = "${origin.trim()} → ${dest.trim()} Corridor"
                            onAddRoute(
                                instId,
                                campId,
                                origin.trim(),
                                dest.trim(),
                                corridor,
                                fareStr.toIntOrNull() ?: 200,
                                stops.ifBlank { "${origin.trim()}, ${dest.trim()}" },
                                8
                            )
                            showAddRouteDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RydGreenPrimary)
                ) {
                    Text("Save Route")
                }
            },
            dismissButton = { TextButton(onClick = { showAddRouteDialog = false }) { Text("Cancel") } }
        )
    }

    if (showAddCampusDialog) {
        var campName by remember { mutableStateOf("") }
        var campDesc by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddCampusDialog = false },
            title = { Text("Add Institution Campus", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    OutlinedTextField(value = campName, onValueChange = { campName = it }, label = { Text("Campus Name (e.g. Choba Campus)") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(value = campDesc, onValueChange = { campDesc = it }, label = { Text("Faculties & Operating Area Description") }, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (campName.isNotBlank()) {
                            val instId = currentInstitution?.id ?: ""
                            onAddCampus(instId, campName.trim(), campDesc.trim().ifBlank { "Authorized Campus Operating Area" })
                            showAddCampusDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RydGreenPrimary)
                ) {
                    Text("Save Campus")
                }
            },
            dismissButton = { TextButton(onClick = { showAddCampusDialog = false }) { Text("Cancel") } }
        )
    }
}

// -------------------------------------------------------------
// TAB 3: ACTIVE TRIPS MONITORING
// -------------------------------------------------------------
@Composable
fun AdminActiveTripsTab(allTrips: List<TripEntity>) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            Spacer(modifier = Modifier.height(14.dp))
            Text("ACTIVE CAMPUS SHUTTLES FLEET", fontSize = 12.sp, fontWeight = FontWeight.Black, color = Color(0xFF475569))
            Spacer(modifier = Modifier.height(8.dp))
        }

        items(allTrips) { trip ->
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Trip #${trip.tripNumber}", fontSize = 13.5.sp, fontWeight = FontWeight.Black, color = Color(0xFF0F172A))
                        TripStatusChip(status = trip.status)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("${trip.origin} → ${trip.destination}", fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF334155))
                    Text("Driver: ${trip.driverName} (${trip.driverPhone})", fontSize = 11.5.sp, color = Color(0xFF64748B))
                    Text("Vehicle: ${trip.vehicleModel} • Reg: ${trip.vehicleRegistration}", fontSize = 11.sp, color = Color(0xFF475569))
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Occupancy: ${trip.bookedSeatsCount}/${trip.totalCapacity} seats", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                        if (trip.liveTrackingActive) {
                            Text("● GPS Broadcasting Live", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = RydBrandGreen)
                        } else {
                            Text("Standard Tracking", fontSize = 11.sp, color = Color(0xFF64748B))
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

// -------------------------------------------------------------
// TAB 4: PASSENGER BOOKINGS
// -------------------------------------------------------------
@Composable
fun AdminBookingsTab(allBookings: List<BookingEntity>, allTrips: List<TripEntity>) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            Spacer(modifier = Modifier.height(14.dp))
            Text("PASSENGER BOOKING MANIFESTS", fontSize = 12.sp, fontWeight = FontWeight.Black, color = Color(0xFF475569))
            Spacer(modifier = Modifier.height(8.dp))
        }

        items(allBookings) { booking ->
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Booking: ${booking.bookingReference}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                        BoardingStatusChip(status = booking.boardingStatus)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Passenger: ${booking.passengerName} (${booking.passengerPhone})", fontSize = 12.sp, color = Color(0xFF334155))
                    Text("Route: ${booking.pickupLocation} → ${booking.destinationLocation}", fontSize = 11.5.sp, color = Color(0xFF64748B))
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Fare: ${formatNaira(booking.totalFareNaira)} (${booking.seatsCount} seat)", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = RydGreenPrimary)
                            Spacer(modifier = Modifier.width(6.dp))
                            PaymentStatusChip(status = booking.paymentStatus)
                        }
                        if (booking.refundStatus != "NONE") {
                            Text("Refund: ${booking.refundStatus}", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0369A1))
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

// -------------------------------------------------------------
// TAB 6: COMPLAINTS, TECHNICAL REPORTS & INVESTIGATION (REQ 16, 17, 18)
// -------------------------------------------------------------
@Composable
fun AdminReportsTab(
    reports: List<ReportEntity>,
    onResolveReport: (String) -> Unit,
    onUpdateComplaintStatus: (reportId: String, status: String, adminNotes: String) -> Unit = { id, status, _ ->
        if (status == "RESOLVED") onResolveReport(id)
    }
) {
    var reportToInvestigate by remember { mutableStateOf<ReportEntity?>(null) }
    var selectedStatus by remember { mutableStateOf("UNDER_REVIEW") }
    var investigationNotes by remember { mutableStateOf("") }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            Spacer(modifier = Modifier.height(14.dp))
            Text("INCIDENT, COMPLAINT & TECHNICAL REPORTS (${reports.size})", fontSize = 12.sp, fontWeight = FontWeight.Black, color = Color(0xFF475569))
            Text("Investigate passenger and driver complaints, add investigation notes, or escalate to Super Admin", fontSize = 10.5.sp, color = Color(0xFF64748B))
            Spacer(modifier = Modifier.height(8.dp))
        }

        items(reports) { report ->
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = if (report.issueType == "TECHNICAL") Color(0xFFE0F2FE) else Color(0xFFFEF3C7)
                                ) {
                                    Text(
                                        text = "${report.issueType} • ${report.category}",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black,
                                        color = if (report.issueType == "TECHNICAL") Color(0xFF0369A1) else Color(0xFFB45309),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(report.subject, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                        }
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = when (report.status) {
                                "RESOLVED" -> RydGreenContainer
                                "ESCALATED" -> Color(0xFFFEE2E2)
                                "UNDER_REVIEW" -> Color(0xFFE0F2FE)
                                else -> Color(0xFFFEF3C7)
                            }
                        ) {
                            Text(
                                text = report.status.replace("_", " "),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = when (report.status) {
                                    "RESOLVED" -> RydBrandGreen
                                    "ESCALATED" -> RydError
                                    "UNDER_REVIEW" -> Color(0xFF0369A1)
                                    else -> Color(0xFFB45309)
                                },
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(report.description, fontSize = 11.5.sp, color = Color(0xFF334155))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Reporter: ${report.reporterName} (${report.reporterRole.displayName}) • Ref: ${report.routeOrTripRef}",
                        fontSize = 10.5.sp,
                        color = Color(0xFF64748B)
                    )
                    if (report.screenshotReference.isNotBlank()) {
                        Text(
                            text = "Attached Diagnostic Evidence: ${report.screenshotReference}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF0369A1)
                        )
                    }
                    if (report.adminNotes.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFF0FDF4),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Investigation Notes: ${report.adminNotes}",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = RydGreenPrimary,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = {
                                selectedStatus = report.status
                                investigationNotes = report.adminNotes
                                reportToInvestigate = report
                            },
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Text("Investigate / Update Status", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        if (report.status != "RESOLVED") {
                            Button(
                                onClick = {
                                    onUpdateComplaintStatus(
                                        report.id,
                                        "RESOLVED",
                                        report.adminNotes.ifBlank { "Reviewed and resolved by Institution Transport Administration." }
                                    )
                                },
                                shape = RoundedCornerShape(6.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = RydGreenPrimary),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Text("Mark Resolved", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }

    reportToInvestigate?.let { rep ->
        val statuses = listOf("OPEN", "UNDER_REVIEW", "RESOLVED", "ESCALATED")
        AlertDialog(
            onDismissRequest = { reportToInvestigate = null },
            title = { Text("Investigate Ticket (${rep.id})", fontWeight = FontWeight.Black, fontSize = 16.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(rep.subject, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                    Text("SELECT STATUS", fontSize = 10.sp, fontWeight = FontWeight.Black, color = Color(0xFF475569))
                    statuses.forEach { st ->
                        val isSel = selectedStatus == st
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSel) RydGreenContainer else Color(0xFFF8FAFC),
                            border = BorderStroke(1.dp, if (isSel) RydBrandGreen else Color(0xFFE2E8F0)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedStatus = st }
                        ) {
                            Text(
                                text = st.replace("_", " "),
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSel) RydGreenPrimary else Color(0xFF334155),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
                            )
                        }
                    }
                    OutlinedTextField(
                        value = investigationNotes,
                        onValueChange = { investigationNotes = it },
                        label = { Text("Admin Investigation Notes") },
                        placeholder = { Text("Record findings, action taken, or reason for escalation...") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onUpdateComplaintStatus(rep.id, selectedStatus, investigationNotes)
                        reportToInvestigate = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RydGreenPrimary)
                ) {
                    Text("Save Investigation", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { reportToInvestigate = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
