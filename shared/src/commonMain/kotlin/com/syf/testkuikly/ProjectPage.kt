package com.syf.testkuikly

import com.syf.testkuikly.base.openWebDetail
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.syf.testkuikly.base.Utils
import com.syf.testkuikly.data.Article
import com.syf.testkuikly.data.ProjectTag
import com.syf.testkuikly.project.ProjectIntent
import com.syf.testkuikly.project.ProjectState
import com.syf.testkuikly.project.ProjectViewModel
import com.tencent.kuikly.compose.coil3.rememberAsyncImagePainter
import com.tencent.kuikly.compose.foundation.ExperimentalFoundationApi
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
import com.tencent.kuikly.compose.foundation.layout.size
import com.tencent.kuikly.compose.foundation.layout.width
import com.tencent.kuikly.compose.foundation.layout.padding
import com.tencent.kuikly.compose.foundation.lazy.LazyColumn
import com.tencent.kuikly.compose.foundation.lazy.LazyRow
import com.tencent.kuikly.compose.foundation.lazy.items
import com.tencent.kuikly.compose.foundation.lazy.rememberLazyListState
import com.tencent.kuikly.compose.foundation.shape.RoundedCornerShape
import com.tencent.kuikly.compose.material3.Text
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.draw.clip
import com.tencent.kuikly.compose.ui.text.font.FontWeight
import com.tencent.kuikly.compose.foundation.Image
import com.tencent.kuikly.compose.ui.text.style.TextAlign
import com.tencent.kuikly.compose.ui.text.style.TextOverflow
import com.tencent.kuikly.compose.ui.unit.dp

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ProjectPage() {
    val viewModel = remember { ProjectViewModel() }
    var state by remember { mutableStateOf(viewModel.currentState) }
    val listState = rememberLazyListState()

    LaunchedEffect(Unit) {
        viewModel.sendIntent(ProjectIntent.LoadTags)
        viewModel.viewState.collect { state = it }
    }

    LaunchedEffect(listState) {
        if (!listState.canScrollForward && state.hasMore && !state.isLoadingMore && state.articles.isNotEmpty()) {
            viewModel.sendIntent(ProjectIntent.LoadMore)
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(AppColors.SurfaceVariant)) {
        // 标签选择器
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .background(AppColors.Surface)
                .padding(horizontal = AppSpacing.small, vertical = AppSpacing.small),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.small)
        ) {
            state.tags.forEach { tag ->
                val isSelected = tag.id == state.selectedTagId
                item(key = "tag_${tag.id}") {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .background(
                                if (isSelected) AppColors.Primary else AppColors.SurfaceContainer,
                                RoundedCornerShape(AppRadius.pill)
                            )
                            .clickable { viewModel.sendIntent(ProjectIntent.SelectTag(tag.id)) }
                    ) {
                        Text(
                            text = tag.name,
                            style = AppTypography.labelLarge,
                            color = if (isSelected) AppColors.OnPrimary else AppColors.OnSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(0.5.dp)
                .background(AppColors.OutlineVariant)
        )

        // 文章列表
        LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
            items(state.articles, key = { it.id }) { article ->
                ProjectArticleItem(article)
            }
            if (state.isLoadingMore) {
                item { LoadingFooter() }
            } else if (!state.hasMore && state.articles.isNotEmpty()) {
                item { NoMoreFooter() }
            }
        }
    }
}

@Composable
private fun ProjectArticleItem(article: Article) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { openWebDetail(article.link, article.title) }
            .background(AppColors.Surface)
            .padding(horizontal = AppSpacing.medium, vertical = AppSpacing.medium)
    ) {
        // 左侧缩略图
        if (article.envelopePic.isNotEmpty()) {
            Image(
                modifier = Modifier
                    .size(100.dp, 72.dp)
                    .clip(RoundedCornerShape(AppRadius.medium))
                    .background(AppColors.SurfaceVariant),
                painter = rememberAsyncImagePainter(article.envelopePic),
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(AppSpacing.small))
        }

        // 右侧内容
        Column(modifier = Modifier.weight(1f)) {
            // 标题
            Text(
                text = article.decodedTitle,
                style = AppTypography.titleMedium,
                color = AppColors.OnSurface,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.weight(1f))

            // 底部元信息
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = article.desc.ifEmpty { "点击查看详情" },
                    style = AppTypography.labelSmall,
                    color = AppColors.OnSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(AppSpacing.small))
                Text(
                    text = article.author.ifEmpty { article.shareUser.ifEmpty { "匿名" } },
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
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(0.5.dp)
            .padding(start = AppSpacing.medium)
            .background(AppColors.OutlineVariant)
    )
}
