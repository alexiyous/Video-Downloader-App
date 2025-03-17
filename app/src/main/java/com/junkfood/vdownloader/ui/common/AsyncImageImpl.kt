package com.junkfood.vdownloader.ui.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.DefaultAlpha
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImagePainter
import coil.imageLoader
import coil.request.ImageRequest
import com.junkfood.vdownloader.R

@Composable
fun AsyncImageImpl(
    model: Any?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    transform: (AsyncImagePainter.State) -> AsyncImagePainter.State =
        AsyncImagePainter.DefaultTransform,
    onState: ((AsyncImagePainter.State) -> Unit)? = null,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Fit,
    alpha: Float = DefaultAlpha,
    colorFilter: ColorFilter? = null,
    filterQuality: FilterQuality = DrawScope.DefaultFilterQuality,
    isPreview: Boolean = LocalInspectionMode.current,
) {
    val roundedModifier =
        modifier
            .clip(RoundedCornerShape(14.dp)) // Adjust the corner radius as needed
            .background(MaterialTheme.colorScheme.surface) // Optional: Add background color

    if (isPreview)
        Box(modifier = roundedModifier) {
            Image(
                painter = painterResource(R.drawable.sample3),
                contentDescription = contentDescription,
                modifier = Modifier.fillMaxSize(),
                alignment = alignment,
                contentScale = contentScale,
                alpha = alpha,
                colorFilter = colorFilter,
            )
        }
    else
        Box(modifier = roundedModifier) {
            coil.compose.AsyncImage(
                model =
                    ImageRequest.Builder(LocalContext.current).data(model).crossfade(true).build(),
                contentDescription = contentDescription,
                imageLoader = LocalContext.current.imageLoader,
                modifier = Modifier.fillMaxSize(),
                transform = transform,
                onState = onState,
                alignment = alignment,
                contentScale = contentScale,
                alpha = alpha,
                colorFilter = colorFilter,
                filterQuality = filterQuality,
            )
        }
}
