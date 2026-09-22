package com.titanali.app.ui.nav

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.titanali.app.R
import com.titanali.app.ui.screens.ChatScreen
import com.titanali.app.ui.screens.HomeScreen
import com.titanali.app.ui.screens.LearnScreen
import com.titanali.app.ui.screens.LessonScreen
import com.titanali.app.ui.screens.SettingsScreen
import com.titanali.app.ui.screens.TitanaliScreen

private val TOP_ROUTES = listOf("home", "titanali", "learn", "settings")

@Composable
fun AppNav() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = currentRoute in TOP_ROUTES

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    BottomItem(
                        route = "home",
                        icon = { Icon(Icons.Filled.Chat, contentDescription = null) },
                        label = { Text(stringResource(R.string.nav_chat)) },
                        selected = currentRoute == "home",
                        nav = navController,
                    )
                    BottomItem(
                        route = "titanali",
                        icon = { Icon(Icons.Filled.AutoAwesome, contentDescription = null) },
                        label = { Text(stringResource(R.string.nav_titanali)) },
                        selected = currentRoute == "titanali",
                        nav = navController,
                    )
                    BottomItem(
                        route = "learn",
                        icon = { Icon(Icons.Filled.School, contentDescription = null) },
                        label = { Text(stringResource(R.string.nav_learn)) },
                        selected = currentRoute == "learn",
                        nav = navController,
                    )
                    BottomItem(
                        route = "settings",
                        icon = { Icon(Icons.Filled.Settings, contentDescription = null) },
                        label = { Text(stringResource(R.string.nav_settings)) },
                        selected = currentRoute == "settings",
                        nav = navController,
                    )
                }
            }
        },
    ) { innerPadding ->
        Box(Modifier.fillMaxSize().padding(innerPadding)) {
            NavHost(navController, startDestination = "home") {
                composable("home") {
                    HomeScreen(navController = navController)
                }
                composable("titanali") {
                    TitanaliScreen(navController = navController)
                }
                composable("learn") {
                    LearnScreen(navController = navController)
                }
                composable("settings") {
                    SettingsScreen()
                }
                composable(
                    route = "chat/{convId}",
                    arguments = listOf(navArgument("convId") { type = NavType.LongType }),
                ) { entry ->
                    val convId = entry.arguments?.getLong("convId") ?: return@composable
                    ChatScreen(convId = convId, navController = navController)
                }
                composable(
                    route = "lesson/{lang}/{lessonId}",
                    arguments = listOf(
                        navArgument("lang") { type = NavType.StringType },
                        navArgument("lessonId") { type = NavType.StringType },
                    ),
                ) { entry ->
                    val lang = entry.arguments?.getString("lang") ?: return@composable
                    val lessonId = entry.arguments?.getString("lessonId") ?: return@composable
                    LessonScreen(lang = lang, lessonId = lessonId)
                }
            }
        }
    }
}

@Composable
private fun BottomItem(
    route: String,
    icon: @Composable () -> Unit,
    label: @Composable () -> Unit,
    selected: Boolean,
    nav: NavHostController,
) {
    NavigationBarItem(
        selected = selected,
        onClick = {
            if (!selected) {
                nav.navigate(route) { launchSingleTop = true }
            }
        },
        icon = icon,
        label = label,
        alwaysShowLabel = true,
    )
}
