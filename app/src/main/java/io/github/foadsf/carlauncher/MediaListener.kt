package io.github.foadsf.carlauncher

import android.service.notification.NotificationListenerService

/**
 * Deliberately empty. Android only lets an app read other apps' media sessions
 * (MediaSessionManager.getActiveSessions) if it holds notification-listener access,
 * and that access is granted to a NotificationListenerService. No notification is read.
 */
class MediaListener : NotificationListenerService()
