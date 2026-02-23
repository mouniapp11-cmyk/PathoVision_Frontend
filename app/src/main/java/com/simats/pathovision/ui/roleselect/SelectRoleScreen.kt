package com.simats.pathovision.ui.roleselect

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Biotech
import androidx.compose.material.icons.rounded.School
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val BackgroundGray = Color(0xFFF4F5F7)
val TextDark = Color(0xFF1A1F2E)
val TextGray = Color(0xFF8E9BB5)
val White = Color.White

data class RoleOption(
    val role: String,
    val title: String,
    val description: String,
    val icon: ImageVector,
    val iconBg: Color,
    val iconTint: Color
)

val roleOptions = listOf(
    RoleOption(
        role = "pathologist",
        title = "Pathologist",
        description = "Upload & analyze cases",
        icon = Icons.Rounded.Biotech,
        iconBg = Color(0xFFE8EDFF),
        iconTint = Color(0xFF4A6FE3)
    ),
    RoleOption(
        role = "patient",
        title = "Patient",
        description = "View reports & results",
        icon = Icons.Rounded.AccountCircle,
        iconBg = Color(0xFFE8F8EE),
        iconTint = Color(0xFF2EB872)
    ),
    RoleOption(
        role = "student",
        title = "Medical Student",
        description = "Access case studies",
        icon = Icons.Rounded.School,
        iconBg = Color(0xFFF0EBFF),
        iconTint = Color(0xFF7C5FDC)
    )
)

@Composable
fun SelectRoleScreen(
    onRoleSelected: (role: String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray)
            .padding(horizontal = 24.dp, vertical = 48.dp)
    ) {
        Column {
            Text(
                text = "Select Your Role",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextDark
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Choose how you want to access the system",
                fontSize = 15.sp,
                color = TextGray
            )

            Spacer(modifier = Modifier.height(40.dp))

            roleOptions.forEach { option ->
                RoleCard(option = option, onClick = { onRoleSelected(option.role) })
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun RoleCard(option: RoleOption, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon circle
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(option.iconBg, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = option.icon,
                    contentDescription = option.title,
                    tint = option.iconTint,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = option.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = option.description,
                    fontSize = 13.sp,
                    color = TextGray
                )
            }

            Icon(
                imageVector = Icons.Rounded.AccountCircle, // just a forward arrow substitute
                contentDescription = "Go",
                tint = Color(0xFFCBD0DB),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
