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
    private lateinit var progressBar: ProgressBar
    private lateinit var imageUrl: String
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
                progressBar.show()
                lifecycleScope.launch { convertBitmapAndUpload() }
            }
        }
    }

    private suspend fun convertBitmapAndUpload() {
        //todo simplify,coroutines
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

                        Coroutines.io { uploadImageToRTDB() }
                    }
                }
            } else {
                progressBar.hide()
                toast(getString(R.string.something_went_wrong))
            }
        }
    }

    private suspend fun uploadImageToRTDB() {
        val dbReference = databaseReference.child(category)
        val uniqueKey = dbReference.push().key
        dbReference.child(uniqueKey!!).setValue(imageUrl)
            .addOnSuccessListener {
                progressBar.hide()
                toast(getString(R.string.uploaded_successfully))
                finish()
            }.addOnFailureListener {
                progressBar.hide()
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
        val categories = resources.getStringArray(R.array.image_categories)
        this.spinner(
            categories,
            binding.spinnerImageCategory
        ).onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                category = categories[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

        }
    }

    private fun init() {
        databaseReference = FirebaseDatabase.getInstance().reference.child(RTDB_GALLERY)
        storageReference = FirebaseStorage.getInstance().reference.child(RTDB_GALLERY)
        progressBar = this.progressBar(binding.linearLayout)
    }

    companion object {
        private const val GALLERY_REQ_CODE = 1
        private const val RTDB_GALLERY = "Gallery"
    }

}