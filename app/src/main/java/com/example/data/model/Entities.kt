package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "states")
data class StateEntity(
    @PrimaryKey val id: String,
    val name: String,
    val code: String,
    val isActive: Boolean = true
)

@Entity(tableName = "universities")
data class UniversityEntity(
    @PrimaryKey val id: String,
    val name: String,
    val shortCode: String,
    val stateOrCity: String,
    val institutionType: InstitutionType = InstitutionType.UNIVERSITY,
    val stateId: String = "state_rivers",
    val state: String = "Rivers State",
    val city: String = "Port Harcourt",
    val centerLat: Double = 0.0,
    val centerLng: Double = 0.0,
    val isActive: Boolean = true
) {
    val institutionId: String get() = id
    val institutionName: String get() = name
}

typealias InstitutionEntity = UniversityEntity

@Entity(tableName = "campuses")
data class CampusEntity(
    @PrimaryKey val id: String,
    val universityId: String,
    val name: String,
    val description: String,
    val centerLat: Double = 0.0,
    val centerLng: Double = 0.0,
    val boundaryRadiusMeters: Int = 2500
) {
    val campusId: String get() = id
    val campusName: String get() = name
    val institutionId: String get() = universityId
}

@Entity(tableName = "transport_parks")
data class TransportParkEntity(
    @PrimaryKey val id: String,
    val universityId: String,
    val campusId: String,
    val name: String,
    val parkManager: String,
    val totalBays: Int = 10
) {
    val institutionId: String get() = universityId
}

@Entity(tableName = "service_zones")
data class ServiceZoneEntity(
    @PrimaryKey val id: String,
    val universityId: String,
    val campusId: String,
    val name: String,
    val centerLat: Double,
    val centerLng: Double,
    val radiusMeters: Int = 450,
    val maxEtaMinutesThreshold: Int = 10,
    val supportedRoutes: String,
    val isActive: Boolean = true
) {
    val institutionId: String get() = universityId
}

@Entity(tableName = "driver_queue_entries")
data class DriverQueueEntryEntity(
    @PrimaryKey val id: String,
    val institutionId: String,
    val campusId: String,
    val driverId: String,
    val driverName: String,
    val vehicleRegistration: String,
    val vehicleModel: String,
    val vehicleCategory: VehicleCategory,
    val totalCapacity: Int,
    val availableSeats: Int,
    val serviceZoneId: String,
    val serviceZoneName: String,
    val routeDirection: String,
    val queueStatus: DriverQueueStatus = DriverQueueStatus.QUEUED,
    val queueEntryTimestamp: Long,
    val entryTimeFormatted: String,
    val zoneEnteredAt: Long,
    val zoneLeftAt: Long? = null,
    val currentLat: Double,
    val currentLng: Double,
    val distanceToPassengerMeters: Int = 250,
    val estimatedArrivalMinutes: Int = 3,
    val temporarilySkippedReason: String = "",
    val currentTripId: String? = null
)

@Entity(tableName = "vehicles")
data class VehicleEntity(
    @PrimaryKey val id: String,
    val driverId: String,
    val universityId: String,
    val campusId: String,
    val vehicleType: String,
    val registrationNumber: String,
    val makeModel: String,
    val licensePlate: String,
    val capacity: Int,
    val isAvailable: Boolean = true,
    val isVerified: Boolean = true
) {
    val institutionId: String get() = universityId
}

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val role: UserRole,
    val fullName: String,
    val phoneNumber: String,
    val email: String,
    val rating: Float = 4.8f,
    val verificationStatus: DriverVerificationStatus = DriverVerificationStatus.VERIFIED,
    val universityId: String,
    val authorizedInstitutionIds: String = universityId,
    val campusId: String,
    val passwordHash: String = "rydrover123",
    val profileComplete: Boolean = true,
    val accountStatus: AccountStatus = AccountStatus.ACTIVE,
    val createdAt: Long = System.currentTimeMillis()
) {
    val userId: String get() = id
    val phone: String get() = phoneNumber
    val institutionId: String get() = universityId
}

