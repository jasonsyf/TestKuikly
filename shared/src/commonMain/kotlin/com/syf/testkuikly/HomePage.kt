package com.syf.testkuikly

import com.syf.testkuikly.base.openWebDetail
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.syf.testkuikly.data.Article
import com.syf.testkuikly.data.BannerItem
import com.syf.testkuikly.home.HomeEffect
import com.syf.testkuikly.home.HomeIntent
import com.syf.testkuikly.home.HomeState
import com.syf.testkuikly.home.HomeViewModel
import com.tencent.kuikly.compose.coil3.rememberAsyncImagePainter
import com.tencent.kuikly.compose.foundation.Image
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
import com.tencent.kuikly.compose.foundation.layout.size
import com.tencent.kuikly.compose.foundation.layout.width
import com.tencent.kuikly.compose.foundation.lazy.LazyColumn
import com.tencent.kuikly.compose.foundation.lazy.items
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
import kotlinx.coroutines.delay

@Composable
fun HomePage() {
    val viewModel = remember { HomeViewModel() }
    var state by remember { mutableStateOf(viewModel.currentState) }
    val listState = rememberLazyListState()

    LaunchedEffect(Unit) {
        viewModel.sendIntent(HomeIntent.LoadInitial)
        viewModel.viewState.collect { state = it }
    }

    LaunchedEffect(listState) {
        if (!listState.canScrollForward && state.hasMore && !state.isLoadingMore && !state.isRefreshing && state.articles.isNotEmpty()) {
            viewModel.sendIntent(HomeIntent.LoadMore)
        }
    }

    LaunchedEffect(viewModel.effect) {
        viewModel.effect.collect { effect ->
            when (effect) {
                HomeEffect.ShowError -> { /* TODO: show toast */ }
            }
        }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize()
    ) {
        if (state.banners.isNotEmpty()) {
            item { BannerSection(state.banners) }
            item { Spacer(modifier = Modifier.height(AppSpacing.small)) }
        }

        state.topArticles.forEach { article ->
            item(key = "top_${article.id}") { ArticleItem(article = article, isTop = true) }
        }

        items(state.allArticles.filter { !it.top }, key = { it.id }) { article ->
            ArticleItem(article = article)
        }

        if (state.isLoadingMore) {
            item { LoadingFooter() }
        } else if (!state.hasMore && state.articles.isNotEmpty()) {
            item { NoMoreFooter() }
        }
    }
}

// ==================== Banner ====================

@Composable
private fun BannerSection(banners: List<BannerItem>) {
    if (banners.isEmpty()) return
    var currentIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(banners.size) {
        if (banners.size > 1) {
            while (true) { delay(3000); currentIndex = (currentIndex + 1) % banners.size }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(horizontal = AppSpacing.medium, vertical = AppSpacing.small)
            .clip(RoundedCornerShape(AppRadius.large))
            .clickable { openWebDetail(banners[currentIndex].url, banners[currentIndex].title) }
    ) {
        val currentBanner = banners[currentIndex]
        Image(
            modifier = Modifier.fillMaxSize(),
            painter = rememberAsyncImagePainter(currentBanner.imagePath),
            contentDescription = null
        )
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(
                    AppColors.Black.copy(alpha = 0.45f),
                    RoundedCornerShape(bottomStart = AppRadius.large, bottomEnd = AppRadius.large)
                )
                .padding(horizontal = AppSpacing.medium, vertical = AppSpacing.small)
        ) {
            Text(
                text = currentBanner.title,
                style = AppTypography.titleSmall,
                color = AppColors.OnPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                banners.forEachIndexed { index, _ ->
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 3.dp)
                            .size(if (index == currentIndex) 18.dp else 6.dp, 3.dp)
                            .background(
                                if (index == currentIndex) AppColors.OnPrimary else AppColors.OnPrimary.copy(alpha = 0.5f),
                                RoundedCornerShape(1.5.dp)
                            )
                    )
                }
            }
        }
    }
}

// ==================== 文章列表项 ====================

@Composable
fun ArticleItem(article: Article, isTop: Boolean = false) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { openWebDetail(article.link, article.title) }
            .background(AppColors.Surface)
            .padding(horizontal = AppSpacing.medium, vertical = AppSpacing.medium)
    ) {
        // 标签行
        if (isTop || article.top || article.fresh || article.tagNames.isNotEmpty()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isTop || article.top) {
                    TagChip("置顶", AppColors.Primary, AppColors.OnPrimary)
                    Spacer(modifier = Modifier.width(AppSpacing.extraSmall))
                }
                if (article.fresh) {
                    TagChip("新", AppColors.Success, AppColors.OnSuccess)
                    Spacer(modifier = Modifier.width(AppSpacing.extraSmall))
                }
                article.tagNames.forEach { tag ->
                    TagChip(tag, AppColors.Tertiary, AppColors.OnTertiary)
                    Spacer(modifier = Modifier.width(AppSpacing.extraSmall))
                }
            }
            Spacer(modifier = Modifier.height(AppSpacing.extraSmall))
        }

        // 标题
        Text(
            text = article.decodedTitle,
            style = AppTypography.titleMedium,
            color = AppColors.OnSurface,
            fontWeight = FontWeight.Medium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(AppSpacing.extraSmall))

        // 描述
        Text(
            text = article.decodedDesc.ifEmpty { "点击查看详情" },
            style = AppTypography.labelLarge,
            color = AppColors.OnSurfaceVariant,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis
        )

        // 缩略图
        if (article.envelopePic.isNotEmpty()) {
            Spacer(modifier = Modifier.height(AppSpacing.small))
            Image(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(AppRadius.medium))
                    .background(AppColors.SurfaceVariant),
                painter = rememberAsyncImagePainter(article.envelopePic),
                contentDescription = null
            )
        }

        Spacer(modifier = Modifier.height(AppSpacing.extraSmall))

        // 底部元信息
        val author = if (article.author.isNotEmpty()) article.author else article.shareUser
        val chapterText = (if (article.superChapterName.isNotEmpty()) "${article.superChapterName} / " else "") + article.chapterName
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = chapterText,
                style = AppTypography.labelSmall,
                color = AppColors.OnSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(AppSpacing.small))
            Text(
                text = author.ifEmpty { "匿名" },
                style = AppTypography.labelSmall,
                color = AppColors.OnSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.width(AppSpacing.small))
            Text(
                text = article.niceDate,
                style = AppTypography.labelSmall,
                color = AppColors.OnSurfaceVariant,
                maxLines = 1
            )
        }
    }

    // 分隔线
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(0.5.dp)
            .padding(start = AppSpacing.large)
            .background(AppColors.OutlineVariant)
    )
}

// ==================== 标签 Chip ====================

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

// ==================== Footer ====================

@Composable
internal fun LoadingFooter() {
    Box(
        modifier = Modifier.fillMaxWidth().padding(AppSpacing.large),
        contentAlignment = Alignment.Center
    ) {
        Text("加载中...", style = AppTypography.labelLarge, color = AppColors.OnSurfaceVariant)
    }
}

@Composable
internal fun NoMoreFooter() {
    Box(
        modifier = Modifier.fillMaxWidth().padding(AppSpacing.large),
        contentAlignment = Alignment.Center
    ) {
        Text("- 没有更多了 -", style = AppTypography.labelLarge, color = AppColors.OnSurfaceVariant)
    }
}
