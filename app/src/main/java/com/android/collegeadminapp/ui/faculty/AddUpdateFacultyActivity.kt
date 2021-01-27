package com.android.collegeadminapp.ui.faculty

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.android.collegeadminapp.R
import com.android.collegeadminapp.databinding.ActivityAddUpdateFacultyBinding
import com.android.collegeadminapp.util.*
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

class AddUpdateFacultyActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddUpdateFacultyBinding
    private lateinit var progressBar: ProgressBar
    private lateinit var databaseReference: DatabaseReference       //Real time database reference
    private lateinit var storageReference: StorageReference
    private lateinit var department: String
    private lateinit var downloadUrl: String
    private var bitmap: Bitmap? = null
    private var isAdd = true
    private lateinit var faculty: Faculty

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_add_update_faculty)

        init()

        setSpinner()

        binding.ivFacultyImage.setOnClickListener { openGallery() }

        binding.btnUpdateFaculty.setOnClickListener { buttonUpdateFaculty() }

        binding.btnDeleteFaculty.setOnClickListener { deleteFaculty() }
    }

    private fun deleteFaculty() {
        databaseReference.child(faculty.category).child(faculty.key).removeValue()
            .addOnCompleteListener{
                toast(getString(R.string.teacher_deleted_successfully))
                finish()
            }.addOnFailureListener {
                toast(getString(R.string.something_went_wrong))
            }
    }

    private fun buttonUpdateFaculty() {
        val name = binding.etFacultyName.text!!.trim().toString()
        val email = binding.etFacultyEmail.text!!.trim().toString()
        val post = binding.etFacultyPost.text!!.trim().toString()
        if (isValid(name, email, post)) {
            progressBar.show()
            if (!isAdd && bitmap == null) {
                lifecycleScope.launch { uploadFacultyToRTDB(name, email, post) }
            } else {
                lifecycleScope.launch { convertAndUploadData(name, email, post) }
            }

        }
    }

    private suspend fun convertAndUploadData(name: String, email: String, post: String) {
        //todo simplify
        //converting bitmap to upload task then, upload image to firebase storage
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
                toast(getString(R.string.something_went_wrong))
            }
        }
    }

    private suspend fun uploadFacultyToRTDB(name: String, email: String, post: String) {
        if (isAdd) {
            val dbReference = databaseReference.child(department)
            val uniqueKey = dbReference.push().key
            val faculty = Faculty(name, email, post, downloadUrl, uniqueKey!!,department)
            dbReference.child(uniqueKey).setValue(faculty)
                .addOnSuccessListener {
                    progressBar.hide()
                    toast(getString(R.string.faculty_updated_successfully))
                    finish()
                }.addOnFailureListener {
                    progressBar.hide()
                    toast(getString(R.string.something_went_wrong))
                }
        } else {
            val data: HashMap<String, Any> = HashMap()
            data["name"] = name
            data["email"] = email
            data["post"] = post
            if (bitmap == null) {
                data["image"] = faculty.image
            } else {
                data["image"] = downloadUrl
            }
            databaseReference.child(faculty.category).child(faculty.key).updateChildren(data)
                .addOnSuccessListener {
                    progressBar.hide()
                    toast(getString(R.string.faculty_updated_successfully))
                    finish()
                }.addOnFailureListener {
                    progressBar.hide()
                    toast(getString(R.string.something_went_wrong))
                }
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
            isAdd && department == getString(R.string.select_category) -> {
                toast("Please select any category")
                return false
            }
            isAdd && bitmap == null -> {
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
        val categories = resources.getStringArray(R.array.departments)
        this.spinner(
            categories,
            binding.spinnerTeacherDepartments
        ).onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                department = categories[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

        }
    }

    private fun init() {
        isAdd = intent!!.getBooleanExtra(IS_ADD, true)
        if (isAdd) {
            binding.btnDeleteFaculty.hide()
            binding.btnUpdateFaculty.text = getString(R.string.add_faculty)
        } else {
            //if is update
            faculty = intent!!.getParcelableExtra(UPDATE_OBJ)!!
            binding.spinnerTeacherDepartments.hide()
            Picasso.get().load(faculty.image).into(binding.ivFacultyImage)
            binding.etFacultyName.setText(faculty.name)
            binding.etFacultyEmail.setText(faculty.email)
            binding.etFacultyPost.setText(faculty.post)
        }

        databaseReference = FirebaseDatabase.getInstance().reference.child(RTDB_FACULTY)
        storageReference = FirebaseStorage.getInstance().reference.child(RTDB_FACULTY)
        progressBar = this.progressBar(binding.linearLayout)
    }

    companion object {
        private const val GALLERY_REQ_CODE = 1
        const val RTDB_FACULTY = "Faculty"
        const val IS_ADD = "is_add"
        const val UPDATE_OBJ = "faculty"
    }
}