package com.azavalacoria.testfiles.helpers

import android.content.Context
import android.widget.Toast

class ToastHelper {
    fun showMessage(context: Context , message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }
}