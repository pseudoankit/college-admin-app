package com.android.collegeadminapp.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.databinding.DataBindingUtil
import com.android.collegeadminapp.R
import com.android.collegeadminapp.databinding.ActivityMainBinding
import com.android.collegeadminapp.ui.auth.LoginActivity
import com.android.collegeadminapp.ui.faculty.UpdateFacultyActivity
import com.android.collegeadminapp.ui.notice.DeleteNoticeActivity
import com.android.collegeadminapp.ui.notice.UploadNoticeActivity
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var auth : FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        auth = FirebaseAuth.getInstance()

        binding.layoutUploadNotice.setOnClickListener {
            startActivity(Intent(this, UploadNoticeActivity::class.java))
        }

        binding.layoutUploadImage.setOnClickListener {
            startActivity(Intent(this, UploadImageActivity::class.java))
        }

        binding.layoutUploadPdf.setOnClickListener {
            startActivity(Intent(this, UploadPdfActivity::class.java))
        }

        binding.layoutUpdateFaculty.setOnClickListener {
            startActivity(Intent(this, UpdateFacultyActivity::class.java))
        }
        binding.layoutDeleteNotice.setOnClickListener {
            startActivity(Intent(this, DeleteNoticeActivity::class.java))
        }
    }

    private fun openLoginActivity() {
        startActivity(Intent(this,LoginActivity::class.java))
        finish()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if(item.itemId == R.id.logout){
            auth.signOut()
            openLoginActivity()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.home_option_menu,menu!!)
        return super.onCreateOptionsMenu(menu)
    }
}