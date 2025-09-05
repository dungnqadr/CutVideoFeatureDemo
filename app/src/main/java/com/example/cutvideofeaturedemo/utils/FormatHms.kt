package com.example.cutvideofeaturedemo.utils

import android.annotation.SuppressLint


@SuppressLint("DefaultLocale")
fun formatHms(sec: Float): String {
    val total = sec.toLong()
    val h = total / 3600
    val m = (total % 3600) / 60
    val s = total % 60
    return if (h > 0) String.format("%d:%02d:%02d", h, m, s)
    else String.format("%02d:%02d", m,s)
}