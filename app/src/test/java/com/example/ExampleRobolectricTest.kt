package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.TripEntity
import com.example.data.model.TripStatus
import com.example.data.model.VehicleCategory
import com.example.data.service.RouteCoordinationService
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("RydRover", appName)
  }

  @Test
  fun `verify fare calculation formula`() {
    val trip = TripEntity(
        id = "test_trip",
        tripNumber = "RYD-T-101",
        universityId = "uni_uniport",
        campusId = "camp_uniport_choba",
        parkId = "park_choba_main",
        serviceZoneId = "zone_uniport_choba",
        serviceZoneName = "Choba Service Zone",
        driverId = "d1",
        driverName = "Chidi Amadi",
        driverPhone = "+234 802 334 1122",
        driverRating = 4.9f,
        vehicleCategory = VehicleCategory.COMMERCIAL_TAXI,
        vehicleModel = "Toyota Corolla Commercial Taxi",
        vehicleRegistration = "UNIPORT-SH-01",
        licensePlate = "RIV-892-PH",
        origin = "St. John's Junction, Eneka",
        destination = "Rumuokoro",
        corridor = "Eneka - Rumuokoro Commercial Corridor",
        departureTime = "07:30 AM",
        departureDate = "Today",
        totalFareNaira = 2000,
        totalCapacity = 4,
        bookedSeatsCount = 4,
        pickupPoints = "St. John's Junction, Eneka, Eneka Link Road, Queens Garden City, Centenary, Rumuokoro",
        status = TripStatus.OPEN_FOR_BOOKINGS
    )

    // Formula: Individual passenger fare = Total trip fare ÷ Number of fare-paying passengers
    // Total ₦2,000 ÷ 4 passengers = ₦500 each
    assertEquals(500, trip.calculateCurrentFarePerPassenger(4))
    // Total ₦2,000 ÷ 2 passengers = ₦1,000 each
    assertEquals(1000, trip.calculateCurrentFarePerPassenger(2))
    // 0 passengers fallback to capacity 4 = ₦500
    assertEquals(500, trip.calculateCurrentFarePerPassenger(0))
  }

  @Test
  fun `verify route coordination service finds compatible trips`() {
    val trip = TripEntity(
        id = "test_trip_compat",
        tripNumber = "RYD-T-102",
        universityId = "uni_uniport",
        campusId = "camp_uniport_choba",
        parkId = "park_choba_main",
        serviceZoneId = "zone_uniport_choba",
        serviceZoneName = "Choba Service Zone",
        driverId = "d1",
        driverName = "Chidi Amadi",
        driverPhone = "+234 802 334 1122",
        driverRating = 4.9f,
        vehicleCategory = VehicleCategory.COMMERCIAL_TAXI,
        vehicleModel = "Toyota Corolla Taxi",
        vehicleRegistration = "UNIPORT-SH-01",
        licensePlate = "RIV-892-PH",
        origin = "St. John's Junction, Eneka",
        destination = "Rumuokoro",
        corridor = "Eneka - Rumuokoro Corridor",
        departureTime = "07:30 AM",
        departureDate = "Today",
        totalFareNaira = 2000,
        totalCapacity = 4,
        bookedSeatsCount = 1,
        pickupPoints = "St. John's Junction, Eneka, Queens Garden City, Rumuokoro",
        status = TripStatus.PARTIALLY_BOOKED
    )

    val matches = RouteCoordinationService.findCompatibleTrips(
        trips = listOf(trip),
        pickupJunction = "St. John's Junction, Eneka",
        destination = "Rumuokoro",
        seatsRequested = 2
    )

    assertEquals(1, matches.size)
    assertEquals(3, matches[0].availableSeats)
  }

  @Test
  fun `verify available seats never negative`() {
    val trip = TripEntity(
        id = "test_trip_capacity",
        tripNumber = "RYD-T-103",
        universityId = "uni_uniport",
        campusId = "camp_uniport_choba",
        parkId = "park_choba_main",
        serviceZoneId = "zone_uniport_choba",
        serviceZoneName = "Choba Service Zone",
        driverId = "d1",
        driverName = "Chidi Amadi",
        driverPhone = "+234 802 334 1122",
        driverRating = 4.9f,
        vehicleCategory = VehicleCategory.COMMERCIAL_TAXI,
        vehicleModel = "Toyota Corolla Taxi",
        vehicleRegistration = "UNIPORT-SH-01",
        licensePlate = "RIV-892-PH",
        origin = "St. John's Junction, Eneka",
        destination = "Rumuokoro",
        corridor = "Eneka - Rumuokoro Corridor",
        departureTime = "07:30 AM",
        departureDate = "Today",
        totalFareNaira = 2000,
        totalCapacity = 4,
        bookedSeatsCount = 5, // edge case
        pickupPoints = "St. John's Junction, Eneka",
        status = TripStatus.FULLY_BOOKED
    )

    assertEquals(0, trip.availableSeats)
    assertTrue(trip.isFull)
  }
}