@Entity(tableName = "passenger_profiles")
data class PassengerProfileEntity(
    @PrimaryKey val passengerId: String,
    val institutionId: String,
    val campusId: String,
    val studentIdOrMatric: String = "",
    val departmentOrFaculty: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "driver_profiles")
data class DriverProfileEntity(
    @PrimaryKey val driverId: String,
    val institutionId: String,
    val campusId: String = "",
    val authorizedInstitutionIds: String = institutionId,
    val vehicleCategory: VehicleCategory,
    val vehicleMakeModel: String,
    val licensePlate: String,
    val licenseNumber: String,
    val color: String,
    val registrationNumber: String,
    val totalTrips: Int = 0,
    val completedTripsToday: Int = 4,
    val rating: Float = 4.9f,
    val isOnline: Boolean = true,
    val queueStatus: DriverQueueStatus = DriverQueueStatus.QUEUED,
    val currentServiceZoneId: String,
    val currentServiceZoneName: String,
    val currentRouteDirection: String,
    val queueEntryTimestamp: Long = System.currentTimeMillis() - 900_000L,
    val currentLat: Double = 0.0,
    val currentLng: Double = 0.0,
    val verificationStatus: DriverVerificationStatus = DriverVerificationStatus.VERIFIED
)

@Entity(tableName = "routes")
data class RouteEntity(
    @PrimaryKey val id: String,
    val universityId: String,
    val campusId: String,
    val parkId: String,
    val originName: String,
    val destinationName: String,
    val corridorName: String,
    val standardFareNaira: Int,
    val pickupPoints: String,
    val estimatedMinutes: Int
) {
    val institutionId: String get() = universityId
}

@Entity(tableName = "pickup_locations")
data class PickupLocationEntity(
    @PrimaryKey val id: String,
    val universityId: String,
    val campusId: String,
    val name: String,
    val corridor: String,
    val landmarkDescription: String,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val isActive: Boolean = true
) {
    val institutionId: String get() = universityId
}

@Entity(tableName = "trips")
data class TripEntity(
    @PrimaryKey val id: String,
    val tripNumber: String,
    val universityId: String,
    val campusId: String,
    val parkId: String,
    val serviceZoneId: String,
    val serviceZoneName: String,
    val driverId: String,
    val driverName: String,
    val driverPhone: String,
    val driverRating: Float,
    val vehicleCategory: VehicleCategory,
    val vehicleModel: String,
    val licensePlate: String,
    val vehicleRegistration: String,
    val origin: String,
    val destination: String,
    val corridor: String,
    val departureTime: String,
    val departureDate: String,
    val totalFareNaira: Int,
    val totalCapacity: Int,
    val bookedSeatsCount: Int = 0,
    val pickupPoints: String,
    val fareSharingMode: FareSharingMode = FareSharingMode.EQUAL_SPLIT,
    val status: TripStatus = TripStatus.OPEN_FOR_BOOKINGS,
    val notes: String = "",
    val isOnline: Boolean = true,
    val liveTrackingActive: Boolean = false,
    val driverLat: Double = 0.0,
    val driverLng: Double = 0.0,
    val estimatedArrivalMinutes: Int = 4,
    val distanceToPassengerMeters: Int = 350,
    val queuePriorityRank: Int = 1,
    val entryTimeFormatted: String = "10:00 AM",
    val createdAt: Long = System.currentTimeMillis()
) {
    val institutionId: String get() = universityId

    val availableSeats: Int
        get() = (totalCapacity - bookedSeatsCount).coerceAtLeast(0)

    val isFull: Boolean
        get() = availableSeats <= 0

    // Dynamic fare calculation per passenger:
    // Individual passenger fare = Total trip fare ÷ Number of fare-paying passengers
    fun calculateCurrentFarePerPassenger(activePassengers: Int = bookedSeatsCount): Int {
        val divisor = when {
            activePassengers > 0 -> activePassengers
            totalCapacity > 0 -> totalCapacity
            else -> 1
        }
        return (totalFareNaira / divisor).coerceAtLeast(100)
    }

    val fareIfFullCapacity: Int
        get() = if (totalCapacity > 0) (totalFareNaira / totalCapacity).coerceAtLeast(100) else totalFareNaira

    fun computeStatusForSeats(booked: Int): TripStatus {
        if (status == TripStatus.IN_PROGRESS || status == TripStatus.COMPLETED || status == TripStatus.CANCELLED) {
            return status
        }
        if (status == TripStatus.CONFIRMED && booked >= totalCapacity) {
            return TripStatus.FULLY_BOOKED
        }
        return when {
            booked >= totalCapacity -> TripStatus.FULLY_BOOKED
            booked > 0 -> TripStatus.PARTIALLY_BOOKED
            else -> TripStatus.OPEN_FOR_BOOKINGS
        }
    }
}

