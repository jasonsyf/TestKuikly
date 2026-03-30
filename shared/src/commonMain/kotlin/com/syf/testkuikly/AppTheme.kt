package com.syf.testkuikly

import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.compose.ui.text.TextStyle
import com.tencent.kuikly.compose.ui.text.font.FontWeight
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.kuikly.compose.ui.unit.sp

/**
 * 全局设计 Token - Material 3 规范
 * 所有页面必须引用此文件中的常量，禁止硬编码颜色/字号/间距
 */

// ==================== 颜色体系 ====================

object AppColors {
    // Primary (橙红色主色)
    val Primary = Color(0xFFE64A19)
    val OnPrimary = Color.White
    val PrimaryContainer = Color(0xFFFFDBC5)
    val OnPrimaryContainer = Color(0xFF3E0E00)

    // Secondary
    val Secondary = Color(0xFF675A52)
    val OnSecondary = Color.White
    val SecondaryContainer = Color(0xFFF2DFF7)
    val OnSecondaryContainer = Color(0xFF211A14)

    // Tertiary (标签蓝色)
    val Tertiary = Color(0xFF4285F4)
    val OnTertiary = Color.White

    // Success (新标签绿色)
    val Success = Color(0xFF2E7D32)
    val OnSuccess = Color.White

    // Error
    val Error = Color(0xFFB3261E)
    val OnError = Color.White
    val ErrorContainer = Color(0xFFF9DEDC)
    val OnErrorContainer = Color(0xFF410E0B)

    // Surface
    val Surface = Color.White
    val OnSurface = Color(0xFF1C1B1F)
    val OnSurfaceVariant = Color(0xFF79747E)
    val SurfaceContainerLow = Color(0xFFF7F2FA)
    val SurfaceContainer = Color(0xFFF3EDF7)
    val SurfaceVariant = Color(0xFFF5F5F5)
    val SurfaceDim = Color(0xFFDED8E1)
    val SurfaceBright = Color.White

    // Outline / Divider
    val Outline = Color(0xFFE0E0E0)
    val OutlineVariant = Color(0xFFEEEEEE)

    // Bottom Nav
    val BottomNavInactive = Color(0xFF999999)
    val BottomNavRipple = Color(0x1AE64A19)

    // Utility
    val Black = Color.Black
    val Transparent = Color.Transparent
}

// ==================== 字号体系 (Material 3 Type Scale) ====================

object AppTypography {
    // 标题
    val headlineMedium = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold, lineHeight = 26.sp)
    val headlineSmall = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold, lineHeight = 24.sp)
    val titleLarge = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Medium, lineHeight = 22.sp)
    val titleMedium = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Medium, lineHeight = 20.sp)
    val titleSmall = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium, lineHeight = 19.sp)

    // 正文
    val bodyLarge = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Normal, lineHeight = 21.sp)
    val bodyMedium = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Normal, lineHeight = 18.sp)
    val bodySmall = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Normal, lineHeight = 16.sp)

    // 标签/辅助
    val labelLarge = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Medium, lineHeight = 16.sp)
    val labelMedium = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Normal, lineHeight = 15.sp)
    val labelSmall = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.Normal, lineHeight = 14.sp)
}

// ==================== 间距体系 ====================

object AppSpacing {
    val extraSmall = 4.dp
    val small = 8.dp
    val medium = 12.dp
    val large = 16.dp
    val extraLarge = 24.dp
    val huge = 32.dp
}

// ==================== 圆角体系 ====================

object AppRadius {
    val extraSmall = 4.dp
    val small = 6.dp
    val medium = 8.dp
    val large = 12.dp
    val extraLarge = 16.dp
    val pill = 20.dp
    val full = 28.dp
}

// ==================== 通用样式 ====================

object AppShape {
    val Card = AppRadius.large
    val Chip = AppRadius.pill
    val Button = AppRadius.full
    val Dialog = AppRadius.large
    val Image = AppRadius.medium
    val Tag = AppRadius.extraSmall
}

// ==================== 阴影/高度 ====================

object AppElevation {
    val none = 0.dp
    val level1 = 1.dp
    val level2 = 3.dp
    val level3 = 6.dp
}
