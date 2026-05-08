package com.example.thulur.presentation.chat.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.thulur.domain.model.DailyFeedThread
import com.example.thulur.presentation.theme.thulurDp

@Composable
fun ThreadSectionSelector(
    threads: List<DailyFeedThread>,
    selectedThreadId: String?,
    onThreadSelected: (DailyFeedThread) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState()),
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