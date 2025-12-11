package com.example.languagebuddy.data.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import android.database.Cursor

@Database(
    entities = [
        AccountEntity::class,
        UserEntity::class,
        SessionEntity::class,
        UserSessionJoinEntity::class,
        FriendEntity::class,
        MessageEntity::class,
        FriendRequestEntity::class,
        FriendshipEntity::class,
        FriendRatingEntity::class,
        ChatEntity::class,
        SessionInviteEntity::class,
        NotificationEntity::class
    ],
    version = 9,
    exportSchema = false
)
abstract class LanguageBuddyDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountDao
    abstract fun userDao(): UserDao
    abstract fun sessionDao(): SessionDao
    abstract fun userSessionJoinDao(): UserSessionJoinDao
    abstract fun friendDao(): FriendDao
    abstract fun messageDao(): MessageDao
    abstract fun chatDao(): ChatDao
    abstract fun sessionInviteDao(): SessionInviteDao
    abstract fun notificationDao(): NotificationDao

    companion object {
        @Volatile
        private var INSTANCE: LanguageBuddyDatabase? = null

        private val MIGRATION_1_2 = migration(1, 2)
        private val MIGRATION_2_3 = migration(2, 3)
        private val MIGRATION_3_4 = migration(3, 4)
        private val MIGRATION_4_5 = migration(4, 5)
        private val MIGRATION_5_6 = migration(5, 6)
        private val MIGRATION_6_7 = migration(6, 7)
        private val MIGRATION_7_8 = migration(7, 8)
        private val MIGRATION_8_9 = migration(8, 9)

        private val ALL_MIGRATIONS = arrayOf(
            MIGRATION_1_2,
            MIGRATION_2_3,
            MIGRATION_3_4,
            MIGRATION_4_5,
            MIGRATION_5_6,
            MIGRATION_6_7,
            MIGRATION_7_8,
            MIGRATION_8_9
        )

        fun getInstance(context: Context): LanguageBuddyDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    LanguageBuddyDatabase::class.java,
                    "language_buddy_db"
                )
                    .addMigrations(*ALL_MIGRATIONS)
                    .build()
                    .also { INSTANCE = it }
            }
        }

        private fun migration(from: Int, to: Int) = object : Migration(from, to) {
            override fun migrate(db: SupportSQLiteDatabase) {
                ensureLatestSchema(db)
            }
        }

        private fun ensureLatestSchema(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `accounts` (
                    `accountId` INTEGER NOT NULL DEFAULT 0,
                    `email` TEXT NOT NULL,
                    `name` TEXT,
                    `displayName` TEXT,
                    `status` TEXT NOT NULL DEFAULT 'student',
                    `bio` TEXT,
                    `languages` TEXT,
                    `specialties` TEXT,
                    `timeZone` TEXT,
                    `location` TEXT,
                    `availability` TEXT,
                    `createdAt` INTEGER NOT NULL DEFAULT 0,
                    `rating` REAL NOT NULL DEFAULT 0.0,
                    `ratingCount` INTEGER NOT NULL DEFAULT 0,
                    PRIMARY KEY(`email`)
                )
                """.trimIndent()
            )
            ensureColumn(db, "accounts", "accountId", "INTEGER NOT NULL DEFAULT 0")
            ensureColumn(db, "accounts", "displayName", "TEXT")
            ensureColumn(db, "accounts", "languages", "TEXT")
            ensureColumn(db, "accounts", "timeZone", "TEXT")
            ensureColumn(db, "accounts", "location", "TEXT")
            ensureColumn(db, "accounts", "createdAt", "INTEGER NOT NULL DEFAULT 0")
            ensureColumn(db, "accounts", "rating", "REAL NOT NULL DEFAULT 0.0")
            ensureColumn(db, "accounts", "ratingCount", "INTEGER NOT NULL DEFAULT 0")
            ensureColumn(db, "accounts", "status", "TEXT NOT NULL DEFAULT 'student'")
            ensureColumn(db, "accounts", "bio", "TEXT")
            ensureColumn(db, "accounts", "specialties", "TEXT")
            ensureColumn(db, "accounts", "availability", "TEXT")
            ensureColumn(db, "accounts", "ratingCount", "INTEGER NOT NULL DEFAULT 0")

            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `users` (
                    `email` TEXT NOT NULL,
                    `name` TEXT,
                    `languages` TEXT,
                    `timeZone` TEXT,
                    PRIMARY KEY(`email`)
                )
                """.trimIndent()
            )
            ensureColumn(db, "users", "languages", "TEXT")
            ensureColumn(db, "users", "timeZone", "TEXT")

            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `sessions` (
                    `sessionId` TEXT NOT NULL,
                    `hostEmail` TEXT NOT NULL,
                    `title` TEXT NOT NULL,
                    `language` TEXT,
                    `description` TEXT,
                    `date` TEXT NOT NULL,
                    `time` TEXT NOT NULL,
                    `duration` TEXT NOT NULL,
                    PRIMARY KEY(`sessionId`)
                )
                """.trimIndent()
            )
            ensureColumn(db, "sessions", "language", "TEXT")
            ensureColumn(db, "sessions", "description", "TEXT")
            ensureColumn(db, "sessions", "duration", "TEXT NOT NULL DEFAULT ''")

            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `user_session_join` (
                    `email` TEXT NOT NULL,
                    `sessionId` TEXT NOT NULL,
                    PRIMARY KEY(`email`, `sessionId`)
                )
                """.trimIndent()
            )

            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `friends` (
                    `userEmail` TEXT NOT NULL,
                    `friendEmail` TEXT NOT NULL,
                    PRIMARY KEY(`userEmail`, `friendEmail`)
                )
                """.trimIndent()
            )

            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `messages` (
                    `messageId` TEXT NOT NULL,
                    `chatId` INTEGER NOT NULL DEFAULT 0,
                    `senderId` INTEGER NOT NULL DEFAULT 0,
                    `receiverId` INTEGER NOT NULL DEFAULT 0,
                    `senderEmail` TEXT NOT NULL,
                    `receiverEmail` TEXT NOT NULL,
                    `message` TEXT NOT NULL,
                    `timestamp` INTEGER NOT NULL,
                    PRIMARY KEY(`messageId`)
                )
                """.trimIndent()
            )
            ensureColumn(db, "messages", "chatId", "INTEGER NOT NULL DEFAULT 0")
            ensureColumn(db, "messages", "senderId", "INTEGER NOT NULL DEFAULT 0")
            ensureColumn(db, "messages", "receiverId", "INTEGER NOT NULL DEFAULT 0")
            ensureColumn(db, "messages", "senderEmail", "TEXT NOT NULL DEFAULT ''")
            ensureColumn(db, "messages", "receiverEmail", "TEXT NOT NULL DEFAULT ''")
            ensureColumn(db, "messages", "timestamp", "INTEGER NOT NULL DEFAULT 0")
            if (!hasColumn(db, "messages", "message")) {
                db.execSQL("ALTER TABLE `messages` ADD COLUMN `message` TEXT NOT NULL DEFAULT ''")
                if (hasColumn(db, "messages", "content")) {
                    db.execSQL("UPDATE `messages` SET `message` = `content` WHERE `message` = ''")
                }
            }

            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `chats` (
                    `chatId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `user1Id` INTEGER NOT NULL,
                    `user2Id` INTEGER NOT NULL
                )
                """.trimIndent()
            )

            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `friend_requests` (
                    `requestId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `fromEmail` TEXT NOT NULL,
                    `toEmail` TEXT NOT NULL,
                    `status` TEXT NOT NULL DEFAULT 'pending'
                )
                """.trimIndent()
            )
            ensureColumn(db, "friend_requests", "status", "TEXT NOT NULL DEFAULT 'pending'")

            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `friendships` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `user1Email` TEXT NOT NULL,
                    `user2Email` TEXT NOT NULL
                )
                """.trimIndent()
            )

            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `friend_ratings` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `friendEmail` TEXT NOT NULL,
                    `raterEmail` TEXT NOT NULL,
                    `rating` REAL NOT NULL,
                    `timestamp` INTEGER NOT NULL
                )
                """.trimIndent()
            )

            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `session_invites` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `senderEmail` TEXT NOT NULL,
                    `receiverEmail` TEXT NOT NULL,
                    `title` TEXT NOT NULL,
                    `language` TEXT,
                    `date` TEXT NOT NULL,
                    `time` TEXT NOT NULL,
                    `duration` TEXT NOT NULL,
                    `description` TEXT,
                    `status` TEXT NOT NULL DEFAULT 'pending',
                    `createdAt` INTEGER NOT NULL
                )
                """.trimIndent()
            )

            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `notifications` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `userEmail` TEXT NOT NULL,
                    `message` TEXT NOT NULL,
                    `createdAt` INTEGER NOT NULL
                )
                """.trimIndent()
            )
        }

        private fun ensureColumn(
            db: SupportSQLiteDatabase,
            table: String,
            column: String,
            definition: String
        ) {
            if (!hasColumn(db, table, column)) {
                db.execSQL("ALTER TABLE `$table` ADD COLUMN `$column` $definition")
            }
        }

        private fun hasColumn(
            db: SupportSQLiteDatabase,
            table: String,
            column: String
        ): Boolean {
            val cursor: Cursor = db.query("PRAGMA table_info(`$table`)")
            cursor.use {
                val nameIndex = cursor.getColumnIndex("name")
                while (cursor.moveToNext()) {
                    if (nameIndex != -1 && cursor.getString(nameIndex) == column) {
                        return true
                    }
                }
            }
            return false
        }
    }
}
