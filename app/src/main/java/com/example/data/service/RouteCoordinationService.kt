package com.example.data.service

import com.example.data.model.DriverQueueEntryEntity
import com.example.data.model.DriverQueueStatus
import com.example.data.model.FareSharingMode
import com.example.data.model.ServiceZoneEntity
import com.example.data.model.TripEntity
import com.example.data.model.TripStatus
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Modular Route Coordination & GPS Queue Dispatch Service.
 * Implements route-aware FIFO driver queuing, geofenced service zone matching,
 * GPS distance/ETA threshold checks, and commercial fare calculations.
 */
object RouteCoordinationService {

    /**
     * Calculates Haversine GPS distance in meters between two coordinates.
     */
    fun calculateDistanceMeters(
        lat1: Double,
        lng1: Double,
        lat2: Double,
        lng2: Double
    ): Int {
        val earthRadiusMeters = 6371000.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLng = Math.toRadians(lng2 - lng1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLng / 2) * sin(dLng / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return (earthRadiusMeters * c).roundToInt().coerceAtLeast(25)
    }

    /**
     * Estimates arrival time in minutes based on campus shuttle average speed (~22 km/h = 366m/min).
     */
    fun estimateArrivalMinutes(distanceMeters: Int): Int {
        return (distanceMeters / 350.0).roundToInt().coerceAtLeast(1)
    }

    /**
     * Checks if a GPS coordinate is inside or within pickup range of a ServiceZoneEntity.
     */
    fun isWithinServiceZone(
        lat: Double,
        lng: Double,
        zone: ServiceZoneEntity,
        extraBufferMeters: Int = 400
    ): Boolean {
        val dist = calculateDistanceMeters(lat, lng, zone.centerLat, zone.centerLng)
        return dist <= (zone.radiusMeters + extraBufferMeters)
    }

    /**
     * Detects which ServiceZoneEntity contains the given GPS coordinates.
     */
    fun detectServiceZone(
        lat: Double,
        lng: Double,
        zones: List<ServiceZoneEntity>
    ): ServiceZoneEntity? {
        if (zones.isEmpty()) return null
        return zones
            .filter { isWithinServiceZone(lat, lng, it) }
            .minByOrNull { calculateDistanceMeters(lat, lng, it.centerLat, it.centerLng) }
    }

    /**
     * Filters active trips that match the passenger's pickup, destination, category, and search query.
     */
    fun filterAvailableTrips(
        allTrips: List<TripEntity>,
        pickupFilter: String = "All",
        destinationFilter: String = "All",
        categoryFilter: com.example.data.model.VehicleCategory? = null,
        searchQuery: String = ""
    ): List<TripEntity> {
        val q = searchQuery.trim().lowercase()
        return allTrips.filter { trip ->
            val isNotTerminated = trip.status != TripStatus.COMPLETED &&
                trip.status != TripStatus.CANCELLED
            val matchesPickup = pickupFilter == "All" || isPickupCompatible(trip, pickupFilter)
            val matchesDest = destinationFilter == "All" || isDestinationCompatible(trip, destinationFilter)
            val matchesCat = categoryFilter == null || trip.vehicleCategory == categoryFilter
            val matchesQuery = q.isEmpty() ||
                trip.origin.lowercase().contains(q) ||
                trip.destination.lowercase().contains(q) ||
                trip.corridor.lowercase().contains(q) ||
                trip.driverName.lowercase().contains(q) ||
                trip.vehicleRegistration.lowercase().contains(q) ||
                trip.pickupPoints.lowercase().contains(q)
            isNotTerminated && matchesPickup && matchesDest && matchesCat && matchesQuery && trip.isOnline
        }
    }

    /**
     * Ranks eligible trips for a passenger using route-aware FIFO queue order and GPS distance/ETA.
     */
    fun rankEligibleTripsForPassenger(
        allTrips: List<TripEntity>,
        queueEntries: List<DriverQueueEntryEntity>,
        serviceZones: List<ServiceZoneEntity>,
        pickupLocation: String,
        destinationLocation: String,
        passengerLat: Double,
        passengerLng: Double,
        requiredSeats: Int = 1
    ): List<EligibleTripOption> {
        val compatible = findCompatibleTrips(
            trips = allTrips,
            pickupJunction = pickupLocation,
            destination = destinationLocation,
            seatsRequested = requiredSeats
        )
        return compatible.mapIndexed { index, trip ->
            val qEntry = queueEntries.find {
                it.driverId == trip.driverId && it.institutionId == trip.institutionId
            }
            val distMeters = if (passengerLat != 0.0 && passengerLng != 0.0 && trip.driverLat != 0.0 && trip.driverLng != 0.0) {
                calculateDistanceMeters(passengerLat, passengerLng, trip.driverLat, trip.driverLng)
            } else {
                trip.distanceToPassengerMeters
            }
            val etaMins = estimateArrivalMinutes(distMeters)
            val zone = serviceZones.find { it.id == trip.serviceZoneId }
            val maxEta = zone?.maxEtaMinutesThreshold ?: 10
            EligibleTripOption(
                trip = trip,
                queueEntry = qEntry,
                routeQueuePosition = index + 1,
                distanceMeters = distMeters,
                estimatedArrivalMinutes = etaMins,
                withinEtaThreshold = etaMins <= maxEta
            )
        }
    }

    /**
     * Checks whether a commercial trip serves the requested pickup location or GPS zone.
     */
    fun isPickupCompatible(trip: TripEntity, requestedPickup: String): Boolean {
        if (requestedPickup == "All Locations" || requestedPickup.isBlank()) return true
        val normalizedReq = requestedPickup.trim().lowercase()

        // Passengers requesting from "Current location" / GPS are matched via nearby service zone
        if (normalizedReq.contains("current") || normalizedReq.contains("gps")) {
            // If a specific zone or landmark is mentioned in parentheses, check if it matches, else allow
            val zoneHint = normalizedReq
                .substringAfter("near ", "")
                .substringBefore(")")
                .trim()
            if (zoneHint.isEmpty()) return true
            return trip.origin.lowercase().contains(zoneHint) ||
                    trip.serviceZoneName.lowercase().contains(zoneHint) ||
                    trip.pickupPoints.lowercase().contains(zoneHint)
        }

        val normalizedOrigin = trip.origin.trim().lowercase()
        val normalizedZone = trip.serviceZoneName.trim().lowercase()

        if (normalizedOrigin.contains(normalizedReq) ||
            normalizedReq.contains(normalizedOrigin) ||
            normalizedZone.contains(normalizedReq) ||
            normalizedReq.contains(normalizedZone)
        ) {
            return true
        }

        // Check designated stops along corridor
        val stops = trip.pickupPoints.split(",").map { it.trim().lowercase() }
        return stops.any { it.isNotEmpty() && (it.contains(normalizedReq) || normalizedReq.contains(it)) }
    }

    /**
     * Checks whether a commercial trip serves the requested destination.
     */
    fun isDestinationCompatible(trip: TripEntity, requestedDestination: String): Boolean {
        if (requestedDestination == "All Locations" ||
            requestedDestination == "All Destinations" ||
            requestedDestination.isBlank()
        ) return true
        val normalizedReq = requestedDestination.trim().lowercase()
        val normalizedDest = trip.destination.trim().lowercase()
        val normalizedCorridor = trip.corridor.trim().lowercase()

        if (normalizedDest.contains(normalizedReq) ||
            normalizedReq.contains(normalizedDest) ||
            normalizedCorridor.contains(normalizedReq)
        ) {
            return true
        }

        val stops = trip.pickupPoints.split(",").map { it.trim().lowercase() }
        return stops.any { it.isNotEmpty() && (it.contains(normalizedReq) || normalizedReq.contains(it)) }
    }

    /**
     * Identifies compatible trips matching passenger institution, pickup and destination requirements,
     * ordered by Route-Aware Queue Priority (FIFO queue timestamp) + GPS ETA threshold.
     */
    fun findCompatibleTrips(
        trips: List<TripEntity>,
        pickupJunction: String,
        destination: String,
        seatsRequested: Int = 1,
        maxEtaThresholdMinutes: Int = 10,
        institutionId: String? = null,
        campusId: String? = null
    ): List<TripEntity> {
        return trips.filter { trip ->
            val matchesInstitution = institutionId.isNullOrBlank() || trip.institutionId == institutionId
            val matchesCampus = campusId.isNullOrBlank() || trip.campusId.isBlank() || trip.campusId == campusId
            val isNotTerminated = trip.status != TripStatus.COMPLETED &&
                    trip.status != TripStatus.CANCELLED &&
                    trip.status != TripStatus.FULLY_BOOKED
            val hasAvailableSeats = trip.availableSeats >= seatsRequested
            val pickupMatches = isPickupCompatible(trip, pickupJunction)
            val destMatches = isDestinationCompatible(trip, destination)

            matchesInstitution && matchesCampus && isNotTerminated && hasAvailableSeats && pickupMatches && destMatches && trip.isOnline
        }.sortedWith(
            // Longest-waiting eligible driver within GPS ETA threshold gets priority first
            compareBy<TripEntity> { it.estimatedArrivalMinutes > maxEtaThresholdMinutes }
                .thenBy { it.queuePriorityRank }
                .thenBy { it.estimatedArrivalMinutes }
                .thenBy { it.createdAt }
        )
    }

    /**
     * Computes the 1-based queue position of a driver within their specific (Institution + ServiceZone + RouteDirection) queue.
     */
    fun computeRouteQueuePosition(
        entry: DriverQueueEntryEntity,
        allEntries: List<DriverQueueEntryEntity>
    ): Int {
        val eligibleStates = setOf(
            DriverQueueStatus.QUEUED,
            DriverQueueStatus.AVAILABLE,
            DriverQueueStatus.ONLINE
        )
        val sameRouteQueue = allEntries
            .filter {
                it.institutionId == entry.institutionId &&
                it.serviceZoneId == entry.serviceZoneId &&
                it.routeDirection.equals(entry.routeDirection, ignoreCase = true) &&
                it.queueStatus in eligibleStates
            }
            .sortedBy { it.queueEntryTimestamp }

        val index = sameRouteQueue.indexOfFirst { it.driverId == entry.driverId }
        return if (index >= 0) index + 1 else (sameRouteQueue.size + 1)
    }

    /**
     * Modular fare calculation based on pricing arrangement.
     */
    fun calculateIndividualFare(
        totalTripFare: Int,
        activePassengers: Int,
        vehicleCapacity: Int,
        mode: FareSharingMode = FareSharingMode.EQUAL_SPLIT
    ): Int {
        return when (mode) {
            FareSharingMode.EQUAL_SPLIT -> {
                val divisor = if (activePassengers > 0) activePassengers else vehicleCapacity.coerceAtLeast(1)
                (totalTripFare / divisor).coerceAtLeast(100)
            }
            FareSharingMode.FIXED_PER_SEAT -> {
                val divisor = vehicleCapacity.coerceAtLeast(1)
                (totalTripFare / divisor).coerceAtLeast(100)
            }
            FareSharingMode.DISTANCE_WEIGHTED -> {
                val base = (totalTripFare / vehicleCapacity.coerceAtLeast(1))
                (base * 1.1).toInt().coerceAtLeast(100)
            }
        }
    }
}

data class EligibleTripOption(
    val trip: TripEntity,
    val queueEntry: DriverQueueEntryEntity?,
    val routeQueuePosition: Int,
    val distanceMeters: Int,
    val estimatedArrivalMinutes: Int,
    val withinEtaThreshold: Boolean
)

