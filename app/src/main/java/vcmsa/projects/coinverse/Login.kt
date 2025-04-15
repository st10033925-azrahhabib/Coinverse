package vcmsa.projects.coinverse

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Login : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val loginTab = findViewById<TextView>(R.id.loginTab)
        val registerTab = findViewById<TextView>(R.id.registerTab)
        val loginContainer = findViewById<View>(R.id.loginContainer)
        val registerContainer = findViewById<View>(R.id.registerContainer)

        // Set initial state: show login, hide register, and set colors for tabs
        loginContainer.visibility = View.VISIBLE
        registerContainer.visibility = View.GONE
        loginTab.setTextColor(Color.WHITE)
        registerTab.setTextColor(Color.parseColor("#80FFFFFF"))

        loginTab.setOnClickListener {
            // Show Login form and hide Register form
            loginContainer.visibility = View.VISIBLE
            registerContainer.visibility = View.GONE

            // Change tab text colors
            loginTab.setTextColor(Color.WHITE)
            registerTab.setTextColor(Color.parseColor("#80FFFFFF"))
        }

        registerTab.setOnClickListener {
            // Show Register form and hide Login form
            loginContainer.visibility = View.GONE
            registerContainer.visibility = View.VISIBLE

            // Change tab text colors
            registerTab.setTextColor(Color.WHITE)
            loginTab.setTextColor(Color.parseColor("#80FFFFFF"))
        }
    }
}
