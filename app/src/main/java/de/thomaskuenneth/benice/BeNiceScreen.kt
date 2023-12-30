package de.thomaskuenneth.benice

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun BeNiceScreen(
    state: BeNiceScreenUiState,
    onClick: (AppInfo) -> Unit,
    onAddLinkClicked: (AppInfo) -> Unit,
    modifier: Modifier
) {
    var contextMenuAppInfo by rememberSaveable { mutableStateOf<AppInfo?>(null) }
    val haptics = LocalHapticFeedback.current
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    )
    {
        when (state.isLoading) {
            true -> CircularProgressIndicator()
            false -> {
                if (state.installedApps.isEmpty()) {
                    Text(
                        text = stringResource(
                            id = if (state.filterOn) {
                                R.string.no_portrait_apps
                            } else {
                                R.string.no_apps
                            }
                        ),
                        style = MaterialTheme.typography.displaySmall,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )
                } else {
                    LazyVerticalGrid(
                        modifier = Modifier.align(Alignment.TopStart),
                        columns = GridCells.Fixed(count = 2)
                    ) {
                        state.installedApps.forEach { appInfo ->
                            item {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .combinedClickable(
                                            onClick = { onClick(appInfo) },
                                            onLongClick = {
                                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                                contextMenuAppInfo = appInfo
                                            },
                                            onLongClickLabel = stringResource(id = R.string.open_context_menu)
                                        )
                                        .padding(horizontal = 24.dp, vertical = 16.dp),
                                    horizontalArrangement = Arrangement.Start,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    AppIconImage(drawable = appInfo.icon)
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Text(
                                        text = appInfo.label,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        if (contextMenuAppInfo != null) {
            ModalBottomSheet(
                onDismissRequest = { contextMenuAppInfo = null },
                windowInsets = WindowInsets(0),
            ) {
                MenuItem(
                    onClick = {
                        contextMenuAppInfo?.let {
                            onClick(it)
                        }
                        contextMenuAppInfo = null
                    },
                    imageRes = R.drawable.baseline_launch_24,
                    textRes = R.string.launch
                )
                MenuItem(
                    onClick = {
                        contextMenuAppInfo?.let {
                            onAddLinkClicked(it)
                        }
                        contextMenuAppInfo = null
                    },
                    imageRes = R.drawable.baseline_add_link_24,
                    textRes = R.string.add_link
                )
            }
        }
    }
}
