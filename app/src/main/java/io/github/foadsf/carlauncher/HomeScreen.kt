package io.github.foadsf.carlauncher

import android.text.format.DateFormat
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.util.Date
import kotlin.math.ceil
import kotlin.math.max

private val TILE_MIN = 120.dp // the smallest touch target anywhere on screen
private val TILE_MIN_WIDTH = 110.dp // three columns on a 411 dp-wide phone
private val GAP = 12.dp
private val TileColor = Color(0xFF1C1C1C)
private val Dim = Color(0xFFB0B0B0)

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun HomeScreen(
    tiles: List<Tile>,
    nowPlaying: NowPlaying?,
    hasMediaAccess: Boolean,
    onTile: (android.content.Intent) -> Unit,
    onGrantMediaAccess: () -> Unit,
    onPrevious: () -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onOpenPlayer: (String) -> Unit,
) {
    MaterialTheme(colorScheme = darkColorScheme(background = Color.Black, surface = Color.Black)) {
        BoxWithConstraints(
            Modifier
                .fillMaxSize()
                .background(Color.Black)
                .safeDrawingPadding()
                .padding(GAP)
                // Exposes testTag as resource-id, so `uiautomator dump` can find tiles by name.
                .semantics { testTagsAsResourceId = true },
        ) {
            val media: @Composable () -> Unit = {
                MediaStrip(nowPlaying, hasMediaAccess, onGrantMediaAccess, onPrevious, onPlayPause, onNext, onOpenPlayer)
            }
            if (maxWidth > maxHeight) {
                Row(horizontalArrangement = Arrangement.spacedBy(GAP)) {
                    Column(
                        Modifier.weight(0.45f).fillMaxHeight(),
                        verticalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Clock()
                        media()
                    }
                    TileGrid(tiles, onTile, Modifier.weight(0.55f).fillMaxHeight())
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(GAP)) {
                    Clock()
                    media()
                    TileGrid(tiles, onTile, Modifier.weight(1f).fillMaxWidth())
                }
            }
        }
    }
}

@Composable
private fun Clock() {
    val context = LocalContext.current
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            now = System.currentTimeMillis()
            delay(1_000 - now % 1_000) // tick on the second boundary
        }
    }
    val date = Date(now)
    Column(Modifier.testTag("clock")) {
        // getTimeFormat follows the phone's 12/24-hour setting.
        Text(DateFormat.getTimeFormat(context).format(date), color = Color.White, fontSize = 88.sp, fontWeight = FontWeight.Bold, lineHeight = 92.sp)
        Text(java.text.DateFormat.getDateInstance(java.text.DateFormat.FULL).format(date), color = Dim, fontSize = 24.sp)
    }
}

@Composable
private fun MediaStrip(
    nowPlaying: NowPlaying?,
    hasAccess: Boolean,
    onGrant: () -> Unit,
    onPrevious: () -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onOpenPlayer: (String) -> Unit,
) {
    val shape = RoundedCornerShape(20.dp)
    if (!hasAccess) {
        Column(
            Modifier
                .testTag("grant_media_access")
                .fillMaxWidth()
                .heightIn(min = TILE_MIN)
                .clip(shape)
                .background(Color(0xFF3A2A00))
                .clickable(onClick = onGrant)
                .padding(20.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            Text(stringResource(R.string.grant_media_access), color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold)
            Text(stringResource(R.string.grant_media_access_detail), color = Dim, fontSize = 18.sp)
        }
        return
    }
    Column(
        Modifier.testTag("now_playing").fillMaxWidth().clip(shape).background(TileColor).padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        val title = nowPlaying?.title?.takeIf { it.isNotBlank() } ?: stringResource(R.string.nothing_playing)
        Column(
            Modifier
                .fillMaxWidth()
                .clickable(enabled = nowPlaying != null) { nowPlaying?.let { onOpenPlayer(it.packageName) } }
                .padding(horizontal = 8.dp),
        ) {
            Text(title, color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            val artist = nowPlaying?.artist.orEmpty()
            if (artist.isNotBlank()) Text(artist, color = Dim, fontSize = 20.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(GAP)) {
            val previous = stringResource(R.string.previous)
            val playPause = stringResource(R.string.play_pause)
            val next = stringResource(R.string.next)
            MediaButton(PreviousIcon, previous, "media_previous", onPrevious)
            MediaButton(if (nowPlaying?.isPlaying == true) PauseIcon else PlayIcon, playPause, "media_play_pause", onPlayPause)
            MediaButton(NextIcon, next, "media_next", onNext)
        }
    }
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.MediaButton(
    icon: ImageVector,
    description: String,
    tag: String,
    onClick: () -> Unit,
) {
    Column(
        Modifier
            .testTag(tag)
            .weight(1f)
            .height(TILE_MIN)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF2A2A2A))
            .clickable(onClick = onClick)
            .semantics { contentDescription = description },
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(64.dp))
    }
}

/** A grid that fills its space with as many columns as fit, never shrinking a tile below [TILE_MIN]. */
@Composable
private fun TileGrid(tiles: List<Tile>, onTile: (android.content.Intent) -> Unit, modifier: Modifier) {
    BoxWithConstraints(modifier) {
        val columns = max(1, ((maxWidth + GAP) / (TILE_MIN_WIDTH + GAP)).toInt())
        val rows = max(1, ceil(tiles.size / columns.toFloat()).toInt())
        val fill: Dp = (maxHeight - GAP * (rows - 1)) / rows
        val tileHeight = if (fill > TILE_MIN) fill else TILE_MIN
        // Scrolls only if the screen is too small for every tile at minimum size.
        Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(GAP)) {
            tiles.chunked(columns).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(GAP)) {
                    row.forEach { tile -> TileView(tile, onTile, Modifier.weight(1f).height(tileHeight)) }
                    repeat(columns - row.size) { Spacer(Modifier.weight(1f)) }
                }
            }
        }
    }
}

@Composable
private fun TileView(tile: Tile, onTile: (android.content.Intent) -> Unit, modifier: Modifier) {
    Column(
        modifier
            .testTag("tile_${tile.key}")
            .clip(RoundedCornerShape(20.dp))
            .background(TileColor)
            .clickable { onTile(tile.intent) }
            .padding(8.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        tile.icon?.let { Image(it, contentDescription = null, modifier = Modifier.size(56.dp)) }
        Spacer(Modifier.height(8.dp).width(1.dp))
        Text(
            tile.label,
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            lineHeight = 22.sp,
        )
    }
}
