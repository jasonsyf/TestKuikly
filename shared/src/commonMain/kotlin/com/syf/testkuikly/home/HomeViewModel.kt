package com.syf.testkuikly.home

import com.syf.testkuikly.base.mvi.BaseViewModel
import com.syf.testkuikly.base.mvi.MviEffect
import com.syf.testkuikly.base.mvi.MviIntent
import com.syf.testkuikly.base.mvi.MviState
import com.syf.testkuikly.data.Article
import com.syf.testkuikly.data.BannerItem
import com.syf.testkuikly.data.WanRepository


data class HomeState(
    val banners: List<BannerItem> = emptyList(),
    val topArticles: List<Article> = emptyList(),
    val articles: List<Article> = emptyList(),
    val currentPage: Int = 0,
    val isRefreshing: Boolean = false,
    val isLoadingMore: Boolean = false,
    val hasMore: Boolean = true
) : MviState {
    val allArticles: List<Article> get() = topArticles + articles.filter { !it.top }
}

sealed class HomeIntent : MviIntent {
    data object LoadInitial : HomeIntent()
    data object Refresh : HomeIntent()
    data object LoadMore : HomeIntent()
}

sealed class HomeEffect : MviEffect {
    data object ShowError : HomeEffect()
}

class HomeViewModel : BaseViewModel<HomeState, HomeIntent, HomeEffect>(HomeState()) {

    private val repository = WanRepository()

    override fun handleIntent(intent: HomeIntent) {
        when (intent) {
            is HomeIntent.LoadInitial -> loadInitial()
            is HomeIntent.Refresh -> refresh()
            is HomeIntent.LoadMore -> loadMore()
        }
    }

    private fun loadInitial() {
        reduce { copy(isRefreshing = true) }
        launch {
            try {
                repository.getBanners().collect { banners ->
                    reduce { copy(banners = banners) }
                }
            } catch (_: Exception) {}
        }
        launch {
            try {
                val topArticles = repository.getTopArticles()
                reduce { copy(topArticles = topArticles) }
            } catch (_: Exception) {}
        }
        launch {
            try {
                repository.getHomeArticles(0).collect { articles ->
                    reduce {
                        copy(
                            articles = articles.datas,
                            currentPage = 0,
                            isRefreshing = false,
                            hasMore = !articles.over
                        )
                    }
                }
            } catch (e: Exception) {
                reduce { copy(isRefreshing = false) }
                sendEffect(HomeEffect.ShowError)
            }
        }
    }

    private fun refresh() = loadInitial()

    private fun loadMore() {
        if (currentState.isLoadingMore || !currentState.hasMore) return
        reduce { copy(isLoadingMore = true) }
        launch {
            try {
                val nextPage = currentState.currentPage + 1
                repository.getHomeArticles(nextPage).collect { result ->
                    reduce {
                        copy(
                            articles = articles + result.datas,
                            currentPage = nextPage,
                            isLoadingMore = false,
                            hasMore = !result.over
                        )
                    }
                }
            } catch (e: Exception) {
                reduce { copy(isLoadingMore = false) }
                sendEffect(HomeEffect.ShowError)
            }
        }
    }
}
