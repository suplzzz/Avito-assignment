package com.suplz.avitoassignment.domain.entity


sealed interface ReaderContent {


    data class Chapter(val title: String) : ReaderContent


    data class Text(val text: String) : ReaderContent

    data object Separator : ReaderContent
}