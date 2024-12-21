package de.thomaskuenneth.benice

import android.graphics.drawable.BitmapDrawable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.res.ResourcesCompat


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppChooser(
    installedApps: List<AppInfo>,
    columns: Int,
    letterPosition: Int,
    showSpecials: Boolean,
    shouldAddEmptySpace: Boolean = false,
    onClick: (AppInfo) -> Unit,
    onLongClick: (AppInfo) -> Unit,
    selectBitmap: () -> Unit
) {
    val iconColor = MaterialTheme.colorScheme.primary.toArgb()
    val context = LocalContext.current
    val onDoneUrl: (String) -> Unit = { url ->
        onClick(
            AppInfo(packageName = MIME_TYPE_URL,
                className = url,
                label = url.createLabelFromURL(context.getString(R.string.open_url)),
                icon = ResourcesCompat.getDrawable(
                    context.resources, R.drawable.baseline_open_in_browser_24, context.theme
                )!!.also {
                    it.setTint(iconColor)
                })
        )
    }
    val haptics = LocalHapticFeedback.current
    val displayCutoutPadding = WindowInsets.displayCutout.asPaddingValues()
    val left = displayCutoutPadding.calculateLeftPadding(LocalLayoutDirection.current)
    val right = displayCutoutPadding.calculateRightPadding(LocalLayoutDirection.current)
    if (installedApps.isEmpty()) {
        Text(
            text = stringResource(R.string.no_apps),
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Start
        )
    } else {
        val bottomPadding =
            with(LocalDensity.current) { WindowInsets.navigationBars.getBottom(this).toDp() }
        LazyVerticalGrid(
            modifier = Modifier.fillMaxSize(), columns = GridCells.Fixed(count = columns)
        ) {
            var last = ""
            var counter = 0
            if (showSpecials) {
                item {
                    OpenInBrowserMenuItem(onDone = onDoneUrl)
                }
                item {
                    ShowImageMenuItem(selectBitmap = selectBitmap, bitmapSelected = { bitmap, uri ->
                        bitmap?.run {
                            onClick(
                                AppInfo(
                                    packageName = MIME_TYPE_IMAGE,
                                    className = uri.toString(),
                                    label = context.getString(R.string.image),
                                    icon = BitmapDrawable(context.resources, bitmap)
                                )
                            )
                        }
                    })
                }
            }
            installedApps.forEach { appInfo ->
                appInfo.label.ifEmpty { "?" }.substring((0 until 1)).uppercase().let { current ->
                    if (current != last) {
                        last = current
                        header(key = counter++) {
                            Text(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                                    .clip(shape = MaterialTheme.shapes.small)
                                    .background(color = MaterialTheme.colorScheme.secondaryContainer)
                                    .padding(horizontal = 8.dp),
                                text = current,
                                style = MaterialTheme.typography.labelLarge,
                                textAlign = when (letterPosition) {
                                    0 -> TextAlign.Left
                                    1 -> TextAlign.Center
                                    else -> TextAlign.Right
                                },
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }
                item(key = counter++) {
                    AppChooserItem(
                        appInfo = appInfo, modifier = Modifier
                            .padding(
                                start = maxOf(left, 16.dp),
                                end = maxOf(right, 16.dp),
                                top = 8.dp,
                                bottom = 8.dp
                            )
                            .clip(shape = MaterialTheme.shapes.small)
                            .combinedClickable(
                                onClick = { onClick(appInfo) },
                                onLongClick = {
                                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onLongClick(appInfo)
                                },
                                onLongClickLabel = stringResource(id = R.string.open_context_menu)
                            )
                    )
                }
            }
            if (shouldAddEmptySpace) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Spacer(
                        modifier = Modifier.padding(
                            bottom = bottomPadding
                        )
                    )
                }
            }
        }
    }
}
