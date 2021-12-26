package eu.sebaro.uller

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColors(
    primary = Blue300,
    primaryVariant = Blue300,
    onPrimary = Color.Black,
    secondary = Blue300,
    secondaryVariant = Blue200,
    onSecondary = Color.Black,
    error = Red800,
)

private val DarkColors = darkColors(
    primary = Red300,
    primaryVariant = Red700,
    onPrimary = Color.Black,
    secondary = Red300,
    onSecondary = Color.Black,
    error = Red200,
)

@Composable
fun UllerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colors = if (darkTheme) DarkColors else LightColors,
        shapes = UllerShapes,
        content = content
    )
}

fun getCategoryImage(product: Product): Int {
    when (product.category.lowercase()) {
        "ps5" -> return R.drawable.ic_ps5
        "xbox" -> return R.drawable.ic_xbox
        "gpu" -> return R.drawable.ic_gpu
        "nintendoswitch" -> return R.drawable.ic_nintendoswitch
    }
    return R.drawable.ic_game_console
}


