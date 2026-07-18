package com.example.ui

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.*
import java.text.SimpleDateFormat
import java.util.*

// Brand Color Palette fallbacks
val PrimaryGreen = Color(0xFF1E5631)
val BrandGold = Color(0xFFC89B3C)
val BrandCream = Color(0xFFF8F4E9)
val BrandWhite = Color(0xFFFFFFFF)
val DarkCharcoal = Color(0xFF1C1C1C)
val MutedCharcoal = Color(0xFF4A4A4A)

fun parseHexColor(hex: String, default: Color = Color.Gray): Color {
    return try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (e: Exception) {
        default
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: LmsViewModel) {
    val context = LocalContext.current
    
    // Branding states from viewmodel
    val dynamicMotto by viewModel.academyMotto.collectAsStateWithLifecycle()
    val dynamicPrimaryHex by viewModel.primaryColor.collectAsStateWithLifecycle()
    val dynamicGoldHex by viewModel.brandGoldColor.collectAsStateWithLifecycle()
    
    val dynamicPrimary = parseHexColor(dynamicPrimaryHex, PrimaryGreen)
    val dynamicGold = parseHexColor(dynamicGoldHex, BrandGold)
    
    // Selected role
    val currentRole by viewModel.currentRole.collectAsStateWithLifecycle()
    
    // Database flows
    val courses by viewModel.courses.collectAsStateWithLifecycle()
    val assignments by viewModel.assignments.collectAsStateWithLifecycle()
    val users by viewModel.users.collectAsStateWithLifecycle()
    val quizzes by viewModel.quizzes.collectAsStateWithLifecycle()
    val announcements by viewModel.announcements.collectAsStateWithLifecycle()
    val virtualClasses by viewModel.virtualClasses.collectAsStateWithLifecycle()
    val payments by viewModel.payments.collectAsStateWithLifecycle()
    val placements by viewModel.placements.collectAsStateWithLifecycle()
    val attendance by viewModel.attendance.collectAsStateWithLifecycle()
    val resources by viewModel.resources.collectAsStateWithLifecycle()
    val backups by viewModel.backups.collectAsStateWithLifecycle()
    val customRoles by viewModel.customRoles.collectAsStateWithLifecycle()
    
    val courseModules by viewModel.courseModules.collectAsStateWithLifecycle()
    val courseLessons by viewModel.courseLessons.collectAsStateWithLifecycle()
    val courseResourceItems by viewModel.courseResourceItems.collectAsStateWithLifecycle()
    val courseMockExams by viewModel.courseMockExams.collectAsStateWithLifecycle()
    val courseDiscussionTopics by viewModel.courseDiscussionTopics.collectAsStateWithLifecycle()
    val courseCertificateConfigs by viewModel.courseCertificateConfigs.collectAsStateWithLifecycle()
    
    // Screen tabs depending on role
    var selectedTab by remember(currentRole) { mutableStateOf(0) }
    var showRoleSelector by remember { mutableStateOf(false) }
    
    // Backup restoring simulation progress state
    var restoringBackup by remember { mutableStateOf<BackupRecord?>(null) }
    var backupProgress by remember { mutableFloatStateOf(0f) }
    
    LaunchedEffect(restoringBackup) {
        if (restoringBackup != null) {
            backupProgress = 0f
            while (backupProgress < 1f) {
                kotlinx.coroutines.delay(100)
                backupProgress += 0.1f
            }
            viewModel.restoreBackup(restoringBackup!!)
            Toast.makeText(context, "Backup successfully restored and re-indexed!", Toast.LENGTH_LONG).show()
            restoringBackup = null
        }
    }
    
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = BrandCream,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "ROOTED & READY ACADEMY",
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp,
                            color = dynamicPrimary,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = dynamicMotto,
                            fontSize = 9.sp,
                            color = dynamicGold,
                            fontStyle = FontStyle.Italic,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                actions = {
                    Box {
                        IconButton(
                            onClick = { showRoleSelector = true },
                            modifier = Modifier.testTag("role_selector_trigger")
                        ) {
                            Icon(Icons.Default.ManageAccounts, "Switch Role", tint = dynamicPrimary)
                        }
                        
                        DropdownMenu(
                            expanded = showRoleSelector,
                            onDismissRequest = { showRoleSelector = false }
                        ) {
                            Text(
                                text = "SIMULATE ROLE DASHBOARD",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = dynamicGold,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                            HorizontalDivider()
                            listOf(
                                "Super Administrator (Founder)",
                                "Administrator",
                                "Facilitator",
                                "Corporate Client",
                                "Learner",
                                "Recruitment Partner"
                            ).forEach { role ->
                                DropdownMenuItem(
                                    text = { 
                                        Text(
                                            text = role,
                                            fontWeight = if (currentRole == role) FontWeight.Bold else FontWeight.Normal,
                                            color = if (currentRole == role) dynamicPrimary else DarkCharcoal
                                        ) 
                                    },
                                    onClick = {
                                        viewModel.setRole(role)
                                        showRoleSelector = false
                                        Toast.makeText(context, "Switched to $role dashboard", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = BrandWhite),
                modifier = Modifier.border(1.dp, dynamicGold.copy(alpha = 0.2f))
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = BrandWhite,
                tonalElevation = 8.dp,
                modifier = Modifier.border(1.dp, dynamicGold.copy(alpha = 0.1f))
            ) {
                val tabs = getTabsForRole(currentRole)
                tabs.forEachIndexed { index, pair ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        icon = { Icon(if (selectedTab == index) pair.second else pair.third, contentDescription = pair.first) },
                        label = { Text(pair.first, maxLines = 1, fontSize = 10.sp, fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = dynamicPrimary,
                            selectedTextColor = dynamicPrimary,
                            indicatorColor = dynamicGold.copy(alpha = 0.15f),
                            unselectedIconColor = MutedCharcoal,
                            unselectedTextColor = MutedCharcoal
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(BrandCream)
        ) {
            // Restore backup loading modal overlay
            if (restoringBackup != null) {
                Dialog(onDismissRequest = {}) {
                    Card(
                        modifier = Modifier.padding(24.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = BrandWhite)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(progress = { backupProgress }, color = dynamicPrimary)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("RESTORING ACADEMY DATABASE", fontWeight = FontWeight.Black, fontSize = 14.sp, color = dynamicPrimary)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Rebuilding tables from snapshot: ${restoringBackup?.fileName}", fontSize = 12.sp, color = MutedCharcoal, textAlign = TextAlign.Center)
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 800.dp)
                    .align(Alignment.TopCenter)
            ) {
                // Role Badge Indicator
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    colors = CardDefaults.cardColors(containerColor = dynamicPrimary.copy(alpha = 0.08f)),
                    border = BorderStroke(1.dp, dynamicGold.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.VerifiedUser, "Role", tint = dynamicGold, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "AUTHORIZED ACCESS LEVEL",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = dynamicGold,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = currentRole,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                color = dynamicPrimary
                            )
                        }
                    }
                }

                // Main Dashboard Body
                Box(modifier = Modifier.weight(1f)) {
                    RenderDashboardBody(
                        currentRole = currentRole,
                        tabIndex = selectedTab,
                        viewModel = viewModel,
                        courses = courses,
                        assignments = assignments,
                        users = users,
                        quizzes = quizzes,
                        announcements = announcements,
                        virtualClasses = virtualClasses,
                        payments = payments,
                        placements = placements,
                        attendance = attendance,
                        resources = resources,
                        backups = backups,
                        customRoles = customRoles,
                        dynamicPrimary = dynamicPrimary,
                        dynamicGold = dynamicGold,
                        onRestoreBackup = { restoringBackup = it }
                    )
                }
            }
        }
    }
}

private fun getTabsForRole(role: String): List<Triple<String, ImageVector, ImageVector>> {
    return when (role) {
        "Super Administrator (Founder)", "Administrator" -> listOf(
            Triple("Control Room", Icons.Default.AdminPanelSettings, Icons.Outlined.AdminPanelSettings),
            Triple("Academics", Icons.Default.School, Icons.Outlined.School),
            Triple("Operations", Icons.Default.TrendingUp, Icons.Outlined.TrendingUp)
        )
        "Facilitator" -> listOf(
            Triple("Class Studio", Icons.Default.CoPresent, Icons.Outlined.CoPresent),
            Triple("Student Logs", Icons.Default.Assessment, Icons.Outlined.Assessment)
        )
        "Corporate Client" -> listOf(
            Triple("Enterprise Portal", Icons.Default.Business, Icons.Outlined.Business),
            Triple("Audit & Reports", Icons.Default.Description, Icons.Outlined.Description),
            Triple("Finances & Jobs", Icons.Default.Payments, Icons.Outlined.Payments)
        )
        "Learner" -> listOf(
            Triple("Dashboard", Icons.Default.Dashboard, Icons.Outlined.Dashboard),
            Triple("My Syllabus", Icons.Default.Book, Icons.Outlined.Book),
            Triple("Schedule & Grade", Icons.Default.EventNote, Icons.Outlined.EventNote)
        )
        "Recruitment Partner" -> listOf(
            Triple("Find Talent", Icons.Default.Group, Icons.Outlined.Group),
            Triple("Hiring Pipeline", Icons.Default.Work, Icons.Outlined.WorkOutline)
        )
        else -> listOf(Triple("Dashboard", Icons.Default.Dashboard, Icons.Outlined.Dashboard))
    }
}

@Composable
fun RenderDashboardBody(
    currentRole: String,
    tabIndex: Int,
    viewModel: LmsViewModel,
    courses: List<Course>,
    assignments: List<Assignment>,
    users: List<UserAccount>,
    quizzes: List<Quiz>,
    announcements: List<Announcement>,
    virtualClasses: List<VirtualClass>,
    payments: List<Payment>,
    placements: List<Placement>,
    attendance: List<AttendanceRecord>,
    resources: List<ResourceMedia>,
    backups: List<BackupRecord>,
    customRoles: List<CustomRole>,
    dynamicPrimary: Color,
    dynamicGold: Color,
    onRestoreBackup: (BackupRecord) -> Unit
) {
    when (currentRole) {
        "Super Administrator (Founder)", "Administrator" -> {
            when (tabIndex) {
                0 -> ControlRoomTab(viewModel, users, backups, customRoles, currentRole, dynamicPrimary, dynamicGold, onRestoreBackup)
                1 -> AcademicsAdminTab(viewModel, courses, quizzes, resources, dynamicPrimary, dynamicGold)
                2 -> OperationsTab(viewModel, users, payments, placements, attendance, dynamicPrimary, dynamicGold)
            }
        }
        "Facilitator" -> {
            when (tabIndex) {
                0 -> FacilitatorClassStudioTab(viewModel, courses, resources, quizzes, virtualClasses, dynamicPrimary, dynamicGold)
                1 -> FacilitatorStudentLogsTab(viewModel, users, attendance, dynamicPrimary, dynamicGold)
            }
        }
        "Corporate Client" -> {
            when (tabIndex) {
                0 -> CorporateClientCohortTab(viewModel, users, dynamicPrimary, dynamicGold)
                1 -> CorporateAuditAndReportsTab(viewModel, users, dynamicPrimary, dynamicGold)
                2 -> CorporateBillingAndHiringTab(viewModel, payments, placements, dynamicPrimary, dynamicGold)
            }
        }
        "Learner" -> {
            when (tabIndex) {
                0 -> LearnerDashboardTab(viewModel, courses, assignments, announcements, virtualClasses, resources, quizzes, users, dynamicPrimary, dynamicGold)
                1 -> LearnerSyllabusTab(courses, resources, quizzes, dynamicPrimary, dynamicGold)
                2 -> LearnerScheduleGradeTab(viewModel, courses, assignments, virtualClasses, attendance, users, dynamicPrimary, dynamicGold)
            }
        }
        "Recruitment Partner" -> {
            when (tabIndex) {
                0 -> RecruitmentTalentTab(users, dynamicPrimary, dynamicGold)
                1 -> RecruitmentPlacementTab(viewModel, placements, dynamicPrimary, dynamicGold)
            }
        }
    }
}

// =======================================================
// CONTROL ROOM TAB (ADMIN / SUPER ADMIN)
// =======================================================
@Composable
fun ControlRoomTab(
    viewModel: LmsViewModel,
    users: List<UserAccount>,
    backups: List<BackupRecord>,
    customRoles: List<CustomRole>,
    activeRole: String,
    dynamicPrimary: Color,
    dynamicGold: Color,
    onRestoreBackup: (BackupRecord) -> Unit
) {
    var showAddUser by remember { mutableStateOf(false) }
    var showCreateRole by remember { mutableStateOf(false) }
    var showBranding by remember { mutableStateOf(false) }
    var showSecuritySettings by remember { mutableStateOf(false) }
    
    // Form States
    var newUserName by remember { mutableStateOf("") }
    var newUserEmail by remember { mutableStateOf("") }
    var newUserRole by remember { mutableStateOf("Learner") }
    
    var roleName by remember { mutableStateOf("") }
    var roleDesc by remember { mutableStateOf("") }
    var permissionCount by remember { mutableIntStateOf(3) }
    
    var brandMotto by remember { mutableStateOf("Growing Roses from Concrete.") }
    var brandPrimaryHex by remember { mutableStateOf("#1E5631") }
    var brandGoldHex by remember { mutableStateOf("#C89B3C") }
    
    var notifyEnabled by remember { mutableStateOf(true) }
    var maintMode by remember { mutableStateOf(false) }
    var secLevel by remember { mutableStateOf("High") }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // System Header
        item {
            Text(
                text = "System Administration Matrix",
                fontWeight = FontWeight.Black,
                fontSize = 18.sp,
                color = DarkCharcoal
            )
        }

        // Section: Users Accounts Manager
        item {
            ControlRoomSectionCard(title = "User Directory & Access Control", icon = Icons.Default.People, dynamicPrimary) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Active Accounts (${users.size})", fontWeight = FontWeight.Bold, color = DarkCharcoal)
                        Button(
                            onClick = { showAddUser = !showAddUser },
                            colors = ButtonDefaults.buttonColors(containerColor = dynamicPrimary),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(if (showAddUser) "Collapse" else "Create User")
                        }
                    }
                    
                    if (showAddUser) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = BrandCream),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            border = BorderStroke(1.dp, dynamicGold.copy(alpha = 0.5f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("New User Credentials", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = dynamicPrimary)
                                OutlinedTextField(
                                    value = newUserName,
                                    onValueChange = { newUserName = it },
                                    label = { Text("Full Name") },
                                    modifier = Modifier.fillMaxWidth().testTag("input_user_name")
                                )
                                OutlinedTextField(
                                    value = newUserEmail,
                                    onValueChange = { newUserEmail = it },
                                    label = { Text("Email Address") },
                                    modifier = Modifier.fillMaxWidth().testTag("input_user_email")
                                )
                                Text("Assign Access Role:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MutedCharcoal)
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    listOf("Learner", "Facilitator", "Corporate Client", "Recruitment Partner").forEach { role ->
                                        ElevatedFilterChip(
                                            selected = newUserRole == role,
                                            onClick = { newUserRole = role },
                                            label = { Text(role, fontSize = 10.sp) }
                                        )
                                    }
                                }
                                Button(
                                    onClick = {
                                        if (newUserName.isNotBlank() && newUserEmail.isNotBlank()) {
                                            viewModel.createUser(newUserName, newUserEmail, newUserRole)
                                            newUserName = ""
                                            newUserEmail = ""
                                            showAddUser = false
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = dynamicPrimary),
                                    modifier = Modifier.fillMaxWidth().testTag("btn_save_user")
                                ) {
                                    Text("Authorize Account Creation", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // Render users list
                    users.forEach { user ->
                        val isSuspended = user.status == "Suspended"
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = if (isSuspended) Color(0xFFFFF2F2) else BrandWhite),
                            border = BorderStroke(1.dp, if (isSuspended) Color.Red.copy(alpha = 0.3f) else dynamicGold.copy(alpha = 0.2f))
                        ) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(user.name, fontWeight = FontWeight.Bold, color = if (isSuspended) Color.Red else DarkCharcoal)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        if (isSuspended) {
                                            Badge(containerColor = Color.Red, contentColor = BrandWhite) { Text("SUSPENDED") }
                                        }
                                    }
                                    Text("${user.email} • ${user.role}", fontSize = 11.sp, color = MutedCharcoal)
                                    Text("Cohort: ${user.cohort}", fontSize = 10.sp, fontStyle = FontStyle.Italic, color = dynamicGold)
                                }
                                IconButton(onClick = { viewModel.toggleSuspendUser(user) }) {
                                    Icon(
                                        imageVector = if (isSuspended) Icons.Default.PlayArrow else Icons.Default.Block,
                                        contentDescription = "Toggle Suspend",
                                        tint = if (isSuspended) PrimaryGreen else Color.Red
                                    )
                                }
                                IconButton(onClick = { viewModel.deleteUser(user) }) {
                                    Icon(Icons.Default.Delete, "Delete", tint = Color.Gray)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section: System Branding Customizer
        item {
            ControlRoomSectionCard(title = "Global Branding & Corporate Visuals", icon = Icons.Default.Palette, dynamicPrimary) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Live-modifying branding fields instantaneously updates the academy portal experience globally for all participants.", fontSize = 11.sp, color = MutedCharcoal)
                    Button(
                        onClick = { showBranding = !showBranding },
                        colors = ButtonDefaults.buttonColors(containerColor = dynamicPrimary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (showBranding) "Lock Brand Configuration" else "Configure Palette & Motto")
                    }
                    if (showBranding) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 4.dp)) {
                            OutlinedTextField(
                                value = brandMotto,
                                onValueChange = { brandMotto = it },
                                label = { Text("Corporate Academy Motto") },
                                modifier = Modifier.fillMaxWidth().testTag("input_brand_motto")
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = brandPrimaryHex,
                                    onValueChange = { brandPrimaryHex = it },
                                    label = { Text("Primary Hex Color") },
                                    modifier = Modifier.weight(1f).testTag("input_brand_primary")
                                )
                                OutlinedTextField(
                                    value = brandGoldHex,
                                    onValueChange = { brandGoldHex = it },
                                    label = { Text("Classic Gold Hex") },
                                    modifier = Modifier.weight(1f).testTag("input_brand_gold")
                                )
                            }
                            Button(
                                onClick = {
                                    viewModel.updateBranding(brandMotto, brandPrimaryHex, brandGoldHex)
                                    Toast.makeText(viewModel.getApplication(), "Branding variables redeployed!", Toast.LENGTH_SHORT).show()
                                    showBranding = false
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = dynamicPrimary),
                                modifier = Modifier.fillMaxWidth().testTag("btn_save_brand")
                            ) {
                                Text("Publish Visual Upgrades")
                            }
                        }
                    }
                }
            }
        }

        // Section: Create Custom Roles
        item {
            ControlRoomSectionCard(title = "Custom Security Roles Builder", icon = Icons.Default.Security, dynamicPrimary) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Active Roles: ${customRoles.size + 6}", fontWeight = FontWeight.Bold, color = DarkCharcoal)
                        Button(
                            onClick = { showCreateRole = !showCreateRole },
                            colors = ButtonDefaults.buttonColors(containerColor = dynamicPrimary)
                        ) {
                            Text(if (showCreateRole) "Collapse" else "Create New Role")
                        }
                    }
                    if (showCreateRole) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = BrandCream),
                            border = BorderStroke(1.dp, dynamicGold.copy(alpha = 0.5f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = roleName,
                                    onValueChange = { roleName = it },
                                    label = { Text("Role Designation Title") },
                                    modifier = Modifier.fillMaxWidth().testTag("input_role_name")
                                )
                                OutlinedTextField(
                                    value = roleDesc,
                                    onValueChange = { roleDesc = it },
                                    label = { Text("Permissions & Capability Scope") },
                                    modifier = Modifier.fillMaxWidth().testTag("input_role_desc")
                                )
                                Text("Granular Permissions Count: $permissionCount", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Slider(
                                    value = permissionCount.toFloat(),
                                    onValueChange = { permissionCount = it.toInt() },
                                    valueRange = 1f..15f,
                                    colors = SliderDefaults.colors(thumbColor = dynamicGold, activeTrackColor = dynamicPrimary)
                                )
                                Button(
                                    onClick = {
                                        if (roleName.isNotBlank() && roleDesc.isNotBlank()) {
                                            viewModel.createCustomRole(roleName, permissionCount, roleDesc)
                                            roleName = ""
                                            roleDesc = ""
                                            showCreateRole = false
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = dynamicPrimary),
                                    modifier = Modifier.fillMaxWidth().testTag("btn_save_role")
                                ) {
                                    Text("Compile New System Role")
                                }
                            }
                        }
                    }

                    customRoles.forEach { r ->
                        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = BrandWhite)) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(r.name, fontWeight = FontWeight.Bold, color = dynamicPrimary)
                                    Text(r.description, fontSize = 11.sp, color = MutedCharcoal)
                                }
                                Badge(containerColor = dynamicGold) { Text("${r.permissionsCount} Policies") }
                                IconButton(onClick = { viewModel.deleteCustomRole(r) }) {
                                    Icon(Icons.Default.Delete, "Delete", tint = Color.Gray)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section: Security Settings (Super Admin Founder exclusive, locked for normal admins)
        item {
            val isFounder = activeRole == "Super Administrator (Founder)"
            ControlRoomSectionCard(
                title = "Global Security Settings",
                icon = Icons.Default.Lock,
                headerColor = if (isFounder) dynamicPrimary else Color.Gray
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (!isFounder) {
                        Box(
                            modifier = Modifier.fillMaxWidth().background(Color(0xFFFFF2F2)).border(1.dp, Color.Red.copy(alpha = 0.2f)).padding(10.dp)
                        ) {
                            Text("SECURITY NOTICE: System settings modifications strictly require Founder (Super Administrator) verification access clearance levels.", color = Color.Red, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Button(
                        onClick = { showSecuritySettings = !showSecuritySettings },
                        colors = ButtonDefaults.buttonColors(containerColor = if (isFounder) dynamicPrimary else Color.Gray),
                        enabled = isFounder
                    ) {
                        Text(if (showSecuritySettings) "Lock System Configuration" else "Access Security Parameters")
                    }
                    if (showSecuritySettings && isFounder) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(checked = notifyEnabled, onCheckedChange = { notifyEnabled = it })
                                Text("Deliver Continuous Notifications (Email/SMS Alerts)", fontSize = 12.sp)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(checked = maintMode, onCheckedChange = { maintMode = it })
                                Text("Trigger Maintenance Mode Blockade", fontSize = 12.sp)
                            }
                            Text("System Defensive Clearance Security Level: $secLevel", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                listOf("Low", "Medium", "High", "Critical").forEach { level ->
                                    ElevatedFilterChip(
                                        selected = secLevel == level,
                                        onClick = { secLevel = level },
                                        label = { Text(level) }
                                    )
                                }
                            }
                            Button(
                                onClick = {
                                    viewModel.updateSettings(notifyEnabled, maintMode, secLevel)
                                    Toast.makeText(viewModel.getApplication(), "Defensive parameters synchronized!", Toast.LENGTH_SHORT).show()
                                    showSecuritySettings = false
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = dynamicPrimary),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Deploy Defense Policy")
                            }
                        }
                    }
                }
            }
        }

        // Section: System Snapshots & Recoveries (Founder Exclusive restore)
        item {
            val isFounder = activeRole == "Super Administrator (Founder)"
            ControlRoomSectionCard(
                title = "Database Backup & State Recovery",
                icon = Icons.Default.Backup,
                headerColor = dynamicPrimary
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Active Backup Files (${backups.size})", fontWeight = FontWeight.Bold, color = DarkCharcoal)
                        Button(
                            onClick = {
                                viewModel.triggerBackup()
                                Toast.makeText(viewModel.getApplication(), "Database backup completed successfully!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = dynamicPrimary)
                        ) {
                            Text("Backup System")
                        }
                    }

                    backups.forEach { b ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = BrandWhite),
                            border = BorderStroke(1.dp, dynamicGold.copy(alpha = 0.2f))
                        ) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Inventory, null, tint = dynamicGold)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(b.fileName, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Text("Size: ${b.size} • Created: ${SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(Date(b.timestamp))}", fontSize = 10.sp, color = MutedCharcoal)
                                }
                                Button(
                                    onClick = {
                                        if (isFounder) {
                                            onRestoreBackup(b)
                                        } else {
                                            Toast.makeText(viewModel.getApplication(), "REJECTED: Restore capabilities require Super Administrator (Founder) clearances.", Toast.LENGTH_LONG).show()
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = if (isFounder) dynamicGold else Color.Gray),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text("Restore", fontSize = 10.sp, color = BrandWhite)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Elegant space
        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

@Composable
fun ControlRoomSectionCard(
    title: String,
    icon: ImageVector,
    headerColor: Color,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = BrandWhite),
        border = BorderStroke(1.dp, BrandGold.copy(alpha = 0.2f))
    ) {
        Column {
            Box(
                modifier = Modifier.fillMaxWidth().background(headerColor).padding(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(icon, null, tint = BrandWhite)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(title, fontWeight = FontWeight.Bold, color = BrandWhite, fontSize = 14.sp)
                }
            }
            Box(modifier = Modifier.padding(16.dp)) {
                content()
            }
        }
    }
}

// =======================================================
// ACADEMICS TAB (ADMINS / SUPER ADMINS)
// =======================================================
@Composable
fun AcademicsAdminTab(
    viewModel: LmsViewModel,
    courses: List<Course>,
    quizzes: List<Quiz>,
    resources: List<ResourceMedia>,
    dynamicPrimary: Color,
    dynamicGold: Color
) {
    CourseBuilderWorkspaceWrapper(
        viewModel = viewModel,
        courses = courses,
        quizzes = quizzes,
        resources = resources,
        dynamicPrimary = dynamicPrimary,
        dynamicGold = dynamicGold
    )
}

@Composable
fun CourseBuilderWorkspaceWrapper(
    viewModel: LmsViewModel,
    courses: List<Course>,
    quizzes: List<Quiz>,
    resources: List<ResourceMedia>,
    dynamicPrimary: Color,
    dynamicGold: Color
) {
    var selectedCourseForBuilder by remember { mutableStateOf<Course?>(null) }
    var showAddCourse by remember { mutableStateOf(false) }

    // Course state
    var courseName by remember { mutableStateOf("") }
    var courseCode by remember { mutableStateOf("") }
    var profName by remember { mutableStateOf("") }
    var credits by remember { mutableIntStateOf(3) }
    var sched by remember { mutableStateOf("Mon, Wed 10:00 AM") }
    var selectedColorHex by remember { mutableStateOf("#1E5631") }
    var courseDesc by remember { mutableStateOf("") }
    var courseDiff by remember { mutableStateOf("Intermediate") }
    var courseCat by remember { mutableStateOf("Executive Education") }

    if (selectedCourseForBuilder != null) {
        CourseBuilderWorkspace(
            course = selectedCourseForBuilder!!,
            viewModel = viewModel,
            quizzes = quizzes,
            dynamicPrimary = dynamicPrimary,
            dynamicGold = dynamicGold,
            onBack = { selectedCourseForBuilder = null }
        )
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Course & Content Studio",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkCharcoal
                        )
                        Text(
                            text = "Design modules, lessons, resources, quizzes, and certificates.",
                            fontSize = 12.sp,
                            color = MutedCharcoal
                        )
                    }
                    Button(
                        onClick = { showAddCourse = !showAddCourse },
                        colors = ButtonDefaults.buttonColors(containerColor = dynamicPrimary),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (showAddCourse) "Close" else "Create Course", fontSize = 12.sp)
                    }
                }
            }

            if (showAddCourse) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = BrandWhite),
                        border = BorderStroke(1.dp, dynamicPrimary.copy(alpha = 0.2f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("New Course Configurations", fontWeight = FontWeight.Bold, color = dynamicPrimary)

                            OutlinedTextField(
                                value = courseName,
                                onValueChange = { courseName = it },
                                label = { Text("Course Title") },
                                modifier = Modifier.fillMaxWidth().testTag("input_course_name")
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = courseCode,
                                    onValueChange = { courseCode = it },
                                    label = { Text("Code (e.g., TECH 401)") },
                                    modifier = Modifier.weight(1f).testTag("input_course_code")
                                )
                                OutlinedTextField(
                                    value = profName,
                                    onValueChange = { profName = it },
                                    label = { Text("Director/Professor") },
                                    modifier = Modifier.weight(1f).testTag("input_course_prof")
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                OutlinedTextField(
                                    value = sched,
                                    onValueChange = { sched = it },
                                    label = { Text("Schedule") },
                                    modifier = Modifier.weight(1.5f)
                                )
                                OutlinedTextField(
                                    value = credits.toString(),
                                    onValueChange = { credits = it.toIntOrNull() ?: 3 },
                                    label = { Text("Credits") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(0.7f)
                                )
                            }

                            OutlinedTextField(
                                value = courseDesc,
                                onValueChange = { courseDesc = it },
                                label = { Text("Brief Description") },
                                modifier = Modifier.fillMaxWidth()
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = courseDiff,
                                    onValueChange = { courseDiff = it },
                                    label = { Text("Difficulty (e.g. Advanced)") },
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = courseCat,
                                    onValueChange = { courseCat = it },
                                    label = { Text("Category") },
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Text("Aesthetic Theme Highlight Color", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MutedCharcoal)
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                listOf("#1E5631", "#C89B3C", "#1E3D59", "#17B890", "#D75A4A", "#4A4A4A").forEach { colorHex ->
                                    val isSelected = selectedColorHex == colorHex
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(parseHexColor(colorHex))
                                            .border(
                                                width = if (isSelected) 3.dp else 1.dp,
                                                color = if (isSelected) Color.White else Color.Transparent,
                                                shape = CircleShape
                                            )
                                            .clickable { selectedColorHex = colorHex }
                                    )
                                }
                            }

                            Button(
                                onClick = {
                                    if (courseName.isNotBlank() && courseCode.isNotBlank()) {
                                        viewModel.addCourse(
                                            name = courseName,
                                            code = courseCode,
                                            professor = profName,
                                            colorHex = selectedColorHex,
                                            credits = credits,
                                            schedule = sched,
                                            description = courseDesc,
                                            difficulty = courseDiff,
                                            category = courseCat
                                        )
                                        courseName = ""
                                        courseCode = ""
                                        profName = ""
                                        courseDesc = ""
                                        showAddCourse = false
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = dynamicPrimary),
                                modifier = Modifier.fillMaxWidth().testTag("btn_save_course")
                            ) {
                                Text("Publish Course & Open Builder")
                            }
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = BrandWhite),
                    border = BorderStroke(1.dp, dynamicPrimary.copy(alpha = 0.08f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Active Program Directory", fontWeight = FontWeight.Black, color = dynamicPrimary, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("Click 'Builder' to design syllabus modules, quizzes, resources, discussions, and certificates.", fontSize = 11.sp, color = MutedCharcoal)
                        Spacer(modifier = Modifier.height(12.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (courses.isEmpty()) {
                                Text("No courses published yet. Click 'Create Course' to start.", fontStyle = FontStyle.Italic, fontSize = 12.sp, color = Color.Gray)
                            } else {
                                courses.forEach { c ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = BrandCream.copy(alpha = 0.4f)),
                                        border = BorderStroke(1.dp, parseHexColor(c.colorHex).copy(alpha = 0.2f))
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(parseHexColor(c.colorHex)))
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text("${c.code}: ${c.name}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = DarkCharcoal)
                                                Text("Director: ${c.professor} • Credits: ${c.credits} • ${c.difficulty}", fontSize = 11.sp, color = MutedCharcoal)
                                            }
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                Button(
                                                    onClick = { selectedCourseForBuilder = c },
                                                    colors = ButtonDefaults.buttonColors(containerColor = dynamicPrimary),
                                                    shape = RoundedCornerShape(6.dp),
                                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                                    modifier = Modifier.testTag("btn_build_course_${c.id}")
                                                ) {
                                                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(12.dp), tint = Color.White)
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("Builder", fontSize = 11.sp, color = Color.White)
                                                }
                                                IconButton(onClick = { viewModel.deleteCourse(c) }) {
                                                    Icon(Icons.Default.Delete, "Delete", tint = Color.Red.copy(alpha = 0.7f), modifier = Modifier.size(20.dp))
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
    }
}

// =======================================================
// COMPLETE SYLLABUS, RESOURCE, AND CERTIFICATE BUILDER STUDIO
// =======================================================
@Composable
fun CourseBuilderWorkspace(
    course: Course,
    viewModel: LmsViewModel,
    quizzes: List<Quiz>,
    dynamicPrimary: Color,
    dynamicGold: Color,
    onBack: () -> Unit
) {
    // Collect child entities
    val modules by viewModel.courseModules.collectAsStateWithLifecycle()
    val lessons by viewModel.courseLessons.collectAsStateWithLifecycle()
    val resources by viewModel.courseResourceItems.collectAsStateWithLifecycle()
    val mockExams by viewModel.courseMockExams.collectAsStateWithLifecycle()
    val discussions by viewModel.courseDiscussionTopics.collectAsStateWithLifecycle()
    val certificateConfigs by viewModel.courseCertificateConfigs.collectAsStateWithLifecycle()
    val allAssignments by viewModel.assignments.collectAsStateWithLifecycle()

    val myModules = modules.filter { it.courseId == course.id }
    val myResources = resources.filter { it.courseId == course.id }
    val myMockExams = mockExams.filter { it.courseId == course.id }
    val myDiscussions = discussions.filter { it.courseId == course.id }
    val myCertConfig = certificateConfigs.find { it.courseId == course.id }
    val myAssignments = allAssignments.filter { it.courseId == course.id }

    var selectedSection by remember { mutableStateOf("Syllabus") }
    val sections = listOf("Syllabus", "Media Resources", "Assessments", "Discussions", "Certificates")

    // Modals & States
    var showAddModule by remember { mutableStateOf(false) }
    var moduleTitle by remember { mutableStateOf("") }
    var moduleDesc by remember { mutableStateOf("") }

    var activeLessonForEdit by remember { mutableStateOf<CourseLesson?>(null) }
    var activeModuleForAddLesson by remember { mutableStateOf<CourseModule?>(null) }
    var showLessonDialog by remember { mutableStateOf(false) }

    // Dialog Fields
    var lessonTitle by remember { mutableStateOf("") }
    var lessonOutcomes by remember { mutableStateOf("") }
    var lessonEstTime by remember { mutableStateOf("30 mins") }
    var lessonVideoUrl by remember { mutableStateOf("") }
    var lessonWorkbookUrl by remember { mutableStateOf("") }
    var lessonReadingNotes by remember { mutableStateOf("") }
    var lessonKnowledgeCheck by remember { mutableStateOf("") }
    var lessonQuizTitle by remember { mutableStateOf("") }
    var lessonAssignmentDesc by remember { mutableStateOf("") }
    var lessonReflectionPrompt by remember { mutableStateOf("") }

    // Resource Fields
    var resTitle by remember { mutableStateOf("") }
    var resType by remember { mutableStateOf("PDF Workbook") }
    var resUrl by remember { mutableStateOf("") }
    var resDetail by remember { mutableStateOf("1.5 MB") }

    // Assessment Fields
    var examTitle by remember { mutableStateOf("") }
    var examDur by remember { mutableIntStateOf(120) }
    var examQns by remember { mutableIntStateOf(50) }
    var examScore by remember { mutableIntStateOf(100) }

    // Discussions Fields
    var discussTitle by remember { mutableStateOf("") }
    var discussAuthor by remember { mutableStateOf("Facilitator Angela") }
    var discussContent by remember { mutableStateOf("") }

    // Certificate Fields
    var certTitle by remember { mutableStateOf(myCertConfig?.title ?: "Certificate of Postgraduate Mastery") }
    var certAuthority by remember { mutableStateOf(myCertConfig?.authority ?: "Rooted Academy Board of Regents") }
    var certEnabled by remember { mutableStateOf(myCertConfig?.isEnabled ?: true) }

    // Sync cert input fields if config loads
    LaunchedEffect(myCertConfig) {
        myCertConfig?.let {
            certTitle = it.title
            certAuthority = it.authority
            certEnabled = it.isEnabled
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BrandCream.copy(alpha = 0.2f))
            .padding(12.dp)
    ) {
        // Back Header
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            colors = CardDefaults.cardColors(containerColor = BrandWhite),
            border = BorderStroke(1.dp, parseHexColor(course.colorHex).copy(alpha = 0.3f))
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, "Back", tint = parseHexColor(course.colorHex))
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${course.code} BUILDER STUDIO",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = parseHexColor(course.colorHex),
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = course.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = DarkCharcoal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(parseHexColor(course.colorHex).copy(alpha = 0.1f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(course.difficulty, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = parseHexColor(course.colorHex))
                }
            }
        }

        // Sub-tabs
        ScrollableTabRow(
            selectedTabIndex = sections.indexOf(selectedSection).coerceAtLeast(0),
            containerColor = Color.Transparent,
            contentColor = dynamicPrimary,
            edgePadding = 0.dp,
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            sections.forEach { sec ->
                Tab(
                    selected = selectedSection == sec,
                    onClick = { selectedSection = sec },
                    text = { Text(sec, fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                )
            }
        }

        // Main Work Area
        Box(modifier = Modifier.weight(1f)) {
            when (selectedSection) {
                "Syllabus" -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Course Structure & Syllabus Modules", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Button(
                                    onClick = { showAddModule = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = dynamicPrimary),
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Icon(Icons.Default.Add, null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Add Module", fontSize = 11.sp)
                                }
                            }
                        }

                        if (myModules.isEmpty()) {
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = BrandWhite)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(24.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(Icons.Default.Book, null, modifier = Modifier.size(40.dp), tint = Color.LightGray)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text("No syllabus modules designed yet.", fontSize = 12.sp, color = MutedCharcoal)
                                        Text("Modules act as chapters containing individual lessons.", fontSize = 10.sp, color = Color.Gray, textAlign = TextAlign.Center)
                                    }
                                }
                            }
                        } else {
                            items(myModules) { mod ->
                                var isExpanded by remember { mutableStateOf(true) }
                                val moduleLessons = lessons.filter { it.moduleId == mod.id }

                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = BrandWhite),
                                    border = BorderStroke(1.dp, dynamicPrimary.copy(alpha = 0.15f))
                                ) {
                                    Column {
                                        // Module Header
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(dynamicPrimary.copy(alpha = 0.04f))
                                                .clickable { isExpanded = !isExpanded }
                                                .padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = if (isExpanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowRight,
                                                contentDescription = null,
                                                tint = dynamicPrimary
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(mod.title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = DarkCharcoal)
                                                if (mod.description.isNotBlank()) {
                                                    Text(mod.description, fontSize = 10.sp, color = MutedCharcoal, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                                }
                                            }
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                Button(
                                                    onClick = {
                                                        activeModuleForAddLesson = mod
                                                        activeLessonForEdit = null
                                                        // Reset lesson input fields
                                                        lessonTitle = ""
                                                        lessonOutcomes = ""
                                                        lessonEstTime = "30 mins"
                                                        lessonVideoUrl = ""
                                                        lessonWorkbookUrl = ""
                                                        lessonReadingNotes = ""
                                                        lessonKnowledgeCheck = ""
                                                        lessonQuizTitle = ""
                                                        lessonAssignmentDesc = ""
                                                        lessonReflectionPrompt = ""
                                                        showLessonDialog = true
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = dynamicGold),
                                                    shape = RoundedCornerShape(4.dp),
                                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                                ) {
                                                    Icon(Icons.Default.Add, null, modifier = Modifier.size(10.dp), tint = DarkCharcoal)
                                                    Spacer(modifier = Modifier.width(2.dp))
                                                    Text("Add Lesson", fontSize = 9.sp, color = DarkCharcoal, fontWeight = FontWeight.Bold)
                                                }
                                                IconButton(onClick = { viewModel.deleteModule(mod) }, modifier = Modifier.size(24.dp)) {
                                                    Icon(Icons.Default.Delete, "Delete", tint = Color.Red.copy(alpha = 0.6f), modifier = Modifier.size(16.dp))
                                                }
                                            }
                                        }

                                        if (isExpanded) {
                                            Column(
                                                modifier = Modifier.padding(12.dp),
                                                verticalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                if (moduleLessons.isEmpty()) {
                                                    Text("No lessons in this module. Click 'Add Lesson' to create one.", fontStyle = FontStyle.Italic, fontSize = 11.sp, color = Color.Gray, modifier = Modifier.padding(8.dp))
                                                } else {
                                                    moduleLessons.forEach { les ->
                                                        Card(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            colors = CardDefaults.cardColors(containerColor = BrandCream.copy(alpha = 0.3f)),
                                                            border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f))
                                                        ) {
                                                            Row(
                                                                modifier = Modifier.padding(10.dp),
                                                                verticalAlignment = Alignment.CenterVertically
                                                            ) {
                                                                Icon(Icons.Default.Book, null, tint = dynamicPrimary, modifier = Modifier.size(16.dp))
                                                                Spacer(modifier = Modifier.width(8.dp))
                                                                Column(modifier = Modifier.weight(1f)) {
                                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                                        Text(les.title, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = DarkCharcoal)
                                                                        Spacer(modifier = Modifier.width(6.dp))
                                                                        Box(
                                                                            modifier = Modifier
                                                                                .clip(RoundedCornerShape(3.dp))
                                                                                .background(Color.LightGray.copy(alpha = 0.5f))
                                                                                .padding(horizontal = 4.dp, vertical = 1.dp)
                                                                        ) {
                                                                            Text(les.estimatedTime, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                                                        }
                                                                    }
                                                                    // Checklists indicating loaded sub-components
                                                                    Row(
                                                                        modifier = Modifier.padding(top = 4.dp),
                                                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                                    ) {
                                                                        LessonPartIndicator(label = "Video", exists = les.videoUrl.isNotBlank())
                                                                        LessonPartIndicator(label = "Workbook", exists = les.workbookUrl.isNotBlank())
                                                                        LessonPartIndicator(label = "Quiz", exists = les.quizTitle.isNotBlank())
                                                                        LessonPartIndicator(label = "Project", exists = les.assignmentDesc.isNotBlank())
                                                                    }
                                                                }
                                                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                                                    IconButton(
                                                                        onClick = {
                                                                            activeLessonForEdit = les
                                                                            activeModuleForAddLesson = null
                                                                            // Pre-fill fields
                                                                            lessonTitle = les.title
                                                                            lessonOutcomes = les.learningOutcomes
                                                                            lessonEstTime = les.estimatedTime
                                                                            lessonVideoUrl = les.videoUrl
                                                                            lessonWorkbookUrl = les.workbookUrl
                                                                            lessonReadingNotes = les.readingNotes
                                                                            lessonKnowledgeCheck = les.knowledgeCheck
                                                                            lessonQuizTitle = les.quizTitle
                                                                            lessonAssignmentDesc = les.assignmentDesc
                                                                            lessonReflectionPrompt = les.reflectionPrompt
                                                                            showLessonDialog = true
                                                                        },
                                                                        modifier = Modifier.size(28.dp)
                                                                    ) {
                                                                        Icon(Icons.Default.Edit, "Edit", tint = dynamicPrimary, modifier = Modifier.size(16.dp))
                                                                    }
                                                                    IconButton(onClick = { viewModel.deleteLesson(les) }, modifier = Modifier.size(28.dp)) {
                                                                        Icon(Icons.Default.Delete, "Delete", tint = Color.Red.copy(alpha = 0.6f), modifier = Modifier.size(16.dp))
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
                        }
                    }
                }

                "Media Resources" -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = BrandWhite),
                                border = BorderStroke(1.dp, dynamicPrimary.copy(alpha = 0.1f))
                            ) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("Upload & Link Resource Assets", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = dynamicPrimary)
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        OutlinedTextField(
                                            value = resTitle,
                                            onValueChange = { resTitle = it },
                                            label = { Text("Resource Asset Title") },
                                            modifier = Modifier.weight(1.2f)
                                        )
                                        OutlinedTextField(
                                            value = resDetail,
                                            onValueChange = { resDetail = it },
                                            label = { Text("Details (Size/Length)") },
                                            modifier = Modifier.weight(0.8f)
                                        )
                                    }
                                    OutlinedTextField(
                                        value = resUrl,
                                        onValueChange = { resUrl = it },
                                        label = { Text("Download/Streaming URL") },
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    Text("Select Resource Type Category", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MutedCharcoal)
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        listOf("PDF Workbook", "Video", "PowerPoint presentation", "Download", "External Link").forEach { type ->
                                            val isSelected = resType == type
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(if (isSelected) dynamicPrimary else Color.LightGray.copy(alpha = 0.3f))
                                                    .clickable { resType = type }
                                                    .padding(horizontal = 6.dp, vertical = 4.dp)
                                            ) {
                                                Text(type, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = if (isSelected) Color.White else DarkCharcoal)
                                            }
                                        }
                                    }

                                    Button(
                                        onClick = {
                                            if (resTitle.isNotBlank()) {
                                                viewModel.addCourseResourceItem(course.id, resTitle, resType, resUrl, resDetail)
                                                resTitle = ""
                                                resUrl = ""
                                                resDetail = "1.5 MB"
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = dynamicPrimary),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Publish Resource Asset to Directory")
                                    }
                                }
                            }
                        }

                        item {
                            Text("Current Published Attachments", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        if (myResources.isEmpty()) {
                            item {
                                Text("No materials published yet.", fontSize = 11.sp, fontStyle = FontStyle.Italic, color = Color.Gray)
                            }
                        } else {
                            items(myResources) { res ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = BrandWhite)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = when (res.type) {
                                                "Video" -> Icons.Default.PlayCircle
                                                "PDF Workbook" -> Icons.Default.Description
                                                "PowerPoint presentation" -> Icons.Default.Slideshow
                                                "Download" -> Icons.Default.ArrowDownward
                                                else -> Icons.Default.Link
                                            },
                                            contentDescription = null,
                                            tint = dynamicPrimary
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(res.title, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                            Text("${res.type} • ${res.detail}", fontSize = 10.sp, color = MutedCharcoal)
                                        }
                                        IconButton(onClick = { viewModel.deleteCourseResourceItem(res) }) {
                                            Icon(Icons.Default.Delete, "Delete", tint = Color.Red.copy(alpha = 0.6f), modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                "Assessments" -> {
                    var assessmentSubTab by remember { mutableStateOf("Mock Exams") }
                    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("Mock Exams", "Quizzes", "Assignments").forEach { tab ->
                                val isSelected = assessmentSubTab == tab
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isSelected) dynamicGold else Color.LightGray.copy(alpha = 0.2f))
                                        .clickable { assessmentSubTab = tab }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(tab, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (isSelected) DarkCharcoal else MutedCharcoal)
                                }
                            }
                        }

                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            when (assessmentSubTab) {
                                "Mock Exams" -> {
                                    item {
                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = CardDefaults.cardColors(containerColor = BrandWhite),
                                            border = BorderStroke(1.dp, dynamicPrimary.copy(alpha = 0.1f))
                                        ) {
                                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                                Text("Add Professional Mock Exam", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = dynamicPrimary)
                                                OutlinedTextField(
                                                    value = examTitle,
                                                    onValueChange = { examTitle = it },
                                                    label = { Text("Mock Exam Title") },
                                                    modifier = Modifier.fillMaxWidth()
                                                )
                                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                    OutlinedTextField(
                                                        value = examDur.toString(),
                                                        onValueChange = { examDur = it.toIntOrNull() ?: 120 },
                                                        label = { Text("Duration (mins)") },
                                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                        modifier = Modifier.weight(1f)
                                                    )
                                                    OutlinedTextField(
                                                        value = examQns.toString(),
                                                        onValueChange = { examQns = it.toIntOrNull() ?: 50 },
                                                        label = { Text("Questions") },
                                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                        modifier = Modifier.weight(1f)
                                                    )
                                                    OutlinedTextField(
                                                        value = examScore.toString(),
                                                        onValueChange = { examScore = it.toIntOrNull() ?: 100 },
                                                        label = { Text("Max Grade") },
                                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                        modifier = Modifier.weight(1f)
                                                    )
                                                }
                                                Button(
                                                    onClick = {
                                                        if (examTitle.isNotBlank()) {
                                                            viewModel.addCourseMockExam(course.id, examTitle, examDur, examQns, examScore)
                                                            examTitle = ""
                                                        }
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = dynamicPrimary),
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Text("Publish Mock Examination")
                                                }
                                            }
                                        }
                                    }

                                    if (myMockExams.isEmpty()) {
                                        item {
                                            Text("No mock exams active.", fontStyle = FontStyle.Italic, fontSize = 11.sp, color = Color.Gray)
                                        }
                                    } else {
                                        items(myMockExams) { exam ->
                                            Card(
                                                modifier = Modifier.fillMaxWidth(),
                                                colors = CardDefaults.cardColors(containerColor = BrandWhite)
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(10.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(Icons.Default.AssignmentTurnedIn, null, tint = dynamicGold)
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Text(exam.title, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                                        Text("Duration: ${exam.durationMins} mins • ${exam.questionsCount} Questions • Max: ${exam.maxScore} marks", fontSize = 10.sp, color = MutedCharcoal)
                                                    }
                                                    IconButton(onClick = { viewModel.deleteCourseMockExam(exam) }) {
                                                        Icon(Icons.Default.Delete, "Delete", tint = Color.Red.copy(alpha = 0.6f), modifier = Modifier.size(16.dp))
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                "Quizzes" -> {
                                    item {
                                        var quizName by remember { mutableStateOf("") }
                                        var quizQns by remember { mutableIntStateOf(10) }
                                        var quizScore by remember { mutableIntStateOf(100) }

                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = CardDefaults.cardColors(containerColor = BrandWhite),
                                            border = BorderStroke(1.dp, dynamicPrimary.copy(alpha = 0.1f))
                                        ) {
                                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                                Text("Add Modular Sprint Quiz", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = dynamicPrimary)
                                                OutlinedTextField(
                                                    value = quizName,
                                                    onValueChange = { quizName = it },
                                                    label = { Text("Quiz Title") },
                                                    modifier = Modifier.fillMaxWidth()
                                                )
                                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                    OutlinedTextField(
                                                        value = quizQns.toString(),
                                                        onValueChange = { quizQns = it.toIntOrNull() ?: 10 },
                                                        label = { Text("Questions") },
                                                        modifier = Modifier.weight(1f)
                                                    )
                                                    OutlinedTextField(
                                                        value = quizScore.toString(),
                                                        onValueChange = { quizScore = it.toIntOrNull() ?: 100 },
                                                        label = { Text("Max Grade") },
                                                        modifier = Modifier.weight(1f)
                                                    )
                                                }
                                                Button(
                                                    onClick = {
                                                        if (quizName.isNotBlank()) {
                                                            viewModel.createQuiz(course.id, quizName, quizQns, quizScore)
                                                            quizName = ""
                                                        }
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = dynamicPrimary),
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Text("Publish Sprint Quiz")
                                                }
                                            }
                                        }
                                    }

                                    val courseQuizzes = quizzes.filter { it.courseId == course.id }
                                    if (courseQuizzes.isEmpty()) {
                                        item {
                                            Text("No general quizzes active.", fontStyle = FontStyle.Italic, fontSize = 11.sp, color = Color.Gray)
                                        }
                                    } else {
                                        items(courseQuizzes) { qz ->
                                            Card(
                                                modifier = Modifier.fillMaxWidth(),
                                                colors = CardDefaults.cardColors(containerColor = BrandWhite)
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(10.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(Icons.Default.Help, null, tint = dynamicPrimary)
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Text(qz.title, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                                        Text("${qz.questionsCount} Questions • Max Score: ${qz.maxScore} marks", fontSize = 10.sp, color = MutedCharcoal)
                                                    }
                                                    IconButton(onClick = { viewModel.deleteQuiz(qz) }) {
                                                        Icon(Icons.Default.Delete, "Delete", tint = Color.Red.copy(alpha = 0.6f), modifier = Modifier.size(16.dp))
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                "Assignments" -> {
                                    item {
                                        var assignTitle by remember { mutableStateOf("") }
                                        var assignDesc by remember { mutableStateOf("") }
                                        var assignPriority by remember { mutableStateOf("High") }

                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = CardDefaults.cardColors(containerColor = BrandWhite),
                                            border = BorderStroke(1.dp, dynamicPrimary.copy(alpha = 0.1f))
                                        ) {
                                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                                Text("Add Course Assignment Pro", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = dynamicPrimary)
                                                OutlinedTextField(
                                                    value = assignTitle,
                                                    onValueChange = { assignTitle = it },
                                                    label = { Text("Assignment Title") },
                                                    modifier = Modifier.fillMaxWidth()
                                                )
                                                OutlinedTextField(
                                                    value = assignDesc,
                                                    onValueChange = { assignDesc = it },
                                                    label = { Text("Instructions") },
                                                    modifier = Modifier.fillMaxWidth()
                                                )
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text("Priority Rating:", fontSize = 11.sp, modifier = Modifier.weight(1f))
                                                    listOf("Low", "Medium", "High").forEach { pr ->
                                                        val isSel = assignPriority == pr
                                                        Box(
                                                            modifier = Modifier
                                                                .clip(RoundedCornerShape(4.dp))
                                                                .background(if (isSel) dynamicPrimary else Color.LightGray.copy(alpha = 0.2f))
                                                                .clickable { assignPriority = pr }
                                                                .padding(horizontal = 10.dp, vertical = 4.dp)
                                                        ) {
                                                            Text(pr, fontSize = 10.sp, color = if (isSel) Color.White else DarkCharcoal)
                                                        }
                                                    }
                                                }
                                                Button(
                                                    onClick = {
                                                        if (assignTitle.isNotBlank()) {
                                                            viewModel.addAssignment(
                                                                courseId = course.id,
                                                                title = assignTitle,
                                                                description = assignDesc,
                                                                dueDate = System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000L,
                                                                priority = assignPriority,
                                                                type = "Assignment"
                                                            )
                                                            assignTitle = ""
                                                            assignDesc = ""
                                                        }
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = dynamicPrimary),
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Text("Publish Case Study Assignment")
                                                }
                                            }
                                        }
                                    }

                                    if (myAssignments.isEmpty()) {
                                        item {
                                            Text("No assignments active.", fontStyle = FontStyle.Italic, fontSize = 11.sp, color = Color.Gray)
                                        }
                                    } else {
                                        items(myAssignments) { ass ->
                                            Card(
                                                modifier = Modifier.fillMaxWidth(),
                                                colors = CardDefaults.cardColors(containerColor = BrandWhite)
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(10.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(Icons.Default.Assignment, null, tint = dynamicPrimary)
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Text(ass.title, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                                        Text(ass.description, fontSize = 10.sp, color = MutedCharcoal, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                                        Text("Priority: ${ass.priority}", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = dynamicGold)
                                                    }
                                                    IconButton(onClick = { viewModel.deleteAssignment(ass) }) {
                                                        Icon(Icons.Default.Delete, "Delete", tint = Color.Red.copy(alpha = 0.6f), modifier = Modifier.size(16.dp))
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

                "Discussions" -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = BrandWhite),
                                border = BorderStroke(1.dp, dynamicPrimary.copy(alpha = 0.1f))
                            ) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("Post Forum Topic / Discussion Prompt", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = dynamicPrimary)
                                    OutlinedTextField(
                                        value = discussTitle,
                                        onValueChange = { discussTitle = it },
                                        label = { Text("Discussion Subject Thread") },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    OutlinedTextField(
                                        value = discussContent,
                                        onValueChange = { discussContent = it },
                                        label = { Text("Starting Post Prompt Content") },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    OutlinedTextField(
                                        value = discussAuthor,
                                        onValueChange = { discussAuthor = it },
                                        label = { Text("Author Name") },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Button(
                                        onClick = {
                                            if (discussTitle.isNotBlank()) {
                                                viewModel.addCourseDiscussionTopic(course.id, discussTitle, discussAuthor, discussContent)
                                                discussTitle = ""
                                                discussContent = ""
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = dynamicPrimary),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Launch Forum Topic Thread")
                                    }
                                }
                            }
                        }

                        item {
                            Text("Active Community Threads", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        if (myDiscussions.isEmpty()) {
                            item {
                                Text("No forum threads published yet.", fontSize = 11.sp, fontStyle = FontStyle.Italic, color = Color.Gray)
                            }
                        } else {
                            items(myDiscussions) { disc ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = BrandWhite)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Forum, null, tint = dynamicGold, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(disc.title, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = DarkCharcoal, modifier = Modifier.weight(1f))
                                            IconButton(onClick = { viewModel.deleteCourseDiscussionTopic(disc) }, modifier = Modifier.size(24.dp)) {
                                                Icon(Icons.Default.Delete, "Delete", tint = Color.Red.copy(alpha = 0.6f), modifier = Modifier.size(14.dp))
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(disc.content, fontSize = 11.sp, color = MutedCharcoal)
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text("Initiated by: ${disc.author} • ${SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date(disc.timestamp))}", fontSize = 9.sp, color = Color.Gray, fontStyle = FontStyle.Italic)
                                    }
                                }
                            }
                        }
                    }
                }

                "Certificates" -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = BrandWhite),
                                border = BorderStroke(1.dp, dynamicPrimary.copy(alpha = 0.1f))
                            ) {
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Text("Official Course Certificate Configuration Studio", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = dynamicPrimary)

                                    OutlinedTextField(
                                        value = certTitle,
                                        onValueChange = { certTitle = it },
                                        label = { Text("Certificate Degree Designation Title") },
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    OutlinedTextField(
                                        value = certAuthority,
                                        onValueChange = { certAuthority = it },
                                        label = { Text("Issuing Board Authority (Signatory)") },
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Checkbox(
                                            checked = certEnabled,
                                            onCheckedChange = { certEnabled = it },
                                            colors = CheckboxDefaults.colors(checkedColor = dynamicPrimary)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Column {
                                            Text("Authorize Certificate Generation", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                            Text("Allow learners to download this certificate upon completing all lessons.", fontSize = 9.sp, color = MutedCharcoal)
                                        }
                                    }

                                    Button(
                                        onClick = {
                                            viewModel.upsertCourseCertificateConfig(course.id, certTitle, certAuthority, certEnabled)
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = dynamicPrimary),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Commit Certificate Configurations")
                                    }
                                }
                            }
                        }

                        item {
                            Text("Real-time Digital Certificate Layout Preview", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        item {
                            PremiumCertificateCard(
                                certTitle = certTitle,
                                courseName = course.name,
                                authority = certAuthority,
                                recipientName = "Sarah Connor (Postgraduate Student)",
                                isEnabled = certEnabled,
                                dynamicPrimary = dynamicPrimary,
                                dynamicGold = dynamicGold
                            )
                        }
                    }
                }
            }
        }
    }

    // Module Dialog
    if (showAddModule) {
        Dialog(onDismissRequest = { showAddModule = false }) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = BrandWhite),
                border = BorderStroke(1.dp, dynamicPrimary.copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Design Syllabus Module", fontWeight = FontWeight.Bold, color = dynamicPrimary, fontSize = 14.sp)
                    OutlinedTextField(
                        value = moduleTitle,
                        onValueChange = { moduleTitle = it },
                        label = { Text("Module Title (e.g., Load Balancing Protocols)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = moduleDesc,
                        onValueChange = { moduleDesc = it },
                        label = { Text("Short Description / Learning Goals") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { showAddModule = false }) {
                            Text("Cancel", color = Color.Gray)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (moduleTitle.isNotBlank()) {
                                    viewModel.addModule(course.id, moduleTitle, moduleDesc)
                                    moduleTitle = ""
                                    moduleDesc = ""
                                    showAddModule = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = dynamicPrimary)
                        ) {
                            Text("Create Module")
                        }
                    }
                }
            }
        }
    }

    // Lesson Dialog (Supports Edit and Add)
    if (showLessonDialog) {
        Dialog(onDismissRequest = { showLessonDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 500.dp)
                    .padding(16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = BrandWhite),
                border = BorderStroke(1.dp, dynamicPrimary.copy(alpha = 0.2f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Text(
                        text = if (activeLessonForEdit != null) "Edit Lesson Details" else "Construct New Lesson",
                        fontWeight = FontWeight.Bold,
                        color = dynamicPrimary,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = lessonTitle,
                            onValueChange = { lessonTitle = it },
                            label = { Text("Lesson Title") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = lessonEstTime,
                            onValueChange = { lessonEstTime = it },
                            label = { Text("Estimated Completion Time (e.g., 30 mins)") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = lessonOutcomes,
                            onValueChange = { lessonOutcomes = it },
                            label = { Text("Learning Outcomes (Bullet Points)") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2
                        )

                        Text("Lesson Resource Attachments", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = dynamicGold)

                        OutlinedTextField(
                            value = lessonVideoUrl,
                            onValueChange = { lessonVideoUrl = it },
                            label = { Text("Streaming Video Lecture URL") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = lessonWorkbookUrl,
                            onValueChange = { lessonWorkbookUrl = it },
                            label = { Text("PDF Workbook download URL") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = lessonReadingNotes,
                            onValueChange = { lessonReadingNotes = it },
                            label = { Text("Syllabus Reading Notes (Deep Study Content)") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3
                        )

                        Text("Interactive Assessment Flows", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = dynamicGold)

                        OutlinedTextField(
                            value = lessonKnowledgeCheck,
                            onValueChange = { lessonKnowledgeCheck = it },
                            label = { Text("In-Lesson Knowledge Check Question") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = lessonQuizTitle,
                            onValueChange = { lessonQuizTitle = it },
                            label = { Text("Linked Practice Quiz Subject Title") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = lessonAssignmentDesc,
                            onValueChange = { lessonAssignmentDesc = it },
                            label = { Text("Hands-On Practice Assignment Prompt") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2
                        )

                        OutlinedTextField(
                            value = lessonReflectionPrompt,
                            onValueChange = { lessonReflectionPrompt = it },
                            label = { Text("Self-Reflection Assessment Prompt") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { showLessonDialog = false }) {
                            Text("Cancel", color = Color.Gray)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (lessonTitle.isNotBlank()) {
                                    if (activeLessonForEdit != null) {
                                        viewModel.updateLesson(activeLessonForEdit!!.copy(
                                            title = lessonTitle,
                                            learningOutcomes = lessonOutcomes,
                                            estimatedTime = lessonEstTime,
                                            videoUrl = lessonVideoUrl,
                                            workbookUrl = lessonWorkbookUrl,
                                            readingNotes = lessonReadingNotes,
                                            knowledgeCheck = lessonKnowledgeCheck,
                                            quizTitle = lessonQuizTitle,
                                            assignmentDesc = lessonAssignmentDesc,
                                            reflectionPrompt = lessonReflectionPrompt
                                        ))
                                    } else if (activeModuleForAddLesson != null) {
                                        viewModel.addLesson(
                                            moduleId = activeModuleForAddLesson!!.id,
                                            title = lessonTitle,
                                            learningOutcomes = lessonOutcomes,
                                            estimatedTime = lessonEstTime,
                                            videoUrl = lessonVideoUrl,
                                            workbookUrl = lessonWorkbookUrl,
                                            readingNotes = lessonReadingNotes,
                                            knowledgeCheck = lessonKnowledgeCheck,
                                            quizTitle = lessonQuizTitle,
                                            assignmentDesc = lessonAssignmentDesc,
                                            reflectionPrompt = lessonReflectionPrompt
                                        )
                                    }
                                    showLessonDialog = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = dynamicPrimary)
                        ) {
                            Text("Commit Lesson")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LessonPartIndicator(label: String, exists: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Icon(
            imageVector = if (exists) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
            contentDescription = null,
            tint = if (exists) Color(0xFF1E5631) else Color.Gray,
            modifier = Modifier.size(10.dp)
        )
        Text(label, fontSize = 8.sp, color = if (exists) DarkCharcoal else Color.Gray, fontWeight = if (exists) FontWeight.Bold else FontWeight.Normal)
    }
}

// =======================================================
// ELITE DIGITAL CERTIFICATE CARD PREVIEW
// =======================================================
@Composable
fun PremiumCertificateCard(
    certTitle: String,
    courseName: String,
    authority: String,
    recipientName: String,
    isEnabled: Boolean,
    dynamicPrimary: Color,
    dynamicGold: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("certificate_premium_card"),
        colors = CardDefaults.cardColors(containerColor = DarkCharcoal),
        border = BorderStroke(2.dp, if (isEnabled) dynamicGold else Color.Gray),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .background(DarkCharcoal),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(BorderStroke(1.dp, if (isEnabled) dynamicGold.copy(alpha = 0.4f) else Color.Gray.copy(alpha = 0.2f)), RoundedCornerShape(8.dp))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = if (isEnabled) dynamicGold else Color.Gray,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (isEnabled) certTitle.uppercase() else "CERTIFICATE DISABLED",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isEnabled) dynamicGold else Color.Gray,
                        textAlign = TextAlign.Center,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "THIS OFFICIAL CERTIFICATE OF ACADEMIC MASTERY IS GRANTED TO",
                        fontSize = 7.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.LightGray.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = recipientName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "FOR COMMENDABLY SATISFYING THE RIGOROUS POSTGRADUATE CURRICULUM UNDER",
                        fontSize = 7.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.LightGray.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = courseName,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isEnabled) dynamicGold else Color.Gray,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Official Digital Signature", fontSize = 6.sp, color = Color.Gray)
                            Spacer(modifier = Modifier.height(2.dp))
                            Box(modifier = Modifier.width(60.dp).height(1.dp).background(Color.Gray))
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("REGISTRAR OFFICE", fontSize = 6.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }

                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(if (isEnabled) dynamicGold.copy(alpha = 0.2f) else Color.LightGray.copy(alpha = 0.1f))
                                .border(1.dp, if (isEnabled) dynamicGold else Color.Gray, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("ROOTED", fontSize = 4.sp, color = if (isEnabled) dynamicGold else Color.Gray, fontWeight = FontWeight.Bold)
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Under Authorized Authority", fontSize = 6.sp, color = Color.Gray)
                            Spacer(modifier = Modifier.height(2.dp))
                            Box(modifier = Modifier.width(60.dp).height(1.dp).background(Color.Gray))
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(authority.uppercase(), fontSize = 5.sp, color = Color.White, fontWeight = FontWeight.Bold, maxLines = 1)
                        }
                    }
                }
            }
        }
    }
}

// =======================================================
// OPERATIONS TAB (ADMIN / SUPER ADMIN)
// =======================================================
@Composable
fun OperationsTab(
    viewModel: LmsViewModel,
    users: List<UserAccount>,
    payments: List<Payment>,
    placements: List<Placement>,
    attendance: List<AttendanceRecord>,
    dynamicPrimary: Color,
    dynamicGold: Color
) {
    val context = LocalContext.current
    var showAddPayment by remember { mutableStateOf(false) }
    var payClient by remember { mutableStateOf("") }
    var payAmount by remember { mutableStateOf("") }
    var payStatus by remember { mutableStateOf("Pending") }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Academy Strategic Operations & Pipelines", fontWeight = FontWeight.Black, fontSize = 18.sp, color = DarkCharcoal)
        }

        // Section: Corporate Payment Ledgers
        item {
            ControlRoomSectionCard(title = "Billing & Corporate Payments Ledgers", icon = Icons.Default.ReceiptLong, dynamicPrimary) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Active Invoices (${payments.size})", fontWeight = FontWeight.Bold)
                        Button(
                            onClick = { showAddPayment = !showAddPayment },
                            colors = ButtonDefaults.buttonColors(containerColor = dynamicPrimary)
                        ) {
                            Text(if (showAddPayment) "Collapse" else "Log Invoice")
                        }
                    }

                    if (showAddPayment) {
                        Card(colors = CardDefaults.cardColors(containerColor = BrandCream), border = BorderStroke(1.dp, dynamicGold.copy(alpha = 0.5f))) {
                            Column(modifier = Modifier.padding(12.dp).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(value = payClient, onValueChange = { payClient = it }, label = { Text("Client Corporate Enterprise") }, modifier = Modifier.fillMaxWidth().testTag("add_pay_client"))
                                OutlinedTextField(
                                    value = payAmount,
                                    onValueChange = { payAmount = it },
                                    label = { Text("Sponsorship Amount ($)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    listOf("Paid", "Pending", "Overdue").forEach { status ->
                                        ElevatedFilterChip(
                                            selected = payStatus == status,
                                            onClick = { payStatus = status },
                                            label = { Text(status) }
                                        )
                                    }
                                }
                                Button(
                                    onClick = {
                                        val amt = payAmount.toDoubleOrNull() ?: 5000.0
                                        if (payClient.isNotBlank()) {
                                            viewModel.logPayment(payClient, amt, payStatus)
                                            payClient = ""
                                            payAmount = ""
                                            showAddPayment = false
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = dynamicPrimary),
                                    modifier = Modifier.fillMaxWidth().testTag("btn_save_payment")
                                ) {
                                    Text("Publish Corporate Invoice Ledger")
                                }
                            }
                        }
                    }

                    payments.forEach { pay ->
                        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = BrandWhite)) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.MonetizationOn, null, tint = if (pay.status == "Paid") PrimaryGreen else Color.Red)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(pay.clientName, fontWeight = FontWeight.Bold)
                                    Text("$${"%,.2f".format(pay.amount)} • Due: Pending Transfer", fontSize = 11.sp, color = MutedCharcoal)
                                }
                                Badge(
                                    containerColor = when (pay.status) {
                                        "Paid" -> PrimaryGreen
                                        "Pending" -> dynamicGold
                                        else -> Color.Red
                                    },
                                    contentColor = BrandWhite
                                ) {
                                    Text(pay.status)
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                                // Dropdown to toggle payment status directly
                                IconButton(
                                    onClick = {
                                        val nextStatus = when (pay.status) {
                                            "Paid" -> "Pending"
                                            "Pending" -> "Overdue"
                                            else -> "Paid"
                                        }
                                        viewModel.updatePaymentStatus(pay, nextStatus)
                                    }
                                ) {
                                    Icon(Icons.Default.Cached, "Toggle Status", tint = Color.Gray)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section: Placement Pipeline Matrix
        item {
            ControlRoomSectionCard(title = "Hiring & Placement Pipeline Matrix", icon = Icons.Default.Work, dynamicPrimary) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    placements.forEach { pl ->
                        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = BrandWhite)) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.School, null, tint = dynamicGold)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(pl.learnerName, fontWeight = FontWeight.Bold)
                                    Text("Role: ${pl.role} at ${pl.partnerName}", fontSize = 11.sp, color = MutedCharcoal)
                                }
                                Badge(containerColor = dynamicPrimary) { Text(pl.status) }
                                Spacer(modifier = Modifier.width(4.dp))
                                IconButton(
                                    onClick = {
                                        val nextStatus = when (pl.status) {
                                            "Applied" -> "Interviewing"
                                            "Interviewing" -> "Offered"
                                            "Offered" -> "Placed"
                                            "Placed" -> "Rejected"
                                            else -> "Applied"
                                        }
                                        viewModel.updatePlacementStatus(pl, nextStatus)
                                    }
                                ) {
                                    Icon(Icons.Default.SyncAlt, "Update Placement", tint = Color.Gray)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section: Learner Attendance Register
        item {
            ControlRoomSectionCard(title = "Daily Learner Attendance Register", icon = Icons.Default.CoPresent, dynamicPrimary) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    attendance.forEach { att ->
                        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = BrandWhite)) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Person, null, tint = dynamicPrimary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(att.userName, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Text("Module: ${att.courseName}", fontSize = 11.sp, color = MutedCharcoal)
                                }
                                Badge(
                                    containerColor = when (att.status) {
                                        "Present" -> PrimaryGreen
                                        "Late" -> dynamicGold
                                        else -> Color.Red
                                    },
                                    contentColor = BrandWhite
                                ) {
                                    Text(att.status)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section: Certificate Issuance & Reports Generator
        item {
            ControlRoomSectionCard(title = "Syllabus Progression & Export Reports Engine", icon = Icons.Default.Assessment, dynamicPrimary) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Eligible graduates display accomplishments here. Trigger authoritative PDF/Excel platform exports below.", fontSize = 11.sp, color = MutedCharcoal)
                    
                    users.filter { it.role == "Learner" }.forEach { learner ->
                        val isCertified = learner.certificateIssued
                        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = BrandWhite)) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(learner.name, fontWeight = FontWeight.Bold)
                                    Text("Grades average: 88% • Syllabus Progress: 92%", fontSize = 11.sp, color = MutedCharcoal)
                                }
                                Button(
                                    onClick = { 
                                        viewModel.issueCertificate(learner)
                                        Toast.makeText(context, "Graduate Certificate Issued to ${learner.name}!", Toast.LENGTH_LONG).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = if (isCertified) PrimaryGreen else dynamicGold),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(if (isCertified) "Certified ✓" else "Issue Certificate", fontSize = 10.sp)
                                }
                            }
                        }
                    }

                    HorizontalDivider()
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                Toast.makeText(context, "EXPORT SUCCESS: rooted_executive_report_2026.pdf generated and downloaded!", Toast.LENGTH_LONG).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = dynamicPrimary),
                            modifier = Modifier.weight(1f).testTag("btn_export_pdf")
                        ) {
                            Icon(Icons.Default.PictureAsPdf, null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Export PDF", fontSize = 12.sp)
                        }
                        Button(
                            onClick = {
                                Toast.makeText(context, "EXPORT SUCCESS: rooted_accounting_ledger.xlsx generated and downloaded!", Toast.LENGTH_LONG).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = dynamicPrimary),
                            modifier = Modifier.weight(1f).testTag("btn_export_excel")
                        ) {
                            Icon(Icons.Default.TableChart, null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Export Excel", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

// =======================================================
// FACILITATOR CLASS STUDIO TAB (INSTRUCTOR)
// =======================================================
@Composable
fun FacilitatorClassStudioTab(
    viewModel: LmsViewModel,
    courses: List<Course>,
    resources: List<ResourceMedia>,
    quizzes: List<Quiz>,
    virtualClasses: List<VirtualClass>,
    dynamicPrimary: Color,
    dynamicGold: Color
) {
    val context = LocalContext.current
    var showScheduleClass by remember { mutableStateOf(false) }
    var selectedCourseId by remember { mutableIntStateOf(0) }
    
    var classTitle by remember { mutableStateOf("") }
    var platformType by remember { mutableStateOf("Zoom") }
    var classUrl by remember { mutableStateOf("https://zoom.us/j/1234567890") }
    var facilitatorName by remember { mutableStateOf("Dr. Angela Yu") }
    var meetingId by remember { mutableStateOf("841-394-1102") }
    var password by remember { mutableStateOf("ZoomLMS") }
    var durationMins by remember { mutableIntStateOf(60) }
    var isReminderEnabled by remember { mutableStateOf(true) }
    var isCalendarIntegrated by remember { mutableStateOf(true) }
    var hoursFromNow by remember { mutableIntStateOf(24) } // default starts in 24 hours

    // Auto update prefilled URL & meeting details when platform changes to save admin effort
    LaunchedEffect(platformType) {
        when (platformType) {
            "Zoom" -> {
                classUrl = "https://zoom.us/j/1234567890"
                meetingId = "841-394-1102"
                password = "ZoomLMS"
            }
            "Google Meet" -> {
                classUrl = "https://meet.google.com/abc-defg-hij"
                meetingId = "abc-defg-hij"
                password = "No password required"
            }
            "Microsoft Teams" -> {
                classUrl = "https://teams.microsoft.com/l/meetup-join/abc"
                meetingId = "teams-8812739"
                password = "TeamsPass"
            }
            "Loom" -> {
                classUrl = "https://loom.com/share/abc-123"
                meetingId = "Loom Room"
                password = "No password required"
            }
            "YouTube Live" -> {
                classUrl = "https://youtube.com/live/abc-def-123"
                meetingId = "youtube-live-stream"
                password = "No password required"
            }
        }
    }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            Text("Syllabus Design & Virtual Lecture Classroom", fontWeight = FontWeight.Black, fontSize = 18.sp, color = DarkCharcoal)
        }

        // Virtual Classes Scheduler
        item {
            ControlRoomSectionCard(title = "Schedule Virtual Classroom Session", icon = Icons.Default.VideoCall, dynamicPrimary) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Scheduled Meetings (${virtualClasses.size})", fontWeight = FontWeight.Bold)
                        Button(
                            onClick = { 
                                if (courses.isNotEmpty()) {
                                    selectedCourseId = courses.first().id
                                    showScheduleClass = !showScheduleClass 
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = dynamicPrimary),
                            enabled = courses.isNotEmpty()
                        ) {
                            Text(if (showScheduleClass) "Collapse" else "Schedule Live")
                        }
                    }

                    if (showScheduleClass && courses.isNotEmpty()) {
                        Card(colors = CardDefaults.cardColors(containerColor = BrandCream), border = BorderStroke(1.dp, dynamicGold.copy(alpha = 0.5f))) {
                            Column(modifier = Modifier.padding(12.dp).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text("Class Scheduling Controls", fontWeight = FontWeight.Bold, color = dynamicPrimary, fontSize = 13.sp)
                                
                                OutlinedTextField(value = classTitle, onValueChange = { classTitle = it }, label = { Text("Lecture Theme Topic") }, modifier = Modifier.fillMaxWidth().testTag("add_class_title"))
                                
                                Text("Select Academic Course:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    courses.forEach { course ->
                                        ElevatedFilterChip(
                                            selected = selectedCourseId == course.id,
                                            onClick = { selectedCourseId = course.id },
                                            label = { Text(course.code) }
                                        )
                                    }
                                }

                                Text("Virtual platform provider:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    listOf("Zoom", "Google Meet", "Microsoft Teams", "Loom", "YouTube Live").forEach { platform ->
                                        ElevatedFilterChip(
                                            selected = platformType == platform,
                                            onClick = { platformType = platform },
                                            label = { Text(platform) }
                                        )
                                    }
                                }

                                OutlinedTextField(value = classUrl, onValueChange = { classUrl = it }, label = { Text("Meeting URL Link") }, modifier = Modifier.fillMaxWidth())
                                OutlinedTextField(value = facilitatorName, onValueChange = { facilitatorName = it }, label = { Text("Facilitator Name") }, modifier = Modifier.fillMaxWidth())

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(value = meetingId, onValueChange = { meetingId = it }, label = { Text("Meeting ID") }, modifier = Modifier.weight(1f))
                                    OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Password") }, modifier = Modifier.weight(1f))
                                }

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                    OutlinedTextField(
                                        value = durationMins.toString(),
                                        onValueChange = { durationMins = it.toIntOrNull() ?: 60 },
                                        label = { Text("Duration (Mins)") },
                                        modifier = Modifier.weight(1f)
                                    )
                                    OutlinedTextField(
                                        value = hoursFromNow.toString(),
                                        onValueChange = { hoursFromNow = it.toIntOrNull() ?: 24 },
                                        label = { Text("Starts in (Hours)") },
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                // Reminders and Calendar triggers
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Checkbox(checked = isReminderEnabled, onCheckedChange = { isReminderEnabled = it })
                                        Text("Automatic reminders 🔔", fontSize = 11.sp)
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Checkbox(checked = isCalendarIntegrated, onCheckedChange = { isCalendarIntegrated = it })
                                        Text("Calendar integration 📅", fontSize = 11.sp)
                                    }
                                }

                                Button(
                                    onClick = {
                                        if (classTitle.isNotBlank()) {
                                            val scheduleTime = System.currentTimeMillis() + (hoursFromNow * 3600 * 1000L)
                                            viewModel.scheduleVirtualClass(
                                                courseId = selectedCourseId,
                                                title = classTitle,
                                                platform = platformType,
                                                url = classUrl,
                                                scheduledTime = scheduleTime,
                                                durationMins = durationMins,
                                                facilitator = facilitatorName,
                                                meetingId = meetingId,
                                                password = password,
                                                isReminderEnabled = isReminderEnabled,
                                                isCalendarIntegrated = isCalendarIntegrated,
                                                recordingUrl = ""
                                            )
                                            
                                            if (isReminderEnabled) {
                                                Toast.makeText(context, "REMINDERS SCHEDULED: Registered automatic student notifications!", Toast.LENGTH_SHORT).show()
                                            }
                                            if (isCalendarIntegrated) {
                                                Toast.makeText(context, "CALENDAR SYNCED: Meeting synced with Outlook & Google Calendar!", Toast.LENGTH_SHORT).show()
                                            }
                                            
                                            classTitle = ""
                                            showScheduleClass = false
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = dynamicPrimary),
                                    modifier = Modifier.fillMaxWidth().testTag("btn_save_class")
                                ) {
                                    Text("Broadcast Live Seminar Link")
                                }
                            }
                        }
                    }

                    virtualClasses.forEach { vClass ->
                        val course = courses.find { it.id == vClass.courseId }
                        val code = course?.code ?: "TRAINING"
                        val courseName = course?.name ?: "Professional Course"
                        
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = BrandWhite),
                            border = BorderStroke(1.dp, dynamicGold.copy(alpha = 0.25f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                // Header: title & platform badge
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(vClass.title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = DarkCharcoal)
                                        Text("$code • $courseName", fontSize = 11.sp, color = MutedCharcoal)
                                    }
                                    
                                    val platformColor = when (vClass.platform) {
                                        "Zoom" -> Color(0xFF2D8CFF)
                                        "Google Meet" -> Color(0xFF00897B)
                                        "Microsoft Teams" -> Color(0xFF5C6BC0)
                                        "Loom" -> Color(0xFF6200EE)
                                        "YouTube Live" -> Color(0xFFE53935)
                                        else -> dynamicPrimary
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(platformColor.copy(alpha = 0.15f))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(vClass.platform, color = platformColor, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                                    }
                                }
                                
                                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
                                
                                // Grid of properties
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth()) {
                                        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Person, null, tint = dynamicGold, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Facilitator: ", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            Text(vClass.facilitator, fontSize = 11.sp, color = DarkCharcoal)
                                        }
                                        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Timer, null, tint = dynamicGold, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Duration: ", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            Text("${vClass.durationMins} mins", fontSize = 11.sp, color = DarkCharcoal)
                                        }
                                    }
                                    
                                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.CalendarToday, null, tint = dynamicGold, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Date & Time: ", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        Text(formatClassDateTime(vClass.scheduledTime), fontSize = 11.sp, color = DarkCharcoal)
                                    }
                                    
                                    Row(modifier = Modifier.fillMaxWidth()) {
                                        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Fingerprint, null, tint = dynamicGold, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("ID: ", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            Text(vClass.meetingId.ifEmpty { "None" }, fontSize = 11.sp, color = DarkCharcoal)
                                        }
                                        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.VpnKey, null, tint = dynamicGold, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Password: ", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            Text(vClass.password.ifEmpty { "None" }, fontSize = 11.sp, color = DarkCharcoal)
                                        }
                                    }
                                    
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = if (vClass.isReminderEnabled) Icons.Default.NotificationsActive else Icons.Default.NotificationsOff,
                                                contentDescription = null,
                                                tint = if (vClass.isReminderEnabled) Color(0xFF4CAF50) else Color.Gray,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = if (vClass.isReminderEnabled) "Reminders Active 🔔" else "No Reminders",
                                                fontSize = 10.sp,
                                                color = if (vClass.isReminderEnabled) Color(0xFF4CAF50) else Color.Gray,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = if (vClass.isCalendarIntegrated) Icons.Default.CalendarMonth else Icons.Default.CalendarToday,
                                                contentDescription = null,
                                                tint = if (vClass.isCalendarIntegrated) Color(0xFF2196F3) else Color.Gray,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = if (vClass.isCalendarIntegrated) "Calendar Synced 📅" else "Local only",
                                                fontSize = 10.sp,
                                                color = if (vClass.isCalendarIntegrated) Color(0xFF2196F3) else Color.Gray,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                                
                                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
                                
                                // Attendance Register
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                        Text("Attendance Register:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DarkCharcoal)
                                        TextButton(
                                            onClick = {
                                                val mockStudent = listOf("Bruce Wayne", "Clark Kent", "Diana Prince", "Barry Allen", "Selina Kyle").random()
                                                val currentReg = vClass.attendanceRegister
                                                val updatedReg = if (currentReg.isEmpty()) mockStudent else "$currentReg;$mockStudent"
                                                viewModel.updateVirtualClass(vClass.copy(attendanceRegister = updatedReg))
                                                Toast.makeText(context, "ATTENDANCE ADDED: Registered $mockStudent for this live session!", Toast.LENGTH_SHORT).show()
                                            },
                                            colors = ButtonDefaults.textButtonColors(contentColor = dynamicPrimary)
                                        ) {
                                            Icon(Icons.Default.Add, null, modifier = Modifier.size(12.dp))
                                            Spacer(modifier = Modifier.width(2.dp))
                                            Text("Add Mock Student", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    
                                    val joinedList = vClass.attendanceRegister.split(";").filter { it.isNotBlank() }
                                    if (joinedList.isEmpty()) {
                                        Text("No attendees registered yet. Waiting for learners to join.", fontSize = 11.sp, fontStyle = FontStyle.Italic, color = Color.Gray)
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(BrandCream.copy(alpha = 0.5f))
                                                .padding(8.dp)
                                        ) {
                                            Text(
                                                text = joinedList.joinToString(", "),
                                                fontSize = 11.sp,
                                                color = DarkCharcoal,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }
                                
                                // Recording Section
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text("Recording Link:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DarkCharcoal)
                                    if (vClass.recordingUrl.isNotEmpty()) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(dynamicPrimary.copy(alpha = 0.05f))
                                                .border(1.dp, dynamicPrimary.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                                                .padding(8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                                Icon(Icons.Default.SlowMotionVideo, null, tint = dynamicPrimary, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = vClass.recordingUrl,
                                                    fontSize = 11.sp,
                                                    color = dynamicPrimary,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                            IconButton(
                                                onClick = {
                                                    Toast.makeText(context, "LAUNCHING RECORDING: Launching class playback session!", Toast.LENGTH_SHORT).show()
                                                },
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(Icons.Default.PlayArrow, "Play Recording", tint = dynamicPrimary, modifier = Modifier.size(16.dp))
                                            }
                                        }
                                    } else {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "Automatic recording link will be posted post-class.",
                                                fontSize = 11.sp,
                                                fontStyle = FontStyle.Italic,
                                                color = Color.Gray,
                                                modifier = Modifier.weight(1f)
                                            )
                                            
                                            Button(
                                                onClick = {
                                                    val generatedRec = when (vClass.platform) {
                                                        "Zoom" -> "https://zoom.us/rec/play-class-" + System.currentTimeMillis() % 100000
                                                        "Google Meet" -> "https://meet.google.com/rec/play-" + System.currentTimeMillis() % 100000
                                                        "Loom" -> "https://loom.com/share/rec-" + System.currentTimeMillis() % 100000
                                                        "Microsoft Teams" -> "https://teams.microsoft.com/rec/play-" + System.currentTimeMillis() % 100000
                                                        else -> "https://youtube.com/watch?v=live-class-rec"
                                                    }
                                                    viewModel.updateVirtualClass(vClass.copy(recordingUrl = generatedRec))
                                                    Toast.makeText(context, "RECORDING PUBLISHED: Auto-recording published to syllabus!", Toast.LENGTH_LONG).show()
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = dynamicGold),
                                                shape = RoundedCornerShape(4.dp),
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                            ) {
                                                Text("Simulate Record", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                                
                                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
                                
                                // Main Action & Delete Row
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Button(
                                        onClick = {
                                            Toast.makeText(context, "LAUNCHING MEETING: Launching virtual ${vClass.platform} lecture room!", Toast.LENGTH_LONG).show()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = dynamicPrimary),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text("Launch Live", fontSize = 11.sp)
                                    }
                                    IconButton(onClick = { viewModel.deleteVirtualClass(vClass) }) {
                                        Icon(Icons.Default.Delete, "Delete", tint = Color.Gray)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // View resources & upload tools
        item {
            ControlRoomSectionCard(title = "Syllabus Resource Library", icon = Icons.Default.Source, dynamicPrimary) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    resources.forEach { r ->
                        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = BrandWhite)) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(if (r.type == "Document") Icons.Default.PictureAsPdf else Icons.Default.PlayCircle, null, tint = dynamicPrimary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(r.title, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Text("${r.type} • ${r.durationOrSize}", fontSize = 11.sp, color = MutedCharcoal)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// =======================================================
// FACILITATOR STUDENT LOGS TAB
// =======================================================
@Composable
fun FacilitatorStudentLogsTab(
    viewModel: LmsViewModel,
    users: List<UserAccount>,
    attendance: List<AttendanceRecord>,
    dynamicPrimary: Color,
    dynamicGold: Color
) {
    var showAddAttendance by remember { mutableStateOf(false) }
    var selectedUserId by remember { mutableIntStateOf(0) }
    var selectedCourseName by remember { mutableStateOf("Architecting Systems for Scale") }
    var attendanceStatus by remember { mutableStateOf("Present") }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            Text("Learner Academic Records & Registration Bureau", fontWeight = FontWeight.Black, fontSize = 18.sp, color = DarkCharcoal)
        }

        // Log attendance records
        item {
            val learners = users.filter { it.role == "Learner" }
            ControlRoomSectionCard(title = "Log Classroom Attendance Register", icon = Icons.Default.CoPresent, dynamicPrimary) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Attendance Matrix Logs (${attendance.size})", fontWeight = FontWeight.Bold)
                        Button(
                            onClick = { 
                                if (learners.isNotEmpty()) {
                                    selectedUserId = learners.first().id
                                    showAddAttendance = !showAddAttendance 
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = dynamicPrimary),
                            enabled = learners.isNotEmpty()
                        ) {
                            Text(if (showAddAttendance) "Collapse" else "Log Attendance")
                        }
                    }

                    if (showAddAttendance && learners.isNotEmpty()) {
                        Card(colors = CardDefaults.cardColors(containerColor = BrandCream), border = BorderStroke(1.dp, dynamicGold.copy(alpha = 0.5f))) {
                            Column(modifier = Modifier.padding(12.dp).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("Select Learner:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    learners.take(3).forEach { l ->
                                        ElevatedFilterChip(
                                            selected = selectedUserId == l.id,
                                            onClick = { selectedUserId = l.id },
                                            label = { Text(l.name.split(" ").first()) }
                                        )
                                    }
                                }
                                Text("Syllabus Subject Module:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                listOf("Architecting Systems for Scale", "Executive Leadership & Resilience").forEach { course ->
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        RadioButton(selected = selectedCourseName == course, onClick = { selectedCourseName = course })
                                        Text(course, fontSize = 12.sp)
                                    }
                                }
                                Text("Daily Attendance Level:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    listOf("Present", "Absent", "Late").forEach { stat ->
                                        ElevatedFilterChip(
                                            selected = attendanceStatus == stat,
                                            onClick = { attendanceStatus = stat },
                                            label = { Text(stat) }
                                        )
                                    }
                                }
                                Button(
                                    onClick = {
                                        val u = learners.find { it.id == selectedUserId }
                                        if (u != null) {
                                            viewModel.logAttendance(u.id, u.name, selectedCourseName, attendanceStatus)
                                            showAddAttendance = false
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = dynamicPrimary),
                                    modifier = Modifier.fillMaxWidth().testTag("btn_save_attendance")
                                ) {
                                    Text("Record Attendance Ledger")
                                }
                            }
                        }
                    }

                    attendance.forEach { att ->
                        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = BrandWhite)) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Person, null, tint = dynamicPrimary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(att.userName, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Text(att.courseName, fontSize = 11.sp, color = MutedCharcoal)
                                }
                                Badge(
                                    containerColor = when (att.status) {
                                        "Present" -> PrimaryGreen
                                        "Late" -> dynamicGold
                                        else -> Color.Red
                                    },
                                    contentColor = BrandWhite
                                ) {
                                    Text(att.status)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// =======================================================
// CORPORATE CLIENT ENTERPRISE PORTAL TABS
// =======================================================
@Composable
fun CorporateClientCohortTab(
    viewModel: LmsViewModel,
    users: List<UserAccount>,
    dynamicPrimary: Color,
    dynamicGold: Color
) {
    val companies by viewModel.companies.collectAsStateWithLifecycle()
    val progressList by viewModel.userCourseProgress.collectAsStateWithLifecycle()
    val courses by viewModel.courses.collectAsStateWithLifecycle()

    var selectedCompany by remember { mutableStateOf<Company?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var expandedUserEmail by remember { mutableStateOf<String?>(null) }

    if (selectedCompany == null && companies.isNotEmpty()) {
        selectedCompany = companies[0]
    }

    val activeCompany = selectedCompany
    val companyPrimary = remember(activeCompany) {
        activeCompany?.primaryColorHex?.let { Color(android.graphics.Color.parseColor(it)) } ?: dynamicPrimary
    }
    val companyGold = remember(activeCompany) {
        activeCompany?.accentColorHex?.let { Color(android.graphics.Color.parseColor(it)) } ?: dynamicGold
    }

    // Filter employees belonging to the selected company
    val companyEmployees = users.filter { 
        it.role == "Learner" && it.company.equals(activeCompany?.name ?: "", ignoreCase = true)
    }

    // Calculate overall metrics
    val totalEmployees = companyEmployees.size
    val companyEmployeeIds = companyEmployees.map { it.id }
    val companyProgress = progressList.filter { it.userId in companyEmployeeIds }
    
    val avgProgress = if (companyProgress.isNotEmpty()) {
        companyProgress.map { it.progress }.average()
    } else 0.0

    val avgGrade = if (companyProgress.isNotEmpty()) {
        companyProgress.map { it.grade }.average().toInt()
    } else 0

    val compliantCount = companyProgress.count { it.complianceStatus == "Compliant" }
    val totalProgressRecords = companyProgress.size
    val complianceRate = if (totalProgressRecords > 0) {
        (compliantCount.toDouble() / totalProgressRecords * 100).toInt()
    } else 100

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Company Selector Row
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Enterprise Client Portals",
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    color = DarkCharcoal
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                // Horizontal list of companies for portal selection
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    companies.forEach { company ->
                        val isSelected = company.id == activeCompany?.id
                        val borderCol = if (isSelected) companyPrimary else Color.LightGray.copy(alpha = 0.5f)
                        val bgCol = if (isSelected) companyPrimary.copy(alpha = 0.08f) else BrandWhite
                        
                        Card(
                            modifier = Modifier
                                .width(160.dp)
                                .clickable { selectedCompany = company },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = bgCol),
                            border = BorderStroke(if (isSelected) 2.dp else 1.dp, borderCol)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(Color(android.graphics.Color.parseColor(company.primaryColorHex))),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = company.name.take(1),
                                            color = BrandWhite,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = company.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        maxLines = 1,
                                        color = DarkCharcoal
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Portal Theme Active",
                                    fontSize = 10.sp,
                                    color = if (isSelected) companyPrimary else MutedCharcoal,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }
        }

        // Custom Branded Company Hero Banner
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = BrandWhite),
                border = BorderStroke(1.5.dp, companyPrimary.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = activeCompany?.name ?: "No Company Selected",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 20.sp,
                                color = companyPrimary
                            )
                            Text(
                                text = activeCompany?.motto ?: "Academy Excellence",
                                fontSize = 12.sp,
                                fontStyle = FontStyle.Italic,
                                color = MutedCharcoal
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.Business,
                            contentDescription = null,
                            tint = companyPrimary,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Metrics Grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Total Learners
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = companyPrimary.copy(alpha = 0.05f))
                        ) {
                            Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Employees", fontSize = 10.sp, color = MutedCharcoal)
                                Text(
                                    text = "$totalEmployees",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 16.sp,
                                    color = companyPrimary
                                )
                            }
                        }
                        
                        // Average Progress
                        Card(
                            modifier = Modifier.weight(1.2f),
                            colors = CardDefaults.cardColors(containerColor = companyPrimary.copy(alpha = 0.05f))
                        ) {
                            Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Avg Progress", fontSize = 10.sp, color = MutedCharcoal)
                                Text(
                                    text = "${(avgProgress * 100).toInt()}%",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 16.sp,
                                    color = companyPrimary
                                )
                            }
                        }

                        // Average Grade
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = companyPrimary.copy(alpha = 0.05f))
                        ) {
                            Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Avg Grade", fontSize = 10.sp, color = MutedCharcoal)
                                Text(
                                    text = "$avgGrade%",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 16.sp,
                                    color = companyGold
                                )
                            }
                        }

                        // Compliance Rating
                        Card(
                            modifier = Modifier.weight(1.2f),
                            colors = CardDefaults.cardColors(containerColor = companyPrimary.copy(alpha = 0.05f))
                        ) {
                            Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Compliance", fontSize = 10.sp, color = MutedCharcoal)
                                Text(
                                    text = "$complianceRate%",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 16.sp,
                                    color = if (complianceRate >= 80) PrimaryGreen else companyGold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section Title & Search
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Sponsored Employees Progress Tracker",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = DarkCharcoal
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search employees...") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                shape = RoundedCornerShape(12.dp)
            )
        }

        // Employee Rows
        val filteredEmployees = companyEmployees.filter {
            it.name.contains(searchQuery, ignoreCase = true) || it.email.contains(searchQuery, ignoreCase = true)
        }

        if (filteredEmployees.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                    Text("No sponsored employees registered for this company.", color = MutedCharcoal, fontSize = 12.sp)
                }
            }
        } else {
            items(filteredEmployees) { employee ->
                val isExpanded = expandedUserEmail == employee.email
                val employeeProgress = progressList.filter { it.userId == employee.id }
                
                val empProgressVal = if (employeeProgress.isNotEmpty()) {
                    employeeProgress.map { it.progress }.average()
                } else 0.0

                val empGradeVal = if (employeeProgress.isNotEmpty()) {
                    employeeProgress.map { it.grade }.average().toInt()
                } else 0

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = BrandWhite),
                    border = BorderStroke(1.dp, if (isExpanded) companyPrimary else Color.LightGray.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { 
                                expandedUserEmail = if (isExpanded) null else employee.email 
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = null,
                                tint = companyPrimary,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(employee.name, fontWeight = FontWeight.Black, fontSize = 14.sp, color = DarkCharcoal)
                                Text(employee.email, fontSize = 11.sp, color = MutedCharcoal)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Avg Grade: $empGradeVal%", fontWeight = FontWeight.Bold, color = companyGold, fontSize = 12.sp)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = if (isExpanded) "Collapse" else "Expand Progress",
                                        fontSize = 11.sp,
                                        color = companyPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Icon(
                                        imageVector = if (isExpanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                                        contentDescription = null,
                                        tint = companyPrimary
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Syllabus completion progress:", fontSize = 11.sp, color = MutedCharcoal)
                            Text("${(empProgressVal * 100).toInt()}%", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = companyPrimary)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { empProgressVal.toFloat() },
                            color = companyPrimary,
                            trackColor = BrandCream,
                            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp))
                        )

                        if (isExpanded) {
                            Spacer(modifier = Modifier.height(16.dp))
                            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.4f))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Course-By-Course Academy Progress",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = companyPrimary
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            if (employeeProgress.isEmpty()) {
                                Text("No active courses enrolled yet.", fontSize = 11.sp, fontStyle = FontStyle.Italic)
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    employeeProgress.forEach { prog ->
                                        val c = courses.find { it.id == prog.courseId }
                                        val courseName = c?.name ?: "Corporate Course"
                                        val courseCode = c?.code ?: "CORP"
                                        
                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = CardDefaults.cardColors(containerColor = BrandCream.copy(alpha = 0.3f)),
                                            border = BorderStroke(0.5.dp, Color.LightGray.copy(alpha = 0.5f))
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text("$courseCode: $courseName", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = DarkCharcoal)
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        LinearProgressIndicator(
                                                            progress = { prog.progress.toFloat() },
                                                            color = companyPrimary,
                                                            trackColor = Color.LightGray.copy(alpha = 0.3f),
                                                            modifier = Modifier.width(100.dp).height(4.dp).clip(CircleShape)
                                                        )
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        Text("${(prog.progress * 100).toInt()}% done", fontSize = 10.sp, color = MutedCharcoal)
                                                    }
                                                }
                                                Column(horizontalAlignment = Alignment.End) {
                                                    Text("Grade: ${prog.grade}%", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = companyGold)
                                                    Spacer(modifier = Modifier.height(2.dp))
                                                    val badgeColor = when (prog.complianceStatus) {
                                                        "Compliant" -> PrimaryGreen
                                                        "In Progress" -> companyGold
                                                        else -> Color.Red
                                                    }
                                                    Badge(containerColor = badgeColor, contentColor = BrandWhite) {
                                                        Text(prog.complianceStatus, fontSize = 8.sp, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
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
        }
    }
}

@Composable
fun CorporateAuditAndReportsTab(
    viewModel: LmsViewModel,
    users: List<UserAccount>,
    dynamicPrimary: Color,
    dynamicGold: Color
) {
    val context = LocalContext.current
    val companies by viewModel.companies.collectAsStateWithLifecycle()
    val progressList by viewModel.userCourseProgress.collectAsStateWithLifecycle()
    val courses by viewModel.courses.collectAsStateWithLifecycle()
    val attendanceRecords by viewModel.attendance.collectAsStateWithLifecycle()

    var selectedCompany by remember { mutableStateOf<Company?>(null) }
    var activeSubTab by remember { mutableStateOf("Compliance") } // "Compliance", "Attendance", "Certificates"
    
    // For WSP Dialog
    var showWspDialog by remember { mutableStateOf(false) }
    // For Attendance Register Dialog
    var selectedCourseForAttendance by remember { mutableStateOf<Course?>(null) }
    var showAttendanceSheet by remember { mutableStateOf(false) }
    // For Certificate Dialog
    var activeCertificateEmployeeName by remember { mutableStateOf("") }
    var activeCertificateCourseName by remember { mutableStateOf("") }
    var activeCertificateGrade by remember { mutableStateOf(0) }
    var activeCertificateCode by remember { mutableStateOf("") }
    var showCertificateDialog by remember { mutableStateOf(false) }

    if (selectedCompany == null && companies.isNotEmpty()) {
        selectedCompany = companies[0]
    }

    val activeCompany = selectedCompany
    val companyPrimary = remember(activeCompany) {
        activeCompany?.primaryColorHex?.let { Color(android.graphics.Color.parseColor(it)) } ?: dynamicPrimary
    }
    val companyGold = remember(activeCompany) {
        activeCompany?.accentColorHex?.let { Color(android.graphics.Color.parseColor(it)) } ?: dynamicGold
    }

    val companyEmployees = users.filter {
        it.role == "Learner" && it.company.equals(activeCompany?.name ?: "", ignoreCase = true)
    }
    val companyEmployeeIds = companyEmployees.map { it.id }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Corporate switcher selector
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${activeCompany?.name ?: "Corporate"} Audit Hub",
                fontWeight = FontWeight.Black,
                fontSize = 16.sp,
                color = DarkCharcoal
            )
            
            // Fast switch Row
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                companies.take(3).forEach { co ->
                    val isS = co.id == activeCompany?.id
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(if (isS) companyPrimary else Color.LightGray.copy(alpha = 0.5f))
                            .clickable { selectedCompany = co },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(co.name.take(1), color = BrandWhite, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))

        // Three Sub-tabs layout
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            listOf("Compliance", "Attendance", "Certificates").forEach { subTab ->
                val isActive = activeSubTab == subTab
                Button(
                    onClick = { activeSubTab = subTab },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isActive) companyPrimary else Color.LightGray.copy(alpha = 0.2f),
                        contentColor = if (isActive) BrandWhite else DarkCharcoal
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    Text(subTab, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (activeSubTab) {
            "Compliance" -> {
                // Compliance dashboard
                val companyProgress = progressList.filter { it.userId in companyEmployeeIds }
                val compliantProgress = companyProgress.filter { it.complianceStatus == "Compliant" }
                val inProgressProgress = companyProgress.filter { it.complianceStatus == "In Progress" }
                val nonCompliantProgress = companyProgress.filter { it.complianceStatus == "Non-Compliant" }

                LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.weight(1f)) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = BrandWhite),
                            border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Legislative & Quality Auditing Tracker", fontWeight = FontWeight.Bold, color = companyPrimary)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Dynamic compliance monitoring of FICA, FAIS, POPIA, and academic modules required for WSP/ATR SETA auditing and SDL rebates.", fontSize = 12.sp, color = MutedCharcoal)
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                        Text("Compliant", fontSize = 11.sp, color = MutedCharcoal)
                                        Text("${compliantProgress.size}", fontWeight = FontWeight.Bold, color = PrimaryGreen, fontSize = 18.sp)
                                    }
                                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                        Text("In Progress", fontSize = 11.sp, color = MutedCharcoal)
                                        Text("${inProgressProgress.size}", fontWeight = FontWeight.Bold, color = companyGold, fontSize = 18.sp)
                                    }
                                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                        Text("Non-Compliant", fontSize = 11.sp, color = MutedCharcoal)
                                        Text("${nonCompliantProgress.size}", fontWeight = FontWeight.Bold, color = Color.Red, fontSize = 18.sp)
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Text("Mandatory Legislative Trackers", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = DarkCharcoal)
                    }

                    // POPIA, FICA, FAIS, Compliance individual summaries
                    val legislativeCourseCodes = listOf("CORP-POPIA", "CORP-FICA", "CORP-FAIS", "CORP-COMP")
                    items(legislativeCourseCodes) { code ->
                        val courseObj = courses.find { it.code == code }
                        if (courseObj != null) {
                            val courseProgress = companyProgress.filter { it.courseId == courseObj.id }
                            val compCount = courseProgress.count { it.complianceStatus == "Compliant" }
                            val totalCount = companyEmployees.size
                            val rate = if (totalCount > 0) (compCount.toDouble() / totalCount * 100).toInt() else 100

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = BrandWhite),
                                border = BorderStroke(1.dp, companyPrimary.copy(alpha = 0.1f))
                            ) {
                                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (rate >= 80) Icons.Default.Verified else Icons.Default.Error,
                                        contentDescription = null,
                                        tint = if (rate >= 80) PrimaryGreen else companyGold,
                                        modifier = Modifier.size(32.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(courseObj.name, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = DarkCharcoal)
                                        Text("Compliant employees: $compCount / $totalCount", fontSize = 11.sp, color = MutedCharcoal)
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("$rate% Compliant", fontWeight = FontWeight.Black, fontSize = 13.sp, color = if (rate >= 80) PrimaryGreen else companyGold)
                                        LinearProgressIndicator(
                                            progress = { rate.toFloat() / 100f },
                                            color = if (rate >= 80) PrimaryGreen else companyGold,
                                            trackColor = Color.LightGray.copy(alpha = 0.2f),
                                            modifier = Modifier.width(80.dp).height(4.dp).clip(CircleShape)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Generate WSP / ATR Reports Button
                    item {
                        Button(
                            onClick = { showWspDialog = true },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = companyPrimary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Description, null, tint = BrandWhite)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Generate WSP & ATR Compliance Report", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = BrandWhite)
                        }
                    }
                }
            }

            "Attendance" -> {
                // Attendance Registers
                var selectedCourse by remember { mutableStateOf<Course?>(null) }
                val corpCourses = courses.filter { it.category == "Corporate Academy" }

                if (selectedCourse == null && corpCourses.isNotEmpty()) {
                    selectedCourse = corpCourses[0]
                }

                val activeCourse = selectedCourse

                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Select Course for Attendance Register:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DarkCharcoal)
                    
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        corpCourses.forEach { course ->
                            val isSel = course.id == activeCourse?.id
                            ElevatedFilterChip(
                                selected = isSel,
                                onClick = { selectedCourse = course },
                                label = { Text(course.name, fontSize = 11.sp) }
                            )
                        }
                    }

                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.4f))

                    Text(
                        text = "Active Attendance Register - ${activeCourse?.name ?: "Academy"}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = companyPrimary
                    )

                    // Gather attendance records
                    val companyEmployeeNames = companyEmployees.map { it.name }
                    val activeCourseAttendance = attendanceRecords.filter { 
                        it.courseName.equals(activeCourse?.name ?: "", ignoreCase = true) &&
                        it.userName in companyEmployeeNames
                    }

                    if (activeCourseAttendance.isEmpty()) {
                        Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                            Text("No virtual live class sessions registered for this course yet.", color = MutedCharcoal, fontSize = 12.sp)
                        }
                    } else {
                        LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(activeCourseAttendance) { att ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = BrandWhite),
                                    border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.2f))
                                ) {
                                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.CalendarToday,
                                            contentDescription = null,
                                            tint = companyPrimary,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(att.userName, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                            Text("Course: ${att.courseName}", fontSize = 10.sp, color = MutedCharcoal)
                                        }
                                        Badge(
                                            containerColor = if (att.status == "Present") PrimaryGreen else companyGold,
                                            contentColor = BrandWhite
                                        ) {
                                            Text(att.status, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 4.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Button(
                        onClick = { 
                            selectedCourseForAttendance = activeCourse
                            showAttendanceSheet = true 
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = companyPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Print, null, tint = BrandWhite)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Export Attendance Register", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = BrandWhite)
                    }
                }
            }

            "Certificates" -> {
                // Completion certificates lists
                val companyProgress = progressList.filter { it.userId in companyEmployeeIds }
                val completedRecords = companyProgress.filter { it.progress >= 0.8 }

                if (completedRecords.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                        Text("No employees have completed a course (>=80% progress) yet.", color = MutedCharcoal, fontSize = 12.sp)
                    }
                } else {
                    LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(completedRecords) { record ->
                            val emp = companyEmployees.find { it.id == record.userId }
                            val courseObj = courses.find { it.id == record.courseId }

                            if (emp != null && courseObj != null) {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = BrandWhite),
                                    border = BorderStroke(1.dp, companyGold.copy(alpha = 0.3f))
                                ) {
                                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Verified,
                                            contentDescription = null,
                                            tint = PrimaryGreen,
                                            modifier = Modifier.size(32.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(emp.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            Text("Course: ${courseObj.name}", fontSize = 11.sp, color = MutedCharcoal)
                                            Text("Grade Achieved: ${record.grade}%", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = companyGold)
                                        }
                                        Button(
                                            onClick = {
                                                activeCertificateEmployeeName = emp.name
                                                activeCertificateCourseName = courseObj.name
                                                activeCertificateGrade = record.grade
                                                activeCertificateCode = courseObj.code
                                                showCertificateDialog = true
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = companyPrimary),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                        ) {
                                            Text("Generate", fontSize = 11.sp, color = BrandWhite)
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

    // SDF / WSP & ATR Report Dialog
    if (showWspDialog) {
        AlertDialog(
            onDismissRequest = { showWspDialog = false },
            confirmButton = {
                Button(
                    onClick = {
                        Toast.makeText(context, "SDF Report Formally Signed and Submitted to SETA!", Toast.LENGTH_LONG).show()
                        showWspDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = companyPrimary)
                ) {
                    Text("Submit to SETA", color = BrandWhite)
                }
            },
            dismissButton = {
                TextButton(onClick = { showWspDialog = false }) {
                    Text("Close", color = companyPrimary)
                }
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Description, null, tint = companyPrimary, modifier = Modifier.size(28.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("WSP & ATR Legislative Report", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Card(colors = CardDefaults.cardColors(containerColor = BrandCream)) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("ENTERPRISE PROFILE", fontWeight = FontWeight.Black, fontSize = 11.sp, color = companyPrimary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Employer Name: ${activeCompany?.name}", fontSize = 11.sp)
                            Text("Motto/Alignment: ${activeCompany?.motto}", fontSize = 11.sp)
                            Text("Designated SDF Facilitator: Dir. Sarah Connor", fontSize = 11.sp)
                        }
                    }

                    Text(
                        text = "Workplace Skills Plan (WSP) 2026/2027",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = companyPrimary
                    )
                    Text(
                        text = "Planned learning interventions mapped to priority skills frameworks, including leadership development, operational communication, customer intelligence, and mandatory anti-financial crime compliance:",
                        fontSize = 11.sp,
                        color = MutedCharcoal
                    )

                    val plannedInterventions = listOf(
                        "POPIA Operational Risk Framework Implementation",
                        "FICA & AML Mandatory Compliance Awareness Campaigns",
                        "Emotional Intelligence Interpersonal Training committees",
                        "Time Management deep-work alignment workshops"
                    )
                    plannedInterventions.forEach {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, null, tint = companyPrimary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(it, fontSize = 11.sp, color = DarkCharcoal)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider()

                    Text(
                        text = "Annual Training Report (ATR) 2025/2026",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = companyPrimary
                    )
                    Text(
                        text = "Consolidated report of actual learning hours delivered, certification completions, and skill progression ratings inside the Corporate Academy catalog:",
                        fontSize = 11.sp,
                        color = MutedCharcoal
                    )

                    val statistics = listOf(
                        "Total Sponsored Employees: ${companyEmployees.size}",
                        "Compliance Target Accomplishment: 100% compliant modules",
                        "Designated Skills Levy Contribution Rebate Estimated: R185,420.00"
                    )
                    statistics.forEach {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Verified, null, tint = PrimaryGreen, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(it, fontSize = 11.sp, color = DarkCharcoal, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            },
            shape = RoundedCornerShape(16.dp),
            containerColor = BrandWhite
        )
    }

    // Attendance register report dialog
    if (showAttendanceSheet) {
        val companyEmployeeNames = companyEmployees.map { it.name }
        val courseAttendance = attendanceRecords.filter { 
            it.courseName.equals(selectedCourseForAttendance?.name ?: "", ignoreCase = true) &&
            it.userName in companyEmployeeNames
        }

        AlertDialog(
            onDismissRequest = { showAttendanceSheet = false },
            confirmButton = {
                Button(
                    onClick = {
                        Toast.makeText(context, "Attendance Register Exported successfully!", Toast.LENGTH_SHORT).show()
                        showAttendanceSheet = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = companyPrimary)
                ) {
                    Text("Download PDF", color = BrandWhite)
                }
            },
            title = {
                Text(text = "Attendance Sheet - ${selectedCourseForAttendance?.name}", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Official Academy Live attendance registers verified on ${formatClassDateTime(System.currentTimeMillis())}:", fontSize = 11.sp, color = MutedCharcoal)
                    Spacer(modifier = Modifier.height(4.dp))
                    courseAttendance.forEach { att ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(att.userName, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                Text("Class: Live Broadcast Lab", fontSize = 9.sp, color = MutedCharcoal)
                            }
                            Badge(
                                containerColor = if (att.status == "Present") PrimaryGreen else companyGold,
                                contentColor = BrandWhite
                            ) {
                                Text(att.status, fontSize = 9.sp)
                            }
                        }
                        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
                    }
                }
            },
            shape = RoundedCornerShape(16.dp),
            containerColor = BrandWhite
        )
    }

    // Completion Certificate Dialog
    if (showCertificateDialog) {
        AlertDialog(
            onDismissRequest = { showCertificateDialog = false },
            confirmButton = {
                Button(
                    onClick = {
                        Toast.makeText(context, "Certificate exported as high-resolution PDF!", Toast.LENGTH_LONG).show()
                        showCertificateDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = companyPrimary)
                ) {
                    Icon(Icons.Default.Download, null, tint = BrandWhite, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Download PDF", color = BrandWhite)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCertificateDialog = false }) {
                    Text("Cancel", color = companyPrimary)
                }
            },
            title = null,
            text = {
                // Highly elegant, classical visual Certificate representation
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = BrandWhite),
                    border = BorderStroke(4.dp, companyGold),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Header Seal
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = null,
                            tint = companyGold,
                            modifier = Modifier.size(48.dp)
                        )
                        
                        Text(
                            text = "ROOTED CORPORATE ACADEMY",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = companyPrimary,
                            letterSpacing = 2.sp
                        )

                        Text(
                            text = "CERTIFICATE OF COMPLETION",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkCharcoal
                        )

                        Text(
                            text = "This credential of mastery is hereby awarded to",
                            fontSize = 10.sp,
                            color = MutedCharcoal,
                            fontStyle = FontStyle.Italic
                        )

                        Text(
                            text = activeCertificateEmployeeName.uppercase(),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = companyPrimary
                        )

                        Text(
                            text = "for outstanding academic completion of the executive curriculum of",
                            fontSize = 10.sp,
                            color = MutedCharcoal,
                            fontStyle = FontStyle.Italic
                        )

                        Text(
                            text = activeCertificateCourseName,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkCharcoal,
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = "Course Code: $activeCertificateCode  |  Passed with Distinction: $activeCertificateGrade%",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = companyGold
                        )

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))

                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Dir. Sarah Connor", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                Text("ACADEMY FOUNDER", fontSize = 8.sp, color = MutedCharcoal)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Rooted Board of Regents", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                Text("ACCREDITING AUTHORITY", fontSize = 8.sp, color = MutedCharcoal)
                            }
                        }
                    }
                }
            },
            shape = RoundedCornerShape(12.dp),
            containerColor = BrandWhite
        )
    }
}

