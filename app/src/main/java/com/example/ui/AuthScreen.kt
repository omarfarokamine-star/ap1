package com.example.ui

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun HybridAuthScreen(
    viewModel: ProfileViewModel,
    onAuthSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isLoginMode by remember { mutableStateOf(true) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        AnimatedContent(
            targetState = isLoginMode,
            transitionSpec = {
                if (targetState) {
                    (slideInHorizontally { width -> -width } + fadeIn()) togetherWith
                            (slideOutHorizontally { width -> width } + fadeOut())
                } else {
                    (slideInHorizontally { width -> width } + fadeIn()) togetherWith
                            (slideOutHorizontally { width -> -width } + fadeOut())
                }
            },
            label = "AuthModeTransition"
        ) { activeLoginMode ->
            if (activeLoginMode) {
                LoginView(
                    viewModel = viewModel,
                    onLoginSuccess = onAuthSuccess,
                    onNavigateToRegister = { isLoginMode = false }
                )
            } else {
                RegisterView(
                    viewModel = viewModel,
                    onRegisterSuccess = onAuthSuccess,
                    onNavigateToLogin = { isLoginMode = true }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginView(
    viewModel: ProfileViewModel,
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    val context = LocalContext.current
    var phoneNumber by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.height(30.dp))

        // Multi-Brand Geometric logo combining F, Camera & X glyphs
        HybridBrandHeroLogo(modifier = Modifier.size(90.dp))

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "تسجيل الدخول",
            color = White,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.testTag("login_title_text")
        )

        Text(
            text = "مرحباً بك! سجل الدخول بواسطة رقم هاتفك للاستمرار",
            color = MutedGrey,
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 6.dp, bottom = 32.dp)
        )

        // Phone Input
        Text(
            text = "رقم الهاتف والاتصال",
            color = White,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
            textAlign = TextAlign.Right
        )
        OutlinedTextField(
            value = phoneNumber,
            onValueChange = { phoneNumber = it },
            placeholder = { Text("مثال: 0550000000", color = Color.Gray, textAlign = TextAlign.Right) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Phone,
                    contentDescription = null,
                    tint = MutedGrey
                )
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
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
                .padding(bottom = 16.dp)
                .testTag("login_phone_input")
        )

        // Password Input
        Text(
            text = "كلمة المرور الكود",
            color = White,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
            textAlign = TextAlign.Right
        )
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            placeholder = { Text("أدخل كلمة المرور السرية", color = Color.Gray, textAlign = TextAlign.Right) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = MutedGrey
                )
            },
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.Check else Icons.Default.Lock,
                        contentDescription = "Toggle password visibility",
                        tint = MutedGrey
                    )
                }
            },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
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
                .padding(bottom = 24.dp)
                .testTag("login_password_input")
        )

        // Submit Button with Hybrid Instagram-like Gradient
        Button(
            onClick = {
                if (phoneNumber.isBlank() || password.isBlank()) {
                    Toast.makeText(context, "يرجى ملء جميع الحقول المطلوبة", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                isLoading = true
                viewModel.loginUser(phoneNumber, password) { isSuccess, msg ->
                    isLoading = false
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    if (isSuccess) {
                        onLoginSuccess()
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("login_submit_button"),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            contentPadding = PaddingValues(0.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                FollowBlue,
                                Color(0xFFC13584), // Instagram Pink
                                Color(0xFFFF7A00)  // Vivid Orange
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = White, modifier = Modifier.size(24.dp))
                } else {
                    Text(
                        text = "تسجيل الدخول",
                        color = White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Redirect links
        Row(
            modifier = Modifier.clickable { onNavigateToRegister() },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "سجل حساباً جديداً الآن",
                color = FollowBlue,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.testTag("to_register_button")
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "ليس لديك حساب؟",
                color = MutedGrey,
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.height(50.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterView(
    viewModel: ProfileViewModel,
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val context = LocalContext.current
    var fullName by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        // Geometric logo
        HybridBrandHeroLogo(modifier = Modifier.size(90.dp))

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "إنشاء حساب جديد",
            color = White,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.testTag("register_title_text")
        )

        Text(
            text = "قم بالتسجيل برقم هاتفك دون الحاجة لـ OTP مباشرة للمشروع",
            color = MutedGrey,
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 6.dp, bottom = 28.dp)
        )

        // Full Name Input
        Text(
            text = "الاسم الكامل (سيظهر في حسابك)",
            color = White,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
            textAlign = TextAlign.Right
        )
        OutlinedTextField(
            value = fullName,
            onValueChange = { fullName = it },
            placeholder = { Text("مثال: فاروق عمر", color = Color.Gray, textAlign = TextAlign.Right) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = MutedGrey
                )
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
                .padding(bottom = 16.dp)
                .testTag("register_name_input")
        )

        // Phone Input
        Text(
            text = "رقم الهاتف",
            color = White,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
            textAlign = TextAlign.Right
        )
        OutlinedTextField(
            value = phoneNumber,
            onValueChange = { phoneNumber = it },
            placeholder = { Text("مثال: 0550000000", color = Color.Gray, textAlign = TextAlign.Right) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Phone,
                    contentDescription = null,
                    tint = MutedGrey
                )
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
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
                .padding(bottom = 16.dp)
                .testTag("register_phone_input")
        )

        // Password Input
        Text(
            text = "كلمة المرور",
            color = White,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
            textAlign = TextAlign.Right
        )
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            placeholder = { Text("أدخل كلمة المرور", color = Color.Gray, textAlign = TextAlign.Right) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = MutedGrey
                )
            },
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.Check else Icons.Default.Lock,
                        contentDescription = "Toggle password visibility",
                        tint = MutedGrey
                    )
                }
            },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
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
                .padding(bottom = 24.dp)
                .testTag("register_password_input")
        )

        // Submit Register Button with Instagram/Facebook/X vibrant color mix gradient
        Button(
            onClick = {
                if (fullName.isBlank() || phoneNumber.isBlank() || password.isBlank()) {
                    Toast.makeText(context, "الرجاء تعبئة كافة الحلقات الشاغرة", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                isLoading = true
                viewModel.registerUser(fullName, phoneNumber, password)
                isLoading = false
                Toast.makeText(context, "تم إنشاء الحساب والولوج بنجاح! 🎉", Toast.LENGTH_SHORT).show()
                onRegisterSuccess()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("register_submit_button"),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            contentPadding = PaddingValues(0.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF1877F2), // Facebook Blue
                                Color(0xFFE1306C), // Instagram Red/Pink
                                Color(0xFF1DA1F2)  // Twitter/X Sky
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = White, modifier = Modifier.size(24.dp))
                } else {
                    Text(
                        text = "إنشاء وتفعيل الحساب",
                        color = White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Redirect back to Login
        Row(
            modifier = Modifier.clickable { onNavigateToLogin() },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "سجل دخولك الآن",
                color = FollowBlue,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.testTag("to_login_button")
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "لديك حساب بالفعل؟",
                color = MutedGrey,
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.height(50.dp))
    }
}

// --- Custom Canvas Hybrid Design Logo ---

@Composable
fun HybridBrandHeroLogo(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val mainStroke = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)

        // 1. Draw outer sleek camera square with rounded corner (Instagram influence)
        drawRoundRect(
            brush = Brush.linearGradient(
                colors = listOf(
                    Color(0xFF1877F2), // Facebook
                    Color(0xFFC13584), // Instagram
                    Color(0xFFFF7A00)  // Sunset
                )
            ),
            style = mainStroke,
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(16.dp.toPx(), 16.dp.toPx())
        )

        // 2. Draw modern high-contrast cross diagonal "X" indicator inside (X influence)
        // Upper left to lower right
        drawLine(
            color = White,
            start = Offset(w * 0.25f, h * 0.25f),
            end = Offset(w * 0.75f, h * 0.75f),
            strokeWidth = 3.dp.toPx(),
            cap = StrokeCap.Round
        )
        // Lower left to upper right (thicker/hollowed block style typical of X logo)
        drawLine(
            color = White.copy(alpha = 0.5f),
            start = Offset(w * 0.75f, h * 0.25f),
            end = Offset(w * 0.25f, h * 0.75f),
            strokeWidth = 1.5.dp.toPx(),
            cap = StrokeCap.Round
        )

        // 3. Central glowing circular dot symbolizing camera lens node
        drawCircle(
            color = Color(0xFF1877F2), // Facebook brand blue glow
            radius = w * 0.14f,
            center = Offset(w * 0.5f, h * 0.5f)
        )

        drawCircle(
            color = White,
            radius = w * 0.07f,
            center = Offset(w * 0.5f, h * 0.5f)
        )
    }
}
