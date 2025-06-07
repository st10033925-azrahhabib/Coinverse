package vcmsa.projects.coinverse

import android.content.Context

class DarkModePref(context: Context) {
    private val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)

    fun isDarkModeEnabled(): Boolean {
        return prefs.getBoolean("dark_mode", false)
    }

    fun setDarkModeEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("dark_mode", enabled).apply()
    }
}