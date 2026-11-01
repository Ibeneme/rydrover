package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
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
import com.example.data.model.FareSharingMode
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        StateEntity::class,
        UniversityEntity::class,
        CampusEntity::class,
        TransportParkEntity::class,
        ServiceZoneEntity::class,
        DriverQueueEntryEntity::class,
        VehicleEntity::class,
        UserEntity::class,
        PassengerProfileEntity::class,
        DriverProfileEntity::class,
        RouteEntity::class,
        PickupLocationEntity::class,
        TripEntity::class,
        BookingEntity::class,
        PassengerWalletEntity::class,
        DriverWalletEntity::class,
        WalletTransactionEntity::class,
        NotificationEntity::class,
        CoordinatedRequestEntity::class,
        ReportEntity::class,
        PlatformSettingsEntity::class
    ],
    version = 9,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun stateDao(): StateDao
    abstract fun universityDao(): UniversityDao
    abstract fun campusDao(): CampusDao
    abstract fun transportParkDao(): TransportParkDao
    abstract fun serviceZoneDao(): ServiceZoneDao
    abstract fun driverQueueDao(): DriverQueueDao
    abstract fun vehicleDao(): VehicleDao
    abstract fun userDao(): UserDao
    abstract fun tripDao(): TripDao
    abstract fun bookingDao(): BookingDao
    abstract fun routeDao(): RouteDao
    abstract fun pickupLocationDao(): PickupLocationDao
    abstract fun coordinatedRequestDao(): CoordinatedRequestDao
    abstract fun platformDao(): PlatformDao
    abstract fun walletDao(): WalletDao
    abstract fun notificationDao(): NotificationDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "rydrover_database"
                )
                .fallbackToDestructiveMigration()
                .addCallback(DatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database)
                    }
                }
            }

            override fun onDestructiveMigration(db: SupportSQLiteDatabase) {
                super.onDestructiveMigration(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database)
                    }
                }
            }
        }

        suspend fun populateInitialData(database: AppDatabase) {
            val stateDao = database.stateDao()
            val universityDao = database.universityDao()
            val campusDao = database.campusDao()
            val transportParkDao = database.transportParkDao()
            val serviceZoneDao = database.serviceZoneDao()
            val driverQueueDao = database.driverQueueDao()
            val vehicleDao = database.vehicleDao()
            val userDao = database.userDao()
            val routeDao = database.routeDao()
            val pickupLocationDao = database.pickupLocationDao()
            val tripDao = database.tripDao()
            val bookingDao = database.bookingDao()
            val requestDao = database.coordinatedRequestDao()
            val platformDao = database.platformDao()
            val walletDao = database.walletDao()
            val notificationDao = database.notificationDao()

            val baseNow = System.currentTimeMillis()

            // 0. States (Scalable Nationwide Hierarchy: RydRover -> State -> Institution -> Campus)
            val initialStates = listOf(
                StateEntity(id = "state_rivers", name = "Rivers State", code = "RV", isActive = true),
                StateEntity(id = "state_lagos", name = "Lagos State", code = "LA", isActive = true),
                StateEntity(id = "state_fct", name = "FCT Abuja", code = "FC", isActive = true)
            )
            stateDao.insertAllStates(initialStates)

            // 1. Institutions (University, Polytechnic, College)
            val initialUniversities = listOf(
                UniversityEntity(
                    id = "uni_uniport",
                    name = "University of Port Harcourt (UNIPORT)",
                    shortCode = "UNIPORT",
                    stateOrCity = "Choba, Port Harcourt",
                    institutionType = InstitutionType.UNIVERSITY,
                    stateId = "state_rivers",
                    state = "Rivers State",
                    city = "Port Harcourt",
                    centerLat = 4.9015,
                    centerLng = 6.9240,
                    isActive = true
                ),
                UniversityEntity(
                    id = "uni_rsu",
                    name = "University of Science and Technology / Rivers State University (UST / RSU)",
                    shortCode = "UST / RSU",
                    stateOrCity = "Nkpolu-Oroworukwo, Port Harcourt",
                    institutionType = InstitutionType.UNIVERSITY,
                    stateId = "state_rivers",
                    state = "Rivers State",
                    city = "Port Harcourt",
                    centerLat = 4.7963,
                    centerLng = 6.9818,
                    isActive = true
                ),
                UniversityEntity(
                    id = "uni_ceapoly",
                    name = "Captain Elechi Amadi Polytechnic",
                    shortCode = "CEAPOLY",
                    stateOrCity = "Rumuola, Port Harcourt",
                    institutionType = InstitutionType.POLYTECHNIC,
                    stateId = "state_rivers",
                    state = "Rivers State",
                    city = "Port Harcourt",
                    centerLat = 4.8312,
                    centerLng = 7.0085,
                    isActive = true
                ),
                UniversityEntity(
                    id = "uni_iaue",
                    name = "Ignatius Ajuru University of Education",
                    shortCode = "IAUE",
                    stateOrCity = "Rumuolumeni, Port Harcourt",
                    institutionType = InstitutionType.COLLEGE,
                    stateId = "state_rivers",
                    state = "Rivers State",
                    city = "Port Harcourt",
                    centerLat = 4.8050,
                    centerLng = 6.9420,
                    isActive = true
                ),
                UniversityEntity(
                    id = "uni_rschst",
                    name = "Rivers State College of Health Science & Management Technology",
                    shortCode = "RSCHST",
                    stateOrCity = "Oro-Owo, Rumueme, Port Harcourt",
                    institutionType = InstitutionType.TECHNICAL_COLLEGE,
                    stateId = "state_rivers",
                    state = "Rivers State",
                    city = "Port Harcourt",
                    centerLat = 4.8225,
                    centerLng = 6.9920,
                    isActive = true
                )
            )
            universityDao.insertAllUniversities(initialUniversities)

            // 2. Campuses (with GPS boundaries per institution)
            val initialCampuses = listOf(
                CampusEntity(
                    id = "camp_abuja",
                    universityId = "uni_uniport",
                    name = "Abuja Campus (Main)",
                    description = "Main Administration, Faculty of Science, Engineering & Central Park",
                    centerLat = 4.9015,
                    centerLng = 6.9240,
                    boundaryRadiusMeters = 2500
                ),
                CampusEntity(
                    id = "camp_choba",
                    universityId = "uni_uniport",
                    name = "Choba Campus",
                    description = "Faculty of Humanities, Education & Choba Gate Transit Terminal",
                    centerLat = 4.8980,
                    centerLng = 6.9180,
                    boundaryRadiusMeters = 1800
                ),
                CampusEntity(
                    id = "camp_delta",
                    universityId = "uni_uniport",
                    name = "Delta Campus",
                    description = "College of Health Sciences, Clinical Students & Sports Complex",
                    centerLat = 4.9002,
                    centerLng = 6.9210,
                    boundaryRadiusMeters = 1600
                ),
                CampusEntity(
                    id = "camp_rsu_main",
                    universityId = "uni_rsu",
                    name = "UST / RSU Main Campus (Nkpolu-Oroworukwo)",
                    description = "UST Main Gate (Mile 3), Faculty of Law, Engineering, Amphitheatre & NDDC Hostels",
                    centerLat = 4.7963,
                    centerLng = 6.9818,
                    boundaryRadiusMeters = 2400
                ),
                CampusEntity(
                    id = "camp_rsu_med",
                    universityId = "uni_rsu",
                    name = "UST College of Medical Sciences Annex",
                    description = "UST Medical Annex, Basic Medical Sciences & Clinical Shuttle Terminal",
                    centerLat = 4.8012,
                    centerLng = 6.9875,
                    boundaryRadiusMeters = 1600
                ),
                CampusEntity(
                    id = "camp_ceapoly_main",
                    universityId = "uni_ceapoly",
                    name = "Rumuola Main Polytechnic Campus",
                    description = "School of Engineering Technology, Science & Rumuola Gate Bay",
                    centerLat = 4.8312,
                    centerLng = 7.0085,
                    boundaryRadiusMeters = 1500
                ),
                CampusEntity(
                    id = "camp_iaue_main",
                    universityId = "uni_iaue",
                    name = "Rumuolumeni Main Campus",
                    description = "Main Academic Complex, Senate Building & Rumuolumeni Shuttle Park",
                    centerLat = 4.8050,
                    centerLng = 6.9420,
                    boundaryRadiusMeters = 1800
                ),
                CampusEntity(
                    id = "camp_rschst_main",
                    universityId = "uni_rschst",
                    name = "Oro-Owo Rumueme Main Campus",
                    description = "School of Public Health, Environmental Health & College Gate",
                    centerLat = 4.8225,
                    centerLng = 6.9920,
                    boundaryRadiusMeters = 1400
                )
            )
            campusDao.insertAllCampuses(initialCampuses)

            // 3. Transport Parks
            val initialParks = listOf(
                TransportParkEntity(
                    id = "park_abuja_main",
                    universityId = "uni_uniport",
                    campusId = "camp_abuja",
                    name = "Abuja Campus Central Transport Park",
                    parkManager = "Mr. K. Nengi (Head of Park Operations)",
                    totalBays = 12
                ),
                TransportParkEntity(
                    id = "park_choba_gate",
                    universityId = "uni_uniport",
                    campusId = "camp_choba",
                    name = "Choba Gate Transit Terminal",
                    parkManager = "Mr. B. Dan (Terminal Controller)",
                    totalBays = 8
                ),
                TransportParkEntity(
                    id = "park_delta_bay",
                    universityId = "uni_uniport",
                    campusId = "camp_delta",
                    name = "Delta Campus Shuttle Bay",
                    parkManager = "Mrs. F. Amadi (Bay Dispatcher)",
                    totalBays = 6
                ),
                TransportParkEntity(
                    id = "park_rsu_main",
                    universityId = "uni_rsu",
                    campusId = "camp_rsu_main",
                    name = "UST / RSU Main Gate Shuttle Park",
                    parkManager = "Chief D. Horsfall (UST Park Coordinator)",
                    totalBays = 10
                ),
                TransportParkEntity(
                    id = "park_ceapoly_main",
                    universityId = "uni_ceapoly",
                    campusId = "camp_ceapoly_main",
                    name = "Rumuola Polytechnic Shuttle Park",
                    parkManager = "Engr. T. Weli (Polytechnic Transport Officer)",
                    totalBays = 8
                ),
                TransportParkEntity(
                    id = "park_iaue_main",
                    universityId = "uni_iaue",
                    campusId = "camp_iaue_main",
                    name = "Rumuolumeni Campus Transit Bay",
                    parkManager = "Dr. M. Abbey (IAUE Transit Coordinator)",
                    totalBays = 8
                )
            )
            transportParkDao.insertAllParks(initialParks)

            // 4. Configurable Geographic Service Zones (Geofences)
            val initialServiceZones = listOf(
                ServiceZoneEntity(
                    id = "zone_eng_block",
                    universityId = "uni_uniport",
                    campusId = "camp_abuja",
                    name = "Engineering Block Zone",
                    centerLat = 4.9038,
                    centerLng = 6.9268,
                    radiusMeters = 450,
                    maxEtaMinutesThreshold = 8,
                    supportedRoutes = "Main Gate, Student Hostel, Medical Centre"
                ),
                ServiceZoneEntity(
                    id = "zone_main_gate",
                    universityId = "uni_uniport",
                    campusId = "camp_abuja",
                    name = "Main Gate Zone",
                    centerLat = 4.9022,
                    centerLng = 6.9255,
                    radiusMeters = 500,
                    maxEtaMinutesThreshold = 10,
                    supportedRoutes = "Engineering Block, Student Hostel, Medical Centre, Library"
                ),
                ServiceZoneEntity(
                    id = "zone_hostel",
                    universityId = "uni_uniport",
                    campusId = "camp_abuja",
                    name = "Student Hostel Zone",
                    centerLat = 4.9052,
                    centerLng = 6.9281,
                    radiusMeters = 400,
                    maxEtaMinutesThreshold = 8,
                    supportedRoutes = "Engineering Block, Main Gate, Faculty of Science, Library"
                ),
                ServiceZoneEntity(
                    id = "zone_science",
                    universityId = "uni_uniport",
                    campusId = "camp_abuja",
                    name = "Faculty of Science Zone",
                    centerLat = 4.9044,
                    centerLng = 6.9258,
                    radiusMeters = 400,
                    maxEtaMinutesThreshold = 8,
                    supportedRoutes = "Main Gate, Student Hostel, Medical Centre"
                ),
                ServiceZoneEntity(
                    id = "zone_medical",
                    universityId = "uni_uniport",
                    campusId = "camp_abuja",
                    name = "Medical Centre Zone",
                    centerLat = 4.9065,
                    centerLng = 6.9295,
                    radiusMeters = 350,
                    maxEtaMinutesThreshold = 8,
                    supportedRoutes = "Main Gate, Engineering Block, Student Hostel"
                ),
                ServiceZoneEntity(
                    id = "zone_library",
                    universityId = "uni_uniport",
                    campusId = "camp_abuja",
                    name = "Library Zone",
                    centerLat = 4.9030,
                    centerLng = 6.9235,
                    radiusMeters = 400,
                    maxEtaMinutesThreshold = 8,
                    supportedRoutes = "Main Gate, Choba Campus Gate, Student Hostel"
                ),
                // UST / Rivers State University (RSU) Isolated Service Zones
                ServiceZoneEntity(
                    id = "zone_rsu_gate",
                    universityId = "uni_rsu",
                    campusId = "camp_rsu_main",
                    name = "UST Main Gate Zone",
                    centerLat = 4.7963,
                    centerLng = 6.9818,
                    radiusMeters = 450,
                    maxEtaMinutesThreshold = 8,
                    supportedRoutes = "UST Faculty of Law, UST Amphitheatre, NDDC Student Hostel, UST Engineering Complex"
                ),
                ServiceZoneEntity(
                    id = "zone_rsu_law",
                    universityId = "uni_rsu",
                    campusId = "camp_rsu_main",
                    name = "UST Faculty of Law Zone",
                    centerLat = 4.7988,
                    centerLng = 6.9842,
                    radiusMeters = 400,
                    maxEtaMinutesThreshold = 8,
                    supportedRoutes = "UST Main Gate, NDDC Student Hostel, UST Engineering Complex"
                ),
                ServiceZoneEntity(
                    id = "zone_rsu_hostel",
                    universityId = "uni_rsu",
                    campusId = "camp_rsu_main",
                    name = "UST NDDC Hostel Zone",
                    centerLat = 4.8005,
                    centerLng = 6.9860,
                    radiusMeters = 400,
                    maxEtaMinutesThreshold = 8,
                    supportedRoutes = "UST Main Gate, UST Faculty of Law, UST Amphitheatre"
                ),
                ServiceZoneEntity(
                    id = "zone_rsu_eng",
                    universityId = "uni_rsu",
                    campusId = "camp_rsu_main",
                    name = "UST Engineering & Senate Zone",
                    centerLat = 4.7978,
                    centerLng = 6.9835,
                    radiusMeters = 420,
                    maxEtaMinutesThreshold = 8,
                    supportedRoutes = "UST Main Gate, UST Faculty of Law, NDDC Student Hostel"
                ),
                // CEAPOLY Service Zones
                ServiceZoneEntity(
                    id = "zone_ceapoly_gate",
                    universityId = "uni_ceapoly",
                    campusId = "camp_ceapoly_main",
                    name = "Rumuola Poly Gate Zone",
                    centerLat = 4.8312,
                    centerLng = 7.0085,
                    radiusMeters = 400,
                    maxEtaMinutesThreshold = 8,
                    supportedRoutes = "School of Engineering Block, Admin Block, Poly Hostel"
                ),
                // IAUE Service Zones
                ServiceZoneEntity(
                    id = "zone_iaue_gate",
                    universityId = "uni_iaue",
                    campusId = "camp_iaue_main",
                    name = "Rumuolumeni Main Gate Zone",
                    centerLat = 4.8050,
                    centerLng = 6.9420,
                    radiusMeters = 400,
                    maxEtaMinutesThreshold = 8,
                    supportedRoutes = "Senate Building, Education Complex, IAUE Hostels"
                )
            )
            serviceZoneDao.insertAllServiceZones(initialServiceZones)

            // 5. Institution Users (Passengers, Drivers, Institution Admins, Super Admin)
            val initialUsers = listOf(
                UserEntity(
                    id = "usr_pass_1",
                    role = UserRole.PASSENGER,
                    fullName = "Amaka Okon",
                    phoneNumber = "+234 803 712 9043",
                    email = "amaka.okon@uniport.edu.ng",
                    rating = 4.9f,
                    universityId = "uni_uniport",
                    authorizedInstitutionIds = "uni_uniport",
                    campusId = "camp_abuja"
                ),
                UserEntity(
                    id = "usr_pass_2",
                    role = UserRole.PASSENGER,
                    fullName = "Tamuno George",
                    phoneNumber = "+234 812 554 9912",
                    email = "tamuno.george@uniport.edu.ng",
                    rating = 4.8f,
                    universityId = "uni_uniport",
                    authorizedInstitutionIds = "uni_uniport",
                    campusId = "camp_abuja"
                ),
                UserEntity(
                    id = "usr_pass_3",
                    role = UserRole.PASSENGER,
                    fullName = "Ngozi Eze",
                    phoneNumber = "+234 805 119 4488",
                    email = "ngozi.eze@uniport.edu.ng",
                    rating = 4.7f,
                    universityId = "uni_uniport",
                    authorizedInstitutionIds = "uni_uniport",
                    campusId = "camp_choba"
                ),
                UserEntity(
                    id = "usr_driv_1",
                    role = UserRole.DRIVER,
                    fullName = "Babatunde Alabi",
                    phoneNumber = "+234 802 334 1122",
                    email = "babatunde.alabi@rydrover.ng",
                    rating = 4.95f,
                    verificationStatus = DriverVerificationStatus.VERIFIED,
                    universityId = "uni_uniport",
                    authorizedInstitutionIds = "uni_uniport",
                    campusId = "camp_abuja"
                ),
                UserEntity(
                    id = "usr_driv_2",
                    role = UserRole.DRIVER,
                    fullName = "Emeka Okocha",
                    phoneNumber = "+234 805 771 2233",
                    email = "emeka.okocha@rydrover.ng",
                    rating = 4.85f,
                    verificationStatus = DriverVerificationStatus.VERIFIED,
                    universityId = "uni_uniport",
                    authorizedInstitutionIds = "uni_uniport",
                    campusId = "camp_abuja"
                ),
                UserEntity(
                    id = "usr_driv_3",
                    role = UserRole.DRIVER,
                    fullName = "Chidi Amadi",
                    phoneNumber = "+234 818 902 4433",
                    email = "chidi.amadi@rydrover.ng",
                    rating = 4.80f,
                    verificationStatus = DriverVerificationStatus.VERIFIED,
                    universityId = "uni_uniport",
                    authorizedInstitutionIds = "uni_uniport",
                    campusId = "camp_choba"
                ),
                UserEntity(
                    id = "usr_driv_4",
                    role = UserRole.DRIVER,
                    fullName = "Boma Briggs",
                    phoneNumber = "+234 809 123 7788",
                    email = "boma.briggs@rydrover.ng",
                    rating = 4.50f,
                    verificationStatus = DriverVerificationStatus.PENDING,
                    universityId = "uni_uniport",
                    authorizedInstitutionIds = "uni_uniport",
                    campusId = "camp_delta",
                    accountStatus = AccountStatus.DRIVER_APPROVAL_PENDING
                ),
                UserEntity(
                    id = "usr_admin_1",
                    role = UserRole.ADMIN,
                    fullName = "Prof. O. Briggs (UNIPORT Admin)",
                    phoneNumber = "+234 800 793 76837",
                    email = "transport.director@uniport.edu.ng",
                    rating = 5.0f,
                    universityId = "uni_uniport",
                    authorizedInstitutionIds = "uni_uniport",
                    campusId = "camp_abuja"
                ),
                // UST / RSU Drivers, Passengers & Admin
                UserEntity(
                    id = "usr_driv_rsu_1",
                    role = UserRole.DRIVER,
                    fullName = "Tari Horsfall",
                    phoneNumber = "+234 806 441 8890",
                    email = "tari.horsfall@ust.edu.ng",
                    rating = 4.92f,
                    verificationStatus = DriverVerificationStatus.VERIFIED,
                    universityId = "uni_rsu",
                    authorizedInstitutionIds = "uni_rsu",
                    campusId = "camp_rsu_main"
                ),
                UserEntity(
                    id = "usr_driv_rsu_2",
                    role = UserRole.DRIVER,
                    fullName = "Diepreye Jack",
                    phoneNumber = "+234 803 918 2201",
                    email = "diepreye.jack@ust.edu.ng",
                    rating = 4.88f,
                    verificationStatus = DriverVerificationStatus.VERIFIED,
                    universityId = "uni_rsu",
                    authorizedInstitutionIds = "uni_rsu",
                    campusId = "camp_rsu_main"
                ),
                UserEntity(
                    id = "usr_pass_rsu_1",
                    role = UserRole.PASSENGER,
                    fullName = "Preye Dokubo",
                    phoneNumber = "+234 806 882 1940",
                    email = "preye.dokubo@ust.edu.ng",
                    rating = 4.9f,
                    universityId = "uni_rsu",
                    authorizedInstitutionIds = "uni_rsu",
                    campusId = "camp_rsu_main"
                ),
                UserEntity(
                    id = "usr_pass_rsu_2",
                    role = UserRole.PASSENGER,
                    fullName = "Ibimina West",
                    phoneNumber = "+234 813 402 7711",
                    email = "ibimina.west@ust.edu.ng",
                    rating = 4.85f,
                    universityId = "uni_rsu",
                    authorizedInstitutionIds = "uni_rsu",
                    campusId = "camp_rsu_main"
                ),
                UserEntity(
                    id = "usr_admin_rsu_1",
                    role = UserRole.ADMIN,
                    fullName = "Dr. I. Horsfall (UST / RSU Admin)",
                    phoneNumber = "+234 803 551 0900",
                    email = "transport.admin@ust.edu.ng",
                    rating = 5.0f,
                    universityId = "uni_rsu",
                    authorizedInstitutionIds = "uni_rsu",
                    campusId = "camp_rsu_main"
                ),
                // CEAPOLY & IAUE Drivers
                UserEntity(
                    id = "usr_driv_ceapoly_1",
                    role = UserRole.DRIVER,
                    fullName = "Confidence Weli",
                    phoneNumber = "+234 807 221 6500",
                    email = "confidence.weli@ceapoly.edu.ng",
                    rating = 4.86f,
                    verificationStatus = DriverVerificationStatus.VERIFIED,
                    universityId = "uni_ceapoly",
                    authorizedInstitutionIds = "uni_ceapoly",
                    campusId = "camp_ceapoly_main"
                ),
                UserEntity(
                    id = "usr_driv_iaue_1",
                    role = UserRole.DRIVER,
                    fullName = "Godspower Abbey",
                    phoneNumber = "+234 808 610 3390",
                    email = "godspower.abbey@iaue.edu.ng",
                    rating = 4.89f,
                    verificationStatus = DriverVerificationStatus.VERIFIED,
                    universityId = "uni_iaue",
                    authorizedInstitutionIds = "uni_iaue",
                    campusId = "camp_iaue_main"
                ),
                UserEntity(
                    id = "usr_super_admin",
                    role = UserRole.SUPER_ADMIN,
                    fullName = "Engr. S. Kpegemea (Super Admin)",
                    phoneNumber = "+234 800 793 0000",
                    email = "superadmin@rydrover.ng",
                    rating = 5.0f,
                    universityId = "uni_uniport",
                    authorizedInstitutionIds = "uni_uniport,uni_rsu,uni_ceapoly,uni_iaue,uni_rschst",
                    campusId = "camp_abuja"
                )
            )
            userDao.insertAll(initialUsers)

            // 5b. Passenger Profiles (Separated from Driver Profiles)
            val initialPassengerProfiles = listOf(
                PassengerProfileEntity(
                    passengerId = "usr_pass_1",
                    institutionId = "uni_uniport",
                    campusId = "camp_abuja",
                    studentIdOrMatric = "U2022/3010042",
                    departmentOrFaculty = "Department of Computer Science, Faculty of Computing"
                ),
                PassengerProfileEntity(
                    passengerId = "usr_pass_2",
                    institutionId = "uni_uniport",
                    campusId = "camp_abuja",
                    studentIdOrMatric = "U2021/5570018",
                    departmentOrFaculty = "Department of Electrical/Electronic Engineering"
                ),
                PassengerProfileEntity(
                    passengerId = "usr_pass_3",
                    institutionId = "uni_uniport",
                    campusId = "camp_choba",
                    studentIdOrMatric = "U2023/1820091",
                    departmentOrFaculty = "Department of English Studies, Faculty of Humanities"
                ),
                PassengerProfileEntity(
                    passengerId = "usr_pass_4",
                    institutionId = "uni_uniport",
                    campusId = "camp_delta",
                    studentIdOrMatric = "U2020/4710012",
                    departmentOrFaculty = "College of Health Sciences"
                ),
                PassengerProfileEntity(
                    passengerId = "usr_pass_rsu_1",
                    institutionId = "uni_rsu",
                    campusId = "camp_rsu_main",
                    studentIdOrMatric = "DE.2022/4108",
                    departmentOrFaculty = "Faculty of Law, Rivers State University"
                ),
                PassengerProfileEntity(
                    passengerId = "usr_pass_rsu_2",
                    institutionId = "uni_rsu",
                    campusId = "camp_rsu_main",
                    studentIdOrMatric = "DE.2023/2891",
                    departmentOrFaculty = "Faculty of Engineering, Rivers State University"
                )
            )
            userDao.insertAllPassengerProfiles(initialPassengerProfiles)

            // 6. Driver Profiles with Explicit Institution & Campus Context
            val driverProfiles = listOf(
                DriverProfileEntity(
                    driverId = "usr_driv_1",
                    institutionId = "uni_uniport",
                    campusId = "camp_abuja",
                    authorizedInstitutionIds = "uni_uniport",
                    vehicleCategory = VehicleCategory.SUV_SIENNA,
                    vehicleMakeModel = "Toyota Sienna Campus Shuttle (Air Conditioned)",
                    licensePlate = "RIV-892-PH",
                    licenseNumber = "RV-TX-08922",
                    color = "University Green & White",
                    registrationNumber = "UNIPORT-SH-04",
                    totalTrips = 184,
                    completedTripsToday = 5,
                    rating = 4.95f,
                    isOnline = true,
                    queueStatus = DriverQueueStatus.QUEUED,
                    currentServiceZoneId = "zone_eng_block",
                    currentServiceZoneName = "Engineering Block Zone",
                    currentRouteDirection = "Engineering Block → Main Gate",
                    queueEntryTimestamp = baseNow - 900_000L,
                    currentLat = 4.9038,
                    currentLng = 6.9268,
                    verificationStatus = DriverVerificationStatus.VERIFIED
                ),
                DriverProfileEntity(
                    driverId = "usr_driv_2",
                    institutionId = "uni_uniport",
                    campusId = "camp_abuja",
                    authorizedInstitutionIds = "uni_uniport",
                    vehicleCategory = VehicleCategory.MINIBUS,
                    vehicleMakeModel = "Toyota HiAce Campus Minibus (14-Seater)",
                    licensePlate = "RIV-214-AB",
                    licenseNumber = "RV-MB-12093",
                    color = "University White with Green Stripe",
                    registrationNumber = "UNIPORT-SH-01",
                    totalTrips = 320,
                    completedTripsToday = 7,
                    rating = 4.85f,
                    isOnline = true,
                    queueStatus = DriverQueueStatus.QUEUED,
                    currentServiceZoneId = "zone_eng_block",
                    currentServiceZoneName = "Engineering Block Zone",
                    currentRouteDirection = "Engineering Block → Student Hostel",
                    queueEntryTimestamp = baseNow - 600_000L,
                    currentLat = 4.9040,
                    currentLng = 6.9270,
                    verificationStatus = DriverVerificationStatus.VERIFIED
                ),
                DriverProfileEntity(
                    driverId = "usr_driv_3",
                    institutionId = "uni_uniport",
                    campusId = "camp_choba",
                    authorizedInstitutionIds = "uni_uniport",
                    vehicleCategory = VehicleCategory.COMMERCIAL_TAXI,
                    vehicleMakeModel = "Toyota Corolla Campus Sedan",
                    licensePlate = "RIV-551-XY",
                    licenseNumber = "RV-SD-33109",
                    color = "Silver",
                    registrationNumber = "UNIPORT-TX-08",
                    totalTrips = 95,
                    completedTripsToday = 3,
                    rating = 4.80f,
                    isOnline = true,
                    queueStatus = DriverQueueStatus.QUEUED,
                    currentServiceZoneId = "zone_eng_block",
                    currentServiceZoneName = "Engineering Block Zone",
                    currentRouteDirection = "Engineering Block → Main Gate",
                    queueEntryTimestamp = baseNow - 300_000L,
                    currentLat = 4.9036,
                    currentLng = 6.9265,
                    verificationStatus = DriverVerificationStatus.VERIFIED
                ),
                DriverProfileEntity(
                    driverId = "usr_driv_4",
                    institutionId = "uni_uniport",
                    campusId = "camp_delta",
                    authorizedInstitutionIds = "uni_uniport",
                    vehicleCategory = VehicleCategory.APPROVED_TRANSIT,
                    vehicleMakeModel = "Campus Tricycle Shuttle (Keke)",
                    licensePlate = "RIV-401-KZ",
                    licenseNumber = "RV-KK-44910",
                    color = "Yellow",
                    registrationNumber = "UNIPORT-KE-02",
                    totalTrips = 12,
                    completedTripsToday = 1,
                    rating = 4.50f,
                    isOnline = false,
                    queueStatus = DriverQueueStatus.OFFLINE,
                    currentServiceZoneId = "zone_main_gate",
                    currentServiceZoneName = "Main Gate Zone",
                    currentRouteDirection = "Main Gate → Medical Centre",
                    queueEntryTimestamp = baseNow - 120_000L,
                    currentLat = 4.9022,
                    currentLng = 6.9255,
                    verificationStatus = DriverVerificationStatus.PENDING
                ),
                // UST / RSU Driver Profiles
                DriverProfileEntity(
                    driverId = "usr_driv_rsu_1",
                    institutionId = "uni_rsu",
                    campusId = "camp_rsu_main",
                    authorizedInstitutionIds = "uni_rsu",
                    vehicleCategory = VehicleCategory.SUV_SIENNA,
                    vehicleMakeModel = "Toyota Sienna UST Campus Shuttle",
                    licensePlate = "RIV-773-UST",
                    licenseNumber = "RV-UST-9910",
                    color = "UST Blue & White",
                    registrationNumber = "UST-SH-01",
                    totalTrips = 140,
                    completedTripsToday = 6,
                    rating = 4.92f,
                    isOnline = true,
                    queueStatus = DriverQueueStatus.QUEUED,
                    currentServiceZoneId = "zone_rsu_gate",
                    currentServiceZoneName = "UST Main Gate Zone",
                    currentRouteDirection = "UST Main Gate → UST Faculty of Law",
                    queueEntryTimestamp = baseNow - 720_000L,
                    currentLat = 4.7963,
                    currentLng = 6.9818,
                    verificationStatus = DriverVerificationStatus.VERIFIED
                ),
                DriverProfileEntity(
                    driverId = "usr_driv_rsu_2",
                    institutionId = "uni_rsu",
                    campusId = "camp_rsu_main",
                    authorizedInstitutionIds = "uni_rsu",
                    vehicleCategory = VehicleCategory.MINIBUS,
                    vehicleMakeModel = "Toyota HiAce UST Shuttle Bus",
                    licensePlate = "RIV-504-UST",
                    licenseNumber = "RV-UST-8841",
                    color = "UST White & Navy",
                    registrationNumber = "UST-SH-02",
                    totalTrips = 112,
                    completedTripsToday = 4,
                    rating = 4.88f,
                    isOnline = true,
                    queueStatus = DriverQueueStatus.QUEUED,
                    currentServiceZoneId = "zone_rsu_gate",
                    currentServiceZoneName = "UST Main Gate Zone",
                    currentRouteDirection = "UST Main Gate → NDDC Student Hostel",
                    queueEntryTimestamp = baseNow - 480_000L,
                    currentLat = 4.7968,
                    currentLng = 6.9822,
                    verificationStatus = DriverVerificationStatus.VERIFIED
                ),
                // CEAPOLY & IAUE Driver Profiles
                DriverProfileEntity(
                    driverId = "usr_driv_ceapoly_1",
                    institutionId = "uni_ceapoly",
                    campusId = "camp_ceapoly_main",
                    authorizedInstitutionIds = "uni_ceapoly",
                    vehicleCategory = VehicleCategory.COMMERCIAL_TAXI,
                    vehicleMakeModel = "Toyota Corolla Poly Shuttle",
                    licensePlate = "RIV-309-PL",
                    licenseNumber = "RV-PL-3091",
                    color = "Sky Blue & White",
                    registrationNumber = "CEAPOLY-SH-01",
                    totalTrips = 88,
                    completedTripsToday = 4,
                    rating = 4.86f,
                    isOnline = true,
                    queueStatus = DriverQueueStatus.QUEUED,
                    currentServiceZoneId = "zone_ceapoly_gate",
                    currentServiceZoneName = "Rumuola Poly Gate Zone",
                    currentRouteDirection = "Rumuola Poly Gate → School of Engineering Block",
                    queueEntryTimestamp = baseNow - 500_000L,
                    currentLat = 4.8312,
                    currentLng = 7.0085,
                    verificationStatus = DriverVerificationStatus.VERIFIED
                ),
                DriverProfileEntity(
                    driverId = "usr_driv_iaue_1",
                    institutionId = "uni_iaue",
                    campusId = "camp_iaue_main",
                    authorizedInstitutionIds = "uni_iaue",
                    vehicleCategory = VehicleCategory.SUV_SIENNA,
                    vehicleMakeModel = "Toyota Sienna IAUE Shuttle",
                    licensePlate = "RIV-412-ED",
                    licenseNumber = "RV-ED-4120",
                    color = "Green & Gold",
                    registrationNumber = "IAUE-SH-01",
                    totalTrips = 94,
                    completedTripsToday = 5,
                    rating = 4.89f,
                    isOnline = true,
                    queueStatus = DriverQueueStatus.QUEUED,
                    currentServiceZoneId = "zone_iaue_gate",
                    currentServiceZoneName = "Rumuolumeni Main Gate Zone",
                    currentRouteDirection = "Rumuolumeni Main Gate → Senate Building",
                    queueEntryTimestamp = baseNow - 540_000L,
                    currentLat = 4.8050,
                    currentLng = 6.9420,
                    verificationStatus = DriverVerificationStatus.VERIFIED
                )
            )
            userDao.insertAllDriverProfiles(driverProfiles)

            // 7. Route-Aware Dynamic Driver Queue Entries (Strictly Isolated by Institution)
            val initialQueueEntries = listOf(
                DriverQueueEntryEntity(
                    id = "q_driv_1",
                    institutionId = "uni_uniport",
                    campusId = "camp_abuja",
                    driverId = "usr_driv_1",
                    driverName = "Babatunde Alabi (Driver A)",
                    vehicleRegistration = "UNIPORT-SH-04",
                    vehicleModel = "Toyota Sienna Shuttle",
                    vehicleCategory = VehicleCategory.SUV_SIENNA,
                    totalCapacity = 6,
                    availableSeats = 4,
                    serviceZoneId = "zone_eng_block",
                    serviceZoneName = "Engineering Block Zone",
                    routeDirection = "Engineering Block → Main Gate",
                    queueStatus = DriverQueueStatus.QUEUED,
                    queueEntryTimestamp = baseNow - 900_000L,
                    entryTimeFormatted = "10:00 AM",
                    zoneEnteredAt = baseNow - 900_000L,
                    currentLat = 4.9038,
                    currentLng = 6.9268,
                    distanceToPassengerMeters = 280,
                    estimatedArrivalMinutes = 3,
                    currentTripId = "trip_uni_101"
                ),
                DriverQueueEntryEntity(
                    id = "q_driv_2",
                    institutionId = "uni_uniport",
                    campusId = "camp_abuja",
                    driverId = "usr_driv_2",
                    driverName = "Emeka Okocha (Driver B)",
                    vehicleRegistration = "UNIPORT-SH-01",
                    vehicleModel = "Toyota HiAce Minibus",
                    vehicleCategory = VehicleCategory.MINIBUS,
                    totalCapacity = 14,
                    availableSeats = 8,
                    serviceZoneId = "zone_eng_block",
                    serviceZoneName = "Engineering Block Zone",
                    routeDirection = "Engineering Block → Student Hostel",
                    queueStatus = DriverQueueStatus.QUEUED,
                    queueEntryTimestamp = baseNow - 600_000L,
                    entryTimeFormatted = "10:05 AM",
                    zoneEnteredAt = baseNow - 600_000L,
                    currentLat = 4.9040,
                    currentLng = 6.9270,
                    distanceToPassengerMeters = 350,
                    estimatedArrivalMinutes = 4,
                    currentTripId = "trip_uni_102"
                ),
                DriverQueueEntryEntity(
                    id = "q_driv_3",
                    institutionId = "uni_uniport",
                    campusId = "camp_abuja",
                    driverId = "usr_driv_3",
                    driverName = "Chidi Amadi (Driver C)",
                    vehicleRegistration = "UNIPORT-TX-08",
                    vehicleModel = "Toyota Corolla Sedan",
                    vehicleCategory = VehicleCategory.COMMERCIAL_TAXI,
                    totalCapacity = 4,
                    availableSeats = 3,
                    serviceZoneId = "zone_eng_block",
                    serviceZoneName = "Engineering Block Zone",
                    routeDirection = "Engineering Block → Main Gate",
                    queueStatus = DriverQueueStatus.QUEUED,
                    queueEntryTimestamp = baseNow - 300_000L,
                    entryTimeFormatted = "10:10 AM",
                    zoneEnteredAt = baseNow - 300_000L,
                    currentLat = 4.9036,
                    currentLng = 6.9265,
                    distanceToPassengerMeters = 450,
                    estimatedArrivalMinutes = 5,
                    currentTripId = "trip_uni_103"
                ),
                // UST / RSU Isolated Queue Entries
                DriverQueueEntryEntity(
                    id = "q_driv_rsu_1",
                    institutionId = "uni_rsu",
                    campusId = "camp_rsu_main",
                    driverId = "usr_driv_rsu_1",
                    driverName = "Tari Horsfall (UST Shuttle #1)",
                    vehicleRegistration = "UST-SH-01",
                    vehicleModel = "Toyota Sienna UST Shuttle",
                    vehicleCategory = VehicleCategory.SUV_SIENNA,
                    totalCapacity = 6,
                    availableSeats = 4,
                    serviceZoneId = "zone_rsu_gate",
                    serviceZoneName = "UST Main Gate Zone",
                    routeDirection = "UST Main Gate → UST Faculty of Law",
                    queueStatus = DriverQueueStatus.QUEUED,
                    queueEntryTimestamp = baseNow - 720_000L,
                    entryTimeFormatted = "10:03 AM",
                    zoneEnteredAt = baseNow - 720_000L,
                    currentLat = 4.7963,
                    currentLng = 6.9818,
                    distanceToPassengerMeters = 210,
                    estimatedArrivalMinutes = 2,
                    currentTripId = "trip_rsu_201"
                ),
                DriverQueueEntryEntity(
                    id = "q_driv_rsu_2",
                    institutionId = "uni_rsu",
                    campusId = "camp_rsu_main",
                    driverId = "usr_driv_rsu_2",
                    driverName = "Diepreye Jack (UST Shuttle #2)",
                    vehicleRegistration = "UST-SH-02",
                    vehicleModel = "Toyota HiAce UST Minibus",
                    vehicleCategory = VehicleCategory.MINIBUS,
                    totalCapacity = 14,
                    availableSeats = 12,
                    serviceZoneId = "zone_rsu_gate",
                    serviceZoneName = "UST Main Gate Zone",
                    routeDirection = "UST Main Gate → NDDC Student Hostel",
                    queueStatus = DriverQueueStatus.QUEUED,
                    queueEntryTimestamp = baseNow - 480_000L,
                    entryTimeFormatted = "10:06 AM",
                    zoneEnteredAt = baseNow - 480_000L,
                    currentLat = 4.7968,
                    currentLng = 6.9822,
                    distanceToPassengerMeters = 310,
                    estimatedArrivalMinutes = 4,
                    currentTripId = "trip_rsu_202"
                ),
                // CEAPOLY & IAUE Queue Entries
                DriverQueueEntryEntity(
                    id = "q_driv_ceapoly_1",
                    institutionId = "uni_ceapoly",
                    campusId = "camp_ceapoly_main",
                    driverId = "usr_driv_ceapoly_1",
                    driverName = "Confidence Weli (CEAPOLY #1)",
                    vehicleRegistration = "CEAPOLY-SH-01",
                    vehicleModel = "Toyota Corolla Poly Shuttle",
                    vehicleCategory = VehicleCategory.COMMERCIAL_TAXI,
                    totalCapacity = 4,
                    availableSeats = 3,
                    serviceZoneId = "zone_ceapoly_gate",
                    serviceZoneName = "Rumuola Poly Gate Zone",
                    routeDirection = "Rumuola Poly Gate → School of Engineering Block",
                    queueStatus = DriverQueueStatus.QUEUED,
                    queueEntryTimestamp = baseNow - 500_000L,
                    entryTimeFormatted = "10:02 AM",
                    zoneEnteredAt = baseNow - 500_000L,
                    currentLat = 4.8312,
                    currentLng = 7.0085,
                    distanceToPassengerMeters = 190,
                    estimatedArrivalMinutes = 3,
                    currentTripId = "trip_ceapoly_301"
                ),
                DriverQueueEntryEntity(
                    id = "q_driv_iaue_1",
                    institutionId = "uni_iaue",
                    campusId = "camp_iaue_main",
                    driverId = "usr_driv_iaue_1",
                    driverName = "Godspower Abbey (IAUE #1)",
                    vehicleRegistration = "IAUE-SH-01",
                    vehicleModel = "Toyota Sienna IAUE Shuttle",
                    vehicleCategory = VehicleCategory.SUV_SIENNA,
                    totalCapacity = 6,
                    availableSeats = 4,
                    serviceZoneId = "zone_iaue_gate",
                    serviceZoneName = "Rumuolumeni Main Gate Zone",
                    routeDirection = "Rumuolumeni Main Gate → Senate Building",
                    queueStatus = DriverQueueStatus.QUEUED,
                    queueEntryTimestamp = baseNow - 540_000L,
                    entryTimeFormatted = "10:04 AM",
                    zoneEnteredAt = baseNow - 540_000L,
                    currentLat = 4.8050,
                    currentLng = 6.9420,
                    distanceToPassengerMeters = 220,
                    estimatedArrivalMinutes = 3,
                    currentTripId = "trip_iaue_401"
                )
            )
            driverQueueDao.insertAllQueueEntries(initialQueueEntries)

            // 8. Registered Campus Vehicles
            val initialVehicles = listOf(
                VehicleEntity(
                    id = "veh_01",
                    driverId = "usr_driv_1",
                    universityId = "uni_uniport",
                    campusId = "camp_abuja",
                    vehicleType = "Campus Shuttle (Sienna 6-Seater)",
                    registrationNumber = "UNIPORT-SH-04",
                    makeModel = "Toyota Sienna (Green & White)",
                    licensePlate = "RIV-892-PH",
                    capacity = 6,
                    isAvailable = true,
                    isVerified = true
                ),
                VehicleEntity(
                    id = "veh_02",
                    driverId = "usr_driv_2",
                    universityId = "uni_uniport",
                    campusId = "camp_abuja",
                    vehicleType = "Campus Minibus (14-Seater)",
                    registrationNumber = "UNIPORT-SH-01",
                    makeModel = "Toyota HiAce Minibus",
                    licensePlate = "RIV-214-AB",
                    capacity = 14,
                    isAvailable = true,
                    isVerified = true
                ),
                VehicleEntity(
                    id = "veh_03",
                    driverId = "usr_driv_3",
                    universityId = "uni_uniport",
                    campusId = "camp_choba",
                    vehicleType = "Campus Sedan (4-Seater)",
                    registrationNumber = "UNIPORT-TX-08",
                    makeModel = "Toyota Corolla Sedan",
                    licensePlate = "RIV-551-XY",
                    capacity = 4,
                    isAvailable = true,
                    isVerified = true
                ),
                VehicleEntity(
                    id = "veh_04",
                    driverId = "usr_driv_4",
                    universityId = "uni_uniport",
                    campusId = "camp_delta",
                    vehicleType = "Campus Tricycle (Keke 3-Seater)",
                    registrationNumber = "UNIPORT-KE-02",
                    makeModel = "Bajaj RE Tricycle",
                    licensePlate = "RIV-401-KZ",
                    capacity = 3,
                    isAvailable = false,
                    isVerified = false
                ),
                VehicleEntity(
                    id = "veh_rsu_01",
                    driverId = "usr_driv_rsu_1",
                    universityId = "uni_rsu",
                    campusId = "camp_rsu_main",
                    vehicleType = "UST Campus Shuttle (Sienna 6-Seater)",
                    registrationNumber = "UST-SH-01",
                    makeModel = "Toyota Sienna (UST Blue & White)",
                    licensePlate = "RIV-773-UST",
                    capacity = 6,
                    isAvailable = true,
                    isVerified = true
                ),
                VehicleEntity(
                    id = "veh_rsu_02",
                    driverId = "usr_driv_rsu_2",
                    universityId = "uni_rsu",
                    campusId = "camp_rsu_main",
                    vehicleType = "UST Campus Minibus (14-Seater)",
                    registrationNumber = "UST-SH-02",
                    makeModel = "Toyota HiAce UST Shuttle",
                    licensePlate = "RIV-504-UST",
                    capacity = 14,
                    isAvailable = true,
                    isVerified = true
                ),
                VehicleEntity(
                    id = "veh_ceapoly_01",
                    driverId = "usr_driv_ceapoly_1",
                    universityId = "uni_ceapoly",
                    campusId = "camp_ceapoly_main",
                    vehicleType = "Polytechnic Sedan (4-Seater)",
                    registrationNumber = "CEAPOLY-SH-01",
                    makeModel = "Toyota Corolla Poly Shuttle",
                    licensePlate = "RIV-309-PL",
                    capacity = 4,
                    isAvailable = true,
                    isVerified = true
                ),
                VehicleEntity(
                    id = "veh_iaue_01",
                    driverId = "usr_driv_iaue_1",
                    universityId = "uni_iaue",
                    campusId = "camp_iaue_main",
                    vehicleType = "IAUE Campus Shuttle (6-Seater)",
                    registrationNumber = "IAUE-SH-01",
                    makeModel = "Toyota Sienna IAUE Shuttle",
                    licensePlate = "RIV-412-ED",
                    capacity = 6,
                    isAvailable = true,
                    isVerified = true
                )
            )
            vehicleDao.insertAllVehicles(initialVehicles)

            // 9. Designated Institution Pickup Points
            val initialPickupLocations = listOf(
                PickupLocationEntity(
                    id = "loc_eng_block",
                    universityId = "uni_uniport",
                    campusId = "camp_abuja",
                    name = "Engineering Block",
                    corridor = "Engineering Block Zone",
                    landmarkDescription = "Mechanical Engineering Workshop & Shuttle Stand",
                    latitude = 4.9038,
                    longitude = 6.9268
                ),
                PickupLocationEntity(
                    id = "loc_main_gate",
                    universityId = "uni_uniport",
                    campusId = "camp_abuja",
                    name = "Main Gate",
                    corridor = "Main Gate Zone",
                    landmarkDescription = "Beside Main Security Gate & Pedestrian Canopy",
                    latitude = 4.9022,
                    longitude = 6.9255
                ),
                PickupLocationEntity(
                    id = "loc_hostel_junc",
                    universityId = "uni_uniport",
                    campusId = "camp_abuja",
                    name = "Student Hostel",
                    corridor = "Student Hostel Zone",
                    landmarkDescription = "Nelson Mandela & Kwame Nkrumah Hall Junction",
                    latitude = 4.9052,
                    longitude = 6.9281
                ),
                PickupLocationEntity(
                    id = "loc_science",
                    universityId = "uni_uniport",
                    campusId = "camp_abuja",
                    name = "Faculty of Science",
                    corridor = "Faculty of Science Zone",
                    landmarkDescription = "Ofrima Complex & Science Lecture Theatre Stand",
                    latitude = 4.9044,
                    longitude = 6.9258
                ),
                PickupLocationEntity(
                    id = "loc_medical_centre",
                    universityId = "uni_uniport",
                    campusId = "camp_abuja",
                    name = "Medical Centre",
                    corridor = "Medical Centre Zone",
                    landmarkDescription = "Front Ambulance Bay & Pharmacy Waiting Area",
                    latitude = 4.9065,
                    longitude = 6.9295
                ),
                PickupLocationEntity(
                    id = "loc_central_library",
                    universityId = "uni_uniport",
                    campusId = "camp_abuja",
                    name = "Library",
                    corridor = "Library Zone",
                    landmarkDescription = "Donald Ekong Library Entrance Walkway",
                    latitude = 4.9030,
                    longitude = 6.9235
                ),
                PickupLocationEntity(
                    id = "loc_abuja_park",
                    universityId = "uni_uniport",
                    campusId = "camp_abuja",
                    name = "Abuja Central Transport Park",
                    corridor = "Abuja Campus Central Hub",
                    landmarkDescription = "Main Park Loading Bay & Passenger Waiting Shelter",
                    latitude = 4.9015,
                    longitude = 6.9240
                ),
                PickupLocationEntity(
                    id = "loc_choba_gate",
                    universityId = "uni_uniport",
                    campusId = "camp_choba",
                    name = "Choba Campus Gate",
                    corridor = "Choba Commercial Link",
                    landmarkDescription = "Choba Transit Terminal Bay & Security Post",
                    latitude = 4.8980,
                    longitude = 6.9180
                ),
                // UST / RSU Isolated Pickup Locations
                PickupLocationEntity(
                    id = "loc_rsu_gate",
                    universityId = "uni_rsu",
                    campusId = "camp_rsu_main",
                    name = "UST Main Gate",
                    corridor = "UST Main Gate Zone",
                    landmarkDescription = "Ikwerre Road / Mile 3 Main Security Gate Shuttle Bay",
                    latitude = 4.7963,
                    longitude = 6.9818
                ),
                PickupLocationEntity(
                    id = "loc_rsu_law",
                    universityId = "uni_rsu",
                    campusId = "camp_rsu_main",
                    name = "UST Faculty of Law",
                    corridor = "UST Faculty of Law Zone",
                    landmarkDescription = "UST Law Auditorium & Moot Court Shuttle Stand",
                    latitude = 4.7988,
                    longitude = 6.9842
                ),
                PickupLocationEntity(
                    id = "loc_rsu_hostel",
                    universityId = "uni_rsu",
                    campusId = "camp_rsu_main",
                    name = "NDDC Student Hostel",
                    corridor = "UST NDDC Hostel Zone",
                    landmarkDescription = "UST NDDC & Post-Graduate Hostel Walkway",
                    latitude = 4.8005,
                    longitude = 6.9860
                ),
                PickupLocationEntity(
                    id = "loc_rsu_amphi",
                    universityId = "uni_rsu",
                    campusId = "camp_rsu_main",
                    name = "UST Amphitheatre",
                    corridor = "UST Central Academic Corridor",
                    landmarkDescription = "UST Convocation Arena & Amphitheatre Junction",
                    latitude = 4.7975,
                    longitude = 6.9830
                ),
                PickupLocationEntity(
                    id = "loc_rsu_eng",
                    universityId = "uni_rsu",
                    campusId = "camp_rsu_main",
                    name = "UST Engineering Complex",
                    corridor = "UST Engineering & Senate Zone",
                    landmarkDescription = "UST Faculty of Engineering & ICT Centre Stand",
                    latitude = 4.7978,
                    longitude = 6.9835
                ),
                // CEAPOLY & IAUE Pickup Locations
                PickupLocationEntity(
                    id = "loc_ceapoly_gate",
                    universityId = "uni_ceapoly",
                    campusId = "camp_ceapoly_main",
                    name = "Rumuola Poly Gate",
                    corridor = "Rumuola Poly Gate Zone",
                    landmarkDescription = "Main Polytechnic Entrance & Security Bay",
                    latitude = 4.8312,
                    longitude = 7.0085
                ),
                PickupLocationEntity(
                    id = "loc_ceapoly_eng",
                    universityId = "uni_ceapoly",
                    campusId = "camp_ceapoly_main",
                    name = "School of Engineering Block",
                    corridor = "Polytechnic Academic Corridor",
                    landmarkDescription = "Engineering Technology Complex Stand",
                    latitude = 4.8325,
                    longitude = 7.0098
                ),
                PickupLocationEntity(
                    id = "loc_iaue_gate",
                    universityId = "uni_iaue",
                    campusId = "camp_iaue_main",
                    name = "Rumuolumeni Main Gate",
                    corridor = "Rumuolumeni Main Gate Zone",
                    landmarkDescription = "IAUE Main Gate Shuttle Park",
                    latitude = 4.8050,
                    longitude = 6.9420
                ),
                PickupLocationEntity(
                    id = "loc_iaue_senate",
                    universityId = "uni_iaue",
                    campusId = "camp_iaue_main",
                    name = "Senate Building",
                    corridor = "IAUE Central Academic Loop",
                    landmarkDescription = "Central Administration & Lecture Hall Bay",
                    latitude = 4.8065,
                    longitude = 6.9435
                )
            )
            pickupLocationDao.insertAllPickupLocations(initialPickupLocations)

            // 10. Institution Routes
            val initialRoutes = listOf(
                RouteEntity(
                    id = "rte_eng_maingate",
                    universityId = "uni_uniport",
                    campusId = "camp_abuja",
                    parkId = "park_abuja_main",
                    originName = "Engineering Block",
                    destinationName = "Main Gate",
                    corridorName = "Engineering Block → Main Gate Corridor",
                    standardFareNaira = 200,
                    pickupPoints = "Engineering Block, Faculty of Science, Library, Main Gate",
                    estimatedMinutes = 8
                ),
                RouteEntity(
                    id = "rte_eng_hostel",
                    universityId = "uni_uniport",
                    campusId = "camp_abuja",
                    parkId = "park_abuja_main",
                    originName = "Engineering Block",
                    destinationName = "Student Hostel",
                    corridorName = "Engineering Block → Student Hostel Corridor",
                    standardFareNaira = 200,
                    pickupPoints = "Engineering Block, Student Hostel, Medical Centre",
                    estimatedMinutes = 10
                ),
                RouteEntity(
                    id = "rte_maingate_med",
                    universityId = "uni_uniport",
                    campusId = "camp_abuja",
                    parkId = "park_abuja_main",
                    originName = "Main Gate",
                    destinationName = "Medical Centre",
                    corridorName = "Main Gate → Medical Centre Loop",
                    standardFareNaira = 250,
                    pickupPoints = "Main Gate, Engineering Block, Student Hostel, Medical Centre",
                    estimatedMinutes = 12
                ),
                // UST / RSU Isolated Routes
                RouteEntity(
                    id = "rte_rsu_gate_law",
                    universityId = "uni_rsu",
                    campusId = "camp_rsu_main",
                    parkId = "park_rsu_main",
                    originName = "UST Main Gate",
                    destinationName = "UST Faculty of Law",
                    corridorName = "UST Main Gate → UST Faculty of Law & NDDC Hostel",
                    standardFareNaira = 150,
                    pickupPoints = "UST Main Gate, UST Amphitheatre, UST Faculty of Law, NDDC Student Hostel",
                    estimatedMinutes = 7
                ),
                RouteEntity(
                    id = "rte_rsu_gate_hostel",
                    universityId = "uni_rsu",
                    campusId = "camp_rsu_main",
                    parkId = "park_rsu_main",
                    originName = "UST Main Gate",
                    destinationName = "NDDC Student Hostel",
                    corridorName = "UST Main Gate → NDDC Student Hostel",
                    standardFareNaira = 150,
                    pickupPoints = "UST Main Gate, UST Amphitheatre, UST Engineering Complex, NDDC Student Hostel",
                    estimatedMinutes = 8
                ),
                RouteEntity(
                    id = "rte_rsu_law_eng",
                    universityId = "uni_rsu",
                    campusId = "camp_rsu_main",
                    parkId = "park_rsu_main",
                    originName = "UST Faculty of Law",
                    destinationName = "UST Engineering Complex",
                    corridorName = "UST Faculty of Law → UST Engineering & ICT Loop",
                    standardFareNaira = 150,
                    pickupPoints = "UST Faculty of Law, UST Amphitheatre, UST Engineering Complex, UST Main Gate",
                    estimatedMinutes = 6
                ),
                // CEAPOLY & IAUE Routes
                RouteEntity(
                    id = "rte_ceapoly_main",
                    universityId = "uni_ceapoly",
                    campusId = "camp_ceapoly_main",
                    parkId = "park_ceapoly_main",
                    originName = "Rumuola Poly Gate",
                    destinationName = "School of Engineering Block",
                    corridorName = "Rumuola Poly Gate → School of Engineering Block",
                    standardFareNaira = 150,
                    pickupPoints = "Rumuola Poly Gate, School of Engineering Block",
                    estimatedMinutes = 6
                ),
                RouteEntity(
                    id = "rte_iaue_main",
                    universityId = "uni_iaue",
                    campusId = "camp_iaue_main",
                    parkId = "park_iaue_main",
                    originName = "Rumuolumeni Main Gate",
                    destinationName = "Senate Building",
                    corridorName = "Rumuolumeni Main Gate → Senate Building",
                    standardFareNaira = 150,
                    pickupPoints = "Rumuolumeni Main Gate, Senate Building",
                    estimatedMinutes = 6
                )
            )
            routeDao.insertAllRoutes(initialRoutes)

            // 11. Active Institution Trips (Strictly Isolated by Institution & Driver)
            val initialTrips = listOf(
                TripEntity(
                    id = "trip_uni_101",
                    tripNumber = "RYD-UNI-101",
                    universityId = "uni_uniport",
                    campusId = "camp_abuja",
                    parkId = "park_abuja_main",
                    serviceZoneId = "zone_eng_block",
                    serviceZoneName = "Engineering Block Zone",
                    driverId = "usr_driv_1",
                    driverName = "Babatunde Alabi",
                    driverPhone = "+234 802 334 1122",
                    driverRating = 4.95f,
                    vehicleCategory = VehicleCategory.SUV_SIENNA,
                    vehicleModel = "Toyota Sienna Shuttle",
                    licensePlate = "RIV-892-PH",
                    vehicleRegistration = "UNIPORT-SH-04",
                    origin = "Engineering Block",
                    destination = "Main Gate",
                    corridor = "Engineering Block → Main Gate",
                    departureTime = "3 mins away",
                    departureDate = "Today",
                    totalFareNaira = 1200,
                    totalCapacity = 6,
                    bookedSeatsCount = 2,
                    pickupPoints = "Engineering Block, Faculty of Science, Library, Main Gate, Medical Centre",
                    fareSharingMode = FareSharingMode.FIXED_PER_SEAT,
                    status = TripStatus.OPEN_FOR_BOOKINGS,
                    notes = "Queue #1 (Joined 10:00 AM) • Serving Engineering Block → Main Gate",
                    isOnline = true,
                    liveTrackingActive = true,
                    driverLat = 4.9038,
                    driverLng = 6.9268,
                    estimatedArrivalMinutes = 3,
                    distanceToPassengerMeters = 280,
                    queuePriorityRank = 1,
                    entryTimeFormatted = "10:00 AM"
                ),
                TripEntity(
                    id = "trip_uni_102",
                    tripNumber = "RYD-UNI-102",
                    universityId = "uni_uniport",
                    campusId = "camp_abuja",
                    parkId = "park_abuja_main",
                    serviceZoneId = "zone_eng_block",
                    serviceZoneName = "Engineering Block Zone",
                    driverId = "usr_driv_2",
                    driverName = "Emeka Okocha",
                    driverPhone = "+234 805 771 2233",
                    driverRating = 4.85f,
                    vehicleCategory = VehicleCategory.MINIBUS,
                    vehicleModel = "Toyota HiAce Minibus",
                    licensePlate = "RIV-214-AB",
                    vehicleRegistration = "UNIPORT-SH-01",
                    origin = "Engineering Block",
                    destination = "Student Hostel",
                    corridor = "Engineering Block → Student Hostel",
                    departureTime = "4 mins away",
                    departureDate = "Today",
                    totalFareNaira = 2800,
                    totalCapacity = 14,
                    bookedSeatsCount = 6,
                    pickupPoints = "Engineering Block, Student Hostel, Medical Centre, Choba Campus Gate",
                    fareSharingMode = FareSharingMode.FIXED_PER_SEAT,
                    status = TripStatus.PARTIALLY_BOOKED,
                    notes = "Queue #1 (Joined 10:05 AM) • Serving Engineering Block → Student Hostel",
                    isOnline = true,
                    liveTrackingActive = true,
                    driverLat = 4.9040,
                    driverLng = 6.9270,
                    estimatedArrivalMinutes = 4,
                    distanceToPassengerMeters = 350,
                    queuePriorityRank = 1,
                    entryTimeFormatted = "10:05 AM"
                ),
                TripEntity(
                    id = "trip_uni_103",
                    tripNumber = "RYD-UNI-103",
                    universityId = "uni_uniport",
                    campusId = "camp_abuja",
                    parkId = "park_abuja_main",
                    serviceZoneId = "zone_eng_block",
                    serviceZoneName = "Engineering Block Zone",
                    driverId = "usr_driv_3",
                    driverName = "Chidi Amadi",
                    driverPhone = "+234 818 902 4433",
                    driverRating = 4.80f,
                    vehicleCategory = VehicleCategory.COMMERCIAL_TAXI,
                    vehicleModel = "Toyota Corolla Sedan",
                    licensePlate = "RIV-551-XY",
                    vehicleRegistration = "UNIPORT-TX-08",
                    origin = "Engineering Block",
                    destination = "Main Gate",
                    corridor = "Engineering Block → Main Gate",
                    departureTime = "5 mins away",
                    departureDate = "Today",
                    totalFareNaira = 800,
                    totalCapacity = 4,
                    bookedSeatsCount = 1,
                    pickupPoints = "Engineering Block, Faculty of Science, Main Gate",
                    fareSharingMode = FareSharingMode.FIXED_PER_SEAT,
                    status = TripStatus.OPEN_FOR_BOOKINGS,
                    notes = "Queue #2 (Joined 10:10 AM) • Serving Engineering Block → Main Gate",
                    isOnline = true,
                    liveTrackingActive = true,
                    driverLat = 4.9036,
                    driverLng = 6.9265,
                    estimatedArrivalMinutes = 5,
                    distanceToPassengerMeters = 450,
                    queuePriorityRank = 2,
                    entryTimeFormatted = "10:10 AM"
                ),
                // Isolated UST / Rivers State University (RSU) Active Trips
                TripEntity(
                    id = "trip_rsu_201",
                    tripNumber = "RYD-UST-201",
                    universityId = "uni_rsu",
                    campusId = "camp_rsu_main",
                    parkId = "park_rsu_main",
                    serviceZoneId = "zone_rsu_gate",
                    serviceZoneName = "UST Main Gate Zone",
                    driverId = "usr_driv_rsu_1",
                    driverName = "Tari Horsfall",
                    driverPhone = "+234 806 441 8890",
                    driverRating = 4.92f,
                    vehicleCategory = VehicleCategory.SUV_SIENNA,
                    vehicleModel = "Toyota Sienna UST Shuttle",
                    licensePlate = "RIV-773-UST",
                    vehicleRegistration = "UST-SH-01",
                    origin = "UST Main Gate",
                    destination = "UST Faculty of Law",
                    corridor = "UST Main Gate → UST Faculty of Law & NDDC Hostel",
                    departureTime = "2 mins away",
                    departureDate = "Today",
                    totalFareNaira = 900,
                    totalCapacity = 6,
                    bookedSeatsCount = 2,
                    pickupPoints = "UST Main Gate, UST Amphitheatre, UST Faculty of Law, NDDC Student Hostel",
                    fareSharingMode = FareSharingMode.FIXED_PER_SEAT,
                    status = TripStatus.PARTIALLY_BOOKED,
                    notes = "Queue #1 (Joined 10:03 AM) • Serving UST Main Gate → UST Faculty of Law",
                    isOnline = true,
                    liveTrackingActive = true,
                    driverLat = 4.7963,
                    driverLng = 6.9818,
                    estimatedArrivalMinutes = 2,
                    distanceToPassengerMeters = 210,
                    queuePriorityRank = 1,
                    entryTimeFormatted = "10:03 AM"
                ),
                TripEntity(
                    id = "trip_rsu_202",
                    tripNumber = "RYD-UST-202",
                    universityId = "uni_rsu",
                    campusId = "camp_rsu_main",
                    parkId = "park_rsu_main",
                    serviceZoneId = "zone_rsu_gate",
                    serviceZoneName = "UST Main Gate Zone",
                    driverId = "usr_driv_rsu_2",
                    driverName = "Diepreye Jack",
                    driverPhone = "+234 803 918 2201",
                    driverRating = 4.88f,
                    vehicleCategory = VehicleCategory.MINIBUS,
                    vehicleModel = "Toyota HiAce UST Minibus",
                    licensePlate = "RIV-504-UST",
                    vehicleRegistration = "UST-SH-02",
                    origin = "UST Main Gate",
                    destination = "NDDC Student Hostel",
                    corridor = "UST Main Gate → NDDC Student Hostel",
                    departureTime = "4 mins away",
                    departureDate = "Today",
                    totalFareNaira = 2100,
                    totalCapacity = 14,
                    bookedSeatsCount = 2,
                    pickupPoints = "UST Main Gate, UST Amphitheatre, UST Engineering Complex, NDDC Student Hostel",
                    fareSharingMode = FareSharingMode.FIXED_PER_SEAT,
                    status = TripStatus.PARTIALLY_BOOKED,
                    notes = "Queue #2 (Joined 10:06 AM) • Serving UST Main Gate → NDDC Student Hostel",
                    isOnline = true,
                    liveTrackingActive = true,
                    driverLat = 4.7968,
                    driverLng = 6.9822,
                    estimatedArrivalMinutes = 4,
                    distanceToPassengerMeters = 310,
                    queuePriorityRank = 2,
                    entryTimeFormatted = "10:06 AM"
                ),
                // Isolated CEAPOLY & IAUE Active Trips
                TripEntity(
                    id = "trip_ceapoly_301",
                    tripNumber = "RYD-CEAPOLY-301",
                    universityId = "uni_ceapoly",
                    campusId = "camp_ceapoly_main",
                    parkId = "park_ceapoly_main",
                    serviceZoneId = "zone_ceapoly_gate",
                    serviceZoneName = "Rumuola Poly Gate Zone",
                    driverId = "usr_driv_ceapoly_1",
                    driverName = "Confidence Weli",
                    driverPhone = "+234 807 221 6500",
                    driverRating = 4.86f,
                    vehicleCategory = VehicleCategory.COMMERCIAL_TAXI,
                    vehicleModel = "Toyota Corolla Poly Shuttle",
                    licensePlate = "RIV-309-PL",
                    vehicleRegistration = "CEAPOLY-SH-01",
                    origin = "Rumuola Poly Gate",
                    destination = "School of Engineering Block",
                    corridor = "Rumuola Poly Gate → School of Engineering Block",
                    departureTime = "3 mins away",
                    departureDate = "Today",
                    totalFareNaira = 600,
                    totalCapacity = 4,
                    bookedSeatsCount = 1,
                    pickupPoints = "Rumuola Poly Gate, School of Engineering Block",
                    fareSharingMode = FareSharingMode.FIXED_PER_SEAT,
                    status = TripStatus.OPEN_FOR_BOOKINGS,
                    notes = "Queue #1 (Joined 10:02 AM) • Serving Rumuola Poly Gate → Engineering Block",
                    isOnline = true,
                    liveTrackingActive = true,
                    driverLat = 4.8312,
                    driverLng = 7.0085,
                    estimatedArrivalMinutes = 3,
                    distanceToPassengerMeters = 190,
                    queuePriorityRank = 1,
                    entryTimeFormatted = "10:02 AM"
                ),
                TripEntity(
                    id = "trip_iaue_401",
                    tripNumber = "RYD-IAUE-401",
                    universityId = "uni_iaue",
                    campusId = "camp_iaue_main",
                    parkId = "park_iaue_main",
                    serviceZoneId = "zone_iaue_gate",
                    serviceZoneName = "Rumuolumeni Main Gate Zone",
                    driverId = "usr_driv_iaue_1",
                    driverName = "Godspower Abbey",
                    driverPhone = "+234 808 610 3390",
                    driverRating = 4.89f,
                    vehicleCategory = VehicleCategory.SUV_SIENNA,
                    vehicleModel = "Toyota Sienna IAUE Shuttle",
                    licensePlate = "RIV-412-ED",
                    vehicleRegistration = "IAUE-SH-01",
                    origin = "Rumuolumeni Main Gate",
                    destination = "Senate Building",
                    corridor = "Rumuolumeni Main Gate → Senate Building",
                    departureTime = "3 mins away",
                    departureDate = "Today",
                    totalFareNaira = 900,
                    totalCapacity = 6,
                    bookedSeatsCount = 2,
                    pickupPoints = "Rumuolumeni Main Gate, Senate Building",
                    fareSharingMode = FareSharingMode.FIXED_PER_SEAT,
                    status = TripStatus.OPEN_FOR_BOOKINGS,
                    notes = "Queue #1 (Joined 10:04 AM) • Serving Rumuolumeni Main Gate → Senate Building",
                    isOnline = true,
                    liveTrackingActive = true,
                    driverLat = 4.8050,
                    driverLng = 6.9420,
                    estimatedArrivalMinutes = 3,
                    distanceToPassengerMeters = 220,
                    queuePriorityRank = 1,
                    entryTimeFormatted = "10:04 AM"
                )
            )
            tripDao.insertAll(initialTrips)

            // 12. Initial Bookings (Strictly Scoped by institutionId & campusId)
            val initialBookings = listOf(
                // UNIPORT Bookings
                BookingEntity(
                    id = "bk_uni_001",
                    institutionId = "uni_uniport",
                    campusId = "camp_abuja",
                    tripId = "trip_uni_101",
                    passengerId = "usr_pass_1",
                    passengerName = "Amaka Okon",
                    passengerPhone = "+234 803 712 9043",
                    passengerIndex = 1,
                    pickupLocation = "Engineering Block (Current GPS Location)",
                    destinationLocation = "Main Gate",
                    seatsCount = 1,
                    farePerSeatNaira = 200,
                    totalFareNaira = 200,
                    bookingReference = "RR-48291",
                    verificationCode = "7394",
                    qrCodePayload = "RYDROVER|RR-48291|7394|trip_uni_101|usr_pass_1",
                    isQrVerified = false,
                    status = BookingStatus.CONFIRMED,
                    boardingStatus = "Waiting",
                    passengerLat = 4.9035,
                    passengerLng = 6.9262,
                    distanceMeters = 280,
                    estimatedArrivalMinutes = 3,
                    driverNotes = "Waiting 280m from Engineering Block shuttle stand"
                ),
                BookingEntity(
                    id = "bk_uni_002",
                    institutionId = "uni_uniport",
                    campusId = "camp_abuja",
                    tripId = "trip_uni_101",
                    passengerId = "usr_pass_2",
                    passengerName = "Tamuno George",
                    passengerPhone = "+234 812 554 9912",
                    passengerIndex = 2,
                    pickupLocation = "Faculty of Science",
                    destinationLocation = "Main Gate",
                    seatsCount = 1,
                    farePerSeatNaira = 200,
                    totalFareNaira = 200,
                    bookingReference = "RR-48295",
                    verificationCode = "5182",
                    qrCodePayload = "RYDROVER|RR-48295|5182|trip_uni_101|usr_pass_2",
                    isQrVerified = true,
                    status = BookingStatus.CONFIRMED,
                    boardingStatus = "Boarded",
                    passengerLat = 4.9044,
                    passengerLng = 6.9258,
                    distanceMeters = 120,
                    estimatedArrivalMinutes = 2,
                    driverNotes = "QR Verified & Boarded at Faculty of Science"
                ),
                // UST / RSU Bookings (Strictly for UST Drivers & UST Passengers)
                BookingEntity(
                    id = "bk_rsu_001",
                    institutionId = "uni_rsu",
                    campusId = "camp_rsu_main",
                    tripId = "trip_rsu_201",
                    passengerId = "usr_pass_rsu_1",
                    passengerName = "Preye Dokubo",
                    passengerPhone = "+234 806 882 1940",
                    passengerIndex = 1,
                    pickupLocation = "UST Main Gate",
                    destinationLocation = "UST Faculty of Law",
                    seatsCount = 1,
                    farePerSeatNaira = 150,
                    totalFareNaira = 150,
                    bookingReference = "RR-62104",
                    verificationCode = "8419",
                    qrCodePayload = "RYDROVER|RR-62104|8419|trip_rsu_201|usr_pass_rsu_1",
                    isQrVerified = false,
                    status = BookingStatus.CONFIRMED,
                    boardingStatus = "Waiting",
                    passengerLat = 4.7965,
                    passengerLng = 6.9820,
                    distanceMeters = 180,
                    estimatedArrivalMinutes = 2,
                    driverNotes = "Waiting at UST Main Gate shuttle canopy"
                ),
                BookingEntity(
                    id = "bk_rsu_002",
                    institutionId = "uni_rsu",
                    campusId = "camp_rsu_main",
                    tripId = "trip_rsu_201",
                    passengerId = "usr_pass_rsu_2",
                    passengerName = "Ibimina West",
                    passengerPhone = "+234 813 402 7711",
                    passengerIndex = 2,
                    pickupLocation = "UST Amphitheatre",
                    destinationLocation = "UST Faculty of Law",
                    seatsCount = 1,
                    farePerSeatNaira = 150,
                    totalFareNaira = 150,
                    bookingReference = "RR-62109",
                    verificationCode = "3920",
                    qrCodePayload = "RYDROVER|RR-62109|3920|trip_rsu_201|usr_pass_rsu_2",
                    isQrVerified = true,
                    status = BookingStatus.CONFIRMED,
                    boardingStatus = "Boarded",
                    passengerLat = 4.7975,
                    passengerLng = 6.9830,
                    distanceMeters = 95,
                    estimatedArrivalMinutes = 1,
                    driverNotes = "QR Verified & Boarded at UST Amphitheatre Stand"
                ),
                // IAUE Bookings (Strictly for IAUE Drivers & IAUE Passengers)
                BookingEntity(
                    id = "bk_iaue_001",
                    institutionId = "uni_iaue",
                    campusId = "camp_iaue_main",
                    tripId = "trip_iaue_401",
                    passengerId = "usr_pass_iaue_1",
                    passengerName = "Chinaza O.",
                    passengerPhone = "+234 809 331 5520",
                    passengerIndex = 1,
                    pickupLocation = "Rumuolumeni Main Gate",
                    destinationLocation = "Senate Building",
                    seatsCount = 1,
                    farePerSeatNaira = 500,
                    totalFareNaira = 500,
                    bookingReference = "RR-74102",
                    verificationCode = "6284",
                    qrCodePayload = "RYDROVER|RR-74102|6284|trip_iaue_401|usr_pass_iaue_1",
                    isQrVerified = false,
                    status = BookingStatus.CONFIRMED,
                    boardingStatus = "Waiting",
                    paymentStatus = PaymentStatus.PAID,
                    passengerLat = 4.8052,
                    passengerLng = 6.9422,
                    distanceMeters = 210,
                    estimatedArrivalMinutes = 3,
                    driverNotes = "Waiting at Rumuolumeni Main Gate shuttle stand"
                ),
                // CEAPOLY Bookings (Strictly for CEAPOLY Drivers & CEAPOLY Passengers)
                BookingEntity(
                    id = "bk_ceapoly_001",
                    institutionId = "uni_ceapoly",
                    campusId = "camp_ceapoly_main",
                    tripId = "trip_ceapoly_301",
                    passengerId = "usr_pass_ceapoly_1",
                    passengerName = "Kelechi N.",
                    passengerPhone = "+234 807 441 9021",
                    passengerIndex = 1,
                    pickupLocation = "Rumuola Poly Gate",
                    destinationLocation = "School of Engineering Block",
                    seatsCount = 1,
                    farePerSeatNaira = 300,
                    totalFareNaira = 300,
                    bookingReference = "RR-85119",
                    verificationCode = "4192",
                    qrCodePayload = "RYDROVER|RR-85119|4192|trip_ceapoly_301|usr_pass_ceapoly_1",
                    isQrVerified = false,
                    status = BookingStatus.CONFIRMED,
                    boardingStatus = "Waiting",
                    paymentStatus = PaymentStatus.PAID,
                    passengerLat = 4.8314,
                    passengerLng = 7.0087,
                    distanceMeters = 190,
                    estimatedArrivalMinutes = 3,
                    driverNotes = "Waiting at Rumuola Poly Gate bay"
                )
            )
            bookingDao.insertAll(initialBookings)

            // 13. Coordinated Requests (Scoped by institutionId & campusId)
            val initialRequests = listOf(
                CoordinatedRequestEntity(
                    id = "req_uni_001",
                    institutionId = "uni_uniport",
                    campusId = "camp_abuja",
                    passengerId = "usr_pass_3",
                    passengerName = "Ngozi Eze",
                    passengerPhone = "+234 805 119 4488",
                    pickupJunction = "Library",
                    destination = "Main Gate",
                    passengersCount = 2,
                    desiredDepartureTime = "02:00 PM",
                    notes = "Staff shuttle request for 2 university registry staff.",
                    status = "PENDING_DRIVER"
                ),
                CoordinatedRequestEntity(
                    id = "req_rsu_001",
                    institutionId = "uni_rsu",
                    campusId = "camp_rsu_main",
                    passengerId = "usr_pass_rsu_1",
                    passengerName = "Preye Dokubo",
                    passengerPhone = "+234 806 882 1940",
                    pickupJunction = "UST Faculty of Law",
                    destination = "NDDC Student Hostel",
                    passengersCount = 3,
                    desiredDepartureTime = "03:15 PM",
                    notes = "Law students group transit to NDDC Hostel.",
                    status = "PENDING_DRIVER"
                )
            )
            initialRequests.forEach { requestDao.insertRequest(it) }

            // 14. Platform Settings (No global UNIPORT hardcoding)
            platformDao.saveSettings(
                PlatformSettingsEntity(
                    id = 1,
                    activeUniversity = "RydRover Multi-Institution Platform",
                    fareSharingMode = FareSharingMode.EQUAL_SPLIT,
                    platformCommissionPercent = 5,
                    cancellationCutoffMinutes = 15,
                    pilotLocation = "Multi-Institution Transport Network",
                    emergencyContact = "+234 800 793 76837"
                )
            )

            // 15. Reports / Complaint Tickets (Scoped by institutionId)
            platformDao.insertAllReports(
                listOf(
                    ReportEntity(
                        id = "rep_uni_001",
                        institutionId = "uni_uniport",
                        campusId = "camp_abuja",
                        userId = "usr_pass_1",
                        reporterName = "Amaka Okon",
                        reporterRole = UserRole.PASSENGER,
                        category = "General Complaint",
                        passengerId = "usr_pass_1",
                        subject = "Shuttle wait time at Hostel Zone",
                        description = "Please allocate an extra shuttle during peak morning lectures (8:00 AM - 9:30 AM).",
                        routeOrTripRef = "Student Hostel Zone",
                        status = "RESOLVED",
                        adminNotes = "Additional commercial shuttle allocated to Hostel Zone corridor."
                    ),
                    ReportEntity(
                        id = "rep_uni_002",
                        institutionId = "uni_uniport",
                        campusId = "camp_abuja",
                        userId = "usr_driv_1",
                        reporterName = "Babatunde Alabi",
                        reporterRole = UserRole.DRIVER,
                        category = "Vehicle/Route Problem",
                        driverId = "usr_driv_1",
                        subject = "Parking obstruction near Engineering Block Zone",
                        description = "Private cars parked along the student pickup bay, please request campus security to clear the bay.",
                        routeOrTripRef = "Engineering Block Zone",
                        status = "OPEN"
                    ),
                    ReportEntity(
                        id = "rep_rsu_001",
                        institutionId = "uni_rsu",
                        campusId = "camp_rsu_main",
                        userId = "usr_driv_rsu_1",
                        reporterName = "Tari Horsfall",
                        reporterRole = UserRole.DRIVER,
                        category = "Vehicle/Route Problem",
                        driverId = "usr_driv_rsu_1",
                        subject = "UST Main Gate evening peak demand",
                        description = "High student turnout at UST Main Gate between 4:00 PM and 5:30 PM toward NDDC Hostel.",
                        routeOrTripRef = "UST Main Gate Zone",
                        status = "OPEN"
                    )
                )
            )

            // 16. Passenger RydWallets (Tied to authenticated passenger accounts)
            walletDao.insertAllPassengerWallets(
                listOf(
                    PassengerWalletEntity(
                        passengerId = "usr_pass_1",
                        institutionId = "uni_uniport",
                        availableBalanceNaira = 4800,
                        heldBalanceNaira = 200,
                        totalAddedNaira = 5000,
                        totalSpentNaira = 0,
                        totalRefundedNaira = 0,
                        paymentMode = PaymentEnvironmentMode.DEMO_SIMULATED
                    ),
                    PassengerWalletEntity(
                        passengerId = "usr_pass_2",
                        institutionId = "uni_uniport",
                        availableBalanceNaira = 3300,
                        heldBalanceNaira = 200,
                        totalAddedNaira = 3500,
                        totalSpentNaira = 0,
                        totalRefundedNaira = 0,
                        paymentMode = PaymentEnvironmentMode.DEMO_SIMULATED
                    ),
                    PassengerWalletEntity(
                        passengerId = "usr_pass_3",
                        institutionId = "uni_uniport",
                        availableBalanceNaira = 5000,
                        heldBalanceNaira = 0,
                        totalAddedNaira = 5000,
                        totalSpentNaira = 0,
                        totalRefundedNaira = 0,
                        paymentMode = PaymentEnvironmentMode.DEMO_SIMULATED
                    ),
                    PassengerWalletEntity(
                        passengerId = "usr_pass_rsu_1",
                        institutionId = "uni_rsu",
                        availableBalanceNaira = 4850,
                        heldBalanceNaira = 150,
                        totalAddedNaira = 5000,
                        totalSpentNaira = 0,
                        totalRefundedNaira = 0,
                        paymentMode = PaymentEnvironmentMode.DEMO_SIMULATED
                    ),
                    PassengerWalletEntity(
                        passengerId = "usr_pass_rsu_2",
                        institutionId = "uni_rsu",
                        availableBalanceNaira = 2850,
                        heldBalanceNaira = 150,
                        totalAddedNaira = 3000,
                        totalSpentNaira = 0,
                        totalRefundedNaira = 0,
                        paymentMode = PaymentEnvironmentMode.DEMO_SIMULATED
                    )
                )
            )

            // 17. Driver RydWallets (Private to authenticated drivers)
            walletDao.insertAllDriverWallets(
                listOf(
                    DriverWalletEntity(
                        driverId = "usr_driv_1",
                        institutionId = "uni_uniport",
                        availableBalanceNaira = 8400,
                        pendingEarningsNaira = 380,
                        completedTripEarningsNaira = 8400,
                        totalWithdrawnNaira = 4000,
                        bankName = "Access Bank PLC",
                        accountNumberMasked = "**** 4821",
                        accountName = "Babatunde Alabi",
                        paymentMode = PaymentEnvironmentMode.DEMO_SIMULATED
                    ),
                    DriverWalletEntity(
                        driverId = "usr_driv_2",
                        institutionId = "uni_uniport",
                        availableBalanceNaira = 6200,
                        pendingEarningsNaira = 0,
                        completedTripEarningsNaira = 6200,
                        totalWithdrawnNaira = 2000,
                        bankName = "Zenith Bank PLC",
                        accountNumberMasked = "**** 9104",
                        accountName = "Chinedu Okafor",
                        paymentMode = PaymentEnvironmentMode.DEMO_SIMULATED
                    ),
                    DriverWalletEntity(
                        driverId = "usr_driv_3",
                        institutionId = "uni_uniport",
                        availableBalanceNaira = 5100,
                        pendingEarningsNaira = 0,
                        completedTripEarningsNaira = 5100,
                        totalWithdrawnNaira = 0,
                        bankName = "First Bank of Nigeria",
                        accountNumberMasked = "**** 3319",
                        accountName = "Ibrahim Musa",
                        paymentMode = PaymentEnvironmentMode.DEMO_SIMULATED
                    ),
                    DriverWalletEntity(
                        driverId = "usr_driv_4",
                        institutionId = "uni_uniport",
                        availableBalanceNaira = 4750,
                        pendingEarningsNaira = 0,
                        completedTripEarningsNaira = 4750,
                        totalWithdrawnNaira = 0,
                        bankName = "Guaranty Trust Bank (GTCO)",
                        accountNumberMasked = "**** 7742",
                        accountName = "Etim Bassey",
                        paymentMode = PaymentEnvironmentMode.DEMO_SIMULATED
                    ),
                    DriverWalletEntity(
                        driverId = "usr_driv_rsu_1",
                        institutionId = "uni_rsu",
                        availableBalanceNaira = 9600,
                        pendingEarningsNaira = 285,
                        completedTripEarningsNaira = 9600,
                        totalWithdrawnNaira = 3500,
                        bankName = "United Bank for Africa (UBA)",
                        accountNumberMasked = "**** 6290",
                        accountName = "Tari Horsfall",
                        paymentMode = PaymentEnvironmentMode.DEMO_SIMULATED
                    ),
                    DriverWalletEntity(
                        driverId = "usr_driv_rsu_2",
                        institutionId = "uni_rsu",
                        availableBalanceNaira = 7100,
                        pendingEarningsNaira = 0,
                        completedTripEarningsNaira = 7100,
                        totalWithdrawnNaira = 0,
                        bankName = "Fidelity Bank PLC",
                        accountNumberMasked = "**** 1058",
                        accountName = "Diepreye Jack",
                        paymentMode = PaymentEnvironmentMode.DEMO_SIMULATED
                    ),
                    DriverWalletEntity(
                        driverId = "usr_driv_iaue_1",
                        institutionId = "uni_iaue",
                        availableBalanceNaira = 6850,
                        pendingEarningsNaira = 475,
                        completedTripEarningsNaira = 6850,
                        totalWithdrawnNaira = 2500,
                        bankName = "First Bank of Nigeria",
                        accountNumberMasked = "**** 5512",
                        accountName = "Godspower Abbey",
                        paymentMode = PaymentEnvironmentMode.DEMO_SIMULATED
                    ),
                    DriverWalletEntity(
                        driverId = "usr_driv_ceapoly_1",
                        institutionId = "uni_ceapoly",
                        availableBalanceNaira = 5400,
                        pendingEarningsNaira = 285,
                        completedTripEarningsNaira = 5400,
                        totalWithdrawnNaira = 1500,
                        bankName = "Guaranty Trust Bank (GTCO)",
                        accountNumberMasked = "**** 3091",
                        accountName = "Confidence Weli",
                        paymentMode = PaymentEnvironmentMode.DEMO_SIMULATED
                    )
                )
            )

            // 18. Initial Wallet Transactions
            walletDao.insertAllTransactions(
                listOf(
                    WalletTransactionEntity(
                        id = "txn_pass_1_topup",
                        userId = "usr_pass_1",
                        userRole = UserRole.PASSENGER,
                        institutionId = "uni_uniport",
                        type = WalletTransactionType.TOP_UP,
                        amountNaira = 5000,
                        paymentStatus = PaymentStatus.PAID,
                        providerReference = "SIM-TOPUP-88210",
                        title = "RydWallet Top-Up",
                        description = "Simulated Instant Bank Transfer to RydWallet",
                        timestamp = baseNow - 3_600_000L
                    ),
                    WalletTransactionEntity(
                        id = "txn_pass_1_hold",
                        userId = "usr_pass_1",
                        userRole = UserRole.PASSENGER,
                        institutionId = "uni_uniport",
                        tripId = "trip_uni_101",
                        bookingId = "bk_uni_001",
                        bookingReference = "RR-48291",
                        type = WalletTransactionType.RIDE_PAYMENT_HOLD,
                        amountNaira = 200,
                        paymentStatus = PaymentStatus.PAID,
                        providerReference = "SIM-HOLD-48291",
                        title = "Ride Fare Paid & Held (RR-48291)",
                        description = "Student Hostel → Main Gate • Assigned to Babatunde Alabi",
                        timestamp = baseNow - 900_000L
                    ),
                    WalletTransactionEntity(
                        id = "txn_pass_rsu_1_topup",
                        userId = "usr_pass_rsu_1",
                        userRole = UserRole.PASSENGER,
                        institutionId = "uni_rsu",
                        type = WalletTransactionType.TOP_UP,
                        amountNaira = 5000,
                        paymentStatus = PaymentStatus.PAID,
                        providerReference = "SIM-TOPUP-99104",
                        title = "RydWallet Top-Up",
                        description = "Simulated Instant Bank Transfer to RydWallet",
                        timestamp = baseNow - 3_600_000L
                    ),
                    WalletTransactionEntity(
                        id = "txn_pass_rsu_1_hold",
                        userId = "usr_pass_rsu_1",
                        userRole = UserRole.PASSENGER,
                        institutionId = "uni_rsu",
                        tripId = "trip_rsu_201",
                        bookingId = "bk_rsu_001",
                        bookingReference = "RR-62104",
                        type = WalletTransactionType.RIDE_PAYMENT_HOLD,
                        amountNaira = 150,
                        paymentStatus = PaymentStatus.PAID,
                        providerReference = "SIM-HOLD-62104",
                        title = "Ride Fare Paid & Held (RR-62104)",
                        description = "UST Main Gate → UST Faculty of Law • Assigned to Tari Horsfall",
                        timestamp = baseNow - 800_000L
                    ),
                    WalletTransactionEntity(
                        id = "txn_driv_1_settled",
                        userId = "usr_driv_1",
                        userRole = UserRole.DRIVER,
                        institutionId = "uni_uniport",
                        type = WalletTransactionType.DRIVER_EARNING_SETTLED,
                        amountNaira = 8400,
                        paymentStatus = PaymentStatus.PAID,
                        providerReference = "SIM-SETTLE-101",
                        title = "Completed Trips Settlement",
                        description = "Net driver earnings settled after 5% platform fee",
                        timestamp = baseNow - 7_200_000L
                    ),
                    WalletTransactionEntity(
                        id = "txn_driv_1_pending",
                        userId = "usr_driv_1",
                        userRole = UserRole.DRIVER,
                        institutionId = "uni_uniport",
                        tripId = "trip_uni_101",
                        bookingReference = "RR-48291, RR-48295",
                        type = WalletTransactionType.DRIVER_EARNING_PENDING,
                        amountNaira = 380,
                        paymentStatus = PaymentStatus.PENDING,
                        providerReference = "SIM-PEND-101",
                        title = "Pending Trip Earnings (RYD-UNIPORT-101)",
                        description = "2 paid bookings held in escrow until trip completion",
                        timestamp = baseNow - 600_000L
                    ),
                    WalletTransactionEntity(
                        id = "txn_driv_rsu_1_settled",
                        userId = "usr_driv_rsu_1",
                        userRole = UserRole.DRIVER,
                        institutionId = "uni_rsu",
                        type = WalletTransactionType.DRIVER_EARNING_SETTLED,
                        amountNaira = 9600,
                        paymentStatus = PaymentStatus.PAID,
                        providerReference = "SIM-SETTLE-201",
                        title = "Completed UST Trips Settlement",
                        description = "Net driver earnings settled after 5% platform fee",
                        timestamp = baseNow - 7_200_000L
                    ),
                    WalletTransactionEntity(
                        id = "txn_driv_rsu_1_pending",
                        userId = "usr_driv_rsu_1",
                        userRole = UserRole.DRIVER,
                        institutionId = "uni_rsu",
                        tripId = "trip_rsu_201",
                        bookingReference = "RR-62104, RR-62109",
                        type = WalletTransactionType.DRIVER_EARNING_PENDING,
                        amountNaira = 285,
                        paymentStatus = PaymentStatus.PENDING,
                        providerReference = "SIM-PEND-201",
                        title = "Pending UST Trip Earnings (RYD-RSU-201)",
                        description = "2 paid UST bookings held in escrow until trip completion",
                        timestamp = baseNow - 500_000L
                    )
                )
            )

            // 19. Initial Role-Scoped & Institution-Scoped Notifications
            notificationDao.insertAllNotifications(
                listOf(
                    NotificationEntity(
                        id = "notif_pass_1_1",
                        userId = "usr_pass_1",
                        userRole = UserRole.PASSENGER,
                        institutionId = "uni_uniport",
                        title = "Booking Confirmed & Paid (RR-48291)",
                        message = "₦200 has been authorized from your RydWallet. Driver Babatunde Alabi (UNIPORT-SH-01) is assigned to your pickup at Student Hostel.",
                        category = "PAYMENT",
                        relatedBookingId = "bk_uni_001",
                        relatedTripId = "trip_uni_101",
                        timestamp = baseNow - 850_000L
                    ),
                    NotificationEntity(
                        id = "notif_pass_rsu_1_1",
                        userId = "usr_pass_rsu_1",
                        userRole = UserRole.PASSENGER,
                        institutionId = "uni_rsu",
                        title = "Booking Confirmed & Paid (RR-62104)",
                        message = "₦150 has been authorized from your RydWallet. Driver Tari Horsfall (RSU-SH-01) is assigned to your pickup at UST Main Gate.",
                        category = "PAYMENT",
                        relatedBookingId = "bk_rsu_001",
                        relatedTripId = "trip_rsu_201",
                        timestamp = baseNow - 780_000L
                    ),
                    NotificationEntity(
                        id = "notif_driv_1_1",
                        userId = "usr_driv_1",
                        userRole = UserRole.DRIVER,
                        institutionId = "uni_uniport",
                        title = "New Paid Booking Assigned (RR-48291)",
                        message = "Amaka Okon booked 1 seat at Student Hostel → Main Gate. Payment Status: PAID (₦200). Do not collect cash.",
                        category = "BOOKING",
                        relatedBookingId = "bk_uni_001",
                        relatedTripId = "trip_uni_101",
                        timestamp = baseNow - 840_000L
                    ),
                    NotificationEntity(
                        id = "notif_driv_rsu_1_1",
                        userId = "usr_driv_rsu_1",
                        userRole = UserRole.DRIVER,
                        institutionId = "uni_rsu",
                        title = "New Paid UST Booking Assigned (RR-62104)",
                        message = "Preye Dokubo booked 1 seat at UST Main Gate → UST Faculty of Law. Payment Status: PAID (₦150). Do not collect cash.",
                        category = "BOOKING",
                        relatedBookingId = "bk_rsu_001",
                        relatedTripId = "trip_rsu_201",
                        timestamp = baseNow - 770_000L
                    )
                )
            )
        }
    }
}
