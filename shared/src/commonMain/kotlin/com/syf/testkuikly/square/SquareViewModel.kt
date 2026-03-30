package com.syf.testkuikly.square

import com.syf.testkuikly.base.mvi.BaseViewModel
import com.syf.testkuikly.base.mvi.MviIntent
import com.syf.testkuikly.base.mvi.MviState
import com.syf.testkuikly.data.Article
import com.syf.testkuikly.data.KtorfitInstance

import com.syf.testkuikly.data.WendaItem

data class SquareState(
    val wendaArticles: List<Article> = emptyList(),
    val squareArticles: List<Article> = emptyList(),
    val wendaPage: Int = 1,
    val squarePage: Int = 0,
    val wendaHasMore: Boolean = true,
    val squareHasMore: Boolean = true,
    val wendaLoadingMore: Boolean = false,
    val squareLoadingMore: Boolean = false
) : MviState

sealed class SquareIntent : MviIntent {
    data object LoadWenda : SquareIntent()
    data object LoadSquare : SquareIntent()
    data object LoadMoreWenda : SquareIntent()
    data object LoadMoreSquare : SquareIntent()
}

class SquareViewModel : BaseViewModel<SquareState, SquareIntent, Nothing>(SquareState()) {

    private val api = KtorfitInstance.api

    override fun handleIntent(intent: SquareIntent) {
        when (intent) {
            is SquareIntent.LoadWenda -> loadWenda()
            is SquareIntent.LoadSquare -> loadSquare()
            is SquareIntent.LoadMoreWenda -> loadMoreWenda()
            is SquareIntent.LoadMoreSquare -> loadMoreSquare()
        }
    }

    private fun loadWenda() {
        launch {
            try {
                val result = api.getWendaList(1)
                reduce {
                    copy(
                        wendaArticles = result.data?.datas?.map { it.toArticle() } ?: emptyList(),
                        wendaPage = result.data?.curPage ?: 1,
                        wendaHasMore = result.data?.over == false
                    )
                }
            } catch (_: Exception) {}
        }
    }

    private fun loadSquare() {
        launch {
            try {
                val result = api.getSquareList(0)
                reduce {
                    copy(
                        squareArticles = result.data?.datas ?: emptyList(),
                        squarePage = 0,
                        squareHasMore = result.data?.over == false
                    )
                }
            } catch (_: Exception) {}
        }
    }

    private fun loadMoreWenda() {
        if (currentState.wendaLoadingMore || !currentState.wendaHasMore) return
        reduce { copy(wendaLoadingMore = true) }
        launch {
            try {
                val page = currentState.wendaPage + 1
                val result = api.getWendaList(page)
                reduce {
                    copy(
                        wendaArticles = wendaArticles + (result.data?.datas?.map { it.toArticle() } ?: emptyList()),
                        wendaPage = result.data?.curPage ?: page,
                        wendaLoadingMore = false,
                        wendaHasMore = result.data?.over == false
                    )
                }
            } catch (_: Exception) {
                reduce { copy(wendaLoadingMore = false) }
            }
        }
    }

    private fun loadMoreSquare() {
        if (currentState.squareLoadingMore || !currentState.squareHasMore) return
        reduce { copy(squareLoadingMore = true) }
        launch {
            try {
                val page = currentState.squarePage + 1
                val result = api.getSquareList(page)
                reduce {
                    copy(
                        squareArticles = squareArticles + (result.data?.datas ?: emptyList()),
                        squarePage = page,
                        squareLoadingMore = false,
                        squareHasMore = result.data?.over == false
                    )
                }
            } catch (_: Exception) {
                reduce { copy(squareLoadingMore = false) }
            }
        }
    }
}

private fun WendaItem.toArticle() = Article(
    id = id, title = title, link = link, author = author,
    shareUser = "", envelopePic = "", niceDate = niceDate,
    chapterName = chapterName, superChapterName = "", projectId = 0,
    collect = collect, fresh = false, top = false, tags = emptyList(),
    desc = desc
)
