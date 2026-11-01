package com.example.ui

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.model.AccountStatus
import com.example.data.model.BookingEntity
import com.example.data.model.BookingStatus
import com.example.data.model.CampusEntity
import com.example.data.model.CoordinatedRequestEntity
import com.example.data.model.DriverProfileEntity
import com.example.data.model.DriverQueueEntryEntity
import com.example.data.model.DriverQueueStatus
import com.example.data.model.DriverVerificationStatus
import com.example.data.model.DriverWalletEntity
import com.example.data.model.FareSharingMode
import com.example.data.model.InstitutionEntity
import com.example.data.model.InstitutionType
import com.example.data.model.NotificationEntity
import com.example.data.model.PassengerProfileEntity
import com.example.data.model.PassengerWalletEntity
import com.example.data.model.PickupLocationEntity
import com.example.data.model.PlatformSettingsEntity
import com.example.data.model.ReportEntity
import com.example.data.model.RouteEntity
import com.example.data.model.ServiceZoneEntity
import com.example.data.model.StateEntity
import com.example.data.model.TransportParkEntity
import com.example.data.model.TripEntity
import com.example.data.model.TripStatus
import com.example.data.model.UniversityEntity
import com.example.data.model.UserEntity
import com.example.data.model.UserRole
import com.example.data.model.VehicleCategory
import com.example.data.model.VehicleEntity
import com.example.data.model.WalletTransactionEntity
import com.example.data.repository.RydRoverRepository
import com.example.data.service.EligibleTripOption
import com.example.data.service.RouteCoordinationService
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class RydRoverViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val database: AppDatabase = AppDatabase.getDatabase(application, viewModelScope)
    private val repository: RydRoverRepository = RydRoverRepository(database)
    private val authPrefs = application.getSharedPreferences("rydrover_auth_session", Context.MODE_PRIVATE)

    // Authentication & Multi-Step Registration State
    private val _authOnboardingStep = MutableStateFlow(AuthOnboardingStep.WELCOME)
    val authOnboardingStep: StateFlow<AuthOnboardingStep> = _authOnboardingStep.asStateFlow()

    private val _onboardingSelectedRole = MutableStateFlow<UserRole?>(null)
    val onboardingSelectedRole: StateFlow<UserRole?> = _onboardingSelectedRole.asStateFlow()

    private val _authErrorMessage = MutableStateFlow<String?>(null)
    val authErrorMessage: StateFlow<String?> = _authErrorMessage.asStateFlow()

    private val _isAuthProcessing = MutableStateFlow(false)
    val isAuthProcessing: StateFlow<Boolean> = _isAuthProcessing.asStateFlow()

    // Active Portal Role (Passenger, Commercial Driver, Institution Admin, Super Admin)
    private val _activeRole = MutableStateFlow(UserRole.PASSENGER)
    val activeRole: StateFlow<UserRole> = _activeRole.asStateFlow()
    val currentRole: StateFlow<UserRole> = activeRole

    // Active User ID (Empty until user logs in or registers; persisted for returning users)
    private val _currentUserId = MutableStateFlow(
        authPrefs.getString("authenticated_user_id", "") ?: ""
    )
    val currentUserId: StateFlow<String> = _currentUserId.asStateFlow()

    init {
        viewModelScope.launch {
            val existingInstitutions = repository.allUniversities.firstOrNull()
            if (existingInstitutions.isNullOrEmpty()) {
                AppDatabase.populateInitialData(database)
            }
            restoreAuthenticatedSession()
        }
    }

    private suspend fun restoreAuthenticatedSession() {
        val savedUserId = authPrefs.getString("authenticated_user_id", "")?.trim().orEmpty()
        if (savedUserId.isBlank()) {
            _authOnboardingStep.value = AuthOnboardingStep.WELCOME
            return
        }
        val user = repository.getUserSnapshot(savedUserId)
        if (user == null) {
            authPrefs.edit().remove("authenticated_user_id").apply()
            _currentUserId.value = ""
            _authOnboardingStep.value = AuthOnboardingStep.WELCOME
            return
        }
        _currentUserId.value = user.id
        _activeRole.value = user.role
        _onboardingSelectedRole.value = user.role
        routeUserToAppropriateStepOrDashboard(user)
    }

    private suspend fun routeUserToAppropriateStepOrDashboard(user: UserEntity) {
        _currentUserId.value = user.id
        _activeRole.value = user.role
        _onboardingSelectedRole.value = user.role
        authPrefs.edit().putString("authenticated_user_id", user.id).apply()

        // Administrators & Super Admins go directly to their dashboard
        if (user.role == UserRole.ADMIN || user.role == UserRole.SUPER_ADMIN) {
            _authOnboardingStep.value = AuthOnboardingStep.AUTHENTICATED_DASHBOARD
            return
        }

        // Check AccountSetupStatus to resume incomplete onboarding if needed
        when {
            user.accountStatus == AccountStatus.ACCOUNT_CREATED -> {
                _onboardingSelectedRole.value = null
                _authOnboardingStep.value = AuthOnboardingStep.ROLE_SELECTION
            }
            user.accountStatus == AccountStatus.ROLE_SELECTED || user.institutionId.isBlank() -> {
                _authOnboardingStep.value = AuthOnboardingStep.INSTITUTION_SELECTION
            }
            user.accountStatus == AccountStatus.INSTITUTION_SELECTED || user.campusId.isBlank() -> {
                val instCampuses = repository.getCampusesByInstitution(user.institutionId).firstOrNull().orEmpty()
                if (instCampuses.size == 1) {
                    repository.selectAccountCampus(user.id, user.institutionId, instCampuses.first().id)
                    _authOnboardingStep.value = AuthOnboardingStep.PROFILE_SETUP
                } else {
                    _authOnboardingStep.value = AuthOnboardingStep.CAMPUS_SELECTION
                }
            }
            !user.profileComplete || user.accountStatus == AccountStatus.CAMPUS_SELECTED -> {
                _authOnboardingStep.value = AuthOnboardingStep.PROFILE_SETUP
            }
            else -> {
                _authOnboardingStep.value = AuthOnboardingStep.AUTHENTICATED_DASHBOARD
            }
        }
    }

    // Search & Route Filters (Passenger Portal)
    private val _selectedPickupFilter = MutableStateFlow("All")
    val selectedPickupFilter: StateFlow<String> = _selectedPickupFilter.asStateFlow()
    val passengerPickup: StateFlow<String> = selectedPickupFilter

    private val _selectedDestinationFilter = MutableStateFlow("All")
    val selectedDestinationFilter: StateFlow<String> = _selectedDestinationFilter.asStateFlow()
    val passengerDestination: StateFlow<String> = selectedDestinationFilter

    private val _selectedCategoryFilter = MutableStateFlow<VehicleCategory?>(null)
    val selectedCategoryFilter: StateFlow<VehicleCategory?> = _selectedCategoryFilter.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // GPS Location Mode & Real Device Location Tracking (No Hardcoded UNIPORT Fallbacks)
    private val _useCurrentGpsPickup = MutableStateFlow(true)
    val useCurrentGpsPickup: StateFlow<Boolean> = _useCurrentGpsPickup.asStateFlow()

    private val _hasGpsPermission = MutableStateFlow(false)
    val hasGpsPermission: StateFlow<Boolean> = _hasGpsPermission.asStateFlow()

    private val _deviceGpsCoordinates = MutableStateFlow<Pair<Double, Double>?>(null)
    val deviceGpsCoordinates: StateFlow<Pair<Double, Double>?> = _deviceGpsCoordinates.asStateFlow()

    // Selected Trip ID for Driver Dashboard detail / manifest inspection
    private val _selectedDriverTripId = MutableStateFlow<String?>(null)
    val selectedDriverTripId: StateFlow<String?> = _selectedDriverTripId.asStateFlow()

    // Latest QR / Code Verification Result Banner for Driver
    private val _qrVerificationResult = MutableStateFlow<QrScanFeedback?>(null)
    val qrVerificationResult: StateFlow<QrScanFeedback?> = _qrVerificationResult.asStateFlow()

    // UI Feedback Snackbar Message
    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()
    val snackMessage: StateFlow<String?> = snackbarMessage

    // Platform-wide streams (for Super Admin and directory lookups)
    val allStates: StateFlow<List<StateEntity>> = repository.allStates
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allUniversities: StateFlow<List<UniversityEntity>> = repository.allUniversities
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allInstitutions: StateFlow<List<InstitutionEntity>> = repository.allInstitutions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allCampuses: StateFlow<List<CampusEntity>> = repository.allCampuses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allParks: StateFlow<List<TransportParkEntity>> = repository.allParks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allServiceZones: StateFlow<List<ServiceZoneEntity>> = repository.allServiceZones
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allQueueEntries: StateFlow<List<DriverQueueEntryEntity>> = repository.allQueueEntries
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allVehicles: StateFlow<List<VehicleEntity>> = repository.allVehicles
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allUsers: StateFlow<List<UserEntity>> = repository.allUsers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTrips: StateFlow<List<TripEntity>> = repository.allTrips
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allBookings: StateFlow<List<BookingEntity>> = repository.allBookings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allRoutes: StateFlow<List<RouteEntity>> = repository.allRoutes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allPickupLocations: StateFlow<List<PickupLocationEntity>> = repository.allPickupLocations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val pickupLocations: StateFlow<List<PickupLocationEntity>> = allPickupLocations

    val activePickupLocations: StateFlow<List<PickupLocationEntity>> = repository.activePickupLocations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allCoordinatedRequests: StateFlow<List<CoordinatedRequestEntity>> = repository.allCoordinatedRequests
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val openCoordinatedRequests: StateFlow<List<CoordinatedRequestEntity>> = repository.openCoordinatedRequests
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val platformSettings: StateFlow<PlatformSettingsEntity> = repository.platformSettings
        .map { it ?: PlatformSettingsEntity() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PlatformSettingsEntity())

    val allReports: StateFlow<List<ReportEntity>> = repository.allReports
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allDriverProfiles: StateFlow<List<DriverProfileEntity>> = repository.allDriverProfiles
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Step 1: Authenticated User Profile (Fetched directly by _currentUserId, never defaulting to UNIPORT)
    val currentUser: StateFlow<UserEntity?> = _currentUserId
        .flatMapLatest { id -> repository.getUserById(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Step 2a: Authenticated Driver Profile (Fetched directly by _currentUserId)
    val currentDriverProfile: StateFlow<DriverProfileEntity?> = _currentUserId
        .flatMapLatest { id ->
            if (id.isBlank()) flowOf(null) else repository.getDriverProfileById(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Step 2b: Authenticated Passenger Profile (Fetched directly by _currentUserId)
    val currentPassengerProfile: StateFlow<PassengerProfileEntity?> = _currentUserId
        .flatMapLatest { id ->
            if (id.isBlank()) flowOf(null) else repository.getPassengerProfileById(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Passenger RydWallet (Strictly accessible only when authenticated user role == PASSENGER)
    val currentPassengerWallet: StateFlow<PassengerWalletEntity?> = currentUser
        .flatMapLatest { user ->
            if (user == null || user.role != UserRole.PASSENGER) {
                flowOf(null)
            } else {
                repository.getPassengerWallet(user.id)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Driver RydWallet (Strictly private to authenticated DRIVER; NEVER exposed to passengers)
    val currentDriverWallet: StateFlow<DriverWalletEntity?> = currentUser
        .flatMapLatest { user ->
            if (user == null || user.role != UserRole.DRIVER) {
                flowOf(null)
            } else {
                repository.getDriverWallet(user.id)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Authenticated User's Wallet Transactions (Scoped strictly to currentUser.id & role)
    val currentUserWalletTransactions: StateFlow<List<WalletTransactionEntity>> = currentUser
        .flatMapLatest { user ->
            if (user == null) {
                flowOf(emptyList())
            } else {
                repository.getWalletTransactionsForUser(user.id).map { list ->
                    list.filter { it.userRole == user.role }
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Authenticated User's Notifications (Scoped strictly to currentUser.id & institutionId)
    val currentUserNotifications: StateFlow<List<NotificationEntity>> = currentUser
        .flatMapLatest { user ->
            if (user == null) {
                flowOf(emptyList())
            } else if (user.role == UserRole.ADMIN) {
                combine(
                    repository.getNotificationsForUser(user.id),
                    repository.getAdminNotificationsByInstitution(user.institutionId)
                ) { direct, adminNotifs ->
                    (direct + adminNotifs).distinctBy { it.id }.sortedByDescending { it.timestamp }
                }
            } else {
                repository.getNotificationsForUser(user.id)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentUserWallet: StateFlow<PassengerWalletEntity?> = currentPassengerWallet
    val currentUserTransactions: StateFlow<List<WalletTransactionEntity>> = currentUserWalletTransactions

    // Authenticated User's Submitted Support & Technical Tickets
    val currentUserSupportTickets: StateFlow<List<ReportEntity>> = combine(
        currentUser,
        allReports
    ) { user, reports ->
        if (user == null) {
            emptyList()
        } else {
            reports.filter {
                it.institutionId == user.institutionId &&
                    (it.userId == user.id || it.reporterName.equals(user.fullName, ignoreCase = true))
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Step 3: Resolve Effective Institution Context (driverProfile.institutionId -> user.institutionId)
    private val activeContextFlow = combine(
        currentUser,
        currentDriverProfile,
        _activeRole
    ) { user, driverProfile, role ->
        if (user == null) {
            null
        } else if (role == UserRole.DRIVER) {
            val instId = driverProfile?.institutionId?.ifBlank { user.institutionId } ?: user.institutionId
            val campId = driverProfile?.campusId?.ifBlank { user.campusId } ?: user.campusId
            InstitutionCampusContext(
                userId = user.id,
                role = role,
                institutionId = instId,
                campusId = campId
            )
        } else {
            InstitutionCampusContext(
                userId = user.id,
                role = role,
                institutionId = user.institutionId,
                campusId = user.campusId
            )
        }
    }

    // Active Institution Entity (Database-queried by institutionId; NEVER falls back to universities.firstOrNull())
    val currentInstitution: StateFlow<InstitutionEntity?> = activeContextFlow
        .flatMapLatest { ctx ->
            if (ctx == null || ctx.institutionId.isBlank()) {
                flowOf(null)
            } else {
                repository.getInstitutionById(ctx.institutionId)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Institution Campuses (Database-queried by institutionId)
    val institutionCampuses: StateFlow<List<CampusEntity>> = activeContextFlow
        .flatMapLatest { ctx ->
            if (ctx == null || ctx.institutionId.isBlank()) {
                flowOf(emptyList())
            } else {
                repository.getCampusesByInstitution(ctx.institutionId)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val currentInstitutionCampuses: StateFlow<List<CampusEntity>> = institutionCampuses

    // Active Campus Entity within the current Institution
    val currentCampus: StateFlow<CampusEntity?> = combine(
        activeContextFlow,
        institutionCampuses
    ) { ctx, campuses ->
        if (ctx == null) null
        else campuses.find { it.id == ctx.campusId } ?: campuses.firstOrNull()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Institution Loading / Readiness State (Prevents rendering stale/default data while institution context loads)
    val isInstitutionContextReady: StateFlow<Boolean> = combine(
        currentUser,
        currentDriverProfile,
        currentInstitution,
        _activeRole
    ) { user, driverProfile, institution, role ->
        if (user == null || institution == null) {
            false
        } else if (role == UserRole.DRIVER) {
            val expectedInstId = driverProfile?.institutionId?.ifBlank { user.institutionId } ?: user.institutionId
            driverProfile != null &&
                driverProfile.driverId == user.id &&
                institution.id == expectedInstId
        } else {
            institution.id == user.institutionId
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    // Authorized Institutions for the active Driver or Admin
    val authorizedInstitutionsForCurrentUser: StateFlow<List<InstitutionEntity>> = combine(
        currentUser,
        currentDriverProfile,
        allUniversities
    ) { user, driverProfile, universities ->
        if (user == null) emptyList()
        else if (user.role == UserRole.SUPER_ADMIN) universities
        else {
            val csv = driverProfile?.authorizedInstitutionIds?.ifBlank { user.authorizedInstitutionIds }
                ?: user.authorizedInstitutionIds
            val allowedIds = csv.split(",").map { it.trim() }.filter { it.isNotBlank() }.toSet()
            universities.filter { it.id in allowedIds || it.id == user.institutionId }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val driverAuthorizedInstitutions: StateFlow<List<InstitutionEntity>> = authorizedInstitutionsForCurrentUser

    // Database-Isolated Institution Transport Network Streams
    val institutionParks: StateFlow<List<TransportParkEntity>> = activeContextFlow
        .flatMapLatest { ctx ->
            if (ctx == null || ctx.institutionId.isBlank()) flowOf(emptyList())
            else repository.getParksByInstitution(ctx.institutionId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val institutionServiceZones: StateFlow<List<ServiceZoneEntity>> = activeContextFlow
        .flatMapLatest { ctx ->
            if (ctx == null || ctx.institutionId.isBlank()) flowOf(emptyList())
            else repository.getServiceZonesByInstitution(ctx.institutionId).map { zones ->
                val campusFiltered = if (ctx.campusId.isNotBlank()) {
                    zones.filter { it.campusId == ctx.campusId }
                } else emptyList()
                campusFiltered.ifEmpty { zones }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val institutionPickupLocations: StateFlow<List<PickupLocationEntity>> = activeContextFlow
        .flatMapLatest { ctx ->
            if (ctx == null || ctx.institutionId.isBlank()) flowOf(emptyList())
            else repository.getPickupLocationsByInstitution(ctx.institutionId).map { locs ->
                val activeLocs = locs.filter { it.isActive }
                val campusFiltered = if (ctx.campusId.isNotBlank()) {
                    activeLocs.filter { it.campusId == ctx.campusId }
                } else emptyList()
                campusFiltered.ifEmpty { activeLocs }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val institutionRoutes: StateFlow<List<RouteEntity>> = activeContextFlow
        .flatMapLatest { ctx ->
            if (ctx == null || ctx.institutionId.isBlank()) flowOf(emptyList())
            else repository.getRoutesByInstitution(ctx.institutionId).map { routes ->
                val campusFiltered = if (ctx.campusId.isNotBlank()) {
                    routes.filter { it.campusId == ctx.campusId }
                } else emptyList()
                campusFiltered.ifEmpty { routes }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val institutionQueueEntries: StateFlow<List<DriverQueueEntryEntity>> = activeContextFlow
        .flatMapLatest { ctx ->
            if (ctx == null || ctx.institutionId.isBlank()) flowOf(emptyList())
            else repository.getQueueEntriesByInstitution(ctx.institutionId).map { entries ->
                // Strictly enforce institutionId match; filter by campusId when entries exist for that campus
                val strictInst = entries.filter { it.institutionId == ctx.institutionId }
                val campusFiltered = if (ctx.campusId.isNotBlank()) {
                    strictInst.filter { it.campusId.isBlank() || it.campusId == ctx.campusId }
                } else strictInst
                campusFiltered.ifEmpty { strictInst }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val institutionVehicles: StateFlow<List<VehicleEntity>> = activeContextFlow
        .flatMapLatest { ctx ->
            if (ctx == null || ctx.institutionId.isBlank()) flowOf(emptyList())
            else repository.getVehiclesByInstitution(ctx.institutionId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val institutionTrips: StateFlow<List<TripEntity>> = activeContextFlow
        .flatMapLatest { ctx ->
            if (ctx == null || ctx.institutionId.isBlank()) flowOf(emptyList())
            else repository.getTripsByInstitution(ctx.institutionId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val institutionBookings: StateFlow<List<BookingEntity>> = activeContextFlow
        .flatMapLatest { ctx ->
            if (ctx == null || ctx.institutionId.isBlank()) flowOf(emptyList())
            else repository.getBookingsByInstitution(ctx.institutionId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val institutionUsers: StateFlow<List<UserEntity>> = activeContextFlow
        .flatMapLatest { ctx ->
            if (ctx == null || ctx.institutionId.isBlank()) flowOf(emptyList())
            else repository.getUsersByInstitution(ctx.institutionId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val institutionDriverProfiles: StateFlow<List<DriverProfileEntity>> = activeContextFlow
        .flatMapLatest { ctx ->
            if (ctx == null || ctx.institutionId.isBlank()) flowOf(emptyList())
            else repository.getDriverProfilesByInstitution(ctx.institutionId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val institutionCoordinatedRequests: StateFlow<List<CoordinatedRequestEntity>> = activeContextFlow
        .flatMapLatest { ctx ->
            if (ctx == null || ctx.institutionId.isBlank()) flowOf(emptyList())
            else repository.getRequestsByInstitution(ctx.institutionId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val institutionReports: StateFlow<List<ReportEntity>> = activeContextFlow
        .flatMapLatest { ctx ->
            if (ctx == null || ctx.institutionId.isBlank()) flowOf(emptyList())
            else repository.getReportsByInstitution(ctx.institutionId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Dynamic Institution-Scoped GPS Location & Service Zone (Never defaults to UNIPORT Engineering Block)
    val passengerLat: StateFlow<Double> = combine(
        _deviceGpsCoordinates,
        currentCampus,
        institutionServiceZones,
        currentInstitution
    ) { gps, campus, zones, inst ->
        gps?.first
            ?: zones.firstOrNull()?.centerLat?.takeIf { it != 0.0 }
            ?: campus?.centerLat?.takeIf { it != 0.0 }
            ?: inst?.centerLat ?: 0.0
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val passengerLng: StateFlow<Double> = combine(
        _deviceGpsCoordinates,
        currentCampus,
        institutionServiceZones,
        currentInstitution
    ) { gps, campus, zones, inst ->
        gps?.second
            ?: zones.firstOrNull()?.centerLng?.takeIf { it != 0.0 }
            ?: campus?.centerLng?.takeIf { it != 0.0 }
            ?: inst?.centerLng ?: 0.0
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val gpsServiceZoneName: StateFlow<String> = combine(
        _hasGpsPermission,
        _deviceGpsCoordinates,
        institutionServiceZones,
        currentCampus
    ) { hasPerm, gps, zones, campus ->
        if (zones.isEmpty()) {
            campus?.name ?: "Institution Service Area"
        } else if (hasPerm && gps != null) {
            val nearest = RouteCoordinationService.detectServiceZone(gps.first, gps.second, zones)
                ?: zones.minByOrNull {
                    RouteCoordinationService.calculateDistanceMeters(gps.first, gps.second, it.centerLat, it.centerLng)
                }
            nearest?.name ?: zones.first().name
        } else {
            zones.first().name
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Loading Service Zone...")

    val gpsLocationName: StateFlow<String> = combine(
        _hasGpsPermission,
        _deviceGpsCoordinates,
        institutionPickupLocations,
        institutionServiceZones,
        currentCampus
    ) { hasPerm, gps, pickups, zones, campus ->
        val firstPickup = pickups.firstOrNull()?.name
            ?: zones.firstOrNull()?.name?.removeSuffix(" Zone")
            ?: campus?.name
            ?: "Campus Pickup Point"
        if (hasPerm && gps != null && pickups.isNotEmpty()) {
            val nearestPickup = pickups.minByOrNull {
                RouteCoordinationService.calculateDistanceMeters(gps.first, gps.second, it.latitude, it.longitude)
            }
            "${nearestPickup?.name ?: firstPickup} (Current GPS Location)"
        } else {
            "$firstPickup (Campus Zone)"
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Loading Pickup Location...")

    // Current Driver Queue Entry (Strictly scoped to driverId AND driver's institutionId)
    val currentDriverQueueEntry: StateFlow<DriverQueueEntryEntity?> = activeContextFlow
        .flatMapLatest { ctx ->
            if (ctx == null || ctx.institutionId.isBlank()) {
                flowOf(null)
            } else {
                repository.getQueueEntryForDriverAndInstitution(ctx.userId, ctx.institutionId)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Filtered Trips for Passenger Feed (Strictly scoped to passenger's registered institution)
    val filteredPassengerTrips: StateFlow<List<TripEntity>> = combine(
        institutionTrips,
        _selectedPickupFilter,
        _selectedDestinationFilter,
        _selectedCategoryFilter,
        _searchQuery
    ) { trips, pickup, dest, category, query ->
        val baseFiltered = RouteCoordinationService.filterAvailableTrips(
            allTrips = trips,
            pickupFilter = pickup,
            destinationFilter = dest,
            categoryFilter = category,
            searchQuery = query
        )
        baseFiltered.sortedWith(
            compareBy<TripEntity> { it.queuePriorityRank }
                .thenBy { it.estimatedArrivalMinutes }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val availableRides: StateFlow<List<TripEntity>> = filteredPassengerTrips

    // Dynamic Route-Aware Queue Eligibility Preview for Passenger's Selected Route (Strictly Institution-Scoped)
    val eligibleQueueOptionsForSelection: StateFlow<List<EligibleTripOption>> = combine(
        institutionTrips,
        institutionQueueEntries,
        institutionServiceZones,
        _selectedPickupFilter,
        _selectedDestinationFilter
    ) { trips, queueEntries, zones, pickup, dest ->
        val defaultZoneOrigin = zones.firstOrNull()?.name?.removeSuffix(" Zone")?.trim() ?: ""
        val effectivePickup = if (pickup == "All") defaultZoneOrigin else pickup
        val effectiveDest = if (dest == "All") "" else dest
        val pLat = passengerLat.value
        val pLng = passengerLng.value
        RouteCoordinationService.rankEligibleTripsForPassenger(
            allTrips = trips,
            queueEntries = queueEntries,
            serviceZones = zones,
            pickupLocation = effectivePickup,
            destinationLocation = effectiveDest,
            passengerLat = pLat,
            passengerLng = pLng,
            requiredSeats = 1
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Current Passenger's Bookings (Strictly Database-Queried by passengerId AND institutionId)
    val currentPassengerBookings: StateFlow<List<BookingEntity>> = activeContextFlow
        .flatMapLatest { ctx ->
            if (ctx == null || ctx.institutionId.isBlank()) {
                flowOf(emptyList())
            } else {
                repository.getBookingsForPassengerAndInstitution(ctx.userId, ctx.institutionId)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val latestPassengerActiveBooking: StateFlow<BookingEntity?> = currentPassengerBookings
        .map { list ->
            list.firstOrNull {
                it.status != BookingStatus.CANCELLED &&
                    it.status != BookingStatus.COMPLETED &&
                    it.boardingStatus != "Cancelled" &&
                    it.boardingStatus != "Completed"
            } ?: list.firstOrNull()
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val latestPassengerActiveTrip: StateFlow<TripEntity?> = combine(
        latestPassengerActiveBooking,
        institutionTrips
    ) { booking, trips ->
        if (booking == null) null
        else trips.find { it.id == booking.tripId }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Institution Admin Analytics (Scoped strictly to the active institution)
    val adminAnalytics: StateFlow<AdminAnalytics> = combine(
        institutionQueueEntries,
        institutionServiceZones,
        institutionTrips,
        institutionBookings,
        institutionReports
    ) { queueEntries, zones, trips, bookings, reports ->
        val queuedCount = queueEntries.count { it.queueStatus != DriverQueueStatus.OFFLINE }
        val activeTripsCount = trips.count {
            it.status != TripStatus.COMPLETED && it.status != TripStatus.CANCELLED
        }
        val completedCount = trips.count { it.status == TripStatus.COMPLETED }
        val gmv = bookings.filter { it.status != BookingStatus.CANCELLED }.sumOf { it.totalFareNaira }
        val openReports = reports.count { it.status == "OPEN" }
        AdminAnalytics(
            queuedDriversCount = queuedCount,
            totalServiceZonesCount = zones.size,
            activeTripsCount = activeTripsCount,
            completedTripsCount = completedCount,
            totalGmvNaira = gmv,
            openReportsCount = openReports
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AdminAnalytics())

    // Super Admin Platform-Wide Analytics
    val superAdminPlatformAnalytics: StateFlow<AdminAnalytics> = combine(
        allQueueEntries,
        allServiceZones,
        allTrips,
        allBookings,
        allReports
    ) { queueEntries, zones, trips, bookings, reports ->
        val queuedCount = queueEntries.count { it.queueStatus != DriverQueueStatus.OFFLINE }
        val activeTripsCount = trips.count {
            it.status != TripStatus.COMPLETED && it.status != TripStatus.CANCELLED
        }
        val completedCount = trips.count { it.status == TripStatus.COMPLETED }
        val gmv = bookings.filter { it.status != BookingStatus.CANCELLED }.sumOf { it.totalFareNaira }
        val openReports = reports.count { it.status == "OPEN" }
        AdminAnalytics(
            queuedDriversCount = queuedCount,
            totalServiceZonesCount = zones.size,
            activeTripsCount = activeTripsCount,
            completedTripsCount = completedCount,
            totalGmvNaira = gmv,
            openReportsCount = openReports
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AdminAnalytics())

    // Current Driver's Trips (Strictly Database-Queried by driverId AND driver's institutionId & campusId)
    val currentDriverTrips: StateFlow<List<TripEntity>> = activeContextFlow
        .flatMapLatest { ctx ->
            if (ctx == null || ctx.institutionId.isBlank()) {
                flowOf(emptyList())
            } else {
                repository.getTripsByDriverAndInstitution(ctx.userId, ctx.institutionId).map { trips ->
                    val strictInst = trips.filter { it.institutionId == ctx.institutionId }
                    val campusMatched = if (ctx.campusId.isNotBlank()) {
                        strictInst.filter { it.campusId.isBlank() || it.campusId == ctx.campusId }
                    } else strictInst
                    campusMatched.ifEmpty { strictInst }
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Bookings Assigned to Current Driver's Trips (Enforces booking.institutionId === driver.institutionId & campusId)
    val driverAssignedBookings: StateFlow<List<BookingEntity>> = combine(
        activeContextFlow,
        currentDriverTrips,
        institutionBookings
    ) { ctx, driverTrips, bookings ->
        if (ctx == null || ctx.institutionId.isBlank()) {
            emptyList()
        } else {
            val driverTripIds = driverTrips.map { it.id }.toSet()
            bookings.filter { booking ->
                booking.tripId in driverTripIds &&
                    booking.institutionId == ctx.institutionId &&
                    (ctx.campusId.isBlank() || booking.campusId.isBlank() || booking.campusId == ctx.campusId)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // =========================================================================
    // AUTHENTICATION, REGISTRATION & ONBOARDING FLOW ACTIONS
    // =========================================================================

    fun clearAuthError() {
        _authErrorMessage.value = null
    }

    fun navigateToCreateAccount() {
        _authErrorMessage.value = null
        _onboardingSelectedRole.value = null
        _authOnboardingStep.value = AuthOnboardingStep.SIGN_UP_ACCOUNT
    }

    fun navigateToLogin() {
        _authErrorMessage.value = null
        _authOnboardingStep.value = AuthOnboardingStep.LOGIN
    }

    fun navigateToWelcome() {
        _authErrorMessage.value = null
        _authOnboardingStep.value = AuthOnboardingStep.WELCOME
    }

    fun navigateOnboardingBack() {
        _authErrorMessage.value = null
        when (_authOnboardingStep.value) {
            AuthOnboardingStep.SIGN_UP_ACCOUNT -> {
                _authOnboardingStep.value = AuthOnboardingStep.WELCOME
            }
            AuthOnboardingStep.ROLE_SELECTION -> {
                _authOnboardingStep.value = AuthOnboardingStep.SIGN_UP_ACCOUNT
            }
            AuthOnboardingStep.INSTITUTION_SELECTION -> {
                _authOnboardingStep.value = AuthOnboardingStep.ROLE_SELECTION
            }
            AuthOnboardingStep.CAMPUS_SELECTION -> {
                _authOnboardingStep.value = AuthOnboardingStep.INSTITUTION_SELECTION
            }
            AuthOnboardingStep.PROFILE_SETUP -> {
                val campuses = institutionCampuses.value
                if (campuses.size > 1) {
                    _authOnboardingStep.value = AuthOnboardingStep.CAMPUS_SELECTION
                } else {
                    _authOnboardingStep.value = AuthOnboardingStep.INSTITUTION_SELECTION
                }
            }
            else -> {
                _authOnboardingStep.value = AuthOnboardingStep.WELCOME
            }
        }
    }

    fun submitLogin(identifier: String, password: String) {
        _authErrorMessage.value = null
        _isAuthProcessing.value = true
        viewModelScope.launch {
            val result = repository.authenticateUser(identifier, password)
            _isAuthProcessing.value = false
            result.fold(
                onSuccess = { user ->
                    _selectedDriverTripId.value = null
                    routeUserToAppropriateStepOrDashboard(user)
                    _snackbarMessage.value = "Welcome back, ${user.fullName}!"
                },
                onFailure = { err ->
                    _authErrorMessage.value = err.message ?: "Login failed."
                }
            )
        }
    }

    fun quickLoginAsUser(userId: String) {
        _authErrorMessage.value = null
        _isAuthProcessing.value = true
        viewModelScope.launch {
            val user = repository.getUserSnapshot(userId)
            _isAuthProcessing.value = false
            if (user != null) {
                _selectedDriverTripId.value = null
                routeUserToAppropriateStepOrDashboard(user)
                _snackbarMessage.value = "Signed in as ${user.fullName} (${user.role.displayName})."
            } else {
                _authErrorMessage.value = "Account not found."
            }
        }
    }

    fun submitInitialAccount(
        fullName: String,
        phone: String,
        email: String,
        password: String,
        confirmPassword: String
    ) {
        _authErrorMessage.value = null
        if (fullName.trim().length < 2) {
            _authErrorMessage.value = "Please enter your full name."
            return
        }
        if (phone.trim().length < 7) {
            _authErrorMessage.value = "Please enter a valid phone number."
            return
        }
        if (email.isNotBlank() && (!email.contains("@") || !email.contains("."))) {
            _authErrorMessage.value = "Please enter a valid email address or leave it blank."
            return
        }
        if (password.length < 6) {
            _authErrorMessage.value = "Password must be at least 6 characters."
            return
        }
        if (password != confirmPassword) {
            _authErrorMessage.value = "Passwords do not match. Please confirm your password."
            return
        }

        _isAuthProcessing.value = true
        viewModelScope.launch {
            val result = repository.createInitialAccount(fullName, phone, email, password)
            _isAuthProcessing.value = false
            result.fold(
                onSuccess = { newUser ->
                    _currentUserId.value = newUser.id
                    authPrefs.edit().putString("authenticated_user_id", newUser.id).apply()
                    _onboardingSelectedRole.value = null
                    _authOnboardingStep.value = AuthOnboardingStep.ROLE_SELECTION
                },
                onFailure = { err ->
                    _authErrorMessage.value = err.message ?: "Could not create account."
                }
            )
        }
    }

    fun selectOnboardingRole(role: UserRole) {
        _authErrorMessage.value = null
        if (role == UserRole.ADMIN || role == UserRole.SUPER_ADMIN) {
            _authErrorMessage.value = "Administrator accounts cannot be selected during public registration."
            return
        }
        val uid = _currentUserId.value
        if (uid.isBlank()) {
            _authErrorMessage.value = "Please create your account first."
            _authOnboardingStep.value = AuthOnboardingStep.SIGN_UP_ACCOUNT
            return
        }
        _isAuthProcessing.value = true
        viewModelScope.launch {
            val result = repository.selectAccountRole(uid, role)
            _isAuthProcessing.value = false
            result.fold(
                onSuccess = { updatedUser ->
                    _onboardingSelectedRole.value = updatedUser.role
                    _activeRole.value = updatedUser.role
                    _authOnboardingStep.value = AuthOnboardingStep.INSTITUTION_SELECTION
                },
                onFailure = { err ->
                    _authErrorMessage.value = err.message ?: "Failed to save role."
                }
            )
        }
    }

    fun selectOnboardingInstitution(institutionId: String) {
        _authErrorMessage.value = null
        val uid = _currentUserId.value
        if (uid.isBlank()) return
        _isAuthProcessing.value = true
        viewModelScope.launch {
            val result = repository.selectAccountInstitution(uid, institutionId)
            _isAuthProcessing.value = false
            result.fold(
                onSuccess = { (_, campuses) ->
                    if (campuses.size <= 1) {
                        // Automatically selected the single campus; proceed directly to Profile setup
                        _authOnboardingStep.value = AuthOnboardingStep.PROFILE_SETUP
                    } else {
                        _authOnboardingStep.value = AuthOnboardingStep.CAMPUS_SELECTION
                    }
                },
                onFailure = { err ->
                    _authErrorMessage.value = err.message ?: "Failed to select institution."
                }
            )
        }
    }

    fun selectOnboardingCampus(campusId: String) {
        _authErrorMessage.value = null
        val uid = _currentUserId.value
        val instId = currentUser.value?.institutionId ?: currentInstitution.value?.id ?: ""
        if (uid.isBlank() || instId.isBlank()) return
        _isAuthProcessing.value = true
        viewModelScope.launch {
            val result = repository.selectAccountCampus(uid, instId, campusId)
            _isAuthProcessing.value = false
            result.fold(
                onSuccess = {
                    _authOnboardingStep.value = AuthOnboardingStep.PROFILE_SETUP
                },
                onFailure = { err ->
                    _authErrorMessage.value = err.message ?: "Failed to select campus."
                }
            )
        }
    }

    fun completePassengerOnboardingProfile(
        fullName: String,
        phone: String,
        studentIdOrMatric: String,
        departmentOrFaculty: String
    ) {
        _authErrorMessage.value = null
        val uid = _currentUserId.value
        val instId = currentUser.value?.institutionId ?: currentInstitution.value?.id ?: ""
        val campId = currentUser.value?.campusId ?: currentCampus.value?.id ?: ""
        if (uid.isBlank() || instId.isBlank()) {
            _authErrorMessage.value = "Institution selection is missing."
            return
        }
        _isAuthProcessing.value = true
        viewModelScope.launch {
            val result = repository.completePassengerOnboarding(
                userId = uid,
                fullName = fullName,
                phoneNumber = phone,
                institutionId = instId,
                campusId = campId,
                studentIdOrMatric = studentIdOrMatric,
                departmentOrFaculty = departmentOrFaculty
            )
            _isAuthProcessing.value = false
            result.fold(
                onSuccess = { user ->
                    _activeRole.value = UserRole.PASSENGER
                    _selectedPickupFilter.value = "All"
                    _selectedDestinationFilter.value = "All"
                    authPrefs.edit().putString("authenticated_user_id", user.id).apply()
                    _authOnboardingStep.value = AuthOnboardingStep.AUTHENTICATED_DASHBOARD
                    val instName = allUniversities.value.find { it.id == user.institutionId }?.shortCode ?: "Institution"
                    _snackbarMessage.value = "Welcome to RydRover, ${user.fullName}! Loaded $instName Passenger Network."
                },
                onFailure = { err ->
                    _authErrorMessage.value = err.message ?: "Could not complete passenger setup."
                }
            )
        }
    }

    fun completeDriverOnboardingProfile(
        fullName: String,
        phone: String,
        licenseNumber: String,
        vehicleCategory: VehicleCategory,
        vehicleMakeModel: String,
        vehicleColor: String,
        licensePlate: String,
        registrationNumber: String,
        availableSeats: Int
    ) {
        _authErrorMessage.value = null
        val uid = _currentUserId.value
        val instId = currentUser.value?.institutionId ?: currentInstitution.value?.id ?: ""
        val campId = currentUser.value?.campusId ?: currentCampus.value?.id ?: ""
        if (uid.isBlank() || instId.isBlank()) {
            _authErrorMessage.value = "Institution selection is missing."
            return
        }
        if (licenseNumber.isBlank() || vehicleMakeModel.isBlank() || licensePlate.isBlank()) {
            _authErrorMessage.value = "Driver identification, vehicle model, and license plate are required."
            return
        }
        _isAuthProcessing.value = true
        viewModelScope.launch {
            val result = repository.completeDriverOnboarding(
                userId = uid,
                fullName = fullName,
                phoneNumber = phone,
                institutionId = instId,
                campusId = campId,
                licenseNumber = licenseNumber,
                vehicleCategory = vehicleCategory,
                vehicleMakeModel = vehicleMakeModel,
                vehicleColor = vehicleColor,
                licensePlate = licensePlate,
                registrationNumber = registrationNumber,
                availableSeats = availableSeats
            )
            _isAuthProcessing.value = false
            result.fold(
                onSuccess = { user ->
                    _activeRole.value = UserRole.DRIVER
                    _selectedDriverTripId.value = null
                    authPrefs.edit().putString("authenticated_user_id", user.id).apply()
                    _authOnboardingStep.value = AuthOnboardingStep.AUTHENTICATED_DASHBOARD
                    val instName = allUniversities.value.find { it.id == user.institutionId }?.shortCode ?: "Institution"
                    _snackbarMessage.value = "Driver profile created under $instName! Pending Administrator Approval."
                },
                onFailure = { err ->
                    _authErrorMessage.value = err.message ?: "Could not complete driver onboarding."
                }
            )
        }
    }

    fun logout() {
        authPrefs.edit().remove("authenticated_user_id").apply()
        _selectedDriverTripId.value = null
        _currentUserId.value = ""
        _onboardingSelectedRole.value = null
        _authErrorMessage.value = null
        clearFilters()
        _authOnboardingStep.value = AuthOnboardingStep.WELCOME
    }

    // Role-Locked Portal Security: Normal users cannot switch portals from their dashboard
    fun selectRole(role: UserRole) {
        val user = currentUser.value
        if (user != null && user.role != role) {
            _snackbarMessage.value = "ACCESS DENIED: Portal switching is disabled. Your account is locked to ${user.role.displayName}. Please Log Out to sign into a different account."
            return
        }
    }

    fun switchDemoUser(userId: String) {
        val current = currentUser.value
        if (current != null && current.role != UserRole.SUPER_ADMIN && current.id != userId) {
            _snackbarMessage.value = "ACCESS DENIED: Please Log Out first to sign into another account."
            return
        }
        _selectedDriverTripId.value = null
        _currentUserId.value = userId
        authPrefs.edit().putString("authenticated_user_id", userId).apply()
        viewModelScope.launch {
            val targetUser = repository.getUserSnapshot(userId) ?: allUsers.value.find { it.id == userId }
            if (targetUser != null) {
                routeUserToAppropriateStepOrDashboard(targetUser)
            }
        }
    }

    fun switchRole(role: UserRole) = selectRole(role)

    fun switchActiveUserProfile(userId: String) = switchDemoUser(userId)

    fun loginAsDriver(driverId: String) {
        switchDemoUser(driverId)
    }

    fun resetDemoData() {
        clearFilters()
        viewModelScope.launch {
            AppDatabase.populateInitialData(database)
            _snackbarMessage.value = "Multi-institution transport data refreshed."
        }
    }

    // Passenger: Select or switch school ("What school do you attend?") & campus
    fun selectPassengerInstitutionAndCampus(institutionId: String, campusId: String?) {
        val passenger = currentUser.value ?: return
        val resolvedCampusId = campusId
            ?: allCampuses.value.firstOrNull { it.universityId == institutionId }?.id
            ?: ""
        viewModelScope.launch {
            repository.updatePassengerInstitutionAndCampus(
                passengerId = passenger.id,
                institutionId = institutionId,
                campusId = resolvedCampusId
            )
            _selectedPickupFilter.value = "All"
            _selectedDestinationFilter.value = "All"
            val instName = allUniversities.value.find { it.id == institutionId }?.name ?: "Institution"
            _snackbarMessage.value = "Loaded $instName transport network."
        }
    }

    // Passenger Registration with Institution Selection ("What school do you attend?")
    fun registerPassengerWithInstitution(
        fullName: String,
        phoneNumber: String,
        email: String,
        institutionId: String,
        campusId: String?
    ) {
        val resolvedCampusId = campusId
            ?: allCampuses.value.firstOrNull { it.universityId == institutionId }?.id
            ?: ""
        viewModelScope.launch {
            val newPassenger = repository.registerOrSwitchPassengerProfile(
                fullName = fullName,
                phoneNumber = phoneNumber,
                email = email,
                institutionId = institutionId,
                campusId = resolvedCampusId
            )
            _currentUserId.value = newPassenger.id
            _activeRole.value = UserRole.PASSENGER
            _selectedPickupFilter.value = "All"
            _selectedDestinationFilter.value = "All"
            val instName = allUniversities.value.find { it.id == institutionId }?.name ?: "Institution"
            _snackbarMessage.value = "Welcome ${newPassenger.fullName}! Registered under $instName."
        }
    }

    // Driver: Switch active operating institution / campus or switch to that institution's dedicated driver
    fun switchDriverActiveInstitution(institutionId: String, campusId: String?) {
        val driver = currentUser.value ?: return
        val resolvedCampusId = campusId
            ?: allCampuses.value.firstOrNull { it.universityId == institutionId }?.id
            ?: ""
        _selectedDriverTripId.value = null

        // Check if there is a dedicated driver account for the selected institution
        val dedicatedDriver = allUsers.value.find {
            it.role == UserRole.DRIVER && it.institutionId == institutionId
        }
        if (dedicatedDriver != null && dedicatedDriver.id != driver.id) {
            _currentUserId.value = dedicatedDriver.id
            if (campusId != null && campusId != dedicatedDriver.campusId) {
                viewModelScope.launch {
                    repository.updateDriverActiveInstitutionAndCampus(
                        driverId = dedicatedDriver.id,
                        institutionId = institutionId,
                        campusId = resolvedCampusId
                    )
                }
            }
            val instName = allUniversities.value.find { it.id == institutionId }?.shortCode ?: "Institution"
            _snackbarMessage.value = "Switched to $instName Driver (${dedicatedDriver.fullName})."
            return
        }

        viewModelScope.launch {
            repository.updateDriverActiveInstitutionAndCampus(
                driverId = driver.id,
                institutionId = institutionId,
                campusId = resolvedCampusId
            )
            val instName = allUniversities.value.find { it.id == institutionId }?.shortCode ?: "Institution"
            _snackbarMessage.value = "Driver operating area switched to $instName."
        }
    }

    // Admin / Super Admin: Switch institution admin context
    fun switchAdminActiveInstitution(institutionId: String) {
        val admin = currentUser.value ?: return
        val resolvedCampusId = allCampuses.value.firstOrNull { it.universityId == institutionId }?.id
            ?: admin.campusId
        viewModelScope.launch {
            repository.updatePassengerInstitutionAndCampus(
                passengerId = admin.id,
                institutionId = institutionId,
                campusId = resolvedCampusId
            )
            val instName = allUniversities.value.find { it.id == institutionId }?.name ?: "Institution"
            _snackbarMessage.value = "Managing $instName transport network."
        }
    }

    // Super Admin Actions
    fun saveInstitution(
        id: String?,
        name: String,
        shortCode: String,
        institutionType: InstitutionType,
        stateId: String,
        stateName: String,
        city: String,
        centerLat: Double,
        centerLng: Double,
        isActive: Boolean = true
    ) {
        if (name.isBlank() || shortCode.isBlank()) {
            _snackbarMessage.value = "Institution name and short code are required."
            return
        }
        viewModelScope.launch {
            repository.saveInstitution(
                id = id,
                name = name,
                shortCode = shortCode,
                institutionType = institutionType,
                stateId = stateId,
                stateName = stateName,
                city = city,
                centerLat = centerLat,
                centerLng = centerLng,
                isActive = isActive
            )
            _snackbarMessage.value = "Institution '${name.trim()}' saved."
        }
    }

    fun toggleInstitutionStatus(institutionId: String, isActive: Boolean) {
        viewModelScope.launch {
            repository.toggleInstitutionActiveStatus(institutionId, isActive)
            val statusLabel = if (isActive) "activated" else "deactivated"
            _snackbarMessage.value = "Institution $statusLabel."
        }
    }

    fun assignInstitutionAdministrator(
        fullName: String,
        email: String,
        phone: String,
        institutionId: String,
        campusId: String
    ) {
        if (fullName.isBlank() || email.isBlank()) {
            _snackbarMessage.value = "Administrator full name and email are required."
            return
        }
        viewModelScope.launch {
            val admin = repository.assignInstitutionAdmin(
                fullName = fullName,
                email = email,
                phone = phone,
                institutionId = institutionId,
                campusId = campusId
            )
            _snackbarMessage.value = "Assigned ${admin.fullName} as Institution Administrator."
        }
    }

    fun assignDriverAuthorizedInstitutions(
        driverId: String,
        primaryInstitutionId: String,
        authorizedIdsCsv: String
    ) {
        viewModelScope.launch {
            repository.assignAuthorizedInstitutions(driverId, primaryInstitutionId, authorizedIdsCsv)
            _snackbarMessage.value = "Updated driver's authorized institutions."
        }
    }

    fun addState(name: String, code: String) {
        if (name.isBlank() || code.isBlank()) {
            _snackbarMessage.value = "State name and code are required."
            return
        }
        viewModelScope.launch {
            repository.addState(name, code)
            _snackbarMessage.value = "State '${name.trim()}' added to platform."
        }
    }

    fun savePlatformSettings(
        commissionPercent: Int,
        cancellationCutoffMinutes: Int,
        emergencyContact: String,
        fareSharingMode: FareSharingMode
    ) {
        viewModelScope.launch {
            repository.updatePlatformSettings(
                commissionPercent = commissionPercent,
                cancellationCutoffMinutes = cancellationCutoffMinutes,
                emergencyContact = emergencyContact,
                fareSharingMode = fareSharingMode
            )
            _snackbarMessage.value = "Platform-wide settings updated."
        }
    }

    // GPS Permission & Real Device Location Synchronization (Never falls back to UNIPORT)
    fun updateGpsPermissionStatus(granted: Boolean) {
        _hasGpsPermission.value = granted
        if (!granted) {
            _deviceGpsCoordinates.value = null
        }
    }

    @SuppressLint("MissingPermission")
    fun syncDeviceGpsLocation(context: Context) {
        val fineGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarseGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val hasPermission = fineGranted || coarseGranted
        _hasGpsPermission.value = hasPermission

        if (!hasPermission) {
            _deviceGpsCoordinates.value = null
            return
        }

        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        val lastKnown = locationManager?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            ?: locationManager?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)

        if (lastKnown != null && (lastKnown.latitude != 0.0 || lastKnown.longitude != 0.0)) {
            _deviceGpsCoordinates.value = lastKnown.latitude to lastKnown.longitude
            val activeDriver = currentUser.value
            if (_activeRole.value == UserRole.DRIVER && activeDriver != null) {
                viewModelScope.launch {
                    repository.updateDriverLiveGpsLocation(
                        driverId = activeDriver.id,
                        lat = lastKnown.latitude,
                        lng = lastKnown.longitude
                    )
                }
            }
        }
    }

    fun setUseCurrentGpsPickup(enabled: Boolean) {
        _useCurrentGpsPickup.value = enabled
        if (enabled) {
            val firstStop = institutionPickupLocations.value.firstOrNull()?.name
                ?: institutionServiceZones.value.firstOrNull()?.name?.removeSuffix(" Zone")?.trim()
                ?: "All"
            _selectedPickupFilter.value = firstStop
        }
    }

    fun refreshGpsLocation(context: Context? = null) {
        if (context != null) {
            syncDeviceGpsLocation(context)
        }
        _useCurrentGpsPickup.value = true
        val instName = currentInstitution.value?.shortCode ?: "Institution"
        val zoneName = gpsServiceZoneName.value
        val firstStop = institutionPickupLocations.value.firstOrNull()?.name
            ?: institutionServiceZones.value.firstOrNull()?.name?.removeSuffix(" Zone")?.trim()
            ?: "All"
        _selectedPickupFilter.value = firstStop
        if (_hasGpsPermission.value) {
            _snackbarMessage.value = "GPS Synced: $zoneName ($instName)."
        } else {
            _snackbarMessage.value = "Location permission unavailable • Using $instName campus network ($zoneName)."
        }
    }

    // Filter Actions
    fun setPickupFilter(pickup: String) {
        _selectedPickupFilter.value = pickup
        val firstStop = institutionPickupLocations.value.firstOrNull()?.name
        if (pickup != firstStop) {
            _useCurrentGpsPickup.value = false
        }
    }

    fun setDestinationFilter(destination: String) {
        _selectedDestinationFilter.value = destination
    }

    fun setCategoryFilter(category: VehicleCategory?) {
        _selectedCategoryFilter.value = category
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun clearFilters() {
        _selectedPickupFilter.value = "All"
        _selectedDestinationFilter.value = "All"
        _selectedCategoryFilter.value = null
        _searchQuery.value = ""
    }

    fun selectDriverTrip(tripId: String?) {
        _selectedDriverTripId.value = tripId
    }

    fun clearSnackbar() {
        _snackbarMessage.value = null
    }

    fun clearQrVerificationFeedback() {
        _qrVerificationResult.value = null
    }

    // Passenger Action: Book a seat on an eligible queued vehicle
    fun bookSeatOnTrip(
        tripId: String,
        pickupLocation: String,
        destinationLocation: String,
        seatsCount: Int,
        notes: String
    ) {
        val passenger = currentUser.value ?: return
        viewModelScope.launch {
            val result = repository.bookTrip(
                tripId = tripId,
                passenger = passenger,
                pickupLocation = pickupLocation,
                destinationLocation = destinationLocation,
                passengerLat = passengerLat.value,
                passengerLng = passengerLng.value,
                seatsCount = seatsCount,
                notes = notes
            )
            result.fold(
                onSuccess = { booking ->
                    _snackbarMessage.value =
                        "Seat Reserved! Booking ID: ${booking.bookingReference} • Code: ${booking.verificationCode} • Total: ₦${booking.totalFareNaira}"
                },
                onFailure = { error ->
                    _snackbarMessage.value = error.message ?: "Could not complete booking."
                }
            )
        }
    }

    // Passenger Action: Cancel booking
    fun cancelPassengerBooking(bookingId: String) {
        viewModelScope.launch {
            val result = repository.cancelBooking(bookingId)
            result.fold(
                onSuccess = { msg -> _snackbarMessage.value = msg },
                onFailure = { err -> _snackbarMessage.value = err.message ?: "Cancellation failed." }
            )
        }
    }

    // Passenger Action: Request Coordinated Campus Route
    fun submitCoordinatedRequest(
        pickupJunction: String,
        destination: String,
        passengerCount: Int,
        desiredTime: String,
        notes: String
    ) {
        val passenger = currentUser.value ?: return
        viewModelScope.launch {
            val result = repository.requestCoordinatedTrip(
                passenger = passenger,
                pickupJunction = pickupJunction,
                destination = destination,
                passengerCount = passengerCount,
                desiredDepartureTime = desiredTime,
                notes = notes
            )
            result.fold(
                onSuccess = {
                    val instShort = currentInstitution.value?.shortCode ?: "Campus"
                    _snackbarMessage.value = "Route request broadcast to verified $instShort drivers in your service zone!"
                },
                onFailure = { err ->
                    _snackbarMessage.value = err.message ?: "Could not submit route request."
                }
            )
        }
    }

    // Driver Action: Scan QR Code or Verify Booking ID / 4-digit Code (Strictly Institution-Scoped)
    fun verifyPassengerBookingCodeOrQr(inputCodeOrPayload: String) {
        val driver = currentUser.value ?: return
        viewModelScope.launch {
            val result = repository.scanAndVerifyPassengerQr(driver.id, inputCodeOrPayload)
            result.fold(
                onSuccess = { verifiedBooking ->
                    _qrVerificationResult.value = QrScanFeedback(
                        isSuccess = true,
                        title = "Passenger Verified & Boarded!",
                        message = "${verifiedBooking.passengerName} • Booking ${verifiedBooking.bookingReference} (${verifiedBooking.seatsCount} seat) confirmed for ${verifiedBooking.pickupLocation} → ${verifiedBooking.destinationLocation}.",
                        booking = verifiedBooking
                    )
                    _snackbarMessage.value = "Verified ${verifiedBooking.passengerName} (${verifiedBooking.bookingReference}) — Marked Boarded!"
                },
                onFailure = { error ->
                    _qrVerificationResult.value = QrScanFeedback(
                        isSuccess = false,
                        title = "Verification Rejected",
                        message = error.message ?: "Invalid QR or verification code.",
                        booking = null
                    )
                    _snackbarMessage.value = error.message ?: "Verification failed."
                }
            )
        }
    }

    // Driver Action: Update Passenger Boarding Status (Waiting, Boarded, Cancelled, Completed)
    fun updateBoardingStatus(bookingId: String, newBoardingStatus: String) {
        viewModelScope.launch {
            val result = repository.updatePassengerBoardingStatus(bookingId, newBoardingStatus)
            result.fold(
                onSuccess = {
                    _snackbarMessage.value = "Passenger status updated to '$newBoardingStatus'."
                },
                onFailure = { err ->
                    _snackbarMessage.value = err.message ?: "Failed to update boarding status."
                }
            )
        }
    }

    // Driver Action: Start Trip (enables Live GPS tracking & TRIP_ACTIVE queue state)
    fun startDriverTrip(tripId: String) {
        val driver = currentUser.value ?: return
        viewModelScope.launch {
            repository.startTrip(tripId, driver.id)
            _snackbarMessage.value = "Trip Started! Live GPS tracking enabled for boarded passengers."
        }
    }

    // Driver Action: Complete Trip (returns driver to BACK of their institution service zone queue)
    fun completeDriverTrip(tripId: String) {
        val driver = currentUser.value ?: return
        viewModelScope.launch {
            repository.completeTrip(tripId, driver.id)
            val instShort = currentInstitution.value?.shortCode ?: "Institution"
            _snackbarMessage.value = "Trip Completed! Re-entered at the BACK of the $instShort Service Zone Queue."
        }
    }

    // Driver Action: Toggle Online / Offline Status
    fun toggleDriverOnlineStatus(isOnline: Boolean) {
        val driver = currentUser.value ?: return
        viewModelScope.launch {
            repository.setDriverOnlineStatus(driver.id, isOnline)
            val instShort = currentInstitution.value?.shortCode ?: "Campus"
            _snackbarMessage.value = if (isOnline) {
                "You are now ONLINE and queued in your $instShort Service Zone."
            } else {
                "You are now OFFLINE."
            }
        }
    }

    // Driver Action: Update Queue State explicitly
    fun setDriverQueueState(status: DriverQueueStatus) {
        val driver = currentUser.value ?: return
        viewModelScope.launch {
            repository.updateDriverQueueState(driver.id, status)
            _snackbarMessage.value = "Queue status updated to: ${status.label}"
        }
    }

    // Driver Action: Accept a New Booking Request
    fun acceptDriverBooking(bookingId: String) {
        val driver = currentUser.value ?: return
        if (driver.role != UserRole.DRIVER) {
            _snackbarMessage.value = "Access Denied: Only authenticated drivers can accept bookings."
            return
        }
        viewModelScope.launch {
            val result = repository.acceptBookingRequest(driver.id, bookingId)
            result.fold(
                onSuccess = { accepted ->
                    _snackbarMessage.value = "Booking ${accepted.bookingReference} (${accepted.passengerName}) ACCEPTED! Proceed to ${accepted.pickupLocation}."
                },
                onFailure = { err ->
                    _snackbarMessage.value = err.message ?: "Unable to accept booking request."
                }
            )
        }
    }

    // Driver Action: Reject a New Booking Request (Refunds Passenger RydWallet Hold)
    fun rejectDriverBooking(bookingId: String, reason: String = "Driver unable to accept ride request") {
        val driver = currentUser.value ?: return
        if (driver.role != UserRole.DRIVER) {
            _snackbarMessage.value = "Access Denied: Only authenticated drivers can reject bookings."
            return
        }
        viewModelScope.launch {
            val result = repository.rejectBookingRequest(driver.id, bookingId, reason)
            result.fold(
                onSuccess = {
                    _snackbarMessage.value = "Ride request declined. Passenger RydWallet hold has been refunded."
                },
                onFailure = { err ->
                    _snackbarMessage.value = err.message ?: "Unable to reject booking request."
                }
            )
        }
    }

    // Driver Action: Mark Arrived at Passenger Pickup Location
    fun markArrivedAtPassengerPickup(bookingId: String) {
        val driver = currentUser.value ?: return
        if (driver.role != UserRole.DRIVER) {
            _snackbarMessage.value = "Access Denied: Only authenticated drivers can update arrival status."
            return
        }
        viewModelScope.launch {
            val result = repository.markDriverArrivedAtPickup(driver.id, bookingId)
            result.fold(
                onSuccess = {
                    _snackbarMessage.value = "Marked Arrived at Pickup! Ask passenger for QR or 4-digit code."
                },
                onFailure = { err ->
                    _snackbarMessage.value = err.message ?: "Failed to update arrival status."
                }
            )
        }
    }

    // Driver Action: Enter / Switch Service Zone & Route Direction (within driver's institution)
    fun switchDriverServiceZoneAndRoute(
        zoneId: String,
        zoneName: String,
        routeDirection: String
    ) {
        val driver = currentUser.value ?: return
        viewModelScope.launch {
            repository.enterServiceZoneQueue(
                driverId = driver.id,
                zoneId = zoneId,
                zoneName = zoneName,
                routeDirection = routeDirection
            )
            _snackbarMessage.value = "Joined $zoneName Queue ($routeDirection) with new timestamp."
        }
    }

    // Admin Action: Temporarily Skip / Unskip Driver in Queue due to GPS ETA threshold
    fun toggleSkipDriverInQueue(driverId: String, skipReason: String) {
        viewModelScope.launch {
            repository.toggleTemporarySkipDriver(driverId, skipReason)
            _snackbarMessage.value = if (skipReason.isBlank()) {
                "Driver restored to active queue eligibility."
            } else {
                "Driver temporarily skipped ($skipReason) — Queue timestamp preserved."
            }
        }
    }

    // Driver Action: Create new commercial trip
    fun createDriverTrip(
        vehicleCategory: VehicleCategory,
        vehicleModel: String,
        licensePlate: String,
        origin: String,
        destination: String,
        corridor: String,
        departureTime: String,
        departureDate: String,
        totalFareNaira: Int,
        totalCapacity: Int,
        pickupPoints: String,
        notes: String
    ) {
        val driver = currentUser.value ?: return
        if (driver.verificationStatus != DriverVerificationStatus.VERIFIED) {
            val instShort = currentInstitution.value?.shortCode ?: "Institution"
            _snackbarMessage.value = "Only $instShort-verified drivers can publish commercial trips."
            return
        }

        viewModelScope.launch {
            val result = repository.createTrip(
                driver = driver,
                vehicleCategory = vehicleCategory,
                vehicleModel = vehicleModel,
                licensePlate = licensePlate,
                origin = origin,
                destination = destination,
                corridor = corridor,
                departureTime = departureTime,
                departureDate = departureDate,
                totalFareNaira = totalFareNaira,
                totalCapacity = totalCapacity,
                pickupPoints = pickupPoints,
                notes = notes
            )
            result.fold(
                onSuccess = { trip ->
                    _snackbarMessage.value = "Trip ${trip.tripNumber} published to ${trip.serviceZoneName} queue!"
                },
                onFailure = { err ->
                    _snackbarMessage.value = err.message ?: "Failed to create trip."
                }
            )
        }
    }

    fun updateDriverTripStatus(tripId: String, newStatus: TripStatus) {
        val driver = currentUser.value ?: return
        viewModelScope.launch {
            when (newStatus) {
                TripStatus.IN_PROGRESS -> startDriverTrip(tripId)
                TripStatus.COMPLETED -> completeDriverTrip(tripId)
                else -> {
                    repository.allTrips
                    _snackbarMessage.value = "Trip status updated to ${newStatus.label}."
                }
            }
        }
    }

    // Admin Action: Add Geographic Service Zone (Geofence)
    fun addServiceZone(
        campusId: String,
        name: String,
        centerLat: Double,
        centerLng: Double,
        radiusMeters: Int,
        maxEtaMinutes: Int,
        supportedRoutes: String
    ) {
        if (name.isBlank() || supportedRoutes.isBlank()) {
            _snackbarMessage.value = "Zone name and supported routes are required."
            return
        }
        val activeInstitutionId = currentInstitution.value?.id ?: currentUser.value?.institutionId ?: return
        viewModelScope.launch {
            repository.addServiceZone(
                universityId = activeInstitutionId,
                campusId = campusId,
                name = name,
                centerLat = centerLat,
                centerLng = centerLng,
                radiusMeters = radiusMeters,
                maxEtaMinutes = maxEtaMinutes,
                supportedRoutes = supportedRoutes
            )
            _snackbarMessage.value = "Service Zone '$name' added to institution geofence network."
        }
    }

    fun deleteServiceZone(zoneId: String) {
        viewModelScope.launch {
            repository.deleteServiceZone(zoneId)
            _snackbarMessage.value = "Service Zone removed."
        }
    }

    // Admin Actions: Manage Institution Locations & Infrastructure
    fun addUniversity(
        name: String,
        shortCode: String,
        city: String,
        institutionType: InstitutionType = InstitutionType.UNIVERSITY,
        stateId: String = "state_rivers",
        stateName: String = "Rivers State"
    ) {
        if (name.isBlank() || shortCode.isBlank()) {
            _snackbarMessage.value = "Institution name and short code are required."
            return
        }
        viewModelScope.launch {
            repository.addUniversity(
                name = name,
                shortCode = shortCode,
                city = city,
                institutionType = institutionType,
                stateId = stateId,
                stateName = stateName
            )
            _snackbarMessage.value = "Institution '$shortCode' added."
        }
    }

    fun addCampus(universityId: String, name: String, description: String) {
        if (name.isBlank()) {
            _snackbarMessage.value = "Campus name is required."
            return
        }
        viewModelScope.launch {
            repository.addCampus(universityId, name, description)
            _snackbarMessage.value = "Campus '$name' added."
        }
    }

    fun addTransportPark(universityId: String, campusId: String, name: String, manager: String) {
        if (name.isBlank()) {
            _snackbarMessage.value = "Transport park name is required."
            return
        }
        viewModelScope.launch {
            repository.addTransportPark(universityId, campusId, name, manager)
            _snackbarMessage.value = "Transport Park '$name' registered."
        }
    }

    fun registerCampusVehicle(
        driverId: String,
        campusId: String,
        vehicleType: String,
        regNum: String,
        model: String,
        plate: String,
        capacity: Int
    ) {
        if (regNum.isBlank() || plate.isBlank()) {
            _snackbarMessage.value = "Registration number and license plate are required."
            return
        }
        val activeInstitutionId = currentInstitution.value?.id ?: currentUser.value?.institutionId ?: return
        viewModelScope.launch {
            repository.registerVehicle(
                driverId = driverId,
                universityId = activeInstitutionId,
                campusId = campusId,
                vehicleType = vehicleType,
                regNum = regNum,
                model = model,
                plate = plate,
                capacity = capacity
            )
            _snackbarMessage.value = "Vehicle '$regNum' added to institution fleet."
        }
    }

    fun addCampusRoute(
        campusId: String = "",
        origin: String,
        destination: String,
        corridor: String,
        fare: Int,
        pickupPoints: String,
        estMinutes: Int
    ) {
        if (origin.isBlank() || destination.isBlank()) {
            _snackbarMessage.value = "Origin and destination are required."
            return
        }
        val activeInstitutionId = currentInstitution.value?.id ?: currentUser.value?.institutionId ?: return
        val resolvedCampusId = campusId.ifBlank {
            currentCampus.value?.id ?: currentUser.value?.campusId ?: ""
        }
        viewModelScope.launch {
            repository.addRoute(
                universityId = activeInstitutionId,
                campusId = resolvedCampusId,
                origin = origin,
                destination = destination,
                corridor = corridor.ifBlank { "$origin → $destination" },
                fare = fare.coerceAtLeast(100),
                pickupPoints = pickupPoints.ifBlank { "$origin, $destination" },
                estMinutes = estMinutes.coerceAtLeast(3)
            )
            _snackbarMessage.value = "Campus route '$origin → $destination' added."
        }
    }

    fun addPickupPoint(
        campusId: String = "",
        name: String,
        corridor: String,
        landmark: String
    ) {
        if (name.isBlank()) {
            _snackbarMessage.value = "Pickup point name is required."
            return
        }
        val activeInstitutionId = currentInstitution.value?.id ?: currentUser.value?.institutionId ?: return
        val resolvedCampusId = campusId.ifBlank {
            currentCampus.value?.id ?: currentUser.value?.campusId ?: ""
        }
        viewModelScope.launch {
            repository.addPickupLocation(
                universityId = activeInstitutionId,
                campusId = resolvedCampusId,
                name = name,
                corridor = corridor.ifBlank { "Campus Corridor" },
                landmark = landmark.ifBlank { "Designated Shuttle Stand" }
            )
            _snackbarMessage.value = "Pickup location '$name' added."
        }
    }

    fun deletePickupPoint(id: String) {
        viewModelScope.launch {
            repository.deletePickupLocation(id)
            _snackbarMessage.value = "Pickup location removed."
        }
    }

    fun setDriverVerificationStatus(driverId: String, status: DriverVerificationStatus) {
        viewModelScope.launch {
            repository.updateDriverVerification(driverId, status)
            _snackbarMessage.value = "Driver verification updated to ${status.label}."
        }
    }

    fun resolveComplaintReport(reportId: String) {
        viewModelScope.launch {
            repository.resolveReport(reportId)
            _snackbarMessage.value = "Report marked as resolved."
        }
    }

    // Convenience / Delegate Methods for Passenger, Admin & Super Admin Screens
    fun setPassengerPickup(pickup: String) = setPickupFilter(pickup)

    fun setPassengerDestination(destination: String) = setDestinationFilter(destination)

    fun findRides() {
        val instCode = currentInstitution.value?.shortCode ?: "Campus"
        val count = filteredPassengerTrips.value.size
        _snackbarMessage.value = "Found $count active $instCode shuttle(s) matching your route."
    }

    fun confirmRide(tripId: String, pickup: String, dest: String) {
        bookSeatOnTrip(
            tripId = tripId,
            pickupLocation = pickup,
            destinationLocation = dest,
            seatsCount = 1,
            notes = ""
        )
    }

    fun registerNewPassengerWithInstitution(
        fullName: String,
        phoneNumber: String,
        email: String,
        institutionId: String,
        campusId: String?
    ) = registerPassengerWithInstitution(fullName, phoneNumber, email, institutionId, campusId)

    fun verifyDriver(driverId: String, approve: Boolean) {
        setDriverVerificationStatus(
            driverId,
            if (approve) DriverVerificationStatus.VERIFIED else DriverVerificationStatus.REJECTED
        )
    }

    fun suspendDriver(driverId: String) {
        setDriverVerificationStatus(driverId, DriverVerificationStatus.REJECTED)
    }

    fun toggleTemporarySkipDriver(driverId: String, reason: String) {
        toggleSkipDriverInQueue(driverId, reason)
    }

    fun addServiceZone(
        universityId: String,
        campusId: String,
        name: String,
        centerLat: Double,
        centerLng: Double,
        radiusMeters: Int,
        maxEtaMinutes: Int,
        supportedRoutes: String
    ) {
        if (name.isBlank() || supportedRoutes.isBlank()) {
            _snackbarMessage.value = "Zone name and supported routes are required."
            return
        }
        val resolvedUniId = universityId.ifBlank {
            currentInstitution.value?.id ?: currentUser.value?.institutionId ?: return
        }
        viewModelScope.launch {
            repository.addServiceZone(
                universityId = resolvedUniId,
                campusId = campusId,
                name = name,
                centerLat = centerLat,
                centerLng = centerLng,
                radiusMeters = radiusMeters,
                maxEtaMinutes = maxEtaMinutes,
                supportedRoutes = supportedRoutes
            )
            _snackbarMessage.value = "Service Zone '$name' added to institution geofence network."
        }
    }

    fun registerVehicle(
        driverId: String,
        universityId: String,
        campusId: String,
        vehicleType: String,
        regNum: String,
        model: String,
        plate: String,
        capacity: Int
    ) {
        val resolvedUniId = universityId.ifBlank {
            currentInstitution.value?.id ?: currentUser.value?.institutionId ?: return
        }
        viewModelScope.launch {
            repository.registerVehicle(
                driverId = driverId,
                universityId = resolvedUniId,
                campusId = campusId,
                vehicleType = vehicleType,
                regNum = regNum,
                model = model,
                plate = plate,
                capacity = capacity
            )
            _snackbarMessage.value = "Vehicle '$regNum' added to institution fleet."
        }
    }

    fun addRoute(
        universityId: String,
        campusId: String,
        origin: String,
        destination: String,
        corridor: String,
        fare: Int,
        pickupPoints: String,
        estMinutes: Int
    ) {
        val resolvedUniId = universityId.ifBlank {
            currentInstitution.value?.id ?: currentUser.value?.institutionId ?: return
        }
        viewModelScope.launch {
            repository.addRoute(
                universityId = resolvedUniId,
                campusId = campusId,
                origin = origin,
                destination = destination,
                corridor = corridor.ifBlank { "$origin → $destination" },
                fare = fare.coerceAtLeast(100),
                pickupPoints = pickupPoints.ifBlank { "$origin, $destination" },
                estMinutes = estMinutes.coerceAtLeast(3)
            )
            _snackbarMessage.value = "Campus route '$origin → $destination' added."
        }
    }

    fun addDesignatedPickupLocation(
        universityId: String,
        campusId: String,
        name: String,
        corridor: String,
        landmark: String
    ) {
        val resolvedUniId = universityId.ifBlank {
            currentInstitution.value?.id ?: currentUser.value?.institutionId ?: return
        }
        viewModelScope.launch {
            repository.addPickupLocation(
                universityId = resolvedUniId,
                campusId = campusId,
                name = name,
                corridor = corridor.ifBlank { "Campus Corridor" },
                landmark = landmark.ifBlank { "Designated Shuttle Stand" }
            )
            _snackbarMessage.value = "Pickup location '$name' added."
        }
    }

    fun deletePickupLocation(id: String) = deletePickupPoint(id)

    fun resolveReport(reportId: String) = resolveComplaintReport(reportId)

    fun saveInstitution(
        id: String?,
        name: String,
        shortCode: String,
        institutionType: InstitutionType,
        stateId: String,
        stateName: String,
        city: String,
        isActive: Boolean = true
    ) {
        val existing = if (id != null) allUniversities.value.find { it.id == id } else null
        saveInstitution(
            id = id,
            name = name,
            shortCode = shortCode,
            institutionType = institutionType,
            stateId = stateId,
            stateName = stateName,
            city = city,
            centerLat = existing?.centerLat ?: 0.0,
            centerLng = existing?.centerLng ?: 0.0,
            isActive = isActive
        )
    }

    fun toggleInstitutionActiveStatus(institutionId: String, isActive: Boolean) =
        toggleInstitutionStatus(institutionId, isActive)

    fun assignInstitutionAdmin(
        fullName: String,
        email: String,
        phone: String,
        institutionId: String,
        campusId: String
    ) = assignInstitutionAdministrator(fullName, email, phone, institutionId, campusId)

    fun updatePlatformSettings(
        commissionPercent: Int,
        cancellationCutoffMinutes: Int,
        emergencyContact: String,
        fareSharingMode: FareSharingMode
    ) = savePlatformSettings(commissionPercent, cancellationCutoffMinutes, emergencyContact, fareSharingMode)

    // =========================================================================
    // RYDWALLET, NOTIFICATIONS, COMPLAINTS & TECHNICAL SUPPORT ACTIONS
    // =========================================================================

    fun topUpPassengerWallet(amountNaira: Int, channelLabel: String = "Simulated Instant Bank Transfer") {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            val result = repository.topUpPassengerWallet(user.id, amountNaira, channelLabel)
            result.fold(
                onSuccess = { wallet ->
                    _snackbarMessage.value = "Added ₦$amountNaira to RydWallet! New Balance: ₦${wallet.availableBalanceNaira}."
                },
                onFailure = { err ->
                    _snackbarMessage.value = err.message ?: "Could not add funds to RydWallet."
                }
            )
        }
    }

    fun withdrawDriverEarnings(amountNaira: Int) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            val result = repository.withdrawDriverEarnings(user.id, amountNaira)
            result.fold(
                onSuccess = { wallet ->
                    _snackbarMessage.value = "Simulated withdrawal of ₦$amountNaira to ${wallet.bankName} (${wallet.accountNumberMasked}) completed!"
                },
                onFailure = { err ->
                    _snackbarMessage.value = err.message ?: "Withdrawal failed."
                }
            )
        }
    }

    fun updateDriverBankDetails(bankName: String, accountLast4: String, accountName: String) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            val result = repository.updateDriverBankSettlementDetails(user.id, bankName, accountLast4, accountName)
            result.fold(
                onSuccess = { wallet ->
                    _snackbarMessage.value = "Settlement bank updated to ${wallet.bankName} (${wallet.accountNumberMasked})."
                },
                onFailure = { err ->
                    _snackbarMessage.value = err.message ?: "Could not update settlement details."
                }
            )
        }
    }

    fun rateCompletedBooking(bookingId: String, rating: Int) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            val result = repository.rateBooking(bookingId, user.id, rating)
            result.fold(
                onSuccess = {
                    _snackbarMessage.value = "Thank you! Rated ride $rating/5 stars."
                },
                onFailure = { err ->
                    _snackbarMessage.value = err.message ?: "Could not submit rating."
                }
            )
        }
    }

    fun submitSupportOrTechnicalTicket(
        category: String,
        subject: String,
        description: String,
        isTechnicalProblem: Boolean = false,
        screenshotAttachmentName: String = "",
        tripId: String? = null,
        bookingId: String? = null,
        driverId: String? = null,
        passengerId: String? = null,
        routeOrTripRef: String = ""
    ) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            val result = repository.submitSupportOrTechnicalTicket(
                actor = user,
                category = category,
                subject = subject,
                description = description,
                isTechnicalProblem = isTechnicalProblem,
                screenshotAttachmentName = screenshotAttachmentName,
                tripId = tripId,
                bookingId = bookingId,
                driverId = driverId,
                passengerId = passengerId,
                routeOrTripRef = routeOrTripRef
            )
            result.fold(
                onSuccess = { ticket ->
                    _snackbarMessage.value = "Ticket submitted (${ticket.id.uppercase()}): ${ticket.category} [${ticket.status}]."
                },
                onFailure = { err ->
                    _snackbarMessage.value = err.message ?: "Could not submit support ticket."
                }
            )
        }
    }

    fun updateComplaintInvestigation(
        reportId: String,
        newStatus: String,
        adminNotes: String,
        escalateToSuperAdmin: Boolean = false
    ) {
        val admin = currentUser.value ?: return
        viewModelScope.launch {
            val result = repository.updateComplaintTicketInvestigation(
                adminUser = admin,
                reportId = reportId,
                newStatus = newStatus,
                adminNotes = adminNotes,
                escalateToSuperAdmin = escalateToSuperAdmin
            )
            result.fold(
                onSuccess = {
                    _snackbarMessage.value = "Ticket updated to $newStatus."
                },
                onFailure = { err ->
                    _snackbarMessage.value = err.message ?: "Failed to update ticket."
                }
            )
        }
    }

    fun addMoneyToPassengerWallet(amountNaira: Int, channelLabel: String = "Simulated Instant Bank Transfer") {
        topUpPassengerWallet(amountNaira, channelLabel)
    }

    fun withdrawDriverEarnings(
        amountNaira: Int,
        bankName: String,
        accountNumber: String,
        accountName: String
    ) {
        if (bankName.isNotBlank() && accountNumber.isNotBlank()) {
            updateDriverBankDetails(bankName, accountNumber.takeLast(4), accountName)
        }
        withdrawDriverEarnings(amountNaira)
    }

    fun rateCompletedBooking(bookingId: String, rating: Int, reviewText: String) {
        rateCompletedBooking(bookingId, rating)
    }

    fun submitSupportComplaint(
        category: String,
        subject: String,
        description: String,
        tripId: String = "",
        bookingId: String = "",
        reportedDriverId: String = "",
        reportedVehiclePlate: String = ""
    ) {
        submitSupportOrTechnicalTicket(
            category = category,
            subject = subject.ifBlank { category },
            description = description,
            isTechnicalProblem = false,
            tripId = tripId.ifBlank { null },
            bookingId = bookingId.ifBlank { null },
            driverId = reportedDriverId.ifBlank { null },
            routeOrTripRef = reportedVehiclePlate.ifBlank { bookingId.ifBlank { tripId } }
        )
    }

    fun submitSupportComplaint(
        issueType: String,
        details: String,
        bookingId: String = "",
        tripId: String = ""
    ) {
        submitSupportOrTechnicalTicket(
            category = issueType,
            subject = issueType,
            description = details,
            isTechnicalProblem = false,
            tripId = tripId.ifBlank { null },
            bookingId = bookingId.ifBlank { null },
            routeOrTripRef = bookingId.ifBlank { tripId }
        )
    }

    fun submitTechnicalProblemReport(
        issueCategory: String,
        description: String,
        tripId: String = "",
        bookingId: String = "",
        screenshotReference: String = ""
    ) {
        submitSupportOrTechnicalTicket(
            category = issueCategory,
            subject = "Technical Issue: $issueCategory",
            description = description,
            isTechnicalProblem = true,
            screenshotAttachmentName = screenshotReference,
            tripId = tripId.ifBlank { null },
            bookingId = bookingId.ifBlank { null },
            routeOrTripRef = issueCategory
        )
    }

    fun submitTechnicalProblem(
        issueType: String,
        screenSection: String,
        details: String,
        hasDiagnosticLog: Boolean = true
    ) {
        submitSupportOrTechnicalTicket(
            category = issueType,
            subject = "Technical Issue: $issueType ($screenSection)",
            description = details,
            isTechnicalProblem = true,
            screenshotAttachmentName = if (hasDiagnosticLog) "diagnostic_log_${System.currentTimeMillis()}.txt" else "",
            routeOrTripRef = screenSection
        )
    }

    fun updateReportStatus(
        reportId: String,
        status: com.example.data.model.ReportStatus,
        notes: String
    ) {
        updateComplaintInvestigation(
            reportId = reportId,
            newStatus = status.name,
            adminNotes = notes,
            escalateToSuperAdmin = status == com.example.data.model.ReportStatus.ESCALATED
        )
    }

    fun updateReportStatus(
        reportId: String,
        status: String,
        notes: String
    ) {
        updateComplaintInvestigation(
            reportId = reportId,
            newStatus = status,
            adminNotes = notes,
            escalateToSuperAdmin = status.equals("ESCALATED", ignoreCase = true)
        )
    }

    fun markNotificationRead(notificationId: String) {
        viewModelScope.launch {
            repository.markNotificationAsRead(notificationId)
        }
    }

    fun markAllNotificationsRead() {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            repository.markAllNotificationsRead(user.id)
            _snackbarMessage.value = "All notifications marked as read."
        }
    }
}

data class AdminAnalytics(
    val queuedDriversCount: Int = 0,
    val totalServiceZonesCount: Int = 0,
    val activeTripsCount: Int = 0,
    val completedTripsCount: Int = 0,
    val totalGmvNaira: Int = 0,
    val openReportsCount: Int = 0
)

enum class AuthOnboardingStep(val stepNumber: Int, val stepTitle: String) {
    WELCOME(0, "Welcome"),
    LOGIN(0, "Log In"),
    SIGN_UP_ACCOUNT(1, "Account"),
    ROLE_SELECTION(2, "Role"),
    INSTITUTION_SELECTION(3, "Institution"),
    CAMPUS_SELECTION(4, "Campus"),
    PROFILE_SETUP(5, "Profile"),
    AUTHENTICATED_DASHBOARD(6, "Dashboard")
}

private data class InstitutionCampusContext(
    val userId: String,
    val role: UserRole,
    val institutionId: String,
    val campusId: String
)

data class QrScanFeedback(
    val isSuccess: Boolean,
    val title: String,
    val message: String,
    val booking: BookingEntity?
)

class RydRoverViewModelFactory(
    private val application: Application
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RydRoverViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RydRoverViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
