package com.example.obdanalyzer

import android.Manifest

object Constants {
    const val WRITE_EXTERNAL_STORAGE = Manifest.permission.WRITE_EXTERNAL_STORAGE
    val REQUIRED_PERMISSIONS = listOf(WRITE_EXTERNAL_STORAGE)
}
