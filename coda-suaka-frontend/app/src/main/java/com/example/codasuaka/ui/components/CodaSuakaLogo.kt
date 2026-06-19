package com.example.codasuaka.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.codasuaka.R
import com.example.codasuaka.ui.theme.Primary

/**
 * CodaSuaka Logo — reusable component for Login & Register screens.
 * Displays the CodaSuaka brand logo from drawable resources,
 * followed by the "CodaSuaka" brand name below.
 *
 * @param iconSize    Overall size of the logo image (default 80dp for login, 64dp for register)
 * @param showText    Whether to show the "CodaSuaka" text below the icon (default true)
 */
@Composable
fun CodaSuakaLogo(
    iconSize: Dp = 80.dp,
    showText: Boolean = true,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        // ── Logo image ──
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "CodaSuaka Logo",
            modifier = Modifier.size(iconSize),
            contentScale = ContentScale.Fit
        )

        // ── Brand name ──
        if (showText) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "CodaSuaka",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Primary
            )
        }
    }
}
