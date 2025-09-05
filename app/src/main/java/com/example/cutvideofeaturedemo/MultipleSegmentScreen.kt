package com.example.cutvideofeaturedemo

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import com.example.cutvideofeaturedemo.utils.copyUriToCache
import com.example.cutvideofeaturedemo.utils.getVideoDuration
import cutMultipleSegments
import kotlinx.coroutines.launch
import kotlin.math.max

@Composable
fun MultiCutScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var cut1Start by remember { mutableStateOf(2f) }
    var cut1End by remember { mutableStateOf(3f) }
    var cut2Start by remember { mutableStateOf(5f) }
    var cut2End by remember { mutableStateOf(7f) }
    var result by remember { mutableStateOf<String?>(null) }

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

        Text("Cut 1: ${cut1Start}s → ${cut1End}s")
        RangeSlider(
            value = cut1Start..cut1End,
            onValueChange = { range ->
                cut1Start = range.start
                cut1End = range.endInclusive
            },
            valueRange = 0f..duration
        )

        Text("Cut 2: ${cut2Start}s → ${cut2End}s")
        RangeSlider(
            value = cut2Start..cut2End,
            onValueChange = { range ->
                cut2Start = range.start
                cut2End = range.endInclusive
            },
            valueRange = 0f..duration
        )

        Spacer(Modifier.height(16.dp))
        Button(onClick = {
            val uri = pickedUri ?: return@Button
            scope.launch {
                val cuts = listOf(
                    cut1Start to cut1End,
                    cut2Start to cut2End
                ).sortedBy { it.first } // sort theo start time


                val inputPath = copyUriToCache(context, uri, "crop_input_${System.currentTimeMillis()}.mp4")

                cutMultipleSegments(context, inputPath.absolutePath, cuts, duration) { success, output ->
                    result = if (success) "Xuất thành công: $output" else "Lỗi khi export"
                }
            }
        }) {
            Text("Export")
        }

        result?.let {
            Spacer(Modifier.height(8.dp))
            Text(it)
        }
    }
}
