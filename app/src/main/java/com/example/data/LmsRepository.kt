package com.example.data

import kotlinx.coroutines.flow.Flow

class LmsRepository(private val lmsDao: LmsDao) {
    // Courses
    val allCourses: Flow<List<Course>> = lmsDao.getAllCourses()
    val allAssignments: Flow<List<Assignment>> = lmsDao.getAllAssignments()

    fun getAssignmentsForCourse(courseId: Int): Flow<List<Assignment>> {
        return lmsDao.getAssignmentsForCourse(courseId)
    }

    suspend fun getCourseById(id: Int): Course? = lmsDao.getCourseById(id)
    suspend fun insertCourse(course: Course): Long = lmsDao.insertCourse(course)
    suspend fun updateCourse(course: Course) = lmsDao.updateCourse(course)
    suspend fun deleteCourse(course: Course) = lmsDao.deleteCourse(course)

    // Assignments
    suspend fun getAssignmentById(id: Int): Assignment? = lmsDao.getAssignmentById(id)
    suspend fun insertAssignment(assignment: Assignment): Long = lmsDao.insertAssignment(assignment)
    suspend fun updateAssignment(assignment: Assignment) = lmsDao.updateAssignment(assignment)
    suspend fun deleteAssignment(assignment: Assignment) = lmsDao.deleteAssignment(assignment)

    // Users
    val allUsers: Flow<List<UserAccount>> = lmsDao.getAllUsers()
    suspend fun insertUser(user: UserAccount): Long = lmsDao.insertUser(user)
    suspend fun updateUser(user: UserAccount) = lmsDao.updateUser(user)
    suspend fun deleteUser(user: UserAccount) = lmsDao.deleteUser(user)

    // Quizzes
    val allQuizzes: Flow<List<Quiz>> = lmsDao.getAllQuizzes()
    suspend fun insertQuiz(quiz: Quiz): Long = lmsDao.insertQuiz(quiz)
    suspend fun deleteQuiz(quiz: Quiz) = lmsDao.deleteQuiz(quiz)

    // Announcements
    val allAnnouncements: Flow<List<Announcement>> = lmsDao.getAllAnnouncements()
    suspend fun insertAnnouncement(announcement: Announcement): Long = lmsDao.insertAnnouncement(announcement)
    suspend fun deleteAnnouncement(announcement: Announcement) = lmsDao.deleteAnnouncement(announcement)

    // Virtual Classes
    val allVirtualClasses: Flow<List<VirtualClass>> = lmsDao.getAllVirtualClasses()
    suspend fun insertVirtualClass(virtualClass: VirtualClass): Long = lmsDao.insertVirtualClass(virtualClass)
    suspend fun updateVirtualClass(virtualClass: VirtualClass) = lmsDao.updateVirtualClass(virtualClass)
    suspend fun deleteVirtualClass(virtualClass: VirtualClass) = lmsDao.deleteVirtualClass(virtualClass)

    // Payments
    val allPayments: Flow<List<Payment>> = lmsDao.getAllPayments()
    suspend fun insertPayment(payment: Payment): Long = lmsDao.insertPayment(payment)
    suspend fun updatePayment(payment: Payment) = lmsDao.updatePayment(payment)
    suspend fun deletePayment(payment: Payment) = lmsDao.deletePayment(payment)

    // Placements
    val allPlacements: Flow<List<Placement>> = lmsDao.getAllPlacements()
    suspend fun insertPlacement(placement: Placement): Long = lmsDao.insertPlacement(placement)
    suspend fun updatePlacement(placement: Placement) = lmsDao.updatePlacement(placement)
    suspend fun deletePlacement(placement: Placement) = lmsDao.deletePlacement(placement)

    // Attendance
    val allAttendance: Flow<List<AttendanceRecord>> = lmsDao.getAllAttendance()
    suspend fun insertAttendance(attendance: AttendanceRecord): Long = lmsDao.insertAttendance(attendance)
    suspend fun deleteAttendance(attendance: AttendanceRecord) = lmsDao.deleteAttendance(attendance)

    // Resources
    val allResources: Flow<List<ResourceMedia>> = lmsDao.getAllResources()
    suspend fun insertResource(resource: ResourceMedia): Long = lmsDao.insertResource(resource)
    suspend fun deleteResource(resource: ResourceMedia) = lmsDao.deleteResource(resource)

    // Backups
    val allBackups: Flow<List<BackupRecord>> = lmsDao.getAllBackups()
    suspend fun insertBackup(backup: BackupRecord): Long = lmsDao.insertBackup(backup)
    suspend fun deleteBackup(backup: BackupRecord) = lmsDao.deleteBackup(backup)

    // Custom Roles
    val allCustomRoles: Flow<List<CustomRole>> = lmsDao.getAllCustomRoles()
    suspend fun insertCustomRole(customRole: CustomRole): Long = lmsDao.insertCustomRole(customRole)
    suspend fun deleteCustomRole(customRole: CustomRole) = lmsDao.deleteCustomRole(customRole)

