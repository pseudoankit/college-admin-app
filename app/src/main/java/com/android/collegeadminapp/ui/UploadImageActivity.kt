package com.android.collegeadminapp.ui

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.android.collegeadminapp.R
import com.android.collegeadminapp.databinding.ActivityUploadImageBinding
import com.android.collegeadminapp.util.*
import com.android.collegeadminapp.util.FireBaseConstants.FB_GALLERY
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

class UploadImageActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUploadImageBinding
    private lateinit var databaseReference: DatabaseReference       //Real time database reference
    private lateinit var storageReference: StorageReference
    private lateinit var category: String
    private lateinit var dialog: Dialog
    private var bitmap: Bitmap? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_upload_image)

        init()

        setSpinner()

        binding.layoutSelectImage.setOnClickListener { openGallery(GALLERY_REQ_CODE) }

        binding.btnUploadImage.setOnClickListener { buttonUploadImage() }

    }


    private fun buttonUploadImage() {
        when {
            bitmap == null -> {
                toast(getString(R.string.please_select_image))
            }
            category == getString(R.string.select_category) -> {
                toast(getString(R.string.please_select_image_category))
            }
            else -> {
                dialog.showProgressDialog()
                lifecycleScope.launch { convertBitmapAndUpload() }
            }
        }
    }

    private suspend fun convertBitmapAndUpload() {
        uploadImageToFBStorage(bitmap!!, storageReference, dialog) { uri ->
            val imageUrl = uri.toString()
            Coroutines.io {
                uploadImageToRTDB(imageUrl)
            }
        }
    }

    private fun uploadImageToRTDB(imageUrl: String) {
        val dbReference = databaseReference.child(category)
        val uniqueKey = dbReference.push().key
        dbReference.child(uniqueKey!!).setValue(imageUrl)
            .addOnSuccessListener {
                dialog.hideProgressDialog()
                toast(getString(R.string.uploaded_successfully))
                finish()
            }.addOnFailureListener {
                dialog.hideProgressDialog()
                toast("getString(R.string.something_went_wrong)")
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GALLERY_REQ_CODE && resultCode == RESULT_OK) {
            bitmap = getSelectedGalleryBitmap(data!!.data)
            binding.ivGallery.setImageBitmap(bitmap!!)
        }
    }

    private fun setSpinner() {
        dialog.showSpinner(
            resources.getStringArray(R.array.image_categories),
            binding.spinnerImageCategory){
            category = it
        }
    }

    private fun init() {
        databaseReference = FirebaseDatabase.getInstance().reference.child(FB_GALLERY)
        storageReference = FirebaseStorage.getInstance().reference.child(FB_GALLERY)
        dialog = Dialog(this)
    }

    companion object {
        private const val GALLERY_REQ_CODE = 1
    }

}