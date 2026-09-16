package com.ascendai.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Insights
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    object Scan : Screen("scan", "Scan", Icons.Default.CameraAlt)
    object Scanning : Screen("scanning", "Scanning", Icons.Default.CameraAlt)
    object Results : Screen("results", "Report", Icons.Default.Insights)
    object Ascension : Screen("ascension", "Guides", Icons.Default.AutoAwesome)
    object Routines : Screen("routines", "Routines", Icons.Default.FactCheck)
    object History : Screen("history", "Log", Icons.Default.History)

    companion object {
        val bottomNavItems = listOf(
            Scan,
            Ascension,
            Routines,
            History
        )
    }
}
