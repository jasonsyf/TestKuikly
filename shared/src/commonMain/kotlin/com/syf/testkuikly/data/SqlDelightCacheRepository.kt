package com.syf.testkuikly.data

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * 基于 SQLDelight 的缓存实现（Android/iOS）
 * 不直接映射复杂表结构，而是将列表数据序列化为 JSON 字符串存储。
 */
internal class SqlDelightCacheRepository : CacheRepository {

    private val database by lazy { WanDb(createDatabaseDriver()) }
    private val json by lazy { Json { ignoreUnknownKeys = true } }

    // ==================== Banner ====================

    override suspend fun getCachedBanners(): List<BannerItem> {
        val row = database.wanDbQueries.selectCacheByKey("banners").executeAsOneOrNull()
        return if (row != null) try { json.decodeFromString<List<BannerItem>>(row.json_data) } catch (_: Exception) { emptyList() }
        else emptyList()
    }

    override suspend fun saveBanners(banners: List<BannerItem>) {
        database.wanDbQueries.insertOrReplaceCache("banners", json.encodeToString(banners), currentTimeMillis())
    }

    // ==================== 首页文章 ====================

    override suspend fun getCachedHomeArticles(): List<Article> = getArticlesByKey("home")

    override suspend fun saveHomeArticles(articles: List<Article>) = saveArticlesByKey("home", articles)

    // ==================== 项目标签 ====================

    override suspend fun getCachedProjectTags(): List<ProjectTag> {
        val row = database.wanDbQueries.selectCacheByKey("project_tags").executeAsOneOrNull()
        return if (row != null) try { json.decodeFromString<List<ProjectTag>>(row.json_data) } catch (_: Exception) { emptyList() }
        else emptyList()
    }

    override suspend fun saveProjectTags(tags: List<ProjectTag>) {
        database.wanDbQueries.insertOrReplaceCache("project_tags", json.encodeToString(tags), currentTimeMillis())
    }

    // ==================== 项目文章 ====================

    override suspend fun getCachedProjectArticles(cid: Int): List<Article> = getArticlesByKey("project_$cid")

    override suspend fun saveProjectArticles(cid: Int, articles: List<Article>) = saveArticlesByKey("project_$cid", articles)

    // ==================== 体系分类 ====================

    override suspend fun getCachedTrees(): List<Tree> {
        val row = database.wanDbQueries.selectCacheByKey("trees").executeAsOneOrNull()
        return if (row != null) try { json.decodeFromString<List<Tree>>(row.json_data) } catch (_: Exception) { emptyList() }
        else emptyList()
    }

    override suspend fun saveTrees(trees: List<Tree>) {
        database.wanDbQueries.insertOrReplaceCache("trees", json.encodeToString(trees), currentTimeMillis())
    }

    // ==================== 体系文章 ====================

    override suspend fun getCachedTreeArticles(cid: Int): List<Article> = getArticlesByKey("tree_$cid")

    override suspend fun saveTreeArticles(cid: Int, articles: List<Article>) = saveArticlesByKey("tree_$cid", articles)

    // ==================== 导航 ====================

    override suspend fun getCachedNavigations(): List<NavigationItem> {
        val row = database.wanDbQueries.selectCacheByKey("navigations").executeAsOneOrNull()
        return if (row != null) try { json.decodeFromString<List<NavigationItem>>(row.json_data) } catch (_: Exception) { emptyList() }
        else emptyList()
    }

    override suspend fun saveNavigations(items: List<NavigationItem>) {
        database.wanDbQueries.insertOrReplaceCache("navigations", json.encodeToString(items), currentTimeMillis())
    }

    // ==================== 辅助 ====================

    private suspend fun getArticlesByKey(key: String): List<Article> {
        val row = database.wanDbQueries.selectCacheByKey(key).executeAsOneOrNull()
        return if (row != null) try { json.decodeFromString<List<Article>>(row.json_data) } catch (_: Exception) { emptyList() }
        else emptyList()
    }

    private suspend fun saveArticlesByKey(key: String, articles: List<Article>) {
        database.wanDbQueries.insertOrReplaceCache(key, json.encodeToString(articles), currentTimeMillis())
    }

    private fun currentTimeMillis(): Long {
        return try {
            kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
        } catch (_: Exception) {
            0L
        }
    }

    override fun clearAll() {
        database.wanDbQueries.deleteAllCache()
    }
}
