package com.example.thulur.presentation.chat.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.thulur.domain.model.MainFeedThread
import com.example.thulur.presentation.theme.thulurDp

@Composable
fun ThreadSectionSelector(
    threads: List<MainFeedThread>,
    selectedThreadId: String?,
    onThreadSelected: (MainFeedThread) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.thulurDp()),
    ) {
        threads.forEach { thread ->
            ThreadSectionItem(
                label = thread.name,
                selected = thread.id == selectedThreadId,
                onClick = { onThreadSelected(thread) },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}