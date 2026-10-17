package io.github.foadsf.carlauncher

import android.app.NotificationManager
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.KeyEvent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

data class NowPlaying(val title: String, val artist: String, val isPlaying: Boolean, val packageName: String)

/** Tracks the most relevant active media session and exposes it as Compose state. */
class NowPlayingSource(private val context: Context) {
    var hasAccess by mutableStateOf(false)
        private set
    var current by mutableStateOf<NowPlaying?>(null)
        private set

    private val sessions = context.getSystemService(MediaSessionManager::class.java)
    private val listener = ComponentName(context, MediaListener::class.java)
    private val handler = Handler(Looper.getMainLooper())
    private var controller: MediaController? = null
    private var listening = false

    private val callback = object : MediaController.Callback() {
        override fun onMetadataChanged(metadata: MediaMetadata?) = publish()
        override fun onPlaybackStateChanged(state: PlaybackState?) = publish()
        override fun onSessionDestroyed() = refresh()
    }

    private val sessionsChanged = MediaSessionManager.OnActiveSessionsChangedListener { pick(it.orEmpty()) }

    fun start() {
        hasAccess = context.getSystemService(NotificationManager::class.java)
            .isNotificationListenerAccessGranted(listener)
        if (!hasAccess || listening) return
        try {
            sessions.addOnActiveSessionsChangedListener(sessionsChanged, listener, handler)
            listening = true
            refresh()
        } catch (_: SecurityException) {
            hasAccess = false
        }
    }

    fun stop() {
        if (listening) sessions.removeOnActiveSessionsChangedListener(sessionsChanged)
        listening = false
        controller?.unregisterCallback(callback)
        controller = null
    }

    private fun refresh() = pick(
        try { sessions.getActiveSessions(listener) } catch (_: SecurityException) { emptyList() }
    )

    private fun pick(list: List<MediaController>) {
        val best = list.firstOrNull { it.playbackState?.state == PlaybackState.STATE_PLAYING } ?: list.firstOrNull()
        if (best?.sessionToken != controller?.sessionToken) {
            controller?.unregisterCallback(callback)
            controller = best
            best?.registerCallback(callback, handler)
        }
        publish()
    }

    private fun publish() {
        val c = controller
        val meta = c?.metadata
        current = c?.let {
            NowPlaying(
                title = meta?.getString(MediaMetadata.METADATA_KEY_TITLE).orEmpty(),
                artist = meta?.getString(MediaMetadata.METADATA_KEY_ARTIST)
                    ?: meta?.getString(MediaMetadata.METADATA_KEY_ALBUM_ARTIST).orEmpty(),
                isPlaying = it.playbackState?.state == PlaybackState.STATE_PLAYING,
                packageName = it.packageName,
            )
        }
    }

    fun previous() = controller?.transportControls?.skipToPrevious() ?: sendKey(KeyEvent.KEYCODE_MEDIA_PREVIOUS)
    fun next() = controller?.transportControls?.skipToNext() ?: sendKey(KeyEvent.KEYCODE_MEDIA_NEXT)

    fun playPause() {
        val c = controller
        when {
            c == null -> sendKey(KeyEvent.KEYCODE_MEDIA_PLAY) // resumes whichever player last had media focus
            current?.isPlaying == true -> c.transportControls.pause()
            else -> c.transportControls.play()
        }
    }

    private fun sendKey(code: Int) {
        val audio = context.getSystemService(AudioManager::class.java)
        audio.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, code))
        audio.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_UP, code))
    }

    /** Opens the system screen where access is granted: the per-app page, else the full list. */
    fun openGrantScreen() {
        val detail = Intent(Settings.ACTION_NOTIFICATION_LISTENER_DETAIL_SETTINGS)
            .putExtra(Settings.EXTRA_NOTIFICATION_LISTENER_COMPONENT_NAME, listener.flattenToString())
        val list = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
        for (intent in listOf(detail, list)) {
            try {
                context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                return
            } catch (_: ActivityNotFoundException) {
            }
        }
    }
}
