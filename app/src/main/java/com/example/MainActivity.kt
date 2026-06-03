package com.example

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.*
import com.example.ui.JobViewModel
import com.example.ui.JobViewModelFactory
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize repository from local Room database
        val database = AppDatabase.getDatabase(this)
        val repository = JobRepository(database.jobDao())
        val factory = JobViewModelFactory(repository)

        setContent {
            val viewModel: JobViewModel = viewModel(factory = factory)
            val isDarkTheme by viewModel.isDarkTheme.collectAsStateWithLifecycle()

            MyApplicationTheme(darkTheme = isDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen(viewModel = viewModel)
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MainScreen(viewModel: JobViewModel) {
    val context = LocalContext.current
    val activeTab by viewModel.activeTab.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val searchResults by viewModel.searchResults.collectAsStateWithLifecycle()
    val bookmarkedJobs by viewModel.bookmarkedJobs.collectAsStateWithLifecycle()
    val selectedAlert by viewModel.selectedAlert.collectAsStateWithLifecycle()
    val isDarkTheme by viewModel.isDarkTheme.collectAsStateWithLifecycle()

    var showAboutDialog by remember { mutableStateOf(false) }
    var showDisclaimerDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            // Header panel mirroring Red HTML layout
            Column {
                val headerGradientColors = if (isDarkTheme) {
                    listOf(Color(0xFF2B221D), Color(0xFF1F1B16))
                } else {
                    listOf(Color(0xFFFFDCC0), Color(0xFFFFDBCD))
                }
                val headerTextColor = if (isDarkTheme) Color(0xFFFDF8F6) else Color(0xFF351000)
                val headerSubtextColor = if (isDarkTheme) Color(0xFFF7EEE8).copy(alpha = 0.8f) else Color(0xFF52443C)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Brush.verticalGradient(colors = headerGradientColors))
                        .padding(top = 40.dp, bottom = 12.dp, start = 16.dp, end = 16.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "All India Latest Job™",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Black,
                                    color = headerTextColor,
                                    fontFamily = FontFamily.SansSerif,
                                    modifier = Modifier.clickable { viewModel.setActiveTab("home") }
                                )
                                Text(
                                    text = "Sarkari Naukri, Results, Admit Card Alerts",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = headerSubtextColor
                                )
                            }

                            // Theme & saved toggle button
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { viewModel.toggleTheme() },
                                    modifier = Modifier.testTag("theme_toggle_btn")
                                ) {
                                    Icon(
                                        imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                                        contentDescription = "Toggle Theme",
                                        tint = headerTextColor
                                    )
                                }
                                IconButton(
                                    onClick = { viewModel.setActiveTab("bookmarks") },
                                    modifier = Modifier.testTag("bookmarks_nav_btn")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Bookmark,
                                        contentDescription = "View Bookmarks",
                                        tint = if (activeTab == "bookmarks") MaterialTheme.colorScheme.primary else headerTextColor
                                    )
                                }
                            }
                        }
                    }
                }

                // Horizontal navigation tabs matching the website navbar exactly
                val tabs = listOf(
                    "home" to "Home",
                    "jobs" to "Latest Job",
                    "admit" to "Admit Card",
                    "result" to "Result",
                    "admission" to "Admission",
                    "syllabus" to "Syllabus",
                    "answerkey" to "Answer Key",
                    "contact" to "Contact"
                )

                val tabContainerColor = if (isDarkTheme) Color(0xFF1F1B16) else Color(0xFFF4ECE4)
                val tabActiveColor = MaterialTheme.colorScheme.primary
                val tabInactiveColor = if (isDarkTheme) Color(0xFFFDF8F6).copy(alpha = 0.6f) else Color(0xFF85736B)

                ScrollableTabRow(
                    selectedTabIndex = tabs.indexOfFirst { it.first == activeTab }.coerceAtLeast(0),
                    containerColor = tabContainerColor,
                    contentColor = tabActiveColor,
                    edgePadding = 8.dp,
                    divider = {}
                ) {
                    tabs.forEach { (route, label) ->
                        Tab(
                            selected = activeTab == route,
                            onClick = { viewModel.setActiveTab(route) },
                            text = {
                                Text(
                                    text = label,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (activeTab == route) tabActiveColor else tabInactiveColor
                                )
                            }
                        )
                    }
                }

                // Ticker component mimicking marquee exactly
                val tickerBg = if (isDarkTheme) Color(0xFF473931) else Color(0xFFFFDCC0)
                val tickerBorder = if (isDarkTheme) Color(0xFF52443C) else Color(0xFFFFB68D)
                val tickerText = if (isDarkTheme) Color(0xFFFDF8F6) else Color(0xFF351000)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(tickerBg)
                        .border(1.dp, tickerBorder)
                        .padding(vertical = 6.dp, horizontal = 12.dp)
                ) {
                    Text(
                        text = "🔴 SSC CGL 2026 Online Form – Last Date 31 July  |  🔴 UPSC Civil Services 2026 Notification Out  |  🔴 Railway RRB ALP 11127 Posts Form Active  |  🔴 BPSC 71st CCE Result Out  |  🔴 UP Police Constable Exam Date 2026  |  🔴 SBI Clerk 2026 Notification Coming Soon",
                        color = tickerText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        modifier = Modifier
                            .basicMarquee(iterations = Int.MAX_VALUE)
                            .fillMaxWidth()
                    )
                }
            }
        },
        bottomBar = {
            // Footer credits and visual links matching the web layout
            val footerBg = if (isDarkTheme) Color(0xFF251B17) else Color(0xFFF7EEE8)
            val footerBorder = if (isDarkTheme) Color(0xFF473931) else Color(0xFFEDE0D4)
            val footerTextMuted = if (isDarkTheme) Color(0xFFFDF8F6).copy(alpha = 0.6f) else Color(0xFF85736B)
            
            Surface(
                tonalElevation = 2.dp,
                color = footerBg,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(BorderStroke(0.5.dp, footerBorder))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 28.dp, top = 12.dp, start = 12.dp, end = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = { showAboutDialog = true },
                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("About", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        TextButton(
                            onClick = { showDisclaimerDialog = true },
                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("Disclaimer", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        TextButton(
                            onClick = { showPrivacyDialog = true },
                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("Privacy", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        TextButton(
                            onClick = { viewModel.setActiveTab("contact") },
                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("Contact", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "All India Latest Job™ © 2026",
                        fontSize = 11.sp,
                        color = footerTextMuted
                    )
                    Text(
                        text = "Not associated with any Government website. For information only.",
                        fontSize = 9.sp,
                        color = footerTextMuted.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center,
                        lineHeight = 12.sp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
        }
    ) { paddingValues ->
        // Main core layout flow
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (activeTab) {
                "home" -> HomeScreen(viewModel = viewModel)
                "jobs", "admit", "result", "admission", "syllabus", "answerkey" -> SectionScreen(
                    category = activeTab,
                    viewModel = viewModel
                )
                "bookmarks" -> BookmarksScreen(viewModel = viewModel)
                "contact" -> ContactScreen()
                "search" -> SearchResultsScreen(viewModel = viewModel)
            }
        }
    }

    // Modal Sheet representation representing search and notifications popup
    selectedAlert?.let { alert ->
        val isBookmarked = bookmarkedJobs.any { it.title == alert.title }

        AlertDialog(
            onDismissRequest = { viewModel.selectAlert(null) },
            confirmButton = {
                TextButton(
                    onClick = {
                        val queryUrl = "https://www.google.com/search?q=${Uri.encode(alert.title)} official notification"
                        launchBrowser(context, queryUrl)
                    },
                    modifier = Modifier.testTag("dialog_docs_btn")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Notification")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        val queryUrl = "https://www.google.com/search?q=${Uri.encode(alert.title)} apply online"
                        launchBrowser(context, queryUrl)
                    },
                    modifier = Modifier.testTag("dialog_apply_btn")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.OpenInBrowser, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Apply Online")
                    }
                }
            },
            title = {
                Text(
                    text = alert.title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = getCategoryColor(alert.category),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Department: ",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            modifier = Modifier.width(90.dp)
                        )
                        Text(text = alert.dept, fontSize = 13.sp, modifier = Modifier.weight(1f))
                    }
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Posts count: ",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            modifier = Modifier.width(90.dp)
                        )
                        Text(text = alert.posts, fontSize = 13.sp, modifier = Modifier.weight(1f))
                    }
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Last Date: ",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            modifier = Modifier.width(90.dp)
                        )
                        var dateColor = MaterialTheme.colorScheme.onSurface
                        if (alert.date.contains("July") || alert.date.contains("Active") || alert.date.contains("Today")) {
                            dateColor = MaterialTheme.colorScheme.primary
                        }
                        Text(
                            text = alert.date,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = dateColor,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Divider()

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { viewModel.toggleBookmark(alert) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isBookmarked) Color.Gray else getCategoryColor(alert.category)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(
                                imageVector = if (isBookmarked) Icons.Default.BookmarkRemove else Icons.Default.BookmarkAdd,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isBookmarked) "Saved" else "Save Alert", fontSize = 12.sp)
                        }

                        IconButton(
                            onClick = { shareJob(context, alert) },
                            modifier = Modifier.testTag("dialog_share_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share details",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            },
            shape = RoundedCornerShape(12.dp)
        )
    }

    // Standard static resource modals (About, Privacy, Disclaimer)
    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            confirmButton = { Button(onClick = { showAboutDialog = false }) { Text("OK") } },
            title = { Text("About Us", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "All India Latest Job is a completely free application providing latest government sector job listings (Sarkari Naukri), results checking, admit card downloads, calendars, syllabi and admission details fast and accurately across India.\n\nVerify notifications on official platforms before applying.",
                    fontSize = 14.sp
                )
            }
        )
    }

    if (showDisclaimerDialog) {
        AlertDialog(
            onDismissRequest = { showDisclaimerDialog = false },
            confirmButton = { Button(onClick = { showDisclaimerDialog = false }) { Text("OK") } },
            title = { Text("Disclaimer", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) },
            text = {
                Text(
                    "We are an independent platform aggregate and curate governmental listings purely for informational purposes. This application has ZERO official affiliation, liaison or representation of any state or national government agencies.",
                    fontSize = 14.sp
                )
            }
        )
    }

    if (showPrivacyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            confirmButton = { Button(onClick = { showPrivacyDialog = false }) { Text("OK") } },
            title = { Text("Privacy Policy", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "This app behaves with strict user privacy principles. It stores no personally identifiable information (PII). All bookmarked government career alerts are retained strictly locally on your Android device in a secure SQLite database using Jetpack Room orchestration.",
                    fontSize = 14.sp
                )
            }
        )
    }
}

