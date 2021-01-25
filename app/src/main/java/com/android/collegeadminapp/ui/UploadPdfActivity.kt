package com.android.collegeadminapp.ui

import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.android.collegeadminapp.R
import com.android.collegeadminapp.databinding.ActivityUploadPdfBinding
import com.android.collegeadminapp.util.*
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.util.*
import kotlin.collections.HashMap

class UploadPdfActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUploadPdfBinding
    private lateinit var databaseReference: DatabaseReference       //Real time database reference
    private lateinit var storageReference: StorageReference        //storage reference
    private lateinit var progressBar: ProgressBar
    private var pdfData: Uri? = null
    private lateinit var pdfName: String
    private lateinit var pdfTitle: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_upload_pdf)

        init()

        binding.layoutSelectPdf.setOnClickListener { openGallery() }

        binding.btnUploadPdf.setOnClickListener { buttonUploadPdf() }
    }

    private fun buttonUploadPdf() {

        pdfTitle = binding.etPdfTitle.text!!.trim().toString()
        when {
            pdfData == null -> {
                toast("Please Select a pdf")
            }
            pdfTitle.isEmpty() -> {
                binding.etPdfTitle.error = getString(R.string.error_required)
                binding.etPdfTitle.requestFocus()
            }
            else -> {
                progressBar.show()
                lifecycleScope.launch { uploadPdf() }

            }
        }
    }

    private suspend fun uploadPdf() {
        //Todo - listener response coroutines
        val stReference = storageReference.child("pdf/${pdfName}-${System.currentTimeMillis()}.pdf")
        stReference.putFile(pdfData!!)
            .addOnSuccessListener {
                val uriTask = it.storage.downloadUrl
                while (!uriTask.isComplete) {
                    continue
                }
                val pdfUrl = uriTask.result.toString()
                Coroutines.io { uploadData(pdfUrl) }
            }
            .addOnFailureListener {
                progressBar.hide()
                toast("Something went wrong")
            }
    }

    private suspend fun uploadData(pdfUrl: String) {
        val uniqueKey = databaseReference.push().key
        val data: HashMap<String, String> = HashMap()
        data["pdfTitle"] = pdfTitle
        data["pdfUrl"] = pdfUrl
        databaseReference.child(uniqueKey!!).setValue(data)
            .addOnCompleteListener {
                progressBar.hide()
                toast("Pdf Uploaded Successfully")
            }.addOnFailureListener {
                progressBar.hide()
                toast("Failed to upload pdf")
            }
    }

    private fun openGallery() {
        Intent().apply {
            this.type = "application/pdf"
            this.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(Intent.createChooser(this, "Select pdf file"), REQ_CODE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQ_CODE && resultCode == RESULT_OK) {
            pdfData = data!!.data
            if (pdfData!!.toString().startsWith("content://")) {
                var cursor: Cursor? = null
                try {
                    cursor = this.contentResolver.query(pdfData!!, null, null, null, null)
                    if (cursor != null && cursor.moveToFirst()) {
                        pdfName =
                            cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                    }
                    cursor!!.close()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else if (pdfData!!.toString().startsWith("file://")) {
                pdfName = File(pdfData.toString()).name
            }
            binding.tvSelectedPdf.text = pdfName
        }
    }

    private fun init() {
        databaseReference = FirebaseDatabase.getInstance().reference.child("pdf")
        storageReference = FirebaseStorage.getInstance().reference
        progressBar = this.progressBar(binding.linearLayout)
    }

    companion object {
        private const val REQ_CODE = 1
    }
}