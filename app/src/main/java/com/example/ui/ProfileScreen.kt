package com.example.ui

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.data.DiscoverPerson
import com.example.data.Post
import com.example.data.UserProfile
import com.example.ui.theme.*

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val profileState by viewModel.profile.collectAsState()
    val postsState by viewModel.posts.collectAsState()
    val suggestionsState by viewModel.suggestions.collectAsState()
    val postsCountState by viewModel.postsCount.collectAsState()

    // UI state controllers
    var activeTab by remember { mutableStateOf(0) } // 0: Grid, 1: Reels, 2: Reposts, 3: Tagged
    var showEditDialog by remember { mutableStateOf(false) }
    var showCreatePostDialog by remember { mutableStateOf(false) }
    var showBubbleDialog by remember { mutableStateOf(false) }
    var showSuggestionsSection by remember { mutableStateOf(true) }
    var selectedPostForViewer by remember { mutableStateOf<Post?>(null) }
    var showAvatarViewer by remember { mutableStateOf(false) }
    var showUnderConstructionToast by remember { mutableStateOf<String?>(null) }

    // Trigger toast alerts
    LaunchedEffect(showUnderConstructionToast) {
        showUnderConstructionToast?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            showUnderConstructionToast = null
        }
    }

    val currentProfile = profileState ?: UserProfile(
        id = 1,
        username = "fara_farouk",
        displayName = "Farok Omar",
        bioBubble = "Ask friends anything...",
        followers = 47,
        following = 121
    )

    var currentBottomTab by remember { mutableStateOf("الملف الشخصي") }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = DarkBackground,
        bottomBar = {
            ThreadsBottomBar(
                activeTab = currentBottomTab,
                avatarResId = R.drawable.img_profile_avatar_1782204639267,
                onComposeClick = { showCreatePostDialog = true },
                onNavClick = { label ->
                    currentBottomTab = label
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (currentBottomTab) {
                "الرئيسية" -> {
                    HomeFeedTab(
                        posts = postsState,
                        profile = currentProfile,
                        onPostClick = { selectedPostForViewer = it },
                        onQuickPostTrigger = { showCreatePostDialog = true },
                        onResetTrigger = {
                            viewModel.resetData()
                            showUnderConstructionToast = "تمت إعادة تهيئة البيانات الافتراضية بنجاح 🔄"
                        }
                    )
                }
                "البحث" -> {
                    SearchDiscoveryTab(
                        suggestions = suggestionsState,
                        onFollowToggle = { id, followed -> viewModel.toggleFollow(id, followed) },
                        onDismissSuggestion = { viewModel.dismissSearchSuggestion(it) },
                        onSeeAllClick = {
                            showUnderConstructionToast = "استكشاف جميع المستخدمين المقترحين"
                        }
                    )
                }
                "الفيديوهات" -> {
                    ReelsSimulationTab()
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .align(Alignment.TopCenter),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                // 1. Sleek Header
                item {
                    ProfileHeader(
                        username = currentProfile.username,
                        onAddClick = { showCreatePostDialog = true },
                        onMenuClick = { showUnderConstructionToast = "تم فتح القائمة الجانبية" },
                        onOptionClick = { showUnderConstructionToast = "تم النقر على الخيارات" },
                        onLogoutClick = { viewModel.logoutUser() }
                    )
                }

                // 2. Profile Main Details Block
                item {
                    ProfileMainInfo(
                        profile = currentProfile,
                        postsCount = postsCountState,
                        onAvatarClick = { showAvatarViewer = true },
                        onBubbleClick = { showBubbleDialog = true },
                        onAddBannerClick = { showBubbleDialog = true }
                    )
                }

                // 3. Edit & Share & Suggestions Action row
                item {
                    ActionButtonsRow(
                        onEditClick = { showEditDialog = true },
                        onShareClick = {
                            showUnderConstructionToast = "تم نسخ رابط الملف الشخصي بنجاح"
                        },
                        suggestionsActive = showSuggestionsSection,
                        onSuggestionsToggle = { showSuggestionsSection = !showSuggestionsSection }
                    )
                }

                // 4. Discover People Section (Collapsible with nice transition)
                item {
                    AnimatedVisibility(
                        visible = showSuggestionsSection,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        DiscoverPeopleRow(
                            suggestions = suggestionsState,
                            onFollowToggle = { id, followed -> viewModel.toggleFollow(id, followed) },
                            onDismiss = { id -> viewModel.dismissSearchSuggestion(id) },
                            onSeeAllClick = {
                                showUnderConstructionToast = "استكشاف جميع المستخدمين المقترحين"
                            }
                        )
                    }
                }

                // Space separator
                item { Spacer(modifier = Modifier.height(12.dp)) }

                // 5. Center Icon Filters Tab Bar
                item {
                    ProfileTabs(
                        activeTab = activeTab,
                        onTabSelected = { activeTab = it }
                    )
                }

                // 6. Tab Display Content Area
                if (activeTab == 0) {
                    if (postsState.isEmpty()) {
                        item {
                            EmptyPostsState(
                                onCreateClick = { showCreatePostDialog = true }
                            )
                        }
                    } else {
                        // Display user posts beautifully
                        items(postsState) { post ->
                            PostCardItem(
                                post = post,
                                username = currentProfile.username,
                                displayName = currentProfile.displayName,
                                onClick = { selectedPostForViewer = post }
                            )
                        }
                    }
                } else {
                    item {
                        OtherTabsEmptyState(tabIndex = activeTab)
                    }
                }

                // Footer cushion spacing
                item { Spacer(modifier = Modifier.height(50.dp)) }
            }
            }
            }

            // 7. Interactive Dialogs

            // A. Edit Profile Dialog
            if (showEditDialog) {
                EditProfileDialog(
                    profile = currentProfile,
                    onDismiss = { showEditDialog = false },
                    onSave = { updatedUser, updatedDisplay, updatedBio ->
                        viewModel.editProfile(updatedUser, updatedDisplay, updatedBio)
                        showEditDialog = false
                    }
                )
            }

            // B. Create Post Dialog
            if (showCreatePostDialog) {
                CreatePostDialog(
                    onDismiss = { showCreatePostDialog = false },
                    onPostPublished = { text ->
                        if (text.isNotBlank()) {
                            viewModel.makePost(text)
                            showCreatePostDialog = false
                            showUnderConstructionToast = "تم نشر منشورك الأول بنجاح! 🎉"
                        }
                    }
                )
            }

            // C. Edit Bio Bubble Dialog (The "Ask friends anything" status pill)
            if (showBubbleDialog) {
                EditBubbleDialog(
                    currentBubble = currentProfile.bioBubble,
                    onDismiss = { showBubbleDialog = false },
                    onSave = { newBubble ->
                        viewModel.editProfile(
                            currentProfile.username,
                            currentProfile.displayName,
                            newBubble
                        )
                        showBubbleDialog = false
                    }
                )
            }

            // D. Avatar full screen viewer
            if (showAvatarViewer) {
                FullScreenAvatarDialog(
                    onDismiss = { showAvatarViewer = false }
                )
            }

            // E. Post Details viewer
            selectedPostForViewer?.let { post ->
                PostDetailsDialog(
                    post = post,
                    username = currentProfile.username,
                    displayName = currentProfile.displayName,
                    onDismiss = { selectedPostForViewer = null }
                )
            }
        }
    }
}

