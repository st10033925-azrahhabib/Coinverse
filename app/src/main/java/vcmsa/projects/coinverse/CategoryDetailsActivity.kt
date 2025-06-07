package vcmsa.projects.coinverse

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class CategoryDetailsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category_details)

        val categoryName = intent.getStringExtra("CATEGORY_NAME")
        val categoryId = intent.getStringExtra("CATEGORY_ID")

        val textView = findViewById<TextView>(R.id.textCategoryName)
        textView.text = "Details for $categoryName"
    }
}
