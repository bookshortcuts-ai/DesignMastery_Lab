package com.example.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.horizontalScroll
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.*
import com.example.viewmodel.TeamHubViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AppNavigation(
    viewModel: TeamHubViewModel,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val viewedProtoUser by viewModel.viewedUserProfile.collectAsState()
    val currentScreenPath = remember { mutableStateFlowOf("auth") }
    
    LaunchedEffect(currentUser) {
        if (currentUser != null) {
            currentScreenPath.value = "home"
        } else {
            currentScreenPath.value = "auth"
        }
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val isWideScreen = maxWidth > 720.dp
        
        if (currentScreenPath.value == "auth") {
            LoginScreen(viewModel = viewModel)
        } else {
            val user = currentUser ?: return@BoxWithConstraints
            
            if (isWideScreen) {
                // Expanded screen layout with left Navigation Rail
                Row(modifier = Modifier.fillMaxSize()) {
                    PermanentNavigationRail(
                        currentRoute = currentScreenPath.value,
                        onNavigate = { currentScreenPath.value = it },
                        currentUser = user,
                        onLogout = { viewModel.logout() }
                    )
                    
                    VerticalDivider(color = MaterialTheme.colorScheme.outline)
                    
                    Box(modifier = Modifier.weight(1f)) {
                        MainScreenContent(
                            route = currentScreenPath.value,
                            viewModel = viewModel,
                            onNavigate = { currentScreenPath.value = it }
                        )
                    }
                }
            } else {
                // Mobile screen layout with bottom Navigation Bar
                Scaffold(
                    bottomBar = {
                        BottomNavBar(
                            currentRoute = currentScreenPath.value,
                            onNavigate = { currentScreenPath.value = it },
                            currentUser = user
                        )
                    }
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        MainScreenContent(
                            route = currentScreenPath.value,
                            viewModel = viewModel,
                            onNavigate = { currentScreenPath.value = it }
                        )
                    }
                }
            }
        }

        // Global Overlay Dialog for Viewing Public Portfolios and Badges
        viewedProtoUser?.let { selectedUser ->
            PublicProfileOverlayDialog(
                user = selectedUser,
                viewModel = viewModel,
                onClose = { viewModel.closeProfile() }
            )
        }
    }
}

// Wrapper for responsive navigation state support
fun <T> mutableStateFlowOf(value: T): MutableState<T> = mutableStateOf(value)

@Composable
fun PermanentNavigationRail(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    currentUser: User,
    onLogout: () -> Unit
) {
    NavigationRail(
        containerColor = MaterialTheme.colorScheme.surface,
        header = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(vertical = 12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.tertiary
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "DML",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "TEAM HUB",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp
                )
            }
        },
        modifier = Modifier.fillMaxHeight()
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            NavigationRailItem(
                selected = currentRoute == "home",
                onClick = { onNavigate("home") },
                icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
                label = { Text("Hub Home", fontSize = 11.sp) }
            )
            NavigationRailItem(
                selected = currentRoute == "dashboard",
                onClick = { onNavigate("dashboard") },
                icon = { Icon(Icons.Filled.Dashboard, contentDescription = "Dashboard") },
                label = { Text("Ops Desk", fontSize = 11.sp) }
            )
            NavigationRailItem(
                selected = currentRoute == "projects",
                onClick = { onNavigate("projects") },
                icon = { Icon(Icons.Filled.FolderOpen, contentDescription = "Projects") },
                label = { Text("Projects", fontSize = 11.sp) }
            )
            NavigationRailItem(
                selected = currentRoute == "directory",
                onClick = { onNavigate("directory") },
                icon = { Icon(Icons.Filled.PeopleAlt, contentDescription = "Directory") },
                label = { Text("Directory", fontSize = 11.sp) }
            )
            NavigationRailItem(
                selected = currentRoute == "leaderboard",
                onClick = { onNavigate("leaderboard") },
                icon = { Icon(Icons.Filled.EmojiEvents, contentDescription = "Leaderboard") },
                label = { Text("Ranks", fontSize = 11.sp) }
            )
            NavigationRailItem(
                selected = currentRoute == "team_area",
                onClick = { onNavigate("team_area") },
                icon = { Icon(Icons.Filled.Forum, contentDescription = "Team Area") },
                label = { Text("Team Talk", fontSize = 11.sp) }
            )
            NavigationRailItem(
                selected = currentRoute == "profile",
                onClick = { onNavigate("profile") },
                icon = { Icon(Icons.Filled.ContactPage, contentDescription = "My Profile") },
                label = { Text("My Card", fontSize = 11.sp) }
            )
        }

        NavigationRailItem(
            selected = false,
            onClick = onLogout,
            icon = { Icon(Icons.Filled.ExitToApp, contentDescription = "Logout", tint = Color.Red) },
            label = { Text("Exit", fontSize = 11.sp, color = Color.Red) }
        )
    }
}

@Composable
fun BottomNavBar(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    currentUser: User
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        NavigationBarItem(
            selected = currentRoute == "home",
            onClick = { onNavigate("home") },
            icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
            label = { Text("Home", overflow = TextOverflow.Ellipsis, maxLines = 1) }
        )
        NavigationBarItem(
            selected = currentRoute == "dashboard",
            onClick = { onNavigate("dashboard") },
            icon = { Icon(Icons.Filled.Dashboard, contentDescription = "Dashboard") },
            label = { Text("Ops", overflow = TextOverflow.Ellipsis, maxLines = 1) }
        )
        NavigationBarItem(
            selected = currentRoute == "projects",
            onClick = { onNavigate("projects") },
            icon = { Icon(Icons.Filled.FolderOpen, contentDescription = "Projects") },
            label = { Text("Briefs", overflow = TextOverflow.Ellipsis, maxLines = 1) }
        )
        NavigationBarItem(
            selected = currentRoute == "directory",
            onClick = { onNavigate("directory") },
            icon = { Icon(Icons.Filled.PeopleAlt, contentDescription = "Directory") },
            label = { Text("Team", overflow = TextOverflow.Ellipsis, maxLines = 1) }
        )
        NavigationBarItem(
            selected = currentRoute == "leaderboard",
            onClick = { onNavigate("leaderboard") },
            icon = { Icon(Icons.Filled.EmojiEvents, contentDescription = "Leaderboard") },
            label = { Text("Ranks", overflow = TextOverflow.Ellipsis, maxLines = 1) }
        )
        NavigationBarItem(
            selected = currentRoute == "team_area",
            onClick = { onNavigate("team_area") },
            icon = { Icon(Icons.Filled.Forum, contentDescription = "Team Area") },
            label = { Text("Talk", overflow = TextOverflow.Ellipsis, maxLines = 1) }
        )
    }
}

@Composable
fun MainScreenContent(
    route: String,
    viewModel: TeamHubViewModel,
    onNavigate: (String) -> Unit
) {
    AnimatedContent(
        targetState = route,
        transitionSpec = {
            fadeIn(animationSpec = spring()) togetherWith fadeOut(animationSpec = spring())
        },
        label = "screen_transition"
    ) { screen ->
        when (screen) {
            "home" -> HomeScreen(viewModel = viewModel, onNavigate = onNavigate)
            "dashboard" -> DashboardOperationalScreen(viewModel = viewModel, onNavigate = onNavigate)
            "projects" -> ProjectsScreen(viewModel = viewModel)
            "directory" -> DirectoryScreen(viewModel = viewModel)
            "leaderboard" -> LeaderboardScreen(viewModel = viewModel)
            "team_area" -> TeamAreaScreen(viewModel = viewModel)
            "profile" -> ProfileScreen(viewModel = viewModel)
            else -> HomeScreen(viewModel = viewModel, onNavigate = onNavigate)
        }
    }
}

// -------------------------------------------------------------
// HELPER: Avatar system based on Initial / ID for pixel perfect UI
@Composable
fun AvatarWidget(
    avatarId: Int,
    fullName: String,
    modifier: Modifier = Modifier,
    size: Int = 54
) {
    // 5 agency-style light pink and aubergine palette indices
    val colors = listOf(
        Color(0xFFE91E63), // Pink
        Color(0xFF9C27B0), // Purple
        Color(0xFF3F51B5), // Indigo
        Color(0xFF009688), // Teal
        Color(0xFFEC407A)  // Light coral pink
    )
    
    val bg = colors[avatarId % colors.size]
    val initials = if (fullName.trim().isNotEmpty()) {
        val parts = fullName.trim().split(" ")
        if (parts.size > 1) {
            "${parts[0].take(1).uppercase()}${parts[1].take(1).uppercase()}"
        } else {
            fullName.take(2).uppercase()
        }
    } else {
        "DM"
    }

    Box(
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(Brush.radialGradient(listOf(bg, bg.copy(alpha = 0.8f)))),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initials,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = (size * 0.35).sp,
            letterSpacing = 0.5.sp
        )
    }
}

// -------------------------------------------------------------
// DYNAMIC COMPRESSED FLOW BADGES, PORTFOLIOS, AND ANALYTICS CHANNELS
// -------------------------------------------------------------

// ADVANCED CLOCKING & REUSABLE CLICKABLE AVATAR EMBED
@Composable
fun ClickableAvatar(
    avatarId: Int,
    fullName: String,
    modifier: Modifier = Modifier,
    size: Int = 54,
    onClick: (() -> Unit)? = null
) {
    val colors = listOf(
        Color(0xFFE91E63), // Pink
        Color(0xFF9C27B0), // Purple
        Color(0xFF3F51B5), // Indigo
        Color(0xFF009688), // Teal
        Color(0xFFEC407A), // Coral Pink
        Color(0xFFFF9800)  // Gold Amber
    )
    val bg = colors[avatarId % colors.size]
    val initials = if (fullName.trim().isNotEmpty()) {
        val parts = fullName.trim().split(" ")
        if (parts.size > 1) {
            "${parts[0].take(1).uppercase()}${parts[1].take(1).uppercase()}"
        } else {
            fullName.take(2).uppercase()
        }
    } else {
        "DM"
    }

    val clickableModifier = if (onClick != null) {
        modifier
            .size(size.dp)
            .clip(CircleShape)
            .clickable { onClick() }
            .background(Brush.radialGradient(listOf(bg, bg.copy(alpha = 0.8f))))
    } else {
        modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(Brush.radialGradient(listOf(bg, bg.copy(alpha = 0.8f))))
    }

    Box(
        modifier = clickableModifier,
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initials,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = (size * 0.35).sp,
            letterSpacing = 0.5.sp
        )
    }
}

