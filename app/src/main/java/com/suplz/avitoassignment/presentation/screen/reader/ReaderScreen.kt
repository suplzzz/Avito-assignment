@file:OptIn(ExperimentalMaterial3Api::class, FlowPreview::class)

package com.suplz.avitoassignment.presentation.screen.reader

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.suplz.avitoassignment.R
import com.suplz.avitoassignment.domain.entity.ReaderContent
import com.suplz.avitoassignment.presentation.util.ForceStatusBarAppearance
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged

@Composable
fun ReaderScreen(
    onBack: () -> Unit,
    viewModel: ReaderViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    ForceStatusBarAppearance(isDarkTheme = state.isDarkMode)
    val readerColorScheme = if (state.isDarkMode) darkColorScheme() else lightColorScheme()

    LaunchedEffect(state.isDeleted) {
        if (state.isDeleted) {
            onBack()
        }
    }

    MaterialTheme(colorScheme = readerColorScheme) {
        ReaderContent(
            state = state,
            onBack = onBack,
            onCommand = viewModel::processCommand
        )
    }
}

@Composable
private fun ReaderContent(
    state: ReaderUiState,
    onBack: () -> Unit,
    onCommand: (ReaderCommand) -> Unit
) {
    val listState = rememberLazyListState()
    var showSettingsSheet by remember { mutableStateOf(false) }

    LaunchedEffect(state.content.size) {
        if (state.content.isNotEmpty() && state.initialScrollIndex > 0) {
            listState.scrollToItem(state.initialScrollIndex)
        }
    }

    LaunchedEffect(listState, state.content) {
        if (state.content.isNotEmpty()) {
            snapshotFlow { listState.firstVisibleItemIndex }
                .distinctUntilChanged()
                .debounce(1000L)
                .collectLatest { index ->
                    if (index > 0) onCommand(ReaderCommand.SaveProgress(index))
                }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            val index = listState.firstVisibleItemIndex
            if (state.content.isNotEmpty() && index > 0) {
                onCommand(ReaderCommand.SaveProgress(index))
            }
        }
    }

    val progressPercentage by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val totalItems = layoutInfo.totalItemsCount
            if (totalItems == 0) return@derivedStateOf 0
            val visibleItemsInfo = layoutInfo.visibleItemsInfo
            if (visibleItemsInfo.isEmpty()) return@derivedStateOf 0
            val lastVisibleIndex = visibleItemsInfo.last().index
            val firstVisibleIndex = listState.firstVisibleItemIndex
            if (lastVisibleIndex == totalItems - 1) 100 else ((firstVisibleIndex.toFloat() / totalItems) * 100).toInt().coerceIn(0, 99)
        }
    }

    if (showSettingsSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSettingsSheet = false },
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
            sheetState = rememberModalBottomSheetState()
        ) {
            ReaderSettingsSheet(
                textSize = state.textSizeSp,
                isDarkMode = state.isDarkMode,
                onTextSizeChange = { onCommand(ReaderCommand.ChangeFontSize(it)) },
                onThemeToggle = { onCommand(ReaderCommand.ToggleTheme) }
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.bookTitle, maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 18.sp) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack,
                    stringResource(R.string.back_reading)
                ) } },
                actions = { IconButton(onClick = { showSettingsSheet = true }) { Icon(Icons.Default.FormatSize,
                    stringResource(R.string.settings)
                ) } },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        bottomBar = {
            if (!state.isLoading && state.error == null && state.content.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(stringResource(R.string.progress, progressPercentage), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)) {
            when {
                state.isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))

                state.error != null -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = stringResource(R.string.open_error, state.error),
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = { onCommand(ReaderCommand.DeleteBook) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                contentColor = MaterialTheme.colorScheme.onErrorContainer
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                        ) {
                            Icon(Icons.Default.DeleteForever, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.delete_and_close), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                else -> {
                    LazyColumn(
                        state = listState,
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp)
                    ) {
                        items(state.content) { item ->
                            when (item) {
                                is ReaderContent.Chapter -> Text(
                                    text = item.title,
                                    fontSize = (state.textSizeSp + 6).sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    modifier = Modifier.padding(top = 32.dp, bottom = 16.dp)
                                )
                                is ReaderContent.Text -> Text(
                                    text = item.text,
                                    fontSize = state.textSizeSp.sp,
                                    lineHeight = (state.textSizeSp * 1.5).sp,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    textAlign = TextAlign.Start,
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )
                                ReaderContent.Separator -> HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 24.dp),
                                    color = MaterialTheme.colorScheme.outlineVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReaderSettingsSheet(
    textSize: Int,
    isDarkMode: Boolean,
    onTextSizeChange: (Int) -> Unit,
    onThemeToggle: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .padding(
                bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
            )
    ) {
        Text(
            text = stringResource(R.string.settings_title),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(24.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.sp_settings),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                FilledTonalButton(
                    onClick = { onTextSizeChange(textSize - 2) },
                    enabled = textSize > 12,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(stringResource(R.string.a_minus), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }

                Text(
                    text = stringResource(R.string.sp, textSize),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                FilledTonalButton(
                    onClick = { onTextSizeChange(textSize + 2) },
                    enabled = textSize < 40,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(stringResource(R.string.a_plus), fontSize = 22.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                .clickable { onThemeToggle() }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.DarkMode,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = stringResource(R.string.dark_theme),
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Switch(
                checked = isDarkMode,
                onCheckedChange = { onThemeToggle() }
            )
        }
    }
}