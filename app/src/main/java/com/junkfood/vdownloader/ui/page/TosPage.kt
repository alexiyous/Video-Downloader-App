package com.junkfood.vdownloader.ui.page

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.junkfood.vdownloader.R

@Composable
fun TosPage(modifier: Modifier = Modifier, onContinue: () -> Unit) {
    BoxWithConstraints(
        modifier = modifier.background(color = MaterialTheme.colorScheme.onSurface)
    ) {
        // Set base dimensions for responsiveness
        val boxWidth = maxWidth
        val boxHeight = maxHeight

        Box(
            modifier =
                Modifier.requiredWidth(boxWidth)
                    .requiredHeight(boxHeight)
                    .background(color = MaterialTheme.colorScheme.surface)
                    .align(Alignment.Center)
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_tos_background),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )

            Box(
                modifier =
                    Modifier.align(Alignment.TopCenter)
                        .offset(y = boxHeight * 0.593f) // Adjust based on height
            ) {
                // Main title
                Text(
                    text = "Video Downloader",
                    style =
                        TextStyle(
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            lineHeight = 0.43.em,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                        ),
                    modifier = Modifier.align(Alignment.TopCenter),
                )

                // Subtitle
                Text(
                    text = "Best fastest video downloader app",
                    style =
                        TextStyle(
                            textAlign = TextAlign.Center,
                            lineHeight = 0.86.em,
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                        ),
                    modifier = Modifier.padding(top = 40.dp).align(Alignment.TopCenter),
                )
            }

            Box(modifier = Modifier.offset(y = (-60).dp).align(Alignment.BottomCenter)) {
                // Text Element with Link
                val annotatedString = buildAnnotatedString {
                    append("By continuing you accept our ")
                    pushStringAnnotation("TermsOfUse", "TermsOfUse")
                    withStyle(style = SpanStyle(textDecoration = TextDecoration.Underline)) {
                        append("Terms of Use")
                    }
                    pop()
                    append(" and \n")
                    pushStringAnnotation("PrivacyPolicy", "PrivacyPolicy")
                    withStyle(style = SpanStyle(textDecoration = TextDecoration.Underline)) {
                        append("Privacy Policy")
                    }
                    pop()
                    append(".")
                }

                ClickableText(
                    text = annotatedString,
                    onClick = { offset ->
                        annotatedString
                            .getStringAnnotations("TermsOfUse", offset, offset)
                            .firstOrNull()
                            ?.let {
                                // Handle the click event for "Terms of Use"
                                // For example, navigate to a Terms of Use screen or show a dialog
                                println("Terms of Use Clicked")
                            }

                        annotatedString
                            .getStringAnnotations("PrivacyPolicy", offset, offset)
                            .firstOrNull()
                            ?.let {
                                // Handle the click event for "Privacy Policy"
                                // For example, navigate to a Privacy Policy screen or show a dialog
                                println("Privacy Policy Clicked")
                            }
                    },
                    style =
                        TextStyle(
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            lineHeight = 1.3.em,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                        ),
                    modifier =
                        Modifier.align(Alignment.BottomCenter) // Align to the bottom center
                            .padding(bottom = 75.dp), // Add some space below the text
                )

                // Button Element
                Button(
                    onClick = { onContinue() },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    modifier =
                        Modifier.align(Alignment.BottomCenter) // Align to the bottom center
                            .width(boxWidth - 30.dp)
                            .height(60.dp),
                ) {
                    Text(
                        text = "Continue", // Button text
                        style =
                            TextStyle(
                                color = Color.Black, // Text color (adjust as needed)
                                textAlign = TextAlign.Center,
                                lineHeight = 0.55.em,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Black,
                            ),
                    )
                }
            }
        }
    }
}

@Preview(widthDp = 360, heightDp = 800)
@Composable
private fun GeneratedCodePreview() {
    TosPage(Modifier, onContinue = {})
}
