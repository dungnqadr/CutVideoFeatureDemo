package com.example.cutvideofeaturedemo.crop

import ai.instavision.ffmpegkit.FFmpegKit
import ai.instavision.ffmpegkit.ReturnCode
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.cutvideofeaturedemo.utils.copyUriToCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream

@RequiresApi(Build.VERSION_CODES.Q)
suspend fun cropVideoAndSave(
    context: Context,
    source: Uri,
    x: Int,
    y: Int,
    desiredWidth: Int,
    desiredHeight: Int,
): Boolean =
    withContext(Dispatchers.IO) {
        Log.d("DEBUG", "Source: $source")
        val input = copyUriToCache(context, source, "crop_input_${System.currentTimeMillis()}.mp4")

        // Prepare the empty file
        val output = File(context.cacheDir, "crop_${System.currentTimeMillis()}.mp4")

        // Hardcode desired width and height
        val cropFilter = "crop=$desiredHeight:$desiredWidth:$x:$y"

        val cmd = "-i \"${input.absolutePath}\" -vf $cropFilter -c:a copy \"${output}\""
        val session = FFmpegKit.execute(cmd)

        // Return Code
        Log.d("DEBUG", "Return Code: ${ReturnCode.isSuccess(session.returnCode)}")
        if (!ReturnCode.isSuccess(session.returnCode)) return@withContext false

        val resolver = context.contentResolver
        val values = ContentValues().apply {
            put(MediaStore.Video.Media.DISPLAY_NAME, "crop_${System.currentTimeMillis()}.mp4")
            put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
            put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/CropVideo")
            put(MediaStore.Video.Media.IS_PENDING, 1)
        }

        val collection = MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        val targetUri = resolver.insert(collection, values) ?: return@withContext false

        resolver.openOutputStream(targetUri)?.use { outStream ->
            FileInputStream(output).use { inStream -> inStream.copyTo(outStream) }
        }

        values.clear()
        values.put(MediaStore.Video.Media.IS_PENDING, 0)
        resolver.update(targetUri, values, null, null)

        input.delete()
        output.delete()

        true
    }
