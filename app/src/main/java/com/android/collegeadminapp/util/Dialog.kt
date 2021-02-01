package com.android.collegeadminapp.util

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import com.android.collegeadminapp.R

class Dialog (private val activity: Activity) {

    private lateinit var progressDialog: AlertDialog

    fun showConfirmationDialog(
        title: String,
        message: String,
        isYes: (DialogInterface, Int) -> Unit
    ) {
        val builder = AlertDialog.Builder(activity)
        builder.apply {
            setTitle(title)
            setMessage(message)
            setIcon(android.R.drawable.ic_dialog_alert)
            setPositiveButton(android.R.string.yes,DialogInterface.OnClickListener(isYes))
            setNegativeButton(android.R.string.no) { dialog, _ ->
                dialog.dismiss()
            }
            setCancelable(false)
        }
        return builder.create().show()
    }


    fun showProgressDialog(){
        val builder = AlertDialog.Builder(activity)
        val inflater = activity.layoutInflater
        builder.setView(inflater.inflate(R.layout.custom_dialog,null))
        builder.setCancelable(false)

        progressDialog = builder.create()
        progressDialog.show()
    }

    fun hideProgressDialog() {
        progressDialog.dismiss()
    }

    fun showSpinner(spinnerItem: Array<String>, spinner: Spinner, itemClick: (String) -> Unit) {
        val adapter = ArrayAdapter(activity, android.R.layout.simple_spinner_item, spinnerItem).also {
            it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        spinner.apply {
            this.adapter = adapter
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    itemClick(spinnerItem[position])
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }
        }
    }
}

