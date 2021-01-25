package com.android.collegeadminapp.ui.faculty

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.android.collegeadminapp.R
import com.android.collegeadminapp.databinding.ActivityUpdateFacultyBinding

class UpdateFacultyActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUpdateFacultyBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_update_faculty)

        binding.fabUpdateFaculty.setOnClickListener {
            startActivity(Intent(this, AddFacultyActivity::class.java))
        }
    }
}