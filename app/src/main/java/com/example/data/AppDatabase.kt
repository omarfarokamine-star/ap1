package com.example.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

// --- Entities ---

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: Int = 1,
    val username: String,
    val displayName: String,
    val bioBubble: String,
    val followers: Int,
    val following: Int,
    val phoneNumber: String = "",
    val passwordPlain: String = "",
    val isLoggedIn: Boolean = false
)

@Entity(tableName = "posts")
data class Post(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val content: String,
    val likes: Int = 0,
    val timeText: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "discover_people")
data class DiscoverPerson(
    @PrimaryKey val id: Int,
    val name: String,
    val subtitle: String,
    val imageResName: String,
    val isFollowed: Boolean = false,
    val isClosed: Boolean = false
)

// --- DAOs ---

@Dao
interface ProfileDao {
    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    fun getProfile(): Flow<UserProfile?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: UserProfile)
}

@Dao
interface PostDao {
    @Query("SELECT * FROM posts ORDER BY timestamp DESC")
    fun getAllPosts(): Flow<List<Post>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: Post)

    @Query("SELECT COUNT(*) FROM posts")
    fun getPostsCount(): Flow<Int>
}

@Dao
interface DiscoverPersonDao {
    @Query("SELECT * FROM discover_people WHERE isClosed = 0")
    fun getActiveSuggestions(): Flow<List<DiscoverPerson>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPeople(people: List<DiscoverPerson>)

    @Query("UPDATE discover_people SET isFollowed = :isFollowed WHERE id = :id")
    suspend fun updateFollowState(id: Int, isFollowed: Boolean)

    @Query("UPDATE discover_people SET isClosed = 1 WHERE id = :id")
    suspend fun closeSuggestion(id: Int)
}

// --- Room Database ---

@Database(
    entities = [UserProfile::class, Post::class, DiscoverPerson::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract val profileDao: ProfileDao
    abstract val postDao: PostDao
    abstract val discoverPersonDao: DiscoverPersonDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "threads_profile_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
