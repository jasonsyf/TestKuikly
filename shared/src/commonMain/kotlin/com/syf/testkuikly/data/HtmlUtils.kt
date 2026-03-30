package com.syf.testkuikly.data

/**
 * 跨平台 HTML 解码工具，处理 API 返回中的 HTML 实体和标签
 */
internal object HtmlUtils {

    private val htmlEntities = mapOf(
        // 核心
        "&amp;" to "&",
        "&lt;" to "<",
        "&gt;" to ">",
        "&quot;" to "\"",
        "&apos;" to "'",
        "&#39;" to "'",
        // 空白
        "&nbsp;" to " ",
        "&ensp;" to "\u2002",
        "&emsp;" to "\u2003",
        "&thinsp;" to "\u2009",
        // 标点符号
        "&mdash;" to "\u2014",
        "&ndash;" to "\u2013",
        "&lsquo;" to "\u2018",
        "&rsquo;" to "\u2019",
        "&sbquo;" to "\u201A",
        "&ldquo;" to "\u201C",
        "&rdquo;" to "\u201D",
        "&bdquo;" to "\u201E",
        "&laquo;" to "\u00AB",
        "&raquo;" to "\u00BB",
        "&hellip;" to "\u2026",
        "&prime;" to "\u2032",
        "&Prime;" to "\u2033",
        "&sect;" to "\u00A7",
        "&para;" to "\u00B6",
        "&dagger;" to "\u2020",
        "&Dagger;" to "\u2021",
        "&bull;" to "\u2022",
        "&middot;" to "\u00B7",
        "&iexcl;" to "\u00A1",
        "&iquest;" to "\u00BF",
        // 数学/逻辑
        "&times;" to "\u00D7",
        "&divide;" to "\u00F7",
        "&plusmn;" to "\u00B1",
        "&minus;" to "\u2212",
        "&deg;" to "\u00B0",
        "&micro;" to "\u00B5",
        "&euro;" to "\u20AC",
        "&pound;" to "\u00A3",
        "&yen;" to "\u00A5",
        "&cent;" to "\u00A2",
        "&copy;" to "\u00A9",
        "&reg;" to "\u00AE",
        "&trade;" to "\u2122",
        // 特殊符号
        "&hearts;" to "\u2665",
        "&diams;" to "\u2666",
        "&clubs;" to "\u2663",
        "&spades;" to "\u2660",
        "&rarr;" to "\u2192",
        "&larr;" to "\u2190",
        "&harr;" to "\u2194",
        "&uarr;" to "\u2191",
        "&darr;" to "\u2193",
        "#39;" to "'",
    )

    /**
     * 去除 HTML 标签并解码 HTML 实体
     */
    fun decode(html: String): String {
        if (html.isEmpty()) return html
        val noTags = html.replace(Regex("<[^>]*>"), "")
        return decodeEntities(noTags)
    }

    private fun decodeEntities(text: String): String {
        var result = text
        htmlEntities.forEach { (entity, char) ->
            result = result.replace(entity, char)
        }
        // 处理数字实体 &#123;
        result = result.replace(Regex("&#(\\d+);")) { match ->
            match.groupValues[1].toIntOrNull()?.let { code ->
                try { charArrayOf(code.toChar()).concatToString() } catch (_: Exception) { match.value }
            } ?: match.value
        }
        return result
    }
}
