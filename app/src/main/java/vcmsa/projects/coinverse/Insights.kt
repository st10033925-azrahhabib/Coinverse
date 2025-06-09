package vcmsa.projects.coinverse

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.github.mikephil.charting.charts.PieChart
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import android.graphics.Color
import android.util.Log
import android.widget.TextView
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.LegendEntry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import java.util.Calendar
import kotlin.math.exp
import kotlin.math.min

class Insights : AppCompatActivity() {

    private lateinit var pieChart: PieChart
    private lateinit var tExpense: TextView
    private lateinit var tBudgets: TextView
    private lateinit var avgExpense: TextView
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private var currentMonth: Int = Calendar.getInstance().get(Calendar.MONTH)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_insights)

        pieChart = findViewById(R.id.pieChart)
        setUpPieChart()

        //updates the budgets based on the fragment
        val monthFragment = MonthFragment().apply {
            onMonthSelected = {monthIndex ->
                currentMonth = monthIndex
                fetchPieChartData()
                Log.d("MonthFragment", "Month selected: $monthIndex")
            }
        }

        if (savedInstanceState == null) {
            // adds the month fragment into the frame layout !
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, monthFragment)
                .commit()
        }

        navigationBar()
    }

    private fun setUpPieChart() {
        pieChart.description.isEnabled = false
        pieChart.setDrawEntryLabels(false)
        pieChart.isDrawHoleEnabled = true
        pieChart.setEntryLabelColor(Color.BLACK)
        pieChart.setUsePercentValues(false)
        pieChart.setHoleColor(Color.TRANSPARENT)
        pieChart.animateY(800, Easing.EaseInOutQuad)
        pieChart.setHoleRadius(64f)

        //legend customization
        val legend = pieChart.legend
        legend.isEnabled = true
        legend.form = Legend.LegendForm.CIRCLE
        legend.textSize = 14f
        legend.textColor = Color.BLACK
        legend.formSize = 12f
        legend.xEntrySpace = 10f
        legend.yEntrySpace = 8f

        //center it horizontally at the top
        legend.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
        legend.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
        legend.orientation = Legend.LegendOrientation.HORIZONTAL
        legend.setDrawInside(false)
    }

    private fun updateValues(totalExpense: Double, totalBudget: Double, expenses: List<Expense>) {
        tExpense = findViewById(R.id.tvTotalExpensesValue1)
        tBudgets = findViewById(R.id.tvTotalExpensesValue2)
        avgExpense = findViewById(R.id.tvAverageExpensesValue)

        //calculate average
        val average = if (expenses.isNotEmpty()) {
            totalExpense / expenses.size
        }
        else { 0.0 }

        //setText
        tExpense.text = String.format("- R %.2f", totalExpense)
        tBudgets.text = String.format("R %.2f", totalBudget)
        avgExpense.text = String.format("R %.2f", average)
    }

    private fun fetchPieChartData() {
        val userID = auth.currentUser?.uid

        val calendar = Calendar.getInstance()

//            calendar.set(Calendar.YEAR, Calendar.getInstance().get(Calendar.YEAR)) // optional
        calendar.set(Calendar.MONTH, currentMonth)
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startDate = calendar.time

        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val endDate = calendar.time

        Log.d("InsightDebug", "Fetching for month: $currentMonth")
        Log.d("InsightDebug", "Start date: $startDate")
        Log.d("InsightDebug", "End date: $endDate")

        val budgetMap = mutableMapOf<String, Double>()
        val expenseMap = mutableMapOf<String, Double>()
        var totalBudget = 0.0
        var totalExpenses = 0.0
        val expenseList = mutableListOf<Expense>()

        if (userID != null) {
            firestore.collection("Budget")
                .whereEqualTo("userId", userID)
                .whereGreaterThanOrEqualTo("creationDate", startDate)
                .whereLessThanOrEqualTo("creationDate", endDate)
                .orderBy("creationDate")
                .get()
                .addOnSuccessListener { budgetSnap ->
                    for (doc in budgetSnap.documents) {
                        val category = doc.getString("category") ?: continue
                        val amount = doc.getDouble("amount") ?: 0.0
                        budgetMap[category] = budgetMap.getOrDefault(category, 0.0) + amount
                        totalBudget += amount
                        Log.w("InsightDebug", "Budget Success!")
                    }

                    firestore.collection("Expenses")
                        .whereEqualTo("userId", userID)
                        .whereGreaterThanOrEqualTo("date", startDate)
                        .whereLessThanOrEqualTo("date", endDate)
                        .orderBy("date")
                        .get()
                        .addOnSuccessListener { expenseSnap ->
                            for (doc in expenseSnap.documents) {
                                val category = doc.getString("category") ?: continue
                                val amount = doc.getDouble("amount") ?: 0.0
                                expenseMap[category] = expenseMap.getOrDefault(category, 0.0) + amount
                                totalExpenses += amount

                                Log.w("InsightDebug", "Expense Success!")

                                val expense = doc.toObject(Expense::class.java)
                                if(expense != null) expenseList.add(expense)
                            }

                            //Prepare Pie Participants
                            val allCategories = (budgetMap.keys + expenseMap.keys).toSet() //previously used for categories
                            val balance = totalBudget - totalExpenses
                            val pieEntries = mutableListOf<PieEntry>()

                            //pie logic
                            pieEntries.add(PieEntry(totalBudget.toFloat(), "Budgeted"))
                            pieEntries.add(PieEntry(min(totalExpenses, totalBudget).toFloat(), "Expenditure"))

                            if (totalExpenses > totalBudget) {
                                pieEntries.add(PieEntry((totalExpenses - totalBudget).toFloat(), "Overspent"))
                            }

                            val pieDataSet = PieDataSet(pieEntries, "")
                            pieDataSet.colors = listOf(
                                Color.parseColor("#D1C4E9"), //Light Purple (Budget)
                                Color.parseColor("#7E57C2"), //Dark Purple (Spent)
                                Color.parseColor("#E57373") //Red (Over)
                            )

                            pieDataSet.setDrawValues(false) //no values on slice
                            val pieData = PieData(pieDataSet)
                            pieChart.data = pieData
                            pieChart.setCenterText("Balance:\nR ${String.format("%.2f", balance)}")
                            pieChart.setCenterTextSize(16f)
                            pieChart.invalidate()

                            //Update text fields
                            updateValues(totalExpenses, totalBudget, expenseList)
                        }
                        .addOnFailureListener { Log.e("InsightsFirestore", "Failed to fetch expenses.") }
                }
                .addOnFailureListener { Log.e("InsightsFirestore", "Failed to fetch budgets. ") }
        }
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