@Composable
fun HomeScreen(viewModel: JobViewModel) {
    val context = LocalContext.current
    var inputQuery by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Simple search input
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSystemInDarkTheme()) Color(0xFF2B221D) else Color(0xFFF4ECE4)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = inputQuery,
                        onValueChange = { inputQuery = it },
                        placeholder = { Text("Search Jobs, Admit Cards, Results...") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("search_field"),
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedTextColor = MaterialTheme.colorScheme.onBackground,
                            unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Button(
                        onClick = { viewModel.submitSearch(inputQuery) },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.testTag("search_button")
                    ) {
                        Text("Search")
                    }
                }
            }
        }

        // Quick buttons container matching the colored grids of the web mockup
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "⭐ Categories",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CategoryBlock(
                        label = "Latest Jobs",
                        color = getCategoryColor("jobs"),
                        icon = Icons.Default.Work,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.setActiveTab("jobs") }
                    )
                    CategoryBlock(
                        label = "Admit Cards",
                        color = getCategoryColor("admit"),
                        icon = Icons.Default.ContactMail,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.setActiveTab("admit") }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CategoryBlock(
                        label = "Results",
                        color = getCategoryColor("result"),
                        icon = Icons.Default.CheckCircle,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.setActiveTab("result") }
                    )
                    CategoryBlock(
                        label = "Answer Key",
                        color = getCategoryColor("answerkey"),
                        icon = Icons.Default.VpnKey,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.setActiveTab("answerkey") }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CategoryBlock(
                        label = "Admissions",
                        color = getCategoryColor("admission"),
                        icon = Icons.Default.School,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.setActiveTab("admission") }
                    )
                    CategoryBlock(
                        label = "Syllabus",
                        color = getCategoryColor("syllabus"),
                        icon = Icons.Default.Book,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.setActiveTab("syllabus") }
                    )
                }
            }
        }

        // Main listings boxes mapping the left-middle-right cards of web view
        item {
            SectionBox(
                title = "🔴 Latest Jobs",
                color = getCategoryColor("jobs"),
                alerts = MockJobAlertData.jobs.take(6),
                onSelect = { viewModel.selectAlert(it) },
                onViewMore = { viewModel.setActiveTab("jobs") }
            )
        }

        item {
            SectionBox(
                title = "🎫 Admit Cards",
                color = getCategoryColor("admit"),
                alerts = MockJobAlertData.admit.take(6),
                onSelect = { viewModel.selectAlert(it) },
                onViewMore = { viewModel.setActiveTab("admit") }
            )
        }

        item {
            SectionBox(
                title = "📋 Results",
                color = getCategoryColor("result"),
                alerts = MockJobAlertData.result.take(6),
                onSelect = { viewModel.selectAlert(it) },
                onViewMore = { viewModel.setActiveTab("result") }
            )
        }

        // Important official websites section
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(1.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    Text(
                        text = "⭐ Important Official Portals",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Divider(color = MaterialTheme.colorScheme.primary, thickness = 1.5.dp)
                    Spacer(modifier = Modifier.height(8.dp))

                    val portals = listOf(
                        "UPSC Official" to "https://upsc.gov.in",
                        "SSC Official" to "https://ssc.gov.in",
                        "Railway Board" to "https://indianrailways.gov.in",
                        "IBPS Official" to "https://ibps.in",
                        "SBI Careers" to "https://sbi.co.in",
                        "UPPSC Board" to "https://uppsc.up.nic.in",
                        "BPSC Patna" to "https://bpsc.bih.nic.in",
                        "Indian Army" to "https://joinindianarmy.nic.in",
                        "NTA Exams" to "https://nta.ac.in",
                        "CBSE Board" to "https://cbse.gov.in"
                    )

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        portals.forEach { (name, link) ->
                            Text(
                                text = name,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .background(
                                        MaterialTheme.colorScheme.secondaryContainer,
                                        shape = RoundedCornerShape(4.dp)
                                    )
                                    .clickable { launchBrowser(context, link) }
                                    .padding(vertical = 6.dp, horizontal = 10.dp)
                            )
                        }
                    }
                }
            }
        }

        // Quick View Table
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(2.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    Text(
                        text = "📊 Latest Job Postings Table",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Table headers
                    val tableHeaderColor = if (isSystemInDarkTheme()) Color(0xFF2B221D) else Color(0xFFF4ECE4)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(tableHeaderColor, shape = RoundedCornerShape(4.dp))
                            .padding(vertical = 8.dp, horizontal = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Post Title", color = MaterialTheme.colorScheme.onBackground, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(2f))
                        Text("Agency", color = MaterialTheme.colorScheme.onBackground, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                        Text("Last Date", color = MaterialTheme.colorScheme.onBackground, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                    }

                    // Table rows
                    MockJobAlertData.jobs.take(5).forEachIndexed { index, job ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(if (index % 2 == 0) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                .clickable { viewModel.selectAlert(job) }
                                .padding(vertical = 8.dp, horizontal = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = job.title,
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(2f),
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = job.dept,
                                fontSize = 10.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1.1f),
                                textAlign = TextAlign.End,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                            Text(
                                text = job.date,
                                fontSize = 10.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(0.9f),
                                textAlign = TextAlign.End,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryBlock(
    label: String,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(54.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(6.dp),
        colors = CardDefaults.cardColors(containerColor = color)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label.uppercase(),
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}

@Composable
fun SectionBox(
    title: String,
    color: Color,
    alerts: List<JobAlert>,
    onSelect: (JobAlert) -> Unit,
    onViewMore: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(6.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color)
                    .padding(8.dp)
            ) {
                Text(
                    text = title.uppercase(),
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                alerts.forEach { alert ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(alert) }
                            .padding(vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "•  " + alert.title,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        if (alert.badge.isNotEmpty()) {
                            BadgeIndicator(badge = alert.badge)
                        }
                    }
                    Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), thickness = 0.5.dp)
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onViewMore)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "View All →",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
fun BadgeIndicator(badge: String) {
    val containerColor = if (badge == "new") Color(0xFFA03E00) else Color(0xFF5A7F5A)
    Box(
        modifier = Modifier
            .padding(start = 6.dp)
            .background(containerColor, shape = RoundedCornerShape(2.dp))
            .padding(horizontal = 4.dp, vertical = 1.dp)
    ) {
        Text(
            text = badge.uppercase(),
            color = Color.White,
            fontSize = 7.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun SectionScreen(category: String, viewModel: JobViewModel) {
    val alerts = remember(category) { MockJobAlertData.getAlertsByCategory(category) }
    var filterQuery by remember { mutableStateOf("") }

    val filteredAlerts = remember(alerts, filterQuery) {
        if (filterQuery.isBlank()) alerts else {
            alerts.filter {
                it.title.lowercase().contains(filterQuery.lowercase().trim()) ||
                it.dept.lowercase().contains(filterQuery.lowercase().trim())
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        Text(
            text = "${getCategoryLabel(category)} Listings",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = getCategoryColor(category),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        OutlinedTextField(
            value = filterQuery,
            onValueChange = { filterQuery = it },
            label = { Text("Filter results by keywords...") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            leadingIcon = { Icon(Icons.Default.FilterList, contentDescription = null) },
            singleLine = true
        )

        if (filteredAlerts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text("No alerts match your keywords.", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredAlerts) { alert ->
                    JobCard(
                        alert = alert,
                        onSelect = { viewModel.selectAlert(alert) }
                    )
                }
            }
        }
    }
}

@Composable
fun BookmarksScreen(viewModel: JobViewModel) {
    val bookmarkedJobs by viewModel.bookmarkedJobs.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        Text(
            text = "Saved Jobs & Alerts",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        if (bookmarkedJobs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.WorkOutline,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = Color.LightGray
                    )
                    Text(
                        text = "No saved jobs yet",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray
                    )
                    Text(
                        text = "Bookmark jobs and alerts to access them securely offline.",
                        fontSize = 12.sp,
                        color = Color.Gray.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 32.dp)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(bookmarkedJobs) { item ->
                    val jobAlert = JobAlert(
                        title = item.title,
                        dept = item.dept,
                        posts = item.posts,
                        date = item.date,
                        badge = item.badge,
                        category = item.category
                    )
                    JobCard(
                        alert = jobAlert,
                        onSelect = { viewModel.selectAlert(jobAlert) }
                    )
                }
            }
        }
    }
}

@Composable
fun SearchResultsScreen(viewModel: JobViewModel) {
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val searchResults by viewModel.searchResults.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Search: \"$searchQuery\"",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = { viewModel.setActiveTab("home") }) {
                Text("Clear search", color = MaterialTheme.colorScheme.primary)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (searchResults.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text("No results found. Try other keywords (e.g. ssc, upsssc, army)", color = Color.Gray)
            }
        } else {
            Text(
                text = "${searchResults.size} alerts found",
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(searchResults) { alert ->
                    JobCard(
                        alert = alert,
                        onSelect = { viewModel.selectAlert(alert) }
                    )
                }
            }
        }
    }
}

@Composable
fun JobCard(alert: JobAlert, onSelect: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = getCategoryLabel(alert.category).uppercase(),
                    color = Color.White,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier
                        .background(getCategoryColor(alert.category), shape = RoundedCornerShape(2.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )

                if (alert.badge.isNotEmpty()) {
                    BadgeIndicator(badge = alert.badge)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = alert.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "${alert.dept}  |  Last Date: ${alert.date}",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
fun ContactScreen() {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "📞 Contact Us",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Text(
            text = "For suggestions, feedback, correction requests, or advertisement enquiries, reach us on our official support lines below. We respond within 24 hours.",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
            lineHeight = 18.sp
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(2.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ContactItem(
                    label = "Support Email",
                    value = "ayushyadavcomic@gmail.com",
                    icon = Icons.Default.Mail,
                    onClick = {
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:ayushyadavcomic@gmail.com")
                            putExtra(Intent.EXTRA_SUBJECT, "All India Latest Job App Enquiry")
                        }
                        try {
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "Could not open email app.", Toast.LENGTH_SHORT).show()
                        }
                    }
                )

                ContactItem(
                    label = "Instagram Handle",
                    value = "@f1AYUSHYADAV",
                    icon = Icons.Default.CameraAlt,
                    onClick = {
                        launchBrowser(context, "https://instagram.com/f1AYUSHYADAV")
                    }
                )

                ContactItem(
                    label = "Twitter / X Profile",
                    value = "@Dcayushyadav",
                    icon = Icons.Default.AlternateEmail,
                    onClick = {
                        launchBrowser(context, "https://x.com/Dcayushyadav")
                    }
                )
            }
        }
    }
}

@Composable
fun ContactItem(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(label, fontSize = 11.sp, color = Color.Gray)
            Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        }

        Icon(Icons.Default.ArrowForwardIos, contentDescription = null, modifier = Modifier.size(12.dp), tint = Color.Gray)
    }
}

// Layout helper for wrapping portals lists
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable () -> Unit
) {
    androidx.compose.foundation.layout.FlowRow(
        modifier = modifier,
        horizontalArrangement = horizontalArrangement,
        verticalArrangement = verticalArrangement
    ) {
        content()
    }
}

fun getCategoryColor(cat: String): Color {
    return when (cat) {
        "jobs" -> Color(0xFFA03E00)       // Warm terracotta/copper
        "admit" -> Color(0xFF3B6B9C)      // Elegant warm blue
        "result" -> Color(0xFF5A7F5A)     // Cozy organic green / sage
        "answerkey" -> Color(0xFFD67A2A)  // Sunset orange ochre
        "admission" -> Color(0xFF7A4A8F)  // Warm berry mulberry
        "syllabus" -> Color(0xFF2A7C7C)   // Serene pine teal
        else -> Color(0xFF85736B)         // Sand slate
    }
}

fun getCategoryLabel(cat: String): String {
    return when (cat) {
        "jobs" -> "Latest Job"
        "admit" -> "Admit Card"
        "result" -> "Result"
        "answerkey" -> "Answer Key"
        "admission" -> "Admission"
        "syllabus" -> "Syllabus"
        else -> "ALERT"
    }
}

fun launchBrowser(context: Context, url: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "Could not open browser link.", Toast.LENGTH_SHORT).show()
    }
}

fun shareJob(context: Context, alert: JobAlert) {
    val text = """
        📢 Government Job Alert: ${alert.title}
        🏢 Department: ${alert.dept}
        🔢 Positions: ${alert.posts}
        📅 Last Date: ${alert.date}
        
        Stay updated! Get Sarkari exam admit cards & results alerts in realtime with All India Latest Job application.
        
        Download App & Track: https://AllIndiaLatestJob.com
    """.trimIndent()

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    try {
        context.startActivity(Intent.createChooser(intent, "Share Alert via"))
    } catch (e: Exception) {
        Toast.makeText(context, "Could not share.", Toast.LENGTH_SHORT).show()
    }
}
