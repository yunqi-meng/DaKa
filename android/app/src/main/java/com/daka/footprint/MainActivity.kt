package com.daka.footprint

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.daka.footprint.ui.achievement.AchievementScreen
import com.daka.footprint.ui.checkin.CheckinEditScreen
import com.daka.footprint.ui.main.MapMainScreen
import com.daka.footprint.ui.main.OfflineMapScreen
import com.daka.footprint.ui.profile.ProfileScreen
import com.daka.footprint.ui.theme.DakaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DakaTheme {
                MainApp()
            }
        }
    }
}

sealed class BottomTab(val route: String, val labelRes: Int, val icon: ImageVector) {
    data object Map : BottomTab("map", R.string.tab_map, Icons.Filled.Map)
    data object Achievement : BottomTab("achievement", R.string.tab_achievement, Icons.Filled.EmojiEvents)
    data object Profile : BottomTab("profile", R.string.tab_profile, Icons.Filled.Person)
}

private val tabs = listOf(BottomTab.Map, BottomTab.Achievement, BottomTab.Profile)

@Composable
fun MainApp() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination

    Scaffold(
        bottomBar = {
            NavigationBar {
                tabs.forEach { tab ->
                    val selected = currentDestination?.hierarchy?.any { it.route == tab.route } == true
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(tab.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(tab.icon, contentDescription = null) },
                        label = { Text(stringResource(tab.labelRes)) }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = BottomTab.Map.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(BottomTab.Map.route) {
                MapMainScreen(
                    onOfflineMapClick = { navController.navigate("offline_map") },
                    onCheckinClick = { lat, lng ->
                        navController.navigate("checkin_edit/$lat/$lng")
                    }
                )
            }
            composable("offline_map") {
                OfflineMapScreen(
                    onBack = { navController.popBackStack() }
                )
            }
            composable(
                route = "checkin_edit/{lat}/{lng}",
            ) { backStackEntry ->
                val lat = backStackEntry.arguments?.getString("lat")?.toDoubleOrNull() ?: 0.0
                val lng = backStackEntry.arguments?.getString("lng")?.toDoubleOrNull() ?: 0.0
                CheckinEditScreen(
                    latitude = lat,
                    longitude = lng,
                    onSaved = { navController.popBackStack() },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(BottomTab.Achievement.route) { AchievementScreen() }
            composable(BottomTab.Profile.route) { ProfileScreen() }
        }
    }
}