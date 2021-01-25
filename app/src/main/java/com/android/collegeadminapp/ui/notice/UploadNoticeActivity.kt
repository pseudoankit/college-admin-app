package com.android.collegeadminapp.ui.notice

import android.content.Intent
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.ProgressBar
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.android.collegeadminapp.R
import com.android.collegeadminapp.databinding.ActivityUploadNoticeBinding
import com.android.collegeadminapp.util.*
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.IOException

class UploadNoticeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUploadNoticeBinding
    private lateinit var databaseReference: DatabaseReference       //Real time database reference
    private lateinit var storageReference: StorageReference        //storage reference
    private lateinit var downloadUrl: String
    private lateinit var progressBar: ProgressBar
    private var bitmap: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_upload_notice)

        init()

        binding.layoutSelectImage.setOnClickListener { openGallery() }

        binding.btnUploadNotice.setOnClickListener { buttonUploadNotice() }

    }

    private fun buttonUploadNotice() {
        when {
            binding.etNoticeTitle.text.isNullOrEmpty() -> {
                binding.etNoticeTitle.error = getString(R.string.error_required)
                binding.etNoticeTitle.requestFocus()
            }
            bitmap == null -> {
                lifecycleScope.launch { uploadNotice() }
            }
            else -> {
                progressBar.show()
                lifecycleScope.launch { convertBitmapAndUpload() }
            }
        }
    }

    private suspend fun convertBitmapAndUpload() {
        //upload image to firebase if all ok,converting bitmap to upload task to upload to firebase
        val bos: ByteArrayOutputStream = ByteArrayOutputStream()
        bitmap!!.compress(Bitmap.CompressFormat.JPEG, 50, bos)
        val finalImage = bos.toByteArray()
        val filePath = storageReference.child("${finalImage}jpg")
        val uploadTask = filePath.putBytes(finalImage)
        uploadTask.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                uploadTask.addOnSuccessListener {
                    filePath.downloadUrl.addOnSuccessListener { uri ->
                        downloadUrl = uri.toString()
                        Coroutines.io { uploadNotice() }
                    }
                }
            } else {
                progressBar.hide()
                toast("error")
            }
        }
    }

    private suspend fun uploadNotice() {
        //if title is generated successfully then uploading to firebase db and storage
        val uniqueKey = databaseReference.push().key
        val date = getCurrentDate()
        val time = getCurrentTime()
        val title = binding.etNoticeTitle.text!!.trim().toString()

        val noticeData = Notice(title, downloadUrl, date, time, uniqueKey!!)

        databaseReference.child(uniqueKey).setValue(noticeData)
            .addOnSuccessListener {
                progressBar.hide()
                toast("Notice Uploaded Successfully")
            }.addOnFailureListener {
                progressBar.hide()
                toast("Error${it.toString()}")
            }
    }

    private fun openGallery() {
        Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).apply {
            startActivityForResult(this, REQ_CODE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQ_CODE && resultCode == RESULT_OK) {
            val uri = data!!.data
            try {
                bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
            } catch (e: IOException) {
                e.printStackTrace()
            }
            binding.ivSelectedNotice.setImageBitmap(bitmap!!)
        }
    }

    private fun init() {
        databaseReference = FirebaseDatabase.getInstance().reference.child("Notice")
        storageReference = FirebaseStorage.getInstance().reference.child("Notice")
        progressBar = this.progressBar(binding.linearLayout)
    }

    companion object {
        private const val REQ_CODE = 1
    }
}