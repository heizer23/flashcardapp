package com.example.flashcardapp.main

import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

/**
 * Base activity that handles edge-to-edge window insets (required for targetSdk 35 / Android 15).
 * Applies system bar padding to the content view so nothing is hidden behind status/nav bars.
 */
abstract class BaseActivity : AppCompatActivity() {

    override fun setContentView(layoutResID: Int) {
        super.setContentView(layoutResID)
        applySystemBarInsets()
    }

    override fun setContentView(view: View) {
        super.setContentView(view)
        applySystemBarInsets()
    }

    private fun applySystemBarInsets() {
        val content = findViewById<View>(android.R.id.content)
        ViewCompat.setOnApplyWindowInsetsListener(content) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }
    }
}
