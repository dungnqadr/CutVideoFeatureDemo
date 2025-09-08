package com.example.cutvideofeaturedemo.preview

import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.example.cutvideofeaturedemo.utils.getVideoDuration
import kotlin.math.max

@OptIn(UnstableApi::class)
@Composable
fun VideoCropperScreen() {
    val context = LocalContext.current
    var pickedUri by remember { mutableStateOf<Uri?>(null) }
    var duration by remember { mutableStateOf(0f) }

    // Pick Video
    val pickVideo =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri ?: return@rememberLauncherForActivityResult
            pickedUri = uri

            val dSec = getVideoDuration(context, uri)
            duration = max(dSec, 1f)

            Toast.makeText(context, "Video dài: ${duration.toInt()}s", Toast.LENGTH_SHORT).show()

        }

    Column(Modifier.padding(16.dp)) {
        Button(onClick = { pickVideo.launch("video/*") }) { Text("Pick Video") }

        Spacer(Modifier.height(12.dp))

        if (pickedUri != null) {
            // ExoPlayer
            val exoPlayer = remember {
                ExoPlayer.Builder(context).build().apply {
                    setMediaItem(MediaItem.fromUri(pickedUri!!))
                    prepare()
                    playWhenReady = false
                }
            }

            val playerView = remember {
                PlayerView(context).apply {
                    useController = true
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                    player = exoPlayer
                }
            }

            // State crop box
            var offsetX by remember { mutableStateOf(200f) }
            var offsetY by remember { mutableStateOf(200f) }
            var boxWidth by remember { mutableStateOf(400f) }
            var boxHeight by remember { mutableStateOf(300f) }

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                // Video player
                AndroidView(
                    factory = { playerView },
                    modifier = Modifier.fillMaxSize()
                )

                // Crop box overlay
                Box(
                    modifier = Modifier
                        .offset { IntOffset(offsetX.toInt(), offsetY.toInt()) }
                        .size(boxWidth.dp, boxHeight.dp)
                        .border(3.dp, Color.Red, RoundedCornerShape(4.dp))
                        .pointerInput(Unit) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                offsetX += dragAmount.x
                                offsetY += dragAmount.y
                            }
                        }
                )

                // Button confirm crop
                Button(
                    onClick = {
                        // Lấy tọa độ crop (x, y, width, height)
                        Log.d("DEBUG", offsetX.toString())
                        Log.d("DEBUG", offsetY.toString())
                        Log.d("DEBUG", boxWidth.toString())
                        Log.d("DEBUG", boxHeight.toString())
                    },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                ) {
                    Text("Crop Video")
                }
            }
        }
    }
}
