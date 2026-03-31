package com.syf.testkuikly

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.tencent.kuikly.compose.setContent
import com.tencent.kuikly.compose.foundation.Canvas
import com.tencent.kuikly.compose.foundation.background
import com.tencent.kuikly.compose.foundation.clickable
import com.tencent.kuikly.compose.foundation.layout.Arrangement
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.layout.Column
import com.tencent.kuikly.compose.foundation.layout.Row
import com.tencent.kuikly.compose.foundation.layout.Spacer
import com.tencent.kuikly.compose.foundation.layout.fillMaxSize
import com.tencent.kuikly.compose.foundation.layout.fillMaxWidth
import com.tencent.kuikly.compose.foundation.layout.height
import com.tencent.kuikly.compose.foundation.layout.padding
import com.tencent.kuikly.compose.foundation.layout.size
import com.tencent.kuikly.compose.foundation.layout.width
import com.tencent.kuikly.compose.foundation.shape.RoundedCornerShape
import com.tencent.kuikly.compose.material3.Text
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.geometry.CornerRadius
import com.tencent.kuikly.compose.ui.geometry.Offset
import com.tencent.kuikly.compose.ui.geometry.Size
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.compose.ui.graphics.Path
import com.tencent.kuikly.compose.ui.graphics.StrokeCap
import com.tencent.kuikly.compose.ui.graphics.drawscope.DrawScope
import com.tencent.kuikly.compose.ui.graphics.drawscope.Stroke
import com.tencent.kuikly.compose.ui.platform.LocalActivity
import com.tencent.kuikly.compose.ui.unit.dp
import com.syf.testkuikly.base.BasePager
import com.tencent.kuikly.core.annotations.Page

/**
 * 主页面
 * 包含底部导航栏和五个主要功能页面
 */
@Page("main", supportInLocal = true)
internal class MainPage : BasePager() {

    override fun willInit() {
        super.willInit()
        setContent {
            MainContent()
        }
    }
}

@Composable
private fun MainContent() {
    var selectedIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("首页", "项目", "广场", "导航", "我的")
    val statusBarHeight = LocalActivity.current.pageData.statusBarHeight

    Column(modifier = Modifier.fillMaxSize().background(AppColors.SurfaceVariant)) {
        Spacer(modifier = Modifier.height((statusBarHeight).dp))

        Box(modifier = Modifier.weight(1f)) {
            when (selectedIndex) {
                0 -> HomePage()
                1 -> ProjectPage()
                2 -> SquarePage()
                3 -> NavigationPage()
                4 -> MinePage()
            }
        }

        BottomNavBar(
            tabs = tabs,
            selectedIndex = selectedIndex,
            onSelect = { selectedIndex = it }
        )
    }
}

@Composable
private fun BottomNavBar(
    tabs: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppColors.Surface)
    ) {
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(0.5.dp)
                .background(AppColors.OutlineVariant)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp, bottom = 6.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            tabs.forEachIndexed { index, title ->
                val isSelected = selectedIndex == index
                val color = if (isSelected) AppColors.Primary else AppColors.BottomNavInactive
                Column(
                    modifier = Modifier
                        .clickable { onSelect(index) }
                        .padding(horizontal = AppSpacing.small, vertical = AppSpacing.extraSmall),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Canvas(modifier = Modifier.size(22.dp)) {
                        when (index) {
                            0 -> drawHomeIcon(color)
                            1 -> drawProjectIcon(color)
                            2 -> drawSquareIcon(color)
                            3 -> drawNavigationIcon(color)
                            4 -> drawMineIcon(color)
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = title,
                        style = AppTypography.labelSmall,
                        color = color
                    )
                }
            }
        }
    }
}

// ==================== 图标绘制 ====================

