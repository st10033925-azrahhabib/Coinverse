package vcmsa.projects.coinverse

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
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

class CategoryDetailsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ListExpensesAdapter
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    private var currentMonth: Int = -1
    private var categoryName: String? = null
    private val expenseList = mutableListOf<Expense>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category_details)

        // back button
        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        btnBack.setOnClickListener {
            finish()
        }

        //Gets necessary details attached to the Intent
        categoryName = intent.getStringExtra("category")
        val categoryId = intent.getStringExtra("CATEGORY_ID")
        currentMonth = intent.getIntExtra("month", -1)

        //Sets the View Title
        val textView = findViewById<TextView>(R.id.textCategoryName)
        textView.text = "$categoryName"

        firestore = Firebase.firestore
        auth = Firebase.auth

        recyclerView = findViewById(R.id.category_rv)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = ListExpensesAdapter(expenseList)
        recyclerView.adapter = adapter

        if (currentMonth >= 0 && categoryName != null) {
            fetchCategoryExpenses()
        }

        navigationBar()
    }

    //Gets and displays list of expenses within a category
    private fun fetchCategoryExpenses() {
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

        Log.d("CategoryDetailsDebug", "Fetching for month: $currentMonth")
        Log.d("CategoryDetailsDebug", "Start date: $startDate")
        Log.d("CategoryDetailsDebug", "End date: $endDate")

        if (userID != null) {
            firestore.collection("Expenses")
                .whereEqualTo("userId", userID)
                .whereEqualTo("category", categoryName)
                .whereGreaterThanOrEqualTo("date", startDate)
                .whereLessThanOrEqualTo("date", endDate)
                .orderBy("date")
                .get()
                .addOnSuccessListener { querySnapshot ->
                    val fetched = querySnapshot.toObjects(Expense::class.java)
                    expenseList.clear()
                    expenseList.addAll(fetched)
                    adapter.notifyDataSetChanged()
                }
                .addOnFailureListener { e ->
                    Log.e("CategoryDetailsDebug", "Error fetching category $categoryName's expenses")
                }
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
