package com.example.data.model

enum class UserRole(val displayName: String, val roleKey: String) {
    PASSENGER("Passenger", "passenger"),
    DRIVER("Commercial Driver", "driver"),
    ADMIN("Institution Administrator", "institution_admin"),
    SUPER_ADMIN("RydRover Super Admin", "super_admin");

    val label: String get() = displayName
}

enum class PaymentStatus(val label: String) {
    PAID("PAID"),
    PENDING("PENDING"),
    FAILED("FAILED"),
    REFUNDED("REFUNDED"),
    CANCELLED("CANCELLED")
}

enum class PaymentEnvironmentMode(val badgeLabel: String, val description: String) {
    DEMO_SIMULATED(
        "DEMO / SIMULATED PAYMENT",
        "Simulated RydWallet ledger for testing — no real bank or card transfer occurs."
    ),
    LIVE_GATEWAY(
        "LIVE PAYMENT",
        "Connected to live Nigerian payment gateway."
    );

    val label: String get() = badgeLabel
}

enum class WalletTransactionType(val label: String) {
    TOP_UP("RydWallet Top-Up"),
    RIDE_PAYMENT_HOLD("Ride Fare Reserved / Held"),
    RIDE_PAYMENT_SETTLED("Ride Fare Finalized"),
    RIDE_PAYMENT("Ride Fare Payment"),
    RIDE_REFUND("Booking Fare Refunded"),
    REFUND("Booking Fare Refunded"),
    DRIVER_EARNING_PENDING("Trip Earning (Pending Completion)"),
    DRIVER_EARNING_SETTLED("Trip Earning Settled"),
    DRIVER_EARNING("Trip Earning"),
    DRIVER_WITHDRAWAL("Bank Settlement / Withdrawal"),
    WITHDRAWAL("Bank Settlement / Withdrawal"),
    PLATFORM_FEE("Platform Fee")
}

enum class ReportCategory(val label: String) {
    COMPLAINT("Complaint"),
    TECHNICAL_PROBLEM("Technical Problem")
}

enum class ReportStatus(val label: String) {
    OPEN("Open"),
    UNDER_REVIEW("Under Review"),
    ESCALATED("Escalated"),
    RESOLVED("Resolved")
}

enum class ComplaintStatus(val label: String) {
    OPEN("OPEN"),
    UNDER_REVIEW("UNDER_REVIEW"),
    RESOLVED("RESOLVED"),
    CLOSED("CLOSED")
}

enum class AccountStatus(val label: String) {
    ACCOUNT_CREATED("Account Created"),
    ROLE_SELECTED("Role Selected"),
    INSTITUTION_SELECTED("Institution Selected"),
    CAMPUS_SELECTED("Campus Selected"),
    PASSENGER_PROFILE_COMPLETED("Passenger Profile Completed"),
    DRIVER_PROFILE_COMPLETED("Driver Profile Completed"),
    DRIVER_APPROVAL_PENDING("Driver Approval Pending"),
    ACTIVE("Fully Active Account")
}

enum class InstitutionType(val label: String) {
    UNIVERSITY("University"),
    POLYTECHNIC("Polytechnic"),
    COLLEGE("College"),
    TECHNICAL_COLLEGE("Technical College"),
    OTHER("Other")
}

enum class DriverQueueStatus(val label: String) {
    OFFLINE("Offline"),
    ONLINE("Online"),
    AVAILABLE("Available"),
    QUEUED("Queued"),
    BOOKED("Booked"),
    EN_ROUTE_TO_PASSENGER("En Route to Passenger"),
    PASSENGER_BOARDING("Passenger Boarding"),
    TRIP_ACTIVE("Trip Active"),
    TRIP_COMPLETED("Trip Completed"),
    TEMPORARILY_UNAVAILABLE("Temporarily Unavailable")
}

enum class TripStatus(val label: String) {
    OPEN_FOR_BOOKINGS("Open for Bookings"),
    PARTIALLY_BOOKED("Partially Booked"),
    FULLY_BOOKED("Fully Booked"),
    CONFIRMED("Confirmed"),
    IN_PROGRESS("In Progress"),
    COMPLETED("Completed"),
    CANCELLED("Cancelled")
}

enum class BookingStatus(val label: String) {
    CONFIRMED("Confirmed"),
    PENDING("Pending"),
    PENDING_APPROVAL("Pending Driver Review"),
    REJECTED("Rejected by Driver"),
    COMPLETED("Completed"),
    CANCELLED("Cancelled")
}

enum class VehicleCategory(val label: String, val typicalCapacity: Int) {
    COMMERCIAL_TAXI("Commercial Taxi", 4),
    SEDAN("Four-Seater Sedan", 4),
    SUV_SIENNA("Sienna / 6-Seater", 6),
    MINIBUS("Commercial Minibus", 14),
    APPROVED_TRANSIT("Approved Public Transit", 4);

    val defaultSeats: Int get() = typicalCapacity
}

enum class FareSharingMode(val label: String, val description: String) {
    EQUAL_SPLIT(
        "Equal Fare Split",
        "Total commercial route fare divided equally among active passengers"
    ),
    FIXED_PER_SEAT(
        "Fixed Per Seat",
        "Standard commercial stage fare per passenger seat"
    ),
    DISTANCE_WEIGHTED(
        "Stage/Junction Based",
        "Fare calculated based on boarding junction and destination"
    )
}

enum class DriverVerificationStatus(val label: String) {
    VERIFIED("Verified Commercial Operator"),
    PENDING("Inspection Pending"),
    REJECTED("Documents Incomplete")
}
