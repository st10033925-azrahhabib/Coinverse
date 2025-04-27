package vcmsa.projects.coinverse

import android.graphics.Color
import android.os.Bundle
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import vcmsa.projects.coinverse.R

class AddCategoryActivity : AppCompatActivity() {

    private lateinit var iconGrid: GridLayout
    private lateinit var categoryNameEditText: EditText
    private lateinit var cancelButton: Button
    private lateinit var confirmButton: Button

    private var selectedIcon: Int? = null

    // icon list
    private val icons = listOf(
        R.drawable.ic_book,
        R.drawable.ic_shopping,
        R.drawable.ic_movie,
        R.drawable.ic_coffee,
        R.drawable.ic_engagement,
        R.drawable.ic_transport,
        R.drawable.ic_laptop,
        R.drawable.ic_savings,
        R.drawable.ic_dog,
        R.drawable.ic_cat,
        R.drawable.ic_guitar,
        R.drawable.ic_health,
        R.drawable.ic_entertainment,
        R.drawable.ic_savings,
        R.drawable.ic_smoking,
        R.drawable.ic_tea,
        R.drawable.ic_music,
        R.drawable.ic_sports
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_category)

        // view bind
        iconGrid = findViewById(R.id.iconGrid)
        categoryNameEditText = findViewById(R.id.categoryNameEditText)
        cancelButton = findViewById(R.id.cancelButton)
        confirmButton = findViewById(R.id.confirmButton)

        populateIconGrid()

        cancelButton.setOnClickListener {
            finish() // closes activity
        }

        confirmButton.setOnClickListener {
            val categoryName = categoryNameEditText.text.toString().trim()

            if (categoryName.isEmpty()) {
                Toast.makeText(this, "Please enter a category name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener // toast notif when there isnt a category name
            }

            if (selectedIcon == null) {
                Toast.makeText(this, "Please select an icon", Toast.LENGTH_SHORT).show()
                return@setOnClickListener // toast notif when there isnt an icon
            }

            // toast notif when category succesfully added
            Toast.makeText(this, "Category '$categoryName' added", Toast.LENGTH_SHORT).show()
            finish()
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
                    selectedIcon = iconRes
                    // rest icons to grey
                    for (i in 0 until iconGrid.childCount) {
                        (iconGrid.getChildAt(i) as ImageView).setColorFilter(Color.LTGRAY)
                    }
                    // icon shadow
                    setColorFilter(Color.parseColor("#7A63AC"))
                }
            }
            iconGrid.addView(iconView)
        }
    }
}
