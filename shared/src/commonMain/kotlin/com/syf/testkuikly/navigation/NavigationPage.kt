package com.syf.testkuikly.navigation

import com.syf.testkuikly.base.openWebDetail
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.syf.testkuikly.AppColors
import com.syf.testkuikly.AppRadius
import com.syf.testkuikly.AppSpacing
import com.syf.testkuikly.AppTypography
import com.syf.testkuikly.home.LoadingFooter
import com.syf.testkuikly.home.NoMoreFooter
import com.syf.testkuikly.data.Article
import com.tencent.kuikly.compose.foundation.background
import com.tencent.kuikly.compose.foundation.clickable
import com.tencent.kuikly.compose.foundation.layout.Arrangement
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.layout.Column
import com.tencent.kuikly.compose.foundation.layout.Row
import com.tencent.kuikly.compose.foundation.layout.Spacer
import com.tencent.kuikly.compose.foundation.layout.fillMaxHeight
import com.tencent.kuikly.compose.foundation.layout.fillMaxSize
import com.tencent.kuikly.compose.foundation.layout.fillMaxWidth
import com.tencent.kuikly.compose.foundation.layout.height
import com.tencent.kuikly.compose.foundation.layout.padding
import com.tencent.kuikly.compose.foundation.layout.width
import com.tencent.kuikly.compose.foundation.lazy.LazyColumn
import com.tencent.kuikly.compose.foundation.lazy.LazyRow
import com.tencent.kuikly.compose.foundation.lazy.items
import com.tencent.kuikly.compose.foundation.lazy.itemsIndexed
import com.tencent.kuikly.compose.foundation.lazy.rememberLazyListState
import com.tencent.kuikly.compose.foundation.shape.RoundedCornerShape
import com.tencent.kuikly.compose.material3.Text
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.draw.clip
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.compose.ui.text.font.FontWeight
import com.tencent.kuikly.compose.ui.text.style.TextOverflow
import com.tencent.kuikly.compose.ui.unit.dp

@Composable
fun NavigationPage() {
    var selectedTab by remember { mutableIntStateOf(0) }
    val viewModel = remember { NavigationViewModel() }
    var state by remember { mutableStateOf(viewModel.currentState) }

    LaunchedEffect(Unit) {
        viewModel.sendIntent(NavigationIntent.LoadData)
        viewModel.viewState.collect { state = it }
    }

    Column(modifier = Modifier.fillMaxSize().background(AppColors.SurfaceVariant)) {
        TabBar(
            tabs = listOf("体系", "导航"),
            selectedIndex = selectedTab,
            onSelect = {
                selectedTab = it
                viewModel.sendIntent(NavigationIntent.SwitchTab(it))
            }
        )
        when (selectedTab) {
            0 -> TreeTab(
                state = state,
                onSelectTree = { viewModel.sendIntent(NavigationIntent.SelectTree(it)) },
                onSelectChild = { viewModel.sendIntent(NavigationIntent.SelectChild(it)) },
                onLoadMore = { viewModel.sendIntent(NavigationIntent.LoadMoreTreeArticles) }
            )
            1 -> NaviTab(
                state = state,
                onSelectNavi = { viewModel.sendIntent(NavigationIntent.SelectNavi(it)) }
            )
        }
    }
}

@Composable
private fun TabBar(tabs: List<String>, selectedIndex: Int, onSelect: (Int) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().background(AppColors.Surface)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = AppSpacing.large),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.extraLarge)
        ) {
            tabs.forEachIndexed { index, title ->
                val selected = index == selectedIndex
                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(AppRadius.medium))
                        .clickable { onSelect(index) }
                        .padding(vertical = AppSpacing.medium),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = title,
                        style = AppTypography.titleSmall,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                        color = if (selected) AppColors.Primary else AppColors.OnSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .width(if (selected) 20.dp else 0.dp)
                            .height(3.dp)
                            .clip(RoundedCornerShape(1.5.dp))
                            .background(if (selected) AppColors.Primary else Color.Transparent)
                    )
                }
            }
        }
        Box(modifier = Modifier.fillMaxWidth().height(0.5.dp).background(AppColors.OutlineVariant))
    }
}

// ==================== 体系 Tab ====================

