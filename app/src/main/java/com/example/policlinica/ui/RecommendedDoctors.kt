package com.example.policlinica.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
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

data class RecommendedDoctor(
    val name: String,
    val specialty: String,
    val rating: Float,
    val experience: Int,
    val avatar: String
)

@Composable
fun RecommendedDoctorsSection() {
    val doctors = listOf(
        RecommendedDoctor("Dr. Maria Ionescu", "Cardiologie", 4.9f, 15, "👩‍⚕️"),
        RecommendedDoctor("Dr. Andrei Popescu", "Dermatologie", 4.8f, 12, "👨‍⚕️"),
        RecommendedDoctor("Dr. Elena Dumitrescu", "Pedagogie", 4.7f, 10, "👩‍⚕️"),
        RecommendedDoctor("Dr. Mihai Radu", "Ortopedie", 4.9f, 18, "👨‍⚕️")
    )

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "⭐ Doctori Recomandați",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1565C0)
                )
                Text(
                    text = "Cei mai bine cotați specialiști",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
            TextButton(onClick = { }) {
                Text("Vezi toți →", color = Color(0xFF2196F3))
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(doctors) { doctor ->
                DoctorCard(doctor)
            }
        }
    }
}

@Composable
fun DoctorCard(doctor: RecommendedDoctor) {
    Card(
        modifier = Modifier
            .width(200.dp)
            .shadow(6.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFE3F2FD),
                            Color.White
                        )
                    )
                )
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(Color(0xFF2196F3), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = doctor.avatar,
                    fontSize = 32.sp
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = doctor.name,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1565C0)
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = doctor.specialty,
                fontSize = 12.sp,
                color = Color.Gray
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = Color(0xFFFFC107),
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "${doctor.rating}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1565C0)
                )
                Text(
                    text = "• ${doctor.experience} ani exp.",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }
        }
    }
}
