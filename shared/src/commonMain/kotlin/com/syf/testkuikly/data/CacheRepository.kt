package com.syf.testkuikly.data

/**
 * 跨平台缓存接口，各平台通过 expect/actual 实现
 */
interface CacheRepository {
    // Banner
    suspend fun getCachedBanners(): List<BannerItem>
    suspend fun saveBanners(banners: List<BannerItem>)

    // 首页文章
    suspend fun getCachedHomeArticles(): List<Article>
    suspend fun saveHomeArticles(articles: List<Article>)

    // 项目标签
    suspend fun getCachedProjectTags(): List<ProjectTag>
    suspend fun saveProjectTags(tags: List<ProjectTag>)

    // 项目文章
    suspend fun getCachedProjectArticles(cid: Int): List<Article>
    suspend fun saveProjectArticles(cid: Int, articles: List<Article>)

    // 体系分类
    suspend fun getCachedTrees(): List<Tree>
    suspend fun saveTrees(trees: List<Tree>)

    // 体系文章
    suspend fun getCachedTreeArticles(cid: Int): List<Article>
    suspend fun saveTreeArticles(cid: Int, articles: List<Article>)

    // 导航
    suspend fun getCachedNavigations(): List<NavigationItem>
    suspend fun saveNavigations(items: List<NavigationItem>)

    fun clearAll()
}

expect fun createCacheRepository(): CacheRepository
