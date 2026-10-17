package io.github.foadsf.carlauncher

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp

// Material icon paths (Apache-2.0), inlined to avoid the multi-megabyte icons-extended library.
private fun icon(name: String, path: String) = ImageVector.Builder(name, 24.dp, 24.dp, 24f, 24f)
    .addPath(addPathNodes(path), fill = SolidColor(Color.White))
    .build()

val PlayIcon = icon("play", "M8,5v14l11,-7z")
val PauseIcon = icon("pause", "M6,19h4V5H6v14zm8,-14v14h4V5h-4z")
val NextIcon = icon("next", "M6,18l8.5,-6L6,6v12zM16,6v12h2V6h-2z")
val PreviousIcon = icon("previous", "M6,6h2v12H6zm3.5,6l8.5,6V6z")
