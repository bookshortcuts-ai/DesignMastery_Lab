package com.example.data

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users ORDER BY id ASC")
    fun getAllUsers(): Flow<List<User>>

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): User?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User): Long

    @Update
    suspend fun updateUser(user: User)

    @Delete
    suspend fun deleteUser(user: User)

    @Query("DELETE FROM users WHERE id = :id")
    suspend fun deleteUserById(id: Int)
}

@Dao
interface ProjectDao {
    @Query("SELECT * FROM projects ORDER BY creationDate DESC")
    fun getAllProjects(): Flow<List<Project>>

    @Query("SELECT * FROM projects WHERE id = :id LIMIT 1")
    suspend fun getProjectById(id: Int): Project?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: Project): Long

    @Update
    suspend fun updateProject(project: Project)

    @Delete
    suspend fun deleteProject(project: Project)
}

@Dao
interface AnnouncementDao {
    @Query("SELECT * FROM announcements ORDER BY timestamp DESC")
    fun getAllAnnouncements(): Flow<List<Announcement>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnnouncement(announcement: Announcement): Long

    @Query("DELETE FROM announcements WHERE id = :id")
    suspend fun deleteAnnouncementById(id: Int)
}

@Dao
interface DiscussionDao {
    @Query("SELECT * FROM discussion_messages ORDER BY timestamp ASC")
    fun getRecentMessages(): Flow<List<DiscussionMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: DiscussionMessage): Long
}

@Dao
interface ResourceDao {
    @Query("SELECT * FROM resource_shares ORDER BY timestamp DESC")
    fun getAllResources(): Flow<List<ResourceShare>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResource(resource: ResourceShare): Long

    @Query("DELETE FROM resource_shares WHERE id = :id")
    suspend fun deleteResourceById(id: Int)
}

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications WHERE targetUsername = :username OR targetUsername = 'All' ORDER BY timestamp DESC")
    fun getNotificationsForUser(username: String): Flow<List<Notification>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: Notification): Long

    @Query("UPDATE notifications SET read = 1 WHERE id = :id")
    suspend fun markAsRead(id: Int)
}

@Dao
interface ProjectCommentDao {
    @Query("SELECT * FROM project_comments WHERE projectId = :projectId ORDER BY timestamp ASC")
    fun getCommentsForProject(projectId: Int): Flow<List<ProjectComment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComment(comment: ProjectComment): Long

    @Query("DELETE FROM project_comments WHERE id = :id")
    suspend fun deleteCommentById(id: Int)
}

@Database(
    entities = [
        User::class,
        Project::class,
        Announcement::class,
        DiscussionMessage::class,
        ResourceShare::class,
        Notification::class,
        ProjectComment::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun projectDao(): ProjectDao
    abstract fun announcementDao(): AnnouncementDao
    abstract fun discussionDao(): DiscussionDao
    abstract fun resourceDao(): ResourceDao
    abstract fun notificationDao(): NotificationDao
    abstract fun projectCommentDao(): ProjectCommentDao
}
