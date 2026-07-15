package com.larchertech.antispam.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.larchertech.antispam.R
import com.larchertech.antispam.blocking.PermissionStatus
import com.larchertech.antispam.ui.screens.calls.CallsScreen
import com.larchertech.antispam.ui.screens.onboarding.OnboardingScreen
import com.larchertech.antispam.ui.screens.settings.SettingsScreen

private sealed class Destination(val route: String, val labelRes: Int, val icon: ImageVector) {
    data object Calls : Destination("calls", R.string.nav_calls, Icons.Filled.Call)
    data object Settings : Destination("settings", R.string.nav_settings, Icons.Filled.Settings)
}

private const val ONBOARDING_ROUTE = "onboarding"

private val bottomNavDestinations = listOf(Destination.Calls, Destination.Settings)

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current

    val startDestination = remember {
        val protectionComplete = PermissionStatus.isCallProtectionComplete(context)
        if (protectionComplete) Destination.Calls.route else ONBOARDING_ROUTE
    }

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = bottomNavDestinations.any { it.route == currentRoute }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavDestinations.forEach { destination ->
                        NavigationBarItem(
                            selected = currentRoute == destination.route,
                            onClick = {
                                navController.navigate(destination.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(destination.icon, contentDescription = null) },
                            label = { Text(stringResource(destination.labelRes)) },
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        val onOpenOnboarding: () -> Unit = { navController.navigate(ONBOARDING_ROUTE) }

        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(Destination.Calls.route) { CallsScreen(onOpenOnboarding = onOpenOnboarding) }
            composable(Destination.Settings.route) { SettingsScreen(onOpenOnboarding = onOpenOnboarding) }
            composable(ONBOARDING_ROUTE) {
                OnboardingScreen(
                    onContinue = {
                        navController.navigate(Destination.Calls.route) {
                            popUpTo(ONBOARDING_ROUTE) { inclusive = true }
                        }
                    },
                )
            }
        }
    }
}
