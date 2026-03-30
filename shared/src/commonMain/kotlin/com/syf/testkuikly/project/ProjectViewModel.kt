package com.syf.testkuikly.project

import com.syf.testkuikly.base.mvi.BaseViewModel
import com.syf.testkuikly.base.mvi.MviIntent
import com.syf.testkuikly.base.mvi.MviState
import com.syf.testkuikly.data.Article
import com.syf.testkuikly.data.WanRepository
import com.syf.testkuikly.data.ProjectTag


data class ProjectState(
    val tags: List<ProjectTag> = emptyList(),
    val selectedTagId: Int = -1,
    val articles: List<Article> = emptyList(),
    val currentPage: Int = 1,
    val isLoadingMore: Boolean = false,
    val hasMore: Boolean = true
) : MviState

sealed class ProjectIntent : MviIntent {
    data object LoadTags : ProjectIntent()
    data class SelectTag(val tagId: Int) : ProjectIntent()
    data object LoadMore : ProjectIntent()
}

class ProjectViewModel : BaseViewModel<ProjectState, ProjectIntent, Nothing>(ProjectState()) {

    private val repository = WanRepository()

    override fun handleIntent(intent: ProjectIntent) {
        when (intent) {
            is ProjectIntent.LoadTags -> loadTags()
            is ProjectIntent.SelectTag -> selectTag(intent.tagId)
            is ProjectIntent.LoadMore -> loadMore()
        }
    }

    private fun loadTags() {
        launch {
            try {
                repository.getProjectTags().collect { tags ->
                    reduce { copy(tags = tags) }
                    if (tags.isNotEmpty() && currentState.selectedTagId <= 0) {
                        selectTag(tags.first().id)
                    }
                }
            } catch (_: Exception) {}
        }
    }

    private fun selectTag(tagId: Int) {
        reduce { copy(selectedTagId = tagId, currentPage = 1, articles = emptyList()) }
        loadArticles(1, tagId)
    }

    private fun loadMore() {
        if (currentState.isLoadingMore || !currentState.hasMore || currentState.selectedTagId <= 0) return
        reduce { copy(isLoadingMore = true) }
        loadArticles(currentState.currentPage + 1, currentState.selectedTagId)
    }

    private fun loadArticles(page: Int, cid: Int) {
        launch {
            try {
                repository.getProjectList(page, cid).collect { result ->
                    reduce {
                        copy(
                            articles = if (page == 1) result.datas else articles + result.datas,
                            currentPage = page,
                            isLoadingMore = false,
                            hasMore = !result.over
                        )
                    }
                }
            } catch (_: Exception) {
                reduce { copy(isLoadingMore = false) }
            }
        }
    }
}
