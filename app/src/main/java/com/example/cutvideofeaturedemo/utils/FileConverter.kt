package com.example.cutvideofeaturedemo.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

suspend fun copyUriToCache(context: Context, uri: Uri, nameHint: String): File =
    withContext(Dispatchers.IO) {
        val out = File(context.cacheDir, nameHint)
        context.contentResolver.openInputStream(uri)!!.use { input ->
            FileOutputStream(out).use { input.copyTo(it) }
        }
        Log.d("DEBUG", out.toString())
        out
    }