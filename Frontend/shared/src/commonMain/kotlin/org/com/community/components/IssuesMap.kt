package org.com.community.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlinx.serialization.Serializable
import org.com.campus.data.University
import org.com.community.model.IssueReport

@Serializable
data class MapIssueMarker(
    val id: String,
    val lat: Double,
    val lng: Double,
    val cat: String,
    val desc: String,
    val status: String
)

@Composable
expect fun IssuesMap(
    modifier: Modifier = Modifier,
    university: University? = null,
    issues: List<IssueReport> = emptyList(),
    selectedIssue: IssueReport? = null,
    onIssueSelected: (IssueReport?) -> Unit = {},
    onBack: () -> Unit = {}
)
