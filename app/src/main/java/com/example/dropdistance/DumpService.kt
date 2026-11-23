package com.example.dropdistance

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DumpService : AccessibilityService() {

    private var lastRun = 0L
    private var lastSaved = ""

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val pkg = event?.packageName?.toString() ?: return
        if (pkg == packageName || pkg == "com.android.systemui") return

        val now = System.currentTimeMillis()
        if (now - lastRun < 300) return
        lastRun = now

        val roots = mutableListOf<AccessibilityNodeInfo>()
        try {
            windows?.forEach { w -> w.root?.let { roots.add(it) } }
        } catch (_: Exception) {}
        if (roots.isEmpty()) rootInActiveWindow?.let { roots.add(it) }

        val sb = StringBuilder()
        for (r in roots) {
            if (r.packageName?.toString() == packageName) continue
            sb.append("[package=").append(r.packageName).append("]\n")
            walk(r, 0, sb)
        }
        val text = sb.toString()

        // Only keep dumps that look like the order pop-up
        if (text.contains("accept", ignoreCase = true) && text != lastSaved) {
            lastSaved = text
            val time = SimpleDateFormat("HH:mm:ss", Locale.US).format(Date())
            getSharedPreferences("dump", MODE_PRIVATE).edit()
                .putString("last", "Captured at $time\n\n$text")
                .apply()
        }
    }

    private fun walk(node: AccessibilityNodeInfo?, depth: Int, sb: StringBuilder) {
        if (node == null) return
        val t = node.text?.toString()
        val d = node.contentDescription?.toString()
        if (!t.isNullOrBlank() || !d.isNullOrBlank()) {
            sb.append("  ".repeat(depth))
            if (!t.isNullOrBlank()) sb.append("text=\"").append(t).append("\" ")
            if (!d.isNullOrBlank()) sb.append("desc=\"").append(d).append("\" ")
            node.viewIdResourceName?.let { sb.append("id=").append(it) }
            sb.append("\n")
        }
        for (i in 0 until node.childCount) {
            walk(node.getChild(i), depth + 1, sb)
        }
    }

    override fun onInterrupt() {}
}
