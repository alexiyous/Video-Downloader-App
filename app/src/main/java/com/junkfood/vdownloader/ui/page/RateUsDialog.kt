package com.junkfood.vdownloader.ui.page

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.junkfood.vdownloader.R

@Composable
fun RateUsDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    var rating by remember { mutableStateOf(5) }

    val screenWidth = LocalConfiguration.current.screenWidthDp.dp

    // Set dialog width based on the screen width (for mobile, tablet, or other)
    val dialogWidth =
        when {
            screenWidth < 600.dp -> screenWidth - 20.dp // Mobile devices (less than 600dp)
            screenWidth in 600.dp..1200.dp ->
                screenWidth - 60.dp // Tablets (between 600dp and 1200dp)
            else -> screenWidth - 100.dp // Larger devices (above 1200dp)
        }

    val logoImage =
        when {
            rating >= 4 -> R.drawable.star_5 // Replace with actual image resource
            rating >= 3 -> R.drawable.star_4
            rating >= 2 -> R.drawable.star_3
            rating >= 1 -> R.drawable.star_2
            else -> R.drawable.star_1 // Default logo image
        }
    Dialog(onDismissRequest = { onDismiss() }) {
        Box(
            modifier =
                Modifier.width(dialogWidth) // Set the dialog width based on the screen size
                    .clip(RoundedCornerShape(16.dp)) // Border radius
                    .background(MaterialTheme.colorScheme.background)
                    .padding(10.dp)
        ) {
            // Top-right Close Icon
            IconButton(
                modifier = Modifier.align(Alignment.TopEnd).size(30.dp),
                onClick = { onDismiss() },
            ) {
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = "Dismiss",
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }

            // Column to hold the logo, title, message, rating, image, and confirm button
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                // Logo image at the top (changes based on rating)
                Image(
                    modifier =
                        Modifier.padding(bottom = 15.dp)
                            .width(111.dp)
                            .height(104.dp), // Adjust based on design requirements
                    painter =
                        painterResource(id = logoImage), // Dynamically change logo based on rating
                    contentDescription = "Logo",
                )

                // Title text directly below the logo
                Text(
                    text = "We're working hard to make it better!",
                    style =
                        MaterialTheme.typography.labelLarge.copy(
                            fontSize = 16.sp, // Set font size to 30.sp
                            fontWeight = FontWeight.Bold, // Make text bold
                        ),
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Message content
                Text(
                    text = "We'd greatly appreciate if you can\n rate us.",
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 14.sp),
                    textAlign = TextAlign.Center,
                )

                // Rating IconButton group (5 stars)
                Row(
                    modifier = Modifier.padding(top = 16.dp), // Spacing between text and rating
                    horizontalArrangement = Arrangement.Center,
                ) {
                    (1..5).forEach { starIndex ->
                        IconButton(onClick = { rating = starIndex }) {
                            Image(
                                painter =
                                    painterResource(
                                        id =
                                            if (starIndex <= rating) R.drawable.ico_star
                                            else R.drawable.ico_star_faild
                                    ),
                                contentDescription = "Rating $starIndex",
                                modifier = Modifier.size(35.dp), // You can adjust the size as needed
                            )
                        }
                    }
                }

                // Image below the rating
                //                Image(
                //                    modifier = Modifier
                //                        .width(195.dp)
                //                        .height(36.dp), // Spacing between rating and image
                //                    painter = painterResource(id = R.drawable.img_proud), //
                // Replace with your image resource
                //                    contentDescription = "Image in Dialog",
                //                )

                Spacer(modifier = Modifier.height(20.dp))
                // Confirm Icon button below the image
                Box(
                    modifier =
                        Modifier.fillMaxWidth()
                            .height(60.dp) // Adjust the height as needed
                            .clip(RoundedCornerShape(30.dp)) // Apply border radius
                            .background(Color(0xFF00B06D)) // Set background color
                            .clickable {
                                // Call onConfirm with the selected rating
                                onConfirm()
                            },
                    contentAlignment = Alignment.Center,
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        // Text on the right
                        Text(
                            text = stringResource(R.string.up_rate_us),
                            fontSize = 22.sp,
                            style =
                                MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                            color = Color.White, // Ensure text color contrasts with background
                        )
                    }
                }
            }
        }
    }
}

// Preview for RateUsDialog
@Preview(showBackground = true)
@Composable
fun RateUsDialogPreview() {
    RateUsDialog(
        onDismiss = { /* Handle dismiss action */ },
        onConfirm = { /* Handle confirm action */ },
    )
}
