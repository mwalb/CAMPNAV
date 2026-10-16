package org.com.community.presentation

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.com.campus.data.CampusRepository
import org.com.campus.data.University
import org.com.community.components.IssuesMap
import org.com.community.data.CommunityRepository
import org.com.community.model.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityMapScreen(
    initialUniversityId: Long? = 1L,
    onReportSelected: ((String) -> Unit)? = null,
    onBack: () -> Unit
) {
    val communityRepository = remember { CommunityRepository() }
    val campusRepository = remember { CampusRepository() }
    val scope = rememberCoroutineScope()

    var universities by remember {
        mutableStateOf(
            listOf(
                University(id = 1L, name = "UDSM", description = "University of Dar es Salaam", latitude = -6.780543, longitude = 39.204593, defaultZoom = 15f),
                University(id = 2L, name = "UDOM", description = "University of Dodoma", latitude = -6.1700, longitude = 35.7981, defaultZoom = 15f),
                University(id = 3L, name = "MUST", description = "Mbeya University of Science & Tech", latitude = -8.9328, longitude = 33.4390, defaultZoom = 15f),
                University(id = 4L, name = "SUA", description = "Sokoine University of Agriculture", latitude = -6.8480, longitude = 37.6530, defaultZoom = 15f)
            )
        )
    }

    var selectedUniversityId by remember { mutableStateOf<Long?>(initialUniversityId ?: 1L) }
    var selectedCategory by remember { mutableStateOf<IssueCategory?>(null) }
    var selectedReport by remember { mutableStateOf<IssueReport?>(null) }

    var allReports by remember { mutableStateOf<List<IssueReport>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var fetchError by remember { mutableStateOf<String?>(null) }

    // Load active universities list dynamically from backend
    LaunchedEffect(Unit) {
        scope.launch {
            try {
                val fetchedUnivs = campusRepository.getUniversities()
                if (fetchedUnivs.isNotEmpty()) {
                    universities = fetchedUnivs
                }
            } catch (_: Exception) {
                // Keep fallback universities list if offline
            }
        }
    }

    val selectedUniversityObj = remember(universities, selectedUniversityId) {
        universities.find { it.id == selectedUniversityId } ?: universities.firstOrNull()
    }

    // Load reports for the selected university
    fun loadReportsForSelectedUniversity() {
        isLoading = true
        fetchError = null
        allReports = emptyList() // Clear stale reports immediately when switching universities
        selectedReport = null

        val univId = selectedUniversityId
        scope.launch {
            try {
                val reports = if (univId != null) {
                    communityRepository.getReportsByUniversity(univId)
                } else {
                    communityRepository.getAllReports()
                }
                allReports = reports
                fetchError = null
            } catch (e: Exception) {
                println("[CAMPNAV] Error loading issues for university $univId: ${e.message}")
                fetchError = "Unable to connect to server. Check network connection."
            } finally {
                isLoading = false
            }
        }
    }

    // Refresh reports whenever selected university changes
    LaunchedEffect(selectedUniversityId) {
        loadReportsForSelectedUniversity()
    }

    // Filter reports by category
    val filteredReports = remember(allReports, selectedCategory) {
        allReports.filter { report ->
            selectedCategory == null || report.category == selectedCategory
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        // 1. Issues Map View
        IssuesMap(
            modifier = Modifier.fillMaxSize(),
            university = selectedUniversityObj,
            issues = filteredReports,
            selectedIssue = selectedReport,
            onIssueSelected = { selectedReport = it },
            onBack = onBack
        )

        // 2. Top Header with Back & Refresh Buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "Campus Issues Map",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = { loadReportsForSelectedUniversity() },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
                ) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = "Refresh",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                org.com.core.ui.components.ThemeToggleButton()
            }
        }

        // 3. University & Category Filter Bars
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(top = 64.dp)
        ) {
            // University Selection Bar
            LazyRow(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(universities) { univ ->
                    val isSelected = selectedUniversityId == univ.id
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            selectedUniversityId = univ.id
                        },
                        label = { Text(univ.name, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            // Category Filter Bar
            LazyRow(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedCategory == null,
                        onClick = { selectedCategory = null },
                        label = { Text("All Categories", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
                items(IssueCategory.entries.toTypedArray()) { cat ->
                    val isSelected = selectedCategory == cat
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategory = if (isSelected) null else cat },
                        label = { Text(cat.displayName, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
        }

        // 4. Loading Indicator Overlay
        if (isLoading) {
            Surface(
                modifier = Modifier.align(Alignment.Center),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.width(12.dp))
                    Text("Loading campus issues...", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        // 5. Network / Backend Error Banner
        fetchError?.let { err ->
            Surface(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 160.dp, start = 16.dp, end = 16.dp),
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFDC2626).copy(alpha = 0.9f),
                shadowElevation = 6.dp
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Icon(Icons.Default.Error, contentDescription = null, tint = Color.White)
                    Spacer(Modifier.width(8.dp))
                    Text(err, color = Color.White, fontSize = 12.sp, modifier = Modifier.weight(1f))
                    TextButton(onClick = { loadReportsForSelectedUniversity() }) {
                        Text("RETRY", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 6. University Empty-State Banner (When zero reported issues exist for selected university)
        if (!isLoading && fetchError == null && filteredReports.isEmpty()) {
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp, start = 20.dp, end = 20.dp),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                shadowElevation = 6.dp,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = "The selected university has no reported issues.",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // 7. Selected Issue Details Card
        selectedReport?.let { issue ->
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 12.dp,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = issue.category.displayName,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        IconButton(
                            onClick = { selectedReport = null },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Close details", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    Text(
                        text = issue.description,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("STATUS", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                            Text(issue.status.name, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }

                        Column {
                            Text("REF ID", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                            Text(issue.id.ifBlank { "CAM-REF" }, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        }

                        Column {
                            Text("URGENCY", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                            Text(issue.urgency, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (issue.urgency == "EMERGENCY") Color.Red else MaterialTheme.colorScheme.onSurface)
                        }
                    }

                    if (onReportSelected != null) {
                        Spacer(Modifier.height(12.dp))
                        Button(
                            onClick = { onReportSelected.invoke(issue.id) },
                            modifier = Modifier.fillMaxWidth().height(42.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("View Report Details", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}