@Composable
private fun TreeTab(
    state: NavigationState,
    onSelectTree: (Int) -> Unit,
    onSelectChild: (Int) -> Unit,
    onLoadMore: () -> Unit
) {
    if (state.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("加载中...", style = AppTypography.labelLarge, color = AppColors.OnSurfaceVariant)
        }
        return
    }
    val tree = state.currentTree
    if (tree == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("暂无数据", style = AppTypography.labelLarge, color = AppColors.OnSurfaceVariant)
        }
        return
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // 一级分类
        LazyRow(
            modifier = Modifier.fillMaxWidth().background(AppColors.Surface).padding(start = AppSpacing.small, end = AppSpacing.small, top = AppSpacing.small, bottom = AppSpacing.extraSmall),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.extraSmall)
        ) {
            items(state.trees, key = { "tree_${it.id}" }) { t ->
                val isSelected = t.id == tree.id
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(AppRadius.pill))
                        .background(if (isSelected) AppColors.Primary else AppColors.SurfaceContainer)
                        .clickable {
                            val idx = state.trees.indexOf(t)
                            if (idx >= 0) onSelectTree(idx)
                        }
                        .padding(horizontal = 14.dp, vertical = 7.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = t.name,
                        style = AppTypography.labelLarge,
                        fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                        color = if (isSelected) AppColors.OnPrimary else AppColors.OnSurface
                    )
                }
            }
        }

        // 二级分类
        LazyRow(
            modifier = Modifier.fillMaxWidth().background(AppColors.Surface).padding(start = AppSpacing.medium, end = AppSpacing.medium, top = AppSpacing.extraSmall, bottom = AppSpacing.extraSmall),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.small)
        ) {
            items(tree.children, key = { "child_${it.id}" }) { child ->
                val index = tree.children.indexOf(child)
                val isSelected = index == state.selectedChildIndex
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(AppRadius.extraLarge))
                        .background(if (isSelected) AppColors.PrimaryContainer else Color.Transparent)
                        .clickable { onSelectChild(index) }
                        .padding(horizontal = AppSpacing.medium, vertical = AppSpacing.extraSmall),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = child.name,
                        style = AppTypography.labelLarge,
                        fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                        color = if (isSelected) AppColors.Primary else AppColors.OnSurfaceVariant
                    )
                }
            }
        }
        Box(modifier = Modifier.fillMaxWidth().height(0.5.dp).background(AppColors.OutlineVariant))

        // 文章列表
        val listState = rememberLazyListState()
        LaunchedEffect(listState) {
            if (!listState.canScrollForward && state.treeArticleHasMore && !state.isLoadingTreeArticles && state.treeArticles.isNotEmpty()) {
                onLoadMore()
            }
        }
        LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
            items(state.treeArticles, key = { "tree_art_${it.id}" }) { article ->
                TreeArticleItem(article)
            }
            if (state.isLoadingTreeArticles) {
                item { LoadingFooter() }
            } else if (!state.treeArticleHasMore && state.treeArticles.isNotEmpty()) {
                item { NoMoreFooter() }
            }
        }
    }
}

@Composable
private fun TreeArticleItem(article: Article) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppColors.Surface)
            .clickable { openWebDetail(article.link, article.title) }
            .padding(horizontal = AppSpacing.large, vertical = AppSpacing.medium)
    ) {
        if (article.fresh || article.tagNames.isNotEmpty()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (article.fresh) {
                    TagChip("新", AppColors.Success, AppColors.OnSuccess)
                    Spacer(modifier = Modifier.Companion.width(AppSpacing.extraSmall))
                }
                article.tagNames.forEach { tag ->
                    TagChip(tag, AppColors.Primary, AppColors.OnPrimary)
                    Spacer(modifier = Modifier.Companion.width(AppSpacing.extraSmall))
                }
            }
            Spacer(modifier = Modifier.Companion.height(AppSpacing.extraSmall))
        }
        Text(
            text = article.decodedTitle,
            style = AppTypography.titleMedium,
            color = AppColors.OnSurface,
            fontWeight = FontWeight.Medium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.Companion.height(AppSpacing.extraSmall))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = article.author.ifEmpty { article.shareUser.ifEmpty { "匿名" } }, style = AppTypography.labelMedium, color = AppColors.OnSurfaceVariant)
            Text(text = article.niceDate, style = AppTypography.labelMedium, color = AppColors.OnSurfaceVariant)
        }
    }
    Box(modifier = Modifier.fillMaxWidth().height(0.5.dp).padding(start = AppSpacing.large).background(
        AppColors.OutlineVariant))
}