    // CourseModules
    val allCourseModules: Flow<List<CourseModule>> = lmsDao.getAllModules()
    fun getModulesForCourse(courseId: Int): Flow<List<CourseModule>> = lmsDao.getModulesForCourse(courseId)
    suspend fun insertModule(module: CourseModule): Long = lmsDao.insertModule(module)
    suspend fun updateModule(module: CourseModule) = lmsDao.updateModule(module)
    suspend fun deleteModule(module: CourseModule) = lmsDao.deleteModule(module)

    // CourseLessons
    fun getLessonsForModule(moduleId: Int): Flow<List<CourseLesson>> = lmsDao.getLessonsForModule(moduleId)
    val allLessons: Flow<List<CourseLesson>> = lmsDao.getAllLessons()
    suspend fun insertLesson(lesson: CourseLesson): Long = lmsDao.insertLesson(lesson)
    suspend fun updateLesson(lesson: CourseLesson) = lmsDao.updateLesson(lesson)
    suspend fun deleteLesson(lesson: CourseLesson) = lmsDao.deleteLesson(lesson)

    // CourseResourceItems
    val allCourseResourceItems: Flow<List<CourseResourceItem>> = lmsDao.getAllResourceItems()
    fun getResourcesForCourse(courseId: Int): Flow<List<CourseResourceItem>> = lmsDao.getResourcesForCourse(courseId)
    suspend fun insertResourceItem(item: CourseResourceItem): Long = lmsDao.insertResourceItem(item)
    suspend fun updateResourceItem(item: CourseResourceItem) = lmsDao.updateResourceItem(item)
    suspend fun deleteResourceItem(item: CourseResourceItem) = lmsDao.deleteResourceItem(item)

    // CourseMockExams
    val allCourseMockExams: Flow<List<CourseMockExam>> = lmsDao.getAllMockExams()
    fun getMockExamsForCourse(courseId: Int): Flow<List<CourseMockExam>> = lmsDao.getMockExamsForCourse(courseId)
    suspend fun insertMockExam(exam: CourseMockExam): Long = lmsDao.insertMockExam(exam)
    suspend fun updateMockExam(exam: CourseMockExam) = lmsDao.updateMockExam(exam)
    suspend fun deleteMockExam(exam: CourseMockExam) = lmsDao.deleteMockExam(exam)

    // CourseDiscussionTopics
    val allCourseDiscussionTopics: Flow<List<CourseDiscussionTopic>> = lmsDao.getAllDiscussionTopics()
    fun getDiscussionsForCourse(courseId: Int): Flow<List<CourseDiscussionTopic>> = lmsDao.getDiscussionsForCourse(courseId)
    suspend fun insertDiscussionTopic(topic: CourseDiscussionTopic): Long = lmsDao.insertDiscussionTopic(topic)
    suspend fun updateDiscussionTopic(topic: CourseDiscussionTopic) = lmsDao.updateDiscussionTopic(topic)
    suspend fun deleteDiscussionTopic(topic: CourseDiscussionTopic) = lmsDao.deleteDiscussionTopic(topic)

    // CourseCertificateConfigs
    val allCourseCertificateConfigs: Flow<List<CourseCertificateConfig>> = lmsDao.getAllCertificateConfigs()
    fun getCertificateConfigForCourse(courseId: Int): Flow<CourseCertificateConfig?> = lmsDao.getCertificateConfigForCourse(courseId)
    suspend fun insertCertificateConfig(config: CourseCertificateConfig): Long = lmsDao.insertCertificateConfig(config)
    suspend fun updateCertificateConfig(config: CourseCertificateConfig) = lmsDao.updateCertificateConfig(config)

    // Companies
    val allCompanies: Flow<List<Company>> = lmsDao.getAllCompanies()
    suspend fun insertCompany(company: Company): Long = lmsDao.insertCompany(company)
    suspend fun updateCompany(company: Company) = lmsDao.updateCompany(company)
    suspend fun deleteCompany(company: Company) = lmsDao.deleteCompany(company)

    // UserCourseProgress
    val allUserCourseProgress: Flow<List<UserCourseProgress>> = lmsDao.getAllUserCourseProgress()
    fun getUserCourseProgressForUser(userId: Int): Flow<List<UserCourseProgress>> = lmsDao.getUserCourseProgressForUser(userId)
    suspend fun insertUserCourseProgress(progress: UserCourseProgress): Long = lmsDao.insertUserCourseProgress(progress)
    suspend fun updateUserCourseProgress(progress: UserCourseProgress) = lmsDao.updateUserCourseProgress(progress)
    suspend fun deleteUserCourseProgress(progress: UserCourseProgress) = lmsDao.deleteUserCourseProgress(progress)
}
