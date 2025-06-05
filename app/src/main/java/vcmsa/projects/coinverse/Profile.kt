package vcmsa.projects.coinverse

import android.content.Intent
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

class Profile : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile)

        // gets and displays username
        val currentUser = Firebase.auth.currentUser
        val welcomeText = findViewById<TextView>(R.id.tvWelcomeBack)

        currentUser?.let { user ->
            val username = user.displayName ?: user.email?.substringBefore("@") ?: "User"
            welcomeText.text = "Welcome back, $username!"
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left,0, systemBars.right, 0)
            insets
        }

        navigationBar()
        logout()
        updateBadges()
    }

    private fun updateBadges() {
        val totalSaved = getTotalSavings()
        val badgesEarned = (totalSaved / 1000).toInt().coerceAtMost(4) // 4 badges

        val badgeViews = listOf(
            findViewById<ImageView>(R.id.badge1),
            findViewById<ImageView>(R.id.badge2),
            findViewById<ImageView>(R.id.badge3),
            findViewById<ImageView>(R.id.badge4)
        )

        val badgeDrawables = listOf(
            R.drawable.badge_1,
            R.drawable.badge_2,
            R.drawable.badge_3,
            R.drawable.badge_4
        )
        // badge desc
        val badgeDescriptions = listOf(
            "Save R1,000.",
            "Save R2,000.",
            "Save R3,000.",
            "Save R4,000."
        )

        for (i in badgeViews.indices) {
            val badgeView = badgeViews[i]
            badgeView.setImageResource(badgeDrawables[i])

            val matrix = ColorMatrix()
            if (i < badgesEarned) {
                badgeView.colorFilter = null // full colour for earned
            } else {
                matrix.setSaturation(0f) // grayscale for locked
                badgeView.colorFilter = ColorMatrixColorFilter(matrix)
            }

            // toast messages for badges
            badgeView.setOnClickListener {
                val message = if (i < badgesEarned) {
                    "✓ You’ve earned this badge!\n${badgeDescriptions[i]}"
                } else {
                    "🔒 Locked badge\nHow to earn: ${badgeDescriptions[i]}"
                }
                Toast.makeText(this, message, Toast.LENGTH_LONG).show()
            }
        }
    }

    // temp hardcoded savings
    private fun getTotalSavings(): Double {
        // replace w firebase stuff later
        val savingsList = listOf(500.0, 850.0, 200.0, 700.0) // total = 2250
        return savingsList.sum()
    }

    //Logout
    private fun logout() {
        val logout = findViewById<Button>(R.id.btnLogout)

        logout.setOnClickListener {
            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton("OK") { dialog, _ ->
                    Firebase.auth.signOut()
                    Toast.makeText(this, "Signed out successfully!", Toast.LENGTH_SHORT).show()
                    //Sign out and redirect to Main
                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK //Effectively clears the backstack - prevents access to previous activities
                    startActivity(intent)
                    finish()
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }
                .setCancelable(false)
                .show()
//            Firebase.auth.signOut()
        }
    }

    //Nav Bar
    private fun navigationBar() {
        val homeLink = findViewById<ImageButton>(R.id.ivHome)
        val insightsLink = findViewById<ImageButton>(R.id.ivInsights)
        val addLink = findViewById<ImageButton>(R.id.ivAdd)
        val goalsLink = findViewById<ImageButton>(R.id.ivGoals)
        val profileLink = findViewById<ImageButton>(R.id.ivProfile)

        homeLink.setOnClickListener {
            val intent = Intent(this, ExpensesActivity::class.java)
            startActivity(intent)
        }

        insightsLink.setOnClickListener {
            val intent = Intent(this, Insights::class.java)
            startActivity(intent)
        }

        addLink.setOnClickListener {
            val intent = Intent(this, LogExpense::class.java)
            startActivity(intent)
        }

        goalsLink.setOnClickListener {
            val intent = Intent(this, BudgetGoals::class.java)
            startActivity(intent)
        }

        profileLink.setOnClickListener {
            val intent = Intent(this, Profile::class.java)
            startActivity(intent)
        }
    }
}