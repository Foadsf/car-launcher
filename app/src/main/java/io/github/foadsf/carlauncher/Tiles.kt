package io.github.foadsf.carlauncher

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import androidx.annotation.StringRes
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap

/** What the home screen offers, in display order. Edit this list to change the tiles. */
sealed interface TileSpec {
    val key: String
    @get:StringRes val label: Int

    /** Launches the first of [packages] that is installed and enabled; hidden if none is. */
    data class App(override val key: String, @StringRes override val label: Int, val packages: List<String>) : TileSpec

    /** Fires [action]; hidden if nothing on the phone handles it. */
    data class Action(override val key: String, @StringRes override val label: Int, val action: String) : TileSpec
}

// Every package named here must also appear in <queries> in AndroidManifest.xml.
val TILES: List<TileSpec> = listOf(
    TileSpec.App("comaps", R.string.tile_comaps, listOf("app.comaps.fdroid")),
    TileSpec.App("google_maps", R.string.tile_google_maps, listOf("com.google.android.apps.maps")),
    TileSpec.App("tomtom", R.string.tile_tomtom, listOf("com.tomtom.gplay.navapp.gofleet", "com.tomtom.gplay.navapp")),
    TileSpec.App("here", R.string.tile_here, listOf("com.here.app.maps")),
    TileSpec.App("music", R.string.tile_music, listOf("org.lineageos.twelve")),
    TileSpec.Action("phone", R.string.tile_phone, Intent.ACTION_DIAL),
    TileSpec.Action("settings", R.string.tile_settings, Settings.ACTION_SETTINGS),
)

class Tile(val key: String, val label: String, val intent: Intent, val icon: ImageBitmap?)

private const val ICON_PX = 192

fun resolveTiles(context: Context): List<Tile> {
    val pm = context.packageManager
    return TILES.mapNotNull { spec ->
        when (spec) {
            is TileSpec.App -> spec.packages.firstNotNullOfOrNull { pkg ->
                // Null for a package that is missing, disabled, or has no launcher activity.
                pm.getLaunchIntentForPackage(pkg)?.let { intent ->
                    Tile(spec.key, context.getString(spec.label), intent.newTask(), appIcon(pm, pkg))
                }
            }
            is TileSpec.Action -> {
                val intent = Intent(spec.action)
                pm.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)?.let { info ->
                    val icon = runCatching { info.loadIcon(pm).toBitmap(ICON_PX, ICON_PX).asImageBitmap() }.getOrNull()
                    Tile(spec.key, context.getString(spec.label), intent.newTask(), icon)
                }
            }
        }
    }
}

private fun Intent.newTask() = addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)

private fun appIcon(pm: PackageManager, pkg: String): ImageBitmap? =
    runCatching { pm.getApplicationIcon(pkg).toBitmap(ICON_PX, ICON_PX).asImageBitmap() }.getOrNull()
