package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class LmsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: LmsRepository

    // Role-based state
    private val _currentRole = MutableStateFlow("Super Administrator (Founder)")
    val currentRole: StateFlow<String> = _currentRole.asStateFlow()

    // Dynamic Branding Customizer States
    private val _academyMotto = MutableStateFlow("Growing Roses from Concrete.")
    val academyMotto: StateFlow<String> = _academyMotto.asStateFlow()

    private val _primaryColor = MutableStateFlow("#1E5631") // Academy Green
    val primaryColor: StateFlow<String> = _primaryColor.asStateFlow()

    private val _brandGoldColor = MutableStateFlow("#C89B3C") // Classic Gold
    val brandGoldColor: StateFlow<String> = _brandGoldColor.asStateFlow()

    // System Settings States
    private val _notificationsEnabled = MutableStateFlow(true)
    val notificationsEnabled: StateFlow<Boolean> = _notificationsEnabled.asStateFlow()

    private val _maintenanceMode = MutableStateFlow(false)
    val maintenanceMode: StateFlow<Boolean> = _maintenanceMode.asStateFlow()

    private val _securityLevel = MutableStateFlow("High") // "Low", "Medium", "High", "Critical"
    val securityLevel: StateFlow<String> = _securityLevel.asStateFlow()

    init {
        val database = LmsDatabase.getDatabase(application)
        repository = LmsRepository(database.lmsDao())

        // Populate sample data if courses database is empty on start
        viewModelScope.launch {
            try {
                val currentCourses = repository.allCourses.first()
                if (currentCourses.isEmpty()) {
                    populateSampleData()
                }
            } catch (e: Exception) {
                populateSampleData()
            }
        }
    }

    // Streams from database
    val courses: StateFlow<List<Course>> = repository.allCourses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val assignments: StateFlow<List<Assignment>> = repository.allAssignments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val users: StateFlow<List<UserAccount>> = repository.allUsers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val quizzes: StateFlow<List<Quiz>> = repository.allQuizzes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val announcements: StateFlow<List<Announcement>> = repository.allAnnouncements
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val virtualClasses: StateFlow<List<VirtualClass>> = repository.allVirtualClasses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val payments: StateFlow<List<Payment>> = repository.allPayments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val placements: StateFlow<List<Placement>> = repository.allPlacements
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val attendance: StateFlow<List<AttendanceRecord>> = repository.allAttendance
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val resources: StateFlow<List<ResourceMedia>> = repository.allResources
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val backups: StateFlow<List<BackupRecord>> = repository.allBackups
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val customRoles: StateFlow<List<CustomRole>> = repository.allCustomRoles
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val courseModules: StateFlow<List<CourseModule>> = repository.allCourseModules
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val courseLessons: StateFlow<List<CourseLesson>> = repository.allLessons
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val courseResourceItems: StateFlow<List<CourseResourceItem>> = repository.allCourseResourceItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val courseMockExams: StateFlow<List<CourseMockExam>> = repository.allCourseMockExams
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val courseDiscussionTopics: StateFlow<List<CourseDiscussionTopic>> = repository.allCourseDiscussionTopics
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val courseCertificateConfigs: StateFlow<List<CourseCertificateConfig>> = repository.allCourseCertificateConfigs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val companies: StateFlow<List<Company>> = repository.allCompanies
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userCourseProgress: StateFlow<List<UserCourseProgress>> = repository.allUserCourseProgress
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // UI state for search and filters
    private val _courseSearchQuery = MutableStateFlow("")
    val courseSearchQuery = _courseSearchQuery.asStateFlow()

    private val _assignmentSearchQuery = MutableStateFlow("")
    val assignmentSearchQuery = _assignmentSearchQuery.asStateFlow()

    private val _selectedCourseFilter = MutableStateFlow<Int?>(null) // null means all
    val selectedCourseFilter = _selectedCourseFilter.asStateFlow()

    private val _assignmentStatusFilter = MutableStateFlow("All") // "All", "Pending", "Completed"
    val assignmentStatusFilter = _assignmentStatusFilter.asStateFlow()

    // Combined flows for filtered displays
    val filteredAssignments: StateFlow<List<Assignment>> = combine(
        assignments,
        _assignmentSearchQuery,
        _selectedCourseFilter,
        _assignmentStatusFilter
    ) { list, search, courseId, status ->
        list.filter { item ->
            val matchesSearch = item.title.contains(search, ignoreCase = true) || 
                                item.description.contains(search, ignoreCase = true)
            val matchesCourse = courseId == null || item.courseId == courseId
            val matchesStatus = when (status) {
                "Pending" -> !item.isCompleted
                "Completed" -> item.isCompleted
                else -> true
            }
            matchesSearch && matchesCourse && matchesStatus
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setRole(role: String) {
        _currentRole.value = role
    }

    fun updateBranding(motto: String, primary: String, gold: String) {
        _academyMotto.value = motto
        _primaryColor.value = primary
        _brandGoldColor.value = gold
    }

    fun updateSettings(notifications: Boolean, maintenance: Boolean, security: String) {
        _notificationsEnabled.value = notifications
        _maintenanceMode.value = maintenance
        _securityLevel.value = security
    }

    fun setCourseSearchQuery(query: String) {
        _courseSearchQuery.value = query
    }

    fun setAssignmentSearchQuery(query: String) {
        _assignmentSearchQuery.value = query
    }

    fun setCourseFilter(courseId: Int?) {
        _selectedCourseFilter.value = courseId
    }

    fun setAssignmentStatusFilter(status: String) {
        _assignmentStatusFilter.value = status
    }

    // Enterprise / Corporate Operations
    fun addCompany(name: String, logoUrl: String, primaryColorHex: String, accentColorHex: String, motto: String) {
        viewModelScope.launch {
            repository.insertCompany(Company(
                name = name,
                logoUrl = logoUrl,
                primaryColorHex = primaryColorHex,
                accentColorHex = accentColorHex,
                motto = motto
            ))
        }
    }

    fun updateCompany(company: Company) {
        viewModelScope.launch {
            repository.updateCompany(company)
        }
    }

    fun deleteCompany(company: Company) {
        viewModelScope.launch {
            repository.deleteCompany(company)
        }
    }

    fun addUserCourseProgress(userId: Int, courseId: Int, progress: Double, grade: Int, complianceStatus: String, completedDate: Long? = null, certificateUrl: String = "") {
        viewModelScope.launch {
            repository.insertUserCourseProgress(UserCourseProgress(
                userId = userId,
                courseId = courseId,
                progress = progress,
                grade = grade,
                complianceStatus = complianceStatus,
                completedDate = completedDate,
                certificateUrl = certificateUrl
            ))
        }
    }

    fun updateUserCourseProgress(progress: UserCourseProgress) {
        viewModelScope.launch {
            repository.updateUserCourseProgress(progress)
        }
    }

    // Dynamic CRUD Functions

    // Courses
    fun addCourse(
        name: String, 
        code: String, 
        professor: String, 
        colorHex: String, 
        credits: Int, 
        schedule: String,
        description: String = "Empowering professionals with practical, high-impact competence.",
        difficulty: String = "Intermediate",
        category: String = "Professional Education",
        rating: Float = 4.8f
    ) {
        viewModelScope.launch {
            repository.insertCourse(Course(
                name = name, 
                code = code, 
                professor = professor, 
                colorHex = colorHex, 
                credits = credits, 
                schedule = schedule,
                description = description,
                difficulty = difficulty,
                category = category,
                rating = rating
            ))
        }
    }

    fun updateCourse(course: Course) {
        viewModelScope.launch {
            repository.updateCourse(course)
        }
    }

    fun deleteCourse(course: Course) {
        viewModelScope.launch {
            repository.deleteCourse(course)
        }
    }

    // Assignments
    fun addAssignment(courseId: Int, title: String, description: String, dueDate: Long, priority: String, type: String, score: Int? = null, maxScore: Int = 100) {
        viewModelScope.launch {
            repository.insertAssignment(Assignment(
                courseId = courseId,
                title = title,
                description = description,
                dueDate = dueDate,
                priority = priority,
                type = type,
                score = score,
                maxScore = maxScore
            ))
        }
    }

    fun updateAssignment(assignment: Assignment) {
        viewModelScope.launch {
            repository.updateAssignment(assignment)
        }
    }

    fun toggleAssignmentCompleted(assignment: Assignment) {
        viewModelScope.launch {
            repository.updateAssignment(assignment.copy(isCompleted = !assignment.isCompleted))
        }
    }

    fun deleteAssignment(assignment: Assignment) {
        viewModelScope.launch {
            repository.deleteAssignment(assignment)
        }
    }

    // Users (Create, Delete, Suspend)
    fun createUser(name: String, email: String, role: String, cohort: String = "Executive-2026") {
        viewModelScope.launch {
            repository.insertUser(UserAccount(
                name = name,
                email = email,
                role = role,
                cohort = cohort,
                status = "Active"
            ))
        }
    }

    fun deleteUser(user: UserAccount) {
        viewModelScope.launch {
            repository.deleteUser(user)
        }
    }

    fun toggleSuspendUser(user: UserAccount) {
        viewModelScope.launch {
            val updatedStatus = if (user.status == "Active") "Suspended" else "Active"
            repository.updateUser(user.copy(status = updatedStatus))
        }
    }

    fun issueCertificate(user: UserAccount) {
        viewModelScope.launch {
            repository.updateUser(user.copy(certificateIssued = true))
        }
    }

    // Announcements
    fun createAnnouncement(title: String, content: String, author: String) {
        viewModelScope.launch {
            repository.insertAnnouncement(Announcement(
                title = title,
                content = content,
                author = author
            ))
        }
    }

    fun deleteAnnouncement(announcement: Announcement) {
        viewModelScope.launch {
            repository.deleteAnnouncement(announcement)
        }
    }

    // Quizzes
    fun createQuiz(courseId: Int, title: String, questionsCount: Int, maxScore: Int) {
        viewModelScope.launch {
            repository.insertQuiz(Quiz(
                courseId = courseId,
                title = title,
                questionsCount = questionsCount,
                maxScore = maxScore
            ))
        }
    }

    fun deleteQuiz(quiz: Quiz) {
        viewModelScope.launch {
            repository.deleteQuiz(quiz)
        }
    }

    // --- COURSE BUILDER ACTIONS ---

    // Modules
    fun addModule(courseId: Int, title: String, description: String = "") {
        viewModelScope.launch {
            repository.insertModule(CourseModule(courseId = courseId, title = title, description = description))
        }
    }

    fun updateModule(module: CourseModule) {
        viewModelScope.launch {
            repository.updateModule(module)
        }
    }

    fun deleteModule(module: CourseModule) {
        viewModelScope.launch {
            repository.deleteModule(module)
        }
    }

    // Lessons
    fun addLesson(
        moduleId: Int,
        title: String,
        learningOutcomes: String = "",
        estimatedTime: String = "30 mins",
        videoUrl: String = "",
        workbookUrl: String = "",
        readingNotes: String = "",
        knowledgeCheck: String = "",
        quizTitle: String = "",
        assignmentDesc: String = "",
        reflectionPrompt: String = ""
    ) {
        viewModelScope.launch {
            repository.insertLesson(CourseLesson(
                moduleId = moduleId,
                title = title,
                learningOutcomes = learningOutcomes,
                estimatedTime = estimatedTime,
                videoUrl = videoUrl,
                workbookUrl = workbookUrl,
                readingNotes = readingNotes,
                knowledgeCheck = knowledgeCheck,
                quizTitle = quizTitle,
                assignmentDesc = assignmentDesc,
                reflectionPrompt = reflectionPrompt
            ))
        }
    }

    fun updateLesson(lesson: CourseLesson) {
        viewModelScope.launch {
            repository.updateLesson(lesson)
        }
    }

    fun deleteLesson(lesson: CourseLesson) {
        viewModelScope.launch {
            repository.deleteLesson(lesson)
        }
    }

    fun toggleLessonCompletion(lesson: CourseLesson) {
        viewModelScope.launch {
            repository.updateLesson(lesson.copy(isCompleted = !lesson.isCompleted))
        }
    }

    // Resource items (Videos, PDF Workbooks, PowerPoints, Downloads, External Links)
    fun addCourseResourceItem(courseId: Int, title: String, type: String, url: String, detail: String = "") {
        viewModelScope.launch {
            repository.insertResourceItem(CourseResourceItem(courseId = courseId, title = title, type = type, url = url, detail = detail))
        }
    }

    fun deleteCourseResourceItem(item: CourseResourceItem) {
        viewModelScope.launch {
            repository.deleteResourceItem(item)
        }
    }

    // Mock Exams
    fun addCourseMockExam(courseId: Int, title: String, durationMins: Int = 120, questionsCount: Int = 50, maxScore: Int = 100) {
        viewModelScope.launch {
            repository.insertMockExam(CourseMockExam(courseId = courseId, title = title, durationMins = durationMins, questionsCount = questionsCount, maxScore = maxScore))
        }
    }

    fun deleteCourseMockExam(exam: CourseMockExam) {
        viewModelScope.launch {
            repository.deleteMockExam(exam)
        }
    }

    // Discussion Board Topics
    fun addCourseDiscussionTopic(courseId: Int, title: String, author: String = "Admin", content: String = "") {
        viewModelScope.launch {
            repository.insertDiscussionTopic(CourseDiscussionTopic(courseId = courseId, title = title, author = author, content = content))
        }
    }

    fun deleteCourseDiscussionTopic(topic: CourseDiscussionTopic) {
        viewModelScope.launch {
            repository.deleteDiscussionTopic(topic)
        }
    }

    // Certificate Configurations
    fun upsertCourseCertificateConfig(courseId: Int, title: String, authority: String, isEnabled: Boolean) {
        viewModelScope.launch {
            val config = CourseCertificateConfig(courseId = courseId, title = title, authority = authority, isEnabled = isEnabled)
            repository.insertCertificateConfig(config)
        }
    }

    // Virtual Classes (Zoom, Google Meet, Microsoft Teams, Loom, YouTube Live)
    fun scheduleVirtualClass(
        courseId: Int,
        title: String,
        platform: String,
        url: String,
        scheduledTime: Long,
        durationMins: Int = 60,
        facilitator: String = "Dr. Shazi",
        meetingId: String = "",
        password: String = "",
        isReminderEnabled: Boolean = true,
        isCalendarIntegrated: Boolean = true,
        recordingUrl: String = "",
        attendanceRegister: String = ""
    ) {
        viewModelScope.launch {
            repository.insertVirtualClass(VirtualClass(
                courseId = courseId,
                title = title,
                platform = platform,
                url = url,
                scheduledTime = scheduledTime,
                durationMins = durationMins,
                facilitator = facilitator,
                meetingId = meetingId,
                password = password,
                isReminderEnabled = isReminderEnabled,
                isCalendarIntegrated = isCalendarIntegrated,
                recordingUrl = recordingUrl,
                attendanceRegister = attendanceRegister
            ))
        }
    }

    fun updateVirtualClass(virtualClass: VirtualClass) {
        viewModelScope.launch {
            repository.updateVirtualClass(virtualClass)
        }
    }

    fun deleteVirtualClass(virtualClass: VirtualClass) {
        viewModelScope.launch {
            repository.deleteVirtualClass(virtualClass)
        }
    }

    // Upload Documents & Videos
    fun uploadResource(courseId: Int, title: String, type: String, url: String, durationOrSize: String) {
        viewModelScope.launch {
            repository.insertResource(ResourceMedia(
                courseId = courseId,
                title = title,
                type = type,
                url = url,
                durationOrSize = durationOrSize
            ))
        }
    }

    fun deleteResource(resource: ResourceMedia) {
        viewModelScope.launch {
            repository.deleteResource(resource)
        }
    }

    // Payments
    fun logPayment(clientName: String, amount: Double, status: String) {
        viewModelScope.launch {
            repository.insertPayment(Payment(
                clientName = clientName,
                amount = amount,
                status = status
            ))
        }
    }

    fun updatePaymentStatus(payment: Payment, newStatus: String) {
        viewModelScope.launch {
            repository.updatePayment(payment.copy(status = newStatus))
        }
    }

    // Placement status
    fun updatePlacementStatus(placement: Placement, newStatus: String) {
        viewModelScope.launch {
            repository.updatePlacement(placement.copy(status = newStatus))
        }
    }

    fun addPlacementCandidate(learnerName: String, partnerName: String, role: String, status: String) {
        viewModelScope.launch {
            repository.insertPlacement(Placement(
                learnerName = learnerName,
                partnerName = partnerName,
                role = role,
                status = status
            ))
        }
    }

    // Attendance
    fun logAttendance(userId: Int, userName: String, courseName: String, status: String) {
        viewModelScope.launch {
            repository.insertAttendance(AttendanceRecord(
                userId = userId,
                userName = userName,
                courseName = courseName,
                status = status
            ))
        }
    }

    // Custom roles creator
    fun createCustomRole(name: String, permissionsCount: Int, description: String) {
        viewModelScope.launch {
            repository.insertCustomRole(CustomRole(
                name = name,
                permissionsCount = permissionsCount,
                description = description
            ))
        }
    }

    fun deleteCustomRole(customRole: CustomRole) {
        viewModelScope.launch {
            repository.deleteCustomRole(customRole)
        }
    }

    // Backup & Restore
    fun triggerBackup() {
        viewModelScope.launch {
            val randomNum = (100..999).random()
            val size = "%.1f MB".format((2.0 + Math.random() * 8.0))
            repository.insertBackup(BackupRecord(
                fileName = "rooted_backup_2026_${randomNum}.sql",
                size = size
            ))
        }
    }

    fun restoreBackup(backup: BackupRecord) {
        // Simple mock restore: re-populates baseline sample data 
        viewModelScope.launch {
            populateSampleData()
        }
    }

    private suspend fun populateSampleData() {
        // Insert baseline courses
        val csId = repository.insertCourse(Course(
            name = "Architecting Systems for Scale",
            code = "TECH 401",
            professor = "Dr. Angela Yu",
            colorHex = "#1E5631", // Primary Green
            credits = 4,
            schedule = "Mon, Wed 10:00 AM",
            description = "Master architectural scale, caching structures, microservices, and high-availability paradigms to deliver flawless systems.",
            difficulty = "Advanced",
            category = "Technology & Systems",
            rating = 4.9f
        )).toInt()

        val mathId = repository.insertCourse(Course(
            name = "Executive Leadership & Resilience",
            code = "LEAD 502",
            professor = "Dir. James Maxwell",
            colorHex = "#C89B3C", // Brand Gold
            credits = 3,
            schedule = "Tue, Thu 1:30 PM",
            description = "Cultivating strategic corporate alignment, communication matrices, and resilient high-impact team leadership.",
            difficulty = "Intermediate",
            category = "Executive Education",
            rating = 4.8f
        )).toInt()

        val litId = repository.insertCourse(Course(
            name = "Corporate Value & Forecasting",
            code = "FIN 320",
            professor = "Sen. Fellow Clara Adams",
            colorHex = "#4A4A4A", // Muted Charcoal
            credits = 3,
            schedule = "Friday 9:00 AM",
            description = "Unpack quantitative metrics, risk boundaries, forecasting modeling, and sustainable corporate capital allocation.",
            difficulty = "Advanced",
            category = "Business Strategy",
            rating = 4.7f
        )).toInt()

        val now = System.currentTimeMillis()
        val oneDay = 24 * 60 * 60 * 1000L

        // TECH 401 Assignments
        repository.insertAssignment(Assignment(
            courseId = csId,
            title = "Case Study: Fault-Tolerant Clusters",
            description = "Map and analyze the failover capabilities of multi-region active-active distributed clusters.",
            dueDate = now - 2 * oneDay,
            isCompleted = true,
            priority = "High",
            type = "Assignment"
        ))

        repository.insertAssignment(Assignment(
            courseId = csId,
            title = "Architecture Midterm Review",
            description = "Quantitative evaluation covering Consistency, Availability, Partition Tolerance (CAP), and Consensus models.",
            dueDate = now + 4 * oneDay,
            isCompleted = false,
            priority = "High",
            type = "Exam"
        ))

        // LEAD 502 Assignments
        repository.insertAssignment(Assignment(
            courseId = mathId,
            title = "Strategic Alignment Paper",
            description = "Draft a 1500-word corporate alignment proposal addressing cultural friction and scaling constraints.",
            dueDate = now + 2 * oneDay,
            isCompleted = false,
            priority = "Medium",
            type = "Assignment"
        ))

        // Populate sample user accounts
        repository.insertUser(UserAccount(name = "Sarah Connor", email = "founder@rootedacademy.com", role = "Super Administrator (Founder)", cohort = "Faculty", performance = 1.0, attendanceRate = 1.0))
        repository.insertUser(UserAccount(name = "Marcus Miller", email = "admin@rootedacademy.com", role = "Administrator", cohort = "Staff", performance = 0.95, attendanceRate = 0.98))
        repository.insertUser(UserAccount(name = "Prof. Angela Yu", email = "angela@rootedacademy.com", role = "Facilitator", cohort = "Faculty", performance = 0.98, attendanceRate = 0.99))
        repository.insertUser(UserAccount(name = "Wayne Corp (Bruce Wayne)", email = "bruce@waynecorp.com", role = "Corporate Client", cohort = "Corporate Sponsor", performance = 0.88, attendanceRate = 0.92))
        repository.insertUser(UserAccount(name = "Peter Parker", email = "peter.parker@student.com", role = "Learner", cohort = "Executive-2026", performance = 0.82, attendanceRate = 0.88))
        repository.insertUser(UserAccount(name = "Tony Stark", email = "recruiting@starkindustries.com", role = "Recruitment Partner", cohort = "Enterprise Partner", performance = 0.90, attendanceRate = 0.95))
        
        // Quizzes
        repository.insertQuiz(Quiz(courseId = csId, title = "Consistency & CAP Theorem Sprint Quiz", questionsCount = 5, maxScore = 50))
        repository.insertQuiz(Quiz(courseId = mathId, title = "Strategic Corporate Friction Quiz", questionsCount = 10, maxScore = 100))

        // Announcements
        repository.insertAnnouncement(Announcement(title = "Welcome Executive Cohort of 2026!", content = "Welcome to your career acceleration portal. Dive into your custom curriculum and schedule your mentoring virtual live classes."))
        repository.insertAnnouncement(Announcement(title = "Virtual Live Lab Scheduled", content = "Dr. Angela Yu is hosting an architect scalability masterclass this Wednesday via Zoom. Be sure to download the cheat sheet PDF."))

        // Virtual Classes
        repository.insertVirtualClass(VirtualClass(courseId = csId, title = "Interactive Scalability Lab 02", platform = "Zoom", url = "https://zoom.us/j/123456789", scheduledTime = now + 2 * oneDay))
        repository.insertVirtualClass(VirtualClass(courseId = mathId, title = "Strategic Communication Masterclass", platform = "Google Meet", url = "https://meet.google.com/abc-defg-hij", scheduledTime = now + 3 * oneDay))

        // Payments
        repository.insertPayment(Payment(clientName = "Wayne Enterprises Corp", amount = 15000.00, status = "Paid"))
        repository.insertPayment(Payment(clientName = "Stark Industries Career Sponsor", amount = 5000.00, status = "Pending"))
        repository.insertPayment(Payment(clientName = "Oscorp Corporate Training Program", amount = 8500.00, status = "Overdue"))

        // Placements
        repository.insertPlacement(Placement(learnerName = "Peter Parker", partnerName = "Stark Industries", role = "Cloud Architect Intern", status = "Interviewing"))
        repository.insertPlacement(Placement(learnerName = "Gwen Stacy", partnerName = "Oscorp Corp", role = "Research & Strategy Lead", status = "Offered"))
        repository.insertPlacement(Placement(learnerName = "Ned Leeds", partnerName = "Daily Bugle Systems", role = "Security Engineer", status = "Placed"))

        // Attendance records
        repository.insertAttendance(AttendanceRecord(userId = 5, userName = "Peter Parker", courseName = "Architecting Systems for Scale", status = "Present"))
        repository.insertAttendance(AttendanceRecord(userId = 5, userName = "Peter Parker", courseName = "Executive Leadership & Resilience", status = "Late"))

        // Documents and videos
        repository.insertResource(ResourceMedia(courseId = csId, title = "Distributed Systems Scaling Cheat Sheet.pdf", type = "Document", url = "https://academy.rooted.com/files/scaling_cheat_sheet.pdf", durationOrSize = "3.4 MB"))
        repository.insertResource(ResourceMedia(courseId = csId, title = "High Availability Cluster Architectures.mp4", type = "Video", url = "https://academy.rooted.com/videos/ha_clusters.mp4", durationOrSize = "18 mins"))
        repository.insertResource(ResourceMedia(courseId = mathId, title = "Conflict Friction Resolution Roadmap.pdf", type = "Document", url = "https://academy.rooted.com/files/friction_roadmap.pdf", durationOrSize = "1.8 MB"))

        // --- COURSE BUILDER SAMPLE DATA INJECTION ---
        // Modules for TECH 401 (csId)
        val m1Id = repository.insertModule(CourseModule(
            courseId = csId,
            title = "High-Availability and Load Balancing",
            description = "Explore resilient clusters, failover algorithms, and automated health probing matrices."
        )).toInt()

        val m2Id = repository.insertModule(CourseModule(
            courseId = csId,
            title = "Caching Topologies & Distributed Consensus",
            description = "Configure multi-level Redis partitioning and understand the Raft & Paxos consensus algorithms."
        )).toInt()

        // Lessons for Module 1
        repository.insertLesson(CourseLesson(
            moduleId = m1Id,
            title = "Introduction to Fault Tolerance",
            learningOutcomes = "1. Identify common cluster failure modes\n2. Design multi-region active-active clusters\n3. Configure automatic DNS routing protocols",
            estimatedTime = "45 mins",
            videoUrl = "https://academy.rooted.com/videos/fault_tolerance_intro.mp4",
            workbookUrl = "https://academy.rooted.com/files/fault_tolerance_workbook.pdf",
            readingNotes = "A fault-tolerant system operates continuously even during server disruptions. Implementing self-healing nodes paired with health checkpoints ensures negligible application downtime.",
            knowledgeCheck = "Which distributed cluster setup allows simultaneous read-write operations across multiple geographic zones with minimal latency overhead?",
            quizTitle = "Uptime Foundations & Failover Mechanics Quiz",
            assignmentDesc = "Map a multi-tier active-active architecture with standard load balancers and backup cache stores. Draft a 500-word disaster recovery policy document.",
            reflectionPrompt = "How do you reconcile data state discrepancies when multi-region replication suffers a temporary cross-continent network partition?",
            isCompleted = true
        ))

        repository.insertLesson(CourseLesson(
            moduleId = m1Id,
            title = "Load Balancing Protocols & Algorithms",
            learningOutcomes = "1. Compare Round-Robin vs. Least Connections\n2. Implement Layer 4 vs. Layer 7 balancing\n3. Understand SSL termination bottlenecks",
            estimatedTime = "30 mins",
            videoUrl = "https://academy.rooted.com/videos/load_balancing.mp4",
            workbookUrl = "https://academy.rooted.com/files/load_balancing_workbook.pdf",
            readingNotes = "Layer 4 balancing routes packets purely based on TCP/IP criteria, whereas Layer 7 balancing evaluates HTTP headers, cookies, and URI endpoints for fine-grained application routing.",
            knowledgeCheck = "When would you choose a Least-Connections balancing algorithm over a standard Weighted Round-Robin algorithm?",
            quizTitle = "L4/L7 Traffic Management Evaluation",
            assignmentDesc = "Configure a local NGINX proxy script showcasing content-aware routing to alternate upstream servers based on incoming URI query strings.",
            reflectionPrompt = "In an environment with extremely irregular request load profiles, what load metric would you rely on to trigger auto-scaling nodes?",
            isCompleted = false
        ))

        // Module & Lesson for LEAD 502 (mathId)
        val leadM1Id = repository.insertModule(CourseModule(
            courseId = mathId,
            title = "Organizational Alignment & Friction Mitigation",
            description = "Cultivate operational coherence and address structural communications bottlenecks across departments."
        )).toInt()

        repository.insertLesson(CourseLesson(
            moduleId = leadM1Id,
            title = "Managing Multi-Tier Communication Friction",
            learningOutcomes = "1. Uncover latent executive-to-engineer communication blockages\n2. Formulate objective feedback loops\n3. Drive psychological safety indices",
            estimatedTime = "60 mins",
            videoUrl = "https://academy.rooted.com/videos/friction_mitigation.mp4",
            workbookUrl = "https://academy.rooted.com/files/comms_friction_workbook.pdf",
            readingNotes = "Clear, structured channels paired with collaborative transparency eliminates communication silos. Focus on constructing shared objectives to unite diverging teams.",
            knowledgeCheck = "What is the primary psychological driver of friction during rapid organizational structural changes?",
            quizTitle = "Psychological Safety & Collaboration Quiz",
            assignmentDesc = "Draft a communication roadmap showing how to present critical architectural refactoring priorities to non-technical financial stakeholders.",
            reflectionPrompt = "Recall a recent project alignment setback. What structural communication tool would have resolved the core misunderstanding early?",
            isCompleted = false
        ))

        // Extra Resource items (Videos, PDF Workbooks, PowerPoints, Downloads, External Links)
        repository.insertResourceItem(CourseResourceItem(courseId = csId, title = "System Design Interview Cheat Sheet", type = "PDF Workbook", url = "https://academy.rooted.com/resources/system_design_cheatsheet.pdf", detail = "4.5 MB"))
        repository.insertResourceItem(CourseResourceItem(courseId = csId, title = "High Scale Database Sharding Slides", type = "PowerPoint presentation", url = "https://academy.rooted.com/resources/sharding_slides.pptx", detail = "12.8 MB"))
        repository.insertResourceItem(CourseResourceItem(courseId = csId, title = "Distributed Cache Architecture Video Lectures", type = "Video", url = "https://academy.rooted.com/resources/cache_video.mp4", detail = "45 mins"))
        repository.insertResourceItem(CourseResourceItem(courseId = csId, title = "AWS High Availability Config Templates", type = "Download", url = "https://academy.rooted.com/resources/aws_templates.zip", detail = "1.2 MB"))
        repository.insertResourceItem(CourseResourceItem(courseId = csId, title = "Official Raft Consensus Paper", type = "External Link", url = "https://raft.github.io/raft.pdf", detail = "Academic Article"))

        repository.insertResourceItem(CourseResourceItem(courseId = mathId, title = "Resilient Team Alignment Framework", type = "PDF Workbook", url = "https://academy.rooted.com/resources/alignment_framework.pdf", detail = "2.1 MB"))

        // Mock Exams
        repository.insertMockExam(CourseMockExam(courseId = csId, title = "Elite Certified Solutions Architect Mock Exam 01", durationMins = 120, questionsCount = 65, maxScore = 100))
        repository.insertMockExam(CourseMockExam(courseId = csId, title = "Advanced Infrastructure Engineering Assessment", durationMins = 180, questionsCount = 100, maxScore = 100))
        repository.insertMockExam(CourseMockExam(courseId = mathId, title = "Executive Leadership Certification Prep Evaluation", durationMins = 90, questionsCount = 50, maxScore = 100))

        // Discussion topics
        repository.insertDiscussionTopic(CourseDiscussionTopic(courseId = csId, title = "Raft vs. Paxos: Practical Tradeoffs in Production Environments", author = "Dr. Angela Yu", content = "Let's debate: why has Raft completely eclipsed Paxos in modern infrastructure systems like etcd, CockroachDB, and Consul? Is it purely understandability? Share your experiences!"))
        repository.insertDiscussionTopic(CourseDiscussionTopic(courseId = csId, title = "Addressing Thundering Herd Problems in Distributed Redis Clusters", author = "Peter Parker", content = "I am experiencing heavy load spike on backup databases when a core cached key expires. What lock mechanisms are you guys using to prevent this? Jitter/Cache stampede solutions?"))

        repository.insertDiscussionTopic(CourseDiscussionTopic(courseId = mathId, title = "Dealing with Passive Aggression during Agile Retro Meetings", author = "Dir. James Maxwell", content = "Passive-aggressive silence often masks deeper structural issues or team fatigue. How do you draw out feedback while maintaining psychological safety? Let's discuss your templates."))

        // Certificate configurations
        repository.insertCertificateConfig(CourseCertificateConfig(courseId = csId, title = "Postgraduate Masterclass Certificate in Distributed System Architecture", authority = "Rooted Academy Board of Regents", isEnabled = true))
        repository.insertCertificateConfig(CourseCertificateConfig(courseId = mathId, title = "Executive Certificate in Resilient Leadership & Organizational Strategy", authority = "Rooted Academy Board of Regents", isEnabled = true))
        repository.insertCertificateConfig(CourseCertificateConfig(courseId = litId, title = "Postgraduate Certificate in Corporate Valuation & Financial Modeling", authority = "Rooted Academy Board of Regents", isEnabled = true))

        // Backups
        repository.insertBackup(BackupRecord(fileName = "rooted_prod_backup_2026-07-10.sql", size = "5.2 MB"))

        // Seed corporate academy data
        seedCorporateAcademy()
    }

    private suspend fun seedCorporateAcademy() {
        // Insert companies
        repository.insertCompany(Company(name = "Wayne Enterprises", logoUrl = "wayne", primaryColorHex = "#1A365D", accentColorHex = "#D69E2E", motto = "Innovating Beyond Tomorrow"))
        repository.insertCompany(Company(name = "Stark Industries", logoUrl = "stark", primaryColorHex = "#9B2C2C", accentColorHex = "#ECC94B", motto = "Tech-Driven Future"))
        repository.insertCompany(Company(name = "Oscorp Corp", logoUrl = "oscorp", primaryColorHex = "#22543D", accentColorHex = "#4299E1", motto = "Advancing Science & Humanity"))
        repository.insertCompany(Company(name = "Globex Corporation", logoUrl = "globex", primaryColorHex = "#2C5282", accentColorHex = "#ED8936", motto = "Global Operations Redefined"))
        repository.insertCompany(Company(name = "Umbrella Corp", logoUrl = "umbrella", primaryColorHex = "#742A2A", accentColorHex = "#A0AEC0", motto = "Our Business is Life Itself"))

        // Insert corporate academy courses
        val courseData = listOf(
            Triple("Leadership", "CORP-LEAD", "Cultivating resilient vision, corporate alignments, and strategic leadership paradigms."),
            Triple("Customer Service Excellence", "CORP-CSE", "Exceeding expectations through high-impact customer relations and professional satisfaction standards."),
            Triple("Call Centre Excellence", "CORP-CCE", "Mastering telephonic communication matrices, problem-solving flows, and system queues."),
            Triple("Compliance", "CORP-COMP", "Understanding critical governance structures, ethical practices, and corporate policies."),
            Triple("POPIA", "CORP-POPIA", "Protection of Personal Information Act. Compliance principles, risk mitigation, and security."),
            Triple("FICA", "CORP-FICA", "Financial Intelligence Centre Act. Preventing money laundering, identity validation, and reporting."),
            Triple("FAIS", "CORP-FAIS", "Financial Advisory and Intermediary Services. Market standards, client advisory, and disclosures."),
            Triple("Sales Excellence", "CORP-SALE", "High-velocity negotiation frameworks, client discovery, and pipeline conversion excellence."),
            Triple("Communication Skills", "CORP-COMM", "Assertive writing style, active listening, conflict-de-escalation, and virtual meeting coordination."),
            Triple("Emotional Intelligence", "CORP-EQ", "Self-awareness, empathy matrix, self-regulation, and high-impact interpersonal relationships."),
            Triple("Team Building", "CORP-TEAM", "Fostering psychological safety, trust loops, collective accountability, and cohesive team scaling."),
            Triple("Conflict Resolution", "CORP-CONF", "Mediation techniques, problem-solving, neutral facilitation, and win-win corporate agreements."),
            Triple("Time Management", "CORP-TIME", "Prioritization matrices, pomodoro scaling, deep work habits, and team deadline alignment."),
            Triple("Presentation Skills", "CORP-PRES", "Slide design elegance, delivery confidence, stage presence, and storytelling frameworks."),
            Triple("SDF Fundamentals", "CORP-SDF", "Skills Development Facilitator fundamentals, training committees, and legislative alignments."),
            Triple("WSP & ATR", "CORP-WSP", "Drafting Workplace Skills Plans and Annual Training Reports for corporate accreditation.")
        )

        val insertedCourses = mutableListOf<Int>()
        courseData.forEach { (name, code, desc) ->
            val id = repository.insertCourse(Course(
                name = name,
                code = code,
                professor = "Dir. Sarah Connor",
                colorHex = "#1E5631",
                credits = 3,
                schedule = "Self-Paced Corporate",
                description = desc,
                difficulty = "Intermediate",
                category = "Corporate Academy",
                rating = 4.8f
            )).toInt()
            insertedCourses.add(id)
        }

        // Insert Employees (Learners) associated with companies
        val employees = listOf(
            Triple("John Diggle", "john.diggle@waynecorp.com", "Wayne Enterprises"),
            Triple("Felicity Smoak", "felicity@waynecorp.com", "Wayne Enterprises"),
            Triple("Oliver Queen", "oliver@waynecorp.com", "Wayne Enterprises"),
            
            Triple("Pepper Potts", "pepper@starkcorp.com", "Stark Industries"),
            Triple("Happy Hogan", "happy@starkcorp.com", "Stark Industries"),
            Triple("Rhodey Rhodes", "rhodey@starkcorp.com", "Stark Industries"),
            
            Triple("Harry Osborn", "harry@oscorp.com", "Oscorp Corp"),
            Triple("Dr. Curt Connors", "curt@oscorp.com", "Oscorp Corp"),
            
            Triple("Hank Scorpio", "scorpio@globex.com", "Globex Corporation"),
            Triple("Albert Einstein", "albert@globex.com", "Globex Corporation"),
            
            Triple("Albert Wesker", "wesker@umbrella.com", "Umbrella Corp"),
            Triple("Jill Valentine", "jill@umbrella.com", "Umbrella Corp")
        )

        val userIds = mutableListOf<Int>()
        employees.forEach { (name, email, companyName) ->
            val uId = repository.insertUser(UserAccount(
                name = name,
                email = email,
                role = "Learner",
                cohort = "Corporate Cohort",
                performance = 0.6 + Math.random() * 0.35,
                attendanceRate = 0.75 + Math.random() * 0.23,
                company = companyName
            )).toInt()
            userIds.add(uId)
            
            // Seed course progress for each employee across corporate courses
            insertedCourses.forEachIndexed { idx, cId ->
                // Every employee has progress in most courses
                if ((uId + idx) % 5 != 0) {
                    val prog = 0.2 + Math.random() * 0.8
                    val grade = (60 + Math.random() * 40).toInt()
                    val compliance = if (prog > 0.85) "Compliant" else if (prog > 0.4) "In Progress" else "Non-Compliant"
                    
                    repository.insertUserCourseProgress(UserCourseProgress(
                        userId = uId,
                        courseId = cId,
                        progress = prog,
                        grade = grade,
                        complianceStatus = compliance,
                        completedDate = if (prog > 0.85) System.currentTimeMillis() - (1..30).random() * 24 * 60 * 60 * 1000L else null,
                        certificateUrl = if (prog > 0.85) "CERT-${cId}-${uId}" else ""
                    ))
                    
                    // Also record attendance records for virtual classes
                    repository.insertAttendance(AttendanceRecord(
                        userId = uId,
                        userName = name,
                        courseName = courseData[idx].first,
                        status = if (Math.random() > 0.15) "Present" else "Absent"
                    ))
                }
            }
        }
    }
}

class LmsViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LmsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LmsViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
