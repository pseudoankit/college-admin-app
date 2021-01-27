package com.android.collegeadminapp.ui.notice

import android.content.Intent
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
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

class UploadNoticeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUploadNoticeBinding
    private lateinit var databaseReference: DatabaseReference       //Real time database reference
    private lateinit var storageReference: StorageReference        //storage reference
    private lateinit var imageUrl: String
    private lateinit var progressBar: ProgressBar
    private var bitmap: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_upload_notice)

        init()

        binding.layoutSelectImage.setOnClickListener { openGallery(GALLERY_REQ_CODE) }

        binding.btnUploadNotice.setOnClickListener { buttonUploadNotice() }

    }

    private fun buttonUploadNotice() {
        when {
            binding.etNoticeTitle.text.isNullOrEmpty() -> {
                binding.etNoticeTitle.error = getString(R.string.error_required)
                binding.etNoticeTitle.requestFocus()
            }
            bitmap == null -> {
                imageUrl = ""
                lifecycleScope.launch { uploadNoticeToRTDB() }
            }
            else -> {
                progressBar.show()
                lifecycleScope.launch { convertBitmapAndUpload() }
            }
        }
    }

    private suspend fun convertBitmapAndUpload() {
        //todo simplify coroutines
        //upload image to firebase if all ok,converting bitmap to upload task to upload to firebase
        val bos: ByteArrayOutputStream = ByteArrayOutputStream()
        bitmap!!.compress(Bitmap.CompressFormat.JPEG, 50, bos)
        val finalImage = bos.toByteArray()
        val storageFilePath = storageReference.child("${finalImage}jpg")
        val uploadTask = storageFilePath.putBytes(finalImage)
        uploadTask.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                uploadTask.addOnSuccessListener {
                    storageFilePath.downloadUrl.addOnSuccessListener { uri ->
                        imageUrl = uri.toString()
                        Coroutines.io { uploadNoticeToRTDB() }
                    }
                }
            } else {
                progressBar.hide()
                toast(getString(R.string.something_went_wrong))
            }
        }
    }

    private suspend fun uploadNoticeToRTDB() {
        //if title is generated successfully then uploading to firebase db and storage
        val uniqueKey = databaseReference.push().key
        val date = getCurrentDate()
        val time = getCurrentTime()
        val title = binding.etNoticeTitle.text!!.trim().toString()

        val noticeData = Notice(title, imageUrl, date, time, uniqueKey!!)

        databaseReference.child(uniqueKey).setValue(noticeData)
            .addOnSuccessListener {
                progressBar.hide()
                toast(getString(R.string.uploaded_successfully))
                finish()
            }.addOnFailureListener {
                progressBar.hide()
                toast(getString(R.string.something_went_wrong))
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GALLERY_REQ_CODE && resultCode == RESULT_OK) {
            bitmap = getSelectedGalleryBitmap(data!!.data)
            binding.ivSelectedNotice.setImageBitmap(bitmap!!)
        }
    }

    private fun init() {
        databaseReference = FirebaseDatabase.getInstance().reference.child(FB_CHILD_NOTICE)
        storageReference = FirebaseStorage.getInstance().reference.child(FB_CHILD_NOTICE)
        progressBar = this.progressBar(binding.linearLayout)
    }

    companion object {
        private const val GALLERY_REQ_CODE = 1
        const val FB_CHILD_NOTICE = "Notice"
    }
}