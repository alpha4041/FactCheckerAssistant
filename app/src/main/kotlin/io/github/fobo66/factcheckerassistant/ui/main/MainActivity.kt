package io.github.fobo66.factcheckerassistant.ui.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.core.view.WindowCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.paging.compose.collectAsLazyPagingItems
import dagger.hilt.android.AndroidEntryPoint
import io.github.fobo66.factcheckerassistant.R
import io.github.fobo66.factcheckerassistant.ui.guide.FactCheckGuide
import io.github.fobo66.factcheckerassistant.ui.list.ClaimDetails
import io.github.fobo66.factcheckerassistant.ui.list.ClaimsSearch
import io.github.fobo66.factcheckerassistant.ui.theme.FactCheckerAssistantTheme
import io.github.fobo66.factcheckerassistant.util.Screen

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val bottomBarItems = listOf(
        Screen.Search,
        Screen.Guide
    )

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            FactCheckerAssistantTheme {
                val navController = rememberNavController()
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = {
                                Text(
                                    stringResource(R.string.app_name)
                                )
                            }
                        )
                    },
                    bottomBar = {
                        BottomNavBar(navController, Modifier.navigationBarsPadding())
                    }
                ) { innerPadding ->
                    val mainViewModel: MainViewModel = hiltViewModel()

                    NavHost(navController, startDestination = "search") {
                        composable("search") {
                            val claims = mainViewModel.claims.collectAsLazyPagingItems()
                            val query by mainViewModel.query.collectAsState()

                            ClaimsSearch(
                                modifier = Modifier.padding(innerPadding),
                                query = query,
                                claims = claims,
                                onSearch = {
                                    mainViewModel.search(it)
                                },
                                onSearchResultClick = {
                                    mainViewModel.selectClaim(it)
                                    navController.navigate("details")
                                }
                            )
                        }
                        composable("details") {
                            val claim by mainViewModel.selectedClaim.collectAsState()
                            ClaimDetails(
                                claim,
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                        composable("guide") {
                            FactCheckGuide(
                                modifier = Modifier.padding(
                                    innerPadding
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun BottomNavBar(navController: NavHostController, modifier: Modifier = Modifier) {
        NavigationBar(
            modifier = modifier
        ) {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination
            bottomBarItems.forEach { screen ->
                NavigationBarItem(
                    icon = {
                        Icon(
                            screen.icon,
                            contentDescription = null
                        )
                    },
                    label = { Text(stringResource(screen.resourceId)) },
                    selected = currentDestination?.hierarchy
                        ?.any { it.route == screen.route } == true,
                    onClick = {
                        navController.navigate(screen.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    }
}
