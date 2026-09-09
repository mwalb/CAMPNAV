package org.com.campus.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import org.com.campus.data.CampusRepository
import org.com.campus.data.University
import org.com.core.ui.theme.AppColorScheme

@Composable
fun UniversitySelectionScreen(
    onUniversitySelected: (University) -> Unit
) {
    val repository = remember { CampusRepository() }
    var universities by remember { mutableStateOf(emptyList<University>()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var retryCount by remember { mutableStateOf(0) }

    LaunchedEffect(retryCount) {
        isLoading = true
        error = null
        try {
            universities = repository.getUniversities()
            if (universities.isEmpty()) {
                error = "No universities found on the server."
            }
        } catch (e: Exception) {
            println("Connection Error: ${e.message}")
            error = "Unable to connect to the server at ${org.com.core.network.ApiConfig.getBaseUrl()}.\nPlease ensure the backend is running."
        } finally {
            isLoading = false
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColorScheme.background)
    ) {
        val isLargeScreen = maxWidth > 900.dp
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(Modifier.height(48.dp))

            Text(
                text = "CAMPUS NAVIGATION",
                style = MaterialTheme.typography.displayMedium,
                color = Color.White,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )

            Text(
                text = "CHOOSE YOUR UNIVERSITY",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp, bottom = 48.dp)
            )

            if (isLoading) {
                Box(modifier = Modifier.height(400.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else if (error != null) {
                Box(modifier = Modifier.height(400.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = error!!,
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(Modifier.height(24.dp))
                        Button(
                            onClick = { retryCount++ },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Retry Connection")
                        }
                    }
                }
            } else {
                if (isLargeScreen) {
                    Row(
                        modifier = Modifier
                            .widthIn(max = 1200.dp)
                            .height(IntrinsicSize.Max)
                            .padding(bottom = 48.dp),
                        horizontalArrangement = Arrangement.spacedBy(32.dp, Alignment.CenterHorizontally),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        universities.forEach { university ->
                            UniversityCard(
                                university = university,
                                onClick = { onUniversitySelected(university) },
                                modifier = Modifier.weight(1f).fillMaxHeight()
                            )
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .widthIn(max = 500.dp)
                            .padding(bottom = 48.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(32.dp)
                    ) {
                        universities.forEach { university ->
                            UniversityCard(
                                university = university,
                                onClick = { onUniversitySelected(university) },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(48.dp))
        }
    }
}

@Composable
fun UniversityCard(
    university: University,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(28.dp))
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = AppColorScheme.surface),
        shape = RoundedCornerShape(28.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier
                    .size(140.dp)
                    .clip(RoundedCornerShape(20.dp)),
                color = Color.White.copy(alpha = 0.05f)
            ) {
                AsyncImage(
                    model = university.logoUrl,
                    contentDescription = "${university.name} Logo",
                    modifier = Modifier.padding(16.dp).fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = university.name.uppercase(),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                textAlign = TextAlign.Center,
                lineHeight = 28.sp,
                modifier = Modifier.heightIn(min = 56.dp)
            )

            Text(
                text = university.shortName ?: "",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 8.dp)
            )
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 12.dp)
            ) {
                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = "${university.city}, ${university.country}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }

            if (university.description != null) {
                Text(
                    text = university.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 16.dp).heightIn(min = 32.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Explore Campus", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(Modifier.width(12.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(20.dp))
            }
        }
    }
}
