package com.example.dailycost.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.dailycost.ui.addedit.AddEditScreen
import com.example.dailycost.ui.main.MainScreen
import com.example.dailycost.ui.settings.SettingsScreen

object Routes {
    const val MAIN = "main"
    const val ADD_EDIT = "add_edit/{itemId}"
    const val SETTINGS = "settings"
    fun addEdit(itemId: Long = -1L) = "add_edit/$itemId"
}

@Composable
fun DailyCostNavHost() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Routes.MAIN) {

        composable(Routes.MAIN) {
            MainScreen(
                onNavigateToAdd = { navController.navigate(Routes.addEdit()) },
                onNavigateToEdit = { id -> navController.navigate(Routes.addEdit(id)) },
                onNavigateToSettings = { navController.navigate(Routes.SETTINGS) }
            )
        }

        composable(route = Routes.ADD_EDIT, arguments = listOf(navArgument("itemId") { type = NavType.LongType })) { entry ->
            val id = entry.arguments?.getLong("itemId") ?: -1L
            AddEditScreen(itemId = if (id == -1L) null else id, onNavigateBack = { navController.popBackStack() })
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(onNavigateBack = { navController.popBackStack() })
        }
    }
}
