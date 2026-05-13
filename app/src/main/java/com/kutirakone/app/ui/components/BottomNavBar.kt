package com.kutirakone.app.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.kutirakone.app.navigation.Screen
import com.kutirakone.app.ui.theme.Green600

data class NavItem(
    val screen: Screen,
    val label: String,
    val icon: ImageVector
)

val navItems = listOf(
    NavItem(Screen.Home,     "Home",    Icons.Filled.Home),
    NavItem(Screen.Map,      "Map",     Icons.Filled.Map),
    NavItem(Screen.Upload,   "Upload",  Icons.Filled.AddCircle),
    NavItem(Screen.MyTrades, "Trades",  Icons.Filled.SwapHoriz),
    NavItem(Screen.Profile,  "Profile", Icons.Filled.AccountCircle),
)

@Composable
fun KutiraBottomNav(navController: NavController) {
    val currentRoute = navController
        .currentBackStackEntryAsState().value?.destination?.route

    NavigationBar(containerColor = Color.White) {
        navItems.forEach { item ->
            val selected = currentRoute == item.screen.route
            NavigationBarItem(
                selected = selected,
                onClick  = {
                    if (currentRoute != item.screen.route) {
                        navController.navigate(item.screen.route) {
                            popUpTo(Screen.Home.route) {
                                inclusive = false
                            }
                            launchSingleTop = true
                        }
                    }
                },
                icon  = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label, fontSize = 10.sp) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor   = Green600,
                    selectedTextColor   = Green600,
                    indicatorColor      = Color(0xFFEAF3DE),
                    unselectedIconColor = Color.Gray,
                    unselectedTextColor = Color.Gray
                )
            )
        }
    }
}