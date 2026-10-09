package com.example.policlinica.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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

@Composable
fun ForecastSection() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "📈 Prognoză Programări",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1565C0)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Estimare pentru săptămâna viitoare",
                fontSize = 12.sp,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            // Line chart simulation
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            ) {
                val width = size.width
                val height = size.height
                val points = listOf(
                    androidx.compose.ui.geometry.Offset(0f, height * 0.6f),
                    androidx.compose.ui.geometry.Offset(width * 0.16f, height * 0.4f),
                    androidx.compose.ui.geometry.Offset(width * 0.33f, height * 0.5f),
                    androidx.compose.ui.geometry.Offset(width * 0.5f, height * 0.2f),
                    androidx.compose.ui.geometry.Offset(width * 0.66f, height * 0.3f),
                    androidx.compose.ui.geometry.Offset(width * 0.83f, height * 0.15f),
                    androidx.compose.ui.geometry.Offset(width, height * 0.25f)
                )
                
                // Draw line
                for (i in 0 until points.size - 1) {
                    drawLine(
                        color = Color(0xFF2196F3),
                        start = points[i],
                        end = points[i + 1],
                        strokeWidth = 4f
                    )
                }
                
                // Draw points
                points.forEach { point ->
                    drawCircle(
                        color = Color(0xFF1565C0),
                        radius = 6f,
                        center = point
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 3f,
                        center = point
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Stats grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ForecastStatItem(
                    icon = "📊",
                    label = "Estimare",
                    value = "45-50",
                    color = Color(0xFF2196F3),
                    modifier = Modifier.weight(1f)
                )
                ForecastStatItem(
                    icon = "📈",
                    label = "Creștere",
                    value = "+12%",
                    color = Color(0xFF4CAF50),
                    modifier = Modifier.weight(1f)
                )
                ForecastStatItem(
                    icon = "⏰",
                    label = "Vârf",
                    value = "Joi",
                    color = Color(0xFFFF9800),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun ForecastStatItem(
    icon: String,
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(color.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = icon, fontSize = 20.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            fontSize = 11.sp,
            color = Color.Gray
        )
    }
}