// ==================== 导航 Tab ====================

@Composable
private fun NaviTab(state: NavigationState, onSelectNavi: (Int) -> Unit) {
    if (state.isLoading && !state.navLoaded) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("加载中...", style = AppTypography.labelLarge, color = AppColors.OnSurfaceVariant)
        }
        return
    }
    if (state.navLoaded && state.navItems.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("暂无数据", style = AppTypography.labelLarge, color = AppColors.OnSurfaceVariant)
        }
        return
    }

    val naviItem = state.currentNaviItem

    Row(modifier = Modifier.fillMaxSize()) {
        // 左侧导航栏
        LazyColumn(
            modifier = Modifier.width(100.dp).fillMaxHeight().background(AppColors.SurfaceContainerLow)
        ) {
            item { Spacer(modifier = Modifier.Companion.height(AppSpacing.extraSmall)) }
            itemsIndexed(state.navItems, key = { index, item -> "navi_sidebar_${index}_${item.id}" }) { index, item ->
                val isSelected = state.navItems.indexOf(naviItem) == index
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(if (isSelected) AppColors.Surface else Color.Transparent)
                        .clickable { onSelectNavi(index) }
                        .padding(horizontal = AppSpacing.small, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .width(3.dp)
                                .height(16.dp)
                                .clip(RoundedCornerShape(1.5.dp))
                                .background(AppColors.Primary)
                        )
                        Spacer(modifier = Modifier.Companion.width(AppSpacing.extraSmall))
                    } else {
                        Spacer(modifier = Modifier.Companion.width(AppSpacing.small))
                    }
                    Text(
                        text = item.name,
                        style = if (isSelected) AppTypography.labelLarge else AppTypography.labelMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) AppColors.Primary else AppColors.OnSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
        Box(modifier = Modifier.width(0.5.dp).fillMaxHeight().background(AppColors.OutlineVariant))

        // 右侧内容
        if (naviItem != null) {
            LazyColumn(
                modifier = Modifier.fillMaxHeight().weight(1f).background(AppColors.Surface)
            ) {
                item(key = "navi_header") {
                    Text(
                        text = naviItem.name,
                        style = AppTypography.titleLarge,
                        color = AppColors.OnSurface,
                        modifier = Modifier.padding(start = AppSpacing.medium, end = AppSpacing.medium, top = AppSpacing.medium, bottom = AppSpacing.small)
                    )
                    Box(modifier = Modifier.fillMaxWidth().height(0.5.dp).background(AppColors.OutlineVariant))
                }
                items(naviItem.articles, key = { "navi_art_${it.id}" }) { article ->
                    NaviArticleItem(article)
                }
                item { Spacer(modifier = Modifier.Companion.height(AppSpacing.medium)) }
            }
        } else {
            Box(modifier = Modifier.fillMaxHeight().weight(1f), contentAlignment = Alignment.Center) {
                Text("请选择导航分类", style = AppTypography.labelLarge, color = AppColors.OnSurfaceVariant)
            }
        }
    }
}

@Composable
private fun NaviArticleItem(article: Article) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AppSpacing.medium, vertical = AppSpacing.extraSmall)
            .clip(RoundedCornerShape(AppRadius.small))
            .background(AppColors.SurfaceVariant)
            .clickable { openWebDetail(article.link, article.title) }
            .padding(10.dp)
    ) {
        Text(
            text = article.decodedTitle,
            style = AppTypography.titleSmall,
            color = AppColors.OnSurface,
            fontWeight = FontWeight.Medium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.Companion.height(AppSpacing.extraSmall))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = article.author.ifEmpty { article.shareUser.ifEmpty { "匿名" } }, style = AppTypography.labelMedium, color = AppColors.OnSurfaceVariant)
            Text(text = article.niceDate, style = AppTypography.labelMedium, color = AppColors.OnSurfaceVariant)
        }
    }
}

// ==================== 标签 Chip (复用) ====================

@Composable
private fun TagChip(text: String, bgColor: Color, textColor: Color) {
    Text(
        text = text,
        style = AppTypography.labelSmall,
        color = textColor,
        modifier = Modifier
            .background(bgColor, RoundedCornerShape(AppRadius.extraSmall))
            .padding(horizontal = 5.dp, vertical = 1.dp)
    )
}
