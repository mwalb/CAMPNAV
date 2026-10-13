package org.com.community.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.com.campus.data.University
import org.com.community.model.IssueReport

@Composable
expect fun IssuesMap(
    modifier: Modifier = Modifier,
    university: University? = null,
    issues: List<IssueReport> = emptyList(),
    selectedIssue: IssueReport? = null,
    onIssueSelected: (IssueReport?) -> Unit = {},
    onBack: () -> Unit = {}
)
