package com.syf.testkuikly

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.syf.testkuikly.base.Utils
import com.syf.testkuikly.mine.MineEffect
import com.syf.testkuikly.mine.MineIntent
import com.syf.testkuikly.mine.MineState
import com.syf.testkuikly.mine.MineViewModel
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
import com.tencent.kuikly.compose.foundation.shape.CircleShape
import com.tencent.kuikly.compose.foundation.shape.RoundedCornerShape
import com.tencent.kuikly.compose.material3.Text
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.compose.ui.text.TextStyle
import com.tencent.kuikly.compose.ui.text.font.FontWeight
import com.tencent.kuikly.compose.ui.text.style.TextAlign
import com.tencent.kuikly.compose.ui.unit.dp

@Composable
fun MinePage() {
    val viewModel = remember { MineViewModel() }
    var state by remember { mutableStateOf(viewModel.currentState) }
    var showLoginDialog by remember { mutableStateOf(false) }
    var isRegisterMode by remember { mutableStateOf(false) }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var loginMsg by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.sendIntent(MineIntent.LoadUserInfo)
        viewModel.viewState.collect { state = it }
    }

    LaunchedEffect(viewModel.effect) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is MineEffect.ShowToast -> Utils.currentBridgeModule().toast(effect.message)
                is MineEffect.LoginResult -> {
                    if (effect.success) { showLoginDialog = false; loginMsg = "" }
                    else { loginMsg = effect.message }
                }
                is MineEffect.RegisterResult -> {
                    if (effect.success) { isRegisterMode = false; loginMsg = effect.message }
                    else { loginMsg = effect.message }
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(AppColors.SurfaceVariant)) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            // 用户信息头部
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .background(
                            AppColors.Primary,
                            RoundedCornerShape(bottomStart = AppRadius.extraLarge, bottomEnd = AppRadius.extraLarge)
                        )
                        .padding(horizontal = AppSpacing.extraLarge, vertical = AppSpacing.large)
                        .let { mod ->
                            if (!state.isLoggedIn) mod.clickable {
                                showLoginDialog = true; isRegisterMode = false
                            } else mod
                        }
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxHeight()) {
                        Box(
                            modifier = Modifier.size(56.dp).background(AppColors.OnPrimary.copy(alpha = 0.25f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            val info = state.userInfo?.userInfo
                            Text(
                                text = if (state.isLoggedIn) (info?.nickname?.take(1) ?: info?.username?.take(1) ?: "?") else "?",
                                style = AppTypography.headlineSmall,
                                color = AppColors.OnPrimary
                            )
                        }
                        Spacer(modifier = Modifier.width(AppSpacing.medium))
                        Column {
                            val info = state.userInfo?.userInfo
                            Text(
                                text = if (state.isLoggedIn) {
                                    info?.nickname?.ifEmpty { info?.username ?: "用户" } ?: "用户"
                                } else "点击登录",
                                style = AppTypography.headlineSmall,
                                color = AppColors.OnPrimary
                            )
                            Spacer(modifier = Modifier.height(AppSpacing.extraSmall))
                            Text(
                                text = if (state.isLoggedIn) "ID: ${state.userInfo?.userInfo?.id ?: ""}" else "登录后查看更多功能",
                                style = AppTypography.labelLarge,
                                color = AppColors.OnPrimary.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }

            // 积分统计
            val info = state.userInfo
            if (state.isLoggedIn && info != null) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = AppSpacing.extraLarge)
                            .padding(top = AppSpacing.small)
                            .background(AppColors.Surface, RoundedCornerShape(AppRadius.large))
                            .padding(AppSpacing.large),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatItem("${info.coinInfo.coinCount}", "积分")
                        StatItem("${info.coinInfo.rank}", "排名")
                        StatItem("Lv.${info.coinInfo.level}", "等级")
                    }
                }
            }

            // 菜单组 1
            item {
                Spacer(modifier = Modifier.height(AppSpacing.small))
                MenuGroup(
                    items = listOf("我的收藏", "积分排行", "分享的文章", "待办清单"),
                    onClick = { }
                )
            }

            // 菜单组 2
            item {
                Spacer(modifier = Modifier.height(AppSpacing.small))
                MenuGroup(
                    items = if (state.isLoggedIn) listOf("系统设置", "关于我们", "退出登录") else listOf("系统设置", "关于我们", "登录 / 注册"),
                    onClick = { title ->
                        when (title) {
                            "退出登录" -> viewModel.sendIntent(MineIntent.Logout)
                            "登录 / 注册" -> { showLoginDialog = true; isRegisterMode = false }
                        }
                    }
                )
                Spacer(modifier = Modifier.height(AppSpacing.large))
            }
        }

        if (showLoginDialog) {
            LoginRegisterDialog(
                isRegisterMode = isRegisterMode,
                username = username,
                password = password,
                confirmPassword = confirmPassword,
                loginMsg = loginMsg,
                onModeSwitch = { isRegisterMode = it; loginMsg = "" },
                onUsernameChange = { username = it },
                onPasswordChange = { password = it },
                onConfirmPasswordChange = { confirmPassword = it },
                onDismiss = { showLoginDialog = false; loginMsg = "" },
                onSubmit = {
                    if (isRegisterMode) {
                        if (username.isBlank() || password.isBlank() || confirmPassword.isBlank()) { loginMsg = "请填写完整信息"; return@LoginRegisterDialog }
                        if (password != confirmPassword) { loginMsg = "两次密码不一致"; return@LoginRegisterDialog }
                        viewModel.sendIntent(MineIntent.Register(username, password))
                    } else {
                        if (username.isBlank() || password.isBlank()) { loginMsg = "请输入用户名和密码"; return@LoginRegisterDialog }
                        viewModel.sendIntent(MineIntent.Login(username, password))
                    }
                }
            )
        }
    }
}

