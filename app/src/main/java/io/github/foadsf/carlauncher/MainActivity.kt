package io.github.foadsf.carlauncher

import android.content.ActivityNotFoundException
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat

class MainActivity : ComponentActivity() {
    private var tiles by mutableStateOf(emptyList<Tile>())
    private lateinit var nowPlaying: NowPlayingSource

    // Install, uninstall, enable and disable all change which tiles should show.
    private val packageChanges = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) = refreshTiles()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Light icons on dark bars, including the 3-button navigation bar.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.BLACK),
        )
        super.onCreate(savedInstanceState)
        nowPlaying = NowPlayingSource(applicationContext)
        setContent {
            BackHandler { } // a home screen has nowhere to go back to
            HomeScreen(
                tiles = tiles,
                nowPlaying = nowPlaying.current,
                hasMediaAccess = nowPlaying.hasAccess,
                onTile = ::launch,
                onGrantMediaAccess = nowPlaying::openGrantScreen,
                onPrevious = nowPlaying::previous,
                onPlayPause = nowPlaying::playPause,
                onNext = nowPlaying::next,
                onOpenPlayer = { pkg -> packageManager.getLaunchIntentForPackage(pkg)?.let(::launch) },
            )
        }
    }

    override fun onStart() {
        super.onStart()
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addAction(Intent.ACTION_PACKAGE_CHANGED)
            addAction(Intent.ACTION_PACKAGE_REPLACED)
            addDataScheme("package")
        }
        ContextCompat.registerReceiver(this, packageChanges, filter, ContextCompat.RECEIVER_NOT_EXPORTED)
        refreshTiles()
    }

    override fun onResume() {
        super.onResume()
        nowPlaying.start() // re-checks access: the user may be returning from the grant screen
    }

    override fun onStop() {
        unregisterReceiver(packageChanges)
        nowPlaying.stop()
        super.onStop()
    }

    private fun refreshTiles() {
        tiles = resolveTiles(this)
    }

    private fun launch(intent: Intent) {
        try {
            startActivity(intent)
        } catch (_: ActivityNotFoundException) {
            refreshTiles() // the app vanished since the tiles were built
        }
    }
}
