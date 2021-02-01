package com.android.collegeadminapp.ui.auth

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.android.collegeadminapp.R
import com.android.collegeadminapp.ui.MainActivity
import com.android.collegeadminapp.util.toast
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        btn_login.setOnClickListener { validateUser() }
    }

    private fun validateUser() {
        val email = et_login_email.text.toString().trim()
        val password = et_login_password.text.toString().trim()

        when {
            email.isEmpty() -> {
                et_login_email.apply {
                    error = getString(R.string.error_required)
                    requestFocus()
                }
            }
            password.isEmpty() -> {
                et_login_password.apply {
                    error = getString(R.string.error_required)
                    requestFocus()
                }
            }
            else -> {
                performLogin(email,password)
            }
        }

    }

    private fun performLogin(email: String, password: String) {
        auth.signInWithEmailAndPassword(email,password)
            .addOnCompleteListener {
                if(it.isSuccessful) {
                    openMainActivity()
                } else {
                    toast(it.exception!!.message!!)
                }
            }.addOnFailureListener { toast(it.message!!) }
    }

    private fun openMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    override fun onStart() {
        super.onStart()
        if (auth.currentUser != null) openMainActivity()
    }
}


/**
 * Extension function to simplify setting an afterTextChanged action to EditText components.
 */
fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(editable: Editable?) {
            afterTextChanged.invoke(editable.toString())
        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
    })
}