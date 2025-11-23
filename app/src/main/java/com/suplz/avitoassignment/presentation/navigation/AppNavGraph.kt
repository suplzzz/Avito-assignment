package com.suplz.avitoassignment.presentation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import com.suplz.avitoassignment.R
import com.suplz.avitoassignment.presentation.screen.auth.LoginScreen
import com.suplz.avitoassignment.presentation.screen.auth.RegisterScreen
import com.suplz.avitoassignment.presentation.screen.books.BooksScreen
import com.suplz.avitoassignment.presentation.screen.profile.ProfileScreen
import com.suplz.avitoassignment.presentation.screen.reader.ReaderScreen
import com.suplz.avitoassignment.presentation.screen.upload.UploadScreen

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()

    val routeAuth = "auth_graph"
    val routeMain = "main_graph"

    val bottomNavItems = listOf(
        Screen.BookList,
        Screen.Upload,
        Screen.Profile
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val showBottomBar = bottomNavItems.any { it.route == currentDestination?.route }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { screen ->

                        val isSelected = currentDestination?.hierarchy?.any { it.route == screen.route } == true

                        NavigationBarItem(
                            icon = {
                                when (screen) {
                                    Screen.BookList -> Icon(Icons.AutoMirrored.Filled.LibraryBooks, contentDescription = null)
                                    Screen.Upload -> Icon(Icons.Default.CloudUpload, contentDescription = null)
                                    Screen.Profile -> Icon(Icons.Default.Person, contentDescription = null)
                                    else -> {}
                                }
                            },
                            label = {
                                when (screen) {
                                    Screen.BookList -> Text(stringResource(R.string.Books))
                                    Screen.Upload -> Text(stringResource(R.string.Download))
                                    Screen.Profile -> Text(stringResource(R.string.Profile))
                                    else -> {}
                                }
                            },
                            selected = isSelected,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(Screen.BookList.route) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    )  { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = routeAuth,
            modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            navigation(startDestination = Screen.Login.route, route = routeAuth) {
                composable(Screen.Login.route) {
                    LoginScreen(
                        onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                        onLoginSuccess = {
                            navController.navigate(routeMain) {
                                popUpTo(routeAuth) { inclusive = true }
                            }
                        }
                    )
                }
                composable(Screen.Register.route) {
                    RegisterScreen(onBack = { navController.popBackStack() })
                }
            }

            navigation(startDestination = Screen.BookList.route, route = routeMain) {
                composable(Screen.BookList.route) {
                    BooksScreen(
                        onBookClick = { bookId ->
                            navController.navigate(Screen.Reader.createRoute(bookId))
                        }
                    )
                }
                composable(Screen.Upload.route) {
                    UploadScreen()
                }
                composable(Screen.Profile.route) {
                    ProfileScreen(
                        onLogout = {
                            navController.navigate(routeAuth) {
                                popUpTo(routeMain) { inclusive = true }
                            }
                        }
                    )
                }

                composable(Screen.Reader.route) { _ ->

                    ReaderScreen(
                        onBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}