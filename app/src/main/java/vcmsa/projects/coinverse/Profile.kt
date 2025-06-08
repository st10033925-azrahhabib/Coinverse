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
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SwitchCompat
import androidx.core.text.HtmlCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import vcmsa.projects.coinverse.db.AppDatabase
import vcmsa.projects.coinverse.firebase.CategoryFirestore
import vcmsa.projects.coinverse.repository.CategoryRepository

class Profile : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val darkModePref = DarkModePref(this) // load pref before UI
        if (darkModePref.isDarkModeEnabled()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        val darkModeSwitch = findViewById<SwitchCompat>(R.id.switchDarkMode)
        darkModeSwitch.isChecked = darkModePref.isDarkModeEnabled()

        darkModeSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                darkModePref.setDarkModeEnabled(true)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                darkModePref.setDarkModeEnabled(false)
            }
            recreate()
        }



        // gets and displays username + welcome back message
        val currentUser = Firebase.auth.currentUser
        val welcomeText = findViewById<TextView>(R.id.tvWelcomeBack)

        currentUser?.let { user ->
            val username = user.displayName ?: user.email?.substringBefore("@") ?: "User"
            welcomeText.text = HtmlCompat.fromHtml(
                "Welcome back, <b>$username</b>! \uD83D\uDC4B",
                HtmlCompat.FROM_HTML_MODE_LEGACY
            )
        }

        navigationBar()
        logout()
        updateBadges()
        setupChangePasswordButton()
        syncCategoryData()
    }

    //sync category to firebase
    private fun syncCategoryData() {
        // Initialize all the necessary components for the repository
        val categoryDao = AppDatabase.getDatabase(this).categoryDao()
        val firestoreHelper = CategoryFirestore()
        val repository = CategoryRepository(categoryDao, firestoreHelper)

        lifecycleScope.launch {
            try {
                // Call the main repository function to sync the data.
                repository.syncAllLocalCategoriesToFirestore()
                println("Category data sync completed successfully.")
            } catch (e: Exception) {
                // Handle potential errors
                println("Sync failed: ${e.message}")
                Toast.makeText(this@Profile, "Data sync failed.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    //change password
    private fun setupChangePasswordButton() {
        val changePasswordButton = findViewById<Button>(R.id.changePassword)

        changePasswordButton.setOnClickListener {
            ChangePasswordFragment.newInstance().show(supportFragmentManager, ChangePasswordFragment.TAG)
        }
    }

    private fun updateBadges() {
        val userId = Firebase.auth.currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        db.collection("Budget")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { querySnapshot ->
                var totalBudgeted = 0.0
                for (doc in querySnapshot) {
                    val amount = doc.getDouble("amount") ?: 0.0
                    totalBudgeted += amount
                }

                val badgesEarned = (totalBudgeted / 1000).toInt().coerceAtMost(4)

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

                val badgeDescriptions = listOf(
                    "You’ve created a budget totaling R1,000",
                    "You’ve smashed the R2,000 budget milestone",
                    "You’ve mastered budgeting up to R3,000",
                    "You’ve conquered the R4,000 budget mark"
                )

                val badgeNames = listOf(
                    "Bronze Budgeter",
                    "Silver Saver",
                    "Gold Goal Setter",
                    "Platinum Planner"
                )

                // iterates over badges and determines if earned or not
                for (i in badgeViews.indices) {
                    val badgeView = badgeViews[i]
                    val earned = i < badgesEarned

                    // sets the grayscale for locked badges
                    val matrix = ColorMatrix()
                    if (earned) {
                        badgeView.colorFilter = null
                    } else {
                        matrix.setSaturation(0f)
                        badgeView.colorFilter = ColorMatrixColorFilter(matrix)
                    }

                    badgeView.setImageResource(badgeDrawables[i])

                    badgeView.setOnClickListener {
                        showBadgeBottomSheet(
                            drawableRes = badgeDrawables[i],
                            badgeName = badgeNames[i],
                            description = badgeDescriptions[i],
                            isEarned = earned
                        )
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load budgets for badges", Toast.LENGTH_SHORT).show()
            }
    }

    // helper method to display badge details in a BottomSheetDialog.
    private fun showBadgeBottomSheet(
        drawableRes: Int,
        badgeName: String,
        description: String,
        isEarned: Boolean
    ) {
        val view = layoutInflater.inflate(R.layout.badge_description_bottom_sheet, null)
        val dialog = BottomSheetDialog(this)
        dialog.setContentView(view)

        val imageView = view.findViewById<ImageView>(R.id.ivBadgeImage)
        val nameView = view.findViewById<TextView>(R.id.tvBadgeName)
        val descriptionView = view.findViewById<TextView>(R.id.tvBadgeDescription)

        imageView.setImageResource(drawableRes)

        // show the badge name if earned and a locked title if not

        if (isEarned) {
            nameView.text = badgeName
        } else {
            nameView.text = "You haven't unlocked this badge yet"
        }

        descriptionView.text = description

        dialog.show()
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
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }
                .setCancelable(false)
                .show()
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