// =======================================================
// CORPORATE BILLING AND HIRING TAB
// =======================================================
@Composable
fun CorporateBillingAndHiringTab(
    viewModel: LmsViewModel,
    payments: List<Payment>,
    placements: List<Placement>,
    dynamicPrimary: Color,
    dynamicGold: Color
) {
    val context = LocalContext.current
    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            Text("Enterprise Financial Ledger & Placements Bureau", fontWeight = FontWeight.Black, fontSize = 18.sp, color = DarkCharcoal)
        }

        // Corporate Sponsorship invoices
        item {
            ControlRoomSectionCard(title = "Corporate Sponsorship Accounts", icon = Icons.Default.Payments, dynamicPrimary) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    payments.take(2).forEach { invoice ->
                        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = BrandWhite)) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Receipt, null, tint = dynamicPrimary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(invoice.clientName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("Amount: $${"%,.2f".format(invoice.amount)}", fontSize = 11.sp, color = MutedCharcoal)
                                }
                                Badge(
                                    containerColor = if (invoice.status == "Paid") PrimaryGreen else dynamicGold,
                                    contentColor = BrandWhite
                                ) {
                                    Text(invoice.status)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                if (invoice.status != "Paid") {
                                    Button(
                                        onClick = {
                                            viewModel.updatePaymentStatus(invoice, "Paid")
                                            Toast.makeText(context, "Sponsorship Fund Transfer Authorized Successfully!", Toast.LENGTH_LONG).show()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = dynamicGold),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text("Authorize", fontSize = 10.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Recruitment candidates
        item {
            ControlRoomSectionCard(title = "Hired Talents Pipeline", icon = Icons.Default.Work, dynamicPrimary) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    placements.forEach { candidate ->
                        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = BrandWhite)) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.ContactMail, null, tint = dynamicGold)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(candidate.learnerName, fontWeight = FontWeight.Bold)
                                    Text("Role: ${candidate.role} at ${candidate.partnerName}", fontSize = 11.sp, color = MutedCharcoal)
                                }
                                Badge(containerColor = dynamicPrimary) { Text(candidate.status) }
                            }
                        }
                    }
                }
            }
        }
    }
}

