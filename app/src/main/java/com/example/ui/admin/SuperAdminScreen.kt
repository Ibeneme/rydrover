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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BookingEntity
import com.example.data.model.CampusEntity
import com.example.data.model.FareSharingMode
import com.example.data.model.InstitutionEntity
import com.example.data.model.InstitutionType
import com.example.data.model.PickupLocationEntity
import com.example.data.model.PlatformSettingsEntity
import com.example.data.model.ReportCategory
import com.example.data.model.ReportEntity
import com.example.data.model.ReportStatus
import com.example.data.model.RouteEntity
import com.example.data.model.ServiceZoneEntity
import com.example.data.model.StateEntity
import com.example.data.model.TripEntity
import com.example.data.model.UserEntity
import com.example.data.model.UserRole
import com.example.data.model.VehicleEntity
import com.example.ui.AdminAnalytics
import com.example.ui.components.formatNaira
import com.example.ui.theme.RydBrandGreen
import com.example.ui.theme.RydError
import com.example.ui.theme.RydGreenContainer
import com.example.ui.theme.RydGreenPrimary
import com.example.ui.theme.RydOnGreenContainer

@Composable
fun SuperAdminDashboard(
    platformAnalytics: AdminAnalytics,
    allStates: List<StateEntity>,
    allInstitutions: List<InstitutionEntity>,
    allCampuses: List<CampusEntity>,
    allServiceZones: List<ServiceZoneEntity>,
    allPickupLocations: List<PickupLocationEntity>,
    allRoutes: List<RouteEntity>,
    allUsers: List<UserEntity>,
    allVehicles: List<VehicleEntity>,
    allTrips: List<TripEntity>,
    allBookings: List<BookingEntity>,
    allReports: List<ReportEntity> = emptyList(),
    platformSettings: PlatformSettingsEntity,
    onSaveInstitution: (
        id: String?,
        name: String,
        shortCode: String,
        type: InstitutionType,
        stateId: String,
        stateName: String,
        city: String,
        isActive: Boolean
    ) -> Unit,
    onToggleInstitutionActive: (institutionId: String, isActive: Boolean) -> Unit,
    onAssignInstitutionAdmin: (
        fullName: String,
        email: String,
        phone: String,
        institutionId: String,
        campusId: String
    ) -> Unit,
    onAssignDriverInstitutions: (
        driverId: String,
        primaryInstitutionId: String,
        authorizedIdsCsv: String
    ) -> Unit,
    onAddState: (name: String, code: String) -> Unit,
    onUpdatePlatformSettings: (
        commissionPercent: Int,
        cutoffMinutes: Int,
        emergencyContact: String,
        fareMode: FareSharingMode
    ) -> Unit,
    onUpdateReportStatus: (reportId: String, status: ReportStatus, notes: String) -> Unit = { _, _, _ -> },
    onSwitchToInstitutionAdmin: (adminUserId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val openOrEscalatedCount = allReports.count {
        it.reportStatus == ReportStatus.ESCALATED ||
            it.reportCategory == ReportCategory.TECHNICAL_PROBLEM ||
            !it.isResolved
    }

    val tabs = listOf(
        "Platform Overview",
        "Institutions (${allInstitutions.size})",
        "States (${allStates.size})",
        "Admins & Drivers",
        "Support & Tech Reports ($openOrEscalatedCount)",
        "Platform Settings"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
    ) {
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
                0 -> SuperAdminOverviewTab(
                    platformAnalytics = platformAnalytics,
                    allStates = allStates,
                    allInstitutions = allInstitutions,
                    allCampuses = allCampuses,
                    allServiceZones = allServiceZones,
                    allPickupLocations = allPickupLocations,
                    allRoutes = allRoutes,
                    allUsers = allUsers,
                    allVehicles = allVehicles,
                    allTrips = allTrips,
                    allBookings = allBookings
                )
                1 -> SuperAdminInstitutionsTab(
                    allStates = allStates,
                    allInstitutions = allInstitutions,
                    allCampuses = allCampuses,
                    allServiceZones = allServiceZones,
                    allRoutes = allRoutes,
                    allUsers = allUsers,
                    allTrips = allTrips,
                    onSaveInstitution = onSaveInstitution,
                    onToggleInstitutionActive = onToggleInstitutionActive
                )
                2 -> SuperAdminStatesTab(
                    allStates = allStates,
                    allInstitutions = allInstitutions,
                    onAddState = onAddState
                )
                3 -> SuperAdminAdminsAndDriversTab(
                    allInstitutions = allInstitutions,
                    allCampuses = allCampuses,
                    allUsers = allUsers,
                    onAssignInstitutionAdmin = onAssignInstitutionAdmin,
                    onAssignDriverInstitutions = onAssignDriverInstitutions,
                    onSwitchToInstitutionAdmin = onSwitchToInstitutionAdmin
                )
                4 -> SuperAdminReportsAndTechnicalTab(
                    allReports = allReports,
                    allInstitutions = allInstitutions,
                    onUpdateReportStatus = onUpdateReportStatus
                )
                5 -> SuperAdminPlatformSettingsTab(
                    settings = platformSettings,
                    onUpdateSettings = onUpdatePlatformSettings
                )
            }
        }
    }
}

