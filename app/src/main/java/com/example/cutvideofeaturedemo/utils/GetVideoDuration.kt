package com.example.cutvideofeaturedemo.utils

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri

fun getVideoDuration(context: Context, uri: Uri): Float {
    val retriever = MediaMetadataRetriever()

    try {
        // Open the file in read mode
        // afd gives you a FileDescriptor, which is what the retriever needs to access the file.
        context.contentResolver.openAssetFileDescriptor(uri, "r")?.use { afd ->
            retriever.setDataSource(afd.fileDescriptor)
        }

        val ms = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            ?.toLongOrNull() ?: 0L

        return ms / 1000f

    } finally {
        // Release the retriever’s resources to prevent memory leaks.
        retriever.release()
    }
}