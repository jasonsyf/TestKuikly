package com.syf.testkuikly.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ========== 首页Banner ==========

@Serializable
data class BannerItem(
    @SerialName("id") val id: Int = 0,
    @SerialName("title") val title: String = "",
    @SerialName("url") val url: String = "",
    @SerialName("imagePath") val imagePath: String = "",
    @SerialName("desc") val desc: String = ""
)

// ========== 文章 ==========

@Serializable
data class Article(
    @SerialName("id") val id: Int = 0,
    @SerialName("title") val title: String = "",
    @SerialName("link") val link: String = "",
    @SerialName("author") val author: String = "",
    @SerialName("shareUser") val shareUser: String = "",
    @SerialName("envelopePic") val envelopePic: String = "",
    @SerialName("niceDate") val niceDate: String = "",
    @SerialName("chapterName") val chapterName: String = "",
    @SerialName("superChapterName") val superChapterName: String = "",
    @SerialName("projectId") val projectId: Int = 0,
    @SerialName("collect") val collect: Boolean = false,
    @SerialName("fresh") val fresh: Boolean = false,
    @SerialName("top") val top: Boolean = false,
    @SerialName("tags") val tags: List<TagItem> = emptyList(),
    @SerialName("desc") val desc: String = ""
) {
    val tagNames: List<String> get() = tags.mapNotNull { it.name.takeIf { n -> n.isNotEmpty() } }
    val decodedTitle: String get() = HtmlUtils.decode(title)
    val decodedDesc: String get() = HtmlUtils.decode(desc)
}

@Serializable
data class TagItem(
    @SerialName("name") val name: String = "",
    @SerialName("url") val url: String = ""
)

data class ArticleList(
    val curPage: Int,
    val datas: List<Article>,
    val offset: Int,
    val over: Boolean,
    val pageCount: Int,
    val size: Int,
    val total: Int
)

// ========== 体系/分类 ==========

@Serializable
data class Tree(
    @SerialName("id") val id: Int = 0,
    @SerialName("name") val name: String = "",
    @SerialName("children") val children: List<TreeChild> = emptyList()
)

@Serializable
data class TreeChild(
    @SerialName("id") val id: Int = 0,
    @SerialName("name") val name: String = ""
)

// ========== 导航 ==========

@Serializable
data class NavigationItem(
    @SerialName("id") val id: Int = 0,
    @SerialName("name") val name: String = "",
    @SerialName("articles") val articles: List<Article> = emptyList()
)

// ========== 项目 ==========

@Serializable
data class ProjectTag(
    @SerialName("id") val id: Int = 0,
    @SerialName("name") val name: String = ""
)

// ========== 公众号 ==========

@Serializable
data class WxChapter(
    @SerialName("id") val id: Int = 0,
    @SerialName("name") val name: String = ""
)

// ========== 搜索热词 ==========

@Serializable
data class HotKey(
    @SerialName("id") val id: Int = 0,
    @SerialName("name") val name: String = "",
    @SerialName("link") val link: String = ""
)

// ========== 常用网站 ==========

@Serializable
data class Friend(
    @SerialName("id") val id: Int = 0,
    @SerialName("name") val name: String = "",
    @SerialName("link") val link: String = "",
    @SerialName("icon") val icon: String = "",
    @SerialName("visible") val visible: Int = 0
)

// ========== 用户信息 ==========

@Serializable
data class CoinInfo(
    @SerialName("coinCount") val coinCount: Int = 0,
    @SerialName("level") val level: Int = 0,
    @SerialName("rank") val rank: Int = 0,
    @SerialName("userId") val userId: Int = 0,
    @SerialName("username") val username: String = ""
)

@Serializable
data class UserProfile(
    @SerialName("id") val id: Int = 0,
    @SerialName("username") val username: String = "",
    @SerialName("nickname") val nickname: String = "",
    @SerialName("email") val email: String = "",
    @SerialName("icon") val icon: String = ""
)

data class UserInfo(
    val coinInfo: CoinInfo,
    val userInfo: UserProfile
)

// ========== 积分排行 ==========

@Serializable
data class CoinRank(
    @SerialName("coinCount") val coinCount: Int = 0,
    @SerialName("level") val level: Int = 0,
    @SerialName("rank") val rank: Int = 0,
    @SerialName("userId") val userId: Int = 0,
    @SerialName("username") val username: String = ""
)

data class CoinRankList(
    val curPage: Int,
    val datas: List<CoinRank>,
    val offset: Int,
    val over: Boolean,
    val pageCount: Int,
    val size: Int,
    val total: Int
)

// ========== 问答 ==========

@Serializable
data class WendaItem(
    @SerialName("id") val id: Int = 0,
    @SerialName("title") val title: String = "",
    @SerialName("author") val author: String = "",
    @SerialName("niceDate") val niceDate: String = "",
    @SerialName("chapterName") val chapterName: String = "",
    @SerialName("link") val link: String = "",
    @SerialName("desc") val desc: String = "",
    @SerialName("collect") val collect: Boolean = false
) {
    val decodedTitle: String get() = HtmlUtils.decode(title)
    val decodedDesc: String get() = HtmlUtils.decode(desc)
}

data class WendaList(
    val curPage: Int,
    val datas: List<WendaItem>,
    val offset: Int,
    val over: Boolean,
    val pageCount: Int,
    val size: Int,
    val total: Int
)

// ========== 统一响应（保留给外部使用） ==========

data class ApiResponse<T>(
    val data: T?,
    val errorCode: Int,
    val errorMsg: String
) {
    val isSuccess: Boolean get() = errorCode == 0
}
