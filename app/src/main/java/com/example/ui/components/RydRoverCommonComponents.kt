package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AirportShuttle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.EventSeat
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalTaxi
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.SwitchAccount
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.BookingEntity
import com.example.data.model.BookingStatus
import com.example.data.model.CampusEntity
import com.example.data.model.DriverQueueStatus
import com.example.data.model.DriverVerificationStatus
import com.example.data.model.InstitutionEntity
import com.example.data.model.PaymentStatus
import com.example.data.model.TripEntity
import com.example.data.model.TripStatus
import com.example.data.model.UserEntity
import com.example.data.model.UserRole
import com.example.data.model.VehicleCategory
import com.example.ui.theme.DarkRydSurfaceCard
import com.example.ui.theme.RydBrandGreen
import com.example.ui.theme.RydBrandRed
import com.example.ui.theme.RydError
import com.example.ui.theme.RydGold
import com.example.ui.theme.RydGoldContainer
import com.example.ui.theme.RydGreenContainer
import com.example.ui.theme.RydGreenPrimary
import com.example.ui.theme.RydOnGoldContainer
import com.example.ui.theme.RydOnGreenContainer
import com.example.ui.theme.RydSuccess
import java.text.NumberFormat
import java.util.Locale

enum class RydNavTab(val label: String, val icon: ImageVector) {
    HOME("Home", Icons.Default.Home),
    FIND_RIDE("Find Ride", Icons.Default.Search),
    MY_TRIPS("My Ride", Icons.Default.DirectionsCar),
    PROFILE("Profile", Icons.Default.Person)
}

fun formatNaira(amount: Int): String {
    val formatter = NumberFormat.getNumberInstance(Locale.US)
    return "₦${formatter.format(amount)}"
}

