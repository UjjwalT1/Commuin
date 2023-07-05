package com.cyrax.commuin.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.core.view.ViewCompat
import com.cyrax.commuin.R

val RainForest = lightColorScheme(
    primary = CadetBlue,
    onPrimary = Color.Black,
    secondary = Greenery,
    onSecondary= Color.Black,
    tertiary = GreeneryDark,
    onTertiary = Color.Black,
    error = CadetBlue,
    onError = Color.White,
    background = CadetBlueTrans,
    outline = TransparentGreen,
    surface = TransparentGreenBefore,
    onSurface = TransparentGreenAfter,
)
private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = CadetBlue,
    onPrimary = Color.Black,
    secondary = Greenery,
    onSecondary= Color.Black,
    tertiary = GreeneryDark,
    onTertiary = Color.Black,
    error = CadetBlue,
    onError = Color.White,
    background = CadetBlueTrans
    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

@Composable
fun CommuinTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            (view.context as Activity).window.statusBarColor = colorScheme.primary.toArgb()
            ViewCompat.getWindowInsetsController(view)?.isAppearanceLightStatusBars = darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

//Fonts
val Jost = FontFamily(
    Font(R.font.jost_black, weight = FontWeight.Black),
    Font(R.font.jost_blackitalic, weight = FontWeight.Black, style = FontStyle.Italic),
    Font(R.font.jost_bold, weight = FontWeight.Bold),
    Font(R.font.jost_bolditalic, weight = FontWeight.Bold, style = FontStyle.Italic),
    Font(R.font.jost_extra_bold, weight = FontWeight.ExtraBold),
    Font(R.font.jost_extra_bolditalic, weight = FontWeight.ExtraBold, style = FontStyle.Italic),
    Font(R.font.jost_extra_light, weight = FontWeight.ExtraLight),
    Font(R.font.jost_extra_light_italic, weight = FontWeight.ExtraLight, style = FontStyle.Italic),
    Font(R.font.jost_italic,style = FontStyle.Italic),
    Font(R.font.jost_light, weight = FontWeight.Light),
    Font(R.font.jost_light_italic, weight = FontWeight.Light, style = FontStyle.Italic),
    Font(R.font.jost_medium, weight = FontWeight.Medium),
    Font(R.font.jost_medium_italic, weight = FontWeight.Medium, style = FontStyle.Italic),
    Font(R.font.jost_regular, weight = FontWeight.Normal),
    Font(R.font.jost_semi_bold, weight = FontWeight.SemiBold),
    Font(R.font.jost_thin, weight = FontWeight.Thin),
    Font(R.font.jost_thin_italic, weight = FontWeight.Thin, style = FontStyle.Italic),
)