@Composable
private fun SuperAdminOverviewTab(
    platformAnalytics: AdminAnalytics,
    allStates: List<StateEntity>,
    allInstitutions: List<InstitutionEntity>,
    allCampuses: List<CampusEntity>,
    allServiceZones: List<ServiceZoneEntity>,
    allPickupLocations: List<PickupLocationEntity>,
    allRoutes: List<RouteEntity>,
    allUsers: List<UserEntity>,
    allVehicles: List<VehicleEntity>,
    allTrips: List<TripEntity>,
    allBookings: List<BookingEntity>
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            Spacer(modifier = Modifier.height(14.dp))

            // Multi-Institution Architecture Header Card
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
                                text = "RYDROVER MULTI-INSTITUTION PLATFORM",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Black,
                                color = RydBrandGreen,
                                letterSpacing = 0.6.sp
                            )
                            Text(
                                text = "Nationwide Higher Institution Transit Network",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF0F172A)
                            )
                        }
                        Surface(shape = RoundedCornerShape(8.dp), color = RydGreenContainer) {
                            Text(
                                text = "Super Admin",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = RydOnGreenContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Hierarchy: RydRover → State (${allStates.size}) → Institution (${allInstitutions.size}) → Campus (${allCampuses.size}) → Transport Zones (${allServiceZones.size}) → Pickup Points (${allPickupLocations.size}) → Routes (${allRoutes.size}) → Drivers & Fleet.",
                        fontSize = 11.5.sp,
                        color = Color(0xFF475569)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "OVERALL PLATFORM ACTIVITY",
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF475569)
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AdminMetricTile(
                    label = "Active Institutions",
                    value = "${allInstitutions.count { it.isActive }}/${allInstitutions.size}",
                    accentColor = RydBrandGreen,
                    icon = Icons.Default.AccountBalance,
                    modifier = Modifier.weight(1f)
                )
                AdminMetricTile(
                    label = "States Covered",
                    value = "${allStates.size}",
                    accentColor = Color(0xFF0369A1),
                    icon = Icons.Default.Place,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AdminMetricTile(
                    label = "Total Campuses",
                    value = "${allCampuses.size}",
                    accentColor = Color(0xFF0369A1),
                    icon = Icons.Default.Place,
                    modifier = Modifier.weight(1f)
                )
                AdminMetricTile(
                    label = "Transport Zones",
                    value = "${allServiceZones.size}",
                    accentColor = RydBrandGreen,
                    icon = Icons.Default.Verified,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AdminMetricTile(
                    label = "Active Trips",
                    value = "${platformAnalytics.activeTripsCount}",
                    accentColor = Color(0xFF0369A1),
                    icon = Icons.Default.DirectionsBus,
                    modifier = Modifier.weight(1f)
                )
                AdminMetricTile(
                    label = "Platform GMV",
                    value = formatNaira(platformAnalytics.totalGmvNaira),
                    accentColor = RydBrandGreen,
                    icon = Icons.Default.MonetizationOn,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))
            Text(
                text = "INSTITUTION-LEVEL STATISTICS & ISOLATION MATRIX",
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF475569)
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        items(allInstitutions) { inst ->
            val instCampuses = allCampuses.count { it.institutionId == inst.id }
            val instZones = allServiceZones.count { it.institutionId == inst.id }
            val instPickups = allPickupLocations.count { it.institutionId == inst.id }
            val instRoutes = allRoutes.count { it.institutionId == inst.id }
            val instDrivers = allUsers.count {
                it.role == UserRole.DRIVER &&
                    (it.institutionId == inst.id || it.authorizedInstitutionIds.contains(inst.id))
            }
            val instPassengers = allUsers.count { it.role == UserRole.PASSENGER && it.institutionId == inst.id }
            val instVehicles = allVehicles.count { it.institutionId == inst.id }
            val instTrips = allTrips.count { it.institutionId == inst.id }
            val instBookings = allBookings.count { it.institutionId == inst.id }

            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, if (inst.isActive) Color(0xFFE2E8F0) else Color(0xFFFECACA)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = RydGreenContainer
                                ) {
                                    Text(
                                        text = inst.institutionType.label,
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = RydOnGreenContainer,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${inst.city}, ${inst.state}",
                                    fontSize = 10.5.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = "${inst.name} (${inst.shortCode})",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF0F172A)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (inst.isActive) RydGreenContainer else Color(0xFFFEE2E2)
                        ) {
                            Text(
                                text = if (inst.isActive) "Active" else "Deactivated",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (inst.isActive) RydBrandGreen else RydError,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Divider(color = Color(0xFFF1F5F9))
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Campuses: $instCampuses • Zones: $instZones • Pickup Points: $instPickups • Routes: $instRoutes",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF334155)
                    )
                    Text(
                        text = "Drivers: $instDrivers • Vehicles: $instVehicles • Passengers: $instPassengers • Active Trips: $instTrips • Bookings: $instBookings",
                        fontSize = 11.sp,
                        color = Color(0xFF64748B)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

@Composable
private fun SuperAdminInstitutionsTab(
    allStates: List<StateEntity>,
    allInstitutions: List<InstitutionEntity>,
    allCampuses: List<CampusEntity>,
    allServiceZones: List<ServiceZoneEntity>,
    allRoutes: List<RouteEntity>,
    allUsers: List<UserEntity>,
    allTrips: List<TripEntity>,
    onSaveInstitution: (
        id: String?,
        name: String,
        shortCode: String,
        type: InstitutionType,
        stateId: String,
        stateName: String,
        city: String,
        isActive: Boolean
    ) -> Unit,
    onToggleInstitutionActive: (institutionId: String, isActive: Boolean) -> Unit
) {
    var editingInstitution by remember { mutableStateOf<InstitutionEntity?>(null) }
    var isAddDialogOpen by remember { mutableStateOf(false) }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            Spacer(modifier = Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "MANAGE INSTITUTIONS (${allInstitutions.size})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF475569)
                    )
                    Text(
                        text = "Add, edit, or deactivate Universities, Polytechnics, Colleges & Technical Colleges",
                        fontSize = 10.5.sp,
                        color = Color(0xFF64748B)
                    )
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = RydGreenContainer,
                    modifier = Modifier
                        .clickable {
                            editingInstitution = null
                            isAddDialogOpen = true
                        }
                        .testTag("super_admin_add_institution_btn")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = RydBrandGreen, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Institution", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = RydBrandGreen)
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        items(allInstitutions) { inst ->
            val campusNames = allCampuses.filter { it.institutionId == inst.id }.joinToString(", ") { it.name }
            val admins = allUsers.filter { it.role == UserRole.ADMIN && it.institutionId == inst.id }

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
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${inst.name} (${inst.shortCode})",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF0F172A)
                            )
                            Text(
                                text = "Type: ${inst.institutionType.label} • ${inst.city}, ${inst.state}",
                                fontSize = 11.5.sp,
                                color = Color(0xFF0369A1),
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Switch(
                            checked = inst.isActive,
                            onCheckedChange = { active -> onToggleInstitutionActive(inst.id, active) },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = RydBrandGreen)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Campuses: ${campusNames.ifBlank { "Main Campus" }}",
                        fontSize = 11.sp,
                        color = Color(0xFF475569)
                    )
                    Text(
                        text = "Institution Admins: ${if (admins.isEmpty()) "Unassigned" else admins.joinToString { it.fullName }}",
                        fontSize = 11.sp,
                        color = Color(0xFF64748B)
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = {
                                editingInstitution = inst
                                isAddDialogOpen = true
                            },
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Edit Institution", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }

    if (isAddDialogOpen) {
        val initial = editingInstitution
        var name by remember { mutableStateOf(initial?.name ?: "") }
        var shortCode by remember { mutableStateOf(initial?.shortCode ?: "") }
        var selectedType by remember { mutableStateOf(initial?.institutionType ?: InstitutionType.UNIVERSITY) }
        var selectedState by remember {
            mutableStateOf(allStates.find { it.id == initial?.stateId } ?: allStates.firstOrNull() ?: StateEntity("state_rivers", "Rivers State", "RV"))
        }
        var city by remember { mutableStateOf(initial?.city ?: "Port Harcourt") }

        AlertDialog(
            onDismissRequest = { isAddDialogOpen = false },
            title = {
                Text(
                    text = if (initial == null) "Add New Institution" else "Edit Institution",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.5.sp
                )
            },
            text = {
                Column {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Institution Name") },
                        placeholder = { Text("e.g. University of Lagos") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedTextField(
                            value = shortCode,
                            onValueChange = { shortCode = it },
                            label = { Text("Short Code") },
                            placeholder = { Text("e.g. UNILAG") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = city,
                            onValueChange = { city = it },
                            label = { Text("City") },
                            placeholder = { Text("e.g. Akoka") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("INSTITUTION TYPE", fontSize = 10.sp, fontWeight = FontWeight.Black, color = Color(0xFF475569))
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(InstitutionType.entries.toList()) { type ->
                            val isSelected = type == selectedType
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isSelected) RydBrandGreen else Color(0xFFF1F5F9),
                                modifier = Modifier.clickable { selectedType = type }
                            ) {
                                Text(
                                    text = type.label,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else Color(0xFF334155),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("STATE", fontSize = 10.sp, fontWeight = FontWeight.Black, color = Color(0xFF475569))
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(allStates) { state ->
                            val isSelected = state.id == selectedState.id
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isSelected) RydBrandGreen else Color(0xFFF1F5F9),
                                modifier = Modifier.clickable { selectedState = state }
                            ) {
                                Text(
                                    text = state.name,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else Color(0xFF334155),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (name.isNotBlank() && shortCode.isNotBlank()) {
                            onSaveInstitution(
                                initial?.id,
                                name,
                                shortCode,
                                selectedType,
                                selectedState.id,
                                selectedState.name,
                                city,
                                initial?.isActive ?: true
                            )
                            isAddDialogOpen = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RydGreenPrimary)
                ) {
                    Text("Save Institution")
                }
            },
            dismissButton = {
                TextButton(onClick = { isAddDialogOpen = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun SuperAdminStatesTab(
    allStates: List<StateEntity>,
    allInstitutions: List<InstitutionEntity>,
    onAddState: (name: String, code: String) -> Unit
) {
    var showAddStateDialog by remember { mutableStateOf(false) }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            Spacer(modifier = Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("NATIONWIDE STATES (${allStates.size})", fontSize = 12.sp, fontWeight = FontWeight.Black, color = Color(0xFF475569))
                    Text("Scale RydRover from Rivers State across multiple states nationwide", fontSize = 10.5.sp, color = Color(0xFF64748B))
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = RydGreenContainer,
                    modifier = Modifier.clickable { showAddStateDialog = true }
                ) {
                    Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = RydBrandGreen, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add State", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = RydBrandGreen)
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        items(allStates) { state ->
            val stateInstitutions = allInstitutions.filter { it.stateId == state.id || it.state.equals(state.name, ignoreCase = true) }
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
                        Text("${state.name} (${state.code})", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF0F172A))
                        Surface(shape = RoundedCornerShape(6.dp), color = RydGreenContainer) {
                            Text(
                                text = "${stateInstitutions.size} Institutions",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = RydBrandGreen,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                    if (stateInstitutions.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stateInstitutions.joinToString(" • ") { "${it.shortCode} (${it.institutionType.label})" },
                            fontSize = 11.sp,
                            color = Color(0xFF475569)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }

    if (showAddStateDialog) {
        var stateName by remember { mutableStateOf("") }
        var stateCode by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddStateDialog = false },
            title = { Text("Add State to RydRover Network", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    OutlinedTextField(
                        value = stateName,
                        onValueChange = { stateName = it },
                        label = { Text("State Name (e.g. Bayelsa State)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = stateCode,
                        onValueChange = { stateCode = it },
                        label = { Text("State Code (e.g. BY)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (stateName.isNotBlank()) {
                            onAddState(stateName, stateCode.ifBlank { stateName.take(2).uppercase() })
                            showAddStateDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RydGreenPrimary)
                ) {
                    Text("Save State")
                }
            },
            dismissButton = { TextButton(onClick = { showAddStateDialog = false }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun SuperAdminAdminsAndDriversTab(
    allInstitutions: List<InstitutionEntity>,
    allCampuses: List<CampusEntity>,
    allUsers: List<UserEntity>,
    onAssignInstitutionAdmin: (String, String, String, String, String) -> Unit,
    onAssignDriverInstitutions: (String, String, String) -> Unit,
    onSwitchToInstitutionAdmin: (String) -> Unit
) {
    var showAssignAdminDialog by remember { mutableStateOf(false) }
    val institutionAdmins = allUsers.filter { it.role == UserRole.ADMIN }
    val drivers = allUsers.filter { it.role == UserRole.DRIVER }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            Spacer(modifier = Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("INSTITUTION ADMINISTRATORS (${institutionAdmins.size})", fontSize = 12.sp, fontWeight = FontWeight.Black, color = Color(0xFF475569))
                    Text("Each Institution Admin manages only their authorized institution", fontSize = 10.5.sp, color = Color(0xFF64748B))
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = RydGreenContainer,
                    modifier = Modifier.clickable { showAssignAdminDialog = true }
                ) {
                    Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = RydBrandGreen, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Assign Admin", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = RydBrandGreen)
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        items(institutionAdmins) { admin ->
            val inst = allInstitutions.find { it.id == admin.institutionId }
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
                        Text(admin.fullName, fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                        Text(
                            text = "Authorized Institution: ${inst?.name ?: "UNIPORT"} (${inst?.shortCode ?: "UNIPORT"})",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = RydBrandGreen
                        )
                        Text("${admin.email} • ${admin.phoneNumber}", fontSize = 10.5.sp, color = Color(0xFF64748B))
                    }
                    OutlinedButton(
                        onClick = { onSwitchToInstitutionAdmin(admin.id) },
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text("Open Console", fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text("DRIVER MULTI-INSTITUTION AUTHORIZATION (${drivers.size})", fontSize = 12.sp, fontWeight = FontWeight.Black, color = Color(0xFF475569))
            Text("Configure which institution networks each commercial driver is authorized to serve", fontSize = 10.5.sp, color = Color(0xFF64748B))
            Spacer(modifier = Modifier.height(8.dp))
        }

        items(drivers) { driver ->
            val currentAuthSet = driver.authorizedInstitutionIds.split(",").map { it.trim() }.filter { it.isNotBlank() }.toSet() + driver.institutionId
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(driver.fullName, fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                    Text("Tap institution badges to authorize or revoke operating access:", fontSize = 10.5.sp, color = Color(0xFF64748B))
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(allInstitutions) { inst ->
                            val isAuthorized = inst.id in currentAuthSet
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isAuthorized) RydBrandGreen else Color(0xFFF1F5F9),
                                border = BorderStroke(1.dp, if (isAuthorized) RydBrandGreen else Color(0xFFCBD5E1)),
                                modifier = Modifier.clickable {
                                    val updatedSet = if (isAuthorized && currentAuthSet.size > 1) {
                                        currentAuthSet - inst.id
                                    } else {
                                        currentAuthSet + inst.id
                                    }
                                    val primaryId = if (driver.institutionId in updatedSet) driver.institutionId else updatedSet.first()
                                    onAssignDriverInstitutions(driver.id, primaryId, updatedSet.joinToString(","))
                                }
                            ) {
                                Text(
                                    text = if (isAuthorized) "✓ ${inst.shortCode}" else "+ ${inst.shortCode}",
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isAuthorized) Color.White else Color(0xFF334155),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }

    if (showAssignAdminDialog) {
        var fullName by remember { mutableStateOf("") }
        var email by remember { mutableStateOf("") }
        var phone by remember { mutableStateOf("+234 803 ") }
        var selectedInst by remember { mutableStateOf(allInstitutions.firstOrNull()) }

        AlertDialog(
            onDismissRequest = { showAssignAdminDialog = false },
            title = { Text("Assign Institution Administrator", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    OutlinedTextField(value = fullName, onValueChange = { fullName = it }, label = { Text("Administrator Full Name") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Official Email") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone Number") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("SELECT AUTHORIZED INSTITUTION", fontSize = 10.sp, fontWeight = FontWeight.Black, color = Color(0xFF475569))
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(allInstitutions) { inst ->
                            val isSelected = selectedInst?.id == inst.id
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isSelected) RydBrandGreen else Color(0xFFF1F5F9),
                                modifier = Modifier.clickable { selectedInst = inst }
                            ) {
                                Text(
                                    text = inst.shortCode,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else Color(0xFF334155),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val inst = selectedInst
                        if (fullName.isNotBlank() && inst != null) {
                            val campId = allCampuses.firstOrNull { it.institutionId == inst.id }?.id ?: "camp_abuja"
                            onAssignInstitutionAdmin(
                                fullName,
                                email.ifBlank { "admin@${inst.shortCode.lowercase()}.edu.ng" },
                                phone,
                                inst.id,
                                campId
                            )
                            showAssignAdminDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RydGreenPrimary)
                ) {
                    Text("Assign Admin")
                }
            },
            dismissButton = { TextButton(onClick = { showAssignAdminDialog = false }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun SuperAdminPlatformSettingsTab(
    settings: PlatformSettingsEntity,
    onUpdateSettings: (Int, Int, String, FareSharingMode) -> Unit
) {
    var commissionStr by remember(settings) { mutableStateOf(settings.platformCommissionPercent.toString()) }
    var cutoffStr by remember(settings) { mutableStateOf(settings.cancellationCutoffMinutes.toString()) }
    var emergencyContact by remember(settings) { mutableStateOf(settings.emergencyContact) }
    var selectedFareMode by remember(settings) { mutableStateOf(settings.fareSharingMode) }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            Spacer(modifier = Modifier.height(14.dp))
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Settings, contentDescription = null, tint = RydBrandGreen)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Platform-Wide Configuration", fontSize = 15.sp, fontWeight = FontWeight.Black, color = Color(0xFF0F172A))
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = commissionStr,
                        onValueChange = { commissionStr = it },
                        label = { Text("Platform Service Fee / Commission (%)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = cutoffStr,
                        onValueChange = { cutoffStr = it },
                        label = { Text("Free Cancellation Cutoff (Minutes)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = emergencyContact,
                        onValueChange = { emergencyContact = it },
                        label = { Text("Platform Security & Control Hotline") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Text("DEFAULT FARE SHARING POLICY", fontSize = 10.5.sp, fontWeight = FontWeight.Black, color = Color(0xFF475569))
                    Spacer(modifier = Modifier.height(6.dp))

                    FareSharingMode.entries.forEach { mode ->
                        val isSelected = mode == selectedFareMode
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) RydGreenContainer.copy(alpha = 0.6f) else Color(0xFFF8FAFC),
                            border = BorderStroke(1.dp, if (isSelected) RydBrandGreen else Color(0xFFE2E8F0)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp)
                                .clickable { selectedFareMode = mode }
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(mode.label, fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                                Text(mode.description, fontSize = 10.5.sp, color = Color(0xFF64748B))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = {
                            onUpdateSettings(
                                commissionStr.toIntOrNull() ?: 5,
                                cutoffStr.toIntOrNull() ?: 15,
                                emergencyContact,
                                selectedFareMode
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = RydGreenPrimary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Save Platform Settings", fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SuperAdminReportsAndTechnicalTab(
    allReports: List<ReportEntity>,
    allInstitutions: List<InstitutionEntity>,
    onUpdateReportStatus: (reportId: String, status: ReportStatus, notes: String) -> Unit
) {
    var selectedFilter by remember { mutableStateOf("ALL") }
    var activeNoteDialogReport by remember { mutableStateOf<ReportEntity?>(null) }

    val filteredReports = remember(allReports, selectedFilter) {
        when (selectedFilter) {
            "ESCALATED" -> allReports.filter { it.reportStatus == ReportStatus.ESCALATED }
            "TECHNICAL" -> allReports.filter { it.reportCategory == ReportCategory.TECHNICAL_PROBLEM }
            "COMPLAINTS" -> allReports.filter { it.reportCategory == ReportCategory.COMPLAINT }
            "OPEN" -> allReports.filter { !it.isResolved }
            else -> allReports
        }
    }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "NATIONWIDE COMPLAINTS & TECHNICAL REPORTS (${allReports.size})",
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF475569)
            )
            Text(
                text = "Super Admin oversight for escalated institution complaints, payment disputes & technical error logs",
                fontSize = 10.5.sp,
                color = Color(0xFF64748B)
            )
            Spacer(modifier = Modifier.height(8.dp))

            val filterChips = listOf(
                "ALL" to "All (${allReports.size})",
                "ESCALATED" to "Escalated (${allReports.count { it.reportStatus == ReportStatus.ESCALATED }})",
                "TECHNICAL" to "Technical (${allReports.count { it.reportCategory == ReportCategory.TECHNICAL_PROBLEM }})",
                "COMPLAINTS" to "Complaints (${allReports.count { it.reportCategory == ReportCategory.COMPLAINT }})",
                "OPEN" to "Unresolved (${allReports.count { !it.isResolved }})"
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(filterChips) { (key, label) ->
                    val isSelected = selectedFilter == key
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) RydBrandGreen else Color.White,
                        border = BorderStroke(1.dp, if (isSelected) RydBrandGreen else Color(0xFFCBD5E1)),
                        modifier = Modifier.clickable { selectedFilter = key }
                    ) {
                        Text(
                            text = label,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.White else Color(0xFF334155),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        items(filteredReports) { report ->
            val inst = allInstitutions.find { it.id == report.institutionId }
            val isTech = report.reportCategory == ReportCategory.TECHNICAL_PROBLEM
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(
                    1.dp,
                    when (report.reportStatus) {
                        ReportStatus.ESCALATED -> Color(0xFFFCA5A5)
                        ReportStatus.RESOLVED -> Color(0xFFBBF7D0)
                        else -> Color(0xFFE2E8F0)
                    }
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = if (isTech) Color(0xFFE0F2FE) else Color(0xFFFEF3C7)
                            ) {
                                Text(
                                    text = if (isTech) "TECH REPORT" else "COMPLAINT",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (isTech) Color(0xFF0369A1) else Color(0xFFB45309),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0xFFF1F5F9)
                            ) {
                                Text(
                                    text = inst?.shortCode ?: report.institutionId,
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF334155),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = when (report.reportStatus) {
                                ReportStatus.RESOLVED -> RydGreenContainer
                                ReportStatus.ESCALATED -> Color(0xFFFEE2E2)
                                ReportStatus.UNDER_REVIEW -> Color(0xFFFEF3C7)
                                ReportStatus.OPEN -> Color(0xFFF1F5F9)
                            }
                        ) {
                            Text(
                                text = report.reportStatus.label.uppercase(),
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Black,
                                color = when (report.reportStatus) {
                                    ReportStatus.RESOLVED -> RydBrandGreen
                                    ReportStatus.ESCALATED -> RydError
                                    ReportStatus.UNDER_REVIEW -> Color(0xFFB45309)
                                    ReportStatus.OPEN -> Color(0xFF475569)
                                },
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(report.issueType, fontSize = 13.5.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF0F172A))
                    Text(report.details, fontSize = 11.5.sp, color = Color(0xFF334155))
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Reporter: ${report.reporterName} (${report.reporterRole.label}) • Screen: ${report.screenSection}",
                        fontSize = 10.5.sp,
                        color = Color(0xFF64748B)
                    )
                    if (!report.bookingId.isNullOrBlank() || !report.tripId.isNullOrBlank()) {
                        Text(
                            text = "Booking: ${report.bookingId ?: "N/A"} • Trip: ${report.tripId ?: "N/A"}",
                            fontSize = 10.5.sp,
                            color = Color(0xFF0369A1),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    if (!report.adminNotes.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFF8FAFC),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Resolution Notes: ${report.adminNotes}",
                                fontSize = 10.5.sp,
                                color = Color(0xFF0F172A),
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedButton(
                            onClick = { activeNoteDialogReport = report },
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text("Update / Add Note", fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                        }
                        if (report.reportStatus != ReportStatus.RESOLVED) {
                            Button(
                                onClick = {
                                    onUpdateReportStatus(
                                        report.id,
                                        ReportStatus.RESOLVED,
                                        report.adminNotes ?: "Resolved by RydRover Super Admin."
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = RydGreenPrimary),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Text("Resolve", fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }

    activeNoteDialogReport?.let { report ->
        var selectedStatus by remember(report) { mutableStateOf(report.reportStatus) }
        var notes by remember(report) { mutableStateOf(report.adminNotes ?: "") }

        AlertDialog(
            onDismissRequest = { activeNoteDialogReport = null },
            title = { Text("Super Admin Case Review", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = {
                Column {
                    Text("${report.issueType} (${report.reporterName})", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(ReportStatus.entries.toList()) { st ->
                            val isSelected = st == selectedStatus
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isSelected) RydBrandGreen else Color(0xFFF1F5F9),
                                modifier = Modifier.clickable { selectedStatus = st }
                            ) {
                                Text(
                                    text = st.label,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else Color(0xFF334155),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Super Admin Resolution Notes") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onUpdateReportStatus(report.id, selectedStatus, notes)
                        activeNoteDialogReport = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RydGreenPrimary)
                ) {
                    Text("Save Update")
                }
            },
            dismissButton = {
                TextButton(onClick = { activeNoteDialogReport = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
