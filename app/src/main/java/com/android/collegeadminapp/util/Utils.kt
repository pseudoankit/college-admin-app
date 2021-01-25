package com.android.collegeadminapp.util

import android.content.Context
import android.graphics.Bitmap
import android.view.View
import android.widget.*
import com.android.collegeadminapp.R
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*

fun uploadTask(bitmap: Bitmap, storageReference: StorageReference, pathString: String): UploadTask {
    val bos: ByteArrayOutputStream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, 50, bos)
    val finalImage = bos.toByteArray()
    val filePath = storageReference.child(pathString).child("${finalImage}jpg")
    return filePath.putBytes(finalImage)
}

fun Context.progressBar(layout: LinearLayout): ProgressBar {
    ProgressBar(this).apply {
        this.layoutParams = LinearLayout.LayoutParams(
            150, 150
        )
        this.background = resources.getDrawable(R.drawable.custom_progressbar, null)
        layout.addView(this)
        this.hide()
        return this
    }
}

fun Context.spinner(spinnerItem: Array<String>, spinner: Spinner): Spinner {
    ArrayAdapter(this, android.R.layout.simple_spinner_item, spinnerItem).apply {
        this.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = this
    }
    return spinner
}

fun Context.toast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
}

fun getCurrentDate(): String {
    val calForDate = Calendar.getInstance()
    val currentDate = SimpleDateFormat("dd-MM-yy")
    return currentDate.format(calForDate.time)
}

fun getCurrentTime(): String {
    val calForTime = Calendar.getInstance()
    val currentTime = SimpleDateFormat("hh:mm a")
    return currentTime.format(calForTime.time)
}

fun View.show() {
    this.visibility = View.VISIBLE
}

fun View.hide() {
    this.visibility = View.GONE
}