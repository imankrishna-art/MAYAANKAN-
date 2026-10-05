package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Screenshot
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.components.GlassCard
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonMagenta
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary

@Composable
fun QuickActionsDialog(
    onDismiss: () -> Unit,
    onVisionClick: () -> Unit,
    onWeatherClick: () -> Unit,
    onContactsClick: () -> Unit,
    onOpenAppsClick: () -> Unit
) {
    val context = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        GlassCard(
            shape = RoundedCornerShape(26.dp),
            modifier = Modifier.fillMaxWidth(),
            highlightCyan = true
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Quick Actions",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(18.dp))

                // 2-column Grid
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ActionCard(
                            title = "Camera",
                            subtitle = "Vision Mode",
                            icon = Icons.Default.CameraAlt,
                            iconColor = NeonCyan,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                onDismiss()
                                onVisionClick()
                            }
                        )
                        ActionCard(
                            title = "Screen",
                            subtitle = "Understanding",
                            icon = Icons.Default.Screenshot,
                            iconColor = NeonMagenta,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                onDismiss()
                                // Assist intent / screen understanding trigger
                                try {
                                    val intent = Intent(Intent.ACTION_ASSIST)
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    onVisionClick()
                                }
                            }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ActionCard(
                            title = "Location",
                            subtitle = "Weather & Maps",
                            icon = Icons.Default.LocationOn,
                            iconColor = NeonCyan,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                onDismiss()
                                onWeatherClick()
                            }
                        )
                        ActionCard(
                            title = "Contacts",
                            subtitle = "Search & Call",
                            icon = Icons.Default.Call,
                            iconColor = NeonViolet,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                onDismiss()
                                onContactsClick()
                            }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ActionCard(
                            title = "Messages",
                            subtitle = "SMS & Chat",
                            icon = Icons.Default.Message,
                            iconColor = NeonPink,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                onDismiss()
                                try {
                                    val intent = Intent(Intent.ACTION_MAIN).apply {
                                        addCategory(Intent.CATEGORY_APP_MESSAGING)
                                    }
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    val fallback = Intent(Intent.ACTION_VIEW, Uri.parse("sms:"))
                                    context.startActivity(fallback)
                                }
                            }
                        )
                        ActionCard(
                            title = "App Control",
                            subtitle = "Open Settings",
                            icon = Icons.Default.Apps,
                            iconColor = NeonCyan,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                onDismiss()
                                onOpenAppsClick()
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0x331E293B)
                    ),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(20.dp))
                        .testTag("close_quick_actions")
                ) {
                    Text("Close", color = TextPrimary, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
private fun ActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0x990F172A))
            .border(1.dp, Color(0x2600E5FF), RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(14.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = title,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = subtitle,
                color = TextMuted,
                fontSize = 11.sp
            )
        }
    }
}