@Composable
private fun StatItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, style = AppTypography.headlineSmall, color = AppColors.Primary)
        Spacer(modifier = Modifier.height(AppSpacing.extraSmall))
        Text(text = label, style = AppTypography.labelLarge, color = AppColors.OnSurfaceVariant)
    }
}

@Composable
private fun MenuGroup(items: List<String>, onClick: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AppSpacing.extraLarge)
            .background(AppColors.Surface, RoundedCornerShape(AppRadius.large))
    ) {
        items.forEachIndexed { index, title ->
            MenuItem(title) { onClick(title) }
            if (index < items.size - 1) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(0.5.dp)
                        .padding(start = 48.dp)
                        .background(AppColors.OutlineVariant)
                )
            }
        }
    }
}

@Composable
private fun MenuItem(title: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = AppSpacing.large, vertical = 14.dp)
    ) {
        Text(text = title, style = AppTypography.titleMedium, color = AppColors.OnSurface)
    }
}

@Composable
private fun LoginRegisterDialog(
    isRegisterMode: Boolean, username: String, password: String, confirmPassword: String,
    loginMsg: String, onModeSwitch: (Boolean) -> Unit, onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit, onConfirmPasswordChange: (String) -> Unit,
    onDismiss: () -> Unit, onSubmit: () -> Unit
) {
    Modal(modifier = Modifier.background(Color(0x80000000.toInt()))) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AppSpacing.extraLarge)
                    .background(AppColors.Surface, RoundedCornerShape(AppRadius.large))
                    .padding(AppSpacing.extraLarge)
            ) {
                Text(
                    text = if (isRegisterMode) "注册" else "登录",
                    style = AppTypography.headlineMedium,
                    color = AppColors.OnSurface,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(AppSpacing.medium))

                // 登录/注册 Tab 切换
                Row(
                    modifier = Modifier.fillMaxWidth().height(36.dp).background(AppColors.SurfaceVariant, RoundedCornerShape(AppRadius.full)).padding(2.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    TabPill("登录", !isRegisterMode) { onModeSwitch(false) }
                    TabPill("注册", isRegisterMode) { onModeSwitch(true) }
                }

                Spacer(modifier = Modifier.height(AppSpacing.medium))
                // 用户名
                Text("用户名", style = AppTypography.labelLarge, color = AppColors.OnSurfaceVariant)
                Spacer(modifier = Modifier.height(AppSpacing.extraSmall))
                TextField(
                    modifier = Modifier.fillMaxWidth().height(44.dp).background(AppColors.SurfaceVariant, RoundedCornerShape(AppRadius.medium)).padding(horizontal = AppSpacing.medium),
                    value = username, onValueChange = onUsernameChange, placeholder = "请输入用户名",
                    textStyle = AppTypography.bodyMedium.copy(color = AppColors.OnSurface),
                    placeholderColor = AppColors.OnSurfaceVariant
                )

                Spacer(modifier = Modifier.height(AppSpacing.small))
                Text("密码", style = AppTypography.labelLarge, color = AppColors.OnSurfaceVariant)
                Spacer(modifier = Modifier.height(AppSpacing.extraSmall))
                TextField(
                    modifier = Modifier.fillMaxWidth().height(44.dp).background(AppColors.SurfaceVariant, RoundedCornerShape(AppRadius.medium)).padding(horizontal = AppSpacing.medium),
                    value = password, onValueChange = onPasswordChange, placeholder = "请输入密码",
                    textStyle = AppTypography.bodyMedium.copy(color = AppColors.OnSurface),
                    placeholderColor = AppColors.OnSurfaceVariant
                )

                if (isRegisterMode) {
                    Spacer(modifier = Modifier.height(AppSpacing.small))
                    Text("确认密码", style = AppTypography.labelLarge, color = AppColors.OnSurfaceVariant)
                    Spacer(modifier = Modifier.height(AppSpacing.extraSmall))
                    TextField(
                        modifier = Modifier.fillMaxWidth().height(44.dp).background(AppColors.SurfaceVariant, RoundedCornerShape(AppRadius.medium)).padding(horizontal = AppSpacing.medium),
                        value = confirmPassword, onValueChange = onConfirmPasswordChange, placeholder = "请再次输入密码",
                        textStyle = AppTypography.bodyMedium.copy(color = AppColors.OnSurface),
                        placeholderColor = AppColors.OnSurfaceVariant
                    )
                }

                if (loginMsg.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(AppSpacing.small))
                    Text(text = loginMsg, style = AppTypography.labelLarge, color = AppColors.Error, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                }

                Spacer(modifier = Modifier.height(AppSpacing.medium))
                // 提交按钮
                Box(
                    modifier = Modifier.fillMaxWidth().height(48.dp).background(AppColors.Primary, RoundedCornerShape(AppRadius.full)).clickable { onSubmit() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = if (isRegisterMode) "注册" else "登录", style = AppTypography.titleLarge, color = AppColors.OnPrimary)
                }
                Spacer(modifier = Modifier.height(AppSpacing.small))
                Box(
                    modifier = Modifier.fillMaxWidth().height(44.dp).background(AppColors.SurfaceVariant, RoundedCornerShape(AppRadius.full)).clickable { onDismiss() },
                    contentAlignment = Alignment.Center
                ) {
                    Text("取消", style = AppTypography.titleMedium, color = AppColors.OnSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun TabPill(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier.height(32.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.weight(1f).height(32.dp)
                .background(if (isSelected) AppColors.Surface else AppColors.Transparent, RoundedCornerShape(AppRadius.extraLarge))
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = AppTypography.labelLarge,
                color = if (isSelected) AppColors.Primary else AppColors.OnSurfaceVariant,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}
