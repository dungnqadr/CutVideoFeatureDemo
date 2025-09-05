package com.example.cutvideofeaturedemo.utils

import ai.instavision.ffmpegkit.FFmpegKit
import ai.instavision.ffmpegkit.ReturnCode
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.Q)
suspend fun cutAndSaveToGallery(
    context: Context,
    source: Uri,
    startSec: Float,
    endSec: Float
): Boolean =
    withContext(Dispatchers.IO) {
        val input = copyUriToCache(context, source, "input_${System.currentTimeMillis()}.mov")
        val output = File(context.cacheDir, "cut_${System.currentTimeMillis()}.mp4")

        // Session
        val start = String.format(Locale.US,"%.3f", startSec)
        val end = String.format(Locale.US,"%.3f", endSec)

        val cmd = "-y -i \"${input.absolutePath}\" -ss $start -to $end -map 0:v -map 0:a -c:v mpeg4 \"${output.absolutePath}\""

        val session = FFmpegKit.execute(cmd)

        // Return Code
        if (!ReturnCode.isSuccess(session.returnCode)) return@withContext false

        val resolver = context.contentResolver
        val values = ContentValues().apply {
            put(MediaStore.Video.Media.DISPLAY_NAME, "cut_${System.currentTimeMillis()}.mp4")
            put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
            put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/CutVideo")
            put(MediaStore.Video.Media.IS_PENDING, 1)
        }


        val collection = MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        val targetUri = resolver.insert(collection, values) ?: return@withContext false

        resolver.openOutputStream(targetUri)?.use { outStream ->
            FileInputStream(output).use { inStream -> inStream.copyTo(outStream) }
        }

        values.clear()
        values.put(MediaStore.Video.Media.IS_PENDING, 0)
        resolver.update(targetUri, values, null,null)

        input.delete()
        output.delete()

        true
    }
