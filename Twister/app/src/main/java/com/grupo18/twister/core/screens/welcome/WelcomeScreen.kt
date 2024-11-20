package com.grupo18.twister.core.screens.welcome

import com.grupo18.twister.R
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily

@Composable
fun WelcomeScreen(onNavigateToAuth: () -> Unit) {
    val jaroFontFamily = FontFamily(Font(R.font.jaro))
    val jockeyFontFamily = FontFamily(Font(R.font.jockeyone))
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Image(
            painter = painterResource(id = R.drawable.bg_welcome),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "TWIST",
                    fontSize = 88.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    fontFamily = jaroFontFamily,
                    letterSpacing = 6.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Learn, play, and win: Knowledge is your superpower!",
                    textAlign = TextAlign.Center,
                    fontSize = 20.sp,
                    color = Color.White,
                    fontFamily = jockeyFontFamily
                )

                Spacer(modifier = Modifier.height(32.dp))

                Image(
                    painter = painterResource(id = R.drawable.ico),
                    contentDescription = null,
                    modifier = Modifier
                        .padding(top = 32.dp)
                )
            }

            Button(
                onClick = onNavigateToAuth,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp, bottom = 10.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "Start playing",
                    fontSize = 20.sp,
                    color = Color.White,
                    fontFamily = jockeyFontFamily
                )
            }
        }
    }
}
