import ai.instavision.ffmpegkit.FFmpegKit
import ai.instavision.ffmpegkit.ReturnCode
import android.content.Context
import java.io.File

fun cutMultipleSegments(
    context: Context,
    inputPath: String,

    // List of abandon cut
    cuts: List<Pair<Float, Float>>,
    duration: Float,
    onDone: (Boolean, String?) -> Unit
) {
    val cacheDir = context.cacheDir
    val segments = mutableListOf<String>()
    var currentStart = 0f
    var index = 0

    cuts.forEach { cut ->
        val (cutStart, cutEnd) = cut
        if (currentStart < cutStart) {
            val filePath = File(cacheDir, "part${index++}.mp4").absolutePath
            val cmd = "-i $inputPath -ss $currentStart -to $cutStart -c copy $filePath"
            FFmpegKit.execute(cmd)
            segments.add(filePath)
        }
        currentStart = cutEnd
    }

    // đoạn cuối từ lastCutEnd → duration
    if (currentStart < duration) {
        val filePath = File(cacheDir, "part${index++}.mp4").absolutePath
        val cmd = "-i $inputPath -ss $currentStart -to $duration -c copy $filePath"
        FFmpegKit.execute(cmd)
        segments.add(filePath)
    }

    // concat file list
    val listFile = File(cacheDir, "list.txt")
    listFile.writeText(segments.joinToString("\n") { "file '$it'" })

    val output = File(cacheDir, "merged.mp4").absolutePath
    val cmdMerge = "-f concat -safe 0 -i ${listFile.absolutePath} -c copy $output"
    val session = FFmpegKit.execute(cmdMerge)

    if (ReturnCode.isSuccess(session.returnCode)) {
        onDone(true, output)
    } else {
        onDone(false, null)
    }
}