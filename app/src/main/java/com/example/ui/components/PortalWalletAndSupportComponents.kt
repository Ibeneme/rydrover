package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BookingEntity
import com.example.data.model.CampusEntity
import com.example.data.model.DriverWalletEntity
import com.example.data.model.InstitutionEntity
import com.example.data.model.NotificationEntity
import com.example.data.model.PassengerWalletEntity
import com.example.data.model.PaymentStatus
import com.example.data.model.PlatformSettingsEntity
import com.example.data.model.ReportEntity
import com.example.data.model.TripEntity
import com.example.data.model.UserEntity
import com.example.data.model.UserRole
import com.example.data.model.WalletTransactionEntity
import com.example.data.model.WalletTransactionType
import com.example.ui.theme.RydBrandGreen
import com.example.ui.theme.RydError
import com.example.ui.theme.RydGold
import com.example.ui.theme.RydGoldContainer
import com.example.ui.theme.RydGreenContainer
import com.example.ui.theme.RydGreenPrimary
import com.example.ui.theme.RydOnGoldContainer
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// =============================================================
// EMPTY INSTITUTION TRANSPORT NETWORK FALLBACK CARD (REQ 24)
// =============================================================
@Composable
fun TransportNetworkSetupInProgressCard(
    institutionName: String,
    campusName: String? = null,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)),
        border = BorderStroke(1.dp, Color(0xFFFDE68A)),
        modifier = modifier
            .fillMaxWidth()
            .testTag("transport_network_setup_in_progress_card")
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = CircleShape,
                color = Color(0xFFFEF3C7),
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = null,
                        tint = Color(0xFFB45309),
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Transport network setup is in progress for this institution.",
                fontSize = 14.5.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF92400E),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = buildString {
                    append(institutionName)
                    if (!campusName.isNullOrBlank()) append(" ($campusName)")
                    append(" does not have active routes, zones, or pickup points configured yet. ")
                    append("Your Institution Administrator is configuring campus stops and shuttle corridors.")
                },
                fontSize = 12.sp,
                color = Color(0xFF78350F),
                textAlign = TextAlign.Center,
                lineHeight = 16.sp
            )
        }
    }
}

