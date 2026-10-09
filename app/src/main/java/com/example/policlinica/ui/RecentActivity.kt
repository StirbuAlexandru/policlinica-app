package com.example.policlinica.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class ActivityItem(
    val icon: String,
    val title: String,
    val description: String,
    val time: String,
    val color: Color
)

@Composable
fun RecentActivitySection() {
    val activities = listOf(
        ActivityItem(
            "✅",
            "Programare Confirmată",
            "Consultație Cardiologie - Dr. Maria Ionescu",
            "Acum 2 ore",
            Color(0xFF4CAF50)
        ),
        ActivityItem(
            "📅",
            "Programare Nouă",
            "Analize de sânge programate pentru 15 Ianuarie",
            "Acum 5 ore",
            Color(0xFF2196F3)
        ),
        ActivityItem(
            "💊",
            "Rețetă Eliberată",
            "Rețetă pentru tratament hipertensiune",
            "Ieri",
            Color(0xFFFF9800)
        ),
        ActivityItem(
            "🔔",
            "Reamintire",
            "Programare Dermatologie mâine la ora 10:00",
            "Acum 2 zile",
            Color(0xFFE91E63)
        )
    )

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "🕐 Activitate Recentă",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1565C0)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Ultimele acțiuni și actualizări",
            fontSize = 12.sp,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(12.dp))
        
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            activities.forEach { activity ->
                ActivityCard(activity)
            }
        }
    }
}

@Composable
fun ActivityCard(activity: ActivityItem) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(activity.color.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = activity.icon,
                    fontSize = 22.sp
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = activity.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1565C0)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = activity.description,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
            
            Text(
                text = activity.time,
                fontSize = 11.sp,
                color = Color.Gray,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}
