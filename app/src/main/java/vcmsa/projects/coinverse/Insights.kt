package vcmsa.projects.coinverse

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Insights : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_insights)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, 0, systemBars.right, 0)
            insets
        }

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, MonthFragment())
                .commit()
        }

        navigationBar()
    }

    // nav bar
    private fun navigationBar() {
        val homeLink = findViewById<ImageButton>(R.id.ivHome)
        val insightsLink = findViewById<ImageButton>(R.id.ivInsights)
        val addLink = findViewById<ImageButton>(R.id.ivAdd)
        val goalsLink = findViewById<ImageButton>(R.id.ivGoals)
        val profileLink = findViewById<ImageButton>(R.id.ivProfile)

        homeLink.setOnClickListener {
            startActivity(Intent(this, ExpensesActivity::class.java))
        }

        insightsLink.setOnClickListener {
            startActivity(Intent(this, Insights::class.java))
        }

        addLink.setOnClickListener {
            startActivity(Intent(this, LogExpense::class.java))
        }

        goalsLink.setOnClickListener {
            startActivity(Intent(this, BudgetGoals::class.java))
        }

        profileLink.setOnClickListener {
            startActivity(Intent(this, Profile::class.java))
        }
    }
}
