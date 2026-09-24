package org.com.campus.presentation

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
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

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun UniversitySelectionScreen(
    onUniversitySelected: (University) -> Unit,
    onBack: () -> Unit
) {
    val repository = remember { CampusRepository() }
    var universities by remember { mutableStateOf(emptyList<University>()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var retryCount by remember { mutableStateOf(0) }

    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(retryCount) {
        isLoading = true
        error = null
        try {
            universities = repository.getUniversities()
            if (universities.isEmpty()) {
                error = "No universities found on the server."
            }
            visible = true
        } catch (e: Exception) {
            println("Connection Error: ${e.message}")
            error = "Unable to connect to the server at ${org.com.core.network.ApiConfig.getBaseUrl()}.\nPlease ensure the backend is running."
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                Spacer(Modifier.height(24.dp))

                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(tween(800)) + slideInVertically(tween(800)) { -40 }
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "CAMPNAV",
                            style = MaterialTheme.typography.displayMedium,
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = "Campus Navigation",
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color.White.copy(alpha = 0.7f),
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))

                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(tween(800, 200)) + slideInVertically(tween(800, 200)) { 20 }
                ) {
                    Text(
                        text = "CHOOSE YOUR UNIVERSITY",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 48.dp)
                    )
                }

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
                    FlowRow(
                        modifier = Modifier
                            .widthIn(max = 1400.dp)
                            .padding(horizontal = 24.dp, vertical = 24.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalArrangement = Arrangement.spacedBy(32.dp),
                        maxItemsInEachRow = 3
                    ) {
                        universities.forEachIndexed { index, university ->
                            UniversityCardAnimated(
                                university = university,
                                onClick = { onUniversitySelected(university) },
                                index = index,
                                parentVisible = visible,
                                modifier = Modifier
                                    .padding(horizontal = 16.dp)
                                    .width(380.dp)
                            )
                        }
                    }
                }
                Spacer(Modifier.height(64.dp))
            }
        }
    }
}

@Composable
fun UniversityCardAnimated(
    university: University,
    onClick: () -> Unit,
    index: Int,
    parentVisible: Boolean,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (parentVisible) 1f else 0.7f,
        animationSpec = tween(600, delayMillis = 400 + (index * 100))
    )
    val alpha by animateFloatAsState(
        targetValue = if (parentVisible) 1f else 0f,
        animationSpec = tween(600, delayMillis = 400 + (index * 100))
    )

    UniversityCard(
        university = university,
        onClick = onClick,
        modifier = modifier.scale(scale).alpha(alpha)
    )
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
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier
                    .size(140.dp)
                    .clip(RoundedCornerShape(24.dp)),
                color = Color.White
            ) {
                // If a local high-quality logo is available, we could use it here.
                // For now, using the seeded URL.
                AsyncImage(
                    model = university.logoUrl,
                    contentDescription = "${university.name} Logo",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Text(
                text = university.name.uppercase(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                textAlign = TextAlign.Center,
                lineHeight = 24.sp,
                minLines = 2
            )

            Text(
                text = university.shortName ?: "",
                style = MaterialTheme.typography.bodyLarge,
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
                    modifier = Modifier.padding(top = 16.dp).heightIn(min = 32.dp),
                    maxLines = 2
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Explore Campus", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(Modifier.width(8.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
            }
        }
    }
}
