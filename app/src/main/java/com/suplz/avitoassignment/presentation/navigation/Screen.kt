package com.suplz.avitoassignment.presentation.navigation

sealed class Screen(val route: String) {
    data object Login : Screen("login")
    data object Register : Screen("register")

    data object BookList : Screen("book_list")
    data object Upload : Screen("upload")
    data object Profile : Screen("profile")

    data object Reader : Screen("reader/{bookId}") {
        fun createRoute(bookId: String) = "reader/$bookId"
    }
}