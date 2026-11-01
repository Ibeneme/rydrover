package com.example.data.repository

import com.example.data.dao.BookingDao
import com.example.data.dao.CampusDao
import com.example.data.dao.CoordinatedRequestDao
import com.example.data.dao.DriverQueueDao
import com.example.data.dao.NotificationDao
import com.example.data.dao.PickupLocationDao
import com.example.data.dao.PlatformDao
import com.example.data.dao.RouteDao
import com.example.data.dao.ServiceZoneDao
import com.example.data.dao.StateDao
import com.example.data.dao.TransportParkDao
import com.example.data.dao.TripDao
import com.example.data.dao.UniversityDao
import com.example.data.dao.UserDao
import com.example.data.dao.VehicleDao
import com.example.data.dao.WalletDao
import com.example.data.database.AppDatabase
import com.example.data.model.AccountStatus
import com.example.data.model.BookingEntity
import com.example.data.model.BookingStatus
import com.example.data.model.CampusEntity
import com.example.data.model.ComplaintStatus
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
import com.example.data.model.PaymentEnvironmentMode
import com.example.data.model.PaymentStatus
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
import com.example.data.model.WalletTransactionType
import com.example.data.service.RouteCoordinationService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class RydRoverRepository(
    private val stateDao: StateDao,
    private val universityDao: UniversityDao,
    private val campusDao: CampusDao,
    private val transportParkDao: TransportParkDao,
    private val serviceZoneDao: ServiceZoneDao,
    private val driverQueueDao: DriverQueueDao,
    private val vehicleDao: VehicleDao,
    private val tripDao: TripDao,
    private val bookingDao: BookingDao,
    private val userDao: UserDao,
    private val routeDao: RouteDao,
    private val pickupLocationDao: PickupLocationDao,
    private val coordinatedRequestDao: CoordinatedRequestDao,
    private val platformDao: PlatformDao,
    private val walletDao: WalletDao,
    private val notificationDao: NotificationDao
) {
    constructor(database: AppDatabase) : this(
        stateDao = database.stateDao(),
        universityDao = database.universityDao(),
        campusDao = database.campusDao(),
        transportParkDao = database.transportParkDao(),
        serviceZoneDao = database.serviceZoneDao(),
        driverQueueDao = database.driverQueueDao(),
        vehicleDao = database.vehicleDao(),
        tripDao = database.tripDao(),
        bookingDao = database.bookingDao(),
        userDao = database.userDao(),
        routeDao = database.routeDao(),
        pickupLocationDao = database.pickupLocationDao(),
        coordinatedRequestDao = database.coordinatedRequestDao(),
        platformDao = database.platformDao(),
        walletDao = database.walletDao(),
        notificationDao = database.notificationDao()
    )
    // Platform-wide streams (used by Super Admin & global directory lookups)
    val allStates: Flow<List<StateEntity>> = stateDao.getAllStates()
    val allUniversities: Flow<List<UniversityEntity>> = universityDao.getAllUniversities()
    val allInstitutions: Flow<List<InstitutionEntity>> = universityDao.getAllUniversities()
    val allCampuses: Flow<List<CampusEntity>> = campusDao.getAllCampuses()
    val allParks: Flow<List<TransportParkEntity>> = transportParkDao.getAllParks()
    val allServiceZones: Flow<List<ServiceZoneEntity>> = serviceZoneDao.getAllServiceZones()
    val allQueueEntries: Flow<List<DriverQueueEntryEntity>> = driverQueueDao.getAllQueueEntries()
    val allVehicles: Flow<List<VehicleEntity>> = vehicleDao.getAllVehicles()
    val allTrips: Flow<List<TripEntity>> = tripDao.getAllTrips()
    val activeTrips: Flow<List<TripEntity>> = tripDao.getActiveTrips()
    val allBookings: Flow<List<BookingEntity>> = bookingDao.getAllBookings()
    val allUsers: Flow<List<UserEntity>> = userDao.getAllUsers()
    val allRoutes: Flow<List<RouteEntity>> = routeDao.getAllRoutes()
    val allPickupLocations: Flow<List<PickupLocationEntity>> = pickupLocationDao.getAllPickupLocations()
    val activePickupLocations: Flow<List<PickupLocationEntity>> = pickupLocationDao.getActivePickupLocations()
    val openCoordinatedRequests: Flow<List<CoordinatedRequestEntity>> = coordinatedRequestDao.getPendingRequests()
    val allCoordinatedRequests: Flow<List<CoordinatedRequestEntity>> = coordinatedRequestDao.getAllRequests()
    val platformSettings: Flow<PlatformSettingsEntity?> = platformDao.getSettings()
    val allReports: Flow<List<ReportEntity>> = platformDao.getAllReports()
    val allDriverProfiles: Flow<List<DriverProfileEntity>> = userDao.getAllDriverProfiles()

    // Database-level Institution & Campus Isolated Queries (Backend Data Isolation)
    fun getInstitutionById(institutionId: String): Flow<UniversityEntity?> =
        universityDao.getUniversityById(institutionId)

    fun getCampusesByInstitution(institutionId: String): Flow<List<CampusEntity>> =
        campusDao.getCampusesByUniversity(institutionId)

    fun getParksByInstitution(institutionId: String): Flow<List<TransportParkEntity>> =
        transportParkDao.getParksByInstitution(institutionId)

    fun getServiceZonesByInstitution(institutionId: String): Flow<List<ServiceZoneEntity>> =
        serviceZoneDao.getServiceZonesByInstitution(institutionId)

    fun getServiceZonesByInstitutionAndCampus(institutionId: String, campusId: String): Flow<List<ServiceZoneEntity>> =
        serviceZoneDao.getServiceZonesByInstitutionAndCampus(institutionId, campusId)

    fun getQueueEntriesByInstitution(institutionId: String): Flow<List<DriverQueueEntryEntity>> =
        driverQueueDao.getQueueEntriesByInstitution(institutionId)

    fun getQueueEntriesByInstitutionAndCampus(institutionId: String, campusId: String): Flow<List<DriverQueueEntryEntity>> =
        driverQueueDao.getQueueEntriesByInstitutionAndCampus(institutionId, campusId)

    fun getQueueEntryForDriverAndInstitution(driverId: String, institutionId: String): Flow<DriverQueueEntryEntity?> =
        driverQueueDao.getQueueEntryForDriverAndInstitution(driverId, institutionId)

    fun getTripsByInstitution(institutionId: String): Flow<List<TripEntity>> =
        tripDao.getTripsByInstitution(institutionId)

    fun getTripsByInstitutionAndCampus(institutionId: String, campusId: String): Flow<List<TripEntity>> =
        tripDao.getTripsByInstitutionAndCampus(institutionId, campusId)

    fun getTripsByDriverAndInstitution(driverId: String, institutionId: String): Flow<List<TripEntity>> =
        tripDao.getTripsByDriverAndInstitution(driverId, institutionId)

    fun getBookingsByInstitution(institutionId: String): Flow<List<BookingEntity>> =
        bookingDao.getBookingsByInstitution(institutionId)

    fun getBookingsByInstitutionAndCampus(institutionId: String, campusId: String): Flow<List<BookingEntity>> =
        bookingDao.getBookingsByInstitutionAndCampus(institutionId, campusId)

    fun getBookingsForPassengerAndInstitution(passengerId: String, institutionId: String): Flow<List<BookingEntity>> =
        bookingDao.getBookingsForPassengerAndInstitution(passengerId, institutionId)

    fun getRoutesByInstitution(institutionId: String): Flow<List<RouteEntity>> =
        routeDao.getRoutesByInstitution(institutionId)

    fun getRoutesByInstitutionAndCampus(institutionId: String, campusId: String): Flow<List<RouteEntity>> =
        routeDao.getRoutesByInstitutionAndCampus(institutionId, campusId)

    fun getPickupLocationsByInstitution(institutionId: String): Flow<List<PickupLocationEntity>> =
        pickupLocationDao.getPickupLocationsByInstitution(institutionId)

    fun getPickupLocationsByInstitutionAndCampus(institutionId: String, campusId: String): Flow<List<PickupLocationEntity>> =
        pickupLocationDao.getPickupLocationsByInstitutionAndCampus(institutionId, campusId)

    fun getVehiclesByInstitution(institutionId: String): Flow<List<VehicleEntity>> =
        vehicleDao.getVehiclesByInstitution(institutionId)

    fun getUsersByInstitution(institutionId: String): Flow<List<UserEntity>> =
        userDao.getUsersByInstitution(institutionId)

    fun getDriverProfilesByInstitution(institutionId: String): Flow<List<DriverProfileEntity>> =
        userDao.getDriverProfilesByInstitution(institutionId)

    fun getDriverProfileById(driverId: String): Flow<DriverProfileEntity?> =
        userDao.getDriverProfile(driverId)

    fun getPassengerProfileById(passengerId: String): Flow<PassengerProfileEntity?> =
        userDao.getPassengerProfile(passengerId)

    fun getUserById(userId: String): Flow<UserEntity?> =
        userDao.getUserById(userId)

    suspend fun getUserSnapshot(userId: String): UserEntity? =
        userDao.getUserByIdSuspend(userId)

    fun getRequestsByInstitution(institutionId: String): Flow<List<CoordinatedRequestEntity>> =
        coordinatedRequestDao.getRequestsByInstitution(institutionId)

    fun getReportsByInstitution(institutionId: String): Flow<List<ReportEntity>> =
        platformDao.getReportsByInstitution(institutionId)

    // RydWallet & Notification Streams (Strictly User-Scoped & Role-Scoped)
    fun getPassengerWallet(passengerId: String): Flow<PassengerWalletEntity?> =
        walletDao.getPassengerWallet(passengerId)

    fun getDriverWallet(driverId: String): Flow<DriverWalletEntity?> =
        walletDao.getDriverWallet(driverId)

    fun getWalletTransactionsForUser(userId: String): Flow<List<WalletTransactionEntity>> =
        walletDao.getTransactionsForUser(userId)

    fun getNotificationsForUser(userId: String): Flow<List<NotificationEntity>> =
        notificationDao.getNotificationsForUser(userId)

    fun getAdminNotificationsByInstitution(institutionId: String): Flow<List<NotificationEntity>> =
        notificationDao.getAdminNotificationsByInstitution(institutionId)

    // =========================================================================
    // BACKEND ROLE-BASED & INSTITUTION-BASED AUTHORIZATION GUARD
    // =========================================================================

    suspend fun verifyPortalAccessPermission(
        actorUserId: String,
        requiredRole: UserRole,
        targetInstitutionId: String? = null
    ): Result<UserEntity> {
        val actor = userDao.getUserByIdSuspend(actorUserId)
            ?: return Result.failure(SecurityException("ACCESS DENIED: Unauthenticated session."))

        if (actor.role != requiredRole && actor.role != UserRole.SUPER_ADMIN) {
            return Result.failure(
                SecurityException("ACCESS DENIED: ${actor.role.displayName} cannot access ${requiredRole.displayName} resources.")
            )
        }

        // Drivers cannot access passenger wallet or passenger routes even if attempted programmatically
        if (requiredRole == UserRole.PASSENGER && actor.role != UserRole.PASSENGER) {
            return Result.failure(
                SecurityException("ACCESS DENIED: Only authenticated Passenger accounts can access Passenger Portal or Passenger RydWallet.")
            )
        }
        if (requiredRole == UserRole.DRIVER && actor.role != UserRole.DRIVER) {
            return Result.failure(
                SecurityException("ACCESS DENIED: Only authenticated Driver accounts can access Driver Portal or Driver RydWallet.")
            )
        }

        // Enforce strict institution isolation for non-Super-Admin roles
        if (actor.role != UserRole.SUPER_ADMIN && !targetInstitutionId.isNullOrBlank()) {
            if (actor.institutionId != targetInstitutionId) {
                return Result.failure(
                    SecurityException("ACCESS DENIED: Cross-institution access between '${actor.institutionId}' and '$targetInstitutionId' is prohibited.")
                )
            }
        }
        return Result.success(actor)
    }

    private fun formatQueueTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("hh:mm a", Locale.US)
        return sdf.format(Date(timestamp))
    }

    // =========================================================================
    // AUTHENTICATION & STEP-BY-STEP REGISTRATION / ONBOARDING (Multi-Institution)
    // =========================================================================

    suspend fun authenticateUser(
        identifier: String,
        password: String
    ): Result<UserEntity> {
        val cleanId = identifier.trim()
        if (cleanId.isBlank()) {
            return Result.failure(IllegalArgumentException("Please enter your email address or phone number."))
        }
        if (password.isBlank()) {
            return Result.failure(IllegalArgumentException("Please enter your password."))
        }
        val user = userDao.findUserByPhoneOrEmail(cleanId)
            ?: return Result.failure(IllegalArgumentException("No RydRover account found for '$cleanId'. Please check your details or Create Account."))

        if (user.passwordHash.isNotBlank() && user.passwordHash != password) {
            return Result.failure(IllegalArgumentException("Incorrect password. Please try again."))
        }
        return Result.success(user)
    }

    // Step 1: Create initial account (Full name, Phone, Email, Password) — No role or institution yet
    suspend fun createInitialAccount(
        fullName: String,
        phoneNumber: String,
        email: String,
        password: String
    ): Result<UserEntity> {
        val cleanName = fullName.trim()
        val cleanPhone = phoneNumber.trim()
        val cleanEmail = email.trim()

        if (cleanName.length < 2) {
            return Result.failure(IllegalArgumentException("Please enter your full name."))
        }
        if (cleanPhone.length < 7) {
            return Result.failure(IllegalArgumentException("Please enter a valid phone number."))
        }
        if (password.length < 6) {
            return Result.failure(IllegalArgumentException("Password must be at least 6 characters."))
        }

        val existingByPhone = userDao.findUserByPhoneOrEmail(cleanPhone)
        if (existingByPhone != null) {
            return Result.failure(IllegalStateException("An account with this phone number already exists. Please Log In."))
        }
        if (cleanEmail.isNotBlank()) {
            val existingByEmail = userDao.findUserByPhoneOrEmail(cleanEmail)
            if (existingByEmail != null) {
                return Result.failure(IllegalStateException("An account with this email address already exists. Please Log In."))
            }
        }

        val newUserId = "usr_${UUID.randomUUID().toString().take(8)}"
        val newUser = UserEntity(
            id = newUserId,
            role = UserRole.PASSENGER, // placeholder until Step 2 (tracked via AccountStatus.ACCOUNT_CREATED)
            fullName = cleanName,
            phoneNumber = cleanPhone,
            email = cleanEmail.ifBlank { "${newUserId}@rydrover.user" },
            rating = 5.0f,
            verificationStatus = DriverVerificationStatus.VERIFIED,
            universityId = "",
            authorizedInstitutionIds = "",
            campusId = "",
            passwordHash = password,
            profileComplete = false,
            accountStatus = AccountStatus.ACCOUNT_CREATED
        )
        userDao.insertUser(newUser)
        return Result.success(newUser)
    }

    // Step 2: Role Selection (Strictly PASSENGER or DRIVER; ADMIN is prohibited during public sign-up)
    suspend fun selectAccountRole(
        userId: String,
        selectedRole: UserRole
    ): Result<UserEntity> {
        if (selectedRole == UserRole.ADMIN || selectedRole == UserRole.SUPER_ADMIN) {
            return Result.failure(SecurityException("Administrator accounts cannot be created through public registration."))
        }
        val user = userDao.getUserByIdSuspend(userId)
            ?: return Result.failure(IllegalStateException("Account not found."))

        userDao.updateUserRoleStep(userId, selectedRole, AccountStatus.ROLE_SELECTED)
        val updated = user.copy(
            role = selectedRole,
            accountStatus = AccountStatus.ROLE_SELECTED
        )
        return Result.success(updated)
    }

    // Step 3: Institution Selection (Loads campuses for selected institution; auto-selects if only 1 campus)
    suspend fun selectAccountInstitution(
        userId: String,
        institutionId: String
    ): Result<Pair<UserEntity, List<CampusEntity>>> {
        if (institutionId.isBlank()) {
            return Result.failure(IllegalArgumentException("Please select your institution."))
        }
        val institution = universityDao.getUniversityById(institutionId).firstOrNull()
            ?: return Result.failure(IllegalArgumentException("Selected institution does not exist."))

        val user = userDao.getUserByIdSuspend(userId)
            ?: return Result.failure(IllegalStateException("Account not found."))

        val campuses = campusDao.getCampusesByUniversity(institution.id).firstOrNull().orEmpty()
        if (campuses.size == 1) {
            val singleCampus = campuses.first()
            userDao.updateUserInstitutionStep(
                userId = userId,
                institutionId = institution.id,
                authorizedIds = institution.id,
                status = AccountStatus.CAMPUS_SELECTED
            )
            userDao.updateUserCampusStep(
                userId = userId,
                campusId = singleCampus.id,
                status = AccountStatus.CAMPUS_SELECTED
            )
            val updated = user.copy(
                universityId = institution.id,
                authorizedInstitutionIds = institution.id,
                campusId = singleCampus.id,
                accountStatus = AccountStatus.CAMPUS_SELECTED
            )
            return Result.success(updated to campuses)
        } else {
            userDao.updateUserInstitutionStep(
                userId = userId,
                institutionId = institution.id,
                authorizedIds = institution.id,
                status = AccountStatus.INSTITUTION_SELECTED
            )
            val updated = user.copy(
                universityId = institution.id,
                authorizedInstitutionIds = institution.id,
                campusId = "",
                accountStatus = AccountStatus.INSTITUTION_SELECTED
            )
            return Result.success(updated to campuses)
        }
    }

    // Step 4: Campus Selection (Verifies campus belongs strictly to the user's selected institution)
    suspend fun selectAccountCampus(
        userId: String,
        institutionId: String,
        campusId: String
    ): Result<UserEntity> {
        if (campusId.isBlank()) {
            return Result.failure(IllegalArgumentException("Please select your campus."))
        }
        val user = userDao.getUserByIdSuspend(userId)
            ?: return Result.failure(IllegalStateException("Account not found."))

        val campuses = campusDao.getCampusesByUniversity(institutionId).firstOrNull().orEmpty()
        val matchedCampus = campuses.find { it.id == campusId && it.universityId == institutionId }
            ?: return Result.failure(SecurityException("Selected campus does not belong to your institution."))

        userDao.updateUserCampusStep(
            userId = userId,
            campusId = matchedCampus.id,
            status = AccountStatus.CAMPUS_SELECTED
        )
        return Result.success(
            user.copy(
                universityId = institutionId,
                campusId = matchedCampus.id,
                accountStatus = AccountStatus.CAMPUS_SELECTED
            )
        )
    }

    // Step 5A: Complete Passenger / Student Profile Setup
    suspend fun completePassengerOnboarding(
        userId: String,
        fullName: String,
        phoneNumber: String,
        institutionId: String,
        campusId: String,
        studentIdOrMatric: String,
        departmentOrFaculty: String
    ): Result<UserEntity> {
        val user = userDao.getUserByIdSuspend(userId)
            ?: return Result.failure(IllegalStateException("Account not found."))

        if (user.role != UserRole.PASSENGER) {
            return Result.failure(SecurityException("Only Passenger accounts can complete passenger profile setup."))
        }
        if (institutionId.isBlank()) {
            return Result.failure(IllegalArgumentException("Institution selection is required."))
        }
        val campuses = campusDao.getCampusesByUniversity(institutionId).firstOrNull().orEmpty()
        val resolvedCampusId = campusId.ifBlank { campuses.firstOrNull()?.id.orEmpty() }

        val passengerProfile = PassengerProfileEntity(
            passengerId = userId,
            institutionId = institutionId,
            campusId = resolvedCampusId,
            studentIdOrMatric = studentIdOrMatric.trim(),
            departmentOrFaculty = departmentOrFaculty.trim()
        )
        userDao.insertPassengerProfile(passengerProfile)

        val cleanName = fullName.trim().ifBlank { user.fullName }
        val cleanPhone = phoneNumber.trim().ifBlank { user.phoneNumber }

        userDao.completeUserOnboarding(
            userId = userId,
            fullName = cleanName,
            phone = cleanPhone,
            institutionId = institutionId,
            campusId = resolvedCampusId,
            role = UserRole.PASSENGER,
            verificationStatus = DriverVerificationStatus.VERIFIED,
            profileComplete = true,
            accountStatus = AccountStatus.ACTIVE
        )

        val updatedUser = userDao.getUserByIdSuspend(userId) ?: user.copy(
            fullName = cleanName,
            phoneNumber = cleanPhone,
            universityId = institutionId,
            authorizedInstitutionIds = institutionId,
            campusId = resolvedCampusId,
            role = UserRole.PASSENGER,
            profileComplete = true,
            accountStatus = AccountStatus.ACTIVE
        )

        // Initialize Passenger RydWallet with ₦5,000 Demo Balance for immediate ride booking testing
        val initialWallet = PassengerWalletEntity(
            passengerId = userId,
            institutionId = institutionId,
            availableBalanceNaira = 5000,
            heldBalanceNaira = 0,
            totalAddedNaira = 5000,
            totalSpentNaira = 0,
            totalRefundedNaira = 0,
            paymentMode = PaymentEnvironmentMode.DEMO_SIMULATED
        )
        walletDao.upsertPassengerWallet(initialWallet)
        walletDao.insertTransaction(
            WalletTransactionEntity(
                id = "txn_${UUID.randomUUID().toString().take(8)}",
                userId = userId,
                userRole = UserRole.PASSENGER,
                institutionId = institutionId,
                type = WalletTransactionType.TOP_UP,
                amountNaira = 5000,
                paymentStatus = PaymentStatus.PAID,
                paymentMode = PaymentEnvironmentMode.DEMO_SIMULATED,
                providerReference = "SIM-WELCOME-${(10000..99999).random()}",
                title = "Welcome RydWallet Credit (Demo)",
                description = "Simulated ₦5,000 starter balance for campus shuttle rides"
            )
        )
        notificationDao.insertNotification(
            NotificationEntity(
                id = "notif_${UUID.randomUUID().toString().take(8)}",
                userId = userId,
                userRole = UserRole.PASSENGER,
                institutionId = institutionId,
                title = "Welcome to RydRover Passenger Portal",
                message = "Your Passenger RydWallet is active with ₦5,000 (Simulated Balance). Select a campus route to Book & Pay.",
                category = "WALLET"
            )
        )
        return Result.success(updatedUser)
    }

    // Step 5B: Complete Driver Registration & Vehicle Onboarding (Preserves Driver Approval Process)
    suspend fun completeDriverOnboarding(
        userId: String,
        fullName: String,
        phoneNumber: String,
        institutionId: String,
        campusId: String,
        licenseNumber: String,
        vehicleCategory: VehicleCategory,
        vehicleMakeModel: String,
        vehicleColor: String,
        licensePlate: String,
        registrationNumber: String,
        availableSeats: Int
    ): Result<UserEntity> {
        val user = userDao.getUserByIdSuspend(userId)
            ?: return Result.failure(IllegalStateException("Account not found."))

        if (user.role != UserRole.DRIVER) {
            return Result.failure(SecurityException("Only Driver accounts can complete commercial driver onboarding."))
        }
        if (institutionId.isBlank()) {
            return Result.failure(IllegalArgumentException("Institution selection is required."))
        }
        if (licenseNumber.isBlank() || vehicleMakeModel.isBlank() || licensePlate.isBlank()) {
            return Result.failure(IllegalArgumentException("Driver identification, vehicle model, and license plate are required."))
        }

        val institution = universityDao.getUniversityById(institutionId).firstOrNull()
        val campuses = campusDao.getCampusesByUniversity(institutionId).firstOrNull().orEmpty()
        val resolvedCampus = campuses.find { it.id == campusId } ?: campuses.firstOrNull()
        val resolvedCampusId = resolvedCampus?.id ?: campusId

        val zones = serviceZoneDao.getServiceZonesByInstitution(institutionId).firstOrNull().orEmpty()
        val campusZones = zones.filter { it.campusId == resolvedCampusId }.ifEmpty { zones }
        val defaultZone = campusZones.firstOrNull()

        val routes = routeDao.getRoutesByInstitution(institutionId).firstOrNull().orEmpty()
        val campusRoutes = routes.filter { it.campusId == resolvedCampusId }.ifEmpty { routes }
        val defaultRoute = campusRoutes.firstOrNull()
        val defaultRouteDirection = if (defaultRoute != null) {
            "${defaultRoute.originName} → ${defaultRoute.destinationName}"
        } else if (defaultZone != null) {
            val firstDest = defaultZone.supportedRoutes.split(",").firstOrNull()?.trim().orEmpty()
            val cleanOrigin = defaultZone.name.removeSuffix(" Zone").trim()
            if (firstDest.isNotBlank()) "$cleanOrigin → $firstDest" else cleanOrigin
        } else {
            "${institution?.shortCode ?: "Campus"} Transit Loop"
        }

        val targetLat = defaultZone?.centerLat ?: resolvedCampus?.centerLat ?: institution?.centerLat ?: 0.0
        val targetLng = defaultZone?.centerLng ?: resolvedCampus?.centerLng ?: institution?.centerLng ?: 0.0
        val targetZoneId = defaultZone?.id ?: ""
        val targetZoneName = defaultZone?.name ?: (resolvedCampus?.name ?: "${institution?.shortCode ?: "Institution"} Service Area")

        val cleanName = fullName.trim().ifBlank { user.fullName }
        val cleanPhone = phoneNumber.trim().ifBlank { user.phoneNumber }
        val cleanPlate = licensePlate.trim().uppercase()
        val cleanReg = registrationNumber.trim().uppercase().ifBlank {
            "${institution?.shortCode ?: "RYD"}-SH-${(10..99).random()}"
        }
        val seats = availableSeats.coerceIn(1, 30)

        // New drivers require Institution Administrator approval before unrestricted dispatch
        val initialVerification = DriverVerificationStatus.PENDING
        val driverProfile = DriverProfileEntity(
            driverId = userId,
            institutionId = institutionId,
            campusId = resolvedCampusId,
            authorizedInstitutionIds = institutionId,
            vehicleCategory = vehicleCategory,
            vehicleMakeModel = vehicleMakeModel.trim(),
            licensePlate = cleanPlate,
            licenseNumber = licenseNumber.trim().uppercase(),
            color = vehicleColor.trim().ifBlank { "White & Green" },
            registrationNumber = cleanReg,
            totalTrips = 0,
            completedTripsToday = 0,
            rating = 5.0f,
            isOnline = false,
            queueStatus = DriverQueueStatus.OFFLINE,
            currentServiceZoneId = targetZoneId,
            currentServiceZoneName = targetZoneName,
            currentRouteDirection = defaultRouteDirection,
            queueEntryTimestamp = System.currentTimeMillis(),
            currentLat = targetLat,
            currentLng = targetLng,
            verificationStatus = initialVerification
        )
        userDao.insertDriverProfile(driverProfile)

        val vehicle = VehicleEntity(
            id = "veh_${UUID.randomUUID().toString().take(6)}",
            driverId = userId,
            universityId = institutionId,
            campusId = resolvedCampusId,
            vehicleType = vehicleCategory.label,
            registrationNumber = cleanReg,
            makeModel = vehicleMakeModel.trim(),
            licensePlate = cleanPlate,
            capacity = seats,
            isAvailable = true,
            isVerified = false
        )
        vehicleDao.insertVehicle(vehicle)

        userDao.completeUserOnboarding(
            userId = userId,
            fullName = cleanName,
            phone = cleanPhone,
            institutionId = institutionId,
            campusId = resolvedCampusId,
            role = UserRole.DRIVER,
            verificationStatus = initialVerification,
            profileComplete = true,
            accountStatus = AccountStatus.DRIVER_APPROVAL_PENDING
        )

        val updatedUser = userDao.getUserByIdSuspend(userId) ?: user.copy(
            fullName = cleanName,
            phoneNumber = cleanPhone,
            universityId = institutionId,
            authorizedInstitutionIds = institutionId,
            campusId = resolvedCampusId,
            role = UserRole.DRIVER,
            verificationStatus = initialVerification,
            profileComplete = true,
            accountStatus = AccountStatus.DRIVER_APPROVAL_PENDING
        )

        // Initialize private Driver RydWallet
        val initialDriverWallet = DriverWalletEntity(
            driverId = userId,
            institutionId = institutionId,
            availableBalanceNaira = 0,
            pendingEarningsNaira = 0,
            completedTripEarningsNaira = 0,
            totalWithdrawnNaira = 0,
            bankName = "Access Bank PLC",
            accountNumberMasked = "**** ${(1000..9999).random()}",
            accountName = cleanName,
            paymentMode = PaymentEnvironmentMode.DEMO_SIMULATED
        )
        walletDao.upsertDriverWallet(initialDriverWallet)
        notificationDao.insertNotification(
            NotificationEntity(
                id = "notif_${UUID.randomUUID().toString().take(8)}",
                userId = userId,
                userRole = UserRole.DRIVER,
                institutionId = institutionId,
                title = "Driver RydWallet & Profile Created",
                message = "Your commercial driver profile and settlement wallet for ${institution?.shortCode ?: "your institution"} are ready. Pending Institution Admin verification.",
                category = "WALLET"
            )
        )
        return Result.success(updatedUser)
    }

    // Passenger Registration / Institution & Campus Selection ("What school do you attend?")
    suspend fun updatePassengerInstitutionAndCampus(
        passengerId: String,
        institutionId: String,
        campusId: String
    ) {
        userDao.updateUserInstitutionAndCampus(passengerId, institutionId, campusId)
    }

    suspend fun registerOrSwitchPassengerProfile(
        fullName: String,
        phoneNumber: String,
        email: String,
        institutionId: String,
        campusId: String
    ): UserEntity {
        val newPassenger = UserEntity(
            id = "usr_pass_${UUID.randomUUID().toString().take(6)}",
            role = UserRole.PASSENGER,
            fullName = fullName.trim().ifBlank { "Student Passenger" },
            phoneNumber = phoneNumber.trim().ifBlank { "+234 803 000 0000" },
            email = email.trim().ifBlank { "student@institution.edu.ng" },
            rating = 5.0f,
            universityId = institutionId,
            authorizedInstitutionIds = institutionId,
            campusId = campusId
        )
        userDao.insertUser(newPassenger)
        return newPassenger
    }

    // Driver: Switch active operating institution / campus (updates User, DriverProfile, and Queue context)
    suspend fun updateDriverActiveInstitutionAndCampus(
        driverId: String,
        institutionId: String,
        campusId: String
    ) {
        val campuses = campusDao.getCampusesByUniversity(institutionId).firstOrNull().orEmpty()
        val resolvedCampus = campuses.find { it.id == campusId } ?: campuses.firstOrNull()
        val resolvedCampusId = resolvedCampus?.id ?: campusId

        val zones = serviceZoneDao.getServiceZonesByInstitution(institutionId).firstOrNull().orEmpty()
        val campusZones = zones.filter { it.campusId == resolvedCampusId }.ifEmpty { zones }
        val defaultZone = campusZones.firstOrNull()

        val routes = routeDao.getRoutesByInstitution(institutionId).firstOrNull().orEmpty()
        val campusRoutes = routes.filter { it.campusId == resolvedCampusId }.ifEmpty { routes }
        val defaultRoute = campusRoutes.firstOrNull()
        val defaultRouteDirection = if (defaultRoute != null) {
            "${defaultRoute.originName} → ${defaultRoute.destinationName}"
        } else if (defaultZone != null) {
            val firstDest = defaultZone.supportedRoutes.split(",").firstOrNull()?.trim().orEmpty()
            val cleanOrigin = defaultZone.name.removeSuffix(" Zone").trim()
            if (firstDest.isNotBlank()) "$cleanOrigin → $firstDest" else cleanOrigin
        } else {
            "Campus Transit Loop"
        }

        val targetLat = defaultZone?.centerLat ?: resolvedCampus?.centerLat ?: 0.0
        val targetLng = defaultZone?.centerLng ?: resolvedCampus?.centerLng ?: 0.0
        val targetZoneId = defaultZone?.id ?: ""
        val targetZoneName = defaultZone?.name ?: (resolvedCampus?.name ?: "Institution Service Zone")

        userDao.updateUserInstitutionAndCampus(driverId, institutionId, resolvedCampusId)

        val now = System.currentTimeMillis()
        val formattedNow = formatQueueTime(now)

        val profile = userDao.getDriverProfile(driverId).firstOrNull()
        if (profile != null) {
            val existingAuth = profile.authorizedInstitutionIds
                .split(",")
                .map { it.trim() }
                .filter { it.isNotBlank() }
                .toMutableSet()
            existingAuth.add(institutionId)
            userDao.insertDriverProfile(
                profile.copy(
                    institutionId = institutionId,
                    campusId = resolvedCampusId,
                    authorizedInstitutionIds = existingAuth.joinToString(","),
                    currentServiceZoneId = targetZoneId,
                    currentServiceZoneName = targetZoneName,
                    currentRouteDirection = defaultRouteDirection,
                    currentLat = targetLat,
                    currentLng = targetLng,
                    queueEntryTimestamp = now
                )
            )
        }

        val queueEntry = driverQueueDao.getQueueEntryForDriverSuspend(driverId)
        if (queueEntry != null) {
            driverQueueDao.updateDriverZoneAndRoute(
                driverId = driverId,
                institutionId = institutionId,
                campusId = resolvedCampusId,
                zoneId = targetZoneId,
                zoneName = targetZoneName,
                routeDirection = defaultRouteDirection,
                lat = targetLat,
                lng = targetLng,
                newTimestamp = now,
                formattedTime = formattedNow,
                status = queueEntry.queueStatus
            )
        }
        recalculateQueuePriorities()
    }

    // Driver: Update live GPS coordinates for the logged-in driver
    suspend fun updateDriverLiveGpsLocation(driverId: String, lat: Double, lng: Double) {
        if (lat == 0.0 && lng == 0.0) return
        userDao.updateDriverCoordinates(driverId, lat, lng)
        val profile = userDao.getDriverProfile(driverId).firstOrNull() ?: return
        val driverTrips = tripDao.getTripsByDriverAndInstitution(driverId, profile.institutionId).firstOrNull().orEmpty()
        driverTrips.filter { it.status != TripStatus.COMPLETED && it.status != TripStatus.CANCELLED }.forEach { trip ->
            tripDao.updateTripLiveTracking(trip.id, trip.liveTrackingActive, lat, lng)
        }
    }

    // Admin / Super Admin: Assign authorized institutions to a driver or administrator
    suspend fun assignAuthorizedInstitutions(
        userId: String,
        primaryInstitutionId: String,
        authorizedIdsCsv: String
    ) {
        userDao.updateUserAuthorizedInstitutions(userId, primaryInstitutionId, authorizedIdsCsv)
        userDao.updateDriverAuthorizedInstitutions(userId, primaryInstitutionId, authorizedIdsCsv)
    }

    // Super Admin: Add / Edit / Deactivate Institution
    suspend fun saveInstitution(
        id: String?,
        name: String,
        shortCode: String,
        institutionType: InstitutionType,
        stateId: String,
        stateName: String,
        city: String,
        centerLat: Double = 4.7963,
        centerLng: Double = 6.9818,
        isActive: Boolean = true
    ): InstitutionEntity {
        val instId = id ?: "uni_${UUID.randomUUID().toString().take(6)}"
        val institution = UniversityEntity(
            id = instId,
            name = name.trim(),
            shortCode = shortCode.trim().uppercase(),
            stateOrCity = "${city.trim()}, ${stateName.trim()}",
            institutionType = institutionType,
            stateId = stateId,
            state = stateName.trim(),
            city = city.trim(),
            centerLat = centerLat,
            centerLng = centerLng,
            isActive = isActive
        )
        universityDao.insertUniversity(institution)

        val existingCampuses = campusDao.getAllCampuses().firstOrNull().orEmpty()
        if (existingCampuses.none { it.universityId == instId }) {
            val defaultCampus = CampusEntity(
                id = "camp_${UUID.randomUUID().toString().take(6)}",
                universityId = instId,
                name = "${shortCode.trim().uppercase()} Main Campus",
                description = "Main Campus & Central Transport Zone for ${name.trim()}",
                centerLat = centerLat,
                centerLng = centerLng,
                boundaryRadiusMeters = 2200
            )
            campusDao.insertCampus(defaultCampus)
        }
        return institution
    }

    suspend fun toggleInstitutionActiveStatus(institutionId: String, isActive: Boolean) {
        universityDao.updateInstitutionActiveStatus(institutionId, isActive)
    }

    // Super Admin: Assign or Create Institution Administrator
    suspend fun assignInstitutionAdmin(
        fullName: String,
        email: String,
        phone: String,
        institutionId: String,
        campusId: String
    ): UserEntity {
        val adminUser = UserEntity(
            id = "usr_admin_${UUID.randomUUID().toString().take(6)}",
            role = UserRole.ADMIN,
            fullName = fullName.trim(),
            phoneNumber = phone.trim().ifBlank { "+234 800 793 7683" },
            email = email.trim(),
            rating = 5.0f,
            universityId = institutionId,
            authorizedInstitutionIds = institutionId,
            campusId = campusId
        )
        userDao.insertUser(adminUser)
        return adminUser
    }

    // Super Admin: Manage States
    suspend fun addState(name: String, code: String) {
        val state = StateEntity(
            id = "state_${UUID.randomUUID().toString().take(6)}",
            name = name.trim(),
            code = code.trim().uppercase().take(3),
            isActive = true
        )
        stateDao.insertState(state)
    }

    // Super Admin: Save Platform-Wide Settings
    suspend fun updatePlatformSettings(
        commissionPercent: Int,
        cancellationCutoffMinutes: Int,
        emergencyContact: String,
        fareSharingMode: FareSharingMode
    ) {
        val current = platformDao.getSettings().firstOrNull() ?: PlatformSettingsEntity()
        platformDao.saveSettings(
            current.copy(
                platformCommissionPercent = commissionPercent.coerceIn(0, 30),
                cancellationCutoffMinutes = cancellationCutoffMinutes.coerceIn(5, 60),
                emergencyContact = emergencyContact.trim(),
                fareSharingMode = fareSharingMode
            )
        )
    }

    // Passenger: Book & Pay for an available seat using Passenger RydWallet
    suspend fun bookTrip(
        tripId: String,
        passenger: UserEntity,
        pickupLocation: String,
        destinationLocation: String,
        passengerLat: Double = 0.0,
        passengerLng: Double = 0.0,
        seatsCount: Int = 1,
        notes: String = ""
    ): Result<BookingEntity> {
        // Role-based security check: Only PASSENGER can book rides
        if (passenger.role != UserRole.PASSENGER) {
            return Result.failure(SecurityException("ACCESS DENIED: Only Passenger accounts can book rides."))
        }

        val trip = tripDao.getTripByIdSuspend(tripId)
            ?: return Result.failure(Exception("Trip not found."))

        // Verify strict institution network isolation
        if (trip.institutionId != passenger.institutionId) {
            return Result.failure(SecurityException("ACCESS DENIED: This vehicle operates under a different institution network."))
        }

        if (trip.status == TripStatus.COMPLETED || trip.status == TripStatus.CANCELLED) {
            return Result.failure(Exception("Trip is no longer accepting bookings (${trip.status.label})."))
        }

        if (trip.availableSeats < seatsCount) {
            return Result.failure(Exception("Only ${trip.availableSeats} seat(s) available in this vehicle."))
        }

        // Verify pickup compatibility (supports GPS current location and service zone stops)
        if (!RouteCoordinationService.isPickupCompatible(trip, pickupLocation)) {
            return Result.failure(Exception("Pickup location '$pickupLocation' is not served by this campus route."))
        }

        // Check duplicate active booking
        val existing = bookingDao.findExistingBooking(tripId, passenger.id)
        if (existing != null) {
            return Result.failure(Exception("You already have an active booking on this vehicle (${existing.bookingReference})."))
        }

        val newBookedSeatsCount = trip.bookedSeatsCount + seatsCount
        if (newBookedSeatsCount > trip.totalCapacity) {
            return Result.failure(Exception("Cannot exceed vehicle capacity of ${trip.totalCapacity} passengers."))
        }

        // Calculate fare per seat
        val farePerSeat = if (trip.fareSharingMode == FareSharingMode.FIXED_PER_SEAT) {
            (trip.totalFareNaira / trip.totalCapacity.coerceAtLeast(1)).coerceAtLeast(100)
        } else {
            RouteCoordinationService.calculateIndividualFare(
                totalTripFare = trip.totalFareNaira,
                activePassengers = newBookedSeatsCount,
                vehicleCapacity = trip.totalCapacity,
                mode = trip.fareSharingMode
            )
        }
        val totalFare = farePerSeat * seatsCount

        // Check Passenger RydWallet Balance & Reserve/Hold Fare
        val currentWallet = walletDao.getPassengerWalletSuspend(passenger.id) ?: PassengerWalletEntity(
            passengerId = passenger.id,
            institutionId = passenger.institutionId,
            availableBalanceNaira = 5000,
            heldBalanceNaira = 0,
            totalAddedNaira = 5000
        )
        if (currentWallet.availableBalanceNaira < totalFare) {
            return Result.failure(
                IllegalStateException("Insufficient RydWallet balance. Fare is ₦$totalFare, but your available RydWallet balance is ₦${currentWallet.availableBalanceNaira}. Please tap 'Add Money' to top up.")
            )
        }

        val currentTripBookings = bookingDao.getBookingsForTripAndInstitution(tripId, trip.institutionId).firstOrNull() ?: emptyList()
        val nextPassengerIndex = currentTripBookings.size + 1

        val resolvedPassengerLat = if (passengerLat != 0.0) passengerLat else trip.driverLat
        val resolvedPassengerLng = if (passengerLng != 0.0) passengerLng else trip.driverLng

        // Calculate GPS distance & ETA between assigned vehicle and passenger
        val distanceMeters = if (trip.driverLat != 0.0 && resolvedPassengerLat != 0.0) {
            RouteCoordinationService.calculateDistanceMeters(
                trip.driverLat,
                trip.driverLng,
                resolvedPassengerLat,
                resolvedPassengerLng
            )
        } else {
            250
        }
        val etaMinutes = RouteCoordinationService.estimateArrivalMinutes(distanceMeters)

        // Generate unique Booking ID, 4-digit verification code, Payment Ref, and QR payload
        val bookingRef = "RR-${(10000..99999).random()}"
        val verificationCode = "${(1000..9999).random()}"
        val paymentRef = "RYD-PAY-${(100000..999999).random()}"
        val qrPayload = "RYDROVER|$bookingRef|$verificationCode|$tripId|${passenger.id}"
        val bookingId = "bk_${UUID.randomUUID().toString().take(8)}"

        // 1. Reserve / Hold Fare in Passenger RydWallet
        val updatedPassengerWallet = currentWallet.copy(
            availableBalanceNaira = currentWallet.availableBalanceNaira - totalFare,
            heldBalanceNaira = currentWallet.heldBalanceNaira + totalFare,
            updatedAt = System.currentTimeMillis()
        )
        walletDao.upsertPassengerWallet(updatedPassengerWallet)
        walletDao.insertTransaction(
            WalletTransactionEntity(
                id = "txn_${UUID.randomUUID().toString().take(8)}",
                userId = passenger.id,
                userRole = UserRole.PASSENGER,
                institutionId = trip.institutionId,
                tripId = tripId,
                bookingId = bookingId,
                bookingReference = bookingRef,
                type = WalletTransactionType.RIDE_PAYMENT_HOLD,
                amountNaira = totalFare,
                paymentStatus = PaymentStatus.PAID,
                paymentMode = currentWallet.paymentMode,
                providerReference = paymentRef,
                title = "Ride Fare Paid & Held ($bookingRef)",
                description = "$pickupLocation → ${destinationLocation.ifBlank { trip.destination }} • ${trip.driverName} (${trip.vehicleRegistration})"
            )
        )

        // 2. Credit Driver RydWallet Pending Earnings (Net of platform commission)
        val commissionPercent = platformDao.getSettings().firstOrNull()?.platformCommissionPercent ?: 5
        val netDriverEarning = (totalFare * (100 - commissionPercent) / 100).coerceAtLeast(1)
        val currentDriverWallet = walletDao.getDriverWalletSuspend(trip.driverId) ?: DriverWalletEntity(
            driverId = trip.driverId,
            institutionId = trip.institutionId,
            accountName = trip.driverName
        )
        walletDao.upsertDriverWallet(
            currentDriverWallet.copy(
                pendingEarningsNaira = currentDriverWallet.pendingEarningsNaira + netDriverEarning,
                updatedAt = System.currentTimeMillis()
            )
        )
        walletDao.insertTransaction(
            WalletTransactionEntity(
                id = "txn_${UUID.randomUUID().toString().take(8)}",
                userId = trip.driverId,
                userRole = UserRole.DRIVER,
                institutionId = trip.institutionId,
                tripId = tripId,
                bookingId = bookingId,
                bookingReference = bookingRef,
                type = WalletTransactionType.DRIVER_EARNING_PENDING,
                amountNaira = netDriverEarning,
                paymentStatus = PaymentStatus.PENDING,
                paymentMode = currentDriverWallet.paymentMode,
                providerReference = paymentRef,
                title = "Pending Ride Earning ($bookingRef)",
                description = "Passenger ${passenger.fullName} paid ₦$totalFare (Net ₦$netDriverEarning held until trip completion)"
            )
        )

        // 3. Create Confirmed Paid Booking
        val booking = BookingEntity(
            id = bookingId,
            institutionId = trip.institutionId,
            campusId = trip.campusId,
            tripId = tripId,
            passengerId = passenger.id,
            passengerName = passenger.fullName,
            passengerPhone = passenger.phoneNumber,
            passengerIndex = nextPassengerIndex,
            pickupLocation = pickupLocation,
            destinationLocation = destinationLocation.ifBlank { trip.destination },
            seatsCount = seatsCount,
            farePerSeatNaira = farePerSeat,
            totalFareNaira = totalFare,
            bookingReference = bookingRef,
            verificationCode = verificationCode,
            qrCodePayload = qrPayload,
            isQrVerified = false,
            status = BookingStatus.CONFIRMED,
            boardingStatus = "Waiting",
            paymentStatus = PaymentStatus.PAID,
            paymentReference = paymentRef,
            paymentMode = currentWallet.paymentMode,
            refundStatus = "None",
            passengerLat = resolvedPassengerLat,
            passengerLng = resolvedPassengerLng,
            distanceMeters = distanceMeters,
            estimatedArrivalMinutes = etaMinutes,
            driverNotes = notes.ifBlank { "PAID via RydWallet • $distanceMeters m away" }
        )

        bookingDao.insertBooking(booking)

        // Update trip status: Open -> Partially Booked -> Fully Booked
        val updatedStatus = trip.computeStatusForSeats(newBookedSeatsCount)
        tripDao.updateBookedSeatsAndStatus(tripId, newBookedSeatsCount, updatedStatus)
        tripDao.updateTripLiveTracking(tripId, liveTracking = true, lat = trip.driverLat, lng = trip.driverLng)

        // Update driver queue state to BOOKED or EN_ROUTE_TO_PASSENGER
        val newDriverQueueStatus = if (updatedStatus == TripStatus.FULLY_BOOKED) {
            DriverQueueStatus.EN_ROUTE_TO_PASSENGER
        } else {
            DriverQueueStatus.BOOKED
        }
        userDao.updateDriverQueueStatus(trip.driverId, newDriverQueueStatus)
        driverQueueDao.updateQueueStatus(trip.driverId, newDriverQueueStatus)

        // 4. Emit Notifications for Passenger & Driver
        notificationDao.insertNotification(
            NotificationEntity(
                id = "notif_${UUID.randomUUID().toString().take(8)}",
                userId = passenger.id,
                userRole = UserRole.PASSENGER,
                institutionId = trip.institutionId,
                title = "Booking Confirmed & Paid ($bookingRef)",
                message = "Payment of ₦$totalFare is PAID via RydWallet. Driver ${trip.driverName} (${trip.vehicleRegistration}) is assigned. Verification Code: $verificationCode.",
                category = "PAYMENT",
                relatedBookingId = bookingId,
                relatedTripId = tripId
            )
        )
        notificationDao.insertNotification(
            NotificationEntity(
                id = "notif_${UUID.randomUUID().toString().take(8)}",
                userId = trip.driverId,
                userRole = UserRole.DRIVER,
                institutionId = trip.institutionId,
                title = "New Paid Booking ($bookingRef)",
                message = "${passenger.fullName} booked $seatsCount seat(s) from $pickupLocation to ${destinationLocation.ifBlank { trip.destination }}. Payment Status: PAID (₦$totalFare). Do not collect cash.",
                category = "BOOKING",
                relatedBookingId = bookingId,
                relatedTripId = tripId
            )
        )

        return Result.success(booking)
    }

    // Driver: Scan & Verify Passenger QR Code / Verification Code / Booking ID (Strictly Institution-Scoped)
    suspend fun scanAndVerifyPassengerQr(
        driverId: String,
        scannedInput: String
    ): Result<BookingEntity> {
        val cleaned = scannedInput.trim()
        if (cleaned.isBlank()) {
            return Result.failure(Exception("Please scan a valid QR code or enter Booking ID / Verification Code."))
        }

        val driverUser = userDao.getUserByIdSuspend(driverId)
        val driverProfile = userDao.getDriverProfile(driverId).firstOrNull()
        val driverInstitutionId = driverProfile?.institutionId ?: driverUser?.institutionId ?: ""
        if (driverInstitutionId.isBlank()) {
            return Result.failure(Exception("Driver institution profile not loaded."))
        }

        // Query ONLY bookings and trips belonging to the driver's institution
        val institutionBookings = bookingDao.getBookingsByInstitution(driverInstitutionId).firstOrNull() ?: emptyList()
        val driverTrips = tripDao.getTripsByDriverAndInstitution(driverId, driverInstitutionId).firstOrNull() ?: emptyList()
        val driverTripIds = driverTrips.map { it.id }.toSet()

        // Match by full QR payload, Booking Reference, or 4-digit Verification Code within driver's institution
        val matchedBooking = institutionBookings.find { b ->
            b.qrCodePayload.equals(cleaned, ignoreCase = true) ||
            b.bookingReference.equals(cleaned, ignoreCase = true) ||
            (b.verificationCode == cleaned && b.tripId in driverTripIds) ||
            (cleaned.contains(b.bookingReference, ignoreCase = true) && b.bookingReference.isNotBlank())
        } ?: institutionBookings.find { b ->
            b.verificationCode == cleaned
        }

        if (matchedBooking == null) {
            return Result.failure(Exception("Verification Failed: No booking found in your institution network matching '$cleaned'."))
        }

        // Enforce booking.institutionId == driver.institutionId
        if (matchedBooking.institutionId != driverInstitutionId) {
            return Result.failure(Exception("Access Denied: Passenger booking belongs to a different institution."))
        }

        val institutionTrips = tripDao.getTripsByInstitution(driverInstitutionId).firstOrNull() ?: emptyList()
        val assignedTrip = institutionTrips.find { it.id == matchedBooking.tripId }
            ?: return Result.failure(Exception("Verification Failed: Associated trip no longer exists in your institution."))

        if (assignedTrip.driverId != driverId) {
            return Result.failure(
                Exception("Wrong Vehicle: Booking ${matchedBooking.bookingReference} belongs to ${assignedTrip.vehicleRegistration} (${assignedTrip.driverName}).")
            )
        }

        if (matchedBooking.status == BookingStatus.CANCELLED || matchedBooking.status == BookingStatus.REJECTED) {
            return Result.failure(
                Exception("Invalid Booking: Booking ${matchedBooking.bookingReference} was ${matchedBooking.status.label}.")
            )
        }

        if (matchedBooking.status == BookingStatus.COMPLETED) {
            return Result.failure(
                Exception("Expired Booking: Booking ${matchedBooking.bookingReference} has already been completed.")
            )
        }

        if (matchedBooking.isQrVerified || matchedBooking.boardingStatus == "Boarded") {
            return Result.failure(
                Exception("Already Used: Booking ${matchedBooking.bookingReference} has already been verified and boarded.")
            )
        }

        if (matchedBooking.paymentStatus != PaymentStatus.PAID) {
            return Result.failure(
                Exception("Payment Not Authorized: Booking ${matchedBooking.bookingReference} payment status is ${matchedBooking.paymentStatus.label}.")
            )
        }

        // Validated! Mark QR verified and boardingStatus = "Boarded"
        bookingDao.confirmQrBoarding(matchedBooking.id)
        userDao.updateDriverQueueStatus(driverId, DriverQueueStatus.PASSENGER_BOARDING)
        driverQueueDao.updateQueueStatus(driverId, DriverQueueStatus.PASSENGER_BOARDING)

        notificationDao.insertNotification(
            NotificationEntity(
                id = "notif_${UUID.randomUUID().toString().take(8)}",
                userId = matchedBooking.passengerId,
                userRole = UserRole.PASSENGER,
                institutionId = matchedBooking.institutionId,
                title = "Boarding Verified (${matchedBooking.bookingReference})",
                message = "Your QR/verification code was verified by ${assignedTrip.driverName} (${assignedTrip.vehicleRegistration}). Enjoy your ride!",
                category = "TRIP",
                relatedBookingId = matchedBooking.id,
                relatedTripId = assignedTrip.id
            )
        )
        notificationDao.insertNotification(
            NotificationEntity(
                id = "notif_${UUID.randomUUID().toString().take(8)}",
                userId = driverId,
                userRole = UserRole.DRIVER,
                institutionId = matchedBooking.institutionId,
                title = "Passenger Verified (${matchedBooking.bookingReference})",
                message = "${matchedBooking.passengerName} is verified and authorized to board (${matchedBooking.paymentStatus.label}).",
                category = "TRIP",
                relatedBookingId = matchedBooking.id,
                relatedTripId = assignedTrip.id
            )
        )

        return Result.success(matchedBooking.copy(isQrVerified = true, boardingStatus = "Boarded"))
    }

    // Driver: Accept a New Booking Request (Strictly Institution/Campus/Zone Verified & Atomic)
    suspend fun acceptBookingRequest(
        driverId: String,
        bookingId: String
    ): Result<BookingEntity> {
        val permission = verifyPortalAccessPermission(driverId, UserRole.DRIVER)
        if (permission.isFailure) {
            return Result.failure(permission.exceptionOrNull() ?: SecurityException("ACCESS DENIED"))
        }
        val driverUser = permission.getOrThrow()
        val driverProfile = userDao.getDriverProfile(driverId).firstOrNull()
        val driverInstitutionId = driverProfile?.institutionId ?: driverUser.institutionId

        if (driverUser.verificationStatus == DriverVerificationStatus.PENDING ||
            driverUser.accountStatus == AccountStatus.DRIVER_APPROVAL_PENDING ||
            driverProfile?.verificationStatus == DriverVerificationStatus.PENDING
        ) {
            return Result.failure(IllegalStateException("PENDING APPROVAL: Your driver account awaits Institution Administrator verification before accepting bookings."))
        }

        if (driverProfile != null && !driverProfile.isOnline) {
            return Result.failure(IllegalStateException("You are currently OFFLINE. Go ONLINE first to accept ride requests."))
        }

        val booking = bookingDao.getBookingById(bookingId).firstOrNull()
            ?: return Result.failure(IllegalArgumentException("Booking request is no longer available."))

        // 1. Verify same institution & campus isolation
        if (booking.institutionId != driverInstitutionId) {
            return Result.failure(SecurityException("ACCESS DENIED: Booking belongs to a different institution."))
        }
        if (driverProfile != null && driverProfile.campusId.isNotBlank() && booking.campusId.isNotBlank() && booking.campusId != driverProfile.campusId) {
            return Result.failure(SecurityException("ACCESS DENIED: Booking belongs to a different campus zone."))
        }

        // 2. Verify booking is still available
        if (booking.status == BookingStatus.CANCELLED || booking.status == BookingStatus.REJECTED || booking.status == BookingStatus.COMPLETED) {
            return Result.failure(IllegalStateException("Booking ${booking.bookingReference} is no longer available (${booking.status.label})."))
        }

        // 3. Verify booking has not already been accepted by another driver
        val trip = tripDao.getTripByIdSuspend(booking.tripId)
        if (trip != null && trip.driverId.isNotBlank() && trip.driverId != driverId) {
            return Result.failure(IllegalStateException("Booking ${booking.bookingReference} has already been assigned to another driver (${trip.driverName})."))
        }

        // 4. Atomically mark booking as Accepted by this driver
        val updatedNotes = if (booking.driverNotes.contains("[ACCEPTED]")) {
            booking.driverNotes
        } else {
            "[ACCEPTED] ${booking.driverNotes}".trim()
        }
        val acceptedBooking = booking.copy(
            status = BookingStatus.CONFIRMED,
            boardingStatus = if (booking.isQrVerified || booking.boardingStatus == "Boarded") "Boarded" else "Accepted",
            driverNotes = updatedNotes
        )
        bookingDao.insertBooking(acceptedBooking)

        userDao.updateDriverQueueStatus(driverId, DriverQueueStatus.EN_ROUTE_TO_PASSENGER)
        driverQueueDao.updateQueueStatus(driverId, DriverQueueStatus.EN_ROUTE_TO_PASSENGER)

        notificationDao.insertNotification(
            NotificationEntity(
                id = "notif_${UUID.randomUUID().toString().take(8)}",
                userId = booking.passengerId,
                userRole = UserRole.PASSENGER,
                institutionId = booking.institutionId,
                title = "Driver Accepted Your Ride (${booking.bookingReference})",
                message = "${driverUser.fullName} (${driverProfile?.registrationNumber ?: trip?.vehicleRegistration ?: "Campus Shuttle"}) accepted your ride from ${booking.pickupLocation} to ${booking.destinationLocation}. Prepare your QR / code ${booking.verificationCode} for boarding.",
                category = "BOOKING",
                relatedBookingId = booking.id,
                relatedTripId = booking.tripId
            )
        )
        notificationDao.insertNotification(
            NotificationEntity(
                id = "notif_${UUID.randomUUID().toString().take(8)}",
                userId = driverId,
                userRole = UserRole.DRIVER,
                institutionId = booking.institutionId,
                title = "Booking Accepted (${booking.bookingReference})",
                message = "You accepted ${booking.passengerName}'s ride (${booking.pickupLocation} → ${booking.destinationLocation}). Proceed to pickup and verify QR / code ${booking.verificationCode}.",
                category = "BOOKING",
                relatedBookingId = booking.id,
                relatedTripId = booking.tripId
            )
        )

        return Result.success(acceptedBooking)
    }

    // Driver: Reject a New Booking Request (Releases Seat & Automatically Refunds Passenger RydWallet Hold)
    suspend fun rejectBookingRequest(
        driverId: String,
        bookingId: String,
        reason: String = "Driver unable to fulfill request"
    ): Result<Unit> {
        val permission = verifyPortalAccessPermission(driverId, UserRole.DRIVER)
        if (permission.isFailure) {
            return Result.failure(permission.exceptionOrNull() ?: SecurityException("ACCESS DENIED"))
        }
        val driverUser = permission.getOrThrow()
        val driverProfile = userDao.getDriverProfile(driverId).firstOrNull()
        val driverInstitutionId = driverProfile?.institutionId ?: driverUser.institutionId

        val booking = bookingDao.getBookingById(bookingId).firstOrNull()
            ?: return Result.failure(IllegalArgumentException("Booking request not found."))

        if (booking.institutionId != driverInstitutionId) {
            return Result.failure(SecurityException("ACCESS DENIED: Booking belongs to a different institution."))
        }
        if (booking.status == BookingStatus.COMPLETED || booking.isQrVerified || booking.boardingStatus == "Boarded") {
            return Result.failure(IllegalStateException("Cannot reject a booking after passenger boarding or trip completion."))
        }

        val now = System.currentTimeMillis()
        val rejectedBooking = booking.copy(
            status = BookingStatus.REJECTED,
            boardingStatus = "Rejected",
            paymentStatus = PaymentStatus.REFUNDED,
            refundStatus = "Refunded to RydWallet",
            driverNotes = "Rejected by driver: $reason"
        )
        bookingDao.insertBooking(rejectedBooking)

        // Release seat on the trip
        val trip = tripDao.getTripByIdSuspend(booking.tripId)
        if (trip != null) {
            val updatedSeats = (trip.bookedSeatsCount - booking.seatsCount).coerceAtLeast(0)
            tripDao.updateBookedSeatsAndStatus(trip.id, updatedSeats, trip.computeStatusForSeats(updatedSeats))
        }

        // Refund passenger's held RydWallet balance if fare was paid/held
        if (booking.paymentStatus == PaymentStatus.PAID || booking.paymentStatus == PaymentStatus.PENDING) {
            val passengerWallet = walletDao.getPassengerWalletSuspend(booking.passengerId)
            if (passengerWallet != null) {
                val refundAmount = booking.totalFareNaira
                walletDao.upsertPassengerWallet(
                    passengerWallet.copy(
                        availableBalanceNaira = passengerWallet.availableBalanceNaira + refundAmount,
                        heldBalanceNaira = (passengerWallet.heldBalanceNaira - refundAmount).coerceAtLeast(0),
                        totalRefundedNaira = passengerWallet.totalRefundedNaira + refundAmount,
                        updatedAt = now
                    )
                )
                walletDao.insertTransaction(
                    WalletTransactionEntity(
                        id = "txn_${UUID.randomUUID().toString().take(8)}",
                        userId = booking.passengerId,
                        userRole = UserRole.PASSENGER,
                        institutionId = booking.institutionId,
                        tripId = booking.tripId,
                        bookingId = booking.id,
                        bookingReference = booking.bookingReference,
                        type = WalletTransactionType.RIDE_REFUND,
                        amountNaira = refundAmount,
                        paymentStatus = PaymentStatus.REFUNDED,
                        paymentMode = passengerWallet.paymentMode,
                        providerReference = "REF-${booking.bookingReference}",
                        title = "Full Refund: Driver Declined (${booking.bookingReference})",
                        description = "₦$refundAmount returned to your available RydWallet balance.",
                        timestamp = now
                    )
                )
            }

            // Deduct pending escrow from driver wallet
            val commissionPercent = platformDao.getSettings().firstOrNull()?.platformCommissionPercent ?: 5
            val netDriverEarning = (booking.totalFareNaira * (100 - commissionPercent) / 100).coerceAtLeast(1)
            val driverWallet = walletDao.getDriverWalletSuspend(driverId)
            if (driverWallet != null) {
                walletDao.upsertDriverWallet(
                    driverWallet.copy(
                        pendingEarningsNaira = (driverWallet.pendingEarningsNaira - netDriverEarning).coerceAtLeast(0),
                        updatedAt = now
                    )
                )
            }
        }

        notificationDao.insertNotification(
            NotificationEntity(
                id = "notif_${UUID.randomUUID().toString().take(8)}",
                userId = booking.passengerId,
                userRole = UserRole.PASSENGER,
                institutionId = booking.institutionId,
                title = "Booking Declined & Refunded (${booking.bookingReference})",
                message = "Your booking ${booking.bookingReference} could not be accepted by the driver. ₦${booking.totalFareNaira} has been refunded to your RydWallet.",
                category = "PAYMENT",
                relatedBookingId = booking.id,
                relatedTripId = booking.tripId,
                timestamp = now
            )
        )

        return Result.success(Unit)
    }

    // Driver: Mark Arrived at Passenger Pickup Location
    suspend fun markDriverArrivedAtPickup(
        driverId: String,
        bookingId: String
    ): Result<Unit> {
        val booking = bookingDao.getBookingById(bookingId).firstOrNull()
            ?: return Result.failure(IllegalArgumentException("Booking not found."))
        val driverUser = userDao.getUserByIdSuspend(driverId)
        val driverProfile = userDao.getDriverProfile(driverId).firstOrNull()

        bookingDao.updateBoardingStatus(bookingId, "Arrived at Pickup")
        userDao.updateDriverQueueStatus(driverId, DriverQueueStatus.PASSENGER_BOARDING)
        driverQueueDao.updateQueueStatus(driverId, DriverQueueStatus.PASSENGER_BOARDING)

        notificationDao.insertNotification(
            NotificationEntity(
                id = "notif_${UUID.randomUUID().toString().take(8)}",
                userId = booking.passengerId,
                userRole = UserRole.PASSENGER,
                institutionId = booking.institutionId,
                title = "Driver Arrived at ${booking.pickupLocation} (${booking.bookingReference})",
                message = "${driverUser?.fullName ?: "Your driver"} (${driverProfile?.registrationNumber ?: "Shuttle"}) is waiting at ${booking.pickupLocation}. Show QR / code ${booking.verificationCode} to board.",
                category = "TRIP",
                relatedBookingId = booking.id,
                relatedTripId = booking.tripId
            )
        )
        return Result.success(Unit)
    }

    // Driver: Update passenger boarding status (Waiting, Accepted, Arrived at Pickup, Boarded, Cancelled, Completed)
    suspend fun updatePassengerBoardingStatus(bookingId: String, boardingStatus: String): Result<Unit> {
        val booking = bookingDao.getBookingById(bookingId).firstOrNull()
            ?: return Result.failure(Exception("Booking not found."))

        bookingDao.updateBoardingStatus(bookingId, boardingStatus)
        if (boardingStatus == "Boarded") {
            bookingDao.confirmQrBoarding(bookingId)
        } else if (boardingStatus == "Completed") {
            bookingDao.updateBookingStatus(bookingId, BookingStatus.COMPLETED)
        } else if (boardingStatus == "Cancelled") {
            bookingDao.updateBookingStatus(bookingId, BookingStatus.CANCELLED)
            val trip = tripDao.getTripByIdSuspend(booking.tripId)
            if (trip != null) {
                val updatedSeats = (trip.bookedSeatsCount - booking.seatsCount).coerceAtLeast(0)
                tripDao.updateBookedSeatsAndStatus(trip.id, updatedSeats, trip.computeStatusForSeats(updatedSeats))
            }
        }
        return Result.success(Unit)
    }

    // Driver: Start Trip with Live GPS location sharing enabled & TRIP_ACTIVE queue state
    suspend fun startTrip(tripId: String, driverId: String): Result<Unit> {
        val trip = tripDao.getTripByIdSuspend(tripId)
            ?: return Result.failure(Exception("Trip not found."))

        if (trip.driverId != driverId) {
            return Result.failure(SecurityException("ACCESS DENIED: You can only start trips assigned to your driver account."))
        }

        tripDao.updateTripStatus(tripId, TripStatus.IN_PROGRESS)
        tripDao.updateTripLiveTracking(tripId, liveTracking = true, lat = trip.driverLat, lng = trip.driverLng)
        userDao.updateDriverQueueStatus(driverId, DriverQueueStatus.TRIP_ACTIVE)
        driverQueueDao.updateQueueStatus(driverId, DriverQueueStatus.TRIP_ACTIVE)

        val tripBookings = bookingDao.getBookingsForTripAndInstitution(tripId, trip.institutionId).firstOrNull().orEmpty()
        tripBookings.filter { it.status == BookingStatus.CONFIRMED }.forEach { b ->
            notificationDao.insertNotification(
                NotificationEntity(
                    id = "notif_${UUID.randomUUID().toString().take(8)}",
                    userId = b.passengerId,
                    userRole = UserRole.PASSENGER,
                    institutionId = trip.institutionId,
                    title = "Trip Started (${trip.tripNumber})",
                    message = "Your shuttle ${trip.vehicleRegistration} (${trip.origin} → ${trip.destination}) is now in progress with live GPS tracking.",
                    category = "TRIP",
                    relatedBookingId = b.id,
                    relatedTripId = tripId
                )
            )
        }
        notificationDao.insertNotification(
            NotificationEntity(
                id = "notif_${UUID.randomUUID().toString().take(8)}",
                userId = driverId,
                userRole = UserRole.DRIVER,
                institutionId = trip.institutionId,
                title = "Trip Started (${trip.tripNumber})",
                message = "Trip ${trip.origin} → ${trip.destination} is now active.",
                category = "TRIP",
                relatedTripId = tripId
            )
        )
        return Result.success(Unit)
    }

    // Driver: Complete Trip -> Finalize Passenger Payments, Settle Driver Earnings & Return Driver to BACK of Institution Queue
    suspend fun completeTrip(tripId: String, driverId: String): Result<Unit> {
        val now = System.currentTimeMillis()
        val formattedNow = formatQueueTime(now)
        val trip = tripDao.getTripByIdSuspend(tripId)
            ?: return Result.failure(Exception("Trip not found."))

        if (trip.driverId != driverId) {
            return Result.failure(SecurityException("ACCESS DENIED: You can only complete trips assigned to your driver account."))
        }

        val profile = userDao.getDriverProfile(driverId).firstOrNull()
        val endLat = profile?.currentLat?.takeIf { it != 0.0 } ?: trip.driverLat
        val endLng = profile?.currentLng?.takeIf { it != 0.0 } ?: trip.driverLng

        // Finalize payments and settle driver earnings for all confirmed bookings on this trip
        val tripBookings = bookingDao.getBookingsForTripAndInstitution(tripId, trip.institutionId).firstOrNull().orEmpty()
        val activePaidBookings = tripBookings.filter { it.status == BookingStatus.CONFIRMED }
        val commissionPercent = platformDao.getSettings().firstOrNull()?.platformCommissionPercent ?: 5
        var totalNetDriverSettlement = 0

        for (booking in activePaidBookings) {
            val pWallet = walletDao.getPassengerWalletSuspend(booking.passengerId)
            if (pWallet != null) {
                val heldToRelease = booking.totalFareNaira.coerceAtMost(pWallet.heldBalanceNaira)
                walletDao.upsertPassengerWallet(
                    pWallet.copy(
                        heldBalanceNaira = (pWallet.heldBalanceNaira - heldToRelease).coerceAtLeast(0),
                        totalSpentNaira = pWallet.totalSpentNaira + booking.totalFareNaira,
                        updatedAt = now
                    )
                )
            }
            walletDao.insertTransaction(
                WalletTransactionEntity(
                    id = "txn_${UUID.randomUUID().toString().take(8)}",
                    userId = booking.passengerId,
                    userRole = UserRole.PASSENGER,
                    institutionId = trip.institutionId,
                    tripId = tripId,
                    bookingId = booking.id,
                    bookingReference = booking.bookingReference,
                    type = WalletTransactionType.RIDE_PAYMENT_SETTLED,
                    amountNaira = booking.totalFareNaira,
                    paymentStatus = PaymentStatus.PAID,
                    paymentMode = booking.paymentMode,
                    providerReference = booking.paymentReference,
                    title = "Trip Completed • Payment Finalized (${booking.bookingReference})",
                    description = "${booking.pickupLocation} → ${booking.destinationLocation} completed with ${trip.driverName}",
                    timestamp = now
                )
            )
            notificationDao.insertNotification(
                NotificationEntity(
                    id = "notif_${UUID.randomUUID().toString().take(8)}",
                    userId = booking.passengerId,
                    userRole = UserRole.PASSENGER,
                    institutionId = trip.institutionId,
                    title = "Trip Completed (${booking.bookingReference})",
                    message = "You have arrived at ${booking.destinationLocation}. Your ₦${booking.totalFareNaira} RydWallet payment has been finalized.",
                    category = "TRIP",
                    relatedBookingId = booking.id,
                    relatedTripId = tripId,
                    timestamp = now
                )
            )
            val netBookingEarning = (booking.totalFareNaira * (100 - commissionPercent) / 100).coerceAtLeast(1)
            totalNetDriverSettlement += netBookingEarning
        }

        if (totalNetDriverSettlement > 0) {
            val dWallet = walletDao.getDriverWalletSuspend(driverId) ?: DriverWalletEntity(
                driverId = driverId,
                institutionId = trip.institutionId,
                accountName = trip.driverName
            )
            val updatedPending = (dWallet.pendingEarningsNaira - totalNetDriverSettlement).coerceAtLeast(0)
            walletDao.upsertDriverWallet(
                dWallet.copy(
                    availableBalanceNaira = dWallet.availableBalanceNaira + totalNetDriverSettlement,
                    pendingEarningsNaira = updatedPending,
                    completedTripEarningsNaira = dWallet.completedTripEarningsNaira + totalNetDriverSettlement,
                    updatedAt = now
                )
            )
            walletDao.insertTransaction(
                WalletTransactionEntity(
                    id = "txn_${UUID.randomUUID().toString().take(8)}",
                    userId = driverId,
                    userRole = UserRole.DRIVER,
                    institutionId = trip.institutionId,
                    tripId = tripId,
                    type = WalletTransactionType.DRIVER_EARNING_SETTLED,
                    amountNaira = totalNetDriverSettlement,
                    paymentStatus = PaymentStatus.PAID,
                    paymentMode = dWallet.paymentMode,
                    providerReference = "SETTLE-${trip.tripNumber}",
                    title = "Trip Earnings Settled (${trip.tripNumber})",
                    description = "Net earnings of ₦$totalNetDriverSettlement moved from pending to withdrawable balance",
                    timestamp = now
                )
            )
            notificationDao.insertNotification(
                NotificationEntity(
                    id = "notif_${UUID.randomUUID().toString().take(8)}",
                    userId = driverId,
                    userRole = UserRole.DRIVER,
                    institutionId = trip.institutionId,
                    title = "Earnings Credited: ₦$totalNetDriverSettlement",
                    message = "Trip ${trip.tripNumber} completed! ₦$totalNetDriverSettlement is now available in your Driver RydWallet.",
                    category = "WALLET",
                    relatedTripId = tripId,
                    timestamp = now
                )
            )
        }

        tripDao.updateTripStatus(tripId, TripStatus.COMPLETED)
        tripDao.updateTripLiveTracking(tripId, liveTracking = false, lat = endLat, lng = endLng)
        bookingDao.completeBookingsForTrip(tripId)

        // Move driver to BACK of their institution queue with fresh timestamp
        userDao.incrementDriverCompletedTripsAndRequeue(
            driverId = driverId,
            newQueueTimestamp = now,
            newStatus = DriverQueueStatus.QUEUED
        )
        driverQueueDao.requeueDriverAtBack(
            driverId = driverId,
            newTimestamp = now,
            formattedTime = formattedNow,
            status = DriverQueueStatus.QUEUED
        )
        recalculateQueuePriorities()
        return Result.success(Unit)
    }

    // Driver: Go Online / Offline
    suspend fun setDriverOnlineStatus(driverId: String, isOnline: Boolean) {
        val now = System.currentTimeMillis()
        val formattedNow = formatQueueTime(now)
        val newQueueStatus = if (isOnline) DriverQueueStatus.QUEUED else DriverQueueStatus.OFFLINE
        userDao.updateDriverOnlineStatus(driverId, isOnline, newQueueStatus)
        tripDao.updateDriverTripsOnlineStatus(driverId, isOnline)
        if (isOnline) {
            driverQueueDao.requeueDriverAtBack(driverId, now, formattedNow, DriverQueueStatus.QUEUED)
        } else {
            driverQueueDao.updateQueueStatus(driverId, DriverQueueStatus.OFFLINE)
        }
        recalculateQueuePriorities()
    }

    // Driver: Update Queue State explicitly
    suspend fun updateDriverQueueState(driverId: String, newStatus: DriverQueueStatus) {
        val now = System.currentTimeMillis()
        val formattedNow = formatQueueTime(now)
        val isOnline = newStatus != DriverQueueStatus.OFFLINE
        userDao.updateDriverOnlineStatus(driverId, isOnline, newStatus)
        tripDao.updateDriverTripsOnlineStatus(driverId, isOnline)
        if (newStatus == DriverQueueStatus.QUEUED || newStatus == DriverQueueStatus.AVAILABLE) {
            driverQueueDao.requeueDriverAtBack(driverId, now, formattedNow, newStatus)
        } else {
            driverQueueDao.updateQueueStatus(driverId, newStatus)
        }
        recalculateQueuePriorities()
    }

    // Driver: Enter / Switch Service Zone & Route Direction within the driver's institution
    suspend fun enterServiceZoneQueue(
        driverId: String,
        zoneId: String,
        zoneName: String,
        routeDirection: String
    ) {
        val now = System.currentTimeMillis()
        val formattedNow = formatQueueTime(now)
        val driverUser = userDao.getUserByIdSuspend(driverId)
        val driverProfile = userDao.getDriverProfile(driverId).firstOrNull()
        val institutionId = driverProfile?.institutionId ?: driverUser?.institutionId ?: ""
        val zones = serviceZoneDao.getServiceZonesByInstitution(institutionId).firstOrNull().orEmpty()
        val targetZone = zones.find { it.id == zoneId }
        val campusId = targetZone?.campusId ?: driverProfile?.campusId ?: driverUser?.campusId ?: ""
        val targetLat = targetZone?.centerLat ?: driverProfile?.currentLat ?: 0.0
        val targetLng = targetZone?.centerLng ?: driverProfile?.currentLng ?: 0.0

        userDao.updateDriverZoneAndQueueTimestamp(
            driverId = driverId,
            institutionId = institutionId,
            campusId = campusId,
            zoneId = zoneId,
            zoneName = zoneName,
            routeDirection = routeDirection,
            lat = targetLat,
            lng = targetLng,
            timestamp = now,
            status = DriverQueueStatus.QUEUED
        )
        driverQueueDao.updateDriverZoneAndRoute(
            driverId = driverId,
            institutionId = institutionId,
            campusId = campusId,
            zoneId = zoneId,
            zoneName = zoneName,
            routeDirection = routeDirection,
            lat = targetLat,
            lng = targetLng,
            newTimestamp = now,
            formattedTime = formattedNow,
            status = DriverQueueStatus.QUEUED
        )
        recalculateQueuePriorities()
    }

    // Admin / Dispatch: Temporarily skip a driver who exceeds GPS ETA threshold without removing them from queue
    suspend fun toggleTemporarySkipDriver(driverId: String, skipReason: String) {
        driverQueueDao.updateTemporarySkipReason(driverId, skipReason)
        recalculateQueuePriorities()
    }

    // Recalculate 1-based FIFO Queue Ranks per (institutionId, serviceZoneId, routeDirection)
    private suspend fun recalculateQueuePriorities() {
        val entries = driverQueueDao.getAllQueueEntries().firstOrNull() ?: return
        val grouped = entries.groupBy { "${it.institutionId}|${it.serviceZoneId}|${it.routeDirection.lowercase()}" }
        for ((_, group) in grouped) {
            val sorted = group.sortedWith(
                compareBy<DriverQueueEntryEntity> { it.temporarilySkippedReason.isNotBlank() }
                    .thenBy { it.queueEntryTimestamp }
            )
            sorted.forEachIndexed { index, entry ->
                val rank = index + 1
                tripDao.updateDriverTripsQueueRank(
                    driverId = entry.driverId,
                    institutionId = entry.institutionId,
                    rank = rank,
                    timeFormatted = entry.entryTimeFormatted
                )
            }
        }
    }

    // Coordinated Route Request: when no suitable trip exists
    suspend fun requestCoordinatedTrip(
        passenger: UserEntity,
        pickupJunction: String,
        destination: String,
        passengerCount: Int,
        desiredDepartureTime: String,
        notes: String
    ): Result<CoordinatedRequestEntity> {
        if (pickupJunction.isBlank() || destination.isBlank()) {
            return Result.failure(Exception("Pickup location and destination are required."))
        }

        val request = CoordinatedRequestEntity(
            id = "req_${UUID.randomUUID().toString().take(8)}",
            institutionId = passenger.institutionId,
            campusId = passenger.campusId,
            passengerId = passenger.id,
            passengerName = passenger.fullName,
            passengerPhone = passenger.phoneNumber,
            pickupJunction = pickupJunction,
            destination = destination,
            passengersCount = passengerCount.coerceAtLeast(1),
            desiredDepartureTime = desiredDepartureTime,
            notes = notes,
            status = "PENDING_DRIVER"
        )
        coordinatedRequestDao.insertRequest(request)
        return Result.success(request)
    }

    // Cancel Booking & Issue Automatic Refund to Passenger RydWallet
    suspend fun cancelBooking(bookingId: String): Result<String> {
        val booking = bookingDao.getBookingById(bookingId).firstOrNull()
            ?: return Result.failure(Exception("Booking not found."))

        if (booking.status == BookingStatus.CANCELLED) {
            return Result.failure(Exception("Booking is already cancelled."))
        }

        val trip = tripDao.getTripByIdSuspend(booking.tripId)
        if (trip != null && (trip.status == TripStatus.IN_PROGRESS || trip.status == TripStatus.COMPLETED)) {
            return Result.failure(Exception("Cannot cancel booking. Trip has already commenced or completed."))
        }

        val now = System.currentTimeMillis()
        bookingDao.updateBookingStatus(bookingId, BookingStatus.CANCELLED)
        bookingDao.updateBoardingStatus(bookingId, "Cancelled")
        bookingDao.updateBookingPaymentAndRefund(bookingId, PaymentStatus.REFUNDED, "Refunded to RydWallet")

        // Refund reserved fare back to Passenger RydWallet
        val pWallet = walletDao.getPassengerWalletSuspend(booking.passengerId)
        if (pWallet != null) {
            val refundAmount = booking.totalFareNaira
            walletDao.upsertPassengerWallet(
                pWallet.copy(
                    availableBalanceNaira = pWallet.availableBalanceNaira + refundAmount,
                    heldBalanceNaira = (pWallet.heldBalanceNaira - refundAmount).coerceAtLeast(0),
                    totalRefundedNaira = pWallet.totalRefundedNaira + refundAmount,
                    updatedAt = now
                )
            )
            walletDao.insertTransaction(
                WalletTransactionEntity(
                    id = "txn_${UUID.randomUUID().toString().take(8)}",
                    userId = booking.passengerId,
                    userRole = UserRole.PASSENGER,
                    institutionId = booking.institutionId,
                    tripId = booking.tripId,
                    bookingId = booking.id,
                    bookingReference = booking.bookingReference,
                    type = WalletTransactionType.RIDE_REFUND,
                    amountNaira = refundAmount,
                    paymentStatus = PaymentStatus.REFUNDED,
                    paymentMode = booking.paymentMode,
                    providerReference = "REF-${booking.paymentReference}",
                    title = "Booking Refunded (${booking.bookingReference})",
                    description = "₦$refundAmount returned to your available RydWallet balance",
                    timestamp = now
                )
            )
        }

        if (trip != null) {
            val updatedSeats = (trip.bookedSeatsCount - booking.seatsCount).coerceAtLeast(0)
            val updatedStatus = trip.computeStatusForSeats(updatedSeats)
            tripDao.updateBookedSeatsAndStatus(trip.id, updatedSeats, updatedStatus)

            // Adjust driver's pending earnings
            val commissionPercent = platformDao.getSettings().firstOrNull()?.platformCommissionPercent ?: 5
            val netDriverEarning = (booking.totalFareNaira * (100 - commissionPercent) / 100).coerceAtLeast(1)
            val dWallet = walletDao.getDriverWalletSuspend(trip.driverId)
            if (dWallet != null) {
                walletDao.upsertDriverWallet(
                    dWallet.copy(
                        pendingEarningsNaira = (dWallet.pendingEarningsNaira - netDriverEarning).coerceAtLeast(0),
                        updatedAt = now
                    )
                )
            }

            notificationDao.insertNotification(
                NotificationEntity(
                    id = "notif_${UUID.randomUUID().toString().take(8)}",
                    userId = trip.driverId,
                    userRole = UserRole.DRIVER,
                    institutionId = trip.institutionId,
                    title = "Booking Cancelled (${booking.bookingReference})",
                    message = "Passenger ${booking.passengerName} cancelled booking ${booking.bookingReference}. Seat released back to vehicle capacity.",
                    category = "BOOKING",
                    relatedBookingId = booking.id,
                    relatedTripId = trip.id,
                    timestamp = now
                )
            )
        }

        notificationDao.insertNotification(
            NotificationEntity(
                id = "notif_${UUID.randomUUID().toString().take(8)}",
                userId = booking.passengerId,
                userRole = UserRole.PASSENGER,
                institutionId = booking.institutionId,
                title = "Refund Issued: ₦${booking.totalFareNaira} (${booking.bookingReference})",
                message = "Your cancelled booking ${booking.bookingReference} has been refunded to your Passenger RydWallet.",
                category = "PAYMENT",
                relatedBookingId = booking.id,
                relatedTripId = booking.tripId,
                timestamp = now
            )
        )

        return Result.success("Booking ${booking.bookingReference} cancelled and ₦${booking.totalFareNaira} refunded to RydWallet.")
    }

    // Driver: Create commercial trip in the driver's institution & campus service zone queue
    suspend fun createTrip(
        driver: UserEntity,
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
    ): Result<TripEntity> {
        if (origin.isBlank() || destination.isBlank()) {
            return Result.failure(Exception("Pickup point and Destination are required."))
        }
        if (totalCapacity <= 0 || totalCapacity > 30) {
            return Result.failure(Exception("Vehicle capacity must be between 1 and 30."))
        }
        if (totalFareNaira < 100) {
            return Result.failure(Exception("Total fare must be at least ₦100."))
        }

        val now = System.currentTimeMillis()
        val formattedTime = formatQueueTime(now)
        val routeDir = "${origin.trim()} → ${destination.trim()}"
        val institution = universityDao.getUniversityById(driver.institutionId).firstOrNull()
        val instShort = institution?.shortCode?.split("/")?.firstOrNull()?.trim() ?: "INST"
        val tripNumber = "RYD-$instShort-${(100..999).random()}"

        // Resolve institution-specific park, service zone, and coordinates (NEVER hardcode UNIPORT)
        val driverProfile = userDao.getDriverProfile(driver.id).firstOrNull()
        val institutionParks = transportParkDao.getParksByInstitution(driver.institutionId).firstOrNull().orEmpty()
        val campusPark = institutionParks.find { it.campusId == driver.campusId } ?: institutionParks.firstOrNull()
        val resolvedParkId = campusPark?.id ?: "park_${driver.institutionId}"

        val institutionZones = serviceZoneDao.getServiceZonesByInstitution(driver.institutionId).firstOrNull().orEmpty()
        val matchedZone = institutionZones.find {
            it.id == driverProfile?.currentServiceZoneId ||
            it.name.contains(origin.trim(), ignoreCase = true)
        } ?: institutionZones.find { it.campusId == driver.campusId } ?: institutionZones.firstOrNull()

        val resolvedZoneId = matchedZone?.id ?: driverProfile?.currentServiceZoneId ?: "zone_${driver.institutionId}"
        val resolvedZoneName = matchedZone?.name ?: driverProfile?.currentServiceZoneName ?: "${origin.trim()} Zone"

        val institutionCampuses = campusDao.getCampusesByUniversity(driver.institutionId).firstOrNull().orEmpty()
        val activeCampus = institutionCampuses.find { it.id == driver.campusId } ?: institutionCampuses.firstOrNull()

        val resolvedLat = driverProfile?.currentLat?.takeIf { it != 0.0 }
            ?: matchedZone?.centerLat?.takeIf { it != 0.0 }
            ?: activeCampus?.centerLat?.takeIf { it != 0.0 }
            ?: institution?.centerLat ?: 0.0
        val resolvedLng = driverProfile?.currentLng?.takeIf { it != 0.0 }
            ?: matchedZone?.centerLng?.takeIf { it != 0.0 }
            ?: activeCampus?.centerLng?.takeIf { it != 0.0 }
            ?: institution?.centerLng ?: 0.0

        val trip = TripEntity(
            id = "trip_${UUID.randomUUID().toString().take(8)}",
            tripNumber = tripNumber,
            universityId = driver.institutionId,
            campusId = driver.campusId,
            parkId = resolvedParkId,
            serviceZoneId = resolvedZoneId,
            serviceZoneName = resolvedZoneName,
            driverId = driver.id,
            driverName = driver.fullName,
            driverPhone = driver.phoneNumber,
            driverRating = driver.rating,
            vehicleCategory = vehicleCategory,
            vehicleModel = vehicleModel.ifBlank { "${vehicleCategory.label} ($licensePlate)" },
            licensePlate = licensePlate.ifBlank { "RIV-CAMPUS" },
            vehicleRegistration = driverProfile?.registrationNumber?.ifBlank { null } ?: licensePlate.ifBlank { "$instShort-SH-05" },
            origin = origin.trim(),
            destination = destination.trim(),
            corridor = corridor.ifBlank { routeDir },
            departureTime = departureTime,
            departureDate = departureDate.ifBlank { "Today" },
            totalFareNaira = totalFareNaira,
            totalCapacity = totalCapacity,
            bookedSeatsCount = 0,
            pickupPoints = pickupPoints.ifBlank { "${origin.trim()}, ${destination.trim()}" },
            fareSharingMode = FareSharingMode.FIXED_PER_SEAT,
            status = TripStatus.OPEN_FOR_BOOKINGS,
            notes = notes.trim().ifBlank { "Queued at $formattedTime • Serving $routeDir" },
            isOnline = true,
            liveTrackingActive = true,
            driverLat = resolvedLat,
            driverLng = resolvedLng,
            queuePriorityRank = 1,
            entryTimeFormatted = formattedTime
        )

        tripDao.insertTrip(trip)
        recalculateQueuePriorities()
        return Result.success(trip)
    }

    // Admin: Manage Geographic Service Zones (Geofences)
    suspend fun addServiceZone(
        universityId: String,
        campusId: String,
        name: String,
        centerLat: Double,
        centerLng: Double,
        radiusMeters: Int,
        maxEtaMinutes: Int,
        supportedRoutes: String
    ) {
        val zone = ServiceZoneEntity(
            id = "zone_${UUID.randomUUID().toString().take(6)}",
            universityId = universityId,
            campusId = campusId,
            name = if (name.endsWith("Zone", ignoreCase = true)) name.trim() else "${name.trim()} Zone",
            centerLat = centerLat,
            centerLng = centerLng,
            radiusMeters = radiusMeters.coerceIn(100, 5000),
            maxEtaMinutesThreshold = maxEtaMinutes.coerceIn(2, 30),
            supportedRoutes = supportedRoutes.trim()
        )
        serviceZoneDao.insertServiceZone(zone)
    }

    suspend fun deleteServiceZone(zoneId: String) {
        serviceZoneDao.deleteServiceZone(zoneId)
    }

    // Admin / Super Admin: Manage Institution Locations & Infrastructure
    suspend fun addUniversity(
        name: String,
        shortCode: String,
        city: String,
        institutionType: InstitutionType = InstitutionType.UNIVERSITY,
        stateId: String = "state_rivers",
        stateName: String = "Rivers State"
    ) {
        saveInstitution(
            id = null,
            name = name,
            shortCode = shortCode,
            institutionType = institutionType,
            stateId = stateId,
            stateName = stateName,
            city = city
        )
    }

    suspend fun addCampus(universityId: String, name: String, description: String) {
        val institution = universityDao.getUniversityById(universityId).firstOrNull()
        val camp = CampusEntity(
            id = "camp_${UUID.randomUUID().toString().take(6)}",
            universityId = universityId,
            name = name.trim(),
            description = description.trim(),
            centerLat = institution?.centerLat ?: 0.0,
            centerLng = institution?.centerLng ?: 0.0
        )
        campusDao.insertCampus(camp)
    }

    suspend fun addTransportPark(universityId: String, campusId: String, name: String, manager: String) {
        val park = TransportParkEntity(
            id = "park_${UUID.randomUUID().toString().take(6)}",
            universityId = universityId,
            campusId = campusId,
            name = name.trim(),
            parkManager = manager.trim()
        )
        transportParkDao.insertPark(park)
    }

    suspend fun registerVehicle(
        driverId: String,
        universityId: String,
        campusId: String,
        vehicleType: String,
        regNum: String,
        model: String,
        plate: String,
        capacity: Int
    ) {
        val vehicle = VehicleEntity(
            id = "veh_${UUID.randomUUID().toString().take(6)}",
            driverId = driverId,
            universityId = universityId,
            campusId = campusId,
            vehicleType = vehicleType.trim(),
            registrationNumber = regNum.trim(),
            makeModel = model.trim(),
            licensePlate = plate.trim(),
            capacity = capacity
        )
        vehicleDao.insertVehicle(vehicle)
    }

    suspend fun addRoute(
        universityId: String,
        campusId: String,
        origin: String,
        destination: String,
        corridor: String,
        fare: Int,
        pickupPoints: String,
        estMinutes: Int
    ) {
        val parks = transportParkDao.getParksByInstitution(universityId).firstOrNull().orEmpty()
        val defaultParkId = parks.find { it.campusId == campusId }?.id ?: parks.firstOrNull()?.id ?: "park_$universityId"
        val route = RouteEntity(
            id = "rte_${UUID.randomUUID().toString().take(6)}",
            universityId = universityId,
            campusId = campusId,
            parkId = defaultParkId,
            originName = origin.trim(),
            destinationName = destination.trim(),
            corridorName = corridor.trim(),
            standardFareNaira = fare,
            pickupPoints = pickupPoints.trim(),
            estimatedMinutes = estMinutes
        )
        routeDao.insertRoute(route)
    }

    suspend fun addPickupLocation(
        universityId: String,
        campusId: String,
        name: String,
        corridor: String,
        landmark: String
    ) {
        val campus = campusDao.getCampusesByUniversity(universityId).firstOrNull()?.find { it.id == campusId }
        val institution = universityDao.getUniversityById(universityId).firstOrNull()
        val location = PickupLocationEntity(
            id = "loc_${UUID.randomUUID().toString().take(6)}",
            universityId = universityId,
            campusId = campusId,
            name = name.trim(),
            corridor = corridor.trim(),
            landmarkDescription = landmark.trim(),
            latitude = campus?.centerLat ?: institution?.centerLat ?: 0.0,
            longitude = campus?.centerLng ?: institution?.centerLng ?: 0.0
        )
        pickupLocationDao.insertPickupLocation(location)
    }

    suspend fun deletePickupLocation(id: String) {
        pickupLocationDao.deletePickupLocation(id)
    }

    // Admin: Driver Approval / Suspension
    suspend fun updateDriverVerification(driverId: String, status: DriverVerificationStatus) {
        val newAccountStatus = if (status == DriverVerificationStatus.VERIFIED) {
            AccountStatus.ACTIVE
        } else {
            AccountStatus.DRIVER_APPROVAL_PENDING
        }
        userDao.updateDriverVerificationAndAccountStatus(driverId, status, newAccountStatus)
        userDao.updateDriverProfileVerification(driverId, status)
    }

    // Admin: Resolve Report
    suspend fun resolveReport(reportId: String) {
        platformDao.updateReportStatus(reportId, "RESOLVED")
    }

    // =========================================================================
    // PASSENGER & DRIVER RYDWALLET OPERATIONS (DEMO/SIMULATED vs LIVE GATEWAY)
    // =========================================================================

    suspend fun topUpPassengerWallet(
        passengerId: String,
        amountNaira: Int,
        channelLabel: String = "Simulated Nigerian Bank Transfer / USSD"
    ): Result<PassengerWalletEntity> {
        val permission = verifyPortalAccessPermission(passengerId, UserRole.PASSENGER)
        if (permission.isFailure) {
            return Result.failure(permission.exceptionOrNull() ?: SecurityException("ACCESS DENIED"))
        }
        val user = permission.getOrThrow()
        if (amountNaira < 100) {
            return Result.failure(IllegalArgumentException("Minimum RydWallet top-up is ₦100."))
        }

        val now = System.currentTimeMillis()
        val existing = walletDao.getPassengerWalletSuspend(passengerId) ?: PassengerWalletEntity(
            passengerId = passengerId,
            institutionId = user.institutionId,
            availableBalanceNaira = 0,
            heldBalanceNaira = 0,
            totalAddedNaira = 0
        )
        val updated = existing.copy(
            availableBalanceNaira = existing.availableBalanceNaira + amountNaira,
            totalAddedNaira = existing.totalAddedNaira + amountNaira,
            updatedAt = now
        )
        walletDao.upsertPassengerWallet(updated)

        val ref = "SIM-TOPUP-${(100000..999999).random()}"
        walletDao.insertTransaction(
            WalletTransactionEntity(
                id = "txn_${UUID.randomUUID().toString().take(8)}",
                userId = passengerId,
                userRole = UserRole.PASSENGER,
                institutionId = user.institutionId,
                type = WalletTransactionType.TOP_UP,
                amountNaira = amountNaira,
                paymentStatus = PaymentStatus.PAID,
                paymentMode = updated.paymentMode,
                providerReference = ref,
                title = "RydWallet Funded (+₦$amountNaira)",
                description = "$channelLabel • Simulated Top-Up (No real card/bank charge)",
                timestamp = now
            )
        )
        notificationDao.insertNotification(
            NotificationEntity(
                id = "notif_${UUID.randomUUID().toString().take(8)}",
                userId = passengerId,
                userRole = UserRole.PASSENGER,
                institutionId = user.institutionId,
                title = "RydWallet Top-Up Successful (+₦$amountNaira)",
                message = "Your Passenger RydWallet has been credited with ₦$amountNaira ($channelLabel). Available balance: ₦${updated.availableBalanceNaira}.",
                category = "WALLET",
                timestamp = now
            )
        )
        return Result.success(updated)
    }

    suspend fun withdrawDriverEarnings(
        driverId: String,
        amountNaira: Int
    ): Result<DriverWalletEntity> {
        val permission = verifyPortalAccessPermission(driverId, UserRole.DRIVER)
        if (permission.isFailure) {
            return Result.failure(permission.exceptionOrNull() ?: SecurityException("ACCESS DENIED"))
        }
        val driver = permission.getOrThrow()
        val existing = walletDao.getDriverWalletSuspend(driverId) ?: DriverWalletEntity(
            driverId = driverId,
            institutionId = driver.institutionId,
            accountName = driver.fullName
        )
        if (amountNaira < 500) {
            return Result.failure(IllegalArgumentException("Minimum withdrawal amount is ₦500."))
        }
        if (existing.availableBalanceNaira < amountNaira) {
            return Result.failure(IllegalStateException("Insufficient withdrawable balance (Available: ₦${existing.availableBalanceNaira})."))
        }

        val now = System.currentTimeMillis()
        val updated = existing.copy(
            availableBalanceNaira = existing.availableBalanceNaira - amountNaira,
            totalWithdrawnNaira = existing.totalWithdrawnNaira + amountNaira,
            updatedAt = now
        )
        walletDao.upsertDriverWallet(updated)

        val ref = "SIM-WD-${(100000..999999).random()}"
        walletDao.insertTransaction(
            WalletTransactionEntity(
                id = "txn_${UUID.randomUUID().toString().take(8)}",
                userId = driverId,
                userRole = UserRole.DRIVER,
                institutionId = driver.institutionId,
                type = WalletTransactionType.DRIVER_WITHDRAWAL,
                amountNaira = amountNaira,
                paymentStatus = PaymentStatus.PAID,
                paymentMode = updated.paymentMode,
                providerReference = ref,
                title = "Withdrawal to ${updated.bankName} (${updated.accountNumberMasked})",
                description = "Simulated Driver Settlement Payout • Ref: $ref",
                timestamp = now
            )
        )
        notificationDao.insertNotification(
            NotificationEntity(
                id = "notif_${UUID.randomUUID().toString().take(8)}",
                userId = driverId,
                userRole = UserRole.DRIVER,
                institutionId = driver.institutionId,
                title = "Withdrawal Settled (₦$amountNaira)",
                message = "Simulated payout of ₦$amountNaira to ${updated.bankName} (${updated.accountNumberMasked}) completed.",
                category = "WALLET",
                timestamp = now
            )
        )
        return Result.success(updated)
    }

    suspend fun updateDriverBankSettlementDetails(
        driverId: String,
        bankName: String,
        accountNumberLast4: String,
        accountName: String
    ): Result<DriverWalletEntity> {
        val permission = verifyPortalAccessPermission(driverId, UserRole.DRIVER)
        if (permission.isFailure) {
            return Result.failure(permission.exceptionOrNull() ?: SecurityException("ACCESS DENIED"))
        }
        val driver = permission.getOrThrow()
        val existing = walletDao.getDriverWalletSuspend(driverId) ?: DriverWalletEntity(
            driverId = driverId,
            institutionId = driver.institutionId,
            accountName = driver.fullName
        )
        val cleanLast4 = accountNumberLast4.filter { it.isDigit() }.takeLast(4).padStart(4, '0')
        val updated = existing.copy(
            bankName = bankName.trim().ifBlank { existing.bankName },
            accountNumberMasked = "**** $cleanLast4",
            accountName = accountName.trim().ifBlank { driver.fullName },
            updatedAt = System.currentTimeMillis()
        )
        walletDao.upsertDriverWallet(updated)
        return Result.success(updated)
    }

    suspend fun rateBooking(bookingId: String, passengerId: String, rating: Int): Result<Unit> {
        val permission = verifyPortalAccessPermission(passengerId, UserRole.PASSENGER)
        if (permission.isFailure) {
            return Result.failure(permission.exceptionOrNull() ?: SecurityException("ACCESS DENIED"))
        }
        bookingDao.updatePassengerRating(bookingId, rating.coerceIn(1, 5))
        return Result.success(Unit)
    }

    // =========================================================================
    // HELP, COMPLAINTS & TECHNICAL PROBLEM REPORTING (INSTITUTION-ISOLATED)
    // =========================================================================

    suspend fun submitSupportOrTechnicalTicket(
        actor: UserEntity,
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
    ): Result<ReportEntity> {
        if (description.isBlank()) {
            return Result.failure(IllegalArgumentException("Please provide a description of the issue."))
        }
        val now = System.currentTimeMillis()
        val report = ReportEntity(
            id = "rep_${UUID.randomUUID().toString().take(8)}",
            institutionId = actor.institutionId,
            campusId = actor.campusId,
            userId = actor.id,
            reporterName = actor.fullName,
            reporterRole = actor.role,
            category = category.ifBlank { if (isTechnicalProblem) "Technical Problem" else "General Complaint" },
            isTechnicalProblem = isTechnicalProblem,
            screenshotAttachmentName = screenshotAttachmentName.trim(),
            tripId = tripId,
            bookingId = bookingId,
            driverId = driverId ?: if (actor.role == UserRole.DRIVER) actor.id else null,
            passengerId = passengerId ?: if (actor.role == UserRole.PASSENGER) actor.id else null,
            subject = subject.trim().ifBlank { category },
            description = description.trim(),
            routeOrTripRef = routeOrTripRef.ifBlank { bookingId ?: tripId ?: "Institution Network" },
            status = ComplaintStatus.OPEN.label,
            timestamp = now
        )
        platformDao.insertReport(report)

        notificationDao.insertNotification(
            NotificationEntity(
                id = "notif_${UUID.randomUUID().toString().take(8)}",
                userId = actor.id,
                userRole = actor.role,
                institutionId = actor.institutionId,
                title = "Support Ticket Submitted (${report.category})",
                message = "Ticket '${report.subject}' has been logged with your Institution Administration (${report.status}).",
                category = "SUPPORT",
                relatedBookingId = bookingId,
                relatedTripId = tripId,
                timestamp = now
            )
        )
        return Result.success(report)
    }

    suspend fun updateComplaintTicketInvestigation(
        adminUser: UserEntity,
        reportId: String,
        newStatus: String,
        adminNotes: String,
        escalateToSuperAdmin: Boolean = false
    ): Result<Unit> {
        if (adminUser.role != UserRole.ADMIN && adminUser.role != UserRole.SUPER_ADMIN) {
            return Result.failure(SecurityException("ACCESS DENIED: Only Institution Administrators or Super Admins can update complaint tickets."))
        }
        val allReportsList = platformDao.getAllReports().firstOrNull().orEmpty()
        val targetReport = allReportsList.find { it.id == reportId }
            ?: return Result.failure(IllegalArgumentException("Complaint ticket not found."))

        // Enforce strict institution isolation for Institution Admin
        if (adminUser.role == UserRole.ADMIN && targetReport.institutionId != adminUser.institutionId) {
            return Result.failure(SecurityException("ACCESS DENIED: Institution Admin cannot access another institution's complaints."))
        }

        platformDao.updateReportInvestigation(
            reportId = reportId,
            status = newStatus,
            adminNotes = adminNotes.trim(),
            escalated = escalateToSuperAdmin
        )

        if (targetReport.userId.isNotBlank()) {
            notificationDao.insertNotification(
                NotificationEntity(
                    id = "notif_${UUID.randomUUID().toString().take(8)}",
                    userId = targetReport.userId,
                    userRole = targetReport.reporterRole,
                    institutionId = targetReport.institutionId,
                    title = "Complaint Update: ${targetReport.subject} ($newStatus)",
                    message = if (adminNotes.isNotBlank()) "Admin Response: $adminNotes" else "Your support ticket status is now $newStatus.",
                    category = "SUPPORT",
                    relatedBookingId = targetReport.bookingId,
                    relatedTripId = targetReport.tripId
                )
            )
        }
        return Result.success(Unit)
    }

    suspend fun markNotificationAsRead(notificationId: String) {
        notificationDao.markNotificationRead(notificationId)
    }

    suspend fun markAllNotificationsRead(userId: String) {
        notificationDao.markAllNotificationsReadForUser(userId)
    }
}