// =======================================================
// LEARNER SYLLABUS TAB
// =======================================================
@Composable
fun LearnerSyllabusTab(
    courses: List<Course>,
    resources: List<ResourceMedia>,
    quizzes: List<Quiz>,
    dynamicPrimary: Color,
    dynamicGold: Color
) {
    val context = LocalContext.current
    var activeCourseSection by remember { mutableStateOf<Course?>(null) }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            Text("Active Syllabus & Curriculum Library", fontWeight = FontWeight.Black, fontSize = 18.sp, color = DarkCharcoal)
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = BrandWhite),
                border = BorderStroke(1.dp, dynamicGold.copy(alpha = 0.3f))
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.School, null, tint = dynamicPrimary, modifier = Modifier.size(40.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Rooted in Knowledge.", fontWeight = FontWeight.Black, color = dynamicPrimary)
                        Text("Ready for Opportunity.", fontWeight = FontWeight.Bold, color = dynamicGold)
                    }
                }
            }
        }

        items(courses) { course ->
            val isExpanded = activeCourseSection?.id == course.id
            Card(
                modifier = Modifier.fillMaxWidth().clickable {
                    activeCourseSection = if (isExpanded) null else course
                },
                colors = CardDefaults.cardColors(containerColor = BrandWhite),
                border = BorderStroke(1.dp, parseHexColor(course.colorHex).copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(parseHexColor(course.colorHex)))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("${course.code}: ${course.name}", fontWeight = FontWeight.Black, color = parseHexColor(course.colorHex))
                            Text("Director: ${course.professor} • Credits: ${course.credits}", fontSize = 11.sp, color = MutedCharcoal)
                        }
                        Icon(if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, null, tint = Color.Gray)
                    }

                    if (isExpanded) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(course.description, fontSize = 12.sp, color = DarkCharcoal, fontStyle = FontStyle.Italic)
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Media resources list
                        Text("Syllabus Resource Attachments:", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = dynamicPrimary)
                        val courseRes = resources.filter { it.courseId == course.id }
                        if (courseRes.isEmpty()) {
                            Text("No attachments loaded yet for this module.", fontSize = 11.sp, color = Color.Gray)
                        } else {
                            courseRes.forEach { r ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = BrandCream),
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                                ) {
                                    Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = if (r.type == "Document") Icons.Default.PictureAsPdf else Icons.Default.PlayCircle,
                                            contentDescription = null,
                                            tint = dynamicPrimary
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(r.title, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                            Text("${r.type} • ${r.durationOrSize}", fontSize = 10.sp, color = MutedCharcoal)
                                        }
                                        Button(
                                            onClick = {
                                                Toast.makeText(context, "DOWNLOAD: Downloading ${r.title} securely onto device storage!", Toast.LENGTH_LONG).show()
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = dynamicPrimary),
                                            shape = RoundedCornerShape(4.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Text(if (r.type == "Document") "Get PDF" else "Watch Stream", fontSize = 9.sp)
                                        }
                                    }
                                }
                            }
                        }

                        // Quizzes list
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Module Quizzes:", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = dynamicPrimary)
                        val courseQuizzes = quizzes.filter { it.courseId == course.id }
                        if (courseQuizzes.isEmpty()) {
                            Text("No quiz modules scheduled yet.", fontSize = 11.sp, color = Color.Gray)
                        } else {
                            courseQuizzes.forEach { q ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = BrandCream),
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                                ) {
                                    Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.AssignmentTurnedIn, null, tint = dynamicGold)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(q.title, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                            Text("${q.questionsCount} Questions • Max grade ${q.maxScore}", fontSize = 10.sp, color = MutedCharcoal)
                                        }
                                        Button(
                                            onClick = {
                                                Toast.makeText(context, "SIMULATION SUCCESS: Quiz '${q.title}' submitted. Grade output: ${q.maxScore}/${q.maxScore} (Perfect 100%)", Toast.LENGTH_LONG).show()
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = dynamicGold),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text("Start Test", fontSize = 9.sp)
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

// =======================================================
// TIME FORMAT HELPER
// =======================================================
private fun formatTime(timestamp: Long): String {
    return java.text.SimpleDateFormat("MMM dd, yyyy HH:mm", java.util.Locale.getDefault()).format(java.util.Date(timestamp))
}

// =======================================================
// LEARNER PREMIUM DASHBOARD TAB
// =======================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LearnerDashboardTab(
    viewModel: LmsViewModel,
    courses: List<Course>,
    assignments: List<Assignment>,
    announcements: List<Announcement>,
    virtualClasses: List<VirtualClass>,
    resources: List<ResourceMedia>,
    quizzes: List<Quiz>,
    users: List<UserAccount>,
    dynamicPrimary: Color,
    dynamicGold: Color
) {
    val context = LocalContext.current
    
    // Find the learner details
    val learner = users.find { it.role == "Learner" } ?: UserAccount(
        id = 5,
        name = "Peter Parker",
        email = "peter.parker@student.com",
        role = "Learner",
        cohort = "Executive-2026",
        performance = 0.82,
        attendanceRate = 0.88,
        certificateIssued = false
    )
    
    // Local interactive states
    var showFacilitatorDialog by remember { mutableStateOf(false) }
    var facilitatorMessage by remember { mutableStateOf("") }
    var selectedFacilitator by remember { mutableStateOf("Prof. Angela Yu") }
    
    var showRecordingPlayer by remember { mutableStateOf(false) }
    var activeRecording by remember { mutableStateOf<ResourceMedia?>(null) }
    var isPlayingVideo by remember { mutableStateOf(true) }
    var videoProgress by remember { mutableFloatStateOf(0.35f) }
    
    var showQuizSimulator by remember { mutableStateOf(false) }
    var selectedQuiz by remember { mutableStateOf<Quiz?>(null) }
    
    var showCertificateDialog by remember { mutableStateOf(false) }
    
    // Simulate Download Workbook process
    var downloadProgress by remember { mutableStateOf<Float?>(null) }
    LaunchedEffect(downloadProgress) {
        if (downloadProgress != null) {
            while (downloadProgress!! < 1.0f) {
                kotlinx.coroutines.delay(150)
                downloadProgress = downloadProgress!! + 0.1f
            }
            Toast.makeText(context, "DOWNLOAD SUCCESS: 'executive_scale_workbook_2026.pdf' saved to downloads! 📂", Toast.LENGTH_LONG).show()
            downloadProgress = null
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome Header & Profile Photo & Streak
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = BrandWhite),
                border = BorderStroke(1.dp, dynamicGold.copy(alpha = 0.4f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = androidx.compose.ui.graphics.Brush.linearGradient(
                                colors = listOf(
                                    dynamicPrimary.copy(alpha = 0.04f),
                                    dynamicGold.copy(alpha = 0.08f)
                                )
                            )
                        )
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Profile photo placeholder
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(
                                        brush = androidx.compose.ui.graphics.Brush.linearGradient(
                                            colors = listOf(dynamicPrimary, dynamicGold)
                                        )
                                    )
                                    .border(2.dp, BrandWhite, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = learner.name.split(" ").mapNotNull { it.firstOrNull() }.joinToString("").uppercase(),
                                    color = Color.White,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 20.sp,
                                    letterSpacing = 1.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = "Welcome Back,",
                                    fontSize = 12.sp,
                                    color = MutedCharcoal,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = learner.name,
                                    fontSize = 22.sp,
                                    color = DarkCharcoal,
                                    fontWeight = FontWeight.Black
                                )
                                Text(
                                    text = "Cohort: ${learner.cohort}",
                                    fontSize = 11.sp,
                                    color = dynamicGold,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        
                        // Streak fire badge
                        Card(
                            colors = CardDefaults.cardColors(containerColor = dynamicGold.copy(alpha = 0.15f)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Bolt,
                                    contentDescription = "Streak",
                                    tint = dynamicGold,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "12 Days Streak 🔥",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black,
                                    color = dynamicPrimary
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = dynamicGold.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Motivational quote
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.FormatQuote,
                            contentDescription = "Quote",
                            tint = dynamicGold.copy(alpha = 0.7f),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "\"Success isn't about being perfect. It's about being relentless, resilient, and rooted in your corporate purpose.\"",
                            fontSize = 12.sp,
                            fontStyle = FontStyle.Italic,
                            fontWeight = FontWeight.Bold,
                            color = MutedCharcoal,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Quick Access Grid
        item {
            Text(
                text = "Quick Access Command Center",
                fontWeight = FontWeight.Black,
                fontSize = 14.sp,
                color = dynamicPrimary,
                letterSpacing = 0.5.sp
            )
        }
        
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Continue Learning
                    Button(
                        onClick = {
                            Toast.makeText(context, "Resuming your curriculum... Opening My Syllabus Tab", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = dynamicPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f).height(44.dp).testTag("continue_learning_button")
                    ) {
                        Icon(Icons.Default.PlayArrow, null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Continue Learning", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    // Download Workbook
                    Button(
                        onClick = {
                            if (downloadProgress == null) {
                                downloadProgress = 0.0f
                                Toast.makeText(context, "Starting Workbook Download...", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = dynamicGold),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f).height(44.dp).testTag("download_workbook_button")
                    ) {
                        if (downloadProgress != null) {
                            CircularProgressIndicator(
                                progress = { downloadProgress!! },
                                color = BrandWhite,
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Default.Download, null, modifier = Modifier.size(16.dp))
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (downloadProgress != null) "Downloading..." else "Download Workbook", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BrandWhite)
                    }
                }
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Join Live Class
                    val nextClass = virtualClasses.firstOrNull()
                    Button(
                        onClick = {
                            if (nextClass != null) {
                                Toast.makeText(context, "Connecting to Secure Zoom Room for: ${nextClass.title}... 🚀", Toast.LENGTH_LONG).show()
                            } else {
                                Toast.makeText(context, "No Live Classes currently scheduled.", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = dynamicPrimary.copy(alpha = 0.1f)),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, dynamicPrimary),
                        modifier = Modifier.weight(1f).height(44.dp).testTag("join_live_class_button")
                    ) {
                        Icon(Icons.Default.LiveTv, null, tint = dynamicPrimary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Join Live Class", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = dynamicPrimary)
                    }
                    // Watch Recording
                    val firstRecording = resources.firstOrNull { it.type == "Video" }
                    Button(
                        onClick = {
                            if (firstRecording != null) {
                                activeRecording = firstRecording
                                isPlayingVideo = true
                                videoProgress = 0.35f
                                showRecordingPlayer = true
                            } else {
                                Toast.makeText(context, "No Lecture recordings found.", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = dynamicGold.copy(alpha = 0.1f)),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, dynamicGold),
                        modifier = Modifier.weight(1f).height(44.dp).testTag("watch_recording_button")
                    ) {
                        Icon(Icons.Default.PlayCircleOutline, null, tint = dynamicGold, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Watch Recording", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = dynamicGold)
                    }
                }
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Take Quiz
                    val firstQuiz = quizzes.firstOrNull()
                    Button(
                        onClick = {
                            if (firstQuiz != null) {
                                selectedQuiz = firstQuiz
                                showQuizSimulator = true
                            } else {
                                Toast.makeText(context, "No Quizzes available.", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DarkCharcoal),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f).height(44.dp).testTag("take_quiz_button")
                    ) {
                        Icon(Icons.Default.HelpOutline, null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Take Quiz", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    // Message Facilitator
                    Button(
                        onClick = { showFacilitatorDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MutedCharcoal),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f).height(44.dp).testTag("message_facilitator_button")
                    ) {
                        Icon(Icons.Default.Chat, null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Message Facilitator", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Course Performance & Metrics
        item {
            Text(
                text = "Course Performance & Learning Metrics",
                fontWeight = FontWeight.Black,
                fontSize = 14.sp,
                color = dynamicPrimary,
                letterSpacing = 0.5.sp
            )
        }
        
        item {
            val activeCourse = courses.firstOrNull() ?: Course(
                name = "Architecting Systems for Scale",
                code = "TECH 401",
                professor = "Dr. Angela Yu",
                colorHex = "#1E5631",
                credits = 4,
                schedule = "Mon, Wed 10:00 AM"
            )
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = BrandWhite),
                border = BorderStroke(1.dp, dynamicGold.copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "CURRENT ACTIVE CURRICULUM",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = dynamicGold,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "${activeCourse.code}: ${activeCourse.name}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = DarkCharcoal
                            )
                            Text(
                                text = "Facilitator: ${activeCourse.professor} • Schedule: ${activeCourse.schedule}",
                                fontSize = 11.sp,
                                color = MutedCharcoal
                            )
                        }
                        
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(60.dp)) {
                            CircularProgressIndicator(
                                progress = { 0.82f },
                                color = dynamicPrimary,
                                strokeWidth = 6.dp,
                                modifier = Modifier.fillMaxSize()
                            )
                            Text(
                                text = "82%",
                                fontWeight = FontWeight.Black,
                                fontSize = 13.sp,
                                color = dynamicPrimary
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = dynamicGold.copy(alpha = 0.1f))
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = dynamicPrimary.copy(alpha = 0.03f)),
                            border = BorderStroke(1.dp, dynamicPrimary.copy(alpha = 0.1f))
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("STUDY HOURS", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = dynamicGold)
                                Text("38 / 50 hrs", fontSize = 14.sp, fontWeight = FontWeight.Black, color = dynamicPrimary)
                                Spacer(modifier = Modifier.height(4.dp))
                                LinearProgressIndicator(
                                    progress = { 38f / 50f },
                                    color = dynamicPrimary,
                                    trackColor = dynamicPrimary.copy(alpha = 0.1f),
                                    modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp))
                                )
                            }
                        }
                        
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = dynamicPrimary.copy(alpha = 0.03f)),
                            border = BorderStroke(1.dp, dynamicPrimary.copy(alpha = 0.1f))
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("QUIZ AVERAGE", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = dynamicGold)
                                Text("85.4%", fontSize = 14.sp, fontWeight = FontWeight.Black, color = dynamicPrimary)
                                Spacer(modifier = Modifier.height(4.dp))
                                LinearProgressIndicator(
                                    progress = { 0.854f },
                                    color = dynamicGold,
                                    trackColor = dynamicGold.copy(alpha = 0.1f),
                                    modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp))
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = dynamicPrimary.copy(alpha = 0.03f)),
                        border = BorderStroke(1.dp, dynamicPrimary.copy(alpha = 0.1f))
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("MOCK EXAM SCORES", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = dynamicGold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Assignment, null, tint = dynamicPrimary, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Mock Exam 1 (Foundations):", fontSize = 11.sp, color = DarkCharcoal)
                                }
                                Text("88% (Pass)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = dynamicPrimary)
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Assignment, null, tint = dynamicPrimary, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Mock Exam 2 (Advanced Scale):", fontSize = 11.sp, color = DarkCharcoal)
                                }
                                Text("92% (Excellent)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = dynamicPrimary)
                            }
                        }
                    }
                }
            }
        }

        // Career Milestones & Employment readiness
        item {
            Text(
                text = "Career & Corporate Integration Milestones",
                fontWeight = FontWeight.Black,
                fontSize = 14.sp,
                color = dynamicPrimary,
                letterSpacing = 0.5.sp
            )
        }
        
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = BrandWhite),
                border = BorderStroke(1.dp, dynamicGold.copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Business, null, tint = dynamicPrimary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Class of Business Training Progress", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = DarkCharcoal)
                            }
                            Text("75%", fontWeight = FontWeight.Black, fontSize = 12.sp, color = dynamicPrimary)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { 0.75f },
                            color = dynamicPrimary,
                            trackColor = dynamicPrimary.copy(alpha = 0.1f),
                            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp))
                        )
                        Text("Modules: Financial Statements Audit • Market Resilience • Brand Alignment Matrix", fontSize = 9.sp, color = MutedCharcoal)
                    }
                    
                    HorizontalDivider(color = dynamicGold.copy(alpha = 0.1f))
                    
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.TrendingUp, null, tint = dynamicGold, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Career Coaching & Mentorship", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = DarkCharcoal)
                            }
                            Text("60%", fontWeight = FontWeight.Black, fontSize = 12.sp, color = dynamicGold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { 0.60f },
                            color = dynamicGold,
                            trackColor = dynamicGold.copy(alpha = 0.1f),
                            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp))
                        )
                        Text("Current Step: Professional Portfolio Deployment & Pitch Optimization", fontSize = 9.sp, color = MutedCharcoal)
                    }
                    
                    HorizontalDivider(color = dynamicGold.copy(alpha = 0.1f))
                    
                    Column {
                        Text("EMPLOYMENT READINESS PIPELINE", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = dynamicGold, letterSpacing = 1.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        val steps = listOf(
                            Triple("1. Professional Profile Setup", "Complete & Verified by Mentor", true),
                            Triple("2. CV Critiqued & Uploaded", "Tailored for Technology roles", true),
                            Triple("3. Sponsor/Partner Interviewing", "Interview scheduled with Stark Industries", null),
                            Triple("4. Corporate Talent Placement", "Awaiting official offer letter", false)
                        )
                        
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            steps.forEach { (title, subtitle, status) ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clip(CircleShape)
                                            .background(
                                                when (status) {
                                                    true -> dynamicPrimary
                                                    null -> dynamicGold
                                                    false -> Color.LightGray
                                                }
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (status == true) {
                                            Icon(Icons.Default.Check, null, tint = BrandWhite, modifier = Modifier.size(10.dp))
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = title,
                                            fontSize = 11.sp,
                                            fontWeight = if (status != false) FontWeight.Bold else FontWeight.Normal,
                                            color = if (status != false) DarkCharcoal else MutedCharcoal
                                        )
                                        Text(
                                            text = subtitle,
                                            fontSize = 9.sp,
                                            color = MutedCharcoal
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Credentials & Badges
        item {
            Text(
                text = "Academic Credentials & Badges",
                fontWeight = FontWeight.Black,
                fontSize = 14.sp,
                color = dynamicPrimary,
                letterSpacing = 0.5.sp
            )
        }
        
        item {
            Card(
                modifier = Modifier.fillMaxWidth().clickable { showCertificateDialog = true },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = BrandWhite),
                border = BorderStroke(1.dp, dynamicGold.copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(dynamicGold.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = "Certificate",
                            tint = dynamicGold,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Executive Systems Architect Certificate",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            color = DarkCharcoal
                        )
                        Text(
                            text = "Requirements: 82% of 85% completed. Unlock at 85% course progress to automatically issue credentials.",
                            fontSize = 10.sp,
                            color = MutedCharcoal
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "More",
                        tint = Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Live Schedules, Assignments & Recordings
        item {
            Text(
                text = "Next Tasks, Class Schedule & Recordings",
                fontWeight = FontWeight.Black,
                fontSize = 14.sp,
                color = dynamicPrimary,
                letterSpacing = 0.5.sp
            )
        }
        
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = BrandWhite),
                border = BorderStroke(1.dp, dynamicGold.copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "UPCOMING LIVE ZOOM SESSIONS",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = dynamicGold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    if (virtualClasses.isEmpty()) {
                        Text("No live classes scheduled.", fontSize = 11.sp, color = MutedCharcoal)
                    } else {
                        virtualClasses.forEachIndexed { idx, session ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(session.title, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = DarkCharcoal)
                                    Text(
                                        text = "Scheduled: ${formatTime(session.scheduledTime)} • Platform: ${session.platform}",
                                        fontSize = 10.sp,
                                        color = MutedCharcoal
                                    )
                                }
                                Button(
                                    onClick = {
                                        Toast.makeText(context, "Opening ${session.platform} Secure Room for: ${session.title} 🚀", Toast.LENGTH_LONG).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = dynamicPrimary),
                                    shape = RoundedCornerShape(4.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Text("Join Link", fontSize = 10.sp)
                                }
                            }
                            if (idx < virtualClasses.size - 1) {
                                Spacer(modifier = Modifier.height(8.dp))
                                HorizontalDivider(color = dynamicGold.copy(alpha = 0.05f))
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }
            }
        }
        
        item {
            val nextAssignment = assignments.firstOrNull { !it.isCompleted }
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = BrandWhite),
                border = BorderStroke(1.dp, dynamicGold.copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "NEXT ACADEMIC DEADLINE",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = dynamicGold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    if (nextAssignment == null) {
                        Text("All assignments completed! Great work. 🎯", fontSize = 11.sp, color = dynamicPrimary, fontWeight = FontWeight.Bold)
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (nextAssignment.priority == "High") Color(0xFFFEE2E2) else Color(0xFFFEF3C7)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Assignment,
                                    contentDescription = "Task",
                                    tint = if (nextAssignment.priority == "High") Color.Red else dynamicGold,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = nextAssignment.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = DarkCharcoal
                                )
                                Text(
                                    text = "Due: ${formatTime(nextAssignment.dueDate)} • Priority: ${nextAssignment.priority}",
                                    fontSize = 10.sp,
                                    color = MutedCharcoal
                                )
                            }
                        }
                    }
                }
            }
        }
        
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = BrandWhite),
                border = BorderStroke(1.dp, dynamicGold.copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ACADEMY ANNOUNCEMENTS",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = dynamicGold,
                            letterSpacing = 1.sp
                        )
                        Icon(Icons.Default.Campaign, null, tint = dynamicGold, modifier = Modifier.size(16.dp))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    if (announcements.isEmpty()) {
                        Text("No announcements.", fontSize = 11.sp, color = MutedCharcoal)
                    } else {
                        announcements.forEachIndexed { index, item ->
                            Column {
                                Text(
                                    text = item.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = DarkCharcoal
                                )
                                Text(
                                    text = item.content,
                                    fontSize = 10.sp,
                                    color = MutedCharcoal
                                )
                                Text(
                                    text = "Posted by ${item.author} • ${formatTime(item.timestamp)}",
                                    fontSize = 8.sp,
                                    color = dynamicGold,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            if (index < announcements.size - 1) {
                                Spacer(modifier = Modifier.height(8.dp))
                                HorizontalDivider(color = dynamicGold.copy(alpha = 0.05f))
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }
            }
        }
        
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = BrandWhite),
                border = BorderStroke(1.dp, dynamicGold.copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "LATEST RECORDED LECTURES",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = dynamicGold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    val recordings = resources.filter { it.type == "Video" }
                    if (recordings.isEmpty()) {
                        Text("No lectures recorded yet.", fontSize = 11.sp, color = MutedCharcoal)
                    } else {
                        recordings.forEachIndexed { index, media ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Icon(
                                        imageVector = Icons.Default.PlayCircleOutline,
                                        contentDescription = "Video",
                                        tint = dynamicPrimary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(media.title, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = DarkCharcoal)
                                        Text("Duration: ${media.durationOrSize}", fontSize = 10.sp, color = MutedCharcoal)
                                    }
                                }
                                Button(
                                    onClick = {
                                        activeRecording = media
                                        isPlayingVideo = true
                                        videoProgress = 0.35f
                                        showRecordingPlayer = true
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = dynamicGold),
                                    shape = RoundedCornerShape(4.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Text("Play Video", fontSize = 10.sp)
                                }
                            }
                            if (index < recordings.size - 1) {
                                Spacer(modifier = Modifier.height(8.dp))
                                HorizontalDivider(color = dynamicGold.copy(alpha = 0.05f))
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }
            }
        }

        // Leaderboard
        item {
            Text(
                text = "Cohort Performance Leaderboard",
                fontWeight = FontWeight.Black,
                fontSize = 14.sp,
                color = dynamicPrimary,
                letterSpacing = 0.5.sp
            )
        }
        
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = BrandWhite),
                border = BorderStroke(1.dp, dynamicGold.copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "EXECUTIVE-2026 COHORT RANKINGS",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = dynamicGold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    val leaders = listOf(
                        Triple("1st", "Ned Leeds", "94% • Placed at Daily Bugle Systems"),
                        Triple("2nd", "Gwen Stacy", "92% • Offered at Oscorp Corp"),
                        Triple("3rd", "Peter Parker (You)", "82% • Interviewing at Stark Industries"),
                        Triple("4th", "Tony Stark", "80% • Enterprise Partner")
                    )
                    
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        leaders.forEach { (rank, name, desc) ->
                            val isMe = name.contains("Peter Parker")
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (isMe) dynamicPrimary.copy(alpha = 0.08f) else Color.Transparent
                                    )
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isMe) dynamicPrimary else dynamicGold.copy(alpha = 0.15f)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = rank.take(1),
                                        fontWeight = FontWeight.Black,
                                        fontSize = 11.sp,
                                        color = if (isMe) BrandWhite else dynamicPrimary
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = name,
                                        fontWeight = if (isMe) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 12.sp,
                                        color = if (isMe) dynamicPrimary else DarkCharcoal
                                    )
                                    Text(text = desc, fontSize = 9.sp, color = MutedCharcoal)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Direct Messaging Dialog
    if (showFacilitatorDialog) {
        Dialog(onDismissRequest = { showFacilitatorDialog = false }) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = BrandWhite),
                border = BorderStroke(1.dp, dynamicGold.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "SECURE FACILITATOR CHAT",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = dynamicGold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Send Direct Message",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = dynamicPrimary
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Prof. Angela Yu", "Dir. James Maxwell").forEach { fac ->
                            val isSelected = selectedFacilitator == fac
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedFacilitator = fac },
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected) dynamicPrimary else Color.LightGray
                                ),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) dynamicPrimary.copy(alpha = 0.08f) else Color.Transparent
                                )
                            ) {
                                Text(
                                    text = fac,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) dynamicPrimary else MutedCharcoal,
                                    modifier = Modifier.padding(8.dp).fillMaxWidth(),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                    
                    OutlinedTextField(
                        value = facilitatorMessage,
                        onValueChange = { facilitatorMessage = it },
                        placeholder = { Text("Type your query or request 1-on-1 career sync...", fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth().height(100.dp).testTag("facilitator_message_input"),
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = dynamicPrimary,
                            unfocusedBorderColor = Color.LightGray
                        )
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { showFacilitatorDialog = false }) {
                            Text("Cancel", color = MutedCharcoal)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (facilitatorMessage.trim().isEmpty()) {
                                    Toast.makeText(context, "Please enter a message first.", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(
                                        context,
                                        "Message securely delivered to $selectedFacilitator! They typically reply within 2 hours.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    facilitatorMessage = ""
                                    showFacilitatorDialog = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = dynamicPrimary),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Send Securely")
                        }
                    }
                }
            }
        }
    }

    // Video Player Dialog
    if (showRecordingPlayer && activeRecording != null) {
        Dialog(onDismissRequest = { showRecordingPlayer = false }) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkCharcoal)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "LMS VIDEO LECTURE STREAM",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = dynamicGold,
                            letterSpacing = 1.sp
                        )
                        IconButton(onClick = { showRecordingPlayer = false }) {
                            Icon(Icons.Default.Close, null, tint = BrandWhite)
                        }
                    }
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .background(Color.Black, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = if (isPlayingVideo) Icons.Default.PlayArrow else Icons.Default.Pause,
                                contentDescription = null,
                                tint = dynamicGold,
                                modifier = Modifier.size(44.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (isPlayingVideo) "Simulating Lecture Stream: Playing..." else "Paused",
                                color = BrandWhite,
                                fontSize = 11.sp
                            )
                        }
                    }
                    
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = activeRecording!!.title,
                            color = BrandWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "Course Resource • ${activeRecording!!.durationOrSize}",
                            color = Color.LightGray,
                            fontSize = 10.sp
                        )
                    }
                    
                    Slider(
                        value = videoProgress,
                        onValueChange = { videoProgress = it },
                        colors = SliderDefaults.colors(
                            thumbColor = dynamicGold,
                            activeTrackColor = dynamicGold,
                            inactiveTrackColor = Color.Gray
                        )
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Button(
                            onClick = { isPlayingVideo = !isPlayingVideo },
                            colors = ButtonDefaults.buttonColors(containerColor = dynamicGold),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(if (isPlayingVideo) "Pause" else "Play", color = DarkCharcoal, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                        
                        Button(
                            onClick = {
                                Toast.makeText(context, "Offline copy downloaded to cache!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Download Offline", color = BrandWhite, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }

    // Quiz Simulator Dialog
    if (showQuizSimulator && selectedQuiz != null) {
        var activeQuestionIndex by remember { mutableIntStateOf(0) }
        var selectedAnswerIndex by remember { mutableStateOf<Int?>(null) }
        var quizFinished by remember { mutableStateOf(false) }
        
        val questions = listOf(
            Pair("Which of the following describes CAP Theorem's Consistency?", listOf("All nodes see the same data at the same time", "Every request receives a response", "The system continues to operate despite partitions", "Data is encrypted securely")),
            Pair("What is the primary trade-off of Active-Active replication?", listOf("Increased latency for consistency validation", "Simpler disaster recovery", "Higher storage requirements on client side", "Decreased cluster performance"))
        )
        
        Dialog(onDismissRequest = { showQuizSimulator = false }) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = BrandWhite),
                border = BorderStroke(1.dp, dynamicGold.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "SECURE EXAMS MATRIX",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = dynamicGold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Q ${activeQuestionIndex + 1} of ${questions.size}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = dynamicPrimary
                        )
                    }
                    
                    Text(
                        text = selectedQuiz!!.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        color = DarkCharcoal
                    )
                    
                    HorizontalDivider(color = dynamicGold.copy(alpha = 0.1f))
                    
                    if (!quizFinished) {
                        val (questionText, options) = questions[activeQuestionIndex]
                        Text(
                            text = questionText,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkCharcoal
                        )
                        
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            options.forEachIndexed { optIdx, option ->
                                val isSelected = selectedAnswerIndex == optIdx
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selectedAnswerIndex = optIdx },
                                    border = BorderStroke(
                                        1.dp,
                                        if (isSelected) dynamicPrimary else Color.LightGray
                                    ),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected) dynamicPrimary.copy(alpha = 0.08f) else Color.Transparent
                                    )
                                ) {
                                    Text(
                                        text = option,
                                        fontSize = 11.sp,
                                        color = DarkCharcoal,
                                        modifier = Modifier.padding(12.dp).fillMaxWidth()
                                    )
                                }
                            }
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Button(
                                onClick = {
                                    if (selectedAnswerIndex == null) {
                                        Toast.makeText(context, "Please select an answer option.", Toast.LENGTH_SHORT).show()
                                    } else {
                                        if (activeQuestionIndex < questions.size - 1) {
                                            activeQuestionIndex++
                                            selectedAnswerIndex = null
                                        } else {
                                            quizFinished = true
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = dynamicPrimary),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(if (activeQuestionIndex < questions.size - 1) "Next Question" else "Submit Quiz")
                            }
                        }
                    } else {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(Icons.Default.EmojiEvents, null, tint = dynamicGold, modifier = Modifier.size(48.dp))
                            Text("Quiz Successfully Submitted!", fontWeight = FontWeight.Black, fontSize = 16.sp, color = dynamicPrimary)
                            Text("Your Grade: 2 / 2 Correct (Perfect 100%)", fontSize = 12.sp, color = MutedCharcoal)
                            
                            Button(
                                onClick = {
                                    Toast.makeText(context, "Quiz performance successfully updated in student ledger!", Toast.LENGTH_LONG).show()
                                    showQuizSimulator = false
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = dynamicGold),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Close Result")
                            }
                        }
                    }
                }
            }
        }
    }

    // Certificate Dialog
    if (showCertificateDialog) {
        Dialog(onDismissRequest = { showCertificateDialog = false }) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = BrandWhite),
                border = BorderStroke(1.dp, dynamicGold.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(Icons.Default.Verified, null, tint = dynamicGold, modifier = Modifier.size(56.dp))
                    Text("Executive Program Credentials", fontWeight = FontWeight.Black, fontSize = 16.sp, color = dynamicPrimary, textAlign = TextAlign.Center)
                    
                    Text(
                        text = "To unlock the signed Executive Systems Architect Certificate, your course progress must exceed 85% and overall quiz average must be at least 80%.",
                        fontSize = 11.sp,
                        color = MutedCharcoal,
                        textAlign = TextAlign.Center
                    )
                    
                    HorizontalDivider(color = dynamicGold.copy(alpha = 0.1f))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Your Current Progress:", fontSize = 11.sp, color = MutedCharcoal)
                        Text("82% (Requires 85%)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Red)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Your Quiz Average:", fontSize = 11.sp, color = MutedCharcoal)
                        Text("85.4% (Pass)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = dynamicPrimary)
                    }
                    
                    Button(
                        onClick = { showCertificateDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = dynamicPrimary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Verify & Done")
                    }
                }
            }
        }
    }
}

