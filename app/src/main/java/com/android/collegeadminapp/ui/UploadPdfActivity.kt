package com.android.collegeadminapp.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
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
import kotlinx.coroutines.launch
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
                toast(getString(R.string.please_select_pdf))
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
        //uploads pdf to fb storage
        //Todo - listener response coroutines
        val storageFilePath = storageReference.child("${pdfName}-${System.currentTimeMillis()}.pdf")
        storageFilePath.putFile(pdfData!!)
            .addOnSuccessListener {
                val uriTask = it.storage.downloadUrl
                while (!uriTask.isComplete) {
                    continue
                }
                val pdfUrl = uriTask.result.toString()
                Coroutines.io { uploadPdfToRTDB(pdfUrl) }
            }
            .addOnFailureListener {
                progressBar.hide()
                toast(getString(R.string.something_went_wrong))
            }
    }

    private suspend fun uploadPdfToRTDB(pdfUrl: String) {
        val uniqueKey = databaseReference.push().key
        val data: HashMap<String, String> = HashMap()
        data[RTDB_PDF_TITLE] = pdfTitle
        data[RTDB_PDF_URL] = pdfUrl
        databaseReference.child(uniqueKey!!).setValue(data)
            .addOnCompleteListener {
                progressBar.hide()
                toast(getString(R.string.uploaded_successfully))
                finish()
            }.addOnFailureListener {
                progressBar.hide()
                toast(getString(R.string.failed_to_upload))
            }
    }

    private fun openGallery() {
        Intent().apply {
            this.type = "application/pdf"
            this.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(Intent.createChooser(this, getString(R.string.select_pdf)), PDF_REQ_CODE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PDF_REQ_CODE && resultCode == RESULT_OK) {
            pdfData = data!!.data
            pdfName = this.getPdfName(pdfData)

            binding.tvSelectedPdf.text = pdfName
        }
    }

    private fun init() {
        databaseReference = FirebaseDatabase.getInstance().reference.child(RTDB_PDF)
        storageReference = FirebaseStorage.getInstance().reference.child(RTDB_PDF)
        progressBar = this.progressBar(binding.linearLayout)
    }

    companion object {
        private const val RTDB_PDF = "Pdf"
        private const val PDF_REQ_CODE = 1
        private const val RTDB_PDF_TITLE = "Title"
        private const val RTDB_PDF_URL = "Url"
    }
}