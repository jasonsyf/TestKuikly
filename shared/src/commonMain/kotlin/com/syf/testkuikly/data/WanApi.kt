package com.syf.testkuikly.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class ApiBaseResponse<T>(
    @SerialName("errorCode") val errorCode: Int = -1,
    @SerialName("errorMsg") val errorMsg: String = "",
    @SerialName("data") val data: T? = null
) {
    val isSuccess: Boolean get() = errorCode == 0
}

/**
 * 玩Android API 接口定义
 */
interface WanApiService {

    // ==================== 首页 ====================

    suspend fun getHomeArticles(page: Int): ApiBaseResponse<ArticleListData>

    suspend fun getBanners(): ApiBaseResponse<List<BannerItem>>

    suspend fun getTopArticles(): ApiBaseResponse<List<Article>>

    suspend fun getHotKeys(): ApiBaseResponse<List<HotKey>>

    suspend fun searchArticles(page: Int, keyword: String): ApiBaseResponse<ArticleListData>

    // ==================== 体系 ====================

    suspend fun getTreeData(): ApiBaseResponse<List<Tree>>

    suspend fun getTreeArticles(page: Int, cid: Int): ApiBaseResponse<ArticleListData>

    // ==================== 导航 ====================

    suspend fun getNavigationData(): ApiBaseResponse<List<NavigationItem>>

    // ==================== 项目 ====================

    suspend fun getProjectTags(): ApiBaseResponse<List<ProjectTag>>

    suspend fun getProjectList(page: Int, cid: Int): ApiBaseResponse<ArticleListData>

    suspend fun getLatestProjects(page: Int): ApiBaseResponse<ArticleListData>

    // ==================== 公众号 ====================

    suspend fun getWxChapters(): ApiBaseResponse<List<WxChapter>>

    suspend fun getWxArticles(id: Int, page: Int): ApiBaseResponse<ArticleListData>

    // ==================== 广场 ====================

    suspend fun getSquareList(page: Int): ApiBaseResponse<ArticleListData>

    suspend fun getWendaList(page: Int): ApiBaseResponse<WendaListData>

    // ==================== 用户 ====================

    suspend fun login(username: String, password: String): ApiBaseResponse<JsonElement>

    suspend fun register(username: String, password: String, repassword: String): ApiBaseResponse<JsonElement>

    suspend fun logout(): ApiBaseResponse<JsonElement>

    suspend fun getUserInfo(): ApiBaseResponse<UserInfoData>

    suspend fun getCoinRank(page: Int): ApiBaseResponse<CoinRankListData>

    suspend fun getCollectList(page: Int): ApiBaseResponse<ArticleListData>

    suspend fun collectArticle(articleId: Int): ApiBaseResponse<JsonElement>

    suspend fun uncollectArticle(articleId: Int): ApiBaseResponse<JsonElement>

    // ==================== 工具 ====================

    suspend fun getFriendList(): ApiBaseResponse<List<Friend>>
}

// ==================== API 响应 Data 类型 ====================

@Serializable
data class ArticleListData(
    @SerialName("curPage") val curPage: Int = 0,
    @SerialName("datas") val datas: List<Article> = emptyList(),
    @SerialName("offset") val offset: Int = 0,
    @SerialName("over") val over: Boolean = true,
    @SerialName("pageCount") val pageCount: Int = 0,
    @SerialName("size") val size: Int = 0,
    @SerialName("total") val total: Int = 0
) {
    fun toArticleList() = ArticleList(curPage, datas, offset, over, pageCount, size, total)
}

@Serializable
data class WendaListData(
    @SerialName("curPage") val curPage: Int = 0,
    @SerialName("datas") val datas: List<WendaItem> = emptyList(),
    @SerialName("offset") val offset: Int = 0,
    @SerialName("over") val over: Boolean = true,
    @SerialName("pageCount") val pageCount: Int = 0,
    @SerialName("size") val size: Int = 0,
    @SerialName("total") val total: Int = 0
) {
    fun toWendaList() = WendaList(curPage, datas, offset, over, pageCount, size, total)
}

@Serializable
data class CoinRankListData(
    @SerialName("curPage") val curPage: Int = 0,
    @SerialName("datas") val datas: List<CoinRank> = emptyList(),
    @SerialName("offset") val offset: Int = 0,
    @SerialName("over") val over: Boolean = true,
    @SerialName("pageCount") val pageCount: Int = 0,
    @SerialName("size") val size: Int = 0,
    @SerialName("total") val total: Int = 0
) {
    fun toCoinRankList() = CoinRankList(curPage, datas, offset, over, pageCount, size, total)
}

@Serializable
data class UserInfoData(
    @SerialName("coinInfo") val coinInfo: CoinInfo? = null,
    @SerialName("userInfo") val userProfile: UserProfile? = null
) {
    fun toUserInfo() = UserInfo(
        coinInfo = coinInfo ?: CoinInfo(0, 0, 0, 0, ""),
        userInfo = userProfile ?: UserProfile(0, "", "", "", "")
    )
}
