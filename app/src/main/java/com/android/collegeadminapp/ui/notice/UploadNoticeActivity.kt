package com.android.collegeadminapp.ui.notice

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.android.collegeadminapp.R
import com.android.collegeadminapp.databinding.ActivityUploadNoticeBinding
import com.android.collegeadminapp.util.*
import com.android.collegeadminapp.util.FireBaseConstants.FB_NOTICE
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
    private lateinit var dialog: Dialog
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
                dialog.showProgressDialog()
                lifecycleScope.launch { uploadNoticeToRTDB() }
            }
            else -> {
                dialog.showProgressDialog()
                lifecycleScope.launch { convertBitmapAndUpload() }
            }
        }
    }

    private suspend fun convertBitmapAndUpload() {
        uploadImageToFBStorage(bitmap!!, storageReference, dialog) { uri ->
            imageUrl = uri.toString()
            Coroutines.io {
                uploadNoticeToRTDB()
            }
        }
    }

    private fun uploadNoticeToRTDB() {
        //if title is generated successfully then uploading to firebase db and storage
        val uniqueKey = databaseReference.push().key
        val date = getCurrentDate()
        val time = getCurrentTime()
        val title = binding.etNoticeTitle.text!!.trim().toString()

        val noticeData = Notice(title, imageUrl, date, time, uniqueKey!!)

        databaseReference.child(uniqueKey).setValue(noticeData)
            .addOnSuccessListener {
                dialog.hideProgressDialog()
                toast(getString(R.string.uploaded_successfully))
                finish()
            }.addOnFailureListener {
                dialog.hideProgressDialog()
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
        databaseReference = FirebaseDatabase.getInstance().reference.child(FB_NOTICE)
        storageReference = FirebaseStorage.getInstance().reference.child(FB_NOTICE)
        dialog = Dialog(this)
    }

    companion object {
        private const val GALLERY_REQ_CODE = 1
    }
}