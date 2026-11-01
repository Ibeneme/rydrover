package com.example.ui.auth

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.EventSeat
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.CampusEntity
import com.example.data.model.InstitutionEntity
import com.example.data.model.InstitutionType
import com.example.data.model.UserEntity
import com.example.data.model.UserRole
import com.example.data.model.VehicleCategory
import com.example.ui.AuthOnboardingStep
import com.example.ui.theme.RydBrandGreen
import com.example.ui.theme.RydError
import com.example.ui.theme.RydGoldContainer
import com.example.ui.theme.RydGreenContainer
import com.example.ui.theme.RydGreenPrimary
import com.example.ui.theme.RydOnGoldContainer
import com.example.ui.theme.RydOnGreenContainer

@Composable
fun RydRoverAuthOnboardingScreen(
    currentStep: AuthOnboardingStep,
    currentUser: UserEntity?,
    selectedRole: UserRole?,
    selectedInstitution: InstitutionEntity?,
    selectedCampus: CampusEntity?,
    allInstitutions: List<InstitutionEntity>,
    campusesForSelectedInstitution: List<CampusEntity>,
    allUsers: List<UserEntity>,
    authErrorMessage: String?,
    isProcessing: Boolean,
    onClearError: () -> Unit,
    onNavigateToCreateAccount: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onBackToWelcome: () -> Unit,
    onStepBack: () -> Unit,
    onSubmitLogin: (identifier: String, password: String) -> Unit,
    onQuickLoginDemoUser: (userId: String) -> Unit,
    onSubmitInitialAccount: (fullName: String, phone: String, email: String, password: String, confirmPassword: String) -> Unit,
    onSelectRole: (UserRole) -> Unit,
    onSelectInstitution: (institutionId: String) -> Unit,
    onSelectCampus: (campusId: String) -> Unit,
    onCompletePassengerProfile: (fullName: String, phone: String, studentId: String, department: String) -> Unit,
    onCompleteDriverProfile: (
        fullName: String,
        phone: String,
        licenseNumber: String,
        vehicleCategory: VehicleCategory,
        vehicleMakeModel: String,
        vehicleColor: String,
        licensePlate: String,
        registrationNumber: String,
        availableSeats: Int
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC)),
        color = Color(0xFFF8FAFC)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
        ) {
            // Show 6-step onboarding progress header when in registration steps (1..5)
            if (currentStep.stepNumber in 1..5) {
                OnboardingProgressHeader(
                    currentStep = currentStep,
                    onBackClick = onStepBack
                )
            }

            // Show error message banner if any validation or auth error occurred
            if (!authErrorMessage.isNullOrBlank()) {
                Surface(
                    color = Color(0xFFFEE2E2),
                    border = BorderStroke(1.dp, RydError.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Validation error",
                            tint = RydError,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = authErrorMessage,
                            color = RydError,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = onClearError) {
                            Text("Dismiss", color = RydError, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            AnimatedContent(
                targetState = currentStep,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "auth_onboarding_step_transition",
                modifier = Modifier.weight(1f)
            ) { step ->
                when (step) {
                    AuthOnboardingStep.WELCOME -> {
                        WelcomeAuthScreen(
                            onCreateAccountClick = onNavigateToCreateAccount,
                            onLoginClick = onNavigateToLogin
                        )
                    }

                    AuthOnboardingStep.LOGIN -> {
                        LoginAuthScreen(
                            allUsers = allUsers,
                            allInstitutions = allInstitutions,
                            isProcessing = isProcessing,
                            onBackClick = onBackToWelcome,
                            onCreateAccountClick = onNavigateToCreateAccount,
                            onLoginSubmit = onSubmitLogin,
                            onQuickLoginDemoUser = onQuickLoginDemoUser
                        )
                    }

                    AuthOnboardingStep.SIGN_UP_ACCOUNT -> {
                        SignUpAccountFormScreen(
                            isProcessing = isProcessing,
                            onLoginInsteadClick = onNavigateToLogin,
                            onSubmitAccount = onSubmitInitialAccount
                        )
                    }

                    AuthOnboardingStep.ROLE_SELECTION -> {
                        RoleSelectionScreen(
                            userName = currentUser?.fullName ?: "",
                            initialSelectedRole = selectedRole,
                            isProcessing = isProcessing,
                            onSelectRole = onSelectRole
                        )
                    }

                    AuthOnboardingStep.INSTITUTION_SELECTION -> {
                        InstitutionSelectionScreen(
                            selectedRole = selectedRole ?: currentUser?.role ?: UserRole.PASSENGER,
                            institutions = allInstitutions.filter { it.isActive },
                            initialInstitutionId = selectedInstitution?.id,
                            isProcessing = isProcessing,
                            onSelectInstitution = onSelectInstitution
                        )
                    }

                    AuthOnboardingStep.CAMPUS_SELECTION -> {
                        CampusSelectionScreen(
                            institution = selectedInstitution,
                            campuses = campusesForSelectedInstitution,
                            initialCampusId = selectedCampus?.id,
                            isProcessing = isProcessing,
                            onSelectCampus = onSelectCampus
                        )
                    }

                    AuthOnboardingStep.PROFILE_SETUP -> {
                        val effectiveRole = selectedRole ?: currentUser?.role ?: UserRole.PASSENGER
                        if (effectiveRole == UserRole.DRIVER) {
                            DriverOnboardingProfileScreen(
                                currentUser = currentUser,
                                institution = selectedInstitution,
                                campus = selectedCampus,
                                isProcessing = isProcessing,
                                onCompleteDriverProfile = onCompleteDriverProfile
                            )
                        } else {
                            PassengerOnboardingProfileScreen(
                                currentUser = currentUser,
                                institution = selectedInstitution,
                                campus = selectedCampus,
                                isProcessing = isProcessing,
                                onCompletePassengerProfile = onCompletePassengerProfile
                            )
                        }
                    }

                    AuthOnboardingStep.AUTHENTICATED_DASHBOARD -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = RydBrandGreen)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OnboardingProgressHeader(
    currentStep: AuthOnboardingStep,
    onBackClick: () -> Unit
) {
    val steps = listOf(
        1 to "Account",
        2 to "Role",
        3 to "Institution",
        4 to "Campus",
        5 to "Profile",
        6 to "Dashboard"
    )

    Surface(
        color = Color.White,
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .size(40.dp)
                        .testTag("onboarding_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color(0xFF0F172A)
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Step ${currentStep.stepNumber} of 6 • ${currentStep.stepTitle}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = RydBrandGreen
                    )
                    Text(
                        text = "RydRover Multi-Institution Onboarding",
                        fontSize = 11.sp,
                        color = Color(0xFF64748B)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 6-step visual indicator: 1. Account -> 2. Role -> 3. Institution -> 4. Campus -> 5. Profile -> 6. Dashboard
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                steps.forEach { (num, label) ->
                    val isCompleted = num < currentStep.stepNumber
                    val isCurrent = num == currentStep.stepNumber
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = when {
                            isCurrent -> RydBrandGreen
                            isCompleted -> RydGreenContainer
                            else -> Color(0xFFF1F5F9)
                        },
                        border = if (isCurrent) null else BorderStroke(
                            1.dp,
                            if (isCompleted) RydBrandGreen.copy(alpha = 0.4f) else Color(0xFFE2E8F0)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (isCompleted) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Completed",
                                    tint = RydBrandGreen,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                            Text(
                                text = "$num. $label",
                                fontSize = 11.sp,
                                fontWeight = if (isCurrent || isCompleted) FontWeight.Bold else FontWeight.Medium,
                                color = when {
                                    isCurrent -> Color.White
                                    isCompleted -> RydOnGreenContainer
                                    else -> Color(0xFF64748B)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

// ============================================================================
// 2. WELCOME / AUTHENTICATION SCREEN
// ============================================================================
@Composable
private fun WelcomeAuthScreen(
    onCreateAccountClick: () -> Unit,
    onLoginClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 24.dp, vertical = 20.dp)
            .testTag("rydrover_welcome_auth_screen"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 440.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Exact uploaded RydRover logo
            Image(
                painter = painterResource(id = R.drawable.rydrover_logo),
                contentDescription = "RydRover - your everyday ride",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .widthIn(min = 190.dp, max = 260.dp)
                    .height(140.dp)
                    .testTag("auth_welcome_logo")
            )

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "Welcome to RydRover",
                fontSize = 26.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF0F172A),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Move around your institution with ease.",
                fontSize = 15.5.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF475569),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Feature highlights card
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFFF8FAFC),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    WelcomeFeatureRow(
                        icon = Icons.Default.School,
                        title = "Multi-Institution Campus Transit",
                        subtitle = "Dedicated transport zones, pickup points & routes isolated to your institution."
                    )
                    WelcomeFeatureRow(
                        icon = Icons.Default.DirectionsCar,
                        title = "Verified Campus Shuttles & FIFO Queue",
                        subtitle = "Book seats on verified campus vehicles or register as a commercial driver."
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Primary Button: "Create Account"
            Button(
                onClick = onCreateAccountClick,
                colors = ButtonDefaults.buttonColors(containerColor = RydGreenPrimary),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("create_account_button")
            ) {
                Text(
                    text = "Create Account",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Secondary Option: "Already have an account? Log In"
            OutlinedButton(
                onClick = onLoginClick,
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.5.dp, RydGreenPrimary),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("already_have_account_login_button")
            ) {
                Text(
                    text = "Already have an account? Log In",
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = RydGreenPrimary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun WelcomeFeatureRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String
) {
    Row(verticalAlignment = Alignment.Top) {
        Surface(
            shape = CircleShape,
            color = RydGreenContainer,
            modifier = Modifier.size(38.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = RydBrandGreen,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
            )
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = Color(0xFF64748B)
            )
        }
    }
}

// ============================================================================
// RETURNING USER LOGIN SCREEN
// ============================================================================
@Composable
private fun LoginAuthScreen(
    allUsers: List<UserEntity>,
    allInstitutions: List<InstitutionEntity>,
    isProcessing: Boolean,
    onBackClick: () -> Unit,
    onCreateAccountClick: () -> Unit,
    onLoginSubmit: (identifier: String, password: String) -> Unit,
    onQuickLoginDemoUser: (userId: String) -> Unit
) {
    var identifier by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp)
            .testTag("rydrover_login_screen"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back to welcome"
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Log In to RydRover",
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF0F172A)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Image(
            painter = painterResource(id = R.drawable.rydrover_logo),
            contentDescription = "RydRover Logo",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .widthIn(max = 180.dp)
                .height(90.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Welcome Back",
            fontSize = 22.sp,
            fontWeight = FontWeight.Black,
            color = Color(0xFF0F172A)
        )
        Text(
            text = "Sign in to load your institution's transport dashboard.",
            fontSize = 13.5.sp,
            color = Color(0xFF64748B),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(22.dp))

        OutlinedTextField(
            value = identifier,
            onValueChange = { identifier = it },
            label = { Text("Email Address or Phone Number") },
            placeholder = { Text("e.g., tari.horsfall@ust.edu.ng or +234...") },
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("login_identifier_input")
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = "Toggle password visibility"
                    )
                }
            },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("login_password_input")
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = { onLoginSubmit(identifier, password) },
            enabled = !isProcessing,
            colors = ButtonDefaults.buttonColors(containerColor = RydGreenPrimary),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("login_submit_button")
        ) {
            if (isProcessing) {
                CircularProgressIndicator(
                    color = Color.White,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(20.dp)
                )
            } else {
                Text(
                    text = "Log In",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        TextButton(
            onClick = onCreateAccountClick,
            modifier = Modifier.testTag("login_go_to_signup_button")
        ) {
            Text(
                text = "New to RydRover? Create Account",
                color = RydGreenPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 13.5.sp
            )
        }

        Spacer(modifier = Modifier.height(18.dp))
        HorizontalDivider(color = Color(0xFFE2E8F0))
        Spacer(modifier = Modifier.height(14.dp))

        // Returning Users & Admin Accounts Quick Access (Preserves existing accounts & admin login)
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = Color(0xFFF8FAFC),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "RETURNING USER & ADMINISTRATOR ACCOUNTS",
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF475569),
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Tap an existing seeded account below to sign in directly to its assigned institution & role dashboard (default password: rydrover123):",
                    fontSize = 11.5.sp,
                    color = Color(0xFF64748B)
                )
                Spacer(modifier = Modifier.height(10.dp))

                val quickAccounts = remember(allUsers, allInstitutions) {
                    val order = listOf(
                        "usr_pass_rsu_1",
                        "usr_driv_rsu_1",
                        "usr_pass_1",
                        "usr_driv_1",
                        "usr_driv_ceapoly_1",
                        "usr_admin_rsu_1",
                        "usr_admin_1",
                        "usr_super_admin"
                    )
                    order.mapNotNull { id -> allUsers.find { it.id == id } }
                        .ifEmpty { allUsers.take(6) }
                }

                quickAccounts.forEach { acct ->
                    val instCode = allInstitutions.find { it.id == acct.institutionId }?.shortCode
                        ?: acct.institutionId
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color.White,
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { onQuickLoginDemoUser(acct.id) }
                            .testTag("quick_login_${acct.id}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = when (acct.role) {
                                    UserRole.PASSENGER -> RydGreenContainer
                                    UserRole.DRIVER -> Color(0xFFE0F2FE)
                                    UserRole.ADMIN, UserRole.SUPER_ADMIN -> RydGoldContainer
                                },
                                modifier = Modifier.size(34.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = when (acct.role) {
                                            UserRole.PASSENGER -> Icons.Default.School
                                            UserRole.DRIVER -> Icons.Default.DirectionsCar
                                            UserRole.ADMIN, UserRole.SUPER_ADMIN -> Icons.Default.Shield
                                        },
                                        contentDescription = null,
                                        tint = when (acct.role) {
                                            UserRole.PASSENGER -> RydBrandGreen
                                            UserRole.DRIVER -> Color(0xFF0369A1)
                                            UserRole.ADMIN, UserRole.SUPER_ADMIN -> RydOnGoldContainer
                                        },
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = acct.fullName,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F172A)
                                )
                                Text(
                                    text = "${acct.role.displayName} • $instCode • ${acct.email}",
                                    fontSize = 11.sp,
                                    color = Color(0xFF64748B),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Sign in",
                                tint = RydBrandGreen,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ============================================================================
// 3. STEP 1: SIGN-UP ACCOUNT FORM (No Role or Institution asked here)
// ============================================================================
@Composable
private fun SignUpAccountFormScreen(
    isProcessing: Boolean,
    onLoginInsteadClick: () -> Unit,
    onSubmitAccount: (fullName: String, phone: String, email: String, password: String, confirmPassword: String) -> Unit
) {
    var fullName by rememberSaveable { mutableStateOf("") }
    var phoneNumber by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    var showPassword by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .testTag("signup_step_1_account_screen"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 480.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Create Your RydRover Account",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF0F172A)
                )
                Text(
                    text = "Enter your personal details to get started. You will select your role and institution on the next steps.",
                    fontSize = 13.sp,
                    color = Color(0xFF64748B)
                )

                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("Full Name *") },
                    placeholder = { Text("e.g., Amaka Okon") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("signup_full_name_input")
                )

                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    label = { Text("Phone Number *") },
                    placeholder = { Text("e.g., +234 803 123 4567") },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("signup_phone_input")
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Address (Optional)") },
                    placeholder = { Text("e.g., amaka.okon@student.edu.ng") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("signup_email_input")
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password *") },
                    placeholder = { Text("Minimum 6 characters") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                    trailingIcon = {
                        IconButton(onClick = { showPassword = !showPassword }) {
                            Icon(
                                imageVector = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = "Toggle password"
                            )
                        }
                    },
                    visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("signup_password_input")
                )

                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Confirm Password *") },
                    placeholder = { Text("Re-enter your password") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                    visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("signup_confirm_password_input")
                )

                Spacer(modifier = Modifier.height(4.dp))

                Button(
                    onClick = {
                        onSubmitAccount(fullName, phoneNumber, email, password, confirmPassword)
                    },
                    enabled = !isProcessing,
                    colors = ButtonDefaults.buttonColors(containerColor = RydGreenPrimary),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("signup_continue_button")
                ) {
                    Text(
                        text = "Continue",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }

                TextButton(
                    onClick = onLoginInsteadClick,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text(
                        text = "Already have an account? Log In",
                        color = RydGreenPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// ============================================================================
// 4. STEP 2: ROLE SELECTION ("How will you use RydRover?")
// ============================================================================
@Composable
private fun RoleSelectionScreen(
    userName: String,
    initialSelectedRole: UserRole?,
    isProcessing: Boolean,
    onSelectRole: (UserRole) -> Unit
) {
    var chosenRole by remember(initialSelectedRole) {
        mutableStateOf(
            if (initialSelectedRole == UserRole.PASSENGER || initialSelectedRole == UserRole.DRIVER) {
                initialSelectedRole
            } else null
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .testTag("signup_step_2_role_screen"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 480.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (userName.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = RydGreenContainer
                ) {
                    Text(
                        text = "Account created for $userName",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = RydOnGreenContainer,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            Text(
                text = "How will you use RydRover?",
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF0F172A)
            )
            Text(
                text = "Select your role on the platform. This determines your onboarding setup and dashboard.",
                fontSize = 13.5.sp,
                color = Color(0xFF64748B)
            )

            // Option 1: PASSENGER
            RoleSelectionCard(
                title = "PASSENGER",
                subtitle = "Book rides and travel around your institution.",
                badgeText = "role = \"passenger\"",
                icon = Icons.Default.School,
                isSelected = chosenRole == UserRole.PASSENGER,
                onClick = { chosenRole = UserRole.PASSENGER },
                testTag = "select_role_passenger_card"
            )

            // Option 2: DRIVER
            RoleSelectionCard(
                title = "DRIVER",
                subtitle = "Provide rides and manage passenger trips.",
                badgeText = "role = \"driver\"",
                icon = Icons.Default.DirectionsCar,
                isSelected = chosenRole == UserRole.DRIVER,
                onClick = { chosenRole = UserRole.DRIVER },
                testTag = "select_role_driver_card"
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    chosenRole?.let { onSelectRole(it) }
                },
                enabled = chosenRole != null && !isProcessing,
                colors = ButtonDefaults.buttonColors(containerColor = RydGreenPrimary),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("confirm_role_selection_button")
            ) {
                Text(
                    text = if (chosenRole != null) {
                        "Continue as ${chosenRole!!.displayName}"
                    } else {
                        "Select a Role to Continue"
                    },
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun RoleSelectionCard(
    title: String,
    subtitle: String,
    badgeText: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) RydGreenContainer.copy(alpha = 0.45f) else Color.White
        ),
        border = BorderStroke(
            width = if (isSelected) 2.5.dp else 1.dp,
            color = if (isSelected) RydBrandGreen else Color(0xFFE2E8F0)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = if (isSelected) RydBrandGreen else Color(0xFFF1F5F9),
                modifier = Modifier.size(54.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isSelected) Color.White else Color(0xFF334155),
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = title,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF0F172A)
                    )
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (isSelected) RydBrandGreen else Color(0xFFF1F5F9)
                    ) {
                        Text(
                            text = badgeText,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.White else Color(0xFF64748B),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = subtitle,
                    fontSize = 13.5.sp,
                    color = Color(0xFF475569),
                    fontWeight = FontWeight.Medium
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Selected",
                    tint = RydBrandGreen,
                    modifier = Modifier.size(26.dp)
                )
            }
        }
    }
}

// ============================================================================
// 5. STEP 3: INSTITUTION SELECTION ("Select your institution")
// ============================================================================
@Composable
private fun InstitutionSelectionScreen(
    selectedRole: UserRole,
    institutions: List<InstitutionEntity>,
    initialInstitutionId: String?,
    isProcessing: Boolean,
    onSelectInstitution: (institutionId: String) -> Unit
) {
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var selectedTypeFilter by rememberSaveable { mutableStateOf<InstitutionType?>(null) }
    // Never default to UNIPORT; require explicit user selection unless already chosen
    var chosenInstitutionId by rememberSaveable(initialInstitutionId) {
        mutableStateOf(initialInstitutionId ?: "")
    }

    val filteredInstitutions = remember(institutions, searchQuery, selectedTypeFilter) {
        val q = searchQuery.trim().lowercase()
        institutions.filter { inst ->
            val matchesType = selectedTypeFilter == null || inst.institutionType == selectedTypeFilter
            val matchesSearch = q.isEmpty() ||
                inst.name.lowercase().contains(q) ||
                inst.shortCode.lowercase().contains(q) ||
                inst.city.lowercase().contains(q) ||
                inst.state.lowercase().contains(q) ||
                inst.institutionType.label.lowercase().contains(q)
            matchesType && matchesSearch
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 14.dp)
            .testTag("signup_step_3_institution_screen"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 520.dp)
        ) {
            Text(
                text = "Select your institution",
                fontSize = 23.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF0F172A)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (selectedRole == UserRole.DRIVER) {
                    "Choose the institution transport network where you are authorized to operate."
                } else {
                    "Choose your institution to load its dedicated campuses, routes, transport zones, and shuttles."
                },
                fontSize = 13.sp,
                color = Color(0xFF64748B)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search university, polytechnic, college, or city...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("institution_search_input")
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Institution Type Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedTypeFilter == null,
                    onClick = { selectedTypeFilter = null },
                    label = { Text("All (${institutions.size})") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = RydBrandGreen,
                        selectedLabelColor = Color.White
                    )
                )
                InstitutionType.entries.forEach { type ->
                    val count = institutions.count { it.institutionType == type }
                    if (count > 0) {
                        FilterChip(
                            selected = selectedTypeFilter == type,
                            onClick = {
                                selectedTypeFilter = if (selectedTypeFilter == type) null else type
                            },
                            label = { Text("${type.label} ($count)") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = RydBrandGreen,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (filteredInstitutions.isEmpty()) {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "No matching institutions found",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color(0xFF0F172A)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Try clearing your search query or institution type filter.",
                                fontSize = 12.5.sp,
                                color = Color(0xFF64748B),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    filteredInstitutions.forEach { inst ->
                        val isSelected = chosenInstitutionId == inst.id
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) RydGreenContainer.copy(alpha = 0.45f) else Color.White
                            ),
                            border = BorderStroke(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) RydBrandGreen else Color(0xFFE2E8F0)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { chosenInstitutionId = inst.id }
                                .testTag("institution_option_${inst.id}")
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = if (isSelected) RydBrandGreen else Color(0xFFF1F5F9),
                                    modifier = Modifier.size(44.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.AccountBalance,
                                            contentDescription = null,
                                            tint = if (isSelected) Color.White else Color(0xFF334155),
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(14.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = if (isSelected) RydBrandGreen else RydGreenContainer
                                        ) {
                                            Text(
                                                text = inst.shortCode,
                                                fontSize = 10.5.sp,
                                                fontWeight = FontWeight.Black,
                                                color = if (isSelected) Color.White else RydOnGreenContainer,
                                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                            )
                                        }
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = Color(0xFFF1F5F9)
                                        ) {
                                            Text(
                                                text = inst.institutionType.label,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color(0xFF475569),
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = inst.name,
                                        fontSize = 14.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0F172A)
                                    )
                                    Text(
                                        text = "${inst.city}, ${inst.state} • ID: ${inst.id}",
                                        fontSize = 11.5.sp,
                                        color = Color(0xFF64748B)
                                    )
                                }
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Selected",
                                        tint = RydBrandGreen,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    if (chosenInstitutionId.isNotBlank()) {
                        onSelectInstitution(chosenInstitutionId)
                    }
                },
                enabled = chosenInstitutionId.isNotBlank() && !isProcessing,
                colors = ButtonDefaults.buttonColors(containerColor = RydGreenPrimary),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("confirm_institution_selection_button")
            ) {
                val chosenInst = institutions.find { it.id == chosenInstitutionId }
                Text(
                    text = if (chosenInst != null) {
                        "Continue with ${chosenInst.shortCode}"
                    } else {
                        "Select Your Institution"
                    },
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

// ============================================================================
// 6. STEP 4: CAMPUS SELECTION ("Select your campus")
// ============================================================================
@Composable
private fun CampusSelectionScreen(
    institution: InstitutionEntity?,
    campuses: List<CampusEntity>,
    initialCampusId: String?,
    isProcessing: Boolean,
    onSelectCampus: (campusId: String) -> Unit
) {
    var chosenCampusId by rememberSaveable(initialCampusId, institution?.id) {
        mutableStateOf(initialCampusId ?: "")
    }

    // If an institution has only one campus, automatically select it and continue
    LaunchedEffect(institution?.id, campuses.size) {
        if (campuses.size == 1 && !isProcessing) {
            onSelectCampus(campuses.first().id)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .testTag("signup_step_4_campus_screen"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 500.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (institution != null) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = RydGreenContainer
                ) {
                    Text(
                        text = "${institution.shortCode} • ${institution.name}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = RydOnGreenContainer,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            Text(
                text = "Select your campus",
                fontSize = 23.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF0F172A)
            )
            Text(
                text = "Showing campuses belonging exclusively to ${institution?.name ?: "your selected institution"}.",
                fontSize = 13.sp,
                color = Color(0xFF64748B)
            )

            Spacer(modifier = Modifier.height(4.dp))

            campuses.forEach { campus ->
                val isSelected = chosenCampusId == campus.id
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) RydGreenContainer.copy(alpha = 0.45f) else Color.White
                    ),
                    border = BorderStroke(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) RydBrandGreen else Color(0xFFE2E8F0)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { chosenCampusId = campus.id }
                        .testTag("campus_option_${campus.id}")
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = if (isSelected) RydBrandGreen else Color(0xFFF1F5F9),
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Place,
                                    contentDescription = null,
                                    tint = if (isSelected) Color.White else Color(0xFF334155),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = campus.name,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = campus.description,
                                fontSize = 12.sp,
                                color = Color(0xFF475569)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Campus ID: ${campus.id} • Geofence Radius: ${campus.boundaryRadiusMeters}m",
                                fontSize = 10.5.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Selected",
                                tint = RydBrandGreen,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = {
                    if (chosenCampusId.isNotBlank()) {
                        onSelectCampus(chosenCampusId)
                    }
                },
                enabled = chosenCampusId.isNotBlank() && !isProcessing,
                colors = ButtonDefaults.buttonColors(containerColor = RydGreenPrimary),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("confirm_campus_selection_button")
            ) {
                val chosenCampus = campuses.find { it.id == chosenCampusId }
                Text(
                    text = if (chosenCampus != null) {
                        "Continue with ${chosenCampus.name}"
                    } else {
                        "Select Your Campus"
                    },
                    fontSize = 15.5.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

// ============================================================================
// 7. STEP 5A: PASSENGER / STUDENT PROFILE SETUP
// ============================================================================
@Composable
private fun PassengerOnboardingProfileScreen(
    currentUser: UserEntity?,
    institution: InstitutionEntity?,
    campus: CampusEntity?,
    isProcessing: Boolean,
    onCompletePassengerProfile: (fullName: String, phone: String, studentId: String, department: String) -> Unit
) {
    var studentName by rememberSaveable(currentUser?.fullName) {
        mutableStateOf(currentUser?.fullName ?: "")
    }
    var phone by rememberSaveable(currentUser?.phoneNumber) {
        mutableStateOf(currentUser?.phoneNumber ?: "")
    }
    var studentIdOrMatric by rememberSaveable { mutableStateOf("") }
    var departmentOrFaculty by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .testTag("signup_step_5_passenger_profile_screen"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 500.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = RydGreenContainer,
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.School,
                                contentDescription = null,
                                tint = RydBrandGreen,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    Column {
                        Text(
                            text = "Student / Passenger Profile Setup",
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = "Role: Passenger • Ready for ${institution?.shortCode ?: "Institution"} Network",
                            fontSize = 12.sp,
                            color = RydBrandGreen,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Read-only Selected Institution & Campus Summary Card
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF8FAFC),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "ASSIGNED INSTITUTION & CAMPUS",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF64748B)
                        )
                        Text(
                            text = "Institution: ${institution?.name ?: "Selected Institution"} (${institution?.id ?: ""})",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = "Campus: ${campus?.name ?: "Main Campus"} (${campus?.id ?: ""})",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = RydBrandGreen
                        )
                    }
                }

                OutlinedTextField(
                    value = studentName,
                    onValueChange = { studentName = it },
                    label = { Text("Student / Passenger Name *") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("passenger_profile_name_input")
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number *") },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("passenger_profile_phone_input")
                )

                OutlinedTextField(
                    value = studentIdOrMatric,
                    onValueChange = { studentIdOrMatric = it },
                    label = { Text("Student ID / Matriculation Number (Optional)") },
                    placeholder = { Text("e.g., DE.2023/4102 or Staff/Visitor ID") },
                    leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("passenger_profile_matric_input")
                )

                OutlinedTextField(
                    value = departmentOrFaculty,
                    onValueChange = { departmentOrFaculty = it },
                    label = { Text("Department / Faculty (Optional)") },
                    placeholder = { Text("e.g., Faculty of Engineering / Computer Science") },
                    leadingIcon = { Icon(Icons.Default.School, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("passenger_profile_department_input")
                )

                Spacer(modifier = Modifier.height(6.dp))

                Button(
                    onClick = {
                        onCompletePassengerProfile(
                            studentName,
                            phone,
                            studentIdOrMatric,
                            departmentOrFaculty
                        )
                    },
                    enabled = studentName.isNotBlank() && !isProcessing,
                    colors = ButtonDefaults.buttonColors(containerColor = RydGreenPrimary),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("complete_passenger_onboarding_button")
                ) {
                    Text(
                        text = "Open ${institution?.shortCode ?: "Passenger"} Dashboard",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

// ============================================================================
// 8. STEP 5B: DRIVER REGISTRATION & VEHICLE ONBOARDING
// ============================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DriverOnboardingProfileScreen(
    currentUser: UserEntity?,
    institution: InstitutionEntity?,
    campus: CampusEntity?,
    isProcessing: Boolean,
    onCompleteDriverProfile: (
        fullName: String,
        phone: String,
        licenseNumber: String,
        vehicleCategory: VehicleCategory,
        vehicleMakeModel: String,
        vehicleColor: String,
        licensePlate: String,
        registrationNumber: String,
        availableSeats: Int
    ) -> Unit
) {
    val instShort = institution?.shortCode?.substringBefore(" ")?.trim() ?: "CAMPUS"
    var driverName by rememberSaveable(currentUser?.fullName) {
        mutableStateOf(currentUser?.fullName ?: "")
    }
    var phone by rememberSaveable(currentUser?.phoneNumber) {
        mutableStateOf(currentUser?.phoneNumber ?: "")
    }
    var licenseNumber by rememberSaveable { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(VehicleCategory.SUV_SIENNA) }
    var vehicleMakeModel by rememberSaveable { mutableStateOf("Toyota Sienna Campus Shuttle") }
    var vehicleColor by rememberSaveable { mutableStateOf("White & Green") }
    var licensePlate by rememberSaveable { mutableStateOf("") }
    var registrationNumber by rememberSaveable(instShort) { mutableStateOf("$instShort-SH-12") }
    var availableSeats by rememberSaveable { mutableStateOf(VehicleCategory.SUV_SIENNA.typicalCapacity.toString()) }
    var categoryExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .testTag("signup_step_5_driver_profile_screen"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 520.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFE0F2FE),
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.DirectionsCar,
                                contentDescription = null,
                                tint = Color(0xFF0369A1),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    Column {
                        Text(
                            text = "Commercial Driver & Vehicle Setup",
                            fontSize = 18.5.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = "Role: Driver • ${institution?.shortCode ?: "Institution"} Transit Network",
                            fontSize = 12.sp,
                            color = Color(0xFF0369A1),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Read-only Selected Institution & Campus Summary Card
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF8FAFC),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "OPERATING INSTITUTION & CAMPUS",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF64748B)
                        )
                        Text(
                            text = "Institution: ${institution?.name ?: "Selected Institution"} (${institution?.id ?: ""})",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = "Assigned Campus: ${campus?.name ?: "Main Campus"} (${campus?.id ?: ""})",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = RydBrandGreen
                        )
                    }
                }

                OutlinedTextField(
                    value = driverName,
                    onValueChange = { driverName = it },
                    label = { Text("Driver Full Name *") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("driver_profile_name_input")
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Driver Phone Number *") },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("driver_profile_phone_input")
                )

                OutlinedTextField(
                    value = licenseNumber,
                    onValueChange = { licenseNumber = it },
                    label = { Text("Driver Identification / License Number *") },
                    placeholder = { Text("e.g., RV-DL-90418 or Campus Permit ID") },
                    leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("driver_profile_license_input")
                )

                // Vehicle Category Dropdown
                ExposedDropdownMenuBox(
                    expanded = categoryExpanded,
                    onExpandedChange = { categoryExpanded = !categoryExpanded }
                ) {
                    OutlinedTextField(
                        value = "${selectedCategory.label} (${selectedCategory.typicalCapacity} seats)",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Vehicle Type *") },
                        leadingIcon = { Icon(Icons.Default.DirectionsBus, contentDescription = null) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false }
                    ) {
                        VehicleCategory.entries.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text("${cat.label} (Default: ${cat.typicalCapacity} seats)") },
                                onClick = {
                                    selectedCategory = cat
                                    availableSeats = cat.typicalCapacity.toString()
                                    categoryExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = vehicleMakeModel,
                    onValueChange = { vehicleMakeModel = it },
                    label = { Text("Vehicle Make & Model *") },
                    placeholder = { Text("e.g., Toyota Sienna / Toyota Corolla / Suzuki Shuttle") },
                    leadingIcon = { Icon(Icons.Default.DirectionsCar, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("driver_profile_vehicle_model_input")
                )

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = licensePlate,
                        onValueChange = { licensePlate = it },
                        label = { Text("License Plate *") },
                        placeholder = { Text("e.g., RIV-418-PH") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("driver_profile_plate_input")
                    )

                    OutlinedTextField(
                        value = availableSeats,
                        onValueChange = { availableSeats = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Available Seats *") },
                        leadingIcon = { Icon(Icons.Default.EventSeat, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("driver_profile_seats_input")
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = registrationNumber,
                        onValueChange = { registrationNumber = it },
                        label = { Text("Shuttle Reg Code") },
                        placeholder = { Text("e.g., $instShort-SH-12") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = vehicleColor,
                        onValueChange = { vehicleColor = it },
                        label = { Text("Vehicle Color") },
                        placeholder = { Text("e.g., White & Green") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = RydGoldContainer.copy(alpha = 0.6f),
                    border = BorderStroke(1.dp, RydOnGoldContainer.copy(alpha = 0.25f))
                ) {
                    Text(
                        text = "Note: New driver registrations are submitted for Institution Administrator verification. Your Driver Dashboard will open in Pending Approval mode until verified by your ${institution?.shortCode ?: "Institution"} Transport Administrator.",
                        fontSize = 11.5.sp,
                        color = RydOnGoldContainer,
                        modifier = Modifier.padding(12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Button(
                    onClick = {
                        val seats = availableSeats.toIntOrNull()?.coerceIn(1, 30)
                            ?: selectedCategory.typicalCapacity
                        onCompleteDriverProfile(
                            driverName,
                            phone,
                            licenseNumber,
                            selectedCategory,
                            vehicleMakeModel,
                            vehicleColor,
                            licensePlate,
                            registrationNumber,
                            seats
                        )
                    },
                    enabled = !isProcessing,
                    colors = ButtonDefaults.buttonColors(containerColor = RydGreenPrimary),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("complete_driver_onboarding_button")
                ) {
                    Text(
                        text = "Complete Setup & Open Driver Dashboard",
                        fontSize = 15.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
