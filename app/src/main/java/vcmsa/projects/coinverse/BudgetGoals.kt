package vcmsa.projects.coinverse
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import java.util.Calendar

class BudgetGoals : AppCompatActivity() {

    private lateinit var addBudget: ImageView
    private lateinit var totalTV: TextView
    private lateinit var budgetTV: TextView
    private lateinit var budgetSB: SeekBar
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var budgetRecyclerView: RecyclerView
    private lateinit var budgetAdapter: BudgetAdapter
    private var availableBudget = 0

    private var currentMonth: Int = Calendar.getInstance().get(Calendar.MONTH)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_budget_goals)

        //updates the budgets based on the fragment
        val monthFragment = MonthFragment().apply {
            onMonthSelected = {monthIndex ->
                currentMonth = monthIndex
                calculateRemainingBudget()
                fetchBudgets()
                Log.d("MonthFragment", "Month selected: $monthIndex")
            }
        }

        // adds the month fragment into the frame layout !
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, monthFragment)
            .commit()

        auth = Firebase.auth
        firestore = Firebase.firestore
        totalTV = findViewById(R.id.budgetTotal)
        budgetTV = findViewById(R.id.tvAvailableBudgetValue)
        budgetSB = findViewById(R.id.seekBarBudget)

        //configures rv
        budgetRecyclerView = findViewById(R.id.recyclerBudgetCategories)
        budgetRecyclerView.layoutManager = LinearLayoutManager(this)

        //starts create budget page
        addBudget = findViewById(R.id.ivAddGoal)

        addBudget.setOnClickListener {
            val intent = Intent(this, CreateBudgetActivity::class.java)
            startActivity(intent)
        }

//        fetchBudgets()
        navigationBar()
    }

    //Gets and displays a list of budgets per category
    private fun fetchBudgets() {
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

        Log.d("BudgetDebug", "Fetching for month: $currentMonth")
        Log.d("BudgetDebug", "Start date: $startDate")
        Log.d("BudgetDebug", "End date: $endDate")

        if (userID != null) {
            firestore.collection("Budget")
                .whereEqualTo("userId", userID)
                .whereGreaterThanOrEqualTo("creationDate", startDate)
                .whereLessThanOrEqualTo("creationDate", endDate)
                .orderBy("creationDate")
                .get()
                .addOnSuccessListener { querySnapshot ->
                    val categoryTotals = mutableMapOf<String, Double>()
                    var totalBudget = 0.0

                    for (doc in querySnapshot) {
                        val category = doc.getString("category")
                        val amount = doc.getDouble("amount") ?: 0.0

                        if (category != null) {
                            categoryTotals[category] = (categoryTotals[category] ?: 0.0) + amount
                        }
                        totalBudget += amount
                    }

                    //update text fields
                    totalTV.text = "R ${String.format("%.2f", totalBudget)}"

                    val categoryTotalList = categoryTotals.map { (category, total) ->
                        BudgetCategoryTotal(category, total)
                    }.toList()

                    //update the recyclerView
                    budgetAdapter = BudgetAdapter(categoryTotalList, totalBudget)
                    budgetRecyclerView.adapter = budgetAdapter
                }
                .addOnFailureListener {e ->
                    Log.e("LogFailure","Error fetching budgets.")
                }
        }
    }

    private fun calculateRemainingBudget(){
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

        Log.d("AvailableBudget", "Fetching for month: $currentMonth")
        Log.d("AvailableBudget", "Start date: $startDate")
        Log.d("AvailableBudget", "End date: $endDate")

        var totalBudget = 0.0
        var totalExpenses = 0.0

        if (userID != null) {
            firestore.collection("Budget")
                .whereEqualTo("userId", userID)
                .whereGreaterThanOrEqualTo("creationDate", startDate)
                .whereLessThanOrEqualTo("creationDate", endDate)
                .orderBy("creationDate")
                .get()
                .addOnSuccessListener { budgetSnap ->
                    for (doc in budgetSnap.documents) {
                        val amount = doc.getDouble("amount") ?: 0.0
                        totalBudget += amount
                        Log.w("AvailableBudget", "Budget Success!")
                    }

                    firestore.collection("Expenses")
                        .whereEqualTo("userId", userID)
                        .whereGreaterThanOrEqualTo("date", startDate)
                        .whereLessThanOrEqualTo("date", endDate)
                        .orderBy("date")
                        .get()
                        .addOnSuccessListener { expenseSnap ->
                            for (doc in expenseSnap.documents) {
                                val amount = doc.getDouble("amount") ?: 0.0
                                totalExpenses += amount

                                Log.w("AvailableBudget", "Expense Success!")
                            }
                            var budgetValue = totalBudget - totalExpenses
                            availableBudget = budgetValue.toInt()

                            //update text fields and seek bar
                            budgetTV.text = "R ${String.format("%.2f", budgetValue)}"
                            budgetSB.max = totalBudget.toInt()
                            budgetSB.progress = availableBudget
                        }
                        .addOnFailureListener { Log.e("AvailableBudget", "Failed to fetch expenses.") }
                }
                .addOnFailureListener { Log.e("AvailableBudget", "Failed to fetch budgets. ") }
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

