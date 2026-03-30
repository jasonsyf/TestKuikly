package com.syf.testkuikly.data

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.http.Parameters
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement

private val json = Json { ignoreUnknownKeys = true }

internal fun createWanApiServiceImpl(client: HttpClient, baseUrl: String): WanApiService {
    return object : WanApiService {

        // ==================== 首页 ====================

        override suspend fun getHomeArticles(page: Int): ApiBaseResponse<ArticleListData> =
            client.get("$baseUrl/article/list/$page/json").body()

        override suspend fun getBanners(): ApiBaseResponse<List<BannerItem>> =
            client.get("$baseUrl/banner/json").body()

        override suspend fun getTopArticles(): ApiBaseResponse<List<Article>> =
            client.get("$baseUrl/article/top/json").body()

        override suspend fun getHotKeys(): ApiBaseResponse<List<HotKey>> =
            client.get("$baseUrl/hotkey/json").body()

        override suspend fun searchArticles(page: Int, keyword: String): ApiBaseResponse<ArticleListData> =
            client.post("$baseUrl/article/query/$page/json") {
                url { parameters.append("k", keyword) }
            }.body()

        // ==================== 体系 ====================

        override suspend fun getTreeData(): ApiBaseResponse<List<Tree>> =
            client.get("$baseUrl/tree/json").body()

        override suspend fun getTreeArticles(page: Int, cid: Int): ApiBaseResponse<ArticleListData> =
            client.get("$baseUrl/article/list/$page/json") {
                url { parameters.append("cid", cid.toString()) }
            }.body()

        // ==================== 导航 ====================

        override suspend fun getNavigationData(): ApiBaseResponse<List<NavigationItem>> =
            client.get("$baseUrl/navi/json").body()

        // ==================== 项目 ====================

        override suspend fun getProjectTags(): ApiBaseResponse<List<ProjectTag>> =
            client.get("$baseUrl/project/tree/json").body()

        override suspend fun getProjectList(page: Int, cid: Int): ApiBaseResponse<ArticleListData> =
            client.get("$baseUrl/project/list/$page/json") {
                url { parameters.append("cid", cid.toString()) }
            }.body()

        override suspend fun getLatestProjects(page: Int): ApiBaseResponse<ArticleListData> =
            client.get("$baseUrl/article/listproject/$page/json").body()

        // ==================== 公众号 ====================

        override suspend fun getWxChapters(): ApiBaseResponse<List<WxChapter>> =
            client.get("$baseUrl/wxarticle/chapters/json").body()

        override suspend fun getWxArticles(id: Int, page: Int): ApiBaseResponse<ArticleListData> =
            client.get("$baseUrl/wxarticle/list/$id/$page/json").body()

        // ==================== 广场 ====================

        override suspend fun getSquareList(page: Int): ApiBaseResponse<ArticleListData> =
            client.get("$baseUrl/user_article/list/$page/json").body()

        override suspend fun getWendaList(page: Int): ApiBaseResponse<WendaListData> =
            client.get("$baseUrl/wenda/list/$page/json").body()

        // ==================== 用户 ====================

        override suspend fun login(username: String, password: String): ApiBaseResponse<JsonElement> =
            client.submitForm(
                url = "$baseUrl/user/login",
                formParameters = io.ktor.http.Parameters.build {
                    append("username", username)
                    append("password", password)
                }
            ).body()

        override suspend fun register(username: String, password: String, repassword: String): ApiBaseResponse<JsonElement> =
            client.submitForm(
                url = "$baseUrl/user/register",
                formParameters = io.ktor.http.Parameters.build {
                    append("username", username)
                    append("password", password)
                    append("repassword", repassword)
                }
            ).body()

        override suspend fun logout(): ApiBaseResponse<JsonElement> =
            client.get("$baseUrl/user/logout/json").body()

        override suspend fun getUserInfo(): ApiBaseResponse<UserInfoData> =
            client.get("$baseUrl/user/lg/userinfo/json").body()

        override suspend fun getCoinRank(page: Int): ApiBaseResponse<CoinRankListData> =
            client.get("$baseUrl/coin/rank/$page/json").body()

        override suspend fun getCollectList(page: Int): ApiBaseResponse<ArticleListData> =
            client.get("$baseUrl/lg/collect/list/$page/json").body()

        override suspend fun collectArticle(articleId: Int): ApiBaseResponse<JsonElement> =
            client.post("$baseUrl/lg/collect/$articleId/json").body()

        override suspend fun uncollectArticle(articleId: Int): ApiBaseResponse<JsonElement> =
            client.post("$baseUrl/lg/uncollect_originId/$articleId/json").body()

        // ==================== 工具 ====================

        override suspend fun getFriendList(): ApiBaseResponse<List<Friend>> =
            client.get("$baseUrl/friend/json").body()
    }
}