@Composable
fun RydRoverTopBar(
    currentRole: UserRole,
    currentUser: UserEntity?,
    currentInstitution: InstitutionEntity? = null,
    currentCampus: CampusEntity? = null,
    onRoleSelected: (UserRole) -> Unit = {},
    onSwitchUserProfile: ((String) -> Unit)? = null,
    onResetDemoData: () -> Unit,
    onLogout: (() -> Unit)? = null
) {
    Surface(
        color = RydGreenPrimary,
        shadowElevation = 3.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Official RydRover Brand Logo Badge (Exact Logo Asset as instructed)
                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(8.dp),
                    shadowElevation = 2.dp
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.rydrover_logo),
                        contentDescription = "RydRover - your everyday ride",
                        modifier = Modifier
                            .height(38.dp)
                            .widthIn(max = 160.dp)
                            .padding(horizontal = 8.dp, vertical = 3.dp),
                        contentScale = ContentScale.Fit
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Role-Locked Portal Indicator (Non-interactive: Portal Switching Removed)
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White.copy(alpha = 0.22f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = "Role-Locked Portal",
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = when (currentRole) {
                                    UserRole.PASSENGER -> "Passenger"
                                    UserRole.DRIVER -> "Driver"
                                    UserRole.ADMIN -> "Inst. Admin"
                                    UserRole.SUPER_ADMIN -> "Super Admin"
                                },
                                color = Color.White,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Reset Demo Action Button
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White.copy(alpha = 0.16f),
                        modifier = Modifier
                            .clickable { onResetDemoData() }
                            .testTag("reset_demo_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.SwapHoriz,
                                contentDescription = "Reset Demo",
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "Reset",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    if (onLogout != null) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White.copy(alpha = 0.24f),
                            modifier = Modifier
                                .clickable { onLogout() }
                                .testTag("logout_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                    contentDescription = "Log Out",
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Log Out",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Portal Context Banner (Clean mobile-first sub-header)
            Spacer(modifier = Modifier.height(5.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Color.White.copy(alpha = 0.25f)
                ) {
                    Text(
                        text = when (currentRole) {
                            UserRole.PASSENGER -> "PASSENGER PORTAL"
                            UserRole.DRIVER -> "DRIVER PORTAL"
                            UserRole.ADMIN -> "INSTITUTION ADMIN PORTAL"
                            UserRole.SUPER_ADMIN -> "RYDROVER SUPER ADMIN PORTAL"
                        },
                        color = Color.White,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                currentUser?.let { user ->
                    val contextLabel = if (currentRole == UserRole.SUPER_ADMIN) {
                        "${user.fullName} • Nationwide Platform"
                    } else {
                        val instCode = currentInstitution?.shortCode ?: "Institution"
                        val campName = currentCampus?.name ?: "Campus"
                        "${user.fullName} • $instCode ($campName)"
                    }
                    Text(
                        text = contextLabel,
                        color = Color.White.copy(alpha = 0.92f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun PaymentStatusChip(status: PaymentStatus) {
    PaymentStatusChip(statusLabel = status.label)
}

@Composable
fun PaymentStatusChip(statusLabel: String) {
    val (bgColor, textColor) = when (statusLabel.uppercase()) {
        "PAID" -> Color(0xFFDCFCE7) to Color(0xFF166534)
        "PENDING", "HELD" -> Color(0xFFFEF3C7) to Color(0xFF92400E)
        "REFUNDED" -> Color(0xFFE0F2FE) to Color(0xFF0369A1)
        "FAILED", "CANCELLED" -> Color(0xFFFEE2E2) to Color(0xFF991B1B)
        else -> Color(0xFFF1F5F9) to Color(0xFF334155)
    }
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = bgColor
    ) {
        Text(
            text = statusLabel.uppercase(),
            color = textColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
        )
    }
}

@Composable
fun BoardingStatusChip(status: String) {
    val (bgColor, textColor) = when (status) {
        "Waiting", "Requested" -> Pair(RydGoldContainer, RydOnGoldContainer)
        "Accepted", "Accepted - En Route" -> Pair(Color(0xFFE0F2FE), Color(0xFF0369A1))
        "Arrived at Pickup" -> Pair(Color(0xFFDBEAFE), Color(0xFF1D4ED8))
        "Boarded" -> Pair(RydGreenContainer, RydOnGreenContainer)
        "Completed" -> Pair(Color(0xFFE2E8F0), Color(0xFF334155))
        "Cancelled", "Rejected" -> Pair(Color(0xFFFEE2E2), RydError)
        else -> Pair(Color(0xFFF1F5F9), Color(0xFF475569))
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = status,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

@Composable
fun BookingStatusChip(status: BookingStatus) {
    val (bgColor, textColor) = when (status) {
        BookingStatus.CONFIRMED -> Pair(RydGreenContainer, RydOnGreenContainer)
        BookingStatus.PENDING, BookingStatus.PENDING_APPROVAL -> Pair(RydGoldContainer, RydOnGoldContainer)
        BookingStatus.REJECTED -> Pair(Color(0xFFFEE2E2), RydError)
        BookingStatus.COMPLETED -> Pair(Color(0xFFF1F5F9), Color(0xFF475569))
        BookingStatus.CANCELLED -> Pair(Color(0xFFFEE2E2), RydError)
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = status.label,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

@Composable
fun TripStatusChip(status: TripStatus) {
    val (bgColor, textColor, icon) = when (status) {
        TripStatus.OPEN_FOR_BOOKINGS -> Triple(Color(0xFFE0F2FE), Color(0xFF0369A1), Icons.Default.HourglassTop)
        TripStatus.PARTIALLY_BOOKED -> Triple(RydGoldContainer, RydOnGoldContainer, Icons.Default.Group)
        TripStatus.FULLY_BOOKED -> Triple(Color(0xFFF3E8FF), Color(0xFF6B21A8), Icons.Default.EventSeat)
        TripStatus.CONFIRMED -> Triple(RydGreenContainer, RydOnGreenContainer, Icons.Default.CheckCircle)
        TripStatus.IN_PROGRESS -> Triple(Color(0xFFCCFBF1), Color(0xFF0F766E), Icons.Default.Navigation)
        TripStatus.COMPLETED -> Triple(Color(0xFFF1F5F9), Color(0xFF475569), Icons.Default.CheckCircle)
        TripStatus.CANCELLED -> Triple(Color(0xFFFEE2E2), RydError, Icons.Default.Warning)
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = status.label,
                color = textColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun DriverVerificationBadge(status: DriverVerificationStatus) {
    val (bgColor, textColor, label) = when (status) {
        DriverVerificationStatus.VERIFIED -> Triple(RydGreenContainer, RydBrandGreen, "Verified")
        DriverVerificationStatus.PENDING -> Triple(RydGoldContainer, Color(0xFFB45309), "Pending")
        DriverVerificationStatus.REJECTED -> Triple(Color(0xFFFEE2E2), RydError, "Suspended")
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (status == DriverVerificationStatus.VERIFIED) Icons.Default.Verified else Icons.Default.Warning,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                color = textColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun VehicleIconBadge(category: VehicleCategory) {
    val icon = when (category) {
        VehicleCategory.COMMERCIAL_TAXI -> Icons.Default.LocalTaxi
        VehicleCategory.SEDAN -> Icons.Default.DirectionsCar
        VehicleCategory.SUV_SIENNA -> Icons.Default.AirportShuttle
        VehicleCategory.MINIBUS -> Icons.Default.DirectionsBus
        VehicleCategory.APPROVED_TRANSIT -> Icons.Default.DirectionsCar
    }
    Surface(
        shape = CircleShape,
        color = RydGreenContainer.copy(alpha = 0.5f),
        modifier = Modifier.size(36.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = category.label,
                tint = RydGreenPrimary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun SeatAvailabilityIndicator(availableSeats: Int, totalCapacity: Int) {
    val booked = (totalCapacity - availableSeats).coerceAtLeast(0)
    val progress = if (totalCapacity > 0) booked.toFloat() / totalCapacity else 0f

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$availableSeats of $totalCapacity seats remaining",
                color = if (availableSeats > 0) Color(0xFF0F172A) else RydError,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "$booked booked",
                color = Color(0xFF64748B),
                fontSize = 11.sp
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = if (availableSeats > 0) RydBrandGreen else RydError,
            trackColor = Color(0xFFE2E8F0)
        )
    }
}

// Live Campus Corridor Map Component
@Composable
fun UniversityLiveMapView(
    trip: TripEntity?,
    passengerPickup: String,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse"
    )

    val isTrackingLive = trip?.liveTrackingActive == true

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (isTrackingLive) RydBrandGreen else Color(0xFFF59E0B))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isTrackingLive) "LIVE VEHICLE TRACKING" else "CAMPUS ROUTE RADAR",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = if (isTrackingLive) RydBrandGreen else Color(0xFFB45309),
                        letterSpacing = 0.5.sp
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (isTrackingLive) RydGreenContainer else Color(0xFFFEF3C7)
                ) {
                    Text(
                        text = if (isTrackingLive) "GPS Active" else "Live tracking setup required",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isTrackingLive) RydOnGreenContainer else Color(0xFF92400E),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Canvas Corridor Map Visualizer
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF8FAFC))
            ) {
                Canvas(modifier = Modifier.fillMaxWidth().height(130.dp)) {
                    val w = size.width
                    val h = size.height

                    // Grid / campus streets background
                    val roadY = h * 0.52f
                    drawLine(
                        color = Color(0xFFE2E8F0),
                        start = Offset(20f, roadY),
                        end = Offset(w - 20f, roadY),
                        strokeWidth = 14f,
                        cap = StrokeCap.Round
                    )
                    drawLine(
                        color = Color(0xFFCBD5E1),
                        start = Offset(20f, roadY),
                        end = Offset(w - 20f, roadY),
                        strokeWidth = 2f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 12f), 0f)
                    )

                    // Waypoint 1: Transport Park
                    val p1 = Offset(35f, roadY)
                    drawCircle(Color(0xFF0369A1), radius = 7f, center = p1)

                    // Waypoint 2: Main Gate
                    val p2 = Offset(w * 0.30f, roadY)
                    drawCircle(Color(0xFF64748B), radius = 6f, center = p2)

                    // Waypoint 3: Faculty Gate
                    val p3 = Offset(w * 0.55f, roadY)
                    drawCircle(Color(0xFF64748B), radius = 6f, center = p3)

                    // Waypoint 4: Medical Centre / Destination
                    val p4 = Offset(w - 35f, roadY)
                    drawCircle(RydBrandGreen, radius = 8f, center = p4)

                    // Vehicle position (interpolated or simulated live location on corridor)
                    val vehicleFraction = if (isTrackingLive) {
                        (0.25f + pulseProgress * 0.50f).coerceIn(0.15f, 0.85f)
                    } else {
                        0.25f
                    }
                    val vehiclePos = Offset(w * vehicleFraction, roadY)

                    // Pulse wave if live tracking
                    if (isTrackingLive) {
                        drawCircle(
                            color = RydBrandGreen.copy(alpha = (1f - pulseProgress).coerceAtLeast(0f)),
                            radius = 16f + pulseProgress * 22f,
                            center = vehiclePos,
                            style = Stroke(width = 2.5f)
                        )
                    }

                    // Vehicle Marker
                    drawCircle(color = Color.White, radius = 12f, center = vehiclePos)
                    drawCircle(color = RydGreenPrimary, radius = 9f, center = vehiclePos)
                }

                // Landmark Labels overlay
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                        .align(Alignment.BottomCenter),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(trip?.serviceZoneName ?: trip?.origin ?: "Service Zone", fontSize = 9.sp, color = Color(0xFF0369A1), fontWeight = FontWeight.Bold)
                    Text("Passenger GPS", fontSize = 9.sp, color = Color(0xFF166534), fontWeight = FontWeight.Bold)
                    Text("Pickup Point", fontSize = 9.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Medium)
                    Text(trip?.destination ?: "Destination", fontSize = 9.sp, color = RydBrandGreen, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Explanation notice (Google Play & live location compliance requirement)
            if (!isTrackingLive) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = Color(0xFFD97706),
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Live tracking setup required: Vehicle location will stream live once driver taps 'Start Trip' and connects to campus GPS.",
                        fontSize = 10.sp,
                        color = Color(0xFF92400E),
                        lineHeight = 13.sp
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = RydBrandGreen,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Vehicle ${trip?.vehicleRegistration ?: "SHUTTLE"} (${trip?.driverLat ?: 0.0}, ${trip?.driverLng ?: 0.0}) • ${trip?.distanceToPassengerMeters ?: 250}m to ${passengerPickup.ifBlank { "Passenger GPS" }} • ETA ~${trip?.estimatedArrivalMinutes ?: 3} mins. Other passengers' exact GPS hidden.",
                        fontSize = 10.5.sp,
                        color = Color(0xFF166534),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun DriverQueueStatusChip(status: DriverQueueStatus) {
    val (bgColor, textColor) = when (status) {
        DriverQueueStatus.QUEUED -> Pair(RydGreenContainer, RydOnGreenContainer)
        DriverQueueStatus.AVAILABLE, DriverQueueStatus.ONLINE -> Pair(Color(0xFFE0F2FE), Color(0xFF0369A1))
        DriverQueueStatus.BOOKED, DriverQueueStatus.EN_ROUTE_TO_PASSENGER -> Pair(RydGoldContainer, RydOnGoldContainer)
        DriverQueueStatus.PASSENGER_BOARDING -> Pair(Color(0xFFFEF3C7), Color(0xFF92400E))
        DriverQueueStatus.TRIP_ACTIVE -> Pair(Color(0xFFDCFCE7), Color(0xFF166534))
        DriverQueueStatus.TRIP_COMPLETED -> Pair(Color(0xFFF1F5F9), Color(0xFF334155))
        DriverQueueStatus.TEMPORARILY_UNAVAILABLE, DriverQueueStatus.OFFLINE -> Pair(Color(0xFFFEE2E2), RydError)
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = status.label,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun BookingQrCodeCard(
    booking: BookingEntity,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (booking.isQrVerified) RydBrandGreen else Color(0xFFCBD5E1)
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "BOOKING VERIFICATION & QR PASS",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF475569),
                        letterSpacing = 0.6.sp
                    )
                    Text(
                        text = "Booking ID: ${booking.bookingReference}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF0F172A)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (booking.isQrVerified) RydGreenContainer else RydGoldContainer
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (booking.isQrVerified) Icons.Default.Verified else Icons.Default.HourglassTop,
                            contentDescription = null,
                            tint = if (booking.isQrVerified) RydBrandGreen else Color(0xFFB45309),
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (booking.isQrVerified) "QR Verified" else "Show to Driver",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (booking.isQrVerified) RydOnGreenContainer else RydOnGoldContainer
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                // Visual Deterministic QR Matrix
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color.White,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier.size(116.dp)
                ) {
                    Canvas(
                        modifier = Modifier
                            .padding(8.dp)
                            .size(100.dp)
                    ) {
                        val gridSize = 15
                        val cellW = size.width / gridSize
                        val cellH = size.height / gridSize
                        val seed = booking.qrCodePayload.hashCode()

                        fun drawFinderPattern(startCol: Int, startRow: Int) {
                            for (r in 0..4) {
                                for (c in 0..4) {
                                    val isBorder = r == 0 || r == 4 || c == 0 || c == 4
                                    val isCenter = r == 2 && c == 2
                                    if (isBorder || isCenter) {
                                        drawRect(
                                            color = Color(0xFF0F172A),
                                            topLeft = Offset((startCol + c) * cellW, (startRow + r) * cellH),
                                            size = Size(cellW * 0.95f, cellH * 0.95f)
                                        )
                                    }
                                }
                            }
                        }

                        drawFinderPattern(0, 0)
                        drawFinderPattern(gridSize - 5, 0)
                        drawFinderPattern(0, gridSize - 5)

                        for (r in 0 until gridSize) {
                            for (c in 0 until gridSize) {
                                val inTopLeft = r <= 5 && c <= 5
                                val inTopRight = r <= 5 && c >= gridSize - 6
                                val inBottomLeft = r >= gridSize - 6 && c <= 5
                                if (!inTopLeft && !inTopRight && !inBottomLeft) {
                                    val bit = ((seed ushr ((r * gridSize + c) % 28)) xor (r * 31 + c * 17)) and 1
                                    if (bit == 1) {
                                        drawRect(
                                            color = if ((r + c) % 5 == 0) RydGreenPrimary else Color(0xFF0F172A),
                                            topLeft = Offset(c * cellW, r * cellH),
                                            size = Size(cellW * 0.92f, cellH * 0.92f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 4-Digit Verification Code Box
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(start = 12.dp)
                ) {
                    Text(
                        text = "VERIFICATION CODE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF64748B)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = RydGreenContainer,
                        border = androidx.compose.foundation.BorderStroke(1.dp, RydBrandGreen.copy(alpha = 0.4f))
                    ) {
                        Text(
                            text = booking.verificationCode,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            color = RydOnGreenContainer,
                            letterSpacing = 3.sp,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Present QR or 4-digit code\nto driver when boarding",
                        fontSize = 10.5.sp,
                        color = Color(0xFF475569),
                        textAlign = TextAlign.Center,
                        lineHeight = 13.sp
                    )
                }
            }
        }
    }
}

