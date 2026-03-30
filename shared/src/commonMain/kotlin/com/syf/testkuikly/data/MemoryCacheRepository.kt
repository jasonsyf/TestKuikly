package com.syf.testkuikly.data

/**
 * 内存缓存实现（无 SQLDelight 依赖）
 * JS 和鸿蒙共用。数据仅在应用运行期间有效，重启后清空。
 */
internal class MemoryCacheRepository : CacheRepository {

    private var cachedBanners: List<BannerItem> = emptyList()
    private var cachedHomeArticles: List<Article> = emptyList()
    private var cachedProjectTags: List<ProjectTag> = emptyList()
    private val cachedProjectArticles = mutableMapOf<Int, List<Article>>()
    private var cachedTrees: List<Tree> = emptyList()
    private val cachedTreeArticles = mutableMapOf<Int, List<Article>>()
    private var cachedNavigations: List<NavigationItem> = emptyList()

    override suspend fun getCachedBanners(): List<BannerItem> = cachedBanners

    override suspend fun saveBanners(banners: List<BannerItem>) {
        cachedBanners = banners
    }

    override suspend fun getCachedHomeArticles(): List<Article> = cachedHomeArticles

    override suspend fun saveHomeArticles(articles: List<Article>) {
        cachedHomeArticles = articles
    }

    override suspend fun getCachedProjectTags(): List<ProjectTag> = cachedProjectTags

    override suspend fun saveProjectTags(tags: List<ProjectTag>) {
        cachedProjectTags = tags
    }

    override suspend fun getCachedProjectArticles(cid: Int): List<Article> =
        cachedProjectArticles[cid] ?: emptyList()

    override suspend fun saveProjectArticles(cid: Int, articles: List<Article>) {
        cachedProjectArticles[cid] = articles
    }

    override suspend fun getCachedTrees(): List<Tree> = cachedTrees

    override suspend fun saveTrees(trees: List<Tree>) {
        cachedTrees = trees
    }

    override suspend fun getCachedTreeArticles(cid: Int): List<Article> =
        cachedTreeArticles[cid] ?: emptyList()

    override suspend fun saveTreeArticles(cid: Int, articles: List<Article>) {
        cachedTreeArticles[cid] = articles
    }

    override suspend fun getCachedNavigations(): List<NavigationItem> = cachedNavigations

    override suspend fun saveNavigations(items: List<NavigationItem>) {
        cachedNavigations = items
    }

    override fun clearAll() {
        cachedBanners = emptyList()
        cachedHomeArticles = emptyList()
        cachedProjectTags = emptyList()
        cachedProjectArticles.clear()
        cachedTrees = emptyList()
        cachedTreeArticles.clear()
        cachedNavigations = emptyList()
    }
}
