package org.com.community.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.com.community.data.CommunityRepository
import org.com.core.ui.theme.AppColorScheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackReportScreen(
    onBack: () -> Unit,
    onReportFound: (String) -> Unit
) {
    var reportIdInput by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isSearching by remember { mutableStateOf(false) }

    val repository = remember { CommunityRepository() }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("TRACK A REPORT", fontWeight = FontWeight.Black) },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                "Enter Report ID",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Track the status and timeline of your reported issue.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
            )

            OutlinedTextField(
                value = reportIdInput,
                onValueChange = {
                    reportIdInput = it
                    errorMessage = null
                },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("e.g. CAM-20261006-00124") },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = MaterialTheme.colorScheme.primary
                ),
                singleLine = true
            )

            if (errorMessage != null) {
                Spacer(Modifier.height(8.dp))
                Text(errorMessage ?: "", color = Color(0xFFFF4444), fontSize = 12.sp)
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    if (reportIdInput.isBlank()) {
                        errorMessage = "Please enter a valid report ID."
                        return@Button
                    }
                    isSearching = true
                    scope.launch {
                        try {
                            val report = repository.trackReport(reportIdInput.trim())
                            if (report != null) {
                                onReportFound(report.id)
                            } else {
                                errorMessage = "No report found with ID: $reportIdInput"
                            }
                        } catch (e: Exception) {
                            errorMessage = "Error searching for report."
                        } finally {
                            isSearching = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                enabled = !isSearching
            ) {
                if (isSearching) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Icon(Icons.Default.Search, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Track Report", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
