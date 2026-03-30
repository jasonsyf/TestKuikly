package com.syf.testkuikly.mine

import com.syf.testkuikly.base.mvi.BaseViewModel
import com.syf.testkuikly.base.mvi.MviEffect
import com.syf.testkuikly.base.mvi.MviIntent
import com.syf.testkuikly.base.mvi.MviState
import com.syf.testkuikly.data.KtorfitInstance
import com.syf.testkuikly.data.UserInfo


data class MineState(
    val isLoggedIn: Boolean = false,
    val userInfo: UserInfo? = null,
    val isLoading: Boolean = false
) : MviState

sealed class MineIntent : MviIntent {
    data object LoadUserInfo : MineIntent()
    data object Logout : MineIntent()
    data class Login(val username: String, val password: String) : MineIntent()
    data class Register(val username: String, val password: String) : MineIntent()
}

sealed class MineEffect : MviEffect {
    data class ShowToast(val message: String) : MineEffect()
    data class LoginResult(val success: Boolean, val message: String) : MineEffect()
    data class RegisterResult(val success: Boolean, val message: String) : MineEffect()
}

class MineViewModel : BaseViewModel<MineState, MineIntent, MineEffect>(MineState()) {

    private val api = KtorfitInstance.api

    override fun handleIntent(intent: MineIntent) {
        when (intent) {
            is MineIntent.LoadUserInfo -> loadUserInfo()
            is MineIntent.Logout -> logout()
            is MineIntent.Login -> login(intent.username, intent.password)
            is MineIntent.Register -> register(intent.username, intent.password)
        }
    }

    private fun loadUserInfo() {
        launch {
            try {
                val result = api.getUserInfo()
                val userInfo = result.data?.toUserInfo()
                reduce { copy(isLoggedIn = userInfo != null, userInfo = userInfo) }
            } catch (_: Exception) {
                reduce { copy(isLoggedIn = false) }
            }
        }
    }

    private fun logout() {
        launch {
            try { api.logout() } catch (_: Exception) {}
            reduce { copy(isLoggedIn = false, userInfo = null) }
            sendEffect(MineEffect.ShowToast("已退出登录"))
        }
    }

    private fun login(username: String, password: String) {
        reduce { copy(isLoading = true) }
        launch {
            try {
                println("[Login] start: username=$username")
                val result = api.login(username, password)
                println("[Login] result: errorCode=${result.errorCode}, errorMsg=${result.errorMsg}")
                if (result.isSuccess) {
                    val userInfo = api.getUserInfo().data?.toUserInfo()
                    reduce { copy(isLoggedIn = true, userInfo = userInfo, isLoading = false) }
                    sendEffect(MineEffect.LoginResult(true, "登录成功"))
                } else {
                    reduce { copy(isLoading = false) }
                    sendEffect(MineEffect.LoginResult(false, "用户名或密码错误"))
                }
            } catch (e: Exception) {
                println("[Login] error: ${e.message}")
                e.printStackTrace()
                reduce { copy(isLoading = false) }
                sendEffect(MineEffect.LoginResult(false, "网络错误: ${e.message}"))
            }
        }
    }

    private fun register(username: String, password: String) {
        reduce { copy(isLoading = true) }
        launch {
            try {
                println("[Register] start: username=$username")
                val result = api.register(username, password, password)
                println("[Register] result: errorCode=${result.errorCode}, errorMsg=${result.errorMsg}")
                if (result.isSuccess) {
                    reduce { copy(isLoading = false) }
                    sendEffect(MineEffect.RegisterResult(true, "注册成功，请登录"))
                } else {
                    reduce { copy(isLoading = false) }
                    sendEffect(MineEffect.RegisterResult(false, result.errorMsg.ifEmpty { "注册失败" }))
                }
            } catch (e: Exception) {
                println("[Register] error: ${e.message}")
                e.printStackTrace()
                reduce { copy(isLoading = false) }
                sendEffect(MineEffect.RegisterResult(false, "网络错误: ${e.message}"))
            }
        }
    }
}
