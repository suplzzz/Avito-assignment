package com.suplz.avitoassignment.data.util

private val BOLD_REGEX = Regex("\\*\\*(.*?)\\*\\*")
private val ITALIC_REGEX = Regex("_(.*?)_")
private val LINK_REGEX = Regex("\\[(.*?)\\]\\(.*?\\)")

fun String?.containsVisibleText(): Boolean {
    return !this.isNullOrBlank()
}

fun String.clearMarkdown(): String {
    return this
        .replace(BOLD_REGEX, "$1")
        .replace(ITALIC_REGEX, "$1")
        .replace(LINK_REGEX, "$1")
}

fun String.clearAllMarkdown(): String {
    return this.clearMarkdown()
        .replace("*", "")
        .replace("_", "")
        .replace("#", "")
        .trim()
}