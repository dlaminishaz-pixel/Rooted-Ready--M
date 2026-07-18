package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface LmsDao {
    // Courses
    @Query("SELECT * FROM courses ORDER BY code ASC")
    fun getAllCourses(): Flow<List<Course>>

    @Query("SELECT * FROM courses WHERE id = :id")
    suspend fun getCourseById(id: Int): Course?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourse(course: Course): Long

    @Update
    suspend fun updateCourse(course: Course)

    @Delete
    suspend fun deleteCourse(course: Course)

    // Assignments
    @Query("SELECT * FROM assignments ORDER BY dueDate ASC")
    fun getAllAssignments(): Flow<List<Assignment>>

    @Query("SELECT * FROM assignments WHERE courseId = :courseId ORDER BY dueDate ASC")
    fun getAssignmentsForCourse(courseId: Int): Flow<List<Assignment>>

    @Query("SELECT * FROM assignments WHERE id = :id")
    suspend fun getAssignmentById(id: Int): Assignment?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAssignment(assignment: Assignment): Long

    @Update
    suspend fun updateAssignment(assignment: Assignment)

    @Delete
    suspend fun deleteAssignment(assignment: Assignment)

    // Users
    @Query("SELECT * FROM users ORDER BY name ASC")
    fun getAllUsers(): Flow<List<UserAccount>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserAccount): Long

    @Update
    suspend fun updateUser(user: UserAccount)

    @Delete
    suspend fun deleteUser(user: UserAccount)

    // Quizzes
    @Query("SELECT * FROM quizzes ORDER BY id DESC")
    fun getAllQuizzes(): Flow<List<Quiz>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuiz(quiz: Quiz): Long

    @Delete
    suspend fun deleteQuiz(quiz: Quiz)

    // Announcements
    @Query("SELECT * FROM announcements ORDER BY timestamp DESC")
    fun getAllAnnouncements(): Flow<List<Announcement>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnnouncement(announcement: Announcement): Long

    @Delete
    suspend fun deleteAnnouncement(announcement: Announcement)

    // Virtual Classes
    @Query("SELECT * FROM virtual_classes ORDER BY scheduledTime ASC")
    fun getAllVirtualClasses(): Flow<List<VirtualClass>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVirtualClass(virtualClass: VirtualClass): Long

    @Update
    suspend fun updateVirtualClass(virtualClass: VirtualClass)

    @Delete
    suspend fun deleteVirtualClass(virtualClass: VirtualClass)

    // Payments
    @Query("SELECT * FROM payments ORDER BY date DESC")
    fun getAllPayments(): Flow<List<Payment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: Payment): Long

    @Update
    suspend fun updatePayment(payment: Payment)

    @Delete
    suspend fun deletePayment(payment: Payment)

    // Placements
    @Query("SELECT * FROM placements ORDER BY learnerName ASC")
    fun getAllPlacements(): Flow<List<Placement>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlacement(placement: Placement): Long

    @Update
    suspend fun updatePlacement(placement: Placement)

    @Delete
    suspend fun deletePlacement(placement: Placement)

    // Attendance
    @Query("SELECT * FROM attendance ORDER BY date DESC")
    fun getAllAttendance(): Flow<List<AttendanceRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendance(attendance: AttendanceRecord): Long

    @Delete
    suspend fun deleteAttendance(attendance: AttendanceRecord)

    // Resources
    @Query("SELECT * FROM resource_media ORDER BY title ASC")
    fun getAllResources(): Flow<List<ResourceMedia>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResource(resource: ResourceMedia): Long

    @Delete
    suspend fun deleteResource(resource: ResourceMedia)

    // Backups
    @Query("SELECT * FROM backups ORDER BY timestamp DESC")
    fun getAllBackups(): Flow<List<BackupRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBackup(backup: BackupRecord): Long

    @Delete
    suspend fun deleteBackup(backup: BackupRecord)

    // Custom Roles
    @Query("SELECT * FROM custom_roles ORDER BY name ASC")
    fun getAllCustomRoles(): Flow<List<CustomRole>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomRole(customRole: CustomRole): Long

    @Delete
    suspend fun deleteCustomRole(customRole: CustomRole)

    // CourseModules
    @Query("SELECT * FROM course_modules ORDER BY id ASC")
    fun getAllModules(): Flow<List<CourseModule>>

    @Query("SELECT * FROM course_modules WHERE courseId = :courseId ORDER BY id ASC")
    fun getModulesForCourse(courseId: Int): Flow<List<CourseModule>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertModule(module: CourseModule): Long

    @Update
    suspend fun updateModule(module: CourseModule)

    @Delete
    suspend fun deleteModule(module: CourseModule)

    // CourseLessons
    @Query("SELECT * FROM course_lessons WHERE moduleId = :moduleId ORDER BY id ASC")
    fun getLessonsForModule(moduleId: Int): Flow<List<CourseLesson>>

    @Query("SELECT * FROM course_lessons ORDER BY id ASC")
    fun getAllLessons(): Flow<List<CourseLesson>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLesson(lesson: CourseLesson): Long

    @Update
    suspend fun updateLesson(lesson: CourseLesson)

    @Delete
    suspend fun deleteLesson(lesson: CourseLesson)

    // CourseResourceItems
    @Query("SELECT * FROM course_resource_items ORDER BY id ASC")
    fun getAllResourceItems(): Flow<List<CourseResourceItem>>

    @Query("SELECT * FROM course_resource_items WHERE courseId = :courseId ORDER BY id ASC")
    fun getResourcesForCourse(courseId: Int): Flow<List<CourseResourceItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResourceItem(item: CourseResourceItem): Long

    @Update
    suspend fun updateResourceItem(item: CourseResourceItem)

    @Delete
    suspend fun deleteResourceItem(item: CourseResourceItem)

    // CourseMockExams
    @Query("SELECT * FROM course_mock_exams ORDER BY id ASC")
    fun getAllMockExams(): Flow<List<CourseMockExam>>

    @Query("SELECT * FROM course_mock_exams WHERE courseId = :courseId ORDER BY id ASC")
    fun getMockExamsForCourse(courseId: Int): Flow<List<CourseMockExam>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMockExam(exam: CourseMockExam): Long

    @Update
    suspend fun updateMockExam(exam: CourseMockExam)

    @Delete
    suspend fun deleteMockExam(exam: CourseMockExam)

    // CourseDiscussionTopics
    @Query("SELECT * FROM course_discussions ORDER BY timestamp DESC")
    fun getAllDiscussionTopics(): Flow<List<CourseDiscussionTopic>>

    @Query("SELECT * FROM course_discussions WHERE courseId = :courseId ORDER BY timestamp DESC")
    fun getDiscussionsForCourse(courseId: Int): Flow<List<CourseDiscussionTopic>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDiscussionTopic(topic: CourseDiscussionTopic): Long

    @Update
    suspend fun updateDiscussionTopic(topic: CourseDiscussionTopic)

    @Delete
    suspend fun deleteDiscussionTopic(topic: CourseDiscussionTopic)

    // CourseCertificateConfigs
    @Query("SELECT * FROM course_certificates")
    fun getAllCertificateConfigs(): Flow<List<CourseCertificateConfig>>

    @Query("SELECT * FROM course_certificates WHERE courseId = :courseId LIMIT 1")
    fun getCertificateConfigForCourse(courseId: Int): Flow<CourseCertificateConfig?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCertificateConfig(config: CourseCertificateConfig): Long

    @Update
    suspend fun updateCertificateConfig(config: CourseCertificateConfig)

    // Companies
    @Query("SELECT * FROM companies ORDER BY name ASC")
    fun getAllCompanies(): Flow<List<Company>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCompany(company: Company): Long

    @Update
    suspend fun updateCompany(company: Company)

    @Delete
    suspend fun deleteCompany(company: Company)

    // User Course Progress
    @Query("SELECT * FROM user_course_progress")
    fun getAllUserCourseProgress(): Flow<List<UserCourseProgress>>

    @Query("SELECT * FROM user_course_progress WHERE userId = :userId")
    fun getUserCourseProgressForUser(userId: Int): Flow<List<UserCourseProgress>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserCourseProgress(progress: UserCourseProgress): Long

    @Update
    suspend fun updateUserCourseProgress(progress: UserCourseProgress)

    @Delete
    suspend fun deleteUserCourseProgress(progress: UserCourseProgress)
}
