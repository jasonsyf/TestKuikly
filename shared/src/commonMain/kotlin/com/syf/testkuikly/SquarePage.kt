package com.syf.testkuikly

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.syf.testkuikly.square.SquareIntent
import com.syf.testkuikly.square.SquareState
import com.syf.testkuikly.square.SquareViewModel
import com.tencent.kuikly.compose.foundation.ExperimentalFoundationApi
import com.tencent.kuikly.compose.foundation.background
import com.tencent.kuikly.compose.foundation.clickable
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.layout.Column
import com.tencent.kuikly.compose.foundation.layout.Row
import com.tencent.kuikly.compose.foundation.layout.Spacer
import com.tencent.kuikly.compose.foundation.layout.fillMaxSize
import com.tencent.kuikly.compose.foundation.layout.fillMaxWidth
import com.tencent.kuikly.compose.foundation.layout.height
import com.tencent.kuikly.compose.foundation.layout.padding
import com.tencent.kuikly.compose.foundation.layout.width
import com.tencent.kuikly.compose.foundation.lazy.LazyColumn
import com.tencent.kuikly.compose.foundation.lazy.items
import com.tencent.kuikly.compose.foundation.lazy.rememberLazyListState
import com.tencent.kuikly.compose.foundation.shape.RoundedCornerShape
import com.tencent.kuikly.compose.material3.Text
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.compose.ui.text.font.FontWeight
import com.tencent.kuikly.compose.ui.unit.dp

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SquarePage() {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("问答", "广场")
    val viewModel = remember { SquareViewModel() }
    var state by remember { mutableStateOf(viewModel.currentState) }

    LaunchedEffect(Unit) { viewModel.viewState.collect { state = it } }
    LaunchedEffect(selectedTab) {
        when (selectedTab) {
            0 -> viewModel.sendIntent(SquareIntent.LoadWenda)
            1 -> viewModel.sendIntent(SquareIntent.LoadSquare)
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(AppColors.SurfaceVariant)) {
        // Tab 栏
        Row(
            modifier = Modifier.fillMaxWidth().background(AppColors.Surface)
        ) {
            tabs.forEachIndexed { index, title ->
                val isSelected = index == selectedTab
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { selectedTab = index }
                        .padding(top = AppSpacing.medium, bottom = AppSpacing.medium),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = title,
                        style = AppTypography.titleSmall,
                        color = if (isSelected) AppColors.Primary else AppColors.OnSurfaceVariant,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                    Spacer(modifier = Modifier.height(AppSpacing.extraSmall))
                    Box(
                        modifier = Modifier
                            .height(2.dp)
                            .width(if (isSelected) 24.dp else 0.dp)
                            .background(AppColors.Primary, RoundedCornerShape(1.dp))
                    )
                }
            }
        }
        Box(
            modifier = Modifier.fillMaxWidth().height(0.5.dp).background(AppColors.OutlineVariant)
        )

        when (selectedTab) {
            0 -> WendaTab(viewModel, state)
            1 -> SquareTab(viewModel, state)
        }
    }
}

@Composable
private fun WendaTab(viewModel: SquareViewModel, state: SquareState) {
    val listState = rememberLazyListState()
    LaunchedEffect(listState) {
        if (!listState.canScrollForward && state.wendaHasMore && !state.wendaLoadingMore && state.wendaArticles.isNotEmpty()) {
            viewModel.sendIntent(SquareIntent.LoadMoreWenda)
        }
    }
    LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
        items(state.wendaArticles, key = { it.id }) { ArticleItem(article = it) }
        if (state.wendaLoadingMore) { item { LoadingFooter() } }
    }
}

@Composable
private fun SquareTab(viewModel: SquareViewModel, state: SquareState) {
    val listState = rememberLazyListState()
    LaunchedEffect(listState) {
        if (!listState.canScrollForward && state.squareHasMore && !state.squareLoadingMore && state.squareArticles.isNotEmpty()) {
            viewModel.sendIntent(SquareIntent.LoadMoreSquare)
        }
    }
    LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
        items(state.squareArticles, key = { it.id }) { ArticleItem(article = it) }
        if (state.squareLoadingMore) { item { LoadingFooter() } }
    }
}
