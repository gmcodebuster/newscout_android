package com.fafadiatech.newscout.customcomponent

import android.content.Context
import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.fafadiatech.newscout.R

class BaseAlertDialog {

    companion object {

        fun showAlertDialog(context: Context, message: String) {
            var alertDialog: AlertDialog = AlertDialog.Builder(context).create()
            alertDialog.setMessage(message)
            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", object : DialogInterface.OnClickListener {
                override fun onClick(p0: DialogInterface?, p1: Int) {
                    alertDialog.dismiss()
                }
            })

            alertDialog.setOnShowListener(object : DialogInterface.OnShowListener {
                override fun onShow(p0: DialogInterface?) {
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(context, R.color.colorPrimary))
                }
            })
            alertDialog.show()
        }
    }
}