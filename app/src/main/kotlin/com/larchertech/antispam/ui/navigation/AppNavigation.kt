package com.larchertech.antispam.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sms
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
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.larchertech.antispam.R
import com.larchertech.antispam.ui.screens.calls.CallsScreen
import com.larchertech.antispam.ui.screens.settings.SettingsScreen
import com.larchertech.antispam.ui.screens.sms.SmsScreen

private sealed class Destination(val route: String, val labelRes: Int, val icon: ImageVector) {
    data object Calls : Destination("calls", R.string.nav_calls, Icons.Filled.Call)
    data object Sms : Destination("sms", R.string.nav_sms, Icons.Filled.Sms)
    data object Settings : Destination("settings", R.string.nav_settings, Icons.Filled.Settings)
}

private val bottomNavDestinations = listOf(Destination.Calls, Destination.Sms, Destination.Settings)

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            NavigationBar {
                val backStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = backStackEntry?.destination?.route

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
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Destination.Calls.route,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(Destination.Calls.route) { CallsScreen() }
            composable(Destination.Sms.route) { SmsScreen() }
            composable(Destination.Settings.route) { SettingsScreen() }
        }
    }
}
