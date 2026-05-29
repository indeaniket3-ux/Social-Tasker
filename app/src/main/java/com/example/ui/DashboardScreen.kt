package com.example.ui

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.SocialMessage
import com.example.data.Task
import com.example.ui.viewmodel.DashboardViewModel
import java.text.SimpleDateFormat
import java.util.*

// FocusSync / Professional Polish Theme Colors
private val BgColor = Color(0xFFFEF7FF)
private val TextPrimary = Color(0xFF1D1B20)
private val TextSecondary = Color(0xFF49454F)
private val PurpleAccentPrimary = Color(0xFF6750A4)
private val PurpleAccentLight = Color(0xFFEADDFF)
private val PurpleAccentBorder = Color(0xFFD0BCFF)
private val PurpleAccentDeep = Color(0xFF21005D)
private val BorderColorDefault = Color(0xFFCAC4D0)
private val BorderColorInner = Color(0xFFE7E0EC)
private val GraySubtitle = Color(0xFF79747E)
private val InstaRed = Color(0xFFFF006E)
private val SnapYellow = Color(0xFFFFFC00)

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val tasks by viewModel.tasks.collectAsStateWithLifecycle()
    val socialMessages by viewModel.socialMessages.collectAsStateWithLifecycle()
    val isPermissionGranted by viewModel.isListenerPermissionGranted.collectAsStateWithLifecycle()

    // Form states
    var taskTitle by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Personal") }
    var selectedPriority by remember { mutableStateOf("Medium") }

    // Run permission check on resume or focus
    LaunchedEffect(Unit) {
        viewModel.checkListenerPermission(context)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = BgColor
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BgColor)
                .padding(innerPadding)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp)
            ) {
                // 1. Header with dynamic date and avatar
                item {
                    DashboardHeader(tasks = tasks)
                }

                // 2. Notification System Access Ribbon
                item {
                    PermissionControlCard(
                        isPermissionGranted = isPermissionGranted,
                        onCheckPermission = { viewModel.checkListenerPermission(context) },
                        onGrantClicked = {
                            val intent = Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
                            context.startActivity(intent)
                        }
                    )
                }

                // 3. Pending Social Message Alerts Streams
                item {
                    SocialAlertsSection(
                        messages = socialMessages,
                        onSimulateMsg = { sender, text, platform ->
                            viewModel.simulateIncomingMessage(sender, text, platform)
                        },
                        onMarkAllRead = { viewModel.markAllAsRead() },
                        onClearAll = { viewModel.clearAllSocialMessages() }
                    )
                }

                // 4. Quick Add Form Block
                item {
                    TaskAddCard(
                        taskTitle = taskTitle,
                        onTitleChange = { taskTitle = it },
                        selectedCategory = selectedCategory,
                        onCategorySelect = { selectedCategory = it },
                        selectedPriority = selectedPriority,
                        onPrioritySelect = { selectedPriority = it },
                        onAddTask = {
                            if (taskTitle.isNotBlank()) {
                                viewModel.addTask(taskTitle, selectedCategory, selectedPriority)
                                taskTitle = ""
                            }
                        }
                    )
                }

                // 5. To-Do Checklist Title with counter
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.List,
                                contentDescription = null,
                                tint = TextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "To-Do List",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextSecondary
                            )
                        }
                        val activeCount = tasks.count { !it.isCompleted }
                        Text(
                            text = "$activeCount left",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = PurpleAccentPrimary
                        )
                    }
                }

                // 6. Inline Checklist items
                if (tasks.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Tasks empty",
                                tint = GraySubtitle,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Your checklist is completely clean!",
                                style = MaterialTheme.typography.bodyMedium,
                                color = GraySubtitle
                            )
                        }
                    }
                } else {
                    items(
                        items = tasks,
                        key = { it.id }
                    ) { task ->
                        TaskItemCard(
                            task = task,
                            onToggleComplete = { viewModel.toggleTaskCompleteness(task) },
                            onDelete = { viewModel.deleteTask(task) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DashboardHeader(tasks: List<Task>) {
    val total = tasks.size
    val completed = tasks.count { it.isCompleted }
    val progress = if (total > 0) completed.toFloat() / total else 0f
    
    // Dynamic Header Date like the demo Tuesday, Oct 24
    val formattedDate = remember {
        SimpleDateFormat("EEEE, MMM d", Locale.getDefault()).format(Date()).uppercase()
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = PurpleAccentPrimary,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Social Tasker",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary
                )
            }
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(PurpleAccentLight)
                    .border(1.dp, PurpleAccentBorder, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "User Account Indicator",
                    tint = PurpleAccentDeep,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Daily Priority Tracker banner (M3 Violet with star aspect)
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = PurpleAccentLight),
            modifier = Modifier
                .fillMaxWidth()
                .shadow(2.dp, RoundedCornerShape(24.dp))
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "DAILY PRIORITY ACCURACIES",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = PurpleAccentDeep,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (total == 0) "Complete your objectives" else "Active Tasks Resolution",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = PurpleAccentDeep
                        )
                        Text(
                            text = "$completed completed out of $total tasks",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Priority star",
                        tint = PurpleAccentDeep,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = PurpleAccentPrimary,
                    trackColor = PurpleAccentBorder
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${(progress * 100).toInt()}% completed today",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
fun PermissionControlCard(
    isPermissionGranted: Boolean,
    onCheckPermission: () -> Unit,
    onGrantClicked: () -> Unit
) {
    LaunchedEffect(key1 = isPermissionGranted) {
        onCheckPermission()
    }

    // Professional light background combinations
    val cardColor = if (isPermissionGranted) Color(0xFFF1F8E9) else Color(0xFFFFF3E0)
    val borderColor = if (isPermissionGranted) Color(0xFF81C784) else Color(0xFFFFB74D)
    val contentColor = if (isPermissionGranted) Color(0xFF2E7D32) else Color(0xFFE65100)

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(contentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPermissionGranted) Icons.Default.CheckCircle else Icons.Default.Warning,
                        contentDescription = "System alert icon",
                        tint = contentColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = if (isPermissionGranted) "Tasker Listener Active" else "Notification Listener Disabled",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = if (isPermissionGranted) "Monitoring Insta & Snap messages inline." else "Grant system access to capture messages in near real-time.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            if (!isPermissionGranted) {
                Button(
                    onClick = onGrantClicked,
                    colors = ButtonDefaults.buttonColors(containerColor = contentColor),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("grant_permission_btn")
                ) {
                    Text("Grant", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            } else {
                IconButton(onClick = onCheckPermission) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = "Check Connection", tint = contentColor)
                }
            }
        }
    }
}

@Composable
fun SocialAlertsSection(
    messages: List<SocialMessage>,
    onSimulateMsg: (String, String, String) -> Unit,
    onMarkAllRead: () -> Unit,
    onClearAll: () -> Unit
) {
    val unreadCount = messages.count { !it.isRead }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BorderColorInner, RoundedCornerShape(16.dp))
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Active Alerts",
                        tint = PurpleAccentPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Pending Alerts Stream",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
                if (unreadCount > 0) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(PurpleAccentPrimary)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "$unreadCount New",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Message Alerts Scrolling Track
            if (messages.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(90.dp)
                        .background(BgColor, RoundedCornerShape(12.dp))
                        .border(1.dp, BorderColorInner, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = "No Alerts Box",
                            tint = GraySubtitle,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "No alerts logged yet",
                            style = MaterialTheme.typography.bodySmall,
                            color = GraySubtitle
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 180.dp)
                        .background(BgColor, RoundedCornerShape(12.dp))
                        .border(1.dp, BorderColorInner, RoundedCornerShape(12.dp))
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(messages) { message ->
                            SocialMessageRow(message = message)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Message Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onMarkAllRead,
                    colors = ButtonDefaults.textButtonColors(contentColor = PurpleAccentPrimary),
                    enabled = unreadCount > 0
                ) {
                    Text("Clear Badges", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                TextButton(
                    onClick = onClearAll,
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFC2185B)),
                    enabled = messages.isNotEmpty()
                ) {
                    Text("Clear Stream Logs", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 10.dp),
                color = BorderColorInner
            )

            // Dynamic test simulator
            Text(
                text = "Tap to Simulate Incoming Network Message Alerts",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Insta simulation
                Button(
                    onClick = {
                        val names = listOf("Alex", "Emma", "Sarah", "Devon", "Anya")
                        val msgs = listOf("Wanna grab coffee?", "Did you finish the wireframes?", "Check my profile message!", "Sent a design concept!", "See you at the office!")
                        onSimulateMsg(names.random(), msgs.random(), "Instagram")
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .testTag("simulate_insta_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = InstaRed),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Insta icon",
                        tint = Color.White,
                        modifier = Modifier
                            .size(12.dp)
                            .padding(end = 2.dp)
                    )
                    Text("Instagram Alert", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }

                // Snapchat simulation
                Button(
                    onClick = {
                        val names = listOf("Jake", "Lucy", "Michael", "Chloe", "Ryan")
                        val msgs = listOf("Streak live! 🔥", "Sent a snapshot summary", "New chat updated", "Are you available tonight?", "Haha that's true!")
                        onSimulateMsg(names.random(), msgs.random(), "Snapchat")
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .testTag("simulate_snap_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = SnapYellow),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E200)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = "Snap icon",
                        tint = TextPrimary,
                        modifier = Modifier
                            .size(12.dp)
                            .padding(end = 2.dp)
                    )
                    Text("Snapchat Alert", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                }
            }
        }
    }
}

@Composable
fun SocialMessageRow(message: SocialMessage) {
    val platformColor = if (message.platform == "Instagram") InstaRed else Color(0xFFFFCC00)
    val textStyle = if (!message.isRead) FontWeight.Bold else FontWeight.Normal
    val textBgColor = if (!message.isRead) PurpleAccentLight.copy(alpha = 0.3f) else Color.Transparent
    val timeStr = remember(message.timestamp) {
        SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(message.timestamp))
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(textBgColor)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Rounded Platform Icon Box
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(platformColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (message.platform == "Instagram") Icons.Default.Send else Icons.Default.Email,
                contentDescription = null,
                tint = if (message.platform == "Instagram") Color.White else TextPrimary,
                modifier = Modifier.size(14.dp)
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${message.sender} (${message.platform})",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = timeStr,
                    style = MaterialTheme.typography.labelSmall,
                    color = GraySubtitle
                )
            }
            Text(
                text = message.text,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = textStyle,
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun TaskAddCard(
    taskTitle: String,
    onTitleChange: (String) -> Unit,
    selectedCategory: String,
    onCategorySelect: (String) -> Unit,
    selectedPriority: String,
    onPrioritySelect: (String) -> Unit,
    onAddTask: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BorderColorInner, RoundedCornerShape(16.dp))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Add Daily Task",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            // Elegant Text Input Field
            TextField(
                value = taskTitle,
                onValueChange = onTitleChange,
                placeholder = { Text("What is your next task?", color = GraySubtitle) },
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BorderColorDefault, RoundedCornerShape(10.dp))
                    .testTag("task_title_input"),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                shape = RoundedCornerShape(10.dp),
                singleLine = true
            )

            // Category select row
            Column {
                Text(
                    text = "Category",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val categories = listOf("Personal", "Work", "Social", "Finance")
                    categories.forEach { category ->
                        val isSelected = category == selectedCategory
                        FilterChip(
                            selected = isSelected,
                            onClick = { onCategorySelect(category) },
                            label = { Text(category, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = BgColor,
                                labelColor = TextSecondary,
                                selectedContainerColor = PurpleAccentLight,
                                selectedLabelColor = PurpleAccentDeep
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                borderColor = BorderColorInner,
                                selectedBorderColor = PurpleAccentBorder,
                                enabled = true,
                                selected = isSelected
                            )
                        )
                    }
                }
            }

            // Priority select row
            Column {
                Text(
                    text = "Priority Level",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val priorities = listOf("High", "Medium", "Low")
                    priorities.forEach { priority ->
                        val isSelected = priority == selectedPriority
                        val priorityColor = when (priority) {
                            "High" -> Color(0xFFD32F2F)
                            "Medium" -> Color(0xFFF57C00)
                            else -> Color(0xFF388E3C)
                        }

                        FilterChip(
                            selected = isSelected,
                            onClick = { onPrioritySelect(priority) },
                            label = { Text(priority, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = BgColor,
                                labelColor = TextSecondary,
                                selectedContainerColor = priorityColor.copy(alpha = 0.15f),
                                selectedLabelColor = priorityColor
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                borderColor = BorderColorInner,
                                selectedBorderColor = priorityColor,
                                enabled = true,
                                selected = isSelected
                            )
                        )
                    }
                }
            }

            // Elevated Add button matching focus flow theme
            Button(
                onClick = onAddTask,
                enabled = taskTitle.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp)
                    .testTag("add_task_btn"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PurpleAccentPrimary,
                    disabledContainerColor = PurpleAccentBorder
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Icon", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Add Title Task", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun TaskItemCard(
    task: Task,
    onToggleComplete: () -> Unit,
    onDelete: () -> Unit
) {
    val completedAlpha = if (task.isCompleted) 0.6f else 1.0f
    val cardBg = if (task.isCompleted) PurpleAccentLight.copy(alpha = 0.5f) else Color.White

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BorderColorInner, RoundedCornerShape(16.dp))
            .shadow(if (task.isCompleted) 0.dp else 1.dp, RoundedCornerShape(16.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Interactive Professional Check Button
                IconButton(
                    onClick = onToggleComplete,
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("task_checkbox")
                ) {
                    if (task.isCompleted) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(PurpleAccentPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Checked completed",
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .border(2.dp, PurpleAccentPrimary, RoundedCornerShape(6.dp))
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (task.isCompleted) TextSecondary else TextPrimary,
                        textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Category tag capsule
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(BgColor)
                                .border(1.dp, BorderColorInner, RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = task.category,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextSecondary
                            )
                        }

                        // Priority tag capsule
                        val priorityColor = when (task.priority) {
                            "High" -> Color(0xFFD32F2F)
                            "Medium" -> Color(0xFFF57C00)
                            else -> Color(0xFF388E3C)
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(priorityColor.copy(alpha = 0.1f))
                                .border(1.dp, priorityColor.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = task.priority,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = priorityColor
                            )
                        }
                    }
                }
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .size(32.dp)
                    .testTag("delete_task_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete task",
                    tint = GraySubtitle,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