// =============================================================
// PASSENGER RYDWALLET SCREEN (REQ 4, 5, 6, 8)
// =============================================================
@Composable
fun PassengerRydWalletScreen(
    currentUser: UserEntity?,
    currentInstitution: InstitutionEntity?,
    wallet: PassengerWalletEntity?,
    transactions: List<WalletTransactionEntity>,
    activeBooking: BookingEntity?,
    bookings: List<BookingEntity>,
    platformSettings: PlatformSettingsEntity?,
    onTopUpWallet: (amountNaira: Int, methodLabel: String) -> Unit,
    onGoToFindRide: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddMoneyDialog by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf("ALL") }

    val availableBalance = wallet?.balanceNaira ?: 0
    val heldBalance = wallet?.heldBalanceNaira ?: 0
    val totalRefunded = wallet?.totalRefundedNaira ?: 0
    val totalTopUp = wallet?.totalTopUpNaira ?: 0

    val filteredTransactions = remember(transactions, selectedFilter) {
        when (selectedFilter) {
            "PENDING" -> transactions.filter {
                it.status.equals("HELD", ignoreCase = true) ||
                    it.status.equals("PENDING", ignoreCase = true)
            }
            "REFUNDS" -> transactions.filter { it.type == WalletTransactionType.REFUND }
            "PAYMENTS" -> transactions.filter {
                it.type == WalletTransactionType.RIDE_PAYMENT ||
                    it.type == WalletTransactionType.RIDE_PAYMENT_HOLD
            }
            else -> transactions
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        contentPadding = PaddingValues(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. Main RydWallet Balance Hero Card
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = RydGreenPrimary),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("passenger_rydwallet_card")
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AccountBalanceWallet,
                                contentDescription = null,
                                tint = RydGold,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "PASSENGER RYDWALLET",
                                color = RydGold,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.8.sp
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color.White.copy(alpha = 0.16f)
                        ) {
                            Text(
                                text = platformSettings?.paymentEnvironmentMode?.label ?: "Demo Wallet Mode",
                                color = Color.White,
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Available Balance",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 12.sp
                    )
                    Text(
                        text = formatNaira(availableBalance),
                        color = Color.White,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.testTag("passenger_wallet_balance_text")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Held / Reserved & Refund Summary Pill Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color.White.copy(alpha = 0.14f),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text("Held for Active Ride", color = Color.White.copy(alpha = 0.75f), fontSize = 9.5.sp)
                                Text(
                                    text = formatNaira(heldBalance),
                                    color = RydGold,
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color.White.copy(alpha = 0.14f),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text("Total Refunds", color = Color.White.copy(alpha = 0.75f), fontSize = 9.5.sp)
                                Text(
                                    text = formatNaira(totalRefunded),
                                    color = Color.White,
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color.White.copy(alpha = 0.14f),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text("Total Funded", color = Color.White.copy(alpha = 0.75f), fontSize = 9.5.sp)
                                Text(
                                    text = formatNaira(totalTopUp),
                                    color = Color.White,
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Primary Actions: Add Money & Pay for Ride
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { showAddMoneyDialog = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = RydGold,
                                contentColor = Color(0xFF0F172A)
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("wallet_add_money_button")
                        ) {
                            Icon(Icons.Default.AddCircle, contentDescription = null, modifier = Modifier.size(17.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Add Money", fontWeight = FontWeight.Black, fontSize = 13.sp)
                        }

                        OutlinedButton(
                            onClick = onGoToFindRide,
                            border = BorderStroke(1.2.dp, Color.White),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("wallet_pay_for_ride_button")
                        ) {
                            Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(17.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Book & Pay Ride", fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                        }
                    }
                }
            }
        }

        // 2. Payment Safety & Escrow Protection Banner (REQ 6)
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                border = BorderStroke(1.dp, RydBrandGreen.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = RydGreenPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "RydWallet Payment Safety & Automatic Refund Guarantee",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = "When you tap 'Book & Pay', your fare is securely held against your booking. Funds are only settled to the driver after QR verification and trip completion. Cancelled or unfulfilled rides are automatically refunded.",
                            fontSize = 11.sp,
                            color = Color(0xFF334155),
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        }

        // 3. Active Booking Payment Status Card (if passenger has an active booking)
        if (activeBooking != null) {
            item {
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
                                Text(
                                    text = "CURRENT RIDE PAYMENT STATUS",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    color = RydBrandGreen
                                )
                                Text(
                                    text = "Booking ${activeBooking.bookingReference} • ${formatNaira(activeBooking.totalFareNaira)}",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F172A)
                                )
                            }
                            PaymentStatusChip(status = activeBooking.paymentStatus)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "${activeBooking.pickupLocation} → ${activeBooking.destinationLocation}",
                            fontSize = 12.sp,
                            color = Color(0xFF475569)
                        )
                        Text(
                            text = "Payment Ref: ${activeBooking.paymentReference.ifBlank { "RYD-PAY" }} • Refund Status: ${activeBooking.refundStatus}",
                            fontSize = 11.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }
            }
        }

        // 4. Filter Chips: All Transactions | Pending / Held | Ride Payments | Refunds
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val filters = listOf(
                    "ALL" to "All History (${transactions.size})",
                    "PENDING" to "Pending / Held",
                    "PAYMENTS" to "Ride Payments",
                    "REFUNDS" to "Refunds"
                )
                filters.forEach { (key, label) ->
                    val isSelected = selectedFilter == key
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = if (isSelected) RydGreenPrimary else Color.White,
                        border = BorderStroke(1.dp, if (isSelected) RydGreenPrimary else Color(0xFFCBD5E1)),
                        modifier = Modifier.clickable { selectedFilter = key }
                    ) {
                        Text(
                            text = label,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.White else Color(0xFF334155),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        // 5. Transaction History List
        if (filteredTransactions.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "No matching wallet transactions yet.",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF64748B)
                        )
                    }
                }
            }
        } else {
            items(filteredTransactions) { txn ->
                WalletTransactionRowCard(txn = txn)
            }
        }

        item { Spacer(modifier = Modifier.height(20.dp)) }
    }

    if (showAddMoneyDialog) {
        AddMoneyToRydWalletDialog(
            currentBalance = availableBalance,
            paymentEnvironmentLabel = platformSettings?.paymentEnvironmentMode?.label ?: "Demo Wallet Mode",
            onDismiss = { showAddMoneyDialog = false },
            onConfirmTopUp = { amount, method ->
                onTopUpWallet(amount, method)
                showAddMoneyDialog = false
            }
        )
    }
}

