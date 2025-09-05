package com.example.cutvideofeaturedemo.utils

import ai.instavision.ffmpegkit.FFmpegKit
import ai.instavision.ffmpegkit.ReturnCode
import android.content.Context
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

suspend fun cutVideo (
    context: Context,
    startSec: Int,
    endSec: Int,
    inputPath: String,
    outputPath: String
) {
    withContext(Dispatchers.IO) {
        val cmd = "-i $inputPath -ss $startSec -to $endSec -c copy $outputPath"
        val session = FFmpegKit.execute(cmd)

        if (ReturnCode.isSuccess(session.returnCode)) {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "OK: $outputPath", Toast.LENGTH_LONG).show()
            }
        } else {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Error: ${session.failStackTrace}", Toast.LENGTH_LONG).show()
            }
        }
    }
}