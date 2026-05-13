package com.kutirakone.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.kutirakone.app.ui.screens.auth.LoginScreen
import com.kutirakone.app.ui.screens.chat.ChatScreen
import com.kutirakone.app.ui.screens.design.DesignIdeasScreen
import com.kutirakone.app.ui.screens.home.HomeScreen
import com.kutirakone.app.ui.screens.map.MapScreen
import com.kutirakone.app.ui.screens.profile.ProfileScreen
import com.kutirakone.app.ui.screens.rating.RateUserScreen
import com.kutirakone.app.ui.screens.scrap.ScrapDetailScreen
import com.kutirakone.app.ui.screens.swap.SwapRequestScreen
import com.kutirakone.app.ui.screens.trades.MyTradesScreen
import com.kutirakone.app.ui.screens.upload.UploadScrapScreen

@Composable
fun KutiraNavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Login.route
) {
    NavHost(navController = navController, startDestination = startDestination) {

        composable(Screen.Login.route) {
            LoginScreen(onLoginSuccess = {
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.Login.route) { inclusive = true }
                }
            })
        }

        composable(Screen.Home.route) {
            HomeScreen(
                onScrapClick   = { scrapId -> navController.navigate(Screen.ScrapDetail.createRoute(scrapId)) },
                onUploadClick  = { navController.navigate(Screen.Upload.route) },
                onMapClick     = { navController.navigate(Screen.Map.route) },
                onProfileClick = { navController.navigate(Screen.Profile.route) },
                onTradesClick  = { navController.navigate(Screen.MyTrades.route) }
            )
        }

        composable(Screen.Map.route) {
            MapScreen(
                onScrapClick = { scrapId -> navController.navigate(Screen.ScrapDetail.createRoute(scrapId)) },
                onBackClick  = { navController.popBackStack() }
            )
        }

        composable(Screen.Upload.route) {
            UploadScrapScreen(
                onPublished = { navController.popBackStack() },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            route     = Screen.ScrapDetail.route,
            arguments = listOf(navArgument("scrapId") { type = NavType.StringType })
        ) { backStack ->
            val scrapId = backStack.arguments?.getString("scrapId") ?: return@composable
            ScrapDetailScreen(
                scrapId     = scrapId,
                onBackClick = { navController.popBackStack() },
                onSwapClick = { navController.navigate(Screen.SwapRequest.createRoute(scrapId)) },
                onChatClick = { sellerId -> navController.navigate(Screen.Chat.createRoute(sellerId, scrapId)) },
                onIdeasClick = { navController.navigate(Screen.DesignIdeas.createRoute(scrapId)) }
            )
        }

        composable(
            route     = Screen.SwapRequest.route,
            arguments = listOf(navArgument("scrapId") { type = NavType.StringType })
        ) { backStack ->
            val scrapId = backStack.arguments?.getString("scrapId") ?: return@composable
            SwapRequestScreen(
                targetScrapId = scrapId,
                onRequestSent = { navController.navigate(Screen.MyTrades.route) { popUpTo(Screen.Home.route) } },
                onBackClick   = { navController.popBackStack() }
            )
        }

        composable(
            route     = Screen.DesignIdeas.route,
            arguments = listOf(navArgument("scrapId") { type = NavType.StringType })
        ) { backStack ->
            val scrapId = backStack.arguments?.getString("scrapId") ?: return@composable
            DesignIdeasScreen(scrapId = scrapId, onBackClick = { navController.popBackStack() })
        }

        composable(
            route     = Screen.Chat.route,
            arguments = listOf(
                navArgument("sellerId") { type = NavType.StringType },
                navArgument("scrapId")  { type = NavType.StringType }
            )
        ) { backStack ->
            val sellerId = backStack.arguments?.getString("sellerId") ?: return@composable
            val scrapId  = backStack.arguments?.getString("scrapId")  ?: return@composable
            ChatScreen(sellerId = sellerId, scrapId = scrapId, onBackClick = { navController.popBackStack() })
        }

        composable(Screen.MyTrades.route) {
            MyTradesScreen(
                onRateUser   = { tradeId -> navController.navigate(Screen.RateUser.createRoute(tradeId)) },
                onScrapClick = { scrapId -> navController.navigate(Screen.ScrapDetail.createRoute(scrapId)) },
                onBackClick  = { navController.popBackStack() }
            )
        }

        composable(
            route     = Screen.RateUser.route,
            arguments = listOf(navArgument("tradeId") { type = NavType.StringType })
        ) { backStack ->
            val tradeId = backStack.arguments?.getString("tradeId") ?: return@composable
            RateUserScreen(
                tradeId     = tradeId,
                onSubmitted = { navController.popBackStack() },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                onSignOut   = { navController.navigate(Screen.Login.route) { popUpTo(0) { inclusive = true } } },
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
