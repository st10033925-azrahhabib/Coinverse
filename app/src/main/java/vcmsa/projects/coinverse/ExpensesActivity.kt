package vcmsa.projects.coinverse


import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import java.util.Calendar

class ExpensesActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var expensesRecyclerView: RecyclerView
    private lateinit var expenseAdapter: ExpenseAdapter

    private var currentMonth: Int = Calendar.getInstance().get(Calendar.MONTH)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_expenses)

        //updates the expenses based on the fragment
        val monthFragment = MonthFragment().apply {
            onMonthSelected = {monthIndex ->
                currentMonth = monthIndex
                fetchExpenses()
                Log.d("MonthFragment", "Month selected: $monthIndex")
            }
        }

        // adds the month fragment into the frame layout !
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, monthFragment)
            .commit()

        auth = Firebase.auth
        firestore = Firebase.firestore

        //configures rv
        expensesRecyclerView = findViewById(R.id.rvExpenses)
        expensesRecyclerView.layoutManager = LinearLayoutManager(this)

//        fetchExpenses()
        navigationBar()
    }

    //Gets and displays list of expenses per category
    private fun fetchExpenses() {
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

        Log.d("ExpensesDebug", "Fetching for month: $currentMonth")
        Log.d("ExpensesDebug", "Start date: $startDate")
        Log.d("ExpensesDebug", "End date: $endDate")

        if (userID != null) {
            firestore.collection("Expenses")
                .whereEqualTo("userId", userID)
                .whereGreaterThanOrEqualTo("date", startDate)
                .whereLessThanOrEqualTo("date", endDate)
                .orderBy("date")
                .get()
                .addOnSuccessListener { querySnapshot ->
                    val expenses = querySnapshot.toObjects(Expense::class.java)
                    aggregateExpenses(expenses)
                }
                .addOnFailureListener { e ->
                    Log.e("LogFailure","Error fetching expenses.")
                }
        }
    }

    //Calculates the total expenses for each category
    private fun aggregateExpenses(expenses: List<Expense>) {
        val categoryTotalsMap = mutableMapOf<String, Double>()

        for (expense in expenses) {
            val category = expense.category
            val amount = expense.amount
            categoryTotalsMap[category] = (categoryTotalsMap[category] ?: 0.0) + amount
        }

        val categoryTotals = categoryTotalsMap.map { (category, total) ->
            CategoryTotal(category, total)
        }.toList()

        expenseAdapter = ExpenseAdapter(categoryTotals, currentMonth)
        expensesRecyclerView.adapter = expenseAdapter
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
