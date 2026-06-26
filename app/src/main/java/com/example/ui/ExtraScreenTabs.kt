package com.example.ui

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.DiscoverPerson
import com.example.data.Post
import com.example.data.UserProfile
import com.example.ui.theme.*

@Composable
fun HomeFeedTab(
    posts: List<Post>,
    profile: UserProfile,
    onPostClick: (Post) -> Unit,
    onQuickPostTrigger: () -> Unit,
    onResetTrigger: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // Upper Home Sticky Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .statusBarsPadding(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onResetTrigger) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Reset database",
                    tint = MutedGrey
                )
            }

            // Beautiful styled threads centered logo
            ThreadsCurlIcon(modifier = Modifier.size(30.dp))

            // Transparent spacer for layout balancing
            Spacer(modifier = Modifier.size(48.dp))
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // "What's new?" quick compose row
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onQuickPostTrigger() }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
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

                    Text(
                        text = "ما الجديد في ذهنك يا ${profile.displayName}؟...",
                        color = MutedGrey,
                        fontSize = 14.sp,
                        modifier = Modifier.weight(1f)
                    )

                    Button(
                        onClick = onQuickPostTrigger,
                        colors = ButtonDefaults.buttonColors(containerColor = DarkCard),
                        shape = RoundedCornerShape(18.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text("نشر", color = White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(0.5.dp)
                        .background(BorderDark)
                )
            }

            if (posts.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 120.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Create,
                            contentDescription = null,
                            tint = MutedGrey.copy(alpha = 0.5f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "لا توجد منشورات حالياً",
                            color = White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "كن أول من يكتب وينشر فكرة جديدة على المنصة!",
                            color = MutedGrey,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 32.dp)
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = onQuickPostTrigger,
                            colors = ButtonDefaults.buttonColors(containerColor = FollowBlue),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("أضف منشوراً الآن ✍️", color = White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                items(posts) { post ->
                    PostCardItem(
                        post = post,
                        username = profile.username,
                        displayName = profile.displayName,
                        onClick = { onPostClick(post) }
                    )
                }
            }
        }
    }
}

@Composable
fun SearchDiscoveryTab(
    suggestions: List<DiscoverPerson>,
    onFollowToggle: (Int, Boolean) -> Unit,
    onDismissSuggestion: (Int) -> Unit,
    onSeeAllClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("الكل") }
    val categories = listOf("الكل", "تقنية", "رياضة", "تصميم", "ترفيه")

    // Filter suggestions locally based on search query
    val filteredSuggestions = remember(suggestions, searchQuery) {
        if (searchQuery.isBlank()) {
            suggestions
        } else {
            suggestions.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                        it.subtitle.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .statusBarsPadding()
    ) {
        // Sticky Header Title
        Text(
            text = "استكشاف الأصدقاء",
            color = White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        )

        // Custom Sleek Search Input Field
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("ابحث عن الحسابات والمواضيع...", color = MutedGrey, fontSize = 14.sp) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = MutedGrey
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear", tint = White)
                    }
                }
            },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = FollowBlue,
                unfocusedBorderColor = BorderDark,
                focusedTextColor = White,
                unfocusedTextColor = White,
                focusedContainerColor = DarkCard,
                unfocusedContainerColor = DarkCard
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )

        // Horizontal Category Pill Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categories.forEach { cat ->
                val isActive = selectedCategory == cat
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isActive) White else DarkCard)
                        .border(
                            width = 1.dp,
                            color = if (isActive) Color.Transparent else BorderDark,
                            shape = RoundedCornerShape(20.dp)
                        )
                        .clickable { selectedCategory = cat }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = cat,
                        color = if (isActive) Black else White,
                        fontSize = 13.sp,
                        fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, top = 0.dp, end = 16.dp, bottom = 80.dp)
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "اقتراحات قد تعجبك",
                        color = White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "عرض الكل",
                        color = FollowBlue,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { onSeeAllClick() }
                    )
                }
            }

            if (filteredSuggestions.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 60.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = MutedGrey.copy(alpha = 0.4f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "لا توجد نتائج مطابقة",
                            color = MutedGrey,
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
                items(filteredSuggestions) { person ->
                    SuggestionCardItem(
                        person = person,
                        onFollowToggle = { followed -> onFollowToggle(person.id, followed) },
                        onDismiss = { onDismissSuggestion(person.id) }
                    )
                }
            }

            // A list of trending hashtags inside Search
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "المواضيع المتداولة حالياً",
                    color = White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }

            item {
                TrendingTopicRow(topic = "#ثريدز_العرب", postsCount = "١٢٫٤ ألف منشور")
                TrendingTopicRow(topic = "#مطورين_أندرويد", postsCount = "٨٫٩ ألف منشور")
                TrendingTopicRow(topic = "#كوتلن_جيت_باك", postsCount = "٥٫٢ ألف منشور")
            }
        }
    }
}

