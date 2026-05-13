package com.kutirakone.app.navigation



sealed class Screen(val route: String) {

    object Login : Screen("login")
    object Home : Screen("home")
    object Map : Screen("map")
    object Upload : Screen("upload")
    object Profile : Screen("profile")
    object MyTrades : Screen("my_trades")

    object ScrapDetail : Screen("scrap_detail/{scrapId}") {
        fun createRoute(scrapId: String) = "scrap_detail/$scrapId"
    }

    object SwapRequest : Screen("swap_request/{scrapId}") {
        fun createRoute(scrapId: String) = "swap_request/$scrapId"
    }

    object DesignIdeas : Screen("design_ideas/{scrapId}") {
        fun createRoute(scrapId: String) = "design_ideas/$scrapId"
    }

    object Chat : Screen("chat/{sellerId}/{scrapId}") {
        fun createRoute(sellerId: String, scrapId: String) =
            "chat/$sellerId/$scrapId"
    }

    object RateUser : Screen("rate_user/{tradeId}") {
        fun createRoute(tradeId: String) = "rate_user/$tradeId"
    }
}