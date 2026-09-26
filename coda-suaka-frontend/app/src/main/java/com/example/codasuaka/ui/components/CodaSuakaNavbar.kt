package com.example.codasuaka.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.codasuaka.ui.theme.Neutral
import com.example.codasuaka.ui.theme.Primary
import com.example.codasuaka.ui.theme.Secondary

/**
 * Navbar Item Data Class with Outlined/Filled icon support.
 */
data class NavbarItem(
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val label: String,
    val index: Int,
    val hasBadge: Boolean = false
)

/**
 * Racikan Senior: Modern Glassmorphism Floating Navbar.
 */
@Composable
fun CodaSuakaNavbar(
    items: List<NavbarItem>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .padding(horizontal = 24.dp, vertical = 14.dp) // Optimized padding
            .fillMaxWidth()
            .height(76.dp),
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        shadowElevation = 10.dp, // Balanced 3D effect
        border = androidx.compose.foundation.BorderStroke(1.dp, Primary.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                val isSelected = selectedIndex == item.index
                CodaSuakaNavbarItem(
                    item = item,
                    isSelected = isSelected,
                    onClick = { onItemSelected(item.index) }
                )
            }
        }
    }
}

@Composable
private fun CodaSuakaNavbarItem(
    item: NavbarItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    // --- Animations ---
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) Secondary else Secondary.copy(alpha = 0.5f),
        animationSpec = tween(300),
        label = "contentColor"
    )
    val pillColor by animateColorAsState(
        targetValue = if (isSelected) Color.White else Color.Transparent,
        animationSpec = tween(300),
        label = "pillColor"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) Primary.copy(alpha = 0.5f) else Color.Transparent,
        animationSpec = tween(300),
        label = "borderColor"
    )
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.1f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )

    Box(
        modifier = Modifier
            .width(76.dp)
            .fillMaxHeight()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.scale(scale)
        ) {
            // --- Lifted Pill with Glow Border ---
            Box(
                modifier = Modifier
                    .width(62.dp)
                    .height(38.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(pillColor)
                    .then(
                        if (isSelected) Modifier.border(
                            width = 2.dp,
                            color = borderColor,
                            shape = RoundedCornerShape(20.dp)
                        ) else Modifier
                    ),
                contentAlignment = Alignment.Center
            ) {
                BadgedBox(
                    badge = {
                        if (item.hasBadge) {
                            Badge(
                                containerColor = Color.Red,
                                modifier = Modifier.size(6.dp)
                            )
                        }
                    }
                ) {
                    Icon(
                        imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.label,
                        tint = contentColor,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // --- Label ---
            Text(
                text = item.label,
                color = contentColor,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Bold,
                letterSpacing = 0.sp
            )
        }
    }
}
