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
import androidx.compose.material3.RangeSlider
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
import com.example.cutvideofeaturedemo.utils.cutAndSaveToGallery
import com.example.cutvideofeaturedemo.utils.formatHms
import com.example.cutvideofeaturedemo.utils.getVideoDuration
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.min

@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun CutVideoScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var pickedUri by remember { mutableStateOf<Uri?>(null) }
    var range by remember { mutableStateOf(0f..5f) }
    var duration by remember { mutableStateOf(0f) }
    var isExporting by remember { mutableStateOf(false) }

    // Pick Video
    val pickVideo =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri ?: return@rememberLauncherForActivityResult
            pickedUri = uri

            val dSec = getVideoDuration(context, uri)
            duration = max(dSec, 1f)

            range = 0f..min(duration, 5f)


            Toast.makeText(context, "Video dài: ${duration.toInt()}s", Toast.LENGTH_SHORT).show()

        }

    Column(Modifier.padding(16.dp)) {
        Button(onClick = { pickVideo.launch("video/*") }) { Text("Chọn video") }

        Spacer(Modifier.height(12.dp))

        if (pickedUri != null && duration > 0f) {
            Text("Khoảng cắt: ${formatHms(range.start)} → ${formatHms(range.endInclusive)}")
            RangeSlider(
                value = range,
                onValueChange = { r ->
                    val minGap = 1f // tối thiểu 1 giây
                    val clamped = r.start.coerceIn(0f, duration)..
                            r.endInclusive.coerceIn(0f, duration)

                    range = if (clamped.endInclusive - clamped.start < minGap) {
                        val start = clamped.start
                        (start)..min(duration, start + minGap)
                    } else clamped
                },
                valueRange = 0f..duration,
                steps = max(0, duration.toInt() - 1)
            )

            Spacer(Modifier.height(12.dp))

            Button(
                enabled = !isExporting,
                onClick = {
                    val uri = pickedUri ?: return@Button
                    isExporting = true
                    scope.launch {
                        val ok = cutAndSaveToGallery(
                            context = context,
                            source = uri,
                            startSec = range.start,
                            endSec = range.endInclusive
                        )
                        isExporting = false
                        Toast.makeText(
                            context, if (ok) "Đã lưu vào Gallery (Movies/CutVideo)" else "Xuất thất bại",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            ) {
                Text(if (isExporting) "Exporting ... " else "Cut And Save")
            }

        }
    }
}