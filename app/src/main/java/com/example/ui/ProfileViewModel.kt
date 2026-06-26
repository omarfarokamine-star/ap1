package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.ProfileRepository
import com.example.data.UserProfile
import com.example.data.Post
import com.example.data.DiscoverPerson
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ProfileRepository

    val profile: StateFlow<UserProfile?>
    val posts: StateFlow<List<Post>>
    val suggestions: StateFlow<List<DiscoverPerson>>
    val postsCount: StateFlow<Int>

    init {
        val db = AppDatabase.getDatabase(application)
        repository = ProfileRepository(db)

        profile = repository.profileFlow.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

        posts = repository.postsFlow.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        suggestions = repository.suggestionsFlow.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        postsCount = repository.postsCountFlow.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

        // Seed initial data asynchronously on start
        viewModelScope.launch {
            repository.ensureInitialData()
        }
    }

    fun editProfile(username: String, displayName: String, bioBubble: String) {
        viewModelScope.launch {
            val current = profile.value ?: UserProfile(
                id = 1,
                username = username,
                displayName = displayName,
                bioBubble = bioBubble,
                followers = 47,
                following = 121
            )
            repository.updateProfile(
                current.copy(
                    username = username,
                    displayName = displayName,
                    bioBubble = bioBubble
                )
            )
        }
    }

    fun registerUser(displayName: String, phoneNumber: String, passwordPlain: String) {
        viewModelScope.launch {
            // Generate a clean safe username from display name
            val cleanedName = displayName.lowercase().replace("\\s+".toRegex(), "_")
            val generatedUsername = if (cleanedName.isNotBlank()) cleanedName else "user_${phoneNumber.takeLast(4)}"
            
            val newUser = UserProfile(
                id = 1,
                username = generatedUsername,
                displayName = displayName.ifBlank { "مستخدم جديد" },
                bioBubble = "سألني الأصدقاء أي شيء...",
                followers = 15,
                following = 32,
                phoneNumber = phoneNumber,
                passwordPlain = passwordPlain,
                isLoggedIn = true
            )
            repository.updateProfile(newUser)
        }
    }

    fun loginUser(phoneNumber: String, passwordPlain: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val current = profile.value
            if (current != null && current.phoneNumber == phoneNumber) {
                if (current.passwordPlain == passwordPlain) {
                    repository.updateProfile(current.copy(isLoggedIn = true))
                    onResult(true, "تم تسجيل الدخول بنجاح")
                } else {
                    onResult(false, "رقم السر غير صحيح")
                }
            } else if (current != null && current.phoneNumber.isBlank()) {
                // If seeded profile user has no phone set yet, register this as their phone-pass
                repository.updateProfile(
                    current.copy(
                        phoneNumber = phoneNumber,
                        passwordPlain = passwordPlain,
                        isLoggedIn = true
                    )
                )
                onResult(true, "تم ربط وتحديث الحساب بنجاح")
            } else {
                // If account table is clean or phone doesn't match and no previous account, create one automatically
                val generatedUsername = "user_${phoneNumber.takeLast(4)}"
                val autoCreated = UserProfile(
                    id = 1,
                    username = generatedUsername,
                    displayName = "Farok Omar",
                    bioBubble = "سألني الأصدقاء أي شيء...",
                    followers = 47,
                    following = 121,
                    phoneNumber = phoneNumber,
                    passwordPlain = passwordPlain,
                    isLoggedIn = true
                )
                repository.updateProfile(autoCreated)
                onResult(true, "تم إنشاء الحساب وتفعيله بنجاح")
            }
        }
    }

    fun logoutUser() {
        viewModelScope.launch {
            val current = profile.value
            if (current != null) {
                repository.updateProfile(current.copy(isLoggedIn = false))
            }
        }
    }

    fun makePost(content: String) {
        viewModelScope.launch {
            repository.addPost(content)
        }
    }

    fun toggleFollow(id: Int, isFollowed: Boolean) {
        viewModelScope.launch {
            repository.toggleSuggestionFollow(id, isFollowed)
        }
    }

    fun dismissSearchSuggestion(id: Int) {
        viewModelScope.launch {
            repository.dismissSuggestion(id)
        }
    }

    fun resetData() {
        viewModelScope.launch {
            val db = AppDatabase.getDatabase(getApplication())
            db.clearAllTables()
            repository.ensureInitialData()
        }
    }
}