@Composable
fun SuggestionCardItem(
    person: DiscoverPerson,
    onFollowToggle: (Boolean) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(0.5.dp, BorderDark)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Seeded Suggestion Avatars or Placeholders
            val avatarId = when (person.id) {
                1 -> R.drawable.img_suggest_el_ikhwa_1782204654225
                2 -> R.drawable.img_suggest_ali_1782204668473
                3 -> R.drawable.img_suggest_ilyes_1782204681125
                else -> R.drawable.img_profile_avatar_1782204639267
            }

            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
            ) {
                Image(
                    painter = painterResource(id = avatarId),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = person.name,
                        color = White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Verified",
                        tint = FollowBlue,
                        modifier = Modifier.size(14.dp)
                    )
                }
                Text(
                    text = person.subtitle,
                    color = MutedGrey,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Action Buttons
            Button(
                onClick = {
                    onFollowToggle(!person.isFollowed)
                    val actMsg = if (person.isFollowed) "تم إلغاء متابعة الحساب" else "تمت المتابعة بنجاح! 🎉"
                    Toast.makeText(context, actMsg, Toast.LENGTH_SHORT).show()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (person.isFollowed) DarkBackground else White,
                    contentColor = if (person.isFollowed) White else Black
                ),
                shape = RoundedCornerShape(12.dp),
                border = if (person.isFollowed) BorderStroke(1.dp, BorderDark) else null,
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                modifier = Modifier.height(34.dp)
            ) {
                Text(
                    text = if (person.isFollowed) "متابَع" else "متابعة",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(30.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Dismiss",
                    tint = MutedGrey,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun TrendingTopicRow(topic: String, postsCount: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(text = topic, color = White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text(text = postsCount, color = MutedGrey, fontSize = 12.sp)
        }
        Icon(
            imageVector = Icons.Default.KeyboardArrowRight,
            contentDescription = null,
            tint = MutedGrey
        )
    }
}

@Composable
fun ReelsSimulationTab(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val reelsData = listOf(
        ReelData(
            id = 1,
            creatorName = "ali_timazghin",
            caption = "فخور جداً بالانتهاء من تصميم واجهات التطبيق الرائعة اليوم! ممتن لدعمكم اللامحدود يا أصدقاء 💻🔥✨",
            likes = 452,
            comments = 18,
            gradientColors = listOf(Color(0xFF3F51B5), Color(0xFF00BCD4)) // Cyan Space Gradient
        ),
        ReelData(
            id = 2,
            creatorName = "el_ikhwa_store",
            caption = "وصول المنتجات الجديدة المتميزة للأسبوع الجاري! تصفحوها الآن و احصلوا على خصومات حصرية لـ ثريدز 🛍️✨",
            likes = 1282,
            comments = 97,
            gradientColors = listOf(Color(0xFFE91E63), Color(0xFFFF9800)) // Sunset Magenta Orange
        ),
        ReelData(
            id = 3,
            creatorName = "ilyes_coder",
            caption = "تحدي الـ ٣٠ ثانية: كيف تبني قاعدة بيانات Room محلية بأمان وتتحكم بالمزامنة الكاملة؟ 🤔☕✨",
            likes = 312,
            comments = 41,
            gradientColors = listOf(Color(0xFF4CAF50), Color(0xFF009688)) // Emerald Mint Glow
        )
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Black)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            items(reelsData) { reel ->
                ReelItemSimulated(reel = reel)
            }
        }

        // Overlay Title on top left
        Text(
            text = "فيديوهات ثريدز",
            color = White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .padding(16.dp)
        )
    }
}

data class ReelData(
    val id: Int,
    val creatorName: String,
    val caption: String,
    val likes: Int,
    val comments: Int,
    val gradientColors: List<Color>
)

@Composable
fun ReelItemSimulated(
    reel: ReelData,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isLiked by remember { mutableStateOf(false) }
    var likesCount by remember { mutableStateOf(reel.likes) }

    // Dynamic rotation for vinyl disc continuous simulation
    val infiniteTransition = rememberInfiniteTransition(label = "music_disk")
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    // Playback video progress simulation
    val videoPlayProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "progress"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(580.dp)
            .padding(vertical = 8.dp, horizontal = 12.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Brush.linearGradient(reel.gradientColors))
    ) {
        // Centered translucent neon Play button icon
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.25f))
                    .clickable {
                        Toast.makeText(context, "فيديو المنشئ ${reel.creatorName} قيد العرض التفاعلي 🎥", Toast.LENGTH_SHORT).show()
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Playing",
                    tint = White,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Beautiful Loop Indicator
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                LinearProgressIndicator(
                    progress = { videoPlayProgress },
                    color = White,
                    trackColor = White.copy(alpha = 0.3f),
                    strokeCap = StrokeCap.Round,
                    modifier = Modifier
                        .width(120.dp)
                        .height(3.dp)
                        .clip(RoundedCornerShape(2.dp))
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("تفاعلي", color = White.copy(alpha = 0.8f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Side Interaction Panel layout on the right side
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 24.dp, end = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Like Button
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(
                    onClick = {
                        isLiked = !isLiked
                        likesCount = if (isLiked) likesCount + 1 else likesCount - 1
                    },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.4f))
                        .size(44.dp)
                ) {
                    Icon(
                        imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Like video",
                        tint = if (isLiked) Color.Red else White
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = likesCount.toString(),
                    color = White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Comment Button
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(
                    onClick = {
                        Toast.makeText(context, "التعليقات والمشاركة ستتوفر قريباً", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.4f))
                        .size(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = "Comment video",
                        tint = White
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = reel.comments.toString(),
                    color = White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Share Button
            IconButton(
                onClick = {
                    Toast.makeText(context, "تم مشاركة رابط الفيديو التفاعلي بنجاح", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.4f))
                    .size(44.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Share video",
                    tint = White
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Rotating Vinyl Disc
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Black)
                    .border(2.dp, White.copy(alpha = 0.6f), CircleShape)
                    .rotate(rotationAngle),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Rotating disc decoration",
                    tint = FollowBlue,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Bottom Creators Info layout layered on the left bottom
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth(0.75f)
                .padding(start = 16.dp, bottom = 24.dp, end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .border(1.dp, White, CircleShape)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_profile_avatar_1782204639267),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = "@${reel.creatorName}",
                    color = White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.width(8.dp))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(White.copy(alpha = 0.25f))
                        .clickable {
                            Toast.makeText(context, "تم متابعة الحساب ${reel.creatorName}", Toast.LENGTH_SHORT).show()
                        }
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text("متابعة", color = White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            Text(
                text = reel.caption,
                color = White,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // Music label
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Song",
                    tint = White.copy(alpha = 0.7f),
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "الصوت الأصلي - @${reel.creatorName}",
                    color = White.copy(alpha = 0.7f),
                    fontSize = 11.sp
                )
            }
        }
    }
}
