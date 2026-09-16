package com.ascendai.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.ascendai.app.ui.screens.*
import com.ascendai.app.ui.viewmodel.ScanViewModel

@Composable
fun AscendNavHost(
    navController: NavHostController,
    viewModel: ScanViewModel,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Scan.route,
        modifier = modifier
    ) {
        composable(Screen.Scan.route) {
            ScanScreen(
                viewModel = viewModel,
                onNavigateToScanning = {
                    navController.navigate(Screen.Scanning.route)
                }
            )
        }

        composable(Screen.Scanning.route) {
            ScanningProgressScreen(
                viewModel = viewModel,
                onAnalysisFinished = {
                    navController.navigate(Screen.Results.route) {
                        popUpTo(Screen.Scan.route)
                    }
                }
            )
        }

        composable(Screen.Results.route) {
            ResultsScreen(
                viewModel = viewModel,
                onNavigateToGuides = {
                    navController.navigate(Screen.Ascension.route)
                },
                onNavigateToRoutines = {
                    navController.navigate(Screen.Routines.route)
                },
                onRescan = {
                    navController.navigate(Screen.Scan.route) {
                        popUpTo(Screen.Scan.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Ascension.route) {
            AscensionScreen(viewModel = viewModel)
        }

        composable(Screen.Routines.route) {
            RoutinesScreen(viewModel = viewModel)
        }

        composable(Screen.History.route) {
            HistoryScreen(
                viewModel = viewModel,
                onViewResult = {
                    navController.navigate(Screen.Results.route)
                },
                onStartScan = {
                    navController.navigate(Screen.Scan.route)
                }
            )
        }
    }
}
