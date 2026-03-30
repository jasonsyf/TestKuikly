package com.syf.testkuikly.navigation

import com.syf.testkuikly.base.mvi.BaseViewModel
import com.syf.testkuikly.base.mvi.MviEffect
import com.syf.testkuikly.base.mvi.MviIntent
import com.syf.testkuikly.base.mvi.MviState
import com.syf.testkuikly.data.Article
import com.syf.testkuikly.data.WanRepository
import com.syf.testkuikly.data.NavigationItem
import com.syf.testkuikly.data.Tree

data class NavigationState(
    val trees: List<Tree> = emptyList(),
    val navItems: List<NavigationItem> = emptyList(),
    val selectedTreeIndex: Int = 0,
    val selectedChildIndex: Int = 0,
    val treeArticles: List<Article> = emptyList(),
    val treeArticlePage: Int = 0,
    val treeArticleHasMore: Boolean = true,
    val isLoadingTreeArticles: Boolean = false,
    val selectedNaviIndex: Int = 0,
    val treesLoaded: Boolean = false,
    val navLoaded: Boolean = false,
    val isLoading: Boolean = false
) : MviState {
    val currentTree: Tree? get() = trees.getOrNull(selectedTreeIndex)
    val currentNaviItem: NavigationItem? get() = navItems.getOrNull(selectedNaviIndex)
}

sealed class NavigationIntent : MviIntent {
    data object LoadData : NavigationIntent()
    data class SwitchTab(val index: Int) : NavigationIntent()
    data class SelectTree(val index: Int) : NavigationIntent()
    data class SelectChild(val index: Int) : NavigationIntent()
    data object LoadMoreTreeArticles : NavigationIntent()
    data class SelectNavi(val index: Int) : NavigationIntent()
}

sealed class NavigationEffect : MviEffect {
    data object ShowError : NavigationEffect()
}

class NavigationViewModel : BaseViewModel<NavigationState, NavigationIntent, NavigationEffect>(NavigationState()) {

    private val repository = WanRepository()

    override fun handleIntent(intent: NavigationIntent) {
        when (intent) {
            is NavigationIntent.LoadData -> loadData()
            is NavigationIntent.SwitchTab -> switchTab(intent.index)
            is NavigationIntent.SelectTree -> selectTree(intent.index)
            is NavigationIntent.SelectChild -> selectChild(intent.index)
            is NavigationIntent.LoadMoreTreeArticles -> loadMoreTreeArticles()
            is NavigationIntent.SelectNavi -> selectNavi(intent.index)
        }
    }

    private fun loadData() {
        loadTrees()
    }

    private fun switchTab(index: Int) {
        when (index) {
            0 -> { if (!currentState.treesLoaded) loadTrees() }
            1 -> { if (!currentState.navLoaded) loadNav() }
        }
    }

    private fun loadTrees() {
        reduce { copy(isLoading = true) }
        launch {
            try {
                repository.getTreeData().collect { treeData ->
                    reduce {
                        copy(trees = treeData, treesLoaded = true, isLoading = false)
                    }
                    if (treeData.isNotEmpty() && treeData[0].children.isNotEmpty() && currentState.selectedTreeIndex == 0 && currentState.selectedChildIndex == 0) {
                        loadTreeArticles(cid = treeData[0].children[0].id, reset = true)
                    }
                }
            } catch (_: Exception) {
                reduce { copy(isLoading = false) }
                sendEffect(NavigationEffect.ShowError)
            }
        }
    }

    private fun loadNav() {
        reduce { copy(isLoading = true) }
        launch {
            try {
                repository.getNavigationData().collect { result ->
                    reduce {
                        copy(navItems = result, navLoaded = true, isLoading = false)
                    }
                }
            } catch (_: Exception) {
                reduce { copy(isLoading = false) }
                sendEffect(NavigationEffect.ShowError)
            }
        }
    }

    private fun selectTree(index: Int) {
        val tree = currentState.trees.getOrNull(index) ?: return
        reduce {
            copy(
                selectedTreeIndex = index,
                selectedChildIndex = 0
            )
        }
        if (tree.children.isNotEmpty()) {
            loadTreeArticles(cid = tree.children[0].id, reset = true)
        }
    }

    private fun selectChild(index: Int) {
        val child = currentState.currentTree?.children?.getOrNull(index) ?: return
        reduce { copy(selectedChildIndex = index) }
        loadTreeArticles(cid = child.id, reset = true)
    }

    private fun loadMoreTreeArticles() {
        if (currentState.isLoadingTreeArticles || !currentState.treeArticleHasMore) return
        val child = currentState.currentTree?.children?.getOrNull(currentState.selectedChildIndex) ?: return
        loadTreeArticles(cid = child.id, reset = false)
    }

    private fun loadTreeArticles(cid: Int, reset: Boolean) {
        val page = if (reset) 0 else currentState.treeArticlePage + 1
        reduce {
            copy(isLoadingTreeArticles = true)
        }
        launch {
            try {
                repository.getTreeArticles(page, cid).collect { result ->
                    reduce {
                        copy(
                            treeArticles = if (reset) result.datas else treeArticles + result.datas,
                            treeArticlePage = page,
                            treeArticleHasMore = !result.over,
                            isLoadingTreeArticles = false
                        )
                    }
                }
            } catch (_: Exception) {
                reduce { copy(isLoadingTreeArticles = false) }
                sendEffect(NavigationEffect.ShowError)
            }
        }
    }

    private fun selectNavi(index: Int) {
        reduce { copy(selectedNaviIndex = index) }
    }
}
