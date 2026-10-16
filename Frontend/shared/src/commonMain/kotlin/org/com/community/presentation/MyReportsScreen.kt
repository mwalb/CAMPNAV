package org.com.community.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.com.campus.data.CampusRepository
import org.com.campus.data.University
import org.com.community.data.CommunityRepository
import org.com.community.model.IssueReport
import org.com.core.ui.theme.AppColorScheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyReportsScreen(
    initialUniversityId: Long? = 1L,
    onBack: () -> Unit,
    onReportSelected: (String) -> Unit
) {
    val communityRepository = remember { CommunityRepository() }
    val campusRepository = remember { CampusRepository() }
    val scope = rememberCoroutineScope()

    var universities by remember {
        mutableStateOf(
            listOf(
                University(id = 1L, name = "UDSM", description = "University of Dar es Salaam"),
                University(id = 2L, name = "UDOM", description = "University of Dodoma"),
                University(id = 3L, name = "MUST", description = "Mbeya University of Science & Tech"),
                University(id = 4L, name = "SUA", description = "Sokoine University of Agriculture")
            )
        )
    }

    var selectedUniversityId by remember { mutableStateOf<Long?>(initialUniversityId ?: 1L) }
    var reports by remember { mutableStateOf<List<IssueReport>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // Fetch active universities list dynamically
    LaunchedEffect(Unit) {
        scope.launch {
            try {
                val fetchedUnivs = campusRepository.getUniversities()
                if (fetchedUnivs.isNotEmpty()) {
                    universities = fetchedUnivs
                }
            } catch (_: Exception) {
                // Keep default list
            }
        }
    }

    // Load reports for selected university whenever selection changes
    fun loadReports() {
        isLoading = true
        reports = emptyList() // Immediately clear stale reports
        val univId = selectedUniversityId
        scope.launch {
            try {
                reports = if (univId != null) {
                    communityRepository.getReportsByUniversity(univId)
                } else {
                    communityRepository.getAllReports()
                }
            } catch (_: Exception) {
                reports = emptyList()
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(selectedUniversityId) {
        loadReports()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("REPORTS", fontWeight = FontWeight.Black) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = AppColorScheme.background
    ) { padding ->
        org.com.core.ui.components.CenteredPageContainer(
            maxWidth = 760.dp,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
            // University Filter Chips Bar
            LazyRow(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedUniversityId == null,
                        onClick = { selectedUniversityId = null },
                        label = { Text("All Universities", fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
                items(universities) { univ ->
                    val isSelected = selectedUniversityId == univ.id
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedUniversityId = univ.id },
                        label = { Text(univ.name, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            Box(modifier = Modifier.fillMaxSize()) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.primary
                    )
                } else if (reports.isEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Assignment,
                            null,
                            modifier = Modifier.size(64.dp),
                            tint = Color.Gray.copy(alpha = 0.5f)
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = if (selectedUniversityId != null)
                                "The selected university has no reported issues."
                            else "No reported issues found.",
                            color = Color.Gray,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(reports) { report ->
                            Card(
                                modifier = Modifier.fillMaxWidth().clickable { onReportSelected(report.id) },
                                colors = CardDefaults.cardColors(containerColor = AppColorScheme.surfaceVariant.copy(alpha = 0.5f)),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(report.id, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary, fontSize = 12.sp)
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = when (report.status.name) {
                                                "RESOLVED", "COMMUNITY_VERIFIED" -> Color(0xFF4CAF50).copy(alpha = 0.2f)
                                                "IN_PROGRESS", "ASSIGNED" -> Color(0xFFFF9800).copy(alpha = 0.2f)
                                                else -> Color(0xFF2196F3).copy(alpha = 0.2f)
                                            }
                                        ) {
                                            Text(
                                                report.status.name.replace("_", " "),
                                                color = when (report.status.name) {
                                                    "RESOLVED", "COMMUNITY_VERIFIED" -> Color(0xFF4CAF50)
                                                    "IN_PROGRESS", "ASSIGNED" -> Color(0xFFFF9800)
                                                    else -> Color(0xFF2196F3)
                                                },
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 10.sp,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }

                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        report.description.take(60) + if (report.description.length > 60) "..." else "",
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                        Text("Category: ${report.category.displayName}", color = Color.Gray, fontSize = 11.sp)
                                        Text("Urgency: ${report.userPriority}", color = Color.Gray, fontSize = 11.sp)
                                        report.universityName?.let { uName ->
                                            Text("Campus: $uName", color = MaterialTheme.colorScheme.primary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
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
