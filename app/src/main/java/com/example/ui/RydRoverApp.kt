package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.R
import com.example.data.model.UserRole
import com.example.ui.admin.AdminDashboard
import com.example.ui.admin.SuperAdminDashboard
import com.example.ui.auth.RydRoverAuthOnboardingScreen
import com.example.ui.components.RydRoverTopBar
import com.example.ui.driver.DriverScreen
import com.example.ui.passenger.PassengerDashboard
import com.example.ui.theme.RydBrandGreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest

@Composable
fun RydRoverApp(
    viewModel: RydRoverViewModel = viewModel()
) {
    val authOnboardingStep by viewModel.authOnboardingStep.collectAsState()
    val onboardingSelectedRole by viewModel.onboardingSelectedRole.collectAsState()
    val authErrorMessage by viewModel.authErrorMessage.collectAsState()
    val isAuthProcessing by viewModel.isAuthProcessing.collectAsState()

    val currentRole by viewModel.currentRole.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val currentInstitution by viewModel.currentInstitution.collectAsState()
    val currentCampus by viewModel.currentCampus.collectAsState()
    val currentInstitutionCampuses by viewModel.currentInstitutionCampuses.collectAsState()

    // Platform-wide streams (for Super Admin & Institution Selector)
    val allStates by viewModel.allStates.collectAsState()
    val allInstitutions by viewModel.allInstitutions.collectAsState()
    val allCampuses by viewModel.allCampuses.collectAsState()
    val allUsers by viewModel.allUsers.collectAsState()
    val allTrips by viewModel.allTrips.collectAsState()
    val allBookings by viewModel.allBookings.collectAsState()
    val allVehicles by viewModel.allVehicles.collectAsState()
    val allServiceZones by viewModel.allServiceZones.collectAsState()
    val allRoutes by viewModel.allRoutes.collectAsState()
    val pickupLocations by viewModel.pickupLocations.collectAsState()
    val platformSettings by viewModel.platformSettings.collectAsState()
    val superAdminPlatformAnalytics by viewModel.superAdminPlatformAnalytics.collectAsState()

    // Strictly Isolated Institution Network Streams (for Passenger, Driver & Institution Admin)
    val institutionPickupLocations by viewModel.institutionPickupLocations.collectAsState()
    val institutionServiceZones by viewModel.institutionServiceZones.collectAsState()
    val institutionQueueEntries by viewModel.institutionQueueEntries.collectAsState()
    val institutionRoutes by viewModel.institutionRoutes.collectAsState()
    val institutionTrips by viewModel.institutionTrips.collectAsState()
    val institutionUsers by viewModel.institutionUsers.collectAsState()
    val institutionDriverProfiles by viewModel.institutionDriverProfiles.collectAsState()
    val institutionVehicles by viewModel.institutionVehicles.collectAsState()
    val institutionParks by viewModel.institutionParks.collectAsState()
    val institutionBookings by viewModel.institutionBookings.collectAsState()
    val institutionReports by viewModel.institutionReports.collectAsState()
    val adminAnalytics by viewModel.adminAnalytics.collectAsState()

    // Passenger specific streams
    val availableRides by viewModel.availableRides.collectAsState()
    val currentPassengerBookings by viewModel.currentPassengerBookings.collectAsState()
    val latestPassengerActiveBooking by viewModel.latestPassengerActiveBooking.collectAsState()
    val latestPassengerActiveTrip by viewModel.latestPassengerActiveTrip.collectAsState()

    val passengerPickup by viewModel.passengerPickup.collectAsState()
    val passengerDestination by viewModel.passengerDestination.collectAsState()

    // Wallet, Transactions, Notifications & Support Streams
    val currentUserWallet by viewModel.currentUserWallet.collectAsState()
    val currentUserTransactions by viewModel.currentUserTransactions.collectAsState()
    val currentUserNotifications by viewModel.currentUserNotifications.collectAsState()
    val currentUserSupportTickets by viewModel.currentUserSupportTickets.collectAsState()
    val allReports by viewModel.allReports.collectAsState()

    // Splash / Loading screen state: shown when RydRover launches and before opening each role's dashboard
    var isSplashVisible by remember { mutableStateOf(true) }
    var splashTriggerKey by remember { mutableIntStateOf(0) }

    LaunchedEffect(splashTriggerKey) {
        isSplashVisible = true
        val durationMs = if (splashTriggerKey == 0) 2200L else 950L
        delay(durationMs)
        isSplashVisible = false
    }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.snackMessage.collectLatest { msg ->
            if (!msg.isNullOrBlank() && !isSplashVisible) {
                snackbarHostState.showSnackbar(msg)
                viewModel.clearSnackbar()
            }
        }
    }

    Crossfade(
        targetState = isSplashVisible,
        animationSpec = tween(durationMillis = 350),
        label = "rydrover_launch_transition"
    ) { showSplash ->
        if (showSplash) {
            RydRoverSplashScreen()
        } else if (authOnboardingStep != AuthOnboardingStep.AUTHENTICATED_DASHBOARD || currentUser == null) {
            // FIRST-TIME APP OPENING / AUTHENTICATION & 6-STEP ROLE-SELECTION ONBOARDING FLOW
            BackHandler(enabled = authOnboardingStep != AuthOnboardingStep.WELCOME) {
                viewModel.navigateOnboardingBack()
            }
            Scaffold(
                snackbarHost = { SnackbarHost(snackbarHostState) }
            ) { innerPadding ->
                RydRoverAuthOnboardingScreen(
                    currentStep = authOnboardingStep,
                    currentUser = currentUser,
                    selectedRole = onboardingSelectedRole ?: currentUser?.role,
                    selectedInstitution = currentInstitution,
                    selectedCampus = currentCampus,
                    allInstitutions = allInstitutions,
                    campusesForSelectedInstitution = currentInstitutionCampuses,
                    allUsers = allUsers,
                    authErrorMessage = authErrorMessage,
                    isProcessing = isAuthProcessing,
                    onClearError = { viewModel.clearAuthError() },
                    onNavigateToCreateAccount = { viewModel.navigateToCreateAccount() },
                    onNavigateToLogin = { viewModel.navigateToLogin() },
                    onBackToWelcome = { viewModel.navigateToWelcome() },
                    onStepBack = { viewModel.navigateOnboardingBack() },
                    onSubmitLogin = { identifier, password ->
                        viewModel.submitLogin(identifier, password)
                    },
                    onQuickLoginDemoUser = { userId ->
                        viewModel.quickLoginAsUser(userId)
                    },
                    onSubmitInitialAccount = { fullName, phone, email, password, confirmPassword ->
                        viewModel.submitInitialAccount(fullName, phone, email, password, confirmPassword)
                    },
                    onSelectRole = { role ->
                        viewModel.selectOnboardingRole(role)
                    },
                    onSelectInstitution = { institutionId ->
                        viewModel.selectOnboardingInstitution(institutionId)
                    },
                    onSelectCampus = { campusId ->
                        viewModel.selectOnboardingCampus(campusId)
                    },
                    onCompletePassengerProfile = { fullName, phone, studentId, department ->
                        viewModel.completePassengerOnboardingProfile(
                            fullName = fullName,
                            phone = phone,
                            studentIdOrMatric = studentId,
                            departmentOrFaculty = department
                        )
                    },
                    onCompleteDriverProfile = { fullName, phone, licenseNumber, vehicleCategory, vehicleMakeModel, vehicleColor, licensePlate, registrationNumber, availableSeats ->
                        viewModel.completeDriverOnboardingProfile(
                            fullName = fullName,
                            phone = phone,
                            licenseNumber = licenseNumber,
                            vehicleCategory = vehicleCategory,
                            vehicleMakeModel = vehicleMakeModel,
                            vehicleColor = vehicleColor,
                            licensePlate = licensePlate,
                            registrationNumber = registrationNumber,
                            availableSeats = availableSeats
                        )
                    },
                    modifier = Modifier.padding(innerPadding)
                )
            }
        } else {
            // Enforce strict role protection based on the authenticated user's profile role
            val effectiveRole = currentUser?.role ?: currentRole
            Scaffold(
                topBar = {
                    RydRoverTopBar(
                        currentRole = effectiveRole,
                        currentUser = currentUser,
                        currentInstitution = currentInstitution,
                        currentCampus = currentCampus,
                        onResetDemoData = {
                            viewModel.resetDemoData()
                            splashTriggerKey++
                        },
                        onLogout = {
                            viewModel.logout()
                        }
                    )
                },
                snackbarHost = { SnackbarHost(snackbarHostState) }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    when (effectiveRole) {
                        UserRole.PASSENGER -> {
                            // PASSENGER PORTAL: Isolated Institution Transport Network
                            PassengerDashboard(
                                currentUser = currentUser,
                                currentInstitution = currentInstitution,
                                currentCampus = currentCampus,
                                allInstitutions = allInstitutions,
                                allCampuses = allCampuses,
                                trips = availableRides,
                                bookings = currentPassengerBookings,
                                pickupLocations = institutionPickupLocations,
                                serviceZones = institutionServiceZones,
                                routes = institutionRoutes,
                                activeBooking = latestPassengerActiveBooking,
                                activeTrip = latestPassengerActiveTrip,
                                passengerWallet = currentUserWallet,
                                walletTransactions = currentUserTransactions,
                                notifications = currentUserNotifications,
                                supportTickets = currentUserSupportTickets,
                                platformSettings = platformSettings,
                                passengerPickup = passengerPickup,
                                passengerDestination = passengerDestination,
                                onPickupChanged = { viewModel.setPassengerPickup(it) },
                                onDestinationChanged = { viewModel.setPassengerDestination(it) },
                                onFindRides = { viewModel.findRides() },
                                onConfirmRide = { tripId, pickup, dest ->
                                    viewModel.confirmRide(tripId, pickup, dest)
                                },
                                onCancelBooking = { viewModel.cancelPassengerBooking(it) },
                                onTopUpWallet = { amount, method ->
                                    viewModel.addMoneyToPassengerWallet(amount, method)
                                },
                                onRateRide = { bookingId, rating, review ->
                                    viewModel.rateCompletedBooking(bookingId, rating, review)
                                },
                                onMarkNotificationRead = { notificationId ->
                                    viewModel.markNotificationRead(notificationId)
                                },
                                onMarkAllNotificationsRead = {
                                    viewModel.markAllNotificationsRead()
                                },
                                onSubmitComplaint = { category, subject, description, tripId, bookingId, reportedDriverId, reportedVehiclePlate ->
                                    viewModel.submitSupportComplaint(
                                        category = category,
                                        subject = subject,
                                        description = description,
                                        tripId = tripId,
                                        bookingId = bookingId,
                                        reportedDriverId = reportedDriverId,
                                        reportedVehiclePlate = reportedVehiclePlate
                                    )
                                },
                                onSubmitTechnicalProblem = { issueCategory, description, tripId, bookingId, screenshotReference ->
                                    viewModel.submitTechnicalProblemReport(
                                        issueCategory = issueCategory,
                                        description = description,
                                        tripId = tripId,
                                        bookingId = bookingId,
                                        screenshotReference = screenshotReference
                                    )
                                },
                                onSelectInstitutionAndCampus = { instId, campId ->
                                    viewModel.selectPassengerInstitutionAndCampus(instId, campId)
                                },
                                onRegisterPassenger = { name, phone, email, instId, campId ->
                                    viewModel.registerNewPassengerWithInstitution(name, phone, email, instId, campId)
                                }
                            )
                        }

                        UserRole.DRIVER -> {
                            // COMMERCIAL DRIVER PORTAL: Strictly Scoped to Driver's Institution & Campus Context
                            DriverScreen(
                                viewModel = viewModel
                            )
                        }

                        UserRole.ADMIN -> {
                            // INSTITUTION ADMINISTRATOR: Scoped strictly to their authorized institution
                            AdminDashboard(
                                currentInstitution = currentInstitution,
                                currentCampus = currentCampus,
                                analytics = adminAnalytics,
                                allUsers = institutionUsers,
                                driverProfiles = institutionDriverProfiles,
                                pickupLocations = institutionPickupLocations,
                                allTrips = institutionTrips,
                                allBookings = institutionBookings,
                                allVehicles = institutionVehicles,
                                allParks = institutionParks,
                                allServiceZones = institutionServiceZones,
                                allQueueEntries = institutionQueueEntries,
                                allCampuses = currentInstitutionCampuses,
                                allUniversities = listOfNotNull(currentInstitution),
                                allRoutes = institutionRoutes,
                                reports = institutionReports,
                                settings = platformSettings,
                                onVerifyDriver = { driverId, approve -> viewModel.verifyDriver(driverId, approve) },
                                onSuspendDriver = { driverId -> viewModel.suspendDriver(driverId) },
                                onToggleTemporarySkipDriver = { driverId, reason ->
                                    viewModel.toggleTemporarySkipDriver(driverId, reason)
                                },
                                onAddServiceZone = { uId, cId, name, lat, lng, radius, maxEta, routes ->
                                    viewModel.addServiceZone(uId, cId, name, lat, lng, radius, maxEta, routes)
                                },
                                onDeleteServiceZone = { zId -> viewModel.deleteServiceZone(zId) },
                                onRegisterVehicle = { dId, uId, cId, type, reg, model, plate, cap ->
                                    viewModel.registerVehicle(dId, uId, cId, type, reg, model, plate, cap)
                                },
                                onAddUniversity = { name, code, city -> viewModel.addUniversity(name, code, city) },
                                onAddCampus = { uId, name, desc -> viewModel.addCampus(uId, name, desc) },
                                onAddPark = { uId, cId, name, mgr -> viewModel.addTransportPark(uId, cId, name, mgr) },
                                onAddRoute = { uId, cId, orig, dest, corr, fare, stops, mins ->
                                    viewModel.addRoute(uId, cId, orig, dest, corr, fare, stops, mins)
                                },
                                onAddPickupLocation = { uId, cId, name, corr, land ->
                                    viewModel.addDesignatedPickupLocation(uId, cId, name, corr, land)
                                },
                                onDeletePickupLocation = { id -> viewModel.deletePickupLocation(id) },
                                onResolveReport = { id -> viewModel.resolveReport(id) },
                                onUpdateComplaintStatus = { id, status, notes ->
                                    viewModel.updateReportStatus(id, status, notes)
                                }
                            )
                        }

                        UserRole.SUPER_ADMIN -> {
                            // RYDROVER SUPER ADMIN: Nationwide Multi-Institution Platform Console
                            SuperAdminDashboard(
                                platformAnalytics = superAdminPlatformAnalytics,
                                allStates = allStates,
                                allInstitutions = allInstitutions,
                                allCampuses = allCampuses,
                                allServiceZones = allServiceZones,
                                allPickupLocations = pickupLocations,
                                allRoutes = allRoutes,
                                allUsers = allUsers,
                                allVehicles = allVehicles,
                                allTrips = allTrips,
                                allBookings = allBookings,
                                allReports = allReports,
                                platformSettings = platformSettings,
                                onSaveInstitution = { id, name, code, type, stateId, stateName, city, isActive ->
                                    viewModel.saveInstitution(id, name, code, type, stateId, stateName, city, isActive)
                                },
                                onToggleInstitutionActive = { instId, active ->
                                    viewModel.toggleInstitutionActiveStatus(instId, active)
                                },
                                onAssignInstitutionAdmin = { name, email, phone, instId, campId ->
                                    viewModel.assignInstitutionAdmin(name, email, phone, instId, campId)
                                },
                                onAssignDriverInstitutions = { driverId, primaryId, csv ->
                                    viewModel.assignDriverAuthorizedInstitutions(driverId, primaryId, csv)
                                },
                                onAddState = { name, code ->
                                    viewModel.addState(name, code)
                                },
                                onUpdatePlatformSettings = { comm, cutoff, phone, mode ->
                                    viewModel.updatePlatformSettings(comm, cutoff, phone, mode)
                                },
                                onUpdateReportStatus = { id, status, notes ->
                                    viewModel.updateReportStatus(id, status, notes)
                                },
                                onSwitchToInstitutionAdmin = { adminUserId ->
                                    viewModel.switchActiveUserProfile(adminUserId)
                                    splashTriggerKey++
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Full-screen white RydRover Launch / Loading Screen.
 * Displays the exact uploaded RydRover logo centered with generous white space
 * and a subtle RydRover-green circular loading spinner underneath.
 */
@Composable
fun RydRoverSplashScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .testTag("rydrover_splash_screen"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 40.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.rydrover_logo),
                contentDescription = "RydRover - your everyday ride",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .widthIn(min = 200.dp, max = 270.dp)
                    .height(150.dp)
                    .testTag("rydrover_splash_logo")
            )

            Spacer(modifier = Modifier.height(28.dp))

            CircularProgressIndicator(
                color = RydBrandGreen,
                strokeWidth = 2.5.dp,
                modifier = Modifier
                    .size(24.dp)
                    .testTag("rydrover_splash_spinner")
            )
        }
    }
}
