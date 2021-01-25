package com.android.collegeadminapp.ui

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.AdapterView
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.android.collegeadminapp.R
import com.android.collegeadminapp.databinding.ActivityUploadImageBinding
import com.android.collegeadminapp.util.*
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.ByteArrayOutputStream
import java.io.IOException

class UploadImageActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUploadImageBinding
    private lateinit var category: String
    private lateinit var progressBar: ProgressBar
    private lateinit var databaseReference: DatabaseReference       //Real time database reference
    private lateinit var storageReference: StorageReference
    private lateinit var downloadUrl: String
    private var bitmap: Bitmap? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_upload_image)

        init()

        setSpinner()

        binding.layoutSelectImage.setOnClickListener { openGallery() }

        binding.btnUploadImage.setOnClickListener { buttonUploadImage() }

    }


    private fun buttonUploadImage() {
        when {
            bitmap == null -> {
                toast("Please Select an image")
            }
            category == getString(R.string.select_category) -> {
                toast("Please Select Image Category")
            }
            else -> {
                progressBar.show()
                convertBitmapAndUpload()
            }
        }
    }

    private fun convertBitmapAndUpload() {
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
                        uploadImage()
                    }
                }
            } else {
                progressBar.hide()
                toast("error")
            }
        }
    }

    private fun uploadImage() {
        val dbReference = databaseReference.child(category)
        val uniqueKey = dbReference.push().key
        dbReference.child(uniqueKey!!).setValue(downloadUrl)
            .addOnSuccessListener {
                progressBar.hide()
                toast("Image Uploaded Successfully")
            }.addOnFailureListener {
                progressBar.hide()
                toast("error ${it.toString()}")
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
                TODO("Not yet implemented")
            }

        }
    }

    private fun init() {
        databaseReference = FirebaseDatabase.getInstance().reference.child("Gallery")
        storageReference = FirebaseStorage.getInstance().reference.child("Gallery")
        progressBar = this.progressBar(binding.rootLayout)
    }

    companion object {
        private const val REQ_CODE = 1

    }

}