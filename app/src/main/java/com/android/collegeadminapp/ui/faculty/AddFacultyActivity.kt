package com.android.collegeadminapp.ui.faculty

import android.content.Intent
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.AdapterView
import android.widget.ProgressBar
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.android.collegeadminapp.R
import com.android.collegeadminapp.databinding.ActivityAddFacultyBinding
import com.android.collegeadminapp.util.*
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

class AddFacultyActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddFacultyBinding
    private lateinit var category: String
    private lateinit var progressBar: ProgressBar
    private lateinit var databaseReference: DatabaseReference       //Real time database reference
    private lateinit var storageReference: StorageReference
    private lateinit var downloadUrl: String
    private var bitmap: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_add_faculty)

        init()

        setSpinner()

        binding.ivFacultyImage.setOnClickListener { openGallery() }

        binding.btnUpdateFaculty.setOnClickListener { buttonUpdateFaculty() }
    }

    private fun buttonUpdateFaculty() {
        val name = binding.etFacultyName.text!!.trim().toString()
        val email = binding.etFacultyEmail.text!!.trim().toString()
        val post = binding.etFacultyPost.text!!.trim().toString()
        if (isValid(name, email, post)) {
            progressBar.show()
            lifecycleScope.launch { convertAndUploadData(name,email,post) }
        }
    }

    private suspend fun convertAndUploadData(name: String, email: String, post: String) {
        //todo simplify
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
                        downloadUrl = uri.toString()

                        Coroutines.io { uploadFacultyToRTDB(name, email, post) }
                    }
                }
            } else {
                progressBar.hide()
                toast("error")
            }
        }
    }

    private suspend fun uploadFacultyToRTDB(name: String, email: String, post: String) {
        val dbReference = databaseReference.child(category)
        val uniqueKey = dbReference.push().key
        val faculty = Faculty(name,email,post,downloadUrl,uniqueKey!!)
        dbReference.child(uniqueKey).setValue(faculty)
            .addOnSuccessListener {
                progressBar.hide()
                toast(getString(R.string.faculty_updated_successfully))
                finish()
            }.addOnFailureListener {
                progressBar.hide()
                toast(getString(R.string.something_went_wrong))
            }
    }

    private fun isValid(name: String, email: String, post: String): Boolean {
        when {
            name.isEmpty() -> {
                binding.etFacultyName.error = getString(R.string.error_required)
                binding.etFacultyName.requestFocus()
                return false
            }
            email.isEmpty() -> {
                binding.etFacultyEmail.error = getString(R.string.error_required)
                binding.etFacultyEmail.requestFocus()
                return false
            }
            post.isEmpty() -> {
                binding.etFacultyPost.error = getString(R.string.error_required)
                binding.etFacultyPost.requestFocus()
                return false
            }
            category == getString(R.string.select_category) -> {
                toast("Please select any category")
                return false
            }
            bitmap == null -> {
                toast("Please select an image")
                return false
            }
        }
        return true
    }

    private fun openGallery() {
        Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).apply {
            startActivityForResult(this, GALLERY_REQ_CODE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GALLERY_REQ_CODE && resultCode == RESULT_OK) {
            bitmap = getSelectedGalleryBitmap(data!!.data)
            binding.ivFacultyImage.setImageBitmap(bitmap!!)
        }
    }

    private fun setSpinner() {
        val categories = resources.getStringArray(R.array.streams)
        this.spinner(
            categories,
            binding.spinnerTeacherStream
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
        databaseReference = FirebaseDatabase.getInstance().reference.child(FB_CHILD_FACULTY)
        storageReference = FirebaseStorage.getInstance().reference.child(FB_CHILD_FACULTY)
        progressBar = this.progressBar(binding.linearLayout)
    }

    companion object {
        private const val GALLERY_REQ_CODE = 1
        const val FB_CHILD_FACULTY = "Faculty"
    }
}