@Entity(tableName = "bookings")
data class BookingEntity(
    @PrimaryKey val id: String,
    val institutionId: String,
    val campusId: String = "",
    val tripId: String,
    val passengerId: String,
    val passengerName: String,
    val passengerPhone: String,
    val passengerIndex: Int = 1,
    val pickupLocation: String,
    val destinationLocation: String,
    val seatsCount: Int = 1,
    val farePerSeatNaira: Int,
    val totalFareNaira: Int,
    val bookingReference: String,
    val verificationCode: String = "7394",
    val qrCodePayload: String = "RYDROVER|RR-48291|7394",
    val isQrVerified: Boolean = false,
    val status: BookingStatus = BookingStatus.CONFIRMED,
    val boardingStatus: String = "Waiting", // "Waiting", "Boarded", "Cancelled", "Completed"
    val paymentStatus: PaymentStatus = PaymentStatus.PAID,
    val paymentReference: String = "RYD-PAY-DEMO",
    val paymentMode: PaymentEnvironmentMode = PaymentEnvironmentMode.DEMO_SIMULATED,
    val refundStatus: String = "None",
    val passengerLat: Double = 0.0,
    val passengerLng: Double = 0.0,
    val distanceMeters: Int = 350,
    val estimatedArrivalMinutes: Int = 4,
    val passengerRating: Int = 0,
    val bookingTime: Long = System.currentTimeMillis(),
    val driverNotes: String = ""
)

@Entity(tableName = "passenger_wallets")
data class PassengerWalletEntity(
    @PrimaryKey val passengerId: String,
    val institutionId: String,
    val availableBalanceNaira: Int = 5000,
    val heldBalanceNaira: Int = 0,
    val totalAddedNaira: Int = 5000,
    val totalSpentNaira: Int = 0,
    val totalRefundedNaira: Int = 0,
    val paymentMode: PaymentEnvironmentMode = PaymentEnvironmentMode.DEMO_SIMULATED,
    val updatedAt: Long = System.currentTimeMillis()
) {
    val balanceNaira: Int get() = availableBalanceNaira
    val totalTopUpNaira: Int get() = totalAddedNaira
}

@Entity(tableName = "driver_wallets")
data class DriverWalletEntity(
    @PrimaryKey val driverId: String,
    val institutionId: String,
    val availableBalanceNaira: Int = 0,
    val pendingEarningsNaira: Int = 0,
    val completedTripEarningsNaira: Int = 0,
    val totalWithdrawnNaira: Int = 0,
    val bankName: String = "Access Bank PLC",
    val accountNumberMasked: String = "**** 4821",
    val accountName: String = "",
    val settlementRule: String = "Auto-settled on trip completion (5% platform fee)",
    val paymentMode: PaymentEnvironmentMode = PaymentEnvironmentMode.DEMO_SIMULATED,
    val updatedAt: Long = System.currentTimeMillis()
) {
    val pendingSettlementNaira: Int get() = pendingEarningsNaira
    val totalEarningsNaira: Int get() = completedTripEarningsNaira
    val todayEarningsNaira: Int get() = completedTripEarningsNaira
    val completedTripsPaidCount: Int get() = (completedTripEarningsNaira / 1200).coerceAtLeast(1)
    val accountNumber: String get() = accountNumberMasked
}