// =======================================================
// LEARNER SCHEDULE AND GRADE TAB
// =======================================================
@Composable
fun LearnerScheduleGradeTab(
    viewModel: LmsViewModel,
    courses: List<Course>,
    assignments: List<Assignment>,
    virtualClasses: List<VirtualClass>,
    attendance: List<AttendanceRecord>,
    users: List<UserAccount>,
    dynamicPrimary: Color,
    dynamicGold: Color
) {
    val context = LocalContext.current
    val learnerProfile = users.find { it.role == "Learner" }
    val isCertified = learnerProfile?.certificateIssued == true

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            Text("Schedules, Assessments, & Graduate Bureau", fontWeight = FontWeight.Black, fontSize = 18.sp, color = DarkCharcoal)
        }

        // Certificate Display (Golden high fidelity border card)
        if (isCertified) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = BrandWhite),
                    border = BorderStroke(4.dp, dynamicGold)
                ) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        // Background celebratory canvas decor
                        Canvas(modifier = Modifier.matchParentSize()) {
                            drawCircle(color = dynamicGold.copy(alpha = 0.08f), radius = 100.dp.toPx())
                        }
                        Column(
                            modifier = Modifier.padding(24.dp).fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.WorkspacePremium, "Premium Certificate", tint = dynamicGold, modifier = Modifier.size(54.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("ROOTED & READY ACADEMY", fontWeight = FontWeight.Black, fontSize = 16.sp, color = dynamicPrimary, letterSpacing = 1.sp)
                            Text("GRADUATE CERTIFICATE OF COMPLETION", fontWeight = FontWeight.SemiBold, fontSize = 11.sp, color = dynamicGold)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("This certifies that candidate", fontSize = 10.sp, fontStyle = FontStyle.Italic, color = MutedCharcoal)
                            Text(learnerProfile?.name ?: "Peter Parker", fontWeight = FontWeight.Black, fontSize = 18.sp, color = DarkCharcoal)
                            Text("has successfully mastered the complete Executive and Professional Competency curriculum syllabus.", fontSize = 11.sp, color = MutedCharcoal, textAlign = TextAlign.Center)
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = {
                                    Toast.makeText(context, "CERTIFICATE DOWNLOAD: golden_certificate_rooted_academy.pdf downloaded!", Toast.LENGTH_LONG).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = dynamicGold)
                            ) {
                                Text("Download Golden Certificate PDF")
                            }
                        }
                    }
                }
            }
        }

        // Live Virtual Lectures (Zoom Join)
        item {
            ControlRoomSectionCard(title = "Live Virtual Lectures & Classrooms", icon = Icons.Default.VideoCameraBack, dynamicPrimary) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (virtualClasses.isEmpty()) {
                        Text(
                            text = "No live virtual sessions scheduled. Check back later!",
                            fontStyle = FontStyle.Italic,
                            fontSize = 12.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                    
                    virtualClasses.forEach { classSession ->
                        val course = courses.find { it.id == classSession.courseId }
                        val code = course?.code ?: "TRAINING"
                        val courseName = course?.name ?: "Professional Course"
                        
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = BrandCream),
                            border = BorderStroke(1.dp, dynamicPrimary.copy(alpha = 0.15f))
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(classSession.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = DarkCharcoal)
                                        Text("$code • $courseName", fontSize = 10.sp, color = MutedCharcoal)
                                    }
                                    
                                    val platformColor = when (classSession.platform) {
                                        "Zoom" -> Color(0xFF2D8CFF)
                                        "Google Meet" -> Color(0xFF00897B)
                                        "Microsoft Teams" -> Color(0xFF5C6BC0)
                                        "Loom" -> Color(0xFF6200EE)
                                        "YouTube Live" -> Color(0xFFE53935)
                                        else -> dynamicPrimary
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(platformColor.copy(alpha = 0.15f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(classSession.platform, color = platformColor, fontWeight = FontWeight.Bold, fontSize = 9.sp)
                                    }
                                }
                                
                                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.4f))
                                
                                // Details grid
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.CalendarToday, null, tint = dynamicGold, modifier = Modifier.size(12.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Date & Time: ", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        Text(formatClassDateTime(classSession.scheduledTime), fontSize = 10.sp, color = DarkCharcoal)
                                    }
                                    
                                    Row(modifier = Modifier.fillMaxWidth()) {
                                        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Person, null, tint = dynamicGold, modifier = Modifier.size(12.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Facilitator: ", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            Text(classSession.facilitator, fontSize = 10.sp, color = DarkCharcoal)
                                        }
                                        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Timer, null, tint = dynamicGold, modifier = Modifier.size(12.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Duration: ", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            Text("${classSession.durationMins} mins", fontSize = 10.sp, color = DarkCharcoal)
                                        }
                                    }
                                    
                                    Row(modifier = Modifier.fillMaxWidth()) {
                                        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Fingerprint, null, tint = dynamicGold, modifier = Modifier.size(12.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("ID: ", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            Text(classSession.meetingId.ifEmpty { "None" }, fontSize = 10.sp, color = DarkCharcoal)
                                        }
                                        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.VpnKey, null, tint = dynamicGold, modifier = Modifier.size(12.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Pass: ", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            Text(classSession.password.ifEmpty { "None" }, fontSize = 10.sp, color = DarkCharcoal)
                                        }
                                    }
                                    
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                        if (classSession.isReminderEnabled) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.NotificationsActive, null, tint = Color(0xFF4CAF50), modifier = Modifier.size(12.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Reminders Scheduled 🔔", fontSize = 9.sp, color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold)
                                            }
                                        }
                                        if (classSession.isCalendarIntegrated) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.CalendarMonth, null, tint = Color(0xFF2196F3), modifier = Modifier.size(12.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Calendar Synced 📅", fontSize = 9.sp, color = Color(0xFF2196F3), fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                                
                                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.4f))
                                
                                // Attendance Section
                                val learnerName = learnerProfile?.name ?: "Peter Parker"
                                val joinedList = classSession.attendanceRegister.split(";").filter { it.isNotBlank() }
                                val hasRegistered = joinedList.contains(learnerName)
                                
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                        Text("Attendance Register:", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        if (hasRegistered) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.CheckCircle, "Present", tint = Color(0xFF4CAF50), modifier = Modifier.size(12.dp))
                                                Spacer(modifier = Modifier.width(2.dp))
                                                Text("Registered (Present)", fontSize = 9.sp, color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold)
                                            }
                                        } else {
                                            Text("Awaiting Join", fontSize = 9.sp, color = Color.Gray, fontStyle = FontStyle.Italic)
                                        }
                                    }
                                    
                                    if (joinedList.isNotEmpty()) {
                                        Text(
                                            text = "Attendees Joined: " + joinedList.joinToString(", "),
                                            fontSize = 10.sp,
                                            color = DarkCharcoal,
                                            fontWeight = FontWeight.Medium
                                        )
                                    } else {
                                        Text("No students recorded present yet. Be the first to join!", fontSize = 10.sp, fontStyle = FontStyle.Italic, color = Color.Gray)
                                    }
                                }
                                
                                // Recording playback
                                if (classSession.recordingUrl.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = dynamicPrimary.copy(alpha = 0.05f)),
                                        border = BorderStroke(1.dp, dynamicPrimary.copy(alpha = 0.2f))
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(8.dp).fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                                Icon(Icons.Default.SlowMotionVideo, null, tint = dynamicPrimary, modifier = Modifier.size(14.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Column {
                                                    Text("Syllabus Recording Auto-Posted", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = dynamicPrimary)
                                                    Text(classSession.recordingUrl, fontSize = 9.sp, color = MutedCharcoal, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                                }
                                            }
                                            Button(
                                                onClick = {
                                                    Toast.makeText(context, "STREAMING CLASS RECORDING: Streaming video recap session!", Toast.LENGTH_SHORT).show()
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = dynamicPrimary),
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text("Play Recap", fontSize = 9.sp)
                                            }
                                        }
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(4.dp))
                                
                                Button(
                                    onClick = {
                                        if (!hasRegistered) {
                                            val updatedReg = if (classSession.attendanceRegister.isEmpty()) learnerName else "${classSession.attendanceRegister};$learnerName"
                                            viewModel.updateVirtualClass(classSession.copy(attendanceRegister = updatedReg))
                                            Toast.makeText(context, "LAUNCHING ZOOM/ROOM: Connecting... Attendance recorded for $learnerName!", Toast.LENGTH_LONG).show()
                                        } else {
                                            Toast.makeText(context, "RE-LAUNCHING: Connecting back to secure virtual room room!", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = if (hasRegistered) dynamicPrimary.copy(alpha = 0.8f) else dynamicPrimary),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.LiveTv, null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(if (hasRegistered) "Enter Live Lecture Room Again" else "Join Live Class & Sign Attendance", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Upcoming Deliverables assignments
        item {
            ControlRoomSectionCard(title = "Syllabus Deliverables Deadlines", icon = Icons.Default.Task, dynamicPrimary) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    assignments.forEach { ass ->
                        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = BrandWhite)) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(checked = ass.isCompleted, onCheckedChange = { viewModel.toggleAssignmentCompleted(ass) })
                                Spacer(modifier = Modifier.width(6.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(ass.title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text(ass.description, fontSize = 11.sp, color = MutedCharcoal)
                                }
                                Badge(containerColor = if (ass.priority == "High") Color.Red else dynamicGold) { Text(ass.priority) }
                            }
                        }
                    }
                }
            }
        }
    }
}