// BADGE DISPLAY: Dynamic presentation graphics
@Composable
fun BadgeDisplay(badgeName: String, modifier: Modifier = Modifier, isLarge: Boolean = false) {
    val trimmed = badgeName.trim()
    if (trimmed.isEmpty()) return

    val rarity: String
    val colors: List<Color>
    val icon: ImageVector
    val label: String
    val desc: String

    when (trimmed) {
        "Founder Crown Badge" -> {
            rarity = "Legendary"
            colors = listOf(Color(0xFFFFD700), Color(0xFFE5E4E2)) // Gold and platinum gradient
            icon = Icons.Filled.WorkspacePremium
            label = "Founder & Super Admin"
            desc = "Premium exclusive crown badge of ultimate creative agency authority."
        }
        "Elite Admin Badge" -> {
            rarity = "Epic"
            colors = listOf(Color(0xFF1976D2), Color(0xFFCFD8DC)) // Blue & silver corporate gradient
            icon = Icons.Filled.Shield
            label = "Administrator"
            desc = "Elite administrator shield badge for operational systems."
        }
        "Thumbnail Master" -> {
            rarity = "Legendary"
            colors = listOf(Color(0xFFFF2525), Color(0xFFFF9800))
            icon = Icons.Filled.Image
            label = "Thumbnail Master"
            desc = "High engagement CTR packaging and strategic Youtube banner layouts."
        }
        "Logo Specialist" -> {
            rarity = "Epic"
            colors = listOf(Color(0xFF8E2DE2), Color(0xFF4A00E0))
            icon = Icons.Filled.Brush
            label = "Logo Specialist"
            desc = "Mastery of visual identity typography and vector geometric symbols."
        }
        "Social Media Expert" -> {
            rarity = "Epic"
            colors = listOf(Color(0xFF00C6FF), Color(0xFF0072FF))
            icon = Icons.Filled.CellTower
            label = "Social Media Expert"
            desc = "Bespoke social framework templates tailored for target digital campaigns."
        }
        "Creative Expert" -> {
            rarity = "Legendary"
            colors = listOf(Color(0xFF11998E), Color(0xFF38EF7D))
            icon = Icons.Filled.Palette
            label = "Creative Expert"
            desc = "High-fidelity brand manuals, art assets, and guidelines curation."
        }
        "Fast Delivery" -> {
            rarity = "Common"
            colors = listOf(Color(0xFF78909C), Color(0xFF37474F))
            icon = Icons.Filled.Speed
            label = "Fast Delivery"
            desc = "Phenomenal turnaround speeds respecting aggressive product schedules."
        }
        "Team Leader" -> {
            rarity = "Rare"
            colors = listOf(Color(0xFF1E3C72), Color(0xFF2A5298))
            icon = Icons.Filled.Groups
            label = "Team Leader"
            desc = "Exceptional project mentorship guiding junior designers on accuracy formats."
        }
        "Top Designer" -> {
            rarity = "Legendary"
            colors = listOf(Color(0xFFF12711), Color(0xFFF5AF19))
            icon = Icons.Filled.Star
            label = "Top Designer"
            desc = "Flawless designer execution matching all creative specifications perfectly."
        }
        "Employee of the Month" -> {
            rarity = "Epic"
            colors = listOf(Color(0xFFE100FF), Color(0xFF7F00FF))
            icon = Icons.Filled.CalendarMonth
            label = "Employee of the Month"
            desc = "High-tier coworker recognition for exceeding peer contributions."
        }
        "Rising Talent" -> {
            rarity = "Common"
            colors = listOf(Color(0xFFCFD8DC), Color(0xFF90A4AE))
            icon = Icons.Filled.TrendingUp
            label = "Rising Talent"
            desc = "Fast tracking development with pristine potential inside team boards."
        }
        "Problem Solver" -> {
            rarity = "Common"
            colors = listOf(Color(0xFF4B5563), Color(0xFF1F2937))
            icon = Icons.Filled.AutoFixHigh
            label = "Problem Solver"
            desc = "High speed revision workflows and diagnostic feedback mastery."
        }
        else -> {
            rarity = "Common"
            colors = listOf(Color(0xFF9EA7AA), Color(0xFF6B7A81))
            icon = Icons.Filled.WorkspacePremium
            label = trimmed
            desc = "Awarded contribution and design service credential."
        }
    }

    val glowColor = when(rarity) {
        "Legendary" -> colors[0].copy(alpha = 0.5f)
        "Epic" -> colors[0].copy(alpha = 0.35f)
        "Rare" -> colors[0].copy(alpha = 0.2f)
        else -> Color.Transparent
    }

    if (isLarge) {
        Card(
            modifier = modifier
                .shadow(
                    elevation = if (glowColor != Color.Transparent) 6.dp else 2.dp,
                    shape = RoundedCornerShape(16.dp),
                    ambientColor = glowColor,
                    spotColor = glowColor
                )
                .fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.2.dp, if (rarity == "Legendary") colors[0].copy(alpha = 0.8f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .background(
                            Brush.linearGradient(colors),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = label,
                    fontWeight = FontWeight.Black,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Box(
                    modifier = Modifier
                        .background(
                            when(rarity) {
                                "Legendary" -> Color(0xFFFFEB3B).copy(alpha = 0.2f)
                                "Epic" -> Color(0xFFE040FB).copy(alpha = 0.15f)
                                "Rare" -> Color(0xFF00E5FF).copy(alpha = 0.12f)
                                else -> Color.Gray.copy(alpha = 0.1f)
                            },
                            RoundedCornerShape(20.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = rarity.uppercase() + " GRADIENT",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Black,
                        color = when(rarity) {
                            "Legendary" -> Color(0xFFD4AF37)
                            "Epic" -> Color(0xFF9C27B0)
                            "Rare" -> Color(0xFF00838F)
                            else -> Color.Gray
                        }
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = desc,
                    fontSize = 11.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    lineHeight = 14.sp
                )
            }
        }
    } else {
        Box(
            modifier = modifier
                .shadow(
                    elevation = if (glowColor != Color.Transparent) 4.dp else 0.dp,
                    shape = RoundedCornerShape(8.dp),
                    ambientColor = glowColor,
                    spotColor = glowColor
                )
                .background(
                    Brush.horizontalGradient(colors),
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = Color.White,
                    modifier = Modifier.size(11.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = label,
                    fontSize = 8.5.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    letterSpacing = 0.2.sp
                )
            }
        }
    }
}

// PUBLIC PROFILE DETAIL DIALOG
@Composable
@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
fun PublicProfileOverlayDialog(
    user: User,
    viewModel: TeamHubViewModel,
    onClose: () -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val context = LocalContext.current
    var assignBadgeDropdownExpanded by remember { mutableStateOf(false) }

    val coverBrush = when(user.coverBannerColor) {
        "NeonPink" -> Brush.linearGradient(listOf(Color(0xFFF857A6), Color(0xFFFF5858)))
        "CosmicBlue" -> Brush.linearGradient(listOf(Color(0xFF00C6FF), Color(0xFF0072FF)))
        "GoldGlow" -> Brush.linearGradient(listOf(Color(0xFFF12711), Color(0xFFF5AF19)))
        "Emerald" -> Brush.linearGradient(listOf(Color(0xFF11998E), Color(0xFF38EF7D)))
        "Slate" -> Brush.linearGradient(listOf(Color(0xFF373B44), Color(0xFF4286f4)))
        else -> Brush.linearGradient(listOf(Color(0xFF4F5B66), Color(0xFF232526)))
    }

    val availableBadges = listOf(
        "Thumbnail Master", "Logo Specialist", "Social Media Expert", 
        "Creative Expert", "Fast Delivery", "Team Leader", 
        "Top Designer", "Employee of the Month", "Rising Talent", "Problem Solver",
        "Founder Crown Badge", "Elite Admin Badge"
    )

    AlertDialog(
        onDismissRequest = onClose,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier
            .fillMaxWidth(0.95f)
            .padding(12.dp)
            .testTag("public_profile_dialog_${user.username}"),
        title = null,
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Header Banner Image
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(115.dp)
                            .background(coverBrush, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    ) {
                        IconButton(
                            onClick = onClose,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp)
                                .background(Color.Black.copy(alpha = 0.35f), CircleShape)
                                .size(28.dp)
                        ) {
                            Icon(Icons.Filled.Close, contentDescription = "Close", tint = Color.White, modifier = Modifier.size(14.dp))
                        }
                        if (user.role == "Super Admin") {
                            Icon(
                                imageVector = Icons.Filled.WorkspacePremium,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.15f),
                                modifier = Modifier
                                    .size(90.dp)
                                    .align(Alignment.BottomEnd)
                                    .padding(bottom = 6.dp, end = 12.dp)
                            )
                        }
                    }
                }

                // Avatar and User Profile labels
                item {
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        modifier = Modifier
                            .padding(horizontal = 12.dp)
                            .offset(y = (-40).dp)
                    ) {
                        ClickableAvatar(
                            avatarId = user.avatarId,
                            fullName = user.fullName,
                            size = 74,
                            modifier = Modifier.border(3.5.dp, Color.White, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (user.role == "Super Admin") {
                                    Icon(Icons.Filled.WorkspacePremium, contentDescription = "Crown", tint = Color(0xFFFFD700), modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(3.dp))
                                } else if (user.role == "Admin") {
                                    Icon(Icons.Filled.Shield, contentDescription = "Admin Shield", tint = Color(0xFF1E88E5), modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(3.dp))
                                }
                                Text(
                                    text = user.fullName,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 17.sp,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                            Text(
                                text = "${user.position} • ${user.role}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                // Super Admin Mission Concept statement segment
                if (user.role == "Super Admin" && user.missionStatement.isNotEmpty()) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .offset(y = (-30).dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    "FOUNDER VISION STATEMENT:",
                                    fontSize = 8.5.sp,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.primary,
                                    letterSpacing = 0.5.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "\"" + user.missionStatement + "\"",
                                    fontSize = 12.sp,
                                    fontStyle = FontStyle.Italic,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                    }
                }

                // Achievements stats row
                item {
                    val shiftY = if (user.role == "Super Admin" && user.missionStatement.isNotEmpty()) (-30).dp else (-30).dp
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .offset(y = shiftY),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F7FA)),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = String.format("%02d", user.completedProjectsCount),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                                Text("COMPLETED BRIEFS", fontSize = 8.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${user.performanceScore}%",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text("ACCURACY LEVEL", fontSize = 8.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = user.joinedDate,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFFEC407A)
                                )
                                Text("TEAM JOIN DATE", fontSize = 8.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Bio
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .offset(y = (-25).dp)
                    ) {
                        Text(
                            text = "CREATIVE BIOGRAPHY",
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = user.bio.ifEmpty { "This hub designer specialist has not written a custom description profile yet." },
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.secondary,
                            lineHeight = 15.sp
                        )
                    }
                }

                // Core skills
                if (user.skills.isNotEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .offset(y = (-20).dp)
                        ) {
                            Text(
                                "MODULAR SKILLS DECK",
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                user.skills.split(",").forEach { s ->
                                    Box(
                                        modifier = Modifier
                                            .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(12.dp))
                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = s.trim(),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Earned credentials
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .offset(y = (-15).dp)
                    ) {
                        Text(
                            text = "EARNED BRAND CREDENTIALS",
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        val earnedList = user.earnedBadges.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                        if (earnedList.isEmpty()) {
                            Text("No design badges earned yet. Complete claimed operations with pristine graphics to earn specialized credentials!", fontSize = 11.sp, color = Color.Gray)
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                earnedList.forEach { badge ->
                                    BadgeDisplay(badgeName = badge, modifier = Modifier.fillMaxWidth())
                                }
                            }
                        }
                    }
                }

                // Super Admin featured portfolio segments
                if (user.role == "Super Admin" && user.featuredProjects.isNotEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .offset(y = (-10).dp)
                        ) {
                            Text(
                                "FEATURED PLATFORM SEGMENTS",
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                user.featuredProjects.split(",").forEach { fp ->
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                                        modifier = Modifier.width(135.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Icon(Icons.Filled.FolderZip, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(22.dp))
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(fp.trim(), fontWeight = FontWeight.Bold, fontSize = 10.sp, color = MaterialTheme.colorScheme.secondary)
                                            Text("Sleek Repository", fontSize = 8.sp, color = Color.Gray)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Contact links
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .offset(y = (-5).dp)
                    ) {
                        Text(
                            text = "CONTACT & AUDITING CHANNELS",
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (user.whatsapp.isNotEmpty()) {
                                IconButton(
                                    onClick = {
                                        val filterWa = user.whatsapp.trim().filter { it.isDigit() }
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://api.whatsapp.com/send?phone=$filterWa"))
                                        context.startActivity(intent)
                                    },
                                    modifier = Modifier
                                        .background(Color(0xFF25D366), CircleShape)
                                        .size(36.dp)
                                ) {
                                    Icon(Icons.Filled.Phone, contentDescription = "Launch WhatsApp Chat", tint = Color.White, modifier = Modifier.size(16.dp))
                                }
                            }
                            if (user.contactInfo.isNotEmpty()) {
                                IconButton(
                                    onClick = {
                                        val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:${user.contactInfo}"))
                                        context.startActivity(intent)
                                    },
                                    modifier = Modifier
                                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                                        .size(36.dp)
                                ) {
                                    Icon(Icons.Filled.Email, contentDescription = "Mail Member", tint = Color.White, modifier = Modifier.size(16.dp))
                                }
                            }
                            if (user.website.isNotEmpty()) {
                                IconButton(
                                    onClick = {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(user.website))
                                        context.startActivity(intent)
                                    },
                                    modifier = Modifier
                                        .background(MaterialTheme.colorScheme.secondary, CircleShape)
                                        .size(36.dp)
                                ) {
                                    Icon(Icons.Filled.Language, contentDescription = "Website URL", tint = Color.White, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }

                // Badge gifting console desk (only Admin or Super Admin managing other users)
                if (currentUser?.role == "Super Admin" || currentUser?.role == "Admin") {
                    val notSelf = currentUser?.username != user.username
                    if (notSelf) {
                        item {
                            Spacer(modifier = Modifier.height(4.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                            Spacer(modifier = Modifier.height(4.dp))

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("admin_badge_auditing_panel"),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "EXECUTIVE BRAND BADGE GIFT OFFICE",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.secondary,
                                        letterSpacing = 0.5.sp
                                    )
                                    Text("Award or revoke professional visual credentials instantly.", fontSize = 9.sp, color = Color.Gray)
                                    
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Button(
                                            onClick = { assignBadgeDropdownExpanded = true },
                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(12.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Gift Class Badge", fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                                        }

                                        DropdownMenu(
                                            expanded = assignBadgeDropdownExpanded,
                                            onDismissRequest = { assignBadgeDropdownExpanded = false }
                                        ) {
                                            availableBadges.forEach { b ->
                                                DropdownMenuItem(
                                                    text = { Text(b, fontWeight = FontWeight.Black, fontSize = 12.sp) },
                                                    onClick = {
                                                        viewModel.awardBadge(user.username, b)
                                                        assignBadgeDropdownExpanded = false
                                                        Toast.makeText(context, "$b granted to ${user.fullName}!", Toast.LENGTH_SHORT).show()
                                                        onClose()
                                                    }
                                                )
                                            }
                                        }
                                    }

                                    // Tap to revoke
                                    val currentList = user.earnedBadges.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                                    if (currentList.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text("Tap (X) to revoke visual badge credential:", fontSize = 9.5.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        FlowRow(
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            currentList.forEach { b ->
                                                Box(
                                                    modifier = Modifier
                                                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                                                        .border(1.dp, Color.Red.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                                ) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Text(b, fontSize = 8.5.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Icon(
                                                            imageVector = Icons.Filled.Close,
                                                            contentDescription = "Revoke",
                                                            tint = Color.Red,
                                                            modifier = Modifier
                                                                .size(11.dp)
                                                                .clickable {
                                                                    viewModel.removeBadge(user.username, b)
                                                                    Toast.makeText(context, "$b revoked.", Toast.LENGTH_SHORT).show()
                                                                    onClose()
                                                                }
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onClose) {
                Text("DISMISS", fontWeight = FontWeight.Black)
            }
        }
    )
}

// REGISTERED METADATA NOTIFICATION ENTERPRISE DISPATCH PANEL
@Composable
fun NotificationComposerSection(
    viewModel: TeamHubViewModel,
    users: List<User>
) {
    val context = LocalContext.current
    var isExpanded by remember { mutableStateOf(false) }

    // Forms
    var title by remember { mutableStateOf("") }
    var msg by remember { mutableStateOf("") }
    
    var category by remember { mutableStateOf("Announcement") }
    var catDropdown by remember { mutableStateOf(false) }
    val categories = listOf("Announcement", "Project Assigned", "Project Deadline", "Project Approved", "Revision Required", "Badge Awarded", "Team Update", "Important Alert", "System Update", "Custom")

    var priority by remember { mutableStateOf("Normal") }
    val priorities = listOf("Low", "Normal", "High", "Urgent")

    var bannerPreset by remember { mutableStateOf("Sunset Neon") }
    val banners = listOf("Sunset Neon", "Classic Gold", "Sleek Charcoal", "Deep Sapphire", "Emerald Forest")

    var iconSelected by remember { mutableStateOf("Comment") }
    val iconsList = listOf("Comment", "Shield", "Crown", "Star", "Alert", "Check")

    // Target choices
    var audType by remember { mutableStateOf("All Users") }
    var singleUserTarget by remember { mutableStateOf("") }
    var singleUserDropdown by remember { mutableStateOf(false) }

    var waitMinsTime by remember { mutableStateOf(0) }

    // Analytics (Simulated insights)
    val totalDispatchedCount = 38
    val confirmedReadsCount = 124
    val engagementRate = 92

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("notification_composer_card"),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Executive Broadcast Desk",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text("Design, schedule, and test custom styled campaign notifications.", fontSize = 9.5.sp, color = Color.Gray)
                }

                IconButton(
                    onClick = { isExpanded = !isExpanded },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                        contentDescription = "Expand controls",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            if (isExpanded) {
                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Broadcast Label Title") },
                    placeholder = { Text("Brand brief updated...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = msg,
                    onValueChange = { msg = it },
                    label = { Text("Visual Brief Details Message") },
                    placeholder = { Text("Deliver final revisions within 4 hours...") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 4
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Urgency selections
                Text("Select Notification Urgency Priority:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    priorities.forEach { p ->
                        val selected = priority == p
                        FilterChip(
                            selected = selected,
                            onClick = { priority = p },
                            label = { Text(p, fontSize = 9.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = when(p) {
                                    "Urgent" -> Color.Red.copy(alpha = 0.15f)
                                    "High" -> Color(0xFFFF9800).copy(alpha = 0.15f)
                                    else -> MaterialTheme.colorScheme.primaryContainer
                                },
                                selectedLabelColor = when(p) {
                                    "Urgent" -> Color.Red
                                    "High" -> Color.DarkGray
                                    else -> MaterialTheme.colorScheme.primary
                                }
                            )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                // Category Selection dropdown choice list
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Text("Select Category: ", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    Box {
                        TextButton(onClick = { catDropdown = true }) {
                            Text(category, fontWeight = FontWeight.Black, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                            Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
                        }
                        DropdownMenu(
                            expanded = catDropdown,
                            onDismissRequest = { catDropdown = false }
                        ) {
                            categories.forEach { c ->
                                DropdownMenuItem(
                                    text = { Text(c, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                    onClick = {
                                        category = c
                                        catDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))

                // Visual theme banners selector
                Text("Simulated Cover Presets:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    banners.forEach { b ->
                        val selected = bannerPreset == b
                        val brush = when(b) {
                            "Sunset Neon" -> Brush.linearGradient(listOf(Color(0xFFF857A6), Color(0xFFFF5858)))
                            "Classic Gold" -> Brush.linearGradient(listOf(Color(0xFFF12711), Color(0xFFF5AF19)))
                            "Sleek Charcoal" -> Brush.linearGradient(listOf(Color(0xFF4F5B66), Color(0xFF232526)))
                            "Deep Sapphire" -> Brush.linearGradient(listOf(Color(0xFF00C6FF), Color(0xFF0072FF)))
                            "Emerald Forest" -> Brush.linearGradient(listOf(Color(0xFF11998E), Color(0xFF38EF7D)))
                            else -> Brush.linearGradient(listOf(Color(0xFF4F5B66), Color(0xFF232526)))
                        }
                        Card(
                            modifier = Modifier
                                .width(90.dp)
                                .height(38.dp)
                                .clickable { bannerPreset = b }
                                .border(
                                    width = if (selected) 2.5.dp else 1.dp,
                                    color = if (selected) MaterialTheme.colorScheme.secondary else Color.Transparent,
                                    shape = RoundedCornerShape(6.dp)
                                ),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Box(modifier = Modifier.fillMaxSize().background(brush), contentAlignment = Alignment.Center) {
                                Text(b, color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Black)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                // Customizable icon configurations
                Text("Select Icon Identifier Layout:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                ) {
                    iconsList.forEach { ic ->
                        val selected = iconSelected == ic
                        val iconVector = when(ic) {
                            "Comment" -> Icons.Filled.Forum
                            "Shield" -> Icons.Filled.Shield
                            "Crown" -> Icons.Filled.WorkspacePremium
                            "Star" -> Icons.Filled.Star
                            "Alert" -> Icons.Filled.Campaign
                            "Check" -> Icons.Filled.Check
                            else -> Icons.Filled.Notifications
                        }
                        IconButton(
                            onClick = { iconSelected = ic },
                            modifier = Modifier
                                .size(32.dp)
                                .background(
                                    color = if (selected) MaterialTheme.colorScheme.primaryContainer else Color(0xFFF5F5F5),
                                    shape = CircleShape
                                )
                        ) {
                            Icon(iconVector, contentDescription = ic, modifier = Modifier.size(14.dp), tint = if (selected) MaterialTheme.colorScheme.primary else Color.Gray)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                // Select audience target group
                Text("Target Dispatch Audience:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    listOf("All Users", "Admins Only", "Team Members Only", "Single Member").forEach { aud ->
                        val selected = audType == aud
                        FilterChip(
                            selected = selected,
                            onClick = { 
                                audType = aud
                                if (aud != "Single Member") {
                                    singleUserTarget = ""
                                }
                            },
                            label = { Text(aud, fontSize = 8.5.sp) }
                        )
                    }
                }

                if (audType == "Single Member") {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Select Member: ", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                        Box {
                            TextButton(onClick = { singleUserDropdown = true }) {
                                val label = users.find { it.username == singleUserTarget }?.fullName 
                                    ?: singleUserTarget.ifEmpty { "Choose Member Username" }
                                Text(label, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                                Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
                            }
                            DropdownMenu(
                                expanded = singleUserDropdown,
                                onDismissRequest = { singleUserDropdown = false }
                            ) {
                                users.forEach { u ->
                                    DropdownMenuItem(
                                        text = { Text(u.fullName + " (${u.username})", fontSize = 11.sp) },
                                        onClick = {
                                            singleUserTarget = u.username
                                            singleUserDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                // Schedule minutes offset
                Text("Delivery Delay Hours Setting:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    listOf(0, 5, 15, 60, 240).forEach { mins ->
                        val selected = waitMinsTime == mins
                        FilterChip(
                            selected = selected,
                            onClick = { waitMinsTime = mins },
                            label = { 
                                Text(
                                    text = if (mins == 0) "Direct Delivery" else "+$mins Mins Delay",
                                    fontSize = 9.sp
                                )
                            }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))

                // Broadcast CTA
                Button(
                    onClick = {
                        if (title.trim().isEmpty() || msg.trim().isEmpty()) {
                            Toast.makeText(context, "Clear title & message variables needed.", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        val finalTarget = when(audType) {
                            "Admins Only" -> "Admin"
                            "Team Members Only" -> "Team Member"
                            "Single Member" -> singleUserTarget.ifEmpty { "All" }
                            else -> "All"
                        }

                        val timeStamp = if (waitMinsTime == 0) 0L else System.currentTimeMillis() + (waitMinsTime * 60000L)

                        viewModel.createDetailedNotification(
                            title = title,
                            message = msg,
                            targetUsername = finalTarget,
                            priority = priority,
                            type = category,
                            bannerPreset = bannerPreset,
                            badgeIcon = iconSelected,
                            isScheduled = waitMinsTime > 0,
                            scheduledTime = timeStamp
                        )

                        val reportText = if (waitMinsTime > 0) {
                            "Broadcast delivery scheduled safely inside dispatch indexes!"
                        } else {
                            "Prism Alert broadcasted instantly across all responsive channels!"
                        }
                        Toast.makeText(context, reportText, Toast.LENGTH_SHORT).show()

                        title = ""
                        msg = ""
                        waitMinsTime = 0
                        isExpanded = false
                    },
                    modifier = Modifier.fillMaxWidth().height(42.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Filled.Campaign, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Broadcast Premium Alert", fontWeight = FontWeight.Black)
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(8.dp))

                // CAMPAIGN ANALYTICS DATA DASHBOARD
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F7FA)),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "HISTORICAL ALERT ENGAGEMENT GRAPH",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.secondary,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("$totalDispatchedCount Campaigns", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                                Text("Total Sent", fontSize = 7.5.sp, color = Color.Gray)
                            }
                            Column {
                                Text("$confirmedReadsCount Reads", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                Text("Aesthetic Confirmations", fontSize = 7.5.sp, color = Color.Gray)
                            }
                            Column {
                                Text("$engagementRate% Match", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                                Text("Active Reach Ratio", fontSize = 7.5.sp, color = Color.Gray)
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = 0.92f,
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = Color.LightGray.copy(alpha = 0.4f),
                            modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp))
                        )
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(4.dp))
                Text("Tap to review designer broadcast templates, delay coordinates, and target groups statistics.", fontSize = 9.sp, color = Color.Gray)
            }
        }
    }
}

// -------------------------------------------------------------
// MAIN BRAND HEADER COMPOSABLE
@Composable
fun HeaderBrandBar(
    viewModel: TeamHubViewModel,
    onActionClick: () -> Unit = {},
    actionIcon: ImageVector? = null,
    actionDescription: String = ""
) {
    val user by viewModel.currentUser.collectAsState()
    val notificationList by viewModel.userNotifications.collectAsState()
    val unreadCount = notificationList.filter { !it.read }.size
    var showNotifDialog by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .statusBarsPadding(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // App logo or adaptive icon
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Filled.DesignServices,
                contentDescription = "Logo icon",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(10.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "DesignMastery_Lab",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.secondary,
                letterSpacing = (-0.2).sp
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF4CAF50))
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "TEAM HUB • ${user?.role ?: ""}",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp
                )
            }
        }

        // Notification center
        Box {
            IconButton(onClick = { showNotifDialog = true }) {
                BadgedBox(
                    badge = {
                        if (unreadCount > 0) {
                            Badge(containerColor = MaterialTheme.colorScheme.primary) {
                                Text(unreadCount.toString(), color = Color.White)
                            }
                        }
                    }
                ) {
                    Icon(
                        Icons.Outlined.Notifications,
                        contentDescription = "Notification center",
                        tint = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }

        if (actionIcon != null) {
            IconButton(onClick = onActionClick) {
                Icon(actionIcon, contentDescription = actionDescription, tint = MaterialTheme.colorScheme.secondary)
            }
        }
    }

    if (showNotifDialog) {
        AlertDialog(
            onDismissRequest = { showNotifDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Campaign, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Hub Alerts & Updates", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                if (notificationList.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No new alerts. You are completely up to date!",
                            textAlign = TextAlign.Center,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f)
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 300.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(notificationList) { notif ->
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = if (notif.read) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                                ),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = notif.title,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        if (!notif.read) {
                                            Text(
                                                text = "New",
                                                color = MaterialTheme.colorScheme.primary,
                                                fontWeight = FontWeight.Black,
                                                fontSize = 9.sp,
                                                modifier = Modifier
                                                    .background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(4.dp))
                                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = notif.message,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(Date(notif.timestamp)),
                                            fontSize = 8.sp,
                                            color = Color.Gray
                                        )
                                        if (!notif.read) {
                                            Text(
                                                text = "Dismiss",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier
                                                    .clickable {
                                                        viewModel.markNotificationAsRead(notif.id)
                                                    }
                                                    .padding(4.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showNotifDialog = false }) {
                    Text("Close", color = MaterialTheme.colorScheme.secondary)
                }
            }
        )
    }
}

// -------------------------------------------------------------
// LOGIN SCREEN WITH TEST CREDENTIALS LISTING
@Composable
fun LoginScreen(viewModel: TeamHubViewModel) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var pwordVisible by remember { mutableStateOf(false) }
    val loginError by viewModel.loginError.collectAsState()
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.secondary,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.9f)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            item {
                // Header image or brand logo
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Try to load generated logo, fallback to vector icon
                    AsyncImage(
                        model = "file:///android_asset/img_app_icon_new_1782038641239.jpg", // placeholder try
                        contentDescription = "App Icon",
                        fallback = painterResource(id = R.drawable.img_app_icon_new_1782038641239), // use generated image
                        error = painterResource(id = R.drawable.img_app_icon_new_1782038641239),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.clip(CircleShape)
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "DesignMastery_Lab",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    letterSpacing = (-0.5).sp
                )
                
                Text(
                    text = "Convert ideas into design",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White.copy(alpha = 0.9f),
                    letterSpacing = 1.sp
                )
                
                Spacer(modifier = Modifier.height(24.dp))
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 420.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text(
                            text = "Secure Member Access",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        OutlinedTextField(
                            value = username,
                            onValueChange = {
                                username = it
                                viewModel.clearLoginError()
                            },
                            label = { Text("Username") },
                            placeholder = { Text("e.g. superadmin, john_vector") },
                            leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.Black,
                                unfocusedTextColor = Color.Black,
                                focusedLabelColor = Color.Black,
                                unfocusedLabelColor = Color.Black,
                                focusedPlaceholderColor = Color.Black.copy(alpha = 0.5f),
                                unfocusedPlaceholderColor = Color.Black.copy(alpha = 0.5f),
                                focusedLeadingIconColor = Color.Black,
                                unfocusedLeadingIconColor = Color.Black,
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = password,
                            onValueChange = {
                                password = it
                                viewModel.clearLoginError()
                            },
                            label = { Text("Password") },
                            leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
                            trailingIcon = {
                                IconButton(onClick = { pwordVisible = !pwordVisible }) {
                                    Icon(
                                        imageVector = if (pwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                        contentDescription = if (pwordVisible) "Hide password" else "Show password"
                                    )
                                }
                            },
                            singleLine = true,
                            visualTransformation = if (pwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.Black,
                                unfocusedTextColor = Color.Black,
                                focusedLabelColor = Color.Black,
                                unfocusedLabelColor = Color.Black,
                                focusedLeadingIconColor = Color.Black,
                                unfocusedLeadingIconColor = Color.Black,
                                focusedTrailingIconColor = Color.Black,
                                unfocusedTrailingIconColor = Color.Black,
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        if (loginError != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = loginError ?: "",
                                color = Color.Red,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = {
                                if (username.trim().isEmpty() || password.isEmpty()) {
                                    Toast.makeText(context, "Please enter all fields", Toast.LENGTH_SHORT).show()
                                } else {
                                    viewModel.login(username, password)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Key, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Authenticate Port", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// HOMEPAGE: BRANDING AND FOUNDER / ADMIN CARD SHOWCASING
@Composable
fun HomeScreen(viewModel: TeamHubViewModel, onNavigate: (String) -> Unit) {
    val users by viewModel.allUsers.collectAsState()
    val context = LocalContext.current
    
    // Find Super Admin and Admin profiles
    val superAdmin = users.firstOrNull { it.role == "Super Admin" }
    val admin = users.firstOrNull { it.role == "Admin" }

    Column(modifier = Modifier.fillMaxSize()) {
        HeaderBrandBar(viewModel = viewModel)
        
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hero banner image
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Box(modifier = Modifier.height(180.dp)) {
                        AsyncImage(
                            model = "file:///android_asset/img_hero_banner_1782034654744.jpg",
                            contentDescription = "Creative Agency Showcase Banner",
                            fallback = painterResource(id = R.drawable.img_hero_banner_1782034654744),
                            error = painterResource(id = R.drawable.img_hero_banner_1782034654744),
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        // Gradient Overlay
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))
                                    )
                                )
                        )
                        
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(16.dp)
                        ) {
                            Text(
                                "DesignMastery_Lab",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                "Where premium concepts become visual legacy. Empowering designers to master workflows.",
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 11.sp,
                                maxLines = 2
                            )
                        }
                    }
                }
            }

            // Quick navigation tabs shortcuts
            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    QuickTabCard(
                        title = "Claim Briefs",
                        sub = "Launch Projects",
                        icon = Icons.Filled.FolderZip,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate("projects") }
                    )
                    QuickTabCard(
                        title = "Discussion",
                        sub = "Team Resources",
                        icon = Icons.Filled.Forum,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate("team_area") }
                    )
                }
            }

            // 1. Super Admin profile card
            if (superAdmin != null) {
                item {
                    Text(
                        "Founder & Director Card",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                    AgencyProfileCard(user = superAdmin, context = context, onClick = { viewModel.showProfile(superAdmin) })
                }
            }

            // 2. Admin profile card
            if (admin != null) {
                item {
                    Text(
                        "QA Operations Card",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                    AgencyProfileCard(user = admin, context = context, onClick = { viewModel.showProfile(admin) })
                }
            }
        }
    }
}

@Composable
fun QuickTabCard(title: String, sub: String, icon: ImageVector, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(
        modifier = modifier.clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                Text(sub, fontSize = 8.sp, color = Color.Gray)
            }
        }
    }
}

@Composable
fun AgencyProfileCard(user: User, context: Context, onClick: (() -> Unit)? = null) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = onClick != null) { onClick?.invoke() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ClickableAvatar(avatarId = user.avatarId, fullName = user.fullName, size = 56, onClick = onClick)
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(user.fullName, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = MaterialTheme.colorScheme.secondary)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .background(
                                    if (user.role == "Super Admin") MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer,
                                    RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                user.position,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (user.role == "Super Admin") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }
                
                // Copy profile link feature button
                IconButton(onClick = {
                    val link = "https://designmasterylab/hub/profile/${user.username}"
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("DesignMastery Profile", link)
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(context, "Card Link copied to clipboard!", Toast.LENGTH_SHORT).show()
                }) {
                    Icon(Icons.Filled.Share, contentDescription = "Copy card link", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(user.bio, fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.8f))
            
            Spacer(modifier = Modifier.height(8.dp))
            // Skills chips
            Text("Skill Deck:", fontWeight = FontWeight.Bold, fontSize = 9.sp, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(4.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                items(user.skills.split(",").map { it.trim() }.filter { it.isNotEmpty() }) { skill ->
                    Box(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.background, RoundedCornerShape(20.dp))
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(skill, fontSize = 8.sp, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline)
            Spacer(modifier = Modifier.height(10.dp))

            // Action social links
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (user.whatsapp.isNotEmpty()) {
                        SocialIconButton(icon = Icons.Outlined.Chat, label = "WA") {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://api.whatsapp.com/send?phone=${user.whatsapp}"))
                            context.startActivity(intent)
                        }
                    }
                    if (user.contactInfo.isNotEmpty()) {
                        SocialIconButton(icon = Icons.Outlined.Email, label = "Email") {
                            val intent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("mailto:${user.contactInfo}")
                                putExtra(Intent.EXTRA_SUBJECT, "Collaborative Outreach - DesignMastery_Lab")
                            }
                            context.startActivity(intent)
                        }
                    }
                    if (user.website.isNotEmpty()) {
                        SocialIconButton(icon = Icons.Outlined.Web, label = "Web") {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(user.website))
                            context.startActivity(intent)
                        }
                    }
                }

                // Show social icons directly
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (user.instagram.isNotEmpty()) {
                        SocialOutlineIcon(Icons.Filled.PhotoCamera, "Instagram") {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://instagram.com/${user.instagram}"))
                            context.startActivity(intent)
                        }
                    }
                    if (user.facebook.isNotEmpty()) {
                        SocialOutlineIcon(Icons.Filled.Facebook, "Facebook") {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://facebook.com/${user.facebook}"))
                            context.startActivity(intent)
                        }
                    }
                    if (user.linkedin.isNotEmpty()) {
                        SocialOutlineIcon(Icons.Filled.AccountTree, "LinkedIn") {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://linkedin.com/in/${user.linkedin}"))
                            context.startActivity(intent)
                        }
                    }
                    if (user.youtube.isNotEmpty()) {
                        SocialOutlineIcon(Icons.Filled.PlayCircle, "YouTube") {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://youtube.com/c/${user.youtube}"))
                            context.startActivity(intent)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SocialIconButton(icon: ImageVector, label: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
        modifier = Modifier.height(28.dp),
        shape = RoundedCornerShape(4.dp)
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.width(4.dp))
        Text(label, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
fun SocialOutlineIcon(icon: ImageVector, contentDescription: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(24.dp)
            .clip(CircleShape)
            .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = contentDescription, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.primary)
    }
}

// -------------------------------------------------------------
// OPERATIONAL DASHBOARD VIEW: PROJECTS SUMMARY, ANNOUNCEMENTS LIST
@Composable
fun DashboardOperationalScreen(viewModel: TeamHubViewModel, onNavigate: (String) -> Unit) {
    val projects by viewModel.allProjects.collectAsState()
    val announcements by viewModel.allAnnouncements.collectAsState()
    val user by viewModel.currentUser.collectAsState()
    
    val pendingCount = projects.count { it.status == "Pending" }
    val progressCount = projects.count { it.status == "In Progress" }
    val reviewCount = projects.count { it.status == "Review" }
    val completedCount = projects.count { it.status == "Completed" }

    Column(modifier = Modifier.fillMaxSize()) {
        HeaderBrandBar(viewModel = viewModel)
        
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Welcome Section (Editorial Aesthetic)
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Text(
                        text = "TEAM HUB • ${user?.role?.uppercase() ?: "SUPER ADMIN"}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Welcome back,",
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = "${user?.fullName ?: "Toli Creative"}.",
                        style = MaterialTheme.typography.displayMedium,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            // Executive Dashboard Notification Composer 
            if (user?.role == "Super Admin" || user?.role == "Admin") {
                item {
                    val allUsers by viewModel.allUsers.collectAsState()
                    NotificationComposerSection(viewModel = viewModel, users = allUsers)
                }
            }

            // Metrics summary cards
            item {
                Text(
                    text = "Current Pulse Status",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.secondary,
                    letterSpacing = (-0.2).sp
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    CountCard(
                        title = "New",
                        count = pendingCount,
                        color = MaterialTheme.colorScheme.primary,
                        highlight = false,
                        modifier = Modifier.weight(1f)
                    ) {
                        onNavigate("projects")
                    }
                    CountCard(
                        title = "Active",
                        count = progressCount,
                        color = Color.White,
                        highlight = true,
                        modifier = Modifier.weight(1f)
                    ) {
                        onNavigate("projects")
                    }
                    CountCard(
                        title = "Review",
                        count = reviewCount,
                        color = MaterialTheme.colorScheme.primary,
                        highlight = false,
                        modifier = Modifier.weight(1f)
                    ) {
                        onNavigate("projects")
                    }
                    CountCard(
                        title = "Done",
                        count = completedCount,
                        color = MaterialTheme.colorScheme.primary,
                        highlight = false,
                        modifier = Modifier.weight(1f)
                    ) {
                        onNavigate("projects")
                    }
                }
            }

            // Latest operational announcement
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Announcements Board",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = "View Talk",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable { onNavigate("team_area") }
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                if (announcements.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Text(
                            "No announcements posted yet.",
                            modifier = Modifier.padding(24.dp).fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                } else {
                    val announcement = announcements.first()
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(
                            1.dp, 
                            if (announcement.priority == "High") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (announcement.priority == "High") {
                                    Box(
                                        modifier = Modifier
                                            .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(4.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text("URGENT", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Black, fontSize = 8.sp)
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                }
                                Text(announcement.title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.secondary)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(announcement.content, fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.8f))
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("By: ${announcement.author} (${announcement.authorRole})", fontSize = 9.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                Text(
                                    text = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(announcement.timestamp)),
                                    fontSize = 8.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }
            }

            // Quick overview list of outstanding items
            item {
                Text(
                    text = "Operational Active Brief Queue",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.height(4.dp))
            }

            val runningBriefs = projects.filter { it.status == "In Progress" || it.status == "Review" }
            if (runningBriefs.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Text(
                            "No active design reviews in queue. Ready for next campaign claims!",
                            modifier = Modifier.padding(24.dp).fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                }
            } else {
                items(runningBriefs.take(4)) { brief ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (brief.status == "Review") Color(0xFFE8F5E9) else MaterialTheme.colorScheme.primaryContainer
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (brief.status == "Review") Icons.Filled.RateReview else Icons.Filled.OfflineBolt,
                                    contentDescription = null,
                                    tint = if (brief.status == "Review") Color(0xFF2E7D32) else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(brief.title, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
                                Text("Assigned to: ${brief.assignedTo.ifEmpty { "Unassigned" }}", fontSize = 9.sp, color = Color.Gray)
                            }
                            // Banner badge indicators
                            Box(
                                modifier = Modifier
                                    .background(
                                        color = if (brief.status == "Review") Color(0xFFFFEB3B) else MaterialTheme.colorScheme.primary,
                                        shape = RoundedCornerShape(20.dp)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = brief.status,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (brief.status == "Review") Color.Black else Color.White
                                )
                            }
                        }
                    }
                }
            }

            // Badge Spotlight Accent card (Editorial layout)
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigate("team_area") }
                        .testTag("spotlight_badge_card"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Color.White),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.MilitaryTech,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "LATEST AWARD SPOTLIGHT",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Fast Delivery: Mike Ross",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.Filled.ChevronRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Editorial Action Buttons
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { onNavigate("projects") },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("editorial_action_assign_task"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = MaterialTheme.colorScheme.secondary
                        ),
                        border = BorderStroke(2.dp, MaterialTheme.colorScheme.secondary),
                        shape = RoundedCornerShape(16.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            text = "ASSIGN BRIEF",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                    }
                    Button(
                        onClick = { onNavigate("team_area") },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("editorial_action_new_badge"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(16.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            text = "NEW BADGE",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CountCard(
    title: String,
    count: Int,
    color: Color,
    modifier: Modifier = Modifier,
    highlight: Boolean = false,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable { onClick() }.testTag("count_card_$title"),
        colors = CardDefaults.cardColors(
            containerColor = if (highlight) MaterialTheme.colorScheme.primary else Color.White
        ),
        border = BorderStroke(1.dp, if (highlight) Color.Transparent else MaterialTheme.colorScheme.outline),
        elevation = CardDefaults.cardElevation(defaultElevation = if (highlight) 4.dp else 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = String.format("%02d", count),
                style = MaterialTheme.typography.displayMedium,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = if (highlight) Color.White else MaterialTheme.colorScheme.secondary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = title.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                fontSize = 8.sp,
                color = if (highlight) Color.White.copy(alpha = 0.85f) else Color.Gray,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// -------------------------------------------------------------
// PROJECTS SCREEN: CLAIM, FILE UPLOAD SIMULATORS, STATUS REVIEWS
@Composable
fun ProjectsScreen(viewModel: TeamHubViewModel) {
    val projects by viewModel.allProjects.collectAsState()
    val user by viewModel.currentUser.collectAsState()
    val designers by viewModel.allUsers.collectAsState()
    
    var showCreateDialog by remember { mutableStateOf(false) }
    var selectedProjectForReview by remember { mutableStateOf<Project?>(null) }
    var selectedProjectForSubmission by remember { mutableStateOf<Project?>(null) }
    
    // Project input fields
    var projectTitle by remember { mutableStateOf("") }
    var projectDesc by remember { mutableStateOf("") }
    var projectAssignee by remember { mutableStateOf("") }
    
    // Submission input fields
    var submitFileName by remember { mutableStateOf("") }
    var submitFileType by remember { mutableStateOf("ZIP") }
    val fileTypes = listOf("PSD", "AI", "PNG", "JPG", "ZIP", "PDF")
    var isExpandedTypeDropdown by remember { mutableStateOf(false) }
    var submitNotes by remember { mutableStateOf("") }
    
    // QA / Review inputs
    var reviewFeedback by remember { mutableStateOf("") }

    // Edit project state variables
    var selectedProjectForEdit by remember { mutableStateOf<Project?>(null) }
    var editProjectTitle by remember { mutableStateOf("") }
    var editProjectDesc by remember { mutableStateOf("") }
    var editProjectAssignee by remember { mutableStateOf("") }

    // Direct phone alert notification variables
    var showQuickAlertDialog by remember { mutableStateOf(false) }
    var alertTitle by remember { mutableStateOf("") }
    var alertMessage by remember { mutableStateOf("") }
    var alertSelectedProjectId by remember { mutableStateOf<Int?>(null) }
    var alertSelectedMemberUsername by remember { mutableStateOf("") }
    var showDirectAlertOverlayMessage by remember { mutableStateOf("") }

    // Comments thread state variables
    var expandedCommentProjectId by remember { mutableStateOf<Int?>(null) }
    var projectCommentText by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {
        HeaderBrandBar(
            viewModel = viewModel,
            actionIcon = if (user?.role == "Admin" || user?.role == "Super Admin") Icons.Filled.Add else null,
            actionDescription = "New briefing",
            onActionClick = { showCreateDialog = true }
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Screen Header block
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Workspace Briefings", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = MaterialTheme.colorScheme.secondary)
                        Text("Claim or manage active project specifications & delivery storage.", fontSize = 11.sp, color = Color.Gray)
                    }
                    if (user?.role == "Admin" || user?.role == "Super Admin") {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Button(
                                onClick = { showQuickAlertDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.NotificationsActive, contentDescription = null, modifier = Modifier.size(13.dp), tint = Color.White)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Send Phone Alert", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Button(
                                onClick = { showCreateDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Text("New Brief", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            if (projects.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Text(
                            "No briefings available. Click 'New Brief' above to publish the first task spec.",
                            modifier = Modifier.padding(24.dp).fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }
            } else {
                items(projects) { project ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(project.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.secondary)
                                    Text("By: ${project.createdBy}", fontSize = 9.sp, color = Color.Gray)
                                }
                                // Status badge
                                Box(
                                    modifier = Modifier
                                        .background(
                                            color = when (project.status) {
                                                "Pending" -> Color(0xFFFFF3E0)
                                                "In Progress" -> Color(0xFFE3F2FD)
                                                "Review" -> Color(0xFFF3E5F5)
                                                else -> Color(0xFFE8F5E9)
                                            },
                                            shape = RoundedCornerShape(4.dp)
                                        )
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = project.status,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = when (project.status) {
                                            "Pending" -> Color(0xFFF57C00)
                                            "In Progress" -> Color(0xFF1976D2)
                                            "Review" -> Color(0xFF7B1FA2)
                                            else -> Color(0xFF388E3C)
                                        }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            Text(project.description, fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.85f))
                            
                            Spacer(modifier = Modifier.height(10.dp))
                            
                            // File Storage simulated segment
                            if (project.fileUrls.isNotEmpty()) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Filled.AttachFile, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Column {
                                        Text("Integrated Storage File:", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                        Text(project.fileUrls, fontSize = 10.sp, color = MaterialTheme.colorScheme.secondary)
                                        if (project.submissionNote.isNotEmpty()) {
                                            Text("Submission note: \"${project.submissionNote}\"", fontSize = 9.sp, color = Color.Gray, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                            }

                            if (project.feedback.isNotEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFFF9F9F9), RoundedCornerShape(4.dp))
                                        .padding(8.dp)
                                ) {
                                    Text("Feedback note: \"${project.feedback}\"", fontSize = 9.sp, color = Color.DarkGray)
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                            }

                            HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val assignee = project.assignedTo
                                if (assignee.isEmpty()) {
                                    Text("Status: Available for claim", fontSize = 10.sp, color = Color.Gray)
                                } else {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Outlined.PersonPin, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Claimed by: $assignee", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                                    }
                                }

                                // Interactive actions based on user roles
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    // 1. Edit & Delete if admin
                                    if (user?.role == "Admin" || user?.role == "Super Admin") {
                                        IconButton(
                                            onClick = { 
                                                selectedProjectForEdit = project
                                                editProjectTitle = project.title
                                                editProjectDesc = project.description
                                                editProjectAssignee = project.assignedTo
                                            },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(Icons.Outlined.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary)
                                        }
                                        IconButton(
                                            onClick = { viewModel.removeProject(project) },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(Icons.Outlined.Delete, contentDescription = "Delete", tint = Color.Red)
                                        }
                                    }

                                    // 2. Claim action for Member
                                    if (user?.role == "Team Member" && project.assignedTo.isEmpty()) {
                                        Button(
                                            onClick = { viewModel.claimProject(project.id) },
                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                            contentPadding = PaddingValues(horizontal = 8.dp),
                                            modifier = Modifier.height(28.dp)
                                        ) {
                                            Text("Claim Spec", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    // 3. Upload work action for assigned Member
                                    if (user?.role == "Team Member" && project.assignedTo == user?.username && project.status != "Completed") {
                                        Button(
                                            onClick = {
                                                selectedProjectForSubmission = project
                                                submitFileName = ""
                                                submitNotes = ""
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                            contentPadding = PaddingValues(horizontal = 8.dp),
                                            modifier = Modifier.height(28.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Filled.CloudUpload, contentDescription = null, modifier = Modifier.size(12.dp), tint = Color.White)
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Upload Deliveries", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }

                                    // 4. Review control action for Admins
                                    if ((user?.role == "Admin" || user?.role == "Super Admin") && project.status == "Review") {
                                        Button(
                                            onClick = {
                                                selectedProjectForReview = project
                                                reviewFeedback = ""
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7B1FA2)),
                                            contentPadding = PaddingValues(horizontal = 8.dp),
                                            modifier = Modifier.height(28.dp)
                                        ) {
                                            Text("Review Work", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        }
                                    }

                                    // 5. Toggle Comments section button
                                    val isCommentsExpanded = expandedCommentProjectId == project.id
                                    IconButton(
                                        onClick = {
                                            expandedCommentProjectId = if (isCommentsExpanded) null else project.id
                                            projectCommentText = ""
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            Icons.Outlined.Comment,
                                            contentDescription = "Comments",
                                            tint = if (isCommentsExpanded) MaterialTheme.colorScheme.primary else Color.Gray
                                        )
                                    }
                                }
                            }

                            // EXPANDABLE COMMENTS THREAD SECTION
                            if (expandedCommentProjectId == project.id) {
                                Spacer(modifier = Modifier.height(12.dp))
                                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    "Project Dialogue / Feedback Comments",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )

                                val comments by remember(project.id) { 
                                    viewModel.getCommentsForProject(project.id) 
                                }.collectAsState(initial = emptyList())

                                if (comments.isEmpty()) {
                                    Text(
                                        "No comments yet. Write a feedback or query below.",
                                        fontSize = 10.sp,
                                        color = Color.LightGray,
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    )
                                } else {
                                    Column(
                                        modifier = Modifier
                                            .padding(vertical = 6.dp)
                                            .fillMaxWidth(),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        comments.forEach { comment ->
                                            val isMyComment = comment.author == user?.username
                                            Card(
                                                modifier = Modifier.fillMaxWidth(),
                                                colors = CardDefaults.cardColors(
                                                    containerColor = if (isMyComment) Color(0xFFE0F2FE) else Color(0xFFFFF0F2)
                                                ),
                                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                                            ) {
                                                Column(modifier = Modifier.padding(8.dp)) {
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Text(
                                                            text = "${comment.author} (${comment.authorRole})",
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 9.sp,
                                                            color = MaterialTheme.colorScheme.secondary
                                                        )
                                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                                            Text(
                                                                text = java.text.SimpleDateFormat("MMM dd, HH:mm", java.util.Locale.getDefault()).format(java.util.Date(comment.timestamp)),
                                                                fontSize = 7.sp,
                                                                color = Color.Gray
                                                            )
                                                            if (isMyComment || user?.role == "Super Admin") {
                                                                Spacer(modifier = Modifier.width(4.dp))
                                                                IconButton(
                                                                    onClick = { viewModel.deleteProjectComment(comment.id) },
                                                                    modifier = Modifier.size(16.dp)
                                                                ) {
                                                                    Icon(
                                                                        Icons.Filled.Close,
                                                                        contentDescription = "Delete Comment",
                                                                        tint = Color.Red,
                                                                        modifier = Modifier.size(10.dp)
                                                                    )
                                                                }
                                                            }
                                                        }
                                                    }
                                                    Spacer(modifier = Modifier.height(2.dp))
                                                    Text(
                                                        text = comment.text,
                                                        fontSize = 10.sp,
                                                        color = MaterialTheme.colorScheme.secondary
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    OutlinedTextField(
                                        value = projectCommentText,
                                        onValueChange = { projectCommentText = it },
                                        placeholder = { Text("Write a project comment...", fontSize = 10.sp) },
                                        singleLine = true,
                                        textStyle = LocalTextStyle.current.copy(fontSize = 11.sp),
                                        modifier = Modifier.weight(1f)
                                    )
                                    IconButton(
                                        onClick = {
                                            if (projectCommentText.trim().isNotEmpty()) {
                                                viewModel.postProjectComment(project.id, projectCommentText)
                                                projectCommentText = ""
                                            }
                                        },
                                        modifier = Modifier
                                            .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(4.dp))
                                            .size(36.dp)
                                    ) {
                                        Icon(
                                            Icons.Filled.Send,
                                            contentDescription = "Send",
                                            tint = Color.White,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // CREATE PROJECT DIALOG
    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("Publish Brand brief", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = projectTitle,
                        onValueChange = { projectTitle = it },
                        label = { Text("Project Title") },
                        placeholder = { Text("e.g. Redesign Packaging Graphics") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = projectDesc,
                        onValueChange = { projectDesc = it },
                        label = { Text("Description & Specifications") },
                        placeholder = { Text("Specify assets required, layers, sizing palette...") },
                        maxLines = 4,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Text("Optionally Assign directly to Member:", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                    
                    // Designer picker layout
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        item {
                            FilterChip(
                                selected = projectAssignee.isEmpty(),
                                onClick = { projectAssignee = "" },
                                label = { Text("Let of anyone claim", fontSize = 9.sp) }
                            )
                        }
                        items(designers.filter { it.role == "Team Member" }) { designer ->
                            FilterChip(
                                selected = projectAssignee == designer.username,
                                onClick = { projectAssignee = designer.username },
                                label = { Text(designer.fullName, fontSize = 9.sp) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (projectTitle.trim().isNotEmpty() && projectDesc.trim().isNotEmpty()) {
                            viewModel.createProject(projectTitle.trim(), projectDesc.trim(), projectAssignee)
                            showCreateDialog = false
                            projectTitle = ""
                            projectDesc = ""
                            projectAssignee = ""
                        }
                    }
                ) {
                    Text("Publish Brief")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            }
        )
    }

    // DELIVERIES SUBMISSION DIALOG WITH EXTENSIONS SUPPORT
    if (selectedProjectForSubmission != null) {
        val proj = selectedProjectForSubmission!!
        AlertDialog(
            onDismissRequest = { selectedProjectForSubmission = null },
            title = { Text("Submit completed work files", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Brief: ${proj.title}", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                    
                    OutlinedTextField(
                        value = submitFileName,
                        onValueChange = { submitFileName = it },
                        label = { Text("File Name (e.g. logo_brandmark)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Drops selector of File Extensions specifically requested
                    Text("Select Media Extension type:", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        fileTypes.forEach { ext ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(
                                        if (submitFileType == ext) MaterialTheme.colorScheme.primary else Color(0xFFF0F0F0)
                                    )
                                    .clickable { submitFileType = ext }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = ext,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (submitFileType == ext) Color.White else Color.DarkGray
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = submitNotes,
                        onValueChange = { submitNotes = it },
                        label = { Text("Submission Notes (optional)") },
                        placeholder = { Text("Leave note about layering modifications...") },
                        maxLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (submitFileName.trim().isNotEmpty()) {
                            val resolvedFile = "${submitFileName.trim()}.${submitFileType.lowercase()}"
                            viewModel.submitProjectWork(
                                projectId = proj.id,
                                fileNames = resolvedFile,
                                notes = submitNotes.trim()
                            )
                            selectedProjectForSubmission = null
                        }
                    }
                ) {
                    Text("Upload Work")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedProjectForSubmission = null }) {
                    Text("Cancel", color = Color.Gray)
                }
            }
        )
    }

    // QA REVIEW WORK DIALOG
    if (selectedProjectForReview != null) {
        val proj = selectedProjectForReview!!
        AlertDialog(
            onDismissRequest = { selectedProjectForReview = null },
            title = { Text("Quality Operational Review", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Evaluating submission from: ${proj.assignedTo}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text("Submitted files: ${proj.fileUrls}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    if (proj.submissionNote.isNotEmpty()) {
                        Text("Author design details: \"${proj.submissionNote}\"", fontSize = 10.sp, color = Color.Gray)
                    }

                    OutlinedTextField(
                        value = reviewFeedback,
                        onValueChange = { reviewFeedback = it },
                        label = { Text("Adjustment feedback / Approval notes") },
                        placeholder = { Text("Comments on padding, typography hierarchy...") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(
                        onClick = {
                            viewModel.rejectSubmission(proj.id, reviewFeedback.trim().ifEmpty { "Work requires optimization as per layout standards." })
                            selectedProjectForReview = null
                        }
                    ) {
                        Text("Reject (Rework)", color = Color.Red, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF388E3C)),
                        onClick = {
                            viewModel.approveSubmission(proj.id, reviewFeedback.trim().ifEmpty { "Pristine standard. Brief marks complete!" })
                            selectedProjectForReview = null
                        }
                    ) {
                        Text("Approve Brief", color = Color.White)
                    }
                }
            }
        )
    }

    // EDIT BRIEF DIALOG
    if (selectedProjectForEdit != null) {
        val proj = selectedProjectForEdit!!
        AlertDialog(
            onDismissRequest = { selectedProjectForEdit = null },
            title = { Text("Edit Brand Brief", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = editProjectTitle,
                        onValueChange = { editProjectTitle = it },
                        label = { Text("Project Title") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editProjectDesc,
                        onValueChange = { editProjectDesc = it },
                        label = { Text("Description & Specifications") },
                        maxLines = 4,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Text("Reassign Member:", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                    
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        item {
                            FilterChip(
                                selected = editProjectAssignee.isEmpty(),
                                onClick = { editProjectAssignee = "" },
                                label = { Text("Unassigned / Anyone", fontSize = 9.sp) }
                            )
                        }
                        items(designers.filter { it.role == "Team Member" }) { designer ->
                            FilterChip(
                                selected = editProjectAssignee == designer.username,
                                onClick = { editProjectAssignee = designer.username },
                                label = { Text(designer.fullName, fontSize = 9.sp) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (editProjectTitle.trim().isNotEmpty() && editProjectDesc.trim().isNotEmpty()) {
                            viewModel.editProjectDetails(
                                projectId = proj.id,
                                title = editProjectTitle,
                                description = editProjectDesc,
                                assignedTo = editProjectAssignee
                            )
                            selectedProjectForEdit = null
                        }
                    }
                ) {
                    Text("Save Changes")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedProjectForEdit = null }) {
                    Text("Cancel", color = Color.Gray)
                }
            }
        )
    }

    // QUICK PHONE ALERT AND PROJECT ASSIGNMENT DIALOG
    if (showQuickAlertDialog) {
        AlertDialog(
            onDismissRequest = { showQuickAlertDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.NotificationsActive, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Send Phone Alert Update", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.heightIn(max = 350.dp).verticalScroll(rememberScrollState())) {
                    Text("Send an instant briefing notification alert directly to a member's phone. This can also assign/reassign the selected project.", fontSize = 11.sp, color = Color.Gray)
                    
                    OutlinedTextField(
                        value = alertTitle,
                        onValueChange = { alertTitle = it },
                        label = { Text("Notification Title") },
                        placeholder = { Text("e.g. Urgent Project Update!") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = alertMessage,
                        onValueChange = { alertMessage = it },
                        label = { Text("Brief message") },
                        placeholder = { Text("e.g. Please finish ASAP. ASIN #B08Z4WXZ4P added.") },
                        maxLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Select Project Reference (Adds to alert):", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                    if (projects.isEmpty()) {
                        Text("No active projects to select.", fontSize = 10.sp, color = Color.Gray)
                    } else {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.height(40.dp)) {
                            items(projects) { project ->
                                FilterChip(
                                    selected = alertSelectedProjectId == project.id,
                                    onClick = { 
                                        alertSelectedProjectId = if (alertSelectedProjectId == project.id) null else project.id
                                    },
                                    label = { Text(project.title, fontSize = 9.sp) }
                                )
                            }
                        }
                    }

                    Text("Select Target Designer / Member:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.height(40.dp)) {
                        items(designers.filter { it.role == "Team Member" }) { designer ->
                            FilterChip(
                                selected = alertSelectedMemberUsername == designer.username,
                                onClick = { 
                                        alertSelectedMemberUsername = if (alertSelectedMemberUsername == designer.username) "" else designer.username
                                },
                                label = { Text(designer.fullName, fontSize = 9.sp) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (alertTitle.trim().isNotEmpty() && alertMessage.trim().isNotEmpty() && alertSelectedMemberUsername.isNotEmpty()) {
                            val targetLabel = designers.find { it.username == alertSelectedMemberUsername }?.fullName ?: alertSelectedMemberUsername
                            
                            // Send direct notification
                            viewModel.sendDirectNotification(
                                title = alertTitle,
                                message = alertMessage,
                                targetUsername = alertSelectedMemberUsername
                            )

                            // If a project is selected as reference, assign it to them
                            if (alertSelectedProjectId != null) {
                                val proj = projects.find { it.id == alertSelectedProjectId }
                                if (proj != null) {
                                    viewModel.editProjectDetails(
                                        projectId = proj.id,
                                        title = proj.title,
                                        description = proj.description,
                                        assignedTo = alertSelectedMemberUsername
                                    )
                                }
                            }

                            showDirectAlertOverlayMessage = "Alert update sent successfully to $targetLabel's phone!"
                            showQuickAlertDialog = false
                            alertTitle = ""
                            alertMessage = ""
                            alertSelectedProjectId = null
                            alertSelectedMemberUsername = ""
                        }
                    }
                ) {
                    Text("Send Alert & Assign", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showQuickAlertDialog = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            }
        )
    }

    // Toast/Alert Overlay Dialog
    if (showDirectAlertOverlayMessage.isNotEmpty()) {
        AlertDialog(
            onDismissRequest = { showDirectAlertOverlayMessage = "" },
            icon = { Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(48.dp)) },
            title = { Text("Notification Dispatched", fontWeight = FontWeight.Bold, textAlign = TextAlign.Center) },
            text = {
                Text(
                    showDirectAlertOverlayMessage,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = { showDirectAlertOverlayMessage = "" },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                ) {
                    Text("OK", color = Color.White)
                }
            }
        )
    }
}

// -------------------------------------------------------------
// LEADERBOARD SCREEN: PROJECT COMPLETION RANKS & BADGE GALLERIES
@Composable
fun LeaderboardScreen(viewModel: TeamHubViewModel) {
    val users by viewModel.allUsers.collectAsState()
    
    // Member list sorted specifically by Performance Score or Completed Projects Count
    val sortedMembers = users
        .filter { it.role == "Team Member" }
        .sortedByDescending { it.completedProjectsCount }

    Column(modifier = Modifier.fillMaxSize()) {
        HeaderBrandBar(viewModel = viewModel)
        
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Text("Monthly Hub Leaderboard", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = MaterialTheme.colorScheme.secondary)
                Text("Operational rankings of team members based on validated design briefs count and score.", fontSize = 11.sp, color = Color.Gray)
                
                Spacer(modifier = Modifier.height(12.dp))
            }

            if (sortedMembers.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Text(
                            "No members are currently listed in directory.",
                            modifier = Modifier.padding(24.dp).fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }
            } else {
                items(sortedMembers.size) { index ->
                    val member = sortedMembers[index]
                    val rank = index + 1
                    
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.showProfile(member) },
                        colors = CardDefaults.cardColors(
                            containerColor = when (rank) {
                                1 -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                2 -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                                else -> Color.White
                            }
                        ),
                        border = BorderStroke(
                            1.dp,
                            when (rank) {
                                1 -> MaterialTheme.colorScheme.primary
                                else -> MaterialTheme.colorScheme.outline
                            }
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Rank Number Icon
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when (rank) {
                                            1 -> MaterialTheme.colorScheme.primary
                                            2 -> MaterialTheme.colorScheme.secondary
                                            else -> Color(0xFFE0E0E0)
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = rank.toString(),
                                    fontWeight = FontWeight.Bold,
                                    color = if (rank > 2) Color.DarkGray else Color.White,
                                    fontSize = 11.sp
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            ClickableAvatar(avatarId = member.avatarId, fullName = member.fullName, size = 44, onClick = { viewModel.showProfile(member) })

                            Spacer(modifier = Modifier.width(10.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(member.fullName, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.secondary)
                                Text(member.position, fontSize = 9.sp, color = Color.Gray)
                                
                                // Badge small icons showcase
                                if (member.earnedBadges.trim().isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                        member.earnedBadges.split(",").take(4).forEach { b ->
                                            Box(
                                                modifier = Modifier
                                                    .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(2.dp))
                                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                                            ) {
                                                Text(b.trim(), fontSize = 6.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text("${member.completedProjectsCount} Briefs", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = MaterialTheme.colorScheme.secondary)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.OfflineBolt, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text("Score: ${member.performanceScore}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// TEAM AREA: CHAT STREAM, RESOURCES AND ANNOUNCEMENTS MANAGEMENT
@Composable
fun TeamAreaScreen(viewModel: TeamHubViewModel) {
    val messages by viewModel.recentDiscussionMessages.collectAsState()
    val resources by viewModel.allResources.collectAsState()
    val announcements by viewModel.allAnnouncements.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val context = LocalContext.current

    var selectedSection by remember { mutableStateOf("talk") } // "talk", "resources", "announcements"
    var chatInput by remember { mutableStateOf("") }
    
    // Resource input fields
    var showResourceDialog by remember { mutableStateOf(false) }
    var resourceTitle by remember { mutableStateOf("") }
    var resourceCategory by remember { mutableStateOf("PSD Assets") }
    var resourceLink by remember { mutableStateOf("") }
    val categories = listOf("PSD Assets", "Vector Fonts", "Mockups", "Inspiration")

    // Announcement input fields
    var showAnnouncementDialog by remember { mutableStateOf(false) }
    var annTitle by remember { mutableStateOf("") }
    var annContent by remember { mutableStateOf("") }
    var annPriority by remember { mutableStateOf("Normal") }

    // Announcement editing variables
    var selectedAnnouncementForEdit by remember { mutableStateOf<Announcement?>(null) }
    var editAnnTitle by remember { mutableStateOf("") }
    var editAnnContent by remember { mutableStateOf("") }
    var editAnnPriority by remember { mutableStateOf("Normal") }

    // Private owner/admin chat channel variables
    var chatChannel by remember { mutableStateOf("group") } // "group" or "private"
    var activePrivateMember by remember { mutableStateOf("") } // selected member for private chat (Admin/Owner view)
    val adminUsernames = listOf("superadmin", "admin", "owner")

    Column(modifier = Modifier.fillMaxSize()) {
        HeaderBrandBar(viewModel = viewModel)
        
        // Multi section tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            SectionHeaderTab(title = "Discussion Board", active = selectedSection == "talk", modifier = Modifier.weight(1f)) {
                selectedSection = "talk"
            }
            SectionHeaderTab(title = "Resource Sharing", active = selectedSection == "resources", modifier = Modifier.weight(1f)) {
                selectedSection = "resources"
            }
            SectionHeaderTab(title = "Official Posts", active = selectedSection == "announcements", modifier = Modifier.weight(1f)) {
                selectedSection = "announcements"
            }
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (selectedSection) {
                "talk" -> {
                    // TAB/SEGMENT CONTROLS FOR GLOBAL VS PRIVATE
                    val users by viewModel.allUsers.collectAsState()
                    Column(modifier = Modifier.fillMaxSize()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .padding(horizontal = 16.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = chatChannel == "group",
                                onClick = { chatChannel = "group" },
                                label = { Text("Global Board", fontSize = 10.sp) }
                            )
                            FilterChip(
                                selected = chatChannel == "private",
                                onClick = { 
                                    chatChannel = "private" 
                                    if (currentUser?.role == "Team Member") {
                                        activePrivateMember = currentUser?.username ?: ""
                                    } else if (activePrivateMember.isEmpty()) {
                                        // Auto-select first team member for convenience if admin
                                        activePrivateMember = users.find { it.role == "Team Member" }?.username ?: ""
                                    }
                                },
                                label = { Text("DM Owner & Admin", fontSize = 10.sp) }
                            )
                        }

                        // Determine filtered conversation list
                        val filteredMessages = if (chatChannel == "group") {
                            messages.filter { it.recipient == "All" }
                        } else {
                            if (currentUser?.role == "Team Member") {
                                messages.filter {
                                    (it.recipient == "Admins" && it.author == currentUser?.username) ||
                                    (it.recipient == currentUser?.username && it.authorRole in listOf("Admin", "Super Admin"))
                                }
                            } else {
                                // Admin/Owner views messages to/from activePrivateMember
                                messages.filter {
                                    (it.author == activePrivateMember && it.recipient == "Admins") ||
                                    (it.recipient == activePrivateMember && it.authorRole in listOf("Admin", "Super Admin"))
                                }
                            }
                        }

                        // If admin in private mode: show member selector at the top!
                        if (chatChannel == "private" && (currentUser?.role == "Admin" || currentUser?.role == "Super Admin")) {
                            Text(
                                "Direct messaging channel: select member below to communicate",
                                fontSize = 9.sp,
                                color = Color.Gray,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                            )
                            LazyRow(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                items(users.filter { it.role == "Team Member" }) { designer ->
                                    FilterChip(
                                        selected = activePrivateMember == designer.username,
                                        onClick = { activePrivateMember = designer.username },
                                        label = { Text(designer.fullName, fontSize = 9.sp) }
                                    )
                                }
                            }
                        }

                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 16.dp),
                            contentPadding = PaddingValues(vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            if (chatChannel == "private" && activePrivateMember.isEmpty() && (currentUser?.role == "Admin" || currentUser?.role == "Super Admin")) {
                                item {
                                    Card(
                                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color.White),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                                    ) {
                                        Text(
                                            "No active members in the dm channel yet. Select a member from the directory list above.",
                                            modifier = Modifier.padding(24.dp).fillMaxWidth(),
                                            textAlign = TextAlign.Center,
                                            fontSize = 11.sp,
                                            color = Color.Gray
                                        )
                                    }
                                }
                            } else if (filteredMessages.isEmpty()) {
                                item {
                                    Card(
                                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color.White),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                                    ) {
                                        Text(
                                            if (chatChannel == "group") "Welcome to the global team discussion board! Post your comments below."
                                            else "Direct DM secure channel connected. Secure chat with management is active.",
                                            modifier = Modifier.padding(24.dp).fillMaxWidth(),
                                            textAlign = TextAlign.Center,
                                            fontSize = 11.sp,
                                            color = Color.Gray
                                        )
                                    }
                                }
                            } else {
                                items(filteredMessages) { msg ->
                                    val isMe = msg.author == currentUser?.username
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
                                    ) {
                                        Card(
                                            colors = CardDefaults.cardColors(
                                                containerColor = if (isMe) MaterialTheme.colorScheme.primaryContainer else Color.White
                                            ),
                                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                                            modifier = Modifier.widthIn(max = 280.dp)
                                        ) {
                                            Column(modifier = Modifier.padding(10.dp)) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Text(
                                                        text = if (isMe) "You" else msg.author,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 10.sp,
                                                        color = if (isMe) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                                                    )
                                                    Text(
                                                        text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(msg.timestamp)),
                                                        fontSize = 7.sp,
                                                        color = Color.Gray
                                                    )
                                                }
                                                Text(
                                                    text = msg.messageText,
                                                    fontSize = 11.sp,
                                                    color = MaterialTheme.colorScheme.secondary,
                                                    modifier = Modifier.padding(top = 4.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Input field Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surface)
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = chatInput,
                                onValueChange = { chatInput = it },
                                placeholder = { 
                                    Text(
                                        if (chatChannel == "group") "Post message on general wall..."
                                        else "Type private message to admin & owner..."
                                    )
                                },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            FloatingActionButton(
                                onClick = {
                                    if (chatInput.trim().isNotEmpty()) {
                                        if (chatChannel == "group") {
                                            viewModel.postDiscussionMessage(chatInput, recipient = "All")
                                        } else {
                                            if (currentUser?.role == "Team Member") {
                                                viewModel.postDiscussionMessage(chatInput, recipient = "Admins")
                                            } else {
                                                if (activePrivateMember.isNotEmpty()) {
                                                    viewModel.postDiscussionMessage(chatInput, recipient = activePrivateMember)
                                                }
                                            }
                                        }
                                        chatInput = ""
                                    }
                                },
                                containerColor = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(Icons.Filled.Send, contentDescription = "Send", tint = Color.White)
                            }
                        }
                    }
                }
                
                "resources" -> {
                    // RESOURCES SHARING
                    Column(modifier = Modifier.fillMaxSize()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Repository Layout Tools", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.Gray)
                            Button(
                                onClick = { showResourceDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                modifier = Modifier.height(30.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp)
                            ) {
                                Text("Add Asset Link", fontSize = 10.sp)
                            }
                        }

                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            if (resources.isEmpty()) {
                                item {
                                    Text(
                                        "No shared assets yet. Upload your packaging templates or links!",
                                        modifier = Modifier.fillMaxWidth().padding(40.dp),
                                        textAlign = TextAlign.Center,
                                        fontSize = 11.sp,
                                        color = Color.Gray
                                    )
                                }
                            } else {
                                items(resources) { res ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = Color.White),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = when (res.category) {
                                                        "PSD Assets" -> Icons.Filled.PhotoLibrary
                                                        "Vector Fonts" -> Icons.Filled.TextFields
                                                        "Mockups" -> Icons.Filled.WebAsset
                                                        else -> Icons.Filled.Attachment
                                                    },
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }

                                            Spacer(modifier = Modifier.width(10.dp))

                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(res.title, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
                                                Text("Shared By: ${res.sharedBy} • Category: ${res.category}", fontSize = 8.sp, color = Color.Gray)
                                            }

                                            Row {
                                                IconButton(onClick = {
                                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(res.link))
                                                    context.startActivity(intent)
                                                }) {
                                                    Icon(Icons.Filled.Download, contentDescription = "Download link", tint = MaterialTheme.colorScheme.primary)
                                                }
                                                if (currentUser?.role == "Admin" || currentUser?.role == "Super Admin") {
                                                    IconButton(onClick = { viewModel.deleteResource(res.id) }) {
                                                        Icon(Icons.Filled.DeleteOutline, contentDescription = "Delete", tint = Color.Red)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                "announcements" -> {
                    // OFFICIAL ANNOUNCEMENTS SCREEN WITH CREATION BUTTONS
                    Column(modifier = Modifier.fillMaxSize()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Official Operational Statements", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.Gray)
                            if (currentUser?.role == "Admin" || currentUser?.role == "Super Admin") {
                                Button(
                                    onClick = { showAnnouncementDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                    modifier = Modifier.height(30.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp)
                                ) {
                                    Text("New Post", fontSize = 10.sp)
                                }
                            }
                        }

                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(announcements) { ann ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardColors(
                                        containerColor = Color.White,
                                        contentColor = MaterialTheme.colorScheme.secondary,
                                        disabledContainerColor = Color.Gray,
                                        disabledContentColor = Color.LightGray
                                    ),
                                    border = BorderStroke(
                                        1.dp,
                                        if (ann.priority == "High") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                                    )
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                if (ann.priority == "High") {
                                                    Box(
                                                        modifier = Modifier
                                                            .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(4.dp))
                                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                                    ) {
                                                        Text("HIGH PRIORITY", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Black, fontSize = 7.sp)
                                                    }
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                }
                                                Text(ann.title, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = MaterialTheme.colorScheme.secondary)
                                            }
                                            if (currentUser?.role == "Admin" || currentUser?.role == "Super Admin") {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    IconButton(
                                                        onClick = {
                                                            selectedAnnouncementForEdit = ann
                                                            editAnnTitle = ann.title
                                                            editAnnContent = ann.content
                                                            editAnnPriority = ann.priority
                                                        },
                                                        modifier = Modifier.size(24.dp)
                                                    ) {
                                                        Icon(Icons.Filled.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                                                    }
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    IconButton(
                                                        onClick = { viewModel.removeAnnouncement(ann.id) },
                                                        modifier = Modifier.size(24.dp)
                                                    ) {
                                                        Icon(Icons.Filled.Clear, contentDescription = "Delete", tint = Color.Red, modifier = Modifier.size(14.dp))
                                                    }
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(ann.content, fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.85f))
                                        
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("By: ${ann.author} (${ann.authorRole})", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                            Text(
                                                text = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(ann.timestamp)),
                                                fontSize = 8.sp,
                                                color = Color.Gray
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // CREATE RESOURCES DIALOG
    if (showResourceDialog) {
        AlertDialog(
            onDismissRequest = { showResourceDialog = false },
            title = { Text("Publish Shared Asset link", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = resourceTitle,
                        onValueChange = { resourceTitle = it },
                        label = { Text("Title Asset") },
                        placeholder = { Text("e.g. Elegant Box Graphics PSD mock") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = resourceLink,
                        onValueChange = { resourceLink = it },
                        label = { Text("External URL link") },
                        placeholder = { Text("https://...") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Tool Category:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        categories.forEach { cat ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (resourceCategory == cat) MaterialTheme.colorScheme.primary else Color(0xFFF0F0F0))
                                    .clickable { resourceCategory = cat }
                                    .padding(vertical = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(cat.split(" ")[0], fontSize = 8.sp, color = if (resourceCategory == cat) Color.White else Color.DarkGray, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (resourceTitle.trim().isNotEmpty() && resourceLink.trim().isNotEmpty()) {
                            viewModel.shareResource(resourceTitle, resourceCategory, resourceLink)
                            showResourceDialog = false
                            resourceTitle = ""
                            resourceLink = ""
                        }
                    }
                ) {
                    Text("Publish Resource")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResourceDialog = false }) {
                    Text("Close", color = Color.Gray)
                }
            }
        )
    }

    // CREATE ANNOUNCEMENT DIALOG
    if (showAnnouncementDialog) {
        AlertDialog(
            onDismissRequest = { showAnnouncementDialog = false },
            title = { Text("Post Official announcement", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = annTitle,
                        onValueChange = { annTitle = it },
                        label = { Text("Announcement Title") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = annContent,
                        onValueChange = { annContent = it },
                        label = { Text("Content message text") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 4
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("High Priority Urgent?", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                        Switch(
                            checked = annPriority == "High",
                            onCheckedChange = { annPriority = if (it) "High" else "Normal" }
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (annTitle.trim().isNotEmpty() && annContent.trim().isNotEmpty()) {
                            viewModel.postAnnouncement(annTitle.trim(), annContent.trim(), annPriority)
                            showAnnouncementDialog = false
                            annTitle = ""
                            annContent = ""
                            annPriority = "Normal"
                        }
                    }
                ) {
                    Text("Post Announcement")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAnnouncementDialog = false }) {
                    Text("Close", color = Color.Gray)
                }
            }
        )
    }

    // EDIT ANNOUNCEMENT DIALOG
    if (selectedAnnouncementForEdit != null) {
        val original = selectedAnnouncementForEdit!!
        AlertDialog(
            onDismissRequest = { selectedAnnouncementForEdit = null },
            title = { Text("Edit Announcement Details", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = editAnnTitle,
                        onValueChange = { editAnnTitle = it },
                        label = { Text("Announcement Title") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = editAnnContent,
                        onValueChange = { editAnnContent = it },
                        label = { Text("Content msg text") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 4
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("High Priority Urgent?", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                        Switch(
                            checked = editAnnPriority == "High",
                            onCheckedChange = { editAnnPriority = if (it) "High" else "Normal" }
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (editAnnTitle.trim().isNotEmpty() && editAnnContent.trim().isNotEmpty()) {
                            viewModel.editAnnouncementDetails(
                                annId = original.id,
                                title = editAnnTitle,
                                content = editAnnContent,
                                priority = editAnnPriority
                            )
                            selectedAnnouncementForEdit = null
                        }
                    }
                ) {
                    Text("Save Changes")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedAnnouncementForEdit = null }) {
                    Text("Cancel", color = Color.Gray)
                }
            }
        )
    }
}

@Composable
fun SectionHeaderTab(title: String, active: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .clickable { onClick() }
            .background(if (active) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = if (active) MaterialTheme.colorScheme.primary else Color.Gray,
            textAlign = TextAlign.Center
        )
    }
}

// -------------------------------------------------------------
// TEAM DIRECTORY SCREEN: SEARCH, BADGE ASSIGNMENTS AND SUSPENSION
@Composable
fun DirectoryScreen(viewModel: TeamHubViewModel) {
    val users by viewModel.allUsers.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val context = LocalContext.current

    var searchInput by remember { mutableStateOf("") }
    
    // Admin controls profile viewing dialog
    var selectedMemberCard by remember { mutableStateOf<User?>(null) }
    var badgeDropdownExpanded by remember { mutableStateOf(false) }
    val projectBadges = listOf("Top Designer", "Fast Delivery", "Creative Expert", "Logo Specialist", "Thumbnail Master", "Social Media Expert", "Team Leader")

    // Admin stateful creation parameters of Admin
    var showCreateAdminDialog by remember { mutableStateOf(false) }
    var adminUserField by remember { mutableStateOf("") }
    var adminPassField by remember { mutableStateOf("") }
    var adminNameField by remember { mutableStateOf("") }
    var adminPosField by remember { mutableStateOf("") }
    var adminEmailField by remember { mutableStateOf("") }

    val filteredList = users.filter {
        it.fullName.contains(searchInput, ignoreCase = true) ||
        it.role.contains(searchInput, ignoreCase = true) ||
        it.position.contains(searchInput, ignoreCase = true) ||
        it.username.contains(searchInput, ignoreCase = true)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        HeaderBrandBar(viewModel = viewModel)
        
        Column(modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Team Directory", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = MaterialTheme.colorScheme.secondary)
                    Text("Search profiles, view badge credentials, and manage admin operational privileges.", fontSize = 11.sp, color = Color.Gray)
                }
                
                if (currentUser?.role == "Super Admin") {
                    Button(
                        onClick = { showCreateAdminDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.height(30.dp),
                        contentPadding = PaddingValues(horizontal = 6.dp)
                    ) {
                        Text("Add Admin", fontSize = 10.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = searchInput,
                onValueChange = { searchInput = it },
                placeholder = { Text("Search by name, role, skills...") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(filteredList) { member ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedMemberCard = member },
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ClickableAvatar(avatarId = member.avatarId, fullName = member.fullName, size = 48, onClick = { viewModel.showProfile(member) })
                        
                        Spacer(modifier = Modifier.width(10.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(member.fullName, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.secondary)
                                Spacer(modifier = Modifier.width(6.dp))
                                if (member.status == "Suspended") {
                                    Box(
                                        modifier = Modifier
                                            .background(Color.Red.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 5.dp, vertical = 1.dp)
                                    ) {
                                        Text("SUSPENDED", fontSize = 7.sp, fontWeight = FontWeight.ExtraBold, color = Color.Red)
                                    }
                                }
                            }
                            Text(member.position, fontSize = 9.sp, color = Color.Gray)
                            Text("Role: ${member.role}", fontSize = 8.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        }

                        // Badge icons representation
                        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                            if (member.earnedBadges.trim().isNotEmpty()) {
                                member.earnedBadges.split(",").take(3).forEach { b ->
                                    Box(
                                        modifier = Modifier
                                            .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(20.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(b.trim().take(4).uppercase(), fontSize = 6.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(Icons.Filled.ArrowForwardIos, contentDescription = "View detail card", modifier = Modifier.size(12.dp))
                    }
                }
            }
        }
    }

    // SEPARATE DIALOG CARD VISUALIZATION FOR EDITING ROLES, BADGES AND PRIVILEGES
    if (selectedMemberCard != null) {
        val member = selectedMemberCard!!
        val isSelf = member.username == currentUser?.username
        
        AlertDialog(
            onDismissRequest = { selectedMemberCard = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ClickableAvatar(avatarId = member.avatarId, fullName = member.fullName, size = 42, onClick = {
                        viewModel.showProfile(member)
                        selectedMemberCard = null
                    })
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(member.fullName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(member.position, fontSize = 9.sp, color = Color.Gray)
                    }
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Bio: ${member.bio.ifEmpty { "Creative specialist of DesignMastery_Lab." }}", fontSize = 11.sp, color = Color.DarkGray)
                    Text("Skills: ${member.skills.ifEmpty { "Design, Illustration" }}", fontSize = 11.sp, color = Color.DarkGray)
                    
                    if (member.contactInfo.isNotEmpty()) {
                        Text("Contact Outreach: ${member.contactInfo}", fontSize = 10.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                    Spacer(modifier = Modifier.height(6.dp))

                    Text("Badges Awarded: (${member.earnedBadges.split(",").map { it.trim() }.filter { it.isNotEmpty() }.size})", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    
                    // Show full badge details
                    if (member.earnedBadges.trim().isEmpty()) {
                        Text("No badges earned yet on profile.", fontSize = 10.sp, color = Color.Gray)
                    } else {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            items(member.earnedBadges.split(",").map { it.trim() }.filter { it.isNotEmpty() }) { b ->
                                Box(
                                    modifier = Modifier
                                        .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(20.dp))
                                        .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(b, fontSize = 8.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                        if (!isSelf && (currentUser?.role == "Admin" || currentUser?.role == "Super Admin")) {
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Icon(
                                                Icons.Filled.Cancel,
                                                contentDescription = "Remove badge",
                                                modifier = Modifier
                                                    .size(10.dp)
                                                    .clickable {
                                                        viewModel.removeBadge(member.username, b)
                                                        selectedMemberCard = selectedMemberCard?.copy(
                                                            earnedBadges = selectedMemberCard!!.earnedBadges
                                                                .split(",")
                                                                .map { it.trim() }
                                                                .filter { it != b }
                                                                .joinToString(", ")
                                                        )
                                                    },
                                                tint = Color.Red
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // ADMIN SPECIFIC BADGE MANAGEMENT DROPDOWNS
                    if (!isSelf && (currentUser?.role == "Admin" || currentUser?.role == "Super Admin") && member.role == "Team Member") {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("Assign Badge directly:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                        
                        Box(modifier = Modifier.fillMaxWidth()) {
                            Button(
                                onClick = { badgeDropdownExpanded = true },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                modifier = Modifier.height(28.dp)
                            ) {
                                Text("Choose Brand Badge", fontSize = 10.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            }
                            DropdownMenu(
                                expanded = badgeDropdownExpanded,
                                onDismissRequest = { badgeDropdownExpanded = false }
                            ) {
                                projectBadges.forEach { badge ->
                                    DropdownMenuItem(
                                        text = { Text(badge, fontSize = 11.sp) },
                                        onClick = {
                                            viewModel.awardBadge(member.username, badge)
                                            badgeDropdownExpanded = false
                                            
                                            val currentList = member.earnedBadges.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toMutableList()
                                            if (!currentList.contains(badge)) {
                                                currentList.add(badge)
                                                selectedMemberCard = selectedMemberCard?.copy(earnedBadges = currentList.joinToString(", "))
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // ADMIND SUSPEND SYSTEM
                    if (!isSelf && (currentUser?.role == "Admin" || currentUser?.role == "Super Admin")) {
                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Privilege Management", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Red)
                            
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                // Super Admin can delete any other user account (owner privileges)
                                if (currentUser?.role == "Super Admin" && !isSelf) {
                                    Button(
                                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                                        onClick = {
                                            viewModel.deleteAdminAccount(member)
                                            selectedMemberCard = null
                                        },
                                        modifier = Modifier.height(28.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp)
                                    ) {
                                        Text("Delete Profile", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                // Admins and superadmins can suspend Team Members
                                if (member.role == "Team Member") {
                                    val isSuspended = member.status == "Suspended"
                                    Button(
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isSuspended) Color(0xFF388E3C) else Color.Red
                                        ),
                                        onClick = {
                                            viewModel.toggleSuspendMember(member)
                                            selectedMemberCard = selectedMemberCard?.copy(status = if (isSuspended) "Active" else "Suspended")
                                        },
                                        modifier = Modifier.height(28.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp)
                                    ) {
                                        Text(if (isSuspended) "Activate" else "Suspend", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedMemberCard = null }) {
                    Text("Close", color = Color.Gray)
                }
            }
        )
    }

    // SUPERADMIN CREATE ADMIN ACCOUNTS DIALOG
    if (showCreateAdminDialog) {
        AlertDialog(
            onDismissRequest = { showCreateAdminDialog = false },
            title = { Text("Generate Admin Operational credentials", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = adminUserField,
                        onValueChange = { adminUserField = it },
                        label = { Text("Admin Username ID") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = adminPassField,
                        onValueChange = { adminPassField = it },
                        label = { Text("Temporary Password") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = adminNameField,
                        onValueChange = { adminNameField = it },
                        label = { Text("Admin Full Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = adminPosField,
                        onValueChange = { adminPosField = it },
                        label = { Text("Admin Position (e.g. Lead, QA Analyst)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = adminEmailField,
                        onValueChange = { adminEmailField = it },
                        label = { Text("Outreach Email contact") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (adminUserField.trim().isNotEmpty() && adminPassField.trim().isNotEmpty() && adminNameField.trim().isNotEmpty()) {
                            viewModel.createAdminAccount(
                                adminUsername = adminUserField,
                                adminP = adminPassField,
                                fullName = adminNameField,
                                position = adminPosField,
                                contactInfo = adminEmailField
                            )
                            showCreateAdminDialog = false
                            adminUserField = ""
                            adminPassField = ""
                            adminNameField = ""
                            adminPosField = ""
                            adminEmailField = ""
                        }
                    }
                ) {
                    Text("Deploys Admin Panel")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateAdminDialog = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            }
        )
    }
}

// -------------------------------------------------------------
// PROFILE MANAGEMENT VIEW: EDIT SELF BIOGRAPHY, CONTACTS, PORTFOLIOS
@Composable
fun ProfileScreen(viewModel: TeamHubViewModel) {
    val currentUser by viewModel.currentUser.collectAsState()
    val context = LocalContext.current

    var editMode by remember { mutableStateOf(false) }
    var changePasswordMode by remember { mutableStateOf(false) }

    // State profile parameters
    val user = currentUser ?: return
    var nameField by remember(user) { mutableStateOf(user.fullName) }
    var bioField by remember(user) { mutableStateOf(user.bio) }
    var skillsField by remember(user) { mutableStateOf(user.skills) }
    var portfolioField by remember(user) { mutableStateOf(user.portfolioLinks) }
    var contactField by remember(user) { mutableStateOf(user.contactInfo) }
    
    // Social field parameters
    var instaField by remember(user) { mutableStateOf(user.instagram) }
    var fbField by remember(user) { mutableStateOf(user.facebook) }
    var waField by remember(user) { mutableStateOf(user.whatsapp) }
    var ytField by remember(user) { mutableStateOf(user.youtube) }
    var liField by remember(user) { mutableStateOf(user.linkedin) }
    var webField by remember(user) { mutableStateOf(user.website) }
    
    // Password change fields
    var oldPass by remember { mutableStateOf("") }
    var newPass by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {
        HeaderBrandBar(
            viewModel = viewModel,
            actionIcon = if (editMode) Icons.Filled.Save else Icons.Filled.Edit,
            actionDescription = if (editMode) "Save" else "Edit profile",
            onActionClick = {
                if (editMode) {
                    viewModel.updateProfile(
                        fullName = nameField,
                        bio = bioField,
                        skills = skillsField,
                        portfolioLinks = portfolioField,
                        instagram = instaField,
                        facebook = fbField,
                        whatsapp = waField,
                        youtube = ytField,
                        linkedin = liField,
                        website = webField,
                        contactInfo = contactField,
                        avatarId = user.avatarId
                    )
                    editMode = false
                } else {
                    editMode = true
                }
            }
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Workspace Profile Card", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = MaterialTheme.colorScheme.secondary)
                    
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        TextButton(
                            onClick = { changePasswordMode = true },
                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Filled.LockReset, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Reset my Password", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Visual Card wrapper of member details
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            ClickableAvatar(avatarId = user.avatarId, fullName = user.fullName, size = 64, onClick = { viewModel.showProfile(user) })
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(user.fullName, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = MaterialTheme.colorScheme.secondary)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(4.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(user.position, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Status: ${user.status}", fontSize = 10.sp, color = Color.Gray)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                        Spacer(modifier = Modifier.height(16.dp))

                        if (!editMode) {
                            // View details mode
                            Text("About Me Bio:", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                            Text(user.bio.ifEmpty { "No portfolio biography shared yet. Tap Edit above to customize card!" }, fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
                            
                            Spacer(modifier = Modifier.height(10.dp))
                            
                            Text("Core Skills:", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                            if (user.skills.trim().isEmpty()) {
                                Text("No skills listed.", fontSize = 11.sp, color = Color.Gray)
                            } else {
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.padding(top = 4.dp)) {
                                    user.skills.split(",").forEach { s ->
                                        Box(
                                            modifier = Modifier
                                                .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(20.dp))
                                                .padding(horizontal = 8.dp, vertical = 3.dp)
                                        ) {
                                            Text(s.trim(), fontSize = 9.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text("Portfolio Showcases:", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                            if (user.portfolioLinks.trim().isEmpty()) {
                                Text("No showcases files/links yet.", fontSize = 11.sp, color = Color.Gray)
                            } else {
                                user.portfolioLinks.split(",").forEach { link ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link.trim()))
                                                context.startActivity(intent)
                                            }
                                            .padding(vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Filled.Link, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(link.trim(), fontSize = 11.sp, color = MaterialTheme.colorScheme.primary, textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    }
                                }
                            }
                        } else {
                            // Edit detailed inputs mode
                            OutlinedTextField(
                                value = nameField,
                                onValueChange = { nameField = it },
                                label = { Text("Display Name") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = bioField,
                                onValueChange = { bioField = it },
                                label = { Text("Professional Biography") },
                                maxLines = 3,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = skillsField,
                                onValueChange = { skillsField = it },
                                label = { Text("Skills Deck (Comma separated)") },
                                placeholder = { Text("Logo Specialist, Packaging, Vector Art...") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = portfolioField,
                                onValueChange = { portfolioField = it },
                                label = { Text("Portfolio Showcases (Comma separated URLs)") },
                                placeholder = { Text("https://behance.net/username, https://dribbble.com...") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = contactField,
                                onValueChange = { contactField = it },
                                label = { Text("Client Outreach Email") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(10.dp))
                            Text("Hub Contact Card Networks:", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                            
                            OutlinedTextField(
                                value = waField,
                                onValueChange = { waField = it },
                                label = { Text("WhatsApp Contact (numbers only)") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = webField,
                                onValueChange = { webField = it },
                                label = { Text("Director Portfolio Website") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = instaField,
                                onValueChange = { instaField = it },
                                label = { Text("Instagram Account URL keyword") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = fbField,
                                onValueChange = { fbField = it },
                                label = { Text("Facebook Account URL keyword") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = ytField,
                                onValueChange = { ytField = it },
                                label = { Text("YouTube URL channel handle") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = liField,
                                onValueChange = { liField = it },
                                label = { Text("LinkedIn handle") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }

    // CHANGE PASSWORD SECURITY DIALOG
    if (changePasswordMode) {
        AlertDialog(
            onDismissRequest = { changePasswordMode = false },
            title = { Text("Update security credentials", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Only Members, Admins and SuperAdmins can manage their corresponding local secret credential parameters", fontSize = 11.sp, color = Color.Gray)
                    
                    OutlinedTextField(
                        value = oldPass,
                        onValueChange = { oldPass = it },
                        label = { Text("Current Password") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newPass,
                        onValueChange = { newPass = it },
                        label = { Text("New Security Password") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (oldPass.isNotEmpty() && newPass.isNotEmpty()) {
                            viewModel.changePassword(oldPass, newPass) { success, msg ->
                                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                if (success) {
                                    changePasswordMode = false
                                    oldPass = ""
                                    newPass = ""
                                }
                            }
                        }
                    }
                ) {
                    Text("Deploys Update")
                }
            },
            dismissButton = {
                TextButton(onClick = { changePasswordMode = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            }
        )
    }
}
