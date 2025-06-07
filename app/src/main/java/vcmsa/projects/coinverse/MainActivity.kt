package vcmsa.projects.coinverse

import android.graphics.Color
import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {

    private lateinit var loginTab: TextView
    private lateinit var registerTab: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        // Set theme before view is loaded
        val darkModePref = DarkModePref(this)
        if (darkModePref.isDarkModeEnabled()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        loginTab = findViewById(R.id.loginTab)
        registerTab = findViewById(R.id.registerTab)

        // Load default login fragment
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, LoginFragment())
            .commit()

        loginTab.setOnClickListener {
            loginTab.setTextColor(Color.WHITE)
            registerTab.setTextColor(Color.parseColor("#80FFFFFF"))

            supportFragmentManager.beginTransaction()
                .setCustomAnimations(
                    R.anim.slide_in_left,
                    R.anim.slide_out_right
                )
                .replace(R.id.fragmentContainer, LoginFragment())
                .commit()
        }

        registerTab.setOnClickListener {
            registerTab.setTextColor(Color.WHITE)
            loginTab.setTextColor(Color.parseColor("#80FFFFFF"))

            supportFragmentManager.beginTransaction()
                .setCustomAnimations(
                    R.anim.slide_in_right,
                    R.anim.slide_out_left
                )
                .replace(R.id.fragmentContainer, RegisterFragment())
                .commit()
        }
    }

}

