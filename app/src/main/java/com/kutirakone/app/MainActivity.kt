package com.kutirakone.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.kutirakone.app.navigation.KutiraNavGraph
import com.kutirakone.app.navigation.Screen
import com.kutirakone.app.ui.components.KutiraBottomNav
import com.kutirakone.app.ui.components.navItems
import com.kutirakone.app.ui.theme.KutiraKoneTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KutiraKoneTheme {
                val navController = rememberNavController()
                val currentEntry  by navController.currentBackStackEntryAsState()
                val currentRoute  = currentEntry?.destination?.route
                val showBottomNav = navItems.any { it.screen.route == currentRoute }

                Scaffold(
                    modifier  = Modifier.fillMaxSize(),
                    bottomBar = {
                        if (showBottomNav) KutiraBottomNav(navController)
                    }
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        KutiraNavGraph(
                            navController    = navController,
                            // Always start from Login — never skip to Home
                            startDestination = Screen.Login.route
                        )
                    }
                }
            }
        }
    }
}