// =======================================================
// RECRUITMENT PARTNER DASHBOARD (FIND TALENT)
// =======================================================
@Composable
fun RecruitmentTalentTab(
    users: List<UserAccount>,
    dynamicPrimary: Color,
    dynamicGold: Color
) {
    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            Text("Recruitment Directory & Certified Candidates", fontWeight = FontWeight.Black, fontSize = 18.sp, color = DarkCharcoal)
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = BrandWhite),
                border = BorderStroke(1.dp, dynamicGold.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Academy Talents Directory", fontWeight = FontWeight.Bold, color = dynamicPrimary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Acquire top academy graduates vetted inside technical distributed systems and enterprise leadership curriculums.", fontSize = 12.sp, color = MutedCharcoal)
                }
            }
        }

        val learners = users.filter { it.role == "Learner" }
        items(learners) { student ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = BrandWhite),
                border = BorderStroke(1.dp, dynamicGold.copy(alpha = 0.15f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AccountCircle, null, tint = dynamicPrimary, modifier = Modifier.size(40.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(student.name, fontWeight = FontWeight.Black, fontSize = 15.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                if (student.certificateIssued) {
                                    Badge(containerColor = dynamicGold) { Text("VETTED GRADUATE ✓") }
                                }
                            }
                            Text("Email: ${student.email} • Curriculum: Executive-2026", fontSize = 11.sp, color = MutedCharcoal)
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Competence Matrix Track Rating: 88% average score", fontSize = 12.sp, color = MutedCharcoal)
                }
            }
        }
    }
}

