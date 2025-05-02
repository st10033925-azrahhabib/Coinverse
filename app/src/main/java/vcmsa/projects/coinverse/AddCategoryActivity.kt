package vcmsa.projects.coinverse

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import vcmsa.projects.coinverse.db.AppDatabase
import vcmsa.projects.coinverse.db.Category
import vcmsa.projects.coinverse.db.CategoryDao

class AddCategoryActivity : AppCompatActivity() {

    private lateinit var iconGrid: GridLayout
    private lateinit var categoryNameEditText: EditText
    private lateinit var cancelButton: Button
    private lateinit var confirmButton: Button

    // Room DAO instance
    private lateinit var categoryDao: CategoryDao

    private var selectedIconResId: Int? = null // Store the Resource ID directly

    // icon list
    private val icons = listOf(
        R.drawable.ic_book, R.drawable.ic_shopping, R.drawable.ic_movie,
        R.drawable.ic_coffee, R.drawable.ic_engagement, R.drawable.ic_transport,
        R.drawable.ic_laptop, R.drawable.ic_savings, R.drawable.ic_dog, R.drawable.ic_cat,
        R.drawable.ic_guitar, R.drawable.ic_health, R.drawable.ic_entertainment,
        R.drawable.ic_smoking, R.drawable.ic_tea, R.drawable.ic_music, R.drawable.ic_sports
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_category)

        // Initialize Room DAO
        // Get the singleton database instance and the DAO
        categoryDao = AppDatabase.getDatabase(applicationContext).categoryDao()
        iconGrid = findViewById(R.id.iconGrid)
        categoryNameEditText = findViewById(R.id.categoryNameEditText)
        cancelButton = findViewById(R.id.cancelButton)
        confirmButton = findViewById(R.id.confirmButton)

        populateIconGrid()

        cancelButton.setOnClickListener {
            finish() // closes activity
        }

        confirmButton.setOnClickListener {
            saveCategory() // Call the new save function
        }
    }

    private fun populateIconGrid() {
        icons.forEach { iconRes ->
            val iconView = ImageView(this).apply {
                setImageResource(iconRes)
                setPadding(25, 25, 25, 25)
                layoutParams = ViewGroup.LayoutParams(170, 170)
                setColorFilter(Color.LTGRAY)

                setOnClickListener {
                    selectedIconResId = iconRes // Store the selected resource ID

                    // Reset all icons to gray first
                    for (i in 0 until iconGrid.childCount) {
                        (iconGrid.getChildAt(i) as ImageView).setColorFilter(Color.LTGRAY)
                    }
                    // Highlight the selected icon
                    setColorFilter(Color.parseColor("#7A63AC")) // Use your theme color
                }
            }
            iconGrid.addView(iconView)
        }
    }

    private fun saveCategory() {
        val categoryName = categoryNameEditText.text.toString().trim()
        val currentSelectedIconResId = selectedIconResId // Capture the value locally

        if (categoryName.isEmpty()) {
            Toast.makeText(this, "Please enter a category name", Toast.LENGTH_SHORT).show()
            return // Exit if no name
        }

        if (currentSelectedIconResId == null) {
            Toast.makeText(this, "Please select an icon", Toast.LENGTH_SHORT).show()
            return // Exit if no icon selected
        }

        // Create the Category object to be saved
        val newCategory = Category(name = categoryName, iconResId = currentSelectedIconResId)

        // Launch a coroutine to perform the database insert off the main thread
        lifecycleScope.launch(Dispatchers.IO) { // Use IO dispatcher for database operations
            val result = categoryDao.insert(newCategory)

            // Switch back to the Main thread to update UI
            withContext(Dispatchers.Main) {
                if (result != -1L) {
                    // Insert successful
                    Toast.makeText(
                        this@AddCategoryActivity,
                        "Category '$categoryName' added successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                    val intent = Intent(this@AddCategoryActivity, LogExpense::class.java)
                    val intentless = Intent(this@AddCategoryActivity, ExpensesActivity::class.java)
                    intentless.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK //Effectively clears the backstack - prevents access to previous activities
                    startActivity(intentless) //adds home page to backstack - for cancellation
                    startActivity(intent) //refreshes the log expense
                    finish() // Close the activity
                } else {
                    Toast.makeText(
                        this@AddCategoryActivity,
                        "Category '$categoryName' already exists.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
    // Setup Navigation Bar
    private fun setupNavigationBar() {
        val homeImageButton = findViewById<ImageButton>(R.id.ivHome)
        val insightsImageButton = findViewById<ImageButton>(R.id.ivInsights)
        val addImageButton = findViewById<ImageButton>(R.id.ivAdd)
        val goalsImageButton = findViewById<ImageButton>(R.id.ivGoals)
        val profileImageButton = findViewById<ImageButton>(R.id.ivProfile)

        homeImageButton.setOnClickListener {
            val intent = Intent(this, ExpensesActivity::class.java)
            startActivity(intent)
        }

        insightsImageButton.setOnClickListener {
            val intent = Intent(this, Insights::class.java)
            startActivity(intent)
        }

        addImageButton.setOnClickListener {
            val intent = Intent(this, LogExpense::class.java)
            startActivity(intent)
        }

        goalsImageButton.setOnClickListener {
            val intent = Intent(this, BudgetGoals::class.java)
            startActivity(intent)
        }

        profileImageButton.setOnClickListener {
            val intent = Intent(this, Profile::class.java)
            startActivity(intent)
        }
    }
}