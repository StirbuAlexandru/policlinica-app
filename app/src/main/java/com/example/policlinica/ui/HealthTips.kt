package com.example.policlinica.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class HealthTip(
    val icon: String,
    val title: String,
    val description: String,
    val color: Color
)

@Composable
fun HealthTipsSection() {
    val tips = listOf(
        HealthTip(
            "💊",
            "Programări Periodice",
            "Nu uita să îți programezi consultațiile anuale de rutină pentru menținerea sănătății.",
            Color(0xFFE91E63)
        ),
        HealthTip(
            "🩺",
            "Analize Anuale",
            "Analizele de sânge anuale te ajută să depistezi problemele din timp.",
            Color(0xFFFF9800)
        ),
        HealthTip(
            "🏃",
            "Viață Activă",
            "30 de minute de exerciții zilnic pot îmbunătăți semnificativ sănătatea ta.",
            Color(0xFF4CAF50)
        ),
        HealthTip(
            "😴",
            "Somn Odihnitor",
            "7-8 ore de somn pe noapte sunt esențiale pentru sănătatea fizică și mentală.",
            Color(0xFF2196F3)
        )
    )

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "💡 Sfaturi pentru Sănătate",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1565C0)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Recomandări importante de la doctorii noștri",
            fontSize = 12.sp,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(12.dp))
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(tips) { tip ->
                HealthTipCard(tip)
            }
        }
    }
}

@Composable
fun HealthTipCard(tip: HealthTip) {
    Card(
        modifier = Modifier
            .width(250.dp)
            .shadow(6.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(tip.color.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = tip.icon,
                    fontSize = 24.sp
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = tip.title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1565C0)
            )
            
            Spacer(modifier = Modifier.height(6.dp))
            
            Text(
                text = tip.description,
                fontSize = 12.sp,
                color = Color.Gray,
                lineHeight = 16.sp
            )
        }
    }
}
