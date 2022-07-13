package it.vfsfitvnm.vimusic.ui.screens.settings

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import it.vfsfitvnm.route.RouteHandler
import it.vfsfitvnm.vimusic.R
import it.vfsfitvnm.vimusic.ui.components.TopAppBar
import it.vfsfitvnm.vimusic.ui.components.themed.TextCard
import it.vfsfitvnm.vimusic.ui.screens.*
import it.vfsfitvnm.vimusic.ui.styling.LocalColorPalette
import it.vfsfitvnm.vimusic.ui.styling.LocalTypography
import it.vfsfitvnm.vimusic.utils.LocalPreferences
import it.vfsfitvnm.vimusic.utils.isIgnoringBatteryOptimizations
import it.vfsfitvnm.vimusic.utils.semiBold


@ExperimentalAnimationApi
@Composable
fun OtherSettingsScreen() {
    val albumRoute = rememberAlbumRoute()
    val artistRoute = rememberArtistRoute()

    val scrollState = rememberScrollState()

    RouteHandler(listenToGlobalEmitter = true) {
        albumRoute { browseId ->
            AlbumScreen(
                browseId = browseId ?: error("browseId cannot be null")
            )
        }

        artistRoute { browseId ->
            ArtistScreen(
                browseId = browseId ?: error("browseId cannot be null")
            )
        }

        host {
            val context = LocalContext.current
            val colorPalette = LocalColorPalette.current
            val typography = LocalTypography.current
            val preferences = LocalPreferences.current

            var isIgnoringBatteryOptimizations by remember {
                mutableStateOf(context.isIgnoringBatteryOptimizations)
            }

            val activityResultLauncher =
                rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                    isIgnoringBatteryOptimizations = context.isIgnoringBatteryOptimizations
                }

            Column(
                modifier = Modifier
                    .background(colorPalette.background)
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(bottom = 72.dp)
            ) {
                TopAppBar(
                    modifier = Modifier
                        .height(52.dp)
                ) {
                    Image(
                        painter = painterResource(R.drawable.chevron_back),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(colorPalette.text),
                        modifier = Modifier
                            .clickable(onClick = pop)
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .size(24.dp)
                    )

                    BasicText(
                        text = "Other",
                        style = typography.m.semiBold
                    )

                    Spacer(
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .size(24.dp)
                    )
                }

                SettingsEntryGroupText(title = "SERVICE LIFETIME")

                SettingsEntry(
                    title = "Ignore battery optimizations",
                    isEnabled = !isIgnoringBatteryOptimizations,
                    text = if (isIgnoringBatteryOptimizations) {
                        "Already unrestricted"
                    } else {
                        "Disable background restrictions"
                    },
                    onClick = {
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return@SettingsEntry

                        @SuppressLint("BatteryLife")
                        val intent =
                            Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                                data = Uri.parse("package:${context.packageName}")
                            }

                        if (intent.resolveActivity(context.packageManager) != null) {
                            activityResultLauncher.launch(intent)
                        } else {
                            val fallbackIntent =
                                Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)

                            if (fallbackIntent.resolveActivity(context.packageManager) != null) {
                                activityResultLauncher.launch(fallbackIntent)
                            } else {
                                Toast.makeText(
                                    context,
                                    "Couldn't find battery optimization settings, please whitelist ViMusic manually",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                )

                SwitchSettingEntry(
                    title = "Invincible service",
                    text = "When turning off battery optimizations is not enough",
                    isChecked = preferences.isInvincibilityEnabled,
                    onCheckedChange = {
                        preferences.isInvincibilityEnabled = it
                    }
                )

                TextCard(icon = R.drawable.alert_circle) {
                    Title(text = "Service lifetime")
                    Text(text = "Some device manufacturers may have an aggressive policy against stopped foreground services - the media notification can disappear suddenly when paused.\nThe gentle approach consists in disabling battery optimizations - this is enough for some devices and ROMs.\nHowever, if it's not, you can make the service \"invincible\" - which should keep the service alive.")

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        Spacer(
                            modifier = Modifier
                                .height(32.dp)
                        )

                        Title(text = "Invincible service")
                        Text(text = "Since Android 12, this option works ONLY if battery optimizations are disabled for this application.")
                    }
                }
            }
        }
    }
}

