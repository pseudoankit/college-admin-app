package com.android.collegeadminapp.util

import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.view.View
import android.widget.*
import androidx.core.content.res.ResourcesCompat
import com.android.collegeadminapp.R
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
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
    //Todo progressbar dynamically without view
    ProgressBar(this).apply {
        this.layoutParams = LinearLayout.LayoutParams(
            150, 150
        )
        this.background =
            ResourcesCompat.getDrawable(resources, R.drawable.custom_progressbar, null)
        layout.addView(this)
        this.hide()
        return this
    }
}

fun Context.getSelectedGalleryBitmap(uri: Uri?): Bitmap? {
    //todo remove deprecation
    var bitmap : Bitmap? = null
    try {
        bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
    } catch (e: IOException) {
        e.printStackTrace()
    }
    return bitmap
}

fun Context.getPdfName(pdfData: Uri?): String {
    var pdfName = ""
    if (pdfData!!.toString().startsWith("content://")) {
        var cursor: Cursor? = null
        try {
            cursor = this.contentResolver.query(pdfData, null, null, null, null)
            if (cursor != null && cursor.moveToFirst()) {
                pdfName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
            }
            cursor!!.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    } else if (pdfData.toString().startsWith("file://")) {
        pdfName = File(pdfData.toString()).name
    }
    return pdfName
}

fun openGallery() {
    //todo gallery
//    Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).apply {
//        startActivityForResult(this,8)
//    }
}

fun Context.spinner(spinnerItem: Array<String>, spinner: Spinner): Spinner {
    //Todo simplify spinner
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