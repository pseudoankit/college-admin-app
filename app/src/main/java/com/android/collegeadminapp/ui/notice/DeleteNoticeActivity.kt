package com.android.collegeadminapp.ui.notice

import android.content.DialogInterface
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.collegeadminapp.R
import com.android.collegeadminapp.databinding.ActivityDeleteNoticeBinding
import com.android.collegeadminapp.util.*
import com.android.collegeadminapp.util.FireBaseConstants.FB_NOTICE
import com.google.firebase.database.*

class DeleteNoticeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDeleteNoticeBinding
    private lateinit var databaseReference: DatabaseReference
    private lateinit var list: MutableList<Notice>
    private val adapter by lazy { NoticeAdapter() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_delete_notice)

        init()

        getNotice()
    }

    private fun getNotice() {
        binding.progressBar.show()
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                list = mutableListOf()
                if (!snapshot.exists()) {
                    binding.noNoticeFound.show()
                    binding.rvNotice.hide()
                } else {
                    binding.noNoticeFound.hide()
                    binding.rvNotice.show()
                    snapshot.children.forEach { snap ->
                        list.add(snap.getValue(Notice::class.java)!!)
                    }
                    setUpRv()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                this@DeleteNoticeActivity.toast(error.toString())
            }
        }
        databaseReference.addValueEventListener(listener)
    }

    private fun setUpRv() {
        binding.progressBar.hide()
        adapter.addItems(list)
        binding.rvNotice.setHasFixedSize(true)
        binding.rvNotice.layoutManager = LinearLayoutManager(this)
        binding.rvNotice.adapter = adapter

        adapter.listener = { _, notice, _ ->

            confirmationAlertDialog(
                getString(R.string.delete_notice),
                getString(R.string.dialog_delete_conformation)
            ).setPositiveButton(
                android.R.string.ok,
                DialogInterface.OnClickListener { _: DialogInterface, _: Int ->
                    databaseReference.child(notice.key).removeValue()
                        .addOnCompleteListener {
                            toast(getString(R.string.notice_deleted))
                        }.addOnFailureListener {
                            toast(getString(R.string.something_went_wrong))
                        }
                }).show()
        }
    }

    private fun init() {
        databaseReference = FirebaseDatabase.getInstance().reference.child(FB_NOTICE)
    }
}