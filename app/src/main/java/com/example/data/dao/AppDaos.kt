package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
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
import com.example.data.model.NotificationEntity
import com.example.data.model.PassengerProfileEntity
import com.example.data.model.PassengerWalletEntity
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
import com.example.data.model.VehicleEntity
import com.example.data.model.WalletTransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StateDao {
    @Query("SELECT * FROM states ORDER BY name ASC")
    fun getAllStates(): Flow<List<StateEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertState(state: StateEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllStates(states: List<StateEntity>)

    @Query("DELETE FROM states WHERE id = :id")
    suspend fun deleteState(id: String)
}

@Dao
interface ServiceZoneDao {
    @Query("SELECT * FROM service_zones ORDER BY name ASC")
    fun getAllServiceZones(): Flow<List<ServiceZoneEntity>>

    @Query("SELECT * FROM service_zones WHERE isActive = 1 ORDER BY name ASC")
    fun getActiveServiceZones(): Flow<List<ServiceZoneEntity>>

    @Query("SELECT * FROM service_zones WHERE universityId = :institutionId ORDER BY name ASC")
    fun getServiceZonesByInstitution(institutionId: String): Flow<List<ServiceZoneEntity>>

    @Query("SELECT * FROM service_zones WHERE universityId = :institutionId AND campusId = :campusId ORDER BY name ASC")
    fun getServiceZonesByInstitutionAndCampus(institutionId: String, campusId: String): Flow<List<ServiceZoneEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertServiceZone(zone: ServiceZoneEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllServiceZones(zones: List<ServiceZoneEntity>)

    @Query("DELETE FROM service_zones WHERE id = :id")
    suspend fun deleteServiceZone(id: String)
}

@Dao
interface DriverQueueDao {
    @Query("SELECT * FROM driver_queue_entries ORDER BY queueEntryTimestamp ASC")
    fun getAllQueueEntries(): Flow<List<DriverQueueEntryEntity>>

    @Query("SELECT * FROM driver_queue_entries WHERE institutionId = :institutionId ORDER BY queueEntryTimestamp ASC")
    fun getQueueEntriesByInstitution(institutionId: String): Flow<List<DriverQueueEntryEntity>>

    @Query("SELECT * FROM driver_queue_entries WHERE institutionId = :institutionId AND campusId = :campusId ORDER BY queueEntryTimestamp ASC")
    fun getQueueEntriesByInstitutionAndCampus(institutionId: String, campusId: String): Flow<List<DriverQueueEntryEntity>>

    @Query("SELECT * FROM driver_queue_entries WHERE serviceZoneId = :zoneId AND institutionId = :institutionId ORDER BY queueEntryTimestamp ASC")
    fun getQueueForZone(zoneId: String, institutionId: String): Flow<List<DriverQueueEntryEntity>>

    @Query("SELECT * FROM driver_queue_entries WHERE driverId = :driverId LIMIT 1")
    fun getQueueEntryForDriver(driverId: String): Flow<DriverQueueEntryEntity?>

    @Query("SELECT * FROM driver_queue_entries WHERE driverId = :driverId AND institutionId = :institutionId LIMIT 1")
    fun getQueueEntryForDriverAndInstitution(driverId: String, institutionId: String): Flow<DriverQueueEntryEntity?>

    @Query("SELECT * FROM driver_queue_entries WHERE driverId = :driverId LIMIT 1")
    suspend fun getQueueEntryForDriverSuspend(driverId: String): DriverQueueEntryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQueueEntry(entry: DriverQueueEntryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllQueueEntries(entries: List<DriverQueueEntryEntity>)

    @Query("UPDATE driver_queue_entries SET queueStatus = :status WHERE driverId = :driverId")
    suspend fun updateQueueStatus(driverId: String, status: DriverQueueStatus)

    @Query("UPDATE driver_queue_entries SET queueEntryTimestamp = :newTimestamp, entryTimeFormatted = :formattedTime, queueStatus = :status, temporarilySkippedReason = '' WHERE driverId = :driverId")
    suspend fun requeueDriverAtBack(
        driverId: String,
        newTimestamp: Long,
        formattedTime: String,
        status: DriverQueueStatus
    )

    @Query("UPDATE driver_queue_entries SET institutionId = :institutionId, campusId = :campusId, serviceZoneId = :zoneId, serviceZoneName = :zoneName, routeDirection = :routeDirection, currentLat = :lat, currentLng = :lng, queueEntryTimestamp = :newTimestamp, entryTimeFormatted = :formattedTime, zoneEnteredAt = :newTimestamp, queueStatus = :status, temporarilySkippedReason = '' WHERE driverId = :driverId")
    suspend fun updateDriverZoneAndRoute(
        driverId: String,
        institutionId: String,
        campusId: String,
        zoneId: String,
        zoneName: String,
        routeDirection: String,
        lat: Double,
        lng: Double,
        newTimestamp: Long,
        formattedTime: String,
        status: DriverQueueStatus
    )

    @Query("UPDATE driver_queue_entries SET temporarilySkippedReason = :reason WHERE driverId = :driverId")
    suspend fun updateTemporarySkipReason(driverId: String, reason: String)
}

@Dao
interface TripDao {
    @Query("SELECT * FROM trips ORDER BY queuePriorityRank ASC, createdAt ASC")
    fun getAllTrips(): Flow<List<TripEntity>>

    @Query("SELECT * FROM trips WHERE universityId = :institutionId ORDER BY queuePriorityRank ASC, createdAt ASC")
    fun getTripsByInstitution(institutionId: String): Flow<List<TripEntity>>

    @Query("SELECT * FROM trips WHERE universityId = :institutionId AND campusId = :campusId ORDER BY queuePriorityRank ASC, createdAt ASC")
    fun getTripsByInstitutionAndCampus(institutionId: String, campusId: String): Flow<List<TripEntity>>

    @Query("SELECT * FROM trips WHERE status IN ('OPEN_FOR_BOOKINGS', 'PARTIALLY_BOOKED', 'FULLY_BOOKED', 'CONFIRMED', 'IN_PROGRESS') ORDER BY queuePriorityRank ASC, createdAt ASC")
    fun getActiveTrips(): Flow<List<TripEntity>>

    @Query("SELECT * FROM trips WHERE driverId = :driverId ORDER BY createdAt DESC")
    fun getTripsByDriver(driverId: String): Flow<List<TripEntity>>

    @Query("SELECT * FROM trips WHERE driverId = :driverId AND universityId = :institutionId ORDER BY createdAt DESC")
    fun getTripsByDriverAndInstitution(driverId: String, institutionId: String): Flow<List<TripEntity>>

    @Query("SELECT * FROM trips WHERE id = :tripId LIMIT 1")
    fun getTripById(tripId: String): Flow<TripEntity?>

    @Query("SELECT * FROM trips WHERE id = :tripId LIMIT 1")
    suspend fun getTripByIdSuspend(tripId: String): TripEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrip(trip: TripEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(trips: List<TripEntity>)

    @Update
    suspend fun updateTrip(trip: TripEntity)

    @Query("UPDATE trips SET status = :status WHERE id = :tripId")
    suspend fun updateTripStatus(tripId: String, status: TripStatus)

    @Query("UPDATE trips SET bookedSeatsCount = :bookedSeats, status = :status WHERE id = :tripId")
    suspend fun updateBookedSeatsAndStatus(tripId: String, bookedSeats: Int, status: TripStatus)

    @Query("UPDATE trips SET bookedSeatsCount = :bookedSeats WHERE id = :tripId")
    suspend fun updateBookedSeats(tripId: String, bookedSeats: Int)

    @Query("UPDATE trips SET liveTrackingActive = :liveTracking, driverLat = :lat, driverLng = :lng WHERE id = :tripId")
    suspend fun updateTripLiveTracking(tripId: String, liveTracking: Boolean, lat: Double, lng: Double)

    @Query("UPDATE trips SET isOnline = :isOnline WHERE driverId = :driverId")
    suspend fun updateDriverTripsOnlineStatus(driverId: String, isOnline: Boolean)

    @Query("UPDATE trips SET queuePriorityRank = :rank, entryTimeFormatted = :timeFormatted WHERE driverId = :driverId AND universityId = :institutionId")
    suspend fun updateDriverTripsQueueRank(driverId: String, institutionId: String, rank: Int, timeFormatted: String)

    @Query("DELETE FROM trips WHERE id = :tripId")
    suspend fun deleteTrip(tripId: String)
}

@Dao
interface BookingDao {
    @Query("SELECT * FROM bookings ORDER BY bookingTime DESC")
    fun getAllBookings(): Flow<List<BookingEntity>>

    @Query("SELECT * FROM bookings WHERE institutionId = :institutionId ORDER BY bookingTime DESC")
    fun getBookingsByInstitution(institutionId: String): Flow<List<BookingEntity>>

    @Query("SELECT * FROM bookings WHERE institutionId = :institutionId AND campusId = :campusId ORDER BY bookingTime DESC")
    fun getBookingsByInstitutionAndCampus(institutionId: String, campusId: String): Flow<List<BookingEntity>>

    @Query("SELECT * FROM bookings WHERE passengerId = :passengerId ORDER BY bookingTime DESC")
    fun getBookingsForPassenger(passengerId: String): Flow<List<BookingEntity>>

    @Query("SELECT * FROM bookings WHERE passengerId = :passengerId AND institutionId = :institutionId ORDER BY bookingTime DESC")
    fun getBookingsForPassengerAndInstitution(passengerId: String, institutionId: String): Flow<List<BookingEntity>>

    @Query("SELECT * FROM bookings WHERE tripId = :tripId ORDER BY bookingTime ASC")
    fun getBookingsForTrip(tripId: String): Flow<List<BookingEntity>>

    @Query("SELECT * FROM bookings WHERE tripId = :tripId AND institutionId = :institutionId ORDER BY bookingTime ASC")
    fun getBookingsForTripAndInstitution(tripId: String, institutionId: String): Flow<List<BookingEntity>>

    @Query("SELECT * FROM bookings WHERE id = :bookingId LIMIT 1")
    fun getBookingById(bookingId: String): Flow<BookingEntity?>

    @Query("SELECT * FROM bookings WHERE tripId = :tripId AND passengerId = :passengerId AND status != 'CANCELLED' AND status != 'REJECTED' LIMIT 1")
    suspend fun findExistingBooking(tripId: String, passengerId: String): BookingEntity?

    @Query("SELECT COUNT(*) FROM bookings WHERE tripId = :tripId AND status = 'CONFIRMED'")
    suspend fun getActivePassengersCount(tripId: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBooking(booking: BookingEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(bookings: List<BookingEntity>)

    @Query("UPDATE bookings SET status = :status WHERE id = :bookingId")
    suspend fun updateBookingStatus(bookingId: String, status: BookingStatus)

    @Query("UPDATE bookings SET boardingStatus = :boardingStatus WHERE id = :bookingId")
    suspend fun updateBoardingStatus(bookingId: String, boardingStatus: String)

    @Query("UPDATE bookings SET isQrVerified = 1 WHERE id = :bookingId")
    suspend fun markQrVerified(bookingId: String)

    @Query("UPDATE bookings SET isQrVerified = 1, boardingStatus = 'Boarded' WHERE id = :bookingId")
    suspend fun confirmQrBoarding(bookingId: String)

    @Query("UPDATE bookings SET passengerRating = :rating WHERE id = :bookingId")
    suspend fun updatePassengerRating(bookingId: String, rating: Int)

    @Query("UPDATE bookings SET paymentStatus = :paymentStatus, refundStatus = :refundStatus WHERE id = :bookingId")
    suspend fun updateBookingPaymentAndRefund(bookingId: String, paymentStatus: PaymentStatus, refundStatus: String)

    @Query("UPDATE bookings SET status = 'COMPLETED', boardingStatus = 'Completed', paymentStatus = 'PAID' WHERE tripId = :tripId AND status = 'CONFIRMED'")
    suspend fun completeBookingsForTrip(tripId: String)

    @Query("UPDATE bookings SET status = 'CANCELLED', boardingStatus = 'Cancelled', paymentStatus = 'REFUNDED', refundStatus = 'Refunded to RydWallet' WHERE tripId = :tripId AND status != 'COMPLETED'")
    suspend fun cancelBookingsForTrip(tripId: String)
}

@Dao
interface UserDao {
    @Query("SELECT * FROM users ORDER BY createdAt ASC")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE universityId = :institutionId OR ',' || authorizedInstitutionIds || ',' LIKE '%,' || :institutionId || ',%' ORDER BY createdAt ASC")
    fun getUsersByInstitution(institutionId: String): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    fun getUserById(id: String): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserByIdSuspend(id: String): UserEntity?

    @Query("SELECT * FROM users WHERE LOWER(TRIM(email)) = LOWER(TRIM(:identifier)) OR TRIM(phoneNumber) = TRIM(:identifier) LIMIT 1")
    suspend fun findUserByPhoneOrEmail(identifier: String): UserEntity?

    @Query("SELECT * FROM passenger_profiles WHERE passengerId = :passengerId LIMIT 1")
    fun getPassengerProfile(passengerId: String): Flow<PassengerProfileEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPassengerProfile(profile: PassengerProfileEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllPassengerProfiles(profiles: List<PassengerProfileEntity>)

    @Query("UPDATE users SET role = :role, accountStatus = :status WHERE id = :userId")
    suspend fun updateUserRoleStep(userId: String, role: UserRole, status: AccountStatus)

    @Query("UPDATE users SET universityId = :institutionId, authorizedInstitutionIds = :authorizedIds, accountStatus = :status WHERE id = :userId")
    suspend fun updateUserInstitutionStep(
        userId: String,
        institutionId: String,
        authorizedIds: String,
        status: AccountStatus
    )

    @Query("UPDATE users SET campusId = :campusId, accountStatus = :status WHERE id = :userId")
    suspend fun updateUserCampusStep(userId: String, campusId: String, status: AccountStatus)

    @Query("UPDATE users SET fullName = :fullName, phoneNumber = :phone, universityId = :institutionId, authorizedInstitutionIds = :institutionId, campusId = :campusId, role = :role, verificationStatus = :verificationStatus, profileComplete = :profileComplete, accountStatus = :accountStatus WHERE id = :userId")
    suspend fun completeUserOnboarding(
        userId: String,
        fullName: String,
        phone: String,
        institutionId: String,
        campusId: String,
        role: UserRole,
        verificationStatus: DriverVerificationStatus,
        profileComplete: Boolean,
        accountStatus: AccountStatus
    )

    @Query("UPDATE users SET verificationStatus = :status, accountStatus = :accountStatus WHERE id = :driverId")
    suspend fun updateDriverVerificationAndAccountStatus(
        driverId: String,
        status: DriverVerificationStatus,
        accountStatus: AccountStatus
    )

    @Query("SELECT * FROM users WHERE role = :role")
    fun getUsersByRole(role: UserRole): Flow<List<UserEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(users: List<UserEntity>)

    @Query("UPDATE users SET verificationStatus = :status WHERE id = :driverId")
    suspend fun updateVerificationStatus(driverId: String, status: DriverVerificationStatus)

    @Query("SELECT * FROM driver_profiles WHERE driverId = :driverId LIMIT 1")
    fun getDriverProfile(driverId: String): Flow<DriverProfileEntity?>

    @Query("SELECT * FROM driver_profiles")
    fun getAllDriverProfiles(): Flow<List<DriverProfileEntity>>

    @Query("SELECT * FROM driver_profiles WHERE institutionId = :institutionId OR ',' || authorizedInstitutionIds || ',' LIKE '%,' || :institutionId || ',%'")
    fun getDriverProfilesByInstitution(institutionId: String): Flow<List<DriverProfileEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDriverProfile(profile: DriverProfileEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllDriverProfiles(profiles: List<DriverProfileEntity>)

    @Query("UPDATE driver_profiles SET verificationStatus = :status WHERE driverId = :driverId")
    suspend fun updateDriverProfileVerification(driverId: String, status: DriverVerificationStatus)

    @Query("UPDATE driver_profiles SET isOnline = :isOnline, queueStatus = :queueStatus WHERE driverId = :driverId")
    suspend fun updateDriverOnlineStatus(driverId: String, isOnline: Boolean, queueStatus: DriverQueueStatus)

    @Query("UPDATE driver_profiles SET queueStatus = :queueStatus WHERE driverId = :driverId")
    suspend fun updateDriverQueueStatus(driverId: String, queueStatus: DriverQueueStatus)

    @Query("UPDATE driver_profiles SET institutionId = :institutionId, campusId = :campusId, currentServiceZoneId = :zoneId, currentServiceZoneName = :zoneName, currentRouteDirection = :routeDirection, currentLat = :lat, currentLng = :lng, queueEntryTimestamp = :timestamp, queueStatus = :status WHERE driverId = :driverId")
    suspend fun updateDriverZoneAndQueueTimestamp(
        driverId: String,
        institutionId: String,
        campusId: String,
        zoneId: String,
        zoneName: String,
        routeDirection: String,
        lat: Double,
        lng: Double,
        timestamp: Long,
        status: DriverQueueStatus
    )

    @Query("UPDATE driver_profiles SET currentLat = :lat, currentLng = :lng WHERE driverId = :driverId")
    suspend fun updateDriverCoordinates(driverId: String, lat: Double, lng: Double)

    @Query("UPDATE driver_profiles SET completedTripsToday = completedTripsToday + 1, totalTrips = totalTrips + 1, queueEntryTimestamp = :newQueueTimestamp, queueStatus = :newStatus WHERE driverId = :driverId")
    suspend fun incrementDriverCompletedTripsAndRequeue(
        driverId: String,
        newQueueTimestamp: Long,
        newStatus: DriverQueueStatus
    )

    @Query("UPDATE users SET universityId = :institutionId, campusId = :campusId WHERE id = :userId")
    suspend fun updateUserInstitutionAndCampus(
        userId: String,
        institutionId: String,
        campusId: String
    )

    @Query("UPDATE users SET universityId = :institutionId, authorizedInstitutionIds = :authorizedIds WHERE id = :userId")
    suspend fun updateUserAuthorizedInstitutions(
        userId: String,
        institutionId: String,
        authorizedIds: String
    )

    @Query("UPDATE driver_profiles SET institutionId = :institutionId, authorizedInstitutionIds = :authorizedIds WHERE driverId = :driverId")
    suspend fun updateDriverAuthorizedInstitutions(
        driverId: String,
        institutionId: String,
        authorizedIds: String
    )
}

@Dao
interface RouteDao {
    @Query("SELECT * FROM routes")
    fun getAllRoutes(): Flow<List<RouteEntity>>

    @Query("SELECT * FROM routes WHERE universityId = :institutionId")
    fun getRoutesByInstitution(institutionId: String): Flow<List<RouteEntity>>

    @Query("SELECT * FROM routes WHERE universityId = :institutionId AND campusId = :campusId")
    fun getRoutesByInstitutionAndCampus(institutionId: String, campusId: String): Flow<List<RouteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoute(route: RouteEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllRoutes(routes: List<RouteEntity>)

    @Query("DELETE FROM routes WHERE id = :id")
    suspend fun deleteRoute(id: String)
}

@Dao
interface PickupLocationDao {
    @Query("SELECT * FROM pickup_locations WHERE isActive = 1 ORDER BY name ASC")
    fun getActivePickupLocations(): Flow<List<PickupLocationEntity>>

    @Query("SELECT * FROM pickup_locations ORDER BY name ASC")
    fun getAllPickupLocations(): Flow<List<PickupLocationEntity>>

    @Query("SELECT * FROM pickup_locations WHERE universityId = :institutionId ORDER BY name ASC")
    fun getPickupLocationsByInstitution(institutionId: String): Flow<List<PickupLocationEntity>>

    @Query("SELECT * FROM pickup_locations WHERE universityId = :institutionId AND campusId = :campusId ORDER BY name ASC")
    fun getPickupLocationsByInstitutionAndCampus(institutionId: String, campusId: String): Flow<List<PickupLocationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPickupLocation(location: PickupLocationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllPickupLocations(locations: List<PickupLocationEntity>)

    @Query("DELETE FROM pickup_locations WHERE id = :id")
    suspend fun deletePickupLocation(id: String)
}

@Dao
interface UniversityDao {
    @Query("SELECT * FROM universities ORDER BY name ASC")
    fun getAllUniversities(): Flow<List<UniversityEntity>>

    @Query("SELECT * FROM universities WHERE id = :institutionId LIMIT 1")
    fun getUniversityById(institutionId: String): Flow<UniversityEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUniversity(university: UniversityEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllUniversities(universities: List<UniversityEntity>)

    @Query("UPDATE universities SET isActive = :isActive WHERE id = :id")
    suspend fun updateInstitutionActiveStatus(id: String, isActive: Boolean)

    @Query("DELETE FROM universities WHERE id = :id")
    suspend fun deleteUniversity(id: String)
}

@Dao
interface CampusDao {
    @Query("SELECT * FROM campuses ORDER BY name ASC")
    fun getAllCampuses(): Flow<List<CampusEntity>>

    @Query("SELECT * FROM campuses WHERE universityId = :uniId ORDER BY name ASC")
    fun getCampusesByUniversity(uniId: String): Flow<List<CampusEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCampus(campus: CampusEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllCampuses(campuses: List<CampusEntity>)

    @Query("DELETE FROM campuses WHERE id = :id")
    suspend fun deleteCampus(id: String)
}

@Dao
interface TransportParkDao {
    @Query("SELECT * FROM transport_parks ORDER BY name ASC")
    fun getAllParks(): Flow<List<TransportParkEntity>>

    @Query("SELECT * FROM transport_parks WHERE universityId = :institutionId ORDER BY name ASC")
    fun getParksByInstitution(institutionId: String): Flow<List<TransportParkEntity>>

    @Query("SELECT * FROM transport_parks WHERE campusId = :campusId ORDER BY name ASC")
    fun getParksByCampus(campusId: String): Flow<List<TransportParkEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPark(park: TransportParkEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllParks(parks: List<TransportParkEntity>)

    @Query("DELETE FROM transport_parks WHERE id = :id")
    suspend fun deletePark(id: String)
}

@Dao
interface VehicleDao {
    @Query("SELECT * FROM vehicles ORDER BY registrationNumber ASC")
    fun getAllVehicles(): Flow<List<VehicleEntity>>

    @Query("SELECT * FROM vehicles WHERE universityId = :institutionId ORDER BY registrationNumber ASC")
    fun getVehiclesByInstitution(institutionId: String): Flow<List<VehicleEntity>>

    @Query("SELECT * FROM vehicles WHERE driverId = :driverId")
    fun getVehiclesByDriver(driverId: String): Flow<List<VehicleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVehicle(vehicle: VehicleEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllVehicles(vehicles: List<VehicleEntity>)

    @Query("UPDATE vehicles SET isAvailable = :available WHERE id = :id")
    suspend fun updateVehicleAvailability(id: String, available: Boolean)

    @Query("DELETE FROM vehicles WHERE id = :id")
    suspend fun deleteVehicle(id: String)
}

@Dao
interface CoordinatedRequestDao {
    @Query("SELECT * FROM coordinated_requests ORDER BY createdAt DESC")
    fun getAllRequests(): Flow<List<CoordinatedRequestEntity>>

    @Query("SELECT * FROM coordinated_requests WHERE institutionId = :institutionId ORDER BY createdAt DESC")
    fun getRequestsByInstitution(institutionId: String): Flow<List<CoordinatedRequestEntity>>

    @Query("SELECT * FROM coordinated_requests WHERE status = 'PENDING_DRIVER' ORDER BY createdAt DESC")
    fun getPendingRequests(): Flow<List<CoordinatedRequestEntity>>

    @Query("SELECT * FROM coordinated_requests WHERE passengerId = :passengerId ORDER BY createdAt DESC")
    fun getRequestsByPassenger(passengerId: String): Flow<List<CoordinatedRequestEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRequest(request: CoordinatedRequestEntity)

    @Query("UPDATE coordinated_requests SET status = :status, fulfilledByTripId = :tripId WHERE id = :requestId")
    suspend fun updateRequestStatus(requestId: String, status: String, tripId: String? = null)
}

@Dao
interface PlatformDao {
    @Query("SELECT * FROM platform_settings WHERE id = 1 LIMIT 1")
    fun getSettings(): Flow<PlatformSettingsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSettings(settings: PlatformSettingsEntity)

    @Query("SELECT * FROM reports ORDER BY timestamp DESC")
    fun getAllReports(): Flow<List<ReportEntity>>

    @Query("SELECT * FROM reports WHERE institutionId = :institutionId ORDER BY timestamp DESC")
    fun getReportsByInstitution(institutionId: String): Flow<List<ReportEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: ReportEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllReports(reports: List<ReportEntity>)

    @Query("UPDATE reports SET status = :status WHERE id = :reportId")
    suspend fun updateReportStatus(reportId: String, status: String)

    @Query("UPDATE reports SET status = :status, adminNotes = :adminNotes, isEscalatedToSuperAdmin = :escalated WHERE id = :reportId")
    suspend fun updateReportInvestigation(
        reportId: String,
        status: String,
        adminNotes: String,
        escalated: Boolean
    )
}

@Dao
interface WalletDao {
    @Query("SELECT * FROM passenger_wallets WHERE passengerId = :passengerId LIMIT 1")
    fun getPassengerWallet(passengerId: String): Flow<PassengerWalletEntity?>

    @Query("SELECT * FROM passenger_wallets WHERE passengerId = :passengerId LIMIT 1")
    suspend fun getPassengerWalletSuspend(passengerId: String): PassengerWalletEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPassengerWallet(wallet: PassengerWalletEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllPassengerWallets(wallets: List<PassengerWalletEntity>)

    @Query("SELECT * FROM driver_wallets WHERE driverId = :driverId LIMIT 1")
    fun getDriverWallet(driverId: String): Flow<DriverWalletEntity?>

    @Query("SELECT * FROM driver_wallets WHERE driverId = :driverId LIMIT 1")
    suspend fun getDriverWalletSuspend(driverId: String): DriverWalletEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDriverWallet(wallet: DriverWalletEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllDriverWallets(wallets: List<DriverWalletEntity>)

    @Query("SELECT * FROM wallet_transactions WHERE userId = :userId ORDER BY timestamp DESC")
    fun getTransactionsForUser(userId: String): Flow<List<WalletTransactionEntity>>

    @Query("SELECT * FROM wallet_transactions WHERE userId = :userId AND institutionId = :institutionId ORDER BY timestamp DESC")
    fun getTransactionsForUserAndInstitution(userId: String, institutionId: String): Flow<List<WalletTransactionEntity>>

    @Query("SELECT * FROM wallet_transactions WHERE institutionId = :institutionId ORDER BY timestamp DESC")
    fun getTransactionsByInstitution(institutionId: String): Flow<List<WalletTransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: WalletTransactionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllTransactions(transactions: List<WalletTransactionEntity>)
}

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications WHERE userId = :userId ORDER BY timestamp DESC")
    fun getNotificationsForUser(userId: String): Flow<List<NotificationEntity>>

    @Query("SELECT * FROM notifications WHERE userId = :userId AND institutionId = :institutionId ORDER BY timestamp DESC")
    fun getNotificationsForUserAndInstitution(userId: String, institutionId: String): Flow<List<NotificationEntity>>

    @Query("SELECT * FROM notifications WHERE institutionId = :institutionId AND userRole = 'ADMIN' ORDER BY timestamp DESC")
    fun getAdminNotificationsByInstitution(institutionId: String): Flow<List<NotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllNotifications(notifications: List<NotificationEntity>)

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :notificationId")
    suspend fun markNotificationRead(notificationId: String)

    @Query("UPDATE notifications SET isRead = 1 WHERE userId = :userId")
    suspend fun markAllNotificationsReadForUser(userId: String)
}
