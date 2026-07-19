package com.example.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "courses")
data class Course(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val code: String,
    val professor: String,
    val colorHex: String,
    val credits: Int,
    val schedule: String,
    val description: String = "Enabling corporate leaders and professionals with practical modern career competencies.",
    val difficulty: String = "Intermediate", // Beginner, Intermediate, Advanced
    val category: String = "Professional Education", // Leadership, Tech, Operations
    val rating: Float = 4.8f
)

@Entity(
    tableName = "assignments",
    foreignKeys = [
        ForeignKey(
            entity = Course::class,
            parentColumns = ["id"],
            childColumns = ["courseId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["courseId"])]
)
data class Assignment(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val courseId: Int,
    val title: String,
    val description: String,
    val dueDate: Long,
    val isCompleted: Boolean = false,
    val priority: String, // "High", "Medium", "Low"
    val type: String, // "Assignment", "Exam", "Project", "Quiz", "Other"
    val score: Int? = null, // Optional score obtained
    val maxScore: Int = 100 // Out of what score
)

@Entity(tableName = "users")
data class UserAccount(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val email: String,
    val role: String, // "Super Administrator", "Administrator", "Facilitator", "Corporate Client", "Learner", "Recruitment Partner"
    val status: String = "Active", // "Active", "Suspended"
    val cohort: String = "Executive-2026",
    val performance: Double = 0.85, // 0.0 to 1.0 (85% progress)
    val attendanceRate: Double = 0.90, // 90% attendance
    val certificateIssued: Boolean = false,
    val company: String = "Wayne Enterprises"
)

@Entity(tableName = "quizzes")
data class Quiz(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val courseId: Int,
    val title: String,
    val questionsCount: Int = 10,
    val maxScore: Int = 100
)

@Entity(tableName = "announcements")
data class Announcement(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val author: String = "Super Administrator"
)

@Entity(tableName = "virtual_classes")
data class VirtualClass(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val courseId: Int,
    val title: String,
    val platform: String, // "Zoom", "Google Meet", "Microsoft Teams", "Loom", "YouTube Live"
    val url: String,
    val scheduledTime: Long,
    val durationMins: Int = 60,
    val facilitator: String = "Dr. Shazi",
    val meetingId: String = "",
    val password: String = "",
    val isReminderEnabled: Boolean = true,
    val isCalendarIntegrated: Boolean = true,
    val recordingUrl: String = "",
    val attendanceRegister: String = "" // Semicolon separated list of usernames/emails who joined
)

@Entity(tableName = "payments")
data class Payment(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val clientName: String,
    val amount: Double,
    val status: String, // "Paid", "Pending", "Overdue", "Pending Verification"
    val date: Long = System.currentTimeMillis(),
    val paymentType: String = "Manual EFT", // "Manual EFT", "Card", "Monthly Instalment", "Corporate Invoice"
    val reference: String = "",
    val userEmail: String = "",
    val notes: String = "",
    val hasInvoice: Boolean = true,
    val hasReceipt: Boolean = false,
    val courseId: Int = 0, // 0 means general / multiple
    val dueDate: Long = System.currentTimeMillis() + 14 * 24 * 3600 * 1000L
)

@Entity(tableName = "placements")
data class Placement(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val learnerName: String,
    val partnerName: String,
    val role: String,
    val status: String // "Applied", "Interviewing", "Offered", "Placed", "Rejected"
)

@Entity(tableName = "attendance")
data class AttendanceRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,
    val userName: String,
    val courseName: String,
    val date: Long = System.currentTimeMillis(),
    val status: String // "Present", "Absent", "Late"
)

@Entity(tableName = "resource_media")
data class ResourceMedia(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val courseId: Int,
    val title: String,
    val type: String, // "Document", "Video"
    val url: String,
    val durationOrSize: String // e.g. "12MB" or "14 mins"
)

@Entity(tableName = "backups")
data class BackupRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val fileName: String,
    val timestamp: Long = System.currentTimeMillis(),
    val size: String
)

@Entity(tableName = "custom_roles")
data class CustomRole(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val permissionsCount: Int,
    val description: String
)

@Entity(
    tableName = "course_modules",
    foreignKeys = [
        ForeignKey(
            entity = Course::class,
            parentColumns = ["id"],
            childColumns = ["courseId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["courseId"])]
)
data class CourseModule(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val courseId: Int,
    val title: String,
    val description: String = ""
)

@Entity(
    tableName = "course_lessons",
    foreignKeys = [
        ForeignKey(
            entity = CourseModule::class,
            parentColumns = ["id"],
            childColumns = ["moduleId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["moduleId"])]
)
data class CourseLesson(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val moduleId: Int,
    val title: String,
    val learningOutcomes: String = "",
    val estimatedTime: String = "30 mins",
    val videoUrl: String = "",
    val workbookUrl: String = "",
    val readingNotes: String = "",
    val knowledgeCheck: String = "",
    val quizTitle: String = "",
    val assignmentDesc: String = "",
    val reflectionPrompt: String = "",
    val isCompleted: Boolean = false
)

@Entity(
    tableName = "course_resource_items",
    foreignKeys = [
        ForeignKey(
            entity = Course::class,
            parentColumns = ["id"],
            childColumns = ["courseId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["courseId"])]
)
data class CourseResourceItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val courseId: Int,
    val title: String,
    val type: String, // "Video", "PDF Workbook", "PowerPoint", "Download", "External Link"
    val url: String,
    val detail: String = ""
)

@Entity(
    tableName = "course_mock_exams",
    foreignKeys = [
        ForeignKey(
            entity = Course::class,
            parentColumns = ["id"],
            childColumns = ["courseId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["courseId"])]
)
data class CourseMockExam(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val courseId: Int,
    val title: String,
    val durationMins: Int = 120,
    val questionsCount: Int = 50,
    val maxScore: Int = 100
)

@Entity(
    tableName = "course_discussions",
    foreignKeys = [
        ForeignKey(
            entity = Course::class,
            parentColumns = ["id"],
            childColumns = ["courseId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["courseId"])]
)
data class CourseDiscussionTopic(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val courseId: Int,
    val title: String,
    val author: String = "Admin",
    val content: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "course_certificates",
    foreignKeys = [
        ForeignKey(
            entity = Course::class,
            parentColumns = ["id"],
            childColumns = ["courseId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["courseId"])]
)
data class CourseCertificateConfig(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val courseId: Int,
    val title: String = "Certificate of Mastery",
    val authority: String = "Rooted Academy Board",
    val isEnabled: Boolean = true
)

@Entity(tableName = "companies")
data class Company(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val logoUrl: String = "",
    val primaryColorHex: String = "#1E5631",
    val accentColorHex: String = "#C89B3C",
    val motto: String = "Corporate Leadership Excellence"
)

@Entity(tableName = "user_course_progress")
data class UserCourseProgress(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,
    val courseId: Int,
    val progress: Double, // 0.0 to 1.0
    val grade: Int, // 0 to 100
    val complianceStatus: String, // "Compliant", "Non-Compliant", "In Progress"
    val completedDate: Long? = null,
    val certificateUrl: String = ""
)

