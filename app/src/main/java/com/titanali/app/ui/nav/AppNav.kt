package com.titanali.app.ui.nav

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
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
import com.titanali.app.ui.screens.AnalysisScreen
import com.titanali.app.ui.screens.ChatScreen
import com.titanali.app.ui.screens.HomeScreen
import com.titanali.app.ui.screens.LearnScreen
import com.titanali.app.ui.screens.LessonScreen
import com.titanali.app.ui.screens.SettingsScreen
import com.titanali.app.ui.screens.TitanaliScreen

private val TOP_ROUTES = listOf("home", "analysis", "titanali", "learn", "settings")

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
                    NavigationBarItem(
                        selected = currentRoute == "home",
                        onClick = {
                            if (currentRoute != "home") navController.navigate("home") { launchSingleTop = true }
                        },
                        icon = { Icon(Icons.Filled.Chat, contentDescription = null) },
                        label = { Text(stringResource(R.string.nav_chat)) },
                        alwaysShowLabel = true,
                    )
                    NavigationBarItem(
                        selected = currentRoute == "analysis",
                        onClick = {
                            if (currentRoute != "analysis") navController.navigate("analysis") { launchSingleTop = true }
                        },
                        icon = { Icon(Icons.Filled.Analytics, contentDescription = null) },
                        label = { Text(stringResource(R.string.nav_analysis)) },
                        alwaysShowLabel = true,
                    )
                    NavigationBarItem(
                        selected = currentRoute == "titanali",
                        onClick = {
                            if (currentRoute != "titanali") navController.navigate("titanali") { launchSingleTop = true }
                        },
                        icon = { Icon(Icons.Filled.AutoAwesome, contentDescription = null) },
                        label = { Text(stringResource(R.string.nav_titanali)) },
                        alwaysShowLabel = true,
                    )
                    NavigationBarItem(
                        selected = currentRoute == "learn",
                        onClick = {
                            if (currentRoute != "learn") navController.navigate("learn") { launchSingleTop = true }
                        },
                        icon = { Icon(Icons.Filled.School, contentDescription = null) },
                        label = { Text(stringResource(R.string.nav_learn)) },
                        alwaysShowLabel = true,
                    )
                    NavigationBarItem(
                        selected = currentRoute == "settings",
                        onClick = {
                            if (currentRoute != "settings") navController.navigate("settings") { launchSingleTop = true }
                        },
                        icon = { Icon(Icons.Filled.Settings, contentDescription = null) },
                        label = { Text(stringResource(R.string.nav_settings)) },
                        alwaysShowLabel = true,
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
                composable("analysis") {
                    AnalysisScreen()
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