private fun DrawScope.drawHomeIcon(color: Color) {
    val w = size.width; val h = size.height; val cx = w / 2f
    val roofTop = h * 0.1f; val roofBottom = h * 0.45f; val bodyBottom = h * 0.9f
    val halfWidth = w * 0.42f; val doorHalfWidth = w * 0.1f
    val path = Path(); path.moveTo(cx, roofTop); path.lineTo(cx + halfWidth, roofBottom)
    path.lineTo(cx - halfWidth, roofBottom); path.close(); drawPath(path, color)
    drawRect(color = color, topLeft = Offset(cx - halfWidth * 0.8f, roofBottom), size = Size(halfWidth * 1.6f, bodyBottom - roofBottom))
    drawRect(color = Color.White, topLeft = Offset(cx - doorHalfWidth, roofBottom + (bodyBottom - roofBottom) * 0.35f), size = Size(doorHalfWidth * 2f, (bodyBottom - roofBottom) * 0.65f))
}

private fun DrawScope.drawProjectIcon(color: Color) {
    val p = size.width * 0.15f; val g = size.width * 0.06f
    val cellW = (size.width - p * 2 - g) / 2f; val cellH = (size.height - p * 2 - g) / 2f
    for (r in 0..1) for (c in 0..1) {
        drawRoundRect(color = color, topLeft = Offset(p + c * (cellW + g), p + r * (cellH + g)), size = Size(cellW, cellH), cornerRadius = CornerRadius(2.dp.toPx()))
    }
}

private fun DrawScope.drawSquareIcon(color: Color) {
    val p = size.width * 0.1f; val w = size.width - p * 2; val h = (size.height - p * 2) * 0.82f
    drawRoundRect(color = color, topLeft = Offset(p, p), size = Size(w, h), cornerRadius = CornerRadius(4.dp.toPx()))
    val tail = Path(); tail.moveTo(p + w * 0.35f, p + h); tail.lineTo(p + w * 0.25f, p + h + size.height * 0.18f)
    tail.lineTo(p + w * 0.5f, p + h); tail.close(); drawPath(tail, color)
    val lc = Color.White; val ly1 = p + h * 0.3f; val ly2 = p + h * 0.55f; val ls = p + w * 0.25f; val le = p + w * 0.75f
    drawLine(lc, Offset(ls, ly1), Offset(le, ly1), strokeWidth = 1.5.dp.toPx(), cap = StrokeCap.Round)
    drawLine(lc, Offset(ls, ly2), Offset(p + w * 0.6f, ly2), strokeWidth = 1.5.dp.toPx(), cap = StrokeCap.Round)
}

private fun DrawScope.drawNavigationIcon(color: Color) {
    val cx = size.width / 2f; val cy = size.height / 2f; val r = size.width * 0.4f
    drawCircle(color = color, radius = r, center = Offset(cx, cy), style = Stroke(width = 1.8.dp.toPx()))
    val pl = r * 0.7f; val gs = r * 0.15f
    drawLine(color, Offset(cx, cy - gs), Offset(cx, cy - pl), strokeWidth = 2.dp.toPx(), cap = StrokeCap.Round)
    drawLine(color, Offset(cx, cy + gs), Offset(cx, cy + pl), strokeWidth = 2.dp.toPx(), cap = StrokeCap.Round)
    drawLine(color, Offset(cx - gs, cy), Offset(cx - pl, cy), strokeWidth = 2.dp.toPx(), cap = StrokeCap.Round)
    drawLine(color, Offset(cx + gs, cy), Offset(cx + pl, cy), strokeWidth = 2.dp.toPx(), cap = StrokeCap.Round)
    drawCircle(color = color, radius = 2.dp.toPx(), center = Offset(cx, cy))
}

private fun DrawScope.drawMineIcon(color: Color) {
    val cx = size.width / 2f; val headR = size.width * 0.18f; val headCy = size.height * 0.25f
    drawCircle(color = color, radius = headR, center = Offset(cx, headCy))
    val bt = size.height * 0.5f; val bb = size.height * 0.92f; val bhw = size.width * 0.35f
    val body = Path(); body.moveTo(cx - bhw, bb); body.lineTo(cx - bhw, bt + (bb - bt) * 0.2f)
    body.quadraticBezierTo(cx - bhw, bt, cx - headR * 0.8f, bt)
    body.lineTo(cx + headR * 0.8f, bt)
    body.quadraticBezierTo(cx + bhw, bt, cx + bhw, bt + (bb - bt) * 0.2f)
    body.lineTo(cx + bhw, bb); body.close(); drawPath(body, color)
}