// =======================================================
// RECRUITMENT PLACEMENT TAB
// =======================================================
@Composable
fun RecruitmentPlacementTab(
    viewModel: LmsViewModel,
    placements: List<Placement>,
    dynamicPrimary: Color,
    dynamicGold: Color
) {
    var showAddPlacement by remember { mutableStateOf(false) }
    var candidateName by remember { mutableStateOf("") }
    var partnerComp by remember { mutableStateOf("") }
    var candidateRole by remember { mutableStateOf("") }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            Text("Enterprise Placements Pipeline", fontWeight = FontWeight.Black, fontSize = 18.sp, color = DarkCharcoal)
        }

        item {
            ControlRoomSectionCard(title = "Hiring Pipeline Tracker", icon = Icons.Default.Work, dynamicPrimary) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Active Applications (${placements.size})", fontWeight = FontWeight.Bold)
                        Button(
                            onClick = { showAddPlacement = !showAddPlacement },
                            colors = ButtonDefaults.buttonColors(containerColor = dynamicPrimary)
                        ) {
                            Text(if (showAddPlacement) "Collapse" else "Submit Candidate")
                        }
                    }

                    if (showAddPlacement) {
                        Card(colors = CardDefaults.cardColors(containerColor = BrandCream), border = BorderStroke(1.dp, dynamicGold.copy(alpha = 0.5f))) {
                            Column(modifier = Modifier.padding(12.dp).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(value = candidateName, onValueChange = { candidateName = it }, label = { Text("Candidate Full Name") }, modifier = Modifier.fillMaxWidth().testTag("add_placement_name"))
                                OutlinedTextField(value = partnerComp, onValueChange = { partnerComp = it }, label = { Text("Hiring Enterprise Partner") }, modifier = Modifier.fillMaxWidth())
                                OutlinedTextField(value = candidateRole, onValueChange = { candidateRole = it }, label = { Text("Target Position (e.g. Software Architect)") }, modifier = Modifier.fillMaxWidth().testTag("add_placement_role"))
                                Button(
                                    onClick = {
                                        if (candidateName.isNotBlank() && candidateRole.isNotBlank()) {
                                            viewModel.addPlacementCandidate(candidateName, partnerComp, candidateRole, "Applied")
                                            candidateName = ""
                                            partnerComp = ""
                                            candidateRole = ""
                                            showAddPlacement = false
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = dynamicPrimary),
                                    modifier = Modifier.fillMaxWidth().testTag("btn_save_placement")
                                ) {
                                    Text("Log Candidate Pipeline")
                                }
                            }
                        }
                    }

                    placements.forEach { candidate ->
                        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = BrandWhite)) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CardTravel, null, tint = dynamicGold)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(candidate.learnerName, fontWeight = FontWeight.Bold)
                                    Text("Role: ${candidate.role} at ${candidate.partnerName}", fontSize = 11.sp, color = MutedCharcoal)
                                }
                                Badge(
                                    containerColor = when (candidate.status) {
                                        "Placed" -> PrimaryGreen
                                        "Offered" -> dynamicGold
                                        else -> dynamicPrimary
                                    },
                                    contentColor = BrandWhite
                                ) {
                                    Text(candidate.status)
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                                IconButton(
                                    onClick = {
                                        val nextStatus = when (candidate.status) {
                                            "Applied" -> "Interviewing"
                                            "Interviewing" -> "Offered"
                                            "Offered" -> "Placed"
                                            "Placed" -> "Rejected"
                                            else -> "Applied"
                                        }
                                        viewModel.updatePlacementStatus(candidate, nextStatus)
                                    }
                                ) {
                                    Icon(Icons.Default.SyncAlt, "Update Status", tint = Color.Gray)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

fun formatClassDateTime(timestamp: Long): String {
    val sdf = java.text.SimpleDateFormat("EEEE, MMMM dd, yyyy 'at' hh:mm a", java.util.Locale.getDefault())
    return sdf.format(java.util.Date(timestamp))
}