@Entity(tableName = "wallet_transactions")
data class WalletTransactionEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val userRole: UserRole,
    val institutionId: String,
    val tripId: String? = null,
    val bookingId: String? = null,
    val bookingReference: String = "",
    val type: WalletTransactionType,
    val amountNaira: Int,
    val paymentStatus: PaymentStatus,
    val paymentMode: PaymentEnvironmentMode = PaymentEnvironmentMode.DEMO_SIMULATED,
    val providerReference: String = "",
    val title: String,
    val description: String,
    val timestamp: Long = System.currentTimeMillis()
) {
    val status: String
        get() = if (type == WalletTransactionType.RIDE_PAYMENT_HOLD || paymentStatus == PaymentStatus.PENDING) {
            "HELD"
        } else {
            paymentStatus.label
        }

    val referenceCode: String
        get() = providerReference.ifBlank { bookingReference.ifBlank { id.uppercase() } }
}

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val userRole: UserRole,
    val institutionId: String,
    val title: String,
    val message: String,
    val category: String = "GENERAL", // BOOKING, PAYMENT, TRIP, WALLET, SUPPORT, QUEUE, ADMIN
    val relatedBookingId: String? = null,
    val relatedTripId: String? = null,
    val isRead: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "coordinated_requests")
data class CoordinatedRequestEntity(
    @PrimaryKey val id: String,
    val institutionId: String,
    val campusId: String = "",
    val passengerId: String,
    val passengerName: String,
    val passengerPhone: String,
    val pickupJunction: String,
    val destination: String,
    val passengersCount: Int = 1,
    val desiredDepartureTime: String,
    val notes: String = "",
    val status: String = "PENDING_DRIVER",
    val createdAt: Long = System.currentTimeMillis(),
    val fulfilledByTripId: String? = null
)

@Entity(tableName = "reports")
data class ReportEntity(
    @PrimaryKey val id: String,
    val institutionId: String,
    val campusId: String = "",
    val userId: String = "",
    val reporterName: String,
    val reporterRole: UserRole,
    val category: String = "General Complaint",
    val isTechnicalProblem: Boolean = false,
    val screenshotAttachmentName: String = "",
    val tripId: String? = null,
    val bookingId: String? = null,
    val driverId: String? = null,
    val passengerId: String? = null,
    val subject: String,
    val description: String,
    val routeOrTripRef: String,
    val status: String = "OPEN", // OPEN, UNDER_REVIEW, RESOLVED, CLOSED
    val adminNotes: String = "",
    val isEscalatedToSuperAdmin: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
) {
    val issueType: String
        get() = if (isTechnicalProblem) "TECHNICAL" else subject.ifBlank { category }

    val details: String
        get() = description

    val screenSection: String
        get() = routeOrTripRef.ifBlank { category }

    val screenshotReference: String
        get() = screenshotAttachmentName

    val isResolved: Boolean
        get() = status.equals("RESOLVED", ignoreCase = true) || status.equals("CLOSED", ignoreCase = true)

    val reportStatus: ReportStatus
        get() = when {
            isEscalatedToSuperAdmin || status.equals("ESCALATED", ignoreCase = true) -> ReportStatus.ESCALATED
            status.equals("RESOLVED", ignoreCase = true) || status.equals("CLOSED", ignoreCase = true) -> ReportStatus.RESOLVED
            status.equals("UNDER_REVIEW", ignoreCase = true) -> ReportStatus.UNDER_REVIEW
            else -> ReportStatus.OPEN
        }

    val reportCategory: ReportCategory
        get() = if (isTechnicalProblem) ReportCategory.TECHNICAL_PROBLEM else ReportCategory.COMPLAINT
}

@Entity(tableName = "platform_settings")
data class PlatformSettingsEntity(
    @PrimaryKey val id: Int = 1,
    val activeUniversity: String = "Multi-Institution Transport Platform",
    val fareSharingMode: FareSharingMode = FareSharingMode.EQUAL_SPLIT,
    val platformCommissionPercent: Int = 5,
    val cancellationCutoffMinutes: Int = 15,
    val pilotLocation: String = "Multi-Institution Transport Network",
    val emergencyContact: String = "+234 800 793 76837"
) {
    val paymentEnvironmentMode: PaymentEnvironmentMode
        get() = PaymentEnvironmentMode.DEMO_SIMULATED
}
