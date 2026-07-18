package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        Course::class,
        Assignment::class,
        UserAccount::class,
        Quiz::class,
        Announcement::class,
        VirtualClass::class,
        Payment::class,
        Placement::class,
        AttendanceRecord::class,
        ResourceMedia::class,
        BackupRecord::class,
        CustomRole::class,
        CourseModule::class,
        CourseLesson::class,
        CourseResourceItem::class,
        CourseMockExam::class,
        CourseDiscussionTopic::class,
        CourseCertificateConfig::class,
        Company::class,
        UserCourseProgress::class
    ],
    version = 5,
    exportSchema = false
)
abstract class LmsDatabase : RoomDatabase() {
    abstract fun lmsDao(): LmsDao

    companion object {
        @Volatile
        private var INSTANCE: LmsDatabase? = null

        fun getDatabase(context: Context): LmsDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    LmsDatabase::class.java,
                    "lms_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