@Composable
fun AddMoneyToRydWalletDialog(
    currentBalance: Int,
    paymentEnvironmentLabel: String,
    onDismiss: () -> Unit,
    onConfirmTopUp: (amountNaira: Int, methodLabel: String) -> Unit
) {
    val quickAmounts = listOf(500, 1000, 2000, 5000, 10000)
    var amountText by remember { mutableStateOf("2000") }
    val methods = listOf(
        "Paystack Card / Bank Transfer",
        "Flutterwave Instant Checkout",
        "Demo / Simulated Wallet Top-Up",
        "Campus Student Bursary Card"
    )
    var selectedMethod by remember { mutableStateOf(methods.first()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "Add Money to RydWallet",
                    fontWeight = FontWeight.Black,
                    fontSize = 17.sp,
                    color = Color(0xFF0F172A)
                )
                Text(
                    text = "Current Balance: ${formatNaira(currentBalance)} • $paymentEnvironmentLabel",
                    fontSize = 11.5.sp,
                    color = Color(0xFF64748B)
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "QUICK AMOUNT",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF475569)
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(quickAmounts) { amt ->
                        val isSelected = amountText == amt.toString()
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) RydGreenPrimary else Color(0xFFF1F5F9),
                            border = BorderStroke(1.dp, if (isSelected) RydGreenPrimary else Color(0xFFCBD5E1)),
                            modifier = Modifier.clickable { amountText = amt.toString() }
                        ) {
                            Text(
                                text = formatNaira(amt),
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else Color(0xFF0F172A),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Top-Up Amount (₦)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("wallet_topup_amount_input")
                )

                Text(
                    text = "SELECT FUNDING CHANNEL",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF475569)
                )

                methods.forEach { method ->
                    val isSelected = selectedMethod == method
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) RydGreenContainer.copy(alpha = 0.65f) else Color(0xFFF8FAFC),
                        border = BorderStroke(1.dp, if (isSelected) RydBrandGreen else Color(0xFFE2E8F0)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedMethod = method }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = method,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = Color(0xFF0F172A)
                            )
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = RydBrandGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountText.toIntOrNull() ?: 0
                    if (amt >= 100) {
                        onConfirmTopUp(amt, selectedMethod)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = RydGreenPrimary),
                modifier = Modifier.testTag("confirm_topup_button")
            ) {
                Text("Fund RydWallet", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// =============================================================
// DRIVER RYDWALLET & EARNINGS SETTLEMENT SCREEN (REQ 7, 8)
// =============================================================
@Composable
fun DriverRydWalletScreen(
    driverUser: UserEntity,
    currentInstitution: InstitutionEntity,
    driverWallet: DriverWalletEntity?,
    transactions: List<WalletTransactionEntity>,
    platformSettings: PlatformSettingsEntity?,
    onWithdrawFunds: (amountNaira: Int, bankName: String, accountNumber: String, accountName: String) -> Unit,
    onUpdateBankInfo: (bankName: String, accountNumber: String, accountName: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showWithdrawDialog by remember { mutableStateOf(false) }
    var showBankSetupDialog by remember { mutableStateOf(false) }

    val availableBalance = driverWallet?.availableBalanceNaira ?: 0
    val pendingSettlement = driverWallet?.pendingSettlementNaira ?: 0
    val totalEarnings = driverWallet?.totalEarningsNaira ?: 0
    val todayEarnings = driverWallet?.todayEarningsNaira ?: 0
    val completedPaidTrips = driverWallet?.completedTripsPaidCount ?: 0
    val platformCommissionPct = platformSettings?.platformCommissionPercent ?: 5

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        contentPadding = PaddingValues(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. Driver Earnings & RydWallet Balance Card
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = RydGreenPrimary),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("driver_rydwallet_card")
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AccountBalanceWallet,
                                contentDescription = null,
                                tint = RydGold,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "DRIVER RYDWALLET • ${currentInstitution.shortCode}",
                                color = RydGold,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.8.sp
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color.White.copy(alpha = 0.16f)
                        ) {
                            Text(
                                text = "${100 - platformCommissionPct}% Driver Settlement",
                                color = Color.White,
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Available Withdrawable Balance",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 12.sp
                    )
                    Text(
                        text = formatNaira(availableBalance),
                        color = Color.White,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.testTag("driver_wallet_balance_text")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // 4-Metric Grid: Pending Settlement, Today's Earnings, Total Earnings, Completed Trip Payments
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color.White.copy(alpha = 0.14f),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text("Pending Escrow", color = Color.White.copy(alpha = 0.75f), fontSize = 9.5.sp)
                                Text(
                                    text = formatNaira(pendingSettlement),
                                    color = RydGold,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color.White.copy(alpha = 0.14f),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text("Today's Net", color = Color.White.copy(alpha = 0.75f), fontSize = 9.5.sp)
                                Text(
                                    text = formatNaira(todayEarnings),
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color.White.copy(alpha = 0.14f),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text("Total Earned", color = Color.White.copy(alpha = 0.75f), fontSize = 9.5.sp)
                                Text(
                                    text = formatNaira(totalEarnings),
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color.White.copy(alpha = 0.14f),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text("Paid Rides", color = Color.White.copy(alpha = 0.75f), fontSize = 9.5.sp)
                                Text(
                                    text = "$completedPaidTrips",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { showWithdrawDialog = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = RydGold,
                                contentColor = Color(0xFF0F172A)
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("driver_withdraw_button")
                        ) {
                            Icon(Icons.Default.AccountBalance, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Withdraw Funds", fontWeight = FontWeight.Black, fontSize = 12.5.sp)
                        }

                        OutlinedButton(
                            onClick = { showBankSetupDialog = true },
                            border = BorderStroke(1.dp, Color.White),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                        ) {
                            Text("Payout Bank Info", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // 2. Bank Settlement Account Card
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "DESIGNATED PAYOUT BANK ACCOUNT",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = RydBrandGreen
                        )
                        Text(
                            text = "${driverWallet?.bankName ?: "Access Bank"} • ${driverWallet?.accountNumber ?: "0123456789"}",
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = "Account Name: ${driverWallet?.accountName ?: driverUser.fullName}",
                            fontSize = 11.5.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                    TextButton(onClick = { showBankSetupDialog = true }) {
                        Text("Edit", fontWeight = FontWeight.Bold, color = RydBrandGreen)
                    }
                }
            }
        }

        // 3. Settlement Rules Notice
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                border = BorderStroke(1.dp, RydBrandGreen.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.VerifiedUser,
                        contentDescription = null,
                        tint = RydGreenPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Settlement Protection: Passenger fares are verified and held in RydWallet when booked. Once you verify passenger QR codes and complete the trip, net earnings (${100 - platformCommissionPct}% after ${platformCommissionPct}% platform service fee) settle immediately into your Available Balance.",
                        fontSize = 11.sp,
                        color = Color(0xFF334155),
                        lineHeight = 15.sp
                    )
                }
            }
        }

        // 4. Payment & Settlement History
        item {
            Text(
                text = "PAYMENT & SETTLEMENT HISTORY (${transactions.size})",
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF475569)
            )
        }

        if (transactions.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No completed trip settlements or withdrawals recorded yet.",
                            fontSize = 12.5.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }
            }
        } else {
            items(transactions) { txn ->
                WalletTransactionRowCard(txn = txn)
            }
        }

        item { Spacer(modifier = Modifier.height(20.dp)) }
    }

    if (showWithdrawDialog) {
        var withdrawAmount by remember { mutableStateOf(availableBalance.coerceAtMost(5000).toString()) }
        var bankName by remember { mutableStateOf(driverWallet?.bankName ?: "Access Bank") }
        var accountNumber by remember { mutableStateOf(driverWallet?.accountNumber ?: "0123456789") }
        var accountName by remember { mutableStateOf(driverWallet?.accountName ?: driverUser.fullName) }

        AlertDialog(
            onDismissRequest = { showWithdrawDialog = false },
            title = { Text("Withdraw Driver Earnings", fontWeight = FontWeight.Black) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Available Balance: ${formatNaira(availableBalance)}",
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = RydGreenPrimary
                    )
                    OutlinedTextField(
                        value = withdrawAmount,
                        onValueChange = { withdrawAmount = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Withdrawal Amount (₦)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = bankName,
                        onValueChange = { bankName = it },
                        label = { Text("Bank Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = accountNumber,
                        onValueChange = { accountNumber = it },
                        label = { Text("10-Digit Account Number") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = accountName,
                        onValueChange = { accountName = it },
                        label = { Text("Account Holder Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = withdrawAmount.toIntOrNull() ?: 0
                        onWithdrawFunds(amt, bankName, accountNumber, accountName)
                        showWithdrawDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RydGreenPrimary)
                ) {
                    Text("Confirm Payout", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showWithdrawDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showBankSetupDialog) {
        var bankName by remember { mutableStateOf(driverWallet?.bankName ?: "Access Bank") }
        var accountNumber by remember { mutableStateOf(driverWallet?.accountNumber ?: "0123456789") }
        var accountName by remember { mutableStateOf(driverWallet?.accountName ?: driverUser.fullName) }

        AlertDialog(
            onDismissRequest = { showBankSetupDialog = false },
            title = { Text("Update Driver Payout Bank", fontWeight = FontWeight.Black) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = bankName,
                        onValueChange = { bankName = it },
                        label = { Text("Bank Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = accountNumber,
                        onValueChange = { accountNumber = it },
                        label = { Text("Account Number") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = accountName,
                        onValueChange = { accountName = it },
                        label = { Text("Account Holder Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onUpdateBankInfo(bankName, accountNumber, accountName)
                        showBankSetupDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RydGreenPrimary)
                ) {
                    Text("Save Bank Info", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showBankSetupDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun WalletTransactionRowCard(txn: WalletTransactionEntity) {
    val isPositive = txn.type == WalletTransactionType.TOP_UP ||
        txn.type == WalletTransactionType.REFUND ||
        txn.type == WalletTransactionType.RIDE_REFUND ||
        txn.type == WalletTransactionType.DRIVER_EARNING ||
        txn.type == WalletTransactionType.DRIVER_EARNING_PENDING ||
        txn.type == WalletTransactionType.DRIVER_EARNING_SETTLED

    val amountPrefix = when (txn.type) {
        WalletTransactionType.TOP_UP,
        WalletTransactionType.REFUND,
        WalletTransactionType.RIDE_REFUND,
        WalletTransactionType.DRIVER_EARNING,
        WalletTransactionType.DRIVER_EARNING_PENDING,
        WalletTransactionType.DRIVER_EARNING_SETTLED -> "+"
        WalletTransactionType.RIDE_PAYMENT_HOLD,
        WalletTransactionType.RIDE_PAYMENT_SETTLED,
        WalletTransactionType.RIDE_PAYMENT,
        WalletTransactionType.DRIVER_WITHDRAWAL,
        WalletTransactionType.WITHDRAWAL,
        WalletTransactionType.PLATFORM_FEE -> "-"
    }

    val amountColor = when {
        txn.type == WalletTransactionType.REFUND || txn.type == WalletTransactionType.RIDE_REFUND -> Color(0xFF0369A1)
        isPositive -> RydGreenPrimary
        txn.status.equals("HELD", ignoreCase = true) -> Color(0xFFB45309)
        else -> Color(0xFF0F172A)
    }

    val dateStr = remember(txn.timestamp) {
        SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(txn.timestamp))
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(5.dp),
                        color = if (isPositive) RydGreenContainer else Color(0xFFF1F5F9)
                    ) {
                        Text(
                            text = txn.type.label,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Black,
                            color = if (isPositive) RydGreenPrimary else Color(0xFF334155),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = txn.status,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (txn.status == "HELD") Color(0xFFB45309) else Color(0xFF64748B)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = txn.description,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF0F172A)
                )
                Text(
                    text = "Ref: ${txn.referenceCode} • $dateStr",
                    fontSize = 10.sp,
                    color = Color(0xFF64748B)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "$amountPrefix${formatNaira(txn.amountNaira)}",
                fontSize = 14.5.sp,
                fontWeight = FontWeight.Black,
                color = amountColor
            )
        }
    }
}

// =============================================================
// PORTAL NOTIFICATIONS, HELP & SUPPORT, COMPLAINTS & TECH REPORTS
// (REQ 14, 15, 16, 17, 18)
// =============================================================
@Composable
fun PortalSupportAndNotificationsScreen(
    currentUser: UserEntity?,
    currentInstitution: InstitutionEntity?,
    currentCampus: CampusEntity?,
    notifications: List<NotificationEntity>,
    myTickets: List<ReportEntity>,
    bookings: List<BookingEntity> = emptyList(),
    trips: List<TripEntity> = emptyList(),
    emergencyContact: String = "+234 800 RYDROVER",
    onMarkNotificationRead: (String) -> Unit,
    onMarkAllNotificationsRead: () -> Unit,
    onSubmitComplaint: (
        category: String,
        subject: String,
        description: String,
        tripId: String,
        bookingId: String,
        reportedDriverId: String,
        reportedVehiclePlate: String
    ) -> Unit,
    onSubmitTechnicalProblem: (
        issueCategory: String,
        description: String,
        tripId: String,
        bookingId: String,
        screenshotReference: String
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    var activeSection by remember { mutableIntStateOf(0) }
    val unreadCount = notifications.count { !it.isRead }

    val role = currentUser?.role ?: UserRole.PASSENGER
    val complaintCategories = remember(role) {
        if (role == UserRole.DRIVER) {
            listOf(
                "Passenger misconduct",
                "No-show at pickup",
                "QR / Boarding dispute",
                "Payment / settlement discrepancy",
                "Route obstruction / security",
                "Other"
            )
        } else {
            listOf(
                "Driver behavior",
                "Overcharging or payment issue",
                "Unsafe driving",
                "Delayed pickup",
                "Wrong route",
                "Vehicle condition",
                "Harassment or misconduct",
                "Other"
            )
        }
    }

    val technicalCategories = remember {
        listOf(
            "App crash",
            "Login problem",
            "GPS/location error",
            "Booking error",
            "QR verification problem",
            "Wallet/payment error",
            "Map not loading",
            "Queue not updating",
            "Other"
        )
    }

    var selectedComplaintCategory by remember { mutableStateOf(complaintCategories.first()) }
    var complaintDescription by remember { mutableStateOf("") }
    var selectedRefBooking by remember { mutableStateOf(bookings.firstOrNull()) }
    var selectedRefTrip by remember { mutableStateOf(trips.firstOrNull()) }

    var selectedTechCategory by remember { mutableStateOf(technicalCategories.first()) }
    var techDescription by remember { mutableStateOf("") }
    var attachedScreenshotRef by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
    ) {
        // Sub-navigation pills: Notifications | Help & FAQs | Submit Complaint | Report Tech Problem
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val sections = listOf(
                0 to "Notifications (${if (unreadCount > 0) "$unreadCount new" else notifications.size})",
                1 to "Submit Complaint",
                2 to "Report Tech Issue",
                3 to "Help, FAQs & Tickets (${myTickets.size})"
            )
            sections.forEach { (idx, label) ->
                val isSelected = activeSection == idx
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = if (isSelected) RydGreenPrimary else Color(0xFFF1F5F9),
                    border = BorderStroke(1.dp, if (isSelected) RydGreenPrimary else Color(0xFFE2E8F0)),
                    modifier = Modifier
                        .clickable { activeSection = idx }
                        .testTag("support_section_tab_$idx")
                ) {
                    Text(
                        text = label,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color.White else Color(0xFF334155),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            contentPadding = PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            when (activeSection) {
                0 -> {
                    // SECTION 0: NOTIFICATIONS
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "REAL-TIME ALERTS & NOTIFICATIONS",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF475569)
                                )
                                Text(
                                    text = "Booking confirmations, RydWallet payments, QR verification & trip updates",
                                    fontSize = 10.5.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                            if (unreadCount > 0) {
                                TextButton(onClick = onMarkAllNotificationsRead) {
                                    Icon(Icons.Default.DoneAll, contentDescription = null, modifier = Modifier.size(15.dp), tint = RydBrandGreen)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Mark All Read", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = RydBrandGreen)
                                }
                            }
                        }
                    }

                    if (notifications.isEmpty()) {
                        item {
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(Icons.Default.Notifications, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(32.dp))
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text("No notifications yet.", fontSize = 13.sp, color = Color(0xFF64748B))
                                }
                            }
                        }
                    } else {
                        items(notifications) { notif ->
                            val timeStr = remember(notif.timestamp) {
                                SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(notif.timestamp))
                            }
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (!notif.isRead) Color(0xFFF0FDF4) else Color.White
                                ),
                                border = BorderStroke(
                                    1.dp,
                                    if (!notif.isRead) RydBrandGreen.copy(alpha = 0.5f) else Color(0xFFE2E8F0)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onMarkNotificationRead(notif.id) }
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Surface(
                                                shape = RoundedCornerShape(5.dp),
                                                color = if (!notif.isRead) RydGreenPrimary else Color(0xFFF1F5F9)
                                            ) {
                                                Text(
                                                    text = notif.category,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Black,
                                                    color = if (!notif.isRead) Color.White else Color(0xFF475569),
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = notif.title,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF0F172A)
                                            )
                                        }
                                        if (!notif.isRead) {
                                            Surface(
                                                shape = CircleShape,
                                                color = RydBrandGreen,
                                                modifier = Modifier.size(8.dp)
                                            ) {}
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = notif.message,
                                        fontSize = 12.sp,
                                        color = Color(0xFF334155),
                                        lineHeight = 16.sp
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = timeStr,
                                        fontSize = 10.sp,
                                        color = Color(0xFF64748B)
                                    )
                                }
                            }
                        }
                    }
                }

                1 -> {
                    // SECTION 1: SUBMIT COMPLAINT (REQ 16)
                    item {
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.ReportProblem, contentDescription = null, tint = Color(0xFFD97706))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = "Submit Ride or Conduct Complaint",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Black,
                                            color = Color(0xFF0F172A)
                                        )
                                        Text(
                                            text = "Routed directly to ${currentInstitution?.shortCode ?: "Institution"} Transport Administration",
                                            fontSize = 11.sp,
                                            color = Color(0xFF64748B)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Text(
                                    text = "1. SELECT COMPLAINT CATEGORY",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF475569)
                                )
                                Spacer(modifier = Modifier.height(6.dp))

                                complaintCategories.forEach { cat ->
                                    val isSelected = selectedComplaintCategory == cat
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isSelected) RydGreenContainer.copy(alpha = 0.65f) else Color(0xFFF8FAFC),
                                        border = BorderStroke(1.dp, if (isSelected) RydBrandGreen else Color(0xFFE2E8F0)),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 2.5.dp)
                                            .clickable { selectedComplaintCategory = cat }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = cat,
                                                fontSize = 12.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                color = Color(0xFF0F172A)
                                            )
                                            if (isSelected) {
                                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = RydBrandGreen, modifier = Modifier.size(16.dp))
                                            }
                                        }
                                    }
                                }

                                if (bookings.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = "2. LINK ASSOCIATED BOOKING / RIDE (OPTIONAL)",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFF475569)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        items(bookings.take(5)) { b ->
                                            val isSelected = selectedRefBooking?.id == b.id
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = if (isSelected) RydGreenPrimary else Color(0xFFF1F5F9),
                                                modifier = Modifier.clickable { selectedRefBooking = b }
                                            ) {
                                                Text(
                                                    text = "${b.bookingReference} (${b.pickupLocation} → ${b.destinationLocation})",
                                                    fontSize = 10.5.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isSelected) Color.White else Color(0xFF334155),
                                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                OutlinedTextField(
                                    value = complaintDescription,
                                    onValueChange = { complaintDescription = it },
                                    label = { Text("Describe what happened in detail") },
                                    placeholder = { Text("Include time, driver/passenger conduct, vehicle plate, or fare issue...") },
                                    minLines = 3,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("complaint_description_input")
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Button(
                                    onClick = {
                                        val refB = selectedRefBooking
                                        val linkedTrip = trips.find { it.id == refB?.tripId } ?: selectedRefTrip
                                        onSubmitComplaint(
                                            selectedComplaintCategory,
                                            selectedComplaintCategory,
                                            complaintDescription,
                                            linkedTrip?.id ?: refB?.tripId ?: "",
                                            refB?.bookingReference ?: "",
                                            linkedTrip?.driverId ?: "",
                                            linkedTrip?.vehicleRegistration ?: ""
                                        )
                                        complaintDescription = ""
                                        activeSection = 3 // Jump to Tickets view to see submitted ticket
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = RydGreenPrimary),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(46.dp)
                                        .testTag("submit_complaint_button")
                                ) {
                                    Text("Submit Formal Complaint", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                2 -> {
                    // SECTION 2: REPORT A TECHNICAL PROBLEM (REQ 17)
                    item {
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.BugReport, contentDescription = null, tint = RydGreenPrimary)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = "Report a Technical Problem",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Black,
                                            color = Color(0xFF0F172A)
                                        )
                                        Text(
                                            text = "Automatically attaches your institution, campus, role & session diagnostics",
                                            fontSize = 11.sp,
                                            color = Color(0xFF64748B)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Text(
                                    text = "1. TECHNICAL ISSUE CATEGORY",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF475569)
                                )
                                Spacer(modifier = Modifier.height(6.dp))

                                technicalCategories.forEach { cat ->
                                    val isSelected = selectedTechCategory == cat
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isSelected) RydGreenContainer.copy(alpha = 0.65f) else Color(0xFFF8FAFC),
                                        border = BorderStroke(1.dp, if (isSelected) RydBrandGreen else Color(0xFFE2E8F0)),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 2.5.dp)
                                            .clickable { selectedTechCategory = cat }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = cat,
                                                fontSize = 12.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                color = Color(0xFF0F172A)
                                            )
                                            if (isSelected) {
                                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = RydBrandGreen, modifier = Modifier.size(16.dp))
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                OutlinedTextField(
                                    value = techDescription,
                                    onValueChange = { techDescription = it },
                                    label = { Text("Describe the technical problem") },
                                    placeholder = { Text("What screen were you on and what error occurred?") },
                                    minLines = 3,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("tech_problem_description_input")
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                // Optional Screenshot / Diagnostic Log Attachment
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedButton(
                                        onClick = {
                                            attachedScreenshotRef = if (attachedScreenshotRef.isBlank()) {
                                                "screenshot_${currentInstitution?.shortCode?.lowercase() ?: "app"}_${System.currentTimeMillis() % 10000}.png"
                                            } else ""
                                        },
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(Icons.Default.AttachFile, contentDescription = null, modifier = Modifier.size(15.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (attachedScreenshotRef.isBlank()) "Attach Diagnostic Screenshot" else "Attached: $attachedScreenshotRef",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Diagnostic Metadata Preview Box
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFFF1F5F9),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text("AUTO-ATTACHED DIAGNOSTIC CONTEXT", fontSize = 9.5.sp, fontWeight = FontWeight.Black, color = Color(0xFF475569))
                                        Text(
                                            text = "User: ${currentUser?.fullName ?: "Active User"} (${role.roleKey}) • Inst: ${currentInstitution?.shortCode ?: "N/A"} (${currentInstitution?.id ?: ""}) • Campus: ${currentCampus?.name ?: "Main"}",
                                            fontSize = 10.5.sp,
                                            color = Color(0xFF334155)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Button(
                                    onClick = {
                                        val refB = selectedRefBooking
                                        val linkedTrip = trips.find { it.id == refB?.tripId } ?: selectedRefTrip
                                        onSubmitTechnicalProblem(
                                            selectedTechCategory,
                                            techDescription,
                                            linkedTrip?.id ?: "",
                                            refB?.bookingReference ?: "",
                                            attachedScreenshotRef
                                        )
                                        techDescription = ""
                                        attachedScreenshotRef = ""
                                        activeSection = 3
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = RydGreenPrimary),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(46.dp)
                                        .testTag("submit_tech_problem_button")
                                ) {
                                    Text("Submit Technical Report", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                3 -> {
                    // SECTION 3: HELP & SUPPORT, CONTACTS, SAFETY GUIDELINES, FAQS & MY TICKETS
                    item {
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.SupportAgent, contentDescription = null, tint = RydGreenPrimary)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "${currentInstitution?.shortCode ?: "Institution"} Transport Desk & RydRover Support",
                                        fontSize = 14.5.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFF0F172A)
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "• Institution Transport Office: ${currentInstitution?.name ?: "Campus Transport Unit"} (${currentCampus?.name ?: "Main Campus"})",
                                    fontSize = 12.sp,
                                    color = Color(0xFF334155)
                                )
                                Text(
                                    text = "• Emergency & Campus Security Hotline: $emergencyContact",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = RydGreenPrimary
                                )
                                Text(
                                    text = "• RydRover Platform Support: support@rydrover.ng",
                                    fontSize = 12.sp,
                                    color = Color(0xFF334155)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(
                                        onClick = { activeSection = 1 },
                                        colors = ButtonDefaults.buttonColors(containerColor = RydGreenPrimary),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("File Complaint", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                                    }
                                    OutlinedButton(
                                        onClick = { activeSection = 2 },
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Report Tech Bug", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    // Safety Guidelines Card
                    item {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                            border = BorderStroke(1.dp, RydBrandGreen.copy(alpha = 0.35f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Security, contentDescription = null, tint = RydGreenPrimary, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Campus Safety & Boarding Guidelines", fontSize = 13.sp, fontWeight = FontWeight.Black, color = Color(0xFF0F172A))
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("1. Verify vehicle plate number and shuttle badge before boarding.", fontSize = 11.5.sp, color = Color(0xFF334155))
                                Text("2. Present your 4-digit Booking Code or QR Pass to the driver upon boarding.", fontSize = 11.5.sp, color = Color(0xFF334155))
                                Text("3. Never pay unrecorded cash when your booking is marked PAID via RydWallet.", fontSize = 11.5.sp, color = Color(0xFF334155))
                            }
                        }
                    }

                    // Frequently Asked Questions (FAQs)
                    item {
                        Text(
                            text = "FREQUENTLY ASKED QUESTIONS (FAQs)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF475569)
                        )
                    }

                    item {
                        FaqExpandableItem(
                            question = "How does RydWallet protect my ride payment?",
                            answer = "When you tap 'Book & Pay', RydWallet reserves the exact fare against your booking. The driver is notified that your booking is PAID, and funds are only released to the driver's withdrawable wallet after your trip completes. If a ride is cancelled before completion, your fare is automatically refunded."
                        )
                    }
                    item {
                        FaqExpandableItem(
                            question = "How are drivers matched to passengers?",
                            answer = "RydRover uses a fair FIFO (First-In, First-Out) Service Zone Queue combined with live GPS proximity. The longest-waiting verified driver in your campus zone heading in your route direction within the ETA threshold is matched first."
                        )
                    }

                    // My Submitted Complaints & Technical Tickets
                    item {
                        Text(
                            text = "MY SUBMITTED COMPLAINTS & SUPPORT TICKETS (${myTickets.size})",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF475569)
                        )
                    }

                    if (myTickets.isEmpty()) {
                        item {
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(20.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("No complaints or technical reports submitted yet.", fontSize = 12.sp, color = Color(0xFF64748B))
                                }
                            }
                        }
                    } else {
                        items(myTickets) { ticket ->
                            Card(
                                shape = RoundedCornerShape(12.dp),
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
                                        Text(
                                            text = ticket.subject,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF0F172A)
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(5.dp),
                                            color = when (ticket.status) {
                                                "RESOLVED" -> RydGreenContainer
                                                "ESCALATED" -> Color(0xFFFEE2E2)
                                                "UNDER_REVIEW" -> Color(0xFFE0F2FE)
                                                else -> Color(0xFFFEF3C7)
                                            }
                                        ) {
                                            Text(
                                                text = ticket.status.replace("_", " "),
                                                fontSize = 9.5.sp,
                                                fontWeight = FontWeight.Black,
                                                color = when (ticket.status) {
                                                    "RESOLVED" -> RydGreenPrimary
                                                    "ESCALATED" -> RydError
                                                    "UNDER_REVIEW" -> Color(0xFF0369A1)
                                                    else -> Color(0xFFB45309)
                                                },
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(ticket.description, fontSize = 12.sp, color = Color(0xFF334155))
                                    if (ticket.adminNotes.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = Color(0xFFF0FDF4),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                text = "Admin Investigation Response: ${ticket.adminNotes}",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = RydGreenPrimary,
                                                modifier = Modifier.padding(8.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FaqExpandableItem(
    question: String,
    answer: String
) {
    var expanded by remember { mutableStateOf(false) }
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = question,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A),
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = Color(0xFF64748B)
                )
            }
            AnimatedVisibility(visible = expanded) {
                Column {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = answer,
                        fontSize = 11.5.sp,
                        color = Color(0xFF475569),
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}
