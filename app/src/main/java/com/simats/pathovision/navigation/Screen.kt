package com.simats.pathovision.navigation

sealed class Screen(val route: String) {
    object SelectRole : Screen("select_role")
    object Login : Screen("login/{role}") {
        fun createRoute(role: String) = "login/$role"
    }
    object Register : Screen("register")
    object Main : Screen("main")                        // Shared bottom nav (Pathologist)
    object PathologistDashboard : Screen("main")        // Alias to Main for login routing
    object Cases : Screen("cases")
    object PatientDashboard : Screen("patient_dashboard")
    object StudentDashboard : Screen("student_dashboard")
}
