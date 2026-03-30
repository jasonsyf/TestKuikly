package com.syf.testkuikly.data

import com.syf.testkuikly.base.Utils
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement

private val json = Json { ignoreUnknownKeys = true }

/**
 * 鸿蒙 HTTP 实现（通过 Kuikly Bridge 调用 ArkTS @ohos/net.http）
 */
internal class OhosWanApiService(private val baseUrl: String) : WanApiService {

    private suspend fun httpExecute(method: String, url: String, body: String? = null): String {
        return Utils.currentBridgeModule().httpRequest(method, url, body)
    }

    private inline suspend fun <reified T> httpGet(url: String): ApiBaseResponse<T> {
        val responseBody = httpExecute("GET", url)
        return json.decodeFromString<ApiBaseResponse<T>>(responseBody)
    }

    private inline suspend fun <reified T> httpPost(url: String, body: String? = null): ApiBaseResponse<T> {
        val responseBody = httpExecute("POST", url, body)
        return json.decodeFromString<ApiBaseResponse<T>>(responseBody)
    }

    private inline suspend fun <reified T> httpPostForm(url: String, formBody: String): ApiBaseResponse<T> =
        httpPost(url, formBody)

    // ==================== 首页 ====================

    override suspend fun getHomeArticles(page: Int): ApiBaseResponse<ArticleListData> =
        httpGet("${baseUrl}article/list/$page/json")

    override suspend fun getBanners(): ApiBaseResponse<List<BannerItem>> =
        httpGet("${baseUrl}banner/json")

    override suspend fun getTopArticles(): ApiBaseResponse<List<Article>> =
        httpGet("${baseUrl}article/top/json")

    override suspend fun getHotKeys(): ApiBaseResponse<List<HotKey>> =
        httpGet("${baseUrl}hotkey/json")

    override suspend fun searchArticles(page: Int, keyword: String): ApiBaseResponse<ArticleListData> =
        httpPost("${baseUrl}article/query/$page/json", "k=$keyword")

    // ==================== 体系 ====================

    override suspend fun getTreeData(): ApiBaseResponse<List<Tree>> =
        httpGet("${baseUrl}tree/json")

    override suspend fun getTreeArticles(page: Int, cid: Int): ApiBaseResponse<ArticleListData> =
        httpGet("${baseUrl}article/list/$page/json?cid=$cid")

    // ==================== 导航 ====================

    override suspend fun getNavigationData(): ApiBaseResponse<List<NavigationItem>> =
        httpGet("${baseUrl}navi/json")

    // ==================== 项目 ====================

    override suspend fun getProjectTags(): ApiBaseResponse<List<ProjectTag>> =
        httpGet("${baseUrl}project/tree/json")

    override suspend fun getProjectList(page: Int, cid: Int): ApiBaseResponse<ArticleListData> =
        httpGet("${baseUrl}project/list/$page/json?cid=$cid")

    override suspend fun getLatestProjects(page: Int): ApiBaseResponse<ArticleListData> =
        httpGet("${baseUrl}article/listproject/$page/json")

    // ==================== 公众号 ====================

    override suspend fun getWxChapters(): ApiBaseResponse<List<WxChapter>> =
        httpGet("${baseUrl}wxarticle/chapters/json")

    override suspend fun getWxArticles(id: Int, page: Int): ApiBaseResponse<ArticleListData> =
        httpGet("${baseUrl}wxarticle/list/$id/$page/json")

    // ==================== 广场 ====================

    override suspend fun getSquareList(page: Int): ApiBaseResponse<ArticleListData> =
        httpGet("${baseUrl}user_article/list/$page/json")

    override suspend fun getWendaList(page: Int): ApiBaseResponse<WendaListData> =
        httpGet("${baseUrl}wenda/list/$page/json")

    // ==================== 用户 ====================

    override suspend fun login(username: String, password: String): ApiBaseResponse<JsonElement> =
        httpPostForm("${baseUrl}user/login", "username=$username&password=$password")

    override suspend fun register(username: String, password: String, repassword: String): ApiBaseResponse<JsonElement> =
        httpPostForm("${baseUrl}user/register", "username=$username&password=$password&repassword=$repassword")

    override suspend fun logout(): ApiBaseResponse<JsonElement> =
        httpGet("${baseUrl}user/logout/json")

    override suspend fun getUserInfo(): ApiBaseResponse<UserInfoData> =
        httpGet("${baseUrl}user/lg/userinfo/json")

    override suspend fun getCoinRank(page: Int): ApiBaseResponse<CoinRankListData> =
        httpGet("${baseUrl}coin/rank/$page/json")

    override suspend fun getCollectList(page: Int): ApiBaseResponse<ArticleListData> =
        httpGet("${baseUrl}lg/collect/list/$page/json")

    override suspend fun collectArticle(articleId: Int): ApiBaseResponse<JsonElement> =
        httpPost("${baseUrl}lg/collect/$articleId/json")

    override suspend fun uncollectArticle(articleId: Int): ApiBaseResponse<JsonElement> =
        httpPost("${baseUrl}lg/uncollect_originId/$articleId/json")

    // ==================== 工具 ====================

    override suspend fun getFriendList(): ApiBaseResponse<List<Friend>> =
        httpGet("${baseUrl}friend/json")
}
