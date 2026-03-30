package com.syf.testkuikly.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch

/**
 * 缓存优先 + 后台刷新的 Repository
 * 策略：先立即返回缓存数据，同时后台请求网络，网络成功后更新缓存并推送最新数据。
 */
class WanRepository {

    private val api = KtorfitInstance.api
    private val cache: CacheRepository by lazy { createCacheRepository() }

    // ==================== 首页 ====================

    fun getBanners(): Flow<List<BannerItem>> = cacheFirst(
        cached = { cache.getCachedBanners() },
        network = { api.getBanners().data ?: emptyList() },
        save = { cache.saveBanners(it) },
        isEmpty = { it.isEmpty() }
    )

    suspend fun getTopArticles(): List<Article> {
        return try {
            api.getTopArticles().data ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun getHomeArticles(page: Int): Flow<ArticleList> = if (page == 0) {
        cacheFirstNullable(
            cached = {
                val cached = cache.getCachedHomeArticles()
                if (cached.isNotEmpty()) ArticleList(0, cached, 0, true, 1, cached.size, cached.size) else null
            },
            network = {
                val result = api.getHomeArticles(page)
                result.data?.toArticleList() ?: ArticleList(0, emptyList(), 0, true, 0, 0, 0)
            },
            save = { cache.saveHomeArticles(it.datas) }
        )
    } else {
        networkOnly { api.getHomeArticles(page).data?.toArticleList() ?: ArticleList(0, emptyList(), 0, true, 0, 0, 0) }
    }

    // ==================== 项目 ====================

    fun getProjectTags(): Flow<List<ProjectTag>> = cacheFirst(
        cached = { cache.getCachedProjectTags() },
        network = { api.getProjectTags().data ?: emptyList() },
        save = { cache.saveProjectTags(it) },
        isEmpty = { it.isEmpty() }
    )

    fun getProjectList(page: Int, cid: Int): Flow<ArticleList> = if (page == 1) {
        cacheFirstNullable(
            cached = {
                val cached = cache.getCachedProjectArticles(cid)
                if (cached.isNotEmpty()) ArticleList(1, cached, 0, true, 1, cached.size, cached.size) else null
            },
            network = {
                val result = api.getProjectList(page, cid)
                result.data?.toArticleList() ?: ArticleList(0, emptyList(), 0, true, 0, 0, 0)
            },
            save = { cache.saveProjectArticles(cid, it.datas) }
        )
    } else {
        networkOnly { api.getProjectList(page, cid).data?.toArticleList() ?: ArticleList(0, emptyList(), 0, true, 0, 0, 0) }
    }

    // ==================== 体系 ====================

    fun getTreeData(): Flow<List<Tree>> = cacheFirst(
        cached = { cache.getCachedTrees() },
        network = { api.getTreeData().data ?: emptyList() },
        save = { cache.saveTrees(it) },
        isEmpty = { it.isEmpty() }
    )

    fun getTreeArticles(page: Int, cid: Int): Flow<ArticleList> = if (page == 0) {
        cacheFirstNullable(
            cached = {
                val cached = cache.getCachedTreeArticles(cid)
                if (cached.isNotEmpty()) ArticleList(0, cached, 0, true, 1, cached.size, cached.size) else null
            },
            network = {
                val result = api.getTreeArticles(page, cid)
                result.data?.toArticleList() ?: ArticleList(0, emptyList(), 0, true, 0, 0, 0)
            },
            save = { cache.saveTreeArticles(cid, it.datas) }
        )
    } else {
        networkOnly { api.getTreeArticles(page, cid).data?.toArticleList() ?: ArticleList(0, emptyList(), 0, true, 0, 0, 0) }
    }

    // ==================== 导航 ====================

    fun getNavigationData(): Flow<List<NavigationItem>> = cacheFirst(
        cached = { cache.getCachedNavigations() },
        network = { api.getNavigationData().data ?: emptyList() },
        save = { cache.saveNavigations(it) },
        isEmpty = { it.isEmpty() }
    )

    // ==================== 通用 ====================

    private fun <T> cacheFirstNullable(
        cached: suspend () -> T?,
        network: suspend () -> T,
        save: suspend (T) -> Unit
    ): Flow<T> = channelFlow {
        // 1. 立即发射缓存（如果有）
        try {
            cached()?.let { send(it) }
        } catch (_: Exception) {}

        // 2. 后台请求网络
        try {
            val networkData = network()
            safeSave { save(networkData) }
            send(networkData)
        } catch (_: Exception) {}
    }

    private fun <T> cacheFirst(
        cached: suspend () -> T,
        network: suspend () -> T,
        save: suspend (T) -> Unit,
        isEmpty: (T) -> Boolean
    ): Flow<T> = channelFlow {
        // 1. 立即发射缓存
        try {
            val cachedData = cached()
            if (!isEmpty(cachedData)) send(cachedData)
        } catch (_: Exception) {}

        // 2. 后台请求网络
        try {
            val networkData = network()
            if (!isEmpty(networkData)) {
                safeSave { save(networkData) }
                send(networkData)
            }
        } catch (_: Exception) {}
    }

    private fun <T> networkOnly(network: suspend () -> T): Flow<T> = channelFlow {
        try {
            send(network())
        } catch (e: Exception) {
            close(e)
        }
    }

    private suspend fun safeSave(block: suspend () -> Unit) {
        try { block() } catch (_: Exception) {}
    }
}
