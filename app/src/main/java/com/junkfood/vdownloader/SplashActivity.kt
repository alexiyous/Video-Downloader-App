package com.junkfood.vdownloader

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.*
import kotlinx.coroutines.delay

class SplashActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set content using Jetpack Compose
        setContent {
            SplashScreen {
                navigateToMainActivity() // Navigate to the main activity when progress is complete
            }
        }
    }

    // Function to navigate to the main activity
    private fun navigateToMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finish() // Close the splash activity
    }
}

@Composable
fun SplashScreen(navigateToMain: () -> Unit) {
    val progress = remember { mutableStateOf(50) } // Track progress
    val scope = rememberCoroutineScope()

    // Start the progress simulation when the composable is first displayed
    LaunchedEffect(Unit) { simulateProgress(scope, progress, navigateToMain) }

    // Box with background color from MaterialTheme
    Box(
        modifier =
            Modifier.fillMaxSize()
                .background(
                    MaterialTheme.colorScheme.background
                ), // Background set to MaterialTheme's background
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp), // Optional padding for better layout
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Top Image
            Image(
                painter = painterResource(id = R.drawable.ico_splash_log),
                contentDescription = "Logo",
                modifier =
                    Modifier.align(Alignment.CenterHorizontally).padding(bottom = 16.dp).size(60.dp),
            )

            // Middle Texts and ProgressBar
            // Middle Texts and ProgressBar
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text =
                        stringResource(
                            R.string.video_downloader
                        ), // Replace with stringResource if necessary
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground, // Text color for the background
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text =
                        stringResource(
                            R.string.splash_explain
                        ), // Replace with stringResource if necessary
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface, // Text color for the background
                )
                Spacer(modifier = Modifier.height(25.dp))

                // Box to apply border-radius to the progress bar
                Box(
                    modifier =
                        Modifier.width(162.dp)
                            .height(6.dp)
                            .clip(RoundedCornerShape(50)) // Set the corner radius here
                            .background(
                                MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f)
                            ) // Background of progress bar
                ) {
                    LinearProgressIndicator(
                        progress = progress.value / 100f,
                        modifier =
                            Modifier.fillMaxSize()
                                .clip(RoundedCornerShape(50)), // Set corner radius here
                        color = Color(0xFF00B06D), // Set active color to #00B06D
                        backgroundColor =
                            Color.Transparent, // Ensure no background color on progress bar
                    )
                }
            }
        }
    }
}

// Function to simulate the progress with random updates
private suspend fun simulateProgress(
    scope: CoroutineScope,
    progress: MutableState<Int>,
    navigateToMain: () -> Unit,
) {
    var currentStep = 0
    val maxSteps = 4 // Number of random progress updates

    while (currentStep < maxSteps) {
        delay(500) // Wait for 1 second
        val randomProgress = (progress.value + (10..30).random()).coerceAtMost(100)
        progress.value = randomProgress
        currentStep++
    }

    navigateToMain() // Navigate to the main activity when done
}

@Preview
@Composable
fun PreviewSplashScreen() {
    SplashScreen {}
}
