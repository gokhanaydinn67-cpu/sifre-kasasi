package com.sifre.kasasi

import android.content.ClipData
import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.PersistableBundle
import android.widget.Toast

object ClipboardHelper {
    private val handler = Handler(Looper.getMainLooper())
    private var clearTask: Runnable? = null

    fun copy(context: Context, label: String, text: String) {
        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE)
            as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        if (Build.VERSION.SDK_INT >= 33) {
            clip.description.extras = PersistableBundle().apply {
                putBoolean(ClipDescription.EXTRA_IS_SENSITIVE, true)
            }
        }
        cm.setPrimaryClip(clip)
        Toast.makeText(
            context,
            "$label kopyalandı (30 sn sonra silinir)",
            Toast.LENGTH_SHORT
        ).show()

        clearTask?.let { handler.removeCallbacks(it) }
        val task = Runnable {
            try {
                if (Build.VERSION.SDK_INT >= 28) {
                    cm.clearPrimaryClip()
                } else {
                    cm.setPrimaryClip(ClipData.newPlainText("", ""))
                }
            } catch (e: Exception) {
            }
        }
        clearTask = task
        handler.postDelayed(task, 30000)
    }
}
