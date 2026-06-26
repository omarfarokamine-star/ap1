package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class ProfileRepository(private val db: AppDatabase) {

    val profileFlow: Flow<UserProfile?> = db.profileDao.getProfile()
    val postsFlow: Flow<List<Post>> = db.postDao.getAllPosts()
    val suggestionsFlow: Flow<List<DiscoverPerson>> = db.discoverPersonDao.getActiveSuggestions()
    val postsCountFlow: Flow<Int> = db.postDao.getPostsCount()

    suspend fun updateProfile(profile: UserProfile) {
        db.profileDao.insertOrUpdateProfile(profile)
    }

    suspend fun addPost(content: String) {
        val post = Post(
            content = content,
            likes = (0..500).random(),
            timeText = "الآن" // "Now" in Arabic (fits the requested style)
        )
        db.postDao.insertPost(post)
    }

    suspend fun toggleSuggestionFollow(id: Int, isFollowed: Boolean) {
        db.discoverPersonDao.updateFollowState(id, isFollowed)
        // Also update followers or following stat!
        // When we follow someone, our 'following' count should update!
        val currentProfile = profileFlow.firstOrNull() ?: return
        val delta = if (isFollowed) 1 else -1
        val updatedProfile = currentProfile.copy(following = currentProfile.following + delta)
        db.profileDao.insertOrUpdateProfile(updatedProfile)
    }

    suspend fun dismissSuggestion(id: Int) {
        db.discoverPersonDao.closeSuggestion(id)
    }

    suspend fun ensureInitialData() {
        val currentProfile = profileFlow.firstOrNull()
        if (currentProfile == null) {
            // Seed Profile
            db.profileDao.insertOrUpdateProfile(
                UserProfile(
                    id = 1,
                    username = "fara_farouk",
                    displayName = "Farok Omar",
                    bioBubble = "Ask friends anything...",
                    followers = 47,
                    following = 121
                )
            )

            // Seed Suggestions
            val list = listOf(
                DiscoverPerson(
                    id = 1,
                    name = "الإخوة للمنتجات المتنوعة",
                    subtitle = "Suggested for you",
                    imageResName = "img_suggest_el_ikhwa_1782204654225",
                    isFollowed = false,
                    isClosed = false
                ),
                DiscoverPerson(
                    id = 2,
                    name = "Ali Timazghin",
                    subtitle = "2 mutuals",
                    imageResName = "img_suggest_ali_1782204668473",
                    isFollowed = false,
                    isClosed = false
                ),
                DiscoverPerson(
                    id = 3,
                    name = "Ilyes A.",
                    subtitle = "1 mutual",
                    imageResName = "img_suggest_ilyes_1782204681125",
                    isFollowed = false,
                    isClosed = false
                )
            )
            db.discoverPersonDao.insertPeople(list)
        }
    }
}
