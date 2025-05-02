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
    private lateinit var weekTextView: TextView
    private lateinit var arrowLeft: ImageView
    private lateinit var arrowRight: ImageView
    private lateinit var expenseAdapter: ExpenseAdapter

    private var currentMonth: Int = Calendar.getInstance().get(Calendar.MONTH)
    private var currentWeek: Int = getCurrentWeek()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_expenses)

        // adds the month fragment into the frame layout !
//        supportFragmentManager.beginTransaction()
//            .replace(R.id.fragmentContainer, MonthFragment())
//            .commit()

        auth = Firebase.auth
        firestore = Firebase.firestore

        //configures rv
        expensesRecyclerView = findViewById(R.id.rvExpenses)
        expensesRecyclerView.layoutManager = LinearLayoutManager(this)

        //updateWeekDisplay()
        fetchExpenses()
        navigationBar()
    }

    //Get current week
    private fun getCurrentWeek(): Int {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.MONTH, currentMonth)
        return calendar.get(Calendar.WEEK_OF_MONTH)
    }

    //Updates week selector
    private fun updateWeekDisplay() {
        weekTextView.text = String.format("Week %d", currentWeek)
    }

    //Gets and displays list of expenses per category
    private fun fetchExpenses() {
        val userID = auth.currentUser?.uid

        if (userID != null) {
//            val calendar = Calendar.getInstance()
//
//            calendar.set(Calendar.MONTH, currentMonth)
//            calendar.set(Calendar.WEEK_OF_MONTH, currentWeek)
//            calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
//            val startDate = calendar.time
//
//            calendar.add(Calendar.DAY_OF_WEEK, 6)
//            val endDate = calendar.time

            firestore.collection("Expenses")
                .whereEqualTo("userId", userID)
//                .whereGreaterThanOrEqualTo("date", startDate)
//                .whereLessThanOrEqualTo("date", endDate)
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

        expenseAdapter = ExpenseAdapter(categoryTotals)
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
