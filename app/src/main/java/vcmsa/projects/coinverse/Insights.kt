package vcmsa.projects.coinverse

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat


class Insights : AppCompatActivity(), WeekSelectorFragment.WeekNavigationListener {

    private var currentWeek = 1
    private val totalWeeks = 4

    private val STATE_CURRENT_WEEK = "current_week_state"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_insights)

        // coming soon pop up
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Coming Soon!")
            .setMessage("This feature is still under development. Stay tuned :)")
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
                finish()
            }
            .setCancelable(false)
            .show()
        // end


        if (savedInstanceState != null) {
            currentWeek = savedInstanceState.getInt(STATE_CURRENT_WEEK, 1)
            Log.d("InsightsActivity", "Restored currentWeek: $currentWeek")
        }


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        if (savedInstanceState == null) {

            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, MonthFragment())
                .commit()


            updateWeekFragment()
        }

        navigationBar()
    }

    //Nav bar
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

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(STATE_CURRENT_WEEK, currentWeek)
        Log.d("InsightsActivity", "Saving currentWeek: $currentWeek")
    }



    override fun onPreviousWeekClicked() {
        Log.d("InsightsActivity", "onPreviousWeekClicked called")
        if (currentWeek > 1) {
            currentWeek--
            updateWeekFragment()
            // TODO: Add logic here to update chart/data based on the new week
            Log.d("InsightsActivity", "Navigated to week: $currentWeek")
        } else {
            Log.d("InsightsActivity", "Already at first week")
        }
    }

    override fun onNextWeekClicked() {
        Log.d("InsightsActivity", "onNextWeekClicked called")
        if (currentWeek < totalWeeks) {
            currentWeek++
            updateWeekFragment()
            // TODO: Add logic here to update chart/data based on the new week
            Log.d("InsightsActivity", "Navigated to week: $currentWeek")
        } else {
            Log.d("InsightsActivity", "Already at last week")
        }
    }


    private fun updateWeekFragment() {
        Log.d("InsightsActivity", "Updating week fragment to week: $currentWeek")
        val fragment = WeekSelectorFragment.newInstance(currentWeek, totalWeeks)
        supportFragmentManager.beginTransaction()
            .replace(R.id.weekSelectorFragmentContainer, fragment)
            .commit()
    }
}