package com.android.collegeadminapp.ui.notice

import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import androidx.databinding.DataBindingUtil
import com.android.collegeadminapp.R
import com.android.collegeadminapp.databinding.ActivityUploadNoticeBinding
import com.android.collegeadminapp.util.getCurrentDate
import com.android.collegeadminapp.util.getCurrentTime
import com.android.collegeadminapp.util.toast
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.ByteArrayOutputStream
import java.io.IOException

class UploadNoticeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUploadNoticeBinding
    private var bitmap: Bitmap? = null
    private var databaseReference: DatabaseReference? = null        //Real time database reference
    private var storageReference: StorageReference? = null          //storage reference
    private var downloadUrl: String? = null
    private var progressDialog : ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_upload_notice)

        init()

        binding.layoutAddImage.setOnClickListener { openGallery() }

        binding.btnUploadNotice.setOnClickListener { uploadNoticeToFB() }

    }

    private fun uploadNoticeToFB() {
        when {
            binding.etNoticeTitle.text.isNullOrEmpty() -> {
                binding.etNoticeTitle.error = getString(R.string.error_required)
                binding.etNoticeTitle.requestFocus()
            }
            bitmap == null -> {
                uploadDataToFb()
            }
            else -> {
                convertBitmapAndUpload()
            }
        }
    }

    private fun convertBitmapAndUpload() {
        progressDialog!!.setMessage("Uploading...")
        progressDialog!!.show()

        //upload image to firebase if all ok,converting bitmap to upload task to upload to firebase
        val bos: ByteArrayOutputStream = ByteArrayOutputStream()
        bitmap!!.compress(Bitmap.CompressFormat.JPEG, 50, bos)
        val finalImage = bos.toByteArray()
        val filePath = storageReference!!.child("Notice").child("${finalImage}jpg")
        val uploadTask = filePath.putBytes(finalImage)
        uploadTask.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                uploadTask.addOnSuccessListener {
                    filePath.downloadUrl.addOnSuccessListener { uri ->
                        downloadUrl = uri.toString()
                        uploadDataToFb()
                    }
                }
            } else {
                progressDialog!!.dismiss()
                toast("error")
            }
        }
    }

    private fun uploadDataToFb() {
        //if title is generated successfully then uploading to firebase db and storage
        databaseReference = databaseReference!!.child("Notice")
        val uniqueKey = databaseReference!!.push().key
        val date = getCurrentDate()
        val time = getCurrentTime()
        val title = binding.etNoticeTitle.text!!.trim().toString()

        val noticeData = Notice(title, downloadUrl!!, date, time, uniqueKey!!)

        databaseReference!!.child(uniqueKey).setValue(noticeData)
            .addOnSuccessListener {
                progressDialog!!.dismiss()
                toast("Notice Uploaded")
            }.addOnFailureListener {
                progressDialog!!.dismiss()
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
            binding.ivNotice.setImageBitmap(bitmap!!)
        }
    }

    private fun init() {
        databaseReference = FirebaseDatabase.getInstance().reference
        storageReference = FirebaseStorage.getInstance().reference
        progressDialog = ProgressDialog(this)
    }

    companion object {
        private const val REQ_CODE = 1
    }
}