// --- Visual Components ---

@Composable
fun ProfileHeader(
    username: String,
    onAddClick: () -> Unit,
    onMenuClick: () -> Unit,
    onOptionClick: () -> Unit,
    onLogoutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Left trigger: Plus create icon
        IconButton(
            onClick = onAddClick,
            modifier = Modifier.testTag("header_add_button")
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Create post",
                tint = White,
                modifier = Modifier.size(24.dp)
            )
        }

        // Center Info: Private lock, username, and dropdown caret
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clickable { onOptionClick() }
                .padding(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "Private Account",
                tint = White,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = username,
                color = White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.width(2.dp))
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = "Dropdown",
                tint = White,
                modifier = Modifier.size(18.dp)
            )
        }

        // Right side triggers: Custom Threads style curl, and standard hamburger menu
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Threads Curl Composable Icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .clickable { onOptionClick() }
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                ThreadsCurlIcon(modifier = Modifier.size(22.dp))
            }

            Box {
                IconButton(
                    onClick = { showMenu = true },
                    modifier = Modifier.testTag("header_menu_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Menu options",
                        tint = White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    modifier = Modifier.background(DarkCard)
                ) {
                    DropdownMenuItem(
                        text = { Text("تسجيل الخروج", color = White, fontWeight = FontWeight.Bold) },
                        leadingIcon = { Icon(Icons.Default.ExitToApp, contentDescription = null, tint = Color.Red) },
                        onClick = {
                            showMenu = false
                            onLogoutClick()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("خيارات الحساب", color = White) },
                        leadingIcon = { Icon(Icons.Default.Settings, contentDescription = null, tint = White) },
                        onClick = {
                            showMenu = false
                            onOptionClick()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ProfileMainInfo(
    profile: UserProfile,
    postsCount: Int,
    onAvatarClick: () -> Unit,
    onBubbleClick: () -> Unit,
    onAddBannerClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Side: Rounded Avatar with interactive status bubble
            Box(
                modifier = Modifier.size(110.dp),
                contentAlignment = Alignment.Center
            ) {
                // Interactive Stack elements
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .border(1.5.dp, BorderDark, CircleShape)
                        .clickable { onAvatarClick() }
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_profile_avatar_1782204639267),
                        contentDescription = "User avatar",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Small blue round Add icon over avatar bottom right
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(bottom = 12.dp, end = 12.dp)
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(FollowBlue)
                        .border(1.5.dp, Black, CircleShape)
                        .clickable { onAddBannerClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add status story",
                        tint = White,
                        modifier = Modifier.size(14.dp)
                    )
                }

                // Interactive Bio status pill bubble floating atop avatar
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .offset(y = (-12).dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(BubbleGray)
                        .border(1.dp, BorderDark, RoundedCornerShape(16.dp))
                        .clickable { onBubbleClick() }
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = profile.bioBubble,
                        color = Color.LightGray,
                        fontSize = 9.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Right side: Horizontal Profile stats grid block
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ProfileStatColumn(countText = postsCount.toString(), label = "posts")
                ProfileStatColumn(countText = profile.followers.toString(), label = "followers")
                ProfileStatColumn(countText = profile.following.toString(), label = "following")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Display Name details
        Text(
            text = profile.displayName,
            color = White,
            fontSize = 19.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 12.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Add Banners button styled beautifully in round oval
        Box(
            modifier = Modifier
                .padding(start = 12.dp)
                .clip(RoundedCornerShape(18.dp))
                .border(1.dp, BorderDark, RoundedCornerShape(18.dp))
                .clickable { onAddBannerClick() }
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = White,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Add banners",
                    color = White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun ProfileStatColumn(
    countText: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.clickable { /* Simulate list details view */ }
    ) {
        Text(
            text = countText,
            color = White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            color = MutedGrey,
            fontSize = 12.sp
        )
    }
}

@Composable
fun ActionButtonsRow(
    onEditClick: () -> Unit,
    onShareClick: () -> Unit,
    suggestionsActive: Boolean,
    onSuggestionsToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Edit Profile button
        Button(
            onClick = onEditClick,
            colors = ButtonDefaults.buttonColors(containerColor = DarkCard),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .weight(1f)
                .height(40.dp)
                .testTag("edit_profile_button"),
            contentPadding = PaddingValues(0.dp)
        ) {
            Text(
                text = "Edit profile",
                color = White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Share Profile button
        Button(
            onClick = onShareClick,
            colors = ButtonDefaults.buttonColors(containerColor = DarkCard),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .weight(1f)
                .height(40.dp)
                .testTag("share_profile_button"),
            contentPadding = PaddingValues(0.dp)
        ) {
            Text(
                text = "Share profile",
                color = White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Person Add/Suggestions toggle button
        IconButton(
            onClick = onSuggestionsToggle,
            colors = IconButtonDefaults.iconButtonColors(containerColor = DarkCard),
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .border(if (suggestionsActive) 1.dp else 0.dp, MutedGrey, RoundedCornerShape(8.dp))
                .testTag("suggestions_toggle_button")
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Suggested accounts toggle",
                tint = White,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun DiscoverPeopleRow(
    suggestions: List<DiscoverPerson>,
    onFollowToggle: (Int, Boolean) -> Unit,
    onDismiss: (Int) -> Unit,
    onSeeAllClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.Start
    ) {
        // Section Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Discover people",
                color = White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "See all",
                color = FollowBlue,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { onSeeAllClick() }
            )
        }

        // Horizontal LazyRow containing recommendation cards
        if (suggestions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "لا توجد اقتراحات جديدة حاليًا",
                    color = MutedGrey,
                    fontSize = 13.sp
                )
            }
        } else {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(suggestions, key = { it.id }) { person ->
                    SuggestionCard(
                        person = person,
                        onFollowClick = { onFollowToggle(person.id, !person.isFollowed) },
                        onDismissClick = { onDismiss(person.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun SuggestionCard(
    person: DiscoverPerson,
    onFollowClick: () -> Unit,
    onDismissClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Resolve generated image resource ID dynamically
    val imageResId = when (person.id) {
        1 -> R.drawable.img_suggest_el_ikhwa_1782204654225
        2 -> R.drawable.img_suggest_ali_1782204668473
        else -> R.drawable.img_suggest_ilyes_1782204681125
    }

    Box(
        modifier = modifier
            .width(160.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(DarkCard)
            .border(1.dp, BorderDark, RoundedCornerShape(12.dp))
            .padding(12.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        // Dismiss 'x' button in top-right
        IconButton(
            onClick = onDismissClick,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 6.dp, y = (-6).dp)
                .size(24.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Dismiss suggested user",
                tint = MutedGrey,
                modifier = Modifier.size(16.dp)
            )
        }

        // Card Interior Center Columns
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // Avatar rounded
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .border(1.dp, BorderDark, CircleShape)
            ) {
                Image(
                    painter = painterResource(id = imageResId),
                    contentDescription = "${person.name} avatar",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Suggested name
            Text(
                text = person.name,
                color = White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )

            // Relationship/suggest descriptor
            Text(
                text = person.subtitle,
                color = MutedGrey,
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Follow / Following Action Trigger
            val buttonColor = if (person.isFollowed) DarkBackground else FollowBlue
            val buttonTextColor = if (person.isFollowed) White else White
            val buttonText = if (person.isFollowed) "Following" else "Follow"

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(buttonColor)
                    .border(
                        if (person.isFollowed) 1.dp else 0.dp,
                        BorderDark,
                        RoundedCornerShape(6.dp)
                    )
                    .clickable { onFollowClick() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = buttonText,
                    color = buttonTextColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun ProfileTabs(
    activeTab: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Tab 1: Grid (Index 0)
            TabItemButton(
                isSelected = activeTab == 0,
                onClick = { onTabSelected(0) },
                icon = { GridTabIcon(isHighlighted = activeTab == 0, modifier = Modifier.size(24.dp)) }
            )

            // Tab 2: Reels/Videos (Index 1)
            TabItemButton(
                isSelected = activeTab == 1,
                onClick = { onTabSelected(1) },
                icon = { ReelsTabIcon(isHighlighted = activeTab == 1, modifier = Modifier.size(24.dp)) }
            )

            // Tab 3: Reposts loop (Index 2)
            TabItemButton(
                isSelected = activeTab == 2,
                onClick = { onTabSelected(2) },
                icon = { RepostTabIcon(isHighlighted = activeTab == 2, modifier = Modifier.size(24.dp)) }
            )

            // Tab 4: Tagged grid (Index 3)
            TabItemButton(
                isSelected = activeTab == 3,
                onClick = { onTabSelected(3) },
                icon = { TaggedTabIcon(isHighlighted = activeTab == 3, modifier = Modifier.size(24.dp)) }
            )
        }
        // Minimal tab underline grid divider
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(0.5.dp)
                .background(BorderDark)
        )
    }
}

@Composable
fun RowScope.TabItemButton(
    isSelected: Boolean,
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .weight(1f)
            .clickable { onClick() }
            .padding(vertical = 12.dp)
            .drawBehind {
                if (isSelected) {
                    val strokeWidth = 1.5.dp.toPx()
                    drawLine(
                        color = Color.White,
                        start = Offset(0f, size.height),
                        end = Offset(size.width, size.height),
                        strokeWidth = strokeWidth
                    )
                }
            },
        contentAlignment = Alignment.Center
    ) {
        icon()
    }
}

@Composable
fun EmptyPostsState(
    onCreateClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 48.dp, bottom = 48.dp)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Create your first post",
            color = White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Share your point of view.",
            color = MutedGrey,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(20.dp))
        Button(
            onClick = onCreateClick,
            colors = ButtonDefaults.buttonColors(containerColor = FollowBlue),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .height(44.dp)
                .padding(horizontal = 12.dp)
                .testTag("create_first_post_button")
        ) {
            Text(
                text = "Create",
                color = White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun OtherTabsEmptyState(
    tabIndex: Int,
    modifier: Modifier = Modifier
) {
    val tabName = when (tabIndex) {
        1 -> "Reels / مقاطع الفيديو"
        2 -> "Reposted / إعادة النشر"
        else -> "Tagged / المنشورات المشار إليها"
    }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = when (tabIndex) {
                1 -> Icons.Default.PlayArrow
                2 -> Icons.Default.Refresh
                else -> Icons.Default.Person
            },
            contentDescription = null,
            tint = MutedGrey,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "$tabName فارغ الآن",
            color = White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "لا توجد عناصر لعرضها في هذا التبويب للملف الشخصي",
            color = MutedGrey,
            fontSize = 12.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun PostCardItem(
    post: Post,
    username: String,
    displayName: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isLiked by remember { mutableStateOf(false) }
    var likesCount by remember { mutableStateOf(post.likes) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            // Tiny user avatar post head
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_profile_avatar_1782204639267),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Post content metadata column
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = username,
                        color = White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = post.timeText,
                            color = MutedGrey,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Options",
                            tint = MutedGrey,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Post Content text
                Text(
                    text = post.content,
                    color = White,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Lower post icons bar (like, comment, repost, share)
                Row(
                    modifier = Modifier.width(160.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Like button
                    IconButton(
                        onClick = {
                            isLiked = !isLiked
                            likesCount = if (isLiked) likesCount + 1 else likesCount - 1
                        },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Like",
                            tint = if (isLiked) Color.Red else MutedGrey,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Comment placeholder button
                    Icon(
                        imageVector = Icons.Default.Home, // Replace with stylized comments outline
                        contentDescription = "Comment",
                        tint = MutedGrey,
                        modifier = Modifier.size(18.dp)
                    )

                    // Repost placeholder trigger
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Repost",
                        tint = MutedGrey,
                        modifier = Modifier.size(18.dp)
                    )

                    // Send placeholder action
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Send",
                        tint = MutedGrey,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Post stats row
                Text(
                    text = "$likesCount likes  ·  ${(0..8).random()} replies",
                    color = MutedGrey,
                    fontSize = 12.sp
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(0.5.dp)
                .background(BorderDark)
        )
    }
}

// --- Dynamic Canvas and Drawing Items ---

@Composable
fun ThreadsCurlIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val s = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        // Stylized Threads Curl loop
        drawArc(
            color = White,
            startAngle = 45f,
            sweepAngle = 270f,
            useCenter = false,
            style = s
        )
        drawArc(
            color = White,
            startAngle = -90f,
            sweepAngle = 220f,
            useCenter = false,
            style = s
        )
        // Central hook swirl
        drawCircle(
            color = White,
            radius = w * 0.15f,
            center = Offset(w * 0.5f, h * 0.55f)
        )
    }
}

@Composable
fun GridTabIcon(isHighlighted: Boolean, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val color = if (isHighlighted) White else MutedGrey
        val s = Stroke(width = 1.5.dp.toPx())
        // Draw 3x3 simple Instagram-like grid indicator
        val sizeCell = w / 3.4f
        val radius = 1.dp.toPx()

        for (row in 0..2) {
            for (col in 0..2) {
                val x = col * (sizeCell + 2.dp.toPx())
                val y = row * (sizeCell + 2.dp.toPx())
                drawRoundRect(
                    color = color,
                    topLeft = Offset(x, y),
                    size = androidx.compose.ui.geometry.Size(sizeCell, sizeCell),
                    style = s,
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(radius, radius)
                )
            }
        }
    }
}

@Composable
fun ReelsTabIcon(isHighlighted: Boolean, modifier: Modifier = Modifier) {
    // Stylized Play symbol in rounded container representing Reels tab
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val color = if (isHighlighted) White else MutedGrey
        val s = Stroke(width = 1.5.dp.toPx())

        // Border rectangle
        drawRoundRect(
            color = color,
            style = s,
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx(), 4.dp.toPx())
        )

        // Small play triangle in center
        val path = androidx.compose.ui.graphics.Path().apply {
            moveTo(w * 0.42f, h * 0.35f)
            lineTo(w * 0.65f, h * 0.5f)
            lineTo(w * 0.42f, h * 0.65f)
            close()
        }
        drawPath(path = path, color = color)
    }
}

@Composable
fun RepostTabIcon(isHighlighted: Boolean, modifier: Modifier = Modifier) {
    // Stylized dual curved arrows loop for Repost/Retransmit tab
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val color = if (isHighlighted) White else MutedGrey
        val s = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round)

        // Draw upper arrow curve
        drawArc(
            color = color,
            startAngle = 180f,
            sweepAngle = 100f,
            useCenter = false,
            style = s
        )
        // Draw lower arrow curve
        drawArc(
            color = color,
            startAngle = 0f,
            sweepAngle = 100f,
            useCenter = false,
            style = s
        )
    }
}

@Composable
fun TaggedTabIcon(isHighlighted: Boolean, modifier: Modifier = Modifier) {
    // Stylized profile outline inside rectangle indicating tagged people tab
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val color = if (isHighlighted) White else MutedGrey
        val s = Stroke(width = 1.5.dp.toPx())

        drawCircle(
            color = color,
            radius = w * 0.18f,
            center = Offset(w * 0.5f, h * 0.4f),
            style = s
        )
        drawArc(
            color = color,
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(w * 0.25f, h * 0.55f),
            size = androidx.compose.ui.geometry.Size(w * 0.5f, h * 0.4f),
            style = s
        )
    }
}

// --- Custom Bottom Navigation Bar ---

@Composable
fun ThreadsBottomBar(
    activeTab: String,
    avatarResId: Int,
    onComposeClick: () -> Unit,
    onNavClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Black)
            .windowInsetsPadding(WindowInsets.navigationBars) // Ensure safe areas for bottom gesture bars!
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(0.5.dp)
                    .background(BorderDark)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                // Nav 1: Home feeds
                IconButton(onClick = { onNavClick("الرئيسية") }) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = "HomeFeed",
                        tint = if (activeTab == "الرئيسية") White else MutedGrey,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Nav 2: Search discovery
                IconButton(onClick = { onNavClick("البحث") }) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "SearchUsers",
                        tint = if (activeTab == "البحث") White else MutedGrey,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Nav 3: Compose text post
                IconButton(
                    onClick = onComposeClick,
                    modifier = Modifier.testTag("nav_compose_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "WritePost",
                        tint = MutedGrey,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Nav 4: Reels video
                IconButton(onClick = { onNavClick("الفيديوهات") }) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "ReelsVideos",
                        tint = if (activeTab == "الفيديوهات") White else MutedGrey,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Nav 5: Profile (Active)
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .border(1.5.dp, if (activeTab == "الملف الشخصي") White else Color.Transparent, CircleShape) // Highlighted profile status
                        .clickable { onNavClick("الملف الشخصي") }
                ) {
                    Image(
                        painter = painterResource(id = avatarResId),
                        contentDescription = "ProfileActive",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

// --- Interactive Full Custom Dialog Screens ---

@Composable
fun EditProfileDialog(
    profile: UserProfile,
    onDismiss: () -> Unit,
    onSave: (String, String, String) -> Unit
) {
    var username by remember { mutableStateOf(profile.username) }
    var displayName by remember { mutableStateOf(profile.displayName) }
    var bioBubble by remember { mutableStateOf(profile.bioBubble) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkCard),
            border = BorderStroke(1.dp, BorderDark),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Edit Profile / تعديل الحساب",
                    color = White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Display Name Text Field
                Text(
                    text = "Display Name",
                    color = MutedGrey,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                OutlinedTextField(
                    value = displayName,
                    onValueChange = { displayName = it },
                    textStyle = TextStyle(color = White),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = FollowBlue,
                        unfocusedBorderColor = BorderDark
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .testTag("input_display_name")
                )

                // Username Text Field
                Text(
                    text = "Username (@)",
                    color = MutedGrey,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    textStyle = TextStyle(color = White),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = FollowBlue,
                        unfocusedBorderColor = BorderDark
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .testTag("input_username")
                )

                // Bio Status Bubble Text Field
                Text(
                    text = "Status Bubble",
                    color = MutedGrey,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                OutlinedTextField(
                    value = bioBubble,
                    onValueChange = { bioBubble = it },
                    textStyle = TextStyle(color = White),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = FollowBlue,
                        unfocusedBorderColor = BorderDark
                    ),
                    maxLines = 1,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp)
                        .testTag("input_bio_bubble")
                )

                // Save or Cancel Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = MutedGrey)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onSave(username, displayName, bioBubble) },
                        colors = ButtonDefaults.buttonColors(containerColor = FollowBlue)
                    ) {
                        Text("Save Changes", color = White)
                    }
                }
            }
        }
    }
}

@Composable
fun CreatePostDialog(
    onDismiss: () -> Unit,
    onPostPublished: (String) -> Unit
) {
    var content by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkCard),
            border = BorderStroke(1.dp, BorderDark),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "New Post / مشاركة فكرتك",
                    color = White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    placeholder = { Text("What is on your mind? ...", color = MutedGrey) },
                    textStyle = TextStyle(color = White),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = FollowBlue,
                        unfocusedBorderColor = BorderDark
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .padding(bottom = 16.dp)
                        .testTag("post_input_field")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = MutedGrey)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onPostPublished(content) },
                        colors = ButtonDefaults.buttonColors(containerColor = FollowBlue),
                        enabled = content.isNotBlank(),
                        modifier = Modifier.testTag("dialog_submit_post_button")
                    ) {
                        Text("Publish", color = White)
                    }
                }
            }
        }
    }
}

@Composable
fun EditBubbleDialog(
    currentBubble: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var bubbleText by remember { mutableStateOf(currentBubble) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkCard),
            border = BorderStroke(1.dp, BorderDark),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "What friends ask you... 💭",
                    color = White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = "تعديل الفقاعة النصية المعلقة فوق صورتك الشخصية",
                    color = MutedGrey,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                OutlinedTextField(
                    value = bubbleText,
                    onValueChange = { bubbleText = it },
                    textStyle = TextStyle(color = White),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = FollowBlue,
                        unfocusedBorderColor = BorderDark
                    ),
                    maxLines = 1,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = MutedGrey)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onSave(bubbleText) },
                        colors = ButtonDefaults.buttonColors(containerColor = FollowBlue)
                    ) {
                        Text("Save", color = White)
                    }
                }
            }
        }
    }
}

@Composable
fun FullScreenAvatarDialog(
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable { onDismiss() },
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .aspectRatio(1f),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, BorderDark)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_profile_avatar_1782204639267),
                    contentDescription = "Avatar high res viewer",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
fun PostDetailsDialog(
    post: Post,
    username: String,
    displayName: String,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkCard),
            border = BorderStroke(1.dp, BorderDark),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_profile_avatar_1782204639267),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = displayName,
                            color = White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "@$username  ·  ${post.timeText}",
                            color = MutedGrey,
                            fontSize = 11.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = post.content,
                    color = White,
                    fontSize = 15.sp,
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "❤️ ${post.likes} LIKES",
                        color = MutedGrey,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = DarkBackground),
                        border = BorderStroke(1.dp, BorderDark)
                    ) {
                        Text("Close", color = White)
                    }
                }
            }
        }
    }
}
