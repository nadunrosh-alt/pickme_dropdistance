package com.example.dropdistance

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {

    private lateinit var tv: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32)
        }

        val info = TextView(this).apply {
            text = "1) Open Accessibility settings and turn ON 'PickMe Text Test'.\n" +
                "2) Wait for a PickMe order pop-up.\n" +
                "3) Come back here, press Refresh, then Copy and send the text."
        }

        val b1 = Button(this).apply {
            text = "Open Accessibility settings"
            setOnClickListener { startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) }
        }
        val b2 = Button(this).apply {
            text = "Refresh"
            setOnClickListener { refresh() }
        }
        val b3 = Button(this).apply {
            text = "Copy text"
            setOnClickListener {
                val cm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                cm.setPrimaryClip(ClipData.newPlainText("dump", tv.text))
                Toast.makeText(this@MainActivity, "Copied", Toast.LENGTH_SHORT).show()
            }
        }

        tv = TextView(this).apply {
            setTextIsSelectable(true)
            textSize = 13f
        }
        val sv = ScrollView(this).apply { addView(tv) }

        root.addView(info)
        root.addView(b1)
        root.addView(b2)
        root.addView(b3)
        root.addView(sv)
        setContentView(root)
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    private fun refresh() {
        tv.text = getSharedPreferences("dump", MODE_PRIVATE)
            .getString("last", "Nothing captured yet.")
    }
}
