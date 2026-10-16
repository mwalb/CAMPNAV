package org.com.core.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun CenteredPageContainer(
    modifier: Modifier = Modifier,
    maxWidth: Dp = 680.dp,
    horizontalPadding: Dp = 16.dp,
    verticalPadding: Dp = 16.dp,
    scrollable: Boolean = false,
    content: @Composable BoxScope.() -> Unit
) {
    val boxModifier = if (scrollable) {
        modifier.fillMaxSize().verticalScroll(rememberScrollState())
    } else {
        modifier.fillMaxSize()
    }

    Box(
        modifier = boxModifier,
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = maxWidth)
                .padding(horizontal = horizontalPadding, vertical = verticalPadding),
            content = content
        )
    }
}
