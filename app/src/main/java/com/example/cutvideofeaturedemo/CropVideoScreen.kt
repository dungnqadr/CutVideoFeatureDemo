package com.example.cutvideofeaturedemo

import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.cutvideofeaturedemo.crop.cropVideoAndSave
import com.example.cutvideofeaturedemo.utils.getVideoDuration
import kotlinx.coroutines.launch
import kotlin.math.max

@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun CropVideoScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var pickedUri by remember { mutableStateOf<Uri?>(null) }
    var duration by remember { mutableStateOf(0f) }
    var isExporting by remember { mutableStateOf(false) }

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

        // When we has the video
        if (pickedUri != null) {
            Button(
                enabled = !isExporting,
                onClick = {
                    val uri = pickedUri ?: return@Button
                    isExporting = true
                    scope.launch {
                        val ok = cropVideoAndSave(
                            context = context,
                            source = uri,
                            desiredHeight = 720,
                            desiredWidth = 720,
                            x = 0,
                            y = 0
                        )
                        isExporting = false
                        Toast.makeText(
                            context, if (ok) "Đã lưu vào Gallery (Movies/CropVideo)" else "Failed",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            ) {
                Text(if (isExporting) "Exporting ... " else "Crop and Save")
            }

        }
    }
}