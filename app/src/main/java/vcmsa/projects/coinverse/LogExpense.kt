package vcmsa.projects.coinverse

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.content.Intent
import android.widget.ImageButton

class LogExpense : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_log_expense)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        //Nav-bar
        val homeImageButton = findViewById<ImageButton>(R.id.ivHome)
        val insightsImageButton = findViewById<ImageButton>(R.id.ivInsights)
        val addImageButton = findViewById<ImageButton>(R.id.ivAdd)
        val goalsImageButton = findViewById<ImageButton>(R.id.ivGoals)
        val profileImageButton = findViewById<ImageButton>(R.id.ivProfile)

        homeImageButton.setOnClickListener{
            val intent = Intent(this, ExpensesActivity::class.java)
            startActivity(intent)
        }

        insightsImageButton.setOnClickListener{
            val intent = Intent(this, Insights::class.java)
            startActivity(intent)
        }

        addImageButton.setOnClickListener{
            val intent = Intent(this, LogExpense::class.java)
            startActivity(intent)
        }

        goalsImageButton.setOnClickListener{
            val intent = Intent(this, BudgetGoals::class.java)
            startActivity(intent)
        }

        profileImageButton.setOnClickListener{
            val intent = Intent(this, Profile::class.java)
            startActivity(intent)
        }
    }
}